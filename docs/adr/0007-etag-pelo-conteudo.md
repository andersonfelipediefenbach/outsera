# 0007. ETag derivado do conteúdo

- **Status:** Aceito
- **Data:** 2026-11-23
- **Origem:** [`docs/ai/decisoes.md`](../ai/decisoes.md), entradas "Decisões de arquitetura sem entrada
  própria (ADRs)", seção "ETag derivado do conteúdo", e "Etapa 1 do Marco 2: filtro de ETag próprio e
  contexto do `EmptyLoadIT`"; research R4 da spec 002.

## Contexto

O Marco 2 pede ETag nos dois endpoints de negócio, com 304 quando o `If-None-Match` bate, sem mudar o
corpo das respostas 200 (Princípio III). Os dados não mudam durante uma execução, e os corpos são
pequenos.

## Decisão

- Um filtro, `ContentEtagFilter`, registrado só em `/api/producers/intervals` e `/api/movies`, guarda o
  corpo e põe um ETag forte, `"0"` mais o MD5 do corpo, nas respostas 2xx de GET e HEAD.
- Com `If-None-Match` igual ao ETag atual (sozinho, numa lista ou na forma fraca) ou `*`, a resposta é
  304, sem corpo e com o mesmo ETag. As respostas de erro ficam sem ETag.
- A comparação usa as classes `ETag` do Spring.

## Alternativas consideradas

- `ShallowEtagHeaderFilter` do Spring: era a escolha inicial, mas ele não trata o `If-None-Match: *`, que
  a RFC 9110 manda responder com 304. O método que decide o 304 é privado, e não dá para estendê-lo.
- ETag a partir de uma "versão" dos dados: exigiria guardar estado, e cada combinação de parâmetros da
  listagem ainda precisaria do próprio valor.
- ETag fraco, ou `Cache-Control` com `max-age`: não foram pedidos e mudariam o comportamento dos
  clientes.

## Consequências

- O mesmo arquivo gera o mesmo ETag, inclusive entre execuções. Um arquivo com outro resultado gera outro
  ETag.
- O corpo é gerado mesmo quando a resposta é 304. Com os tamanhos atuais, o custo é irrelevante.
- O contrato OpenAPI 1.1.0 documenta o cabeçalho, o parâmetro e a resposta 304, e as métricas registram o
  304 como `REDIRECTION`, fora da taxa de erro.
