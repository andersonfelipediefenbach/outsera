#!/bin/sh
# Confere as variáveis do front antes do envsubst (contracts/web-image.md, "Variáveis de ambiente"). As mensagens
# vão para o stderr, para o stdout ficar só com o log de acesso.
set -eu

if [ -z "${API_URL:-}" ]; then
  echo "API_URL não definida: informe a origem da API, por exemplo https://awards-....run.app" >&2
  exit 1
fi

case "${LOG_FORMAT:-}" in
  json|main) ;;
  *)
    echo "LOG_FORMAT deve ser json ou main (encontrado: '${LOG_FORMAT:-}')" >&2
    exit 1
    ;;
esac
