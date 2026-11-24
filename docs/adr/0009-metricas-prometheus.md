# 0009. Métricas no formato do Prometheus, com histograma de latência

- **Status:** Aceito
- **Data:** 2026-11-23
- **Origem:** [`docs/ai/decisoes.md`](../ai/decisoes.md), entradas "Decisões de arquitetura sem entrada
  própria (ADRs)", seção "Métricas no formato do Prometheus", e "Etapa 2 do Marco 2: `groups` na saúde
  geral, banner e formato ECS"; research R3 da spec 002.

## Contexto

O Marco 2 pede métricas de latência e de taxa de erro dos endpoints, no formato do Prometheus. Elas são
a medição de que dependem o SLO, os alertas e o teste de carga do Marco 4.

## Decisão

- Micrometer com o registro do Prometheus, em `/actuator/prometheus`.
- A métrica `http.server.requests`, que o Spring MVC já registra, por rota (o modelo, e não o caminho com
  parâmetros), método, status e `outcome`.
- Histograma ligado só para essa métrica, com faixas de 1 ms a 10 s.
- Rótulo `application` (`spring.application.name`) em todas as séries.

## Alternativas consideradas

- Percentis calculados na aplicação: não se agregam entre instâncias e não servem para o SLO por faixa.
- Contadores próprios por endpoint: repetiriam o que o framework já mede.

## Consequências

- A taxa de erro sai da razão entre as séries com `outcome` `CLIENT_ERROR` ou `SERVER_ERROR` e o total. O
  percentil 95 sai do `histogram_quantile` sobre as faixas.
- Caminhos inexistentes caem num único valor de rota, então o número de séries não cresce com os
  caminhos chamados.
- O endpoint é público neste marco. O briefing prevê exigir token nos endpoints operacionais no ambiente
  publicado.
