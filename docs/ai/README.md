# Evidência do uso de IA

Esta pasta reúne a evidência de como a IA foi usada no projeto (item 5.2 do enunciado). O fluxo de
trabalho e os papéis dos agentes estão resumidos no [README principal](../../README.md#10-uso-de-ia).

Nenhum arquivo daqui contém segredos ou credenciais (Princípio VII da
[constituição](../../.specify/memory/constitution.md)).

## O que há em cada arquivo e pasta

| Caminho | O que é | Quem escreve |
| --- | --- | --- |
| [`prompts.md`](prompts.md) | Todas as mensagens enviadas ao Claude Code, em ordem, com data, hora e o identificador da sessão | O hook `.claude/hooks/log-prompt.ps1`, automaticamente. Não é editado à mão |
| [`sessoes/`](sessoes/) | Exportações completas das sessões do Claude Code: mensagens, respostas, comandos executados e resultados | O responsável, com o comando `/export` ao fim de cada bolt |
| [`decisoes.md`](decisoes.md) | Sugestões e decisões relevantes, aceitas ou rejeitadas, com origem, motivo, alternativas e impacto | Os agentes, no bolt em que a decisão acontece |
| [`adversario/`](adversario/) | Os casos de ataque do testador adversário: um CSV e um resultado esperado por caso | O Codex, no papel de testador adversário. Exceções na seção [`adversario/`](#adversario) |
| [`pedidos-documentacao.md`](pedidos-documentacao.md) | Pedidos de outros papéis para arquivos que pertencem ao papel de documentação, como o README | O papel que precisa da mudança |
| [`planejamento/`](planejamento/) | Síntese do planejamento feito antes do código | O responsável. Os agentes só leem |
| `README.md` | Este guia | Papel de documentação |

### `prompts.md`

O hook roda no evento `UserPromptSubmit` do Claude Code (configurado em `.claude/settings.json`) e
acrescenta uma entrada por mensagem:

````markdown
## 2026-11-18 15:24:36 -03:00

- Sessao: `9758a314-ea7b-4e67-ad6f-3c0b7ac28236`

```text
/speckit-constitution
```
````

- A mensagem fica dentro de um bloco de código, exatamente como foi digitada. Isso inclui os comandos
  do Spec Kit (`/speckit-specify`, `/speckit-plan`, `/speckit-tasks`, `/speckit-implement`...).
- O identificador da sessão liga cada mensagem à exportação correspondente em `sessoes/`.

### `sessoes/`

Cada arquivo é a exportação em texto de uma sessão do Claude Code. O nome indica o bolt. A exceção é
o `07-testador-adversario.md`, que é a conversa com o Codex.

| Arquivo | Conteúdo |
| --- | --- |
| `00-preparacao.md` | Hook de prompts, `AGENTS.md`, constituição, spec, plano, research e tarefas, com as rodadas de `/speckit-clarify` e `/speckit-analyze` |
| `01-bolt-1-projeto.md` | Bolt 1: geração do projeto e infraestrutura de teste |
| `02-bolt-2-carga-csv.md` | Bolt 2: carga do CSV |
| `02b-bolt-2-correcao-bom.md` | Correção do bolt 2 pedida pelo Codex no PR: o BOM passou a ser removido antes do parser |
| `02c-bolt-2-correcao-and.md` | Segunda correção pedida pelo Codex no PR do bolt 2: a separação de produtores passou a ser feita em etapas |
| `03-bolt-3-calculo.md` | Bolt 3: cálculo dos intervalos entre vitórias |
| `04-bolt-4-endpoint.md` | Bolt 4: endpoint de intervalos no formato do enunciado |
| `05-bolts-5-6-e-documentacao.md` | Bolts 5 e 6 (listagem, Swagger e fechamento) e, na mesma sessão, o papel de documentação: README, este guia e o teste de verbos não suportados (405) |
| `07-testador-adversario.md` | Conversa com o Codex no papel de testador adversário. Ele trabalhou numa pasta isolada, só com o enunciado, a spec e o contrato, sem acesso ao código de produção |
| `08-adversario-testes-e-correcoes.md` | Testes de integração dos casos do testador adversário e as correções que eles provocaram |
| `09-documentacao-adversario.md` | Papel de documentação: merge da main no branch do testador adversário e atualização dos READMEs com o resultado da rodada |

- As exportações de uma mesma sessão são **cumulativas**: um arquivo exportado mais tarde repete o
  início do anterior.
- Os arquivos são texto UTF-8 com os caracteres de desenho do terminal. Abra num editor, e não como
  Markdown renderizado.
- A lista acima é a que existe hoje. Uma sessão entra aqui quando o responsável a exporta.

### `decisoes.md`

Cada entrada segue o mesmo formato:
- **Origem:** quem apontou o problema ou a sugestão. Pode ser o implementador, uma revisão do Codex no
  PR ou um achado do `/speckit-analyze`.
- **Decisão:** o que foi decidido e por quem, o responsável ou o implementador. Neste segundo caso, a
  decisão fica para revisão no PR.
- **Motivo** ou **Efeito**.
- **Alternativas rejeitadas.**
- **Impacto:** spec, research, tarefas e código afetados.

As entradas mais recentes ficam no topo. As revisões do Codex são o ponto de partida de várias delas
(BOM, separação de produtores), e o texto completo de cada revisão está no pull request correspondente.

### `adversario/`

Os casos que o Codex criou no papel de testador adversário. Ele trabalhou numa pasta isolada, só com o
enunciado, a spec e o contrato, sem ler o código de produção. A conversa está em
`sessoes/07-testador-adversario.md`.

- Cada caso tem um CSV e um `expected.json` com a resposta esperada de
  `GET /api/producers/intervals`. O [`README.md`](adversario/README.md) da pasta explica o que cada caso
  ataca e quais requisitos da spec ele cobre.
- **Os arquivos são os originais do testador, sem alteração.** Ficam aqui como registro da rodada.
- Os testes usam uma cópia em `src/test/resources/scenarios/adversario/`, com um IT por caso em
  `src/test/java/com/outsera/awards/adversario/`.
- **Casos 12 e 13: o esperado foi escrito pelo projeto, não pelo testador.** O testador entregou esses
  dois CSVs sem `expected.json`, porque a spec não determinava uma resposta única (caixa do separador
  `and` e ordem de nomes com acento e caixa). O implementador escreveu os dois arquivos depois, a partir
  das decisões do responsável registradas em `decisoes.md`.
- **Caso 09: o teste diverge do testador.** A decisão de aceitar só anos de 1 a 9999 (FR-006) faz o
  arquivo do caso perder as linhas dos anos 0, 2147483647, 2147483648 e 4294967296, e o esperado passa a
  ser `min` e `max` vazios. O `expected.json` desta pasta continua o do testador. O esperado ajustado
  existe só na cópia de `src/test/`.

### `planejamento/`

| Arquivo | Conteúdo |
| --- | --- |
| `briefing.md` | Objetivo, escopo, contrato, regras de negócio e estratégia de testes definidos antes do código |
| `decisoes-planejamento.md` | Decisões do planejamento, com as alternativas consideradas e o motivo |

São a síntese das conversas de planejamento, que não foram incluídas. Quando o briefing e a spec
divergem, vale a spec. As divergências estão registradas em `decisoes.md`.

## Como ler a evidência

Um caminho para acompanhar uma funcionalidade do pedido até o código:

1. **Planejamento:** `planejamento/briefing.md` mostra o que se queria antes do código.
2. **Regras do projeto:** a [constituição](../../.specify/memory/constitution.md) e o
   [`AGENTS.md`](../../AGENTS.md) mostram as regras que todo agente segue.
3. **Especificação:** em [`specs/001-producer-intervals-api/`](../../specs/001-producer-intervals-api/),
   a spec traz os requisitos, o plano traz a matriz requisito → teste, e as tarefas mostram em que bolt
   cada item entrou.
4. **O que foi pedido à IA:** `prompts.md`, em ordem cronológica.
5. **Como a IA trabalhou:** a sessão do bolt em `sessoes/`, com os testes escritos antes do código, as
   falhas esperadas, a implementação e o `./mvnw verify` de cada etapa.
6. **O que foi aceito ou rejeitado, e por quê:** `decisoes.md`, mais os comentários do Codex em cada
   pull request.
7. **O resultado:** o histórico do git, com um ou mais commits por bolt. A partir do bolt 4, os
   commits feitos pelo Claude Code trazem a linha `Co-Authored-By`. Nos anteriores, o trabalho da IA
   aparece no `prompts.md` e nas sessões exportadas.
