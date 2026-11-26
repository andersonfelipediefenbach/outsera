# Contrato: pipeline de publicação (`.github/workflows/ci.yml`)

Mudanças no workflow de CI (FR-002, FR-008 a FR-016; research R1, R2, R6, R7, R8 e R10).

## Topo do workflow

- `permissions: contents: read` (sem mudança).
- `concurrency.cancel-in-progress: ${{ github.ref != 'refs/heads/main' }}`. Na `main`, nada é cancelado.

## Job `image` (alterado)

- Build com o Buildx e duas saídas: `type=docker,name=awards:ci` e `type=oci,dest=<tmp>/awards-oci.tar`, com
  `provenance: false` e `sbom: false`. Os passos de hoje (contexto, teste de fumaça e Trivy) continuam sobre a
  `awards:ci`.
- Passo novo: lê o digest do manifesto no `index.json` do tar e confere que ele é o manifesto único
  `linux/amd64`. Confere também que o `Id` da `awards:ci` é o digest da configuração ou o do manifesto. Expõe
  o digest como `outputs.digest`.
- Em push na `main`: envia o tar como artefato `awards-oci`, com `retention-days: 1`.

## Job `deploy` (novo)

| Campo | Valor |
| --- | --- |
| `name` | `Publicação no Cloud Run e conferência da URL` |
| `if` | `github.event_name == 'push' && github.ref == 'refs/heads/main' && github.repository == 'andersonfelipediefenbach/outsera'` |
| `needs` | `workflow-policy`, `build`, `postgres`, `codeql`, `dependencies`, `dockerfile-lint`, `image`, `alert-rules`, `load` e `compose` |
| `permissions` | `contents: read`, `id-token: write` |
| `concurrency` | `group: deploy-cloud-run`, `cancel-in-progress: false` |
| `environment` | `name: cloud-run`, `url:` a URL pública do serviço |
| `timeout-minutes` | 15 |
| `env` | identificadores públicos da research R10 |

Passos, em ordem:

1. `actions/checkout` (para os scripts).
2. `actions/download-artifact` do `awards-oci`.
3. Conferência: o digest do `index.json` é igual a `needs.image.outputs.digest`.
4. `google-github-actions/auth` com `workload_identity_provider` e `service_account` (sem `credentials_json`).
5. `google-github-actions/setup-gcloud` com a versão fixada.
6. Envio: `skopeo copy --preserve-digests oci-archive:awards-oci.tar docker://$GCP_REGISTRY/awards:${{ github.sha }}`,
   autenticado por um token de acesso do `gcloud` passado por `stdin`. Depois, a conferência de que o digest no
   registro é o mesmo.
7. `gcloud run deploy` com `--no-traffic --tag=c-<sha curto>`, os parâmetros da research R5 e
   `--set-env-vars=MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=health`.
8. `verify-deployment.sh --expect-no-metrics` na URL da tag.
9. `gcloud run services update-traffic` com 100% para a revisão e a remoção das tags anteriores.
10. `verify-deployment.sh --expect-no-metrics` na URL pública.
11. Resumo: URL, digest, revisão, resultado das conferências e duração (`if: always()`).

## Garantias

- Nenhum `secrets.`, nenhuma chave e nenhum rebuild no job `deploy`.
- Uma falha em qualquer passo anterior ao 9 deixa o tráfego na revisão anterior.
- O job `deploy` não é um check obrigatório da `main`: ele só roda depois do merge.
