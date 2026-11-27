# Runbook dos alertas

Uma seção por alerta do Prometheus do compose (`observability/prometheus/rules/slo.yml`), mais a
[publicação no Cloud Run](#publicação-no-cloud-run) (Marco 5). Cada alerta
traz o link da sua seção em `annotations.runbook_url`. Os objetivos e o orçamento estão em
[`slo.md`](slo.md). Não há Alertmanager: os alertas aparecem em <http://localhost:9090/alerts> e no
dashboard do Grafana.

Comandos úteis, na raiz do repositório:

```bash
docker compose ps                                   # estado e saúde dos serviços
docker compose logs --tail=200 app                  # log da aplicação (JSON ECS)
curl -s http://localhost:8080/actuator/health/readiness
curl -s 'http://localhost:9090/api/v1/alerts'
```

## AwardsIntervalsAvailabilityBudgetBurn

**O que significa:** as respostas 5xx de `GET /api/producers/intervals` estão consumindo o orçamento de
erro da disponibilidade (0,5% em 28 dias) mais rápido que o permitido.

- `severity: page`: 14,4× na última hora e nos últimos 5 minutos. Nesse ritmo, o orçamento do mês acaba em
  cerca de 2 dias.
- `severity: ticket`: 6× nas últimas 6 horas e nos últimos 30 minutos. O orçamento acaba em cerca de 5 dias.

**O que conferir:**

1. No Grafana, o painel "Taxa de erro": o 5xx vem dos intervalos ou de todos os endpoints?
2. `docker compose logs --tail=200 app`: exceções (`"log.level":"ERROR"`) e o `exception` das métricas.
3. A readiness (`/actuator/health/readiness`): com 503, a aplicação ainda está carregando ou perdeu o
   indicador da ingestão.
4. O PostgreSQL: `docker compose ps postgres` e `docker compose logs --tail=100 postgres`. Uma queda do banco
   aparece como 5xx nas leituras.
5. O Kafka e o painel "Atraso do consumidor". Um consumidor parado não causa 5xx, mas indica o
   mesmo problema de infraestrutura.

**O que fazer:**

- Banco fora: `docker compose up -d postgres`. A aplicação volta a responder sem reinício. A gravação de
  eventos espera e tenta de novo, sem perder nenhum (spec 003, FR-015a).
- Erro da aplicação: guardar o log, reiniciar com `docker compose restart app` e abrir um bug com o trecho do
  log e o horário. A carga volta pelo Kafka, e a readiness volta a 200 depois do marcador.
- O alerta `ticket` sem `page` costuma ser um problema intermitente: acompanhar no dashboard e registrar.

## AwardsIntervalsLatencyBudgetBurn

**O que significa:** a fração de requisições aos intervalos acima de 111,848 ms (o bucket
`le="0.111848106"` do histograma) está consumindo o orçamento da latência (1% em 28 dias) mais rápido que o
permitido. As janelas e as gravidades são as mesmas do alerta de disponibilidade.

**O que conferir:**

1. No Grafana, os painéis "Latência p95 e p99" e "Requisições por segundo": subiu o tráfego?
2. Se há ingestão em andamento: painel "Eventos gravados e rejeitados". Durante uma gravação, os
   intervalos são calculados no banco a cada leitura, sem o resultado guardado (ADR 0026). Uma ingestão
   longa e contínua deixa as leituras mais lentas.
3. CPU e memória do container: `docker stats awards-app-1`.
4. O PostgreSQL: conexões e tempo das consultas (`docker compose logs --tail=100 postgres`).

**O que fazer:**

- Ingestão em massa em andamento: esperar o fim dela, e o resultado volta a ser guardado. Se for recorrente,
  registrar para revisar o cache (ADR 0026).
- Container sem CPU ou memória: liberar recursos ou aumentar o limite da máquina.
- Sem causa aparente: guardar o log e as métricas e abrir um bug. O job `load` do pipeline mede a mesma
  latência a cada push e reprova quando o p99 passa do objetivo.

## AwardsAppDown

**O que significa:** o Prometheus não consegue coletar `/actuator/prometheus` da aplicação há mais de 1
minuto (`up{job="app"} == 0`). Para o SLO, esse tempo conta como indisponibilidade.

**O que conferir:**

1. `docker compose ps app`: o container está rodando, reiniciando ou parado?
2. `docker compose logs --tail=200 app`: erro na subida (arquivo CSV ausente ou inválido, banco ou broker
   inalcançáveis) ou falta de memória (`OutOfMemoryError`, ou `OOMKilled` em
   `docker inspect awards-app-1`).
3. A autenticação: com `API_TOKEN` definido, o Prometheus envia o mesmo token. Um token diferente dá 401 na
   coleta, e o alvo aparece como fora em <http://localhost:9090/targets>.

**O que fazer:**

- Container parado: `docker compose up -d app`. A aplicação publica o arquivo de novo, e a readiness volta
  a 200 depois do marcador da carga.
- Erro na subida: corrigir a causa no log (por exemplo, o caminho em `MOVIES_CSV_PATH`) e subir de novo.
- Token divergente: subir o compose com o mesmo `API_TOKEN` para a aplicação e o Prometheus
  (`API_TOKEN=<token> docker compose up -d`).

## Publicação no Cloud Run

Job `deploy` do pipeline, só em push na `main` (spec 005; [ADR 0027](adr/0027-publicacao-no-cloud-run.md)).
O serviço é o `awards`, no projeto `outsera-awards`, região `southamerica-east1`, com a URL pública
<https://awards-2564509384.southamerica-east1.run.app>. Os comandos abaixo precisam do login de quem tem
acesso ao projeto (`gcloud auth login`). O pipeline não tem chave: ele usa a identidade federada.

```bash
gcloud run revisions list --service=awards --region=southamerica-east1 --project=outsera-awards
gcloud run services describe awards --region=southamerica-east1 --project=outsera-awards \
  --format='yaml(status.traffic)'
.github/scripts/verify-deployment.sh --expect-no-metrics https://awards-2564509384.southamerica-east1.run.app
```

**Como ler a execução:** o resumo do job `deploy` traz a URL, o digest da imagem, a revisão, a URL da revisão
e a tabela de cada conferência. Cada tabela tem o status e o tempo de cada chamada.

### Falha antes da troca de tráfego

Se o job parou até o passo "Conferência pela URL da revisão, antes do tráfego", **a URL pública não mudou**.
Ela continua servindo a revisão anterior.

- **Envio da imagem ou implantação:**
  - erro de permissão (`PERMISSION_DENIED`): rodar `deploy/gcp/setup.sh` de novo, que reaplica os vínculos
    que faltarem. Se faltar um papel de leitura do projeto que o script não concede, acrescentar só esse
    papel ao script, com o motivo (spec 005, research R4);
  - revisão que não sobe (o startup probe não chega a 200): ver o log com
    `gcloud run services logs read awards --region=southamerica-east1 --project=outsera-awards --limit=200`.
- **Conferência pela URL da revisão:** a revisão existe, mas não recebe tráfego. O resumo mostra o passo que
  falhou e o corpo recebido. Corrigir num PR novo. Não é preciso voltar nada.

### Falha depois da troca de tráfego

Se o passo "Conferência pela URL pública" falhou, a revisão nova já recebe o tráfego. Para voltar à anterior:

1. Achar a revisão anterior, a segunda da lista, por ordem de criação:

   ```bash
   gcloud run revisions list --service=awards --region=southamerica-east1 --project=outsera-awards \
     --sort-by=~metadata.creationTimestamp --format='value(metadata.name)' --limit=2
   ```

2. Passar o tráfego para ela:

   ```bash
   gcloud run services update-traffic awards --region=southamerica-east1 --project=outsera-awards \
     --to-revisions=<revisão anterior>=100
   ```

3. Conferir com o `verify-deployment.sh` na URL pública e registrar o incidente. A correção vem num PR novo. A
   próxima publicação da `main` volta a promover só depois das duas conferências.

### Publicar de novo

- Reexecutar o job `deploy` na execução da `main` (re-run no GitHub Actions). A operação é segura de repetir:
  a imagem é a do mesmo commit e o fluxo é o mesmo.
- O artefato da imagem (`awards-oci`) fica guardado por 1 dia. Depois disso, a reexecução falha no download, e
  é preciso reexecutar a execução inteira da `main`, que reconstrói e testa a imagem.

## Publicação do front no Cloud Run

Job `deploy-web` do pipeline, só em push na `main` e depois do `deploy` da API (spec 006;
[ADR 0028](adr/0028-front-end-angular-com-nginx.md)). O serviço é o `awards-web`, no mesmo projeto e região, com a
URL pública <https://awards-web-2564509384.southamerica-east1.run.app>. O nginx do front repassa `/api` ao
`awards` pela variável `API_URL` da revisão. Os comandos precisam do login de quem tem acesso ao projeto.

```bash
gcloud run revisions list --service=awards-web --region=southamerica-east1 --project=outsera-awards
gcloud run services describe awards-web --region=southamerica-east1 --project=outsera-awards \
  --format='yaml(status.traffic,spec.template.spec.containers[0].env)'
.github/scripts/verify-web.sh https://awards-web-2564509384.southamerica-east1.run.app
```

**Falha antes da troca de tráfego:** a URL pública do front não mudou. As causas e os passos são os da API
([acima](#falha-antes-da-troca-de-tráfego)), com `awards-web` no lugar de `awards`. Uma revisão que não sobe
costuma ser `API_URL` ausente ou `LOG_FORMAT` inválido: o log de erro mostra "API_URL não definida" ou
"LOG_FORMAT deve ser json ou main".

**Falha depois da troca de tráfego:** voltar o tráfego do `awards-web` para a revisão anterior.

```bash
gcloud run revisions list --service=awards-web --region=southamerica-east1 --project=outsera-awards \
  --sort-by=~metadata.creationTimestamp --format='value(metadata.name)' --limit=2
gcloud run services update-traffic awards-web --region=southamerica-east1 --project=outsera-awards \
  --to-revisions=<revisão anterior>=100
```

Depois, conferir com o `verify-web.sh` na URL pública e registrar o incidente. A correção vem num PR novo.

### O front abre, mas `/api` responde 502 ou 504

A página carrega, e os painéis mostram "Não foi possível carregar os dados.":

1. Conferir a API direto, com
   `.github/scripts/verify-deployment.sh --expect-no-metrics https://awards-2564509384.southamerica-east1.run.app`.
   Se ela falhar, o problema é a API ([publicação no Cloud Run](#publicação-no-cloud-run)).
2. Se a API responde, conferir o `API_URL` da revisão do front (comando `describe` acima). Ele precisa ser a
   origem pública do `awards`, com `https://` e sem barra final.
3. Um 504 logo depois de um período ocioso é a partida a frio do `awards`, que passou dos 60 s do repasse.
   Recarregar a página; se repetir, ver o log do `awards`.
4. O log de acesso do front, em JSON, mostra o `status` e o `upstream_duration` de cada `/api`:
   `gcloud run services logs read awards-web --region=southamerica-east1 --project=outsera-awards --limit=50`.
