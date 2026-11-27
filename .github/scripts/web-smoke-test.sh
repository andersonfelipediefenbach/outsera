#!/usr/bin/env bash
# Teste de fumaça da imagem do front (spec 006, FR-025, FR-026 e FR-026a; contracts/web-image.md, "Teste de
# fumaça").
#
# Uso: .github/scripts/web-smoke-test.sh <imagem> <api_url>
#
# <api_url> é a origem da API vista de dentro do container. O host precisa estar no DNS ou ser um IP: o nginx resolve o
# API_URL pelo resolver, que ignora o /etc/hosts. No runner Linux, use o IP do gateway da rede bridge (job `web`); no
# Docker Desktop, http://host.docker.internal:8080 também funciona, porque ele está no DNS. Roda no job `web` do
# pipeline e, localmente, no Linux e no Git Bash do Windows com Docker Desktop. Precisa de docker, curl e node.
# Cada passo imprime OK ou FALHA; o resumo vai para o GITHUB_STEP_SUMMARY quando existe.
set -euo pipefail

IMAGE="${1:?uso: web-smoke-test.sh <imagem> <api_url>}"
API_URL="${2:?uso: web-smoke-test.sh <imagem> <api_url>}"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

for tool in docker curl node; do
  if ! command -v "$tool" >/dev/null 2>&1; then
    echo "web-smoke-test.sh: '$tool' não foi encontrado no PATH." >&2
    exit 2
  fi
done

# O resultado de referência vem do teste de fumaça da API, sem cópia (contracts/web-image.md, passo 4).
REFERENCE="$(sed -n "s/^REFERENCE='\(.*\)'$/\1/p" "$SCRIPT_DIR/smoke-test.sh")"
[[ -n "$REFERENCE" ]] || { echo "web-smoke-test.sh: REFERENCE não encontrada no smoke-test.sh" >&2; exit 2; }

CONTAINERS=()
cleanup() { for c in "${CONTAINERS[@]}"; do docker rm -f "$c" >/dev/null 2>&1 || true; done; }
trap cleanup EXIT

PASSED=0; FAILED=0
SUMMARY=()
record() { # passo, resultado, descrição
  SUMMARY+=("passo $1: $2 - $3")
  if [[ "$2" == OK ]]; then PASSED=$((PASSED + 1)); else FAILED=$((FAILED + 1)); fi
  echo "==> passo $1: $2 - $3"
}
run_step() { # passo, descrição, função
  echo "--- passo $1: $2"
  if "$3"; then record "$1" OK "$2"; else record "$1" FALHA "$2"; fi
}

# A porta do container vai para uma porta aleatória do host, só em 127.0.0.1.
start_container() { # nome, argumentos extras do docker run...
  local name="$1"; shift
  CONTAINERS+=("$name")
  docker run -d --name "$name" -p "127.0.0.1::8080" --add-host host.docker.internal:host-gateway "$@" "$IMAGE" >/dev/null
}
url_of() { # nome
  local mapping
  mapping="$(docker port "$1" 8080/tcp | sed -n '1p')"
  [[ -n "$mapping" ]] || { echo "    o container $1 não publicou a porta 8080"; return 1; }
  printf 'http://127.0.0.1:%s' "${mapping##*:}"
}
wait_ready() { # nome, segundos
  local base
  for _ in $(seq 1 "$2"); do
    if [[ "$(docker inspect --format '{{.State.Status}}' "$1")" != running ]]; then
      echo "    o container $1 parou"; docker logs "$1" 2>&1 | tail -n 20; return 1
    fi
    base="$(url_of "$1")" || return 1
    curl -sf "$base/health" >/dev/null 2>&1 && return 0
    sleep 1
  done
  echo "    o container $1 não respondeu /health em $2 s"; return 1
}
# Só o stdout do container: o log de acesso. O stderr (log de erro do nginx) fica de fora.
stdout_of() { docker logs "$1" 2>/dev/null; }
# Espera o container terminar e devolve o código de saída.
exit_code_of() { # nome, segundos
  for _ in $(seq 1 "$2"); do
    if [[ "$(docker inspect --format '{{.State.Status}}' "$1")" == exited ]]; then
      docker inspect --format '{{.State.ExitCode}}' "$1"; return 0
    fi
    sleep 1
  done
  echo "ainda-rodando"
}

WEB="awards-web-smoke-$$"
BASE=""

step1() { start_container "$WEB" -e "API_URL=$API_URL" && wait_ready "$WEB" 30 && BASE="$(url_of "$WEB")"; }
step2() {
  local headers body
  headers="$(curl -s -D - -o /dev/null "$BASE/")"
  body="$(curl -sf "$BASE/")" || return 1
  grep -q '^HTTP/1.1 200' <<<"$headers" || { echo "    status: $(sed -n 1p <<<"$headers")"; return 1; }
  grep -q '<app-root' <<<"$body" && grep -q 'lang="pt-BR"' <<<"$body" || { echo "    HTML inesperado"; return 1; }
  grep -qi "^content-security-policy: default-src 'self'" <<<"$headers" || { echo "    sem CSP"; return 1; }
  grep -qi '^x-content-type-options: nosniff' <<<"$headers" && grep -qi '^x-frame-options: DENY' <<<"$headers" \
    && grep -qi '^cache-control: no-cache' <<<"$headers" || { echo "    cabeçalhos: $headers"; return 1; }
  ! grep -qi '^server: nginx/' <<<"$headers" || { echo "    o Server mostra a versão"; return 1; }
}
step3() {
  local index route
  index="$(curl -sf "$BASE/")" && route="$(curl -sf "$BASE/filmes?ano=1990")" || return 1
  [[ "$index" == "$route" ]] || { echo "    /filmes não devolveu o index.html"; return 1; }
}
ETAG=""
step4() {
  local headers body
  # O Authorization vai junto para provar que ele não entra no log (passo 9).
  headers="$(curl -s -D - -o "$TMP_BODY" -H 'Authorization: Bearer teste-de-fumaca' "$BASE/api/producers/intervals")"
  body="$(cat "$TMP_BODY")"
  grep -q '^HTTP/1.1 200' <<<"$headers" || { echo "    status: $(sed -n 1p <<<"$headers")"; return 1; }
  [[ "$body" == "$REFERENCE" ]] || { echo "    corpo: $body"; return 1; }
  ETAG="$(tr -d '\r' <<<"$headers" | awk 'tolower($1) == "etag:" { print $2 }')"
  [[ -n "$ETAG" ]] || { echo "    sem ETag"; return 1; }
}
step5() {
  local status
  status="$(curl -s -o /dev/null -w '%{http_code}' -H "If-None-Match: $ETAG" "$BASE/api/producers/intervals")"
  [[ "$status" == 304 ]] || { echo "    status: $status"; return 1; }
}
step6() {
  local body expected='{"years":[{"year":1986,"winnerCount":2},{"year":1990,"winnerCount":2},{"year":2015,"winnerCount":2}]}'
  body="$(curl -sf "$BASE/api/aggregations/years-with-multiple-winners")" || return 1
  [[ "$body" == "$expected" ]] || { echo "    corpo: $body"; return 1; }
}
step7() {
  local uid
  uid="$(docker exec "$WEB" id -u)"
  [[ -n "$uid" && "$uid" != 0 ]] || { echo "    UID do processo: $uid"; return 1; }
}
step8() {
  local name="awards-web-smoke-sem-api-$$" code
  CONTAINERS+=("$name")
  docker run -d --name "$name" "$IMAGE" >/dev/null
  code="$(exit_code_of "$name" 20)"
  [[ "$code" != 0 && "$code" != ainda-rodando ]] || { echo "    código de saída: $code"; return 1; }
  docker logs "$name" 2>&1 | grep -q 'API_URL não definida' || { echo "    mensagem ausente"; return 1; }
}
step9() {
  local out
  out="$(stdout_of "$WEB")"
  [[ -n "$out" ]] || { echo "    stdout vazio"; return 1; }
  node -e '
    const lines = require("fs").readFileSync(0, "utf8").split("\n").filter((l) => l.trim() !== "");
    const fields = ["time","method","path","query","status","bytes","duration","upstream_duration","user_agent","remote_addr"];
    let api = false;
    for (const line of lines) {
      let entry;
      try { entry = JSON.parse(line); } catch { console.log("    linha que não é JSON: " + line); process.exit(1); }
      const missing = fields.filter((f) => !(f in entry));
      if (missing.length) { console.log("    campos ausentes " + missing + ": " + line); process.exit(1); }
      if (typeof entry.status !== "number" || typeof entry.bytes !== "number" || typeof entry.duration !== "number") {
        console.log("    status, bytes ou duration não numéricos: " + line); process.exit(1);
      }
      if (entry.path === "/health") { console.log("    /health no log: " + line); process.exit(1); }
      if (/authorization|teste-de-fumaca/i.test(line)) { console.log("    Authorization no log: " + line); process.exit(1); }
      if (entry.path === "/api/producers/intervals" && entry.status === 200) api = true;
    }
    if (!api) { console.log("    sem a linha de /api/producers/intervals com 200"); process.exit(1); }
    console.log("    " + lines.length + " linhas de JSON válido");
  ' <<<"$out"
}
step10() {
  local text="awards-web-smoke-texto-$$" bad="awards-web-smoke-xml-$$" line code
  start_container "$text" -e "API_URL=$API_URL" -e LOG_FORMAT=main && wait_ready "$text" 30 || return 1
  curl -sf "$(url_of "$text")/" >/dev/null || return 1
  sleep 1
  line="$(stdout_of "$text" | tail -n 1)"
  if node -e 'JSON.parse(process.argv[1])' "$line" 2>/dev/null; then echo "    LOG_FORMAT=main gerou JSON: $line"; return 1; fi
  CONTAINERS+=("$bad")
  docker run -d --name "$bad" -e "API_URL=$API_URL" -e LOG_FORMAT=xml "$IMAGE" >/dev/null
  code="$(exit_code_of "$bad" 20)"
  [[ "$code" != 0 && "$code" != ainda-rodando ]] || { echo "    LOG_FORMAT=xml: código $code"; return 1; }
  docker logs "$bad" 2>&1 | grep -q 'LOG_FORMAT deve ser json ou main' || { echo "    mensagem ausente"; return 1; }
}

TMP_BODY="$(mktemp)"
trap 'cleanup; rm -f "$TMP_BODY"' EXIT

run_step 1 "sobe com API_URL e responde /health em até 30 s" step1
if [[ -z "$BASE" ]]; then
  echo "web-smoke-test.sh: o container não subiu; os outros passos não rodam." >&2
  docker logs "$WEB" 2>&1 | tail -n 30 || true
  exit 1
fi
run_step 2 "/ devolve o index.html em pt-BR com CSP e cabeçalhos de segurança" step2
run_step 3 "rota interna /filmes?ano=1990 devolve o mesmo index.html" step3
run_step 4 "/api/producers/intervals repassado com o resultado de referência e ETag" step4
run_step 5 "If-None-Match com o ETag dá 304 pelo repasse" step5
run_step 6 "/api/aggregations/years-with-multiple-winners repassado" step6
run_step 7 "o processo do container não roda como root" step7
run_step 8 "sem API_URL, a subida falha com a mensagem esperada" step8
run_step 9 "log de acesso em JSON, sem /health e sem Authorization" step9
run_step 10 "LOG_FORMAT=main gera texto, e LOG_FORMAT=xml falha na subida" step10

echo
echo "web-smoke-test.sh: $PASSED OK, $FAILED FALHA"
if [[ -n "${GITHUB_STEP_SUMMARY:-}" ]]; then
  { echo "### Teste de fumaça da imagem do front"; echo; printf -- '- %s\n' "${SUMMARY[@]}"; } >> "$GITHUB_STEP_SUMMARY"
fi
((FAILED == 0))
