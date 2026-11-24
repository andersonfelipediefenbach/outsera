# 0008. Liveness e readiness pelo Actuator, com a readiness depois da carga

- **Status:** Aceito
- **Data:** 2026-11-23
- **Origem:** [`docs/ai/decisoes.md`](../ai/decisoes.md), entradas "Decisões de arquitetura sem entrada
  própria (ADRs)", seção "Liveness e readiness pelo Actuator", e "Etapa 2 do Marco 2: `groups` na saúde
  geral, banner e formato ECS"; research R1 e R2 da spec 002.

## Contexto

A constituição exige, a partir do Marco 2, probes de liveness e readiness, com a readiness positiva só
depois da carga do CSV. O healthcheck da imagem e o teste de fumaça do pipeline dependem deles, e a
autenticação só chega no Marco 3.

## Decisão

- Spring Boot Actuator, com os probes ligados explicitamente em `/actuator/health/liveness` e
  `/actuator/health/readiness`, na mesma porta da API.
- Só `health` e `prometheus` expostos pela web, sem a página de descoberta e sem detalhes
  (`show-details` e `show-components` em `never`). A saúde geral traz só o status e a lista `groups`
  (`liveness` e `readiness`), que o Boot sempre inclui.
- A readiness fica positiva no `ApplicationReadyEvent`. Como a carga roda antes de o servidor subir
  (ADR 0003), a ordem "carga → pronta" vem do próprio ciclo de vida.

## Alternativas consideradas

- Controllers próprios de saúde: reimplementariam o que o framework já entrega.
- Um `HealthIndicator` com uma flag de "carga concluída": código e estado a mais para a mesma garantia.
- Porta separada para o Actuator: mais uma variável e mais uma porta no container, sem ganho neste
  marco.

## Consequências

- O avaliador e o orquestrador consultam o estado sem autenticação e sem ver detalhes internos.
- O 503 de um probe negativo é provado mudando o estado pela API pública de disponibilidade do Spring.
- No Marco 3, com a carga por Kafka, a readiness vai precisar de um estado publicado pelo consumidor.
