# Contrato: política do workflow (`.github/scripts/check-workflows.sh`)

Mudanças na política (FR-012; research R8). As regras 1, 2, 4, 6, 7 e 8 não mudam.

| Regra | Reprova quando |
| --- | --- |
| 3 | um job pede escrita, exceto `security-events: write` no `codeql` e `id-token: write` no `deploy` |
| 5 | `push: true` no `docker/build-push-action` ou `docker push` em qualquer job; `skopeo copy` (ou `skopeo` com destino `docker://`) fora do job `deploy` |
| 9 | existe o job `deploy` e: o `if`, normalizado (sem `${{ }}`, sem espaços nas pontas e com espaços repetidos reduzidos), não é o do contrato `ci-deploy.md`; falta um dos 10 checks em `needs`; a concorrência não é o grupo `deploy-cloud-run` com `cancel-in-progress: false`; ou há `docker/build-push-action`, `docker build` ou `docker buildx build` no job |
| 10 | o `concurrency.cancel-in-progress` do topo é `true` fixo |

## Casos negativos conferidos na etapa 2

Cada caso é aplicado numa cópia temporária do `.github` e precisa reprovar com a mensagem da regra:

1. `id-token: write` no job `build`.
2. `skopeo copy … docker://…` num passo do job `image`.
3. Job `deploy` sem `compose` em `needs`.
4. Job `deploy` com `docker/build-push-action`.
5. Job `deploy` com `if: github.ref == 'refs/heads/main'` (sem o evento e sem o repositório).
6. `secrets.FOO` no job `deploy`.
7. `cancel-in-progress: true` no topo.

O workflow real tem que passar.
