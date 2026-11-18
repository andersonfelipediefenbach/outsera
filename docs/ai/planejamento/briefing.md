# Briefing do projeto — API Golden Raspberry Awards (Teste Outsera)

15/11/2026 · Anderson

## Objetivo e escopo

A entrega é uma API REST que cumpre o enunciado da Outsera rodando só com `./mvnw`, com extras opcionais em camadas, até 25/11/2026.

| Camada | Conteúdo | Como roda |
| --- | --- | --- |
| Exigido | Endpoint de intervalos, carga do CSV, banco em memória, testes de integração, README e registro de IA | `./mvnw` |
| Extra na API | Listagem de filmes, Swagger, ETag, observabilidade e autenticação parametrizada | `./mvnw` |
| Extra de ambiente | PostgreSQL, Kafka, Prometheus e Grafana | `docker compose up` |
| Extra de entrega | Pipeline, imagem assinada, publicação no Cloud Run e dev container | GitHub Actions |
| Extra de interface | Front-end em Angular | Pasta própria, build separado |

Três regras valem para todo o projeto:

- Nenhum extra pode impedir a subida ou os testes do modo padrão.
- O contrato do endpoint de intervalos é exatamente o da página 2 do PDF.
- No back-end, somente testes de integração, conforme o item 4.2 do enunciado.

## Contrato da API

A API tem um endpoint exigido e um de leitura adicional; os caminhos abaixo são propostas, porque o PDF não fixa a URL.

| Endpoint | Resposta | Origem |
| --- | --- | --- |
| `GET /api/producers/intervals` | Listas `min` e `max` | Exigido (seções 3 e 6 do PDF) |
| `GET /api/movies` | Página de filmes, com filtros `year` e `winner` | Extra, pela leitura da seção 1 do PDF |
| `GET /swagger-ui.html` e `GET /v3/api-docs` | Documentação e contrato OpenAPI | Extra |

Formato do endpoint de intervalos, idêntico ao do enunciado:

```json
{
  "min": [
    { "producer": "Producer 1", "interval": 1, "previousWin": 2008, "followingWin": 2009 }
  ],
  "max": [
    { "producer": "Producer 2", "interval": 99, "previousWin": 1900, "followingWin": 1999 }
  ]
}
```

| Situação | Resposta |
| --- | --- |
| Consulta com sucesso | 200 com o corpo acima |
| Nenhum produtor com duas vitórias | 200 com `min` e `max` vazios |
| Requisição com `If-None-Match` igual ao ETag atual | 304 sem corpo |
| Parâmetro inválido na listagem | 400 em Problem Details |
| Autenticação ligada e token ausente ou errado | 401 em Problem Details |

O contrato OpenAPI é a fonte do Swagger UI e do teste que valida as respostas, então documentação e código não divergem.

## Regras de negócio

O resultado de referência do arquivo fornecido é Joel Silver no mínimo (1990 a 1991, 1 ano) e Matthew Vaughn no máximo (2002 a 2015, 13 anos).

**Carga do CSV**

- Separador de colunas `;`, com o cabeçalho `year;title;studios;producers;winner`.
- A coluna `winner` com `yes` marca o vencedor, ignorando maiúsculas e espaços; vazia significa indicado.
- O caminho do arquivo é configurável, com o arquivo fornecido como padrão.
- Arquivo ausente ou sem o cabeçalho esperado derruba a inicialização com mensagem clara.
- Linha inválida dentro de um arquivo válido: a carga inteira é rejeitada. Esta decisão está em aberto.

**Separação de produtores**

- Os nomes são separados por vírgula e pela palavra `and` cercada de espaços, aceitando também `, and`.
- Cada nome é comparado exatamente como está escrito, sem correção de grafia.

| Caso presente no arquivo fornecido | Exemplo | Tratamento |
| --- | --- | --- |
| Vírgula antes do `and` | Eric Fellner, and Tom Hooper | Separa em dois nomes |
| Lista sem `and` | Gayle Dickie, Kenn Viselman | Separa pela vírgula |
| `and` colado por erro de digitação | Brian Robbinsand Sharla Sumpter Bridgett | Mantém como um nome só |
| `and` dentro de nome composto | Sid and Marty Krofft | Separa em dois, limitação registrada |
| Grafias diferentes | Steven Perry e Steve Perry | Tratados como pessoas distintas |

**Cálculo dos intervalos**

1. Reunir os anos de vitória de cada produtor.
2. Ordenar os anos de cada produtor.
3. Gerar um intervalo para cada par de vitórias vizinhas.
4. Devolver em `min` todos os intervalos com o menor valor e em `max` todos com o maior.

**Decisões sobre ambiguidades do enunciado**

| Ponto ambíguo | Decisão |
| --- | --- |
| Empate no menor ou no maior intervalo | Todos os empatados entram na lista |
| Produtor com três ou mais vitórias | Cada par vizinho é um candidato; nunca se compara a primeira com a última |
| Mesmo produtor com duas vitórias no mesmo ano | Conta como intervalo 0 |
| Ordem dos itens empatados | Por `previousWin` crescente e depois por nome |
| Nenhum produtor com duas vitórias | Listas vazias, sem erro |
| Ano sem vencedor, como 2007 | Não afeta o cálculo |
| Seção 1 fala em leitura da lista, seção 3 exige só os intervalos | A listagem é implementada como leitura básica, sem alterar o contrato exigido |

## Stack e estrutura

O mesmo código roda em dois modos, selecionados por perfil do Spring: o padrão cumpre o enunciado e o Docker mostra o ciclo completo.

| | Modo padrão | Modo Docker |
| --- | --- | --- |
| Como sobe | `./mvnw spring-boot:run` | `docker compose up` |
| Banco | H2 em memória | PostgreSQL |
| Entrada do CSV | Carga direta | Publicação e consumo por Kafka |
| Observabilidade | Endpoints de health e métricas | Prometheus e Grafana coletando |
| Público | Avaliador conferindo os requisitos | Quem quiser ver o ambiente completo |

| Item | Escolha |
| --- | --- |
| Linguagem | Java 25, o LTS vigente |
| Framework | Spring Boot, versão estável atual |
| Persistência | JPA, com esquema versionado por Flyway |
| Build | Maven, com o wrapper no repositório |
| Contrato | OpenAPI com Swagger UI |
| Leitura do CSV | Apache Commons CSV |
| Inicialização | Cache AOT da JVM, a confirmar o suporte na versão do Spring Boot |

A regra de registrar um filme fica em um único serviço; o que muda por perfil é só a origem dos dados.

```text
outsera-backend/
├── .specify/                  # Spec Kit: princípios e modelos
├── specs/                     # Spec Kit: spec, plano e tarefas
├── .devcontainer/             # ambiente pronto para o avaliador
├── .github/
│   ├── workflows/             # pipeline de CI/CD
│   └── dependabot.yml
├── docs/
│   ├── ai/
│   │   ├── README.md          # guia da evidência de IA
│   │   ├── prompts.md         # mensagens gravadas pelo gancho
│   │   ├── sessoes/           # transcrições por bolt e por papel
│   │   ├── decisoes.md        # aceito, rejeitado e por quê
│   │   └── planejamento/      # síntese do planejamento antes do código
│   ├── adr/                   # decisões de arquitetura
│   ├── slo.md
│   └── runbook.md
├── observability/             # Prometheus, alertas e dashboard
├── frontend/                  # Angular, build separado
├── src/
│   ├── main/
│   └── test/                  # testes de integração e CSVs de cenário
├── AGENTS.md                  # regras comuns aos agentes
├── Dockerfile
├── compose.yaml
├── pom.xml
├── mvnw
└── README.md
```

## Testes

No back-end há somente testes de integração, que sobem a aplicação e chamam os endpoints por HTTP; testes unitários ficam restritos ao front-end.

| Cenário | Resultado esperado |
| --- | --- |
| Arquivo fornecido | Joel Silver em `min` e Matthew Vaughn em `max` |
| Empate no menor intervalo | Todos os empatados em `min` |
| Empate no maior intervalo | Todos os empatados em `max` |
| Três ou mais vitórias do mesmo produtor | Só pares vizinhos comparados |
| Linhas fora de ordem cronológica | Mesmo resultado do arquivo ordenado |
| Duas vitórias no mesmo ano | Intervalo 0 |
| Nenhum produtor com duas vitórias | `min` e `max` vazios |
| Formatos de `and` e de vírgula | Nomes separados conforme as regras |
| Variações de `yes` na coluna `winner` | Vencedor reconhecido |
| Arquivo sem cabeçalho válido | Aplicação não sobe |

Além dos cenários fixos:

- **CSVs de ataque:** criados pelo testador adversário a partir da spec, sem ver o código. Cada falha encontrada vira teste permanente.
- **Dados gerados:** datasets aleatórios comparados com um cálculo de referência simples.
- **Contrato:** respostas validadas contra o OpenAPI.
- **Listagem:** paginação, filtros e parâmetros inválidos.
- **ETag:** segunda chamada com `If-None-Match` devolve 304.
- **Equivalência:** o mesmo CSV pela carga direta e pela carga por Kafka produz a mesma resposta.
- **Prontidão:** no modo Kafka, a aplicação não se declara pronta antes do fim da carga.

| Conjunto | Como roda | Exige Docker |
| --- | --- | --- |
| Testes do enunciado e dos extras da API | `./mvnw verify` | Não |
| Kafka | `./mvnw verify`, com o broker embutido do Spring | Não |
| PostgreSQL | Perfil Maven separado, executado no pipeline | Sim |
| Front-end | Testes de componente e fluxo de ponta a ponta, em pipeline próprio | Não |

A regra do item 4.2 vale para a API. No front-end, que está fora do escopo do enunciado, entram testes unitários de componente e automação de ponta a ponta.

## SRE e observabilidade

A operação é tratada como um raciocínio fechado: objetivo, medição, alerta e ação.

| Item | Conteúdo |
| --- | --- |
| Health checks | Liveness e readiness; a aplicação só fica pronta depois da carga dos dados |
| Métricas | Latência e taxa de erro dos endpoints, expostas para o Prometheus |
| Logs | Estruturados em JSON |
| Falhas | Inicialização interrompida com arquivo inválido; erros da API em Problem Details |
| SLO | Uma página com o objetivo de disponibilidade e de latência |
| Alertas | Regras no Prometheus ligadas ao SLO, cada uma apontando para o runbook |
| Runbook | Uma página com o que fazer em cada alerta |
| Teste de carga | k6, com os números medidos publicados no README |
| Dashboard | Grafana, versionado no repositório |
| Imagem | Dockerfile multi-stage, usuário não root, assinada, com SBOM e atestado de procedência |

Os valores do SLO só são definidos depois do teste de carga, para que venham de medição.

## Modo Docker: PostgreSQL e Kafka

No modo Docker, todo dado entra pelo mesmo caminho: o CSV inicial e os novos vencedores passam pelo mesmo tópico e pelo mesmo consumidor.

1. A aplicação sobe no perfil Docker.
2. Um publicador lê o CSV e envia uma mensagem por filme ao tópico.
3. O consumidor grava cada filme no PostgreSQL, usando o mesmo serviço do modo padrão.
4. Ao final, o publicador envia uma mensagem de carga concluída.
5. Só depois de consumir essa mensagem a aplicação se declara pronta.

| Risco | Tratamento |
| --- | --- |
| Consulta antes do fim da carga | Readiness só fica positiva após a mensagem de carga concluída |
| Reinício republica o arquivo | Gravação idempotente, com chave por ano e título |
| Mensagem inválida | Enviada a um tópico de erros, sem travar o consumo |
| Diferenças entre H2 e PostgreSQL | Esquema único por Flyway e consultas sem SQL específico de um banco |
| Dependência vazando para o modo padrão | Kafka e PostgreSQL só são configurados com o perfil ativo |

O banco persistente é o que dá sentido ao Kafka: um vencedor recebido por evento continua lá depois de um reinício. O compose usa a imagem leve do Kafka, e o atraso do consumidor aparece no dashboard.

## Autenticação

A autenticação existe e é testada, mas fica desligada por padrão, porque o avaliador vai chamar o endpoint sem token.

- O token esperado vem de variável de ambiente e é comparado com o cabeçalho da requisição.
- Sem a variável definida, os endpoints de negócio ficam públicos.
- Um token de demonstração é documentado no README, para quem quiser ligar e testar.
- Nenhum token real é gravado no código nem no repositório.
- No ambiente publicado, os endpoints operacionais (métricas e detalhes de health) exigem token.

## Pipeline e publicação

O pipeline no GitHub Actions leva cada mudança do commit até a imagem publicada, e a publicação em servidor é um estágio opcional no final.

| Estágio | Quando roda | O que faz |
| --- | --- | --- |
| CI | Todo push e pull request | Build, testes dos dois perfis, cobertura, análise de segurança, lint do Dockerfile e teste da imagem |
| Entrega | Tag de versão | Imagem assinada no GHCR, SBOM, atestado e release com o JAR |
| Publicação | Tag de versão, após a entrega | Deploy da API no Cloud Run, com autenticação por OIDC |

- Todas as actions são fixadas pelo hash do commit, não pela tag, por causa dos ataques à cadeia de entrega de 2026.
- O Dependabot atualiza dependências e os hashes das actions.
- A `main` é protegida: merge só com o pipeline verde e a revisão feita.
- No Cloud Run só a aplicação é publicada, no modo padrão; Kafka, PostgreSQL e Grafana ficam no compose local.
- A porta vem da variável `PORT` e toda configuração vem de variáveis de ambiente.
- A conta no GCP, o cartão e o alerta de orçamento são passos feitos pelo Anderson.
- O README apresenta a URL como demonstração e avisa que a primeira chamada pode demorar alguns segundos.

## Front-end

O front é o último extra: uma aplicação Angular responsiva, em pasta própria, que não participa do build do Maven.

| Tela | Conteúdo | Endpoints usados |
| --- | --- | --- |
| Painel | Intervalos mínimo e máximo, anos com mais de um vencedor e estúdios mais premiados | Intervalos e duas agregações novas |
| Lista de filmes | Tabela paginada, com filtro por ano e por vencedor | Listagem |

- As duas agregações são criadas na API junto com o front, com o contrato OpenAPI definido antes.
- O layout usa cartões no celular e tabelas no desktop.
- O CORS é configurado por variável de ambiente, só para a origem do front.
- No compose, um container serve o front e encaminha as chamadas para a API.
- A publicação é em hospedagem estática, como Vercel ou Firebase Hosting.
- Os testes são de componente e de ponta a ponta, em pipeline próprio.

## Metodologia, agentes e evidência de IA

A especificação conduz o trabalho com o GitHub Spec Kit, e o ciclo com os agentes segue iterações curtas inspiradas no AI-DLC.

| Elemento | Escolha |
| --- | --- |
| Ferramenta condutora | GitHub Spec Kit (v1.4.0) |
| Princípio | SDD no modo spec-anchored: a spec evolui a cada ciclo e muda antes do código |
| Ciclo | Unidades de trabalho, um bolt por unidade e aprovação por pull request |
| Rastreabilidade | Cada requisito aponta para o teste de integração que o comprova |

Um bolt segue sempre os mesmos passos:

1. O implementador escreve o teste de integração e o código, em um branch.
2. O Anderson abre o pull request.
3. O revisor comenta no pull request.
4. O implementador corrige o que o Anderson aceitar.
5. O merge acontece com o pipeline verde, e a sessão é exportada.

| Papel | Ferramenta | Atua em |
| --- | --- | --- |
| Implementador | Claude Code | Todos os bolts de código |
| Revisor de pull request | Codex | Todo pull request |
| Testador adversário | Codex | Marco 1 e ingestão por Kafka |
| Infraestrutura e SRE | Claude Code, sessão separada | Docker, pipeline, observabilidade e deploy |
| Documentação | Claude Code, sessão separada | README, ADRs, SLO e runbook |
| Front-end | Claude Code, sessão separada | Marco 6 |

No máximo dois ou três agentes trabalham ao mesmo tempo, cada um em sua cópia de trabalho do repositório, e cada arquivo compartilhado tem um dono por rodada. As regras comuns ficam em `AGENTS.md`.

| Evidência exigida pelo item 5.2 | Onde fica |
| --- | --- |
| Mensagens enviadas aos agentes, com data e hora | `docs/ai/prompts.md`, gravado por um gancho |
| Histórico das sessões | `docs/ai/sessoes/`, por bolt e por papel |
| Revisões do Codex | Nos próprios pull requests |
| O que foi aceito, rejeitado e por quê | `docs/ai/decisoes.md` |
| Análise antes do código | `docs/ai/planejamento/`, como síntese de decisões |
| Guia para o avaliador | `docs/ai/README.md` |

Tudo o que for digitado para os agentes será publicado, então tokens e dados pessoais não entram nas mensagens.

**Facilidades para o avaliador**

- README com a seção "avalie em 2 minutos": três comandos, exemplos de `curl` e um diagrama da arquitetura.
- Tabela de requisitos do enunciado apontando para o teste que comprova cada um.
- README dividido em "Requisitos do teste" e "Extras", com o motivo de cada extra.
- Dev container, para abrir o projeto pronto no navegador ou na IDE.
- ADRs curtos para as decisões de arquitetura.

## Marcos e cronograma

São seis marcos em dez dias, cada um fechado com tag e entregável sozinho; as datas são uma proposta, a ajustar às horas disponíveis por dia.

| Etapa | Datas | Conteúdo |
| --- | --- | --- |
| Preparação | 16/11 | Repositório, Spec Kit, `AGENTS.md`, gancho de mensagens, conexão do Codex e a spec inicial |
| Marco 1 | 17 a 18/11 | Carga do CSV, cálculo, endpoint de intervalos, listagem, Swagger e CSVs de ataque |
| Marco 2 | 19/11 | Observabilidade, Dockerfile, pipeline, README de avaliação rápida, ETag e ADRs |
| Marco 3 | 20 a 21/11 | PostgreSQL com Flyway, Kafka como ingestão única e autenticação |
| Decisão de escopo | 21/11 | Se o marco 3 não estiver fechado, os marcos 4 a 6 são reduzidos |
| Marco 4 | 22/11 | Alertas de SLO, k6, dados gerados, cache AOT, assinatura da imagem e dev container |
| Marco 5 | 23/11 | Publicação da API no Cloud Run |
| Marco 6 | 23 a 24/11 | Agregações para o painel e front-end em Angular, com testes |
| Revisão e entrega | 25/11 | Sem construção, só revisão e entrega |

**Ordem de corte, se o tempo apertar**

1. Cache AOT, dados gerados, k6 e assinatura da imagem.
2. Publicação no Cloud Run.
3. Agregações e front-end.

O marco 1 nunca é reduzido, e um extra só entra na entrega se estiver completo.

**Checagem final, no dia da entrega**

- [ ] Clonar o repositório em uma pasta limpa e rodar `./mvnw verify`.
- [ ] Subir a aplicação e chamar o endpoint com o arquivo fornecido e com um CSV diferente.
- [ ] Reler a evidência de IA procurando tokens e dados pessoais.
- [ ] Conferir os links e os comandos do README.
- [ ] Abrir o dev container e rodar os testes dentro dele.

**Pontos em aberto**

- Tratamento de uma linha inválida dentro de um CSV válido.
- Horas disponíveis por dia, para confirmar as datas.
- Versões exatas de Spring Boot e Angular, a confirmar na criação do projeto.
- Nomes dos comandos de exportação de sessão e do evento do gancho, a confirmar na documentação do Claude Code.
