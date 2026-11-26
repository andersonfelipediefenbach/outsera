# Data Model: Marco 5 — recursos do GCP e do pipeline

Não há entidade nova na aplicação. O "modelo" deste marco é o conjunto de recursos que o script cria e que o
pipeline usa. Todos os nomes abaixo são públicos e versionados (FR-013).

## Recursos do GCP (projeto `outsera-awards`, região `southamerica-east1`)

| Recurso | Nome | Atributos | Criado por |
| --- | --- | --- | --- |
| APIs | `run`, `artifactregistry`, `iam`, `iamcredentials`, `sts` | ativadas | script |
| Repositório de imagens | `awards` (Docker) | região `southamerica-east1`; política de limpeza de `deploy/gcp/cleanup-policy.json` | script |
| Conta de publicação | `github-deployer@outsera-awards.iam.gserviceaccount.com` | sem chaves gerenciadas pelo usuário | script |
| Conta de execução | `awards-runtime@outsera-awards.iam.gserviceaccount.com` | nenhum papel no projeto | script |
| Pool de identidade | `github` (global) | ativo | script |
| Provedor OIDC | `github-actions` | emissor do GitHub; mapeamento e condição da research R3 | script |
| Serviço | `awards` | conta de execução; `min-instances=0`, `max-instances=1`; criado com a imagem de exemplo | script (criação); pipeline (revisões) |
| Revisão | `awards-<sufixo>` | imagem por digest; tag `c-<sha curto>` enquanto é conferida; probes de startup e liveness | pipeline |
| Imagem | `southamerica-east1-docker.pkg.dev/outsera-awards/awards/awards:<sha do commit>` | identificada pelo digest do manifesto OCI | pipeline |

## Vínculos de IAM

| Membro | Papel | Recurso | Motivo |
| --- | --- | --- | --- |
| `principalSet://iam.googleapis.com/projects/<n>/locations/global/workloadIdentityPools/github/attribute.repository_id/<id>` | `roles/iam.workloadIdentityUser` | conta de publicação | só este repositório troca o token pela conta |
| conta de publicação | `roles/artifactregistry.writer` | repositório `awards` | envia a imagem testada |
| conta de publicação | `roles/run.developer` | serviço `awards` | implanta revisões e muda o tráfego, sem mudar o IAM |
| conta de publicação | `roles/iam.serviceAccountUser` | conta de execução | implanta revisões que rodam com ela |
| `allUsers` | `roles/run.invoker` | serviço `awards` | API pública, autenticação desligada (FR-004) |
| conta de execução | nenhum | — | a aplicação não chama APIs do GCP |

## Estados de uma revisão no pipeline

```text
implantada sem tráfego (tag c-<sha>)
  ├─ conferência na URL da revisão reprova → fica sem tráfego; job vermelho (FR-015a)
  └─ aprova → recebe 100% do tráfego; tags anteriores removidas
                ├─ conferência na URL pública aprova → job verde
                └─ reprova → job vermelho; volta manual pelo runbook
```

## Artefato entre jobs

| Artefato | Produzido por | Conteúdo | Usado por | Regra |
| --- | --- | --- | --- | --- |
| `awards-oci` | job `image`, só em push na `main` | `awards-oci.tar` (layout OCI, manifesto único `linux/amd64`) | job `deploy` | o digest do `index.json` é igual à saída `digest` do job `image` |

## Transição do estado de cada passo do script

`já existia` (estado desejado lido, nada feito) · `criado` · `ajustado` (existia divergente e convergiu) ·
`restaurado` (estava `DELETED`) · `FALHA` (encerra o script). A segunda execução tem que ter só `já existia`.
