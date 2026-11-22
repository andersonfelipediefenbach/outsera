# Research — Marco 1 (001-producer-intervals-api)

Decisões técnicas da fase 0. As versões foram conferidas no Maven Central em 2026-11-18.

## R1. Versões da stack

- **Decision**:
  - Java 25 (OpenJDK 25.0.2 instalado);
  - Spring Boot **4.1.2**;
  - springdoc-openapi **3.2.0** (`springdoc-openapi-starter-webmvc-ui`);
  - Apache Commons CSV **1.14.1**;
  - Maven **3.9.16** via Maven Wrapper **3.3.4** (distribuição `only-script`);
  - H2 e Flyway na versão gerenciada pelo Boot.
- **Rationale**: a 4.1.2 é a última versão estável do Boot, porque a 4.2.0 ainda está em RC2. O
  springdoc 3.2.0 declara a 4.1.2 como parent, então a compatibilidade é garantida. O Maven 4 ainda é
  RC. O wrapper `only-script` baixa o Maven sem precisar de um JAR binário no repositório.
- **Alternatives considered**:
  - Boot 4.2.0-RC2: não é estável.
  - Boot 3.5.x: é da linha anterior e não é o "estável atual".
  - Maven 4 RC: não é estável.
  - Wrapper com JAR: deixaria um binário no repositório sem necessidade.

## R2. Momento da carga do CSV (FR-001)

- **Decision**: a carga roda em um bean que implementa `SmartInitializingSingleton`
  (`afterSingletonsInstantiated`), depois do Flyway e dos repositórios e **antes** de o servidor web
  começar a aceitar conexões. Uma exceção nessa fase aborta a inicialização.
- **Rationale**: um `ApplicationRunner` ou o `ApplicationReadyEvent` rodam depois de o Tomcat já estar
  ouvindo, o que abriria uma janela de consulta com dados incompletos. O servidor web só sobe em
  `finishRefresh`, depois de todos os singletons.
- **Alternatives considered**:
  - `ApplicationRunner`: abre a janela de consulta antes do fim da carga.
  - `@PostConstruct`: o momento é menos explícito e as dependências ficam menos claras.
  - Readiness via Actuator: é do Marco 2.

## R3. Leitura do CSV e variações aceitas (FR-003, FR-005, FR-005a, FR-006)

- **Decision**: Commons CSV com `CSVFormat.DEFAULT` ajustado para:
  - delimitador `;` e aspas `"` (campos entre aspas podem conter `;`);
  - `ignoreEmptyLines=true`, `trim=true` e `ignoreSurroundingSpaces=true`.

  O BOM (`U+FEFF`) é removido do `Reader` **antes** de o Commons CSV ler qualquer caractere: o leitor
  lê o primeiro caractere com `mark`/`reset` e o descarta se for o BOM. Remover o BOM só do primeiro
  campo é tarde demais (revisão do Codex no PR do bolt 2): com o BOM na frente, o Commons CSV não
  reconhece a aspa de um cabeçalho entre aspas (`"year";...`), e uma linha só com o BOM deixa de ser
  uma linha em branco e vira o cabeçalho.

  O cabeçalho é lido como um registro comum e validado à mão:
  - aplica `trim` e compara os nomes sem diferenciar maiúsculas (`equalsIgnoreCase`), na ordem exata;
  - exige exatamente 5 colunas.

  Se o cabeçalho tiver uma única coluna contendo `,`, a mensagem diz que o separador esperado é `;`.
  Os demais registros são validados por linha (seção "Validação de linha" de `data-model.md`).
- **Número da linha**: é a linha física no arquivo (o cabeçalho é a linha 1), obtida com
  `CSVParser.getCurrentLineNumber()` depois de ler o registro. Assim, as linhas em branco puladas
  continuam contando, e o aviso aponta para a linha certa no editor. Conferido com o Commons CSV
  1.14.1 em 2026-11-22: o número é correto também na última linha sem quebra final e com CRLF. Num
  registro com quebra de linha dentro de aspas, o número é o da última linha física do registro.
- **Linha só com espaços**: o `ignoreEmptyLines` só pula linhas totalmente vazias. Uma linha só com
  espaços chega como um registro de uma coluna vazia e seria rejeitada por `COLUMN_COUNT`. O leitor
  trata esse registro como linha em branco (não conta como lida e não gera aviso), como pede a
  FR-005a.
- **Arquivo que não pode ser analisado** (aspas sem fechamento ou bytes que não são UTF-8 válido): é
  erro estrutural `UNREADABLE`, porque o leitor não consegue separar as linhas com segurança. A
  mensagem cita a linha onde a leitura parou, quando o Commons CSV a informa.
- **Ano**: aceita só dígitos (`\d{1,9}`), sem sinal nem decimal. Ano vazio ou fora desse padrão
  invalida a linha. Isso resolve o ponto "faixa válida de ano", que ficou em aberto na clarificação,
  de forma conservadora e coerente com o FR-006 ("não numérico").
- **Codificação**: UTF-8, explícita na abertura do `Reader`.
- **Alternatives considered**:
  - `setHeader().setSkipHeaderRecord()` com nomes: o Commons CSV compara os nomes diferenciando
    maiúsculas e não detecta ordem errada com uma mensagem clara.
  - `BOMInputStream` (Commons IO): o Commons IO só chega como dependência transitiva do Commons CSV, e
    usá-lo direto exigiria declará-lo no `pom.xml`, uma dependência nova só para um caractere.
  - Remover o BOM do primeiro campo, depois da leitura: não trata o cabeçalho entre aspas nem o BOM
    seguido de linha em branco (abordagem original, substituída após a revisão do Codex).
  - `String.split`: não trata aspas.

## R4. Separação de produtores (FR-007 a FR-010)

- **Decision**: a coluna é separada **em etapas**, sem uma regex única:
  1. dividir o campo pelas vírgulas;
  2. em cada parte, ler as palavras (sequências sem espaço). Uma palavra igual a `and`, exatamente em
     minúsculas como no enunciado, é separador. As palavras entre dois separadores formam um nome;
  3. o nome é o trecho original do campo, da primeira à última palavra do grupo. Assim, o texto interno
     é preservado como está, inclusive espaços duplos;
  4. descartar as partes vazias e remover as repetidas no mesmo filme (`LinkedHashSet`, que preserva a
     ordem).

  Como cada `and` é reconhecido pela palavra, e não pelos espaços em volta, vários `and` seguidos
  contam como separadores, e o início e o fim do campo não precisam de tratamento especial.
- **Histórico (duas revisões do Codex no PR do bolt 2; ver `docs/ai/decisoes.md`):**
  - a primeira regex, `\s*,\s*(?:and\s+)?|\s+and\s+`, exigia espaço depois do `and`. Como o leitor de
    CSV aplica `trim` antes do parser (T031), o campo ` , and ` chegava como `, and` e deixava `and`
    como produtor;
  - a segunda, `\s*,\s*(?:and(?=\s|$)\s*)?|(?:^|\s+)and(?=\s|$)\s*`, consumia o espaço depois de um
    `and`, e o `and` seguinte deixava de ser reconhecido: `Producer A and and Producer B` virava
    `Producer A` e `and Producer B`, e `and and` virava `and`.

  Dois defeitos na mesma regex levaram à troca pela separação em etapas, mais fácil de ler e de
  conferir caso a caso.
- **Casos verificados** (todos cobertos pelo `ProducerSeparatorsIT`, T043, que no bolt 3 absorveu o
  `ProducerNamesIT` do T022b):
  - `Eric Fellner, and Tom Hooper` dá 2 nomes;
  - `Gayle Dickie, Kenn Viselman` dá 2 nomes;
  - `Brian Robbinsand Sharla Sumpter Bridgett` dá 1 nome (não há espaço antes de `and`);
  - `Sid and Marty Krofft` dá 2 nomes;
  - `A, , B` dá `A` e `B`;
  - `, and` (o campo ` , and ` depois do `trim`) e `and` isolado dão nenhum nome, então a linha é
    rejeitada com `PRODUCERS_MISSING`;
  - `Producer A and` e `Producer A, and` dão só `Producer A`;
  - `and Producer B` dá só `Producer B`;
  - `Anderson Silva` dá 1 nome, e `Joel Silver and Anderson` dá `Joel Silver` e `Anderson` (`And`
    maiúsculo não é separador);
  - `A, andrew B` dá `A` e `andrew B` (o `and` seguido de letra não é palavra inteira);
  - `Producer A and and Producer B` dá `Producer A` e `Producer B`;
  - `and and` dá nenhum nome, então a linha é rejeitada com `PRODUCERS_MISSING`;
  - `Sid  Krofft  and  Marty`, com espaços duplos, dá `Sid  Krofft` (o espaço interno é preservado) e
    `Marty`;
  - `Producer R, Producer R` dá um único `Producer R`.
- **Alternatives considered**:
  - corrigir a regex de novo (por exemplo, com lookbehind para o `and` seguinte): seria o terceiro
    ajuste na mesma expressão, cada vez mais difícil de ler e de conferir;
  - dividir por espaços e juntar as palavras com um espaço: normalizaria espaços internos, o que a
    FR-008 proíbe;
  - `and` sem diferenciar maiúsculas: não foi pedido e aumentaria o risco de separar nomes por engano.

## R5. Modelo de persistência e esquema (FR-006d, Princípio V)

- **Decision**: três tabelas criadas pelo Flyway (`V1__create_schema.sql`): `movie`, `producer` e
  `movie_producer`. A unicidade de (`release_year`, `title`) fica na tabela `movie`, e a de `name` na
  tabela `producer`. O JPA roda com `ddl-auto=validate`. As consultas são JPQL, sem SQL nativo, para
  servirem a H2 e a PostgreSQL. A coluna se chama `release_year`, porque `year` é palavra reservada no
  H2.
- **Rationale**: o modelo relacional normalizado permite a consulta de intervalos por JOIN e já é o
  esquema que o Marco 3 (PostgreSQL) vai usar. A duplicata ano + título também é barrada no banco, como
  defesa adicional à verificação do carregador.
- **Alternatives considered**: tabela única com os produtores em texto (exigiria separar os nomes de
  novo a cada consulta) e `ddl-auto=create` (viola o uso do Flyway da constituição).

## R6. Cálculo dos intervalos (FR-011 a FR-018)

- **Decision**: uma consulta JPQL busca os pares (nome do produtor, ano) dos filmes vencedores. O
  serviço agrupa por produtor, ordena os anos, gera os pares vizinhos, calcula o mínimo e o máximo e
  ordena os empatados por `previousWin` e depois por nome, com `String.compareTo` (por code point, que
  diferencia maiúsculas). O cálculo roda a cada requisição.
- **Rationale**: O(n log n) sobre cerca de 200 linhas leva milissegundos. Sem cache, não há estado a
  invalidar no Marco 3, quando novos vencedores chegarem pelo Kafka. A ordenação por code point é
  determinística e independente de locale, o que resolve o ponto "ordem com maiúsculas e acentos".
- **Alternatives considered**:
  - Calcular uma vez na carga: obriga a invalidar o resultado no Marco 3.
  - Cálculo em SQL com funções de janela (`LAG`): é portável entre H2 e PostgreSQL, mas é mais difícil
    de ler e de manter.
  - `Collator` por locale: o resultado mudaria conforme a máquina.

## R7. Parâmetros da listagem e erros (FR-019 a FR-023)

- **Decision**: os parâmetros `year`, `winner`, `page` e `size` chegam como `String` e são validados
  à mão:
  - `year` aceita só dígitos;
  - `winner` aceita só `true` ou `false`, sem diferenciar maiúsculas;
  - `page` precisa ser um inteiro maior ou igual a 0;
  - `size` precisa ser um inteiro de 1 a 100.

  Um erro gera `InvalidParameterException`, convertida por um `@RestControllerAdvice` em
  `ProblemDetail` (status 400, `application/problem+json`) com a propriedade `invalidParams`
  (`[{name, reason}]`). A resposta usa um DTO próprio (`content`, `page`, `size`, `totalElements`,
  `totalPages`), e não a serialização do `Page` do Spring. A ordenação fixa é por ano e depois por
  título. Parâmetros desconhecidos são ignorados.

  Detalhes definidos no bolt 5 (ver `docs/ai/decisoes.md`):
  - todos os parâmetros inválidos da consulta aparecem em `invalidParams`, na ordem `year`, `winner`,
    `page` e `size`;
  - `page` é um inteiro de 32 bits. Uma página além da última responde 200 com `content` vazio e os
    totais reais. Quando `page` × `size` passa de `Integer.MAX_VALUE`, o serviço consulta só a contagem,
    porque o Spring Data recusa esse deslocamento e responderia 500.
- **Rationale**: o conversor padrão do Spring aceita `yes`, `on` e `1` como booleanos, e a spec pede
  400 para valores não booleanos. A serialização direta do `PageImpl` é instável e o próprio Spring
  Data desaconselha.
- **Alternatives considered**: `@RequestParam Boolean` com Bean Validation (aceita valores lenientes)
  e `Pageable` direto (ordenação e limites controlados pelo cliente).

## R8. Contrato OpenAPI e Swagger UI (FR-024, FR-025, Princípio III)

- **Decision**: o contrato vem **antes** do código. O arquivo `src/main/resources/static/openapi.yaml`,
  copiado de `contracts/openapi.yaml`, é a única fonte. O springdoc fornece o Swagger UI
  (`/swagger-ui.html`) apontando para ele (`springdoc.swagger-ui.url=/openapi.yaml`), com a geração
  automática desligada (`springdoc.api-docs.enabled=false`). Os testes validam as respostas reais
  contra esse mesmo arquivo com `swagger-request-validator-core` 3.0.0 (Atlassian), só no escopo de
  teste. Na versão 3.0.0 o artifact foi relocado pelo projeto para
  `com.atlassian.oai:openapi-request-validator-core`, que é a coordenada declarada no `pom.xml`
  (mesmos pacotes Java; decisão de 2026-11-22 em `docs/ai/decisoes.md`).
- **Rationale**: o briefing exige que "o contrato OpenAPI seja a fonte do Swagger UI e do teste que
  valida as respostas". Com geração por anotações, haveria dois contratos, o desenhado e o gerado,
  que poderiam divergir.
- **Impacto na spec**: o caminho do contrato legível por máquina passa de `/v3/api-docs` (proposto nas
  Assumptions) para `/openapi.yaml`. A spec foi atualizada antes do código (Princípio IV).
- **Risco**: o validador usa Jackson 2 e o Boot 4 usa Jackson 3. Os pacotes são diferentes
  (`com.fasterxml` e `tools.jackson`), então os dois convivem. Se houver conflito, a alternativa é o
  `networknt/json-schema-validator` sobre os schemas do contrato.
- **Alternatives considered**: code-first com springdoc (contrato derivado do código) e openapi4j
  (sem manutenção desde 2021).

## R9. Estratégia de testes (Princípios I e VI, FR-026, FR-027)

- **Decision**:
  - **Só testes de integração**, com nomes `*IT` e executados pelo **maven-failsafe-plugin** na fase
    `verify`. O Surefire não tem testes. Cada classe é um cenário que estende a base `ScenarioIT`
    (`@SpringBootTest(webEnvironment = RANDOM_PORT)`) e escolhe o CSV com
    `@TestPropertySource(properties = "movies.csv.path=classpath:scenarios/<cenario>.csv")`. A subclasse
    não redeclara `@SpringBootTest`, porque a anotação dela substituiria a da base. Ou seja, um CSV
    por cenário e um contexto por CSV, guardado no cache de contextos do Spring. As chamadas HTTP são
    reais, feitas com `RestTestClient` (Spring Framework 7, via `@AutoConfigureRestTestClient`).
  - **Falhas de inicialização**: o teste executa `SpringApplication` de verdade, com o CSV inválido e
    `server.port=0`, e verifica a exceção e a mensagem (do `FailureAnalyzer`). Continua sendo a
    aplicação completa subindo.
  - **Logs** (avisos e resumo): verificados com `OutputCaptureExtension` no mesmo teste que sobe o
    cenário.
  - **Testes de carga antes dos endpoints** (Princípio I, constituição 1.2.0): no bolt 2, com a aplicação real no
    ar, o resultado é verificado pelo log e pelo banco (`JdbcTemplate` com SQL sobre as tabelas, sem
    depender das classes de produção). Nos bolts 3 e 5, os mesmos testes ganham a verificação por HTTP.
  - **Oráculo independente** (SC-004): o `RandomizedOracleIT` gera arquivos válidos a partir de
    sementes fixas e compara a resposta HTTP com um cálculo de força bruta escrito só no teste.
  - **Rastreabilidade**: cada método de teste tem `@DisplayName` começando pelos IDs dos requisitos
    (por exemplo, `"FR-012 FR-016 só pares vizinhos, independente da ordem"`). A matriz FR → teste fica
    em `plan.md`.
  - **Banco**: cada contexto usa um H2 com nome único (`spring.datasource.generate-unique-name`, o
    padrão do Boot), então os cenários não se contaminam.
- **SC-004b** (nenhum dado fixo no código): é verificado por uma busca no código de produção
  (`quickstart.md`, passo 6), que será automatizada no pipeline do Marco 2. Não vira um teste JUnit,
  porque não seria um teste de integração (Princípio I).
- **Alternatives considered**:
  - `@DirtiesContext` com um CSV trocado em tempo de execução: é mais lento e menos isolado.
  - MockMvc: não faz uma chamada HTTP real.
  - `TestRestTemplate`: no Boot 4 o `RestTestClient` é o substituto recomendado.

## R10. Configuração (Princípio VII, FR-002)

- **Decision**:
  - `movies.csv.path` tem como padrão `classpath:movielist.csv` e pode ser sobrescrita por
    `MOVIES_CSV_PATH`, pela relaxed binding do Spring.
  - Os prefixos `classpath:` e `file:` são respeitados. Um valor sem prefixo é tratado como caminho no
    sistema de arquivos.
  - A porta é configurada com `server.port=${PORT:8080}`.
  - O H2 Console fica desligado.
- **Rationale**: no contexto web, o `ResourceLoader` resolveria um caminho sem prefixo como recurso do
  servlet context, o que não é o que um avaliador espera ao passar `/tmp/outro.csv`.

## R11. Mensagens de falha na inicialização (FR-005, SC-006)

- **Decision**: a exceção `MovieFileException` tem um motivo tipado: `NOT_FOUND`, `UNREADABLE`,
  `INVALID_HEADER` ou `INVALID_SEPARATOR`. Um `FailureAnalyzer`, registrado em
  `META-INF/spring.factories`, a converte no bloco "APPLICATION FAILED TO START". A **Description**
  traz o caminho, o motivo e o cabeçalho encontrado. A **Action** traz o cabeçalho esperado e a
  variável `MOVIES_CSV_PATH`.
- **Rationale**: sem isso, o avaliador veria uma stack trace longa de `BeanCreationException`.

## R12. Pacote base

- **Decision**: o pacote base é `com.outsera.awards`. O diretório vazio `src/main/java/org/example`,
  gerado pela IDE, será removido.
- **Rationale**: é um nome de domínio legível. O `org.example` é só o padrão da IDE.