# Golden Raspberry Awards API

## 1. Visão geral

API REST que lê o CSV de indicados e vencedores da categoria Pior Filme do Golden Raspberry Awards ao
subir. Ela informa os produtores com o **menor** e o **maior** intervalo entre duas vitórias
consecutivas e lista os filmes com paginação e filtros.

---

## 2. Avalie em 2 minutos

**Pré-requisito:** só o **JDK 25** no `PATH`. Não é preciso Maven, Docker nem banco de dados: o
wrapper baixa o Maven, e o banco é um H2 em memória.

| | Linux, macOS ou Git Bash | Windows (PowerShell ou cmd) |
| --- | --- | --- |
| Subir a aplicação | `./mvnw spring-boot:run` | `.\mvnw.cmd spring-boot:run` |
| Rodar os testes | `./mvnw verify` | `.\mvnw.cmd verify` |

A aplicação responde em `http://localhost:8080`. A carga do CSV termina antes de o servidor aceitar
conexões, e o log mostra o resumo:

```text
Carga do arquivo classpath:movielist.csv concluída: 206 linhas lidas, 206 aceitas, 0 rejeitadas
```

**Intervalos entre prêmios** (o requisito do enunciado):

```bash
curl -s http://localhost:8080/api/producers/intervals
```

```json
{"min":[{"producer":"Joel Silver","interval":1,"previousWin":1990,"followingWin":1991}],"max":[{"producer":"Matthew Vaughn","interval":13,"previousWin":2002,"followingWin":2015}]}
```

**Listagem de filmes** (vencedores de 1990):

```bash
curl -s "http://localhost:8080/api/movies?year=1990&winner=true"
```

```json
{"content":[{"year":1990,"title":"Ghosts Can't Do It","studios":"Triumph Releasing","producers":"Bo Derek","winner":true},{"year":1990,"title":"The Adventures of Ford Fairlane","studios":"20th Century Fox","producers":"Steven Perry and Joel Silver","winner":true}],"page":0,"size":20,"totalElements":2,"totalPages":1}
```

> No Windows PowerShell 5.1, `curl` é um apelido de `Invoke-WebRequest`. Use `curl.exe`, com os mesmos
> argumentos.

Documentação interativa: <http://localhost:8080/swagger-ui.html>.

---

## 3. Como executar

### Porta

A porta padrão é `8080` e muda pela variável `PORT`.

```bash
PORT=9090 ./mvnw spring-boot:run
```

```powershell
$env:PORT = "9090"; .\mvnw.cmd spring-boot:run
```

### Outro arquivo CSV

O padrão é o arquivo do enunciado, embarcado em `src/main/resources/movielist.csv`. Para usar outro,
defina `MOVIES_CSV_PATH`. O valor é um caminho no sistema de arquivos, absoluto ou relativo à raiz do
projeto, e os prefixos `file:` e `classpath:` também são aceitos.

```bash
MOVIES_CSV_PATH=src/test/resources/scenarios/disjoint-producers.csv ./mvnw spring-boot:run
```

```powershell
$env:MOVIES_CSV_PATH = "src\test\resources\scenarios\disjoint-producers.csv"; .\mvnw.cmd spring-boot:run
```

Com esse arquivo, os intervalos ficam assim:

```json
{"min":[{"producer":"Producer A","interval":2,"previousWin":1950,"followingWin":1952}],"max":[{"producer":"Producer B","interval":50,"previousWin":1925,"followingWin":1975}]}
```

No PowerShell, `$env:` vale para a sessão inteira. Remova a variável com
`Remove-Item Env:MOVIES_CSV_PATH` para voltar ao arquivo padrão.

### Arquivo inválido

| Situação | Comportamento |
| --- | --- |
| Arquivo ausente ou ilegível, separador diferente de `;` ou cabeçalho diferente de `year;title;studios;producers;winner` | A aplicação **não sobe**. O log mostra o bloco `APPLICATION FAILED TO START` com o arquivo, o motivo e a correção |
| Linha de dados inválida dentro de um arquivo válido | A linha é **ignorada**, com um aviso que cita o número da linha e o motivo. A carga continua |
| Variações inofensivas (BOM, linhas em branco, espaços em volta, maiúsculas no cabeçalho e no `yes`) | Aceitas sem aviso |

Exemplo, com as colunas do cabeçalho fora de ordem:

```bash
MOVIES_CSV_PATH=src/test/resources/scenarios/header-wrong-order.csv ./mvnw spring-boot:run
```

```text
APPLICATION FAILED TO START
***************************

Description:

Não foi possível carregar o arquivo de filmes.

    Arquivo: src/test/resources/scenarios/header-wrong-order.csv
    Motivo: INVALID_HEADER
    Detalhe: o cabeçalho esperado é "year;title;studios;producers;winner", nessa ordem, mas o encontrado é "title;year;studios;producers;winner"
    Cabeçalho encontrado: title;year;studios;producers;winner

Action:

Informe um arquivo CSV existente, em UTF-8, com o separador ";" e o cabeçalho "year;title;studios;producers;winner", nessa ordem. O caminho do arquivo é configurado pela variável de ambiente MOVIES_CSV_PATH (propriedade movies.csv.path).
```

Os motivos possíveis são `NOT_FOUND`, `UNREADABLE`, `INVALID_SEPARATOR` e `INVALID_HEADER`.

Com linhas inválidas (`src/test/resources/scenarios/invalid-lines.csv`), a aplicação sobe e o log
registra cada rejeição e o resumo:

```text
WARN ... Linha 4 rejeitada: número de colunas diferente de 5 (encontradas 4)
WARN ... Linha 8 rejeitada: ano não numérico (19x0)
WARN ... Linha 13 rejeitada: winner diferente de yes ou vazio (no)
...
Carga do arquivo src/test/resources/scenarios/invalid-lines.csv concluída: 11 linhas lidas, 2 aceitas, 9 rejeitadas
```

---

## 4. Endpoints

O contrato completo está em [`specs/001-producer-intervals-api/contracts/openapi.yaml`](specs/001-producer-intervals-api/contracts/openapi.yaml),
publicado em <http://localhost:8080/openapi.yaml> e navegável no Swagger UI, em
<http://localhost:8080/swagger-ui.html>.

| Método e caminho | O que faz |
| --- | --- |
| `GET /api/producers/intervals` | Produtores com o menor e o maior intervalo entre vitórias consecutivas |
| `GET /api/movies` | Lista paginada de filmes, com filtros |
| `GET /swagger-ui.html` | Documentação interativa |
| `GET /openapi.yaml` | Contrato OpenAPI 3.1 |

### `GET /api/producers/intervals`

Sem parâmetros. Sempre responde **200** com as listas `min` e `max`, no formato exato da página 2 do
enunciado. Cada item tem só `producer`, `interval`, `previousWin` e `followingWin`.

- Os empatados entram todos, ordenados por `previousWin` e depois pelo nome.
- Sem nenhum produtor com duas vitórias, a resposta é `{"min":[],"max":[]}`, também com 200.

O exemplo com o arquivo do enunciado está na [seção 2](#2-avalie-em-2-minutos).

### `GET /api/movies`

| Parâmetro | Valores aceitos | Padrão |
| --- | --- | --- |
| `year` | Ano exato, só dígitos (até 9) | sem filtro |
| `winner` | `true` ou `false`, sem diferenciar maiúsculas | sem filtro |
| `page` | Inteiro de 0 a 2147483647 | `0` |
| `size` | Inteiro de 1 a 100 | `20` |

- A ordem é sempre por ano e depois por título. Parâmetros desconhecidos são ignorados.
- `studios` vem `null` quando a coluna está vazia no arquivo. `producers` e `studios` trazem o texto
  como está no arquivo.
- Uma página além da última, ou um filtro sem resultado, responde 200 com `content` vazio e os totais
  reais.

```bash
curl -s "http://localhost:8080/api/movies?size=2"
```

```json
{"content":[{"year":1980,"title":"Can't Stop the Music","studios":"Associated Film Distribution","producers":"Allan Carr","winner":true},{"year":1980,"title":"Cruising","studios":"Lorimar Productions, United Artists","producers":"Jerry Weintraub","winner":false}],"page":0,"size":2,"totalElements":206,"totalPages":103}
```

Um parâmetro inválido responde **400** em Problem Details (RFC 9457), com `Content-Type:
application/problem+json`. A propriedade `invalidParams` lista todos os parâmetros inválidos da
consulta:

```bash
curl -s "http://localhost:8080/api/movies?page=-1&size=500"
```

```json
{"detail":"Parâmetro inválido na consulta","instance":"/api/movies","status":400,"title":"Bad Request","invalidParams":[{"name":"page","reason":"deve ser um inteiro maior ou igual a 0"},{"name":"size","reason":"deve ser um inteiro de 1 a 100"}]}
```

### Códigos HTTP

| Código | Quando |
| --- | --- |
| 200 | Consulta atendida, inclusive com listas vazias |
| 400 | Parâmetro inválido em `/api/movies`, em Problem Details |
| 404 | Caminho inexistente, em Problem Details |
| 405 | Verbo diferente de GET nos dois endpoints, em Problem Details e com o cabeçalho `Allow: GET` |

---

## 5. Testes

```bash
./mvnw verify                          # todos os testes (Linux, macOS ou Git Bash)
./mvnw verify -Dit.test=ReferenceFileIT # uma classe só
```

```powershell
.\mvnw.cmd verify
.\mvnw.cmd verify "-Dit.test=ReferenceFileIT"   # no PowerShell, o -D vai entre aspas
```

São **121 testes de integração**, executados pelo Maven Failsafe na fase `verify`, sem Docker. A
execução completa leva cerca de 30 s com as dependências já baixadas.

| Grupo (`src/test/java/com/outsera/awards/`) | O que cobre |
| --- | --- |
| `SmokeIT` | A aplicação sobe com o arquivo padrão |
| `ingestion/` | Carga do CSV: falhas de inicialização, variações inofensivas, BOM, linhas inválidas, duplicatas, estúdios vazios, resumo da carga e arquivos sem filmes válidos |
| `intervals/` | Cálculo e formato dos intervalos: resultado de referência, empates em `min` e `max`, três ou mais vitórias, linhas fora de ordem, vitórias no mesmo ano, separação de produtores, arquivo com anos diferentes dos do enunciado e um oráculo independente com arquivos gerados |
| `movies/` | Listagem: paginação, filtros, ordem, parâmetros inválidos e tempo de resposta abaixo de 1 s |
| `docs/` | As respostas reais dos dois endpoints validadas contra o `openapi.yaml`, mais o Swagger UI e o contrato publicado |
| `web/` | Verbos não suportados nos dois endpoints respondem 405 |
| `support/` | Base comum (`ScenarioIT`) e apoios, sem testes próprios |

**Como os testes funcionam:**
- Cada classe sobe a aplicação inteira com `@SpringBootTest` em porta aleatória e faz chamadas HTTP
  reais.
- Cada cenário tem o seu CSV em `src/test/resources/scenarios/`, escolhido pela propriedade
  `movies.csv.path`.
- Nos testes de falha de inicialização (`StartupFailureIT`), o contexto não chega a subir. Por isso,
  eles executam a aplicação real com `SpringApplication` e verificam a exceção e o log.

**Por que só testes de integração:** o item 4.2 do enunciado pede testes de integração, e o Princípio I
da [constituição](.specify/memory/constitution.md) proíbe testes unitários e mocks na API.
- Não há classe `*Test`, e o Mockito foi excluído do classpath de teste.
- Os testes verificam o comportamento pelo HTTP, como o avaliador o vê, e não detalhes internos.
- Log e banco só podem ser usados enquanto um comportamento não tem endpoint. Nenhum teste consulta o
  banco. O log só é verificado no que não tem endpoint: os avisos e o resumo da carga, a ordem entre a
  carga e o início do servidor e as falhas de inicialização.

---

## 6. Requisitos do enunciado

Cada item exigido pelo enunciado e como ele é comprovado: pelo teste de integração ou, nos itens de
entrega (4.4, 4.5, 5.1 e 5.2), pelo artefato correspondente. A matriz completa, requisito por
requisito da spec, está em
[`specs/001-producer-intervals-api/plan.md`](specs/001-producer-intervals-api/plan.md#rastreabilidade-requisito--teste).

| Item do enunciado | Requisitos da spec | Como é comprovado |
| --- | --- | --- |
| API RESTful no nível 2 de Richardson: recursos nomeados (`/api/producers/intervals`, `/api/movies`), verbo GET e códigos HTTP adequados (item 4.1) | FR-017, FR-018, FR-019, FR-023, FR-023a | **200:** `ReferenceFileIT`, `IntervalsShapeIT`, `MoviesListingIT`. **400** em Problem Details: `MoviesInvalidParamsIT`, `MoviesContractIT`. **404** em Problem Details para rota inexistente: `SmokeIT`. **405** para POST, PUT e DELETE nos dois endpoints, com `Allow: GET`: `HttpMethodsIT` |
| Carga do CSV na inicialização, antes de o servidor aceitar conexões | FR-001, FR-002 | `DefaultFileIT` |
| Formato de `min` e `max` (`producer`, `interval`, `previousWin`, `followingWin`), página 2 | FR-017, FR-025 | `IntervalsShapeIT`, `IntervalsContractIT` |
| Intervalo só entre vitórias consecutivas do mesmo produtor | FR-011, FR-012, FR-013, FR-016 | `ThreeOrMoreWinsIT`, `UnorderedLinesIT`, `SameYearWinsIT`, `WinnerVariantsIT` |
| Empates: todos os empatados em `min` e em `max` | FR-014, FR-015 | `TiesMinIT`, `TiesMaxIT`, `IntervalEdgeCasesIT` |
| Listas vazias quando nenhum produtor venceu duas vezes | FR-018 | `NoRepeatWinnersIT`, `IntervalsShapeIT`, `EmptyLoadIT` |
| Resultado de referência com o arquivo fornecido (Joel Silver em `min`, Matthew Vaughn em `max`) | SC-001 | `ReferenceFileIT` |
| Correto com outros arquivos, sem dados fixos no código | FR-016a, FR-016b, SC-004a | `DisjointProducersIT`, `RandomizedOracleIT` |
| Leitura da lista de indicados e vencedores (seção 1) | FR-019 a FR-023 | `MoviesListingIT`, `MoviesInvalidParamsIT`, `MoviesContractIT` |
| Somente testes de integração (item 4.2) | FR-027, Princípio I | Todos os testes são `*IT` com `@SpringBootTest` e sem mocks, executados pelo Failsafe. A exceção é o `StartupFailureIT`, que sobe a aplicação real com `SpringApplication` |
| Execução só com o wrapper, sem Docker e sem instalar banco: SGBD **H2 em memória**, embarcado na aplicação (item 4.3) | FR-026 | O H2 é dependência do `pom.xml`, e nenhuma URL de banco é configurada, então o Spring Boot cria um H2 em memória a cada execução. Todos os testes sobem a aplicação assim, e o `SmokeIT` confirma que ela sobe com o arquivo padrão. Além disso, `./mvnw verify` e `./mvnw spring-boot:run` foram executados em clone limpo, no Linux e no Windows |
| Documentação de como executar e testar (item 4.4) | — | Este README: [seção 2](#2-avalie-em-2-minutos) (avaliação rápida), [seção 3](#3-como-executar) (execução) e [seção 5](#5-testes) (testes) |
| Código em repositório git (item 4.5) | — | Este repositório, com um ou mais commits por bolt e a estrutura descrita na [seção 11](#11-estrutura-do-repositório) |
| Uso de IA no desenvolvimento (item 5.1) | — | Spec Kit, Claude Code e Codex, com o fluxo por bolts e os papéis descritos na [seção 10](#10-uso-de-ia) |
| Registro das interações com a IA (item 5.2) | — | [`docs/ai/README.md`](docs/ai/README.md): prompts com data e hora, sessões exportadas e decisões aceitas ou rejeitadas |

Cada método de teste tem um `@DisplayName` que começa pelos IDs dos requisitos que comprova (por
exemplo, `"FR-014 FR-015 empate no mínimo"`, no `TiesMinIT`).

---

## 7. Regras de negócio

As regras completas estão na [spec](specs/001-producer-intervals-api/spec.md).

### Arquivo e cabeçalho

- O separador é `;` e o cabeçalho é `year;title;studios;producers;winner`, **nessa ordem**. Os nomes das
  colunas não diferenciam maiúsculas (`Year;Title;...` é aceito). Ordem diferente, coluna faltando ou
  coluna a mais impedem a aplicação de subir.
- O arquivo é lido em UTF-8. O **BOM** no início é removido antes da leitura, então não atrapalha um
  cabeçalho entre aspas nem uma linha em branco logo depois dele.
- Linhas em branco, inclusive só com espaços, e espaços em volta dos campos são ignorados sem aviso.
- `winner` igual a `yes`, sem diferenciar maiúsculas e ignorando espaços, marca o vencedor. Vazio
  significa só indicado.

### Linhas inválidas e duplicadas

Uma linha inválida é ignorada, com um aviso que cita o número da linha física no arquivo (o cabeçalho
é a linha 1) e o motivo. A carga continua. Os motivos, verificados nesta ordem:

1. número de colunas diferente de 5;
2. ano vazio;
3. ano não numérico (aceita só dígitos, sem sinal);
4. título vazio;
5. produtores vazios, inclusive quando a separação não gera nenhum nome;
6. `winner` diferente de `yes` ou vazio;
7. **duplicata:** o filme é identificado por ano + título. A primeira ocorrência vale, e as repetições
   são ignoradas com aviso que cita a linha original.

A coluna de estúdios é opcional. Ao final, o log traz o resumo de linhas lidas, aceitas e rejeitadas.

### Separação dos nomes de produtores

- Os separadores são a vírgula, a palavra `and` e a combinação `, and`.
- O `and` só separa como palavra inteira, em minúsculas. `Robbinsand`, `Anderson` e `andrew` não são
  cortados.
- Vários `and` seguidos contam todos como separadores. Partes vazias são descartadas, e um nome
  repetido no mesmo filme conta uma vez.
- Os nomes são comparados **exatamente como escritos**, depois de tirar os espaços em volta. Não há
  correção de grafia, abreviação ou maiúsculas, e os espaços internos são preservados.

| Coluna `producers` | Nomes |
| --- | --- |
| `Eric Fellner, and Tom Hooper` | `Eric Fellner`, `Tom Hooper` |
| `Sid and Marty Krofft` | `Sid`, `Marty Krofft` |
| `Brian Robbinsand Sharla Sumpter Bridgett` | um nome só |
| `A, , B` | `A`, `B` |
| `Producer A and and Producer B` | `Producer A`, `Producer B` |
| `, and` | nenhum (linha rejeitada) |

### Intervalos e empates

- Só os filmes **vencedores** contam.
- Para cada produtor, os anos de vitória são ordenados, e cada par de vitórias **vizinhas** gera um
  intervalo (`followingWin - previousWin`). A primeira vitória nunca é comparada com a última quando há
  outras entre elas.
- Duas vitórias no mesmo ano, em filmes diferentes, dão intervalo 0.
- `min` traz **todos** os intervalos iguais ao menor valor, e `max` **todos** os iguais ao maior. Dentro
  de cada lista, a ordem é por `previousWin` e depois pelo nome (por código Unicode, sem depender do
  idioma da máquina).
- O resultado não depende da ordem das linhas no arquivo. Nada do resultado está fixo no código: ele
  vem só do arquivo carregado.

---

## 8. Arquitetura

### Pacotes (`src/main/java/com/outsera/awards/`)

| Pacote | Responsabilidade |
| --- | --- |
| `ingestion` | Leitura e validação do CSV (`MovieCsvReader`), separação dos produtores (`ProducerNameParser`), gravação (`MovieImportService`, o único caminho de gravação), carga na inicialização (`MovieFileLoader`) e mensagem de falha (`MovieFileFailureAnalyzer`) |
| `movie` | Entidade `Movie`, consulta paginada com filtros (`MovieRepository`, `MovieQueryService`) e `GET /api/movies` |
| `producer` | Entidade `Producer`, cálculo dos intervalos (`ProducerIntervalService`) e `GET /api/producers/intervals` |
| `web` | Erros em Problem Details (`ApiExceptionHandler`) |

### Fluxo da carga até a resposta

```mermaid
flowchart LR
    CSV["movielist.csv<br/>ou MOVIES_CSV_PATH"] --> Reader["MovieCsvReader<br/>BOM, cabeçalho, linhas"]
    Reader -->|linhas válidas| Parser["ProducerNameParser"]
    Reader -->|avisos e resumo| Log[(log)]
    Parser --> Import["MovieImportService"]
    Import --> H2[("H2 em memória<br/>esquema Flyway")]
    H2 --> Intervals["ProducerIntervalService"] --> E1["GET /api/producers/intervals"]
    H2 --> Query["MovieQueryService"] --> E2["GET /api/movies"]
```

1. O Flyway cria o esquema (`movie`, `producer` e `movie_producer`) no H2 em memória.
2. O `MovieFileLoader` roda depois de todos os beans estarem prontos e **antes** de o Tomcat aceitar
   conexões. Ele lê o arquivo, registra os avisos, grava os filmes e loga o resumo. Um erro estrutural
   interrompe a inicialização.
3. A cada requisição de intervalos, uma consulta JPQL traz os pares (produtor, ano) dos vencedores, e o
   serviço calcula os intervalos. Não há cache.
4. A listagem é uma consulta JPQL paginada com filtros opcionais. As consultas não usam SQL específico
   de banco.

### Stack

| Item | Versão |
| --- | --- |
| Java | 25 |
| Spring Boot | 4.1.2 (Spring Framework 7.0.10, Tomcat 11.0.27, Jackson 3.1.7) |
| Persistência | Spring Data JPA com Hibernate 7.4.13, H2 2.4.240 e Flyway 12.4.0 |
| CSV | Apache Commons CSV 1.14.1 |
| Documentação | Swagger UI 5.34.0 (webjar) sobre o `openapi.yaml` estático |
| Build | Maven 3.9.16 pelo Maven Wrapper 3.3.4 |
| Testes | JUnit 6.0.3, `RestTestClient` do Spring, Maven Failsafe e o validador OpenAPI da Atlassian 3.0.0 |

---

## 9. Decisões

As principais, em uma linha cada. O motivo e as alternativas de cada uma estão em
[`docs/ai/decisoes.md`](docs/ai/decisoes.md).

- **Linha inválida é ignorada com aviso**, em vez de rejeitar o arquivo inteiro. Só erros estruturais
  impedem a aplicação de subir.
- **Duplicata por ano + título:** a primeira ocorrência vale, para que uma linha vencedora repetida não
  gere um intervalo 0 falso.
- **Cabeçalho sem diferenciar maiúsculas, mas com ordem fixa.**
- **Separação de produtores em etapas, e não por uma regex**, depois de dois defeitos na regex
  apontados pelo Codex na revisão.
- **BOM removido antes do parser do CSV**, também após revisão do Codex.
- **Carga antes de o servidor aceitar conexões**, para que nenhuma consulta veja dados incompletos.
- **Cálculo dos intervalos a cada requisição, sem cache**, porque leva milissegundos.
- **Contrato estático em `/openapi.yaml` como fonte única:** ele alimenta o Swagger UI e valida as
  respostas nos testes. Não há contrato gerado pelo código.
- **Swagger UI pelo webjar, sem springdoc:** na versão 3.2.0, o springdoc só serve o Swagger UI com a
  geração automática do contrato ligada.
- **Página além da última responde 200 com a lista vazia**, inclusive quando o deslocamento passa do
  limite de `int`.
- **Mockito fora do classpath de teste**, para que a proibição de mocks seja garantida pelo compilador.

---

## 10. Uso de IA

O projeto foi feito com **Spec-Driven Development** pelo [GitHub Spec Kit](https://github.com/github/spec-kit),
no modo *spec-anchored*: a spec é a fonte da verdade e muda antes do código.

**Artefatos do Spec Kit:**
- a [constituição](.specify/memory/constitution.md), com sete princípios inegociáveis;
- a [spec](specs/001-producer-intervals-api/spec.md);
- o [plano](specs/001-producer-intervals-api/plan.md);
- a [research](specs/001-producer-intervals-api/research.md);
- as [tarefas](specs/001-producer-intervals-api/tasks.md).

As regras comuns a todos os agentes estão no [`AGENTS.md`](AGENTS.md).

**Fluxo por bolts:** o trabalho é dividido em ciclos curtos, os *bolts*, cada um num branch próprio.

1. O implementador escreve primeiro os testes de integração, que precisam falhar, e depois o código.
2. O responsável abre o pull request.
3. O revisor comenta no PR.
4. O implementador corrige só o que o responsável aceitar.
5. O merge acontece com os testes verdes, e a sessão é exportada.

PR, merge e tags são feitos só pelo responsável, nunca por um agente.

| Bolt | Entrega |
| --- | --- |
| 1 | Projeto Spring Boot e infraestrutura de teste |
| 2 | Carga do CSV |
| 3 | Cálculo dos intervalos |
| 4 | Endpoint de intervalos no formato do enunciado |
| 5 e 6 | Listagem, Swagger UI e fechamento, num único PR |

**Papéis:**

| Papel | Ferramenta | O que faz |
| --- | --- | --- |
| Implementador | Claude Code | Escreve testes e código de cada bolt |
| Revisor de pull request | Codex | Comenta cada PR e justifica os apontamentos, sem reescrever o código |
| Testador adversário | Codex | Cria CSVs de ataque a partir da spec, sem ler o código de produção. Cada falha vira teste permanente |
| Documentação | Claude Code, em sessão separada | README e guia da evidência de IA |

**Evidência:** cada mensagem enviada aos agentes é gravada com data e hora por um hook. As sessões são
exportadas por bolt, e as sugestões aceitas ou rejeitadas ficam registradas com o motivo. O guia de
leitura está em [`docs/ai/README.md`](docs/ai/README.md).

---

## 11. Estrutura do repositório

```text
.
├── README.md                    # este arquivo
├── AGENTS.md                    # regras comuns a todos os agentes de IA
├── CLAUDE.md                    # aponta o Claude Code para o AGENTS.md
├── pom.xml                      # build Maven (dependências e Failsafe)
├── mvnw, mvnw.cmd, .mvn/        # Maven Wrapper: dispensa instalar o Maven
├── .gitattributes               # fim de linha LF no repositório
├── .gitignore
├── .claude/                     # configuração do Claude Code: hook de prompts e skills do Spec Kit
├── .specify/                    # Spec Kit: constituição, templates e scripts
│   └── memory/constitution.md   # os sete princípios do projeto
├── specs/001-producer-intervals-api/
│   ├── spec.md                  # requisitos (FR/SC), cenários e casos de borda
│   ├── plan.md                  # plano técnico e matriz requisito → teste
│   ├── research.md              # decisões técnicas (R1 a R12)
│   ├── data-model.md            # tabelas, validação do arquivo e mensagens de log
│   ├── quickstart.md            # roteiro de validação de ponta a ponta
│   ├── tasks.md                 # tarefas agrupadas em bolts
│   ├── contracts/openapi.yaml   # contrato OpenAPI, fonte única
│   └── checklists/              # checklist de qualidade da spec
├── src/main/java/com/outsera/awards/
│   ├── AwardsApplication.java   # classe principal
│   ├── ingestion/               # leitura, validação e carga do CSV
│   ├── movie/                   # filmes e GET /api/movies
│   ├── producer/                # produtores e GET /api/producers/intervals
│   └── web/                     # Problem Details
├── src/main/resources/
│   ├── application.yml          # PORT e MOVIES_CSV_PATH, com padrões
│   ├── movielist.csv            # arquivo do enunciado (padrão)
│   ├── db/migration/            # esquema Flyway
│   ├── static/openapi.yaml      # cópia publicada do contrato
│   ├── static/swagger-ui.html   # página do Swagger UI
│   └── META-INF/spring.factories # registra a mensagem de falha da carga
├── src/test/java/com/outsera/awards/  # somente testes de integração (*IT)
│   ├── ingestion/  intervals/  movies/  docs/  web/  support/
│   └── SmokeIT.java
├── src/test/resources/scenarios/  # um CSV por cenário de teste
└── docs/ai/                     # evidência do uso de IA (ver docs/ai/README.md)
```
