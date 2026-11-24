---

description: "Tarefas do Marco 3, num único bolt com cinco etapas por assunto (um commit por etapa)"
---

# Tasks: Marco 3 — Modo Docker: PostgreSQL, ingestão por Kafka, autenticação e compose

**Input**: Design documents from `/specs/003-modo-docker/`

**Prerequisites**: plan.md, spec.md, research.md (R1 a R21), data-model.md, contracts/ (openapi.yaml,
openapi-1.2.0.md, kafka-events.md, operational-endpoints.md, compose.md, ci-pipeline.md), quickstart.md

**Tests**: obrigatórios, e **vêm antes da implementação em cada etapa**. Os testes de cada etapa
precisam **falhar** antes da implementação. A comprovação segue o Princípio VI (constituição 1.7.0):

- **comportamento da aplicação:** somente testes de integração (Princípio I). São classes `*IT`
  executadas pelo Failsafe, com `@SpringBootTest(webEnvironment = RANDOM_PORT)` (base `ScenarioIT`) ou
  com a aplicação real por `SpringApplication` (`ApplicationStarter`). Não há teste unitário, mock nem
  classe `*Test`. Cada método tem `@DisplayName` começando por `S003` e pelos IDs dos requisitos (plano,
  "Rastreabilidade");
- **comportamento que depende do PostgreSQL:** ITs `*PostgresIT`, no perfil Maven `postgres-it`,
  executados obrigatoriamente pelo job `postgres`;
- **compose e pipeline:** o `compose-test.sh` e os jobs;
- **documentação:** as seções de revisão de `checklists/requirements.md`.

**Organization**: **um único bolt** no branch `marco/3-docker`, com um único pull request, em **cinco
etapas por assunto**, na ordem definida pelo responsável: PostgreSQL e perfil `docker`, Kafka,
autenticação com Spring Security, compose com observabilidade, e ADRs com o README (research R21).
Cada etapa:

1. começa pelos testes, e confirma que eles falham;
2. termina com `./mvnw verify` **verde**, sem Docker;
3. fecha com **um commit** próprio, de assunto único.

O PR, o merge e a tag são do Anderson. Nenhum agente abre PR, faz merge, cria tag ou faz push sem
autorização.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: pode ser feita em paralelo (arquivos diferentes e sem dependência de tarefa incompleta).
- **[Story]**: história da spec 003:
  - US1: carga pelo Kafka;
  - US2: PostgreSQL e reinícios;
  - US3: novos vencedores e tópico de erros;
  - US4: autenticação;
  - US5: compose.
- Pacote base `com.outsera.awards`. Testes em `src/test/java/com/outsera/awards/`, cenários em
  `src/test/resources/scenarios/`.
- **Não altere nenhum `*IT.java` que exista na tag `v0.2.0`.** A única exceção aceita é a constante
  `SPEC_CONTRACT` do `ApiDocsIT` (T067). As classes de `support/` podem evoluir, desde que as assinaturas
  usadas pelos ITs antigos continuem iguais.
- Os pontos marcados como **verificar** na research são conferidos na tarefa que os usa. Se o resultado
  for outro, a research é atualizada no mesmo commit.

## Mapeamento etapa → fase

| Etapa | Fase | Histórias | Responsável (`AGENTS.md`) | Commit |
| --- | --- | --- | --- | --- |
| — | 1. Preparação | — | Implementador | não gera commit |
| 1. PostgreSQL e perfil `docker` | 2 | US2 | Implementador; job `postgres` pela infraestrutura | `Etapa 1: PostgreSQL, perfil docker e gravação com atualização` |
| 2. Kafka | 3 | US1, US3, US2 | Implementador | `Etapa 2: ingestão por Kafka com readiness e tópico de erros` |
| 3. Autenticação | 4 | US4 | Implementador; script de ITs inalterados pela infraestrutura | `Etapa 3: autenticação por token com Spring Security e contrato 1.2.0` |
| 4. Compose e observabilidade | 5 | US5 | Infraestrutura | `Etapa 4: compose com PostgreSQL, Kafka, Prometheus e Grafana` |
| 5. ADRs e README | 6 | — (FR-036, FR-037) | Documentação | `Etapa 5: ADRs e README do Marco 3` |

Toda mensagem de commit termina com a linha de atribuição indicada na sessão.

---

## Phase 1: Preparação

**Purpose**: linha de base para medir o que o marco muda.

- [ ] T001 Conferir a linha de base no branch `marco/3-docker`:
  - `git tag --list v0.2.0` existe;
  - `./mvnw verify` está verde;
  - anotar a cobertura de linhas de `target/site/jacoco-it/jacoco.csv` e o tempo total do `verify`.
  O tempo é a referência do limite de 2× da research R15. Os dois números vão para a descrição do
  commit da etapa 1.
- [ ] T002 Conferir se o Docker está disponível localmente (`docker info`). Sem Docker, os
  `*PostgresIT` e o `compose-test.sh` só podem ser verificados no pipeline. Nesse caso, as tarefas que
  pedem execução local registram `PULADO: Docker indisponível` na descrição do commit, e a verificação
  fica para as tarefas "Pendente: push".

---

## Phase 2: Etapa 1 — PostgreSQL e perfil `docker` (US2, Priority: P1) 🎯 MVP

**Goal**: perfil `postgres` com o mesmo esquema Flyway e grupo `docker`. O serviço único passa a criar
ou atualizar o filme, para que reinícios sobre um banco persistente não dupliquem nem falhem.

**Independent Test**: no perfil Maven `postgres-it`, com um PostgreSQL de collation `C`:
- o esquema é criado pela V1;
- o resultado de referência e a listagem são idênticos aos do H2, byte a byte;
- três subidas da carga direta sobre o mesmo esquema não duplicam nada.

### Testes da etapa 1 (escrever primeiro; precisam falhar)

- [ ] T003 [US2] Criar `src/test/java/com/outsera/awards/support/PostgresSupport.java`:
  - lê `DB_URL` (padrão `jdbc:postgresql://localhost:5432/awards`), `DB_USER` (padrão `awards`) e
    `DB_PASSWORD` (padrão vazio) do ambiente;
  - `newSchema(Class<?>)` gera `it_<nome da classe em minúsculas>_<8 caracteres aleatórios>`;
  - `args(String schema)` devolve os argumentos de subida:
    - `--spring.profiles.active=postgres`;
    - `--spring.datasource.url=<DB_URL>?currentSchema=<schema>`;
    - `--spring.flyway.default-schema=<schema>`, `--spring.flyway.create-schemas=true`;
    - `--DB_USER=…` e `--DB_PASSWORD=…`;
  - `assertIsPostgres(DataSource)` confere `getDatabaseProductName()` igual a `PostgreSQL`. Sem isso, um
    perfil ausente faria o teste passar no H2;
  - `dropSchema(String)` roda `DROP SCHEMA <schema> CASCADE` por JDBC depois da classe. É SQL de teste,
    fora da aplicação.
- [ ] T004 [US2] Em `src/test/java/com/outsera/awards/support/ApplicationStarter.java`:
  - acrescentar sobrecargas `start(String csvPath, List<String> extraArgs, ApplicationListener<?>...)` e
    `runAndCall(String csvPath, List<String> extraArgs, IntFunction<T> call)`;
  - acrescentar uma variante que expõe o `ConfigurableApplicationContext` ao `call`, para o
    `assertIsPostgres`;
  - as assinaturas atuais continuam iguais e delegam às novas com a lista vazia.
- [ ] T005 [P] [US2] Criar `src/test/java/com/outsera/awards/postgres/SchemaPostgresIT.java` (`@SpringBootTest`, `@ActiveProfiles("postgres")`, `@DynamicPropertySource` com o esquema do `PostgresSupport`):
  - `S003 FR-004 FR-005 US2-1`: o banco é PostgreSQL; `flyway_schema_history` do esquema tem a versão
    `1` com `success = true` (verificação por banco, porque a migração não tem endpoint); a subida passou
    pelo `ddl-auto: validate`;
  - `S003 FR-006 US2-2`: os intervalos trazem Joel Silver em `min` (1990–1991) e Matthew Vaughn em `max`
    (2002–2015), e `GET /api/movies` tem `totalElements` 206.
- [ ] T006 [P] [US2] Criar `src/test/java/com/outsera/awards/postgres/EquivalencePostgresIT.java` (múltiplas execuções, `ApplicationStarter`):
  - sobe com o arquivo padrão no H2 e depois no PostgreSQL;
  - em cada execução, guarda o corpo e o `ETag` de `GET /api/producers/intervals`, de
    `/api/movies?size=100&page=0..2`, de `/api/movies?winner=true` e de `/api/movies?year=1990`;
  - `S003 FR-006 SC-001`: os corpos são idênticos byte a byte, e os ETags também.
- [ ] T007 [P] [US2] Criar `src/test/java/com/outsera/awards/postgres/ListingOrderPostgresIT.java`:
  - mesmo método do T006, com `classpath:scenarios/adversario/13-ordenacao-com-acentos-e-caixa.csv` (o
    mesmo do `Case13AccentAndCaseOrderIT`);
  - `S003 FR-008`: a listagem completa tem a mesma ordem byte a byte no H2 e no PostgreSQL.
- [ ] T008 [P] [US2] Criar os cenários `src/test/resources/scenarios/postgres/restart-base.csv` e `restart-updated.csv`, com o cabeçalho `year;title;studios;producers;winner`:
  - `restart-base.csv` tem quatro filmes:
    - `2000;Base A;S;Producer A;yes`;
    - `2003;Base B;S;Producer A;` (indicado);
    - `2005;Base C;S;Producer A;yes`;
    - `2010;Base D;S;Producer Z;yes`;
  - `restart-updated.csv` tem as mesmas quatro linhas, com `Base B` como `yes`.
- [ ] T009 [US2] Criar `src/test/java/com/outsera/awards/postgres/RestartDirectPostgresIT.java` (carga direta, perfil `postgres` sem `kafka`, três execuções sobre o mesmo esquema):
  - `S003 FR-002 FR-014 US2-3`: duas subidas com `restart-base.csv` sobem sem erro, e as duas têm
    `totalElements` 4 e os mesmos intervalos (`Producer A`, 2000–2005, intervalo 5, em `min` e `max`);
  - `S003 FR-014 US2-5`: a terceira subida, com `restart-updated.csv`, tem `totalElements` 4, `min` com
    `Producer A` 2003–2005 (2) e `max` com `Producer A` 2000–2003 (3).
- [ ] T010 [US2] No `pom.xml` (infraestrutura de teste, research R16):
  - nas duas execuções atuais do Failsafe (padrão e `json-logs`), acrescentar
    `<excludes><exclude>**/*PostgresIT.java</exclude></excludes>` e `<skip>${it.default.skip}</skip>`,
    com a propriedade `it.default.skip` padrão `false`;
  - no `check-integration` do JaCoCo, acrescentar `<skip>${jacoco.check.skip}</skip>`, com o padrão
    `false`;
  - criar o perfil `postgres-it`:
    - `it.default.skip=true` e `jacoco.check.skip=true`;
    - uma execução `postgres` do Failsafe, com `<includes>**/*PostgresIT.java</includes>` e
      `reportsDirectory`/`summaryFile` em `target/failsafe-reports-postgres/`, sem ligar o paralelismo do
      JUnit (o `RetryPostgresIT`, T035a, não pode rodar em paralelo com outro IT).
- [ ] T011 [US2] Confirmar a falha:
  - `./mvnw verify` continua verde, e nenhum `*PostgresIT` roda;
  - com o Docker (T002), subir o PostgreSQL do quickstart, seção 3, e rodar `./mvnw -Ppostgres-it
    verify`. Os `*PostgresIT` precisam falhar no `assertIsPostgres` ou na conexão, porque ainda não
    existe `application-postgres.yml` nem driver.

### Implementação da etapa 1

- [ ] T012 [US2] No `pom.xml`, acrescentar `org.postgresql:postgresql` e `org.flywaydb:flyway-database-postgresql`, os dois em escopo `runtime`, com as versões do Boot. **Verificar (R3):** `./mvnw dependency:tree` mostra a versão do `flyway-database-postgresql` gerenciada. Se não mostrar, criar a propriedade `flyway.version`, igual à do `flyway-core`.
- [ ] T013 [P] [US2] Criar `src/main/resources/application-postgres.yml`:
  - `spring.datasource.url: ${DB_URL:jdbc:postgresql://postgres:5432/awards}`;
  - `username: ${DB_USER:awards}`;
  - `password: ${DB_PASSWORD}`, **sem padrão**, com um comentário que cita o Princípio VII 1.6.0 (a
    credencial de demonstração só pode estar no compose; research R3);
  - nenhum SQL e nenhuma propriedade do Hibernate específica de banco (FR-005).
- [ ] T014 [P] [US2] No `src/main/resources/application.yml`, acrescentar `spring.profiles.group.docker: [postgres, kafka]`, com um comentário de que o perfil `kafka` ganha configuração na etapa 2 (research R1).
- [ ] T015 [US2] Em `src/main/java/com/outsera/awards/movie/MovieRepository.java`, acrescentar `Optional<Movie> findByYearAndTitle(int year, String title)` (consulta derivada, portável). Em `src/main/java/com/outsera/awards/movie/Movie.java`, acrescentar `update(String studios, String producersRaw, boolean winner, Collection<Producer> producers)`, que substitui os campos e **todos** os vínculos (data-model.md, "Gravação de um filme"). As colunas não mudam: `title VARCHAR(500)`, `studios VARCHAR(1000)` e `producers VARCHAR(1000)`.
- [ ] T016 [US2] Em `src/main/java/com/outsera/awards/ingestion/MovieImportService.java` (research R5):
  - criar `@Transactional public void record(MovieRow row)`. Ele busca por `findByYearAndTitle`; se não
    existe, cria o filme; se existe, chama `update`. Os produtores continuam com `findOrCreate` pelo nome
    exato;
  - `importMovies(List<MovieRow>)` continua numa transação e passa a chamar `record` para cada linha;
  - o javadoc cita o Princípio V e a FR-014.
- [ ] T017 [US2] Rodar:
  - `./mvnw verify`: verde, com a suíte dos Marcos 1 e 2 sem alteração e a cobertura ≥ 90%;
  - com o Docker: `./mvnw -Ppostgres-it verify` verde. **Verificar (R4):** o `ListingOrderPostgresIT`
    passa com `--locale=C`. Se falhar, registrar a diferença na R4 antes de seguir.
- [ ] T018 [US2] (Infraestrutura) Em `.github/workflows/ci.yml`, criar o job `postgres` conforme `contracts/ci-pipeline.md`:
  - service container `postgres:18-alpine` fixado por digest (resolvido com `docker buildx imagetools
    inspect postgres:18-alpine` ou pela API do registro). A tag e o digest vão na descrição do commit;
  - `POSTGRES_HOST_AUTH_METHOD: trust` e `POSTGRES_INITDB_ARGS: --locale=C --encoding=UTF8`;
  - healthcheck `pg_isready`;
  - os passos de checkout e `setup-java` iguais aos do job `build`, com os mesmos hashes;
  - `./mvnw -B -Ppostgres-it verify`, com `DB_URL`, `DB_USER=awards` e `DB_PASSWORD` vazio;
  - `upload-artifact` de `target/failsafe-reports-postgres/` com `if: always()`;
  - `timeout-minutes: 20`;
  - sem `POSTGRES_PASSWORD` e sem `secrets.*`.

  Rodar `.github/scripts/check-workflows.sh` (com o `yq`; sem ele, anotar como no T045 do Marco 2).
- [ ] T019 [US2] Commit `Etapa 1: PostgreSQL, perfil docker e gravação com atualização` com T003 a T018. Na descrição, incluir os números do T001 e o resultado ou o `PULADO` do T011 e do T017.
- [ ] T020 [US2] **Pendente: exige o push, que é do Anderson.** Conferir o job `postgres` verde na execução do branch. Se falhar, corrigir no mesmo branch, num commit de correção da etapa 1.

**Checkpoint**: US2 sem Kafka completa. O modo padrão continua idêntico.

---

## Phase 3: Etapa 2 — Kafka (US1, US3 e reinícios da US2)

**Goal**:
- no perfil `kafka`, o arquivo é publicado no tópico e consumido pelo mesmo serviço de gravação;
- a readiness depende do marcador desta execução;
- eventos inválidos vão ao tópico de erros, e novos vencedores chegam pelo mesmo tópico;
- o Kafka não é configurado sem o perfil.

**Independent Test**: com `@EmbeddedKafka` e H2, no `./mvnw verify`:
- a carga por Kafka produz respostas idênticas às da carga direta;
- a readiness só fica positiva com o marcador;
- um vencedor novo aparece em até 5 s;
- cada evento inválido vai ao tópico de erros sem travar o consumo.

### Apoio da etapa 2

- [ ] T021 [US1] No `pom.xml`, acrescentar `org.springframework.kafka:spring-kafka` (compilação) e `org.springframework.kafka:spring-kafka-test` (teste), com as versões do Boot.
- [ ] T022 [US1] Criar `src/test/java/com/outsera/awards/support/KafkaTestSupport.java`, com um produtor e um consumidor **reais** (clientes Kafka) ligados ao broker embutido:
  - `topics(Class<?>)` devolve `awards.movies.<classe>` e `awards.movies.errors.<classe>`;
  - `args(EmbeddedKafkaBroker, Class<?>)` devolve `--spring.profiles.active=kafka`,
    `--KAFKA_BOOTSTRAP_SERVERS=…`, `--KAFKA_TOPIC_MOVIES=…`, `--KAFKA_TOPIC_ERRORS=…` e
    `--KAFKA_CONSUMER_GROUP=awards-<classe>`;
  - `send(topic, key, byte[] value)` e `sendJson(topic, key, String json)` esperam a confirmação;
  - `readAll(topic, Duration)` devolve registros com chave, valor em bytes e cabeçalhos;
  - `awaitHttp(Supplier<Boolean>, Duration)` repete uma condição HTTP até o prazo;
  - `awaitReady(port)` espera `/actuator/health/readiness` responder 200, com prazo de 30 s.

### Testes da etapa 2 (escrever primeiro; precisam falhar)

- [ ] T023 [P] [US1] Criar `src/test/java/com/outsera/awards/isolation/DefaultModeIsolationIT.java` (`@Tag("log")`; saída completa da subida pelo `ApplicationStarter`, sem perfil):
  - `S003 FR-001`: `GET /actuator/prometheus` não tem nenhuma linha que comece por `kafka_` nem por
    `awards_ingestion_`;
  - o log não contém `AdminClientConfig`, `ProducerConfig` nem `ConsumerConfig` (research R2);
  - os intervalos trazem o resultado de referência.
- [ ] T024 [P] [US1] Criar `src/test/java/com/outsera/awards/kafka/KafkaReferenceFileIT.java` (`@SpringBootTest`, `@ActiveProfiles("kafka")`, `@EmbeddedKafka(partitions = 1)`, `spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}` e tópicos da classe):
  - `S003 US1-1 FR-012`: depois da readiness, os intervalos trazem Joel Silver e Matthew Vaughn;
  - `S003 FR-010 FR-011`: o tópico de filmes tem 206 eventos de filme, com a chave `"<ano>|<título>"` e o
    cabeçalho `awards-run-id`, seguidos de um evento `{"type":"load-completed","runId":…,"count":206}`
    com o mesmo `runId`;
  - `S003 US1-6 FR-009`: o log não tem a mensagem da carga direta (`Carga do arquivo … concluída`).
- [ ] T025 [P] [US1] Criar `src/test/java/com/outsera/awards/kafka/KafkaEquivalenceIT.java` (`@EmbeddedKafka` na classe; duas execuções com o `ApplicationStarter`: carga direta e `kafka`, com o arquivo padrão; a de Kafka chama `awaitReady` antes de consultar):
  - `S003 FR-018 US1-2 SC-001`: os intervalos, `/api/movies?size=100&page=0..2`, `?winner=true` e
    `?year=1990` são idênticos byte a byte, inclusive o `ETag`;
  - repetir com `classpath:scenarios/adversario/13-ordenacao-com-acentos-e-caixa.csv`;
  - repetir com `classpath:scenarios/field-too-long.csv` (T034a), sem exceção (FR-018). As linhas
    rejeitadas por `FIELD_TOO_LONG` faltam nos dois caminhos, e as respostas são idênticas.
- [ ] T026 [P] [US1] Criar `src/test/java/com/outsera/awards/kafka/KafkaReadinessIT.java` (`@SpringBootTest`, perfil `kafka`, `awards.ingestion.publish-completion=false`; research R10):
  - `S003 FR-013 US1-3`:
    - depois de `totalElements` chegar a 206 na listagem, a readiness e `/actuator/health` continuam
      503 e a liveness 200;
    - o teste lê o `runId` no cabeçalho `awards-run-id` e envia um marcador com **outro** `runId`. A
      readiness continua 503 depois de 3 s;
    - depois, envia o marcador com o `runId` certo, e a readiness passa a 200 em até 5 s;
  - `S003 FR-016 US3-5`: com a aplicação pronta, envia um evento válido e um inválido; a readiness
    continua 200.
- [ ] T027 [P] [US1] Criar `src/test/java/com/outsera/awards/kafka/KafkaInvalidLinesIT.java` (`@Tag("log")`, `classpath:scenarios/invalid-lines.csv`, perfil `kafka`):
  - `S003 FR-009 US1-4`: cada linha rejeitada gera o aviso `Linha <n> rejeitada: <motivo>`, com os mesmos
    pares número e motivo do `InvalidLinesIT`;
  - o resumo `Publicação do arquivo <caminho> concluída: <lidas> linhas lidas, <aceitas> publicadas,
    <rejeitadas> rejeitadas` aparece com as contagens do `InvalidLinesIT`;
  - o tópico tem só os eventos das linhas aceitas, e a aplicação fica pronta.
- [ ] T028 [P] [US1] Criar `src/test/java/com/outsera/awards/kafka/KafkaStructuralFailureIT.java` (`@Tag("log")`, falha de inicialização pelo `ApplicationStarter` com os argumentos de Kafka):
  - `S003 FR-009 US1-5`: com arquivo ausente e com `scenarios/header-wrong-order.csv`, a subida falha com
    a mesma mensagem do `StartupFailureIT`;
  - o tópico de filmes fica vazio (`readAll` de 3 s sem registros).
- [ ] T029 [P] [US3] Criar `src/test/java/com/outsera/awards/kafka/KafkaNewWinnerIT.java` (perfil `kafka`, arquivo padrão):
  - `S003 FR-016 US3-1 SC-005`: depois da readiness, guarda o `ETag` dos intervalos. Envia
    `{"year":1991,"title":"Evento Novo","studios":null,"producers":"Producer X, Joel Silver and Producer Y","winner":true}`.
    Em até 5 s, `min` traz exatamente `{"producer":"Joel Silver","interval":0,"previousWin":1991,"followingWin":1991}`
    (validado contra o contrato);
  - `S003 US3-2`: a consulta com o `ETag` antigo em `If-None-Match` responde 200, com um `ETag` diferente;
  - `S003 US3-6 FR-012`: `/api/movies?year=1991` traz o filme com `producers` igual ao texto enviado.
    Os intervalos tratam `Producer X` e `Producer Y` como produtores distintos.
- [ ] T030 [P] [US3] Criar o cenário `src/test/resources/scenarios/kafka/update-base.csv` com o cabeçalho e as linhas:
  - `2000;Upd A;S;Producer A;yes`;
  - `2004;Upd B;S;Producer A;`;
  - `2008;Upd C;S;Producer A, Producer B;yes`;
  - `2012;Upd D;S;Producer B;yes`.

  Criar também `src/test/java/com/outsera/awards/kafka/KafkaUpdateIT.java`:
  - `S003 FR-014 US2-5`: um evento de `2004`/`Upd B` com `winner: true` deixa `Upd B` como vencedor na
    listagem, e `min` passa a `Producer A` 2004–2008 (4);
  - `S003 FR-014` (vencedor que vira indicado): um evento de `2012`/`Upd D` com `winner: false` tira o
    intervalo de `Producer B`, porque só resta a vitória de 2008;
  - `S003 FR-014` (produtor removido): um evento de `2008`/`Upd C` com `producers: "Producer A"` tira o
    vínculo de `Producer B`, e `Producer B` some das respostas;
  - em todos os casos, `totalElements` continua 4.
- [ ] T031 [P] [US3] Criar `src/test/java/com/outsera/awards/kafka/KafkaIdempotencyIT.java`:
  - `S003 FR-014 SC-004`: depois da readiness, o teste lê os 206 eventos de filme do tópico e os reenvia
    todos. `totalElements` continua 206, e os intervalos são os mesmos;
  - o mesmo evento novo enviado duas vezes aumenta `totalElements` em 1.
- [ ] T032 [P] [US3] Criar `src/test/java/com/outsera/awards/kafka/KafkaInvalidEventsIT.java` (`@Tag("log")`, perfil `kafka`, `@ParameterizedTest` com um caso por motivo do data-model.md):
  - os casos:
    - `NOT_UTF8`: bytes `0xC3 0x28`;
    - `NOT_JSON`: `não é json`;
    - `TYPE_UNKNOWN`: `"type":"other"`;
    - `RUN_ID_MISSING`: `{"type":"load-completed","count":1}`;
    - `YEAR_MISSING`;
    - `YEAR_NOT_NUMERIC`: `"year":"1990"` e `"year":1990.5`;
    - `YEAR_OUT_OF_RANGE`: `0` e `10000`;
    - `TITLE_MISSING`: `"   "`;
    - `PRODUCERS_MISSING`: `" , and "`;
    - `WINNER_INVALID`: `"yes"` e ausente;
    - `FIELD_TOO_LONG`: título de 501 caracteres e produtores de 1001;
  - `S003 FR-015 US3-3`: para cada caso, o tópico de erros recebe um registro com **os mesmos bytes**
    enviados e a mesma chave. O cabeçalho `awards-error-reason` começa pelo código do motivo, e os
    cabeçalhos `awards-error-at`, `awards-source-topic`, `awards-source-partition` e
    `awards-source-offset` estão presentes;
  - o log tem um aviso com o motivo e a posição e **não** contém o conteúdo enviado;
  - `S003 US3-4 SC-006`: um evento válido enviado logo depois do inválido aparece na listagem em até 5 s;
  - `S003 FR-017`: `awards_ingestion_events_total{outcome="rejected"}` aumenta 1 por caso.
- [ ] T033 [P] [US3] Criar `src/test/java/com/outsera/awards/kafka/KafkaMetricsIT.java`:
  - `S003 FR-017`: depois da readiness, `/actuator/prometheus` tem
    `awards_ingestion_events_total{outcome="recorded"}` igual a 206 e uma série de atraso do consumidor
    com o rótulo `topic` do tópico de filmes;
  - **Verificar (R19):** fixar no teste o nome exato que o Micrometer publicar e atualizar a R19 e
    `contracts/operational-endpoints.md` com ele.
- [ ] T034 [P] [US1] Criar `src/test/java/com/outsera/awards/kafka/PublisherSummaryIT.java` (`@Tag("log")`):
  - `S003 FR-009`: com `scenarios/load-summary.csv` e o perfil `kafka`, o log tem uma única linha de
    resumo da publicação, com as contagens do `LoadSummaryIT`.
- [ ] T034a [P] [US1] Criar o cenário `src/test/resources/scenarios/field-too-long.csv` e o IT `src/test/java/com/outsera/awards/ingestion/FieldTooLongIT.java` (carga direta, `@Tag("log")`; spec 001 FR-006 e FR-006c, atualizadas pela spec 003):
  - o cenário tem duas linhas válidas e uma linha inválida para cada limite: título de 501 caracteres,
    estúdios de 1001, produtores de 1001 e um nome de produtor de 501. Também tem um título de
    **exatamente** 500 caracteres, que é aceito;
  - `S003 FR-018` e `001 FR-006`: a subida não falha; cada linha inválida gera `Linha <n> rejeitada: um
    campo excede o tamanho da coluna (<campo>)`; o resumo conta 4 rejeitadas; o filme de 500 caracteres
    aparece na listagem.
- [ ] T034b [P] [US1] Criar `src/test/java/com/outsera/awards/kafka/KafkaBrokerUnavailableIT.java` (`ApplicationStarter`, perfil `kafka`, `KAFKA_BOOTSTRAP_SERVERS` apontando para uma porta local que o teste abre com `ServerSocket(0)` e fecha logo depois, sem broker):
  - `S003 FR-013` (caso de borda "Broker indisponível"): a aplicação sobe, e durante 10 s a liveness
    responde 200 e a readiness 503;
  - **verificar:** o tempo de subida com o broker fora (`KafkaAdmin` e o publicador, research R8). Se a
    subida falhar ou passar de 60 s, ajustar `spring.kafka.admin.fail-fast: false` e
    `spring.kafka.admin.operation-timeout` no `application-kafka.yml` e registrar na R8.
- [ ] T034c [US3] **Verificar antes** (research R9, "Tópico de erros fora do ar"): com o broker embutido, subir o perfil `kafka` com `KAFKA_TOPIC_ERRORS="tópico inválido!"` e anotar se o `NewTopic` desse nome derruba a subida. Depois, criar `src/test/java/com/outsera/awards/kafka/KafkaErrorTopicUnavailableIT.java` (`@Tag("log")`) pela saída que a verificação indicar:
  - se a subida não cai, usar o nome inválido;
  - se cai, usar `awards.ingestion.create-error-topic=false` com um nome válido não criado e a criação
    automática de tópicos desligada no `@EmbeddedKafka` (`brokerProperties = "auto.create.topics.enable=false"`);
  - `S003 FR-015` (caso de borda "Tópico de erros indisponível"):
    - um evento inválido não é gravado;
    - o log tem o aviso com o motivo e a falha do envio ao tópico de erros;
    - um evento válido enviado em seguida aparece na listagem em até 15 s;
    - a readiness continua 200.

  Se nenhuma das duas saídas for possível sem dublê, não criar o IT e registrar o limite em
  `docs/ai/decisoes.md` (T048).
- [ ] T035 [US2] Criar `src/test/java/com/outsera/awards/postgres/RestartPostgresIT.java` (`postgres-it`; `@EmbeddedKafka` na classe; **quatro execuções** do `ApplicationStarter`, ou seja, três reinícios, como o SC-004 pede; `PostgresSupport.args` + `KafkaTestSupport.args`, mesmo esquema e mesmo grupo):
  - execução 1, arquivo padrão: depois da readiness, o teste envia um filme novo
    (`2030`/`Filme Novo`, `Joel Silver`, vencedor) e uma atualização de um filme do arquivo
    (`1980`/`Can't Stop the Music`, com `winner: false`), e espera os dois aparecerem;
  - execução 2:
    - `S003 US2-4`: `Filme Novo` continua;
    - `S003 US2-6`: `Can't Stop the Music` voltou a `winner: true`, o valor do arquivo;
  - execuções 2, 3 e 4: `S003 US2-3 SC-004`: `totalElements` 207 e nenhum filme repetido na listagem
    completa.
- [ ] T035a [US2] Criar `src/test/java/com/outsera/awards/postgres/RetryPostgresIT.java` (`postgres-it`, `@Isolated` do JUnit, `@EmbeddedKafka`, perfil `postgres` + `kafka`; research R9, "Comprovação da nova tentativa"):
  - `S003 FR-015a`: prova a **nova tentativa quando a gravação falha**, e não a queda real do banco
    (essa é o passo 7b do T071);
  - com a aplicação pronta, o teste renomeia `movie` para `movie_retry_off` no esquema da classe, por
    JDBC, e publica `{"year":2032,"title":"Durante a Falha","studios":null,"producers":"Producer R","winner":true}`;
  - durante 5 s, a readiness continua 200, o tópico de erros não recebe nada e o log tem a nova
    tentativa do `DefaultErrorHandler`;
  - **num `finally`**, renomeia a tabela de volta para `movie`, mesmo que uma asserção falhe;
  - depois, o filme aparece na listagem em até 60 s, e `awards_ingestion_events_total{outcome="rejected"}`
    não mudou;
  - **sem paralelismo:** a execução `postgres` do Failsafe (T010) não liga o paralelismo do JUnit, e o
    `@Isolated` garante isso mesmo que alguém o ligue.
- [ ] T036 [US1] Confirmar a falha: `./mvnw verify` falha em todos os ITs de `kafka/`, porque não há
  ingestão por Kafka e a carga direta roda sempre. O `FieldTooLongIT` (`ingestion/`, T034a) também falha,
  porque a subida cai com o erro do banco enquanto a regra `FIELD_TOO_LONG` não existe. O `DefaultModeIsolationIT` falha porque a
  autoconfiguração do Kafka cria os clientes, ou passa. Anotar qual dos dois aconteceu. Se passar, ele
  continua como guarda para as tarefas seguintes.

### Implementação da etapa 2

- [ ] T037 [US1] Isolamento (research R2):
  - no `src/main/resources/application.yml`, acrescentar `spring.autoconfigure.exclude` com a
    autoconfiguração do Kafka. **Verificar o nome totalmente qualificado no Boot 4.1.2** no JAR
    `spring-boot-kafka`;
  - criar `src/main/resources/application-kafka.yml`:
    - redefine `spring.autoconfigure.exclude` sem o Kafka;
    - `spring.kafka.bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS:kafka:9092}`;
    - produtor: `acks: all` e `enable.idempotence: true`, com serializadores `String` (chave) e
      `ByteArray` (valor);
    - consumidor: `group-id: ${KAFKA_CONSUMER_GROUP:awards}`, `auto-offset-reset: earliest`,
      desserializadores `String` e `ByteArray`;
    - `listener.concurrency: 1` e `listener.ack-mode: record`;
    - `awards.ingestion.topic-movies: ${KAFKA_TOPIC_MOVIES:awards.movies}`;
    - `awards.ingestion.topic-errors: ${KAFKA_TOPIC_ERRORS:awards.movies.errors}`;
    - `awards.ingestion.publish-completion: true`;
    - `management.endpoint.health.group.readiness.include: readinessState,ingestion` (**verificar
      R10**).
- [ ] T038 [P] [US1] Criar `src/main/java/com/outsera/awards/ingestion/MovieFieldRules.java`, com estas regras:
  - ano de 1 a 9999, sem estouro de `int`;
  - título não vazio;
  - produtores com ao menos um nome pelo `ProducerNameParser`;
  - **tamanho máximo**: `title` ≤ 500, `studios` ≤ 1000, `producers` ≤ 1000 e cada nome de produtor
    ≤ 500 caracteres (`VARCHAR(500)` e `VARCHAR(1000)` da V1).

  Mover para ela as regras de `MovieCsvReader.validate`, que passa a usá-la sem mudar a ordem nem os
  motivos existentes (os ITs do Marco 1 provam que nada mudou). Acrescentar `FIELD_TOO_LONG("um campo
  excede o tamanho da coluna")` ao `RejectionReason`, **antes de `DUPLICATE`** (spec 001, data-model,
  motivo 8). O detalhe da rejeição cita o campo. O parser de eventos (T041) usa a mesma classe, para que
  os dois caminhos rejeitem as mesmas linhas.
- [ ] T039 [P] [US1] Criar `src/main/java/com/outsera/awards/ingestion/MovieFileReading.java`, com a verificação de existência e de leitura do arquivo, o `MovieCsvReader.read` e os avisos `Linha {} rejeitada: {}`, com o mesmo texto e o mesmo logger de hoje (`MovieFileLoader`). Em `MovieFileLoader.java`:
  - pôr `@Profile("!kafka")`;
  - usar `MovieFileReading`;
  - manter o resumo `Carga do arquivo {} concluída: {} linhas lidas, {} aceitas, {} rejeitadas`, com o
    logger atual.

  **Verificar:** os testes do Marco 1 que procuram o logger pelo nome continuam passando.
- [ ] T040 [P] [US1] Criar em `src/main/java/com/outsera/awards/ingestion/kafka/`:
  - `KafkaIngestionProperties` (`@ConfigurationProperties("awards.ingestion")`: `topicMovies`,
    `topicErrors`, `publishCompletion`);
  - `EventRejectionReason` (enum com os códigos e as descrições do data-model.md). Os motivos de
    conteúdo reaproveitam as descrições do `RejectionReason` (inclusive `FIELD_TOO_LONG`), e os de
    representação são `NOT_UTF8`, `NOT_JSON`, `TYPE_UNKNOWN` e `RUN_ID_MISSING`;
  - `LoadCompletedEvent` (record `runId`, `count`).

  Todos com `@Profile("kafka")` onde se aplica.
- [ ] T041 [US1] Criar `src/main/java/com/outsera/awards/ingestion/kafka/MovieEventParser.java` (research R7, data-model.md):
  - entrada em `byte[]`, decodificada em UTF-8 estrito;
  - objeto JSON lido pelo `JsonMapper` do Boot, ignorando campos desconhecidos;
  - `type` ausente ou `movie` → filme; `load-completed` → marcador (`runId` obrigatório); outro →
    `TYPE_UNKNOWN`;
  - `year` inteiro JSON (texto ou decimal → `YEAR_NOT_NUMERIC`), regras pela `MovieFieldRules`;
  - `title` com `trim`;
  - `studios` vazio ou nulo → `null`;
  - `producers` obrigatório;
  - `winner` booleano JSON obrigatório;
  - por último, o tamanho máximo pela **mesma** `MovieFieldRules` do CSV (`FIELD_TOO_LONG`: "`title` >
    500, `studios` > 1000, `producers` > 1000 ou um nome de produtor > 500 caracteres");
  - devolve um resultado selado: `Movie(MovieRow)`, `Completed(LoadCompletedEvent)` ou
    `Rejected(EventRejectionReason, String detalhe)`. O `lineNumber` do `MovieRow` é `0` para eventos.
- [ ] T042 [US1] Criar `src/main/java/com/outsera/awards/ingestion/kafka/IngestionReadiness.java` (`HealthIndicator` de nome `ingestion`, research R10):
  - `OUT_OF_SERVICE` até `complete(runId)` receber o `runId` desta execução, e `UP` depois;
  - outro `runId` é registrado em DEBUG e ignorado;
  - expõe `runId()` para o publicador.
- [ ] T043 [US1] Criar `src/main/java/com/outsera/awards/ingestion/kafka/MovieEventPublisher.java` (research R8):
  - em `afterSingletonsInstantiated`, lê com `MovieFileReading`; os erros estruturais interrompem a
    subida;
  - no `ApplicationReadyEvent`, publica num fio próprio:
    - um evento por linha aceita: chave `"<ano>|<título>"`, cabeçalho `awards-run-id` e JSON
      `{year,title,studios,producers,winner}`, com `winner` booleano;
    - se `publishCompletion`, o marcador;
    - cada envio espera a confirmação;
  - broker indisponível: novas tentativas com espera crescente até 30 s, sem derrubar a aplicação;
  - no fim, registra `Publicação do arquivo {} concluída: {} linhas lidas, {} publicadas, {} rejeitadas`.
- [ ] T044 [US1] Criar `src/main/java/com/outsera/awards/ingestion/kafka/MovieEventListener.java` (research R9):
  - `@KafkaListener` no tópico de filmes, com `ConsumerRecord<String, byte[]>`;
  - filme → `MovieImportService.record` e contador `awards.ingestion.events` com `outcome=recorded`;
  - marcador → `IngestionReadiness.complete`;
  - rejeitado:
    - envia ao tópico de erros os bytes originais, a mesma chave e os cabeçalhos do data-model.md,
      esperando até 10 s;
    - registra `Evento rejeitado (tópico {}, partição {}, offset {}): {}`, sem o conteúdo;
    - incrementa `outcome=rejected`;
    - se o envio ao tópico de erros falhar, registra a falha e segue.
- [ ] T045 [US1] Criar `src/main/java/com/outsera/awards/ingestion/kafka/KafkaIngestionConfiguration.java` (`@Configuration`, `@Profile("kafka")`):
  - `NewTopic` para os dois tópicos (1 partição, replicação 1). O do tópico de erros fica atrás de
    `awards.ingestion.create-error-topic` (padrão `true`) só se o T034c indicar essa saída;
  - `DefaultErrorHandler` com `ExponentialBackOff` (inicial 500 ms, multiplicador 2, máximo 30 s, sem
    limite de tentativas) para as exceções que escapam do ouvinte, ou seja, as falhas de infraestrutura;
  - registro dos contadores com o rótulo `application`.
- [ ] T046 [US1] Rodar `./mvnw verify` até ficar verde:
  - todos os ITs de `kafka/`, o `DefaultModeIsolationIT` e o `FieldTooLongIT` (`ingestion/`);
  - a suíte dos Marcos 1 e 2 sem alteração;
  - a execução `json-logs` com os ITs `@Tag("log")` novos;
  - a cobertura ≥ 90%.

  **Verificar (R15):** se o tempo do `verify` passar de 2× o do T001, compartilhar o broker por execução
  do Failsafe e manter os tópicos por classe. Se o broker embutido falhar no Windows ao apagar os logs,
  aplicar a saída da R15 e registrar o motivo.
- [ ] T047 [US2] Com o Docker: `./mvnw -Ppostgres-it verify` verde, inclusive o `RestartPostgresIT` e o `RetryPostgresIT`. Sem Docker, registrar `PULADO`.
- [ ] T048 [US1] Registrar em `docs/ai/decisoes.md`:
  - o resultado da verificação do T034c e a saída escolhida, ou o limite, se nenhuma for possível;
  - o resultado da verificação do T034b (tempo de subida com o broker fora).
- [ ] T049 [US1] Commit `Etapa 2: ingestão por Kafka com readiness e tópico de erros` com T021 a T048.
- [ ] T050 [US1] **Pedido ao Anderson:** acionar o testador adversário (Codex) para a ingestão por Kafka. Ele cria eventos de ataque a partir da spec 003 e de `contracts/kafka-events.md`, sem ler o código de produção. Cada falha encontrada vira IT permanente em `src/test/java/com/outsera/awards/kafka/adversario/`, com a correção, num commit de correção da etapa 2.
- [ ] T051 [US2] **Pendente: exige o push, que é do Anderson.** Conferir o job `postgres` verde com o `RestartPostgresIT` e o `RetryPostgresIT`.

**Checkpoint**: US1, US3 e a persistência da US2 completas no `./mvnw verify` e no perfil `postgres-it`.

---

## Phase 4: Etapa 3 — Autenticação com Spring Security (US4, Priority: P2)

**Goal**: token por `API_TOKEN`, desligado por padrão. Com ele ligado, os intervalos, a listagem e as
métricas exigem `Authorization: Bearer`, e a resposta sem token é 401 em Problem Details. O ETag é
preservado, sem senha gerada e com o contrato 1.2.0.

**Independent Test**: sem `API_TOKEN`, a suíte dos Marcos 1 e 2 passa sem alteração. Com um token
aleatório:
- 401 sem token e com token errado;
- 200 com o token certo;
- probes e documentação públicas;
- nenhum token no log.

### Testes da etapa 3 (escrever primeiro; precisam falhar)

Os ITs com a autenticação ligada geram o token em tempo de execução (`UUID.randomUUID()` por
`@DynamicPropertySource` em `awards.security.token`). Nenhum token literal no código (Princípio VII).

- [ ] T052 [P] [US4] Criar `src/test/java/com/outsera/awards/security/AuthDisabledByDefaultIT.java` (research R14):
  - `S003 SC-002` (guarda): falha, com a mensagem "API_TOKEN definido no ambiente: a suíte dos Marcos 1 e
    2 estaria rodando com a autenticação ligada", se `System.getenv("API_TOKEN")` não for vazio ou se
    `awards.security.token` do contexto não for vazio;
  - `S003 FR-019 FR-022 US4-1`: sem token, os intervalos, a listagem, `/actuator/prometheus`, as probes,
    `/swagger-ui.html` e `/openapi.yaml` respondem 200, sem `WWW-Authenticate`;
  - um `Authorization: Bearer qualquer` é ignorado e a resposta é 200.
- [ ] T053 [P] [US4] Criar `src/test/java/com/outsera/awards/security/AuthEnabledIT.java`:
  - `S003 FR-020 FR-021 US4-2 US4-3 US4-4`: nos intervalos, na listagem e em `/actuator/prometheus`,
    sem token → 401, `Bearer errado` → 401, `Basic …` → 401 e o token certo → 200;
  - nos intervalos e na listagem com o token, o corpo é o do Marco 2 (Joel Silver e Matthew Vaughn), e
    a revalidação com o `ETag` dá 304;
  - `POST /api/movies` sem token → 401;
  - `S003` (caso de borda `OPTIONS`): com o token ligado, `OPTIONS /api/movies` e `OPTIONS
    /api/producers/intervals` sem token → 401. Com o token desligado (`AuthDisabledByDefaultIT`), o
    mesmo `OPTIONS` responde como no Marco 2 (2xx com `Allow`);
  - `S003 FR-022 US4-5`: liveness, readiness, `/actuator/health`, `/swagger-ui.html`, `/openapi.yaml`,
    um webjar referenciado pela página e um caminho inexistente (404) respondem sem token;
  - o token com espaços em volta ou com outra caixa → 401.
- [ ] T054 [P] [US4] Criar `src/test/java/com/outsera/awards/security/AuthProblemDetailsIT.java`:
  - `S003 FR-023`: o 401 tem `Content-Type: application/problem+json`, `WWW-Authenticate: Bearer
    realm="awards"` e o corpo `{type: about:blank, title: Unauthorized, status: 401, detail: "Token
    ausente ou inválido", instance: <caminho>}`;
  - o corpo é idêntico sem token e com token errado, e não contém o token esperado nem o enviado.
- [ ] T055 [P] [US4] Criar `src/test/java/com/outsera/awards/security/TokenNotLoggedIT.java` (`@Tag("log")`, saída completa da subida pelo `ApplicationStarter`, token aleatório):
  - `S003 FR-025 US4-6`: faz chamadas com o token certo e com um token errado aleatório;
  - nenhum dos dois valores aparece no log nem em `/actuator/prometheus` (consultado com o token).
- [ ] T056 [P] [US4] Criar `src/test/java/com/outsera/awards/security/NoGeneratedPasswordIT.java` (`@Tag("log")`, duas subidas pelo `ApplicationStarter`, sem token e com token):
  - `S003 FR-019`: o log não contém `Using generated security password` nem `UserDetailsService`.
- [ ] T057 [P] [US4] Criar `src/test/java/com/outsera/awards/security/CacheHeadersIT.java` (duas classes aninhadas, token desligado e ligado; research R12):
  - `S003 FR-022a FR-019 FR-021`: as respostas 200 e 304 dos intervalos e da listagem não têm `Cache-Control`,
    `Pragma` nem `Expires` e têm `ETag`;
  - `X-Content-Type-Options: nosniff` e `X-Frame-Options: DENY` aparecem nos endpoints de negócio, na
    readiness, em `/openapi.yaml` e no 401;
  - `/actuator/prometheus` e o 401 têm `Cache-Control` com `no-store`.
- [ ] T058 [P] [US4] Criar `src/test/java/com/outsera/awards/security/AuthContractIT.java`:
  - `S003 FR-026`: com o token ligado, o 401 dos dois endpoints e as respostas 200 com o token são
    validados pelo `ContractValidator` contra o contrato publicado;
  - o `ContractValidator` (`support/`) ganha uma sobrecarga que repassa o cabeçalho `Authorization`,
    sem mudar as assinaturas atuais.
- [ ] T059 [P] [US4] Criar `src/test/java/com/outsera/awards/docs/ApiDocsSecurityIT.java`:
  - `S003 FR-026`: o `/openapi.yaml` publicado tem `info.version` 1.2.0 e
    `components.securitySchemes.bearerAuth` (`type: http`, `scheme: bearer`);
  - os dois endpoints têm `security` igual a `[{}, {bearerAuth: []}]` e a resposta `401` referenciando
    `#/components/responses/Unauthorized`.
- [ ] T060 [US4] (Infraestrutura) Criar `.github/scripts/check-unchanged-tests.sh <tag>` (bash, `set -euo pipefail`, executável), conforme `contracts/ci-pipeline.md`:
  - lista os `src/test/java/**/*IT.java` da tag com `git ls-tree -r --name-only`;
  - ignora os listados em `.github/unchanged-tests-exceptions.txt`, no formato `<caminho> <título da
    entrada>`, e falha se o título não existir em `docs/ai/decisoes.md`;
  - roda `git diff --quiet <tag> -- <arquivo>` em cada um;
  - falha listando os arquivos apagados ou alterados.

  Criar `.github/unchanged-tests-exceptions.txt` com uma única linha:
  ``src/test/java/com/outsera/awards/docs/ApiDocsIT.java Exceção do `ApiDocsIT` à regra dos testes sem alteração``.
  O título é copiado exatamente do `docs/ai/decisoes.md`, **com as crases**, e o script compara o texto
  exato.
  Testar numa cópia temporária, sem versionar: alterar um IT da tag faz o script falhar; o `ApiDocsIT`
  alterado passa; uma exceção com título inexistente falha.
- [ ] T061 [US4] Confirmar a falha: `./mvnw verify` falha no `AuthEnabledIT`, no `AuthProblemDetailsIT`, no `AuthContractIT`, no `ApiDocsSecurityIT` e nos ITs de cabeçalho, porque não há segurança nem contrato 1.2.0. O `AuthDisabledByDefaultIT` passa. `.github/scripts/check-unchanged-tests.sh v0.2.0` passa.

### Implementação da etapa 3

- [ ] T062 [US4] No `pom.xml`, acrescentar `spring-boot-starter-security`. No `application.yml` e no `application-kafka.yml`, acrescentar a `UserDetailsServiceAutoConfiguration` a `spring.autoconfigure.exclude` (**verificar o nome totalmente qualificado no Boot 4.1.2**). No `application.yml`, acrescentar `awards.security.token: ${API_TOKEN:}`.
- [ ] T063 [P] [US4] Criar `src/main/java/com/outsera/awards/security/SecurityProperties.java` (`@ConfigurationProperties("awards.security")`, `token`). `enabled()` é verdadeiro só com o token não vazio. O `toString` nunca mostra o valor.
- [ ] T064 [P] [US4] Criar `src/main/java/com/outsera/awards/security/TokenAuthenticationFilter.java` (`OncePerRequestFilter`):
  - lê `Authorization` e só aceita o prefixo exato `Bearer `;
  - compara `SHA-256(recebido)` com `SHA-256(esperado)` por `MessageDigest.isEqual`, os dois em UTF-8
    (decisão de design da research R11; nunca `String.equals` nem `Arrays.equals`);
  - se forem iguais, põe no `SecurityContext` uma autenticação `api-client` sem papéis; se não forem, não
    faz nada;
  - nunca registra o cabeçalho.
- [ ] T065 [P] [US4] Criar `src/main/java/com/outsera/awards/security/ProblemAuthenticationEntryPoint.java`: 401 com `application/problem+json`, o corpo do T054 (montado com `ProblemDetail` e o `JsonMapper` do Boot) e `WWW-Authenticate: Bearer realm="awards"`.
- [ ] T066 [US4] Criar `src/main/java/com/outsera/awards/security/SecurityConfiguration.java` (research R11 e R12, `contracts/operational-endpoints.md`):
  - uma `SecurityFilterChain`;
  - `csrf`, `formLogin`, `httpBasic`, `logout`, `requestCache` e `anonymous` desligados, e
    `sessionManagement` `STATELESS`;
  - token desligado: `anyRequest().permitAll()`, sem filtro;
  - token ligado:
    - `/api/producers/intervals`, `/api/movies` e `/actuator/prometheus` → `authenticated()`;
    - o resto → `permitAll()`;
    - o filtro antes do `AuthorizationFilter`;
    - o `ProblemAuthenticationEntryPoint` como `authenticationEntryPoint`;
  - cabeçalhos: `cacheControl` desligado e, no lugar dele, um `DelegatingRequestMatcherHeaderWriter` com
    o `CacheControlHeadersWriter` para tudo o que não é endpoint de negócio. `contentTypeOptions` e
    `frameOptions(deny)` continuam ligados.
- [ ] T067 [US4] Copiar `specs/003-modo-docker/contracts/openapi.yaml` para `src/main/resources/static/openapi.yaml` (os dois arquivos ficam idênticos byte a byte). Em `src/test/java/com/outsera/awards/docs/ApiDocsIT.java`, trocar **só** o valor da constante `SPEC_CONTRACT` para `Path.of("specs", "003-modo-docker", "contracts", "openapi.yaml")` e o javadoc dela. Nenhuma asserção muda (decisão registrada em `docs/ai/decisoes.md`). **Verificar (R13):** o validador aceita a alternativa `{}` sem exigir o cabeçalho. Se não aceitar, aplicar a saída da R13.
- [ ] T068 [US4] Rodar `./mvnw verify` até ficar verde:
  - todos os ITs de `security/` e o `ApiDocsSecurityIT`;
  - a suíte dos Marcos 1 e 2 sem alteração, inclusive com o `ApiDocsIT` só com a constante trocada;
  - os ITs de Kafka;
  - a execução `json-logs`;
  - a cobertura ≥ 90%.

  Rodar `.github/scripts/check-unchanged-tests.sh v0.2.0`: verde.
- [ ] T069 [US4] Acrescentar ao `specs/003-modo-docker/checklists/requirements.md` a seção "Revisão de segurança":
  - a comparação do token no `TokenAuthenticationFilter` usa o mecanismo da research R11:
    `MessageDigest.isEqual(sha256(recebido), sha256(esperado))`, em UTF-8, sem `String.equals`,
    `Arrays.equals` nem saída antecipada;
  - o ADR 0017 cita esse mecanismo;
  - o filtro não registra cabeçalhos;
  - a matriz de acesso do `SecurityConfiguration` é igual à de `contracts/operational-endpoints.md`.
- [ ] T070 [US4] Commit `Etapa 3: autenticação por token com Spring Security e contrato 1.2.0` com T052 a T069.

**Checkpoint**: US4 completa. O modo padrão continua idêntico, agora com a guarda do token.

---

## Phase 5: Etapa 4 — Compose com observabilidade (US5, Priority: P3)

**Goal**: `docker compose up` deixa pronto o ambiente completo, com os dados persistentes, o Prometheus
coletando e o Grafana com o dashboard. O pipeline verifica tudo isso.

**Independent Test**: o `compose-test.sh` passa no job `compose`, nos passos 1 a 8 de
`contracts/ci-pipeline.md` e no passo de parada do PostgreSQL.

### Verificação da etapa 4 (escrever primeiro; precisa falhar)

- [ ] T071 [US5] Criar `.github/scripts/compose-test.sh` (bash, `set -euo pipefail`, executável), com os passos 0 a 8 de `contracts/ci-pipeline.md`:
  - `trap 'docker compose --profile tools down -v' EXIT`;
  - mede o tempo dos passos 1 a 4 e falha acima de 180 s (SC-008);
  - no passo 5, publica `{"year":1991,"title":"Compose Novo","studios":null,"producers":"Joel Silver","winner":true}`
    e espera `min` com o intervalo 0 de Joel Silver em até 5 s;
  - no passo 6, publica `{"year":0,…}` e confere `awards-error-reason:YEAR_OUT_OF_RANGE` no tópico de
    erros;
  - no passo 7, roda `docker compose down`, **sem `-v`**, e depois `docker compose up -d --wait` (todos os
    containers são recriados sobre os mesmos volumes, US5-5), e espera `totalElements` 207;
  - no passo 8, gera o token com `openssl rand -hex 16` dentro de `set +x`, sem imprimir nem gravar.

  Acrescentar o **passo 7b**, que comprova a queda real do banco (FR-015a; o `RetryPostgresIT`, T035a,
  comprova a nova tentativa quando a gravação falha):
  1. `docker compose stop postgres`;
  2. publicar `{"year":2031,"title":"Durante a Queda",…}`;
  3. esperar 5 s;
  4. `docker compose start postgres`;
  5. esperar até 60 s pelo filme na listagem;
  6. conferir que o tópico de erros **não** recebeu esse evento.
- [ ] T072 [US5] Em `.github/scripts/check-workflows.sh`, acrescentar as regras de `contracts/ci-pipeline.md`:
  - o ecossistema `docker-compose` no Dependabot;
  - `@sha256:` com 64 caracteres hexadecimais em toda `image:` do `compose.yaml` e dos `services:` dos
    workflows;
  - nenhum `POSTGRES_PASSWORD` no job `postgres`.

  Testar numa pasta temporária, sem versionar, com um caso que falha para cada regra nova e um caso
  válido.
- [ ] T073 [US5] Confirmar a falha: `check-workflows.sh` falha (o Dependabot não tem `docker-compose`), e `compose-test.sh` falha no passo 1 (não há `compose.yaml`). Sem Docker, registrar `PULADO` para o segundo.

### Implementação da etapa 4

- [ ] T074 [US5] Resolver as versões estáveis atuais e os digests de `postgres:18-alpine`, `apache/kafka-native`, `apache/kafka` (mesma versão), `prom/prometheus` e `grafana/grafana`, com `docker buildx imagetools inspect <imagem:tag>` ou pela API do registro. **Verificar (R17):**
  - se a `apache/kafka-native` tem `nc` ou outro binário para o healthcheck;
  - se `prom/prometheus` tem `/bin/sh` e `wget`, e o `grafana/grafana` tem `wget`;
  - aplicar a saída da R17 quando faltar algum.

  Anotar os pares tag → digest e o resultado na descrição do commit.
- [ ] T075 [US5] Criar `compose.yaml` conforme `contracts/compose.md`:
  - os seis serviços, com `kafka-cli` em `profiles: [tools]`;
  - imagens por tag e digest;
  - volumes `pgdata` e `kafkadata`;
  - healthchecks;
  - `depends_on` com `service_healthy` para `app`;
  - as variáveis com os padrões da tabela;
  - o comentário `# credencial de demonstração: só para desenvolvimento local (constituição 1.6.0, Princípio VII)`
    em cada linha com `POSTGRES_PASSWORD` ou `GRAFANA_ADMIN_PASSWORD`;
  - `POSTGRES_INITDB_ARGS=--locale=C --encoding=UTF8`;
  - KRaft com nó único e `KAFKA_AUTO_CREATE_TOPICS_ENABLE=false`;
  - na `app`, `SPRING_PROFILES_ACTIVE=docker`, `DB_PASSWORD=${POSTGRES_PASSWORD:-awards-demo}` e
    `API_TOKEN=${API_TOKEN:-}`.
- [ ] T076 [P] [US5] Criar `observability/prometheus/prometheus.yml` (`scrape_interval: 5s`; job `app`, alvo `app:8080`, `metrics_path: /actuator/prometheus`, `authorization: { type: Bearer, credentials_file: /tmp/api-token }`). No serviço `prometheus` do compose, o `entrypoint` grava `API_TOKEN` com `printf %s` em `/tmp/api-token` e executa o `prometheus`. **Verificar (R18):** o `credentials_file` vazio é aceito. Se não for, aplicar o modelo da R18.
- [ ] T077 [P] [US5] Criar o provisionamento do Grafana:
  - `observability/grafana/provisioning/datasources/prometheus.yml` (`uid: prometheus`, `url:
    http://prometheus:9090`, `isDefault: true`);
  - `observability/grafana/provisioning/dashboards/awards.yml` (provedor de arquivo apontando para
    `/var/lib/grafana/dashboards`).
- [ ] T078 [P] [US5] Criar `observability/grafana/dashboards/awards.json` (`uid: awards`, título "Golden Raspberry Awards API", datasource `uid: prometheus`), com os cinco painéis da research R19. O painel de atraso usa o nome de série fixado no T033.
- [ ] T079 [P] [US5] Em `.github/dependabot.yml`, acrescentar o ecossistema `docker-compose` (`directory: /`, `schedule.interval: weekly`).
- [ ] T080 [US5] Em `.github/workflows/ci.yml`, conforme `contracts/ci-pipeline.md`:
  - no `workflow-policy`, `fetch-depth: 0` no checkout e o passo `.github/scripts/check-unchanged-tests.sh v0.2.0`;
  - o job `compose` (`needs: dockerfile-lint`, `timeout-minutes: 20`), com checkout e
    `.github/scripts/compose-test.sh`. Em falha, os logs vão para o resumo com
    `docker compose logs --no-color` (`if: failure()`).

  Rodar `check-workflows.sh` (com o `yq`).
- [ ] T081 [US5] Com o Docker: rodar `.github/scripts/compose-test.sh` na raiz até passar, inclusive o passo 7b, e anotar o tempo dos passos 1 a 4. Sem Docker, registrar `PULADO`.
- [ ] T082 [US5] Rodar `./mvnw verify`: verde. Nada desta etapa muda a aplicação.
- [ ] T083 [US5] Commit `Etapa 4: compose com PostgreSQL, Kafka, Prometheus e Grafana` com T071 a T082.
- [ ] T084 [US5] **Pendente: exige o push, que é do Anderson.** Conferir na execução do branch:
  - `workflow-policy`, `build`, `postgres` e `compose` verdes, além dos jobs do Marco 2. A configuração dos checks obrigatórios vem depois, no T093;
  - o tempo do SC-008 no resumo do `compose`;
  - os PRs do Dependabot para o `docker-compose` aparecem depois do merge.

  Corrigir as falhas no mesmo branch.

**Checkpoint**: US5 completa. Todos os requisitos de comportamento e de pipeline têm a sua comprovação.

---

## Phase 6: Etapa 5 — ADRs e README

**Purpose**: FR-036 e FR-037, com as pendências do papel de documentação deixadas pela emenda 1.5.0.

### Verificação da etapa 5 (antes da escrita)

- [ ] T085 Conferir que as seções "Revisão do README", "Revisão dos ADRs" e "Revisão de segurança" de `specs/003-modo-docker/checklists/requirements.md` estão desmarcadas. Elas são a comprovação desta etapa (Princípio VI) e só o revisor as marca, no PR.

### Escrita da etapa 5

- [ ] T086 [P] Criar os ADRs em `docs/adr/`, no formato da spec 002 (FR-031). Cada um tem número, título, data, status, contexto, decisão, alternativas e consequências, e cita a entrada de `docs/ai/decisoes.md`:
  - `0013-postgresql-com-esquema-comum.md` (R3, R4, collation `C` e a diferença residual com o
    `String.compareTo`);
  - `0014-ingestao-unica-por-kafka.md` (R6, R8 e R10: partição única, marcador com `runId`, readiness
    pelo indicador `ingestion`);
  - `0015-gravacao-idempotente-com-atualizacao.md` (R5 e a reversão no reinício aceita nas
    Clarifications);
  - `0016-topico-de-erros-e-novas-tentativas.md` (R7, R9 e `FIELD_TOO_LONG`);
  - `0017-autenticacao-por-token.md` (R11 a R13). Precisa citar o mecanismo de comparação do token
    (`MessageDigest.isEqual` sobre SHA-256 em UTF-8, research R11; era a FR-024, retirada da spec), para a
    "Revisão de segurança" conferir. Precisa também registrar **como decisão intencional** que
    `X-Content-Type-Options` e `X-Frame-Options` aparecem em todas as respostas, e que o `Cache-Control`
    fica fora dos endpoints de negócio (FR-037);
  - `0018-compose-com-observabilidade.md` (R17 a R19 e as credenciais de demonstração da constituição
    1.6.0);
  - `0019-testes-com-postgresql-em-perfil-dedicado.md` (R16 e a emenda 1.7.0).

  Criar em `docs/ai/decisoes.md` as entradas que faltarem.
- [ ] T087 [P] Atualizar `docs/adr/0004-somente-testes-de-integracao.md` com os quatro casos de `SpringApplication` da constituição 1.5.0 e o perfil dedicado da 1.7.0. Atualizar o índice `docs/adr/README.md` com 0013 a 0019.
- [ ] T088 Atualizar `README.md` (FR-036), sem reescrever as seções dos Marcos 1 e 2:
  - **modo Docker:** `docker compose up`, as portas, o papel de cada serviço e o `down -v`;
  - **credenciais:** as de demonstração identificadas como só para uso local (1.6.0) e o token de
    demonstração;
  - **eventos:** o formato do evento de filme, com link para `specs/003-modo-docker/contracts/kafka-events.md`,
    e como publicar um vencedor novo e ler o tópico de erros pelo `kafka-cli`;
  - **autenticação:** `API_TOKEN`, `Authorization: Bearer`, a matriz de acesso (inclusive o `OPTIONS`) e
    o exemplo com o token de demonstração;
  - **token de demonstração:** definir aqui o valor, um texto fixo e público, que deixa claro que é só
    para demonstração. É o único lugar do repositório com esse valor (Princípio VII). O quickstart, os
    testes e o compose não o repetem: os testes geram tokens aleatórios, e o compose tem o token
    desligado por padrão;
  - **perfil `postgres-it`:** como rodar e por que fica fora do `verify`;
  - os jobs novos do pipeline;
  - **tabela de requisitos e testes:** acrescentar a spec 003;
  - **pendências da emenda 1.5.0:** a seção de testes e a tabela de rastreabilidade citam os quatro casos
    de `SpringApplication`.

  A seção "Avalie em 2 minutos" continua funcionando só com o JDK.
- [ ] T089 Num clone limpo do branch, rodar a seção "Avalie em 2 minutos" e as seções 1 e 2 de `specs/003-modo-docker/quickstart.md` só com o JDK. Com o Docker, rodar também as seções 3 e 4. Anotar os resultados na descrição do commit.
- [ ] T090 Rodar `./mvnw verify`: verde. Atualizar o **Status** de `specs/003-modo-docker/spec.md` para `Implemented`, listando as tarefas "Pendente: push" que ainda não foram conferidas.
- [ ] T091 Registrar em `docs/ai/decisoes.md` o fechamento do bolt:
  - os resultados do T081 (inclusive o passo 7b, que comprova a R9) e do T089;
  - os `PULADO`;
  - as pendências de push;
  - o resultado do testador adversário (T050).
- [ ] T092 Commit `Etapa 5: ADRs e README do Marco 3` com T085 a T091. Avisar o Anderson de que o bolt está pronto para o PR. Depois do merge, a sessão é exportada para `docs/ai/sessoes/`.
- [ ] T093 **Pedido ao Anderson, depois da primeira execução do pipeline no PR e antes do merge.** Antes da primeira execução, os jobs novos não aparecem na lista de checks da proteção. Pedir a configuração da proteção da `main` (Settings → Branches, ou o ruleset da `main`):
  - incluir como *required status checks* os **dois** jobs novos:
    - `postgres`: comprova o comportamento que depende do PostgreSQL (constituição 1.7.0, Princípio
      VI: o job vermelho reprova o merge; spec 003, FR-007);
    - `compose`: comprova da FR-028 à FR-035 e a queda real do banco da FR-015a (passo 7b);
  - conferir se os jobs existentes já estão na lista: `workflow-policy`, `build`, `codeql`,
    `dependencies`, `dockerfile-lint` e `image`. Incluir os que faltarem;
  - deixar **desligado o bypass para administradores** ("Do not allow bypassing the above settings" na
    proteção de branch, ou nenhum *bypass actor* no ruleset), para que nem o administrador faça merge com
    um job vermelho.

  Depois da configuração, conferir na página do PR que os oito checks aparecem como obrigatórios.
  Registrar em `docs/ai/decisoes.md` a lista configurada e a confirmação do bypass desligado, sem nenhum
  dado pessoal, num commit de fechamento antes do merge.

---

## Dependencies & Execution Order

### Entre etapas

- **Preparação (T001, T002)** → **Etapa 1**.
- **Etapa 1 (PostgreSQL)** → **Etapa 2**:
  - o `record` (T016) é o caminho de gravação do consumidor;
  - o `PostgresSupport` e o perfil `postgres-it` são usados pelo `RestartPostgresIT` (T035);
  - a sobrecarga do `ApplicationStarter` (T004) é usada pelos ITs de Kafka.
- **Etapa 2 (Kafka)** → **Etapa 3**:
  - o T062 mexe no `application-kafka.yml` criado no T037;
  - a matriz de acesso protege as métricas de ingestão.
- **Etapa 3 (autenticação)** → **Etapa 4**:
  - o compose usa `API_TOKEN` (passo 8);
  - o `workflow-policy` chama o `check-unchanged-tests.sh` (T060).

  O responsável de infraestrutura pode **preparar** a etapa 4 em paralelo, numa cópia de trabalho
  própria, a partir dos contratos. O commit dela entra depois do da etapa 3.
- **Etapa 4 (compose)** → **Etapa 5**: os ADRs 0014, 0016 e 0018 descrevem o que foi verificado no
  compose. Os ADRs 0013, 0015 e 0019 podem ser preparados antes.

### Dentro de cada etapa

- apoio → testes → confirmação da falha → implementação → `./mvnw verify` verde → commit;
- etapa 1: T003 → T004 → T005 a T009 → T010 → T011 → T012 a T016 → T017 → T018 → T019;
- etapa 2: T021 → T022 → T023 a T035a (o T034c começa pela verificação) → T036 → T037 → T038 a T040 → T041 → T042 → T043 e T044 →
  T045 → T046 → T047 → T048 → T049;
- etapa 3: T052 a T060 → T061 → T062 → T063 a T065 → T066 → T067 → T068 → T069 → T070;
- etapa 4: T071 e T072 → T073 → T074 → T075 → T076 a T079 → T080 → T081 → T082 → T083.

### Histórias

- US2 (sem Kafka) não depende de nenhuma outra.
- US1 depende do `record` da US2 (T016).
- US3 depende da US1 (consumidor e tópicos).
- A persistência da US2 com eventos (T035) depende da US1 e da US3.
- US4 é independente das histórias de Kafka no comportamento, mas mexe no `application-kafka.yml`.
- US5 depende de todas as anteriores.

## Parallel Opportunities

- **Etapa 1:** T005, T006, T007 e T008 em paralelo, depois do T003 e do T004. T013 e T014 em paralelo.
- **Etapa 2:** T023 a T034b em paralelo, depois do T022 (classes de teste diferentes; o T030 cria o seu
  cenário). T038, T039 e T040 em paralelo.
- **Etapa 3:** T052 a T059 em paralelo; o T060 em paralelo com eles, pela infraestrutura. T063, T064 e
  T065 em paralelo.
- **Etapa 4:** T076, T077, T078 e T079 em paralelo, depois do T075.
- **Etapa 5:** T086 e T087 em paralelo.
- **Entre papéis**, até três agentes, cada um na sua cópia de trabalho e com dono único por arquivo:
  - o implementador na etapa 2 ou 3;
  - a infraestrutura preparando o job `postgres` (T018) e a etapa 4;
  - a documentação preparando os ADRs 0013, 0015 e 0019.

### Exemplo: etapa 2, testes em paralelo

```text
Task: "T024 KafkaReferenceFileIT em src/test/java/com/outsera/awards/kafka/KafkaReferenceFileIT.java"
Task: "T026 KafkaReadinessIT em src/test/java/com/outsera/awards/kafka/KafkaReadinessIT.java"
Task: "T029 KafkaNewWinnerIT em src/test/java/com/outsera/awards/kafka/KafkaNewWinnerIT.java"
Task: "T032 KafkaInvalidEventsIT em src/test/java/com/outsera/awards/kafka/KafkaInvalidEventsIT.java"
```

## Implementation Strategy

### MVP

A etapa 1 (US2 sem Kafka) é o menor incremento com valor próprio: a aplicação roda sobre o PostgreSQL
com o mesmo esquema e sobrevive a reinícios. Ela fecha com commit e `./mvnw verify` verde, mas **não é
mergeada sozinha**: o marco é um único bolt e um único PR (R21), e um extra só entra completo
(Princípio V).

### Incremental

1. Etapa 1: o PostgreSQL e o serviço com atualização. O modo padrão continua idêntico.
2. Etapa 2: o Kafka como caminho único no perfil, com equivalência comprovada.
3. Etapa 3: o token, com a guarda e o contrato 1.2.0.
4. Etapa 4: o compose e a verificação de ponta a ponta no pipeline.
5. Etapa 5: os ADRs e o README. Depois, o PR.

### Corte

Se o prazo apertar (briefing, "Decisão de escopo"), o marco não entra pela metade. A ordem de corte do
briefing vale para os Marcos 4 a 6, e não para partes do Marco 3.

## Notes

- Cada etapa termina com `./mvnw verify` verde **sem Docker**. Os `*PostgresIT` e o compose são
  verificados localmente só quando o Docker estiver disponível, e sempre no pipeline.
- Nenhum `*IT.java` da tag `v0.2.0` muda, salvo a constante do `ApiDocsIT` (T067).
- Nenhum token, senha ou credencial real em código, testes, commits ou mensagens. Os ITs geram o token
  em tempo de execução. As credenciais de demonstração aparecem só no `compose.yaml`.
- Os pontos "verificar" que mudarem a decisão atualizam a research no mesmo commit, e a spec se mudarem
  comportamento (Princípio IV).
- Pull request, merge, tag e push são do Anderson.
