# Contrato: eventos do Kafka

Vale para o perfil `kafka` (no compose, o grupo `docker`). Os nomes dos tópicos vêm de
`KAFKA_TOPIC_MOVIES` e `KAFKA_TOPIC_ERRORS` (data-model.md).

## Tópicos

| Tópico (padrão) | Partições | Quem escreve | Quem lê | Conteúdo |
| --- | --- | --- | --- | --- |
| `awards.movies` | 1 | o publicador da aplicação e qualquer cliente externo | o consumidor da aplicação (grupo `awards`) | eventos de filme e de carga concluída |
| `awards.movies.errors` | 1 | o consumidor da aplicação | pessoas e ferramentas de diagnóstico | eventos rejeitados, com os bytes originais |

A aplicação cria os dois tópicos se não existirem. A partição única garante que o evento de carga
concluída seja consumido depois dos filmes publicados antes dele (research R6).

## Evento de filme

- **Chave:** `"<ano>|<título>"` (recomendada; o consumidor não depende dela).
- **Cabeçalho `awards-run-id`:** o publicador da aplicação põe o `runId` da execução em cada evento. É
  informativo, o consumidor não depende dele e um cliente externo não precisa enviá-lo. Os testes usam o
  cabeçalho para descobrir o `runId` (research R10).
- **Valor:** um único objeto JSON em UTF-8, sem propriedade repetida e sem BOM antes dele. Duas raízes no
  mesmo valor, uma propriedade repetida ou um BOM inicial são `NOT_JSON`.

```json
{
  "year": 2020,
  "title": "Exemplo de Filme",
  "studios": "Estúdio A, Estúdio B",
  "producers": "Producer A, Producer B and Producer C",
  "winner": true
}
```

| Campo | Tipo | Obrigatório | Regra |
| --- | --- | --- | --- |
| `type` | string | não | ausente, `null` ou `"movie"` |
| `year` | integer | sim | 1 a 9999, escrito como inteiro JSON: texto, decimal (`2001.0`) e expoente (`2.001e3`) são `YEAR_NOT_NUMERIC` |
| `title` | string | sim | não vazio depois de retirar os espaços em volta; no máximo 500 |
| `studios` | string ou null | não | vazio equivale a ausente; no máximo 1000 |
| `producers` | string | sim | mesmo formato da coluna do CSV: vírgula, ` and ` e `, and` separam nomes (spec 001, FR-007 a FR-010); precisa gerar ao menos um nome; no máximo 1000 no texto e 500 em cada nome |
| `winner` | boolean | sim | `true` para vencedor, `false` para indicado |

Campos não listados são ignorados. `null` vale ausente em qualquer campo. `title`, `producers` ou
`studios` presente com outro tipo que string (número, array, objeto ou booleano) é `FIELD_TYPE_INVALID`.

- **Tamanhos:** contados em unidades UTF-16, como o H2 mede as colunas. Um emoji fora do plano básico
  conta 2, então um título tem no máximo 250 deles.
- **Espaços em volta:** os caracteres de U+0001 a U+0020. NBSP (U+00A0) e U+200B fazem parte do texto. Em volta do `and`, a tabulação e as quebras de linha também separam.
- **Caractere nulo:** U+0000 em qualquer posição de `title`, `studios` ou `producers`, inclusive nas
  pontas, é `NUL_CHARACTER`. Escrito sem escape dentro da string, ele torna o JSON inválido (`NOT_JSON`).

**Efeito:** o filme identificado por `year` + `title` é criado ou, se já existir, passa a ter os valores
do evento (spec 003, FR-014). Receber o mesmo evento de novo não muda nada.

## Evento de carga concluída

- **Chave:** `"load-completed"`.
- **Valor:**

```json
{ "type": "load-completed", "runId": "3f0c2a8e-6d1b-4a59-9a63-0f7e1f4d2b10", "count": 206 }
```

| Campo | Tipo | Regra |
| --- | --- | --- |
| `type` | string | `"load-completed"` |
| `runId` | string | não vazio; sem ele, `RUN_ID_MISSING` |
| `count` | integer | obrigatório, inteiro JSON ≥ 0; ausente ou de outro tipo, `COUNT_INVALID`. O valor é informativo |

Só a aplicação que gerou o `runId` muda de estado ao consumi-lo: a readiness passa a 200 (FR-013). Um
evento com outro `runId` é ignorado, sem ir ao tópico de erros. Um evento rejeitado (`RUN_ID_MISSING` ou
`COUNT_INVALID`) não libera a readiness. Um cliente externo não tem motivo para enviar este evento.

**Marcador antecipado.** O marcador vale pelo `runId`. O `count` não é conferido contra os filmes
gravados. O publicador nunca antecipa o marcador: o tópico tem uma partição, e ele envia cada evento e
espera a confirmação antes do próximo. Um marcador com o `runId` certo que chega antes do último filme da
carga só acontece com **escrita indevida no tópico**. O `runId` aparece no cabeçalho `awards-run-id` de
todo evento do publicador, então quem lê o tópico pode copiá-lo. Nesse caso, a readiness fica positiva
antes de a carga terminar.

- **A readiness é um sinal operacional, e não uma barreira de segurança.** Ela diz ao orquestrador quando
  mandar tráfego. Ela não protege os dados: quem escreve no tópico já pode gravar qualquer filme.
- **A proteção do tópico é responsabilidade do broker,** com ACLs que limitem quem escreve em
  `awards.movies`. Isso fica fora do escopo deste projeto. O compose de desenvolvimento roda o broker sem
  autenticação.

Comprovação: o `KafkaCase25EarlyMarkerIT` fixa esse comportamento (spec 004, FR-035).

## Registro no tópico de erros

- **Chave:** a do evento original.
- **Valor:** os bytes originais, sem mudança.
- **Cabeçalhos:**

| Cabeçalho | Exemplo |
| --- | --- |
| `awards-error-reason` | `YEAR_OUT_OF_RANGE: ano fora da faixa de 1 a 9999` |
| `awards-error-at` | `2026-11-24T18:03:11.402Z` |
| `awards-source-topic` | `awards.movies` |
| `awards-source-partition` | `0` |
| `awards-source-offset` | `207` |

**Motivos:** `NOT_UTF8`, `NOT_JSON`, `TYPE_UNKNOWN`, `RUN_ID_MISSING`, `COUNT_INVALID`,
`FIELD_TYPE_INVALID`, `FIELD_TOO_LONG`, `NUL_CHARACTER`, `YEAR_MISSING`, `YEAR_NOT_NUMERIC`,
`YEAR_OUT_OF_RANGE`, `TITLE_MISSING`, `PRODUCERS_MISSING`, `WINNER_INVALID` e `DATABASE_REJECTED` (recusa
permanente do banco, com o SQLState; data-model.md). `COUNT_INVALID` e `FIELD_TYPE_INVALID` entraram em
2026-11-25, com o testador adversário (spec 004, FR-035).

## Garantias

| Garantia | Como |
| --- | --- |
| Nenhuma duplicata | Gravação por ano + título, com atualização (research R5) |
| Evento inválido não trava o consumo | Vai ao tópico de erros e o offset avança (R9) |
| Falha de banco não perde evento | Novas tentativas sem avançar o offset (R9) |
| Readiness só com a carga completa | Indicador `ingestion` liberado pelo `runId` desta execução (R10) |

## Exemplo de uso no compose

Pelo serviço `kafka-cli` (perfil `tools` do compose):

```bash
printf '%s\n' '2020|Exemplo de Filme:{"year":2020,"title":"Exemplo de Filme","studios":"Estúdio A","producers":"Joel Silver","winner":true}' \
  | docker compose run --rm -T kafka-cli /opt/kafka/bin/kafka-console-producer.sh \
      --bootstrap-server kafka:9092 --topic awards.movies \
      --property parse.key=true --property key.separator=:

docker compose run --rm kafka-cli /opt/kafka/bin/kafka-console-consumer.sh \
  --bootstrap-server kafka:9092 --topic awards.movies.errors --from-beginning \
  --property print.headers=true --timeout-ms 5000
```
