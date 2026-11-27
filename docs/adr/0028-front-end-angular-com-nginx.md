# 0028. Front-end em Angular, servido por nginx com repasse de /api, num segundo serviço do Cloud Run

- **Status:** Aceito
- **Data:** 2026-11-26
- **Origem:** [`docs/ai/decisoes.md`](../ai/decisoes.md), entradas do Marco 6;
  [spec 006](../../specs/006-frontend-angular/spec.md) e a research R1 a R11 do
  [plano](../../specs/006-frontend-angular/plan.md).

## Contexto

O Marco 6 acrescenta um front com duas telas: o painel, com quatro resumos, e a lista de filmes. Ele está fora do
escopo do enunciado e não pode afetar o que o avaliador roda, ou seja, o `./mvnw verify` só com o JDK
(Princípio II). O briefing previa hospedagem estática (Vercel ou Firebase) e CORS por variável de ambiente. O
Marco 5 já publica a API no Cloud Run, com identidade federada e sem segredos.

## Decisão

- **Angular 22.3 em `frontend/`, com build próprio.**
  - O `./mvnw` não lê a pasta e não precisa de Node (emenda 2.1.0, Princípio II).
  - O app usa componentes standalone, signals e detecção de mudanças sem zone.js, e CSS próprio, sem biblioteca
    de componentes.
- **Uma única origem para o navegador.** O front chama a API por caminho relativo (`/api/...`). Na publicação, o
  nginx repassa `/api` ao serviço `awards`, pela variável `API_URL`. No desenvolvimento, o `ng serve` faz o mesmo
  com o `proxy.conf.json`. A API não ganha CORS.
- **Imagem multi-stage:** o Node 26 constrói o Angular, e a imagem oficial `nginx:1.31-alpine-slim` executa como o
  usuário `nginx` (UID 101), ajustada numa camada sem instalar pacotes, com as duas imagens fixadas por digest. A
  `nginxinc/nginx-unprivileged`, a escolha inicial, não é reconstruída desde 19/10/2026 e reprovou a porta do Trivy
  no PR #16. O nginx:
  - devolve o `index.html` nas rotas internas;
  - aplica a CSP restrita a `'self'` e os cabeçalhos de segurança;
  - tem `/healthz` para o probe;
  - grava o log de acesso **em JSON**, ligado pela variável `LOG_FORMAT=json` que a imagem define (constituição,
    Observabilidade). Com `LOG_FORMAT=main`, o log volta ao texto padrão.
- **Segundo serviço do Cloud Run, `awards-web`.** O fluxo é o do [ADR 0027](0027-publicacao-no-cloud-run.md):
  - 0 a 1 instância, acesso anônimo, a mesma identidade federada e nenhum segredo;
  - uma conta de execução própria, sem nenhum papel;
  - a imagem testada vai pelo digest, e a revisão fica sem tráfego até o `verify-web.sh` aprovar.

  O job `deploy-web` só roda depois do `deploy` da API, para o front novo nunca chamar um endpoint que a API
  publicada ainda não tem.
- **Testes do front** (emenda 2.1.0, Princípio VI): testes unitários com Vitest (mínimo de 80% das linhas) e
  ponta a ponta com o Playwright contra a API real e a imagem do front, sem respostas simuladas, no pipeline.
- **Agregações na API, e não no navegador.** Os anos com mais de um vencedor e as vitórias por estúdio são dois
  endpoints novos, com IT, sem mudar os existentes.

## Alternativas consideradas

- **Hospedagem estática (Vercel ou Firebase) com CORS:** seriam mais um provedor, com conta própria, e duas
  origens, com preflight e a lista de origens permitidas por variável. O Cloud Run já está configurado e
  conferido.
- **Servir o Angular pelo Spring Boot, dentro do JAR:** poria o Node no `./mvnw` e misturaria os builds
  (Princípio II).
- **Calcular as agregações no navegador a partir da listagem:** exigiria baixar todas as páginas e duplicaria a
  regra de negócio fora da API.
- **Reformatar o log de erro do nginx (njs ou sidecar):** é complexidade demais para um servidor de arquivos
  estáticos (Princípio V).

## Consequências

- **O log de erro do nginx fica em texto.** O `error_log` não aceita formato. Só o log de acesso sai em JSON, e
  o de erro vai para o stderr, que o Cloud Run coleta à parte.
- **Partida a frio em dobro.** Depois de um período ocioso, a primeira abertura espera o `awards-web` e, no
  primeiro `/api`, o `awards`. O repasse tolera 60 s, os painéis mostram "Carregando…", e o README avisa.
- **Com a autenticação ligada na API, o front não funciona.** Ele não envia token. Os painéis mostram o erro, e
  a publicação mantém a autenticação desligada (Marco 5).
- **Os checks obrigatórios passaram de 10 para 12** (`frontend` e `web`). A política do workflow exige os 12 nos
  `needs` dos dois jobs de publicação, e o `deploy` nos do `deploy-web`.
- **O front fica fora do `compose.yaml`** (spec 006, FR-031). Localmente, ele roda com `npm start` contra a API do
  `./mvnw spring-boot:run`.
