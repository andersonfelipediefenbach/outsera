# Implementation Plan: Marco 3 — Modo Docker: PostgreSQL, ingestão por Kafka, autenticação e compose

**Branch**: `marco/3-docker` | **Date**: 2026-11-24 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/specs/003-modo-docker/spec.md`

## Summary

O Marco 3 acrescenta o modo Docker e a autenticação, sem mudar o modo padrão nem o contrato de
intervalos.

- **Perfis:** `postgres` (banco) e `kafka` (ingestão) são independentes, e o grupo `docker` liga os
  dois. Sem perfil, vale o Marco 2. A autoconfiguração do Kafka fica excluída fora do perfil (R1 e R2).
- **PostgreSQL:**
  - driver e `flyway-database-postgresql`, com a mesma migração V1 e o `validate` (R3);
  - a ordem da listagem fica igual à do H2 com o banco criado em collation `C`, sem mudar SQL (R4).
- **Serviço único com atualização:** o `MovieImportService.record` cria ou atualiza o filme por ano e
  título. A carga direta e o consumidor usam o mesmo método, e as regras de campo ficam numa classe
  comum ao CSV e aos eventos (R5 e R7).
- **Kafka (Spring for Apache Kafka):**
  - tópico de filmes e de erros, com uma partição cada (R6);
  - o publicador lê o CSV na subida, com as mesmas falhas e os mesmos avisos, e publica depois do
    `ApplicationReadyEvent`, com um `runId` por execução (R8);
  - o consumidor lê os bytes crus. O evento inválido vai ao tópico de erros com os bytes originais e o
    motivo nos cabeçalhos, e a falha de banco é tentada de novo sem avançar o offset (R9);
  - a readiness ganha o indicador `ingestion`, liberado só pelo marcador do `runId` desta execução
    (R10);
  - as métricas trazem o atraso do consumidor e os eventos gravados e rejeitados.
- **Autenticação (Spring Security):**
  - `SecurityFilterChain` sem estado, sem CSRF, sem `formLogin` e sem `httpBasic`;
  - um filtro de `Authorization: Bearer`, com comparação em tempo constante;
  - com o token desligado, tudo liberado. Com ele ligado, só os intervalos, a listagem e as métricas
    exigem o token;
  - 401 em Problem Details;
  - sem usuário gerado (R11);
  - o `Cache-Control` do Security fica desligado nos endpoints de negócio, para preservar o ETag (R12);
  - contrato OpenAPI 1.2.0, com a autenticação opcional (R13).
- **Prova de que os Marcos 1 e 2 não mudaram:**
  - a suíte roda sem token;
  - um IT de guarda falha se `API_TOKEN` estiver definido;
  - um script compara os ITs com a tag `v0.2.0` (R14).
- **Testes:**
  - Kafka com `@EmbeddedKafka` (spring-kafka-test) no `./mvnw verify` (R15);
  - PostgreSQL no perfil Maven `postgres-it`, com o *service container* do GitHub Actions e sem
    Testcontainers (R16).
- **Compose:**
  - imagem nativa do Kafka em KRaft, PostgreSQL, a aplicação, Prometheus e Grafana, com datasource e
    dashboard provisionados por arquivo;
  - um `kafka-cli` sob demanda;
  - imagens por digest;
  - credenciais de demonstração só no compose (R17 a R19, constituição 1.6.0).
- **Pipeline:**
  - jobs novos `postgres` e `compose`. O `compose` verifica a aplicação, o Prometheus, o dashboard, a
    ingestão, a persistência e o token;
  - política ampliada no `workflow-policy` (R20).
- **Entrega:** um bolt, cinco etapas e um PR (R21), nesta ordem: PostgreSQL e perfil `docker`, Kafka,
  autenticação, compose com observabilidade, e ADRs com o README.

## Technical Context

**Language/Version**: Java 25 (OpenJDK 25.0.2), sem mudança

**Primary Dependencies**: as do Marco 2 (Spring Boot 4.1.2), mais:
- `spring-boot-starter-security`;
- `org.springframework.kafka:spring-kafka`;
- `org.postgresql:postgresql` (runtime);
- `org.flywaydb:flyway-database-postgresql` (runtime);
- nos testes, `spring-kafka-test`. O `spring-security-test` não entra: os ITs mandam o cabeçalho real
  pelo `RestTestClient`.

Todas com a versão gerenciada pelo Boot (verificar o `flyway-database-postgresql`, R3)

**Storage**:
- H2 em memória no modo padrão;
- PostgreSQL 18 no perfil `postgres`, com o mesmo esquema Flyway e collation `C`;
- Kafka como transporte no perfil `kafka`, e não como armazenamento da API

**Testing**: JUnit 5 e `@SpringBootTest` com `RestTestClient`, como nos Marcos 1 e 2:
- `@EmbeddedKafka` (broker KRaft embutido) nos ITs de Kafka do `./mvnw verify`;
- `ApplicationStarter` para as múltiplas execuções (equivalência e reinícios);
- perfil Maven `postgres-it` para os `*PostgresIT`;
- scripts bash no pipeline para o compose;
- sem mocks e sem Testcontainers

**Target Platform**:
- JVM 25 pelo `./mvnw`, em qualquer sistema operacional;
- a imagem do Marco 2;
- Docker Compose v2;
- runners `ubuntu-latest`

**Project Type**: web-service (API REST, módulo Maven único), mais infraestrutura de compose e
observabilidade

**Performance Goals**:
- vencedor novo visível em até 5 s (SC-005);
- compose pronto, com o dashboard mostrando dados, em menos de 3 min com as imagens baixadas (SC-008);
- o `./mvnw verify` até 2× o tempo do Marco 2 (R15)

**Constraints**:
- `./mvnw` sem Docker (Princípio II);
- o contrato de intervalos idêntico (Princípio III);
- os ITs dos Marcos 1 e 2 sem alteração;
- SQL portável;
- credenciais de demonstração só no compose (1.6.0);
- actions por hash e imagens por digest

**Scale/Scope**: centenas de filmes; um consumidor, uma partição e uma instância da aplicação; 2
endpoints de negócio protegíveis e 1 operacional; 2 jobs novos de CI

Nenhum item ficou como NEEDS CLARIFICATION. Os pontos que dependem do comportamento exato das versões
estão marcados como **verificar** na [research.md](research.md) (R2, R3, R10, R13, R14, R15, R17, R18 e
R19), cada um com o que fazer se o resultado for outro.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Princípio | Como o plano atende | Pré-design | Pós-design |
| --- | --- | --- | --- |
| I. Somente testes de integração | Todo teste novo é `*IT`, com `@SpringBootTest` ou com `SpringApplication` para as múltiplas execuções (equivalência e reinícios; cláusula 1.5.0). Todos são verificados por HTTP: intervalos, listagem, readiness e métricas. O broker é real (embutido) e o PostgreSQL é real (service container). Nenhum mock. O tópico de erros é lido por um consumidor real no teste. A única verificação fora do HTTP é o log (avisos, resumo, ausência do token e da senha gerada), porque o log é o comportamento. A propriedade `awards.ingestion.publish-completion` (R10) não é um dublê: ela desliga um passo real, e o teste envia o marcador pelo broker real | ✅ | ✅ |
| II. Execução só com `./mvnw` | Os ITs de Kafka usam o broker embutido, sem Docker. Os de PostgreSQL ficam no perfil `postgres-it`, fora do `verify` padrão. A autoconfiguração do Kafka é excluída sem o perfil. A segurança fica desligada sem `API_TOKEN` | ✅ | ✅ |
| III. Contrato idêntico | Os schemas não mudam. O 1.2.0 só acrescenta a autenticação opcional (`{}`) e o 401. O `IntervalsContractIT` e o `ReferenceFileIT` não mudam. A equivalência entre os caminhos de carga e entre os bancos é comparada byte a byte | ✅ | ✅ |
| IV. Spec-anchored | A spec 003 foi clarificada (3 decisões) e a constituição foi emendada (1.6.0 e 1.7.0) antes das tarefas. O conflito do `ApiDocsIT` foi levado ao responsável e decidido (opção A, [contracts/openapi-1.2.0.md](contracts/openapi-1.2.0.md)) | ✅ | ✅ |
| V. Extras isolados, serviço único, simplicidade | O Kafka e o PostgreSQL só com o perfil. A gravação fica num único serviço (`MovieImportService.record`), e o perfil muda só a origem. Não há registro de esquemas, várias partições, Testcontainers nem JWT. A autenticação é ligada só pela variável | ✅ | ✅ |
| VI. Rastreabilidade (1.7.0) | Matriz abaixo. Comportamento comum aos dois bancos: IT no `./mvnw verify`. Comportamento que depende do PostgreSQL: `*PostgresIT` no perfil Maven dedicado `postgres-it`, executado obrigatoriamente pelo job `postgres` (regra da emenda 1.7.0). O Kafka tem broker embutido e por isso fica no `verify`. Compose e pipeline: jobs. README e ADRs: checklist | ✅ | ✅ |
| VII. Sem segredos (1.6.0) | O `API_TOKEN` vem do ambiente e nunca é registrado. As credenciais de demonstração ficam **só** no `compose.yaml`, identificadas e sobrescrevíveis. A aplicação não tem senha padrão (`DB_PASSWORD` obrigatório no perfil). O pipeline usa `trust` no PostgreSQL e um token aleatório gerado no job, sem `secrets.*` | ✅ | ✅ |
| Observabilidade (1.3.0) | A readiness só fica positiva depois da carga, também no Kafka (indicador `ingestion`). O log continua em texto no `./mvnw` e em JSON na imagem. Os ITs de log novos têm `@Tag("log")` e rodam nos dois formatos | ✅ | ✅ |
| Cadeia de entrega | As actions novas são fixadas por hash. As imagens do compose e do service container são fixadas por digest, conferidas pelo `workflow-policy`. O Dependabot ganha o `docker-compose` | ✅ | ✅ |

**Resultado:** nenhuma violação. O "Complexity Tracking" não se aplica. A combinação dos Princípios II e
VI, que ficou ambígua na primeira versão deste plano, foi resolvida pela emenda 1.7.0 do Princípio VI.

## Rastreabilidade (Princípio VI)

| Requisito | Comprovação |
| --- | --- |
| FR-001 | suíte dos Marcos 1 e 2 sem alteração; `DefaultModeIsolationIT`; `check-unchanged-tests.sh` |
| FR-002 | ITs `kafka/` com H2 e `@ActiveProfiles("kafka")`; `*PostgresIT` com `postgres` |
| FR-003 | ITs que sobrescrevem `KAFKA_TOPIC_*` e `DB_*`; `KafkaConsumerGroupIT` (grupo do consumidor); job `compose` |
| FR-004, FR-005 | `SchemaPostgresIT` (job `postgres`); `ddl-auto: validate` nos dois bancos |
| FR-006, FR-008 | `EquivalencePostgresIT`, `ListingOrderPostgresIT` (job `postgres`) |
| FR-007 | job `postgres`, obrigatório na proteção da `main` (T093) |
| FR-009 | `KafkaInvalidLinesIT`, `KafkaStructuralFailureIT`, `PublisherSummaryIT` |
| FR-010, FR-011 | `KafkaReadinessIT` (lê o tópico) e `KafkaReferenceFileIT` |
| FR-012 | `KafkaEquivalenceIT`, `KafkaNewWinnerIT` (separação de nomes) |
| FR-013 | `KafkaReadinessIT`, `KafkaBrokerUnavailableIT`, `KafkaBrokerLateStartIT` (tópicos recriados quando o broker volta) |
| FR-014 | `KafkaIdempotencyIT`, `KafkaUpdateIT`, `RestartDirectPostgresIT`, `RestartPostgresIT` |
| FR-015 | `KafkaInvalidEventsIT`, `KafkaErrorTopicUnavailableIT` |
| FR-015a | `RetryPostgresIT` (job `postgres`: nova tentativa quando a gravação falha); passo 7b do job `compose` (queda real do banco) |
| FR-016 | `KafkaNewWinnerIT` |
| FR-017 | `KafkaMetricsIT` |
| FR-018 | `KafkaEquivalenceIT` (inclui `FIELD_TOO_LONG`); `FieldTooLongIT` (carga direta, spec 001 FR-006) |
| FR-019 a FR-023, FR-025, FR-027 | `AuthDisabledByDefaultIT`, `AuthEnabledIT` (inclui `OPTIONS`), `AuthProblemDetailsIT`, `TokenNotLoggedIT`, `NoGeneratedPasswordIT` |
| FR-022a | `CacheHeadersIT` |
| FR-024 | retirado (decisão de design na R11 e no ADR 0017; "Revisão de segurança" do checklist) |
| FR-026 | `ApiDocsSecurityIT`, `AuthContractIT` |
| FR-028 a FR-032, FR-035 | job `compose` |
| FR-033, FR-034 | job `workflow-policy` (digest, Dependabot e senha fora do workflow); revisão (comentários de demonstração) |
| FR-036, FR-037 | checklist "Revisão do README" e "Revisão dos ADRs" |
| SC-002 | job `build` com a guarda do `AuthDisabledByDefaultIT`; `check-unchanged-tests.sh` |

Cada IT traz os IDs no `@DisplayName`. Os IDs desta spec repetem os das specs 001 e 002, com outros
significados (observação da etapa 6 do Marco 2). Por isso, os ITs novos usam o prefixo `S003`, por
exemplo `S003 FR-013 readiness só depois do marcador`. Os ITs antigos não mudam.

## Project Structure

### Documentation (this feature)

```text
specs/003-modo-docker/
├── plan.md                      # Este arquivo
├── research.md                  # Fase 0 (R1 a R21)
├── data-model.md                # Fase 1
├── quickstart.md                # Fase 1
├── contracts/
│   ├── openapi.yaml             # contrato de negócio 1.2.0 (copiado para src/main/resources/static)
│   ├── openapi-1.2.0.md         # o que muda em relação ao 1.1.0 e o conflito do ApiDocsIT
│   ├── kafka-events.md
│   ├── operational-endpoints.md
│   ├── compose.md
│   └── ci-pipeline.md
├── checklists/
│   └── requirements.md          # inclui "Revisão do README" e "Revisão dos ADRs"
└── tasks.md                     # Fase 2 (/speckit-tasks)
```

### Source Code (repository root)

Só o que muda ou é novo.

```text
pom.xml                                   # security, kafka, postgres, flyway-postgres; testes; perfil postgres-it
compose.yaml                              # novo (R17)
observability/                            # novo: prometheus.yml, provisionamento e dashboard do Grafana (R18, R19)
.github/
├── workflows/ci.yml                      # jobs postgres e compose; workflow-policy com fetch-depth 0
├── scripts/
│   ├── check-workflows.sh                # regras novas (compose por digest, docker-compose no Dependabot)
│   ├── check-unchanged-tests.sh          # novo (R14)
│   └── compose-test.sh                   # novo (R20)
├── unchanged-tests-exceptions.txt        # novo (R14)
└── dependabot.yml                        # + docker-compose
src/main/java/com/outsera/awards/
├── ingestion/
│   ├── MovieImportService.java           # record(MovieRow), cria ou atualiza (R5)
│   ├── MovieFieldRules.java              # novo: regras de ano, título e produtores comuns (R7)
│   ├── MovieFileReading.java             # novo: leitura e avisos comuns à carga e ao publicador (R8)
│   ├── MovieFileLoader.java              # @Profile("!kafka")
│   └── kafka/                            # novo, tudo @Profile("kafka")
│       ├── KafkaIngestionConfiguration.java  # tópicos, fábrica do ouvinte, DefaultErrorHandler (R6, R9)
│       ├── KafkaIngestionProperties.java     # nomes dos tópicos, grupo, publish-completion
│       ├── MovieEventPublisher.java          # leitura na subida, publicação no ApplicationReadyEvent (R8)
│       ├── MovieEventListener.java           # consumo, gravação, tópico de erros, contadores (R9)
│       ├── MovieEventParser.java             # bytes → MovieRow ou rejeição (R7)
│       ├── EventRejectionReason.java
│       ├── LoadCompletedEvent.java
│       └── IngestionReadiness.java           # HealthIndicator "ingestion" (R10)
├── movie/MovieRepository.java            # findByYearAndTitle
├── movie/Movie.java                      # update(studios, producersRaw, winner, producers)
└── security/                             # novo
    ├── SecurityConfiguration.java        # SecurityFilterChain, matriz de acesso, cabeçalhos (R11, R12)
    ├── SecurityProperties.java           # awards.security.token ← API_TOKEN
    ├── TokenAuthenticationFilter.java    # Bearer, SHA-256 + MessageDigest.isEqual
    └── ProblemAuthenticationEntryPoint.java  # 401 em Problem Details
src/main/resources/
├── application.yml                       # exclusões de autoconfiguração (R2); awards.security.token
├── application-kafka.yml                 # novo: Kafka, tópicos, grupo de readiness (R10)
├── application-postgres.yml              # novo: DB_URL, DB_USER, DB_PASSWORD (sem padrão)
└── static/openapi.yaml                   # cópia de specs/003-modo-docker/contracts/openapi.yaml
src/test/java/com/outsera/awards/
├── support/                              # ApplicationStarter com perfis e argumentos; KafkaTestSupport (produtor e consumidor reais); PostgresSupport
├── isolation/   DefaultModeIsolationIT
├── security/    AuthDisabledByDefaultIT, AuthEnabledIT, AuthProblemDetailsIT, TokenNotLoggedIT,
│                NoGeneratedPasswordIT, CacheHeadersIT, AuthContractIT
├── docs/        ApiDocsSecurityIT
├── kafka/       KafkaReferenceFileIT, KafkaEquivalenceIT, KafkaReadinessIT, KafkaInvalidLinesIT,
│                KafkaStructuralFailureIT, KafkaNewWinnerIT, KafkaUpdateIT, KafkaIdempotencyIT,
│                KafkaInvalidEventsIT, KafkaMetricsIT, PublisherSummaryIT, KafkaBrokerUnavailableIT,
│                KafkaErrorTopicUnavailableIT
├── ingestion/   FieldTooLongIT (spec 001 FR-006, motivo FIELD_TOO_LONG)
└── postgres/    SchemaPostgresIT, EquivalencePostgresIT, ListingOrderPostgresIT, RestartDirectPostgresIT,
                 RestartPostgresIT, RetryPostgresIT (@Isolated)
src/test/resources/scenarios/             # CSVs novos dos cenários de Kafka, quando o arquivo fornecido não bastar
docs/adr/                                 # ADRs da FR-037 (papel de documentação)
```

**Structure Decision**: o módulo Maven único continua. O código novo fica em `ingestion/kafka/` e em
`security/`, seguindo os pacotes por assunto que já existem. A infraestrutura nova fica na raiz
(`compose.yaml`) e em `observability/`, como o briefing previa.

## Decisões do responsável sobre o plano (2026-11-24)

Registradas em `docs/ai/decisoes.md`.

1. **`ApiDocsIT`:** opção A. Só a constante `SPEC_CONTRACT` passa a apontar para o contrato da spec
   003, sem mudar nenhuma asserção. A exceção entra em `.github/unchanged-tests-exceptions.txt`, com a
   referência à entrada do `decisoes.md`.
2. **Cabeçalhos de proteção** (R12): `X-Content-Type-Options` e `X-Frame-Options` ficam em todas as
   respostas, com o token ligado ou desligado. O ADR da autenticação os registra como decisão
   intencional (FR-037).
3. **Princípios II e VI:** emenda MINOR 1.7.0 do Princípio VI. O comportamento que depende de
   infraestrutura indisponível no modo padrão é comprovado num perfil Maven dedicado, executado
   obrigatoriamente no pipeline.

## Complexity Tracking

Não se aplica: nenhuma violação no Constitution Check.
