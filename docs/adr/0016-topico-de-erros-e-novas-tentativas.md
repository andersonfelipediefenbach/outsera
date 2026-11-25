# 0016. Tópico de erros para eventos inválidos e novas tentativas para falhas de gravação

- **Status:** Aceito
- **Data:** 2026-11-24
- **Origem:** [`docs/ai/decisoes.md`](../ai/decisoes.md), entradas "Segunda análise da spec 003" e "Etapa 2
  do Marco 3"; research R7 e R9.

## Contexto

Um evento inválido não pode travar o consumo (FR-015), e um evento válido não pode ser perdido quando a
gravação falha, por exemplo com o banco fora (FR-015a). As duas situações pedem tratamentos opostos.

## Decisão

- O consumidor lê os bytes crus e valida o evento com as mesmas regras da linha do CSV, numa classe comum
  (`MovieFieldRules`). Há também os motivos que só existem no JSON: `NOT_UTF8`, `NOT_JSON`, `TYPE_UNKNOWN`
  e `RUN_ID_MISSING`.
- **Evento inválido:** não é gravado. Vai ao tópico de erros com os bytes originais e os cabeçalhos
  `awards-error-reason`, `awards-error-at` e a posição de origem. O log traz o motivo e a posição, sem o
  conteúdo, e o offset avança. Se o tópico de erros estiver fora, o aviso registra a falha e o consumo
  segue.
- **Falha de gravação:** a exceção sobe ao `DefaultErrorHandler`, que tenta de novo sem limite de
  tentativas, com espera crescente até 30 s, sem avançar o offset.
- **Classificação das falhas de gravação** (atualizado em 2026-11-25, apontamento do Codex no PR do Marco 3):
  uma recusa do banco por dado inválido (SQLState das classes 22 e 23) é permanente e vai ao tópico de erros
  com o motivo `DATABASE_REJECTED`, sem nova tentativa. As demais (42P01, perda de conexão, tempo esgotado do
  pool) continuam com a nova tentativa sem limite. Sem isso, um evento que só o banco recusa travaria a
  partição para sempre.
- **Caractere nulo:** a regra `NUL_CHARACTER` (U+0000 no título, nos estúdios ou nos produtores) vale nos dois
  caminhos de carga, porque o PostgreSQL não aceita esse caractere em texto (atualizado em 2026-11-25).
- **Tamanho máximo:** a regra `FIELD_TOO_LONG` (campo maior que a coluna) vale nos dois caminhos. Sem ela, o
  banco recusaria a gravação, e a recusa seria tentada de novo para sempre, travando o tópico.

## Alternativas consideradas

- `DeadLetterPublishingRecoverer` para todos os erros: trataria a falha de banco como evento inválido, e
  a carga ficaria incompleta.
- `JsonDeserializer` com `ErrorHandlingDeserializer`: o conteúdo inválido chegaria ao ouvinte já
  transformado em exceção, sem os bytes originais.
- Avro e um registro de esquemas: complexidade sem necessidade (Princípio V).

## Consequências

- Uma linha do CSV com campo maior que a coluna deixou de derrubar a carga direta: ela é ignorada com aviso
  (spec 001, FR-006, atualizada). A carga direta e o Kafka continuam equivalentes, sem exceção.
- Enquanto o banco está fora, o consumo para. Durante a carga, a readiness fica negativa; depois dela, o
  atraso do consumidor cresce e aparece no dashboard.
- A nova tentativa é comprovada pelo `RetryPostgresIT` (gravação que falha) e pelo passo 7b do compose
  (queda real do banco).
