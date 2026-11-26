---

description: "Tarefas do Marco 4, num único bolt com seis etapas (um commit por etapa, cada uma com o ./mvnw verify verde)"
---

# Tasks: Marco 4 — Acabamento operacional: alertas de SLO, carga, dados gerados, cache AOT, assinatura e dev container

**Input**: Design documents from `/specs/004-acabamento-operacional/`

**Prerequisites**: plan.md, spec.md, research.md (R1 a R14), data-model.md, contracts/ (ci-pipeline.md,
operacao.md), quickstart.md

**Tests**: a comprovação segue o Princípio VI (constituição 1.7.0), pelo tipo de cada requisito:

- **comportamento da aplicação:** só as correções do testador adversário (FR-035), com ITs `*IT` no
  `./mvnw -Pextras verify`, broker embutido, sem mocks, verificados por HTTP, com `@DisplayName` começando por
  `S004` e pelos IDs dos requisitos;
- **regras de alerta:** `promtool test rules`, escrito **antes** das regras e confirmado falhando;
- **scripts e jobs:** cada script novo é rodado antes da mudança que ele verifica e precisa **falhar** (ou
  sair com `FALHA`), e depois passar;
- **documentação:** a seção "Revisão da documentação" de `checklists/requirements.md`.

**Organization**: **um único bolt** no branch `marco/4-acabamento`, com um único pull request, em **seis
etapas** (plano, "Ordem das etapas"). Cada etapa:

1. começa pela verificação que precisa falhar;
2. termina com `./mvnw verify` e `./mvnw -Pextras verify` **verdes**, sem Docker, com o resultado real informado;
3. fecha com **um commit** próprio, de assunto único.

O push, o PR, o merge, as tags e os "Re-run jobs" são do Anderson. Nenhum agente abre PR, faz merge, cria
tag ou faz push sem autorização.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: pode ser feita em paralelo (arquivos diferentes e sem dependência de tarefa incompleta).
- **[Story]**: história da spec 004:
  - US1: alertas de SLO;
  - US2: teste de carga;
  - US3: gerador de CSV;
  - US4: cache AOT;
  - US5: SBOM e assinatura;
  - US6: dev container;
  - US7: pendências do Marco 3.
- **Não altere `src/main`, `pom.xml` nem `application*.yml`**, salvo uma correção do testador adversário
  (T005). **Não altere nenhum `*IT.java` que exista nas tags `v0.2.0` ou `v0.3.0`.**
- Toda action nova é fixada por hash de commit, com as referências aninhadas conferidas (spec 002, R10).
  Toda imagem nova é fixada por tag e digest. Resolver os hashes com `gh api
  repos/<owner>/<repo>/commits/<tag> --jq .sha` e os digests com `docker buildx imagetools inspect
  <imagem>:<tag>`. Anotar os pares na descrição do commit.
- Os pontos marcados como **verificar** na research são conferidos na tarefa que os usa. Se o resultado
  for outro, a research é atualizada no mesmo commit.
- Scripts novos seguem o padrão do Marco 2: bash, `set -euo pipefail`, executáveis, `OK`/`FALHA`/`PULADO`
  por passo, e `PULADO` é falha com `CI=true`. Rodam no Linux e no Git Bash do Windows.

## Mapeamento etapa → fase

| Etapa | Fase | Histórias | Responsável (`AGENTS.md`) | Commit |
| --- | --- | --- | --- | --- |
| — | 1. Preparação | — | Implementador | não gera commit |
| 1. Pendências do Marco 3 | 2 | US7 | Implementador (adversário); infraestrutura (job `dependencies`) | `Etapa 1: pendências do Marco 3 e inventário efetivo no job de dependências` |
| 2. Gerador e teste de carga | 3 | US3, US2 | Infraestrutura | `Etapa 2: gerador de CSV determinístico e teste de carga com k6` |
| 3. SLO e alertas | 4 | US1 | Infraestrutura | `Etapa 3: alertas de SLO no Prometheus e limites do k6 pela medição` |
| 4. Cache AOT | 5 | US4 | Infraestrutura | `Etapa 4: cache AOT da JVM na imagem e medição da subida` |
| 5. SBOM e assinatura | 6 | US5 | Infraestrutura | `Etapa 5: SBOM e assinatura keyless num registro temporário` |
| 6. Dev container e documentação | 7 e 8 | US6 e fechamento | Infraestrutura; documentação | `Etapa 6: dev container, SLO, runbook, ADRs e README do Marco 4` |

---

## Phase 1: Preparação

**Purpose**: ponto de partida medido, sem commit.

- [X] T001 Conferir que o branch é `marco/4-acabamento`, criado a partir da `main` com o merge do Marco 3, e que `git status` só mostra `docs/ai/prompts.md` (gravado pelo hook, não editar). Rodar `./mvnw verify` e anotar o resultado e o tempo total, para a descrição do commit da etapa 1.
- [X] T002 Conferir as ferramentas locais e anotar as que faltam: `docker version` (com compose v2), `jq`, `yq` (mikefarah v4), `gh`. As tarefas que precisam de uma ferramenta ausente marcam `PULADO` localmente, com o motivo, e a comprovação fica no pipeline.

---

## Phase 2: Etapa 1 — Pendências do Marco 3 (US7, Priority: P1) 🎯 MVP

**Goal**: o job de dependências enxerga as transitivas de runtime; o testador adversário roda na ingestão
por Kafka; as decisões do plano ficam registradas.

**Independent Test**: o job `dependencies` lista `lz4-java` e `snappy-java` com as versões efetivas, e
fica vermelho com `lz4-java` 1.11.2 no branch temporário; as falhas do adversário, se houver, viram ITs
verdes no `./mvnw verify`.

### Verificação da etapa 1 (antes da correção; precisa mostrar a lacuna)

- [X] T003 [US7] Investigar a causa (research R12, FR-032). Ler o `action.yaml` da `aquasecurity/trivy-action` v0.36.0 e anotar a versão do Trivy que ela instala. Achar o último commit em que o `lz4-java` efetivo era 1.11.2 (`git log -S lz4-java -- pom.xml`, o commit anterior ao da fixação). Num `git worktree` temporário fora do repositório, nesse commit:
  1. rodar `docker run --rm -v "<worktree>:/src" aquasec/trivy:<versão> fs --scanners vuln --list-all-pkgs --format json --output /src/trivy-pom.json /src` e procurar `lz4-java` e `snappy-java` com `jq`;
  2. rodar `./mvnw -B -q dependency:tree -Dscope=runtime -DoutputFile=deps.txt` e anotar as versões efetivas;
  3. rodar `./mvnw -B -q dependency:copy-dependencies -DincludeScope=runtime -DoutputDirectory=target/runtime-deps` e o mesmo `trivy fs` sobre `target/runtime-deps`, com a porta (`--severity HIGH,CRITICAL --ignore-unfixed --exit-code 1`), que **precisa** sair com 1 pelas CVEs da `lz4-java` e da `snappy-java`.

  Remover o worktree no fim. Registrar em `docs/ai/decisoes.md`, numa entrada "Job de dependências: causa da lacuna das transitivas do kafka-clients", a versão do Trivy, o que o item 1 achou (ausente ou com outra versão), as versões efetivas do item 2, o resultado do item 3 e a causa, com o link da *issue* ou da documentação do Trivy, se houver. Se a causa for outra que a hipótese da R12, atualizar a R12.

### Implementação da etapa 1

- [X] T004 [US7] Registrar em `docs/ai/decisoes.md`, numa entrada "Decisões do plano do Marco 4", as quatro decisões de `specs/004-acabamento-operacional/plan.md` ("Decisões do responsável sobre o plano (2026-11-25)") e as duas clarificações da spec (cache só da JVM; assinatura num registro temporário), sem dado pessoal.
- [X] T005 [US7] **Pedido ao Anderson:** acionar o testador adversário (Codex) para a ingestão por Kafka (T050 da spec 003, FR-035, R13). Ele cria eventos de ataque a partir de `specs/003-modo-docker/spec.md` e de `specs/003-modo-docker/contracts/kafka-events.md`, sem ler `src/main`. Para cada falha encontrada:
  - se a correção muda comportamento, atualizar primeiro `specs/003-modo-docker/spec.md` (Princípio IV);
  - escrever o IT em `src/test/java/com/outsera/awards/kafka/adversario/<Nome>IT.java`, com o broker embutido, sem mocks e verificação por HTTP (Princípio I), `@DisplayName` começando por `S004 FR-035` e pelo requisito da spec 003 violado. Confirmar que ele **falha**;
  - corrigir em `src/main` e confirmar que o IT passa.

  Registrar em `docs/ai/decisoes.md`, numa entrada "Testador adversário: ingestão por Kafka", a lista dos ataques tentados, as falhas, os ITs e as correções, **também se não houver falhas**. Se o Anderson não acionar o testador nesta etapa, marcar `PENDENTE` com o motivo e repetir a pendência na etapa 6 (T062).
- [X] T006 [US7] Em `.github/workflows/ci.yml`, job `dependencies` (FR-033, R12): acrescentar `actions/setup-java` (o mesmo hash e a mesma configuração do job `build`) e um passo `./mvnw -B -q dependency:copy-dependencies -DincludeScope=runtime -DoutputDirectory=target/runtime-deps`. Trocar o `scan-ref` dos dois passos do Trivy de `.` para `target/runtime-deps`, com `scan-type: rootfs` (o `fs` não analisa JARs; verificado no T003, R12). Acrescentar um terceiro passo do Trivy com `format: json`, `list-all-pkgs: true` e `output: trivy-inventario.json`, e um passo `run:` que escreve no `$GITHUB_STEP_SUMMARY` uma tabela com o nome e a versão de `lz4-java` e `snappy-java`, lidos com `jq`. O passo **falha** se algum dos dois não estiver no inventário. Atualizar o comentário do job com a spec 004, FR-033.
- [X] T007 [US7] Conferir o T006 localmente, no HEAD: `./mvnw -B -q dependency:copy-dependencies -DincludeScope=runtime -DoutputDirectory=target/runtime-deps` e o `trivy rootfs` do T003 (com a mesma versão) sobre `target/runtime-deps`. A porta precisa sair com 0, e o inventário precisa ter `lz4-java` 1.11.4 e `snappy-java` 1.1.10.12. Anotar na descrição do commit.
- [X] T008 [US7] Em `.github/workflows/ci.yml`, job `workflow-policy`: acrescentar o passo `Testes de integração dos Marcos 1 a 3 sem alteração desde a v0.3.0`, com `run: .github/scripts/check-unchanged-tests.sh v0.3.0`, depois do passo da `v0.2.0` (R14, FR-001). Rodar `.github/scripts/check-unchanged-tests.sh v0.3.0` localmente: precisa passar.
- [X] T009 [US7] Rodar `.github/scripts/check-workflows.sh`: precisa passar (nenhuma action nova nesta etapa).
- [X] T010 [US7] Rodar `./mvnw verify` e `./mvnw -Pextras verify`: verdes, sem Docker. Informar o resultado real.
- [X] T011 [US7] Commit `Etapa 1: pendências do Marco 3 e inventário efetivo no job de dependências` com T003 a T010. Na descrição: os números do T001, o resultado do T003 e do T007, e o estado do T005.
- [ ] T012 [US7] **NÃO EXECUTADO, por decisão do Anderson (2026-11-26).** A comprovação é a local do T003 (com o `lz4-java` 1.11.2 efetivo, o Trivy em `rootfs` sobre `target/runtime-deps` sai com 1 e aponta a CVE-2026-106451) e do T007 (no HEAD, a mesma porta sai com 0 e o inventário traz o `lz4-java` 1.11.4), mais o job `dependencies` verde no pipeline. O branch `verificacao/trivy-transitivas` ficou só local, sem push. Texto original: **Pendente: exige o push, que é do Anderson.** Verificação única da FR-034, pelo roteiro da T050 do Marco 2 (R12):
  - criar o branch temporário `verificacao/trivy-transitivas` a partir de `marco/4-acabamento`, com um único commit que muda a propriedade da versão do `lz4-java` no `pom.xml` para `1.11.2`;
  - o push do branch é do Anderson ou com a autorização dele;
  - conferir que o job `dependencies` fica **vermelho** pela CVE da `lz4-java`, e que o resumo mostra `lz4-java` 1.11.2;
  - apagar o branch local e remotamente. Confirmar com `git branch -a` e `git log marco/4-acabamento` que nenhum commit dele ficou no marco: nada de cherry-pick, merge ou rebase a partir dele.

  Registrar em `docs/ai/decisoes.md`, numa entrada "Verificação do job de dependências com lz4-java 1.11.2", o nome do branch, o link da execução, o job vermelho, o resultado esperado e o obtido, e a confirmação de que o branch foi apagado. O registro entra no commit da etapa 6.

**Checkpoint**: US7 completa, salvo T012 (push) e a proteção da `main` (T064).

---

## Phase 3: Etapa 2 — Gerador e teste de carga (US3 e US2, Priority: P2 e P1)

**Goal**: um CSV grande e reproduzível, a carga inicial medida nos dois caminhos e o k6 com limites
provisórios rodando no pipeline.

**Independent Test**: duas gerações com os mesmos parâmetros dão o mesmo SHA-256; a aplicação carrega o
arquivo sem rejeitar linhas; o `load-test.sh` termina com o resumo do k6, e um limite impossível faz o k6
sair com 99.

### Verificação da etapa 2 (escrever primeiro; precisa falhar)

- [X] T013 [US2] Criar `.github/scripts/load-test.sh` (contracts/ci-pipeline.md, "Scripts"; R4 a R7). Uso: `load-test.sh <imagem> [filmes] [--only-direct]`, com 100000 filmes por padrão. Precisa de `docker`, `curl`, `jq` e `sha256sum`; sem eles, sai com 2. Passos, cada um com `OK`, `FALHA` ou `PULADO`:
  1. gerar `target/generated-data/a.csv` e `target/generated-data/movies.csv` com `java tools/csv-generator/GenerateMovies.java --movies <filmes> --output ...`, os dois com a semente padrão. Comparar os SHA-256: diferentes é `FALHA`. Escrever os dois hashes no resumo (FR-019). Com os parâmetros padrão (100000 filmes e a semente padrão), comparar também com a constante `EXPECTED_SHA256_DEFAULT` do script, que guarda o hash gerado no Windows no T015. Diferente é `FALHA` (SC-005, research R6). Com outro número de filmes, essa comparação é `PULADO` com o motivo. Com `CI=true`, o script recusa um número de filmes diferente do padrão (saída 2), então no pipeline a comparação sempre roda, e a regra "`PULADO` é falha no pipeline" continua valendo;
  2. carga direta: `docker run -d` da imagem no modo padrão, com `movies.csv` montado só leitura e `MOVIES_CSV_PATH` apontando para ele. Medir do `docker run` à readiness 200, consultando a cada 0,5 s, com limite de 600 s. No log do container, o resumo da carga precisa ter zero rejeitadas. Os intervalos precisam ter `min` e `max` não vazios. Remover o container (FR-018, SC-006);
  3. carga por Kafka: `docker compose -f compose.yaml -f compose.load.yaml up -d --build --wait`. Medir do `State.StartedAt` do container `app` (`docker inspect`) à readiness 200. Conferir os intervalos não vazios. No `docker compose logs app`, o resumo da leitura do publicador precisa ter zero rejeitadas e o mesmo número de aceitas do passo 2 (SC-006, nos dois caminhos);
  4. `docker compose -f compose.yaml -f compose.load.yaml --profile load run --rm k6`. O código de saída do k6 é o do passo (99 = limite ultrapassado);
  5. escrever no `$GITHUB_STEP_SUMMARY` (ou na saída, fora do pipeline) os dois tempos de carga, o número de linhas, o `RUNNER_NAME` e o `summary.md` do k6. Copiar `summary.json`, `summary.md` e os logs para `target/load/`;
  6. sempre, num `trap`: `docker compose ... down -v` e remoção do container do passo 2.

  Opção `--only-direct`, como terceiro argumento: roda só os passos 1 e 2 (geração e carga direta) e o passo 5, sem o compose nem o k6. Ela serve à medição de memória do T021. Com `CI=true`, a opção é recusada (saída 2): o job `load` roda sempre todos os passos.

  Rodar antes de existir o gerador: precisa sair com `FALHA` no passo 1.

### Implementação da etapa 2

- [X] T014 [P] [US3] Criar `tools/csv-generator/GenerateMovies.java` (R6; contracts/operacao.md, "Gerador de CSV"; data-model.md, "Arquivo gerado"). Arquivo único, sem dependências, rodado com `java tools/csv-generator/GenerateMovies.java`. Regras:
  - parâmetros: "`--movies` (1 a 2.000.000; padrão 100.000), `--seed` (padrão 20261125), `--output`" obrigatório. Parâmetro desconhecido, ausente ou fora da faixa: uma linha no erro padrão e saída 2, sem criar arquivo;
  - formato: "UTF-8 sem BOM, `\n`, separador `;`, cabeçalho `year;title;studios;producers;winner`". Escrever com `Files.newBufferedWriter(..., UTF_8)` e `\n` explícito, nunca `println` nem `%n`;
  - chave: "(`year`, `title`) única, sem duplicatas". Títulos `Movie <n>`, anos de 1900 a 2099, estúdios de uma lista fixa, produtores `Producer 00001` a `Producer NNNNN`, com os separadores `, `, ` and ` e `, and ` sorteados. Cerca de 20% vencedores (`yes`); os demais com `winner` vazio;
  - aleatoriedade só por `new java.util.Random(seed)`, sem `Math.random`, `ThreadLocalRandom`, `HashMap` iterado nem nada dependente de sistema;
  - casos plantados: "dois produtores com duas vitórias no mesmo ano (empate em `min`, intervalo 0); dois com vitórias em 1900 e 2099 (empate em `max`)". Os dois do `min` vencem em anos diferentes entre si. Os produtores sorteados nunca vencem duas vezes no mesmo ano e nunca vencem em 1900 e em 2099 ao mesmo tempo, para que `min` e `max` sejam exatamente os plantados;
  - nenhum campo passa de 500 caracteres nem tem U+0000 (spec 001, FR-006);
  - escrita num arquivo temporário na mesma pasta, movido com `ATOMIC_MOVE` no fim. Em erro de escrita, apagar o temporário e sair com 1;
  - no fim, imprimir o caminho, o número de filmes e o SHA-256 (`MessageDigest`).
- [X] T015 [US3] Conferir o T014 localmente: duas gerações com os padrões dão o mesmo SHA-256; com `--seed 1`, o hash é outro; `--movies 0`, `--movies 2000001`, `--movies x` e sem `--output` saem com 2 e não criam arquivo. Anotar o SHA-256 do padrão gerado no Windows (Git Bash), para o README (SC-005), e gravá-lo na constante `EXPECTED_SHA256_DEFAULT` do `.github/scripts/load-test.sh` (T013, passo 1).
- [X] T016 [US3] Subir `./mvnw spring-boot:run` com `MOVIES_CSV_PATH` apontando para o arquivo gerado (**verificar** se o valor aceita um caminho de arquivo comum ou precisa do prefixo `file:`; anotar a forma certa para o README). Conferir: o resumo da carga com 100000 lidas, 100000 aceitas e 0 rejeitadas; a readiness 200; `min` com exatamente os dois produtores plantados, intervalo 0, ordenados por `previousWin`; `max` com exatamente os dois produtores plantados, de 1900 a 2099, ordenados pelo nome; tudo no formato do enunciado (FR-016, US3-3, US3-4).
- [X] T017 [P] [US2] Criar `compose.load.yaml`: sobrepõe só o serviço `app`, montando `./target/generated-data/movies.csv` em `/data/movies.csv:ro` e definindo `MOVIES_CSV_PATH` com a forma do T016. Comentário no topo com a spec 004, FR-013, e o uso.
- [X] T018 [US2] Em `compose.yaml`, acrescentar o serviço `k6` (R4): imagem `grafana/k6:<versão estável>@sha256:<digest>`, `profiles: [load]`, `depends_on: app: condition: service_healthy`, volume `./load:/scripts:ro` e `./target/load:/out`, `environment` com `BASE_URL: http://app:8080`, `API_TOKEN: ${API_TOKEN:-}` e as variáveis dos limites do T019 repassadas (`K6_*: ${K6_*:-}`), e `command: ["run", "/scripts/intervals.js"]`. **Verificar** se o usuário da imagem consegue escrever em `/out` montado; se não, `user:` com o UID do runner, documentado.
- [X] T019 [US2] Criar `load/intervals.js` (R4; data-model.md, "Cenário de carga"):
  - cenários "`intervals` e `movies`, `constant-arrival-rate`, aquecimento de 15 s fora dos limites". Taxa inicial de 50 req/s nos intervalos e 10 nas listagens, por 2 min, com `preAllocatedVUs` e `maxVUs` suficientes. A listagem usa páginas e filtros (`year`, `winner`) sorteados;
  - toda requisição com `tags.endpoint` igual a `intervals` ou `movies`. Os limites usam `{endpoint:...,scenario:...}`, para o aquecimento ficar de fora;
  - `Rate` `contract_errors`: nos intervalos, erro se o status não for 200 ou se o corpo não tiver exatamente as chaves `min` e `max` com itens de exatamente `producer`, `interval`, `previousWin` e `followingWin`. Na listagem, erro se o status não for 200;
  - limites provisórios (R5), lidos de variáveis com padrão: `K6_P95_INTERVALS_MS=2000`, `K6_P99_INTERVALS_MS=4000`, `K6_P95_MOVIES_MS=2000`, `K6_P99_MOVIES_MS=4000` e `K6_MAX_ERROR_RATE=0.05`, aplicados a `http_req_failed`, `contract_errors` e aos percentis de `http_req_duration`, com `abortOnFail` nos de erro;
  - `Authorization: Bearer ${API_TOKEN}` só quando `API_TOKEN` não é vazio;
  - `handleSummary`: grava `/out/summary.json` e `/out/summary.md` (vazão, p95, p99 e taxa de erro por endpoint) e mantém o resumo de texto na saída padrão.
- [X] T020 [US2] Em `.github/workflows/ci.yml`, criar o job `load` (contracts/ci-pipeline.md): `needs: dockerfile-lint`, `timeout-minutes: 30`, `permissions` do topo, `actions/checkout` e `actions/setup-java` (os mesmos hashes), o build da imagem como no job `image` (`load: true`, `tags: awards:ci`, cache `gha`), `run: .github/scripts/load-test.sh awards:ci` e `actions/upload-artifact` (o mesmo hash) de `target/load/` com `if: always()`, nome `relatorios-load`. Comentário com a spec 004, FR-010 a FR-019.
- [X] T021 [US2] **Local, 2026-11-25:** o `load-test.sh` inteiro ficou PULADO (sem `jq` na máquina; o download foi negado); os mesmos passos rodaram à mão com o Docker, com os resultados na descrição do commit da etapa 2. Rodar `.github/scripts/load-test.sh awards:local` localmente com o Docker: todos os passos `OK`. Rodar de novo com `K6_P95_INTERVALS_MS=1`: o passo 4 precisa sair com 99 e o script com `FALHA` (SC-004). Também com o Docker:
  - **autenticação (US2-5):** com o compose e `API_TOKEN` definido com um valor gerado na hora, nunca impresso, rodar o k6 com o mesmo `API_TOKEN`: as respostas são 200 e o k6 sai com 0. Rodar o k6 com outro token, sobrescrevendo só a variável do serviço `k6`: `docker compose -f compose.yaml -f compose.load.yaml --profile load run --rm -e API_TOKEN=<outro valor gerado na hora> k6`. O `app` continua com o token original. O `contract_errors` sobe, porque o 401 não é 200, e o k6 sai com 99, sem medir o 401 como latência boa;
  - **memória (caso de borda da spec):** com `.github/scripts/load-test.sh awards:local <filmes> --only-direct` para 500000, 1000000 e 2000000 filmes, anotar o maior tamanho que fica pronto sem erro de memória com a configuração da imagem. Esse é o limite que o README informa (T062).

  Anotar todos os resultados e os tempos de carga local, identificados como da máquina local, na descrição do commit. O resultado do limite impossível vai também para a entrada do T034. Se o Docker não estiver disponível, `PULADO` com o motivo; a comprovação fica no pipeline.
- [X] T022 [US2] Rodar `.github/scripts/check-workflows.sh` (a imagem do k6 por digest e a action do job `load` por hash) e `.github/scripts/check-docker-context.sh` (nada de `tools/`, `load/` ou `target/generated-data/` no contexto da imagem): os dois precisam passar.
- [X] T023 [US2] Rodar `./mvnw verify` e `./mvnw -Pextras verify`: verdes, sem Docker. Informar o resultado real.
- [X] T024 [US2] Commit `Etapa 2: gerador de CSV determinístico e teste de carga com k6` com T013 a T023. Na descrição: os pares tag → digest e tag → hash, o SHA-256 do T015 e os resultados do T021.
- [X] T025 [US2] **2026-11-26: 3 execuções, e não 5, por decisão do Anderson (R5, "Resultado"); registro em docs/ai/decisoes.md, "Medição do teste de carga e folga dos limites do pipeline".** **Pendente: exige o push, que é do Anderson. Bloqueia a etapa 3.** Medição no runner compartilhado (R5, FR-011a): depois do push do commit do T024, o job `load` roda uma vez pelo push, e o Anderson dispara mais 4 execuções com "Re-run jobs" no **mesmo commit**, sem commits vazios. De cada execução, anotar o link, o p95, o p99, a vazão e a taxa de erro de cada endpoint, e os dois tempos de carga. Calcular, por métrica, o menor, o maior e a mediana. **Parar e levar ao Anderson** se:
  - a maior medição de algum percentil passar de 2× a menor (a folga de 2× não basta);
  - o maior p99 dos intervalos passar de 112 ms (o objetivo de latência precisaria de outro bucket, R1);
  - alguma execução tiver erro acima de 1%.

**Checkpoint**: US3 completa; US2 completa com os limites provisórios.

---

## Phase 4: Etapa 3 — SLO e alertas (US1, Priority: P1)

**Goal**: regras de alerta ligadas ao SLO, testadas com séries sintéticas, carregadas no compose, e os
limites finais do k6 vindos da medição.

**Independent Test**: `promtool test rules` passa, com cada alerta disparando e não disparando nos
cenários certos; o `compose-test.sh` confere as regras carregadas e nenhum alerta `slo` disparando.

### Verificação da etapa 3 (escrever primeiro; precisa falhar)

- [X] T026 [US1] **Verificar** o limite do bucket (R1): subir `./mvnw spring-boot:run`, fazer algumas chamadas aos intervalos e procurar em `/actuator/prometheus` a série `http_server_requests_seconds_bucket{...uri="/api/producers/intervals"...}` com `le` perto de 0,112. Anotar o valor exato de `le` e os rótulos reais (`status`, `outcome`, `method`, `uri`, `application`). Se o valor não for `0.111848106`, usar o encontrado em todas as tarefas abaixo e atualizar a R1.
- [X] T027 [US1] Criar `observability/prometheus/rules/slo.test.yml` (R3; data-model.md, "Teste de regra"), com `rule_files: [slo.yml]` e `evaluation_interval: 1m`. Usar "`input_series` de `http_server_requests_seconds_count` e `_bucket` e de `up`", com os rótulos reais do T026 e `job="app"`. Casos:
  - disponibilidade: 5xx em 10% das requisições por 1 h 10 min → `AwardsIntervalsAvailabilityBudgetBurn` com `severity="page"` em `firing`; 5xx em 0,1% → nenhum alerta; 4xx (401) e 304 em 50% → nenhum alerta (FR-005);
  - latência: 50% das requisições acima do bucket por 1 h 10 min → `AwardsIntervalsLatencyBudgetBurn` em `firing`; 0,5% acima → nenhum alerta;
  - sem tráfego (contadores parados) → nenhum alerta de consumo;
  - `up` em 0 por 2 min → `AwardsAppDown` em `firing`; `up` em 1 → nenhum alerta (US1-5);
  - os alertas em `firing` conferem `exp_labels` (`severity`, `slo`) e `exp_annotations` (`summary`, `description` e `runbook_url` com a âncora em minúsculas, como em contracts/operacao.md, "Alertas").

  Rodar `docker compose run --rm --no-deps --entrypoint promtool prometheus test rules /etc/prometheus/rules/slo.test.yml`, com a montagem do T029 já feita ou com `docker run --rm --entrypoint promtool -v "$PWD/observability/prometheus:/etc/prometheus:ro" <imagem do prometheus do compose> test rules ...`. Precisa **falhar**, porque o `slo.yml` ainda não existe. **Verificar** se o `--entrypoint promtool` substitui o `entrypoint` do serviço (R3); anotar a forma que funciona.

### Implementação da etapa 3

- [X] T028 [US1] Criar `observability/prometheus/rules/slo.yml` (R1, R2; data-model.md, "Regra de alerta"):
  - grupo de regras de gravação `awards:intervals_errors:ratio_rate<janela>` e `awards:intervals_slow:ratio_rate<janela>` para 5m, 30m, 1h e 6h, sobre `uri="/api/producers/intervals"`. Os erros são `status=~"5.."` sobre o total. As lentas são 1 menos o `_bucket` com o `le` do T026 sobre o `_count`;
  - grupo de alertas com o orçamento de 0,005 (disponibilidade de 99,5%) e de 0,01 (latência: 99% até o bucket). "`alert` `AwardsIntervalsAvailabilityBudgetBurn`, `AwardsIntervalsLatencyBudgetBurn`, `AwardsAppDown`". Consumo 14,4× em 1 h **e** 5 min → `severity: page`; 6× em 6 h **e** 30 min → `severity: ticket`. "`for` 2 min nos alertas de consumo; 1 min no `AwardsAppDown`", que é `up{job="app"} == 0`;
  - "`labels.slo` `intervals-availability` ou `intervals-latency`", `annotations.summary`, `annotations.description` com o valor atual (`{{ $value | humanizePercentage }}`) e "`annotations.runbook_url` `.../docs/runbook.md#<âncora-do-alerta>`", com a URL do repositório no GitHub e a âncora em minúsculas.

  Comentário no topo com a spec 004, FR-004 a FR-006, e o link para `docs/slo.md`.
- [X] T029 [US1] Em `observability/prometheus/prometheus.yml`, acrescentar `rule_files: [/etc/prometheus/rules/slo.yml]`. Em `compose.yaml`, no serviço `prometheus`, montar `./observability/prometheus/rules:/etc/prometheus/rules:ro`, sem mudar o `entrypoint`.
- [X] T030 [US1] Rodar `promtool check rules`, `promtool check config` e `promtool test rules` (forma anotada no T027): os três passam (SC-002).
- [X] T031 [US1] Em `.github/workflows/ci.yml`, criar o job `alert-rules` (contracts/ci-pipeline.md): `timeout-minutes: 5`, `actions/checkout` e os três comandos do T030, cada um num passo com nome próprio. Comentário com a spec 004, FR-004, FR-006 e FR-008.
- [X] T032 [US1] Em `.github/scripts/compose-test.sh`, acrescentar depois do passo 4 o passo `4b. Regras do SLO carregadas e nenhum alerta do SLO disparando` (FR-009, SC-003): em `http://localhost:9090/api/v1/rules`, conferir com `jq` que os grupos do `slo.yml` estão presentes e que nenhuma regra tem `health` diferente de `ok` nem `lastError`; em `/api/v1/alerts`, conferir que nenhum alerta com o rótulo `slo` está `firing`. Atualizar o cabeçalho do script e o nome do passo no job `compose` (`passos 0 a 8, 4b e 7b`).
- [X] T033 [US1] Limites finais do k6 (R5, FR-011a): em `load/intervals.js`, trocar os padrões provisórios do T019 por 2× a maior medição de cada percentil do T025, arredondada para cima (em ms inteiros), e `K6_MAX_ERROR_RATE=0.01`. Comentário com a origem dos números e o link da entrada do T034.
- [X] T034 [US1] Registrar em `docs/ai/decisoes.md`, numa entrada "Medição do teste de carga e folga dos limites do pipeline" (FR-011a): as 5 execuções do T025 (links e valores por endpoint), o menor, o maior e a mediana, o tipo do runner, os objetivos do SLO (disponibilidade de 99,5% e 99% até o bucket do T026, em 28 dias), os limites finais do T033, a folga de 2× e o motivo (ruído do runner compartilhado, para o job não ficar instável). Incluir a verificação única do limite impossível do T021 (`K6_P95_INTERVALS_MS=1`, saída 99 do k6 e `FALHA` do script) e a execução com o token errado, como prova de que os limites reprovam o job (SC-004).
- [X] T035 [US1] **2026-11-26: comprovado no pipeline (jobs `compose`, com o passo 4b, e `load`, com os limites finais), por decisão do Anderson; sem execução local.** Rodar localmente com o Docker: `.github/scripts/compose-test.sh`, com todos os passos `OK`, inclusive o 4b. Depois, a seção 4 de `specs/004-acabamento-operacional/quickstart.md`: com `docker compose stop app`, o `AwardsAppDown` fica `firing` em até 2 min, e o `runbook_url` aponta para `docs/runbook.md#awardsappdown`. A âncora só existe depois do T060, então conferir o formato agora e o destino na etapa 6. Também `load-test.sh` com os limites finais: `OK`. Sem o Docker, `PULADO` com o motivo.
- [X] T036 [US1] Rodar `./mvnw verify` e `./mvnw -Pextras verify`: verdes, sem Docker. Informar o resultado real.
- [X] T037 [US1] Commit `Etapa 3: alertas de SLO no Prometheus e limites do k6 pela medição` com T026 a T036. Na descrição: o `le` do T026, a forma do `promtool` do T027 e o resultado do T035.

**Checkpoint**: US1 completa; US2 com os limites finais.

---

## Phase 5: Etapa 4 — Cache AOT (US4, Priority: P2)

**Goal**: a imagem sobe com o cache AOT da JVM, passa nos mesmos testes e a subida fica medida sem e com
o cache.

**Independent Test**: o teste de fumaça e o teste do compose passam com a imagem nova; o
`startup-benchmark.sh` mostra a mediana com o cache menor que sem ele.

### Verificação da etapa 4 (escrever primeiro; precisa falhar)

- [ ] T038 **ADIADO (escopo reduzido, 2026-11-26; spec, Clarifications).** [US4] Criar `.github/scripts/startup-benchmark.sh` (R8; contracts/ci-pipeline.md, "Scripts"; contracts/operacao.md, "Imagem"). Uso: `startup-benchmark.sh <imagem> [repetições]`, com 7 repetições por padrão. Em cada repetição, alternando os dois modos:
  - **sem o cache:** `docker run -d --entrypoint java <imagem> -XX:MaxRAMPercentage=75 -XX:AOTMode=off -jar application.jar`;
  - **com o cache:** `docker run -d <imagem>`, com o `ENTRYPOINT` normal.

  Medir do `docker run` à readiness 200, consultando a cada 0,1 s, e ler o `Started ... in X seconds` do log (no JSON, o campo da mensagem). Remover o container. No fim:
  - escrever no resumo uma tabela com cada tempo e a mediana de cada modo;
  - sair com `FALHA` se a mediana com o cache não for menor (SC-007);
  - sair com `FALHA` se o log do modo com o cache tiver um aviso de que o cache não pôde ser usado.

  Depois das repetições, um passo `Cache inválido não impede a subida` (FR-023, US4-4): `docker run -d --entrypoint sh <imagem> -c 'head -c 4096 app.aot > /tmp/ruim.aot && exec java -XX:MaxRAMPercentage=75 -XX:AOTCache=/tmp/ruim.aot -jar application.jar'`. A readiness precisa chegar a 200 em até 120 s, e o log precisa ter o aviso da JVM sobre o cache. Se não houver aviso ou se a readiness não chegar, `FALHA`.

  Rodar contra a imagem atual: precisa **falhar** (`application.jar` não existe na imagem do Marco 3).

### Implementação da etapa 4

- [ ] T039 **ADIADO (escopo reduzido, 2026-11-26; spec, Clarifications).** [US4] No `Dockerfile` (R8; Clarifications da spec, 2026-11-25):
  - no estágio de build, trocar o `extract --layers --launcher` por `extract --layers`, mantendo a criação das quatro pastas de camada;
  - no estágio final, copiar as quatro camadas para `/app` como hoje. **Verificar** que o `application.jar` e a pasta `lib/` ficam em `/app`;
  - acrescentar, antes do `USER`, o treino: `RUN java -XX:AOTCacheOutput=app.aot -Dspring.context.exit=onRefresh -jar application.jar && chmod 0444 app.aot`;
  - trocar o `ENTRYPOINT` por `["java", "-XX:MaxRAMPercentage=75", "-XX:AOTCache=app.aot", "-jar", "application.jar"]`.

  Não usar o processamento AOT do Spring. Atualizar os comentários: o motivo do classpath de JARs, o treino com o modo padrão (a escolha de perfil continua na execução) e o `AOTMode` padrão (`auto`), que ignora um cache inválido. Manter o usuário `10001`, o `HEALTHCHECK`, o `ENV` e a forma exec.
- [ ] T040 **ADIADO (escopo reduzido, 2026-11-26; spec, Clarifications).** [US4] **Verificar** os pontos da R8 e anotar na descrição do commit:
  - se o treino com `onRefresh` sai antes da carga do CSV e da abertura da porta: ler o log do `RUN` no `docker build --progress=plain`. Se o cache ficar sem ganho de tempo (T042), trocar pelo treino até a readiness descrito na R8 e atualizar a R8;
  - o tamanho do `app.aot` (`docker run --rm --entrypoint ls <imagem> -l app.aot`);
  - o texto exato do aviso da JVM com o cache inválido, que o passo do T038 procura no log. Se o texto da JVM 25 for outro que o previsto, ajustar o padrão do script;
  - que o `.github/scripts/smoke-test.sh` não depende do `JarLauncher` nem do caminho das classes. Se depender, ajustar só a referência, sem mudar os passos.
- [ ] T041 **ADIADO (escopo reduzido, 2026-11-26; spec, Clarifications).** [US4] Rodar localmente `hadolint` (`docker run --rm -i hadolint/hadolint < Dockerfile`, com a configuração do job `dockerfile-lint`), `docker build -t awards:local .`, `.github/scripts/smoke-test.sh awards:local` e `.github/scripts/compose-test.sh`: todos passam (FR-021, US4-1, US4-2, US4-6).
- [ ] T042 **ADIADO (escopo reduzido, 2026-11-26; spec, Clarifications).** [US4] Rodar `.github/scripts/startup-benchmark.sh awards:local 7`: precisa passar, com a mediana com o cache menor. Anotar a tabela, identificada como da máquina local.
- [ ] T043 **ADIADO (escopo reduzido, 2026-11-26; spec, Clarifications).** [US4] Em `.github/workflows/ci.yml`, job `image`: depois do teste de fumaça, o passo `Subida sem e com o cache AOT (7 repetições, mediana) e cache inválido` com `run: .github/scripts/startup-benchmark.sh awards:ci 7` (FR-022, FR-023). Atualizar o comentário do job com a spec 004, FR-020 a FR-023.
- [ ] T044 **ADIADO (escopo reduzido, 2026-11-26; spec, Clarifications).** [US4] Conferir com o relatório do Trivy da imagem (local, com a versão do T003) que o cache não trouxe pacote novo, e rodar `.github/scripts/check-workflows.sh` e `.github/scripts/check-docker-context.sh`: passam.
- [ ] T045 **ADIADO (escopo reduzido, 2026-11-26; spec, Clarifications).** [US4] Rodar `./mvnw verify` e `./mvnw spring-boot:run` (resultado de referência): os dois como no Marco 3, sem Docker (US4-5). Informar o resultado real.
- [ ] T046 **ADIADO (escopo reduzido, 2026-11-26; spec, Clarifications).** [US4] Commit `Etapa 4: cache AOT da JVM na imagem e medição da subida` com T038 a T045. Na descrição: as anotações do T040 e a tabela do T042.

**Checkpoint**: US4 completa; os números do pipeline vêm do job `image` (T063).

---

## Phase 6: Etapa 5 — SBOM e assinatura (US5, Priority: P2)

**Goal**: o pipeline gera o SBOM, assina a imagem e o atestado sem chave num registro temporário e
verifica a identidade, sem publicar nada.

**Independent Test**: o job `supply-chain` verde, com a verificação da identidade certa passando e a da
errada falhando; a política reprova qualquer push ou `id-token` fora do lugar.

### Verificação da etapa 5 (escrever primeiro; precisa falhar)

- [ ] T047 **ADIADO (escopo reduzido, 2026-11-26; spec, Clarifications).** [US5] Em `.github/scripts/check-workflows.sh` (R10, FR-026), mudar as regras:
  - regra 3: `id-token: write` passa a ser permitido **só** no job `supply-chain`. `security-events: write` continua permitido só no `codeql`. Qualquer outra escrita é violação;
  - regra 5: `push: true` no `docker/build-push-action` e `docker push` num `run:` passam a ser permitidos **só** no job `supply-chain`, e só quando todo valor de `tags` (ou o destino do `docker push`) começa por `localhost:5000/`. Fora disso, é violação, com o job e o destino na mensagem.

  Atualizar o cabeçalho do script. Testar numa pasta temporária fora do repositório, sem versionar os exemplos, com um caso que **precisa falhar** para cada situação abaixo, e um caso válido (o `ci.yml` do T048), que precisa passar:
  - `id-token: write` no job `image`;
  - `push: true` com `tags: ghcr.io/x/awards:1` no `supply-chain`;
  - `push: true` com `localhost:5000/awards` no job `image`;
  - `docker push ghcr.io/x/awards` num `run:` do `supply-chain`;
  - `packages: write` no `supply-chain`.

  Antes do T048, o `check-workflows.sh` com o `ci.yml` atual continua passando.

### Implementação da etapa 5

- [ ] T048 **ADIADO (escopo reduzido, 2026-11-26; spec, Clarifications).** [US5] Em `.github/workflows/ci.yml`, criar o job `supply-chain` (R9, R10; contracts/ci-pipeline.md):
  - configuração: `needs: dockerfile-lint`, `timeout-minutes: 20`, `permissions: { contents: read, id-token: write }`, e `if: github.event_name != 'pull_request' || github.event.pull_request.head.repo.full_name == github.repository`;
  - *service container* `registry` com `registry:3@sha256:<digest>` e `ports: ["5000:5000"]`;
  - passos:
    1. `actions/checkout` e `docker/setup-buildx-action` (os mesmos hashes), com `driver-opts: network=host`, para o buildx alcançar `localhost:5000`;
    2. `docker/build-push-action` (o mesmo hash) com `push: true`, `tags: localhost:5000/awards:ci` e o cache `gha`. Guardar `steps.<id>.outputs.digest`;
    3. Trivy (o mesmo hash) com `scan-type: image`, `image-ref: localhost:5000/awards@<digest>`, `format: cyclonedx` e `output: sbom.cdx.json`. **Verificar** se o Trivy lê o registro HTTP em `localhost`. Se não ler, há duas saídas: definir `TRIVY_INSECURE: "true"` no `env` do passo, ou acrescentar `load: true` ao build e analisar `localhost:5000/awards:ci` pelo Docker local, conferindo que o `RepoDigests` é o mesmo digest. Registrar a escolha na R10. Depois, um passo `jq` que **falha** se `lz4-java` ou `snappy-java` não estiverem no SBOM (FR-024);
    4. `sigstore/cosign-installer` fixado por hash, com as referências aninhadas conferidas e anotadas;
    5. `cosign sign --yes localhost:5000/awards@<digest>` e `cosign attest --yes --type cyclonedx --predicate sbom.cdx.json localhost:5000/awards@<digest>`;
    6. `cosign verify` e `cosign verify-attestation --type cyclonedx`, com `--certificate-identity "https://github.com/${{ github.repository }}/.github/workflows/ci.yml@${{ github.ref }}"` e `--certificate-oidc-issuer https://token.actions.githubusercontent.com`;
    7. um passo que verifica com `--certificate-identity "https://github.com/outro/repositorio/.github/workflows/ci.yml@${{ github.ref }}"` e **falha** se a verificação passar (US5-3);
    8. resumo com o digest, o índice da entrada no Rekor (lido da saída do `cosign sign` ou de `cosign tree`/`rekor-cli`) e os comandos de verificação de contracts/operacao.md;
    9. `actions/upload-artifact` de `sbom.cdx.json`, nome `sbom`.

  **Verificar** se o cosign aceita o registro HTTP em `localhost` sem flag; se não, usar `--allow-insecure-registry` e `--allow-http-registry` e atualizar a R10. Comentário do job com a spec 004, FR-024 a FR-027.
- [ ] T049 **ADIADO (escopo reduzido, 2026-11-26; spec, Clarifications).** [US5] Em `.github/workflows/ci.yml`, job `image`: acrescentar o passo `Assinatura pulada (pull request de fork)`, com `if: github.event_name == 'pull_request' && github.event.pull_request.head.repo.full_name != github.repository`, que escreve no `$GITHUB_STEP_SUMMARY` o motivo: a execução não tem a permissão de identidade, e a assinatura roda nos pushes e nos PRs do próprio repositório (FR-027).
- [ ] T050 **ADIADO (escopo reduzido, 2026-11-26; spec, Clarifications).** [US5] Rodar `.github/scripts/check-workflows.sh` com o `ci.yml` novo: passa. Repetir os casos negativos do T047: continuam falhando.
- [ ] T051 **ADIADO (escopo reduzido, 2026-11-26; spec, Clarifications).** [US5] Conferir localmente o SBOM: `docker run --rm -v /var/run/docker.sock:/var/run/docker.sock aquasec/trivy:<versão do T003> image --format cyclonedx --output - awards:local | jq` contém `lz4-java` 1.11.4 e `snappy-java` 1.1.10.12, e os pacotes do Alpine. A assinatura keyless só roda no Actions: `PULADO` local, com a comprovação no T063.
- [ ] T052 **ADIADO (escopo reduzido, 2026-11-26; spec, Clarifications).** [US5] Rodar `./mvnw verify` e `./mvnw -Pextras verify`: verdes, sem Docker. Informar o resultado real.
- [ ] T053 **ADIADO (escopo reduzido, 2026-11-26; spec, Clarifications).** [US5] Commit `Etapa 5: SBOM e assinatura keyless num registro temporário` com T047 a T052. Na descrição: os pares tag → hash e tag → digest, a conferência das referências aninhadas do `cosign-installer`, os casos do T047 e o resultado do T051.

**Checkpoint**: US5 completa, com a comprovação no pipeline (T063).

---

## Phase 7: Etapa 6, parte 1 — Dev container (US6, Priority: P3)

**Goal**: o projeto abre pronto num dev container, com o `verify` comprovado no pipeline.

**Independent Test**: o job `devcontainer` roda `java -version`, `./mvnw -v`, `docker version` e `./mvnw
verify` dentro do container.

### Verificação (escrever primeiro; precisa falhar)

- [ ] T054 **ADIADO (escopo reduzido, 2026-11-26; spec, Clarifications).** [US6] Em `.github/scripts/check-workflows.sh`, regra 6: exigir também o ecossistema `devcontainers` no `.github/dependabot.yml`, com `directory: /` e `schedule.interval` (R11). Testar numa pasta temporária: o `dependabot.yml` atual, sem `devcontainers`, **precisa falhar**. Rodar no repositório: falha até o T056.

### Implementação

- [ ] T055 **ADIADO (escopo reduzido, 2026-11-26; spec, Clarifications).** [US6] Criar `.devcontainer/devcontainer.json` (R11; contracts/operacao.md, "Dev container"):
  - **verificar** se existe a variante do JDK 25 da imagem `mcr.microsoft.com/devcontainers/java`. Se existir, usá-la por tag e digest. Se não, usar `mcr.microsoft.com/devcontainers/base` com o recurso `ghcr.io/devcontainers/features/java` (`version: 25`, `jdkDistro: tem`, `installMaven: false`) e atualizar a R11;
  - recurso `ghcr.io/devcontainers/features/docker-in-docker` fixado por digest (`@sha256:<digest>`, resolvido com `docker buildx imagetools inspect ghcr.io/devcontainers/features/docker-in-docker:<x.y.z>` ou `oras manifest fetch`), com a versão `<x.y.z>` num comentário e o compose v2. Se o recurso do Java for usado (alternativa acima), também por digest;
  - `forwardPorts: [8080, 9090, 3000]`, com `portsAttributes` nomeando a API, o Prometheus e o Grafana;
  - `postCreateCommand: ./mvnw -B -q dependency:go-offline`;
  - nenhum `containerEnv` com segredo;
  - comentário (JSONC) com a spec 004, FR-028 e FR-029.
- [ ] T056 **ADIADO (escopo reduzido, 2026-11-26; spec, Clarifications).** [US6] Em `.github/dependabot.yml`, acrescentar o ecossistema `devcontainers`, com `directory: /` e `schedule.interval: weekly`, e o comentário da spec 004. Rodar `.github/scripts/check-workflows.sh`: passa.
- [ ] T057 **ADIADO (escopo reduzido, 2026-11-26; spec, Clarifications).** [US6] Em `.github/workflows/ci.yml`, criar o job `devcontainer` (FR-030): `timeout-minutes: 30`, `actions/checkout` e `devcontainers/ci` fixado por hash, com as referências aninhadas conferidas, `push: never` e `runCmd: java -version && ./mvnw -v && docker version && ./mvnw -B verify && docker compose up -d --build --wait && curl -fsS http://localhost:8080/api/producers/intervals | jq -e '.min[0].producer == "Joel Silver" and .min[0].previousWin == 1990 and .min[0].followingWin == 1991 and .max[0].producer == "Matthew Vaughn" and .max[0].previousWin == 2002 and .max[0].followingWin == 2015' && docker compose down -v`. Isso comprova o compose dentro do dev container (FR-029). **Verificar** se o `jq` e o `curl` existem na imagem do dev container. Se não existirem, instalá-los no `postCreateCommand` do T055. As portas encaminhadas para a máquina não são observáveis no runner e ficam como item de revisão no checklist (T066). Comentário com a spec 004, FR-028 a FR-030. Rodar `.github/scripts/check-workflows.sh`: passa (o `push: never` não é publicação; **verificar** que a regra 5 não o confunde com `push: true`).
- [ ] T058 **ADIADO (escopo reduzido, 2026-11-26; spec, Clarifications).** [US6] Localmente, se houver o Docker e o Node: `npx --yes @devcontainers/cli@<versão> up --workspace-folder .` e `... exec --workspace-folder . bash -lc "java -version && ./mvnw -v && docker version && ./mvnw -B verify"`; depois, dentro do container, `docker compose up -d --build --wait` e o resultado de referência pela porta encaminhada (US6-3, US6-4). Sem as ferramentas, `PULADO` com o motivo; a comprovação fica no T063.

**Checkpoint**: US6 completa, com a comprovação no pipeline (T063).

---

## Phase 8: Etapa 6, parte 2 — Documentação e fechamento

**Purpose**: a documentação do marco, os registros e a confirmação no pipeline. Papel de documentação para
os arquivos de `docs/` e o README.

- [X] T059 [P] Criar `docs/slo.md` (FR-003; data-model.md, "Objetivo de SLO"): os dois objetivos (disponibilidade de 99,5% e 99% das requisições dos intervalos até o bucket do T026, mostrando o valor exato), a janela de 28 dias, "ruim = 5xx ou alvo sem coleta; boa = 2xx, 304 e 4xx", e, na latência, que o total é o de todas as requisições aos intervalos, com qualquer status (como no T028), a fórmula do orçamento e das taxas de consumo, a origem dos números (link para a entrada do T034), os limites do k6 e a folga de 2× sobre a maior medição, com o motivo, e a tabela dos alertas com os links do runbook.
- [X] T060 [P] Criar `docs/runbook.md` (FR-007): uma seção por alerta, com o título igual ao nome do alerta, para a âncora em minúsculas do `runbook_url` funcionar (`#awardsintervalsavailabilitybudgetburn`, `#awardsintervalslatencybudgetburn`, `#awardsappdown`). Cada seção traz o que o alerta significa, o que conferir (painéis do dashboard, `docker compose logs app`, readiness, atraso do consumidor, PostgreSQL e Kafka) e o que fazer. Conferir que as três âncoras abrem a seção certa no GitHub (conclui o T035).
- [ ] T061 **ADIADO (escopo reduzido, 2026-11-26; spec, Clarifications).** [P] Criar os ADRs `docs/adr/0020-slo-e-alertas-por-consumo-do-orcamento.md`, `0021-teste-de-carga-com-k6-no-pipeline.md`, `0022-gerador-de-csv-deterministico.md`, `0023-cache-aot-da-jvm.md`, `0024-sbom-e-assinatura-keyless-sem-publicacao.md` e `0025-dev-container-e-inventario-efetivo-de-dependencias.md`, no formato dos anteriores (FR-037), cada um com o contexto, a decisão, as alternativas rejeitadas da research e as consequências. Atualizar `docs/adr/README.md` com os seis.
- [X] T062 **Escopo reduzido (2026-11-26): seção do Marco 4 com o roteiro dos itens adiados; sem cache AOT, assinatura nem dev container.** Atualizar o `README.md` (FR-018, FR-036):
  - seção do Marco 4 entre os extras, depois do Marco 3. Ela cobre:
    - alertas e SLO, com links para `docs/slo.md` e `docs/runbook.md`;
    - teste de carga (como rodar, os números de uma execução verde do job `load` e o link da execução);
    - gerador de CSV (uso, o SHA-256 do padrão no Linux e no Windows do T015, os tempos de carga direta e por Kafka do job `load`, e o maior tamanho que carrega sem erro de memória, medido no T021);
    - cache AOT (as medianas do job `image`);
    - SBOM e assinatura (o que é assinado, onde fica o SBOM, como conferir a entrada no Rekor, sem publicação da imagem);
    - dev container (como abrir e o requisito de Docker aninhado);
  - tabela "Requisitos do Marco 4" com a comprovação de cada requisito, como na "Rastreabilidade" do plano;
  - estrutura do repositório com `tools/`, `load/`, `.devcontainer/`, `docs/slo.md` e `docs/runbook.md`;
  - conferir que "Avalie em 2 minutos" continua só com o JDK.

  Os números vêm do T063. Se o T005 ficou `PENDENTE`, a pendência vai aqui também.
- [ ] T063 **Escopo reduzido: os 10 jobs, sem `supply-chain` nem `devcontainer`.** **Pendente: exige o push, que é do Anderson.** Conferir na execução do branch:
  - os 12 jobs verdes: `workflow-policy`, `build`, `postgres`, `codeql`, `dependencies`, `dockerfile-lint`, `image`, `compose`, `alert-rules`, `load`, `supply-chain` e `devcontainer`;
  - no `dependencies`: `lz4-java` e `snappy-java` no resumo (SC-010);
  - no `image`: as medianas sem e com o cache (SC-007);
  - no `load`: os dois hashes iguais (e iguais ao do T015), os tempos de carga e os limites finais respeitados (SC-004 a SC-006);
  - no `supply-chain`: o digest, o índice do Rekor, a verificação certa `OK` e a errada falhando como esperado, e o SBOM nos artefatos (SC-008);
  - no `devcontainer`: o `verify` verde, o compose pronto e o resultado de referência (SC-009, FR-029);
  - no `compose`: o passo 4b `OK` (SC-003).

  Copiar os números para o README (T062).
- [ ] T064 **Escopo reduzido: 10 checks (os 8 do Marco 3, `alert-rules` e `load`).** **Pedido ao Anderson, depois da primeira execução do pipeline no PR e antes do merge** (FR-031, R14; T093 da spec 003). Configurar a proteção da `main` (Settings → Branches, ou o ruleset da `main`):
  - os **12** checks do T063 como *required status checks*;
  - o **bypass para administradores desligado** ("Do not allow bypassing the above settings", ou nenhum *bypass actor* no ruleset).

  Conferir na página do PR que os 12 aparecem como obrigatórios. Registrar em `docs/ai/decisoes.md`, numa entrada "Proteção da main depois do Marco 4", a lista configurada e a confirmação do bypass desligado, sem nenhum dado pessoal.
- [ ] T065 **ADIADO (escopo reduzido, 2026-11-26; spec, Clarifications).** Rodar as seções 1 a 5 e 7 de `specs/004-acabamento-operacional/quickstart.md`, num clone limpo do branch: só com o JDK, a seção 1 e a 2; com o Docker, as demais. Anotar os resultados e os `PULADO` na descrição do commit.
- [ ] T066 **ADIADO (escopo reduzido, 2026-11-26; spec, Clarifications).** Registrar em `docs/ai/decisoes.md` o fechamento do bolt: o resultado do T012 (entrada própria), do T063 e do T065, os `PULADO`, o estado do T005 e as pendências. Marcar na seção "Revisão da documentação" de `specs/004-acabamento-operacional/checklists/requirements.md` os itens conferidos, inclusive o das portas encaminhadas do dev container (FR-029), a partir do T058 ou de uma abertura manual no VS Code.
- [X] T067 **Escopo reduzido: Status da spec 004 atualizado; o da spec 003 fica para o fechamento do marco.** Rodar `./mvnw verify` e `./mvnw -Pextras verify`: verdes, sem Docker. Informar o resultado real. Atualizar o **Status** de `specs/004-acabamento-operacional/spec.md` para `Implemented`, listando o que ainda depende de push ou do Anderson. Atualizar o **Status** de `specs/003-modo-docker/spec.md`, retirando as pendências fechadas (T050 e T093 da spec 003).
- [X] T068 **Escopo reduzido: commit "Marco 4 com escopo reduzido: README, roteiro e etapas 4 a 6 adiadas".** Commit `Etapa 6: dev container, SLO, runbook, ADRs e README do Marco 4` com T054 a T067. Avisar o Anderson de que o bolt está pronto para o PR. Depois do merge, a sessão é exportada para `docs/ai/sessoes/`.

---

## Dependencies & Execution Order

### Entre etapas

- **Preparação (T001, T002)** → **Etapa 1**.
- **Etapa 1** → **Etapa 2**:
  - uma correção do testador adversário (T005) muda o código que a imagem embarca;
  - a versão do Trivy do T003 é usada no T044 e no T051.
- **Etapa 2** → **T025 (push e 5 "Re-run jobs")** → **Etapa 3**: os números do SLO e os limites finais do
  k6 vêm da medição. É o único bloqueio externo no meio do bolt.
- **Etapa 3** → **Etapa 4**: o `compose-test.sh` com o passo 4b é usado no T041.
- **Etapas 4 e 5** são independentes entre si. A ordem 4 → 5 é a do plano, e o T051 usa a imagem do T041.
- **Etapa 6** depende de todas: a documentação usa os números do T063.

### Dentro de cada etapa

- A verificação que precisa falhar (T003, T013, T027, T038, T047, T054) vem antes da implementação.
- O `check-workflows.sh` roda depois de toda mudança no `ci.yml`, no `compose.yaml` ou no `dependabot.yml`.
- O `./mvnw verify` verde e o commit fecham a etapa.

### Histórias

- **US7 (P1)**: etapa 1; T012 e T064 dependem do push.
- **US3 (P2)** → **US2 (P1)**: o teste de carga usa o arquivo gerado.
- **US1 (P1)**: depende da medição da US2 (T025).
- **US4, US5, US6**: independentes das demais, salvo a ordem de commits.

## Parallel Opportunities

- T014 (gerador) e T017 (`compose.load.yaml`) mexem em arquivos diferentes.
- T059, T060 e T061 (SLO, runbook e ADRs) são do papel de documentação, em arquivos diferentes.
- As etapas 4 e 5 poderiam correr em worktrees separados, mas as duas mexem no `ci.yml` (job `image`). Por
  isso ficam em sequência: um dono por arquivo por rodada (`AGENTS.md`).

### Exemplo: etapa 6, documentação em paralelo

```text
Task: "Criar docs/slo.md (T059)"
Task: "Criar docs/runbook.md (T060)"
Task: "Criar os ADRs 0020 a 0025 (T061)"
```

## Implementation Strategy

### MVP

A etapa 1 (US7) fecha uma lacuna real de segurança e as pendências de um marco já mergeado, sem depender
de nada novo. Ela sozinha já pode ser revisada.

### Incremental

Cada etapa é um commit com o `./mvnw verify` verde e entrega uma história completa:

| Etapa | Entrega |
| --- | --- |
| 1 | US7 |
| 2 | US3 e US2 |
| 3 | US1 |
| 4 | US4 |
| 5 | US5 |
| 6 | US6 e a documentação |

O PR é um só, no fim.

### Corte

Se o prazo apertar, vale a ordem de corte do briefing: cache AOT, dados gerados, k6 e assinatura saem
primeiro. Um extra só entra completo:
- cortar a US4 retira a etapa 4 inteira;
- cortar a US2 obriga a cortar a US1, porque os números dela vêm da US2;
- a etapa 1 nunca é cortada.

## Notes

- 68 tarefas. Os IDs seguem a ordem de execução.
- O `docs/ai/prompts.md` é gravado pelo hook: não editar nem incluir à mão em commits de etapa.
- Nenhum token, senha ou dado pessoal nos registros. O token do k6, quando usado, é o de demonstração ou
  um gerado no job, nunca impresso.
