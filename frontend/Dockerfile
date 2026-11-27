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

# Execução: a imagem oficial nginx:1.31-alpine-slim, fixada por digest. A nginxinc/nginx-unprivileged não é
# reconstruída desde 19/10/2026, e todas as variantes dela reprovavam a porta do Trivy (PR #16).
FROM nginx:1.31-alpine-slim@sha256:c293b28729906d97fa5302fce3bd79bd27a3ea9a5d9604e21c6ff0ebf14549fc
# Sem root, como a unprivileged faz por dentro: sem a diretiva user, o pid e os temporários em /tmp, e as pastas
# que o entrypoint e o nginx escrevem (conf.d, gerada pelo envsubst, e o cache) com dono nginx. Só esta camada roda
# como root, sem instalar nem atualizar pacotes.
USER root
RUN sed -i -e '/^user /d' -e 's,^pid .*;,pid /tmp/nginx.pid;,' /etc/nginx/nginx.conf \
    && sed -i '/^http {/a \    client_body_temp_path /tmp/client_temp;\n    proxy_temp_path /tmp/proxy_temp;\n    fastcgi_temp_path /tmp/fastcgi_temp;\n    uwsgi_temp_path /tmp/uwsgi_temp;\n    scgi_temp_path /tmp/scgi_temp;' /etc/nginx/nginx.conf \
    && chown -R 101:101 /var/cache/nginx /etc/nginx/conf.d \
    && grep -q '^pid /tmp/nginx.pid;' /etc/nginx/nginx.conf \
    && ! grep -q '^user ' /etc/nginx/nginx.conf
USER 101
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
