#!/usr/bin/env bash
# Teste do ambiente completo do compose (spec 003, FR-015a, FR-028 a FR-035, SC-005 e SC-008; research
# R20; contracts/ci-pipeline.md). Sobe o compose, confere a aplicação, o Prometheus, o Grafana, a ingestão
# por Kafka, a persistência, a queda real do banco e o token, e derruba tudo no fim, sempre.
#
# Uso: .github/scripts/compose-test.sh   (na raiz do repositório, com o Docker e o Compose v2)
#
# A credencial do Grafana usada aqui é a de demonstração do compose (constituição 1.6.0, Princípio VII).
# O token do passo 8 é gerado na hora e nunca é impresso nem gravado.
set -euo pipefail

# No Git Bash do Windows, impede que caminhos como /opt/kafka/bin virem caminhos do Windows nos
# argumentos do docker. No Linux, não muda nada.
export MSYS_NO_PATHCONV=1

cd "$(git rev-parse --show-toplevel)"

APP="http://localhost:${APP_PORT:-8080}"
PROMETHEUS="http://localhost:9090"
GRAFANA="http://localhost:3000"
# credencial de demonstração: só para desenvolvimento local (constituição 1.6.0, Princípio VII)
GRAFANA_AUTH="admin:${GRAFANA_ADMIN_PASSWORD:-awards-demo}"
MOVIES_TOPIC="awards.movies"
ERRORS_TOPIC="awards.movies.errors"
SETUP_LIMIT=180

cleanup() {
  docker compose --profile tools down -v --remove-orphans >/dev/null 2>&1 || true
}
trap cleanup EXIT

step() { printf '\n== %s\n' "$*"; }
fail() {
  echo "FALHOU: $*" >&2
  docker compose logs --no-color --tail=150 >&2 || true
  exit 1
}

# wait_for <descrição> <segundos> <comando...>: repete o comando até ele dar certo ou o prazo acabar.
wait_for() {
  local description="$1" seconds="$2"
  shift 2
  local deadline=$((SECONDS + seconds))
  until "$@" >/dev/null 2>&1; do
    ((SECONDS < deadline)) || fail "prazo de ${seconds} s esgotado esperando: $description"
    sleep 1
  done
}

body() { curl -sS --max-time 10 "$@"; }
status() { curl -s -o /dev/null -w '%{http_code}' --max-time 10 "$@"; }
contains() { body "$1" | grep -qF -- "$2"; }

# publish <chave> <json>: envia um evento ao tópico de filmes pelo kafka-cli.
publish() {
  printf '%s:%s\n' "$1" "$2" | docker compose --profile tools run --rm -T kafka-cli \
    /opt/kafka/bin/kafka-console-producer.sh --bootstrap-server kafka:9092 --topic "$MOVIES_TOPIC" \
    --property parse.key=true --property key.separator=: >/dev/null
}

# errors: o tópico de erros desde o começo, com chaves e cabeçalhos.
errors() {
  docker compose --profile tools run --rm -T kafka-cli \
    /opt/kafka/bin/kafka-console-consumer.sh --bootstrap-server kafka:9092 --topic "$ERRORS_TOPIC" \
    --from-beginning --timeout-ms 10000 --property print.key=true --property print.headers=true 2>/dev/null || true
}

total_movies() { body "$APP/api/movies?size=1" | sed -E 's/.*"totalElements":([0-9]+).*/\1/'; }
ready() { [[ "$(status "$APP/actuator/health/readiness")" == "200" ]]; }
target_up() { body "$PROMETHEUS/api/v1/query?query=up%7Bjob%3D%22app%22%7D" | grep -qF '"value":[' && body "$PROMETHEUS/api/v1/query?query=up%7Bjob%3D%22app%22%7D" | grep -qE '"1"\]'; }

step "0. Imagens (fora da medição do SC-008)"
docker compose pull --ignore-buildable --quiet
docker compose --profile tools pull --quiet kafka-cli
docker compose build --quiet app

step "1. docker compose up --wait"
start=$SECONDS
docker compose up -d --wait --wait-timeout 300 || fail "o compose não ficou saudável"

step "2. Resultado de referência"
intervals="$(body "$APP/api/producers/intervals")"
grep -qF '"min":[{"producer":"Joel Silver","interval":1,"previousWin":1990,"followingWin":1991}]' <<<"$intervals" \
  || fail "min inesperado: $intervals"
grep -qF '"max":[{"producer":"Matthew Vaughn","interval":13,"previousWin":2002,"followingWin":2015}]' <<<"$intervals" \
  || fail "max inesperado: $intervals"

step "3. Prometheus coletando a aplicação"
wait_for "alvo app com health up" 60 bash -c "curl -s '$PROMETHEUS/api/v1/targets' | grep -q '\"health\":\"up\"'"
wait_for "série awards_ingestion_events_total" 60 \
  bash -c "curl -s '$PROMETHEUS/api/v1/query?query=awards_ingestion_events_total' | grep -q '\"outcome\":\"recorded\"'"

step "4. Grafana com o dashboard e a datasource provisionados"
[[ "$(status -u "$GRAFANA_AUTH" "$GRAFANA/api/dashboards/uid/awards")" == "200" ]] || fail "dashboard awards ausente"
wait_for "datasource prometheus saudável" 60 \
  bash -c "curl -s -u '$GRAFANA_AUTH' '$GRAFANA/api/datasources/uid/prometheus/health' | grep -q '\"status\":\"OK\"'"
elapsed=$((SECONDS - start))
echo "Passos 1 a 4 em ${elapsed} s (limite do SC-008: ${SETUP_LIMIT} s)"
if [[ -n "${GITHUB_STEP_SUMMARY:-}" ]]; then
  printf '### Compose\n\nPassos 1 a 4 (subida, referência, Prometheus e Grafana): %s s (limite %s s)\n' \
    "$elapsed" "$SETUP_LIMIT" >>"$GITHUB_STEP_SUMMARY"
fi
((elapsed <= SETUP_LIMIT)) || fail "passos 1 a 4 em ${elapsed} s, acima de ${SETUP_LIMIT} s (SC-008)"

step "5. Vencedor novo pelo Kafka, em até 5 s (SC-005)"
publish '1991|Compose Novo' '{"year":1991,"title":"Compose Novo","studios":null,"producers":"Joel Silver","winner":true}'
wait_for "Joel Silver com intervalo 0 em min" 5 \
  bash -c "curl -s '$APP/api/producers/intervals' | grep -qF '\"producer\":\"Joel Silver\",\"interval\":0,\"previousWin\":1991,\"followingWin\":1991'"

step "6. Evento inválido no tópico de erros"
publish 'invalido-compose' '{"year":0,"title":"Invalido","studios":null,"producers":"P","winner":true}'
found=""
for _ in 1 2 3; do
  if errors | grep -q 'awards-error-reason:YEAR_OUT_OF_RANGE'; then
    found=1
    break
  fi
done
[[ -n "$found" ]] || fail "o tópico de erros não recebeu o evento com YEAR_OUT_OF_RANGE"

step "7. Persistência: down sem -v e up de novo"
docker compose down
docker compose up -d --wait --wait-timeout 300 || fail "o compose não voltou saudável"
contains "$APP/api/movies?year=1991&size=100" '"title":"Compose Novo"' || fail "o filme do passo 5 sumiu"
[[ "$(total_movies)" == "207" ]] || fail "totalElements $(total_movies), esperado 207"

step "7b. Queda real do banco (FR-015a)"
docker compose stop postgres
publish '2031|Durante a Queda' '{"year":2031,"title":"Durante a Queda","studios":null,"producers":"Producer Q","winner":false}'
# O banco fica fora até a gravação falhar de verdade e ser tentada de novo. O pool de conexões espera até
# 30 s por uma conexão antes de falhar, então uma queda curta não chega a gerar falha.
wait_for "nova tentativa da gravação no log" 120 \
  bash -c "docker compose logs --no-color app | grep -q 'Falha ao gravar o evento'"
docker compose start postgres
wait_for "filme gravado depois da volta do banco" 90 \
  bash -c "curl -s '$APP/api/movies?year=2031' | grep -qF '\"title\":\"Durante a Queda\"'"
if errors | grep -qF '2031|Durante a Queda'; then
  fail "o evento gravado depois da queda do banco foi para o tópico de erros"
fi
ready || fail "a readiness não está 200 depois da queda do banco"

step "8. Autenticação ligada, com o Prometheus coletando"
set +x
API_TOKEN="$(openssl rand -hex 16)"
export API_TOKEN
docker compose up -d --wait --wait-timeout 300 app prometheus || fail "app e prometheus não voltaram com o token"
[[ "$(status "$APP/api/producers/intervals")" == "401" ]] || fail "intervalos sem token deveriam responder 401"
[[ "$(status -H "Authorization: Bearer $API_TOKEN" "$APP/api/producers/intervals")" == "200" ]] \
  || fail "intervalos com o token deveriam responder 200"
wait_for "alvo app up com o token" 60 target_up
unset API_TOKEN

echo
echo "compose-test.sh: todos os passos passaram."
