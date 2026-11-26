---

description: "Tarefas do Marco 5: publicação da API no Google Cloud Run"
---

# Tasks: Marco 5 — Publicação da API no Google Cloud Run

**Input**: documentos de `specs/005-deploy-cloud-run/` (spec, plan, research, data-model, contracts e quickstart)

**Prerequisites**: plan.md, spec.md, research.md, data-model.md e contracts/

**Tests**: nenhum IT novo (FR-022 e FR-026). A comprovação é pelo pipeline, pelas duas execuções do `setup.sh` e
pelas conferências locais rápidas indicadas em cada tarefa. O que o pipeline comprova não é repetido na máquina.

**Organization**: as fases seguem as **etapas do bolt** do plano. Cada etapa termina com `./mvnw verify` e
`./mvnw -Pextras verify` verdes, com o resultado real informado, e com **um commit**. As tarefas levam a
história que atendem. A etapa 1 (US4) vem antes das outras, apesar de ser P2, porque todas dependem dela.

**Paradas:** o implementador só para nas tarefas marcadas **[Anderson]** (push, merge e configurações no
GitHub) ou quando algo falha. O `setup.sh` é executado pelo Claude Code com o login do Anderson, que já está
feito (conta ativa e projeto `outsera-awards`).

**Ambiente local (Windows, Git Bash):** antes de qualquer `gcloud`, rodar `source ~/.bashrc`, que define o
`CLOUDSDK_PYTHON` (research R9). `jq` e `yq` não estão instalados.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: pode rodar em paralelo (arquivos diferentes, sem dependência pendente)
- **[Story]**: história da spec atendida (US1 a US5)
- **[Anderson]**: passo que depende do Anderson; o implementador para e pede

---

## Phase 1: Setup

**Purpose**: estrutura dos arquivos novos

- [X] T001 Criar `deploy/gcp/cleanup-policy.json` com a política de limpeza da research R9 (FR-021): uma regra
  `DELETE` com `tagState: ANY` e `olderThan: 30d`, e uma regra `KEEP` com `mostRecentVersions.keepCount: 10`,
  que prevalece sobre a de apagar, no formato aceito por
  `gcloud artifacts repositories set-cleanup-policies --policy=<arquivo>`. Confirmar que o
  `.dockerignore` (lista de permissão) já deixa `deploy/` fora do contexto da imagem, sem mudá-lo.

---

## Phase 2: Etapa 1 — Configuração do GCP (US4, bloqueia as demais) 🧱

**Goal**: o projeto `outsera-awards` fica configurado por um script versionado e idempotente, e o serviço existe
com a URL pública conhecida.

**Independent Test**: duas execuções seguidas de `deploy/gcp/setup.sh`. A segunda termina com código 0, todos os
passos em `OK (já existia)` e zero mudanças (SC-004).

- [X] T002 [US4] Criar `deploy/gcp/setup.sh` (executável, `#!/usr/bin/env bash`, `set -euo pipefail`), conforme
  [`contracts/setup-script.md`](contracts/setup-script.md):
  - constantes no topo: `PROJECT_ID=outsera-awards`, `REGION=southamerica-east1`,
    `GITHUB_REPO=andersonfelipediefenbach/outsera`, `REPOSITORY=awards`, `SERVICE=awards`, `POOL=github`,
    `PROVIDER=github-actions`, `DEPLOY_SA=github-deployer`, `RUNTIME_SA=awards-runtime` e
    `PLACEHOLDER_IMAGE=us-docker.pkg.dev/cloudrun/container/hello`;
  - funções `record`/`run_step` no estilo do `.github/scripts/smoke-test.sh`, com os resultados
    `OK (já existia)`, `OK (criado)`, `OK (ajustado)`, `OK (restaurado)` e `FALHA`, e um contador de mudanças;
  - wrapper `gc()` que chama o `gcloud` com `--quiet` e remove o `\r` da saída;
  - no Git Bash, se `CLOUDSDK_PYTHON` não estiver definido, apontar para
    `<dir do gcloud>/../platform/bundledpython/python.exe`;
  - passo 0, sem nenhuma escrita: `gcloud` e `curl` no PATH (código 2 se faltar); conta ativa, **sem imprimir
    o e-mail** (a saída vai para a evidência); `gcloud projects describe` com `lifecycleState=ACTIVE`, de onde
    sai o `PROJECT_NUMBER`; `gcloud billing projects describe` com `billingEnabled=True`; `repository_id` e
    `owner.id` lidos de `https://api.github.com/repos/$GITHUB_REPO` com `curl` e extraídos sem `jq` (por
    exemplo, com `sed`), validados como números. Qualquer falha termina com código 1 antes do passo 1.
- [X] T003 [US4] Em `deploy/gcp/setup.sh`, passos 1 a 4 (research R9):
  - passo 1: ativar `run`, `artifactregistry`, `iam`, `iamcredentials` e `sts` (`.googleapis.com`) só se
    faltarem na lista de `gcloud services list --enabled`;
  - passo 2: criar o repositório Docker `awards` em `southamerica-east1`, se não existir, e aplicar
    `deploy/gcp/cleanup-policy.json` com `--no-dry-run` só se a política atual for diferente;
  - passo 3: criar a conta `awards-runtime` (nome de exibição "Execução do serviço awards"), se não existir;
  - passo 4: criar a conta `github-deployer` (nome de exibição "Publicação pelo GitHub Actions"), se não
    existir, e dar `FALHA` se `gcloud iam service-accounts keys list --managed-by=user` listar alguma chave
    (SC-003).
- [X] T004 [US4] Em `deploy/gcp/setup.sh`, passos 5 e 6 (research R3):
  - pool `github` (global): criar se não existir e restaurar (`undelete`) se estiver `DELETED`;
  - provedor OIDC `github-actions`: emissor `https://token.actions.githubusercontent.com`, mapeamento
    `google.subject=assertion.sub,attribute.repository=assertion.repository,attribute.repository_id=assertion.repository_id,attribute.repository_owner_id=assertion.repository_owner_id,attribute.ref=assertion.ref`
    e condição
    `assertion.repository_id == '<id>' && assertion.repository_owner_id == '<id do dono>' && assertion.ref == 'refs/heads/main'`;
  - criar se não existir, restaurar se `DELETED` e atualizar (`OK (ajustado)`) se o mapeamento ou a condição
    divergirem.
- [X] T005 [US4] Em `deploy/gcp/setup.sh`, passo 7 (research R4 e R5): se o serviço `awards` não existir, criar
  com
  `gcloud run deploy awards --image=$PLACEHOLDER_IMAGE --region=$REGION --service-account=awards-runtime@… --min-instances=0 --max-instances=1 --cpu=1 --memory=1Gi --no-allow-unauthenticated`.
  Se existir, não implantar nada: só conferir que a conta de execução é a `awards-runtime` e dar `FALHA` se não
  for. O acesso público vem no passo 8.
- [X] T006 [US4] Em `deploy/gcp/setup.sh`, passo 8: os vínculos de IAM da tabela de
  [`data-model.md`](data-model.md), cada um com um comentário com o motivo (FR-018). Antes de cada
  `add-iam-policy-binding`, ler a política com `get-iam-policy --flatten=bindings[].members` e só adicionar o
  que falta:
  - `principalSet://iam.googleapis.com/projects/<n>/locations/global/workloadIdentityPools/github/attribute.repository_id/<id>`
    com `roles/iam.workloadIdentityUser` na conta `github-deployer`;
  - `github-deployer` com `roles/artifactregistry.writer` no repositório `awards`;
  - `github-deployer` com `roles/run.developer` no serviço `awards`;
  - `github-deployer` com `roles/iam.serviceAccountUser` na conta `awards-runtime`;
  - `allUsers` com `roles/run.invoker` no serviço `awards`.
- [X] T007 [US4] Em `deploy/gcp/setup.sh`, passo 9 e resumo:
  - imprimir as linhas `GCP_PROJECT_ID`, `GCP_PROJECT_NUMBER`, `GCP_REGION`, `GCP_WIF_PROVIDER`,
    `GCP_DEPLOY_SA`, `GCP_RUNTIME_SA`, `GCP_REGISTRY`, `GCP_SERVICE` e `URL`, a URL lida de
    `gcloud run services describe --format='value(status.url)'`;
  - depois, a contagem por resultado e o número de mudanças;
  - código 0 sem `FALHA`, 1 com alguma.
- [X] T008 [US4] Rodar `source ~/.bashrc && deploy/gcp/setup.sh` pela primeira vez e gravar a saída em
  `specs/005-deploy-cloud-run/evidencias/setup-execucao-1.txt`. Conferir que a saída não tem e-mail pessoal nem
  token. Se algum passo der `FALHA`, corrigir o script e rodar de novo; parar e informar se a falha não for do
  script (permissão, faturamento ou cota).
- [X] T009 [US4] Rodar o script pela segunda vez e gravar a saída em
  `specs/005-deploy-cloud-run/evidencias/setup-execucao-2.txt`. Esperado: código 0, só `OK (já existia)` e
  zero mudanças (SC-004). Se não, corrigir a idempotência e repetir T008 e T009.
- [X] T010 [US4] Conferências rápidas (quickstart, passo 1), com o resultado anexado a
  `specs/005-deploy-cloud-run/evidencias/setup-conferencias.txt`:
  - `gcloud iam service-accounts keys list --managed-by=user` da `github-deployer`, com a lista vazia;
  - a `attributeCondition` do provedor;
  - `curl -s -o /dev/null -w '%{http_code}' <URL>`, com 200 da página de exemplo e acesso anônimo.
- [X] T011 [US4] Rodar `./mvnw verify` e `./mvnw -Pextras verify`, informar o resultado real e fazer o commit
  "Marco 5, etapa 1: configuração idempotente do GCP" com `deploy/gcp/` e `specs/005-deploy-cloud-run/evidencias/`.

**Checkpoint**: o GCP está configurado, e a URL do serviço e os identificadores públicos são conhecidos.

---

## Phase 3: Etapa 2 — Política do workflow e imagem como artefato (US2) 🔒

**Goal**: a política aceita só as exceções do job `deploy`, e o job `image` passa a entregar a imagem testada
como tar OCI com o digest.

**Independent Test**: o `check-workflows.sh` passa no workflow real e reprova os casos negativos 1, 2, 6 e 7 de
[`contracts/workflow-policy.md`](contracts/workflow-policy.md). Os casos 3 a 5 dependem do job `deploy` e ficam
na etapa 3. No PR, o `workflow-policy` e o `image` ficam verdes (comprovação do pipeline, depois do push).

- [ ] T012 [P] [US2] Em `.github/scripts/check-workflows.sh`, regra 3: aceitar `id-token: write` só no job
  `deploy`, mantendo `security-events: write` só no `codeql`. Atualizar o cabeçalho de comentários com a lista
  de regras.
- [ ] T013 [US2] Em `.github/scripts/check-workflows.sh`, regra 5: além de `push: true` e `docker push` em
  qualquer job, reprovar qualquer passo `run` com `skopeo` fora do job `deploy`. A regra percorre os jobs
  (`.jobs | to_entries[]`) para saber o job de cada passo.
- [ ] T014 [US2] Em `.github/scripts/check-workflows.sh`, regra 9 (só quando existe o job `deploy`):
  - o `if`, normalizado (sem `${{ }}`, sem espaços nas pontas e com espaços repetidos reduzidos a um), é
    `github.event_name == 'push' && github.ref == 'refs/heads/main' && github.repository == 'andersonfelipediefenbach/outsera'`;
  - os `needs` contêm `workflow-policy`, `build`, `postgres`, `codeql`, `dependencies`, `dockerfile-lint`,
    `image`, `alert-rules`, `load` e `compose`, com um comentário dizendo que a lista acompanha os checks
    obrigatórios da `main` e muda junto com eles (FR-008);
  - a `concurrency` é `group: deploy-cloud-run` com `cancel-in-progress: false`;
  - não há `docker/build-push-action` em `uses`, nem `docker build` ou `docker buildx build` em `run`.
- [ ] T015 [US2] Em `.github/scripts/check-workflows.sh`, regra 10: reprovar se o `concurrency.cancel-in-progress`
  do topo for o booleano `true`.
- [ ] T016 [US2] Em `.github/workflows/ci.yml`, trocar o `cancel-in-progress: true` do topo por
  `cancel-in-progress: ${{ github.ref != 'refs/heads/main' }}` e atualizar o comentário do cabeçalho (o
  workflow passa a publicar na `main`).
- [ ] T017 [US2] Em `.github/workflows/ci.yml`, job `image` ([`contracts/ci-deploy.md`](contracts/ci-deploy.md)):
  - no `docker/build-push-action`, trocar `load: true` e `tags` por
    `outputs: | type=docker,name=awards:ci` e `type=oci,dest=${{ runner.temp }}/awards-oci.tar`, e acrescentar
    `provenance: false` e `sbom: false`;
  - passo novo, depois do teste de fumaça, `id: digest`, que:
    - lê o `index.json` do tar com `tar -xOf … index.json | jq`;
    - exige exatamente um manifesto, com o `mediaType` de manifesto de imagem;
    - lê o manifesto e o digest da configuração dentro do tar;
    - confere que `docker image inspect awards:ci --format '{{.Id}}'` é o digest da configuração ou o do
      manifesto, e reprova em qualquer outro caso;
    - grava `digest=sha256:…` em `$GITHUB_OUTPUT` e no resumo;
  - `outputs: digest: ${{ steps.digest.outputs.digest }}` no job;
  - passo `actions/upload-artifact@cf430e030ddbb5b0abf93d22962f4752f3646cd9 # v7.0.2` com
    `name: awards-oci`, o tar, `retention-days: 1` e `if-no-files-found: error`, condicionado a
    `github.event_name == 'push' && github.ref == 'refs/heads/main'`.
- [ ] T018 [US2] Conferência local rápida da política:
  - baixar o `yq` v4 (binário do Windows, versão fixada, SHA-256 conferido com o `checksums` do release) para o
    scratchpad e pô-lo no PATH só nesse comando;
  - rodar `.github/scripts/check-workflows.sh`, que precisa passar;
  - para os casos 1, 2, 6 e 7 de [`contracts/workflow-policy.md`](contracts/workflow-policy.md), copiar `.github`,
    `compose.yaml` e o `dependabot.yml` para um diretório temporário, aplicar o caso e rodar
    `check-workflows.sh <raiz temporária>`, que precisa reprovar com a mensagem da regra;
  - gravar a saída em `specs/005-deploy-cloud-run/evidencias/politica-etapa-2.txt`.
- [ ] T019 [US2] Rodar `./mvnw verify` e `./mvnw -Pextras verify`, informar o resultado real e fazer o commit
  "Marco 5, etapa 2: política do workflow e imagem testada como artefato OCI".

**Checkpoint**: a política está pronta para o job `deploy`, e a imagem testada sai do job `image` com o digest.

---

## Phase 4: Etapa 3 — Job `deploy` e conferência da URL (US1, US2 e US3) 🎯 MVP

**Goal**: cada push na `main` publica a imagem testada sem tráfego, confere pela URL da revisão, promove e
confere de novo na URL pública.

**Independent Test**: o `verify-deployment.sh` aprova contra a aplicação local com o CSV padrão e reprova com um
CSV de cenário (comprovação local rápida, sem Docker). A publicação real é comprovada pelo pipeline depois do
merge (Phase 6).

- [ ] T020 [P] [US3] Criar `.github/scripts/verify-deployment.sh` (executável, `set -euo pipefail`), conforme
  [`contracts/verify-deployment.md`](contracts/verify-deployment.md):
  - uso `verify-deployment.sh [--expect-no-metrics] <URL base> [espera máxima, padrão 180]`;
  - o tempo de cada chamada (`curl -w '%{time_total}'`) e o tempo da primeira chamada até a readiness na saída
    e no resumo (SC-001);
  - passo 4, só com `--expect-no-metrics`: `/actuator/prometheus` com 404 (FR-004a);
  - código 2 se faltar `curl` ou `jq`;
  - passo 1: readiness 200 com `{"status":"UP"}`, a cada 3 s, com `--max-time 10` por chamada, até a espera
    máxima;
  - passo 2: `/api/producers/intervals` com 200, `Content-Type` `application/json` e o corpo igual, em
    `jq -S`, à `REFERENCE` copiada do `.github/scripts/smoke-test.sh` (linha 100), com um comentário que aponta
    a origem;
  - passo 3: `/swagger-ui.html` com `-L` e 200, e `/openapi.yaml` com 200;
  - em `FALHA`, o status e o corpo truncados em 2 KB;
  - com `GITHUB_STEP_SUMMARY` definido, uma tabela com a URL, os passos e o tempo até a readiness;
  - código 0 ou 1.
- [ ] T021 [US3] Conferência local rápida do script, sem Docker. O `jq` não está instalado: usar um binário
  baixado para o scratchpad, com versão fixada e SHA-256 conferido. Sequência:
  - `./mvnw -q -DskipTests package` uma vez;
  - subir o JAR com `PORT=18080` em segundo plano e rodar `verify-deployment.sh http://127.0.0.1:18080 120`,
    que precisa sair com código 0;
  - parar a aplicação e subir de novo com `MOVIES_CSV_PATH=file:src/test/resources/scenarios/disjoint-producers.csv`;
  - rodar `verify-deployment.sh http://127.0.0.1:18080 60`, que precisa sair com código 1 e o passo 2 em
    `FALHA`;
  - parar a aplicação e gravar as duas saídas em `specs/005-deploy-cloud-run/evidencias/verify-local.txt`.
- [ ] T022 [P] [US1] Conferir na versão local do `gcloud` (594.0.1, a que o workflow fixa) que o
  `gcloud run deploy --help` tem `--startup-probe`, `--liveness-probe`, `--cpu-boost`, `--no-traffic` e
  `--tag`. Se faltar algum flag de probe, aplicar a alternativa da research R5: criar
  `deploy/gcp/service.yaml` e usar `gcloud run services replace`, ajustando T024 e T025. Registrar o resultado
  em `specs/005-deploy-cloud-run/evidencias/gcloud-flags.txt`.
- [ ] T023 [US2] Em `.github/workflows/ci.yml`, criar o job `deploy` com o cabeçalho do contrato
  [`ci-deploy.md`](contracts/ci-deploy.md):
  - `name`, `if`, `needs` (os 10 checks), `permissions` (`contents: read` e `id-token: write`), `concurrency`
    (`deploy-cloud-run`, sem cancelamento), `timeout-minutes: 15` e `environment` (`name: cloud-run` e
    `url: ${{ env.SERVICE_URL }}`, ou a URL literal da T009, se a expressão não for aceita no `environment`);
  - `env` com os identificadores públicos da saída da T009 (`GCP_PROJECT_ID`, `GCP_PROJECT_NUMBER`,
    `GCP_REGION`, `GCP_WIF_PROVIDER`, `GCP_DEPLOY_SA`, `GCP_RUNTIME_SA`, `GCP_REGISTRY`, `GCP_SERVICE` e
    `SERVICE_URL`);
  - os passos 1 a 6 do contrato:
    - `actions/checkout@3d3c42e5aac5ba805825da76410c181273ba90b1 # v7.0.1`;
    - `actions/download-artifact@9000827ccba6bdab643e8b6fd33ac0654aef8333 # v8.0.2` (`awards-oci`);
    - a conferência do digest do `index.json` contra `needs.image.outputs.digest`;
    - `google-github-actions/auth@7c6bc770dae815cd3e89ee6cdf493a5fab2cc093 # v3.0.0`, com
      `workload_identity_provider` e `service_account`;
    - `google-github-actions/setup-gcloud@aa5489c8933f4cc7a4f7d45035b3b1440c9c10db # v3.0.1`, com
      `version: '594.0.1'`;
    - o envio com `skopeo`: conferir `command -v skopeo`, fazer
      `gcloud auth print-access-token | skopeo login -u oauth2accesstoken --password-stdin southamerica-east1-docker.pkg.dev`,
      depois `skopeo copy --preserve-digests oci-archive:… docker://$GCP_REGISTRY/awards:${{ github.sha }}` e
      conferir o digest no registro com `skopeo inspect --format '{{.Digest}}'`.
- [ ] T024 [US1] No job `deploy`, passo 7:
  - `gcloud run deploy "$GCP_SERVICE" --image="$GCP_REGISTRY/awards@$DIGEST" --region="$GCP_REGION"`,
    com `--service-account="$GCP_RUNTIME_SA" --min-instances=0 --max-instances=1 --cpu=1 --memory=1Gi`,
    `--cpu-boost --port=8080 --no-traffic --tag="c-${GITHUB_SHA::7}"` e os probes da research R5;
  - startup em `/actuator/health/readiness`, com período de 2 s e limite de 60 falhas; liveness em
    `/actuator/health/liveness`, com período de 30 s;
  - uma única variável de ambiente nova, `--set-env-vars=MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=health`
    (FR-004a), que fecha as métricas e mantém os probes;
  - ler o nome da revisão e a URL da tag com `gcloud run services describe --format`.
- [ ] T025 [US3] No job `deploy`, passos 8 a 11:
  - `verify-deployment.sh --expect-no-metrics "<URL da tag>"`;
  - `gcloud run services update-traffic "$GCP_SERVICE" --to-revisions=<revisão>=100`, com a remoção das tags
    anteriores (`--remove-tags`);
  - `verify-deployment.sh --expect-no-metrics "$SERVICE_URL"`;
  - o resumo com `if: always()`: URL, digest, revisão, resultado das duas conferências e duração (FR-016);
  - nenhum `secrets.` e nenhum build.
- [ ] T026 [US2] Conferência local rápida da política com o job `deploy`:
  - o `check-workflows.sh` real passa;
  - os casos 3, 4 e 5 de [`contracts/workflow-policy.md`](contracts/workflow-policy.md), numa cópia temporária,
    reprovam com a mensagem da regra 9;
  - a saída é anexada a `specs/005-deploy-cloud-run/evidencias/politica-etapa-3.txt`.
- [ ] T027 [US2] Rodar `./mvnw verify` e `./mvnw -Pextras verify`, informar o resultado real e fazer o commit
  "Marco 5, etapa 3: job deploy com conferência pela URL da revisão e pela URL pública".

**Checkpoint**: o pipeline está pronto para publicar na `main`. No PR, o job `deploy` aparece como pulado.

---

## Phase 5: Etapa 4 — Documentação (US5)

**Goal**: o avaliador acha a URL e entende como e com quais limites a API é publicada.

**Independent Test**: revisão no PR, guiada pelos itens de `checklists/revisao.md`.

- [ ] T028 [P] [US5] Em `README.md`, seção "2. Avalie em 2 minutos": a URL pública de intervalos e do Swagger UI
  (da T009), com o aviso de que a primeira chamada pode levar alguns segundos (partida a frio) e um `curl` de
  exemplo.
- [ ] T029 [US5] Em `README.md`:
  - subseção "Marco 5: publicação no Cloud Run (opcional)", depois da do Marco 4: o que é publicado (só o modo
    padrão), o fluxo sem tráfego, conferência e promoção, a federação sem chaves, o limite de uma instância e
    o motivo (ADR 0026), o que fica de fora e o `deploy/gcp/setup.sh`;
  - "Requisitos do Marco 5" na seção 6, com a comprovação de cada requisito (FR-026);
  - o job `deploy` na seção "Pipeline".
- [ ] T030 [P] [US5] Criar `docs/adr/0027-publicacao-no-cloud-run.md` (contexto, decisão, alternativas e
  consequências: Cloud Run com 0 a 1 instância, WIF restrito pelo ID, a mesma imagem por digest sem rebuild,
  sem tráfego antes da conferência, o serviço criado pelo script, a convivência de duas revisões na troca e as
  métricas fechadas na URL pública com `MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=health`, com o motivo:
  autenticação desligada e nenhum Prometheus coletando no Cloud Run) e incluí-lo no índice de
  `docs/adr/README.md`.
- [ ] T031 [P] [US5] Em `docs/runbook.md`, seção "Publicação no Cloud Run":
  - falha antes da promoção: nada a fazer na URL, e como ler o resumo;
  - falha depois da promoção: `gcloud run services update-traffic awards --region=southamerica-east1 --to-revisions=<anterior>=100`
    e como achar a revisão anterior;
  - como reexecutar a publicação.
- [ ] T032 [P] [US5] Em `docs/ai/decisoes.md`, as entradas:
  - publicação a cada merge na `main`, e não por tag, como previa o briefing (FR-025);
  - opção A da clarificação (sem tráfego antes da conferência);
  - serviço criado pelo script, com a imagem de exemplo;
  - `deploy-cloudrun` não usada;
  - identificadores no `env` do workflow, e não em variáveis do repositório;
  - política do workflow ampliada.
- [ ] T033 [P] [US5] Criar `specs/005-deploy-cloud-run/checklists/revisao.md`, com os itens que o revisor
  confere em FR-023, FR-024 e FR-025:
  - a URL e o aviso no README;
  - a seção e os requisitos do Marco 5;
  - o ADR 0027 e o índice;
  - o runbook;
  - as entradas em `decisoes.md`;
  - nenhum dado pessoal nem segredo nos arquivos novos e nas evidências.
- [ ] T034 [US5] Rodar `./mvnw verify` e `./mvnw -Pextras verify`, informar o resultado real e fazer o commit
  "Marco 5, etapa 4: README, ADR 0027, runbook e decisões".

**Checkpoint**: o bolt está completo no branch.

---

## Phase 6: Pull request, merge e comprovação na `main`

**Purpose**: a comprovação pelo pipeline (FR-001 a FR-016 e SC-001, SC-002, SC-005 e SC-006)

- [ ] T035 [Anderson] Fazer o push do branch `marco/5-deploy` e abrir o PR. O implementador para aqui e
  confere o resultado quando o Anderson avisar: os 10 checks verdes, com o `workflow-policy` e o `image`
  (passo do digest) comprovando a etapa 2, e o job `deploy` pulado.
- [ ] T036 [Anderson] Opcional, nas configurações do repositório no GitHub: no ambiente `cloud-run` (criado
  automaticamente na primeira execução do job, ou antes, à mão), a regra de branch "só `main`" (research R3).
- [ ] T037 [Anderson] Fazer o merge do PR na `main`, depois da revisão do Codex e das correções aceitas.
- [ ] T038 Conferir a execução da `main`:
  - o job `deploy` verde, com o resumo mostrando o digest, a revisão, as duas conferências e a duração abaixo
    de 10 min;
  - `verify-deployment.sh --expect-no-metrics <URL pública>` rodado localmente (rápido), saindo com código 0,
    com `/actuator/prometheus` em 404 na URL pública (FR-004a);
  - SC-001: no resumo, a primeira chamada da conferência pela URL da revisão (partida a frio) até o resultado de
    referência em até 30 s, e a chamada de intervalos na URL pública em até 2 s;
  - `gcloud run services describe awards --region=southamerica-east1` mostrando a imagem pelo digest do resumo,
    `maxScale` igual a 1 e `minScale` igual a 0 ou ausente, que significa 0 (SC-006);
  - se o job falhar, parar, diagnosticar e propor a correção num commit novo (research R4, risco de permissão
    de leitura).
- [ ] T039 Em `specs/005-deploy-cloud-run/spec.md`, passar o `Status` a `Implemented`, com o link da execução
  da `main` que comprovou a publicação. Fazer o commit num branch novo, já que commits na `main` não são
  permitidos, e pedir o push ao Anderson [Anderson].
- [ ] T040 [Anderson] Exportar a sessão (`/export`) para `docs/ai/sessoes/`.

---

## Dependencies & Execution Order

- **Phase 1 → Phase 2 (etapa 1):** o script usa a `cleanup-policy.json`.
- **Phase 2 → Phase 4:** o job `deploy` usa os identificadores impressos pela T009 e o serviço criado pela T005.
- **Phase 3 → Phase 4:** a regra 9 e o artefato `awards-oci` vêm antes do job `deploy`.
- **Phase 5:** depende da URL (T009) e do desenho final do job (T023 a T025).
- **Phase 6:** depende de todas as etapas; cada `[Anderson]` é um ponto de parada.

### Histórias

- **US4** (configuração, etapa 1): base de todas.
- **US2** (publicação automática): etapas 2 e 3.
- **US1** (URL que funciona) e **US3** (conferência): etapa 3, comprovadas na Phase 6.
- **US5** (README): etapa 4.

## Parallel Opportunities

- Etapa 2: T012 em paralelo com T016 e T017 (arquivos diferentes). T013 a T015 ficam em sequência no mesmo
  script.
- Etapa 3: T020 e T022 em paralelo. T023 a T025 ficam em sequência no `ci.yml`.
- Etapa 4: T028, T030, T031, T032 e T033 em paralelo. A T029 vem depois da T028 (mesmo arquivo).

```bash
# Etapa 4, em paralelo:
Task: "ADR 0027 em docs/adr/0027-publicacao-no-cloud-run.md"
Task: "Runbook em docs/runbook.md"
Task: "Decisões em docs/ai/decisoes.md"
Task: "Checklist de revisão em specs/005-deploy-cloud-run/checklists/revisao.md"
```

## Implementation Strategy

- **MVP:** etapas 1 a 3. Com elas, um merge já publica e confere a URL (US1 a US4). A etapa 4 completa o marco
  para o avaliador, e um extra só entra completo (AGENTS.md), então o PR leva as quatro.
- Cada etapa termina com os dois comandos Maven verdes e um commit.
- As conferências locais são só as rápidas (T010, T018, T021, T022 e T026). Build da imagem, envio, implantação
  e conferência na nuvem são comprovados pelo pipeline.
