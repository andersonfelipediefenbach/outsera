# AGENTS.md — Regras comuns aos agentes

Regras que valem para todo agente que trabalha neste repositório (Claude Code, Codex e qualquer outro), em qualquer papel. Derivadas de [`docs/ai/planejamento/briefing.md`](docs/ai/planejamento/briefing.md) e [`docs/ai/planejamento/decisoes-planejamento.md`](docs/ai/planejamento/decisoes-planejamento.md).

## Fontes da verdade

Em caso de conflito, vale a ordem abaixo:

1. O enunciado da Outsera (PDF), principalmente o contrato da página 2 e os itens 4.2 e 4.3.
2. A spec da unidade de trabalho em `specs/` (Spec Kit) e a constituição em `.specify/memory/constitution.md`.
3. Este `AGENTS.md`.
4. O briefing e o registro de decisões em `docs/ai/planejamento/`.

Se a tarefa contradiz uma fonte de prioridade maior, pare e pergunte ao Anderson. Não resolva o conflito sozinho.

## Regras invioláveis

- **O modo padrão é sagrado.** A aplicação sobe e os testes passam só com `./mvnw` (`./mvnw spring-boot:run` e `./mvnw verify`), sem Docker e sem nada instalado além do JDK. Nenhum extra pode quebrar isso.
- **O contrato de intervalos é exatamente o do PDF.** `GET /api/producers/intervals` devolve `{ "min": [...], "max": [...] }` com itens `producer`, `interval`, `previousWin` e `followingWin`. Nenhum campo é adicionado, renomeado ou removido.
- **No back-end, só testes de integração** (item 4.2). Nada de testes unitários na API.
  - Todo teste usa `@SpringBootTest` com o contexto completo da aplicação, sem mocks.
  - O resultado é verificado preferencialmente por HTTP. Log ou banco só valem enquanto o comportamento não tem endpoint exposto.
  - Quando o teste precisa controlar o ciclo de vida da aplicação real, ele a roda com `SpringApplication` (Princípio I, constituição 1.5.0). Os casos são falha de inicialização, múltiplas execuções, ordem da subida e saída completa da subida. Continua sem mocks, e o resultado é verificado por HTTP real sempre que o HTTP o observa.
  - Testes unitários só existem no front-end.
- **Um extra só entra completo.** Extra pela metade não é mergeado. O marco 1 nunca é reduzido.

## Fluxo de trabalho

O trabalho é Spec-Driven Development com o GitHub Spec Kit, no modo *spec-anchored*, em ciclos curtos inspirados no AI-DLC.

- **A spec muda antes do código.** Se o comportamento muda, atualize primeiro a spec em `specs/` e só depois o código e os testes.
- Cada unidade de trabalho é um *bolt*, feito em um branch próprio. Ninguém faz commit direto na `main`.
- Passos de um bolt:
  1. O implementador escreve o teste de integração e o código no branch.
  2. O Anderson abre o pull request.
  3. O revisor comenta no pull request.
  4. O implementador corrige apenas o que o Anderson aceitar.
  5. O merge acontece com o pipeline verde, e a sessão é exportada.
- **Rastreabilidade:** cada requisito aponta para a comprovação adequada ao seu tipo (Princípio VI,
  constituição 1.4.0): teste de integração para comportamento da aplicação, revisão com item no
  checklist da spec para documentação, e execução do pipeline para pipeline e imagem.
- Não abra pull request, não faça merge e não crie tags por conta própria. Esses passos são do Anderson.
- Faça commits pequenos e com mensagens descritivas, sem misturar assuntos.

## Papéis e propriedade

| Papel | Ferramenta | Atua em |
| --- | --- | --- |
| Implementador | Claude Code | Bolts de código da API |
| Revisor de pull request | Codex | Todo pull request |
| Testador adversário | Codex | Marco 1 e ingestão por Kafka |
| Infraestrutura e SRE | Claude Code, sessão separada | Docker, pipeline, observabilidade e deploy |
| Documentação | Claude Code, sessão separada | README, ADRs, SLO e runbook |
| Front-end | Claude Code, sessão separada | `frontend/` (marco 6) |

- Trabalham no máximo três agentes ao mesmo tempo, cada um em sua própria cópia de trabalho (worktree).
- Cada arquivo compartilhado (`pom.xml`, `application*.yml`, `README.md`, `compose.yaml`, OpenAPI) tem um único dono por rodada. Não edite arquivo de outro papel. Se precisar de uma mudança nele, registre o pedido.
- **Revisor:** aponte problemas e justifique. Não reescreva o código.
- **Testador adversário:** crie CSVs de ataque a partir da spec, **sem ler o código de produção**. Cada falha encontrada vira teste permanente em `src/test/`.

## Stack e convenções

| Item | Escolha |
| --- | --- |
| Linguagem | Java 25 (LTS) |
| Framework | Spring Boot, versão estável atual |
| Build | Maven com wrapper (`mvnw`) versionado |
| Persistência | JPA, com o esquema versionado por Flyway (sem `ddl-auto` gerando tabelas) |
| Banco | H2 em memória (padrão); PostgreSQL (perfil Docker) |
| CSV | Apache Commons CSV |
| Contrato | OpenAPI com Swagger UI. O contrato é a fonte da documentação e da validação das respostas nos testes |
| Erros | Problem Details (RFC 9457) |
| Logs | Texto padrão do framework na execução local (`./mvnw`). JSON estruturado no container e em ambientes operacionais, ligado por variável de ambiente que a imagem define. Liveness e readiness a partir do Marco 2 (constituição 1.3.0) |

- **Isolamento por perfil:** Kafka, PostgreSQL e o que for do modo Docker só são configurados com o perfil ativo. Nenhuma dependência desses extras pode ser exigida no modo padrão.
- **Um único caminho de gravação:** a regra de registrar um filme fica em um só serviço. O perfil muda apenas a origem dos dados (carga direta ou consumidor Kafka).
- **SQL portável:** nada de SQL específico de H2 ou de PostgreSQL. O mesmo esquema Flyway serve aos dois bancos.
- **Configuração por variáveis de ambiente**, incluindo a porta (`PORT`), o caminho do CSV, o token e o CORS.
- Fora do escopo: imagem nativa, tracing distribuído e Kubernetes.

## Regras de negócio (resumo)

O detalhe fica na spec. Estas regras não mudam sem decisão registrada.

- CSV com separador `;` e cabeçalho `year;title;studios;producers;winner`. `winner` igual a `yes`, sem diferenciar maiúsculas e ignorando espaços, marca vencedor. Vazio significa indicado.
- Arquivo ausente ou com cabeçalho inválido **impede a aplicação de subir**, com mensagem clara.
- Produtores são separados por vírgula e por ` and ` (inclusive `, and`). Os nomes são comparados exatamente como escritos, sem normalizar grafia.
- Intervalos: só pares de vitórias **vizinhas** de cada produtor, depois de ordenar os anos. `min` e `max` trazem **todos** os empatados, ordenados por `previousWin` crescente e depois por nome. Duas vitórias no mesmo ano dão intervalo 0. Sem produtor com duas vitórias, as listas vêm vazias com status 200.
- Resultado de referência com o arquivo fornecido: Joel Silver em `min` (1990–1991, 1 ano) e Matthew Vaughn em `max` (2002–2015, 13 anos).

## Testes

- Todo comportamento novo ou alterado chega com o teste de integração correspondente, no mesmo bolt.
- Os testes do enunciado e dos extras da API rodam com `./mvnw verify` **sem Docker**. Kafka usa o broker embutido do Spring, não Testcontainers.
- Testes que exigem PostgreSQL ficam em um perfil Maven separado, executado no pipeline.
- CSVs de cenário ficam em `src/test/`.
- Um teste que verifica por log ou banco, porque o endpoint ainda não existe, ganha a verificação por HTTP no bolt em que o endpoint surgir (Princípio I).
- Antes de dar uma tarefa como pronta, rode `./mvnw verify` e informe o resultado real. Se algo falhou ou foi pulado, diga.

## Segurança

- Nunca grave tokens, senhas ou credenciais no código, em commits ou em mensagens aos agentes. Tudo o que é digitado aos agentes será publicado.
- A autenticação existe e é testada, mas fica **desligada por padrão**: sem a variável de ambiente do token, os endpoints de negócio ficam públicos. O único token aceito no repositório é o de demonstração documentado no README.
- No pipeline, as actions são fixadas por **hash de commit**, nunca por tag.
- A imagem Docker é multi-stage e roda com usuário não root.

## Evidência de uso de IA (item 5.2)

| Evidência | Onde fica | Regra |
| --- | --- | --- |
| Mensagens com data e hora | `docs/ai/prompts.md` | Gravado automaticamente pelo hook. **Não edite à mão** |
| Histórico das sessões | `docs/ai/sessoes/` | Uma exportação por bolt e por papel |
| Revisões do Codex | Pull requests | Ficam no próprio PR |
| Aceito, rejeitado e por quê | `docs/ai/decisoes.md` | Registre toda sugestão relevante aceita ou rejeitada |
| Planejamento | `docs/ai/planejamento/` | Síntese, só leitura para os agentes |
| Guia do avaliador | `docs/ai/README.md` | Mantido pelo papel de documentação |

- Não inclua dados pessoais nem segredos em nenhum desses arquivos.
- Decisões de arquitetura viram ADR curto em `docs/adr/`.

## Pontos em aberto

Os pontos abaixo não estão decididos. Não escolha sozinho: pergunte ou siga o que a spec definir.

- Versão exata do Angular (marco 6).

Pontos que estavam em aberto e já foram decididos (detalhe em `specs/001-producer-intervals-api/` e em `docs/ai/decisoes.md`):

- **Linha inválida dentro de um CSV válido:** a linha é ignorada, com aviso no log (número da linha e motivo) e resumo da carga. Erros estruturais continuam impedindo a inicialização (spec 001, FR-005 e FR-006).
- **Acesso a dados:** JPA (plano 001).
- **Spring Boot:** 4.1.2 (plano 001, research R1).

## Definição de pronto

- [ ] A spec reflete o comportamento entregue.
- [ ] Cada requisito do bolt tem a comprovação do seu tipo (Princípio VI): teste de integração para
  comportamento, item de revisão no checklist para documentação, estágio do pipeline para pipeline e
  imagem. `./mvnw verify` passa sem Docker.
- [ ] O contrato de intervalos continua idêntico ao do PDF.
- [ ] Nenhum segredo nem dado pessoal foi adicionado.
- [ ] As decisões relevantes foram registradas em `docs/ai/decisoes.md` ou em um ADR.