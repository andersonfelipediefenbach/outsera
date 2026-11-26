# Runbook dos alertas

Uma seção por alerta do Prometheus do compose (`observability/prometheus/rules/slo.yml`). Cada alerta
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
