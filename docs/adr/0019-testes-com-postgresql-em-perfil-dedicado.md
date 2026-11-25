# 0019. Testes que dependem do PostgreSQL num perfil Maven dedicado, sem Testcontainers

- **Status:** Aceito
- **Data:** 2026-11-24
- **Origem:** [`docs/ai/decisoes.md`](../ai/decisoes.md), entrada "Emenda 1.7.0 da constituição:
  comprovação de comportamento que depende de infraestrutura"; research R16 da spec 003.

## Contexto

O Princípio II exige que o `./mvnw verify` rode sem Docker. Parte do comportamento do Marco 3 só existe
no PostgreSQL: a migração, a ordem com a collation, a persistência entre reinícios e a nova tentativa
quando a gravação falha. Antes da emenda 1.7.0, o Princípio VI pedia o teste de cada comportamento "no
`./mvnw verify`".

## Decisão

- Os ITs que dependem do PostgreSQL terminam em `PostgresIT`, ficam em `postgres/` e são excluídos das
  duas execuções padrão do Failsafe.
- O perfil Maven `postgres-it` roda só esses ITs, numa execução própria do Failsafe, sem o mínimo de
  cobertura (que é o do `verify` padrão) e sem paralelismo.
- O pipeline roda o perfil no job `postgres`, com o PostgreSQL como service container do GitHub Actions,
  em `trust` e sem credencial no workflow. O job é obrigatório na proteção da `main`.
- Cada classe usa um esquema próprio, criado pelo Flyway e apagado no fim.
- O comportamento comum aos dois bancos continua testado no `verify` padrão, com H2. O Kafka tem broker
  embutido e por isso não entra na exceção (constituição 1.7.0).

## Alternativas consideradas

- Testcontainers: exigiria Docker no `verify`, ou um segundo mecanismo de perfil, e o responsável o
  descartou.
- Testar só com H2: a ordem com a collation e a persistência entre reinícios ficariam sem comprovação.

## Consequências

- `./mvnw verify` continua sem Docker. `./mvnw -Ppostgres-it verify` precisa de um PostgreSQL com
  collation `C`, em `DB_URL`, `DB_USER` e `DB_PASSWORD`.
- Um job vermelho reprova o merge só se estiver na lista de checks obrigatórios da `main` (T093 da spec
  003).
