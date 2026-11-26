#!/usr/bin/env bash
# Configuração do GCP para a publicação no Cloud Run (spec 005, FR-017 a FR-021; research R3, R4 e R9;
# contracts/setup-script.md).
#
# Uso: deploy/gcp/setup.sh
#
# Idempotente por convergência: cada passo lê o estado atual e só age se ele diverge do desejado. A segunda
# execução seguida termina com todos os passos em "OK (já existia)" e zero mudanças (SC-004).
#
# Pré-requisitos: Google Cloud CLI no PATH, login feito pelo responsável (gcloud auth login) com permissão
# de dono no projeto, e curl. Roda no Linux e no Git Bash do Windows. Nenhuma credencial é criada, baixada
# ou impressa; a saída pode ir para a evidência pública (o e-mail da conta ativa não é impresso).
#
# Códigos de saída: 0 todos os passos OK, 1 algum passo em FALHA, 2 ferramenta ausente.
set -euo pipefail

PROJECT_ID=outsera-awards
REGION=southamerica-east1
GITHUB_REPO=andersonfelipediefenbach/outsera
REPOSITORY=awards
SERVICE=awards
POOL=github
PROVIDER=github-actions
DEPLOY_SA=github-deployer
RUNTIME_SA=awards-runtime
PLACEHOLDER_IMAGE=us-docker.pkg.dev/cloudrun/container/hello
ISSUER=https://token.actions.githubusercontent.com
APIS=(run.googleapis.com artifactregistry.googleapis.com iam.googleapis.com iamcredentials.googleapis.com sts.googleapis.com)

DEPLOY_EMAIL="$DEPLOY_SA@$PROJECT_ID.iam.gserviceaccount.com"
RUNTIME_EMAIL="$RUNTIME_SA@$PROJECT_ID.iam.gserviceaccount.com"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
CLEANUP_POLICY="$SCRIPT_DIR/cleanup-policy.json"

# Forma canônica da política de limpeza, como o gcloud a devolve (list-cleanup-policies). Espelha o
# cleanup-policy.json: os dois mudam juntos.
EXPECTED_CLEANUP=$'apaga-com-mais-de-30-dias\tDELETE\tANY\t2592000s\t\nmantem-as-10-mais-recentes\tKEEP\t\t\t10'

for tool in gcloud curl; do
  if ! command -v "$tool" >/dev/null 2>&1; then
    echo "setup.sh: '$tool' não foi encontrado no PATH." >&2
    exit 2
  fi
done

# No Git Bash, o gcloud em bash procura "python" no PATH e acha o atalho da Microsoft Store. Sem
# CLOUDSDK_PYTHON definido, usa o Python que vem com o próprio SDK (research R9).
case "$(uname -s)" in
  MINGW*|MSYS*)
    if [[ -z "${CLOUDSDK_PYTHON:-}" ]]; then
      bundled="$(dirname "$(command -v gcloud)")/../platform/bundledpython/python.exe"
      [[ -f "$bundled" ]] && export CLOUDSDK_PYTHON="$bundled"
    fi
    ;;
esac

# gcloud sem prompts, no projeto certo, sem o \r das saídas no Windows.
gc() { gcloud --quiet --project="$PROJECT_ID" "$@" | tr -d '\r'; }

# Escrita com novas tentativas: o IAM leva alguns segundos para propagar uma conta recém-criada.
gc_write() {
  local attempt
  for attempt in 1 2 3 4 5 6; do
    if gc "$@" >/dev/null; then return 0; fi
    echo "    tentativa $attempt falhou; nova tentativa em 10 s"
    sleep 10
  done
  return 1
}

PASSED=0; FAILED=0; CHANGES=0
SUMMARY=()
STEP_LABELS=()
FAIL_REASON=""
changed() { STEP_LABELS+=("$1"); CHANGES=$((CHANGES + 1)); echo "    $1: $2"; }
fail() { FAIL_REASON="$1"; return 1; }
run_step() { # passo, descrição, função
  STEP_LABELS=(); FAIL_REASON=""
  echo "--- passo $1: $2"
  local result
  if "$3"; then
    if (( ${#STEP_LABELS[@]} == 0 )); then
      result="OK (já existia)"
    else
      result="OK ($(printf '%s\n' "${STEP_LABELS[@]}" | awk '!seen[$0]++' | paste -sd, - | sed 's/,/, /g'))"
    fi
    PASSED=$((PASSED + 1))
    echo "==> passo $1: $result - $2"
  else
    result="FALHA"
    FAILED=$((FAILED + 1))
    echo "==> passo $1: FALHA - $2: ${FAIL_REASON:-erro do gcloud (ver acima)}"
  fi
  SUMMARY+=("passo $1: $result - $2")
  # Os passos dependem uns dos outros: o primeiro FALHA encerra.
  if [[ "$result" == "FALHA" ]]; then finish; fi
}
finish() {
  echo
  echo "Resumo:"
  printf '  %s\n' "${SUMMARY[@]}"
  echo "  $PASSED OK, $FAILED FALHA, $CHANGES mudança(s)"
  if (( FAILED > 0 )); then exit 1; fi
  exit 0
}

# Lê "papel<TAB>membro" de uma política de IAM e adiciona o vínculo só se faltar.
ensure_binding() { # descrição do recurso, membro, papel, comando get, comando add (arrays por nome)
  local what="$1" member="$2" role="$3"; shift 3
  local -n getcmd="$1" addcmd="$2"
  local current
  current="$(gc "${getcmd[@]}" --flatten='bindings[].members' --format='value(bindings.role,bindings.members)')" \
    || fail "não foi possível ler a política de IAM de $what" || return 1
  if grep -qxF "$role"$'\t'"$member" <<<"$current"; then
    return 0
  fi
  gc_write "${addcmd[@]}" --member="$member" --role="$role" \
    || fail "não foi possível conceder $role a $member em $what" || return 1
  changed criado "$role para $member em $what"
}

PROJECT_NUMBER=""; REPO_ID=""; OWNER_ID=""

step0() {
  [[ "$(gc auth list --filter=status:ACTIVE --format='value(account)' | wc -l)" -ge 1 ]] \
    || fail "nenhuma conta ativa no gcloud; rode gcloud auth login" || return 1
  echo "    conta ativa: ok"
  local state
  state="$(gc projects describe "$PROJECT_ID" --format='value(lifecycleState,projectNumber)' 2>/dev/null)" \
    || fail "projeto $PROJECT_ID não encontrado ou sem acesso" || return 1
  [[ "${state%%$'\t'*}" == "ACTIVE" ]] || fail "projeto $PROJECT_ID não está ACTIVE (${state%%$'\t'*})" || return 1
  PROJECT_NUMBER="${state##*$'\t'}"
  [[ "$PROJECT_NUMBER" =~ ^[0-9]+$ ]] || fail "número do projeto inválido: $PROJECT_NUMBER" || return 1
  echo "    projeto $PROJECT_ID ACTIVE, número $PROJECT_NUMBER"
  [[ "$(gc billing projects describe "$PROJECT_ID" --format='value(billingEnabled)')" == "True" ]] \
    || fail "faturamento desligado no projeto $PROJECT_ID" || return 1
  echo "    faturamento ligado"
  local ids
  ids="$(curl -fsS "https://api.github.com/repos/$GITHUB_REPO" | tr -d '\r' \
    | awk '/^  "id": /{gsub(/[^0-9]/,""); print "repo " $0} /^  "owner": \{/{o=1} o && /^    "id": /{gsub(/[^0-9]/,""); print "owner " $0; o=0}')" \
    || fail "não foi possível ler $GITHUB_REPO na API do GitHub" || return 1
  REPO_ID="$(awk '$1=="repo"{print $2}' <<<"$ids")"
  OWNER_ID="$(awk '$1=="owner"{print $2}' <<<"$ids")"
  [[ "$REPO_ID" =~ ^[0-9]+$ && "$OWNER_ID" =~ ^[0-9]+$ ]] \
    || fail "IDs do repositório ou do dono inválidos (repo='$REPO_ID', dono='$OWNER_ID')" || return 1
  echo "    $GITHUB_REPO: repository_id $REPO_ID, repository_owner_id $OWNER_ID"
}

step1() {
  local enabled api
  enabled="$(gc services list --enabled --format='value(config.name)')" || fail "não foi possível listar as APIs" || return 1
  for api in "${APIS[@]}"; do
    if ! grep -qxF "$api" <<<"$enabled"; then
      gc_write services enable "$api" || fail "não foi possível ativar $api" || return 1
      changed criado "API $api ativada"
    fi
  done
}

cleanup_state() {
  gc artifacts repositories list-cleanup-policies "$REPOSITORY" --location="$REGION" \
    --format='value(name,action.type,condition.tagState,condition.olderThan,mostRecentVersions.keepCount)' | sort
}

step2() {
  if ! gc artifacts repositories describe "$REPOSITORY" --location="$REGION" --format='value(name)' >/dev/null 2>&1; then
    gc_write artifacts repositories create "$REPOSITORY" --repository-format=docker --location="$REGION" \
      --description="Imagens da API awards publicadas pelo pipeline (spec 005)" \
      || fail "não foi possível criar o repositório $REPOSITORY" || return 1
    changed criado "repositório $REPOSITORY em $REGION"
  fi
  local current dry
  current="$(cleanup_state)" || fail "não foi possível ler a política de limpeza" || return 1
  dry="$(gc artifacts repositories describe "$REPOSITORY" --location="$REGION" --format='value(cleanupPolicyDryRun)')"
  if [[ "$current" != "$EXPECTED_CLEANUP" || "$dry" == "True" ]]; then
    gc_write artifacts repositories set-cleanup-policies "$REPOSITORY" --location="$REGION" \
      --policy="$CLEANUP_POLICY" --no-dry-run \
      || fail "não foi possível aplicar $CLEANUP_POLICY" || return 1
    current="$(cleanup_state)"
    if [[ "$current" != "$EXPECTED_CLEANUP" ]]; then
      printf '    política lida depois de aplicar:\n%s\n' "$current"
      fail "a política aplicada não confere com a forma esperada (EXPECTED_CLEANUP)" || return 1
    fi
    changed ajustado "política de limpeza (30 dias; mantém as 10 mais recentes)"
  fi
}

ensure_sa() { # nome, e-mail, nome de exibição
  if gc iam service-accounts describe "$2" --format='value(email)' >/dev/null 2>&1; then
    return 0
  fi
  gc_write iam service-accounts create "$1" --display-name="$3" \
    || fail "não foi possível criar a conta $2" || return 1
  changed criado "conta de serviço $2"
}

step3() { ensure_sa "$RUNTIME_SA" "$RUNTIME_EMAIL" "Execução do serviço awards"; }

step4() {
  ensure_sa "$DEPLOY_SA" "$DEPLOY_EMAIL" "Publicação pelo GitHub Actions" || return 1
  local keys
  keys="$(gc iam service-accounts keys list --iam-account="$DEPLOY_EMAIL" --managed-by=user --format='value(name)')" \
    || fail "não foi possível listar as chaves de $DEPLOY_EMAIL" || return 1
  [[ -z "$keys" ]] || fail "a conta $DEPLOY_EMAIL tem chave gerenciada pelo usuário (proibido; SC-003)" || return 1
  echo "    $DEPLOY_EMAIL sem chaves gerenciadas pelo usuário"
}

step5() {
  local state
  state="$(gc iam workload-identity-pools describe "$POOL" --location=global --format='value(state)' 2>/dev/null || true)"
  case "$state" in
    ACTIVE) ;;
    DELETED)
      gc_write iam workload-identity-pools undelete "$POOL" --location=global \
        || fail "não foi possível restaurar o pool $POOL" || return 1
      changed restaurado "pool $POOL" ;;
    "")
      gc_write iam workload-identity-pools create "$POOL" --location=global --display-name="GitHub Actions" \
        --description="Identidades do GitHub Actions do repositório $GITHUB_REPO" \
        || fail "não foi possível criar o pool $POOL" || return 1
      changed criado "pool $POOL" ;;
    *) fail "pool $POOL em estado inesperado: $state" || return 1 ;;
  esac
}

ATTRIBUTE_MAPPING="google.subject=assertion.sub,attribute.repository=assertion.repository,attribute.repository_id=assertion.repository_id,attribute.repository_owner_id=assertion.repository_owner_id,attribute.ref=assertion.ref"

step6() {
  # Só tokens deste repositório e do dono (pelos IDs numéricos, que não mudam com uma renomeação) e da
  # branch main (research R3; FR-009).
  local condition="assertion.repository_id == '$REPO_ID' && assertion.repository_owner_id == '$OWNER_ID' && assertion.ref == 'refs/heads/main'"
  local expected_mapping
  expected_mapping="$(tr ',' '\n' <<<"$ATTRIBUTE_MAPPING" | sed 's/=/: /' | sort)"
  local flags=(--location=global --workload-identity-pool="$POOL" --issuer-uri="$ISSUER"
    --attribute-mapping="$ATTRIBUTE_MAPPING" --attribute-condition="$condition")
  local state
  state="$(gc iam workload-identity-pools providers describe "$PROVIDER" --location=global \
    --workload-identity-pool="$POOL" --format='value(state)' 2>/dev/null || true)"
  case "$state" in
    "")
      gc_write iam workload-identity-pools providers create-oidc "$PROVIDER" --display-name="GitHub Actions OIDC" "${flags[@]}" \
        || fail "não foi possível criar o provedor $PROVIDER" || return 1
      changed criado "provedor $PROVIDER"
      return 0 ;;
    DELETED)
      gc_write iam workload-identity-pools providers undelete "$PROVIDER" --location=global --workload-identity-pool="$POOL" \
        || fail "não foi possível restaurar o provedor $PROVIDER" || return 1
      changed restaurado "provedor $PROVIDER" ;;
    ACTIVE) ;;
    *) fail "provedor $PROVIDER em estado inesperado: $state" || return 1 ;;
  esac
  local cur_condition cur_issuer cur_mapping
  cur_condition="$(gc iam workload-identity-pools providers describe "$PROVIDER" --location=global --workload-identity-pool="$POOL" --format='value(attributeCondition)')"
  cur_issuer="$(gc iam workload-identity-pools providers describe "$PROVIDER" --location=global --workload-identity-pool="$POOL" --format='value(oidc.issuerUri)')"
  cur_mapping="$(gc iam workload-identity-pools providers describe "$PROVIDER" --location=global --workload-identity-pool="$POOL" --format='yaml(attributeMapping)' \
    | sed -n 's/^  //p' | sort)"
  if [[ "$cur_condition" != "$condition" || "$cur_issuer" != "$ISSUER" || "$cur_mapping" != "$expected_mapping" ]]; then
    gc_write iam workload-identity-pools providers update-oidc "$PROVIDER" "${flags[@]}" \
      || fail "não foi possível ajustar o provedor $PROVIDER" || return 1
    changed ajustado "provedor $PROVIDER (emissor, mapeamento ou condição)"
  fi
  echo "    condição: $condition"
}

step7() {
  local sa
  if sa="$(gc run services describe "$SERVICE" --region="$REGION" --format='value(spec.template.spec.serviceAccountName)' 2>/dev/null)"; then
    [[ "$sa" == "$RUNTIME_EMAIL" ]] || fail "o serviço $SERVICE roda com '$sa', e não com $RUNTIME_EMAIL" || return 1
    return 0
  fi
  # Criado com a imagem pública de exemplo, só para existir: os papéis da conta de publicação ficam no
  # serviço, e não no projeto, e a URL é conhecida antes da primeira publicação (research R4).
  gc_write run deploy "$SERVICE" --image="$PLACEHOLDER_IMAGE" --region="$REGION" \
    --service-account="$RUNTIME_EMAIL" --min-instances=0 --max-instances=1 --cpu=1 --memory=1Gi \
    --no-allow-unauthenticated \
    || fail "não foi possível criar o serviço $SERVICE" || return 1
  changed criado "serviço $SERVICE com a imagem de exemplo"
}

step8() {
  local principal="principalSet://iam.googleapis.com/projects/$PROJECT_NUMBER/locations/global/workloadIdentityPools/$POOL/attribute.repository_id/$REPO_ID"
  local get add

  # Só identidades deste repositório (pelo ID) podem usar a conta de publicação; a condição do provedor
  # já limita à main (research R3).
  get=(iam service-accounts get-iam-policy "$DEPLOY_EMAIL"); add=(iam service-accounts add-iam-policy-binding "$DEPLOY_EMAIL")
  ensure_binding "conta $DEPLOY_SA" "$principal" roles/iam.workloadIdentityUser get add || return 1

  # A conta de publicação envia a imagem testada, só neste repositório de imagens (não no projeto).
  get=(artifacts repositories get-iam-policy "$REPOSITORY" --location="$REGION")
  add=(artifacts repositories add-iam-policy-binding "$REPOSITORY" --location="$REGION")
  ensure_binding "repositório $REPOSITORY" "serviceAccount:$DEPLOY_EMAIL" roles/artifactregistry.writer get add || return 1

  # Implanta revisões e muda o tráfego só deste serviço, sem poder mudar o IAM dele (não é run.admin).
  get=(run services get-iam-policy "$SERVICE" --region="$REGION")
  add=(run services add-iam-policy-binding "$SERVICE" --region="$REGION")
  ensure_binding "serviço $SERVICE" "serviceAccount:$DEPLOY_EMAIL" roles/run.developer get add || return 1

  # Implanta revisões que rodam com a conta de execução (actAs só nela).
  get=(iam service-accounts get-iam-policy "$RUNTIME_EMAIL"); add=(iam service-accounts add-iam-policy-binding "$RUNTIME_EMAIL")
  ensure_binding "conta $RUNTIME_SA" "serviceAccount:$DEPLOY_EMAIL" roles/iam.serviceAccountUser get add || return 1

  # API pública: a autenticação fica desligada, como no modo padrão (FR-004). Concedido aqui, e não pelo
  # pipeline, que não pode mudar quem invoca o serviço.
  get=(run services get-iam-policy "$SERVICE" --region="$REGION")
  add=(run services add-iam-policy-binding "$SERVICE" --region="$REGION")
  ensure_binding "serviço $SERVICE" allUsers roles/run.invoker get add || return 1

  # A conta de execução não recebe nenhum papel: a aplicação não chama APIs do GCP.
}

SERVICE_URL=""
step9() {
  # A URL determinística (serviço-número.região.run.app) é a que vai para o README e para o workflow; o
  # status.url do gcloud devolve a outra forma, com hash. As duas estão nas URLs do serviço.
  local urls hashed
  SERVICE_URL="https://$SERVICE-$PROJECT_NUMBER.$REGION.run.app"
  urls="$(gc run services describe "$SERVICE" --region="$REGION" --format='value(metadata.annotations."run.googleapis.com/urls")')" \
    || fail "não foi possível ler as URLs do serviço" || return 1
  grep -qF "\"$SERVICE_URL\"" <<<"$urls" || fail "a URL $SERVICE_URL não está entre as do serviço: $urls" || return 1
  hashed="$(gc run services describe "$SERVICE" --region="$REGION" --format='value(status.url)')"
  echo
  echo "GCP_PROJECT_ID=$PROJECT_ID"
  echo "GCP_PROJECT_NUMBER=$PROJECT_NUMBER"
  echo "GCP_REGION=$REGION"
  echo "GCP_WIF_PROVIDER=projects/$PROJECT_NUMBER/locations/global/workloadIdentityPools/$POOL/providers/$PROVIDER"
  echo "GCP_DEPLOY_SA=$DEPLOY_EMAIL"
  echo "GCP_RUNTIME_SA=$RUNTIME_EMAIL"
  echo "GCP_REGISTRY=$REGION-docker.pkg.dev/$PROJECT_ID/$REPOSITORY"
  echo "GCP_SERVICE=$SERVICE"
  echo "URL=$SERVICE_URL"
  echo "URL_ALTERNATIVA=$hashed"
}

run_step 0 "ferramentas, conta, projeto, faturamento e IDs do repositório" step0
run_step 1 "APIs ativadas" step1
run_step 2 "repositório de imagens $REPOSITORY e política de limpeza" step2
run_step 3 "conta de execução $RUNTIME_SA" step3
run_step 4 "conta de publicação $DEPLOY_SA, sem chaves" step4
run_step 5 "pool de identidade $POOL" step5
run_step 6 "provedor OIDC $PROVIDER (repositório e main)" step6
run_step 7 "serviço $SERVICE no Cloud Run" step7
run_step 8 "vínculos de IAM com permissões mínimas" step8
run_step 9 "identificadores públicos" step9
finish
