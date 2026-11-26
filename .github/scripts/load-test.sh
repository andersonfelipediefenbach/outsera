#!/usr/bin/env bash
# Teste de carga e medição da carga inicial (spec 004, FR-010 a FR-019, SC-004 a SC-006; research R4 a R7;
# specs/004-acabamento-operacional/contracts/ci-pipeline.md, "Scripts").
#
# Uso: .github/scripts/load-test.sh <imagem> [filmes] [--only-direct]
#
#   1. gera o CSV duas vezes com o gerador do repositório e compara os SHA-256 (e, com os parâmetros
#      padrão, com o hash registrado no Windows, EXPECTED_SHA256_DEFAULT);
#   2. carga direta: a imagem no modo padrão, com o CSV montado; mede do docker run à readiness 200;
#   3. carga por Kafka: o compose com o compose.load.yaml; mede do início do container app à readiness 200;
#   4. o k6 do perfil load do compose (saída 99 = limite ultrapassado);
#   5. resumo no $GITHUB_STEP_SUMMARY (ou na saída) e artefatos em target/load/;
#   6. sempre: derruba o compose e remove o container da carga direta.
#
# --only-direct roda só os passos 1, 2 e 5 (medição de memória, T021). No pipeline (CI=true), ela é
# recusada, e o número de filmes precisa ser o padrão, para que a comparação com o hash registrado sempre
# rode. Precisa de docker (com compose v2), curl, jq, sha256sum e java. Cada passo imprime OK, FALHA ou
# PULADO (com o motivo); no pipeline, PULADO é falha. Códigos de saída: 0 ok, 1 falha, 2 uso ou ferramenta.
set -euo pipefail

IMAGE="${1:?uso: load-test.sh <imagem> [filmes] [--only-direct]}"
DEFAULT_MOVIES=100000
MOVIES="${2:-$DEFAULT_MOVIES}"
ONLY_DIRECT=0
if [[ "${3:-}" == "--only-direct" ]]; then
  ONLY_DIRECT=1
elif [[ -n "${3:-}" ]]; then
  echo "load-test.sh: terceiro argumento desconhecido: $3 (só --only-direct)" >&2
  exit 2
fi
# Hash do CSV com os parâmetros padrão (100000 filmes, semente padrão), gerado no Windows (Git Bash), no
# T015. O Linux do pipeline precisa dar o mesmo (SC-005; research R6).
EXPECTED_SHA256_DEFAULT="c4b741e30f85898ce6a2649554ca2c5b65836a4217a03dae33be243d7880f4a6"
READY_TIMEOUT=600

if ! [[ "$MOVIES" =~ ^[0-9]+$ ]]; then
  echo "load-test.sh: número de filmes inválido: $MOVIES" >&2
  exit 2
fi
if [[ "${CI:-}" == "true" ]]; then
  if [[ "$MOVIES" != "$DEFAULT_MOVIES" ]]; then
    echo "load-test.sh: no pipeline, o número de filmes é sempre o padrão ($DEFAULT_MOVIES)." >&2
    exit 2
  fi
  if [[ "$ONLY_DIRECT" == 1 ]]; then
    echo "load-test.sh: no pipeline, o --only-direct é recusado: o job load roda todos os passos." >&2
    exit 2
  fi
fi
for tool in docker curl jq sha256sum java; do
  if ! command -v "$tool" >/dev/null 2>&1; then
    echo "load-test.sh: '$tool' não foi encontrado no PATH." >&2
    exit 2
  fi
done

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
DATA_DIR="$REPO_ROOT/target/generated-data"
OUT_DIR="$REPO_ROOT/target/load"
GENERATOR="$REPO_ROOT/tools/csv-generator/GenerateMovies.java"
COMPOSE=(docker compose -f "$REPO_ROOT/compose.yaml" -f "$REPO_ROOT/compose.load.yaml")
DIRECT_CONTAINER="awards-load-direct"
mkdir -p "$DATA_DIR" "$OUT_DIR"

# No Git Bash, os caminhos dos bind mounts precisam estar no formato do Windows, e a conversão automática
# do MSYS não pode mexer nos caminhos de dentro do container.
case "$(uname -s)" in
  MINGW*|MSYS*) export MSYS_NO_PATHCONV=1; IS_WINDOWS=1 ;;
  *) IS_WINDOWS=0 ;;
esac
host_path() {
  if [[ "$IS_WINDOWS" == 1 ]]; then cygpath -w "$1"; else printf '%s' "$1"; fi
}

COMPOSE_UP=0
cleanup() {
  docker rm -f "$DIRECT_CONTAINER" >/dev/null 2>&1 || true
  if [[ "$COMPOSE_UP" == 1 ]]; then
    "${COMPOSE[@]}" logs --no-color app > "$OUT_DIR/compose-app.log" 2>&1 || true
    "${COMPOSE[@]}" --profile load down -v >/dev/null 2>&1 || true
  fi
}
trap cleanup EXIT

FAILED=0
SUMMARY=()
record() { # passo, resultado, descrição
  SUMMARY+=("| $1 | $2 | $3 |")
  [[ "$2" == "FALHA" ]] && FAILED=$((FAILED + 1))
  echo "==> passo $1: $2 - $3"
}
skip_step() { # passo, descrição, motivo
  if [[ "${CI:-}" == "true" ]]; then
    echo "    no pipeline, um passo pulado é falha: $3"
    record "$1" FALHA "$2"
  else
    record "$1" "PULADO ($3)" "$2"
  fi
}
now_ms() { date +%s%3N; }

# Espera a readiness 200 em <url>, consultando a cada 0,5 s, até READY_TIMEOUT segundos. Imprime o
# instante (ms) da primeira resposta 200. Com um container dado, para assim que ele sai e mostra o fim do
# log dele no erro padrão: sem isso, uma aplicação que termina na subida só aparecia depois do prazo
# inteiro (job load, commit 127584c).
await_ready() { # url [container]
  local deadline=$(( $(date +%s) + READY_TIMEOUT ))
  while (( $(date +%s) < deadline )); do
    if [[ -n "${2:-}" && "$(docker inspect -f '{{.State.Running}}' "$2" 2>/dev/null)" != "true" ]]; then
      echo "    o container saiu: $(docker inspect -f 'código {{.State.ExitCode}}, OOMKilled={{.State.OOMKilled}}' "$2" 2>/dev/null)" >&2
      docker logs --tail 40 "$2" >&2 2>&1 || true
      return 1
    fi
    if [[ "$(curl -s -o /dev/null -w '%{http_code}' "$1/actuator/health/readiness" || true)" == "200" ]]; then
      now_ms
      return 0
    fi
    sleep 0.5
  done
  return 1
}

# Os intervalos têm min e max não vazios, no formato do enunciado.
check_intervals() { # url
  curl -fsS "$1/api/producers/intervals" | jq -e '
    (keys == ["max", "min"]) and (.min | length > 0) and (.max | length > 0)
    and all(.min[], .max[]; keys == ["followingWin", "interval", "previousWin", "producer"])' >/dev/null
}

HASH_A=""; HASH_B=""; LINES=""; DIRECT_MS=""; KAFKA_MS=""; ACCEPTED=""

# --- passo 1: geração determinística (FR-014, FR-015, FR-019, SC-005)
step1() {
  rm -f "$DATA_DIR/a.csv" "$DATA_DIR/movies.csv"
  # O java do Windows não entende os caminhos do Git Bash (host_path).
  java "$(host_path "$GENERATOR")" --movies "$MOVIES" --output "$(host_path "$DATA_DIR/a.csv")" || return 1
  java "$(host_path "$GENERATOR")" --movies "$MOVIES" --output "$(host_path "$DATA_DIR/movies.csv")" || return 1
  HASH_A="$(sha256sum "$DATA_DIR/a.csv" | cut -d' ' -f1)"
  HASH_B="$(sha256sum "$DATA_DIR/movies.csv" | cut -d' ' -f1)"
  LINES="$(( $(wc -l < "$DATA_DIR/movies.csv") - 1 ))"
  echo "    SHA-256: $HASH_A e $HASH_B ($LINES filmes)"
  [[ "$HASH_A" == "$HASH_B" ]] || { echo "    as duas gerações deram hashes diferentes"; return 1; }
}
echo "--- passo 1: geração do CSV duas vezes, com os mesmos parâmetros"
if step1; then record 1 OK "duas gerações com o mesmo SHA-256"; else record 1 FALHA "duas gerações com o mesmo SHA-256"; fi
if [[ "$MOVIES" == "$DEFAULT_MOVIES" ]]; then
  if [[ -n "$HASH_B" && "$HASH_B" == "$EXPECTED_SHA256_DEFAULT" ]]; then
    record 1b OK "SHA-256 igual ao registrado no Windows"
  else
    echo "    esperado $EXPECTED_SHA256_DEFAULT, obtido ${HASH_B:-nenhum}"
    record 1b FALHA "SHA-256 igual ao registrado no Windows"
  fi
else
  skip_step 1b "SHA-256 igual ao registrado no Windows" "só com os parâmetros padrão ($DEFAULT_MOVIES filmes)"
fi

# --- passo 2: carga direta (FR-018, SC-006)
step2() {
  [[ -f "$DATA_DIR/movies.csv" ]] || { echo "    sem o CSV gerado"; return 1; }
  local start ready url log
  start="$(now_ms)"
  docker run -d --name "$DIRECT_CONTAINER" -p "127.0.0.1::8080" \
    -v "$(host_path "$DATA_DIR/movies.csv"):/data/movies.csv:ro" -e MOVIES_CSV_PATH=/data/movies.csv \
    "$IMAGE" >/dev/null || return 1
  url="http://127.0.0.1:$(docker port "$DIRECT_CONTAINER" 8080/tcp 2>/dev/null | sed -n '1p' | sed 's/.*://')"
  if ! ready="$(await_ready "$url" "$DIRECT_CONTAINER")"; then
    echo "    sem readiness 200"
    docker logs "$DIRECT_CONTAINER" > "$OUT_DIR/direct.log" 2>&1 || true
    return 1
  fi
  DIRECT_MS=$(( ready - start ))
  docker logs "$DIRECT_CONTAINER" > "$OUT_DIR/direct.log" 2>&1 || true
  log="$(grep -oE '[0-9]+ linhas lidas, [0-9]+ aceitas, [0-9]+ rejeitadas' "$OUT_DIR/direct.log" | tail -1)"
  echo "    readiness em ${DIRECT_MS} ms; resumo: ${log:-ausente}"
  ACCEPTED="$(sed -E 's/.*lidas, ([0-9]+) aceitas.*/\1/' <<<"$log")"
  [[ "$log" == "$LINES linhas lidas, $LINES aceitas, 0 rejeitadas" ]] || { echo "    esperado $LINES lidas e aceitas e 0 rejeitadas"; return 1; }
  check_intervals "$url" || { echo "    intervalos vazios ou fora do formato"; return 1; }
  docker rm -f "$DIRECT_CONTAINER" >/dev/null
}
echo "--- passo 2: carga direta do CSV gerado"
if step2; then record 2 OK "carga direta sem linha rejeitada"; else record 2 FALHA "carga direta sem linha rejeitada"; fi

if [[ "$ONLY_DIRECT" == 0 ]]; then
  # --- passo 3: carga por Kafka (FR-018, SC-006). A imagem dada é a do serviço app (awards:local).
  step3() {
    [[ -f "$DATA_DIR/movies.csv" ]] || { echo "    sem o CSV gerado"; return 1; }
    local started ready start_ms log
    if [[ "$IMAGE" != "awards:local" ]]; then
      docker tag "$IMAGE" awards:local || return 1
    fi
    COMPOSE_UP=1
    "${COMPOSE[@]}" up -d --no-build || return 1
    if ! ready="$(await_ready "http://127.0.0.1:${APP_PORT:-8080}" "$("${COMPOSE[@]}" ps -q app)")"; then
      echo "    sem readiness 200"
      return 1
    fi
    started="$(docker inspect -f '{{.State.StartedAt}}' "$("${COMPOSE[@]}" ps -q app)")"
    start_ms="$(date -d "$started" +%s%3N)"
    KAFKA_MS=$(( ready - start_ms ))
    "${COMPOSE[@]}" logs --no-color app > "$OUT_DIR/compose-app.log" 2>&1 || true
    log="$(grep -oE '[0-9]+ linhas lidas, [0-9]+ publicadas, [0-9]+ rejeitadas' "$OUT_DIR/compose-app.log" | tail -1)"
    echo "    readiness em ${KAFKA_MS} ms depois do início do container; resumo: ${log:-ausente}"
    [[ "$log" == "$LINES linhas lidas, ${ACCEPTED:-$LINES} publicadas, 0 rejeitadas" ]] \
      || { echo "    esperado $LINES lidas, ${ACCEPTED:-$LINES} publicadas e 0 rejeitadas"; return 1; }
    check_intervals "http://127.0.0.1:${APP_PORT:-8080}" || { echo "    intervalos vazios ou fora do formato"; return 1; }
  }
  echo "--- passo 3: carga por Kafka no compose"
  if step3; then record 3 OK "carga por Kafka sem linha rejeitada"; else record 3 FALHA "carga por Kafka sem linha rejeitada"; fi

  # --- passo 4: k6 com os limites (FR-010 a FR-013, SC-004)
  echo "--- passo 4: k6"
  # O k6 escreve o relatório em target/load/ com o usuário de quem roda (compose.yaml, serviço k6).
  K6_USER="$(id -u):$(id -g)"
  export K6_USER
  # O k6 depende do app healthy. Uma carga longa passa do start-period do HEALTHCHECK, o Docker marca o
  # app como unhealthy no meio dela, e ele só volta a healthy na verificação seguinte à readiness (até
  # 10 s depois). Sem esta espera, o compose recusa subir o k6.
  app_id="$("${COMPOSE[@]}" ps -q app 2>/dev/null || true)"
  for _ in $(seq 1 60); do
    [[ -n "$app_id" && "$(docker inspect -f '{{.State.Health.Status}}' "$app_id" 2>/dev/null)" == "healthy" ]] && break
    sleep 1
  done
  set +e
  "${COMPOSE[@]}" --profile load run --rm k6
  K6_EXIT=$?
  set -e
  if [[ "$K6_EXIT" == 0 ]]; then
    record 4 OK "k6 dentro dos limites"
  else
    echo "    o k6 saiu com $K6_EXIT (99 = limite ultrapassado)"
    record 4 FALHA "k6 dentro dos limites"
  fi
else
  skip_step 3 "carga por Kafka sem linha rejeitada" "--only-direct"
  skip_step 4 "k6 dentro dos limites" "--only-direct"
fi

# --- passo 5: resumo
{
  echo "### Teste de carga"
  echo
  echo "| Passo | Resultado | Descrição |"
  echo "| --- | --- | --- |"
  printf '%s\n' "${SUMMARY[@]}"
  echo
  echo "- Filmes: ${LINES:-?}; SHA-256 das duas gerações: \`${HASH_A:-?}\` e \`${HASH_B:-?}\`"
  echo "- Carga direta até a readiness: ${DIRECT_MS:-?} ms"
  echo "- Carga por Kafka até a readiness: ${KAFKA_MS:-?} ms"
  echo "- Runner: ${RUNNER_NAME:-local} (${RUNNER_OS:-$(uname -s)})"
  if [[ -f "$OUT_DIR/summary.md" ]]; then
    echo
    cat "$OUT_DIR/summary.md"
  fi
} > "$OUT_DIR/resumo.md"
if [[ -n "${GITHUB_STEP_SUMMARY:-}" ]]; then
  cat "$OUT_DIR/resumo.md" >> "$GITHUB_STEP_SUMMARY"
else
  cat "$OUT_DIR/resumo.md"
fi

if (( FAILED > 0 )); then
  echo "load-test.sh: FALHA em $FAILED passo(s)."
  exit 1
fi
echo "load-test.sh: todos os passos OK."
