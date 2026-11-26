#!/usr/bin/env bash
# Conferência da URL publicada (spec 005, FR-004a, FR-014 a FR-016 e SC-001; research R6;
# contracts/verify-deployment.md).
#
# Uso: .github/scripts/verify-deployment.sh [--expect-no-metrics] <URL base> [espera máxima em segundos]
#
# Roda no job deploy, contra a URL da revisão (antes de ela receber tráfego) e contra a URL pública (depois),
# e localmente contra qualquer URL, inclusive http://127.0.0.1:<porta>. As chamadas são anônimas (FR-004).
# Com --expect-no-metrics, confere também que /actuator/prometheus responde 404 (FR-004a); sem a opção, como
# na execução local do modo padrão, que expõe as métricas, esse passo não roda.
#
# Cada passo imprime OK ou FALHA. Com GITHUB_STEP_SUMMARY definido, grava uma tabela no resumo.
# Precisa de curl e jq. Códigos de saída: 0 todos os passos OK, 1 algum em FALHA, 2 ferramenta ausente.
set -euo pipefail

EXPECT_NO_METRICS=0
if [[ "${1:-}" == "--expect-no-metrics" ]]; then
  EXPECT_NO_METRICS=1
  shift
fi
BASE_URL="${1:?uso: verify-deployment.sh [--expect-no-metrics] <URL base> [espera máxima em segundos]}"
BASE_URL="${BASE_URL%/}"
MAX_WAIT="${2:-180}"

for tool in curl jq; do
  if ! command -v "$tool" >/dev/null 2>&1; then
    echo "verify-deployment.sh: '$tool' não foi encontrado no PATH." >&2
    exit 2
  fi
done

# A mesma referência do teste de fumaça (.github/scripts/smoke-test.sh, REFERENCE): o resultado do arquivo
# fornecido, no contrato exato do PDF (Princípio III).
REFERENCE='{"min":[{"producer":"Joel Silver","interval":1,"previousWin":1990,"followingWin":1991}],"max":[{"producer":"Matthew Vaughn","interval":13,"previousWin":2002,"followingWin":2015}]}'

TMP="$(mktemp -d)"
trap 'rm -rf "$TMP"' EXIT

PASSED=0; FAILED=0
ROWS=()
record() { # passo, resultado, descrição, detalhe
  ROWS+=("| $1 | $2 | $3 | $4 |")
  if [[ "$2" == OK ]]; then PASSED=$((PASSED + 1)); else FAILED=$((FAILED + 1)); fi
  echo "==> passo $1: $2 - $3 ($4)"
}
run_step() { # passo, descrição, função
  DETAIL=""
  echo "--- passo $1: $2"
  if "$3"; then record "$1" OK "$2" "$DETAIL"; else record "$1" FALHA "$2" "$DETAIL"; fi
}

# GET com status, tempo total e Content-Type; o corpo fica em $TMP/body. Imprime "status tempo tipo".
# O tempo limite por chamada é 10 s, ou CALL_TIMEOUT. Na readiness, 60 s: na partida a frio, o Cloud Run segura
# a primeira chamada até a instância subir, e o tempo dela é o da partida (SC-001).
fetch() { # caminho, [opções extras do curl...]
  local path="$1" out; shift
  out="$(curl -sS -o "$TMP/body" --max-time "${CALL_TIMEOUT:-10}" -w '%{http_code} %{time_total} %{content_type}' \
    "$@" "$BASE_URL$path" 2>"$TMP/err")" || true
  printf '%s' "${out:-000 0 -}"
}
show_body() {
  echo "    corpo recebido (até 2 KB):"
  head -c 2048 "$TMP/body" 2>/dev/null | sed 's/^/      /'
  echo
  [[ -s "$TMP/err" ]] && sed 's/^/    curl: /' "$TMP/err"
  return 0
}

DETAIL=""
step1() {
  # Readiness positiva só depois da carga do CSV. A primeira chamada pode pegar a partida a frio.
  local start now out status time first_time=""
  start="$(date +%s)"
  while :; do
    out="$(CALL_TIMEOUT=60 fetch /actuator/health/readiness)"
    read -r status time _ <<<"$out"
    [[ -z "$first_time" ]] && first_time="$time"
    if [[ "$status" == "200" ]] && [[ "$(jq -r '.status // ""' "$TMP/body" 2>/dev/null)" == "UP" ]]; then
      now="$(date +%s)"
      DETAIL="readiness UP em $((now - start)) s; primeira chamada em ${first_time} s"
      echo "    $DETAIL"
      return 0
    fi
    now="$(date +%s)"
    if (( now - start >= MAX_WAIT )); then
      DETAIL="readiness sem UP em ${MAX_WAIT} s (último status $status)"
      echo "    $DETAIL"
      show_body
      return 1
    fi
    sleep 3
  done
}

step2() {
  local out status time type
  out="$(fetch /api/producers/intervals)"
  read -r status time type <<<"$out"
  DETAIL="HTTP $status em ${time} s"
  echo "    $DETAIL, $type"
  if [[ "$status" != "200" || "$type" != application/json* ]]; then
    show_body
    return 1
  fi
  if [[ "$(jq -S . <<<"$REFERENCE")" != "$(jq -S . "$TMP/body" 2>/dev/null)" ]]; then
    DETAIL="$DETAIL; corpo diferente da referência"
    show_body
    return 1
  fi
  DETAIL="$DETAIL; resultado de referência"
}

step3() {
  local status_ui status_api time_ui
  read -r status_ui time_ui _ <<<"$(fetch /swagger-ui.html -L)"
  read -r status_api _ _ <<<"$(fetch /openapi.yaml)"
  DETAIL="swagger-ui.html HTTP $status_ui em ${time_ui} s; openapi.yaml HTTP $status_api"
  echo "    $DETAIL"
  [[ "$status_ui" == "200" && "$status_api" == "200" ]]
}

step4() {
  local status
  read -r status _ _ <<<"$(fetch /actuator/prometheus)"
  DETAIL="actuator/prometheus HTTP $status"
  echo "    $DETAIL"
  [[ "$status" == "404" ]] || { show_body; return 1; }
}

echo "Conferência de $BASE_URL (espera máxima de ${MAX_WAIT} s)"
run_step 1 "readiness" step1
if (( FAILED == 0 )); then
  run_step 2 "resultado de referência em /api/producers/intervals" step2
  run_step 3 "Swagger UI e contrato OpenAPI" step3
  if (( EXPECT_NO_METRICS == 1 )); then
    run_step 4 "métricas fechadas na URL publicada" step4
  fi
fi

if [[ -n "${GITHUB_STEP_SUMMARY:-}" ]]; then
  {
    echo "### Conferência de $BASE_URL"
    echo
    echo "| Passo | Resultado | O que confere | Detalhe |"
    echo "| --- | --- | --- | --- |"
    printf '%s\n' "${ROWS[@]}"
    echo
  } >> "$GITHUB_STEP_SUMMARY"
fi

echo "verify-deployment.sh: $PASSED OK, $FAILED FALHA"
(( FAILED == 0 ))
