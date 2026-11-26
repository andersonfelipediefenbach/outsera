# Quickstart: validar o Marco 5

Roteiro de validação de ponta a ponta. Os detalhes ficam nos contratos:
[`setup-script.md`](contracts/setup-script.md), [`ci-deploy.md`](contracts/ci-deploy.md),
[`verify-deployment.md`](contracts/verify-deployment.md) e [`workflow-policy.md`](contracts/workflow-policy.md).

## 0. Pré-requisitos (Anderson)

- O projeto `outsera-awards` existe, com faturamento ligado e alerta de orçamento configurado.
- O Google Cloud CLI está instalado. No prompt do Claude Code, o login e o projeto:

  ```text
  ! gcloud auth login
  ! gcloud config set project outsera-awards
  ```

## 1. Configuração do GCP (etapa 1)

```bash
deploy/gcp/setup.sh          # primeira execução: passos "criado"
deploy/gcp/setup.sh          # segunda execução: só "já existia", zero mudanças, código 0
```

Esperado: os identificadores públicos impressos no fim e a URL do serviço respondendo com a página de exemplo
do Cloud Run.

Conferências manuais (SC-003):

```bash
gcloud iam service-accounts keys list --iam-account=github-deployer@outsera-awards.iam.gserviceaccount.com --managed-by=user
# lista vazia
gcloud iam workload-identity-pools providers describe github-actions --location=global --workload-identity-pool=github --format='value(attributeCondition)'
# condição com repository_id, repository_owner_id e refs/heads/main
```

## 2. Conferência local do script de verificação (etapa 3)

```bash
docker build -t awards:ci .
docker run -d --rm -p 18080:8080 --name awards-local awards:ci
.github/scripts/verify-deployment.sh http://127.0.0.1:18080        # código 0
docker rm -f awards-local
docker run -d --rm -p 18080:8080 --name awards-local \
  -v "$PWD/src/test/resources/scenarios/disjoint-producers.csv:/data/m.csv:ro" -e MOVIES_CSV_PATH=file:/data/m.csv awards:ci
.github/scripts/verify-deployment.sh http://127.0.0.1:18080 60     # código 1, passo 2 em FALHA
docker rm -f awards-local
```

## 3. Política do workflow (etapa 2)

O job `workflow-policy` do PR passa com o workflow real. Os sete casos negativos de
[`workflow-policy.md`](contracts/workflow-policy.md) reprovam com a mensagem da regra.

## 4. Modo padrão intacto (toda etapa)

```bash
./mvnw verify
./mvnw -Pextras verify
```

Os dois verdes, sem nenhum IT novo nem alterado.

## 5. Publicação pelo pipeline (depois do merge)

1. O Anderson faz o merge do PR na `main`.
2. Na execução da `main`, os 10 checks ficam verdes, e o job `deploy` roda em seguida.
3. O resumo do job mostra o digest, a revisão, a conferência pela URL da revisão e a conferência pela URL
   pública, as duas `OK`, e a duração abaixo de 10 min (SC-005).
4. De qualquer máquina, sem token:

   ```bash
   curl -s https://awards-<número>.southamerica-east1.run.app/api/producers/intervals
   # Joel Silver em min (1990–1991) e Matthew Vaughn em max (2002–2015)
   ```

5. `gcloud run services describe awards --region=southamerica-east1 --format='value(spec.template.spec.containers[0].image)'`
   mostra a imagem pelo digest do resumo.

## 6. Restrição à `main`

- No PR, o job `deploy` aparece como pulado.
- A condição do provedor, conferida no passo 1 (T010), só aceita `refs/heads/main` do repositório e do dono
  pelos IDs. Não há tentativa real com outra branch: a política do workflow impede qualquer outro job de pedir
  o token.
- Na URL pública, `/actuator/prometheus` responde 404 (FR-004a).

## 7. Recomendado (Anderson, nas configurações do repositório)

Ambiente `cloud-run` com a regra de branch "só `main`", como camada a mais (research R3).
