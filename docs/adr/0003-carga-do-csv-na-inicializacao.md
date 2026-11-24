# 0003. Carga do CSV na inicialização: falha rápida e linha inválida ignorada

- **Status:** Aceito
- **Data:** 2026-11-18
- **Origem:** [`docs/ai/decisoes.md`](../ai/decisoes.md), entrada "Decisões do Marco 1 que divergem do
  briefing (T082)"; clarificação de 2026-11-18 da spec 001; research R2 e R3 da spec 001.

## Contexto

O enunciado manda ler o CSV ao iniciar a aplicação, e a avaliação pode usar outros arquivos. O briefing
deixava em aberto o que fazer com uma linha inválida dentro de um arquivo válido, e sugeria rejeitar a
carga inteira.

## Decisão

- A carga roda em `afterSingletonsInstantiated` (`SmartInitializingSingleton`), depois do Flyway e antes
  de o servidor web aceitar conexões.
- **Erros estruturais** (arquivo ausente ou ilegível, cabeçalho diferente do esperado, separador errado,
  arquivo que não pode ser analisado) interrompem a inicialização, com uma mensagem que cita o arquivo e o
  motivo (`FailureAnalyzer`).
- **Linha inválida** (ano fora de 1 a 9999 ou não numérico, colunas faltando, título ou produtores
  vazios, `winner` inválido, duplicata de ano + título) é ignorada, com aviso no log (número da linha e
  motivo) e resumo de lidas, aceitas e rejeitadas.

## Alternativas consideradas

- Rejeitar a carga inteira por uma linha inválida, como no briefing.
- Carregar num `ApplicationRunner` ou no `ApplicationReadyEvent`: o servidor já estaria aceitando
  conexões com os dados incompletos.
- Mapear as colunas pelo nome, aceitando outra ordem: não foi pedido pelo enunciado.

## Consequências

- Uma linha ruim não derruba a aplicação nem esconde o resultado das linhas boas, e o aviso mostra onde
  está o problema.
- Como a carga termina antes do servidor, a readiness do Marco 2 fica positiva só depois dela sem código
  próprio (ADR 0008).
- No Marco 3, com a carga por Kafka, assíncrona, a readiness vai precisar de um estado explícito.
