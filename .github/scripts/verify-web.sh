#!/usr/bin/env bash
# Conferência do front publicado (spec 006, FR-029 e SC-001; contracts/ci-frontend.md, "verify-web.sh <url>").
#
# Uso: .github/scripts/verify-web.sh <URL base> [espera máxima em segundos]
#
# Roda no job deploy-web, contra a URL da revisão (antes de ela receber tráfego) e contra a URL pública (depois),
# e localmente contra qualquer URL do front. As chamadas são anônimas. Cada passo tenta de novo até a espera
# máxima (padrão 120 s), para tolerar a partida a frio dos dois serviços (o awards-web e o awards, chamado pelo
# repasse de /api). Imprime o tempo da primeira resposta completa do passo 3 (tempo_primeira_resposta), usado
# para registrar o SC-001.
#
# Precisa de curl e node. Códigos de saída: 0 todos os passos OK, 1 algum em FALHA, 2 ferramenta ausente.
set -euo pipefail

BASE_URL="${1:?uso: verify-web.sh <URL base> [espera máxima em segundos]}"
BASE_URL="${BASE_URL%/}"
MAX_WAIT="${2:-120}"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

for tool in curl node; do
  if ! command -v "$tool" >/dev/null 2>&1; then
    echo "verify-web.sh: '$tool' não foi encontrado no PATH." >&2
    exit 2
  fi
done

# O resultado de referência vem do teste de fumaça da API, sem cópia.
REFERENCE="$(sed -n "s/^REFERENCE='\(.*\)'$/\1/p" "$SCRIPT_DIR/smoke-test.sh")"
[[ -n "$REFERENCE" ]] || { echo "verify-web.sh: REFERENCE não encontrada no smoke-test.sh" >&2; exit 2; }
YEARS='{"years":[{"year":1986,"winnerCount":2},{"year":1990,"winnerCount":2},{"year":2015,"winnerCount":2}]}'

TMP="$(mktemp -d)"
trap 'rm -rf "$TMP"' EXIT
START="$(date +%s)"

PASSED=0; FAILED=0
ROWS=()
record() { # passo, resultado, descrição, detalhe
  ROWS+=("| $1 | $2 | $3 | $4 |")
  if [[ "$2" == OK ]]; then PASSED=$((PASSED + 1)); else FAILED=$((FAILED + 1)); fi
  echo "==> passo $1: $2 - $3 ($4)"
}

# GET com status e tempo; o corpo fica em $TMP/body. Imprime "status tempo".
fetch() { # caminho
  local out
  out="$(curl -sS -o "$TMP/body" --max-time 60 -w '%{http_code} %{time_total}' "$BASE_URL$1" 2>"$TMP/err")" || true
  printf '%s' "${out:-000 0}"
}

# Repete o pedido até a verificação passar ou a espera acabar. DETAIL fica com o último resultado.
check() { # passo, descrição, caminho, função que confere o corpo (recebe o status)
  local step="$1" description="$2" path="$3" verify="$4" result status time
  echo "--- passo $step: $description"
  while :; do
    result="$(fetch "$path")"; status="${result%% *}"; time="${result##* }"
    if "$verify" "$status"; then
      DETAIL="HTTP $status em ${time}s"
      [[ "$step" == 3 ]] && echo "tempo_primeira_resposta=$time"
      record "$step" OK "$description" "$DETAIL"
      return 0
    fi
    if (( $(date +%s) - START >= MAX_WAIT )); then
      echo "    resposta recebida (HTTP $status): $(head -c 400 "$TMP/body" 2>/dev/null; cat "$TMP/err" 2>/dev/null)"
      record "$step" FALHA "$description" "HTTP $status"
      return 0
    fi
    sleep 3
  done
}

is_ok() { [[ "$1" == 200 ]]; }
is_html() { [[ "$1" == 200 ]] && grep -q '<app-root' "$TMP/body"; }
INDEX=""
is_same_html() { is_html "$1" && [[ "$(cat "$TMP/body")" == "$INDEX" ]]; }
is_reference() { [[ "$1" == 200 ]] && [[ "$(cat "$TMP/body")" == "$REFERENCE" ]]; }
is_years() { [[ "$1" == 200 ]] && [[ "$(cat "$TMP/body")" == "$YEARS" ]]; }
is_top_studios() {
  [[ "$1" == 200 ]] || return 1
  node -e '
    const s = JSON.parse(require("fs").readFileSync(process.argv[1], "utf8")).studios.slice(0, 3);
    const ok = JSON.stringify(s) === JSON.stringify([
      { name: "Columbia Pictures", winCount: 7 }, { name: "Paramount Pictures", winCount: 6 }, { name: "Warner Bros.", winCount: 5 }]);
    process.exit(ok ? 0 : 1);
  ' "$TMP/body" 2>/dev/null
}

check 1 "/health responde 200" /health is_ok
check 2 "/ devolve o index.html do Angular" / is_html
INDEX="$(cat "$TMP/body" 2>/dev/null || true)"
check 2b "/filmes devolve o mesmo index.html (rota interna)" /filmes is_same_html
check 3 "/api/producers/intervals pelo repasse, com o resultado de referência" /api/producers/intervals is_reference
check 4 "/api/aggregations/years-with-multiple-winners: 1986, 1990 e 2015" /api/aggregations/years-with-multiple-winners is_years
check 5 "/api/aggregations/studios-with-win-count: Columbia 7, Paramount 6, Warner 5" /api/aggregations/studios-with-win-count is_top_studios

echo
echo "verify-web.sh: $PASSED OK, $FAILED FALHA em $BASE_URL"
if [[ -n "${GITHUB_STEP_SUMMARY:-}" ]]; then
  {
    echo "### Conferência do front: $BASE_URL"
    echo
    echo "| Passo | Resultado | O que confere | Detalhe |"
    echo "| --- | --- | --- | --- |"
    printf '%s\n' "${ROWS[@]}"
  } >> "$GITHUB_STEP_SUMMARY"
fi
((FAILED == 0))
