# Data Model — Marco 1 (001-producer-intervals-api)

## Entidades persistidas

### Movie (`movie`)

| Campo | Coluna | Tipo | Regras |
| --- | --- | --- | --- |
| id | `id` | BIGINT, identity | PK |
| year | `release_year` | INTEGER, not null | Só dígitos no CSV (R3) |
| title | `title` | VARCHAR(500), not null | Não vazio após `trim` |
| studios | `studios` | VARCHAR(1000), null | Opcional (FR-006e). Texto como no arquivo, após `trim` |
| producersRaw | `producers` | VARCHAR(1000), not null | Texto como no arquivo, após `trim` |
| winner | `winner` | BOOLEAN, not null | `yes` (sem diferenciar maiúsculas) dá true; vazio dá false |
| producers | — | ManyToMany → Producer | Via `movie_producer` |

- Restrição `uk_movie_year_title` UNIQUE (`release_year`, `title`) (FR-006d).

### Producer (`producer`)

| Campo | Coluna | Tipo | Regras |
| --- | --- | --- | --- |
| id | `id` | BIGINT, identity | PK |
| name | `name` | VARCHAR(500), not null | UNIQUE `uk_producer_name`. Texto exato após `trim`, diferenciando maiúsculas (FR-008) |

### MovieProducer (`movie_producer`)

| Coluna | Tipo | Regras |
| --- | --- | --- |
| `movie_id` | BIGINT, FK → movie.id | PK composta |
| `producer_id` | BIGINT, FK → producer.id | PK composta |

Um produtor repetido no mesmo filme gera um único vínculo (FR-010).

## Objetos derivados (não persistidos)

### ProducerInterval

`producer` (String), `interval` (int, igual a `followingWin - previousWin`, maior ou igual a 0),
`previousWin` (int) e `followingWin` (int, maior ou igual a `previousWin`).

### ProducerIntervalsResponse

`min`: List\<ProducerInterval\>; `max`: List\<ProducerInterval\>. As duas listas existem sempre, mesmo
vazias.

### MoviePage

`content`: List\<MovieItem\>, `page`, `size`, `totalElements` e `totalPages`.
`MovieItem` = `year`, `title`, `studios`, `producers` (texto) e `winner` (boolean).

### LoadReport (só na carga)

`linesRead`, `accepted`, `rejected`, mais a lista de `Rejection(lineNumber, reason)`.

## Validação do arquivo

### Erros estruturais (interrompem a inicialização, FR-005)

| Motivo | Condição |
| --- | --- |
| `NOT_FOUND` | O recurso do caminho configurado não existe |
| `UNREADABLE` | Erro de E/S ou arquivo vazio, sem cabeçalho |
| `INVALID_SEPARATOR` | O cabeçalho tem 1 coluna e contém `,` |
| `INVALID_HEADER` | Depois de remover o BOM e aplicar `trim`, as colunas não são exatamente `year`, `title`, `studios`, `producers` e `winner`, nessa ordem e sem diferenciar maiúsculas |

### Validação de linha (rejeita a linha, com aviso; FR-006)

A verificação segue a ordem abaixo, e o primeiro motivo encontrado é o registrado no aviso:

1. `COLUMN_COUNT`: o número de colunas é diferente de 5.
2. `YEAR_MISSING`: o ano está vazio.
3. `YEAR_NOT_NUMERIC`: o ano não é formado só por dígitos (`\d+`).
4. `YEAR_OUT_OF_RANGE`: o valor do ano, só com dígitos, não está entre 1 e 9999 (sessão 2026-11-22 da
   spec, caso 09 do testador). A verificação não pode estourar com sequências longas de dígitos.
5. `TITLE_MISSING`: o título está vazio.
6. `PRODUCERS_MISSING`: a coluna de produtores está vazia ou não gera nenhum nome depois da separação.
7. `WINNER_INVALID`: `winner` não é vazio e é diferente de `yes`, sem diferenciar maiúsculas.
8. `FIELD_TOO_LONG`: um campo excede a coluna do esquema. Os limites são título > 500, estúdios > 1000,
   produtores > 1000 ou um nome de produtor > 500 caracteres (`VARCHAR(500)` e `VARCHAR(1000)` da V1).
   O aviso cita o campo. Acrescentado pela spec 003 em 2026-11-24. A regra fica na `MovieFieldRules`,
   compartilhada com os eventos do Kafka.
9. `NUL_CHARACTER`: o título, os estúdios ou os produtores contêm o caractere nulo (U+0000). O aviso cita o
   campo. Acrescentado pela spec 003 em 2026-11-25 (apontamento do Codex no PR do Marco 3). A regra também
   fica na `MovieFieldRules`.
10. `DUPLICATE`: o ano e o título repetem uma linha já aceita. O aviso cita a linha original.

Uma linha em branco não é lida, não conta como linha lida e não gera aviso (FR-005a).

### Mensagens de log

- Aviso: `WARN Linha {n} rejeitada: {motivo legível}`.
- Resumo: `INFO Carga do arquivo {caminho} concluída: {lidas} linhas lidas, {aceitas} aceitas,
  {rejeitadas} rejeitadas`.

## Ciclo de vida

Os dados são carregados uma vez, na inicialização, e não mudam durante a execução. Não há escrita pela
API no Marco 1.