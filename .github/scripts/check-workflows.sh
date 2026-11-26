
#!/usr/bin/env bash
# Política dos workflows e do Dependabot (spec 002, FR-028b, FR-029, SC-007; research R9 e R12).
#
# Uso: .github/scripts/check-workflows.sh [raiz]
#
# Verifica, em <raiz>/.github (padrão: a raiz do repositório):
#   1. todo `uses:` está fixado por hash de commit de 40 caracteres;
#   2. o `permissions` do topo de cada workflow é exatamente `contents: read`;
#   3. nenhum job pede escrita, exceto `security-events: write` no job `codeql` e `id-token: write` no job
#      `deploy` (spec 005, FR-012);
#   4. não há referência a `secrets.`;
#   5. não há publicação de imagem (`push: true` no build-push-action ou `docker push`), e o `skopeo`, que
#      envia a imagem testada ao registro, só aparece no job `deploy` (spec 005, FR-012);
#   6. o dependabot.yml tem version 2 e os ecossistemas maven, github-actions, docker e docker-compose,
#      com directory "/" e schedule.interval (docker-compose: spec 003, FR-034);
#   7. toda imagem do compose.yaml e dos service containers dos workflows está fixada por digest
#      (@sha256: com 64 caracteres hexadecimais; spec 003, FR-034; research R17);
#   8. nenhum service container de workflow declara POSTGRES_PASSWORD: o job postgres usa trust, e a
#      credencial de demonstração só pode estar no compose (constituição 1.6.0; research R16);
#   9. se existe o job `deploy`: o `if` (normalizado) é o do contrato, os `needs` têm os checks
#      obrigatórios da main, a concorrência é o grupo `deploy-cloud-run` sem cancelamento e não há build
#      de imagem no job (spec 005, FR-008, FR-011 e FR-012; contracts/workflow-policy.md);
#  10. o `concurrency.cancel-in-progress` do topo não é `true` fixo, para não cancelar uma publicação na
#      main (spec 005, FR-011).
#
# É a verificação oficial do job `workflow-policy`. Precisa do yq (mikefarah/yq v4), que já vem nos
# runners ubuntu-latest. Códigos de saída: 0 ok, 1 violação, 2 ferramenta ausente.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT="${1:-$(cd "$SCRIPT_DIR/../.." && pwd)}"

if ! command -v yq >/dev/null 2>&1; then
  echo "check-workflows.sh: o yq não foi encontrado no PATH. Instale o yq (https://github.com/mikefarah/yq) para rodar localmente; a verificação oficial é a do job workflow-policy do pipeline." >&2
  exit 2
fi

# O yq do Windows pode terminar as linhas com \r; no Linux, isso não muda nada.
yqr() { yq "$@" | tr -d '\r'; }

VIOLATIONS=()
violation() { VIOLATIONS+=("$1: $2"); }

shopt -s nullglob
WORKFLOWS=("$ROOT"/.github/workflows/*.yml "$ROOT"/.github/workflows/*.yaml)
if (( ${#WORKFLOWS[@]} == 0 )); then
  violation "$ROOT/.github/workflows" "nenhum workflow encontrado"
fi

PINNED='^[^@[:space:]]+@[0-9a-f]{40}$'
DIGEST='^[^@[:space:]]+@sha256:[0-9a-f]{64}$'

# Regra 9 (spec 005; contracts/ci-deploy.md). A lista acompanha os checks obrigatórios da main (Marco 4,
# T064): um check obrigatório novo entra aqui e nos `needs` do job deploy, no mesmo commit (FR-008).
DEPLOY_IF="github.event_name == 'push' && github.ref == 'refs/heads/main' && github.repository == 'andersonfelipediefenbach/outsera'"
REQUIRED_CHECKS=(workflow-policy build postgres codeql dependencies dockerfile-lint image alert-rules load compose)

# Normaliza uma expressão de `if`: sem ${{ }}, sem espaços nas pontas e com espaços repetidos reduzidos.
normalize_if() {
  printf '%s' "$1" | tr '\n' ' ' \
    | sed -E 's/^[[:space:]]*\$\{\{[[:space:]]*//; s/[[:space:]]*\}\}[[:space:]]*$//; s/[[:space:]]+/ /g; s/^ //; s/ $//'
}
for wf in "${WORKFLOWS[@]}"; do
  name="${wf#"$ROOT"/}"

  # 1. Toda action fixada por hash de commit (as locais, ./..., ficam de fora).
  while IFS= read -r ref; do
    [[ -z "$ref" || "$ref" == ./* ]] && continue
    [[ "$ref" =~ $PINNED ]] || violation "$name" "uses sem hash de commit de 40 caracteres: $ref"
  done < <(yqr -r '.. | select(tag == "!!map" and has("uses")) | .uses' "$wf")

  # 2. Permissões do topo: exatamente contents: read.
  top="$(yqr -o=json -I=0 '.permissions // null' "$wf")"
  [[ "$top" == '{"contents":"read"}' ]] || violation "$name" "permissions do topo deveria ser exatamente 'contents: read' (encontrado: $top)"

  # 3. Escrita só em security-events, no job codeql, e em id-token, no job deploy.
  while IFS= read -r job; do
    [[ -z "$job" ]] && continue
    kind="$(yqr -r ".jobs.\"$job\".permissions | tag" "$wf")"
    if [[ "$kind" != "!!map" ]]; then
      violation "$name" "job $job declara permissions '$(yqr -r ".jobs.\"$job\".permissions" "$wf")' (use um mapa com permissões explícitas)"
      continue
    fi
    while IFS='=' read -r key value; do
      [[ -z "$key" || "$value" == "read" || "$value" == "none" ]] && continue
      if [[ "$job" == "codeql" && "$key" == "security-events" && "$value" == "write" ]]; then
        continue
      fi
      if [[ "$job" == "deploy" && "$key" == "id-token" && "$value" == "write" ]]; then
        continue
      fi
      violation "$name" "job $job pede '$key: $value' (só o job codeql pode ter security-events: write, e só o job deploy, id-token: write)"
    done < <(yqr -r ".jobs.\"$job\".permissions | to_entries[] | .key + \"=\" + .value" "$wf")
  done < <(yqr -r '.jobs | to_entries[] | select(.value.permissions != null) | .key' "$wf")

  # 4. Nenhum segredo.
  if grep -n 'secrets\.' "$wf" >/dev/null; then
    violation "$name" "referência a secrets.: $(grep -n 'secrets\.' "$wf" | head -n 1)"
  fi

  # 5. Nenhuma publicação de imagem.
  if [[ -n "$(yqr -r '.. | select(tag == "!!map" and has("uses")) | select(.uses | test("^docker/build-push-action@")) | select((.with.push // false) == true) | .uses' "$wf")" ]]; then
    violation "$name" "docker/build-push-action com push: true"
  fi
  if yqr -r '.. | select(tag == "!!map" and has("run")) | .run' "$wf" | grep -q 'docker push'; then
    violation "$name" "passo run com docker push"
  fi
  while IFS= read -r job; do
    [[ -z "$job" ]] && continue
    violation "$name" "job $job usa skopeo (o envio de imagem ao registro só pode estar no job deploy)"
  done < <(yqr -r '.jobs | to_entries[] | select(.key != "deploy") | select([.value.steps[]?.run // "" | test("(^|[^[:alnum:]_-])skopeo([^[:alnum:]_-]|$)")] | any) | .key' "$wf")

  # 9. Job deploy: condição, dependências, concorrência e nenhum build.
  if [[ "$(yqr -r '.jobs | has("deploy")' "$wf")" == "true" ]]; then
    deploy_if="$(normalize_if "$(yqr -r '.jobs.deploy.if // ""' "$wf")")"
    [[ "$deploy_if" == "$DEPLOY_IF" ]] \
      || violation "$name" "job deploy com if diferente do contrato (encontrado: '$deploy_if'; esperado: '$DEPLOY_IF')"
    needs="$(yqr -r '[.jobs.deploy.needs] | flatten | .[] // ""' "$wf")"
    for check in "${REQUIRED_CHECKS[@]}"; do
      grep -qxF "$check" <<<"$needs" || violation "$name" "job deploy sem o check obrigatório '$check' em needs"
    done
    group="$(yqr -r '.jobs.deploy.concurrency.group // ""' "$wf")"
    cancel="$(yqr -o=json '.jobs.deploy.concurrency."cancel-in-progress"' "$wf")"
    [[ "$group" == "deploy-cloud-run" && "$cancel" == "false" ]] \
      || violation "$name" "job deploy deveria ter concurrency.group 'deploy-cloud-run' e cancel-in-progress: false (encontrado: '$group', $cancel)"
    if [[ -n "$(yqr -r '.jobs.deploy.steps[]?.uses // "" | select(test("^docker/build-push-action@"))' "$wf")" ]]; then
      violation "$name" "job deploy com docker/build-push-action (a imagem publicada é a do job image, sem rebuild)"
    fi
    if yqr -r '.jobs.deploy.steps[]?.run // ""' "$wf" | grep -qE 'docker[[:space:]]+(buildx[[:space:]]+)?build'; then
      violation "$name" "job deploy com docker build (a imagem publicada é a do job image, sem rebuild)"
    fi
  fi

  # 10. A concorrência do topo não pode cancelar sempre (cancelaria uma publicação na main).
  if [[ "$(yqr -o=json '.concurrency."cancel-in-progress"' "$wf")" == "true" ]]; then
    violation "$name" "concurrency.cancel-in-progress do topo é true fixo (use uma expressão que não cancele na main)"
  fi

  # 7 e 8. Service containers: imagem por digest e sem POSTGRES_PASSWORD.
  while IFS= read -r image; do
    [[ -z "$image" ]] && continue
    [[ "$image" =~ $DIGEST ]] || violation "$name" "service container sem digest: $image"
  done < <(yqr -r '.jobs[].services[]?.image // ""' "$wf")
  while IFS= read -r job; do
    [[ -z "$job" ]] && continue
    violation "$name" "job $job declara POSTGRES_PASSWORD num service container (use trust; research R16)"
  done < <(yqr -r '.jobs | to_entries[] | select([.value.services[]?.env // {} | has("POSTGRES_PASSWORD")] | any) | .key' "$wf")
done

# 6. Dependabot.
DEPENDABOT="$ROOT/.github/dependabot.yml"
if [[ ! -f "$DEPENDABOT" ]]; then
  violation ".github/dependabot.yml" "arquivo ausente"
else
  version="$(yqr -r '.version // ""' "$DEPENDABOT")"
  [[ "$version" == "2" ]] || violation ".github/dependabot.yml" "version deveria ser 2 (encontrado: '${version:-ausente}')"
  for eco in maven github-actions docker docker-compose; do
    found="$(yqr -r ".updates[]? | select(.\"package-ecosystem\" == \"$eco\" and .directory == \"/\" and (.schedule.interval // \"\") != \"\") | .\"package-ecosystem\"" "$DEPENDABOT")"
    [[ -n "$found" ]] || violation ".github/dependabot.yml" "falta o ecossistema $eco com directory \"/\" e schedule.interval"
  done
fi

# 7. Imagens do compose.yaml por digest (as construídas pelo próprio compose, com build, ficam de fora).
COMPOSE="$ROOT/compose.yaml"
if [[ ! -f "$COMPOSE" ]]; then
  violation "compose.yaml" "arquivo ausente"
else
  while IFS='=' read -r service image; do
    [[ -z "$service" ]] && continue
    [[ "$image" =~ $DIGEST ]] || violation "compose.yaml" "serviço $service com imagem sem digest: $image"
  done < <(yqr -r '.services | to_entries[] | select(.value.image != null and .value.build == null) | .key + "=" + .value.image' "$COMPOSE")
fi

if (( ${#VIOLATIONS[@]} > 0 )); then
  echo "check-workflows.sh: ${#VIOLATIONS[@]} violação(ões) da política:" >&2
  printf '  - %s\n' "${VIOLATIONS[@]}" >&2
  exit 1
fi
echo "check-workflows.sh: ${#WORKFLOWS[@]} workflow(s), o dependabot.yml e o compose.yaml dentro da política."
