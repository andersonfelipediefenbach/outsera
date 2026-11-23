# Implementation Plan: Marco 1 — Carga do CSV, intervalos de produtores e listagem de filmes

**Branch**: `001-producer-intervals-api` | **Date**: 2026-11-18 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/specs/001-producer-intervals-api/spec.md`

## Summary

Este plano cria do zero uma API REST em Spring Boot 4.1.2 sobre Java 25, que carrega o CSV de filmes no
H2 durante a inicialização, antes de o servidor aceitar conexões.

- **Carga:** o esquema é versionado por Flyway e a leitura usa Commons CSV.
- **Tolerância:** variações inofensivas são aceitas, as linhas inválidas são rejeitadas com aviso e
  resumo, e os erros estruturais interrompem a inicialização com uma mensagem clara.
- **Endpoints:**
  - `GET /api/producers/intervals`, no formato exato do enunciado, com o cálculo por pares de vitórias
    vizinhas feito em um serviço;
  - `GET /api/movies`, com paginação e filtros validados à mão.
- **Contrato:** o `openapi.yaml` é escrito antes do código e é a fonte única do Swagger UI (webjar, R8) e
  da validação das respostas nos testes.
- **Testes:** só de integração (`*IT` via Failsafe), com `@SpringBootTest` em porta aleatória, chamadas
  HTTP reais e um CSV de cenário por classe.

## Technical Context

**Language/Version**: Java 25 (OpenJDK 25.0.2)

**Primary Dependencies**: Spring Boot 4.1.2 (`spring-boot-starter-webmvc`, `-data-jpa`, `-flyway`),
H2, Apache Commons CSV 1.14.1, `org.webjars:swagger-ui` 5.34.0 com `webjars-locator-lite` (trocou o
springdoc 3.2.0 no bolt 6, R8). Nos testes:
`spring-boot-starter-test`, `swagger-request-validator-core` 3.0.0.

**Storage**: H2 em memória, com nome único por contexto e esquema via Flyway (`ddl-auto=validate`)

**Testing**: JUnit 5 + `@SpringBootTest(webEnvironment = RANDOM_PORT)` + `RestTestClient`, executados pelo
maven-failsafe-plugin em `./mvnw verify`, mais `OutputCaptureExtension` para os logs e `JdbcTemplate` para as
verificações de banco dos testes de carga, enquanto não há endpoint (Princípio I). No bolt 5, essas
verificações passaram a ser feitas por `GET /api/movies`, e o `JdbcTemplate` saiu dos testes

**Target Platform**: JVM 25 em qualquer sistema operacional (Windows, Linux e macOS), via Maven Wrapper
3.3.4 e Maven 3.9.16

**Project Type**: web-service (API REST, módulo Maven único)

**Performance Goals**: resposta abaixo de 1 s nos dois endpoints com o arquivo fornecido (SC-005); na
prática, alguns milissegundos para cerca de 200 linhas

**Constraints**: subir e testar só com `./mvnw` (sem Docker); contrato de intervalos imutável; nenhum
dado fixo no código; configuração por variável de ambiente (`MOVIES_CSV_PATH`, `PORT`)

**Scale/Scope**: arquivos da ordem de centenas a poucos milhares de linhas; 2 endpoints de negócio e
1 de documentação

Nenhum item ficou como NEEDS CLARIFICATION. Todos foram resolvidos em [research.md](research.md).

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Princípio | Como o plano atende | Pré-design | Pós-design |
| --- | --- | --- | --- |
| I. Somente testes de integração | Só `*IT` com `@SpringBootTest`, contexto completo e nenhum mock. A separação de nomes e o cálculo são testados por CSVs de cenário via HTTP, nunca isolados. Os testes de carga do bolt 2 verificam log e banco até ganharem HTTP nos bolts 3 e 5. As falhas de inicialização usam `SpringApplication` real, pela cláusula própria do princípio (constituição 1.2.0) | ✅ | ✅ |
| II. Execução só com `./mvnw` | Wrapper versionado; H2 em memória; nenhuma dependência de Docker ou de serviço externo | ✅ | ✅ |
| III. Contrato idêntico ao enunciado | `ProducerInterval` só com os 4 campos e `additionalProperties: false` no contrato; os testes validam as respostas contra o `openapi.yaml` | ✅ | ✅ |
| IV. Spec-anchored | O plano mudou a spec antes do código (caminho `/openapi.yaml`); os pontos em aberto da clarificação foram resolvidos em R3 e R6 e serão refletidos na spec se o comportamento mudar | ✅ | ✅ |
| V. Extras isolados e simplicidade | Sem Kafka, PostgreSQL ou autenticação neste marco; serviço único de gravação (`MovieImportService`); sem cache nem camadas extras | ✅ | ✅ |
| VI. Rastreabilidade | `@DisplayName` com os IDs FR/SC em cada teste, mais a matriz abaixo | ✅ | ✅ |
| VII. Sem segredos; config por ambiente | Nenhum segredo neste marco; `MOVIES_CSV_PATH` e `PORT` vêm do ambiente, com padrões funcionais | ✅ | ✅ |

**Resultado:** nenhuma violação, e o "Complexity Tracking" não se aplica.

## Project Structure

### Documentation (this feature)

```text
specs/001-producer-intervals-api/
├── plan.md              # Este arquivo
├── research.md          # Fase 0
├── data-model.md        # Fase 1
├── quickstart.md        # Fase 1
├── contracts/
│   └── openapi.yaml     # Fase 1: fonte do contrato (copiado para src/main/resources/static)
├── checklists/
│   └── requirements.md
└── tasks.md             # Fase 2 (/speckit-tasks)
```

### Source Code (repository root)

```text
pom.xml
mvnw, mvnw.cmd, .mvn/wrapper/maven-wrapper.properties
src/main/java/com/outsera/awards/
├── AwardsApplication.java
├── movie/
│   ├── Movie.java                    # entidade JPA
│   ├── MovieRepository.java          # JPQL: filtros da listagem
│   ├── MovieController.java          # GET /api/movies
│   ├── MovieQueryService.java        # validação de parâmetros + paginação
│   └── MoviePage.java, MovieItem.java
├── producer/
│   ├── Producer.java                 # entidade JPA
│   ├── ProducerRepository.java       # JPQL: pares (nome, ano) dos vencedores (ProducerWin)
│   ├── ProducerIntervalService.java  # cálculo FR-011..FR-016
│   ├── ProducerIntervalController.java  # GET /api/producers/intervals
│   └── ProducerInterval.java, ProducerIntervalsResponse.java, ProducerWin.java
├── ingestion/
│   ├── MovieFileProperties.java      # movies.csv.path (MOVIES_CSV_PATH)
│   ├── MovieCsvReader.java           # cabeçalho, BOM, validação de linha, LoadReport
│   ├── ProducerNameParser.java       # FR-007..FR-010
│   ├── MovieImportService.java       # serviço único de gravação (Princípio V)
│   ├── MovieFileLoader.java          # SmartInitializingSingleton: carga antes do servidor
│   ├── MovieFileException.java, MovieFileFailureAnalyzer.java
│   └── LoadReport.java, Rejection.java, RejectionReason.java, MovieRow.java
└── web/
    ├── ApiExceptionHandler.java      # ProblemDetail (RFC 9457)
    └── InvalidParameterException.java
src/main/resources/
├── application.yml
├── movielist.csv                     # arquivo do enunciado (a ser adicionado)
├── db/migration/V1__create_schema.sql
├── static/openapi.yaml               # cópia de contracts/openapi.yaml
├── static/swagger-ui.html            # Swagger UI (webjar) sobre o /openapi.yaml
└── META-INF/spring.factories         # FailureAnalyzer
src/test/java/com/outsera/awards/     # somente *IT (Failsafe)
├── SmokeIT
├── support/     ScenarioIT (base), ApplicationStarter, LoaderLog, IntervalItem, IntervalsBody,
│                ContractValidator, IntervalOracle, MovieJson, MoviesBody
├── ingestion/   StartupFailureIT, HeaderCaseIT, HarmlessVariationsIT, BomVariationsIT, InvalidLinesIT,
│                LoadSummaryIT, OptionalStudiosIT, DuplicateLinesIT, EmptyLoadIT, DefaultFileIT
├── intervals/   ReferenceFileIT, IntervalsShapeIT, TiesMinIT, TiesMaxIT, ThreeOrMoreWinsIT,
│                UnorderedLinesIT, SameYearWinsIT, NoRepeatWinnersIT, DisjointProducersIT,
│                DuplicateWinnerIT, ProducerSeparatorsIT, WinnerVariantsIT, InvalidLineIgnoredIT,
│                IntervalEdgeCasesIT, RandomizedOracleIT
├── movies/      MoviesListingIT, MoviesInvalidParamsIT, ResponseTimeIT
├── docs/        IntervalsContractIT, MoviesContractIT, ApiDocsIT
└── web/         HttpMethodsIT
src/test/resources/scenarios/*.csv    # um CSV por cenário (o RandomizedOracleIT gera os seus)
```

**Structure Decision**: módulo Maven único na raiz, organizado por funcionalidade (`movie`, `producer`,
`ingestion`, `web`). Assim, o Marco 3 troca só a origem dos dados (`ingestion`) sem tocar no cálculo nem
nos endpoints. Não há `frontend/` neste marco. O diretório vazio `src/main/java/org/example` será
removido.

## Rastreabilidade (requisito → teste)

| Requisitos | Teste(s) de integração |
| --- | --- |
| FR-001 | `DefaultFileIT` (carga do padrão e ordem carga → servidor) |
| FR-002 | `DefaultFileIT` (padrão), `DisjointProducersIT` (chave `MOVIES_CSV_PATH`) |
| FR-003, FR-005 | `StartupFailureIT`, `HeaderCaseIT` |
| FR-004 | `HarmlessVariationsIT`, `WinnerVariantsIT` |
| FR-005a | `HarmlessVariationsIT` (um método por variação, mais a comparação com `harmless-clean.csv`), `HeaderCaseIT`, `BomVariationsIT` (BOM com cabeçalho entre aspas e BOM seguido de linha em branco) |
| FR-006, FR-006a | `InvalidLinesIT` (um caso por motivo), `InvalidLineIgnoredIT` |
| FR-006b | `LoadSummaryIT` |
| FR-006c | `StartupFailureIT`, `HarmlessVariationsIT`, `BomVariationsIT`, `InvalidLinesIT`, `DuplicateLinesIT` |
| FR-006d | `DuplicateLinesIT`, `DuplicateWinnerIT` |
| FR-006e | `OptionalStudiosIT`, `MoviesListingIT` |
| FR-007 a FR-010 | `ProducerSeparatorsIT` (absorveu, no bolt 3, o `ProducerNamesIT` do bolt 2, que verificava pelo banco) |
| FR-011 | `WinnerVariantsIT` |
| FR-012, FR-016 | `ThreeOrMoreWinsIT`, `UnorderedLinesIT` |
| FR-013 | `SameYearWinsIT` |
| FR-014, FR-015 | `TiesMinIT`, `TiesMaxIT`, `IntervalEdgeCasesIT` |
| FR-016a, FR-016b | `DisjointProducersIT`, `RandomizedOracleIT`, a busca do quickstart (passo 6) |
| FR-017 | `ReferenceFileIT`, `IntervalsShapeIT` |
| FR-018 | `NoRepeatWinnersIT`, `IntervalsShapeIT`, `EmptyLoadIT` |
| FR-019 a FR-022 | `MoviesListingIT`; os ITs de carga de `ingestion/` também conferem os filmes por `GET /api/movies` |
| FR-023 | `MoviesInvalidParamsIT` |
| FR-023a | `HttpMethodsIT` |
| FR-024 | `ApiDocsIT` |
| FR-025 | `IntervalsContractIT`, `MoviesContractIT`, `ApiDocsIT` |
| FR-026, FR-027 | `SmokeIT`, `./mvnw verify` sem Docker; esta matriz |
| SC-001 | `ReferenceFileIT` |
| SC-004 | `RandomizedOracleIT` e os 13 casos do testador adversário (`adversario/Case01…Case13*IT`, um por CSV de `docs/ai/adversario/`) |
| FR-006 (faixa de ano), FR-007 (`and` sem diferenciar maiúsculas), FR-015 (ordem Unicode) | `Case09ExtremeYearsIT`, `Case12AndSeparatorCaseIT`, `Case13AccentAndCaseOrderIT` |
| SC-004a | `DisjointProducersIT` |
| SC-005 | `ResponseTimeIT` |
| SC-006, SC-006a | `StartupFailureIT`, `InvalidLinesIT`, `LoadSummaryIT` |
| SC-003 | checagem manual no fechamento (quickstart em clone limpo) |
| Casos de borda (UTF-8, vazio) | `ProducerSeparatorsIT`, `MoviesListingIT`, `EmptyLoadIT` |
| Caso de borda (página além da última) | `MoviesListingIT`, `MoviesInvalidParamsIT` (`page` fora de 32 bits) |

## Riscos

| Risco | Mitigação |
| --- | --- |
| `swagger-request-validator-core` 3.0.0 incompatível com o Boot 4 | Usar escopo de teste e Jackson 2 isolado; o plano B é `networknt/json-schema-validator` (R8) |
| Muitos contextos Spring deixam o `verify` lento | Um contexto por CSV, guardado em cache; a meta é `verify` abaixo de 2 min, e cenários parecidos podem ser agrupados num mesmo CSV se passar disso |
| `movielist.csv` ausente do repositório | Primeira tarefa do bolt: adicionar o arquivo do enunciado |
| Busca do SC-004b com falso positivo (anos em comentários) | A busca é limitada a `src/main/java`; um comentário com ano deve ser reescrito |
| `ResponseTimeIT` instável em máquina lenta | O limite é o do SC-005 (1 s), muito acima do tempo esperado (milissegundos); se falhar, investigar antes de afrouxar |

## Complexity Tracking

Não se aplica: nenhuma violação da constituição.
