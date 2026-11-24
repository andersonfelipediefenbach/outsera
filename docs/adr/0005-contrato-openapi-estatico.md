# 0005. Contrato OpenAPI estático como fonte única, com o Swagger UI pelo webjar

- **Status:** Aceito
- **Data:** 2026-11-22
- **Origem:** [`docs/ai/decisoes.md`](../ai/decisoes.md), entradas "Swagger UI pelo webjar, sem springdoc
  (bolt 6)" e "Decisões do Marco 1 que divergem do briefing (T082)"; research R8 da spec 001.

## Contexto

O briefing pede que o contrato OpenAPI seja a fonte do Swagger UI e da validação das respostas nos
testes. O contrato de intervalos não pode ter nenhum campo a mais nem a menos (Princípio III). No
springdoc 3.2.0, o Swagger UI exige a geração automática do contrato pelo código.

## Decisão

- O contrato é um arquivo estático, escrito antes do código, em `specs/<feature>/contracts/openapi.yaml`,
  copiado byte a byte para `src/main/resources/static/openapi.yaml` e publicado em `/openapi.yaml`.
- O Swagger UI é servido pelo webjar `org.webjars:swagger-ui`, numa página estática que aponta para
  `/openapi.yaml`, sem o springdoc.
- Os testes validam as respostas reais, inclusive os cabeçalhos, contra esse arquivo, e o `ApiDocsIT`
  confere que o publicado é idêntico ao da spec.

## Alternativas consideradas

- Contrato gerado por anotações em `/v3/api-docs`: seria um segundo contrato, que poderia divergir do
  desenhado.
- springdoc com a geração ligada e nenhum caminho gerado: `/v3/api-docs` continuaria publicado, vazio.

## Consequências

- Documentação, validação e código não divergem: qualquer campo a mais na resposta falha nos testes.
- Mudar o contrato exige mudar o arquivo da spec antes do código. No Marco 2, o contrato 1.1.0 passou a
  morar em `specs/002-operacao/`, e o da spec 001 ficou como registro do Marco 1.
