---

description: "Tarefas do Marco 2, num único bolt com seis etapas por assunto (um commit por etapa)"
---

# Tasks: Marco 2 — Operação: ETag, observabilidade, imagem, pipeline e ADRs

**Input**: Design documents from `/specs/002-operacao/`

**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/ (openapi.yaml,
operational-endpoints.md, container.md, ci-pipeline.md), quickstart.md

**Tests**: obrigatórios, e **vêm antes da implementação em cada etapa**. A comprovação segue o
Princípio VI (constituição 1.4.1):

- **comportamento da aplicação:** somente testes de integração (Princípio I). Cada teste é uma classe
  `*IT` executada pelo Failsafe em `./mvnw verify`, com `@SpringBootTest(webEnvironment = RANDOM_PORT)`
  pela base `ScenarioIT`, ou com a aplicação real por `SpringApplication` (classe de apoio
  `ApplicationStarter`). Nada de teste unitário, mock ou classe `*Test`. Cada método tem `@DisplayName`
  começando pelos IDs dos requisitos que comprova;
- **imagem e pipeline:** o script de fumaça e os jobs do pipeline;
- **documentação:** as seções "Revisão dos ADRs" e "Revisão do README" de
  `checklists/requirements.md`.

**Organization**: **um único bolt** no branch `marco/2-operacao`, com um único pull request, dividido em
**seis etapas por assunto**, nesta ordem: ETag, observabilidade, Dockerfile, pipeline e Dependabot, ADRs
e README (research R16). Cada etapa:

1. começa pelos testes ou pela verificação, que precisam **falhar** antes da implementação;
2. termina com `./mvnw verify` **verde**;
3. fecha com **um commit** próprio, de mensagem descritiva e assunto único.

O PR, o merge e a tag são do Anderson. Nenhum agente abre PR, faz merge ou cria tag.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: pode ser feita em paralelo (arquivos diferentes e sem dependência de tarefa incompleta)
- **[Story]**: história da spec 002. US1 = ETag, US2 = liveness e readiness, US3 = métricas,
  US4 = logs, US5 = imagem, US6 = pipeline, US7 = ADRs.
- Pacote base: `com.outsera.awards`. Testes em `src/test/java/com/outsera/awards/`. Cenários em
  `src/test/resources/scenarios/`.

## Mapeamento etapa → fase

| Etapa | Fase | Histórias | Responsável (`AGENTS.md`) | Commit |
| --- | --- | --- | --- | --- |
| — | 1. Preparação | — | Implementador | entra no commit da etapa 1 |
| 1. ETag | 2 | US1 | Implementador | `Etapa 1: ETag e contrato 1.1.0` |
| 2. Observabilidade | 3 | US2, US3, US4 | Implementador | `Etapa 2: health, métricas e logs nos dois formatos` |
| 3. Dockerfile | 4 | US5 | Infraestrutura | `Etapa 3: Dockerfile, .dockerignore e teste de fumaça` |
| 4. Pipeline e Dependabot | 5 | US6 | Implementador (JaCoCo no `pom.xml`) e infraestrutura (`.github/`) | `Etapa 4: pipeline, cobertura mínima e Dependabot` |
| 5. ADRs | 6 | US7 | Documentação | `Etapa 5: ADRs do Marco 1 e do Marco 2` |
| 6. README | 7 | — (FR-034) | Documentação | `Etapa 6: README do Marco 2` |

Toda mensagem de commit termina com a linha de atribuição indicada na sessão.

---

## Phase 1: Preparação

**Purpose**: confirmar o ponto de partida antes de qualquer mudança.

- [X] T001 Confirmar que o branch atual é `marco/2-operacao`, que a constituição é a 1.4.1 (`.specify/memory/constitution.md`) e rodar `./mvnw verify` na raiz: o resultado precisa ser verde, com os 136 ITs do Marco 1 (relatório em `target/failsafe-reports/failsafe-summary.xml`). Anotar o número de ITs e o tempo total para comparar no fim da etapa 2.

---

## Phase 2: Etapa 1 — ETag (US1, Priority: P1) 🎯 MVP

**Goal**: ETag forte nas respostas 200 dos dois endpoints de negócio e 304 na revalidação, com o
contrato 1.1.0 e o corpo idêntico ao do Marco 1 (FR-001 a FR-007; research R4 e R5).

**Independent Test**: `IntervalsEtagIT`, `MoviesEtagIT`, `EtagEmptyIT`, `EtagAcrossRunsIT` e os testes de
contrato passam, e toda a suíte do Marco 1 passa sem alteração de resultado esperado.

### Testes da etapa 1 (escrever primeiro; precisam falhar)

- [X] T002 [P] [US1] Criar `src/test/java/com/outsera/awards/etag/IntervalsEtagIT.java` (estende `ScenarioIT`, arquivo padrão). Métodos, cada um com `@DisplayName` iniciado pelos IDs: (a) `FR-001` GET de `/api/producers/intervals` responde 200 com `ETag` no formato forte `"0` + 32 hex + `"` (regex `^"0[0-9a-f]{32}"$`); (b) `FR-005` o corpo continua idêntico ao do Marco 1 (`assertIntervals` com Joel Silver 1/1990/1991 em `min` e Matthew Vaughn 13/2002/2015 em `max`, JSON estrito); (c) `FR-003` com `If-None-Match` igual ao ETag recebido → 304, corpo vazio e o mesmo `ETag`; (d) `FR-003` com a lista `"outro", <etag>` → 304; (e) `FR-003` com a forma fraca `W/<etag>` → 304; (f) `FR-003` com `*` → 304; (g) `FR-004` com `"outro"` → 200 com o corpo completo e o ETag atual; (h) `FR-004` com valor malformado (`abc`, sem aspas) e com valor vazio → 200 com corpo; (i) `FR-006` POST com `If-None-Match: *` → 405 em Problem Details, sem `ETag`.
- [X] T003 [P] [US1] Criar `src/test/java/com/outsera/awards/etag/MoviesEtagIT.java` (estende `ScenarioIT`, `@TestPropertySource(properties = "movies.csv.path=classpath:scenarios/listing-sample.csv")`, 25 filmes). Métodos: (a) `FR-001` `GET /api/movies?winner=true&size=5` tem `ETag`; (b) `FR-002` a mesma consulta repetida devolve o mesmo `ETag`; (c) `FR-003` US1-4 a mesma consulta com `If-None-Match` do ETag recebido → 304, sem corpo; (d) `FR-002` US1-5 o ETag de `size=5` enviado em `size=6`, em `page=1` e em `winner=false` → 200, com corpo e ETag diferente em cada caso; (e) `FR-006` `winner=talvez` com `If-None-Match: *` → 400 `application/problem+json`, sem `ETag`; (f) caso de borda: `page=2&size=20` → 200 com `content` vazio e `ETag`, e a revalidação com esse ETag → 304.
- [X] T004 [P] [US1] Criar `src/test/java/com/outsera/awards/etag/EtagEmptyIT.java` (`scenarios/header-only.csv`): `FR-001 FR-003` os intervalos vazios (`{"min":[],"max":[]}`) têm `ETag`, e a revalidação com ele → 304 (caso de borda "arquivo só com cabeçalho").
- [X] T005 [US1] Em `src/test/java/com/outsera/awards/support/ApplicationStarter.java`, acrescentar `public static <T> T runAndCall(String csvPath, java.util.function.IntFunction<T> call)`: sobe a aplicação real com `--server.port=0` e `--movies.csv.path=<csvPath>`, lê a porta pelo `local.server.port` do ambiente, executa `call.apply(porta)` com a aplicação no ar e fecha o contexto no fim (inclusive com exceção). O `start` existente não muda.
- [X] T006 [US1] Criar `src/test/java/com/outsera/awards/etag/EtagAcrossRunsIT.java` (classe simples, sem `ScenarioIT`, como o `StartupFailureIT`), usando `ApplicationStarter.runAndCall` e `java.net.http.HttpClient` para `GET /api/producers/intervals`: (a) `FR-002 US1-7` duas execuções com `classpath:movielist.csv` dão o mesmo `ETag`; (b) `FR-002 US1-7` uma execução com `classpath:movielist.csv` e outra com `classpath:scenarios/disjoint-producers.csv` dão ETags diferentes.
- [X] T007 [US1] Em `src/test/java/com/outsera/awards/support/ContractValidator.java`, fazer o `assertValidGet` e o `validateGet` repassarem ao `SimpleResponse` os cabeçalhos da resposta (pelo menos `ETag`), para o validador conferir o cabeçalho `required: true` do contrato, e aceitarem corpo vazio no 304. Em `src/test/java/com/outsera/awards/docs/IntervalsContractIT.java` e `src/test/java/com/outsera/awards/docs/MoviesContractIT.java`, acrescentar: `FR-007` a resposta 200 com `ETag` é válida, e a resposta 304 (revalidação com o ETag recebido) é válida contra o contrato.
- [X] T008 [US1] Em `src/test/java/com/outsera/awards/docs/ApiDocsIT.java`, trocar `SPEC_CONTRACT` para `Path.of("specs", "002-operacao", "contracts", "openapi.yaml")` (research R5) e acrescentar ao `contractDescribesBothEndpoints` a verificação `FR-007` de que os dois GETs declaram a resposta `304` e o parâmetro `If-None-Match`. A lista de parâmetros da listagem passa a ser `year`, `winner`, `page`, `size` e a referência `IfNoneMatch` (conferir pelo `name` resolvido ou pelo `$ref`).
- [X] T009 [US1] Rodar `./mvnw verify` e confirmar que os testes novos **falham** pelo motivo esperado (ausência de `ETag`, de 304 e do contrato 1.1.0). Nenhum teste do Marco 1 pode falhar por outro motivo. **Resultado:** os testes novos falharam pelo motivo esperado; o `EmptyLoadIT` (Marco 1) também falhou, porque passou a reaproveitar o contexto do `header-only.csv` criado antes pelo `EtagEmptyIT` e não via mais o log da inicialização. Ganhou `@DirtiesContext(BEFORE_CLASS)`, como o `DefaultFileIT`, sem mudança de resultado esperado.

### Implementação da etapa 1

- [X] T010 [US1] Criar `src/main/java/com/outsera/awards/web/EtagConfiguration.java`: `@Configuration` com um `@Bean FilterRegistrationBean<ShallowEtagHeaderFilter>` chamado `etagFilter`, com `addUrlPatterns("/api/producers/intervals", "/api/movies")` e ETag forte (o padrão do filtro). Javadoc citando FR-001 a FR-006 e research R4. Nenhuma mudança nos controllers. **Executado com ajuste:** o `ShallowEtagHeaderFilter` não trata `If-None-Match: *` (200 em vez de 304); o `EtagConfiguration` registra o filtro próprio `src/main/java/com/outsera/awards/web/ContentEtagFilter.java`, que faz o mesmo e trata o `*` (research R4, `docs/ai/decisoes.md`).
- [X] T011 [US1] Substituir `src/main/resources/static/openapi.yaml` por uma cópia byte a byte de `specs/002-operacao/contracts/openapi.yaml` (com LF, como manda o `.gitattributes`).
- [X] T012 [US1] Rodar `./mvnw verify`: tudo verde, com os testes do Marco 1 sem nenhuma alteração de resultado esperado (FR-005, SC-001). Se o 304 aparecer com corpo ou o `*` não for tratado, registrar o comportamento observado na research R4 antes de corrigir.
- [X] T013 [US1] Commit `Etapa 1: ETag e contrato 1.1.0` com T002 a T012.

**Checkpoint**: US1 completa e verificável sozinha.

---

## Phase 3: Etapa 2 — Observabilidade (US2, US3, US4)

**Goal**: probes de liveness e readiness, com a readiness após a carga e sem detalhes; métricas no
formato do Prometheus; log em texto no `./mvnw` e JSON ECS com a variável, com os testes de log rodando
nos dois formatos (FR-008 a FR-020; research R1, R2, R3 e R6).

**Independent Test**: os ITs de `health/`, `metrics/` e `logging/` passam, e as classes com `@Tag("log")`
passam nas duas execuções do Failsafe (`default` e `json-logs`).

### Apoio aos testes (sem mudança de comportamento)

- [X] T014 [US4] Criar `src/test/java/com/outsera/awards/support/LogOutput.java`: transforma a saída capturada numa lista de eventos `record Event(String timestamp, String level, String logger, String thread, String message, String stackTrace)`. Regras (research R6, data-model "Evento de log"): uma linha que começa com `{` é lida como JSON ECS (`@timestamp`, `log.level`, `log.logger`, `process.thread.name`, `message`, `error.stack_trace`), com o Jackson já presente no classpath de teste; as demais linhas seguem o padrão de texto do Boot (`<data> <NÍVEL> <pid> --- [<app>] [<thread>] <logger> : <mensagem>`), e as linhas que não casam com o padrão são continuação do evento anterior (mensagem multilinha ou pilha). Métodos: `static List<Event> parse(String output)`, `static boolean isJson(String line)`.
- [X] T015 [US4] Refatorar `src/test/java/com/outsera/awards/support/LoaderLog.java` para usar o `LogOutput`: `loaderWarnings` passa a devolver as **mensagens** dos eventos de nível `WARN` cujo logger termina em `MovieFileLoader`, e `summary` procura o padrão do resumo nas mensagens. Os chamadores (`endsWith("Linha N rejeitada: ...")`) continuam sem mudança. Em `src/test/java/com/outsera/awards/support/ApplicationStarter.java`, acrescentar `Result.messages()`, que devolve as mensagens e pilhas decodificadas pelo `LogOutput`, uma por linha, e uma sobrecarga `start(String csvPath, ApplicationListener<?>... listeners)` que registra os ouvintes na `SpringApplication` antes do `run`. Em `src/test/java/com/outsera/awards/ingestion/StartupFailureIT.java`, trocar as verificações de conteúdo de `result.output()` por `result.messages()`.
- [X] T016 [US4] Marcar com `@Tag("log")` as classes que verificam o log: `StartupFailureIT`, `BomVariationsIT`, `DefaultFileIT`, `DuplicateLinesIT`, `EmptyLoadIT`, `HarmlessVariationsIT`, `HeaderCaseIT`, `InvalidLinesIT`, `LoadSummaryIT` e `OptionalStudiosIT` (em `src/test/java/com/outsera/awards/ingestion/`) e `ProducerSeparatorsIT` (em `src/test/java/com/outsera/awards/intervals/`). Conferir com uma busca por `CapturedOutput` e `LoaderLog` em `src/test/java` se alguma outra classe verifica o log e marcá-la também.
- [X] T017 [US4] No `pom.xml` (Failsafe), acrescentar a execução `json-logs` (research R6): goals `integration-test` e `verify`, `<groups>log</groups>`, `<environmentVariables><LOGGING_STRUCTURED_FORMAT_CONSOLE>ecs</LOGGING_STRUCTURED_FORMAT_CONSOLE></environmentVariables>`, `<reportsDirectory>${project.build.directory}/failsafe-reports-json-logs</reportsDirectory>` e `<summaryFile>${project.build.directory}/failsafe-reports-json-logs/failsafe-summary.xml</summaryFile>`. A execução existente não muda. Rodar `./mvnw verify`: verde nas duas execuções, porque o comportamento ainda não mudou. Se alguma classe com `@Tag("log")` falhar só no JSON, corrigir o `LogOutput`, e não o teste.

### Testes da etapa 2 (escrever primeiro; os de US2 e US3 precisam falhar)

- [X] T018 [P] [US2] Criar `src/test/java/com/outsera/awards/health/HealthProbesIT.java` (estende `ScenarioIT`, arquivo padrão): (a) `FR-008 US2-1` `GET /actuator/health/liveness` → 200 com corpo exatamente `{"status":"UP"}`; (b) `FR-008 FR-009 US2-2` `GET /actuator/health/readiness` → 200 `{"status":"UP"}`, e os dados já estão disponíveis: `movies("")` tem `totalElements` igual a 206, e os intervalos têm Joel Silver em `min`; (c) `FR-011 US2-6` `GET /actuator/health` → 200 com corpo só com a chave `status` (sem `components` nem `details`); (d) `FR-011` `/actuator`, `/actuator/env`, `/actuator/beans`, `/actuator/info` e `/actuator/metrics` → 404.
- [X] T019 [P] [US2] Criar `src/test/java/com/outsera/awards/health/ReadinessInvalidLinesIT.java` (`scenarios/invalid-lines.csv`): `FR-010 US2-4` a readiness responde 200 `{"status":"UP"}` mesmo com as 11 linhas rejeitadas.
- [X] T020 [P] [US2] Criar `src/test/java/com/outsera/awards/health/ProbeStatesIT.java` (estende `ScenarioIT`, `@DirtiesContext`): (a) `FR-008` depois de `AvailabilityChangeEvent.publish(context, ReadinessState.REFUSING_TRAFFIC)`, a readiness → 503 `{"status":"OUT_OF_SERVICE"}`; (b) `FR-008` depois de `AvailabilityChangeEvent.publish(context, LivenessState.BROKEN)`, a liveness → 503 `{"status":"DOWN"}`. Um `@AfterEach` publica de volta `ACCEPTING_TRAFFIC` e `CORRECT` (research R2: API pública do Spring, sem dublê).
- [X] T021 [P] [US2] Criar `src/test/java/com/outsera/awards/health/ReadinessAfterLoadIT.java` (classe simples, com `ApplicationStarter.start(csvPath, listeners)`): (a) `FR-009 SC-003 US2-3` com `classpath:movielist.csv`, um `ApplicationListener<AvailabilityChangeEvent<ReadinessState>>` guarda, no evento `ACCEPTING_TRAFFIC`, a saída capturada até ali; o teste confere que ela já contém o resumo da carga (`LoaderLog.summary`) e que nenhum evento de readiness anterior foi `ACCEPTING_TRAFFIC`; um `ApplicationListener<ApplicationStartedEvent>` confere que, nesse momento, `ApplicationAvailability.getReadinessState()` é `REFUSING_TRAFFIC`; (b) `FR-010 US2-5` com `file:./nao-existe.csv`, a inicialização falha e o evento `ACCEPTING_TRAFFIC` nunca acontece.
- [X] T022 [P] [US3] Criar `src/test/java/com/outsera/awards/support/PrometheusText.java`: lê o texto do Prometheus e devolve as amostras (`nome`, mapa de rótulos, valor), com `static List<Sample> parse(String text)` e filtros por nome e rótulos.
- [X] T023 [US3] Criar `src/test/java/com/outsera/awards/metrics/MetricsIT.java` (estende `ScenarioIT`, arquivo padrão). As contagens são comparadas por diferença (antes e depois), sem depender da ordem dos testes. Métodos: (a) `FR-012 US3-1` `GET /actuator/prometheus` → 200, `Content-Type` compatível com `text/plain`, com `# TYPE http_server_requests_seconds histogram`; (b) `FR-013 US3-2` N=5 chamadas aos intervalos aumentam em 5 o `http_server_requests_seconds_count{uri="/api/producers/intervals",method="GET",status="200",outcome="SUCCESS"}`; (c) `FR-013 US3-3` uma chamada `GET /api/movies?winner=talvez` (400) e um `POST /api/movies` (405) aparecem com `status="400"` e `status="405"`, os dois com `outcome="CLIENT_ERROR"`; (d) `FR-013 US3-4` chamadas a `/api/movies?year=1990` e `/api/movies?page=3` caem na série `uri="/api/movies"`, e nenhum rótulo `uri` contém `?`, `year=` ou `page=`; (e) `FR-015 US3-5` chamadas a três caminhos inexistentes diferentes (`/nao-existe-1`, `/nao-existe-2`, `/x/y/z`) criam no máximo um valor novo de `uri`, nenhum `uri` contém `nao-existe`, e as três chamadas aparecem com esse mesmo valor de `uri` e `status="404"`; (f) `US3-6` uma revalidação com o ETag dos intervalos aparece com `status="304"` e `outcome` diferente de `CLIENT_ERROR` e `SERVER_ERROR`; (g) `FR-014 US3-7` existe `http_server_requests_seconds_bucket` para `uri="/api/producers/intervals"` com vários valores de `le`, incluindo `+Inf`, em ordem cumulativa (contagem não decrescente); (h) `FR-016` toda série `http_server_requests_seconds_count` tem `application="awards"`, e existem `jvm_memory_used_bytes`, `jvm_threads_live_threads` e `process_uptime_seconds` com `application="awards"`.
- [X] T024 [P] [US4] Criar `src/test/java/com/outsera/awards/logging/TextLogFormatIT.java`, anotada com `@DisabledIfEnvironmentVariable(named = "LOGGING_STRUCTURED_FORMAT_CONSOLE", matches = ".+")`, sem `@Tag("log")`: (a) `FR-020 US4-5` com `ApplicationStarter.start("classpath:movielist.csv")`, nenhuma linha da saída é JSON (`LogOutput.isJson`), e a linha do resumo está no padrão de texto do Boot, com nível `INFO` e o logger `MovieFileLoader`; (a2) `FR-020` a saída não contém o banner (nenhuma linha com `:: Spring Boot ::`); (b) `FR-019` com `ApplicationStarter.runAndCall`, um `GET /api/producers/intervals` com `Authorization: Bearer marcador-fr019-texto` não deixa `marcador-fr019-texto` na saída.
- [X] T025 [P] [US4] Criar `src/test/java/com/outsera/awards/logging/JsonLogFormatIT.java`, anotada com `@EnabledIfEnvironmentVariable(named = "LOGGING_STRUCTURED_FORMAT_CONSOLE", matches = "ecs")` e `@Tag("log")`: (a) `FR-017 FR-020 SC-005 US4-1` com `classpath:movielist.csv`, toda linha não vazia da saída é um JSON válido com `@timestamp`, `log.level`, `log.logger`, `process.thread.name`, `message` e `service.name` igual a `awards`, sem nenhuma linha do banner; (b) `FR-018 US4-2` com `classpath:scenarios/invalid-lines.csv`, os avisos de linha rejeitada e o resumo estão nas `message`, com as mesmas mensagens do `InvalidLinesIT` (inclusive os acentos de "ano não numérico"); (c) `FR-017 US4-3` com `file:./nao-existe.csv`, existe um único evento `ERROR` cuja `message` contém o relatório do `MovieFileFailureAnalyzer` inteiro (várias linhas, com o caminho `file:./nao-existe.csv`), e nenhuma linha da saída fica fora de um evento JSON; o rastro não é exigido, porque o analisador o substitui (spec, clarificação de 2026-11-23); (d) caso de borda: com `classpath:scenarios/comma-separated.csv`, a mensagem decodificada contém `";"` (aspas escapadas corretamente no JSON); (e) `FR-019` o mesmo teste do marcador do T024, com `marcador-fr019-json`.
- [X] T026 [US2] Rodar `./mvnw verify` e confirmar que os testes de US2 e US3 **falham** por falta do Actuator (404 nos caminhos `/actuator/**`). Os de US4 precisam falhar por causa do banner: (a2) do `TextLogFormatIT` e (a) do `JsonLogFormatIT`, na execução `json-logs`. O formato JSON em si já é nativo do Boot e não exige código (research R6); se algum outro teste de US4 passar já nesta etapa, registrar na descrição do commit. Nenhum teste do Marco 1 pode falhar. **Resultado:** 15 falhas na execução padrão (404 em `/actuator/**` e o banner no `TextLogFormatIT`). Na `json-logs`, o `JsonLogFormatIT` já passou: o Boot 4.1.2 omite o banner com o JSON ligado (research R6).

### Implementação da etapa 2

- [X] T027 [US2] No `pom.xml`, acrescentar `org.springframework.boot:spring-boot-starter-actuator` e `io.micrometer:micrometer-registry-prometheus`, sem versão (gerenciadas pelo Boot 4.1.2). **Verificar (research R1):** se o starter do Actuator já traz o módulo de métricas do Micrometer; se o `/actuator/prometheus` não aparecer, acrescentar o módulo que faltar e registrar o resultado na R1. **Resultado:** o starter já traz as métricas; nenhum módulo extra. A saúde geral traz também `groups` (liveness e readiness); com a decisão do responsável, a FR-011 foi ajustada antes do código.
- [X] T028 [US2] Em `src/main/resources/application.yml`, acrescentar o bloco `management` (research R1 e R3): `endpoints.web.exposure.include: health,prometheus`; `endpoints.web.discovery.enabled: false`; `endpoint.health.probes.enabled: true`; `endpoint.health.show-details: never`; `endpoint.health.show-components: never`; `metrics.tags.application: ${spring.application.name}`; `metrics.distribution.percentiles-histogram."[http.server.requests]": true`; `metrics.distribution.minimum-expected-value."[http.server.requests]": 1ms`; `metrics.distribution.maximum-expected-value."[http.server.requests]": 10s`. Acrescentar também `spring.main.banner-mode: off`, no bloco `spring` que já existe (FR-020, research R6). Não acrescentar nada de `logging.structured` (research R6).
- [X] T029 [US3] **Verificar (research R3):** se o rótulo `application` aparece com `management.metrics.tags.application` no Boot 4. Se não aparecer, remover a propriedade e criar `src/main/java/com/outsera/awards/web/MetricsConfiguration.java`, com um `MeterRegistryCustomizer<MeterRegistry>` que aplica `commonTags("application", <spring.application.name>)`. Registrar o resultado na R3. **Resultado:** a propriedade funciona; o `MetricsConfiguration` não foi criado.
- [X] T030 [US2] Rodar `./mvnw verify`: verde nas duas execuções do Failsafe. Comparar o tempo total com o anotado no T001; se passar de 3 minutos, reduzir as classes com `@Tag("log")` às que verificam mensagens (plano, riscos). Conferir no relatório `target/failsafe-reports-json-logs/` que as classes marcadas rodaram com o JSON ligado.
- [X] T031 [US2] Rodar a aplicação com `./mvnw spring-boot:run` e conferir à mão os passos 2, 4 e 5 do `quickstart.md` (texto sem a variável, JSON com `LOGGING_STRUCTURED_FORMAT_CONSOLE=ecs`). Registrar na research qualquer item "verificar" resolvido nesta etapa.
- [X] T032 [US2] Commit `Etapa 2: health, métricas e logs nos dois formatos` com T014 a T031.

**Checkpoint**: US2, US3 e US4 completas. O modo padrão continua idêntico para o avaliador, com o log em
texto.

---

## Phase 4: Etapa 3 — Dockerfile (US5, Priority: P3)

**Goal**: imagem multi-stage, JRE mínima, usuário não root, healthcheck na readiness e `.dockerignore`
por lista de permissão, sem nada disso no `./mvnw verify` (FR-021 a FR-027; research R8;
contracts/container.md).

**Independent Test**: o `smoke-test.sh` passa contra a imagem construída localmente (com Docker) ou no
job `image` da etapa 4 (sem Docker local).

### Verificação da etapa 3 (escrever primeiro; precisa falhar)

- [X] T033 [US5] Criar `.github/scripts/smoke-test.sh` (bash, `#!/usr/bin/env bash`, `set -euo pipefail`, executável; recebe o nome da imagem como `$1`), com os passos 1 a 13 de `contracts/ci-pipeline.md` (research R14). **Portabilidade:** o script roda no runner Linux do pipeline e, localmente, no Linux e no Git Bash do Windows com Docker Desktop. No Git Bash (`uname -s` começando com `MINGW` ou `MSYS`), ele exporta `MSYS_NO_PATHCONV=1` e converte o caminho de origem de cada `-v` com `cygpath -w`, numa função `host_path` usada por todos os bind mounts. Passos: (1) `docker run -d -p 8080:8080` e espera o `healthy` em `docker inspect --format '{{.State.Health.Status}}'` por até 90 s; (2) `curl` em `/api/producers/intervals` e comparação com `jq -S` contra `{"min":[{"producer":"Joel Silver","interval":1,"previousWin":1990,"followingWin":1991}],"max":[{"producer":"Matthew Vaughn","interval":13,"previousWin":2002,"followingWin":2015}]}`; (3) a mesma chamada com `If-None-Match` igual ao `ETag` recebido → código 304; (4) `docker exec <id> id -u` diferente de `0`; (5) toda linha de `docker logs` passa em `jq -e .`; (6) `docker stop -t 30` termina em menos de 10 s (medido com `date +%s%N`); (7) container com `-e PORT=9090 -p 9090:9090` fica `healthy` e responde na 9090; (8) container com `-e MOVIES_CSV_PATH=file:/nao-existe.csv` termina com código diferente de 0 (`docker wait`) e o log contém `file:/nao-existe.csv`; (9) container com `-v "$PWD/src/test/resources/scenarios/disjoint-producers.csv:/data/movies.csv:ro"` e `-e MOVIES_CSV_PATH=file:/data/movies.csv` responde `min` = `Producer A` (2, 1950, 1952) e `max` = `Producer B` (50, 1925, 1975); (10) `docker run --rm --entrypoint sh "$1" -c 'test ! -e /app/src && test ! -e /build && test ! -e /app/pom.xml && ! command -v mvn && ! command -v javac'` passa: a imagem final não tem código-fonte, Maven nem compilador (FR-021, US5-1); (11) `.github/scripts/check-docker-context.sh` passa (FR-026, T033a); (12) container com `-e LOGGING_STRUCTURED_FORMAT_CONSOLE=` fica `healthy` e a primeira linha do log não é JSON (FR-024a); (13) **arquivo sem permissão de leitura** (caso de borda, FR-025): copia `src/test/resources/scenarios/disjoint-producers.csv` para um arquivo temporário e aplica `chmod 000`; antes do teste, uma sonda (`docker run --rm --user 10001:10001 --entrypoint sh -v <cópia>:/data/movies.csv:ro "$1" -c 'test -r /data/movies.csv'`) confere se a permissão do host vale dentro do container. Se a sonda **não** consegue ler, o passo roda: container com esse mount e `-e MOVIES_CSV_PATH=file:/data/movies.csv` termina com `docker wait` diferente de 0, e o log contém `UNREADABLE` e "não é um arquivo legível". Se a sonda **consegue** ler, a permissão não se aplica nesse host (por exemplo, NTFS no Git Bash do Windows, onde o `chmod 000` não tem efeito, ou bind mounts do Docker Desktop, que expõem os arquivos com permissão aberta): fora do pipeline, o passo é **pulado** com a mensagem `PULADO (passo 13): a permissão do arquivo no host não se aplica ao bind mount neste ambiente (<uname -s>, Docker <versão>)`; no pipeline (`CI=true`), isso é **falha**, porque no runner Linux a permissão precisa valer. Um `trap` remove todos os containers e arquivos temporários no fim, mesmo com falha. Cada passo imprime o que verifica e o resultado (`OK`, `FALHA` ou `PULADO` com o motivo), e o script termina com um resumo dos passos.
- [X] T033a [US5] Criar `.github/scripts/check-docker-context.sh` (bash, `set -euo pipefail`, executável, com a mesma função `host_path` do T033 para o Git Bash): constrói, com o contexto da raiz e portanto com o mesmo `.dockerignore`, uma imagem descartável a partir de um Dockerfile passado pela entrada padrão (`docker build -f - .`, com `COPY . /ctx`). A base da sonda é **a mesma imagem do estágio de build** do `Dockerfile` (`eclipse-temurin:25-jdk-alpine@sha256:<digest>`), lida do próprio `Dockerfile` pelo script (a linha `FROM ... AS build`), e não um digest próprio. Assim, quando o Dependabot atualizar o digest no `Dockerfile`, a sonda acompanha sem manutenção à parte; lista `/ctx` com `find` e falha se aparecer qualquer caminho fora de `mvnw`, `.mvn/**`, `pom.xml` e `src/main/**`, citando em especial `src/test`, `docs`, `specs`, `.git` e `target` (FR-026). A imagem descartável é removida no fim. Antes do `.dockerignore` existir (T035), o script tem de falhar.
- [X] T034 [US5] Se houver Docker na máquina, rodar `.github/scripts/check-docker-context.sh`, `docker build -t awards:local .` e `.github/scripts/smoke-test.sh awards:local` e confirmar que falham (não há `.dockerignore` nem `Dockerfile`). Sem Docker local, registrar na descrição do commit que a verificação da imagem acontece no job `image` da etapa 4. **Resultado:** o daemon do Docker não estava rodando. Os dois scripts passaram no `bash -n`. O `check-docker-context.sh` falhou por falta do `.dockerignore`, e o `smoke-test.sh` parou por falta do `jq` local, com a mensagem clara. O build e o teste de fumaça ficam para o job `image` (T047).

### Implementação da etapa 3

- [X] T035 [P] [US5] Criar `.dockerignore` por lista de permissão (FR-026): primeira linha `*`, depois `!mvnw`, `!.mvn/`, `!pom.xml` e `!src/main/`. Nada de `src/test/`, `docs/`, `specs/`, `.git/` nem `target/` no contexto.
- [X] T036 [US5] Criar `Dockerfile` na raiz (research R8, contracts/container.md): estágio `build` em `eclipse-temurin:25-jdk-alpine@sha256:<digest>` (digest resolvido com `docker buildx imagetools inspect` e a tag num comentário; **resolvido pela API do registro do Docker Hub, sem o daemon**: `25-jdk-alpine` = `sha256:22edbb10…fa78`, `25-jre-alpine` = `sha256:69c2a26e…bd89`, índices para linux/amd64 e linux/arm64); `WORKDIR /build`; `COPY mvnw pom.xml ./` e `COPY .mvn .mvn`; `RUN --mount=type=cache,target=/root/.m2 sh ./mvnw -B -q dependency:go-offline`; `COPY src/main src/main`; `RUN --mount=type=cache,target=/root/.m2 sh ./mvnw -B -Dmaven.test.skip=true package`; extração em camadas com `java -Djarmode=tools -jar target/awards-*.jar extract --layers --launcher --destination extracted`. Estágio final em `eclipse-temurin:25-jre-alpine@sha256:<digest>`: `addgroup -S -g 10001 app` e `adduser -S -D -H -u 10001 -G app -s /sbin/nologin app`; `WORKDIR /app`; `COPY --from=build` das camadas `dependencies`, `spring-boot-loader`, `snapshot-dependencies` e `application`, nessa ordem; `ENV PORT=8080 LOGGING_STRUCTURED_FORMAT_CONSOLE=ecs`; `EXPOSE 8080`; `USER 10001:10001`; `HEALTHCHECK --interval=10s --timeout=3s --start-period=30s --retries=3 CMD wget -q -O /dev/null "http://127.0.0.1:${PORT}/actuator/health/readiness" || exit 1`; `ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "org.springframework.boot.loader.launch.JarLauncher"]`. Sem `MOVIES_CSV_PATH` (FR-024) e **sem `JAVA_TOOL_OPTIONS`**: com ela definida, a JVM escreve `Picked up JAVA_TOOL_OPTIONS` no stderr, fora do log, e o passo 5 do teste de fumaça falharia (research R8).
- [X] T037 [US5] **Feito localmente em 2026-11-24 (Git Bash, Docker 26.0.0), com a imagem do `Dockerfile` do commit `04b10a7`. (1) hadolint `v2.15.1-debian` (o mesmo digest da action fixada), `--failure-threshold warning`: sem avisos, depois da correção da DL3025 no `CMD` do healthcheck (`04b10a7`), sem nenhum `ignore`. (2) `wget` com 503 (R8): um servidor temporário (BusyBox `nc`, num container da própria imagem) respondeu um status fixo, e um segundo container da imagem, na mesma rede (`--network container:`), rodou o comando do `HEALTHCHECK` lido da imagem com `docker inspect`. Com 503, o código de saída foi 1 (`wget: server returned error: HTTP/1.1 503`); no controle com 200, foi 0. Nos dois casos, a requisição chegou em `GET /actuator/health/readiness`, pelo `PORT` da imagem. O healthcheck não precisa mudar. (3) Variável de log vazia: o container subiu `healthy` com o log em texto (passo 12 do teste de fumaça).** Se houver Docker local: rodar o hadolint (`docker run --rm -i hadolint/hadolint < Dockerfile`) sem nenhum aviso de nível `warning` ou `error` (exceções só com `# hadolint ignore=DLxxxx` e o motivo); **verificar (research R8)** que o `wget` da imagem devolve código diferente de zero para uma resposta de erro (por exemplo, `docker exec <id> wget -q -O /dev/null http://127.0.0.1:8080/actuator/nao-existe; echo $?`); se não devolver, trocar o healthcheck conforme a R8 e registrar. **Verificar (FR-024a, research R6):** `docker run --rm -e LOGGING_STRUCTURED_FORMAT_CONSOLE= awards:local` sobe e escreve o log em texto. Se o valor vazio falhar (por exemplo, como formato desconhecido), não alterar a spec por conta própria: registrar o erro observado na R6 e levar ao Anderson as alternativas (definir o formato na imagem por `ARG` de build em vez de `ENV`, ou documentar outro valor que o Boot aceite como texto), e ajustar o passo 12 do T033 e o `contracts/container.md` conforme a decisão.
- [X] T038 [US5] **Feito localmente em 2026-11-24 (Git Bash, Docker 26.0.0): 12 OK, 0 FALHA e o passo 13 PULADO (o `chmod 000` não vale no bind mount do Docker Desktop; roda no job `image`, T047). A primeira execução expôs defeitos do script (porta fixa no host, passos dependentes sem o container e o contexto de build no Git Bash), corrigidos num commit separado (`docs/ai/decisoes.md`). `./mvnw verify` verde, sem nenhum container iniciado.** Se houver Docker local, rodar `docker build -t awards:local .` e `.github/scripts/smoke-test.sh awards:local` até passar. Rodar `./mvnw verify`: verde e sem nenhum container iniciado (FR-027).
- [X] T039 [US5] Commit `Etapa 3: Dockerfile, .dockerignore e teste de fumaça` com T033 a T038 (inclusive o T033a). Se o passo 13 foi pulado localmente, registrar na descrição do commit a mensagem de `PULADO` e que ele roda no job `image` da etapa 4.

**Checkpoint**: US5 pronta para ser verificada no pipeline.

---

## Phase 5: Etapa 4 — Pipeline e Dependabot (US6, Priority: P2)

**Goal**: pipeline em todo push e PR, com build, testes, cobertura mínima de 90%, CodeQL, Trivy nas
dependências e na imagem (só com correção), hadolint, build da imagem e teste de fumaça; actions por hash
de commit e Dependabot para Maven, Actions e Docker (FR-028 a FR-029; research R7 e R9 a R14;
contracts/ci-pipeline.md).

**Independent Test**: os seis jobs ficam verdes num push do branch, e o branch temporário do T050 deixa
vermelho só o job de cada defeito (SC-008), sem que nenhum defeito chegue ao código do marco.

### Verificação da etapa 4 (escrever primeiro; precisa falhar)

- [X] T040 [US6] Criar `.github/scripts/check-workflows.sh` (bash, `set -euo pipefail`, executável; usa o `yq` que já vem nos runners `ubuntu-latest`). Antes de qualquer verificação, se `command -v yq` falhar, o script termina com código 2 e a mensagem `check-workflows.sh: o yq não foi encontrado no PATH. Instale o yq (https://github.com/mikefarah/yq) para rodar localmente; a verificação oficial é a do job workflow-policy do pipeline.` Ele nunca pula nem passa sem verificar. Para cada `.github/workflows/*.yml`, falha listando o arquivo e o motivo quando: (1) algum `uses:` que não começa com `./` não termina em `@` seguido de 40 caracteres hexadecimais (FR-028b, SC-007); (2) o `permissions` do topo não existe ou não é exatamente `contents: read`; (3) algum job declara `permissions` com escrita além de `security-events: write`, ou esse direito aparece fora do job `codeql`; (4) existe qualquer referência a `secrets.` (FR-028b: nenhum segredo); (5) algum passo de `docker/build-push-action` tem `push: true`, ou algum `run:` contém `docker push` (FR-028b: nenhuma publicação). Para o `.github/dependabot.yml`, falha quando ele não existe, quando `version` não é 2 ou quando falta algum dos ecossistemas `maven`, `github-actions` e `docker`, cada um com `directory: /` e `schedule.interval` definido (FR-029). Testar numa pasta temporária fora do repositório, com um caso que tem de falhar para cada regra (`actions/checkout@v4`; topo sem `permissions`; `contents: write`; `${{ secrets.X }}`; `push: true`; Dependabot sem `docker`) e um caso válido, que tem de passar. Não versionar os exemplos.
- [X] T041 [US6] (Implementador) No `pom.xml`, acrescentar a propriedade `<jacoco.line.minimum>0.90</jacoco.line.minimum>` e o `org.jacoco:jacoco-maven-plugin` 0.8.14 **depois** do Failsafe, com as execuções: `prepare-agent-integration` (fase padrão `pre-integration-test`); `report-integration` (fase `verify`); `check` (fase `verify`), com `<dataFile>${project.build.directory}/jacoco-it.exec</dataFile>` e a regra `BUNDLE` / `LINE` / `COVEREDRATIO` / `<minimum>${jacoco.line.minimum}</minimum>` (research R7). Conferir que o Failsafe não define `argLine` próprio. Rodar `./mvnw verify -Djacoco.line.minimum=0.99` e confirmar que o build **falha** no `check` com a mensagem de cobertura (prova de que o portão funciona). Depois rodar `./mvnw verify` e confirmar que passa, anotando a cobertura de linhas de `target/site/jacoco-it/jacoco.csv`. Se ela tiver caído mais de 3 pontos em relação aos 97,06% medidos, registrar o motivo em `docs/ai/decisoes.md`.

### Implementação da etapa 4

- [X] T042 [US6] Resolver os hashes de commit das versões estáveis mais recentes de `actions/checkout`, `actions/setup-java`, `actions/upload-artifact`, `actions/cache`, `github/codeql-action` (`init` e `analyze`), `docker/setup-buildx-action`, `docker/build-push-action`, `hadolint/hadolint-action` e `aquasecurity/trivy-action`, com `gh api repos/<owner>/<repo>/commits/<tag> --jq .sha` ou, sem o `gh`, com `curl -s https://api.github.com/repos/<owner>/<repo>/commits/<tag> | jq -r .sha`, no repositório oficial de cada uma (research R12). **Referências aninhadas (research R10):** para cada action composta, em especial a `aquasecurity/trivy-action`, ler o `action.yaml` da versão escolhida e confirmar que toda referência interna (`setup-trivy`, `actions/cache` e, dentro do `setup-trivy`, `actions/checkout`, `actions/cache/*` e o `ref` do `install.sh`) está por hash. A v0.36.0 foi verificada em 2026-11-23 e cumpre isso; se a versão escolhida for outra, repetir a verificação. Se alguma referência aninhada estiver por tag ou branch, não usar a action: instalar o binário oficial do Trivy da release, conferindo o SHA-256 contra o `trivy_<versão>_checksums.txt` da mesma release, num passo `run:`, e registrar a troca na R10. Anotar os pares tag → hash, e o resultado da verificação aninhada, na descrição do commit.
- [X] T043 [US6] Criar `.github/workflows/ci.yml` (research R9 a R14, contracts/ci-pipeline.md): `name: CI`; `on: push` (todos os branches) e `pull_request`; `permissions: contents: read` no topo; `concurrency: { group: ci-${{ github.workflow }}-${{ github.ref }}, cancel-in-progress: true }`; `timeout-minutes` em todo job; cada `uses:` no formato `owner/repo@<sha> # vX.Y.Z`. Jobs:
  - `workflow-policy`: checkout e `.github/scripts/check-workflows.sh` (FR-028b, FR-029, SC-007);
  - `build`: checkout, `setup-java` (`distribution: temurin`, `java-version: 25`, `cache: maven`), `./mvnw -B verify`, `upload-artifact` de `target/site/jacoco-it/`, `target/failsafe-reports/` e `target/failsafe-reports-json-logs/` (`if: always()`), e um passo que lê `target/site/jacoco-it/jacoco.csv` e escreve o percentual de linhas cobertas em `$GITHUB_STEP_SUMMARY`;
  - `codeql`: `permissions: { contents: read, security-events: write }`, `init` com `languages: java-kotlin`, `build-mode: none` e `queries: security-extended`; `analyze` com `output: sarif-results` e `upload` condicionado a não ser PR de fork; um passo com `jq` que falha se algum resultado do SARIF tiver regra com `properties."security-severity"` ≥ 7,0 e lista os achados no resumo;
  - `dependencies`: restaura o cache do banco do Trivy (`actions/cache`, chave com a data do dia); Trivy `scan-type: fs` no repositório com todas as severidades, `format: table` e `exit-code: 0` (relatório, no resumo); depois Trivy `fs` com `severity: HIGH,CRITICAL`, `ignore-unfixed: true` e `exit-code: 1` (porta);
  - `dockerfile-lint`: `hadolint/hadolint-action` com `dockerfile: Dockerfile` e `failure-threshold: warning`;
  - `image` (`needs: dockerfile-lint`): `.github/scripts/check-docker-context.sh` (FR-026); `setup-buildx-action`; `build-push-action` com `push: false`, `load: true`, `tags: awards:ci` e cache `type=gha`; Trivy `scan-type: image` em `awards:ci`, uma vez como relatório (todas as severidades, `exit-code: 0`) e outra como porta (`HIGH,CRITICAL`, `ignore-unfixed: true`, `exit-code: 1`); `.github/scripts/smoke-test.sh awards:ci`, medindo o tempo do fim do build até o passo 2 e falhando se passar de 120 s (SC-006).
  Nenhum `secrets.*` além do `GITHUB_TOKEN` implícito. Nenhum `push` de imagem.
- [X] T044 [P] [US6] Criar `.github/dependabot.yml` (research R13): `version: 2`; `updates` com `package-ecosystem: maven`, `github-actions` e `docker`, todos com `directory: /` e `schedule.interval: weekly`; grupo único para `github-actions`; no `maven`, grupo para atualizações `minor` e `patch` e as `major` fora do grupo.
- [X] T045 [US6] Rodar `.github/scripts/check-workflows.sh` na raiz. Com o `yq` instalado, ele tem de passar com o `ci.yml` e o `dependabot.yml` reais. Sem o `yq`, ele tem de terminar com código 2 e a mensagem clara do T040; nesse caso, anotar na descrição do commit que a verificação fica para o job `workflow-policy`, que é a **verificação oficial** (FR-028b, FR-029), conferida no T047. Rodar `./mvnw verify`: verde, com o `check` do JaCoCo.
- [X] T046 [US6] Commit `Etapa 4: pipeline, cobertura mínima e Dependabot` com T040 a T045.
- [ ] T047 [US6] **Pendente: exige o push do branch, que é do Anderson.** Pedir ao Anderson o push do branch `marco/2-operacao` (ou fazê-lo com a autorização dele) e acompanhar a execução do workflow `CI`: os seis jobs precisam ficar verdes. Se algum falhar, corrigir no mesmo branch, com `./mvnw verify` verde, num commit de correção da etapa 4. Se a imagem não tiver sido verificada localmente na etapa 3, este é o momento: o job `image` precisa passar nos treze passos, sem nenhum `PULADO`.
- [ ] T048 [US6] **Pendente: exige o push do branch, que é do Anderson.** Conferir na execução verde do T047: o tempo do teste de fumaça (SC-006), o percentual de cobertura no resumo do `build`, o relatório completo do Trivy (achados médios e baixos aparecem sem reprovar) e que o code scanning recebeu o SARIF do CodeQL. **Verificar (research R10):** se o repositório é público; se não for, registrar a limitação do code scanning em `docs/ai/decisoes.md`.
- [ ] T049 [US6] **Pendente: exige o push do branch, que é do Anderson.** Confirmação operacional (a comprovação da FR-029 é o job `workflow-policy`, T040): conferir que o Dependabot está ativo no repositório (aba Insights → Dependency graph → Dependabot) para os três ecossistemas, depois que o `dependabot.yml` chegar à `main`. Se a ativação depender de configuração do repositório, registrar o pedido ao Anderson.
- [ ] T050 [US6] **Não executada** antes do merge e da tag `v0.2.0`, por prazo de entrega. Ver `docs/ai/decisoes.md`, entrada "2026-11-24 — T050 do Marco 2 (SC-008) não executada". O SC-008 continua aberto; o roteiro abaixo vale sem mudança, a partir da `main`. Verificação do SC-008, **fora do código do marco**: num branch temporário criado a partir de `marco/2-operacao` (por exemplo, `verificacao/sc-008`), que **nunca é mergeado** e é **apagado** no fim, local e remotamente. Nenhum commit desse branch volta ao `marco/2-operacao`: nada de cherry-pick, merge ou rebase a partir dele, e nenhum arquivo dos defeitos fica no código do marco. O push do branch temporário é feito pelo Anderson ou com a autorização dele. Um commit por defeito, cada um revertido antes do próximo, confirmando que só o job esperado fica vermelho:
  1. um IT com uma asserção falsa → `build`;
  2. `jacoco.line.minimum` em `0.99` → `build`;
  3. uma dependência com vulnerabilidade crítica corrigida (por exemplo, `org.apache.commons:commons-text:1.9`, CVE-2022-42889) → `dependencies`;
  4. um `RUN cd /tmp` no Dockerfile (DL3003) → `dockerfile-lint`;
  5. um `ENTRYPOINT` com classe inexistente → `image`;
  6. um `uses:` por tag e, em outro commit, um `push: true` no `build-push-action` → `workflow-policy`;
  7. uma classe temporária `src/main/java/com/outsera/awards/web/VerificacaoSc008Controller.java` com um `GET` que monta SQL concatenando um parâmetro da requisição (`jdbcTemplate.queryForList("select * from movie where title = '" + title + "'")`), que deve gerar a regra `java/sql-injection` (`security-severity` 8,8) → `codeql`. Essa classe existe só no branch temporário.

  Depois de apagar o branch, conferir com `git branch -a` e `git log marco/2-operacao` que nenhum commit dele ficou no marco. Registrar em `docs/ai/decisoes.md`, numa entrada "Verificação do SC-008": o nome do branch, os links das execuções do pipeline, o job que ficou vermelho em cada defeito, o resultado esperado e o obtido, e a confirmação de que o branch foi apagado. Esse registro é a única coisa da verificação que entra no marco, no commit da etapa 5 ou da 6. O PR de verificação, se houver, é aberto pelo Anderson.

**Checkpoint**: US6 completa. O pipeline verde passa a ser o portão de merge do bolt.

---

## Phase 6: Etapa 5 — ADRs (US7, Priority: P3)

**Goal**: doze ADRs curtos em `docs/adr/`, com índice, cada um citando a entrada em
`docs/ai/decisoes.md` (FR-030 e FR-031; research R15).

**Independent Test**: a seção "Revisão dos ADRs" de `checklists/requirements.md` fica toda marcada na
revisão do PR.

### Verificação da etapa 5 (antes da escrita)

- [X] T051 [US7] Conferir que a seção "Revisão dos ADRs" de `specs/002-operacao/checklists/requirements.md` está toda desmarcada e que `docs/adr/` não existe. Essa seção é a comprovação da etapa (Princípio VI): nenhum item pode ser marcado antes de o ADR existir.

### Implementação da etapa 5

- [X] T052 [US7] Em `docs/ai/decisoes.md`, criar uma entrada "2026-11-23 — Decisões de arquitetura sem entrada própria (ADRs)" com uma subseção para cada decisão da R15 marcada como "entrada nova": stack (0001), H2 com JPA e Flyway (0002), Problem Details (0006), ETag (0007), probes (0008), métricas (0009) e imagem (0011). Cada subseção resume a decisão, o motivo e as alternativas, a partir do plano 001, da research 001 e da research 002, sem repetir o ADR inteiro.
- [X] T053 [P] [US7] Criar os ADRs do Marco 1 em `docs/adr/`: `0001-stack-java-spring-boot-maven.md`, `0002-h2-jpa-flyway.md`, `0003-carga-do-csv-na-inicializacao.md`, `0004-somente-testes-de-integracao.md`, `0005-contrato-openapi-estatico.md` e `0006-problem-details.md`. Cada um com título, `Status: Aceito`, data da decisão original, e as seções Contexto, Decisão, Alternativas consideradas, Consequências e Origem, com link para a entrada correspondente em `docs/ai/decisoes.md` (FR-031). No máximo uma página cada.
- [X] T054 [P] [US7] Criar os ADRs do Marco 2 em `docs/adr/`: `0007-etag-pelo-conteudo.md`, `0008-liveness-e-readiness.md`, `0009-metricas-prometheus.md`, `0010-formato-do-log.md`, `0011-imagem-de-container.md` e `0012-pipeline-e-cadeia-de-entrega.md`, no mesmo formato do T053. O 0010 cita a emenda 1.3.0 e a decisão do banner desligado. O 0011 registra por que a memória vai no `ENTRYPOINT`, e não em `JAVA_TOOL_OPTIONS`. O 0012 cita as entradas dos critérios do pipeline e da cobertura e a verificação das referências aninhadas da `trivy-action`.
- [X] T055 [US7] Criar `docs/adr/README.md` com o índice (número, título com link, status e data) dos doze ADRs e um parágrafo sobre o formato e a numeração (data-model, "ADR").
- [X] T056 [US7] Percorrer a seção "Revisão dos ADRs" do checklist como autoverificação (sem marcar os itens, que são do revisor) e rodar `./mvnw verify`: verde.
- [X] T057 [US7] Commit `Etapa 5: ADRs do Marco 1 e do Marco 2` com T051 a T056.

**Checkpoint**: US7 pronta para a revisão.

---

## Phase 7: Etapa 6 — README e fechamento do marco

**Goal**: o README documenta o que o Marco 2 acrescenta, e o bolt fica pronto para o PR (FR-034;
definição de pronto do `AGENTS.md`).

**Independent Test**: a seção "Revisão do README" de `checklists/requirements.md` fica toda marcada na
revisão do PR, e a matriz de rastreabilidade do plano bate com os testes.

### Verificação da etapa 6 (antes da escrita)

- [X] T058 Conferir que a seção "Revisão do README" de `specs/002-operacao/checklists/requirements.md` está desmarcada e listar, a partir do `README.md` atual, o que falta de cada item (ETag, endpoints operacionais, formato do log, imagem, pipeline, tabela de requisitos e ADRs).

### Implementação da etapa 6

- [X] T059 Atualizar `README.md` (FR-034), sem reescrever as seções do Marco 1: em "3. Como executar", a variável `LOGGING_STRUCTURED_FORMAT_CONSOLE` e uma subseção "Imagem Docker (opcional)" com os comandos de `contracts/container.md`; em "4. Endpoints", o ETag e o 304 nos dois endpoints e uma subseção "Endpoints operacionais" com a tabela de `contracts/operational-endpoints.md`; em "5. Testes", as duas execuções do Failsafe, a cobertura mínima de 90% e uma subseção "Pipeline" com os jobs e os critérios de reprovação de `contracts/ci-pipeline.md`; em "6. Requisitos do enunciado", uma tabela dos requisitos do Marco 2 com a comprovação de cada um, a partir da matriz do `plan.md`; em "8. Arquitetura" > "Stack", Actuator, Micrometer com Prometheus e o logging estruturado do Boot; em "9. Decisões", o link para `docs/adr/README.md`; em "11. Estrutura do repositório", `Dockerfile`, `.dockerignore`, `.github/` e `docs/adr/`. A seção "2. Avalie em 2 minutos" continua só com comandos do JDK.
- [X] T060 Num clone limpo do branch, numa pasta temporária, seguir os comandos da seção "2. Avalie em 2 minutos" do README e confirmar que funcionam só com o JDK (checklist "Revisão do README", terceiro item).
- [X] T061 Conferir a matriz de rastreabilidade do `specs/002-operacao/plan.md` contra o código: cada requisito de comportamento (FR-001 a FR-020) aparece no `@DisplayName` de pelo menos um IT (busca pelos IDs em `src/test/java`), e cada requisito de pipeline e imagem tem o job indicado no `ci.yml`. Corrigir a matriz ou os testes, se divergirem.
- [X] T062 Atualizar `specs/002-operacao/spec.md` (`**Status**: Implemented`) e a `research.md`, fechando os itens "verificar" com o resultado observado. Registrar em `docs/ai/decisoes.md` qualquer sugestão relevante aceita ou rejeitada durante o bolt que ainda não tenha entrada.
- [X] T063 Rodar `./mvnw verify`: verde. Conferir a definição de pronto do `AGENTS.md` (spec reflete o entregue, comprovação de cada requisito pelo seu tipo, contrato de intervalos idêntico ao do PDF, nenhum segredo nem dado pessoal, decisões registradas).
- [X] T064 Commit `Etapa 6: README do Marco 2` com T058 a T063. Avisar o Anderson de que o bolt está pronto para o PR. Depois do merge, a sessão é exportada para `docs/ai/sessoes/`, como manda o fluxo.

---

## Dependencies & Execution Order

### Entre etapas

- **Preparação (T001)** → **Etapa 1**.
- **Etapa 1 (ETag)** → **Etapa 2**: as duas mexem nos testes de apoio (`ApplicationStarter`) e a etapa 2
  usa o ETag no `MetricsIT` (US3-6).
- **Etapa 2 (observabilidade)** → **Etapa 3**: o healthcheck e o teste de fumaça usam a readiness, e o
  passo 5 exige o JSON. A etapa 3 pode ser **preparada** em paralelo pelo responsável de infraestrutura,
  numa cópia de trabalho própria, a partir dos contratos; o commit dela entra depois do da etapa 2.
- **Etapa 3 (Dockerfile)** → **Etapa 4**: o job `image` usa o `Dockerfile` e o `smoke-test.sh`.
- **Etapa 4 (pipeline)** → **Etapa 5** → **Etapa 6**. Os ADRs do Marco 1 (T053) podem ser preparados a
  qualquer momento; o commit da etapa 5 entra depois do da 4, porque o ADR 0012 descreve o pipeline
  pronto.

### Dentro de cada etapa

- Testes ou verificação → confirmação da falha → implementação → `./mvnw verify` verde → commit.
- Na etapa 2: apoio (T014 a T017) → testes (T018 a T025) → falha (T026) → implementação (T027 a T029) →
  verde (T030, T031) → commit (T032).

### Histórias

- US1 não depende de nenhuma outra.
- US2, US3 e US4 dependem só do apoio da etapa 2. US3-6 usa o ETag da US1.
- US5 depende de US2 (readiness) e de US4 (JSON na imagem).
- US6 depende de US5 (imagem) e da execução `json-logs` (US4).
- US7 não depende de código, mas o ADR 0012 descreve a US6.

## Parallel Opportunities

- **Etapa 1:** T002, T003 e T004 em paralelo (arquivos diferentes). T005 a T008 mexem em apoio e em
  testes existentes e vão em sequência.
- **Etapa 2:** T018, T019, T020, T021, T022, T024 e T025 em paralelo, depois do apoio. O T023 depende do
  T022.
- **Etapa 3:** T033 e T033a em paralelo (scripts diferentes); T035 (`.dockerignore`) em paralelo com o
  T036 (`Dockerfile`).
- **Etapa 4:** T044 (`dependabot.yml`) em paralelo com o T043 (`ci.yml`).
- **Etapa 5:** T053 e T054 em paralelo.
- **Entre papéis**, respeitando o limite de três agentes e o dono único de cada arquivo: o implementador
  na etapa 2, a infraestrutura preparando a etapa 3 e a documentação preparando os ADRs do Marco 1,
  cada um na sua cópia de trabalho.

### Exemplo: etapa 2, testes em paralelo

```text
Task: "T018 HealthProbesIT em src/test/java/com/outsera/awards/health/HealthProbesIT.java"
Task: "T020 ProbeStatesIT em src/test/java/com/outsera/awards/health/ProbeStatesIT.java"
Task: "T022 PrometheusText em src/test/java/com/outsera/awards/support/PrometheusText.java"
Task: "T025 JsonLogFormatIT em src/test/java/com/outsera/awards/logging/JsonLogFormatIT.java"
```

## Implementation Strategy

### MVP

A etapa 1 (US1, ETag) é o menor incremento com valor próprio: o único item que toca os endpoints de
negócio, e o que mais precisa provar que o contrato do enunciado não mudou. Ela fecha com commit e
`./mvnw verify` verde, mas **não é mergeada sozinha**: o marco é um único bolt e um único PR (R16), e um
extra só entra completo.

### Entrega incremental dentro do bolt

1. Etapa 1 → ETag provado, contrato 1.1.0.
2. Etapa 2 → probes, métricas e logs; modo padrão idêntico para o avaliador.
3. Etapa 3 → imagem e teste de fumaça.
4. Etapa 4 → pipeline verde, que passa a ser o portão de merge.
5. Etapa 5 → ADRs.
6. Etapa 6 → README e fechamento. O Anderson abre o PR, o Codex revisa, e o merge acontece com o
   pipeline verde.

## Notes

- Nenhum teste do Marco 1 pode ter o resultado esperado alterado (SC-002). Se um precisar mudar, a spec
  muda antes (Princípio IV).
- Nenhum IT liga o JSON por `@TestPropertySource`: o formato vem só da execução `json-logs` (research R6,
  risco do Logback global).
- Itens "verificar" da research (R1, R3, R8 e R10) são resolvidos nas tarefas T027, T029, T037 e T048, e o
  resultado volta para a research.
- Arquivos compartilhados: `pom.xml` e `application.yml` são do implementador nas etapas 2 e 4;
  `Dockerfile`, `.dockerignore` e `.github/` são da infraestrutura; `docs/adr/` e `README.md` são da
  documentação.
