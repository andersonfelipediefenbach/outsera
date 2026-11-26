# 0026. Intervalos guardados entre as leituras, invalidados a cada gravação

- **Status:** Aceito
- **Data:** 2026-11-26
- **Origem:** [`docs/ai/decisoes.md`](../ai/decisoes.md), entrada "Intervalos guardados entre as leituras,
  invalidados a cada gravação"; T021 e T025 da spec 004.

## Contexto

O endpoint `GET /api/producers/intervals` recalculava tudo a cada chamada: lia todas as vitórias e montava
os intervalos. Com os 100 mil filmes do teste de carga, o k6 mediu p95 de 227 ms e p99 de 927 ms na
máquina local. O objetivo de latência do SLO é 99% até cerca de 112 ms. O resultado, porém, só muda quando
os dados mudam.

## Decisão

- O resultado calculado fica guardado em memória (`ProducerIntervalsCache`) e é invalidado a cada
  gravação: a carga direta do CSV e cada evento do Kafka gravado ou atualizado.
- **Consistência imediata:** toda gravação marca, dentro da sua transação, uma gravação em andamento até o
  fim dela (commit ou rollback). Uma geração avança no começo e no fim de cada gravação.
  - Com uma gravação em andamento, a leitura consulta o banco direto e não guarda o resultado.
  - Fora disso, a leitura serve o resultado guardado só se ele é da geração atual, e guarda um resultado
    novo só se nada mudou durante o cálculo.
  - Assim, depois de uma gravação confirmada, nenhuma leitura recebe um resultado anterior a ela.
- O ETag continua o mesmo, calculado pelo corpo da resposta (ADR 0007).

## Limite: uma única instância

**O cache vale para uma única instância da aplicação.** A geração e a marca de gravação em andamento ficam
na memória do processo, e só as gravações dessa instância as atualizam. Com duas instâncias consumindo o
mesmo banco, uma gravação feita pela instância A não invalidaria o resultado guardado na instância B, que
continuaria servindo um resultado antigo.

Hoje isso é suficiente: o modo padrão e o compose rodam uma instância, e o consumidor Kafka tem uma
partição. **Com várias instâncias** (Marco 5, Cloud Run, que escala horizontalmente), a invalidação
**precisa** passar a algo compartilhado entre elas:

- **um contador de versão no banco**, incrementado na mesma transação de cada gravação e lido por cada
  leitura antes de usar o resultado guardado. Exige uma migração nova do Flyway e a atualização do
  `SchemaPostgresIT`, que hoje confere que o histórico tem só a versão 1;
- **ou um evento compartilhado** de invalidação, publicado depois de cada gravação confirmada e consumido
  por todas as instâncias. Nesse caso, a consistência entre instâncias deixa de ser imediata e passa a
  depender do atraso do evento, o que precisa ser aceito na spec.

## Alternativas consideradas

- **Contador de versão no banco já agora:** dá a consistência imediata também entre instâncias, mas exige
  a migração V2. Ela mudaria o histórico do Flyway que o `SchemaPostgresIT` (tag `v0.3.0`) confere, e o
  `check-unchanged-tests.sh` reprova a alteração desse IT. Fica para quando houver mais de uma instância.
- **Contador só em memória, incrementado depois do commit:** deixa uma janela entre o commit no banco e o
  incremento, em que outra leitura já vê os dados novos e ainda recebe o resultado antigo.
- **Trocar o bucket do SLO, medir com um arquivo menor ou otimizar só o cálculo:** recusadas pelo
  responsável. O cálculo por chamada continuaria crescendo com os dados.

## Consequências

- Com os 100 mil filmes, o endpoint de intervalos caiu para p95 de 1,54 ms e p99 de 1,84 ms na máquina
  local, e a listagem melhorou de p95 104 ms para 13,1 ms, por não disputar a CPU.
- Durante a ingestão contínua por Kafka, as leituras consultam o banco direto enquanto há gravação em
  andamento, como antes da mudança.
- O `KafkaIntervalsCacheIT` comprova a consistência imediata: lê, grava e lê de novo, sem espera. Com a
  invalidação desligada, ele reprova.
- Um passo para várias instâncias (Marco 5) precisa revisar esta decisão antes de escalar.
