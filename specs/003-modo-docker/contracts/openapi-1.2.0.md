# Contrato: mudanças do OpenAPI 1.1.0 → 1.2.0

O arquivo é `src/main/resources/static/openapi.yaml`. Os schemas dos corpos **não mudam** (Princípio
III). As mudanças são:
- o esquema de autenticação, opcional;
- a resposta 401 nos dois endpoints de negócio;
- a descrição.

## `info`

```yaml
info:
  version: 1.2.0
  description: >
    … (texto atual) …
    A autenticação é opcional e fica desligada por padrão. Com a variável de ambiente API_TOKEN
    definida, os dois endpoints exigem o cabeçalho Authorization: Bearer <token>; sem ele, ou com
    outro valor, a resposta é 401 em Problem Details.
```

## `components`

```yaml
components:
  securitySchemes:
    bearerAuth:
      type: http
      scheme: bearer
      description: Token definido em API_TOKEN. Sem a variável, a autenticação fica desligada.
  headers:
    WWW-Authenticate:
      description: Esquema exigido, presente só no 401.
      schema:
        type: string
        example: Bearer realm="awards"
  responses:
    Unauthorized:
      description: Autenticação ligada e token ausente ou inválido
      headers:
        WWW-Authenticate:
          $ref: '#/components/headers/WWW-Authenticate'
      content:
        application/problem+json:
          schema:
            $ref: '#/components/schemas/ProblemDetail'
          example:
            type: about:blank
            title: Unauthorized
            status: 401
            detail: Token ausente ou inválido
            instance: /api/producers/intervals
```

## Nos dois endpoints de negócio

```yaml
      security:
        - {}              # autenticação desligada (padrão)
        - bearerAuth: []  # autenticação ligada por API_TOKEN
      responses:
        '401':
          $ref: '#/components/responses/Unauthorized'
```

`{}` declara que a requisição sem credencial também é válida. É assim que os testes dos Marcos 1 e 2
continuam validando as requisições sem token contra o contrato (research R13).

## O que não muda

- Os schemas `ProducerIntervals`, `ProducerInterval`, `MoviePage`, `Movie` e `ProblemDetail`.
- `additionalProperties: false` nos schemas de resposta.
- O `ETag`, o `If-None-Match` e o 304 (contrato 1.1.0).
- O endpoint de métricas e as probes continuam fora do contrato de negócio
  ([operational-endpoints.md](operational-endpoints.md)).

## Comprovação

- Um IT novo, o `ApiDocsSecurityIT`, confere no contrato publicado a versão 1.2.0, o
  `securitySchemes.bearerAuth`, a alternativa `{}` e a resposta 401 nos dois endpoints. O `ApiDocsIT`
  continua conferindo que o contrato publicado é o da spec (ver o conflito abaixo).
- O `AuthContractIT` valida o 401 e a resposta 200 com token contra o contrato.
- Os ITs de contrato dos Marcos 1 e 2 (`IntervalsContractIT` e `MoviesContractIT`) continuam sem
  alteração (research R14).

## Conflito com o `ApiDocsIT` (decidido: opção A, 2026-11-24)

O responsável aceitou a opção A. O registro está em `docs/ai/decisoes.md`, na entrada "Exceção do
`ApiDocsIT` à regra dos testes sem alteração".

O `ApiDocsIT` da tag `v0.2.0` compara o `/openapi.yaml` publicado, byte a byte, com
`specs/002-operacao/contracts/openapi.yaml` (constante `SPEC_CONTRACT`). Publicar o contrato 1.2.0
faz esse teste falhar, a menos que uma destas três coisas mude:

| Opção | O que muda | Efeito |
| --- | --- | --- |
| **A (recomendada)** | Só a constante `SPEC_CONTRACT` do `ApiDocsIT` passa a apontar para `specs/003-modo-docker/contracts/openapi.yaml`, como o Marco 2 fez ao trocar a spec 001 pela 002 | Nenhuma asserção muda. O `check-unchanged-tests.sh` ganha uma lista de exceções explícita, com este único arquivo e a referência à entrada em `docs/ai/decisoes.md` |
| B | O arquivo `specs/002-operacao/contracts/openapi.yaml` é sobrescrito com o 1.2.0 | O teste não muda, mas o artefato de uma spec já entregue passa a descrever o marco seguinte |
| C | O contrato publicado continua 1.1.0 | A FR-026 deixa de ser cumprida |

Com a opção A, o arquivo completo fica em `specs/003-modo-docker/contracts/openapi.yaml`, criado na
etapa 1 a partir do 1.1.0 com as mudanças acima, e copiado para `src/main/resources/static/`.
