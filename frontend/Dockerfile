# Imagem do front (spec 006, FR-025, FR-026 e FR-026a; contracts/web-image.md): build do Angular com Node e
# execução com nginx sem root, que serve os arquivos e repassa /api ao serviço da API.

# Build: node:26-alpine (Node 26 LTS), fixado por digest.
FROM node:26-alpine@sha256:6bd9ab43c0ec0190ca7b2d193906cb8b7edf6e7f4afa43ddeccd6452710bffb5 AS build
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci --no-audit --no-fund
COPY angular.json tsconfig.json tsconfig.app.json ./
COPY public ./public
COPY src ./src
RUN npm run build

# Execução: nginxinc/nginx-unprivileged:1.29-alpine, fixado por digest. Roda como o usuário nginx (UID 101).
FROM nginxinc/nginx-unprivileged:1.29-alpine@sha256:0c79d56aee561a1d81c63f00eee5fb5fe29279560cdc55e91425133104c7fbe6
# PORT: a do Cloud Run. LOG_FORMAT=json: log de acesso em JSON no container (constituição, Observabilidade).
# O resolver do repasse vem do /etc/resolv.conf (15-local-resolvers.envsh da imagem base). Só estas variáveis
# passam pelo envsubst; as do nginx ($uri e outras) ficam intactas. Sem as mensagens do entrypoint no stdout,
# o stdout tem só o log de acesso.
ENV PORT=8080 \
    LOG_FORMAT=json \
    NGINX_ENTRYPOINT_LOCAL_RESOLVERS=1 \
    NGINX_ENTRYPOINT_QUIET_LOGS=1 \
    NGINX_ENVSUBST_FILTER="^(PORT|API_URL|LOG_FORMAT|NGINX_LOCAL_RESOLVERS)$"
COPY nginx/security-headers.conf /etc/nginx/snippets/security-headers.conf
COPY nginx/default.conf.template /etc/nginx/templates/default.conf.template
COPY --chmod=0755 nginx/05-awards-env.sh /docker-entrypoint.d/05-awards-env.sh
COPY --from=build /app/dist/frontend/browser /usr/share/nginx/html
EXPOSE 8080
