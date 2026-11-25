# Data Model: Marco 3 — Modo Docker

O esquema relacional não muda. A migração `V1__create_schema.sql` é a mesma no H2 e no PostgreSQL
(FR-004, research R3). O que muda são as regras de gravação e as entidades que passam a circular pelo
Kafka.

## Tabelas (sem mudança)

| Tabela | Chave | Restrições | Observação no Marco 3 |
| --- | --- | --- | --- |
| `movie` | `id` (identidade) | `uk_movie_year_title (release_year, title)` | Identidade lógica do filme. A atualização por evento usa essa chave (R5) |
| `producer` | `id` (identidade) | `uk_producer_name (name)` | Produtor sem vínculo pode existir depois de uma atualização. Não aparece em nenhuma resposta |
| `movie_producer` | `(movie_id, producer_id)` | FKs para `movie` e `producer` | Os vínculos de um filme são substituídos por inteiro numa atualização |

**Collation:** no PostgreSQL, o banco é criado com `--locale=C --encoding=UTF8` (R4). Isso é configuração
do banco, e não do esquema.

## Gravação de um filme (`MovieImportService.record`)

Entrada: um `MovieRow` já validado (ano, título sem espaços em volta, estúdios ou nulo, texto de
produtores, nomes separados e `winner`).

```text
filme = movie onde release_year = ano e title = título
se não existe:
    cria o filme com os valores da linha
senão:
    studios  ← linha.studios
    producers (texto) ← linha.producersRaw
    winner   ← linha.winner
    vínculos ← substituídos pelos produtores da linha
produtores da linha: findOrCreate pelo nome exato (FR-008 da spec 001)
```

| Situação | Resultado |
| --- | --- |
| Filme novo | Inserido |
| Mesmo filme, mesmos valores (evento repetido ou republicação) | Nenhuma mudança visível |
| Mesmo filme, valores diferentes | Atualizado: a última gravação prevalece (FR-014) |
| Duplicata dentro do mesmo CSV | Não chega ao serviço: o leitor a rejeita (FR-006d da spec 001) |

Transações:
- carga direta: uma transação para o arquivo inteiro, como no Marco 1;
- consumidor: uma transação por evento.

## Evento de filme

Tópico `awards.movies`, chave `"<ano>|<título>"`, valor JSON em UTF-8. O formato está em
[contracts/kafka-events.md](contracts/kafka-events.md).

| Campo | Tipo | Obrigatório | Regra (mesma da spec 001 quando existe no CSV) | Motivo de rejeição |
| --- | --- | --- | --- | --- |
| `type` | texto | não | ausente ou `movie` | `TYPE_UNKNOWN` |
| `year` | inteiro JSON | sim | 1 a 9999 | `YEAR_MISSING`, `YEAR_NOT_NUMERIC`, `YEAR_OUT_OF_RANGE` |
| `title` | texto | sim | não vazio depois do `trim`; a chave usa o título sem espaços em volta | `TITLE_MISSING` |
| `studios` | texto ou `null` | não | vazio ou ausente vira nulo | — |
| `producers` | texto | sim | separado pelo `ProducerNameParser`, com ao menos um nome | `PRODUCERS_MISSING` |
| `winner` | booleano JSON | sim | `true` ou `false` | `WINNER_INVALID` |
| (todos) | — | — | tamanho máximo: `title` ≤ 500, `studios` ≤ 1000, `producers` ≤ 1000 e cada nome de produtor ≤ 500 caracteres (`VARCHAR(500)` e `VARCHAR(1000)` da V1) | `FIELD_TOO_LONG` |
| `title`, `studios`, `producers` | — | — | nenhum caractere nulo (U+0000), que o PostgreSQL não aceita em texto. Um caractere nulo nas pontas é removido pelo `trim`, como no CSV, e não chega a ser rejeitado | `NUL_CHARACTER` |

**Validação comum aos dois caminhos.** As regras de conteúdo ficam numa única classe, a
`MovieFieldRules`, usada pelo leitor do CSV e pelo parser de eventos:
- ano de 1 a 9999;
- título não vazio;
- produtores com ao menos um nome;
- `FIELD_TOO_LONG`;
- `NUL_CHARACTER` (acrescentado em 2026-11-25, a partir de um apontamento do Codex no PR do Marco 3).

O `FIELD_TOO_LONG` passou a valer também para a linha do CSV (spec 001, FR-006 e data-model, motivo 8).
Sem ele, o banco recusaria a gravação. Na carga direta, a recusa interrompia a subida. No consumidor, ela
seria tratada como falha de infraestrutura e tentada de novo para sempre (R9). Com a mesma regra nos dois
caminhos, a carga direta e o Kafka continuam equivalentes, sem exceção (spec 003, FR-018 e SC-001).

Motivos que só existem no evento, porque vêm da representação JSON:

| Motivo | Quando |
| --- | --- |
| `NOT_UTF8` | Os bytes não são UTF-8 válido |
| `NOT_JSON` | O texto não é um objeto JSON |
| `TYPE_UNKNOWN` | `type` presente e diferente de `movie` e de `load-completed` |

**Ordem das verificações:** `NOT_UTF8`, `NOT_JSON`, `TYPE_UNKNOWN`, depois a mesma ordem da spec 001:
ano, título, produtores, `winner`, `FIELD_TOO_LONG` e `NUL_CHARACTER`. O primeiro motivo encontrado é o
registrado.

Motivo que vem do banco, depois da validação:

| Motivo | Quando |
| --- | --- |
| `DATABASE_REJECTED` | O banco recusou a gravação por dado inválido, com SQLState da classe 22 (dado) ou 23 (restrição de integridade). O detalhe traz o SQLState. É permanente: o evento vai ao tópico de erros, sem nova tentativa (FR-015a; research R9) |

Campos desconhecidos são ignorados (R7).

## Evento de carga concluída

Mesmo tópico, chave `"load-completed"`.

| Campo | Tipo | Regra |
| --- | --- | --- |
| `type` | texto | `load-completed` |
| `runId` | texto (UUID) | identifica a execução que publicou |
| `count` | inteiro ≥ 0 | filmes publicados nessa execução |

Um evento `load-completed` sem `runId` vai para o tópico de erros com o motivo `RUN_ID_MISSING`.

## Registro de erro

Tópico `awards.movies.errors`, mesma chave do evento original (ou nenhuma), com o valor igual aos
**bytes originais**.

| Cabeçalho | Conteúdo |
| --- | --- |
| `awards-error-reason` | código do motivo (tabelas acima) e a descrição legível, por exemplo `YEAR_OUT_OF_RANGE: ano fora da faixa de 1 a 9999` |
| `awards-error-at` | momento do recebimento, ISO-8601 em UTC |
| `awards-source-topic` | tópico de origem |
| `awards-source-partition` | partição de origem |
| `awards-source-offset` | offset de origem |

## Estado da ingestão (perfil `kafka`)

```text
LENDO ──(arquivo lido)──▶ AGUARDANDO_PUBLICAÇÃO ──(ApplicationReadyEvent)──▶ PUBLICANDO
   │                                                                              │
   └─(erro estrutural)─▶ subida interrompida                     (marcador enviado)
                                                                                  ▼
                              PRONTA ◀──(marcador com o runId desta execução consumido)── AGUARDANDO_CONSUMO
```

| Estado | Liveness | Readiness |
| --- | --- | --- |
| Lendo, aguardando publicação, publicando, aguardando consumo | 200 | 503 (`ingestion` em `OUT_OF_SERVICE`) |
| Pronta | 200 | 200 |
| Broker fora durante a publicação | 200 | 503, com novas tentativas (R8) |
| Evento novo ou inválido depois de pronta | 200 | 200 (FR-016) |

## Token de acesso

| Propriedade | Variável | Padrão | Efeito |
| --- | --- | --- | --- |
| `awards.security.token` | `API_TOKEN` | vazio | Vazio: autenticação desligada. Com valor: intervalos, listagem e métricas exigem `Authorization: Bearer <valor>` |

O valor nunca é registrado no log, nas métricas nem nas respostas (FR-025).

## Configuração por ambiente (novas variáveis)

| Variável | Perfil | Padrão | Uso |
| --- | --- | --- | --- |
| `SPRING_PROFILES_ACTIVE` | — | vazio | `docker` no compose |
| `DB_URL` | `postgres` | `jdbc:postgresql://postgres:5432/awards` | URL do banco |
| `DB_USER` | `postgres` | `awards` | usuário |
| `DB_PASSWORD` | `postgres` | **sem padrão na aplicação**: obrigatória no perfil. O compose a passa com a credencial de demonstração `${POSTGRES_PASSWORD:-awards-demo}`, e o job `postgres` do pipeline a passa vazia, com o service container em `trust` (Princípio VII 1.6.0: a credencial de demonstração só aparece nos arquivos do compose) | senha |
| `KAFKA_BOOTSTRAP_SERVERS` | `kafka` | `kafka:9092` | broker |
| `KAFKA_TOPIC_MOVIES` | `kafka` | `awards.movies` | tópico de filmes |
| `KAFKA_TOPIC_ERRORS` | `kafka` | `awards.movies.errors` | tópico de erros |
| `KAFKA_CONSUMER_GROUP` | `kafka` | `awards` | grupo do consumidor |
| `API_TOKEN` | todos | vazio | autenticação (acima) |

As variáveis do Marco 2 (`PORT`, `MOVIES_CSV_PATH` e `LOGGING_STRUCTURED_FORMAT_CONSOLE`) não mudam.
