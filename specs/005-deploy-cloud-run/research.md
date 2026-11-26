# Research: Marco 5 — Publicação no Cloud Run

Decisões técnicas do plano. Cada uma tem a decisão, o motivo e as alternativas recusadas. As referências a
requisitos são da [spec](spec.md).

## R1. Mesma imagem do job `image`, sem rebuild (FR-002, FR-010)

- **Decisão:** o job `image` passa a gerar a imagem com **duas saídas do mesmo build** do Buildx: `type=docker`
  (a `awards:ci` de hoje, usada pelo teste de fumaça e pelo Trivy) e `type=oci,dest=…/awards-oci.tar` (layout
  OCI em tar). O digest do manifesto é lido do `index.json` do tar e vira saída do job (`digest`). Em push na
  `main`, o tar é enviado como artefato (`awards-oci`, retenção de 1 dia). O job `deploy` baixa esse artefato,
  confere que o digest do tar é o da saída do job `image` e envia o tar ao Artifact Registry com
  `skopeo copy --preserve-digests oci-archive:… docker://…:<sha do commit>`. O `--preserve-digests` faz o
  `skopeo` falhar se o digest mudasse. Depois do envio, o job confere que o digest no registro é o mesmo e
  implanta por `…@sha256:<digest>`.
- **Prova de que a imagem testada é a do tar:** no job `image`, depois do teste de fumaça, um passo confere que
  o `Id` da `awards:ci` carregada no Docker é o digest da configuração do manifesto OCI (armazenamento clássico
  do Docker) ou o próprio digest do manifesto (armazenamento do containerd). Qualquer outro valor reprova o job.
- **Atestados desligados no build** (`provenance: false`, `sbom: false`): sem eles, o tar tem um manifesto de
  plataforma única `linux/amd64`, e não um índice com um manifesto de atestado `unknown/unknown`, que já
  causou rejeição em plataformas de execução. O SBOM e a assinatura continuam adiados (Marco 4, US5).
- **Alternativas recusadas:**
  - `docker save` e `docker push` no job `deploy`: o `docker push` recomprime as camadas, e o digest no
    registro pode não ser o da imagem testada.
  - Publicar no registro já no job `image`: esse job roda em todo push e pull request, e a publicação só pode
    acontecer na `main`, depois de todos os checks (FR-008).
  - Reconstruir no job `deploy` a partir do cache do GHA: seria outra imagem, mesmo que byte a byte igual na
    maior parte das vezes. O pedido é explícito: sem rebuild.
  - `crane` (go-containerregistry): faz o mesmo, mas exigiria baixar um binário ou uma action a mais. O
    `skopeo` já vem no runner `ubuntu-latest`. O job confere a presença dele e falha com mensagem clara se
    faltar.

## R2. Actions do Google e referências aninhadas

| Action | Versão | Hash do commit | Execução | `uses:` aninhado |
| --- | --- | --- | --- | --- |
| `google-github-actions/auth` | v3.0.0 | `7c6bc770dae815cd3e89ee6cdf493a5fab2cc093` | `node24` (`dist/main/index.js`, `dist/post/index.js`) | nenhum |
| `google-github-actions/setup-gcloud` | v3.0.1 | `aa5489c8933f4cc7a4f7d45035b3b1440c9c10db` | `node24` (`dist/index.js`) | nenhum |
| `actions/download-artifact` | v8.0.2 | `9000827ccba6bdab643e8b6fd33ac0654aef8333` | `node24` (`dist/index.js`) | nenhum |

- Conferido em 2026-11-26 com `git ls-remote --tags` (tags leves, então o hash é o do commit) e com o
  `action.yml` de cada hash: as três são JavaScript, empacotadas em `dist/`, sem passos compostos. Não há
  referência aninhada a conferir. O Dependabot (`github-actions`) já cobre as três.
- O `download-artifact` v8 confere o digest do artefato por padrão (`digest-mismatch: error`), o que reforça a
  R1. Ele lê artefatos enviados pelo `upload-artifact` v7 (mesmo backend v4+).
- **`deploy-cloudrun` não é usada.** A implantação sem tráfego, a conferência pela URL da revisão e a troca de
  tráfego (FR-014 e FR-015) são três passos `gcloud` explícitos. A action juntaria os passos e acrescentaria
  mais uma dependência fixada sem ganho.
- **`setup-gcloud` com versão fixada** (`version` com o número exato do `gcloud`, registrado no workflow; a
  mesma usada localmente na etapa 1, `594.0.1`, salvo motivo registrado), para
  que um `gcloud` novo do runner não mude os flags sem aviso. Atualizar é uma mudança revisada no PR.

## R3. Federação de identidade restrita ao repositório e à `main` (FR-009)

- **Pool** `github` e **provedor OIDC** `github-actions`, emissor `https://token.actions.githubusercontent.com`.
- **Mapeamento:** `google.subject=assertion.sub`, `attribute.repository=assertion.repository`,
  `attribute.repository_id=assertion.repository_id`, `attribute.repository_owner_id=assertion.repository_owner_id`,
  `attribute.ref=assertion.ref`.
- **Condição do provedor:** `assertion.repository_id == '<id>' && assertion.repository_owner_id == '<id do dono>'
  && assertion.ref == 'refs/heads/main'`. Os IDs numéricos não mudam se o repositório ou o dono forem
  renomeados, e um repositório novo com o mesmo nome não herda o acesso. O script os lê da API pública do GitHub
  (`GET /repos/andersonfelipediefenbach/outsera`) e os mostra antes de aplicar.
- **Quem pode usar a conta de publicação:** `roles/iam.workloadIdentityUser` só para
  `principalSet://…/workloadIdentityPools/github/attribute.repository_id/<id>`. A condição do provedor já
  barra outras branches; o vínculo limita ao repositório mesmo que o provedor fosse alterado.
- **Ambiente do GitHub `cloud-run`** no job `deploy`, com a URL publicada (o GitHub mostra o histórico de
  publicações). O `sub` do token passa a ter `environment:cloud-run`, mas a condição usa `ref`, que continua
  `refs/heads/main`. A regra de branch do ambiente (só `main`) é uma camada a mais, configurada pelo Anderson
  nas configurações do repositório (quickstart). Não é a garantia principal.
- **Alternativas recusadas:** chave JSON da conta de serviço (proibida pela FR-009 e pelo Princípio VII);
  condição por `assertion.repository` (nome, que pode ser reaproveitado depois de renomeado);
  `assertion.event_name == 'push'` (a reexecução manual de uma execução da `main` continua com o evento
  `push`, então não acrescenta proteção; o `if` do job já limita o evento).

## R4. Permissões mínimas e serviço criado pelo script (FR-004, FR-018)

- **Conta de publicação** `github-deployer`:
  - `roles/artifactregistry.writer` **no repositório** `awards` (não no projeto): envia imagens;
  - `roles/run.developer` **no serviço** `awards` (não no projeto): implanta revisões e muda o tráfego, sem
    poder mudar o IAM do serviço;
  - `roles/iam.serviceAccountUser` **na conta de execução** `awards-runtime`: implanta revisões que rodam com
    ela.
- **Conta de execução** `awards-runtime`: nenhum papel. A aplicação não chama nenhuma API do GCP. O pull da
  imagem é feito pelo agente de serviço do Cloud Run, que lê o Artifact Registry do mesmo projeto por padrão.
- **Acesso público:** `allUsers` com `roles/run.invoker` no serviço, concedido **pelo script**. A conta de
  publicação não tem `run.services.setIamPolicy`, então o pipeline não consegue mudar quem invoca o serviço.
- **O serviço é criado pelo script,** com a imagem pública de exemplo do Cloud Run
  (`us-docker.pkg.dev/cloudrun/container/hello`), já com a conta de execução e os limites de instância. Só
  assim os papéis podem ser concedidos **no serviço**, e não no projeto. A URL pública fica conhecida antes do
  primeiro merge (ela entra no README na etapa 4) e serve o exemplo até a primeira publicação pelo pipeline.
  Esse é também o caso de borda "primeira publicação": já existe uma revisão anterior, e o fluxo é o mesmo.
- **Risco:** algum comando `gcloud run` pode exigir uma permissão de leitura no nível do projeto. Se a primeira
  execução do job falhar por isso, a correção é acrescentar o papel de leitura mínimo que faltar
  (`roles/run.viewer` no projeto), registrado no script com o motivo. Ela não vira `run.admin`.
- **Alternativas recusadas:** `roles/run.admin` (pode tornar o serviço privado ou público e mudar o IAM);
  `--no-invoker-iam-check` no pipeline (mudaria a política de invocação a partir do pipeline);
  `roles/run.developer` no projeto sem necessidade comprovada.

## R5. Configuração do serviço (FR-001, FR-003, FR-005, FR-006)

- `gcloud run deploy awards --image=<registro>/awards@sha256:<digest> --region=southamerica-east1
  --service-account=awards-runtime@… --min-instances=0 --max-instances=1 --cpu=1 --memory=1Gi --cpu-boost
  --port=8080 --no-traffic --tag=c-<sha curto>`, com os probes:
  - **startup:** `GET /actuator/health/readiness`, período de 2 s, até 60 falhas (cerca de 2 min). O Cloud Run
    só manda tráfego à instância depois disso (FR-006);
  - **liveness:** `GET /actuator/health/liveness`, período de 30 s.
- **Uma única variável nova:** `MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=health` (`--set-env-vars`), que
  sobrepõe o `include: health,prometheus` do `application.yml` só no Cloud Run. Ficam `/actuator/health` e os
  grupos de liveness e readiness, que os probes usam, e `/actuator/prometheus` passa a responder 404 na URL
  pública (FR-004a). Com a autenticação desligada, as métricas ficariam abertas na internet, e não há
  Prometheus coletando no Cloud Run. O modo padrão e a imagem não mudam.
- A imagem já define `LOGGING_STRUCTURED_FORMAT_CONSOLE=ecs` (JSON), e o Cloud Run define `PORT`. `API_TOKEN`,
  perfis e `MOVIES_CSV_PATH` **não** são definidos: vale o modo padrão (FR-001, FR-004).
- **1 vCPU, 1 GiB e o impulso de CPU na partida:** a JVM usa 75% da memória (`MaxRAMPercentage`), e o impulso
  encurta a partida a frio (SC-001), sem custo com o serviço ocioso. Cobrança por requisição (padrão), sem CPU
  sempre alocada.
- **`max-instances=1` é por revisão** (FR-003). Na troca, a revisão antiga e a nova podem ter uma instância cada por
  alguns instantes. No modo padrão isso não quebra o ADR 0026: cada instância tem o próprio H2 e não grava
  nada depois da carga, então não há invalidação entre instâncias a perder. Fora da troca, o tráfego vai para
  uma revisão só, com no máximo uma instância.
- **Os flags de probe** (`--startup-probe`, `--liveness-probe`) são conferidos na versão fixada do `gcloud`
  (R2) na etapa 3. Se não existirem, a implantação passa a `gcloud run services replace` com um YAML
  versionado do serviço. O comportamento exigido é o mesmo.

## R6. Sem tráfego, conferir, promover e conferir de novo (FR-014 a FR-016)

1. `gcloud run deploy … --no-traffic --tag=c-<sha curto>` cria a revisão e um endereço só dela
   (`https://c-<sha>---awards-<hash>.a.run.app`), lido de `gcloud run services describe` (`status.traffic`
   com a tag).
2. `.github/scripts/verify-deployment.sh <URL da revisão>`: readiness positiva com espera de até 180 s e o
   resultado de referência (contrato em [`contracts/verify-deployment.md`](contracts/verify-deployment.md)).
   Se reprova, o job para aqui: o tráfego nunca saiu da revisão anterior (FR-015a).
3. `gcloud run services update-traffic awards --to-revisions=<revisão>=100`, e as tags de revisões anteriores
   são removidas, para os endereços antigos não continuarem acessíveis.
4. O mesmo script na URL pública do serviço. Se reprova aqui, o job fica vermelho, e o runbook descreve a
   volta à revisão anterior.
5. O resumo da execução recebe a URL, o digest, a revisão e o resultado de cada conferência (FR-016), com o
   tempo de cada chamada. Na conferência pela URL da revisão, a primeira chamada pega a partida a frio, e o
   tempo dela comprova os 30 s do SC-001. A chamada de intervalos na URL pública, com a instância já ativa,
   comprova os 2 s.

- A referência é a mesma `REFERENCE` do `smoke-test.sh`, comparada com `jq -S`, com as chaves exatas do
  contrato.
- **Alternativa recusada:** promover direto e voltar automaticamente se falhar (opção B da clarificação):
  expõe a revisão ruim na URL pública por alguns segundos.

## R7. Condição, dependências e concorrência do job `deploy` (FR-008, FR-011)

- `if: github.event_name == 'push' && github.ref == 'refs/heads/main' && github.repository == 'andersonfelipediefenbach/outsera'`.
  Um fork que copia o workflow nem chega a pedir o token; se pedisse, o GCP recusaria (R3).
- `needs:` com **os 10 checks obrigatórios da `main`** (Marco 4, T064): `workflow-policy`, `build`,
  `postgres`, `codeql`, `dependencies`, `dockerfile-lint`, `image`, `alert-rules`, `load` e `compose`. A
  política do workflow (R8) confere essa lista. Um check obrigatório novo exige atualizar a lista na política
  e no `needs`, e o comentário da regra 9 diz isso.
- A comparação do `if` na política normaliza o texto: tira `${{ }}` e os espaços nas pontas e reduz espaços
  repetidos a um só. Assim, as duas formas aceitas pelo GitHub passam, e qualquer outra condição reprova.
- **Concorrência:** a do topo hoje é `cancel-in-progress: true` por ref, o que cancelaria uma publicação no meio
  quando chega outro merge. Ela passa a `cancel-in-progress: ${{ github.ref != 'refs/heads/main' }}`. O job
  `deploy` ganha o grupo próprio `deploy-cloud-run`, com `cancel-in-progress: false`.
  - Com dois merges próximos, o segundo espera o primeiro terminar.
  - Com três ou mais, o GitHub mantém só a execução pendente mais nova. A intermediária não é publicada, e
    ganha o commit mais recente. A ordem nunca se inverte, e nenhuma publicação é interrompida.
- **Permissões do job:** `contents: read` e `id-token: write`. Nenhuma outra.

## R8. Política do workflow ampliada (FR-012)

O `check-workflows.sh` muda assim:

| Regra | Hoje | Depois |
| --- | --- | --- |
| 3. Escrita por job | Só `security-events: write` no `codeql` | Também `id-token: write`, **só** no job `deploy` |
| 4. `secrets.` | Proibido | Sem mudança |
| 5. Publicação | Proibido `push: true` e `docker push` | Continua proibido em todo job; `skopeo copy` para `docker://` só no `deploy` |
| 9 (nova). Job `deploy` | — | `if` exatamente como na R7; `needs` com os 10 checks; concorrência própria sem cancelamento; nenhum `docker/build-push-action`, `docker build` ou `docker buildx build` (sem rebuild) |
| 10 (nova). Topo | — | `cancel-in-progress` não pode ser `true` fixo |

- A política é verificada no job `workflow-policy`, como hoje. Os casos negativos (um job qualquer com
  `id-token: write`, `skopeo copy` fora do `deploy`, um `deploy` sem um dos `needs` e um `deploy` com build)
  são conferidos na etapa 2, numa cópia temporária do workflow, com a saída registrada nas tarefas. O yq
  não está instalado na máquina local; os casos rodam num runner ou no WSL, conforme a tarefa.

## R9. Script de configuração do GCP (FR-017 a FR-021)

- **Local:** `deploy/gcp/setup.sh` (fora do contexto da imagem pela lista de permissão do `.dockerignore`).
  Bash, `set -euo pipefail`, saída `OK`/`FALHA` por passo, com resumo no fim, como o `smoke-test.sh`.
  Precisa só do `gcloud` e do `curl`; não usa `jq` (lê os valores com `--format=value(...)`). No Git Bash do
  Windows, as saídas do `gcloud` têm o `\r` removido. Nesse ambiente, o `gcloud` em bash procura `python` no PATH
  e encontra o atalho da Microsoft Store (`Python não foi encontrado`). Por isso, sem `CLOUDSDK_PYTHON` definido,
  o script aponta para o Python do próprio SDK (`<sdk>/platform/bundledpython/python.exe`, achado a partir de
  `command -v gcloud`). Conferido em 2026-11-26 com o Google Cloud SDK 594.0.1.
- **Idempotência por convergência:** cada passo lê o estado atual e só age se ele diverge do desejado.
  - Resultado `OK (já existia)` quando nada muda e `OK (criado)` ou `OK (ajustado)` quando muda.
  - A segunda execução tem que terminar com todos os passos em `OK (já existia)` (SC-004). O script termina
    com a contagem de mudanças, e a tarefa registra a saída das duas execuções.
  - Para um pool ou provedor em estado `DELETED` (excluído há menos de 30 dias), o script restaura em vez de
    criar.
  - Os vínculos de IAM são conferidos antes de adicionar, para não gerar uma versão nova da política sem
    mudança.
- **Verificações antes de mudar qualquer coisa** (FR-019): `gcloud` no PATH; conta ativa
  (`gcloud auth list`); projeto `outsera-awards` existente e `ACTIVE`; faturamento ligado
  (`gcloud billing projects describe`); IDs do repositório lidos da API do GitHub. Qualquer falha encerra
  antes do primeiro passo de escrita, com `FALHA` e o motivo.
- **Passos:** APIs (`run`, `artifactregistry`, `iam`, `iamcredentials`, `sts`); repositório Docker `awards`
  com a política de limpeza versionada (`deploy/gcp/cleanup-policy.json`: uma regra `DELETE` para qualquer
  versão, com ou sem tag, com mais de 30 dias, e uma regra `KEEP` para as 10 mais recentes, que prevalece sobre
  a de apagar; só a `DELETE` de imagens sem tag não bastaria, porque toda imagem recebe a tag do commit); as duas contas de serviço; o pool, o provedor e a condição; o serviço
  com a imagem de exemplo (só se não existir); os vínculos de IAM da R4; e a impressão dos identificadores
  públicos (número do projeto, provedor completo, e-mail da conta de publicação, registro e URL do serviço).
- **Nenhuma credencial** é criada, baixada ou impressa. O script confere que a conta de publicação não tem
  chaves gerenciadas pelo usuário (SC-003) e falha se tiver.
- **Quem roda:** o Claude Code, no terminal do Anderson, depois de ele instalar o Google Cloud CLI e rodar
  `! gcloud auth login` e `! gcloud config set project outsera-awards`.

## R10. Identificadores no workflow (FR-013)

- Os identificadores ficam no `env:` do job `deploy`, versionados: `GCP_PROJECT_ID`, `GCP_PROJECT_NUMBER`,
  `GCP_REGION`, `GCP_WIF_PROVIDER`, `GCP_DEPLOY_SA`, `GCP_RUNTIME_SA`, `GCP_REGISTRY` e `GCP_SERVICE`.
- **Motivo:** são públicos, e no workflow ficam visíveis na revisão do PR, entram no histórico do git e não
  exigem nenhum passo manual no GitHub. Variáveis do repositório (`vars.`) esconderiam a configuração fora do
  código.
- Os valores saem da impressão final do script (R9). A regra 4 da política continua proibindo `secrets.`.

## R11. Comprovação e tempo (FR-026, SC-005)

- Não há IT novo: nenhum comportamento da aplicação muda (FR-022). `./mvnw verify` e `./mvnw -Pextras verify`
  rodam no fim de cada etapa, e o `check-unchanged-tests.sh` (v0.2.0 e v0.3.0) continua no `workflow-policy`.
- O `verify-deployment.sh` é testado antes do merge contra a imagem local (`docker run awards:ci` e a URL
  `http://127.0.0.1:<porta>`), com um caso que aprova e outro que reprova (um CSV de cenário diferente,
  montado no container). O mesmo script roda depois no pipeline.
- **Orçamento de tempo do job `deploy`** (SC-005, abaixo de 10 min): download e envio do tar (cerca de 1 min),
  implantação (cerca de 1 a 2 min), as duas conferências com partida a frio (cerca de 1 min cada). O job tem
  `timeout-minutes: 15`, e o tempo real fica no resumo.
