# Research: Marco 2 — Operação

**Spec**: [spec.md](spec.md) | **Plano**: [plan.md](plan.md) | **Data**: 2026-11-23

Cada item traz a decisão, o motivo e as alternativas. Os itens marcados com **verificar** dependem de um
detalhe da versão do Spring Boot 4.1.2 que só se confirma com o código rodando; o primeiro teste da etapa
correspondente (R16) faz essa verificação, e o resultado volta para esta research.

## R1. Health e métricas pelo Spring Boot Actuator

- **Decisão:**
  - dependências `spring-boot-starter-actuator` e `io.micrometer:micrometer-registry-prometheus`, com
    versões gerenciadas pelo Boot;
  - expostos pela web só `health` e `prometheus`
    (`management.endpoints.web.exposure.include=health,prometheus`);
  - probes ligados explicitamente (`management.endpoint.health.probes.enabled=true`), porque o Boot só os
    liga sozinho quando detecta Kubernetes. Caminhos: `/actuator/health/liveness` e
    `/actuator/health/readiness`;
  - `management.endpoint.health.show-details=never` e `show-components=never` (FR-011);
  - página de descoberta `/actuator` desligada (`management.endpoints.web.discovery.enabled=false`);
  - tudo na mesma porta da API (`PORT`), sem `management.server.port` (FR-008, FR-012).
- **Motivo:** o Actuator é o caminho padrão do framework para os dois probes e para o Prometheus, sem
  código próprio. Expor só o necessário reduz a superfície enquanto a autenticação não existe (Marco 3).
  Uma porta só mantém o `./mvnw` e o healthcheck da imagem simples.
- **Alternativas rejeitadas:**
  - controllers próprios de health e métricas: reimplementariam o que o framework já entrega e testa;
  - porta separada para o Actuator: mais uma variável e mais uma porta no container, sem ganho neste
    marco;
  - `probes.add-additional-paths` (`/livez`, `/readyz`): dois caminhos para a mesma coisa.
- **Verificado na etapa 2:** no Boot 4.1.2, o `spring-boot-starter-actuator` já traz as métricas do
  Micrometer. Com ele e o `micrometer-registry-prometheus`, o `/actuator/prometheus` aparece, sem
  módulo extra.
- **Verificado na etapa 2, saúde geral:** com os probes ligados, o `/actuator/health` responde
  `{"status":"UP","groups":["liveness","readiness"]}`. O Boot sempre lista os grupos, e nenhuma
  propriedade os esconde. O responsável aceitou a lista, e a FR-011 foi ajustada antes do código
  (`docs/ai/decisoes.md`). O content type é `application/vnd.spring-boot.actuator.v3+json`.

## R2. Readiness ligada ao fim da carga

- **Decisão:** usar o estado de disponibilidade do próprio Boot, sem indicador de saúde novo.
  - O `MovieFileLoader` roda em `afterSingletonsInstantiated` (research R2 da spec 001), antes de o
    servidor web subir.
  - O Boot só publica `ReadinessState.ACCEPTING_TRAFFIC` no `ApplicationReadyEvent`, depois do servidor
    e dos runners. Antes disso, o estado é `REFUSING_TRAFFIC`, e a readiness responde 503.
  - Logo, a ordem "resumo da carga → pronta" é garantida pela ordem do ciclo de vida, sem código novo.
- **Como testar (Princípio I):**
  - `ReadinessAfterLoadIT` roda a aplicação real com `SpringApplication` (como o `ApplicationStarter`)
    e registra um `ApplicationListener<AvailabilityChangeEvent<ReadinessState>>` na própria
    `SpringApplication`. No momento do evento `ACCEPTING_TRAFFIC`, o ouvinte guarda a saída capturada até
    ali, e o teste confere que ela já tem o resumo da carga. O ouvinte também confere que o estado antes
    do evento era `REFUSING_TRAFFIC`. Não é dublê: é um ouvinte real de um evento real;
  - o 503 é provado por HTTP: o teste publica `AvailabilityChangeEvent` com `REFUSING_TRAFFIC` (e
    `LivenessState.BROKEN`) pela API pública do Spring, chama os probes e restaura o estado, num contexto
    próprio com `@DirtiesContext`.
- **Motivo:** no modo padrão, a carga é síncrona e termina antes do servidor. Um indicador próprio só
  repetiria o que o ciclo de vida já garante (Princípio V).
- **Marco 3:** com a carga por Kafka, assíncrona, a readiness vai precisar de um estado explícito (por
  exemplo, um `AvailabilityChangeEvent` publicado pelo consumidor ao receber a mensagem de carga
  concluída). Fica registrado como ponto de extensão, sem código agora.
- **Alternativas rejeitadas:** `HealthIndicator` próprio no grupo `readiness`, com uma flag de "carga
  concluída": código e estado a mais para a mesma garantia.

## R3. Métricas no formato do Prometheus

- **Decisão:**
  - métrica `http.server.requests` do Micrometer, que o Spring MVC já registra, exposta como
    `http_server_requests_seconds_{count,sum,bucket}`, com os rótulos `method`, `uri` (modelo de rota),
    `status`, `outcome` (`SUCCESS`, `CLIENT_ERROR`, `SERVER_ERROR`...) e `exception`/`error` (FR-013);
  - histograma ligado só para essa métrica
    (`management.metrics.distribution.percentiles-histogram.http.server.requests=true`), com
    `minimum-expected-value=1ms` e `maximum-expected-value=10s` para limitar o número de faixas (FR-014);
  - rótulo `application` com `spring.application.name` em todas as séries (FR-016), por
    `management.metrics.tags.application`;
  - métricas da JVM e do processo (`jvm_memory_*`, `jvm_threads_*`, `process_uptime_seconds`) vêm do
    Actuator sem configuração (FR-016).
- **Rotas desconhecidas (FR-015):** o rótulo `uri` vem do padrão da rota que atendeu. Um caminho
  inexistente cai no tratador de recursos estáticos (por causa do `swagger-ui.html` e do
  `openapi.yaml`) ou em nenhum tratador; nos dois casos o valor é fixo (`/**`, `NOT_FOUND` ou
  `UNKNOWN`). O teste não fixa o valor: confere que N caminhos inexistentes diferentes produzem uma única
  série, com `status="404"`. Esse valor pode ser o mesmo dos recursos estáticos que existem, e o status
  distingue os dois casos (FR-015).
- **304 (US3-6):** o filtro de observação do Spring tem precedência alta e envolve o filtro de ETag
  (R4), então vê o status final, 304, com `outcome=REDIRECTION`. O teste confere a série com
  `status="304"`. "Conta como sucesso" quer dizer que não entra na taxa de erro, que é calculada sobre
  `outcome` igual a `CLIENT_ERROR` ou `SERVER_ERROR` (data-model, "Série de métrica").
- **Verificado na etapa 2:** `management.metrics.tags.application` funciona no Boot 4.1.2, e o rótulo
  aparece em todas as séries. O `MeterRegistryCustomizer` não foi necessário. O 304 aparece com
  `outcome="REDIRECTION"`, e uma rota inexistente cai num único valor de `uri` (`MetricsIT`).
- **Alternativas rejeitadas:** percentis calculados na aplicação (`percentiles=0.95,0.99`): não se
  agregam entre instâncias e não servem para o SLO por faixa do Marco 4.

## R4. ETag derivado do conteúdo, por filtro

> **Atualização (etapa 1, 2026-11-23):** a decisão original, abaixo, usava o `ShallowEtagHeaderFilter`
> do Spring. Na implementação, o `IntervalsEtagIT` mostrou que ele **não trata `If-None-Match: *`**:
> com o Spring 7 do Boot 4.1.2, a resposta é 200 com corpo. A spec (FR-003) e a RFC 9110 (seção
> 13.1.2) exigem 304, porque `*` casa com qualquer representação atual. Todos os outros casos (valor
> único, lista, forma fraca, malformado, 400 e 405 sem ETag) funcionaram como previsto.
>
> **Decisão corrigida:** um filtro próprio, `ContentEtagFilter`, em `web/`, registrado pelo
> `EtagConfiguration` nos mesmos dois caminhos. Ele faz o mesmo que o filtro do Spring: guarda o corpo
> com o `ContentCachingResponseWrapper`, calcula o ETag forte `"0" + MD5`, só em respostas 2xx de GET e
> HEAD. A comparação usa as classes do próprio Spring (`ETag.parse` e `ETag.compare`, com a comparação
> fraca do `If-None-Match`), e o `*` é tratado explicitamente. O formato do ETag não muda.
>
> *Alternativas rejeitadas:* estender o `ShallowEtagHeaderFilter`, cujo método que decide o 304 é
> privado; tirar o `*` da spec, que contrariaria a RFC 9110.

**Decisão original:**

- **Decisão:** registrar o `ShallowEtagHeaderFilter` do Spring num `FilterRegistrationBean`, só para
  `/api/producers/intervals` e `/api/movies`.
  - O filtro guarda o corpo, calcula o MD5 e põe `ETag: "0<hex>"` (forte) nas respostas 2xx de GET.
  - Ele usa `checkNotModified` do Spring, que trata `If-None-Match` com lista, forma fraca (`W/`) e `*`
    pela comparação fraca da RFC 9110, e responde 304 sem corpo com o mesmo `ETag` (FR-003).
  - O filtro só age em respostas 2xx: só elas ganham ETag, e só elas podem virar 304. Por isso, 400 e
    405 ficam sem ETag e nunca viram 304 (FR-006).
- **Resposta 500 (decisão de 2026-11-23, `docs/ai/decisoes.md`):** não há requisito próprio. A API não
  tem caminho que produza 500 sem um defeito ou um mock, então um requisito sobre ele não teria
  comprovação (Princípio VI). Mesmo assim, o comportamento continua definido pelo próprio Spring:
  - o filtro de ETag não age numa resposta 500, porque ela não é 2xx;
  - as métricas de requisição (R3) registram qualquer status, inclusive 500, com
    `outcome="SERVER_ERROR"`, porque o filtro de observação do Spring vê o status final de toda
    requisição.
- **Determinismo (FR-002):** o corpo depende só dos dados e dos parâmetros, e as duas respostas são
  ordenadas (spec 001, FR-015 e FR-022). Mesmo arquivo → mesmo corpo → mesmo ETag, inclusive entre
  execuções. A listagem tem um ETag por combinação de parâmetros, porque o corpo muda.
- **Motivo:** os dados não mudam durante a execução e os corpos são pequenos (poucos KB). O custo de
  gerar o corpo para calcular o hash é irrelevante, e não há estado novo nem código no controller.
- **Alternativas rejeitadas:**
  - ETag calculado no controller a partir de uma "versão" dos dados: exigiria guardar a versão, e cada
    combinação de parâmetros ainda precisaria de um hash próprio;
  - ETag fraco: o forte é mais estrito e funciona igual com a comparação fraca do `If-None-Match`;
  - `Cache-Control` com `max-age`: muda o comportamento dos clientes e não foi pedido (spec, premissas).
- **HEAD:** o Spring MVC atende HEAD nos mapeamentos GET, e o filtro também põe o ETag nele. Não muda
  a FR-023a da spec 001, que trata de POST, PUT e DELETE.

## R5. Contrato OpenAPI do Marco 2

- **Decisão:** o contrato passa a ter como fonte `specs/002-operacao/contracts/openapi.yaml`, cópia do
  contrato do Marco 1 com:
  - o cabeçalho `ETag` nas respostas 200 dos dois endpoints;
  - o parâmetro `If-None-Match` nos dois endpoints;
  - a resposta 304, sem corpo, com `ETag`;
  - `info.version` 1.1.0.

  Os schemas dos corpos não mudam (FR-005, Princípio III). O `static/openapi.yaml` é substituído por
  essa cópia, e o `ApiDocsIT` passa a compará-lo com o arquivo da spec 002. O contrato da spec 001 fica
  como registro do Marco 1.
- **Endpoints do Actuator fora do contrato:** são operacionais, não de negócio. Ficam descritos em
  [contracts/operational-endpoints.md](contracts/operational-endpoints.md), e o `ApiDocsIT` continua
  exigindo só os dois caminhos de negócio.
- **Validação:** o `ContractValidator` passa a validar também a resposta 304 (sem corpo).
- **Alternativa rejeitada:** editar o contrato da spec 001: apagaria o registro do que o Marco 1
  entregou, e a spec 001 descreve esse marco.

## R6. Logs: texto no `./mvnw`, JSON por variável de ambiente

- **Decisão:** logging estruturado nativo do Spring Boot, ligado pela variável
  `LOGGING_STRUCTURED_FORMAT_CONSOLE`, que o relaxed binding do Boot mapeia para
  `logging.structured.format.console`.
  - Sem a variável, nada muda: o console usa o padrão de texto do framework, como no Marco 1.
  - Com `LOGGING_STRUCTURED_FORMAT_CONSOLE=ecs`, cada evento sai como uma linha JSON no formato Elastic
    Common Schema.
  - Nada é acrescentado ao `application.yml` para o formato, então o log do modo padrão continua o
    texto do Marco 1.
- **Banner desligado (decisão de 2026-11-23, `docs/ai/decisoes.md`):** `spring.main.banner-mode: off`
  no `application.yml`, em todas as execuções. O banner é escrito direto no stdout, fora do logging, e
  não passa pelo formato estruturado. Com o JSON ligado, o desenho ASCII seriam linhas que não são JSON,
  e o `JsonLogFormatIT` e o passo 5 do teste de fumaça falhariam. O banner não traz informação para a
  operação nem para a avaliação.
  - *Alternativas rejeitadas:* `banner-mode: log`, que tornaria o banner um evento de log, com desenho
    ASCII dentro da `message` do JSON; e desligar só na imagem, que deixaria o JSON quebrado em qualquer
    outra execução com a variável, contra a FR-020.
  - *Verificado na etapa 2:* com o logging estruturado ligado, o Boot 4.1.2 **já não escreve o banner**
    (0 ocorrências na execução `json-logs`, contra 68 na execução em texto). A premissa de que o banner
    quebraria o JSON não vale para esta versão. A decisão continua valendo pelo motivo principal, o de
    que o banner não traz informação: o efeito real do `banner-mode: off` é tirá-lo do log em texto do
    `./mvnw`, provado pelo `TextLogFormatIT`. No JSON, o `JsonLogFormatIT` protege contra uma mudança de
    comportamento do framework.
- **Formato ECS:** `@timestamp` (ISO-8601 em UTC), `log.level`, `log.logger`, `process.thread.name`,
  `message`, `service.name` (vem de `spring.application.name`) e, nas exceções, `error.type`,
  `error.message` e `error.stack_trace` no mesmo evento (FR-017). Mapeamento completo em
  [data-model.md](data-model.md).
  - *Verificado na etapa 2:* o Boot 4.1.2 escreve esses campos **aninhados**, e não com a chave
    pontuada. Por exemplo, `{"@timestamp":"…Z","log":{"level":"INFO","logger":"…"},"process":{"pid":…,
    "thread":{"name":"main"}},"service":{"name":"awards"},"message":"…","ecs":{"version":"8.11"}}`. Os
    nomes desta research são os caminhos ECS (`log.level` = `log` → `level`). O `LogOutput` dos testes
    lê os dois jeitos.
- **Motivo:**
  - é o mecanismo do próprio framework (Boot 3.4 em diante), sem encoder do Logback nem dependência
    nova;
  - uma variável que o próprio Boot entende dispensa uma propriedade intermediária no `application.yml`;
  - o ECS é um esquema aberto, com nomes de campo fixos, que os coletores já conhecem.
- **Alternativas rejeitadas:**
  - formato `logstash` ou `gelf`: também nativos, mas o ECS traz `service.name` e é o esquema documentado
    de um coletor amplo;
  - `logstash-logback-encoder`: dependência a mais para o mesmo resultado;
  - variável própria (`LOG_FORMAT`) mapeada no `application.yml`: mais uma indireção, e um valor vazio
    no mapeamento exigiria tratamento à parte;
  - formato por perfil do Spring: rejeitado pelo responsável (`docs/ai/decisoes.md`, 2026-11-23).

### Testes nos dois formatos (FR-018)

- **Problema:** o Logback é global na JVM. Cada contexto do Spring reconfigura o log ao subir, e os
  contextos ficam em cache entre as classes de teste. Ligar o JSON num `@SpringBootTest` dentro da mesma
  JVM mudaria o formato dos testes seguintes, de forma dependente da ordem.
- **Decisão:** uma **segunda execução do Failsafe**, `json-logs`, numa JVM própria, com
  `environmentVariables` `LOGGING_STRUCTURED_FORMAT_CONSOLE=ecs`.
  - Ela roda as classes que verificam o log, marcadas com `@Tag("log")`: os ITs de `ingestion/` que usam
    o `LoaderLog`, o `StartupFailureIT` e os ITs de formato de log.
  - Cada execução tem o seu `reportsDirectory` e o seu `summaryFile`, e as duas passam pelo goal
    `verify`.
  - Assim, "as mesmas verificações passam com o JSON ligado" vale de forma literal: são os mesmos testes.
- **Leitura do log independente do formato:** os testes passam a ler o log por uma classe de apoio,
  `LogOutput`, que transforma a saída capturada em eventos (nível, logger, mensagem e pilha):
  - uma linha que começa com `{` é lida como JSON ECS;
  - as demais linhas, como o texto padrão do Boot.

  O `LoaderLog` passa a usar o `LogOutput` e a devolver as **mensagens** dos avisos, e não as linhas
  cruas. Isso mantém os `endsWith("Linha N rejeitada: ...")` do Marco 1, que falhariam no JSON por causa
  do `"}` no fim da linha. O `StartupFailureIT` passa a verificar o texto decodificado
  (`Result.messages()`), porque no JSON as aspas da mensagem de separador (`";"`) aparecem escapadas.
- **Testes próprios do formato:**
  - `TextLogFormatIT`, só na execução padrão (`@DisabledIfEnvironmentVariable`): o log da
    inicialização está no padrão de texto do Boot, nenhuma linha é JSON e o banner não aparece;
  - `JsonLogFormatIT`, só na execução `json-logs` (`@EnabledIfEnvironmentVariable`): toda linha
    capturada é um JSON válido com os campos obrigatórios, e uma mensagem com várias linhas (o relatório
    do `FailureAnalyzer`) fica num único evento.
- **Falha de inicialização (US4-3, ajustada em 2026-11-23):** o evento JSON da falha traz a mensagem de
  erro; o rastro da exceção só aparece quando existe. Na falha por arquivo, o `MovieFileFailureAnalyzer`
  trata a exceção, e o Boot registra só o relatório do analisador, sem pilha: é a mensagem clara do
  Marco 1, que substitui o rastro. O `JsonLogFormatIT`, com o arquivo ausente, confere que o relatório
  (descrição e ação, em várias linhas) está na `message` de um único evento de nível `ERROR` e que
  nenhuma linha da saída fica fora de um evento JSON. Quando um evento tiver exceção, o rastro vai em
  `error.stack_trace` do mesmo evento, pelo próprio formato ECS (FR-017). Não é preciso provocar uma
  falha sem analisador só para ter pilha.
- **FR-019:** o projeto não registra cabeçalhos de requisição (não há log de acesso, spec, premissas). O
  `JsonLogFormatIT` e o `TextLogFormatIT` enviam um `Authorization` com um valor marcador e conferem que
  ele não aparece na saída.

## R7. Cobertura com JaCoCo sobre o Failsafe

- **Decisão:** `jacoco-maven-plugin` 0.8.14 no `pom.xml`, com:
  - `prepare-agent-integration`, que põe o agente no `argLine` do Failsafe e grava
    `target/jacoco-it.exec`. As duas execuções do Failsafe acumulam no mesmo arquivo (`append`, o
    padrão);
  - `report-integration`, que gera `target/site/jacoco-it/` em HTML, XML e CSV;
  - `check` sobre o `jacoco-it.exec`, regra `BUNDLE`, contador `LINE`, `COVEREDRATIO` mínimo `0.90`
    (FR-028a), na fase `verify`, depois do `verify` do Failsafe.
- **Medição de referência:** 97,06% de linhas (330 de 340) no commit `39b3188`, com essa mesma
  versão, chamada pela linha de comando (`docs/ai/decisoes.md`, 2026-11-23).
- **Motivo:** a 0.8.14 é a versão que instrumenta classes do Java 25 e já foi usada na medição. O
  `prepare-agent-integration` evita mexer no `argLine` à mão, e o `check` no `verify` faz o mínimo
  valer também no `./mvnw verify` local, sem Docker (FR-028a).
- **Cuidado:** o Failsafe não pode definir `argLine` próprio sem incluir `@{argLine}`, ou o agente se
  perde. Hoje ele não define.
- **Verificado na etapa 4:** com `-Djacoco.line.minimum=0.99`, o `check` falha ("lines covered ratio is
  0.97, but expected minimum is 0.99"). Com o padrão, passa. A cobertura das duas execuções juntas é de
  **97,28%** (358 de 368 linhas), e as linhas não cobertas são as mesmas do Marco 1.
- **Teste local do `check-workflows.sh`:** feito com o `yq` v4.54.3, baixado da release oficial para o
  scratchpad, com o SHA-256 conferido contra o arquivo `checksums` da mesma release. Oito fixtures fora do
  repositório (um caso válido e um inválido por regra) se comportaram como esperado.

## R8. Imagem de container

- **Decisão:** `Dockerfile` multi-stage na raiz.
  - **Build:** `eclipse-temurin:25-jdk-alpine`, fixada por digest. Copia `mvnw`, `.mvn/` e `pom.xml`,
    baixa as dependências (`dependency:go-offline`, numa camada própria e com cache do BuildKit em
    `/root/.m2`), copia `src/main` e roda `sh ./mvnw -B -Dmaven.test.skip=true package`. Os testes não
    rodam no build da imagem: o pipeline já os roda no estágio de testes. Depois, extrai o JAR em camadas
    (`java -Djarmode=tools -jar app.jar extract --layers --launcher`).
  - **Execução:** `eclipse-temurin:25-jre-alpine`, fixada por digest. É a menor imagem JRE oficial da
    Temurin e tem `sh` e o `wget` do BusyBox, que o healthcheck usa. Copia só as camadas extraídas.
  - **Usuário:** grupo e usuário `app`, UID e GID 10001, sem shell de login. `USER 10001:10001` (FR-022).
  - **Variáveis:** `PORT=8080` e `LOGGING_STRUCTURED_FORMAT_CONSOLE=ecs` (FR-024a). O `MOVIES_CSV_PATH`
    não é definido, para valer o padrão embarcado (FR-024).
  - **Memória:** `-XX:MaxRAMPercentage=75` vai direto no `ENTRYPOINT`, e **não** em
    `JAVA_TOOL_OPTIONS`. Com essa variável definida, a JVM sempre escreve
    `Picked up JAVA_TOOL_OPTIONS: ...` no stderr, fora do log, e essa linha não é JSON (SC-005, passo 5
    do teste de fumaça; achado H1 da segunda análise). Quem rodar o container e definir
    `JAVA_TOOL_OPTIONS` por conta própria recebe essa linha por escolha própria.
  - **Healthcheck:** `HEALTHCHECK --interval=10s --timeout=3s --start-period=30s --retries=3`, com
    `wget -q -O /dev/null "http://127.0.0.1:${PORT}/actuator/health/readiness"` em forma shell, para
    expandir o `PORT` (FR-023).
  - **Processo:** `ENTRYPOINT` em forma exec
    (`["java", "-XX:MaxRAMPercentage=75", "org.springframework.boot.loader.launch.JarLauncher"]`),
    então a JVM é o PID 1 e recebe o `SIGTERM` direto. O desligamento gracioso do servidor é o padrão do
    Boot desde a 3.4 (FR-025). Na falha de inicialização, a JVM termina com código 1.
- **`.dockerignore` por lista de permissão:** ignora tudo (`*`) e libera só `mvnw`, `.mvn/`, `pom.xml` e
  `src/main/`. Assim, `target/`, `.git/`, `docs/`, `specs/`, `src/test/` e qualquer arquivo novo ficam
  fora do contexto por padrão (FR-026).
- **Motivo:**
  - Alpine com JRE é a menor imagem oficial que ainda tem shell para o healthcheck com `PORT`;
  - o digest torna o build reprodutível, e o Dependabot o atualiza (R13);
  - as camadas separam dependências do código, e o rebuild depois de uma mudança no código é rápido.
- **Alternativas rejeitadas:**
  - distroless (`java25`, `nonroot`): não tem shell nem `wget`, e o healthcheck exigiria uma segunda JVM
    a cada verificação, ou um binário a mais;
  - runtime gerado por `jlink`: imagem menor, mas a lista de módulos precisa de manutenção a cada
    dependência nova;
  - Buildpacks (`spring-boot:build-image`): não produzem o Dockerfile pedido e exigem Docker no build
    do Maven;
  - rodar `./mvnw verify` dentro do build da imagem: duplicaria os testes do pipeline e deixaria o build
    lento.
- **Verificar:** se o `wget` do BusyBox da imagem base atual responde código diferente de zero para um
  503, que é o que faz o healthcheck falhar enquanto a aplicação não está pronta.

## R9. Estrutura do pipeline

- **Decisão:** um workflow, `.github/workflows/ci.yml`, em `push` (todos os branches) e `pull_request`.
  - `permissions: contents: read` no topo. Só o job de CodeQL ganha `security-events: write`.
  - `concurrency` por workflow e ref, com `cancel-in-progress: true`.
  - `timeout-minutes` em todo job.
- **Jobs:**

  | Job | O que faz | Comprova |
  | --- | --- | --- |
  | `workflow-policy` | `check-workflows.sh`: todo `uses:` termina em `@<40 hex>`; `permissions` do topo é `contents: read`; só o `codeql` tem `security-events: write`; nenhuma referência a `secrets.`; nenhum `push: true` nem `docker push`; `dependabot.yml` com os três ecossistemas | FR-028b, FR-029, SC-007 |
  | `build` | `setup-java` (Temurin 25, `cache: maven`) e `./mvnw -B verify`, com as duas execuções do Failsafe e o `check` do JaCoCo. Publica `target/site/jacoco-it/` e os relatórios do Failsafe como artefatos e escreve o total de cobertura no resumo do job | FR-028 (build, testes, cobertura), FR-028a (cobertura), FR-027, SC-002 |
  | `codeql` | análise do código Java e porta de severidade (R10) | FR-028, FR-028a (segurança do código) |
  | `dependencies` | Trivy no `pom.xml`, relatório completo e porta (R10) | FR-028a (dependências) |
  | `dockerfile-lint` | hadolint (R11) | FR-028, SC-008 |
  | `image` | build da imagem, Trivy na imagem e teste de fumaça (R14) | FR-021 a FR-026, FR-028, SC-005, SC-006 |

  O `image` depende do `dockerfile-lint`. Os demais rodam em paralelo.
- **Motivo:** jobs separados mostram no PR qual portão falhou (SC-008) e rodam em paralelo. Um único
  workflow mantém o portão de merge num só lugar.
- **Fork:** num PR vindo de fork, o token é só de leitura, e o envio do SARIF ao code scanning pode
  falhar. O envio é condicionado a não ser fork, e a porta de severidade continua rodando sobre o SARIF
  local.

## R10. Análise de segurança

- **Código (CodeQL):**
  - `github/codeql-action` `init` e `analyze`, linguagem `java-kotlin`, `build-mode: none` (sem
    compilar), suíte `security-extended`;
  - o `analyze` grava o SARIF localmente (`output`), e um passo com `jq` falha o job se algum resultado
    tiver regra com `security-severity` ≥ 7,0, que é a faixa alta (7,0 a 8,9) e crítica (9,0 a 10) do
    GitHub. Achados abaixo disso ficam no code scanning e no resumo, sem reprovar (FR-028a).
- **Dependências e imagem (Trivy):**
  - dependências: `trivy fs` sobre o repositório, com o analisador de `pom.xml`, que resolve as
    dependências transitivas;
  - imagem: `trivy image` sobre a imagem recém-construída, carregada no Docker local, sem publicação;
  - para cada alvo, duas execuções:
    - **relatório:** todas as severidades, inclusive sem correção, em tabela no resumo do job, sem
      reprovar;
    - **porta:** `--severity HIGH,CRITICAL --ignore-unfixed --exit-code 1`, que considera só as
      vulnerabilidades com versão corrigida (FR-028a);
  - o banco de vulnerabilidades do Trivy fica em cache entre execuções (`actions/cache`, chave por dia),
    para não depender do limite de download a cada job.
- **Motivo:** o CodeQL é a análise de código nativa do GitHub, sem segredo. O Trivy cobre dependências e
  imagem com a mesma ferramenta e a mesma regra de "só com correção" (`--ignore-unfixed`).
- **Alternativas rejeitadas:**
  - OWASP Dependency-Check: lento e precisa de uma chave da API do NVD, um segredo;
  - `dependency-review-action`: só roda em PR e não filtra por correção disponível;
  - Grype: equivalente ao Trivy, mas exigiria duas ferramentas (Syft e Grype) para o mesmo resultado.
- **Cadeia de entrega e referências aninhadas (achado M1 da segunda análise):**
  - *Contexto:* em 2026, o ataque à cadeia do Trivy atingiu o `aquasecurity/setup-trivy`, que o
    `trivy-action` usa por dentro. Fixar por hash só o `uses:` do nosso workflow não basta: uma action
    composta pode chamar outras por tag, e o `check-workflows.sh` só enxerga o nosso arquivo.
  - *Verificação (2026-11-23), no repositório oficial:*
    - `aquasecurity/trivy-action` **v0.36.0** (release de 2026-04-22): o `action.yaml` referencia
      `aquasecurity/setup-trivy@3fb12ec12f41e471780db15c232d5dd185dcb514` (v0.2.6) e
      `actions/cache@27d5ce7f107fe9357f9df03efb73ab90386fccae` (v5.0.5). Os dois estão por hash;
    - `setup-trivy` nesse hash: referencia `actions/cache/restore` e `actions/cache/save`
      (`9255dc7a253b0ccc959486e2bca901246202afeb`, v5.0.1) e `actions/checkout`
      (`8e8c483db84b4bee98b60c0593521ed34d9990e8`, v6.0.1) por hash. Ele busca o `contrib/install.sh`
      do Trivy num commit fixo (`ref: 75c4dc0f45c5d7ffd05ae26df1e0c666787bdf2a`);
    - `install.sh` nesse commit: baixa o arquivo de checksums da release e confere o SHA-256 do binário
      (`hash_sha256_verify`) antes de instalar.
  - *Decisão:* manter a `aquasecurity/trivy-action`, fixada pelo hash do commit da v0.36.0 (ou de uma
    versão posterior com a mesma propriedade), porque toda a cadeia aninhada está por hash e o binário
    tem o checksum conferido.
  - *Condição de troca:* se uma versão escolhida (na etapa 4 ou num PR do Dependabot) tiver qualquer
    referência aninhada por tag ou branch, a action sai, e o Trivy passa a ser instalado direto do
    binário oficial da release, com o checksum conferido contra o `trivy_<versão>_checksums.txt` da
    mesma release, num passo `run:` do workflow.
  - *Revisão contínua:* todo PR do Dependabot que atualize a `trivy-action` (ou qualquer action
    composta) repete essa verificação antes do merge (R13).
- **Verificar:** que o repositório no GitHub é público. Em repositório privado, o code scanning exige o
  GitHub Advanced Security.

## R11. Lint do Dockerfile

- **Decisão:** `hadolint/hadolint-action`, com `failure-threshold: warning`. Avisos e erros reprovam;
  `info` e `style` só aparecem.
- **Motivo:** os avisos do hadolint (por exemplo, versão de pacote não fixada ou `cd` em vez de
  `WORKDIR`) são exatamente os problemas que o lint existe para barrar. Uma exceção pontual vai como
  `# hadolint ignore=DLxxxx` na linha, com o motivo num comentário.

## R12. Actions fixadas por hash

- **Decisão:** toda action é referenciada por `owner/repo@<sha de 40 caracteres> # vX.Y.Z`.
  - O hash é o do commit da tag, resolvido no repositório oficial (`gh api repos/<owner>/<repo>/commits/<tag>`,
    que já desreferencia tags anotadas), e não o objeto da tag.
  - O comentário com a versão é o formato que o Dependabot atualiza junto com o hash.
  - O job `workflow-policy` impede que uma referência por tag ou branch entre sem ser notada. O mesmo
    script comprova pelo pipeline o resto da FR-028b (permissões, nenhum segredo, nenhuma publicação) e
    a FR-029 (configuração do Dependabot), como manda o Princípio VI para requisitos de pipeline. Antes,
    essas partes ficavam para a revisão do PR, que não é a forma de comprovação desse tipo de requisito
    (achado C1 do `/speckit-analyze`).
- **Actions previstas:** `actions/checkout`, `actions/setup-java`, `actions/upload-artifact`,
  `actions/cache`, `github/codeql-action/init`, `github/codeql-action/analyze`,
  `docker/setup-buildx-action`, `docker/build-push-action`, `hadolint/hadolint-action` e
  `aquasecurity/trivy-action`. As versões são as estáveis mais recentes na data da etapa 5.
- **Referências aninhadas:** o hash do nosso `uses:` não fixa o que uma action composta chama por
  dentro. Para cada action composta (hoje, a `trivy-action`), o `action.yml` da versão escolhida é lido
  e as referências internas precisam estar por hash (R10).
- **Versões fixadas na etapa 4 (2026-11-24, API pública do GitHub):**

  | Action | Versão | Hash do commit |
  | --- | --- | --- |
  | `actions/checkout` | v7.0.1 | `3d3c42e5aac5ba805825da76410c181273ba90b1` |
  | `actions/setup-java` | v6.3.0 | `d1374f8ac74474200d16ef8ecee22c7ae7a51880` |
  | `actions/upload-artifact` | v7.0.2 | `cf430e030ddbb5b0abf93d22962f4752f3646cd9` |
  | `github/codeql-action` (`init`, `analyze`) | v4.39.4 | `1bd86f2fdb4b86187fa4059f16f31a3202acc9ea` |
  | `docker/setup-buildx-action` | v4.4.1 | `f87e5991a6d7451dcb8d9637bfbc97413f497069` |
  | `docker/build-push-action` | v7.4.0 | `c3c9e263c25d99ce0380d002d59b67737d91b0dc` |
  | `hadolint/hadolint-action` | v3.5.0 | `06be81baf89a55ffd0e24b8f04a4185738dd3387` |
  | `aquasecurity/trivy-action` | v0.36.0 | `ed142fd0673e97e23eac54620cfb913e5ce36c25` |

  - O `actions/cache` não é usado direto: a `trivy-action` já guarda o banco em cache, pela referência
    aninhada por hash (R10).
  - A "release mais recente" do `github/codeql-action` é um bundle do CodeQL (`codeql-bundle-v…`), e
    não uma versão da action. Foi usada a maior tag `vX.Y.Z`.
  - **Referência aninhada da `hadolint-action`:** é uma action Docker, e o `Dockerfile` dela, na v3.5.0,
    usa `ghcr.io/hadolint/hadolint:v2.15.1-debian@sha256:9a3944b7…9e27`, fixada por digest.
- **Ferramenta de consulta:** o `gh` não está instalado na máquina de desenvolvimento atual. A mesma
  consulta funciona pela API pública, sem autenticação:
  `curl -s https://api.github.com/repos/<owner>/<repo>/commits/<tag> | jq -r .sha`.
- **Imagens:** pelo mesmo motivo, as imagens base do Dockerfile são fixadas por digest (R8).

## R13. Dependabot

- **Decisão:** `.github/dependabot.yml`, versão 2, com três ecossistemas na raiz (`/`), semanalmente:
  `maven`, `github-actions` e `docker`.
  - As actions ficam num grupo só, para um PR por semana.
  - As dependências Maven ficam agrupadas por atualização `minor` e `patch`, e as `major` vêm em PR
    próprio.
  - Os PRs do Dependabot passam pelo mesmo pipeline e pela mesma revisão de qualquer PR. Num PR que
    atualize uma action composta, a revisão confere também as referências aninhadas da versão nova
    (R10).
- **Motivo:** cobre exatamente os três ecossistemas da FR-029. O ecossistema `docker` atualiza o digest
  das imagens base do `Dockerfile`. Nenhum script de `.github/scripts/` tem imagem própria: a sonda do
  `check-docker-context.sh` reusa a imagem do estágio de build (R14), então não há digest fora do
  alcance do Dependabot.

## R14. Teste de fumaça do container

- **Decisão:** um script, `.github/scripts/smoke-test.sh` (bash, `set -euo pipefail`), chamado pelo job
  `image`, com a imagem construída e carregada localmente:
  1. sobe o container com `-p 8080:8080` e espera o estado `healthy` do healthcheck (até 90 s);
  2. chama `GET /api/producers/intervals` e compara com `jq` com o resultado de referência (Joel Silver
     1990–1991 e Matthew Vaughn 2002–2015, sem nenhum item a mais);
  3. repete com `If-None-Match` igual ao `ETag` recebido e exige 304;
  4. confere que `id -u` dentro do container não é 0 (FR-022);
  5. confere que toda linha do `docker logs` é JSON (`jq -e .`) (FR-024a, SC-005);
  6. `docker stop` com tempo-limite de 30 s e mede: o container precisa parar em menos de 10 s (FR-025);
  7. sobe outro container com `PORT=9090`, espera o `healthy` e chama a API nessa porta (FR-024);
  8. sobe um container com `MOVIES_CSV_PATH=file:/nao-existe.csv` e exige que ele termine com código
     diferente de zero e com a mensagem do Marco 1 no log (FR-025);
  9. monta um CSV de cenário (`src/test/resources/scenarios/disjoint-producers.csv`, de produtores e anos
     disjuntos do arquivo fornecido) somente leitura e confere o resultado do cenário (US5-5);
  10. confere que a imagem final não tem `/app/src`, `/build`, `/app/pom.xml`, `mvn` nem `javac`
      (FR-021, US5-1);
  11. roda o `check-docker-context.sh`, que constrói uma imagem descartável com o mesmo contexto e o
      mesmo `.dockerignore` e confere que só `mvnw`, `.mvn/`, `pom.xml` e `src/main/` entram (FR-026).
      A base dessa sonda é a imagem do estágio de build, lida do `Dockerfile`, para que o digest
      atualizado pelo Dependabot (R13) valha também para ela;
  12. sobe um container com `LOGGING_STRUCTURED_FORMAT_CONSOLE` vazio e confere o log em texto (FR-024a);
  13. monta uma cópia do CSV com `chmod 000` e exige a falha de arquivo ilegível (caso de borda,
      FR-025).

  Os containers e os arquivos temporários são removidos ao fim, mesmo com falha (`trap`).
- **Portabilidade (Windows e Linux):** o script roda no runner Linux e também localmente, no Linux e no
  Git Bash do Windows com Docker Desktop. No Git Bash, ele exporta `MSYS_NO_PATHCONV=1` e converte o
  caminho de cada bind mount com `cygpath -w`.
- **Passo 13 e permissões:** a permissão de arquivo do host nem sempre vale dentro do container. O
  `chmod 000` não tem efeito no NTFS pelo Git Bash, e o Docker Desktop pode expor os bind mounts com
  permissão aberta. Por isso, o passo começa com uma sonda: um container com o usuário 10001 tenta ler o
  arquivo montado.
  - Se a sonda não lê, a permissão vale, e o passo roda.
  - Se a sonda lê, o passo é **pulado** fora do pipeline, com a mensagem `PULADO`, o motivo, o sistema
    (`uname -s`) e a versão do Docker.
  - No pipeline (`CI=true`), no runner Linux, a permissão precisa valer, então uma sonda que lê é
    **falha**. O caso de borda nunca fica sem prova no pipeline.

  O critério é o comportamento observado (a sonda), e não o nome do sistema. Um ambiente Linux em que a
  permissão não valha também é detectado.
- **Motivo:** cobre os cenários da história 5 que só existem no container, sem nada disso no `./mvnw
  verify` (FR-027). O resultado de referência vem do enunciado, e não de outra execução.
- **SC-006:** o job registra o tempo entre o fim do build e a resposta correta, e o script falha se
  passar de 2 minutos.

## R15. ADRs

- **Decisão:** `docs/adr/`, com:
  - `README.md` de índice (número, título e status);
  - um arquivo `NNNN-titulo-curto.md` por decisão, numerado a partir de `0001`;
  - em cada ADR: título, status (`Aceito`, `Substituído por NNNN`), data, contexto, decisão,
    alternativas consideradas, consequências e a citação da entrada em `docs/ai/decisoes.md` (FR-031).
- **Lista (FR-030):**

  | Nº | Decisão | Origem |
  | --- | --- | --- |
  | 0001 | Stack: Java 25, Spring Boot 4.1.2, Maven com wrapper | plano 001, R1 (entrada nova) |
  | 0002 | H2 com JPA e esquema por Flyway, SQL portável | plano 001 (entrada nova) |
  | 0003 | Carga do CSV na inicialização: falha rápida nos erros estruturais e linha inválida ignorada | decisoes.md, 2026-11-22 (T082) |
  | 0004 | Somente testes de integração no back-end, sem Mockito | emenda 1.2.0 e "Mockito excluído" |
  | 0005 | Contrato OpenAPI estático como fonte única, Swagger UI pelo webjar | decisoes.md, R8 e bolt 6 |
  | 0006 | Erros em Problem Details (RFC 9457) | plano 001 (entrada nova) |
  | 0007 | ETag derivado do conteúdo (`ContentEtagFilter`, no lugar do `ShallowEtagHeaderFilter`) | R4 e decisoes.md, 2026-11-23 |
  | 0008 | Liveness e readiness pelo Actuator, com a readiness após a carga | R1, R2 (entrada nova) |
  | 0009 | Métricas no formato do Prometheus, com histograma de latência | R3 (entrada nova) |
  | 0010 | Log em texto no `./mvnw` e JSON por variável, ligado na imagem | emenda 1.3.0 |
  | 0011 | Imagem multi-stage, JRE Alpine por digest, usuário não root | R8 (entrada nova) |
  | 0012 | Pipeline com actions por hash, portas de segurança e cobertura, Dependabot | decisoes.md (critérios do pipeline) e R9 a R13 |

  "Entrada nova" quer dizer que a entrada em `docs/ai/decisoes.md` é criada junto com o ADR (FR-031).
- **Motivo:** formato curto, no estilo de Michael Nygard, com as alternativas, que é o que o avaliador
  procura. A comprovação é a seção "Revisão dos ADRs" do checklist da spec (Princípio VI, 1.4.0).

## R16. Um bolt, com etapas por assunto

- **Decisão (responsável, `docs/ai/decisoes.md`, 2026-11-23):** o Marco 2 é **um único bolt**, no
  branch `marco/2-operacao`, com um único pull request. O trabalho é dividido em **etapas por assunto**,
  com **um commit por etapa**. Os papéis do `AGENTS.md` são os responsáveis por etapa, e não bolts
  separados.

  | Etapa | Responsável | Conteúdo | Arquivos compartilhados que o responsável possui na etapa |
  | --- | --- | --- | --- |
  | 1 | Implementador | ETag e contrato 1.1.0 | `static/openapi.yaml` (contrato) |
  | 2 | Implementador | Observabilidade: probes, métricas, logs nos dois formatos e execução `json-logs` | `pom.xml`, `application.yml` |
  | 3 | Infraestrutura | `Dockerfile` e `.dockerignore` | — |
  | 4 | Implementador (JaCoCo no `pom.xml`) e infraestrutura (`.github/`) | Pipeline, cobertura mínima, script de fumaça e Dependabot | `pom.xml` (implementador) |
  | 5 | Documentação | ADRs e entradas novas em `docs/ai/decisoes.md` | — |
  | 6 | Documentação | Atualização do README (FR-034) | `README.md` |

  - Cada etapa começa pelos testes (ou pela verificação do pipeline), que precisam falhar antes da
    implementação, e termina com `./mvnw verify` verde antes do commit, como nos bolts 5 e 6 do Marco 1.
  - As etapas 1 e 2 seguem em ordem, porque mexem nos testes de apoio e no `pom.xml`.
  - A etapa 3 depende só dos caminhos dos probes, já fixados em
    [contracts/operational-endpoints.md](contracts/operational-endpoints.md), e pode ser preparada pelo
    responsável de infraestrutura numa cópia de trabalho própria enquanto a etapa 2 anda. O commit dela
    entra no branch depois do da etapa 2.
  - A etapa 4 depende da 2 (execução `json-logs`) e da 3 (imagem).
  - A etapa 5 pode ser preparada a qualquer momento para os ADRs do Marco 1, e o commit dela entra depois
    do da etapa 4. A etapa 6 fecha o bolt.
  - O PR é aberto pelo responsável com as seis etapas prontas. O revisor comenta o PR inteiro, e o merge
    exige o pipeline da etapa 5 verde.
- **Motivo:** um extra só entra completo, e o pipeline só pode ser o portão de merge quando tudo o que ele
  verifica já existe. Um PR por marco, com um commit por etapa, mantém a revisão por assunto sem PRs
  intermediários que deixariam o marco pela metade na `main`. O limite de três agentes e o dono único de
  cada arquivo compartilhado continuam valendo, por etapa.
- **Alternativa rejeitada:** seis bolts, cada um com branch e PR (versão anterior desta research).
