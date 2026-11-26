# Front-end (Marco 6)

Painel e lista de filmes do Golden Raspberry Awards, em Angular 22.3, publicados em
<https://awards-web-2564509384.southamerica-east1.run.app>. A visão geral, os requisitos e a publicação estão no
[README do repositório](../README.md#marco-6-front-end-em-angular-opcional), e a spec, em
[`specs/006-frontend-angular/`](../specs/006-frontend-angular/spec.md).

## Pré-requisitos

- Node 26 LTS (`.nvmrc`). O `./mvnw` da API não depende deste diretório.
- Para rodar contra dados reais, a API no ar: `./mvnw spring-boot:run` na raiz do repositório.

## Comandos

```bash
npm ci                                   # dependências, pelo package-lock.json
npm start                                # http://localhost:4200, com /api repassado a http://localhost:8080
npm run lint                             # angular-eslint, inclusive o ponta a ponta
npm test -- --watch=false --coverage     # testes unitários (Vitest); reprova abaixo de 80% das linhas
npm run build                            # build de produção em dist/frontend/browser
npm run e2e                              # Playwright contra E2E_BASE_URL (padrão http://127.0.0.1:8081)
```

O `npm run e2e` precisa da API e da imagem do front no ar, como o job `web` do pipeline faz:

```bash
# na raiz do repositório
./mvnw -DskipTests package && java -jar target/awards-*.jar &
docker build -t awards-web:local frontend
docker run --rm -d -p 127.0.0.1:8081:8080 --add-host host.docker.internal:host-gateway \
  -e API_URL=http://host.docker.internal:8080 awards-web:local
cd frontend && npx playwright install chromium && npm run e2e
```

## Estrutura

- `src/app/api/`: chamadas à API, por caminho relativo, e a tradução do filtro da URL para a API.
- `src/app/dashboard/`: o painel e os quatro painéis.
- `src/app/movies/`: a lista de filmes e o filtro na URL.
- `src/app/shared/`: estado dos painéis (carregando, vazio, erro), paginação e validação do ano.
- `e2e/`: cenários do Playwright (desktop e celular), sem respostas simuladas.
- `nginx/` e `Dockerfile`: a imagem de execução, com o repasse de `/api`, os cabeçalhos e o log de acesso em JSON.
