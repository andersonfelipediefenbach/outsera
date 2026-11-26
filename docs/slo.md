# SLO do endpoint de intervalos

Objetivos de nível de serviço de `GET /api/producers/intervals`, o endpoint que o enunciado avalia (spec
004, FR-003). As regras de alerta estão em
[`observability/prometheus/rules/slo.yml`](../observability/prometheus/rules/slo.yml), e o que fazer
quando uma delas dispara, no [runbook](runbook.md).

## Objetivos

| Indicador | Objetivo | Janela | Orçamento de erro |
| --- | --- | --- | --- |
| Disponibilidade | 99,5% das requisições respondidas sem 5xx | 28 dias, móvel | 0,5% |
| Latência | 99% das requisições em até **0,111848106 s** (cerca de 112 ms) | 28 dias, móvel | 1% |

- **Requisição ruim para a disponibilidade:** resposta 5xx, e todo o período em que o Prometheus não coleta
  a aplicação (`up{job="app"} == 0`, alerta `AwardsAppDown`).
- **Requisição boa para a disponibilidade:** 2xx, 304 e 4xx. Um 401 com o token ligado e um 304 de um ETag
  revalidado são respostas certas, e não falhas do serviço (FR-005).
- **Latência:** boa é a requisição com duração até o bucket `le="0.111848106"` do histograma
  `http_server_requests_seconds`. O total é o de todas as requisições aos intervalos, com qualquer status.
  O limite é um bucket que já existe no Micrometer (de 1 ms a 10 s), porque o Prometheus só calcula com
  exatidão a fração abaixo de um limite de bucket (research R1).
- **Medição:** pelas métricas que a aplicação já expõe em `/actuator/prometheus`, sem nenhuma configuração
  nova.

## Taxa de consumo do orçamento

A taxa de consumo é a fração de requisições ruins dividida pelo orçamento. Com consumo de 1×, o orçamento
acaba exatamente no fim dos 28 dias. Os alertas usam duas janelas por gravidade (SRE Workbook), para
disparar rápido numa queda forte e não disparar por um pico curto:

| Gravidade | Consumo | Janelas (as duas acima do limite) | Orçamento acaba em |
| --- | --- | --- | --- |
| `page` | 14,4× | 1 hora e 5 minutos | cerca de 2 dias |
| `ticket` | 6× | 6 horas e 30 minutos | cerca de 5 dias |

Na disponibilidade, 14,4× é 7,2% de 5xx, e 6× é 3%. Na latência, 14,4× é 14,4% acima do bucket, e 6× é 6%.
Sem tráfego, a razão é 0/0 (NaN) e nenhum alerta de consumo dispara.

## Alertas

| Alerta | Gravidade | SLO | Runbook |
| --- | --- | --- | --- |
| `AwardsIntervalsAvailabilityBudgetBurn` | `page` ou `ticket` | `intervals-availability` | [runbook](runbook.md#awardsintervalsavailabilitybudgetburn) |
| `AwardsIntervalsLatencyBudgetBurn` | `page` ou `ticket` | `intervals-latency` | [runbook](runbook.md#awardsintervalslatencybudgetburn) |
| `AwardsAppDown` | `page` | `intervals-availability` | [runbook](runbook.md#awardsappdown) |

O job `alert-rules` do pipeline confere as regras e roda os testes delas (`slo.test.yml`): cada alerta
dispara e deixa de disparar nos cenários certos. O job `compose` confere que as regras carregaram no
Prometheus do compose e que nenhum alerta do SLO dispara com a aplicação pronta.

## Origem dos números e limites do pipeline

- **Medição:** 3 execuções do job `load` no runner compartilhado, com 100 mil filmes gerados. O maior p99
  dos intervalos foi 2,02 ms, e o da listagem, 13,15 ms, sem nenhum erro. O detalhe está em
  [`docs/ai/decisoes.md`](ai/decisoes.md), na entrada "Medição do teste de carga e folga dos limites do
  pipeline".
- **Limites do k6 no job `load`,** amarrados ao objetivo: p99 dos intervalos abaixo de 111,848 ms, o mesmo
  bucket do alerta; p99 da listagem abaixo de 100 ms; erro abaixo de 1%.
- **Por que o limite do pipeline não é 2× a medição:** com medianas abaixo de 1 ms, isso só mediria o ruído
  do runner. O job existe para pegar regressões reais, como a perda do cache dos intervalos, que levou o
  p99 a 927 ms com 100 mil filmes ([ADR 0026](adr/0026-intervalos-guardados-entre-leituras.md)).
- **Objetivo e limite são coisas diferentes:** o objetivo vale para a operação em 28 dias. O limite do
  pipeline barra um commit que poria o objetivo em risco.
