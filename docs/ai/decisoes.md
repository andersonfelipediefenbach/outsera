# Registro de decisões

Sugestões e decisões relevantes, aceitas ou rejeitadas, com o motivo (item 5.2 do enunciado). As
decisões de arquitetura ficam em ADRs, em `docs/adr/`.

## 2026-11-25 — Revisão do Codex no PR do Marco 3: grupo do consumidor (apontamento 1)

- **Apontamento (aceito):** o `@KafkaListener(id = "awards-movies")` usa o id como grupo (`idIsGroup`, que é
  verdadeiro por padrão) e ignora `KAFKA_CONSUMER_GROUP`. A FR-003 pedia toda a configuração por variável
  de ambiente, e o grupo estava fixo no código.
- **Por que os testes não pegaram:** cada classe de teste tem o seu broker, então todas usavam o mesmo grupo
  fixo sem conflito. Nenhum teste conferia o grupo registrado no broker.
- **Correção:**
  - a spec veio primeiro: a FR-003 agora cita o grupo do consumidor;
  - o ouvinte passou a usar `idIsGroup = false`, e o grupo vem de `spring.kafka.consumer.group-id`
    (`KAFKA_CONSUMER_GROUP`, padrão `awards`);
  - o `clientIdPrefix = "awards-movies"` ficou explícito. Sem ele, o `client.id` do Kafka seria derivado do
    grupo, e a série de atraso das métricas e o dashboard mudariam de nome conforme a variável. O
    `client_id` passa a ser `awards-movies-<n>`, e o `KafkaMetricsIT`, o dashboard, a R19 e o contrato
    operacional foram atualizados.
- **Comprovação:** o `KafkaConsumerGroupIT` sobe com `KAFKA_CONSUMER_GROUP=grupo-configurado-it` e confere
  no broker, pelo `AdminClient` real, que esse é o grupo registrado, com um membro, e que `awards-movies`
  não aparece. Antes da correção, ele falhou com o grupo `awards-movies`.
- **Efeito no compose:** com o padrão, o grupo passa a ser `awards`. Um volume do Kafka criado antes da
  correção tem os offsets no grupo antigo, então o consumidor novo relê o tópico desde o começo. Isso é
  inofensivo, porque a gravação é idempotente.

## 2026-11-25 — Tempo do `./mvnw verify` do Marco 3 (correção da etapa 2)

- **Origem:** o `verify` passou de 96 s na linha de base para cerca de 4 min, acima do limite de 2× da
  research R15 (entrada da etapa 2). O responsável não aceitou os 4 min nem o corte de casos, e pediu a
  redução sem perder cobertura.
- **Medição** (`./mvnw verify` sem `clean`, com o código já compilado, na mesma máquina):

  | | Tempo | Razão sobre a linha de base (96 s) |
  | --- | --- | --- |
  | Antes | 249 s | 2,6× |
  | Depois | **161 s** | **1,68×**, dentro do limite de 2× |

  Os ITs do Kafka passaram de cerca de 130 s para 59 s, somadas as duas execuções do Failsafe. A cobertura
  de linhas foi de 96,32% para 97,06%.
- **O que mudou, sem tirar nenhum caso:**
  - **perfil de teste `kafkatest`** (`src/test/resources/application-kafkatest.yml`, fora do JAR): prazos
    curtos dos clientes Kafka (`max.block.ms`, `request.timeout.ms`, `delivery.timeout.ms`,
    `default.api.timeout.ms` e os prazos do `KafkaAdmin`). Todos os ITs do Kafka o ligam junto com o perfil
    `kafka`. A produção não muda;
  - **`KafkaInvalidEventsIT` numa única execução:** os 16 eventos inválidos são publicados de uma vez,
    seguidos de um evento válido. Como o tópico tem uma partição, quando o válido aparece na listagem,
    todos os inválidos já foram processados sem travar o consumo. Cada caso é conferido pela chave no tópico
    de erros, com uma mensagem própria por caso:
    - os bytes originais;
    - o motivo e os cabeçalhos;
    - o aviso no log, agora comparado com o offset do próprio evento, o que é mais forte que antes.

    Passou de 16 s para 4 s por execução. O número de testes caiu de 270 para 255 (e de 96 para 81 na
    execução `json-logs`) só porque as 16 invocações parametrizadas viraram um teste com os mesmos 16 casos;
  - **reaproveitamento do contexto:** o `@DirtiesContext` saiu da base `KafkaScenarioIT`.
    - As classes que só leem e têm as mesmas propriedades compartilham o contexto e o broker. O
      `KafkaReferenceFileIT` reaproveita o do `KafkaMetricsIT` e passou de 5,7 s para 0,6 s.
    - As classes que mudam dados ou a readiness declaram `@DirtiesContext` e uma propriedade própria
      (`awards.test.context`): ganham um contexto só delas e nunca sujam o de outra.
    - Os testes com `SpringApplication` (`KafkaEquivalenceIT` e `KafkaStructuralFailureIT`) passaram a
      usar um broker embutido compartilhado na JVM, com tópicos e grupo próprios por classe;
  - **esperas fixas trocadas por condições determinísticas, com a mesma comprovação:**
    - `KafkaStructuralFailureIT`: o tópico vazio é conferido pelos offsets, sem esperar 3 s por caso. Foi
      de 8 s para 1 s;
    - `KafkaReadinessIT`: depois do marcador de outra execução, um filme sentinela, cuja gravação prova que o
      marcador já foi processado, no lugar de 3 s de espera. Foi de 8,6 s para 3,6 s;
    - `KafkaIdempotencyIT`: os 206 eventos são reenviados sem esperar cada confirmação, com um `flush` no
      fim. Foi de 9,4 s para 4,2 s.
- **O que sobrou de mais caro**, se um dia for preciso ir além:
  - `KafkaBrokerUnavailableIT`, cerca de 9 s: a subida sem broker espera o `KafkaAdmin`, e a observação
    dura 5 s;
  - `KafkaEquivalenceIT`, cerca de 8 s: seis subidas da aplicação.

  Reduzir esses dois exigiria encurtar a observação ou comparar menos arquivos, ou seja, tirar
  comprovação, e por isso não foi feito.
- **Verificações:** `./mvnw verify` verde, `./mvnw -Ppostgres-it verify` verde (7 testes, com o
  `RestartPostgresIT` e o `RetryPostgresIT` no perfil de teste) e `check-unchanged-tests.sh v0.2.0` verde.

## 2026-11-24 — Fechamento do bolt do Marco 3 (etapa 5)

- **ADRs (FR-037):**
  - de 0013 a 0019, no formato da spec 002, cada um citando a sua entrada neste registro;
  - o 0017 cita o mecanismo de comparação do token e registra, como decisão intencional, os cabeçalhos
    `X-Content-Type-Options` e `X-Frame-Options` em todas as respostas e o `Cache-Control` fora dos
    endpoints de negócio;
  - o 0004 ganhou os quatro casos de `SpringApplication` (emenda 1.5.0) e o perfil dedicado (emenda
    1.7.0);
  - o índice foi atualizado.
- **README (FR-036):**
  - o modo Docker: serviços, portas, carga por Kafka, publicação de um vencedor novo e leitura do tópico
    de erros;
  - as credenciais de demonstração do compose;
  - a autenticação, com a matriz de acesso e o token de demonstração, definido só no README, que é o
    único lugar do repositório com esse valor;
  - os testes do Marco 3 e o perfil `postgres-it`;
  - os jobs `postgres` e `compose`;
  - a tabela de requisitos do Marco 3;
  - a arquitetura do modo Docker e as decisões do Marco 3.

  As pendências deixadas pela emenda 1.5.0 na seção de testes e na tabela de rastreabilidade foram
  resolvidas. A seção "Avalie em 2 minutos" não mudou.
- **Clone limpo (T089):** num clone novo do commit da etapa 4, só com o JDK:
  - `./mvnw verify` verde em 275 s, com 270 + 96 testes;
  - `./mvnw spring-boot:run` respondeu o resultado de referência e a listagem do README, com o `ETag` e
    sem `Cache-Control`;
  - com `API_TOKEN` igual ao token de demonstração: 401 sem token e com token errado, 200 com ele, 401 nas
    métricas sem token, e a readiness e o Swagger UI públicos. O token não apareceu no log.

  As seções 3 e 4 do quickstart (PostgreSQL e compose) foram verificadas pelo `postgres-it` e pelo
  `compose-test.sh` locais, nas etapas 2 a 4. Os comandos rodaram na porta 18081, porque a 8080 da
  máquina estava ocupada por um processo do usuário.
- **Verificado localmente, com o Docker:**
  - `./mvnw -Ppostgres-it verify`: 7 testes, inclusive a nova tentativa quando a gravação falha;
  - `compose-test.sh`: todos os passos, inclusive a queda real do banco (7b), com os passos 1 a 4 em
    28 s.
- **Pendências** (exigem o push ou o PR, que são do responsável):
  - T020, T051 e T084: os jobs `postgres` e `compose` e os demais no pipeline;
  - T050: o testador adversário (Codex) na ingestão por Kafka;
  - T093: `postgres` e `compose` como checks obrigatórios da `main`, com o bypass desligado.

  Fica também a decisão sobre o tempo do `./mvnw verify`, cerca de 4 min (entrada da etapa 2).
- **Pontos A3, N3 e C4 da segunda análise:**
  - A3: o `DefaultModeIsolationIT` e o `NoGeneratedPasswordIT` já passavam antes da implementação e
    ficaram como guardas;
  - N3: o `OPTIONS` do Marco 2 responde 2xx com `Allow`, verificado e fixado no
    `AuthDisabledByDefaultIT`;
  - C4: a única propriedade usada por teste que restou é a `publish-completion`, documentada no ADR 0014.
    A `create-error-topic` não foi necessária.

## 2026-11-24 — Etapa 4 do Marco 3: compose, verificações e ajustes

- **Imagens (T074):** as versões estáveis mais recentes no Docker Hub, fixadas por tag e digest:
  - `apache/kafka-native:4.4.0` e `apache/kafka:4.4.0` (o `kafka-cli`, sob demanda);
  - `prom/prometheus:v3.16.0`;
  - `grafana/grafana:13.3.2`;
  - `postgres:18-alpine`, com o mesmo digest do job `postgres`.

  O cliente da aplicação (kafka-clients 4.2.2) conversa com o broker 4.4.
- **Healthchecks (R17, verificado):** a imagem nativa do Kafka tem `nc`, e o healthcheck é
  `nc -z localhost 9092`. O Prometheus tem `sh`, `wget` e `printf`, e o Grafana tem `wget`. Nenhuma saída
  alternativa foi necessária.
- **Volume do Kafka:** a imagem roda com o usuário `appuser`. Um volume num caminho que não existe na
  imagem seria criado com dono root, então o volume `kafkadata` fica em `/home/appuser`, que já pertence ao
  `appuser`, com `KAFKA_LOG_DIRS=/home/appuser/kraft-logs`. O `CLUSTER_ID` é fixo, para que o volume sirva
  a um container recriado.
- **Token do Prometheus (R18, verificado):** o `credentials_file` vazio é aceito com a autenticação
  desligada, e a coleta funciona. Com `API_TOKEN` definido, o alvo volta a `up` com o token.
- **Imagem da aplicação na política:** o serviço `app` é construído pelo próprio compose (`build: .`) e
  não tem digest. A regra do `check-workflows.sh` vale para as imagens baixadas.
- **Passo 7b (FR-015a, ajuste):** uma queda curta do PostgreSQL não chega a gerar falha, porque o pool de
  conexões (Hikari) espera até 30 s por uma conexão. O passo passou a manter o banco fora até o log da
  aplicação mostrar `Falha ao gravar o evento`, prova de que a gravação falhou de verdade e foi tentada de
  novo, e só então o religa. O filme é gravado, e o tópico de erros não recebe o evento.
- **Git Bash:** o `compose-test.sh` exporta `MSYS_NO_PATHCONV=1`, para que `/opt/kafka/bin/...` não vire
  um caminho do Windows. No runner Linux, isso não muda nada.
- **Execução local (T081):**
  - `compose-test.sh` verde, com todos os passos de 0 a 8 e o 7b;
  - os passos de 1 a 4 levaram 28 s, para um limite de 180 s (SC-008);
  - rodou com `APP_PORT=18080`, porque a porta 8080 da máquina estava ocupada por um processo Java do
    usuário, fora da sessão.

## 2026-11-24 — Etapa 3 do Marco 3: verificações e ajustes da autenticação

- **Nome da autoconfiguração (verificado):** no Boot 4.1.2, é
  `org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration`. Ela está excluída
  no `application.yml` e no `application-kafka.yml`, e o `NoGeneratedPasswordIT` confere que o log não
  traz senha gerada.
- **`OPTIONS` no Marco 2 (N3 da segunda análise, verificado):** antes do Spring Security, `OPTIONS` nos
  endpoints de negócio respondia 2xx com `Allow`. O `AuthDisabledByDefaultIT` fixa esse comportamento com
  o token desligado. Com o token ligado, a resposta é 401.
- **`Cache-Control` no 401 (R12, ajuste):**
  - o `DelegatingRequestMatcherHeaderWriter` previsto decide só pelo caminho e deixava o 401 dos
    endpoints de negócio sem os cabeçalhos de cache, ao contrário do contrato operacional;
  - foi trocado por um `HeaderWriter` próprio, que aplica o `CacheControlHeadersWriter` fora dos
    endpoints de negócio e em todo 401. O status é conhecido, porque os cabeçalhos são escritos quando a
    resposta é confirmada;
  - os endpoints de negócio continuam sem `Cache-Control` nas respostas 200, 304, 400 e 405.
- **`CacheHeadersIT` em duas classes:** a tarefa previa classes aninhadas, uma para cada estado do token.
  Ficaram duas classes, `CacheHeadersIT` e `CacheHeadersWithTokenIT`, porque cada estado do token precisa
  de um contexto próprio, e a base `ScenarioIT` já faz isso por classe.
- **Validador de contrato:** o `ContractValidator` valida só a resposta, e não o requisito de segurança da
  requisição. O `security: [{}, {bearerAuth: []}]` do contrato 1.2.0 não muda os ITs dos Marcos 1 e 2, e
  o validador não precisou de sobrecarga (R13).
- **Prova de que os Marcos 1 e 2 não mudaram (R14):**
  - o `AuthDisabledByDefaultIT` é a guarda contra `API_TOKEN` definido;
  - o `check-unchanged-tests.sh v0.2.0` passa, com os 59 ITs da tag sem alteração e o `ApiDocsIT` como
    única exceção (só a constante `SPEC_CONTRACT`);
  - o script foi testado com um IT alterado (reprova), com o `ApiDocsIT` alterado (passa) e com uma
    exceção de título inexistente (reprova).
- **Tempo do `./mvnw clean verify`:** 227 s, depois de encurtar os prazos do `KafkaBrokerUnavailableIT`.

## 2026-11-24 — Etapa 2 do Marco 3: verificações e decisões da ingestão por Kafka

- **Nome da autoconfiguração (R2, verificado):** no Boot 4.1.2, são
  `org.springframework.boot.kafka.autoconfigure.KafkaAutoConfiguration` e
  `...kafka.autoconfigure.metrics.KafkaMetricsAutoConfiguration`, excluídas no `application.yml` e
  liberadas no `application-kafka.yml`. Versões: Spring Kafka 4.1.2 e kafka-clients 4.2.2.
- **Nome inválido de tópico (T034c, verificado):**
  - um tópico de erros com um nome que o Kafka não aceita **não derruba a subida**. Com
    `spring.kafka.admin.fail-fast: false`, o `KafkaAdmin` registra o `InvalidTopicException` e a aplicação
    fica pronta;
  - o `KafkaErrorTopicUnavailableIT` usa esse nome, e cada envio é recusado de verdade pelo cliente;
  - a propriedade alternativa `awards.ingestion.create-error-topic` não foi necessária e foi descartada:
    menos uma configuração usada só por teste.
- **Broker fora do ar (T034b, verificado):** a aplicação sobe sem o broker, com a liveness 200 e a
  readiness 503. A subida, com os prazos do `KafkaAdmin`, e o encerramento, com envios pendentes, levam
  cerca de 15 s. O teste encurta os prazos do `KafkaAdmin` e do produtor só para ele.
- **Série de atraso do consumidor (R19, verificado):** a série que aparece logo depois da carga é
  `kafka_consumer_fetch_manager_records_lag_max`, por cliente (`client_id=consumer-awards-movies-<n>`). A
  série por partição só aparece depois da atualização periódica do Micrometer. Com um consumidor e uma
  partição, o máximo do cliente é o atraso do tópico. A R19 e o contrato operacional foram atualizados.
- **Captura de saída por teste:** o resumo da publicação é registrado num fio próprio, durante o
  primeiro teste da classe, e o `OutputCaptureExtension` descarta a captura de cada teste no fim dele.
  Por isso, a verificação do resumo ficou só no `PublisherSummaryIT`, que tem um único teste, e saiu do
  `KafkaInvalidLinesIT`, que confere o tópico e os avisos.
- **Tempo do `./mvnw verify` acima do limite da R15:** 256 s sem `clean`, contra 96 s na linha de base,
  ou seja, 2,7× (limite de 2×). A R15 previa compartilhar o broker entre as classes, mas a medição mostra
  que o broker não é o custo: uma classe inteira, como o `KafkaUpdateIT`, leva 2,9 s com broker e
  contexto. O tempo está em poucos testes que esperam de propósito:
  - `KafkaBrokerUnavailableIT`, cerca de 20 s;
  - `KafkaInvalidEventsIT`, 16 casos, cerca de 18 s em cada uma das duas execuções do Failsafe;
  - `KafkaReadinessIT`, 3 s de espera pelo marcador errado;
  - `KafkaEquivalenceIT`, seis subidas.

  Foram feitos dois ajustes: o produtor dos testes passou a ser reutilizado e a leitura passou a ser feita
  sem grupo de consumidores, o que tirou cerca de 20 s do `KafkaInvalidEventsIT`. O resto foi mantido.
  **Pendente de decisão do responsável:** aceitar os cerca de 4 min do `verify`, ou cortar casos.
- **Nova tentativa quando a gravação falha (FR-015a):** o `RetryPostgresIT` passou no `postgres-it`
  local, com a mensagem `Falha ao gravar o evento … nova tentativa` no log, e o filme foi gravado depois
  de a tabela ser devolvida. A queda real do banco fica para o passo 7b do compose (etapa 4).
- **Validação comum (I1 da segunda análise):** o `FIELD_TOO_LONG` está na `MovieFieldRules`, usada pelo
  leitor do CSV e pelo parser de eventos. O `FieldTooLongIT` (carga direta) e o `KafkaEquivalenceIT` (com
  o arquivo `field-too-long.csv`) passam.

## 2026-11-24 — Segunda análise da spec 003 (`/speckit-analyze`)

O responsável aceitou as correções, com os ajustes abaixo. Elas foram aplicadas na spec, no plano, na
research, nos contratos e nas tarefas. A spec 001 também mudou (I1).

- **C1, nova tentativa quando a gravação falha:**
  - nova FR-015a e o `RetryPostgresIT` no `postgres-it`, que renomeia a tabela `movie` e a devolve num
    `finally`. Ele é `@Isolated` e roda sem paralelismo;
  - a spec e a R9 dizem o que o teste comprova de fato: a nova tentativa quando a gravação falha;
  - a queda real do banco continua comprovada pelo passo 7b do compose.
- **C2, comparação do token:**
  - a FR-024 saiu da spec, e o número ficou reservado;
  - a comparação em tempo constante virou decisão de design na R11, com o mecanismo
    (`MessageDigest.isEqual` sobre SHA-256 em UTF-8);
  - o ADR 0017 cita o mecanismo, e a "Revisão de segurança" o confere.
- **C3, checks obrigatórios:** nova tarefa T093, um pedido ao responsável depois da primeira execução do
  pipeline no PR e antes do merge:
  - incluir `postgres` e `compose` como checks obrigatórios;
  - conferir os seis jobs existentes;
  - deixar desligado o bypass para administradores.
- **I1, `FIELD_TOO_LONG` sem exceção:** a pedido do responsável, a diferença não foi documentada. A
  regra de tamanho máximo passou para a `MovieFieldRules` e vale nos dois caminhos.
  - **Custo, avaliado como baixo:** uma regra, um motivo no `RejectionReason` e um IT novo
    (`FieldTooLongIT`).
  - **Efeito no Marco 1:** uma linha do CSV com campo maior que a coluna deixa de interromper a subida
    com um erro do banco. Ela passa a ser ignorada com aviso, e a spec 001 (FR-006) e o data-model 001
    foram atualizados.
  - Nenhum IT do Marco 1 depende desse caso, e o `KafkaEquivalenceIT` passa a incluir um arquivo com
    esse caso.
  - As diferenças de representação do evento (`winner` booleano, `year` inteiro JSON) foram para a
    FR-010 e a FR-012.
- **I2:** a linha de exceção do `ApiDocsIT` cita o título exato, com as crases.
- **G1:** novo `KafkaBrokerUnavailableIT` (liveness 200 e readiness 503 com o broker fora).
- **G2:** novo `KafkaErrorTopicUnavailableIT`. Antes, a tarefa verifica se um nome inválido de tópico
  derruba a subida; se derrubar, usa um tópico válido não criado.
- **G3:** o passo 7 do compose usa `down` sem `-v` e `up`, e não mais `restart app`.
- **A1:** o `OPTIONS` segue a matriz de acesso, e o CORS fica para o Marco 6.
- **A2:** o valor do token de demonstração é definido só no README (T088), e o quickstart o cita sem
  repeti-lo.
- **I3:** nova FR-022a, com os cabeçalhos de proteção e a ausência de `Cache-Control` nos endpoints de
  negócio, e a definição de "como no Marco 2" na FR-019.
- **I4:** a saúde geral entra na FR-022.
- **I5:** o `RestartPostgresIT` faz 4 subidas (3 reinícios), como o SC-004.
- **Não aplicados**, porque o responsável não os pediu: I6 (deriva entre a estrutura do plano e as
  tarefas, só em parte resolvida pelas mudanças acima), I7 ("aceitas" × "publicadas"), A3 (o T036
  aceita que o `DefaultModeIsolationIT` passe) e C4 (propriedade `publish-completion` usada por teste).

## 2026-11-24 — Exceção do `ApiDocsIT` à regra dos testes sem alteração

- **Origem:** plano 003. O `ApiDocsIT` (Marco 2, tag `v0.2.0`) compara o `/openapi.yaml` publicado, byte
  a byte, com `specs/002-operacao/contracts/openapi.yaml`. O contrato 1.2.0 (autenticação opcional e
  resposta 401, spec 003 FR-026) faz esse teste falhar. Pela R14 do plano, os ITs dos Marcos 1 e 2 não
  podem mudar.
- **Alternativas apresentadas ao responsável:**
  - **A:** trocar só a constante `SPEC_CONTRACT` para o contrato da spec 003, como o Marco 2 fez ao
    trocar a spec 001 pela 002;
  - **B:** sobrescrever o contrato da spec 002 com o 1.2.0;
  - **C:** não publicar o 1.2.0.
- **Decisão do responsável:** opção A, sem mudar nenhuma asserção. O `ApiDocsIT` é a única linha de
  `.github/unchanged-tests-exceptions.txt`, com o título desta entrada. O `check-unchanged-tests.sh`
  aceita a mudança desse arquivo e reprova a de qualquer outro IT da tag.
- **Rejeitadas:**
  - B: o artefato de uma spec já entregue passaria a descrever o marco seguinte;
  - C: a FR-026 deixaria de ser cumprida.

## 2026-11-24 — Cabeçalhos de proteção do Spring Security em todas as respostas

- **Origem:** plano 003, research R12. O Spring Security acrescenta `Cache-Control`, `Pragma`,
  `Expires`, `X-Content-Type-Options` e `X-Frame-Options` a toda resposta. Os três primeiros anulariam o
  ETag, e o Marco 2 não mandava `Cache-Control`.
- **Decisão do responsável:**
  - o `Cache-Control` do Security fica fora dos dois endpoints de negócio (pedido do plano);
  - `X-Content-Type-Options` e `X-Frame-Options` ficam em **todas** as respostas, com o token ligado ou
    desligado;
  - o ADR da autenticação os registra como decisão intencional (spec 003, FR-037, e item do checklist).
- **Leitura da FR-019** ("comportamento idêntico ao do Marco 2"): status, corpo, ETag, 304 e os
  cabeçalhos que o Marco 2 já mandava continuam iguais. Os dois cabeçalhos de proteção são acréscimos
  que não mudam nada do que os testes dos Marcos 1 e 2 verificam.
- **Rejeitada:** desligar também esses dois escritores, para uma igualdade literal de cabeçalhos. Isso
  removeria proteção sem nenhum ganho para o ETag nem para o contrato.

## 2026-11-24 — Emenda 1.7.0 da constituição: comprovação de comportamento que depende de infraestrutura

- **Origem:** Constitution Check do plano 003:
  - o Princípio VI exigia o teste de integração de cada comportamento "no `./mvnw verify`";
  - o Princípio II põe os testes de PostgreSQL num perfil Maven separado, fora do `verify` padrão;
  - o plano tinha lido os dois juntos e pedido a confirmação do responsável.
- **Decisão do responsável:** emenda MINOR do Princípio VI. O comportamento da aplicação que depende de
  infraestrutura indisponível no modo padrão, como o PostgreSQL, é comprovado por testes de integração
  num perfil Maven dedicado, executado obrigatoriamente no pipeline. Todo o resto continua exigindo teste
  no `./mvnw verify`.
- **Detalhes da redação:**
  - os testes do perfil seguem o Princípio I (contexto completo, sem mocks, HTTP);
  - o comportamento comum aos dois modos continua testado no `verify`, com H2;
  - uma dependência com substituto embutido sem Docker, como o broker Kafka do Spring, não entra na
    exceção.
- **Impacto:**
  - 1.6.0 → 1.7.0 (MINOR): é regra nova, e nenhum requisito que cumpria a versão anterior deixa de
    cumprir;
  - foram alinhados a constituição (Princípio VI e Sync Impact Report), o `AGENTS.md` (rastreabilidade e
    seção Testes), a spec 003 (FR-038) e o plano 003 (Constitution Check). O checklist da spec 003 também
    foi atualizado.

## 2026-11-24 — Emenda 1.6.0 da constituição: credenciais de demonstração no compose local

- **Origem:** clarificação da spec 003 (Marco 3). O compose precisa de usuário e senha para o
  PostgreSQL e o Grafana, e o Princípio VII só permitia o token de demonstração no repositório.
- **Alternativas apresentadas ao responsável:**
  - sem credencial: PostgreSQL só na rede interna do compose, sem senha, e Grafana com acesso anônimo;
  - senhas de demonstração, documentadas no README;
  - `.env` não versionado, com um passo obrigatório antes do `docker compose up`.
- **Decisão do responsável:** senhas de demonstração, com emenda do Princípio VII antes do plano. Elas
  são permitidas só no compose de desenvolvimento local e precisam cumprir quatro condições:
  - aparecem só nos arquivos do compose local e na configuração que ele monta;
  - são identificadas como de demonstração no README e no próprio arquivo;
  - podem ser sobrescritas por variável de ambiente;
  - nunca são usadas em ambiente publicado.

  O token de demonstração continua como estava.
- **Motivo:** é a forma mais comum de um compose local subir com um único comando, num clone limpo. Uma
  senha de demonstração restrita à máquina de quem executa não protege nada de valor.
- **Ponto interpretado na redação:** o estágio do pipeline que sobe o mesmo compose num runner
  temporário (spec 003, FR-035) foi declarado como não sendo ambiente publicado. Sem isso, o teste do
  compose no pipeline violaria a condição "só no compose local".
- **Impacto:** MINOR (1.5.0 → 1.6.0), porque amplia o permitido e não proíbe nada que valia antes.
  Foram alinhados a constituição (Princípio VII, motivo e Sync Impact Report), o `AGENTS.md` (seção
  Segurança), a spec 003 (Clarifications e FR-033) e o checklist dela. O README só ganha a
  identificação das credenciais quando o compose existir (spec 003, FR-036).

## 2026-11-24 — T050 do Marco 2 (SC-008) não executada

- **Situação:** a T050, que prova que o pipeline reprova (SC-008), não foi executada antes do merge do
  PR do Marco 2 nem da tag `v0.2.0`. O branch temporário de verificação não chegou a ser criado, e
  nenhum dos sete defeitos foi enviado ao pipeline.
- **Motivo (decisão do responsável):** prazo de entrega. A verificação exige um push e uma execução
  do pipeline por defeito, e isso não cabia no prazo do marco.
- **O que está comprovado:** os demais requisitos de pipeline (FR-028, FR-028b e FR-029) foram
  comprovados pelas execuções verdes do workflow `CI` no branch `marco/2-operacao` e no PR. Depois do
  merge, o pipeline também validou o primeiro pull request do Dependabot (grupo
  `maven-minor-e-patch`, "Bump the maven-minor-e-patch group with 4 updates"), o que confirma que o
  Dependabot está ativo e que os PRs dele passam pelo mesmo portão.
- **O que fica sem comprovação:** que cada job reprova diante do defeito correspondente. As execuções
  verdes mostram que o pipeline aprova código correto, não que ele recusa código com defeito. O único
  portão já visto falhando de propósito é o da cobertura mínima, localmente, com
  `-Djacoco.line.minimum=0.99` (T041).
- **Impacto:** a T050 fica marcada como não executada em `specs/002-operacao/tasks.md`, com referência
  a esta entrada. O SC-008 continua aberto e pode ser comprovado depois, seguindo o roteiro da T050 sem
  mudança, num branch temporário a partir da `main`. Quando isso acontecer, o resultado entra numa
  entrada "Verificação do SC-008", como a T050 prevê.

## 2026-11-24 — Emenda 1.5.0 da constituição: `SpringApplication` para controlar o ciclo de vida

- **Origem:** revisão do Codex no PR do Marco 2. O `EtagAcrossRunsIT` e o `ReadinessAfterLoadIT` usam
  `SpringApplication` em execuções bem-sucedidas, e a constituição 1.4.1 só permitia isso para falha de
  inicialização. **Aceito:** o apontamento estava correto.
- **Decisão do responsável:** emendar o Princípio I, em vez de reescrever os testes. Os dois precisam
  controlar o ciclo de vida: o primeiro compara duas execuções, o segundo observa a ordem da subida, e
  nenhum dos dois cabe num `@SpringBootTest`. A cláusula passa a valer sempre que o teste precisa
  controlar o ciclo de vida da aplicação real: falha de inicialização, múltiplas execuções, ordem da
  subida e saída completa da subida. Continua sem mocks. MINOR (1.4.1 → 1.5.0): amplia o permitido e não
  proíbe nada que valia antes.
- **Dois pontos levados ao responsável antes da redação:**
  - *HTTP real:* o pedido dizia "continua com chamadas HTTP reais", mas o `ReadinessAfterLoadIT` não faz
    chamada HTTP, e a ordem da subida não é observável por HTTP (a probe só responde depois que o
    servidor sobe). **Escolhido:** o resultado é verificado por HTTP real sempre que o comportamento é
    observável por HTTP; a ordem da subida e o próprio log são verificados pelos eventos reais e pela
    saída capturada. O teste não muda.
  - *Testes não citados na revisão:* o `JsonLogFormatIT` e o `TextLogFormatIT` também usam
    `SpringApplication` em subidas bem-sucedidas, para ler o log desde a primeira linha. **Escolhido:**
    cobri-los com o caso "saída completa da subida" e citar a cláusula no javadoc deles.
- **Mudanças:** constituição (cláusula e Sync Impact Report), `AGENTS.md` (regras invioláveis) e o
  javadoc do `EtagAcrossRunsIT`, do `ReadinessAfterLoadIT`, do `JsonLogFormatIT`, do `TextLogFormatIT` e
  do `ApplicationStarter`. Nenhuma linha de código de teste ou de produção mudou.
- **Pendente (papel de documentação):** o README (seção de testes e tabela de rastreabilidade) e o ADR
  0004 ainda dizem que só os testes de falha de inicialização usam `SpringApplication`.

## 2026-11-24 — T037: healthcheck com readiness em 503

- **Pergunta (research R8):** o `wget` do BusyBox da imagem termina com código diferente de zero quando
  a readiness responde 503? Se não terminasse, o container ficaria `healthy` antes da carga do CSV.
- **Experimento, sem alterar a aplicação:** um container da própria imagem rodou um servidor temporário
  com o `nc` do BusyBox, respondendo um status fixo na 8080. Um segundo container da imagem entrou na
  rede do primeiro (`--network container:`) e rodou o comando do `HEALTHCHECK` exatamente como
  `docker inspect` o lê da imagem.
- **Resultado:** com 503, o código de saída foi 1 (`wget: server returned error: HTTP/1.1 503`). Com
  200, foi 0. Nos dois casos, o servidor registrou `GET /actuator/health/readiness`, então o `PORT` da
  imagem foi expandido. O healthcheck não muda, e a T037 foi concluída (hadolint e variável de log vazia
  já verificados antes).
- O script do experimento ficou fora do repositório: é uma verificação única de uma premissa da imagem
  base. Se o Dependabot trocar a base, os passos 1 e 7 do teste de fumaça continuam provando o
  `healthy`, mas não o caso do 503.

## 2026-11-24 — hadolint DL3025 no `CMD` do healthcheck

- **Falha:** o job `dockerfile-lint` reprovou o `Dockerfile:44` com a regra DL3025 ("Use arguments JSON
  notation for CMD and ENTRYPOINT arguments"). O alvo era o `CMD` do `HEALTHCHECK`, em forma shell; o
  `ENTRYPOINT` já estava em forma exec.
- **Decisão:** corrigir, e não ignorar a regra. Ela se aplica ao nosso caso: a notação JSON com o shell
  explícito (`["sh", "-c", "wget ... || exit 1"]`) mantém a expansão do `PORT` a cada verificação, que
  era o único motivo da forma shell. Nenhum `# hadolint ignore` foi necessário, e o pipeline continua
  sem regra desligada.
- **Verificação local:** com a mesma imagem do hadolint que a action fixada usa
  (`ghcr.io/hadolint/hadolint:v2.15.1-debian`, pelo digest) e `--failure-threshold warning`, o
  `Dockerfile` anterior reprova com a DL3025, e o novo passa. Depois do rebuild, o teste de fumaça deu
  12 OK, 0 FALHA e o passo 13 PULADO. O passo 7 (`PORT=9090`) ficou `healthy`, o que prova a expansão.
- A spec mudou antes do código: research R8 e ADR 0011.

## 2026-11-24 — Teste de fumaça local e correções do script

Com o Docker ligado, a imagem foi construída e o `smoke-test.sh` rodou no Git Bash do Windows (Docker
26.0.0). A primeira execução expôs defeitos do próprio script, e não da imagem:

- **Porta fixa no host:** havia um processo Java local na 8080 do host. O passo 1 falhou, e os passos 2
  e 3 "passaram" chamando esse processo, e não o container: falso positivo. **Correção:** cada container
  publica a porta numa porta aleatória do host, só em `127.0.0.1`, lida com `docker port`.
- **Passos dependentes sem o container:** os passos 4 e 6 também passaram com o container parado. O 4
  aceitou um `id -u` vazio, e o 6 "parou" um container que não rodava. **Correção:** os passos 2 a 6
  exigem o container do passo 1 rodando e falham com esse motivo. O passo 4 exige um uid numérico
  diferente de 0. Com uma imagem inexistente, os passos 1 a 6 agora falham.
- **Contexto de build no Git Bash:** o `check-docker-context.sh` exporta `MSYS_NO_PATHCONV=1` e passava
  o caminho POSIX ao `docker.exe`, que não o encontra. **Correção:** no Git Bash, o contexto é convertido
  com `cygpath -w`, como já era feito nos bind mounts.

A spec mudou antes do código (research R14). Depois das correções, o teste de fumaça deu 12 OK, 0 FALHA
e 1 PULADO, com o mesmo processo ainda ocupando a 8080. O passo 13 foi pulado porque o `chmod 000` não
vale no bind mount do Docker Desktop; no pipeline, ele precisa rodar (T047). O log do container saiu em
JSON (33 linhas, todas válidas no `jq`), no formato ECS aninhado, com
`LOGGING_STRUCTURED_FORMAT_CONSOLE=ecs` vindo da imagem. Com a variável vazia, saiu em texto (passo 12).
O `jq` 1.8.1 veio da release oficial, com o checksum conferido, para o diretório temporário da sessão,
fora do repositório.

## 2026-11-24 — Fechamento do bolt do Marco 2 (etapa 6)

- **README (FR-034):** atualizado com o formato do log, a imagem Docker, o ETag e o 304, os endpoints
  operacionais, as duas execuções do Failsafe e a cobertura, o pipeline e os critérios de reprovação, a
  tabela dos requisitos do Marco 2, as decisões e o índice dos ADRs. As seções do Marco 1 não foram
  reescritas.
- **Clone limpo (T060):** num clone novo do branch, a seção "Avalie em 2 minutos" funcionou só com o
  JDK: `verify` verde, a aplicação subiu em cerca de 5 s, os dois `curl` responderam o esperado, e o log
  trouxe o resumo, sem banner. O `verify` levou 273 s no clone novo e 75 s na cópia de trabalho já
  compilada, e o README registra os dois tempos.
- **Rastreabilidade (T061):** cada requisito de comportamento da spec 002 (FR-001 a FR-020) aparece no
  `@DisplayName` de pelo menos um IT do Marco 2. *Observação para o responsável:* os IDs FR-001 a FR-020
  existem nas duas specs, com significados diferentes. Hoje, quem desfaz a ambiguidade é o pacote do
  teste (`etag/`, `health/`, `metrics/` e `logging/` são da spec 002). Um prefixo por spec nos
  `@DisplayName` resolveria isso, mas mudaria os nomes dos testes do Marco 1, e por isso não foi feito sem
  decisão.
- **Pendências do bolt:** dependem do push do branch, que é do responsável:
  - T037 e T038: hadolint, `wget` com 503, variável de log vazia, build e teste de fumaça, que vão para
    os jobs `dockerfile-lint` e `image`;
  - T047 a T050: pipeline verde, conferências da execução, Dependabot ativo e a verificação do SC-008 no
    branch temporário.

  O daemon do Docker não estava rodando na máquina de desenvolvimento, então nada da imagem foi
  verificado localmente.

## 2026-11-24 — Decisões de arquitetura sem entrada própria (ADRs)

Registro feito junto com os ADRs da etapa 5 do Marco 2 (FR-031). As decisões abaixo foram tomadas nos
planos 001 e 002 e nas etapas do Marco 2, mas não tinham entrada aqui. Cada uma é resumida, e o detalhe
fica no ADR e na research.

### Stack: Java 25, Spring Boot 4.1.2 e Maven com wrapper (ADR 0001)

- **Decisão (plano 001, research R1):** Java 25 (LTS), Spring Boot 4.1.2 e Maven 3.9.16 pelo Maven
  Wrapper 3.3.4, na distribuição `only-script`.
- **Motivo:** o Boot 4.1.2 era a última versão estável (a 4.2.0 estava em RC). O wrapper permite subir e
  testar só com o JDK (Princípio II), sem um JAR binário no repositório.
- **Alternativas rejeitadas:** Boot 4.2.0-RC2 e Maven 4 RC, que não eram estáveis; Boot 3.5.x, que não
  era o "estável atual"; wrapper com JAR.

### H2 com JPA e esquema por Flyway (ADR 0002)

- **Decisão (plano 001, research R5):** H2 em memória no modo padrão, JPA com `ddl-auto=validate` e
  esquema versionado pelo Flyway (`V1__create_schema.sql`), com três tabelas (`movie`, `producer` e
  `movie_producer`) e consultas em JPQL, sem SQL nativo.
- **Motivo:** o avaliador roda sem nada instalado, e o mesmo esquema e as mesmas consultas servem ao
  PostgreSQL do Marco 3. A unicidade de ano + título também fica no banco.
- **Alternativas rejeitadas:** tabela única com os produtores em texto; `ddl-auto=create`, que a
  constituição proíbe.

### Erros em Problem Details (ADR 0006)

- **Decisão (plano 001, research R7):** os erros da API saem em Problem Details (RFC 9457), com
  `spring.mvc.problemdetails.enabled` e um `@RestControllerAdvice` que converte o parâmetro inválido em
  400 com a propriedade `invalidParams` (`[{name, reason}]`). O 405 também sai em Problem Details, com o
  cabeçalho `Allow`.
- **Motivo:** é o formato padrão para erros HTTP, que o próprio Spring produz, e a constituição o exige.
- **Alternativas rejeitadas:** um corpo de erro próprio; o corpo de erro padrão do Boot, que não segue a
  RFC.

### ETag derivado do conteúdo (ADR 0007)

- **Decisão (plano 002, research R4; etapa 1):** ETag forte, `"0"` mais o MD5 do corpo, nas respostas
  2xx de GET dos dois endpoints de negócio, pelo filtro `ContentEtagFilter`, e 304 na revalidação,
  inclusive com `If-None-Match: *`.
- **Motivo:** os dados não mudam durante a execução, e os corpos são pequenos. Um ETag do corpo não exige
  estado novo nem mudança nos controllers. O filtro próprio existe porque o `ShallowEtagHeaderFilter` do
  Spring não trata o `*` (entrada da etapa 1, abaixo).
- **Alternativas rejeitadas:** ETag por "versão" dos dados; ETag fraco; `Cache-Control` com `max-age`.

### Liveness e readiness pelo Actuator (ADR 0008)

- **Decisão (plano 002, research R1 e R2; etapa 2):** probes do Actuator em `/actuator/health/liveness` e
  `/actuator/health/readiness`, na mesma porta da API, sem detalhes. A readiness fica positiva no
  `ApplicationReadyEvent`, que o ciclo de vida do Boot garante depois da carga do CSV.
- **Motivo:** é o caminho padrão do framework, sem código próprio. A ordem "carga → pronta" já é garantida
  pelo ciclo de vida.
- **Alternativas rejeitadas:** controllers próprios de saúde; um `HealthIndicator` com uma flag de "carga
  concluída"; uma porta separada para o Actuator.

### Métricas no formato do Prometheus (ADR 0009)

- **Decisão (plano 002, research R3; etapa 2):** Micrometer com o registro do Prometheus, em
  `/actuator/prometheus`. A métrica `http.server.requests` tem histograma de 1 ms a 10 s, e o rótulo
  `application` vai em todas as séries.
- **Motivo:** o histograma permite calcular percentis e a fração abaixo de um limite no Prometheus, que é
  a base do SLO do Marco 4.
- **Alternativas rejeitadas:** percentis calculados na aplicação, que não se agregam entre instâncias.

### Imagem de container (ADR 0011)

- **Decisão (plano 002, research R8; etapa 3):** Dockerfile multi-stage, com
  `eclipse-temurin:25-jdk-alpine` no build e `eclipse-temurin:25-jre-alpine` na execução, fixados por
  digest. O JAR é extraído em camadas, o usuário é 10001, o healthcheck usa a readiness, e o `ENTRYPOINT`
  é em forma exec, com a memória no próprio `ENTRYPOINT` e não em `JAVA_TOOL_OPTIONS`.
- **Motivo:** é a menor imagem JRE oficial que ainda tem shell para o healthcheck com `PORT`. O digest
  torna o build reprodutível. A JVM como PID 1 recebe o `SIGTERM`.
- **Alternativas rejeitadas:** distroless, que não tem shell; runtime por `jlink`; Buildpacks; rodar os
  testes dentro do build da imagem.

## 2026-11-24 — Etapa 2 do Marco 2: `groups` na saúde geral, banner e formato ECS

- **`groups` na saúde geral (FR-011):**
  - *Origem:* implementador, no T030. Com os probes ligados, o `/actuator/health` responde
    `{"status":"UP","groups":["liveness","readiness"]}`. O Boot 4.1.2 sempre inclui a lista de grupos, e
    nenhuma propriedade a esconde. A FR-011 dizia "só o estado";
  - *Decisão (responsável):* aceitar a lista. A FR-011 e o US2-6 foram ajustados antes do código: a saúde
    geral pode trazer, além do `status`, só `groups` com exatamente `liveness` e `readiness`. O
    `HealthProbesIT` exige exatamente esses dois campos;
  - *Motivo:* são os nomes dos dois probes públicos, já publicados no contrato operacional, sem
    componentes, banco, caminhos nem versões;
  - *Alternativas rejeitadas:* esconder `groups` com código próprio; não expor a saúde geral. As duas
    exigiriam código para esconder nomes que já são públicos.
- **Banner com o JSON ligado (correção de uma premissa):**
  - *Observado:* com o logging estruturado ligado, o Boot 4.1.2 já não escreve o banner (0 ocorrências na
    execução `json-logs`, contra 68 na execução em texto). A segunda análise da spec supunha que o banner
    quebraria o JSON, e isso não vale para esta versão;
  - *Efeito na decisão:* a decisão de desligar o banner em todas as execuções continua, pelo motivo
    principal dado pelo responsável (o banner não traz informação). O efeito real do
    `spring.main.banner-mode: off` é tirá-lo do log em texto do `./mvnw`, provado pelo `TextLogFormatIT`.
    A FR-020 e a research R6 foram corrigidas.
- **Formato ECS aninhado:** o Boot 4.1.2 escreve os campos ECS aninhados
  (`{"log":{"level":…},"process":{"thread":{"name":…}}}`), e não com a chave pontuada. O `LogOutput` dos
  testes lê os dois jeitos. Research R6 e data-model atualizados.
- **Content type do Actuator:** `application/vnd.spring-boot.actuator.v3+json`. Para o Spring, ele não é
  compatível com `application/json`, então o teste confere o sufixo `+json`. Contrato operacional
  corrigido.
- **`ReadinessInvalidLinesIT` com `@DirtiesContext`:** ele usa o mesmo CSV do `InvalidLinesIT`, que lê o
  log da inicialização. O contexto é fechado no fim da classe, pelo mesmo motivo do `EmptyLoadIT` na
  etapa 1.
- **Itens "verificar" resolvidos:** R1, porque o starter do Actuator já traz as métricas; R3, porque
  `management.metrics.tags.application` funciona e o `MetricsConfiguration` não foi necessário.
- **Resultado da etapa:** `./mvnw verify` verde. Execução padrão com 189 ITs (5 pulados, que são o
  `JsonLogFormatIT`, só da `json-logs`); execução `json-logs` com 55 ITs de 12 classes; 75 s no total.
  Conferência manual com o JAR nos dois formatos.

## 2026-11-23 — Etapa 1 do Marco 2: filtro de ETag próprio e contexto do `EmptyLoadIT`

- **Filtro de ETag próprio (research R4):**
  - *Origem:* implementador, no T012. Com o `ShallowEtagHeaderFilter` do Spring, o teste
    `IntervalsEtagIT.wildcardIsNotModified` falhou: `If-None-Match: *` devolvia 200 com corpo. A FR-003
    e a RFC 9110 (seção 13.1.2) exigem 304, porque `*` casa com qualquer representação atual. Os demais
    casos (valor único, lista, forma fraca, malformado, 400 e 405 sem ETag) passaram com o filtro do
    Spring;
  - *Decisão (implementador, para revisão do responsável no PR):* um filtro próprio,
    `ContentEtagFilter`, que faz o mesmo que o do Spring (corpo guardado, ETag forte `"0" + MD5`, só em
    2xx de GET e HEAD) e compara com as classes do Spring (`ETag.parse` e `ETag.compare`, comparação
    fraca), tratando o `*` explicitamente. O formato do ETag é o mesmo;
  - *Alternativas rejeitadas:* estender o `ShallowEtagHeaderFilter`, cujo método que decide o 304 é
    privado; tirar o `*` da spec, que contrariaria a RFC 9110.
- **`EmptyLoadIT` com contexto próprio:**
  - *Origem:* o `EmptyLoadIT` (Marco 1) falhou ao rodar com o `EtagEmptyIT`, que usa o mesmo
    `header-only.csv`. O pacote `etag` roda antes de `ingestion`, então o contexto já existia no cache,
    e o resumo da carga não aparecia mais na saída capturada pelo `EmptyLoadIT`;
  - *Decisão (implementador):* as duas classes internas do `EmptyLoadIT` ganharam
    `@DirtiesContext(classMode = BEFORE_CLASS)`, o mesmo padrão que o `DefaultFileIT` já usa. Nenhum
    resultado esperado mudou;
  - *Alternativa rejeitada:* dar ao `EtagEmptyIT` um CSV próprio. Esconderia a fragilidade, que é de
    quem lê o log da inicialização.
- **Resultado da etapa:** `./mvnw verify` verde, com 160 ITs (136 do Marco 1 e 24 novos).

## 2026-11-23 — Correções da segunda análise da spec 002

- **Origem:** segunda rodada do `/speckit-analyze` da spec 002, sem achados críticos. O responsável
  aprovou as correções de H1, H2, M1, M2, M3 e M4. Os achados de nível LOW continuam fora.
- **H1, memória da JVM no `ENTRYPOINT`, e não em `JAVA_TOOL_OPTIONS`:**
  - *Problema:* com `JAVA_TOOL_OPTIONS` definida, a JVM sempre escreve `Picked up JAVA_TOOL_OPTIONS:
    ...` no stderr, fora do log. Essa linha não é JSON e quebraria o SC-005 e o passo 5 do teste de
    fumaça no container;
  - *Decisão:* `-XX:MaxRAMPercentage=75` vai direto no `ENTRYPOINT` em forma exec, e a imagem não define
    `JAVA_TOOL_OPTIONS`;
  - *Impacto:* research R8, `contracts/container.md`, data-model e T036.
- **H2, banner desligado em todas as execuções (opção b):**
  - *Decisão (responsável):* `spring.main.banner-mode: off` no `application.yml`, em todas as execuções
    e nos dois formatos;
  - *Motivo:* o banner não traz informação. Ele é escrito direto no stdout, fora do logging, e em JSON
    viraria um evento com desenho ASCII (com `banner-mode: log`) ou linhas soltas que não são JSON (com o
    padrão `console`);
  - *Alternativas rejeitadas:* (a) `banner-mode: log`, que poria o desenho ASCII dentro da `message` de
    um evento JSON; (c) desligar só na imagem, que deixaria o JSON quebrado em qualquer outra execução
    com a variável, contra a FR-020;
  - *Efeito no Marco 1:* o log do `./mvnw spring-boot:run` perde o banner, e o resto do texto não muda.
    O SC-005 passou a dizer "mesmo texto do Marco 1, sem o banner";
  - *Efeito nos testes:* os testes de US4 da etapa 2 passam a falhar antes da implementação (pelo
    banner), e a mudança no `application.yml` os faz passar;
  - *Impacto:* spec (clarificação, FR-020 e SC-005), research R6, data-model, `contracts/container.md`,
    quickstart, plano e tarefas T024, T025, T026, T028 e T054.
- **M1, referências aninhadas da `trivy-action`:**
  - *Contexto:* o ataque de 2026 à cadeia do Trivy atingiu o `aquasecurity/setup-trivy`, que a
    `trivy-action` usa por dentro. O hash do nosso `uses:` não fixa o que uma action composta chama;
  - *Verificação (implementador, 2026-11-23, API pública do GitHub, porque o `gh` não está instalado):*
    - `trivy-action` v0.36.0, de 2026-04-22: `setup-trivy@3fb12ec…` (v0.2.6) e `actions/cache@27d5ce7…`
      (v5.0.5), os dois por hash;
    - `setup-trivy` nesse hash: `actions/cache/restore` e `actions/cache/save` (`9255dc7…`, v5.0.1) e
      `actions/checkout` (`8e8c483…`, v6.0.1), por hash. O `install.sh` vem de um commit fixo
      (`75c4dc0…`);
    - o `install.sh` confere o SHA-256 do binário contra o arquivo de checksums da release;
  - *Decisão (responsável):* como toda a cadeia aninhada está por hash, a `trivy-action` fica, fixada na
    v0.36.0 ou numa versão posterior que mantenha isso. Se uma versão tiver referência aninhada por tag
    ou branch, a action é trocada pela instalação do binário oficial do Trivy, com o checksum conferido;
  - *Revisão contínua:* todo PR do Dependabot que atualize uma action composta repete a verificação;
  - *Impacto:* research R10, R12 e R13, plano (riscos) e T042, que também passa a aceitar `curl` na API
    pública no lugar do `gh`.
- **M2, `check-workflows.sh` sem o `yq`:**
  - *Decisão (responsável):* sem o `yq` no `PATH`, o script termina com código 2 e uma mensagem clara,
    sem pular nem passar sem verificar;
  - a verificação oficial é a do job `workflow-policy` do pipeline;
  - *Impacto:* T040 e T045.
- **M3, T018(b):** a prova de que os dados já estão disponíveis passa a ser `totalElements` igual a 206,
  porque a listagem devolve no máximo 100 itens por página.
- **M4, base da sonda do `check-docker-context.sh`:**
  - *Decisão (responsável):* a sonda usa a imagem do estágio de build, lida do próprio `Dockerfile`, e
    não um `busybox` com digest próprio;
  - *Motivo:* o Dependabot só atualiza os digests do `Dockerfile`, e um digest só no script ficaria
    desatualizado sem aviso;
  - *Impacto:* T033a e research R13 e R14.

## 2026-11-23 — Correções do `/speckit-analyze` da spec 002

- **Origem:** análise cruzada de spec, plano e tarefas da spec 002, antes da implementação. O
  responsável aprovou as correções de C1, G1, G2, A1, A2, A3, I1 e G3. A U1 tem entrada própria, abaixo.
  Os achados de nível LOW ficaram de fora.
- **C1, comprovação do workflow pelo pipeline (crítico, sem emenda):**
  - *Problema:* parte da FR-028b (permissões, nenhum segredo, nenhuma publicação) e a FR-029
    (Dependabot) eram comprovadas pela revisão do PR. Para requisitos de pipeline, o Princípio VI pede a
    execução do pipeline;
  - *Decisão (responsável):* estender o script da T040, que passa a ser o `check-workflows.sh`. Ele
    verifica hash de commit, `permissions`, `secrets.`, `push: true` e `docker push`, e a configuração
    do Dependabot. O job `pinning` vira `workflow-policy`;
  - *Alternativa rejeitada:* emendar o Princípio VI para aceitar a revisão.
- **G1, conteúdo da imagem e `.dockerignore`:** dois passos novos no teste de fumaça. O passo 10 confere
  que não há código-fonte, Maven nem compilador na imagem final. O passo 11 roda o
  `check-docker-context.sh`, que constrói uma imagem descartável com o mesmo contexto e confere a lista de
  permissão (FR-021 e FR-026).
- **G2, CodeQL reprova o pipeline:**
  - *Decisão (responsável):* o defeito que prova a regra (uma classe com SQL concatenado, regra
    `java/sql-injection`) **não pode ficar no código do marco**;
  - a prova acontece no branch temporário da T050, que nunca é mergeado e é apagado depois, local e
    remotamente, sem cherry-pick, merge nem rebase a partir dele;
  - entra no marco só o registro do resultado: branch, links das execuções, job vermelho por defeito e a
    confirmação de que o branch foi apagado;
  - a SC-008 ganhou as categorias "achado alto na análise de código" e "workflow fora da política".
- **G3, arquivo montado sem permissão de leitura:**
  - *Decisão (responsável):* o teste precisa funcionar no Windows e no Linux, ou ser pulado com motivo
    explícito onde a permissão não se aplica;
  - *Como:*
    - o `smoke-test.sh` converte os caminhos dos bind mounts no Git Bash (`cygpath -w`,
      `MSYS_NO_PATHCONV=1`);
    - o passo 13 começa com uma sonda: um container com o usuário 10001 tenta ler o arquivo com
      `chmod 000`. Se não lê, o passo roda. Se lê, a permissão do host não vale no container (NTFS no
      Git Bash, Docker Desktop), e o passo é pulado com `PULADO`, o motivo, o sistema e a versão do
      Docker;
    - no pipeline (`CI=true`), no runner Linux, uma sonda que lê é falha, e o caso de borda nunca fica
      sem prova;
  - *Motivo da sonda:* o critério é o comportamento observado, e não o nome do sistema. Um Linux em que
    a permissão não valha também é detectado.
- **A1, sobrescrever o formato do log no container:** a T037 verifica se um
  `LOGGING_STRUCTURED_FORMAT_CONSOLE` vazio volta o log para texto, e o passo 12 do teste de fumaça
  comprova. Se o valor vazio falhar, a alternativa vai ao responsável antes de qualquer mudança na spec.
- **A2, A3 e I1 (redação e alinhamento):**
  - FR-015 e US3-5: o valor fixo da rota de caminhos inexistentes pode ser compartilhado com os recursos
    estáticos, e o status 404 distingue os dois casos;
  - US1-7: arquivos "de resultados diferentes" dão ETags diferentes;
  - estrutura e matriz do plano alinhadas às tarefas (`EtagEmptyIT`, `ReadinessInvalidLinesIT`,
    `PrometheusText` e os scripts novos).
- **Impacto:** spec 002 (FR-015, FR-032, US1-7, US3-5, SC-008 e casos de borda), plano (estrutura, matriz
  e riscos), research R3, R9, R12 e R14, `contracts/ci-pipeline.md`, quickstart e `tasks.md` (T023,
  T033, T033a novo, T034, T037, T039, T040, T043, T045, T047, T049 e T050).

## 2026-11-23 — Resposta 500 fora da FR-006 (achado U1 do `/speckit-analyze` da spec 002)

- **Origem:** achado U1 da análise da spec 002. A FR-006 exigia que as respostas 400, 405 **e 500**
  ficassem sem `ETag`, e um caso de borda exigia que um 500 aparecesse nas métricas e no log. Nenhuma
  tarefa comprovava o 500, porque a API não tem caminho que produza esse status sem um defeito ou um
  mock.
- **Decisão (responsável):** opção A da análise. A FR-006 passa a cobrir só 400 e 405, e o caso de
  borda do 500 saiu da spec. A research R4 registra que:
  - o filtro de ETag só age em respostas 2xx;
  - as métricas registram qualquer status, inclusive 500, pelo comportamento do próprio Spring;
  - não há requisito próprio para o 500.
- **Motivo:** um requisito sem comprovação não é entregue (Princípio VI). Provocar um 500 só para
  testá-lo exigiria um defeito ou um mock, contra o Princípio I. O comportamento continua garantido pelo
  framework.
- **Alternativas rejeitadas:**
  - B, manter o 500 como "garantido por construção", sem teste: violaria o Princípio VI;
  - C, provocar um 500 real com `SHUTDOWN` no H2 num IT próprio: comprovaria o 500, mas com um teste que
    derruba o banco para forçar um estado que a API não produz em uso normal.
- **Impacto:** spec 002 (clarificação, FR-006 e casos de borda), research R4 e data-model. As tarefas não
  mudam: o T002(i) e o T003(e) já cobrem 405 e 400.

## 2026-11-23 — Plano 002: um bolt por marco, falha de inicialização no JSON e Princípio V

Três decisões do responsável na revisão do plano 002, antes das tarefas.

### 1. Um bolt por marco, com etapas por assunto

- **Origem:** a research R16 do plano 002 dividia o Marco 2 em seis bolts, cada um com branch e PR, um
  por papel e assunto.
- **Decisão (responsável):** um único bolt por marco. O Marco 2 é entregue no branch
  `marco/2-operacao`, com um único PR, dividido em seis etapas por assunto e com um commit por etapa.
  Os papéis (implementador, infraestrutura e documentação) são os responsáveis por etapa, e não bolts
  separados.
- **Motivo:**
  - um extra só entra completo, e o pipeline só serve de portão de merge quando tudo o que ele verifica
    já existe;
  - PRs intermediários deixariam o marco pela metade na `main`;
  - um commit por etapa mantém a revisão por assunto dentro do PR único, como nos bolts 5 e 6 do Marco 1
    (entrada "Bolts 5 e 6 entregues juntos").
- **Alternativa rejeitada:** seis bolts com branch e PR próprios, como na primeira versão da R16.
- **Regras mantidas:** cada etapa começa pelos testes e termina com `./mvnw verify` verde; o limite de
  três agentes e o dono único de cada arquivo compartilhado valem por etapa; PR, merge e tag continuam
  com o responsável.
- **Impacto:** research R16, plano 002 (resumo e riscos), quickstart e a história 6 da spec ("portão de
  merge do bolt do marco").

### 2. Evento JSON da falha de inicialização sem rastro obrigatório

- **Origem:** a US4-3 da spec 002 pedia a mensagem **e a pilha** da exceção no mesmo evento JSON, usando
  a falha por arquivo ausente como exemplo. O plano apontou que o Boot não registra a pilha quando um
  `FailureAnalyzer` trata a falha, que é o caso do `MovieFileFailureAnalyzer`, e previa procurar outra
  falha, sem analisador, só para ter uma pilha.
- **Decisão (responsável):** o evento JSON da falha de inicialização traz a mensagem de erro. O rastro
  da exceção só aparece quando existe, no mesmo evento, porque o analisador de falhas o substitui por uma
  mensagem clara.
- **Motivo:** a mensagem clara do Marco 1 (spec 001, FR-005 e SC-006) é o comportamento desejado.
  Provocar uma falha artificial só para mostrar uma pilha testaria o formato do Boot, e não a aplicação.
- **Alternativa rejeitada:** manter a pilha obrigatória e testar com uma falha que nenhum analisador
  trata.
- **Impacto:** spec 002 (clarificação, US4-3 e FR-017), research R6 e o risco correspondente do plano,
  que saiu.

### 3. Princípio V: ETag, health e métricas ligados no `./mvnw` (emenda 1.4.1)

- **Origem:** o plano 002 deixou ETag, health e métricas ligados no modo padrão, sem perfil nem chave, e
  registrou isso como uma leitura do Princípio V. O texto do princípio listava o ETag entre os extras que
  "MUST ser ativados só pelo seu perfil ou pela sua configuração".
- **Decisão (responsável):** aceitar a leitura do plano e emendar o texto (PATCH, 1.4.0 → 1.4.1):
  - extras com infraestrutura própria (PostgreSQL, Kafka, Prometheus e Grafana em execução, front-end,
    imagem e deploy) continuam ativados só pelo seu perfil ou pela sua configuração;
  - extras da API sem infraestrutura própria (listagem, Swagger UI, ETag, health e métricas) podem ficar
    ligados no modo padrão, desde que não alterem o comportamento avaliado;
  - a autenticação continua desligada por padrão (Princípio VII).
- **Motivo:**
  - ETag, health e métricas não mudam o contrato de intervalos, os resultados nem os testes do
    enunciado. A suíte do Marco 1 passa sem alteração (SC-002 da spec 002);
  - o briefing já põe esses itens na camada "Extra na API", que roda no `./mvnw`;
  - o objetivo do isolamento é não exigir infraestrutura no modo padrão, e esses extras não têm nenhuma.
- **Por que PATCH:** a emenda esclarece a intenção do texto, sem princípio novo nem regra removida. A
  listagem e o Swagger UI do Marco 1 já ficavam ligados no modo padrão.
- **Alternativa rejeitada:** uma propriedade de liga e desliga para cada extra da API: configuração sem
  uso, contra a regra de simplicidade do próprio Princípio V.
- **Impacto:** constituição 1.4.1, com o Sync Impact Report atualizado, e o Constitution Check do plano
  002. Spec 001 e `AGENTS.md` sem mudança.

### 4. README no escopo do Marco 2, como etapa 6 do bolt

- **Origem:** o pedido de tarefas do responsável ordena as etapas como ETag, observabilidade,
  Dockerfile, pipeline e Dependabot, ADRs e atualização do README. A spec 002 deixava o README fora do
  escopo, e a R16 juntava logs e JaCoCo numa etapa própria.
- **Decisão (responsável, aplicada pelo implementador antes das tarefas, Princípio IV):**
  - a spec ganhou a FR-034 (o que o README passa a documentar), comprovada por revisão, com a seção
    "Revisão do README" no checklist (Princípio VI, 1.4.0);
  - a R16 passou a ter as seis etapas do pedido: os logs entram na etapa de observabilidade, e o JaCoCo
    na de pipeline, com o implementador como responsável pelo `pom.xml`.
- **Impacto:** spec 002 (FR-032, FR-034 e premissas), checklist, research R16, plano (resumo e matriz) e
  `tasks.md`.

## 2026-11-23 — Emenda 1.4.0 da constituição: comprovação por tipo de requisito (Princípio VI)

- **Origem:** clarificação da spec 002. O Princípio VI exigia que todo requisito apontasse para um
  teste de integração. A spec 002 tem requisitos que não são comportamento da aplicação: ADRs (FR-030 e
  FR-031), imagem e pipeline (FR-021 a FR-029). A decisão de comprovar os ADRs por revisão (entrada
  abaixo) deixava a spec em desacordo com o princípio.
- **Decisão (responsável):** emenda MINOR no Princípio VI. Cada requisito aponta para a forma de
  comprovação adequada ao seu tipo:
  - **comportamento da aplicação:** teste de integração, como antes (Princípio I);
  - **documentação** (README, ADRs e guias): revisão no pull request, com item no checklist da spec;
  - **pipeline e imagem** (teste de fumaça do container, análise de segurança, cobertura mínima e lint
    do Dockerfile): execução do pipeline no GitHub Actions.

  Requisito de comportamento da aplicação continua exigindo teste de integração e não pode ser
  comprovado só por revisão ou pelo pipeline.
- **Motivo:**
  - o conteúdo de um documento só uma leitura avalia, e um teste confirmaria só que as seções existem;
  - o container e o pipeline só existem no GitHub Actions. Um teste de integração no `./mvnw verify`
    não os exercita, e trazê-los para lá exigiria Docker no modo padrão, contra o Princípio II;
  - sem a emenda, a spec teria de criar testes de fachada ou deixar requisitos sem comprovação.
- **Alternativas rejeitadas:**
  - manter o Princípio VI e abrir exceções em cada spec: a spec não pode abrir exceção a um princípio,
    o mesmo problema que levou à emenda 1.2.0;
  - verificar ADRs por um estágio do pipeline: teste de forma, não de conteúdo (entrada abaixo).
- **Impacto:**
  - constituição 1.3.0 → 1.4.0 (MINOR, compatível: todo requisito que cumpria a versão anterior
    continua cumprindo), com o Sync Impact Report atualizado;
  - `AGENTS.md`, regra de rastreabilidade do fluxo de trabalho;
  - spec 002: a FR-032 cita o Princípio VI;
  - spec 001: nenhuma mudança, porque todos os seus requisitos são de comportamento.

## 2026-11-23 — ADRs comprovados por revisão (spec 002)

- **Origem:** clarificação da spec 002. O Princípio VI pede que cada requisito aponte para um teste, e
  os ADRs (FR-030 e FR-031) são documentos, sem comportamento a testar.
- **Decisão (responsável):** os ADRs são comprovados por revisão no pull request, pela seção "Revisão
  dos ADRs" do checklist da spec (`specs/002-operacao/checklists/requirements.md`). Ela confere três
  pontos:
  - cada ADR tem contexto, decisão, alternativas consideradas e consequências;
  - cada ADR cita a entrada correspondente neste arquivo;
  - há um ADR para cada decisão de arquitetura listada na FR-030.

  Não há verificação automatizada no pipeline.
- **Motivo:** o que importa num ADR é o conteúdo, e só uma leitura avalia isso. Um teste no pipeline só
  confirmaria que as seções existem.
- **Alternativas rejeitadas:**
  - estágio do pipeline que confere a existência e as seções dos ADRs: um teste de forma, não de
    conteúdo;
  - registrar a decisão só aqui, sem emenda: deixaria a spec em desacordo com o Princípio VI. Foi
    substituída pela emenda 1.4.0 (entrada acima).
- **Efeito:** as decisões da FR-030 que ainda não têm entrada aqui (por exemplo, a stack e o Problem
  Details, decididos no plano 001) ganham a entrada junto com o ADR (FR-031).
- **Impacto:** spec 002 (clarificação de 2026-11-23, história 7, FR-031, FR-032 e SC-009) e o checklist
  da spec.

## 2026-11-23 — Critérios de reprovação do pipeline: segurança e cobertura (spec 002)

- **Origem:** clarificação da spec 002. A FR-028 pedia análise de segurança e cobertura no pipeline,
  sem dizer o que reprova.
- **Decisão (responsável):**
  - **segurança:** achados de severidade alta ou crítica reprovam o pipeline. Na análise de
    dependências e na da imagem, contam só as vulnerabilidades com versão corrigida disponível. Achados
    médios e baixos aparecem no relatório, sem reprovar;
  - **cobertura:** medida pelos testes de integração (JaCoCo sobre a execução do Failsafe), com um
    mínimo de linhas abaixo da cobertura atual medida, com piso de 80% (valor final de 90%, abaixo).
- **Medição (implementador):**
  - commit `39b3188` (fim do Marco 1, `main`), JaCoCo 0.8.14 chamado pela linha de comando
    (`prepare-agent-integration`, `verify` e `report-integration`), sem alterar o `pom.xml`;
  - 136 ITs, sem falhas nem testes pulados;
  - **linhas: 330 de 340, 97,06%**; ramos: 109 de 115, 94,78%;
  - as 10 linhas não cobertas estão no `main` da `AwardsApplication`, em construtores e métodos
    exigidos pelo JPA nas entidades `Movie` e `Producer` e em uma linha de cada um de
    `InvalidParameterException`, `MovieCsvReader`, `MovieFileProperties` e `MovieFileLoader`.
- **Valor escolhido (responsável):** **90% de linhas**, acima do piso de 80%. O implementador havia
  proposto 95%, e o responsável ajustou para 90% porque:
  - uma margem de 2 pontos é pequena para 340 linhas: 7 linhas novas sem cobertura já bastariam para
    reprovar;
  - o Marco 3 acrescenta código que só roda no perfil Docker (PostgreSQL e Kafka), fora do `./mvnw
    verify` padrão, e que portanto reduz a cobertura medida.
- **Motivo:**
  - uma vulnerabilidade alta sem versão corrigida não tem ação possível no bolt, e reprovar por ela
    travaria o merge sem ganho;
  - os achados médios e baixos ficam visíveis no relatório, para decisão caso a caso;
  - a cobertura vem dos testes de integração porque, pelo Princípio I, eles são os únicos testes do
    back-end. Um mínimo tirado da medição evita um número arbitrário.
- **Alternativas rejeitadas:**
  - cobertura só relatada: não impediria que um bolt entregasse código sem teste, contra o Princípio VI;
  - mínimo fixo de 80%: deixaria cair 17 pontos sem nenhum aviso;
  - mínimo de 95%, proposto pelo implementador: margem pequena demais, pelos motivos acima;
  - reprovar também por vulnerabilidades sem correção: bloquearia por algo que o projeto não consegue
    resolver.
- **Impacto:** spec 002 (clarificação de 2026-11-23, história 6, FR-028, FR-028a e SC-008). O plugin
  entra no `pom.xml` no bolt do pipeline, pelo plano.

## 2026-11-23 — Emenda 1.3.0 da constituição: log em texto no `./mvnw`, JSON no container

- **Origem:** clarificação da spec 002 (Marco 2, operação). A constituição 1.1.0 dizia que, a partir do
  Marco 2, "valem os logs estruturados em JSON", sem separar a execução local da execução em container.
  Com isso, o `./mvnw spring-boot:run` do avaliador passaria a mostrar JSON no terminal.
- **Decisão (responsável):**
  - na execução local padrão (`./mvnw spring-boot:run`), o log continua no texto padrão do framework;
  - o JSON é ligado por uma variável de ambiente. O Dockerfile define essa variável na imagem, e ela
    também liga o JSON em qualquer outra execução;
  - a constituição foi emendada: os logs em JSON valem para a execução em container e para os ambientes
    operacionais, e a execução local padrão usa texto.
- **Motivo:**
  - quem avalia roda o `./mvnw` e lê o log no terminal, onde o texto é legível e o JSON não é;
  - o JSON existe para ser lido por coletores de log, que só estão presentes no container e nos
    ambientes operacionais;
  - com uma só variável, o mesmo artefato serve aos dois usos, e o formato é escolhido no ambiente, e
    não no build.
- **Alternativas rejeitadas:**
  - JSON sempre, com texto opcional por variável: cumpria a redação anterior, mas piorava a leitura do
    terminal justamente no modo que o avaliador usa;
  - JSON sempre, sem opção de texto: mesmo problema, e sem como voltar ao texto;
  - formato escolhido por perfil do Spring: misturaria o formato do log com a escolha de banco e de
    ingestão, que é o papel dos perfis.
- **Impacto:**
  - constituição 1.2.0 → 1.3.0 (MINOR), seção "Restrições técnicas e de segurança" > Observabilidade;
  - `AGENTS.md`, linha "Logs" da tabela de stack;
  - spec 002: clarificação de 2026-11-23, história 4, FR-017, FR-018, FR-020, FR-024a, FR-030 e SC-005.
    Os testes que verificam o log passam a valer nos dois formatos;
  - spec 001: nenhuma mudança.

## 2026-11-22 — Três ambiguidades apontadas pelo testador adversário

- **Origem:** rodada do Codex como testador adversário do Marco 1, com os casos em
  `docs/ai/adversario/`. O testador escreveu os CSVs a partir da spec, sem ler o código de produção.
  Os casos 12 e 13 foram entregues sem resultado esperado, porque a spec não determinava uma resposta
  única. O caso 09 usa anos (0, 2147483648, 4294967296) que a spec não proibia.
- **Decisões (responsável):**
  1. **`and` sem diferenciar maiúsculas (caso 12).** `AND`, `And` e `aNd` são separadores, como `and`.
     A regra vale só para o separador: os nomes continuam comparados exatamente como escritos (FR-008).
     Spec: FR-007.
  2. **Ordem Unicode no desempate por nome (caso 13).** Os nomes empatados são ordenados por
     `String.compareTo`, caractere a caractere, independente de locale. Spec: FR-015.
  3. **Ano válido de 1 a 9999 (caso 09).** Fora dessa faixa, a linha é rejeitada como inválida, com
     aviso no log. Spec: FR-006, FR-016b e o motivo novo `YEAR_OUT_OF_RANGE` do `data-model.md`.
- **Motivos:**
  - (1) a spec já não diferencia maiúsculas no `winner` nem no cabeçalho, e um `AND` no meio de nomes é separador para
    quem lê o arquivo. Tratar `Iris AND Nilo` como um único produtor inventaria um nome;
  - (2) a ordem por código de caractere é determinística, não depende da máquina do avaliador e é a
    ordem natural da linguagem. Uma colação linguística mudaria com o locale da JVM;
  - (3) o enunciado trata de anos de premiação. A faixa elimina o ano 0 e os valores que estouram
    `int`, sem exigir tipos maiores no banco e no contrato.
- **Alternativas rejeitadas:**
  - `and` só em minúsculas, como estava: o caso 12 mostrou que o resultado mudaria só pela caixa do
    separador;
  - colação com `java.text.Collator`: o resultado dependeria do locale;
  - anos sem limite, com `long` ou `BigInteger`: exigiria mudar o esquema e o tipo do contrato para um
    caso que não representa uma premiação real.
- **Efeito sobre os casos:** os resultados esperados dos casos 12 e 13 foram escritos pelo implementador
  a partir das decisões (1) e (2), e o README dos casos registra isso.
- **Caso 09 diverge do testador (responsável):** com a decisão (3), as linhas dos anos 0, 2147483647,
  2147483648 e 4294967296 são rejeitadas. Só sobram os anos 1 (`Orion`) e 2 (`Nova`), de produtores
  diferentes, então o esperado passa a ser `min` e `max` vazios. O `expected.json` original do testador
  fica intacto em `docs/ai/adversario/`, como registro da rodada. O esperado ajustado existe só na cópia
  de `src/test/resources/scenarios/adversario/`, que é a usada pelo `Case09ExtremeYearsIT`.
- **O que o caso 09 revelou:** o leitor aceitava o ano 0. O padrão `\d{1,9}` só verificava os dígitos,
  sem faixa. Com o arquivo do testador, a aplicação respondia com `Nova` de 0 a 2 em `min` e em `max`.
  Os anos de 10 dígitos já eram rejeitados, como "ano não numérico", uma mensagem enganosa. Agora todos
  saem com o motivo `YEAR_OUT_OF_RANGE`.
- **Impacto:** spec (sessão 2026-11-22, FR-006, FR-007, FR-015, FR-016b e casos de borda),
  `data-model.md`, research R3 e R4, matriz do `plan.md` e 13 ITs novos em
  `src/test/java/com/outsera/awards/adversario/`.

## 2026-11-22 — Decisões do Marco 1 que divergem do briefing (T082)

Registro consolidado no fechamento do marco. As decisões foram tomadas na clarificação da spec
(sessão de 2026-11-18) e no plano 001. Os detalhes ficam na spec e na research.

- **Linha inválida é ignorada com aviso, e a carga continua (FR-006).**
  - *Briefing:* "a carga inteira é rejeitada", marcado como decisão em aberto (`briefing.md` e
    `decisoes-planejamento.md`).
  - *Decisão (responsável, na clarificação):* só os erros estruturais (arquivo ausente, cabeçalho ou
    separador inválido) impedem a inicialização. Uma linha inválida é ignorada, com aviso no log (número
    da linha e motivo), e a carga termina com um resumo de lidas, aceitas e rejeitadas.
  - *Efeito:* uma linha ruim não derruba a aplicação nem esconde o resultado das linhas boas, e o aviso
    com o número da linha continua mostrando o problema.
  - *Alternativa rejeitada:* rejeitar a carga inteira, como no briefing.
- **Duplicata identificada por ano + título (FR-006d).**
  - *Briefing:* não tratava linhas repetidas.
  - *Decisão (responsável, na clarificação):* ano e título, sem os espaços em volta, identificam o
    filme. A primeira ocorrência prevalece, e as repetições são ignoradas com aviso.
  - *Efeito:* sem isso, uma linha vencedora repetida geraria um intervalo 0 falso para os seus produtores.
  - *Alternativa rejeitada:* contar cada linha como um filme.
- **Cabeçalho sem diferenciar maiúsculas, mas com ordem fixa (FR-003).**
  - *Briefing:* cabeçalho `year;title;studios;producers;winner`, sem dizer se a comparação é exata.
  - *Decisão (responsável, na clarificação):* `Year;Title;...` é aceito, e uma ordem diferente, uma
    coluna faltando ou uma a mais impedem a inicialização.
  - *Efeito:* a diferença de maiúsculas não muda o sentido do arquivo. Já uma ordem diferente indica um
    arquivo de outro formato, e aceitá-la exigiria mapear colunas por nome, sem pedido do enunciado.
  - *Alternativas rejeitadas:* comparação exata, que recusaria um arquivo inofensivo, e colunas em
    qualquer ordem.
- **Contrato em `/openapi.yaml`, e não em `/v3/api-docs` (research R8).**
  - *Briefing:* `GET /v3/api-docs` como contrato OpenAPI.
  - *Decisão (plano 001, research R8):* o contrato estático, escrito antes do código, é a única fonte
    e é publicado em `/openapi.yaml`. No bolt 6, isso levou a tirar o springdoc do projeto (entrada
    "Swagger UI pelo webjar, sem springdoc", abaixo).
  - *Motivo:* o briefing exige que o contrato seja a fonte do Swagger UI e da validação das respostas.
    Um contrato gerado pelo código seria um segundo contrato, que poderia divergir do desenhado.
  - *Alternativa rejeitada:* contrato gerado por anotações em `/v3/api-docs`.

## 2026-11-22 — Swagger UI pelo webjar, sem springdoc (bolt 6)

- **Origem:** implementador, na T076. Com `springdoc.api-docs.enabled=false`, o `/swagger-ui.html`
  respondia 404. Nas classes do springdoc 3.2.0, o `SwaggerConfig` (Swagger UI) tem
  `@ConditionalOnBean(SpringDocConfiguration.class)`, e a `SpringDocConfiguration` tem
  `@ConditionalOnProperty("springdoc.api-docs.enabled", matchIfMissing = true)`. Ou seja, nessa versão
  o Swagger UI do springdoc exige a geração automática do contrato ligada.
- **Decisão (responsável):** tirar o springdoc e servir o Swagger UI pelo webjar
  `org.webjars:swagger-ui` 5.34.0 (a mesma versão que o springdoc 3.2.0 traria), com o
  `webjars-locator-lite` gerenciado pelo Boot e uma página estática `static/swagger-ui.html` que aponta
  para `/openapi.yaml`.
- **Motivo:** mantém o contrato estático como fonte única (research R8). Não há contrato gerado pelo
  código, `/v3/api-docs` responde 404 e nada varre os controllers na inicialização.
- **Alternativas rejeitadas:**
  - springdoc com `api-docs` ligado: publicaria em `/v3/api-docs` um segundo contrato, gerado pelo
    código, que poderia divergir do estático;
  - springdoc com `api-docs` ligado e nenhum caminho gerado (`paths-to-exclude=/**`): `/v3/api-docs`
    continuaria publicado, vazio.
- **Fim de linha:** o `ApiDocsIT` compara `/openapi.yaml` byte a byte com o contrato de `specs/`. Nesta
  cópia de trabalho, o arquivo de `specs/` estava com CRLF, extraído antes do `.gitattributes`
  (`eol=lf`), e o teste falhava. O conteúdo versionado tem LF nos dois arquivos, então o arquivo foi
  extraído de novo. Outros 19 arquivos da cópia de trabalho estão na mesma situação e não foram
  tocados. Num clone novo, todos vêm com LF.
- **Impacto:** research R1 e R8, plano (stack e estrutura), T074 a T076 e `pom.xml`.

## 2026-11-22 — Detalhes da listagem de filmes (bolt 5)

- **Origem:** implementador, ao escrever os testes do bolt 5. A spec não dizia o que acontece com uma
  página além da última. Com `page=2147483647`, o deslocamento (`page` × `size`) passa de
  `Integer.MAX_VALUE`, o Spring Data recusa a consulta e a resposta seria 500.
- **Decisão (implementador, para revisão do responsável no PR):**
  - a spec ganhou um caso de borda antes do código (Princípio IV): uma página além da última responde
    200 com `content` vazio e os totais reais, e um `page` que não cabe em 32 bits é inválido (400);
  - quando o deslocamento passa do limite, o `MovieQueryService` consulta só a contagem;
  - todos os parâmetros inválidos da consulta aparecem em `invalidParams`, e não só o primeiro;
  - o `ResponseTimeIT` usa `@DirtiesContext`, para que a primeira chamada medida aconteça num contexto
    sem nenhuma requisição anterior, incluindo a inicialização preguiçosa do Spring MVC (SC-005).
- **Alternativas rejeitadas:**
  - recusar com 400 uma página além da última: o FR-021 aceita qualquer `page` maior ou igual a 0, e o
    US3-6 já responde 200 com lista vazia quando não há resultado;
  - limitar `page` a um máximo arbitrário: seria uma regra sem origem na spec.
- **T067:** os ITs de carga do bolt 2 (inclusive o `BomVariationsIT`, criado depois da T067) passaram a
  verificar os filmes por `GET /api/movies`, e o `JdbcTemplate` saiu da `ScenarioIT`. No
  `HarmlessVariationsIT`, a consulta à tabela `producer` não tem equivalente na listagem, que devolve os
  produtores como texto. O nome sem espaços em volta continua provado pelos intervalos
  (`Producer A` 1990→1991, classe `WithVariations`).
- **Impacto:** caso de borda novo na spec, research R7, T063 e T071.

## 2026-11-22 — Bolts 5 e 6 entregues juntos

- **Decisão (responsável):** os bolts 5 (listagem) e 6 (Swagger UI e fechamento do Marco 1) são
  entregues em um único branch, `bolt/5-6-listagem-swagger`, e em um único pull request, com um commit
  por etapa:
  1. fase 6 — listagem (T062 a T073);
  2. fase 7 — Swagger UI (T074 a T077);
  3. fase 8 — fechamento (T078 a T083).
- **Regras mantidas:** cada etapa começa pelos testes, que precisam falhar antes da implementação, e
  termina com `./mvnw verify` verde antes do commit. O PR, o merge e a tag continuam com o responsável.
- **Alternativa rejeitada:** um branch e um PR por bolt, como previa o mapeamento original do
  `tasks.md`.
- **Impacto:** mapeamento bolt → fase, checkpoints das fases 6 e 7, dependências e notas do
  `specs/001-producer-intervals-api/tasks.md`. Nenhuma mudança na spec nem no contrato.

## 2026-11-22 — Validador do contrato com a coordenada nova da Atlassian (T055)

- **Origem:** implementador, no bolt 4. Ao resolver
  `com.atlassian.oai:swagger-request-validator-core:3.0.0`, o Maven avisa que o artifact foi relocado
  pelo próprio projeto para `com.atlassian.oai:openapi-request-validator-core:3.0.0`.
- **Decisão (implementador, para revisão do responsável no PR):** declarar no `pom.xml` a coordenada
  nova, na mesma versão 3.0.0 da research R8. O código e os pacotes Java são os mesmos
  (`com.atlassian.oai.validator`), então o plano B (`networknt/json-schema-validator`) não foi
  necessário.
- **Exclusão:** o `jakarta.validation-api` que chega pelo `swagger-core` foi excluído, só nessa
  dependência de teste. Sem a exclusão, o Spring tentava montar um Bean Validation nos testes e
  registrava `Failed to set up a Bean Validation provider`, um comportamento que não existe em
  produção. Com a exclusão, o classpath de teste volta a ser igual ao de produção e os 78 testes
  passam.
- **Controle:** o `IntervalsContractIT` inclui um caso que adultera a resposta real com um campo a
  mais e exige que o validador a recuse (`additionalProperties: false`), para provar que a validação
  não é vazia.
- **T060:** nenhum ajuste foi necessário no `ProducerIntervalController` nem nos records. O controller
  mínimo do bolt 3 já cumpria o formato, e o T056 e o T057 passaram antes da implementação do bolt 4.
  Só o T058 falhou antes do T059, por falta do contrato em `static/`.

## 2026-11-22 — Anos do `disjoint-producers.csv` fora do `movielist.csv` (T041)

- **Origem:** conflito encontrado pelo implementador no bolt 3. A T041 pedia que o `Producer B`
  vencesse em 1960 e 2010, mas o SC-004a exige um arquivo sem nenhum ano em comum com o arquivo
  fornecido, que cobre 1980 a 2019.
- **Decisão (responsável):** seguir a spec, que tem prioridade sobre a task. O `Producer B` vence em
  1925 e 1975, e o intervalo continua 50. O texto da T041 foi corrigido.
- **Alternativa rejeitada:** manter 1960–2010. O cenário deixaria de cumprir o SC-004a.

## 2026-11-22 — Uma vitória a mais no `harmless-variations.csv` (T048)

- **Origem:** a T048 pede que as 4 linhas do `harmless-variations.csv` gerem ao menos um intervalo, e o
  arquivo do bolt 2 não tinha nenhum produtor com duas vitórias.
- **Decisão (implementador):** o filme de 1991 (`winner` igual a ` Yes `) passa a ser do
  `Producer A`, o mesmo do filme de 1990 (`YES`, com espaços em volta do nome). O intervalo
  `Producer A` 1990→1991 só aparece se o BOM, os espaços e as duas variações de `yes` forem tratados
  corretamente. Os testes do bolt 2 sobre esse arquivo continuam valendo sem alteração.

## 2026-11-22 — `ProducerNamesIT` absorvido pelo `ProducerSeparatorsIT` (T043)

- **Decisão (planejada no T022b):** os casos de separação de nomes passam a ser verificados por HTTP no
  `ProducerSeparatorsIT`, e o `ProducerNamesIT` e o `producer-names.csv`, que verificavam pelo banco,
  foram removidos (Princípio I).
- **Como os nomes aparecem nos intervalos:** cada forma está em duas vitórias de anos seguidos, e os
  nomes que se repetem entre formas estão em anos também seguidos. Todo intervalo correto vale 1, e
  `min` e `max` trazem a lista completa de nomes. Um nome separado errado vira um item a mais ou a
  menos, e dois nomes unidos por engano, ou um nome contado duas vezes no mesmo filme, geram um
  intervalo diferente de 1.

## 2026-11-22 — Separação de produtores em etapas, no lugar da regex (segunda revisão do Codex)

- **Origem:** segundo comentário do Codex, como revisor, no pull request do bolt 2, sobre a mesma regex
  do `ProducerNameParser`. A regex `\s*,\s*(?:and(?=\s|$)\s*)?|(?:^|\s+)and(?=\s|$)\s*`, adotada após a
  primeira revisão (entrada "`and` no fim do campo de produtores", de 2026-11-19), consome o espaço
  depois de um `and`, e o `and` seguinte deixa de ser reconhecido:
  - `Producer A and and Producer B` vira `Producer A` e `and Producer B`;
  - `and and` vira o produtor `and`, e a linha é aceita em vez de rejeitada.

  Os dois casos foram reproduzidos com testes antes da correção.
- **Decisão (responsável):** aceitar e, por ser o segundo defeito na mesma regex, trocá-la por uma
  separação em etapas:
  1. dividir o campo pelas vírgulas;
  2. em cada parte, a palavra `and` isolada (separada por espaços) é separador;
  3. o nome é o trecho original entre os separadores, então o texto interno é preservado;
  4. as partes vazias são descartadas.
- **Motivo:** cada etapa é simples de ler e de conferir caso a caso. Como o `and` é reconhecido pela
  palavra, e não pelos espaços em volta, os casos de início e fim do campo (primeira revisão) e de `and`
  repetido (segunda revisão) deixam de precisar de tratamento especial.
- **Alternativas rejeitadas:**
  - ajustar a regex de novo, por exemplo com lookbehind: seria a terceira correção na mesma expressão;
  - dividir por espaços e juntar as palavras com um espaço: normalizaria os espaços internos dos nomes,
    o que a FR-008 proíbe.
- **Verificação:**
  - o novo `ProducerNamesIT` (T022b) cobre todos os casos da research R4, mais `and and` (linha
    rejeitada, sem produtores), `Producer A and and Producer B` (2 produtores) e um nome com espaços
    internos duplos. Os casos antigos já passavam com a regex anterior e continuam passando;
  - a regex antiga e o parser novo foram comparados nas 206 linhas do `movielist.csv` do enunciado,
    sem nenhuma diferença.
- **Impacto:** FR-007 da spec (exemplos com `and` repetido), research R4 (decisão, histórico das duas
  revisões e casos), T030 (texto da separação), nova T022b e T043 (absorve o `producer-names.csv`, e o
  `ProducerNamesIT` sai nesse bolt). A verificação pelo banco é temporária (Princípio I), porque a
  listagem devolve os produtores como texto e não prova a separação.

## 2026-11-22 — BOM removido do `Reader`, antes do Commons CSV (revisão do Codex)

- **Origem:** comentário do Codex, como revisor, no pull request do bolt 2. O leitor removia o BOM só
  do primeiro campo, depois de o Commons CSV já ter analisado a linha. Isso é tarde demais em dois
  casos:
  - BOM seguido de cabeçalho entre aspas (`"year";...`): com o BOM na frente, o Commons CSV não
    reconhece a aspa, o campo vira `"year"` literal e a inicialização falha com `INVALID_HEADER`;
  - BOM seguido de linha em branco: a linha contém só o BOM, deixa de ser uma linha em branco e vira
    o cabeçalho, o que também dá `INVALID_HEADER`.

  Os dois cenários foram reproduzidos com testes antes da correção.
- **Decisão (responsável):** aceitar. O BOM é removido do `Reader` antes de ele ser entregue ao Commons
  CSV, lendo o primeiro caractere com `mark`/`reset`. O tratamento no primeiro campo foi retirado.
- **Motivo:** a FR-005a manda aceitar o BOM sem aviso, qualquer que seja o resto do arquivo. Tirar o BOM
  antes da análise faz o Commons CSV ver o arquivo exatamente como ele seria sem o BOM.
- **Alternativa rejeitada:** `BOMInputStream`, do Commons IO. O Commons IO só chega como dependência
  transitiva do Commons CSV, e usá-lo direto exigiria declará-lo no `pom.xml`: uma dependência nova só
  para um caractere.
- **Impacto:** research R3, tarefa T031 (a remoção do BOM sai da validação do cabeçalho e vai para a
  leitura) e nova tarefa T022a, com o `BomVariationsIT` e os cenários `bom-quoted-header.csv` e
  `bom-blank-line.csv`. A matriz do `plan.md` (FR-005a e FR-006c) foi atualizada.

## 2026-11-22 — Linha só com espaços e arquivo que não pode ser analisado (R3, T031)

- **Origem:** sonda do implementador com o Commons CSV 1.14.1, no início do bolt 2. O
  `ignoreEmptyLines` só pula linhas totalmente vazias. Uma linha só com espaços vira um registro de
  uma coluna vazia, que seria rejeitado por `COLUMN_COUNT`, contrariando a FR-005a ("linhas em branco
  em qualquer posição" são aceitas sem aviso). A spec também não dizia o que fazer com aspas sem
  fechamento ou com bytes fora de UTF-8.
- **Decisão (implementador, a confirmar pelo responsável no PR):**
  - um registro de uma coluna vazia é tratado como linha em branco;
  - aspas sem fechamento e bytes que não são UTF-8 válido dão erro estrutural `UNREADABLE`.
- **Motivo:** a linha só com espaços é uma linha em branco para quem edita o arquivo. No segundo caso,
  o leitor não consegue separar as linhas com segurança, e decodificar com substituição gravaria nomes
  corrompidos sem aviso.
- **Alternativa rejeitada:** rejeitar só a linha com aspas abertas. O Commons CSV lê até o fim do
  arquivo procurando o fechamento, então não há como saber onde a linha termina.
- **Impacto:** research R3 e tarefas T012, T013, T015 e T031, com dois cenários novos de falha de
  inicialização (`unterminated-quote.csv` e `invalid-encoding.csv`) e uma linha só com espaços no
  `harmless-variations.csv`.

## 2026-11-22 — Critério "sem `WARN`" restrito ao carregador do CSV (T014 e T015)

- **Origem:** observação do implementador ao fim do bolt 1. A saída do `./mvnw verify` já trazia
  `WARN` e `WARNING` que não vêm da aplicação: o aviso do Flyway sobre a versão do H2 (2.4.240, mais
  nova que a testada) e o aviso de agente dinâmico do Mockito. Com o critério "o log não contém
  `WARN`", o T014 e o T015 falhariam por causa de bibliotecas, e não do carregador.
- **Decisão (responsável):** o T014 e o T015 verificam apenas as linhas `WARN` emitidas pelo
  carregador do CSV (logger `MovieFileLoader`, pacote `com.outsera.awards.ingestion`), e não o log
  inteiro. O aviso do Flyway sobre a versão do H2 é mantido, sem supressão.
- **Motivo:** o que os testes precisam provar é que o carregador não rejeitou nem reclamou de
  nenhuma linha. Avisos de terceiros não dizem nada sobre isso e podem mudar a cada versão.
- **Alternativa rejeitada:** suprimir os avisos de terceiros na configuração de log. Esconderia
  informação útil e acoplaria o teste a uma lista de avisos externos.
- **Impacto:** texto do T014 e do T015 em `specs/001-producer-intervals-api/tasks.md`, com uma nota
  explicando o critério.

## 2026-11-22 — Mockito excluído do `spring-boot-starter-test`

- **Origem:** a mesma observação acima. O Mockito vem junto com o `spring-boot-starter-test` e emite
  um aviso de agente dinâmico ao se acoplar à JVM.
- **Decisão (responsável):** excluir `mockito-core` e `mockito-junit-jupiter` do
  `spring-boot-starter-test` no `pom.xml` (nova tarefa T011a, no início do bolt 2).
- **Motivo:** o Princípio I proíbe mocks, então o Mockito não tem uso no projeto. Tirá-lo do
  classpath transforma a regra em erro de compilação e elimina o aviso.
- **Alternativa rejeitada:** manter o Mockito e configurar o agente (`-javaagent`) no Failsafe para
  silenciar o aviso. Manteria disponível uma ferramenta que o projeto proíbe.
- **Impacto:** `pom.xml` e `tasks.md` (T011a). Nenhuma mudança na spec nem no contrato.

## 2026-11-19 — Emenda 1.1.0 da constituição: observabilidade a partir do Marco 2

- **Origem:** achado H1 do `/speckit-analyze` da spec 001. A constituição pedia logs em JSON e
  readiness, mas a spec do Marco 1 punha observabilidade fora do escopo.
- **Decisão (responsável):** emendar a constituição. Os logs estruturados em JSON e os probes de
  liveness e readiness passam a valer a partir do marco de observabilidade (Marco 2).
- **Motivo:**
  - o Marco 1 se limita ao que o enunciado avalia, e o enunciado não pede nenhum dos dois itens;
  - antecipá-los acrescentaria dependências e configuração ao modo padrão, que é o que o avaliador
    executa;
  - a garantia da readiness já existe no Marco 1, porque a carga termina antes de o servidor aceitar
    conexões (FR-001).
- **Alternativa rejeitada:** incluir logs em JSON e readiness no Marco 1.
- **Impacto:**
  - versão 1.0.0 → 1.1.0 (MINOR);
  - o `AGENTS.md` foi atualizado;
  - nenhuma mudança na spec 001;
  - no Marco 2, os testes que verificam mensagens de log precisam continuar passando com o formato
    JSON.

## 2026-11-19 — Emenda 1.2.0 da constituição: Princípio I reescrito

- **Origem:** achado C1′ do segundo `/speckit-analyze`. A exceção dos testes de carga tinha sido
  registrada na FR-027 da spec, mas a spec não pode abrir exceção a um princípio da constituição, que
  tem prioridade maior.
- **Decisão (responsável):** emendar a constituição, e não a spec. Novo texto do Princípio I:
  - todo teste é de integração, com `@SpringBootTest` e o contexto completo da aplicação, sem mocks;
  - o resultado é verificado preferencialmente por HTTP, e por log ou banco somente enquanto o
    comportamento não tem endpoint exposto;
  - os testes de falha de inicialização rodam a aplicação real com `SpringApplication`, sem mocks,
    e verificam a exceção e o log, porque um contexto que não sobe não pode ser verificado por
    `@SpringBootTest`.
- **Motivo:** permitir que cada comportamento seja testado no bolt em que nasce, mesmo antes de ter
  endpoint (como a carga do CSV), sem teste unitário e sem exceções espalhadas pelas specs.
- **Alternativas rejeitadas** para a falha de inicialização:
  - um `@SpringBootTest` externo que sobe a aplicação com falha por dentro: cumpre a letra do
    princípio, mas o contexto externo não teria função;
  - executar classes `@SpringBootTest` pelo `EngineTestKit`: acrescenta complexidade sem ganho.
- **Impacto:**
  - versão 1.1.0 → 1.2.0 (MINOR, compatível: todo teste que cumpria a versão anterior continua
    cumprindo);
  - a FR-027 deixou de ter exceções e só referencia o Princípio I;
  - o plano, as tarefas, a research e o `AGENTS.md` foram alinhados.

## 2026-11-19 — `and` no fim do campo de produtores (revisão do Codex)

- **Origem:** comentário do Codex, como revisor, no pull request do bolt 2. O leitor de CSV aplica
  `trim` no campo antes do parser de nomes (T031), então ` , and ` chega como `, and`. A regex da
  research R4, `\s*,\s*(?:and\s+)?|\s+and\s+`, exigia espaço depois do `and` e deixava `and` como
  nome de produtor. Com isso, a linha deixava de ser rejeitada e o caso `PRODUCERS_MISSING`
  (` , and `) do T017 falhava. O mesmo acontecia com `Producer A and`, que virava um único nome.
- **Decisão (responsável):** aceitar. O `and` passa a ser separador como palavra inteira, também no
  início e no fim do campo, com a regex `\s*,\s*(?:and(?=\s|$)\s*)?|(?:^|\s+)and(?=\s|$)\s*`. Nomes
  que contêm `and`, como `Anderson`, `Robbinsand` e `andrew`, continuam inteiros.
- **Motivo:** o parser não pode depender dos espaços em volta do campo, porque o `trim` do leitor os
  remove. A regex nova foi executada contra os casos antigos e os novos, que estão listados na R4.
- **Alternativa rejeitada:** tirar o `trim` do leitor de CSV. A regra continuaria frágil, e os outros
  campos (ano, título, `winner`) dependem do `trim`.
- **Impacto:** a R4, a T030 (regex), a T031 (nota sobre o `trim`) e as FR-007 e FR-009 da spec foram
  atualizadas. O T017 continua com o caso ` , and ` e passa a ser atendido. O T043 ganhou o caso
  `Producer A and`, que deve resultar em 1 produtor.
- **Briefing:** continua dizendo "`and` cercada de espaços" e não foi alterado. Ele é o registro do
  planejamento, e a fonte da verdade é a spec (FR-007).

## 2026-11-19 — Testes de carga antes dos endpoints (substituída pela emenda 1.2.0)

- **Origem:** achado C2 do `/speckit-analyze`. O Princípio I e a FR-027 falam em testes que "chamam
  os endpoints por HTTP", mas o bolt 2 (carga do CSV) ainda não tem endpoint de negócio.
- **Decisão (responsável):**
  - no bolt 2, os ITs de carga verificam o resultado pelo log e pelo banco, com a aplicação real no
    ar (`@SpringBootTest`);
  - nos bolts 3 e 5, os mesmos ITs ganham a verificação por HTTP;
  - os testes de falha de inicialização verificam a exceção e a mensagem, porque não chega a existir
    servidor.

  A exceção foi registrada primeiro na FR-027 da spec 001. Foi retirada de lá e passou para o
  Princípio I da constituição (emenda 1.2.0, acima).
- **Motivo:** manter os bolts pequenos e com os testes antes da implementação, sem criar testes
  unitários nem mocks.
- **Controle:** a tarefa T078 confere, no fechamento do marco, que todo IT de `ingestion/` (menos o
  `StartupFailureIT`) faz pelo menos uma chamada HTTP.