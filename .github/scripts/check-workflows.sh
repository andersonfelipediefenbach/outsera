
#!/usr/bin/env bash
# Política dos workflows e do Dependabot (spec 002, FR-028b, FR-029, SC-007; research R9 e R12).
#
# Uso: .github/scripts/check-workflows.sh [raiz]
#
# Verifica, em <raiz>/.github (padrão: a raiz do repositório):
#   1. todo `uses:` está fixado por hash de commit de 40 caracteres;
#   2. o `permissions` do topo de cada workflow é exatamente `contents: read`;
#   3. nenhum job pede escrita, exceto `security-events: write` no job `codeql`;
#   4. não há referência a `secrets.`;
#   5. não há publicação de imagem (`push: true` no build-push-action ou `docker push`);
#   6. o dependabot.yml tem version 2 e os ecossistemas maven, github-actions e docker, com
#      directory "/" e schedule.interval.
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

  # 3. Escrita só em security-events, só no job codeql.
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
      violation "$name" "job $job pede '$key: $value' (só o job codeql pode ter security-events: write)"
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
done

# 6. Dependabot.
DEPENDABOT="$ROOT/.github/dependabot.yml"
if [[ ! -f "$DEPENDABOT" ]]; then
  violation ".github/dependabot.yml" "arquivo ausente"
else
  version="$(yqr -r '.version // ""' "$DEPENDABOT")"
  [[ "$version" == "2" ]] || violation ".github/dependabot.yml" "version deveria ser 2 (encontrado: '${version:-ausente}')"
  for eco in maven github-actions docker; do
    found="$(yqr -r ".updates[]? | select(.\"package-ecosystem\" == \"$eco\" and .directory == \"/\" and (.schedule.interval // \"\") != \"\") | .\"package-ecosystem\"" "$DEPENDABOT")"
    [[ -n "$found" ]] || violation ".github/dependabot.yml" "falta o ecossistema $eco com directory \"/\" e schedule.interval"
  done
fi

if (( ${#VIOLATIONS[@]} > 0 )); then
  echo "check-workflows.sh: ${#VIOLATIONS[@]} violação(ões) da política:" >&2
  printf '  - %s\n' "${VIOLATIONS[@]}" >&2
  exit 1
fi
echo "check-workflows.sh: ${#WORKFLOWS[@]} workflow(s) e o dependabot.yml dentro da política."
