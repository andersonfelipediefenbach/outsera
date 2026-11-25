# Research: Marco 4 — Acabamento operacional

**Spec**: [spec.md](spec.md) | **Plano**: [plan.md](plan.md) | **Data**: 2026-11-25

Cada item traz a decisão, o motivo e as alternativas. Os pontos que dependem do comportamento exato de
uma versão estão marcados como **verificar**, com o que fazer se o resultado for outro. Nenhum item muda
o código da API em `src/main`, o `pom.xml` ou os `application*.yml`: o marco inteiro fica na imagem, no
compose, no pipeline, nas ferramentas e na documentação (FR-001 e FR-002). A única exceção possível são as
correções do testador adversário (R13).

---

## R1. Métrica e definição de requisição boa e ruim do SLO

- **Decisão:** os dois SLOs usam a série que já existe desde o Marco 2,
  `http_server_requests_seconds_*{uri="/api/producers/intervals"}`, com o histograma ligado
  (`percentiles-histogram`, mínimo 1 ms, máximo 10 s).
  - **Disponibilidade:** ruim é `status=~"5.."`; todo o resto (2xx, 304 e 4xx, inclusive o 401 com o token
    ligado) é bom (FR-005). O período sem coleta conta pela regra `up{job="app"} == 0` (R2).
  - **Latência:** boa é a requisição com duração até o limite de um bucket que **já existe** no histograma
    do Micrometer. O alvo do SLO é escolhido entre esses limites, perto do valor medido (R5), e a página do
    SLO mostra o valor exato do bucket (por exemplo, `0.111848106` s, cerca de 112 ms).
- **Motivo:** não mexe no `application.yml` nem na saída das métricas do modo padrão (FR-001). O
  Prometheus só calcula a fração de requisições abaixo de um limite com exatidão quando o limite é um
  bucket.
- **Alternativas:**
  - `management.metrics.distribution.slo` com um limite redondo (100 ms): muda a configuração e a saída do
    `/actuator/prometheus` no modo padrão, de dono diferente (implementador). Rejeitada pelo responsável em
    2026-11-25: o limite do SLO é o bucket de cerca de 112 ms.
- **Verificar:** o valor exato do bucket na saída real do `/actuator/prometheus` (`le="0.111848106"`). Se o
  Micrometer da versão em uso gerar outro limite perto de 112 ms, usar o que aparece na saída e atualizar
  esta research, a página do SLO e as regras.
- **Verificar:** se o maior p99 medido dos intervalos (R5) passar de 112 ms, parar e levar ao Anderson: o
  objetivo de latência precisaria de outro bucket.
  - `histogram_quantile` no alerta: dá um percentil estimado, e não a fração de requisições ruins que o
    orçamento de erro precisa.

## R2. Forma dos alertas

- **Decisão:** alertas de **taxa de consumo do orçamento** em várias janelas (*multiwindow, multi-burn-rate*,
  do SRE Workbook), com regras de gravação para as razões:
  - `AwardsIntervalsAvailabilityBudgetBurn`: página (gravidade `page`) com consumo de 14,4× em 1 h **e**
    5 min; aviso (`ticket`) com 6× em 6 h **e** 30 min;
  - `AwardsIntervalsLatencyBudgetBurn`: as mesmas janelas, sobre a fração de requisições lentas;
  - `AwardsAppDown`: `up{job="app"} == 0` por 1 min, gravidade `page`, ligado ao SLO de disponibilidade
    (FR-005, US1-5).

  Cada alerta tem `labels.severity`, `labels.slo`, `annotations.summary`, `annotations.description` e
  `annotations.runbook_url` com a âncora da seção em `docs/runbook.md` (FR-006). A janela do SLO é de 28
  dias (FR-003).
- **Sem tráfego:** a razão fica `NaN` (0/0) e a comparação é falsa, então nenhum alerta de consumo dispara
  sem requisições (caso de borda da spec). Isso é coberto pelos testes das regras (R3).
- **Motivo:** é a forma padrão de alerta ligado a SLO; dispara rápido numa queda forte e não dispara por
  um pico curto.
- **Alternativas:** limite fixo de taxa de erro (`> 1%` por 5 min): mais simples, mas desligado do
  orçamento, que é o que a página do SLO explica. Alertmanager: fora do escopo (Assumptions).

## R3. Verificação das regras no pipeline

- **Decisão:** um job novo, `alert-rules`, roda o `promtool` da **mesma imagem do Prometheus do compose**
  (mesmo digest), por `docker compose run --rm --no-deps --entrypoint promtool prometheus ...`:
  1. `promtool check rules observability/prometheus/rules/slo.yml`;
  2. `promtool check config observability/prometheus/prometheus.yml`;
  3. `promtool test rules observability/prometheus/rules/slo.test.yml`.

  Os testes têm, por alerta, ao menos um cenário que dispara (taxa de 5xx alta, fração de lentas alta,
  alvo fora) e um que não dispara (dentro do objetivo e sem tráfego) (FR-008, SC-002).
- **No compose:** o `compose-test.sh` ganha um passo que confere em `/api/v1/rules` os grupos carregados,
  sem `lastError`, e em `/api/v1/alerts` que nenhum alerta com `slo` está `firing` com a aplicação pronta
  (FR-009, SC-003).
- **Motivo:** não instala nada, usa a versão exata que o compose roda, e o Dependabot atualiza as duas
  juntas.
- **Verificar:** que o `entrypoint` customizado do serviço `prometheus` (o que grava o token) é substituído
  pelo `--entrypoint promtool`. Se não for, rodar `docker run --entrypoint promtool` com a imagem lida do
  `compose.yaml` por `yq`.

## R4. Teste de carga

- **Decisão:** k6, na imagem oficial `grafana/k6` fixada por tag e digest, como serviço `k6` do compose no
  perfil `load` (o `workflow-policy` já confere o digest, e o Dependabot já cobre o compose).
  - Roteiro `load/intervals.js`, com dois cenários `constant-arrival-rate`: intervalos (a maior parte do
    tráfego) e listagem (páginas aleatórias, filtro por ano e por vencedor), por 2 min, depois de 15 s de
    aquecimento que não contam nos limites.
  - Cada requisição tem a tag `endpoint`. Os limites (`thresholds`) são por endpoint: `http_req_failed`,
    `p(95)` e `p(99)` de `http_req_duration`, e uma métrica `Rate` própria, `contract_errors`, que conta
    resposta fora do formato do enunciado (chaves exatas `min` e `max`, e itens com exatamente `producer`,
    `interval`, `previousWin` e `followingWin`) ou status diferente de 200 (FR-012).
  - `abortOnFail` nos limites de erro; o k6 sai com código 99 quando um limite falha, e o job fica vermelho
    (FR-011).
  - `handleSummary` grava `summary.json` e um resumo em Markdown, que o job põe no `$GITHUB_STEP_SUMMARY` e
    nos artefatos (FR-010).
  - Com `API_TOKEN` definido, o roteiro envia `Authorization: Bearer`; um 401 conta como erro, e não como
    latência boa (US2-5).
- **Ambiente:** o compose completo com o arquivo gerado (R6), por um arquivo de sobreposição
  `compose.load.yaml` que monta o CSV e define `MOVIES_CSV_PATH`. O k6 roda na rede do compose, contra
  `http://app:8080`, e só começa depois da readiness 200.
- **Alternativas:** Gatling ou JMeter (mais pesados, e o briefing escolheu o k6); a action do k6 (mais uma
  action a fixar, sem ganho sobre a imagem).

## R5. Origem dos números do SLO e dos limites (FR-011a)

- **Decisão:** a medição é feita no próprio job `load`, num runner `ubuntu-latest` compartilhado, com o
  roteiro de R4 e limites provisórios largos (p95 até 2 s, erro até 5%), para que o job não reprove antes de
  existirem números. São ao menos **5 execuções** do job `load` **no mesmo commit**, a primeira pelo push e
  as outras por "Re-run jobs", sem commits vazios (decisão do responsável, 2026-11-25). Para cada uma, o
  resumo do k6 dá a vazão, o p95, o p99 e a taxa de erro por endpoint.
  - **Objetivo do SLO:** disponibilidade de 99,5% em 28 dias (o compose não tem redundância; o número é
    conservador) e latência de 99% das requisições dos intervalos até o bucket de cerca de 112 ms (R1),
    confirmado pelo **maior p99** medido estar abaixo desse limite.
  - **Limite do pipeline:** a **maior** medição de cada percentil multiplicada por 2 (folga aceita pelo
    responsável em 2026-11-25), arredondada para cima, e taxa de erro abaixo de 1%. A folga é mais larga que o objetivo do SLO por
    causa do ruído do runner compartilhado (CPU disputada e disco variável), para o job não ficar instável.
  - O registro em `docs/ai/decisoes.md` traz as 5 execuções (link, valores), a dispersão (menor, maior e
    mediana), o objetivo escolhido e a folga, com o motivo. A página do SLO explica a diferença entre o
    objetivo e o limite do pipeline.
- **Verificar:** se a dispersão entre as execuções passar de 2× (a maior sobre a menor), a folga de 2× não
  basta: parar e levar ao Anderson, com os números, a escolha entre aumentar a folga e reduzir a taxa de
  chegada (Princípio IV).
- **Alternativas:** fixar os números antes da medição (o briefing proíbe); medir na máquina local (não é o
  ambiente do job).

## R6. Gerador de CSV

- **Decisão:** `tools/csv-generator/GenerateMovies.java`, um programa de arquivo único rodado com
  `java tools/csv-generator/GenerateMovies.java --movies 100000 --seed 20261125 --output <arquivo>` (o
  `java` do JDK 25 roda o arquivo-fonte direto; não entra no build do Maven nem no `src/`).
  - Aleatoriedade com `java.util.Random(seed)`, cujo algoritmo é fixado pela especificação da plataforma
    Java: a mesma semente dá a mesma sequência em qualquer JVM e sistema operacional (FR-015).
  - Saída em UTF-8, com `\n` sempre, escrita num arquivo temporário na mesma pasta e movida no fim, para
    não deixar arquivo parcial (FR-016).
  - Conteúdo: anos de 1900 a 2099, títulos `Movie <n>` únicos por ano, estúdios de uma lista fixa,
    produtores de um conjunto de nomes sintéticos (`Producer 00001`, …), com separadores por vírgula, por
    ` and ` e por `, and` sorteados. Cerca de 20% vencedores. Para exercitar as regras (FR-016), o gerador
    planta casos fixos: dois produtores, cada um com duas vitórias no mesmo ano (empate em `min`, com
    intervalo 0), e dois com vitórias em 1900 e 2099 (empate em `max`). Os sorteados nunca repetem esses
    padrões, para que `min` e `max` sejam exatamente os plantados.
  - Limites: `--movies` de 1 a 2.000.000; fora disso, ou com parâmetro desconhecido, sai com código 2 e
    mensagem clara. Sem `--seed`, usa a semente padrão documentada.
  - Nenhum campo passa dos tamanhos máximos do esquema, nem tem caractere nulo (spec 001, FR-006).
- **Determinismo no pipeline (FR-019):** o job `load` gera duas vezes, em arquivos diferentes, e compara o
  `sha256sum`; os dois hashes vão ao resumo. A comparação entre sistemas operacionais (SC-005) é feita uma
  vez no Windows, com o hash registrado no README, e o pipeline confere que o hash do Linux é o mesmo.
- **Sem teste unitário:** o gerador é ferramenta do repositório, não código do back-end (Assumptions da
  spec). É comprovado pelo pipeline e pela carga real com zero linhas rejeitadas (SC-006).
- **Alternativas:** script em Python ou em bash (exige algo além do JDK); classe em `src/test` rodada pelo
  Maven (misturaria ferramenta com teste e entraria no `./mvnw verify`).

## R7. Medição da carga inicial (FR-018)

- **Decisão:** no job `load`, com o arquivo gerado de 100 mil filmes:
  - **carga direta:** `docker run` da imagem no modo padrão, com o arquivo montado e `MOVIES_CSV_PATH`; o
    tempo vai do início do container à readiness 200, e o resumo da carga no log precisa ter zero
    rejeitadas;
  - **ingestão por Kafka:** o tempo da subida do serviço `app` do compose (início do container) até a
    readiness 200, com o mesmo arquivo.

  Os dois tempos, o número de linhas e o tipo do runner vão ao resumo do job; os números de uma execução
  verde vão ao README (FR-018).
- **Verificar:** o heap da imagem (75% da memória do runner) com 100 mil filmes nos dois caminhos. Se a
  readiness demorar mais que o `timeout` do job, reduzir o tamanho padrão e registrar.

## R8. Cache AOT da JVM na imagem

- **Decisão (Clarifications, 2026-11-25):** só o cache AOT da JVM 25 (JEP 483 e JEP 514), com o suporte
  documentado do Spring Boot. O processamento AOT do Spring (`process-aot`) não é usado.
  - O `extract` passa a ser `java -Djarmode=tools -jar awards.jar extract --layers --destination
    extracted`, **sem** `--launcher`: o cache exige um classpath de arquivos JAR (`application.jar` e
    `lib/`), e não de pastas, como o `JarLauncher` explodido. As camadas continuam as mesmas quatro.
  - O treino roda **no estágio final**, com a mesma JRE da execução, como último `RUN` antes do `USER`:
    `java -XX:AOTCacheOutput=app.aot -Dspring.context.exit=onRefresh -jar application.jar`. O contexto
    sobe com H2 e o arquivo embarcado e sai ao fim do refresh. O `app.aot` fica legível pelo usuário
    `10001`.
  - O `ENTRYPOINT` vira `["java", "-XX:MaxRAMPercentage=75", "-XX:AOTCache=app.aot", "-jar",
    "application.jar"]`. Com `AOTMode` no padrão (`auto`), um cache que não serve é ignorado com aviso e a
    JVM sobe sem ele (FR-023).
  - O perfil é escolhido na execução, como antes: o treino com o modo padrão só deixa de pré-carregar as
    classes do Kafka e do PostgreSQL, que o compose carrega normalmente (US4-6).
- **Medição (FR-022):** a mesma imagem, sem e com o cache: com `docker run --entrypoint java ...
  -XX:AOTMode=off -jar application.jar` para o "sem" e o `ENTRYPOINT` normal para o "com". 7 repetições
  alternadas de cada, tempo do início do container à readiness 200 e o `Started ... in X seconds` do log;
  a mediana de cada uma vai ao resumo do job `image`, e uma execução verde, ao README. O script reprova se
  a mediana com cache não for menor (SC-007). Um último passo sobe a imagem com um cache cortado e exige a
  readiness 200 e o aviso da JVM no log (FR-023), para que o job `image` comprove a FR-023.
- **Verificar:**
  - se o `spring.context.exit=onRefresh` sai antes da carga do CSV e da abertura da porta. Se a carga
    rodar no refresh e o treino ficar lento, ou se o cache ficar pequeno demais para ganhar tempo, treinar
    até a readiness (subir em segundo plano, esperar o `wget` da readiness e mandar `SIGTERM`), que também
    grava o cache na saída;
  - que o teste de fumaça não depende do `JarLauncher` (o PID 1 continua sendo a JVM, com `SIGTERM`
    direto);
  - que o aviso da JVM com cache inválido aparece só nesse caso, fora do JSON do log (spec 002, R8);
  - o tamanho do `app.aot` na imagem e o efeito no relatório do Trivy (nenhum pacote novo).
- **Alternativas:** CDS clássico (`-XX:ArchiveClassesAtExit`): ganho menor que o cache AOT na JVM 25;
  treino por perfil (rejeitado na clarificação); AOT do Spring (rejeitado na clarificação).

## R9. SBOM

- **Decisão:** o Trivy, que o pipeline já usa, gera o SBOM da imagem em CycloneDX JSON
  (`format: cyclonedx`, `scan-type: image`), com os pacotes do Alpine e as bibliotecas Java pelas versões
  efetivas dos JARs (FR-024). O SBOM vai aos artefatos e é o predicado do atestado (R10). O job confere, com
  `jq`, que `lz4-java` e `snappy-java` estão no SBOM, nas versões do `dependencyManagement`.
- **Alternativas:** Syft (`anchore/sbom-action`): mais uma action com referências aninhadas a conferir
  (spec 002, R10), sem ganho.

## R10. Assinatura num registro temporário

- **Decisão (Clarifications, 2026-11-25):** um job novo, `supply-chain`:
  - um *service container* `registry:3` (fixado por digest, conferido pelo `workflow-policy`) em
    `localhost:5000`, que morre com o job;
  - o `build-push-action` com `push: true` **só** para `localhost:5000/awards`, reaproveitando o cache
    `gha` do job `image`; o digest sai do `outputs.digest`;
  - `sigstore/cosign-installer` (fixado por hash, com as referências aninhadas conferidas como na spec 002,
    R10);
  - `cosign sign --yes localhost:5000/awards@<digest>` e `cosign attest --yes --type cyclonedx --predicate
    sbom.cdx.json localhost:5000/awards@<digest>`, *keyless*: o certificado vem do Fulcio a partir do
    token OIDC do job, e a entrada vai ao Rekor público (FR-025);
  - verificação: `cosign verify` e `cosign verify-attestation --type cyclonedx` com
    `--certificate-identity` igual a `https://github.com/<repositório>/.github/workflows/ci.yml@<ref>` e
    `--certificate-oidc-issuer https://token.actions.githubusercontent.com`; um terceiro passo verifica com
    uma identidade diferente e **precisa falhar** (US5-3, SC-008);
  - o resumo do job traz o digest, o índice da entrada no Rekor e os comandos de verificação.
- **Permissões:** o job tem `id-token: write` e `contents: read`, e nenhum outro job tem `id-token`
  (FR-026).
- **Forks:** `if: github.event_name != 'pull_request' || github.event.pull_request.head.repo.full_name ==
  github.repository`. Num fork, o job é pulado e um passo do job `image` escreve o motivo no resumo
  (FR-027). Um job pulado conta como verde para o check obrigatório.
- **Política do workflow (FR-026):** o `check-workflows.sh` muda em duas regras, com casos que precisam
  falhar:
  - regra 3: `id-token: write` passa a ser permitido **só** no job `supply-chain`;
  - regra 5: `push: true` e `docker push` passam a ser permitidos **só** no job `supply-chain` e **só** com
    destino `localhost:5000/`. Qualquer outro destino, ou push em outro job, continua violação.
- **Verificar:** que o cosign aceita o registro HTTP em `localhost` (se não, `--allow-insecure-registry` e
  `--allow-http-registry`); que o `concurrency` com `cancel-in-progress` não deixa assinatura pela metade
  (o registro morre com o job, então não há estado parcial).
- **Alternativas:** `sign-blob` de arquivos (rejeitado na clarificação); `actions/attest-build-provenance`
  (publica o atestado na API de atestados do GitHub, o que fica para o marco da entrega).

## R11. Dev container

- **Decisão:** `.devcontainer/devcontainer.json` com:
  - a imagem `mcr.microsoft.com/devcontainers/java` na variante do JDK 25, fixada por digest;
  - o recurso `ghcr.io/devcontainers/features/docker-in-docker`, fixado por digest (`@sha256:`), com a
    versão num comentário e o compose v2. É a mesma convenção das imagens, porque o recurso também é um
    artefato OCI;
  - sem o recurso do Maven: o projeto usa o `./mvnw` (FR-028);
  - `forwardPorts` 8080, 9090 e 3000 (FR-029);
  - `postCreateCommand: ./mvnw -B -q dependency:go-offline`, para o primeiro `verify` não baixar tudo;
  - `containerEnv` vazio de segredos.
- **Pipeline (FR-029, FR-030):** job `devcontainer` com `devcontainers/ci` (fixado por hash) rodando
  dentro do container `java -version`, `./mvnw -v`, `docker version`, `./mvnw -B verify`, `docker compose
  up -d --build --wait` e a conferência do resultado de referência. As portas encaminhadas para a máquina
  não existem no runner e são conferidas na revisão (checklist).
- **Dependabot:** ganha o ecossistema `devcontainers`, e o `check-workflows.sh` passa a exigi-lo (regra 6).
- **Verificar:** que existe a variante do JDK 25 da imagem `devcontainers/java`. Se não existir, usar a
  imagem base `mcr.microsoft.com/devcontainers/base` com o recurso `ghcr.io/devcontainers/features/java`
  (versão 25, distribuição Temurin, sem Maven). Que o `docker-in-docker` roda no runner do GitHub (o
  `devcontainers/ci` usa `--privileged`).
- **Alternativas:** `docker-outside-of-docker` (usa o Docker da máquina; os bind mounts do compose quebram
  com caminhos diferentes entre o container e a máquina).

## R12. Por que o job `dependencies` não apontou as transitivas do `kafka-clients` (FR-032 e FR-033)

- **Hipótese:** o `trivy fs` analisa o `pom.xml` sem rodar o Maven. Ele resolve o `parent`, o BOM e as
  transitivas por conta própria, a partir do Maven Central, e não chega às mesmas versões efetivas do build
  (escopo `runtime` nas transitivas, propriedades do BOM do Spring Boot ou `dependencyManagement` herdado).
  As bibliotecas do `kafka-clients` ficaram fora do inventário ou com outra versão.
- **Investigação (registrar com a evidência em `docs/ai/decisoes.md`):**
  1. no commit anterior à fixação das versões (`lz4-java` 1.11.2 efetiva), rodar `trivy fs --scanners vuln
     --list-all-pkgs --format json .` com a mesma versão do Trivy da action, e procurar `lz4-java` e
     `snappy-java` no resultado;
  2. comparar com `./mvnw -B dependency:tree -Dscope=runtime` (versões efetivas);
  3. conferir na documentação e nas *issues* do Trivy a limitação correspondente, se houver.
- **Correção:** o job deixa de analisar o `pom.xml` e passa a analisar o **inventário efetivo do build**:
  `actions/setup-java` (já fixado), `./mvnw -B -q dependency:copy-dependencies -DincludeScope=runtime
  -DoutputDirectory=target/runtime-deps`, e o `trivy fs` (relatório e porta) sobre essa pasta, que o Trivy
  analisa pelos `pom.properties` dentro dos JARs, como faz na imagem. Um passo lista no resumo, com `jq`
  sobre um relatório `--list-all-pkgs`, as versões de `lz4-java` e `snappy-java` (FR-033). Nada muda no
  `pom.xml`: o plugin de dependências já faz parte do Maven.
- **Comprovação única (FR-034):** roteiro da T050 do Marco 2, num branch temporário
  `verificacao/trivy-transitivas` a partir de `marco/4-acabamento`: um commit volta `lz4-java` para 1.11.2
  no `dependencyManagement`; o job `dependencies` precisa ficar vermelho por essa CVE. O branch nunca é
  mergeado e é apagado, local e remotamente; nada dele volta ao marco. O push é do Anderson ou com a
  autorização dele.
- **Alternativas:** gerar o SBOM do build com o `cyclonedx-maven-plugin` e rodar `trivy sbom`: mais um
  plugin no `pom.xml` (arquivo do implementador e do modo padrão). Analisar só a imagem: já existe, mas é o
  job que pegou o problema tarde; o objetivo é o job de dependências enxergar as transitivas.

## R13. Testador adversário da ingestão por Kafka (FR-035)

- **Decisão:** o roteiro do T050 da spec 003, sem mudança: o Anderson aciona o Codex, que cria eventos de
  ataque a partir da spec 003 e de `specs/003-modo-docker/contracts/kafka-events.md`, sem ler `src/main`.
  Cada falha vira IT permanente em `src/test/java/com/outsera/awards/kafka/adversario/`, com o broker
  embutido e o prefixo `S004` no `@DisplayName`, e a correção vai num commit próprio. Se uma correção
  mudar comportamento, a spec 003 é atualizada antes (Princípio IV). O resultado, com a lista dos ataques,
  vai a `docs/ai/decisoes.md`, com ou sem falhas.
- **Ordem:** é a única parte do marco que pode tocar `src/main`, por isso entra primeiro, antes da imagem
  com o cache (R8), para que a medição da subida já use o código final.

## R14. Proteção da `main` e checks do Marco 4 (FR-031)

- **Decisão:** a lista de checks obrigatórios passa a ter os oito do Marco 3 (`workflow-policy`, `build`,
  `postgres`, `codeql`, `dependencies`, `dockerfile-lint`, `image`, `compose`) e os quatro novos
  (`alert-rules`, `load`, `supply-chain`, `devcontainer`), com o bypass para administradores desligado. A
  configuração é do Anderson, depois da primeira execução do pipeline no PR (antes disso, os jobs novos não
  aparecem na lista). O registro em `docs/ai/decisoes.md` traz a lista e a confirmação do bypass, sem dado
  pessoal.
- **Os ITs dos Marcos 1 a 3:** o `workflow-policy` passa a rodar o `check-unchanged-tests.sh` também contra
  a tag `v0.3.0`, além da `v0.2.0`, para provar que nenhum teste existente mudou (FR-001, SC-001). Os ITs
  novos do testador adversário (R13) são arquivos novos e não contam como alteração.
