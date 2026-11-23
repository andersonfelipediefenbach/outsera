---

description: "Tarefas do Marco 1, agrupadas em bolts (um pull request por bolt)"
---

# Tasks: Marco 1 — Carga do CSV, intervalos de produtores e listagem de filmes

**Input**: Design documents from `/specs/001-producer-intervals-api/`

**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/openapi.yaml, quickstart.md

**Tests**: obrigatórios. **Somente testes de integração** (Princípio I da constituição). Cada teste é uma
classe `*IT` executada pelo Failsafe em `./mvnw verify`, sobe a aplicação com `@SpringBootTest(webEnvironment
= RANDOM_PORT)` e usa **um CSV de cenário por classe** (`properties = "movies.csv.path=classpath:scenarios/<arquivo>.csv"`).
É proibido criar teste unitário, mock de serviço ou classe `*Test`. Cada método de teste tem
`@DisplayName` começando pelos IDs dos requisitos que comprova (Princípio VI).

**Como o Princípio I (constituição 1.2.0) se aplica aos bolts:**
- No bolt 2 ainda não existe endpoint de negócio. Por isso, os testes de carga verificam o resultado
  **pelo log e pelo banco** (`JdbcTemplate`), com a aplicação real no ar.
- Cada um desses testes ganha a verificação **por HTTP** no bolt 3 (intervalos) e no bolt 5
  (listagem), quando o endpoint passa a existir.
- Os testes de falha de inicialização seguem a cláusula própria do princípio: rodam a aplicação real
  com `SpringApplication` e verificam a exceção e o log.

**Organization**: as fases são **bolts**. Cada bolt é um branch e um pull request entregável sozinho, com
`./mvnw verify` verde. Em cada bolt, **os testes vêm primeiro e precisam falhar** antes da
implementação.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: pode ser feita em paralelo (arquivos diferentes e sem dependência de tarefa incompleta)
- **[Story]**: história da spec atendida. US1 = intervalos, US2 = carga do CSV, US3 = listagem e
  US4 = documentação.
- Pacote base: `com.outsera.awards`. Testes em `src/test/java/com/outsera/awards/`. Cenários em
  `src/test/resources/scenarios/`.

## Mapeamento bolt → fase

| Bolt | Branch sugerido | Fase | Histórias |
| --- | --- | --- | --- |
| 1. Geração do projeto | `bolt/1-projeto` | Setup + Foundational | — |
| 2. Carga do CSV | `bolt/2-carga-csv` | US2 | US2 |
| 3. Cálculo dos intervalos | `bolt/3-calculo-intervalos` | US1 (regras) | US1 |
| 4. Endpoint de intervalos | `bolt/4-endpoint-intervalos` | US1 (contrato) | US1 |
| 5. Listagem | `bolt/5-6-listagem-swagger` | US3 | US3 |
| 6. Swagger | `bolt/5-6-listagem-swagger` | US4 + Polish | US4 |

> **Nota sobre os bolts 5 e 6.** Os dois bolts são entregues juntos, em um único branch
> (`bolt/5-6-listagem-swagger`) e um único pull request, com um commit por etapa: fase 6 (listagem),
> fase 7 (Swagger UI) e fase 8 (fechamento). Cada etapa termina com `./mvnw verify` verde antes do seu
> commit, e os testes de cada etapa continuam vindo antes da implementação. Decisão registrada em
> `docs/ai/decisoes.md`.

> **Nota sobre os bolts 3 e 4.** Como só existem testes de integração, as regras de cálculo só podem
> ser verificadas por HTTP. O bolt 3 inclui, portanto, um controller mínimo de
> `GET /api/producers/intervals`, que serve de meio de teste. O bolt 4 fecha o endpoint contra o
> enunciado: resultado de referência com o arquivo padrão, formato exato, listas vazias e validação
> contra o `openapi.yaml`.

---

## Phase 1: Setup — Bolt 1 (geração do projeto)

**Purpose**: gerar o projeto do zero, sem nenhuma dependência além do JDK 25.

- [X] T001 Remover o diretório vazio `src/main/java/org/example` (pacote padrão da IDE, substituído por `com.outsera.awards`; research R12)
- [X] T002 Criar `pom.xml` com:
  - **parent:** `org.springframework.boot:spring-boot-starter-parent:4.1.2`, `groupId` `com.outsera`, `artifactId` `awards` e `<java.version>25</java.version>`;
  - **dependências:** `spring-boot-starter-webmvc`, `spring-boot-starter-data-jpa`, `spring-boot-starter-flyway`, `com.h2database:h2` (runtime) e `org.apache.commons:commons-csv:1.14.1`;
  - **teste:** `spring-boot-starter-test` e o módulo de teste do Boot 4.1 que fornece `RestTestClient`/`@AutoConfigureRestTestClient` (confirmar o artifact no guia de migração do Boot 4);
  - **plugins:** `spring-boot-maven-plugin` e `maven-failsafe-plugin` com goals `integration-test` e `verify`, incluindo `**/*IT.java`.
- [X] T003 Gerar o Maven Wrapper 3.3.4 no modo `only-script`. Extrair `mvnw` e `mvnw.cmd` de `org/apache/maven/wrapper/maven-wrapper-distribution/3.3.4` (classifier `only-script`) no Maven Central e criar `.mvn/wrapper/maven-wrapper.properties` com `distributionUrl=https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.16/apache-maven-3.9.16-bin.zip`. Marcar o `mvnw` como executável no git (`git update-index --chmod=+x mvnw`)
- [X] T004 [P] Criar `.gitattributes` com `mvnw text eol=lf`, `*.cmd text eol=crlf` e `*.csv text eol=lf -diff`, para o wrapper funcionar no Windows e no Linux
- [X] T005 [P] Criar `src/main/resources/application.yml` com:
  - `spring.application.name: awards` e `server.port: ${PORT:8080}`;
  - `spring.mvc.problemdetails.enabled: true`;
  - `spring.jpa.hibernate.ddl-auto: validate` e `spring.jpa.open-in-view: false`;
  - `spring.h2.console.enabled: false`;
  - `movies.csv.path: ${MOVIES_CSV_PATH:classpath:movielist.csv}`.
- [X] T006 [P] Adicionar o arquivo do enunciado em `src/main/resources/movielist.csv`, sem alterar nenhum byte (fornecido pelo responsável)

---

## Phase 2: Foundational — Bolt 1 (continuação)

**Purpose**: a infraestrutura de teste que todos os bolts usam e a aplicação mínima.

**⚠️ CRITICAL**: nenhum bolt seguinte começa antes deste estar mergeado.

- [X] T007 [P] Criar a base de cenário `src/test/java/com/outsera/awards/support/ScenarioIT.java`. É uma classe abstrata com:
  - `@SpringBootTest(webEnvironment = RANDOM_PORT)`, `@AutoConfigureRestTestClient` e `@ExtendWith(OutputCaptureExtension.class)`;
  - um `RestTestClient` injetado, para as verificações por HTTP;
  - um `JdbcTemplate` injetado, para as verificações de banco do bolt 2, permitidas pelo Princípio I enquanto não há endpoint. O `JdbcTemplate` é temporário e sai no T067. As consultas usam só SQL sobre as tabelas do `data-model.md`, sem depender das classes de produção, para que o teste compile antes da implementação.

  Cada subclasse define o seu CSV via `properties`
- [X] T008 [P] Criar `src/test/java/com/outsera/awards/support/ApplicationStarter.java`, um utilitário de teste que executa a `AwardsApplication` real com `SpringApplication`, `--server.port=0` e `--movies.csv.path=<valor>`, e devolve a exceção lançada na inicialização e a saída capturada, para os testes de falha de inicialização
- [X] T009 Escrever `src/test/java/com/outsera/awards/SmokeIT.java` (deve falhar antes do T010):
  - `"FR-026 a aplicação sobe só com o wrapper"`: o contexto sobe em porta aleatória;
  - `GET /rota-inexistente` responde 404 com `Content-Type: application/problem+json`.
- [X] T010 Criar `src/main/java/com/outsera/awards/AwardsApplication.java` (`@SpringBootApplication` + `@ConfigurationPropertiesScan`)
- [X] T011 Rodar `./mvnw verify` (e `mvnw.cmd verify` no Windows) e confirmar `BUILD SUCCESS` com o `SmokeIT` executado pelo Failsafe e nenhum teste no Surefire

**Checkpoint (PR do bolt 1)**: o projeto compila, sobe e testa só com o wrapper.

---

## Phase 3: User Story 2 — Carga do arquivo de filmes (Priority: P1) — Bolt 2

**Goal**: na inicialização, carregar o CSV do caminho configurado para o H2:
- erros estruturais interrompem a inicialização com mensagem clara;
- variações inofensivas são aceitas;
- linhas inválidas são ignoradas, com aviso e resumo no log.

**Independent Test**: subir a aplicação com cada CSV de cenário e verificar:
- **no log** (via `OutputCaptureExtension`): os avisos e o resumo `Carga do arquivo {caminho} concluída: {lidas} linhas lidas, {aceitas} aceitas, {rejeitadas} rejeitadas`;
- **no banco** (via `JdbcTemplate`): os filmes gravados.

Para os erros estruturais, verificar a falha de inicialização com `ApplicationStarter`. As verificações por HTTP desses mesmos cenários entram nos bolts 3 (T049, T050) e 5 (T067).

### Preparação do bolt 2

- [X] T011a [US2] No `pom.xml`, excluir `org.mockito:mockito-core` e `org.mockito:mockito-junit-jupiter` do `spring-boot-starter-test`. Reforça o Princípio I (sem mocks) e elimina o aviso de agente dinâmico que o Mockito emite ao se acoplar à JVM. Rodar `./mvnw verify` e confirmar que o `SmokeIT` continua passando (decisão de 2026-11-22 em `docs/ai/decisoes.md`)

### Testes do bolt 2 (escrever primeiro e ver falhar) ⚠️

- [X] T012 [P] [US2] Criar os CSVs de erro estrutural em `src/test/resources/scenarios/`:
  - `comma-separated.csv`: cabeçalho `year,title,studios,producers,winner` e uma linha separada por vírgula;
  - `header-wrong-order.csv`: `title;year;studios;producers;winner`;
  - `header-missing-column.csv`: `year;title;studios;producers`;
  - `header-extra-column.csv`: `year;title;studios;producers;winner;extra`;
  - `empty-file.csv`: 0 bytes;
  - `unterminated-quote.csv`: cabeçalho válido e uma linha com aspas abertas e não fechadas;
  - `invalid-encoding.csv`: cabeçalho válido e uma linha com um byte que não é UTF-8 válido (`é` em ISO-8859-1).
- [X] T013 [P] [US2] Escrever `src/test/java/com/outsera/awards/ingestion/StartupFailureIT.java`. Um método por motivo, cada um verificando que a inicialização falha:
  - `"FR-005 SC-006 arquivo inexistente"`: caminho `file:./nao-existe.csv`, motivo `NOT_FOUND`, a mensagem cita o caminho;
  - `"FR-005 arquivo vazio"`: `UNREADABLE`;
  - `"FR-005 aspas sem fechamento"` e `"FR-005 arquivo fora de UTF-8"`: `UNREADABLE`, a mensagem cita o caminho (research R3);
  - `"FR-005 separador inválido"`: `INVALID_SEPARATOR`, a mensagem cita o separador `;`;
  - `"FR-003 FR-005 ordem errada"`, `"... coluna faltando"` e `"... coluna a mais"`: `INVALID_HEADER`, a mensagem mostra o cabeçalho esperado `year;title;studios;producers;winner` e o encontrado;
  - em todos: a saída contém `APPLICATION FAILED TO START`, gerado pelo `FailureAnalyzer`.
- [X] T014 [P] [US2] Criar `src/test/resources/scenarios/header-case.csv` (cabeçalho `Year;Title;Studios;Producers;Winner`, 3 linhas válidas) e escrever `src/test/java/com/outsera/awards/ingestion/HeaderCaseIT.java`: `"FR-003 FR-005a cabeçalho com maiúsculas diferentes"`. A aplicação sobe, o resumo mostra `3 linhas lidas, 3 aceitas, 0 rejeitadas`, o carregador não emite `WARN` (ver nota abaixo do T015) e `select count(*) from movie` é 3
- [X] T015 [P] [US2] Criar `src/test/resources/scenarios/harmless-variations.csv`, com 4 linhas válidas:
  - BOM UTF-8 no início;
  - linhas em branco no meio e no fim, uma delas só com espaços (research R3);
  - espaços em volta de campos e do cabeçalho;
  - `winner` em `YES` e ` Yes `.

  Escrever `src/test/java/com/outsera/awards/ingestion/HarmlessVariationsIT.java`. Há **um método por variação** (FR-006c), todos com `"FR-005a ..."` e o nome da variação:
  - `"... BOM"`: o BOM fica antes do cabeçalho e não gera `INVALID_HEADER`; a aplicação sobe e a primeira linha de dados está no banco;
  - `"... linhas em branco"`: o resumo mostra `4 linhas lidas, 4 aceitas, 0 rejeitadas`, porque linhas em branco não contam como lidas;
  - `"... espaços em volta dos campos e do cabeçalho"`: no banco, títulos e produtores estão sem espaços em volta;
  - `"... yes em qualquer caixa"`: `select count(*) from movie where winner` é igual ao número de linhas com `YES` e ` Yes `.

  Em todos, o carregador não emite `WARN`.

  **Nota (T014 e T015):** a verificação de "sem `WARN`" olha só as linhas de nível `WARN` emitidas pelo carregador do CSV (logger `MovieFileLoader`, do pacote `com.outsera.awards.ingestion`), e não o log inteiro. Avisos de outras bibliotecas, como o do Flyway sobre a versão do H2, não fazem o teste falhar (decisão de 2026-11-22 em `docs/ai/decisoes.md`)
- [X] T016 [P] [US2] Criar `src/test/resources/scenarios/invalid-lines.csv`, com cabeçalho válido, 2 linhas válidas e uma linha inválida por motivo, nesta ordem: `COLUMN_COUNT` (4 colunas), `COLUMN_COUNT` (6 colunas), `YEAR_MISSING`, `YEAR_NOT_NUMERIC` (`19x0`), `YEAR_NOT_NUMERIC` (`-1990`), `TITLE_MISSING`, `PRODUCERS_MISSING` (vazio), `PRODUCERS_MISSING` (` , and `), `WINNER_INVALID` (`no`) e, desde a T085, `YEAR_OUT_OF_RANGE` (`0` e `10000`). Inserir uma linha em branco antes de uma das inválidas, para provar que a numeração é a da linha física
- [X] T017 [US2] Escrever `src/test/java/com/outsera/awards/ingestion/InvalidLinesIT.java` (depende do T016):
  - `@ParameterizedTest` com um caso por linha inválida do T016 (FR-006c), no formato `(linha física, motivo legível)`, com nome `"FR-006 FR-006a linha {0} rejeitada: {1}"`. O log contém `WARN` com `Linha {n} rejeitada:` e o motivo, com `n` igual à linha física (cabeçalho = 1);
  - `"FR-006 a carga continua"`: o resumo mostra 2 aceitas e 9 rejeitadas (11 desde a T085), e `select count(*) from movie` é 2.
- [X] T018 [P] [US2] Criar `src/test/resources/scenarios/load-summary.csv` (10 linhas de dados, das quais 2 inválidas) e escrever `src/test/java/com/outsera/awards/ingestion/LoadSummaryIT.java`: `"FR-006b SC-006a resumo"`. O log contém `10 linhas lidas, 8 aceitas, 2 rejeitadas` e o caminho do arquivo, e o banco tem 8 filmes
- [X] T019 [P] [US2] Criar `src/test/resources/scenarios/optional-studios.csv` (uma linha com `studios` vazio) e escrever `src/test/java/com/outsera/awards/ingestion/OptionalStudiosIT.java`: `"FR-006e estúdios é opcional"`. A linha é aceita, sem `WARN`, e no banco o filme tem `studios` nulo
- [X] T020 [P] [US2] Criar `src/test/resources/scenarios/duplicate-lines.csv` (a mesma `1990;Filme X;Estúdio;Producer A;yes` nas linhas 2 e 4, e `1990;Filme X ;Outro;Producer B;` na linha 5, que repete ano e título depois do `trim`) e escrever `src/test/java/com/outsera/awards/ingestion/DuplicateLinesIT.java`: `"FR-006d duplicata ano+título"`. As linhas 4 e 5 geram `WARN` citando a linha original 2, o resumo mostra 1 aceita e 2 rejeitadas, e o banco tem um único `Filme X`, com `studios` = `Estúdio`
- [X] T021 [P] [US2] Criar `src/test/resources/scenarios/header-only.csv` e `src/test/resources/scenarios/all-invalid.csv`, e escrever `src/test/java/com/outsera/awards/ingestion/EmptyLoadIT.java` com duas classes `@Nested`, uma por CSV:
  - `"Edge só cabeçalho"`: a aplicação sobe, o resumo mostra `0 linhas lidas` e a tabela `movie` está vazia;
  - `"Edge todas inválidas"`: a aplicação sobe, o resumo mostra `0 aceitas` e a tabela `movie` está vazia.
- [X] T022 [P] [US2] Escrever `src/test/java/com/outsera/awards/ingestion/DefaultFileIT.java`, sem a propriedade `movies.csv.path`:
  - `"FR-001 FR-002 carrega o arquivo padrão"`: o resumo cita `classpath:movielist.csv` e mostra `0 rejeitadas`, e `select count(*) from movie` é igual ao número de aceitas lido do próprio resumo;
  - `"FR-001 a carga termina antes de o servidor aceitar conexões"`: no log, a linha `Carga do arquivo ... concluída` aparece antes da linha de início do servidor web (`Tomcat started on port`).

- [X] T022a [P] [US2] Criar `src/test/resources/scenarios/bom-quoted-header.csv` (BOM seguido do cabeçalho entre aspas, `"year";"title";"studios";"producers";"winner"`, e 2 linhas válidas) e `src/test/resources/scenarios/bom-blank-line.csv` (BOM seguido de uma linha em branco, depois o cabeçalho e 2 linhas válidas), e escrever `src/test/java/com/outsera/awards/ingestion/BomVariationsIT.java` com uma classe `@Nested` por CSV (revisão do Codex no PR do bolt 2):
  - `"FR-005a BOM com cabeçalho entre aspas"` e `"FR-005a BOM seguido de linha em branco"`: a aplicação sobe, o resumo mostra `2 linhas lidas, 2 aceitas, 0 rejeitadas`, o carregador não emite `WARN` e `select count(*) from movie` é 2.

- [X] T022b [P] [US2] Criar `src/test/resources/scenarios/producer-names.csv`, com uma linha por caso da research R4, inclusive `Producer A and and Producer B`, `and and` e um nome com espaços internos duplos, e escrever `src/test/java/com/outsera/awards/ingestion/ProducerNamesIT.java` (duas revisões do Codex no PR do bolt 2):
  - `"FR-007 FR-008 FR-009 FR-010 {título}: {nomes}"` (`@ParameterizedTest`): no banco, os produtores vinculados a cada filme são exatamente os esperados;
  - `"FR-006 FR-007 só separadores: {linha}"`: as linhas `and and` e ` , and ` são rejeitadas com `produtores vazios`.

  A verificação pelo banco é temporária (Princípio I). A listagem devolve os produtores como texto e não prova a separação, então esses casos passam a ser verificados por HTTP no T043 (bolt 3), que absorve o `producer-names.csv`, e o `ProducerNamesIT` é removido nesse momento.

### Implementação do bolt 2

- [X] T023 [US2] Criar `src/main/resources/db/migration/V1__create_schema.sql` (SQL portável entre H2 e PostgreSQL, sem funções específicas):
  - **`movie`:** `id` BIGINT identity PK; `release_year` INTEGER not null; `title` VARCHAR(500) not null; `studios` VARCHAR(1000) null; `producers` VARCHAR(1000) not null; `winner` BOOLEAN not null; `CONSTRAINT uk_movie_year_title UNIQUE (release_year, title)`.
  - **`producer`:** `id` BIGINT identity PK; `name` VARCHAR(500) not null; `CONSTRAINT uk_producer_name UNIQUE (name)`.
  - **`movie_producer`:** `movie_id` BIGINT FK → movie.id; `producer_id` BIGINT FK → producer.id; PK composta (`movie_id`, `producer_id`).
- [X] T024 [P] [US2] Criar a entidade `src/main/java/com/outsera/awards/movie/Movie.java`:
  - campos `id`, `year` (coluna `release_year`), `title`, `studios` (nullable), `producersRaw` (coluna `producers`) e `winner`;
  - `@ManyToMany` com `Producer` via `movie_producer`, em um `Set` para não duplicar o vínculo (FR-010);
  - construtor protegido para o JPA.
- [X] T025 [P] [US2] Criar a entidade `src/main/java/com/outsera/awards/producer/Producer.java` (`id`, `name` único, comparado exatamente, diferenciando maiúsculas; FR-008)
- [X] T026 [P] [US2] Criar `src/main/java/com/outsera/awards/movie/MovieRepository.java` e `src/main/java/com/outsera/awards/producer/ProducerRepository.java` (`JpaRepository`; `ProducerRepository.findByName(String)`)
- [X] T027 [P] [US2] Criar `src/main/java/com/outsera/awards/ingestion/MovieFileProperties.java` (`@ConfigurationProperties("movies.csv")`, campo `path`). Regra de resolução (research R10): os prefixos `classpath:` e `file:` são respeitados; um valor sem prefixo vira `FileSystemResource`
- [X] T028 [P] [US2] Criar `src/main/java/com/outsera/awards/ingestion/MovieFileException.java` (com o enum `Reason` = `NOT_FOUND`, `UNREADABLE`, `INVALID_SEPARATOR` e `INVALID_HEADER`, mais os campos caminho, cabeçalho encontrado e mensagem) e `src/main/java/com/outsera/awards/ingestion/MovieFileFailureAnalyzer.java`:
  - a **Description** traz o caminho, o motivo e o cabeçalho encontrado;
  - a **Action** traz o cabeçalho esperado `year;title;studios;producers;winner`, o separador `;` e a variável `MOVIES_CSV_PATH`.

  Registrar o analyzer em `src/main/resources/META-INF/spring.factories` (chave `org.springframework.boot.diagnostics.FailureAnalyzer`)
- [X] T029 [P] [US2] Criar `src/main/java/com/outsera/awards/ingestion/Rejection.java` (record `lineNumber`, `reason`, `detail`), o enum `RejectionReason` (`COLUMN_COUNT`, `YEAR_MISSING`, `YEAR_NOT_NUMERIC`, `YEAR_OUT_OF_RANGE` (desde a T086), `TITLE_MISSING`, `PRODUCERS_MISSING`, `WINNER_INVALID` e `DUPLICATE`, cada um com uma mensagem legível em português) e `src/main/java/com/outsera/awards/ingestion/LoadReport.java` (`linesRead`, `accepted`, `rejected` e a lista de `Rejection`)
- [X] T030 [P] [US2] Criar `src/main/java/com/outsera/awards/ingestion/ProducerNameParser.java` (research R4):
  - separar em etapas, sem regex única: dividir pelas vírgulas; em cada parte, tratar a palavra `and` isolada (separada por espaços) como separador; o nome é o trecho original da primeira à última palavra do grupo, preservando o texto interno; os casos estão em R4;
  - descartar as partes vazias;
  - remover as repetidas com um `LinkedHashSet`;
  - devolver uma lista vazia quando nenhum nome resultar.
- [X] T031 [US2] Criar `src/main/java/com/outsera/awards/ingestion/MovieCsvReader.java` (depende de T028 a T030; research R3). Leitura:
  - Commons CSV com delimitador `;`, aspas `"`, `ignoreEmptyLines`, `trim` e `ignoreSurroundingSpaces`;
  - leitura em UTF-8;
  - o BOM é removido do `Reader` antes de o Commons CSV ler o arquivo (`mark`/`reset` no primeiro caractere, sem dependência nova; research R3);
  - número da linha física com `getCurrentLineNumber()`;
  - um registro de uma coluna vazia (linha só com espaços) é tratado como linha em branco: não conta como lido e não gera aviso (research R3);
  - aspas sem fechamento ou bytes que não são UTF-8 válido dão `UNREADABLE` (research R3).

  Validação do cabeçalho:
  - 1 coluna contendo `,` dá `INVALID_SEPARATOR`;
  - nomes diferentes de `year;title;studios;producers;winner` (sem diferenciar maiúsculas, na ordem exata, 5 colunas) dão `INVALID_HEADER`;
  - arquivo sem cabeçalho dá `UNREADABLE`.

  Validação de linha, na ordem de `data-model.md`, registrando o primeiro motivo encontrado:
  1. `COLUMN_COUNT`: número de colunas diferente de 5;
  2. `YEAR_MISSING`: ano vazio;
  3. `YEAR_NOT_NUMERIC`: ano que não é formado só por dígitos (`\d+`; era `\d{1,9}` até a T086);
  4. `YEAR_OUT_OF_RANGE`: ano fora de 1 a 9999 (T086);
  5. `TITLE_MISSING`: título vazio;
  6. `PRODUCERS_MISSING`: produtores vazios ou sem nenhum nome depois do parser. O parser recebe o campo já com `trim` (por exemplo, ` , and ` chega como `, and`) e não pode depender dos espaços em volta;
  7. `WINNER_INVALID`: `winner` não vazio e diferente de `yes`, sem diferenciar maiúsculas;
  8. `DUPLICATE`: ano + título (após `trim`) já aceitos. O detalhe cita a linha original.

  Saída: as linhas válidas (como registros) e o `LoadReport`.
- [X] T032 [US2] Criar `src/main/java/com/outsera/awards/ingestion/MovieImportService.java`. É o **serviço único de gravação** (Princípio V): `@Transactional`, obtém ou cria cada `Producer` por nome (com cache local dentro da carga) e grava os `Movie` com os vínculos
- [X] T033 [US2] Criar `src/main/java/com/outsera/awards/ingestion/MovieFileLoader.java` (depende de T027, T031 e T032), um `SmartInitializingSingleton` que roda **antes** de o servidor aceitar conexões (research R2):
  - resolve o recurso e lança `MovieFileException(NOT_FOUND)` se ele não existir;
  - lê o arquivo, grava os filmes e registra cada `Rejection` como `WARN Linha {n} rejeitada: {motivo legível}`;
  - no fim, registra `INFO Carga do arquivo {caminho} concluída: {lidas} linhas lidas, {aceitas} aceitas, {rejeitadas} rejeitadas`.
- [X] T034 [US2] Rodar `./mvnw verify`: todos os testes T013 a T022b e o `SmokeIT` passam

**Checkpoint (PR do bolt 2)**: a carga está completa e verificável pelo log e pelo banco. Ainda não há endpoint de negócio, e a verificação por HTTP desses cenários entra nos bolts 3 e 5 (Princípio I).

---

## Phase 4: User Story 1 — Regras de cálculo dos intervalos (Priority: P1) — Bolt 3

**Goal**: calcular `min` e `max` corretamente para **qualquer** arquivo válido, com as regras de separação de nomes, as variações de `winner` e as decisões sobre ambiguidades.

**Independent Test**: cada CSV de cenário tem o resultado esperado calculado à mão, escrito no próprio teste. O teste chama `GET /api/producers/intervals` e compara `min` e `max` **inteiros**, com a ordem incluída.

### Testes do bolt 3 (escrever primeiro e ver falhar) ⚠️

Todos os cenários usam nomes fictícios (`Producer A`, `Producer B`...) e anos sem relação com o `movielist.csv` (FR-016a).

- [X] T035 [P] [US1] Criar `src/test/resources/scenarios/ties-min.csv` (dois produtores com intervalo mínimo 1; o que tem o `previousWin` maior aparece primeiro no arquivo, e há um par com o mesmo `previousWin` para desempatar pelo nome) e escrever `src/test/java/com/outsera/awards/intervals/TiesMinIT.java`: `"FR-014 FR-015 empate no mínimo"`. Todos os empatados aparecem em `min`, ordenados por `previousWin` e depois pelo nome
- [X] T036 [P] [US1] Criar `src/test/resources/scenarios/ties-max.csv` e escrever `src/test/java/com/outsera/awards/intervals/TiesMaxIT.java`: `"FR-014 FR-015 empate no máximo"`
- [X] T037 [P] [US1] Criar `src/test/resources/scenarios/three-or-more-wins.csv` (`Producer A` vence em 2000, 2005 e 2020; `Producer B` em 2001 e 2003) e escrever `src/test/java/com/outsera/awards/intervals/ThreeOrMoreWinsIT.java`: `"FR-012 só pares vizinhos"`. `max` é `Producer A` 2005→2020 (15) e nunca 2000→2020; `min` é `Producer B` (2)
- [X] T038 [P] [US1] Criar `src/test/resources/scenarios/unordered-lines.csv` e `src/test/resources/scenarios/ordered-lines.csv` (as mesmas linhas, em ordem embaralhada e em ordem cronológica) e escrever `src/test/java/com/outsera/awards/intervals/UnorderedLinesIT.java`, com uma classe `@Nested` por CSV e o mesmo resultado esperado: `"FR-016 independe da ordem das linhas"`
- [X] T039 [P] [US1] Criar `src/test/resources/scenarios/same-year-wins.csv` (`Producer A` vence dois filmes **diferentes** em 1995 e outro em 2000) e escrever `src/test/java/com/outsera/awards/intervals/SameYearWinsIT.java`: `"FR-013 duas vitórias no mesmo ano = intervalo 0"`. `min` é `Producer A` (0, 1995, 1995)
- [X] T040 [P] [US1] Criar `src/test/resources/scenarios/no-repeat-winners.csv` e escrever `src/test/java/com/outsera/awards/intervals/NoRepeatWinnersIT.java`: `"FR-018 nenhum produtor com duas vitórias"`. A resposta é 200 com `{"min":[],"max":[]}`
- [X] T041 [P] [US1] Criar `src/test/resources/scenarios/disjoint-producers.csv` (`Producer A` vence em 1950 e 1952; `Producer B` em 1925 e 1975, fora dos anos 1980 a 2019 do `movielist.csv`, como pede o SC-004a; mais alguns indicados não vencedores) e escrever `src/test/java/com/outsera/awards/intervals/DisjointProducersIT.java`: `"FR-002 FR-016a FR-016b SC-004a arquivo sem relação com o fornecido"`.
  - O caminho é passado pela chave **`MOVIES_CSV_PATH`** (`properties = "MOVIES_CSV_PATH=classpath:scenarios/disjoint-producers.csv"`), e não por `movies.csv.path`. Isso prova o mapeamento da variável de ambiente no `application.yml`, porque o Spring resolve variáveis de ambiente e propriedades pela mesma chave.
  - `min` é só `Producer A` (2, 1950, 1952) e `max` é só `Producer B` (50, 1925, 1975). Os anos 1960 e 2010, da primeira versão desta tarefa, foram trocados porque 2010 está no `movielist.csv` (decisão de 2026-11-22 em `docs/ai/decisoes.md`).
- [X] T042 [P] [US1] Criar `src/test/resources/scenarios/duplicate-winner.csv` (`1990;Filme X;Estúdio;Producer A;yes` duas vezes, e `Producer A` também vence em 1995) e escrever `src/test/java/com/outsera/awards/intervals/DuplicateWinnerIT.java`: `"FR-006d duplicata não gera intervalo 0"`. `min` e `max` são `Producer A` (5, 1990, 1995)
- [X] T043 [P] [US1] Criar `src/test/resources/scenarios/producer-separators.csv` e escrever `src/test/java/com/outsera/awards/intervals/ProducerSeparatorsIT.java`. O CSV cobre:
  - `Eric Fellner, and Tom Hooper`;
  - `Gayle Dickie, Kenn Viselman`;
  - `Brian Robbinsand Sharla Sumpter Bridgett`;
  - `Sid and Marty Krofft`;
  - `Producer A and`, com o `and` no fim do campo (research R4);
  - `Producer A and and Producer B`, com `and` repetido (research R4);
  - todos os demais casos do `producer-names.csv` do T022b, que é absorvido por este cenário (o `ProducerNamesIT` é removido aqui);
  - `A, , B`;
  - `Steven Perry` e `Steve Perry`;
  - espaços extras em volta dos nomes;
  - o mesmo nome repetido no mesmo filme (`Producer R, Producer R` e `Producer R and Producer R`);
  - um nome com acentos em UTF-8 (`José Núñez`).

  Cada forma aparece em duas vitórias com anos distintos, de modo que o nome resultante apareça (ou não) nos intervalos. Métodos com `"FR-007 FR-008 FR-009 FR-010 ..."`, verificando:
  - nomes separados corretamente;
  - `Brian Robbinsand Sharla Sumpter Bridgett` como um único produtor;
  - `Sid` e `Marty Krofft` como dois produtores;
  - `"FR-007 and no fim do campo"`: `Producer A and` resulta em 1 produtor, `Producer A`. Os intervalos trazem `Producer A`, e nenhum item tem o produtor `and` nem `Producer A and`;
  - `Steven Perry` e `Steve Perry` como produtores distintos;
  - `"FR-010 nome repetido no mesmo filme conta uma vez"`: `Producer R` não gera intervalo 0;
  - `"Edge UTF-8 preservado"`: `José Núñez` aparece na resposta exatamente com os acentos.
- [X] T044 [P] [US1] Criar `src/test/resources/scenarios/winner-variants.csv` (vitórias com `yes`, `YES`, ` Yes ` e uma linha com `winner` vazio, que não pode contar) e escrever `src/test/java/com/outsera/awards/intervals/WinnerVariantsIT.java`: `"FR-004 FR-011 variações de yes; vazio não conta"`
- [X] T045 [P] [US1] Criar `src/test/resources/scenarios/invalid-line-in-calc.csv` (uma linha inválida, com `winner=no` ou ano `19x0`, que criaria o menor intervalo se fosse aceita) e escrever `src/test/java/com/outsera/awards/intervals/InvalidLineIgnoredIT.java`: `"FR-006 linha inválida não influencia o resultado"`
- [X] T046 [P] [US1] Criar `src/test/resources/scenarios/single-interval.csv` e `src/test/resources/scenarios/all-equal-intervals.csv` e escrever `src/test/java/com/outsera/awards/intervals/IntervalEdgeCasesIT.java`, com uma classe `@Nested` por CSV:
  - `"Edge intervalo único aparece em min e max"`;
  - `"Edge todos iguais aparecem em min e max"`.
- [X] T047 [P] [US1] Criar o oráculo de teste `src/test/java/com/outsera/awards/support/IntervalOracle.java` e escrever `src/test/java/com/outsera/awards/intervals/RandomizedOracleIT.java` (`"SC-004 FR-016b arquivo gerado confere com o cálculo independente"`).
  - **Gerador:** para 3 sementes fixas, o `IntervalOracle` gera um CSV válido em diretório temporário, com dezenas de produtores fictícios (`Producer 001`...) e anos aleatórios entre 1900 e 2100:
    - várias vitórias por produtor, inclusive no mesmo ano;
    - linhas embaralhadas;
    - produtores unidos por `, ` e ` and `.
  - **Oráculo:** o resultado esperado é calculado na memória, direto da lista de linhas geradas. É uma implementação literal das FR-011 a FR-015, escrita só no teste e **sem reaproveitar código de produção**:
    - ordena os anos de vitória de cada produtor, mantendo os repetidos;
    - toma os pares consecutivos dessa lista, e dois anos iguais dão intervalo 0;
    - seleciona todos os pares com o menor e o maior valor;
    - ordena por `previousWin` e depois por nome.
  - **Restrições do gerador:** títulos únicos por ano, para não gerar duplicatas da FR-006d, e nomes sem a palavra `and` nem vírgula.
  - **Comparação:** cada semente é uma classe `@Nested`, com `@DynamicPropertySource` apontando `movies.csv.path` para o arquivo `file:` gerado. O teste compara a resposta de `GET /api/producers/intervals` com o oráculo, como JSON estrito.
  - **Reprodução:** as sementes ficam visíveis no `@DisplayName`.
- [X] T048 [P] [US1] Criar `src/test/resources/scenarios/harmless-clean.csv`, com as mesmas 4 linhas de `harmless-variations.csv` sem BOM, sem linhas em branco, sem espaços extras e com `yes` minúsculo. Estender `src/test/java/com/outsera/awards/ingestion/HarmlessVariationsIT.java` com duas classes `@Nested`, uma por CSV, e o mesmo resultado esperado de `GET /api/producers/intervals`: `"FR-005a US2-9 variações não mudam o resultado"`. Garantir que as 4 linhas geram ao menos um intervalo: o filme de 1991 do `harmless-variations.csv` passa a ser do `Producer A`, o mesmo de 1990 (decisão de 2026-11-22 em `docs/ai/decisoes.md`)
- [X] T049 [P] [US1] Estender `src/test/java/com/outsera/awards/ingestion/EmptyLoadIT.java` com a verificação por HTTP (Princípio I). Nas duas classes `@Nested`, `GET /api/producers/intervals` responde 200 com `{"min":[],"max":[]}`: `"FR-018 Edge arquivo sem linhas válidas"`

### Implementação do bolt 3

- [X] T050 [P] [US1] Criar os records `src/main/java/com/outsera/awards/producer/ProducerInterval.java` (`producer` String, `interval` int, `previousWin` int e `followingWin` int, **só esses 4 campos**; Princípio III) e `src/main/java/com/outsera/awards/producer/ProducerIntervalsResponse.java` (`min` e `max`, nunca nulos)
- [X] T051 [US1] Adicionar a `src/main/java/com/outsera/awards/producer/ProducerRepository.java` a consulta JPQL portável `select p.name, m.year from Movie m join m.producers p where m.winner = true`, devolvendo uma projeção (nome, ano)
- [X] T052 [US1] Criar `src/main/java/com/outsera/awards/producer/ProducerIntervalService.java` (depende de T050 e T051; research R6):
  - agrupar os anos por produtor e ordená-los;
  - gerar um intervalo por par vizinho, com `followingWin - previousWin` (o mesmo ano dá 0);
  - `min` e `max` recebem todos os intervalos com o menor e o maior valor;
  - ordenar por `previousWin` crescente e depois por nome (`String.compareTo`);
  - sem intervalos, as duas listas vêm vazias;
  - **nenhum valor fixo** (FR-016a).
- [X] T053 [US1] Criar `src/main/java/com/outsera/awards/producer/ProducerIntervalController.java` com `GET /api/producers/intervals` (`produces = application/json`), delegando ao serviço
- [X] T054 [US1] Rodar `./mvnw verify`: todos os testes T035 a T049 e os anteriores passam

**Checkpoint (PR do bolt 3)**: as regras de cálculo estão comprovadas para arquivos arbitrários, inclusive gerados.

---

## Phase 5: User Story 1 — Endpoint de intervalos no formato do enunciado (Priority: P1) — Bolt 4

**Goal**: garantir que o endpoint cumpre exatamente o contrato da página 2 do enunciado e o `openapi.yaml`, incluindo o resultado de referência com o arquivo padrão.

**Independent Test**: subir com o `movielist.csv` padrão e comparar o corpo JSON **inteiro** com o esperado, e validar as respostas contra o contrato.

### Testes do bolt 4 (escrever primeiro e ver falhar) ⚠️

- [X] T055 [P] [US1] Criar `src/test/java/com/outsera/awards/support/ContractValidator.java`, que carrega `classpath:static/openapi.yaml` e valida status, `Content-Type` e corpo de uma resposta com `com.atlassian.oai:swagger-request-validator-core:3.0.0`. Adicionar essa dependência ao `pom.xml` em escopo `test`. Se houver conflito com o Boot 4, usar o plano B da research R8 (`networknt/json-schema-validator` sobre os schemas do contrato)
- [X] T056 [P] [US1] Escrever `src/test/java/com/outsera/awards/intervals/ReferenceFileIT.java`, sem `movies.csv.path` (arquivo padrão): `"FR-017 SC-001 resultado de referência"`. O corpo é **exatamente** igual, comparado como JSON estrito (`JSONAssert` em modo `STRICT`), a `{"min":[{"producer":"Joel Silver","interval":1,"previousWin":1990,"followingWin":1991}],"max":[{"producer":"Matthew Vaughn","interval":13,"previousWin":2002,"followingWin":2015}]}`. Esses valores ficam **só** no teste, nunca em `src/main`
- [X] T057 [P] [US1] Escrever `src/test/java/com/outsera/awards/intervals/IntervalsShapeIT.java`, que usa `no-repeat-winners.csv` e `disjoint-producers.csv` em classes `@Nested`:
  - `"FR-017 só os 4 campos por item"`: cada item tem exatamente as chaves `producer`, `interval`, `previousWin` e `followingWin`;
  - `"FR-018 listas vazias como [] e não null nem ausentes"`;
  - `"FR-017 Content-Type application/json"`.
- [X] T058 [P] [US1] Escrever `src/test/java/com/outsera/awards/docs/IntervalsContractIT.java`: `"FR-025 resposta de intervalos compatível com o contrato"`, validando com o `ContractValidator` as respostas do arquivo padrão e de `no-repeat-winners.csv`

### Implementação do bolt 4

- [X] T059 [US1] Copiar `specs/001-producer-intervals-api/contracts/openapi.yaml` para `src/main/resources/static/openapi.yaml`, sem nenhuma alteração. A fonte continua sendo o arquivo de `specs/`
- [X] T060 [US1] Ajustar `ProducerIntervalController` e os records até os testes T056 a T058 passarem (sem campos extras, sem `null` nas listas e com o `Content-Type` correto), sem alterar o contrato
- [X] T061 [US1] Rodar `./mvnw verify`: tudo verde

**Checkpoint (PR do bolt 4)**: o requisito obrigatório do enunciado está entregue. **É o MVP.**

---

## Phase 6: User Story 3 — Listagem de filmes com paginação e filtros (Priority: P2) — Bolt 5

**Goal**: `GET /api/movies`, paginado, com os filtros `year` e `winner`, ordenado por ano e título, e com 400 em Problem Details para parâmetros inválidos.

**Independent Test**: com um CSV de 25 filmes conhecidos, conferir itens, totais, ordem e filtros, e enviar parâmetros inválidos.

### Testes do bolt 5 (escrever primeiro e ver falhar) ⚠️

- [X] T062 [P] [US3] Criar `src/test/resources/scenarios/listing-sample.csv`: 25 filmes entre 1981 e 1985, 5 por ano, sendo 1 vencedor por ano. Incluir:
  - dois filmes do mesmo ano com títulos fora de ordem alfabética no arquivo;
  - um filme com `studios` vazio;
  - um filme com título acentuado em UTF-8, `Zé Ninguém`, o único que começa com `Z` no seu ano, para que a ordenação não dependa de collation.
- [X] T063 [US3] Escrever `src/test/java/com/outsera/awards/movies/MoviesListingIT.java` (depende do T062). Métodos:
  - `"FR-019 FR-021 página padrão"`: 20 itens, `page`=0, `size`=20, `totalElements`=25, `totalPages`=2;
  - `"FR-021 segunda página"`: `page=1` traz 5 itens;
  - `"FR-021 Edge página além da última"`: `page=2` e `page=2147483647` respondem 200 com `content` vazio e `totalElements` 25 (caso de borda da spec);
  - `"FR-020 filtro por ano"`: `year=1983` traz 5 itens;
  - `"FR-020 filtro por vencedor"`: `winner=true` traz 5 e `winner=false` traz 20;
  - `"FR-020 filtros combinados"`: `year=1983&winner=true` traz 1;
  - `"FR-022 ordenação ano e título"`;
  - `"US3-6 filtro sem resultado"`: `year=1900` responde 200 com `content` vazio e `totalElements` 0;
  - `"FR-006e estúdios vazio vira null"`;
  - `"FR-019 produtores e estúdios como texto do arquivo"`;
  - `"Edge UTF-8 preservado"`: o título `Zé Ninguém` volta exatamente com os acentos.
- [X] T064 [P] [US3] Escrever `src/test/java/com/outsera/awards/movies/MoviesInvalidParamsIT.java`, com `listing-sample.csv`. Para cada valor inválido, a resposta é 400, `Content-Type: application/problem+json`, e `invalidParams[0].name` é o parâmetro: `year=abc`, `year=-1`, `winner=yes`, `winner=1`, `page=-1`, `page=x`, `size=0` e `size=101`. `@DisplayName` `"FR-023 ..."`
- [X] T065 [P] [US3] Escrever `src/test/java/com/outsera/awards/docs/MoviesContractIT.java`: `"FR-025 respostas da listagem compatíveis com o contrato"`, validando com o `ContractValidator` uma resposta 200 e uma 400
- [X] T066 [P] [US3] Escrever `src/test/java/com/outsera/awards/movies/ResponseTimeIT.java`, sem `movies.csv.path` (arquivo padrão): `"SC-005 respostas abaixo de 1 segundo"`. Medir com `System.nanoTime()` a **primeira** chamada a `GET /api/producers/intervals` e a `GET /api/movies`, sem aquecimento, e exigir menos de 1 s em cada uma
- [X] T067 [US3] Nos ITs do bolt 2, **substituir** as verificações de banco (`JdbcTemplate`) pela verificação por `GET /api/movies`, porque os filmes carregados passam a ter endpoint (Princípio I).
  - As verificações de log continuam, porque os avisos e o resumo não têm endpoint.
  - Ao final, remover o `JdbcTemplate` da `ScenarioIT`.

  Verificações por arquivo:
  - `src/test/java/com/outsera/awards/ingestion/HeaderCaseIT.java`: `totalElements` 3;
  - `src/test/java/com/outsera/awards/ingestion/HarmlessVariationsIT.java`: 4 itens, com títulos e produtores sem espaços em volta e sem BOM;
  - `src/test/java/com/outsera/awards/ingestion/InvalidLinesIT.java`: só as 2 linhas válidas;
  - `src/test/java/com/outsera/awards/ingestion/LoadSummaryIT.java`: `totalElements` 8;
  - `src/test/java/com/outsera/awards/ingestion/OptionalStudiosIT.java`: `studios` nulo;
  - `src/test/java/com/outsera/awards/ingestion/DuplicateLinesIT.java`: um único `Filme X`, com `studios` = `Estúdio`;
  - `src/test/java/com/outsera/awards/ingestion/EmptyLoadIT.java`: `content` vazio e `totalElements` 0 nas duas classes `@Nested`;
  - `src/test/java/com/outsera/awards/ingestion/DefaultFileIT.java`: `totalElements` igual ao número de aceitas lido do resumo.

### Implementação do bolt 5

- [X] T068 [P] [US3] Criar `src/main/java/com/outsera/awards/web/InvalidParameterException.java` (nome e motivo do parâmetro) e `src/main/java/com/outsera/awards/web/ApiExceptionHandler.java` (`@RestControllerAdvice`), que gera um `ProblemDetail` com status 400, título `Bad Request`, `detail` `Parâmetro inválido na consulta` e a propriedade `invalidParams` = `[{name, reason}]`, com `Content-Type` `application/problem+json`
- [X] T069 [P] [US3] Criar os records `src/main/java/com/outsera/awards/movie/MovieItem.java` (`year`, `title`, `studios` nullable, `producers` texto e `winner`) e `src/main/java/com/outsera/awards/movie/MoviePage.java` (`content`, `page`, `size`, `totalElements` e `totalPages`). Não serializar o `Page` do Spring
- [X] T070 [US3] Adicionar a `src/main/java/com/outsera/awards/movie/MovieRepository.java` uma consulta JPQL portável com filtros opcionais, `where (:year is null or m.year = :year) and (:winner is null or m.winner = :winner) order by m.year, m.title`, paginada e com `countQuery`
- [X] T071 [US3] Criar `src/main/java/com/outsera/awards/movie/MovieQueryService.java` (research R7). Os parâmetros chegam como `String` e são validados assim:
  - `year`: `\d{1,9}`;
  - `winner`: `true` ou `false`, sem diferenciar maiúsculas;
  - `page`: inteiro de 32 bits maior ou igual a 0, padrão 0;
  - `size`: inteiro de 1 a 100, padrão 20.

  Um valor inválido lança `InvalidParameterException`, com todos os parâmetros inválidos da consulta. Quando o deslocamento (`page` × `size`) passa de `Integer.MAX_VALUE`, a página fica além da última com qualquer arquivo realista: o serviço devolve `content` vazio com os totais da contagem, sem pedir a página ao banco (o Spring Data recusaria o deslocamento).
- [X] T072 [US3] Criar `src/main/java/com/outsera/awards/movie/MovieController.java` com `GET /api/movies` e os parâmetros `year`, `winner`, `page` e `size` como `String` opcionais. Parâmetros desconhecidos são ignorados
- [X] T073 [US3] Rodar `./mvnw verify`: tudo verde

**Checkpoint (commit da fase 6, no PR dos bolts 5 e 6)**: a listagem está completa e não altera o contrato de intervalos. Todos os ITs de carga do bolt 2 já verificam o resultado por HTTP.

---

## Phase 7: User Story 4 — Swagger UI e contrato publicado (Priority: P3) — Bolt 6

**Goal**: documentação interativa em `/swagger-ui.html`, servida a partir do `openapi.yaml` estático (contrato definido antes do código, research R8).

**Independent Test**: abrir o Swagger UI e o `/openapi.yaml` com a aplicação no ar e comparar o arquivo publicado com o de `specs/`.

### Testes do bolt 6 (escrever primeiro e ver falhar) ⚠️

- [X] T074 [US4] Escrever `src/test/java/com/outsera/awards/docs/ApiDocsIT.java`:
  - `"FR-024 Swagger UI disponível"`: `GET /swagger-ui.html` leva, seguindo o redirect, a uma página 200 em HTML;
  - `"FR-024 Swagger UI aponta para /openapi.yaml"`: a página do Swagger UI carrega o contrato de `/openapi.yaml`, e os arquivos do webjar que ela usa respondem 200;
  - `"FR-024 a geração automática do contrato está desligada"`: `GET /v3/api-docs` responde 404;
  - `"FR-024 FR-025 contrato publicado idêntico ao da spec"`: o corpo de `GET /openapi.yaml` é igual, byte a byte, a `specs/001-producer-intervals-api/contracts/openapi.yaml` (lido do disco relativo ao `basedir` do Maven);
  - `"FR-024 contrato descreve os dois endpoints"`.

### Implementação do bolt 6

- [X] T075 [US4] Adicionar ao `pom.xml` `org.webjars:swagger-ui:5.34.0` e `org.webjars:webjars-locator-lite` (versão gerenciada pelo Boot). **Mudança no bolt 6:** o plano original usava o `springdoc-openapi-starter-webmvc-ui:3.2.0`, que não serve o Swagger UI com `springdoc.api-docs.enabled=false` (research R8; decisão em `docs/ai/decisoes.md`)
- [X] T076 [US4] Criar `src/main/resources/static/swagger-ui.html`, que carrega o Swagger UI dos caminhos sem versão do webjar (`/webjars/swagger-ui/...`) com `url: "/openapi.yaml"`. Nada no `application.yml`
- [X] T077 [US4] Rodar `./mvnw verify`: tudo verde

**Checkpoint (commit da fase 7, no PR dos bolts 5 e 6)**: as quatro histórias funcionam de forma independente.

---

## Phase 8: Polish & Cross-Cutting Concerns — Bolt 6 (fechamento)

- [X] T078 Confirmar que não existe teste unitário: nenhuma classe em `src/test/java` sem o sufixo `IT` contém `@Test` (as classes de `support/` não têm testes). Corrigir qualquer violação do Princípio I. Confirmar também:
  - nenhum teste usa `@MockitoBean`, `@MockBean`, Mockito ou outro dublê;
  - todo `*IT`, menos o `StartupFailureIT`, usa `@SpringBootTest` (direto ou pela `ScenarioIT`);
  - nenhum `*IT` verifica pelo banco (não sobra `JdbcTemplate` nos testes), porque no fim do marco os dados carregados têm endpoint;
  - a verificação por log só aparece para comportamentos sem endpoint (avisos, resumo, ordem da carga e falhas de inicialização), e todo IT de `ingestion/`, menos o `StartupFailureIT`, também faz uma chamada HTTP.
- [X] T079 [P] Executar a busca do SC-004b descrita em `specs/001-producer-intervals-api/quickstart.md` (passo 6) sobre `src/main/java`. O resultado deve ser `OK`
- [X] T080 [P] Conferir a matriz de rastreabilidade de `specs/001-producer-intervals-api/plan.md` com as classes de teste realmente criadas e corrigir qualquer diferença
- [ ] T081 Executar os passos 1 a 7 de `specs/001-producer-intervals-api/quickstart.md` em um clone limpo, no Windows (`mvnw.cmd`) e no Git Bash (`./mvnw`). Itens de checagem manual, registrados na descrição do PR:
  - o `./mvnw verify` termina em menos de 2 minutos com as dependências já baixadas;
  - o passo 4 roda com a variável de ambiente `MOVIES_CSV_PATH` real, no shell (FR-002);
  - SC-003: a aplicação sobe e responde aos intervalos com um único comando em menos de 2 minutos;
  - SC-004: a rodada do testador adversário (Codex) foi feita sobre o Marco 1, e cada falha virou um CSV e um `*IT` permanentes.

  **Situação em 2026-11-22 (aberta):** o implementador executou os passos 1 a 7 num clone limpo do commit `bc4dbde`, no Windows (`mvnw.cmd`) e no Git Bash (`./mvnw`), e todos conferem. O `verify` levou 29 s no Windows e 26 s no Git Bash, com 115 testes. O passo 4 rodou com `MOVIES_CSV_PATH` exportada no shell. Pelo SC-003, a primeira resposta veio 9 s depois do comando. Falta a rodada do testador adversário (SC-004), e o registro na descrição do PR fica com o responsável.
- [X] T082 [P] Completar `docs/ai/decisoes.md` com as decisões do Marco 1 que divergem do briefing ou da sugestão da IA:
  - linha inválida ignorada com aviso, em vez de rejeitar a carga inteira (FR-006);
  - duplicata identificada por ano + título (FR-006d);
  - cabeçalho sem diferenciar maiúsculas, mas com ordem fixa (FR-003);
  - contrato em `/openapi.yaml` em vez de `/v3/api-docs` (R8).

  As emendas 1.1.0 e 1.2.0 da constituição já estão registradas.
- [X] T083 [P] Princípio VI: adicionar ao `README.md` a seção "Requisitos do enunciado → testes", com uma tabela que liga cada item exigido pelo enunciado ao `*IT` que o comprova:
  - a carga do CSV na inicialização;
  - o formato de `min` e `max`;
  - os empates;
  - as listas vazias;
  - o resultado de referência;
  - somente testes de integração (item 4.2);
  - execução só com o wrapper (item 4.3).

  A tabela é derivada da matriz do `plan.md`. Se nesta rodada o `README.md` pertencer ao papel de documentação, registrar o conteúdo da tabela como pedido a esse papel em vez de editar o arquivo (AGENTS.md, "Papéis e propriedade"). Nesse caso, a tarefa só fecha quando a tabela estiver no README

  **Situação em 2026-11-22 (fechada):** a tabela foi registrada como pedido em `docs/ai/pedidos-documentacao.md`, e o papel de documentação a incluiu na seção 6 do `README.md`.

---

## Rodada do testador adversário (Codex) — casos de `docs/ai/adversario/`

Decisões na sessão 2026-11-22 da spec e em `docs/ai/decisoes.md`.

- [X] T084 [US1] Copiar os 13 casos para `src/test/resources/scenarios/adversario/` e escrever um `*IT` por caso em `src/test/java/com/outsera/awards/adversario/`, comparando o corpo de `GET /api/producers/intervals` com o `expected.json` como JSON estrito. Os expected dos casos 01 a 08, 10 e 11 são os do testador, sem alteração. Os dos casos 12 e 13 foram escritos pelo implementador a partir das decisões. O do caso 09 foi ajustado só em `src/test/resources` (listas vazias), e o original do testador fica intacto em `docs/ai/adversario/`
- [X] T085 [US2] Acrescentar ao `invalid-lines.csv` as linhas com ano `0` e `10000` e ao `InvalidLinesIT` os dois casos de `YEAR_OUT_OF_RANGE` (FR-006c)
- [X] T086 [US2] `MovieCsvReader`: aceitar só anos de 1 a 9999 e rejeitar os demais com `YEAR_OUT_OF_RANGE`, sem estourar com sequências longas de dígitos (FR-006)
- [X] T087 [US1] `ProducerNameParser`: reconhecer a palavra `and` como separador sem diferenciar maiúsculas (FR-007)

---

## Dependencies & Execution Order

### Bolt (fase) dependencies

- **Bolt 1** (Setup + Foundational): sem dependências. Bloqueia todos os demais.
- **Bolt 2** (US2, carga): depende do bolt 1.
- **Bolt 3** (US1, regras): depende do bolt 2, porque o cálculo lê os dados carregados. O T048 e o T049 estendem ITs do bolt 2.
- **Bolt 4** (US1, contrato): depende do bolt 3.
- **Bolt 5** (US3, listagem): depende do bolt 2 **e** do `ContractValidator` (T055, bolt 4). Se for feito em paralelo ao bolt 4, mover o T055 para o bolt 5 e o T059 para antes do T065. O T067 estende ITs do bolt 2.
- **Bolt 6** (US4 + Polish): depende do bolt 4 (`static/openapi.yaml`). O fechamento (T078 a T083) depende de todos.
- **Bolts 5 e 6 juntos:** entregues no mesmo branch e PR, na ordem fase 6 → fase 7 → fase 8, um commit por fase.

### Dentro de cada bolt

- Os testes (e os CSVs de cenário) vêm primeiro e precisam **falhar** antes da implementação.
- Primeiro o esquema e as entidades, depois os serviços e por fim os controllers.
- O bolt só fecha com `./mvnw verify` verde, e o PR é aberto pelo responsável (constituição, fluxo do bolt).

### Parallel Opportunities

- Bolt 1: T004, T005 e T006 em paralelo; T007 e T008 em paralelo.
- Bolt 2: todos os CSVs e ITs marcados [P] (T012 a T016 e T018 a T022) em paralelo; as entidades e classes de apoio T024 a T030 em paralelo.
- Bolt 3: T035 a T049 em paralelo (um CSV e um IT por cenário).
- Bolt 4: T055 a T058 em paralelo.
- Bolt 5: T062 e T064 a T066 em paralelo; T068 e T069 em paralelo.
- Os bolts 4 e 5 podem andar em paralelo, em worktrees separados, com o ajuste descrito acima.

---

## Parallel Example: Bolt 3 (User Story 1, regras)

```bash
# Cenários independentes, cada um com o seu CSV e o seu IT:
Task: "T035 ties-min.csv + TiesMinIT"
Task: "T037 three-or-more-wins.csv + ThreeOrMoreWinsIT"
Task: "T041 disjoint-producers.csv + DisjointProducersIT"
Task: "T047 IntervalOracle + RandomizedOracleIT"
```

---

## Implementation Strategy

### MVP First (requisito obrigatório do enunciado)

1. Bolt 1: o projeto sobe e testa com o wrapper.
2. Bolt 2: a carga do CSV.
3. Bolt 3: as regras de cálculo.
4. Bolt 4: o endpoint no formato exato, com o resultado de referência. **Pare e valide.** Isso já cumpre o enunciado.

### Incremental Delivery

5. Bolt 5: a listagem (extra pela seção 1 do PDF).
6. Bolt 6: o Swagger UI e o fechamento do marco (tag `v0.1.0` feita pelo responsável).

---

## Notes

- Um bolt por branch e um PR, com a exceção dos bolts 5 e 6, entregues juntos (ver o mapeamento bolt → fase). Nenhum commit direto na `main`, e PR, merge e tag ficam com o responsável.
- Os CSVs de ataque do testador adversário (Codex) entram como novos arquivos em `src/test/resources/scenarios/`, com o seu `*IT`, no bolt em que a falha for encontrada.
- Os valores do resultado de referência ficam **só** nos testes (T056), nunca em `src/main`.
- Se um teste revelar uma ambiguidade nova, a spec é atualizada antes do código (Princípio IV).