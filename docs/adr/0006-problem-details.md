# 0006. Erros em Problem Details (RFC 9457)

- **Status:** Aceito
- **Data:** 2026-11-18
- **Origem:** [`docs/ai/decisoes.md`](../ai/decisoes.md), entrada "Decisões de arquitetura sem entrada
  própria (ADRs)", seção "Erros em Problem Details"; research R7 da spec 001.

## Contexto

A listagem de filmes recebe parâmetros que podem ser inválidos, e os endpoints de negócio só aceitam GET.
A constituição exige que os erros da API sigam a RFC 9457.

## Decisão

- `spring.mvc.problemdetails.enabled: true`, para os erros que o próprio Spring MVC produz, como o 405
  com o cabeçalho `Allow`.
- Um `@RestControllerAdvice` converte a `InvalidParameterException` em 400 com
  `application/problem+json` e a propriedade `invalidParams` (`[{name, reason}]`), com todos os
  parâmetros inválidos da consulta.
- Os parâmetros da listagem são validados à mão, porque o conversor padrão aceita `yes`, `on` e `1` como
  booleanos.

## Alternativas consideradas

- Um corpo de erro próprio: fora do padrão, e cada cliente precisaria aprender o formato.
- O corpo de erro padrão do Boot (`/error`): não segue a RFC.
- `@RequestParam Boolean` com Bean Validation: aceitaria valores lenientes.

## Consequências

- Os clientes recebem erros num formato padronizado, descrito no contrato OpenAPI.
- As respostas de erro não ganham ETag (ADR 0007).
- A autenticação do Marco 3 responde 401 no mesmo formato.
