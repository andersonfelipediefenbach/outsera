# 0002. H2 com JPA e esquema por Flyway, com SQL portável

- **Status:** Aceito
- **Data:** 2026-11-18
- **Origem:** [`docs/ai/decisoes.md`](../ai/decisoes.md), entrada "Decisões de arquitetura sem entrada
  própria (ADRs)", seção "H2 com JPA e esquema por Flyway"; research R5 da spec 001.

## Contexto

O enunciado pede banco em memória no modo padrão. O Marco 3 acrescenta o PostgreSQL no perfil Docker, e a
constituição exige um único esquema versionado, sem SQL específico de um banco e sem `ddl-auto` gerando
tabelas.

## Decisão

- H2 em memória no modo padrão, com nome único por contexto.
- JPA com `ddl-auto=validate`: o JPA só confere o esquema.
- Esquema versionado pelo Flyway (`V1__create_schema.sql`), com as tabelas `movie`, `producer` e
  `movie_producer`, e a unicidade de ano + título na tabela `movie`.
- Consultas em JPQL, sem SQL nativo. A coluna do ano se chama `release_year`, porque `year` é palavra
  reservada no H2.

## Alternativas consideradas

- Tabela única com os produtores em texto: exigiria separar os nomes a cada consulta.
- `ddl-auto=create`: a constituição proíbe gerar o esquema pelo JPA.
- SQL com funções de janela para os intervalos: é portável, mas é mais difícil de ler e de manter que o
  cálculo no serviço.

## Consequências

- O mesmo esquema e as mesmas consultas servem ao PostgreSQL do Marco 3.
- A duplicata ano + título é barrada também no banco, como defesa adicional à verificação do carregador.
- O Flyway avisa que a versão do H2 é mais nova que a testada. O aviso fica visível, sem supressão.
