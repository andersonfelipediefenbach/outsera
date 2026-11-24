# 0015. Gravação idempotente por ano e título, com atualização

- **Status:** Aceito
- **Data:** 2026-11-24
- **Origem:** [`docs/ai/decisoes.md`](../ai/decisoes.md) e Clarifications da spec 003 (sessão
  2026-11-24, primeira pergunta); research R5.

## Contexto

Com o banco persistente, um reinício republica o arquivo, e o broker pode entregar o mesmo evento mais de
uma vez. A gravação não pode duplicar filmes. Além disso, um evento com o ano e o título de um filme já
gravado precisa ter um efeito definido.

## Decisão

- `MovieImportService.record`: busca o filme por ano e título. Se não existe, cria; se existe, troca os
  estúdios, o texto dos produtores, o `winner` e todos os vínculos pelos do evento. A última gravação
  prevalece (decisão do responsável).
- A carga direta usa o mesmo método, numa transação para o arquivo inteiro. O consumidor usa uma
  transação por evento.
- Duplicatas dentro do mesmo arquivo continuam ignoradas pelo leitor, com aviso (spec 001, FR-006d).

## Alternativas consideradas

- Ignorar o evento repetido, com a primeira gravação prevalecendo: reinícios seriam neutros, mas um
  indicado não poderia virar vencedor por evento.
- Só promover a vencedor: uma regra a mais, sem pedido.
- `MERGE` ou `INSERT … ON CONFLICT`: o SQL deixaria de ser portável.

## Consequências

- Um reinício reaplica os valores do arquivo aos filmes que estão nele. Uma mudança recebida por evento num
  filme do arquivo é desfeita, e os filmes que não estão no arquivo não mudam (consequência aceita;
  `RestartPostgresIT`).
- Um produtor que sai de todos os filmes continua gravado, sem vínculo. Ele não aparece em nenhuma
  resposta, porque os intervalos só usam vencedores.
