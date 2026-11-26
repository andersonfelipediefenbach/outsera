# Contrato: `deploy/gcp/setup.sh`

Script de configuração do GCP (FR-017 a FR-021; research R3, R4 e R9).

## Uso

```bash
deploy/gcp/setup.sh            # aplica a configuração (idempotente)
```

- Sem argumentos. O projeto (`outsera-awards`), a região (`southamerica-east1`) e o repositório do GitHub
  (`andersonfelipediefenbach/outsera`) são constantes no topo do script.
- Pré-requisitos: Google Cloud CLI no PATH, login feito pelo Anderson (`gcloud auth login`) com permissão de
  dono no projeto, `curl` e acesso à API pública do GitHub.
- Roda no Linux e no Git Bash do Windows.

## Saída

Uma linha por passo:

```text
--- passo N: <descrição>
==> passo N: OK (já existia | criado | ajustado | restaurado) - <descrição>
==> passo N: FALHA - <descrição>: <motivo>
```

No fim, o resumo com a contagem por resultado, o número de mudanças e os identificadores públicos:

```text
GCP_PROJECT_ID=outsera-awards
GCP_PROJECT_NUMBER=<número>
GCP_REGION=southamerica-east1
GCP_WIF_PROVIDER=projects/<número>/locations/global/workloadIdentityPools/github/providers/github-actions
GCP_DEPLOY_SA=github-deployer@outsera-awards.iam.gserviceaccount.com
GCP_RUNTIME_SA=awards-runtime@outsera-awards.iam.gserviceaccount.com
GCP_REGISTRY=southamerica-east1-docker.pkg.dev/outsera-awards/awards
GCP_SERVICE=awards
URL=https://awards-<número>.southamerica-east1.run.app
```

## Passos

| N | Passo | Tipo |
| --- | --- | --- |
| 0 | `gcloud` e `curl` disponíveis; conta ativa; projeto existe e está `ACTIVE`; faturamento ligado; IDs do repositório e do dono lidos do GitHub | verificação (sem escrita) |
| 1 | APIs ativadas | escrita |
| 2 | Repositório `awards` e política de limpeza (apaga versões com mais de 30 dias e mantém as 10 mais recentes) | escrita |
| 3 | Conta de execução | escrita |
| 4 | Conta de publicação, sem chaves gerenciadas pelo usuário | escrita e verificação |
| 5 | Pool `github` | escrita |
| 6 | Provedor `github-actions` com mapeamento e condição | escrita |
| 7 | Serviço `awards` (imagem de exemplo, só se não existir; conta de execução; 0 a 1 instância) | escrita |
| 8 | Vínculos de IAM da tabela do data-model | escrita |
| 9 | Impressão dos identificadores | leitura |

## Códigos de saída

`0` todos os passos OK · `1` algum passo em FALHA · `2` ferramenta ausente.

## Garantias

- Um `FALHA` no passo 0 encerra antes de qualquer escrita.
- A segunda execução seguida termina com `0` e com zero mudanças.
- Nada secreto é criado, baixado ou impresso.
- Cada vínculo de IAM tem, no script, um comentário com o motivo (FR-018).
