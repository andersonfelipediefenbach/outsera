# 0013. PostgreSQL no modo Docker, com o mesmo esquema e collation C

- **Status:** Aceito
- **Data:** 2026-11-24
- **Origem:** [`docs/ai/decisoes.md`](../ai/decisoes.md), entrada "Etapa 1 do Marco 3" (descrição do
  commit), e research R1, R3 e R4 da spec 003.

## Contexto

O modo Docker precisa de um banco persistente, para que os filmes recebidos por evento sobrevivam a um
reinício. O modo padrão continua com H2 em memória (Princípio II), e o esquema e as consultas têm de servir
aos dois bancos sem SQL específico (Princípio V e ADR 0002).

## Decisão

- Perfil `postgres`, ligado pelo grupo `docker`, que troca só o banco. A migração `V1__create_schema.sql`
  é a mesma, validada pelo `ddl-auto: validate`.
- Driver `org.postgresql:postgresql` e o módulo `flyway-database-postgresql`, os dois com a versão do Boot.
- `DB_URL` e `DB_USER` com padrões do compose. A `DB_PASSWORD` não tem padrão, porque a credencial de
  demonstração só pode estar no compose (constituição 1.6.0).
- Banco criado com `--locale=C --encoding=UTF8`, no compose e no job `postgres`. A ordem `C` em UTF-8 é a
  dos pontos de código Unicode, a mesma que o H2 usa na listagem.
- Os testes que dependem do PostgreSQL ficam no perfil Maven `postgres-it` (ADR 0019).

## Alternativas consideradas

- `ORDER BY title COLLATE "C"`: o SQL deixaria de ser portável.
- Ordenar em Java: exigiria carregar tudo antes de paginar.
- Uma coluna normalizada só para a ordem: mudaria o esquema sem necessidade.

## Consequências

- O H2 compara unidades UTF-16 (`String.compareTo`), e a collation `C` compara pontos de código. Os dois
  só divergem entre caracteres fora do plano básico (emoji, por exemplo) e caracteres de U+E000 a U+FFFF.
  Esse caso não aparece em títulos de filmes.
- Um PostgreSQL com outra collation daria outra ordem. O README e o compose documentam a exigência.
- A gravação passou a criar ou atualizar o filme por ano e título (ADR 0015), para que a carga direta sobre
  um banco já carregado não viole a restrição única.
