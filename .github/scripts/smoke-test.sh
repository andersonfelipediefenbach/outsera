#!/usr/bin/env bash
# Teste de fumaça da imagem (spec 002, história 5; contracts/ci-pipeline.md; research R14).
#
# Uso: .github/scripts/smoke-test.sh <imagem>
#
# Roda no job `image` do pipeline e, localmente, no Linux e no Git Bash do Windows com Docker
# Desktop. Precisa de docker, curl e jq. Cada passo imprime OK, FALHA ou PULADO (com o motivo). Um
# passo só pode ser pulado fora do pipeline; no pipeline (CI=true), PULADO é falha.
set -euo pipefail

IMAGE="${1:?uso: smoke-test.sh <imagem>}"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
SCENARIO="$REPO_ROOT/src/test/resources/scenarios/disjoint-producers.csv"
START_NS="$(date +%s%N)"

for tool in docker curl jq; do
  if ! command -v "$tool" >/dev/null 2>&1; then
    echo "smoke-test.sh: '$tool' não foi encontrado no PATH." >&2
    exit 2
  fi
done

# No Git Bash, os caminhos dos bind mounts precisam estar no formato do Windows, e a conversão
# automática do MSYS não pode mexer nos caminhos de dentro do container.
case "$(uname -s)" in
  MINGW*|MSYS*) export MSYS_NO_PATHCONV=1; IS_WINDOWS=1 ;;
  *) IS_WINDOWS=0 ;;
esac
host_path() {
  if [[ "$IS_WINDOWS" == 1 ]]; then cygpath -w "$1"; else printf '%s' "$1"; fi
}

CONTAINERS=()
TEMP_FILES=()
cleanup() {
  for c in "${CONTAINERS[@]}"; do docker rm -f "$c" >/dev/null 2>&1 || true; done
  for f in "${TEMP_FILES[@]}"; do chmod 600 "$f" 2>/dev/null || true; rm -f "$f"; done
}
trap cleanup EXIT

PASSED=0; FAILED=0; SKIPPED=0
SUMMARY=()
record() { # passo, resultado, descrição
  SUMMARY+=("passo $1: $2 - $3")
  case "$2" in
    OK) PASSED=$((PASSED + 1)) ;;
    FALHA) FAILED=$((FAILED + 1)) ;;
    PULADO*) SKIPPED=$((SKIPPED + 1)) ;;
  esac
  echo "==> passo $1: $2 - $3"
}
run_step() { # passo, descrição, função
  echo "--- passo $1: $2"
  if "$3"; then record "$1" OK "$2"; else record "$1" FALHA "$2"; fi
}
skip_step() { # passo, descrição, motivo
  if [[ "${CI:-}" == "true" ]]; then
    echo "    no pipeline, um passo pulado é falha: $3"
    record "$1" FALHA "$2"
  else
    record "$1" "PULADO ($3)" "$2"
  fi
}

start_container() { # nome, porta no host, porta no container, argumentos extras...
  local name="$1" host_port="$2" container_port="$3"; shift 3
  CONTAINERS+=("$name")
  docker run -d --name "$name" -p "$host_port:$container_port" "$@" "$IMAGE" >/dev/null
}
wait_healthy() { # nome, segundos
  local name="$1" limit="$2" status
  for _ in $(seq 1 "$limit"); do
    status="$(docker inspect --format '{{if .State.Health}}{{.State.Health.Status}}{{end}}|{{.State.Status}}' "$name")"
    case "$status" in
      healthy\|*) return 0 ;;
      *\|exited|*\|dead) echo "    o container $name parou: $status"; tail -n 20 <<<"$(logs_of "$name")"; return 1 ;;
    esac
    sleep 1
  done
  echo "    o container $name não ficou healthy em ${limit}s (último estado: $status)"
  return 1
}
same_json() { # esperado, obtido
  [[ "$(jq -S . <<<"$1")" == "$(jq -S . <<<"$2")" ]]
}
# O log é lido para uma variável antes de qualquer grep: com pipefail, um `docker logs | grep -q`
# falharia por SIGPIPE mesmo quando o texto é encontrado.
logs_of() { docker logs "$1" 2>&1; }

REFERENCE='{"min":[{"producer":"Joel Silver","interval":1,"previousWin":1990,"followingWin":1991}],"max":[{"producer":"Matthew Vaughn","interval":13,"previousWin":2002,"followingWin":2015}]}'
SCENARIO_EXPECTED='{"min":[{"producer":"Producer A","interval":2,"previousWin":1950,"followingWin":1952}],"max":[{"producer":"Producer B","interval":50,"previousWin":1925,"followingWin":1975}]}'
MAIN=awards-smoke-main

step1() { start_container "$MAIN" 8080 8080 && wait_healthy "$MAIN" 90; }
step2() {
  local body elapsed_ms
  body="$(curl -fsS http://127.0.0.1:8080/api/producers/intervals)" || return 1
  same_json "$REFERENCE" "$body" || { echo "    resposta: $body"; return 1; }
  elapsed_ms=$(( ($(date +%s%N) - START_NS) / 1000000 ))
  echo "    resultado de referência em ${elapsed_ms} ms desde o início do teste (SC-006: < 120000)"
  (( elapsed_ms < 120000 ))
}
step3() {
  local etag code
  etag="$(curl -fsS -D - -o /dev/null http://127.0.0.1:8080/api/producers/intervals \
    | awk 'tolower($1) == "etag:" { sub(/\r$/, "", $2); print $2 }')"
  [[ -n "$etag" ]] || { echo "    resposta sem ETag"; return 1; }
  code="$(curl -s -o /dev/null -w '%{http_code}' -H "If-None-Match: $etag" http://127.0.0.1:8080/api/producers/intervals)"
  echo "    ETag $etag, revalidação $code"
  [[ "$code" == "304" ]]
}
step4() {
  local uid
  uid="$(docker exec "$MAIN" id -u)"
  echo "    uid $uid"
  [[ "$uid" != "0" ]]
}
step5() {
  local logs
  logs="$(logs_of "$MAIN")"
  logs="$(grep -v '^[[:space:]]*$' <<<"$logs" || true)"
  [[ -n "$logs" ]] || { echo "    log vazio"; return 1; }
  jq -e -s 'length > 0' <<<"$logs" >/dev/null || { echo "    há linhas que não são JSON:"; grep -v '^{' <<<"$logs" | head -n 5; return 1; }
}
step6() {
  local t0 t1 ms
  t0="$(date +%s%N)"; docker stop -t 30 "$MAIN" >/dev/null; t1="$(date +%s%N)"
  ms=$(( (t1 - t0) / 1000000 ))
  echo "    parada em ${ms} ms (limite 10000)"
  (( ms < 10000 ))
}
step7() {
  start_container awards-smoke-port 9090 9090 -e PORT=9090 && wait_healthy awards-smoke-port 90 \
    && [[ "$(curl -s -o /dev/null -w '%{http_code}' http://127.0.0.1:9090/api/producers/intervals)" == "200" ]]
}
step8() {
  local name=awards-smoke-missing code
  CONTAINERS+=("$name")
  docker run -d --name "$name" -e MOVIES_CSV_PATH=file:/nao-existe.csv "$IMAGE" >/dev/null
  code="$(docker wait "$name")"
  echo "    código de saída $code"
  [[ "$code" != "0" ]] && grep -q 'file:/nao-existe.csv' <<<"$(logs_of "$name")"
}
step9() {
  local body
  start_container awards-smoke-scenario 8081 8080 \
    -v "$(host_path "$SCENARIO"):/data/movies.csv:ro" -e MOVIES_CSV_PATH=file:/data/movies.csv \
    && wait_healthy awards-smoke-scenario 90 || return 1
  body="$(curl -fsS http://127.0.0.1:8081/api/producers/intervals)" || return 1
  same_json "$SCENARIO_EXPECTED" "$body" || { echo "    resposta: $body"; return 1; }
}
step10() {
  docker run --rm --entrypoint sh "$IMAGE" -c \
    'test ! -e /app/src && test ! -e /build && test ! -e /app/pom.xml && ! command -v mvn && ! command -v javac'
}
step11() { "$SCRIPT_DIR/check-docker-context.sh"; }
step12() {
  local first
  start_container awards-smoke-text 8082 8080 -e LOGGING_STRUCTURED_FORMAT_CONSOLE= \
    && wait_healthy awards-smoke-text 90 || return 1
  first="$(grep -v '^[[:space:]]*$' <<<"$(logs_of awards-smoke-text)" | sed -n '1p' || true)"
  echo "    primeira linha: ${first:0:120}"
  [[ -n "$first" && "${first:0:1}" != "{" ]]
}
UNREADABLE=""
step13() {
  local name=awards-smoke-unreadable code
  CONTAINERS+=("$name")
  docker run -d --name "$name" -v "$(host_path "$UNREADABLE"):/data/movies.csv:ro" \
    -e MOVIES_CSV_PATH=file:/data/movies.csv "$IMAGE" >/dev/null
  local logs
  code="$(docker wait "$name")"
  logs="$(logs_of "$name")"
  echo "    código de saída $code"
  [[ "$code" != "0" ]] && grep -q 'UNREADABLE' <<<"$logs" && grep -q 'não é um arquivo legível' <<<"$logs"
}

run_step 1 "container fica healthy em até 90 s" step1
run_step 2 "intervalos com o resultado de referência" step2
run_step 3 "revalidação com o ETag recebido dá 304" step3
run_step 4 "processo do container não é root" step4
run_step 5 "toda linha do log é JSON" step5
run_step 6 "docker stop termina em menos de 10 s" step6
run_step 7 "com PORT=9090, fica healthy e responde nessa porta" step7
run_step 8 "com arquivo inexistente, termina com código diferente de 0 e a mensagem do Marco 1" step8
run_step 9 "com um CSV de cenário montado, responde o resultado do cenário" step9
run_step 10 "imagem final sem código-fonte, Maven nem compilador" step10
run_step 11 "contexto de build só com a lista de permissão do .dockerignore" step11
run_step 12 "com a variável de formato vazia, o log fica em texto" step12

# Passo 13: a permissão do host precisa valer dentro do container. Uma sonda com o usuário 10001
# decide se o passo roda (research R14).
UNREADABLE="$(mktemp "${TMPDIR:-/tmp}/awards-sem-leitura.XXXXXX.csv")"
TEMP_FILES+=("$UNREADABLE")
cp "$SCENARIO" "$UNREADABLE"
chmod 000 "$UNREADABLE"
if docker run --rm --user 10001:10001 --entrypoint sh -v "$(host_path "$UNREADABLE"):/data/movies.csv:ro" \
    "$IMAGE" -c 'test -r /data/movies.csv' >/dev/null 2>&1; then
  skip_step 13 "arquivo montado sem permissão de leitura" \
    "a permissão do arquivo no host não se aplica ao bind mount neste ambiente ($(uname -s), Docker $(docker version --format '{{.Server.Version}}' 2>/dev/null || echo '?'))"
else
  run_step 13 "arquivo montado sem permissão de leitura" step13
fi

echo
echo "Resumo do teste de fumaça ($IMAGE):"
printf '  %s\n' "${SUMMARY[@]}"
echo "  OK: $PASSED, FALHA: $FAILED, PULADO: $SKIPPED"
(( FAILED == 0 ))
