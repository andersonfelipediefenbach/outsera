# Contrato: imagem do front (`frontend/Dockerfile`)

Comprovado pelo `web-smoke-test.sh` e pelo Playwright no job `web`, e pelo `verify-web.sh` no `deploy-web`
(FR-025, FR-026, FR-026a e FR-029). Detalhes em [research R8](../research.md).

## Construção

- Multi-stage: `node:26-alpine@sha256:…` (`npm ci` e `ng build`) → `nginxinc/nginx-unprivileged:1.29-alpine@sha256:…`.
- Usuário de execução não root (UID 101 da imagem base); nenhum `USER root` na etapa final.
- Contexto: `frontend/`, com `.dockerignore` em lista de permissão (`src/`, `public/`, `package*.json`,
  `angular.json`, `tsconfig*.json`, `nginx/`). `node_modules`, `e2e/` e `dist/` ficam fora.
- Sem segredos e sem variáveis de build com URL: o bundle é o mesmo em qualquer ambiente (FR-018).

## Variáveis de ambiente

| Variável | Padrão | Uso |
| --- | --- | --- |
| `PORT` | `8080` | porta do `listen` (o Cloud Run define) |
| `API_URL` | nenhum: obrigatória | origem da API sem barra final (por exemplo, `https://awards-….run.app` ou `http://127.0.0.1:8080`) |
| `LOG_FORMAT` | `json` (definido no `Dockerfile`) | formato do log de acesso: `json` ou `main` (texto padrão do nginx) |

Sem `API_URL`, o contêiner termina na subida com a mensagem "API_URL não definida" (script em
`/docker-entrypoint.d/`). Um `LOG_FORMAT` diferente de `json` e `main` também termina a subida, com a mensagem
"LOG_FORMAT deve ser json ou main". Só essas três variáveis passam pelo `envsubst` (`NGINX_ENVSUBST_FILTER`),
então `$uri` e as outras variáveis do nginx não são tocadas.

## Log (constituição, Observabilidade; FR-026a)

- O template declara `log_format json escape=json` e usa `access_log /dev/stdout ${LOG_FORMAT};`. O formato
  `main` é o padrão da imagem base.
- Cada requisição gera **uma linha** de JSON válido com os campos: `time` (`$time_iso8601`), `method`
  (`$request_method`), `path` (`$uri`), `query` (`$args`), `status` (`$status`, número), `bytes`
  (`$body_bytes_sent`, número), `duration` (`$request_time`, número, em segundos), `upstream_duration`
  (`$upstream_response_time`, texto, vazio sem repasse), `user_agent` (`$http_user_agent`) e `remote_addr`.
  Os cabeçalhos `Authorization` e `Cookie` nunca entram no log.
- O `/healthz` não é logado (`access_log off`), para o probe não encher o log.
- O log de erro continua em texto no stderr: o nginx não permite formato no `error_log`. O limite fica no ADR
  0028.

## Rotas servidas

| Caminho | Resposta |
| --- | --- |
| `/healthz` | 200 `ok`, `text/plain`, sem acessar a API |
| `/api/...` | repassado a `${API_URL}/api/...` com o mesmo método, a query, `If-None-Match` e `Authorization` (se houver); a resposta volta com status, corpo, `ETag` e `Content-Type` da API |
| arquivo existente em `dist/` | o arquivo |
| qualquer outro caminho | `index.html`, status 200 (rotas internas do Angular) |

- Repasse: `proxy_ssl_server_name on`, `Host` do destino, `resolver` do ambiente (lido de `/etc/resolv.conf` na
  subida), `proxy_read_timeout 60s`.
- A API fora do ar: 502 ou 504 no `/api/...`, que o front mostra como erro do painel.

## Cabeçalhos

| Onde | Cabeçalho |
| --- | --- |
| todas as respostas | `X-Content-Type-Options: nosniff`, `Referrer-Policy: no-referrer`, `X-Frame-Options: DENY`, sem versão no `Server` |
| HTML | `Content-Security-Policy: default-src 'self'; img-src 'self' data:; style-src 'self'; script-src 'self'; connect-src 'self'; frame-ancestors 'none'; base-uri 'self'; form-action 'self'`; `Cache-Control: no-cache` |
| arquivos com hash no nome | `Cache-Control: public, max-age=31536000, immutable` |

Se o build de produção do Angular precisar de estilo em linha (CSS crítico), o `inlineCritical` é desligado no
`angular.json` em vez de afrouxar a CSP.

## Teste de fumaça (`.github/scripts/web-smoke-test.sh <imagem> <api_url>`)

1. Sobe a imagem com `--network host`, `PORT=8081` e o `API_URL` dado; espera `/healthz` em até 30 s.
2. `/` → 200, HTML com `<app-root` e `lang="pt-BR"`, CSP presente.
3. `/filmes?ano=1990` → 200, mesmo `index.html`.
4. `/api/producers/intervals` → 200 e corpo idêntico à `REFERENCE` do `smoke-test.sh`, com `ETag`.
5. Mesmo pedido com `If-None-Match` → 304.
6. `/api/aggregations/years-with-multiple-winners` → 200 com 1986, 1990 e 2015.
7. Usuário do processo do contêiner diferente de 0.
8. Sem `API_URL` → o contêiner termina com código diferente de 0 e a mensagem esperada.
9. Log de acesso: as linhas de `docker logs` geradas pelos passos 2 a 6 (stdout) são, cada uma, JSON válido
   (`node -e 'JSON.parse(...)'` sobre cada linha, já que o runner tem Node e não tem garantia de `jq` com o mesmo
   comportamento) com os dez campos da seção "Log", `status` e `bytes` numéricos, uma linha com `path` igual a
   `/api/producers/intervals` e `status` 200, nenhuma linha com `/healthz` e nenhuma ocorrência de
   `Authorization`.
10. Com `LOG_FORMAT=main`, a mesma requisição a `/` gera uma linha que **não** é JSON (texto padrão), e com
    `LOG_FORMAT=xml` o contêiner termina com a mensagem esperada.
