# 0014. Kafka como caminho único de ingestão no modo Docker

- **Status:** Aceito
- **Data:** 2026-11-24
- **Origem:** [`docs/ai/decisoes.md`](../ai/decisoes.md), entrada "Etapa 2 do Marco 3: verificações e
  decisões da ingestão por Kafka", e research R1, R2, R6, R8 e R10 da spec 003.

## Contexto

No modo Docker, todo dado entra pelo mesmo caminho: o arquivo inicial e os vencedores novos passam pelo
mesmo tópico e pelo mesmo consumidor (briefing). A aplicação não pode se declarar pronta antes de a carga
terminar (FR-013), e o Kafka não pode ser configurado no modo padrão (Princípio V).

## Decisão

- Perfil `kafka`, ligado pelo grupo `docker`. A autoconfiguração do Kafka fica excluída fora dele, e a
  carga direta (`MovieFileLoader`) não roda com ele.
- O publicador lê o arquivo na subida, com as mesmas regras e os mesmos avisos do Marco 1, e publica depois
  do `ApplicationReadyEvent`. São um evento por filme aceito e, no fim, um evento `load-completed` com o
  `runId` da execução. Com o broker fora do ar, ele tenta de novo, e a aplicação fica viva e não pronta.
- O consumidor grava pelo mesmo `MovieImportService` do modo padrão.
- Os tópicos `awards.movies` e `awards.movies.errors` têm uma partição cada. Assim, o marcador é consumido
  depois de todos os filmes da mesma execução.
- A readiness ganha o indicador `ingestion`, que só fica `UP` com o marcador do `runId` desta execução.
- A propriedade `awards.ingestion.publish-completion` (padrão `true`) permite que o marcador seja enviado
  por outro cliente. O `KafkaReadinessIT` a usa para observar a readiness antes e depois do marcador.

## Alternativas consideradas

- Trocar o estado de prontidão à mão: o Boot marca `ACCEPTING_TRAFFIC` antes de a carga por Kafka
  terminar, e haveria uma janela em que a aplicação aparece pronta.
- Várias partições, com um marcador por partição: mais complexo, sem ganho neste volume.
- Kafka só para os vencedores novos, com a carga direta do arquivo: seriam dois caminhos de gravação.

## Consequências

- A carga direta e a carga por Kafka produzem respostas idênticas, byte a byte (`KafkaEquivalenceIT`).
- Um reinício publica o arquivo de novo. A gravação idempotente (ADR 0015) evita duplicatas.
- O publicador e o consumidor rodam dentro da mesma aplicação e supõem uma instância. Várias instâncias
  exigiriam outro desenho da readiness.
