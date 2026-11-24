#!/usr/bin/env bash
# Confere o efeito do .dockerignore no contexto de build (spec 002, FR-026; research R14).
#
# Constrói, com o contexto da raiz e portanto com o mesmo .dockerignore, uma imagem descartável que só
# copia o contexto, e falha se aparecer algo fora da lista de permissão: mvnw, .mvn/, pom.xml e
# src/main/. A base da sonda é a imagem do estágio de build do Dockerfile, lida do próprio Dockerfile,
# para que o digest que o Dependabot atualiza valha também aqui (research R13).
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
PROBE=awards-context-probe:local

case "$(uname -s)" in
  MINGW*|MSYS*) export MSYS_NO_PATHCONV=1 ;;
esac

if ! command -v docker >/dev/null 2>&1; then
  echo "check-docker-context.sh: 'docker' não foi encontrado no PATH." >&2
  exit 2
fi
if [[ ! -f "$REPO_ROOT/.dockerignore" ]]; then
  echo "check-docker-context.sh: não há .dockerignore na raiz do repositório." >&2
  exit 1
fi

BASE="$(awk 'toupper($1) == "FROM" && tolower($3) == "as" && $4 == "build" { print $2; exit }' "$REPO_ROOT/Dockerfile")"
if [[ -z "$BASE" ]]; then
  echo "check-docker-context.sh: o Dockerfile não tem um estágio 'FROM <imagem> AS build'." >&2
  exit 1
fi

cleanup() { docker rmi -f "$PROBE" >/dev/null 2>&1 || true; }
trap cleanup EXIT

printf 'FROM %s\nCOPY . /ctx\n' "$BASE" | docker build -q -f - -t "$PROBE" "$REPO_ROOT" >/dev/null

mapfile -t ENTRIES < <(docker run --rm --entrypoint find "$PROBE" /ctx -mindepth 1 | sed 's|^/ctx/||' | sort)

ALLOWED='^(mvnw|pom\.xml|\.mvn(/.*)?|src|src/main(/.*)?)$'
OFFENDERS=()
for entry in "${ENTRIES[@]}"; do
  [[ "$entry" =~ $ALLOWED ]] || OFFENDERS+=("$entry")
done

echo "contexto de build: ${#ENTRIES[@]} entradas (base da sonda: $BASE)"
if (( ${#OFFENDERS[@]} > 0 )); then
  echo "check-docker-context.sh: entradas fora da lista de permissão do .dockerignore:" >&2
  printf '  %s\n' "${OFFENDERS[@]:0:20}" >&2
  exit 1
fi
LISTING="$(printf '%s\n' "${ENTRIES[@]}")"
for required in mvnw pom.xml .mvn src/main; do
  if ! grep -qx -- "$required" <<<"$LISTING"; then
    echo "check-docker-context.sh: '$required' falta no contexto de build." >&2
    exit 1
  fi
done
echo "contexto de build OK: só mvnw, .mvn/, pom.xml e src/main/"
