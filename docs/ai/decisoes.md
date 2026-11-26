# Registro de decisões

Sugestões e decisões relevantes, aceitas ou rejeitadas, com o motivo (item 5.2 do enunciado). As
decisões de arquitetura ficam em ADRs, em `docs/adr/`.

## 2026-11-26 — Contrato 1.3.0: agregações do painel (spec 006)

- **Origem:** spec 006 (Marco 6), FR-001 a FR-004. O painel do front precisa dos anos com mais de um vencedor e
  das vitórias por estúdio.
- **Decisão:** o contrato passa à versão 1.3.0 (MINOR), com dois paths novos
  (`/api/aggregations/years-with-multiple-winners` e `/api/aggregations/studios-with-win-count`) e quatro esquemas
  fechados. Paths, parâmetros, respostas e esquemas existentes não mudam (Princípio III). O `info.description`
  continua com duas frases, então o `ContractTextIT` passa sem mudança.
- **ITs alterados:** só os dois que já têm exceção em `.github/unchanged-tests-exceptions.txt`, sem título novo:
  - `ApiDocsIT`: o contrato da spec passa a ser o de `specs/006-frontend-angular/contracts/openapi.yaml`, e a lista
    de paths do contrato passa a ter os quatro endpoints;
  - `ApiDocsSecurityIT`: a versão `1.3.0` e os dois caminhos novos na conferência do `security` e da resposta 401.
- **Alternativa rejeitada:** `?projection=` em `/api/movies`, que mudaria um endpoint existente (spec 006, FR-005;
  research R6).

## 2026-11-26 — Emenda 2.1.0 da constituição: comprovação do front-end

- **Origem:** spec 006 (Marco 6), FR-024. O pedido do responsável inclui testes unitários no front e um teste de
  ponta a ponta no pipeline, mas o Princípio VI só conhecia três tipos de comprovação (IT, revisão e pipeline), e o
  Princípio II não dizia nada sobre o Node.
- **Decisão (responsável):** emenda MINOR, aprovada no PR do Marco 6, como primeiro commit do bolt:
  - Princípio II: o front fica fora do `./mvnw`; nenhum comando Maven pode exigir Node nem construir o front;
  - Princípio VI: novo tipo de comprovação, comportamento do front-end, por testes unitários com cobertura mínima
    de 80% das linhas e teste de ponta a ponta contra a aplicação real, sem respostas simuladas, no pipeline;
  - Princípio I: sem regra nova; os testes unitários continuam só no front, agora citando o Princípio VI;
  - portão de merge: inclui os jobs do front.
- **Motivo:** sem a emenda, os requisitos de comportamento do front ficariam sem forma de comprovação válida, ou
  exigiriam IT no back-end para algo que só existe no navegador.
- **Impacto:** `AGENTS.md` (regras invioláveis, testes, stack e pontos em aberto) e spec 006. As specs 001 a 005
  não mudam.

## 2026-11-26 — Contrato 1.2.1 e exceção do `ApiDocsSecurityIT` à regra dos testes sem alteração

- **Origem:** pedido do responsável no fechamento do Marco 5 (spec 005, FR-027). Com o Swagger UI público na
  URL do Cloud Run, o contrato passa à versão 1.2.1, com três ajustes de texto:
  - o `info.description` fica com duas frases. O ETag, o 304 e a autenticação ficam no README;
  - a descrição do `bearerAuth` passa a "Opcional. Exigido só quando a variável API_TOKEN está definida.";
  - nenhum texto tem `<` ou `>` sem escape: o `Bearer <token>` do 1.2.0 sumia na renderização do Swagger UI.

  O `security` de cada operação já tinha a alternativa vazia (`{}`) antes do `bearerAuth` desde o 1.2.0, e
  ficou como estava. Paths, parâmetros, respostas e esquemas não mudam (Princípio III).
- **Conflito com a regra dos ITs sem alteração:** o `ApiDocsSecurityIT` (Marco 3, tag `v0.3.0`) confere
  `info.version == "1.2.0"`. Subir a versão obriga a mudá-lo, e o `check-unchanged-tests.sh v0.3.0`
  reprovaria.
- **Alternativas apresentadas ao responsável:**
  - **exceção mínima:** trocar só a versão no `ApiDocsSecurityIT`;
  - **manter o 1.2.0:** fazer os ajustes de texto sem subir o número;
  - **não ajustar o contrato agora.**
- **Decisão do responsável:** exceção mínima.
  - O `ApiDocsSecurityIT` muda só no literal da versão e nos dois textos que a citam (o `@DisplayName` e o
    javadoc). Nenhuma outra asserção muda, e ele ganha uma linha em `.github/unchanged-tests-exceptions.txt`
    com o título desta entrada.
  - O contrato-fonte passa a `specs/005-deploy-cloud-run/contracts/openapi.yaml`, e o 1.2.0 da spec 003 fica
    como registro, como os das specs 001 e 002.
  - O `ApiDocsIT`, que já tinha exceção ("Exceção do `ApiDocsIT` à regra dos testes sem alteração"), troca só
    a constante `SPEC_CONTRACT`, como no Marco 3.
- **Teste novo:** o `ContractTextIT` (modo padrão) confere as duas frases do `info.description`, a descrição
  do `bearerAuth` e que nenhuma `description`, `summary` ou `title` do contrato contém `<` ou `>`.

## 2026-11-26 — Marco 5: publicação no Cloud Run

Decisões da [spec 005](../../specs/005-deploy-cloud-run/spec.md), do plano e da análise. O resumo de
arquitetura está no [ADR 0027](../adr/0027-publicacao-no-cloud-run.md).

- **Publicação a cada merge na `main`, e não por tag de versão** (FR-025). O briefing previa a publicação por
  tag, depois da entrega. O responsável decidiu publicar a cada push na `main`, depois dos 10 checks
  obrigatórios, para a URL mostrar sempre o que está na `main`. O briefing não foi alterado: ele é o registro
  do planejamento.
- **Revisão sem tráfego até ser conferida** (clarificação, opção A, escolhida pelo responsável). A revisão
  nova é conferida pela URL própria e só depois recebe o tráfego.
  - Recusada a promoção direta com volta automática: deixaria a revisão ruim na URL pública por alguns
    segundos.
  - Recusado o "só reprovar o job": a URL ficaria quebrada até uma ação manual.
- **O serviço é criado pelo `deploy/gcp/setup.sh`, com a imagem pública de exemplo do Cloud Run.** Isso
  permite dar à conta de publicação `roles/run.developer` só no serviço, e não no projeto, e deixa o acesso
  público (`allUsers` como invocador) fora do alcance do pipeline. Até a primeira publicação, a URL serve a
  página de exemplo.
- **Métricas fechadas na URL pública** (análise, achado S1; decisão do responsável). A publicação define
  `MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=health`, e `/actuator/prometheus` responde 404 (FR-004a).
  Recusado expor as métricas como no modo padrão local: com a autenticação desligada, elas ficariam abertas
  na internet, sem um Prometheus para coletá-las.
- **`google-github-actions/deploy-cloudrun` não é usada.** Os passos `gcloud` explícitos permitem implantar
  sem tráfego, conferir e só então trocar o tráfego. Ficaram a `auth` (v3.0.0) e a `setup-gcloud` (v3.0.1),
  além da `download-artifact` (v8.0.2), todas fixadas por hash e sem `uses:` aninhado (research R2).
- **Identificadores públicos no `env` do job, e não em variáveis do repositório** (research R10). Número do
  projeto, provedor de identidade e e-mails das contas não são segredos. No workflow, ficam versionados e
  visíveis na revisão, sem passo manual no GitHub.
- **A política do workflow foi ampliada de forma estreita** (FR-012):
  - `id-token: write` e o `skopeo` passam a ser permitidos só no job `deploy`;
  - duas regras novas exigem, no job, os 10 checks obrigatórios, a concorrência sem cancelamento e nenhum
    build;
  - a concorrência do topo não pode cancelar sempre;
  - a proibição de `secrets.` não mudou.
- **Retenção do registro de 30 dias, mantendo as 10 imagens mais recentes** (análise, achado C1). A regra de
  apagar só as imagens sem tag não bastaria, porque toda imagem recebe a tag do commit.
- **Conferências locais reduzidas, com a política conferida antes do push** (decisões do responsável, durante
  a implementação).
  - Localmente, cada etapa rodou só o `./mvnw verify` padrão. O `./mvnw -Pextras verify` e o teste local do
    `verify-deployment.sh` (T021) ficaram para o pipeline. O script é comprovado pelas duas conferências do
    job `deploy`.
  - A política do workflow (T018 e T026) foi conferida antes do push, a pedido do responsável. Ela é a
    proteção que impede outro job de obter a identidade do GCP.
  - A conferência rodou num contêiner `alpine:3.22` com o `yq` 4.47.2 (mikefarah) e o `bash` do `apk`, sobre o
    commit `6e876e0`:
    - o `check-workflows.sh` passou no HEAD;
    - os 7 casos negativos do contrato e 5 casos extras reprovaram, cada um com a mensagem da sua regra. Os
      extras são `write-all` num job, `id-token: write` num job novo, cancelamento no job `deploy`,
      `docker build` e `docker push` no `deploy`;
    - o mesmo `if` do `deploy`, entre `${{ }}` e com espaços a mais, passou (normalização).
  - O resultado está em
    [`specs/005-deploy-cloud-run/evidencias/politica-workflow.txt`](../../specs/005-deploy-cloud-run/evidencias/politica-workflow.txt),
    com o roteiro usado.
- **No Windows, o `gcloud` em bash precisa do `CLOUDSDK_PYTHON`** apontando para o Python do próprio SDK. Sem
  ele, o atalho `python` da Microsoft Store responde no lugar, e o comando falha. O `setup.sh` define a
  variável quando ela falta (research R9).

## 2026-11-26 — Proteção da main depois do Marco 4

O ruleset `main` está ativo para a branch padrão, com a lista de bypass vazia. Para concluir o T064
(FR-031), foram adicionados os checks de alertas do SLO e de carga aos oito do Marco 3. Os dez checks
obrigatórios têm o GitHub Actions como origem:

| Job | Check obrigatório no GitHub |
| --- | --- |
| `workflow-policy` | Política do workflow, do Dependabot e do compose |
| `build` | Build, testes e cobertura |
| `postgres` | Testes com PostgreSQL |
| `codeql` | Análise de segurança do código (CodeQL) |
| `dependencies` | Vulnerabilidades das dependências (Trivy) |
| `dockerfile-lint` | Lint do Dockerfile (hadolint) |
| `image` | Imagem, vulnerabilidades da imagem e teste de fumaça |
| `compose` | Compose com PostgreSQL, Kafka, Prometheus e Grafana |
| `alert-rules` | Regras de alerta do SLO (promtool) |
| `load` | Teste de carga (k6) e carga inicial com o CSV gerado |

Após salvar o ruleset, os dez apareceram como obrigatórios no
[PR #12](https://github.com/andersonfelipediefenbach/outsera/pull/12/checks) e passaram no commit
`3d83d07`, tanto na [execução do PR](https://github.com/andersonfelipediefenbach/outsera/actions/runs/45836107162)
quanto na [execução do push](https://github.com/andersonfelipediefenbach/outsera/actions/runs/45836092814).

## 2026-11-26 — T012 não executado: verificação do job de dependências

- **Decisão do Anderson:** o T012 da spec 004 não é executado. Ele previa um branch temporário com o
  `lz4-java` 1.11.2, publicado só para ver o job `dependencies` ficar vermelho no pipeline (FR-034).
- **Comprovação que fica no lugar,** toda local, com o Trivy 0.70.0 da action e a mesma porta do pipeline:
  - **T003:** no commit `011eeba`, com o `lz4-java` 1.11.2 efetivo, o Trivy em `rootfs` sobre os JARs de
    runtime copiados pelo Maven (`target/runtime-deps`) saiu com **1**, com a CVE-2026-106451 no
    `lz4-java` e a CVE-2026-108106 e a CVE-2026-93451 no `snappy-java` 1.1.10.7;
  - **T007:** no HEAD, a mesma porta saiu com **0**, e o inventário JSON trouxe o `lz4-java` 1.11.4 e o
    `snappy-java` 1.1.10.12. A expressão `jq` do passo de inventário foi testada, inclusive com um pacote
    ausente, que cai no ramo de falha;
  - **pipeline:** o job `dependencies` passa a rodar com o inventário efetivo e mostra as duas versões no
    resumo (SC-010).

  O registro completo está na entrada "Job de dependências: causa da lacuna das transitivas do
  kafka-clients".
- **O que não fica comprovado:** só a execução do job vermelho no próprio GitHub Actions. O comando, a
  versão do Trivy e a porta são os mesmos do job, então o risco é a diferença de ambiente, e não a lógica.
- **Branch `verificacao/trivy-transitivas`:** ficou só local, com o commit `3c1fc42`, sem push. Nenhum
  commit dele entrou no `marco/4-acabamento`.

## 2026-11-26 — Marco 4 com escopo reduzido

- **Decisão do Anderson:** o Marco 4 fecha com as etapas 1 a 3, e as etapas 4, 5 e 6 ficam adiadas.
- **Entra:**
  - etapa 1: pendências do Marco 3, inventário efetivo no job de dependências e testador adversário;
  - etapa 2: gerador de CSV e teste de carga;
  - etapa 3: SLO e alertas;
  - a página do SLO e o runbook (T059 e T060), porque os alertas apontam para as âncoras do runbook;
  - a seção do Marco 4 no README, com o roteiro do que ficou para depois (T062, reduzido).
- **Fica adiado,** com a spec e as tarefas guardadas para um marco futuro:
  - etapa 4: cache AOT da JVM na imagem (US4, FR-020 a FR-023);
  - etapa 5: SBOM e assinatura keyless (US5, FR-024 a FR-027);
  - etapa 6: dev container (US6, FR-028 a FR-030) e os ADRs 0020 a 0025 (FR-037). As decisões desses temas
    já estão neste registro.
- **Consequências:**
  - os checks obrigatórios da `main` (FR-031, T064) passam a ser 10: os 8 do Marco 3, `alert-rules` e
    `load`;
  - a conferência do pipeline (T063) vale para esses 10 jobs;
  - o T012 não é executado (entrada "T012 não executado: verificação do job de dependências").
  - a spec 004 registra a decisão em Clarifications e no Status, e as tarefas adiadas estão marcadas como
    ADIADO no `tasks.md`.

## 2026-11-26 — Medição do teste de carga e folga dos limites do pipeline

T025, T033 e T034 da spec 004 (FR-011a; research R5). Medição no job `load`, em runners `ubuntu-latest`
compartilhados, com os 100 mil filmes gerados e os limites provisórios.

- **Execuções**, todas no commit `38334af`, todas verdes e sem nenhum erro HTTP ou de contrato:

  | Execução | Intervalos p95 / p99 | Listagem p95 / p99 | Carga direta | Carga por Kafka |
  | --- | --- | --- | --- | --- |
  | push [45826809441](https://github.com/andersonfelipediefenbach/outsera/actions/runs/45826809441) | 0,78 / 1,04 ms | 9,23 / 12,09 ms | 20,1 s | 148,4 s |
  | PR [45826811997](https://github.com/andersonfelipediefenbach/outsera/actions/runs/45826811997), tentativa 1 | 0,95 / 1,34 ms | 10,13 / 13,15 ms | 18,3 s | 192,6 s |
  | PR 45826811997, tentativa 2 | 1,40 / 2,02 ms | 10,00 / 12,40 ms | 16,6 s | 108,4 s |
  | **menor / mediana / maior** | p95 0,78 / 0,95 / 1,40; p99 1,04 / 1,34 / 2,02 | p95 9,23 / 10,00 / 10,13; p99 12,09 / 12,40 / 13,15 | 16,6 / 18,3 / 20,1 s | 108,4 / 148,4 / 192,6 s |

  A vazão foi de cerca de 44,4 req/s nos intervalos e 8,9 req/s na listagem, medida sobre os 2 min 15 s
  do teste. As taxas pedidas são 50 e 10 req/s nos 2 min sem o aquecimento.
- **Por que 3 execuções, e não as 5 da R5:** decisão do Anderson. Com o cache dos intervalos (ADR 0026),
  o maior p99 dos intervalos foi 2,02 ms, contra o objetivo de cerca de 112 ms, com folga de mais de 50×.
  A dispersão de cada percentil (maior sobre menor) ficou abaixo de 2×: 1,79× no p95 e 1,94× no p99 dos
  intervalos, e 1,10× e 1,09× na listagem. Nenhuma condição de parada do T025 disparou.
- **Objetivos do SLO,** que não mudam: disponibilidade de 99,5% e 99% das requisições dos intervalos até o
  bucket `le="0.111848106"` (cerca de 112 ms, confirmado no T026), em 28 dias.
- **Limites finais do k6** (`load/intervals.js`), amarrados ao objetivo do SLO por decisão do Anderson:

  | Limite | Pior medição | Limite |
  | --- | --- | --- |
  | intervalos p99 | 2,02 ms | abaixo de 111,848 ms, o bucket do alerta de latência |
  | listagem p99 | 13,15 ms | abaixo de 100 ms |
  | erro HTTP e de contrato | 0 | abaixo de 1% |

  O p95 continua no resumo do job, sem reprovar.
- **Por que não 2× a pior medição,** a regra original da R5: com medianas abaixo de 1 ms, 2× a pior
  medição dariam limites de 3 e 5 ms nos intervalos, e isso só mediria o ruído do runner compartilhado,
  como uma pausa de GC ou um vizinho barulhento. O job de carga existe para pegar **regressões reais**,
  como a perda do cache dos intervalos, que levou o p99 a 927 ms com 100 mil filmes, e não variações de
  milissegundos. Com o limite no objetivo do SLO, o job reprova quando o objetivo estaria em risco.
- **Prova de que os limites reprovam o job** (SC-004), na máquina local, com o compose e o k6, feita com
  a versão anterior do roteiro, que ainda tinha limites de p95:
  - `K6_P95_INTERVALS_MS=1`: o k6 saiu com 99, com o limite dos intervalos ultrapassado. O mesmo mecanismo
    vale hoje com `K6_P99_INTERVALS_MS`;
  - k6 com um token errado e o `app` com outro: o k6 saiu com 99, com os 401 contados como erro HTTP e como
    erro de contrato.

## 2026-11-26 — Pipeline do commit 127584c: job load e job compose

Execuções 45813784469 e 45814146445, diagnosticadas pelos logs que o Anderson enviou.

- **Job `load`, nas duas execuções:** o passo do script levou cerca de 21,5 min. A geração passou (os dois
  SHA-256 iguais ao registrado no Windows), mas a carga direta e a carga por Kafka falharam, cada uma com
  "sem readiness 200 em 600 s". No passo 4, o compose não subiu o k6 porque "container awards-app-1
  exited (1)": a aplicação terminava na subida, e o script esperava o prazo inteiro sem perceber.
- **Causa provável:** o gerador cria o arquivo com `Files.createTempFile`, que no Linux dá a permissão
  `rw-------`, só para o dono. O `ATOMIC_MOVE` mantém essa permissão. A aplicação roda no container com o
  usuário 10001 e não consegue ler o `movies.csv` montado, então a carga falha na subida ("o caminho não é
  um arquivo legível") e a aplicação sai com 1. No Windows não há permissão POSIX, e tudo passava. Os
  dois caminhos de carga usam o mesmo arquivo.
- **Correção:**
  - o gerador deixa o arquivo com `rw-r--r--` quando o sistema de arquivos tem permissões POSIX. O
    conteúdo e o SHA-256 não mudam;
  - o `load-test.sh` para a espera da readiness assim que o container sai e mostra o fim do log dele no
    log do job. Uma falha assim leva segundos, e não 10 minutos.
- **Tamanho do arquivo:** a carga nunca chegou a começar no runner, então ainda não há medição dos 100 mil
  filmes lá. O tamanho não mudou.
- **Job `compose`, só em 45814146445, em 24 s:** falhou no build da imagem, no passo do Dockerfile que baixa
  as dependências do Maven Central (`dependency:go-offline`, saída 1). Na outra execução do mesmo commit, o
  mesmo build passou, e o job ficou verde em 3 min 38 s. Falha passageira de rede ou de limite do Maven
  Central. Nada foi mudado por ela.

## 2026-11-26 — Intervalos guardados entre as leituras, invalidados a cada gravação

- **Falha encontrada no T021 da spec 004:** com os 100 mil filmes gerados, o endpoint de intervalos deu
  p95 de 227 ms e p99 de 927 ms no k6, na máquina local. O objetivo de latência do SLO é 99% até o bucket
  de cerca de 112 ms (research R1), e o T025 manda parar se o maior p99 passar dele.
- **Opções descartadas pelo Anderson:** manter o objetivo e otimizar só o cálculo, trocar o bucket ou
  medir com um arquivo menor. A decisão foi corrigir o endpoint.
- **Causa:** o endpoint recalculava tudo a cada chamada. Ele lia todas as vitórias (`findWins`) e montava
  os intervalos, embora o resultado só mude quando os dados mudam.
- **Correção (`ProducerIntervalsCache`):** o resultado calculado fica guardado e é invalidado a cada
  gravação.
  - **Onde se invalida:** a carga direta do CSV (`importMovies`) e cada evento do Kafka gravado ou
    atualizado (`record`) chamam `registerWrite()` dentro da sua transação. Com isso, há uma gravação em
    andamento até o fim dela, com commit ou rollback (`afterCompletion`). Uma geração avança no começo e no
    fim de cada gravação.
  - **Consistência imediata:** com uma gravação em andamento, a leitura vai direto ao banco e não guarda
    nada, então vê exatamente o que o banco mostra. Fora disso, ela serve o resultado guardado só se ele é
    da geração atual, e só guarda um resultado novo se nenhuma gravação começou ou terminou durante o
    cálculo. Assim, depois de uma gravação confirmada, nenhuma leitura recebe um resultado anterior a ela,
    mesmo na janela entre o commit no banco e o fim da transação.
  - **Por que em memória, e não numa tabela de versão:** a tabela exigiria a migração V2, e o
    `SchemaPostgresIT` (da `v0.3.0`) confere que o histórico do Flyway tem só a versão 1. Alterar esse IT
    seria mexer num teste que o `check-unchanged-tests.sh` protege.
  - **Uma única instância:** o estado em memória vale para uma instância, que é o caso do modo padrão e do
    compose. Com várias instâncias (Marco 5, Cloud Run), a invalidação precisa passar a um contador de
    versão no banco ou a um evento compartilhado, como registra o
    [ADR 0026](../adr/0026-intervalos-guardados-entre-leituras.md).
  - **ETag:** continua igual, calculado pelo corpo da resposta.
- **Comprovação:**
  - `KafkaIntervalsCacheIT`, novo: lê duas vezes, grava dois vencedores por evento e, assim que a listagem
    os mostra, lê de novo, sem esperar. O resultado e o ETag já são os novos. Depois, uma atualização
    desfaz a mudança, e o resultado e o ETag voltam aos de antes. **Com o `registerWrite()` desligado, o
    IT reprova** em "min logo depois da gravação";
  - `KafkaUpdateIT`, `KafkaNewWinnerIT`, `KafkaIdempotencyIT`, `ReferenceFileIT`, `RandomizedOracleIT`
    e os ITs de ETag: verdes;
  - `./mvnw clean verify` verde (209 testes, 79 s) e `./mvnw -Pextras verify` verde (209, 61 com 5
    pulados, e 85; cobertura de 93,30%; 255 s);
  - **um `-Pextras` vermelho, descartado:** depois da prova de mutação, o arquivo foi restaurado de uma
    cópia com data anterior à classe compilada, e a compilação incremental manteve o bytecode mutante.
    Sete ITs de Kafka reprovaram por isso, com intervalos antigos. Um registro em memória do cache mostrou
    que a invalidação estava certa, e o `clean verify` confirmou. Daqui em diante, toda prova de mutação
    termina com `./mvnw clean`.
- **Medição** (k6 no compose, com os 100 mil filmes gerados, na máquina local, com os limites provisórios):

  | Endpoint | Antes (p95 / p99) | Depois (p95 / p99) |
  | --- | --- | --- |
  | intervalos | 227 ms / 927 ms | **1,54 ms / 1,84 ms** |
  | listagem | 104 ms / 732 ms | 13,1 ms / 16,4 ms |

  Nos dois, nenhum erro HTTP nem de contrato, e o k6 saiu com 0. A listagem melhorou porque os intervalos
  deixaram de disputar a CPU. Os números do pipeline vêm do T025.

## 2026-11-25 — Emenda 2.0.0 da constituição: `./mvnw verify` só com os testes do modo padrão

- **Origem:** com os ITs dos Marcos 2 a 4, o `./mvnw verify` padrão chegou a cerca de 8 min nas medições
  mais lentas da máquina local (540 s no último verde). É o comando que o avaliador roda para o core.
- **Decisão do Anderson:**
  - o `./mvnw verify` padrão roda só os ITs do modo padrão, numa única execução do Failsafe;
  - os ITs do que vem desligado por padrão e a execução com o log em JSON vão para o perfil Maven `extras`
    (`./mvnw -Pextras verify`), sem Docker e só com o JDK. O job `build` roda esse perfil
    obrigatoriamente;
  - a cobertura mínima de 90% é conferida nesse perfil, sobre a soma das execuções.
- **Versão:** o pedido era uma emenda MINOR do Princípio VI. A mudança atinge também o Princípio II, que
  exigia que "os testes do enunciado e dos extras da API" passassem com `./mvnw verify`. Levada a
  escolha ao Anderson, ele decidiu por **MAJOR, 2.0.0**, porque essa garantia literal deixa de valer.
- **Critério do recorte** (ajustado pelo Anderson, no lugar do primeiro recorte, que era só o Marco 1):
  - **perfil padrão:** os ITs de todo comportamento do modo padrão, que roda sem nenhuma configuração.
    São o H2, a carga do CSV com todas as regras (inclusive as da spec 003, como o `FIELD_TOO_LONG` e o
    `NUL_CHARACTER`), os endpoints, o Swagger e o que vem ligado por padrão;
  - **perfil `extras`:** o que vem desligado por padrão, ou seja, Kafka, autenticação ligada, log em JSON
    e observabilidade que dependa de configuração. O PostgreSQL continua no `postgres-it`.
- **Lista do perfil padrão** (`src/test/default-mode-its.txt`, 63 ITs). A execução padrão usa a lista
  como `includesFile`, e a execução `extras` a usa como `excludesFile`, então todo IT roda numa execução
  só. As bases abstratas ficam de fora, porque o Failsafe não as roda.
  - os 46 ITs do Marco 1 (tag `v0.1.0`);
  - `FieldTooLongIT` e `NulCharacterIT`, as regras do CSV da spec 003;
  - `etag/*`, `health/*` e `MetricsIT`: o ETag, as sondas e o `/actuator/prometheus` vêm ligados sem
    configuração (`exposure: health,prometheus`);
  - `TextLogFormatIT`: o log em texto é o padrão;
  - `ApiDocsSecurityIT`: o contrato que o Swagger serve no modo padrão;
  - `AuthDisabledByDefaultIT`, `CacheHeadersIT`, `NoGeneratedPasswordIT` e `DefaultModeIsolationIT`:
    o comportamento do modo padrão sem token, sem senha gerada e sem Kafka.
- **No perfil `extras`:** `kafka/**` (inclusive os ITs do testador adversário), `AuthEnabledIT`,
  `AuthProblemDetailsIT`, `AuthContractIT`, `CacheHeadersWithTokenIT`, `TokenNotLoggedIT` e
  `JsonLogFormatIT`, mais a execução `json-logs`.
- **O que não muda:** nenhum IT foi alterado, só o perfil em que roda. O `check-unchanged-tests.sh` contra
  a `v0.2.0` e a `v0.3.0` continua valendo, e passa. O perfil `postgres-it` não muda.
- **Atualizados:** constituição (II, VI e o portão de merge), `pom.xml`, job `build` do `ci.yml`,
  `AGENTS.md`, README (seções 2 e 5, tabela do pipeline e estrutura) e a spec 004 (Clarifications,
  FR-001, FR-035, FR-038, SC-001, plano e tarefas). Os ADRs 0001, 0004, 0010, 0012 e 0019 falam do
  `./mvnw verify` da época e ficaram como registro histórico.
- **Medição** (mesma máquina, sem Docker, todas verdes):

  | Comando | Onde | Tempo | Testes |
  | --- | --- | --- | --- |
  | `./mvnw verify` antes da emenda | cópia de trabalho compilada | 266 a 540 s | 269 (5 pulados) e 85 |
  | `./mvnw verify` com o primeiro recorte (só o Marco 1) | clone limpo, com a compilação | 64 s | 142 |
  | `./mvnw verify` com o critério do modo padrão | **clone limpo**, com a compilação | **63 s** | 209 |
  | `./mvnw verify` com o critério do modo padrão | cópia de trabalho | 69 s | 209 |
  | `./mvnw -Pextras verify` com o critério do modo padrão | cópia de trabalho compilada | 247 s | 209, 60 (5 pulados) e 85 |

  Os 209 e os 60 somam os 269 de antes, e os 85 da execução com o log em JSON são os mesmos. A
  cobertura de linhas no `-Pextras` ficou em 95,36%. O `./mvnw -Ppostgres-it verify` não muda.

## 2026-11-25 — Publicador Kafka: envio assíncrono, com o marcador por último

- **Falha encontrada pelo T021 da spec 004:** no compose, a carga por Kafka dos 100 mil filmes gerados
  levou cerca de 611 s, do início do container `app` à readiness 200. A espera do `load-test.sh` é de
  600 s, e o runner compartilhado costuma ser mais lento.
- **Causa:** o publicador enviava um evento por vez e esperava a confirmação de cada um (`send().get()`,
  com `acks=all`), cerca de 166 eventos por segundo. O consumidor acompanhava logo atrás.
- **Correção, pedida pelo Anderson:**
  - **filmes:** envio assíncrono, em blocos de 10 mil. O publicador junta as confirmações de cada bloco e
    espera todas antes de seguir. O `flush` do produtor só volta quando todos os registros terminaram;
  - **ordem:** o produtor do publicador é idempotente (`enable.idempotence=true`, `acks=all`), e o tópico
    tem uma partição;
  - **marcador:** continua por último, de forma síncrona, e só sai depois de todos os filmes confirmados;
  - **produtor próprio:** o `linger.ms` (20) e o `batch.size` (256 KiB) valem só para o publicador, numa
    cópia da fábrica do Boot (`copyWithConfigurationOverride`). O produtor do consumidor, que envia ao
    tópico de erros, não muda.
- **Falha de envio, com a spec 003:** uma tentativa em que algum envio falha não publica o marcador, e a
  readiness fica negativa. Depois da espera crescente, a tentativa seguinte recomeça do primeiro evento sem
  confirmação. Os eventos reenviados caem na gravação idempotente por ano e título (FR-014). A falha não
  encerra o publicador de vez, porque o caso de borda "Broker indisponível" da spec 003 exige que a
  aplicação continue viva e publique quando o broker voltar (`KafkaBrokerLateStartIT` e
  `KafkaBrokerUnavailableIT`).
- **Medição local**, no compose, com a mesma imagem de base e o mesmo CSV de 100 mil filmes:

  | | Publicador (fim da publicação) | Carga por Kafka (início do `app` à readiness 200) |
  | --- | --- | --- |
  | Antes | cerca de 600 s | cerca de 611 s |
  | Depois | **cerca de 2 s** | **366 s** |

  Nas duas, 100000 lidas, 100000 publicadas, 0 rejeitadas, 100000 filmes na listagem e os intervalos
  plantados.
- **Gargalo que sobra:** o consumidor, com uma transação e uma confirmação de offset por evento
  (`ack-mode: record`), cerca de 290 eventos por segundo. Mudar isso muda a garantia de "um evento
  inválido não trava o consumo" e a nova tentativa sem avançar o offset (spec 003, FR-015 e FR-015a), e
  fica fora desta correção. Se o job `load` passar da espera no runner, a decisão volta ao Anderson.
- **Comprovação:** os 39 ITs de Kafka verdes (e os 17 da execução com log em JSON), inclusive o
  `KafkaBrokerLateStartIT`, o `KafkaBrokerUnavailableIT`, o `KafkaReadinessIT`, o `PublisherSummaryIT` e os
  casos do marcador do testador adversário. `./mvnw -Ppostgres-it verify` verde (9 testes, com o
  `RestartPostgresIT` e o caso 29). `./mvnw verify` completo verde.

## 2026-11-25 — Carga direta: flush automático a cada consulta, com custo quadrático

- **Falha encontrada pelo T016 da spec 004:** com o CSV gerado de 100 mil filmes, a carga direta
  (`./mvnw spring-boot:run`) passou de 20 minutos sem terminar, com a CPU ocupada o tempo todo.
- **Causa:** a carga grava o arquivo inteiro numa única transação (`MovieImportService.importMovies`), e
  cada linha faz um `findByYearAndTitle`. Com o modo de flush padrão (`AUTO`), o Hibernate sincroniza o
  contexto de persistência antes de cada consulta e confere todas as entidades já carregadas. Com o
  contexto crescendo a cada linha, o custo total é quadrático no número de filmes. Os 206 filmes do
  `movielist.csv` não deixavam isso aparecer.
- **Correção, feita pelo Anderson:** durante a carga direta, o flush passa a `FlushModeType.COMMIT`, e o
  modo anterior volta no fim. A consulta por linha não sincroniza mais o contexto, e tudo é gravado no
  commit da transação.
- **Por que a correção é segura:**
  - a duplicata do arquivo não depende do banco. O `MovieCsvReader` a descarta antes, pelo mapa em memória
    das chaves aceitas (spec 001, FR-006d), então a consulta nunca precisa ver um filme ainda não gravado
    da mesma carga;
  - os produtores criados na carga ficam no mapa `byName` da transação;
  - num reinício sobre o PostgreSQL, os filmes de antes já estão no banco, e a consulta continua os vendo;
  - o caminho do Kafka grava cada evento na sua transação, com o flush padrão, e não muda.
- **Comprovação:**
  - `DuplicateLinesIT` (nas duas execuções do Failsafe), `DuplicateWinnerIT`, `ReferenceFileIT`,
    `KafkaIdempotencyIT`, `KafkaUpdateIT`, `KafkaEquivalenceIT`, `KafkaReferenceFileIT` e
    `KafkaAdversarialEventsIT`: verdes;
  - `./mvnw -Ppostgres-it verify`, contra um PostgreSQL 18 local descartável: 9 testes verdes, com o
    `RestartDirectPostgresIT` e o caso 29;
  - `./mvnw verify` completo: verde.
- **Medição local** (do início da JVM ao fim da carga, JAR já construído, mesma semente do gerador):

  | Filmes | Antes | Depois |
  | --- | --- | --- |
  | 5 mil | 15,2 s | 11,9 s |
  | 10 mil | 40,7 s | 11,8 s |
  | 20 mil | 199,8 s | 13,1 s |
  | 100 mil | mais de 20 min (interrompida) | 19 s até a readiness 200; 24,5 s no container |

  Com os 100 mil filmes, a carga deu 100000 lidas, 100000 aceitas e 0 rejeitadas, com `min` e `max`
  exatamente iguais aos casos plantados pelo gerador.
- **Por que mexer em `src/main` no Marco 4:** o plano só previa mudanças em `src/main` pelo testador
  adversário. Esta é uma falha de desempenho que o T016 encontrou, e sem a correção o teste de carga do
  Marco 4 não roda. O comportamento observável não muda.

## 2026-11-25 — Job de dependências: causa da lacuna das transitivas do kafka-clients

Investigação da spec 004 (FR-032, research R12, T003), feita num `git worktree` temporário fora do
repositório, no commit `011eeba`. É o anterior à fixação das versões (`ae2ba76`), com o `lz4-java` 1.11.2
efetivo. O worktree foi removido no fim.

- **Versão do Trivy:** a `aquasecurity/trivy-action` v0.36.0 (`ed142fd`) instala o Trivy **v0.70.0**
  (input `version`, padrão `v0.70.0` no `action.yaml`). Todas as execuções abaixo usaram a imagem
  `aquasec/trivy:0.70.0`.
- **Item 1, `trivy fs --list-all-pkgs` sobre o repositório, como no job:** o inventário tem só as 14
  dependências do `pom.xml` (13 diretas e a raiz), sem nenhuma transitiva. O `lz4-java`, o `snappy-java` e
  o `kafka-clients` não aparecem. A saída foi 0.
  - Com `--debug`, o motivo aparece. O Trivy busca os POMs no Maven Central na hora da análise, a começar
    pelo pai `spring-boot-starter-parent` 4.1.2. Nas três execuções, o Central respondeu **429** (limite de
    requisições) a todas as buscas.
  - Sem o pai, as versões das diretas que vêm do BOM ficam indefinidas, e o Trivy não desce às
    transitivas. O único aviso é um `WARN` genérico ("Dependency version cannot be determined. Child
    dependencies will not be found"), com o link
    <https://trivy.dev/docs/v0.70/guide/coverage/language/java#empty-dependency-version>. As falhas de busca
    só aparecem em `DEBUG`.
  - **Contraprova:** o mesmo comando com o `~/.m2` local montado e `--offline-scan` não faz nenhuma busca.
    Ele lista 122 pacotes, entre eles `lz4-java` 1.11.2, `snappy-java` 1.1.10.7 e `kafka-clients` 4.2.2,
    com as CVE-2026-106451, CVE-2026-108106 e CVE-2026-93451. Quando tem os POMs, o Trivy chega às mesmas
    versões do build.
- **Item 2, versões efetivas do Maven:** `dependency:list -DincludeScope=runtime` dá `lz4-java` 1.11.2
  (runtime), `snappy-java` 1.1.10.7 (runtime) e `kafka-clients` 4.2.2 (compile). As duas vêm do
  `kafka-clients`. O `dependency:tree -Dscope=runtime` pedido no T003 corta esse ramo e não as mostra, por
  isso o inventário foi conferido com o `dependency:list`, que usa o mesmo filtro do `copy-dependencies`.
- **Item 3, porta sobre `target/runtime-deps`:** os 115 JARs copiados pelo Maven.
  - **Com `trivy fs`, a porta não analisa nada.** O log mostra "Number of language-specific files num=0", e
    a saída é 0. No Trivy, a análise de JAR só existe nos modos `image` e `rootfs`. No `fs`, o Java coberto
    é o `pom.xml`.
  - **Com `trivy rootfs`**, com a porta do pipeline (`--severity HIGH,CRITICAL --ignore-unfixed --exit-code
    1`), a saída é **1**, com exatamente as três CVEs: CVE-2026-106451 no `lz4-java` 1.11.2, CVE-2026-108106
    e CVE-2026-93451 no `snappy-java` 1.1.10.7.
- **Causa:** a análise do `pom.xml` depende de buscas remotas que podem falhar sem reprovar o job. Quando
  uma falha, as transitivas somem do inventário e nada é apontado.
  - Isso difere da hipótese da R12, que era uma resolução de versões diferente da do build. O Trivy resolve
    certo quando tem os POMs.
  - Não há como saber qual busca falhou no runner do Marco 3, porque o log do job não tem `DEBUG`. O efeito
    é o mesmo: o job ficou verde sem ver as transitivas.
  - A R12 foi atualizada com o resultado e com a troca de `fs` por `rootfs`.
- **Correção (T006):** o job `dependencies` passa a analisar o que o Maven copia, com
  `dependency:copy-dependencies -DincludeScope=runtime` e o Trivy em `rootfs`. O inventário não depende da
  rede, porque o Maven já baixou os JARs. Um passo confere no resumo que `lz4-java` e `snappy-java` estão no
  inventário e falha se algum faltar.
- **Conferência no HEAD (T007):** com o `pom.xml` atual, a mesma porta em `rootfs` sai com 0. O inventário
  JSON tem `at.yawk.lz4:lz4-java` 1.11.4 e `org.xerial.snappy:snappy-java` 1.1.10.12. A expressão `jq` do
  passo novo foi testada sobre esse inventário, inclusive com um pacote ausente, que cai no ramo de falha.
  A comprovação contra uma versão vulnerável no pipeline é a verificação única do T012 (FR-034), que
  depende do push.

## 2026-11-25 — Decisões do plano do Marco 4

Decisões do responsável sobre o plano da spec 004 (`plan.md`, "Decisões do responsável sobre o plano"),
mais as duas clarificações da spec:

1. **Limite do SLO de latência:** é o bucket que já existe no histograma, cerca de 112 ms
   (`le="0.111848106"`, a conferir no T026), sem mudar o `application.yml` (R1).
2. **Folga do pipeline:** os limites do k6 são 2× a maior medição de cada percentil, arredondados para cima
   (R5).
3. **Execuções de medição:** as 5 execuções são feitas com "Re-run jobs" do job `load` no mesmo commit, sem
   commits vazios (R5).
4. **Etapas:** cada etapa do bolt termina com o `./mvnw verify` verde e um commit.
5. **Clarificação: cache só da JVM.** O cache AOT é só o da JVM, gerado por uma execução de treino no build
   da imagem. O processamento AOT do Spring fica de fora, porque fixaria as condições de perfil no build. A
   escolha de perfil continua na execução, e a mesma imagem serve ao modo padrão e ao compose (US4-6,
   FR-020).
6. **Clarificação: assinatura num registro temporário.** Sem publicar a imagem, ela vai a um registro
   temporário dentro do job, é assinada lá e recebe o SBOM como atestado assinado. O registro é descartado
   no fim do job. Só a entrada no registro de transparência é pública (US5-6, FR-025).

## 2026-11-25 — Proteção da main ao fim do Marco 3

Configuração da proteção da `main` feita pelo Anderson depois do merge do Marco 3 (T093 da spec 003; spec
004, FR-031, R14). Está registrada aqui como ponto de partida. A lista com os 12 checks do Marco 4 é o
T064, na entrada "Proteção da main depois do Marco 4".

- **Mecanismo:** um ruleset **ativo**, aplicado à branch padrão (`main`).
- **Bypass:** nenhum *bypass actor*. Ninguém, nem administrador, passa por cima das regras.
- **Pull request obrigatório,** com **0 aprovações** exigidas. A revisão do Codex fica nos comentários do
  PR, e o repositório tem um único responsável humano.
- **Resolução das conversas obrigatória** antes do merge.
- **Checks obrigatórios:** os 8 jobs do Marco 3, que são `workflow-policy`, `build`, `postgres`, `codeql`,
  `dependencies`, `dockerfile-lint`, `image` e `compose`.
- **Branch atualizado antes do merge:** não é exigido.
- **Force push:** bloqueado.
- **Exclusão da branch:** bloqueada.

## 2026-11-25 — Testador adversário: ingestão por Kafka

T005 da spec 004 (FR-035, R13; T050 da spec 003). O Anderson acionou o Codex como testador adversário
da ingestão por Kafka. O Codex escreveu 30 casos a partir da spec 003, do contrato dos eventos, da spec
001, do data-model da spec 001, do OpenAPI e do enunciado, sem ler `src/main`. Os casos ficaram fora do
repositório. Todos os casos viraram ITs permanentes em `src/test/java/com/outsera/awards/kafka/adversario/`,
com o `@DisplayName` começando por `S004 FR-035`. Nenhum caso foi cortado.

**Como os casos foram agrupados.** A consolidação segue a do `KafkaInvalidEventsIT` do Marco 3 e foi
pedida pelo Anderson.
- **`KafkaAdversarialEventsIT`:** 24 casos numa única aplicação e num único broker, um depois do outro
  (01 a 21, 27, 28 e 30). São os que não mexem no marcador, na readiness nem na subida.
  - Cada caso tem chaves (`cNN/...`) e títulos (`CNN ...`) próprios. Só ficam sem prefixo os títulos que
    são o próprio dado testado (500 letras, emojis, só NBSP), e eles já são únicos.
  - Cada conferência é uma `SoftAssertions` com a mensagem do caso. Uma exceção num caso vira falha dele,
    e os seguintes rodam.
  - O caso confere os filmes novos ou alterados desde o início dele, que nenhum filme anterior mudou, e os
    registros novos do tópico de erros.
  - No fim de cada caso, todo vencedor gravado é reenviado como indicado, pelo próprio caminho de
    atualização (spec 003, FR-014). Assim, `min` e `max` continuam conferidos exatamente por caso.
- **Contexto próprio:** só os casos que mexem no marcador ou na readiness (22, 23, 24, 25 e as três
  variantes do 26, uma classe cada) e o que mexe na subida (29, no PostgreSQL).
- **Prova de que o agrupamento não enfraqueceu o teste:** com as correções de `src/main` guardadas num
  `git stash`, o `KafkaAdversarialEventsIT` reprovou exatamente os casos 05, 11, 13 e 17, cada um com as
  mensagens dele. Os outros 20 passaram, sem efeito em cascata. Depois, as correções voltaram.
- **Medição local** (`./mvnw verify` sem `clean`, na mesma máquina e no mesmo dia, todas verdes). A
  referência do limite de 2× da research R15 da spec 003 é o tempo do job `build` no pipeline, e não a
  máquina local (decisão do Anderson). Os números abaixo servem para comparar a consolidação:

  | Execução | Tempo | Testes |
  | --- | --- | --- |
  | HEAD do Marco 3, sem os ITs novos (T001 e T010 da spec 004) | 233 a 252 s | 261 (5 pulados) e 85 |
  | Com os ITs novos, um contexto por caso (31 classes) | 563 s | 292 (5 pulados) e 85 |
  | Depois da consolidação (8 classes) | **260 s** | 269 (5 pulados) e 85 |

  - Os ITs do testador somam cerca de 69 s no verify: 39 s do `KafkaAdversarialEventsIT` com os 24 casos,
    contra cerca de 10 s **por caso** com contexto próprio, e de 3,4 s a 6,4 s para cada uma das sete
    classes do marcador.
  - O número de testes caiu de 292 para 269 só porque 24 classes viraram uma, com os mesmos 24 casos.
  - `./mvnw -Ppostgres-it verify` contra um PostgreSQL 18 local descartável, com a mesma imagem e a mesma
    configuração do job `postgres` (porta 55432, para não tocar outro container da máquina): 9 testes,
    verdes, em 71 s. O caso 29 levou 18,8 s.
  - A confirmação do limite fica para o tempo do job `build` na primeira execução do pipeline no
    branch.

**Como os ITs verificam.**
- Aplicação completa, broker embutido e H2, sem mocks (Princípio I). O resultado é lido por HTTP:
  listagem, intervalos, ETag e readiness.
- O tópico de erros é lido pelo broker. Para cada rejeição, o IT confere os bytes originais, o código do
  motivo, os cabeçalhos de origem, o offset do evento no tópico de filmes e o aviso no log.
- O README dos casos pedia filmes, produtores e vínculos conferidos no banco. Os vínculos aparecem nos
  intervalos, por HTTP, e o banco não é consultado.
- O fim de cada sequência é o offset que o consumidor confirmou no grupo (`ack-mode: record`), sem
  esperas fixas.
- **Retenção do marcador** (casos 22 a 26 e 29): os casos pedem para reter no transporte o marcador que
  a aplicação publica. O `HoldingProducerInterceptor`, de `src/test`, é um `ProducerInterceptor` do
  próprio Kafka, ligado só nos testes por `spring.kafka.producer.properties.interceptor.classes`. Ele
  desvia o registro, com chave, valor e cabeçalhos intactos, para o tópico `<filmes>.held`, e o teste o
  solta depois pelo broker real. Não substitui nenhuma classe da aplicação: o publicador envia como
  sempre. Por isso não conta como mock.

**Resultado de cada ataque**, rodado antes de qualquer correção ("passou" quer dizer que a aplicação já
fazia o esperado):

| Caso | Ataque | Antes | Ação |
| --- | --- | --- | --- |
| 01 | JSON truncado, vazio, `null`, array e vírgula final | passou | — |
| 02 | duas raízes JSON no mesmo valor | passou | — |
| 03 | bytes `C3 28` dentro do título | passou | — |
| 04 | ano, título, produtores e `winner` ausentes | passou | — |
| 05 | título número, produtores array, estúdios objeto, título e produtores `null` | **falhou**: o `studios` objeto era gravado como ausente, e o título número e os produtores array saíam como `TITLE_MISSING` e `PRODUCERS_MISSING` | achado decidido; corrigido |
| 06 | `winner` `"yes"`, `" YES "`, `"true"`, `"false"`, `""`, `1`, `0` e `null` | passou | — |
| 07 | ano texto, fração, `2001.0`, `2.001e3` e booleano | passou | achado (expoente) decidido sem mudança |
| 08 | anos 1 e 9999; 0, 10000, -1 e maiores que 32 e 64 bits | passou | — |
| 09 | campos extras, `type` explícito e chave alheia ou nula | passou | — |
| 10 | `type` `"Movie"`, `7` e `null` | passou | achado decidido sem mudança (`null` vale ausente) |
| 11 | propriedade `winner` repetida com valores opostos | **falhou**: valia a última ocorrência | achado decidido; corrigido |
| 12 | BOM UTF-8 antes do objeto | passou (`NOT_JSON`) | achado decidido sem mudança |
| 13 | U+0000 escapado no título, nos estúdios e nos produtores; NUL literal | **falhou**: o U+0000 nas pontas era retirado pelo `trim` e o filme gravado sem ele | conflito entre a spec 001 e o data-model 003 decidido; corrigido nos dois caminhos |
| 14 | NFC e NFD no título e no nome | passou | oráculo ajustado (abaixo) |
| 15 | título só com NBSP, produtor U+200B, Iris com NBSP, título só com espaços | passou | achado decidido sem mudança |
| 16 | título 500/501, estúdios 1000/1001, produtores 1000/1001, nome 500/501 | passou | — |
| 17 | 1, 250, 251, 500 e 501 emojis no título | **falhou**: 251 e 500 passavam na validação e o H2 recusava (`DATABASE_REJECTED`, SQLState 22001) | achado decidido; corrigido |
| 18 | `and` repetido, em caixa mista, nas pontas e embutido em nomes | passou | — |
| 19 | tabulação em volta do `and` | passou | achado decidido sem mudança |
| 20 | reentrega byte a byte antes e depois de outra vitória, com ETag | passou | — |
| 21 | atualização pelo título com espaços, troca de vínculos e replay antigo | passou | — |
| 22 | filmes do publicador com o marcador retido | passou | — |
| 23 | marcador de outra execução repetido; marcador atual repetido | passou | — |
| 24 | marcador sem `runId`; filme com a chave `load-completed` | passou | — |
| 25 | cópia do marcador atual antes do último filme | passou, como documentado (a cópia libera a readiness) | achado decidido; documentado no contrato |
| 26-A | `count` 1 com nenhum filme | passou (libera) | achado decidido sem mudança |
| 26-B | marcador sem `count` | **falhou**: liberava a readiness | achado decidido; corrigido |
| 26-C | `count` `"0"` | **falhou**: liberava a readiness | achado decidido; corrigido |
| 27 | empates em `min` e `max`, vitórias no mesmo ano e ordem Unicode | passou | — |
| 28 | atualização inválida de um filme existente | passou | — |
| 29 | reinício com o PostgreSQL: reaplica o CSV, mantém o filme externo e ignora o marcador antigo | passou | — |
| 30 | três vitórias no mesmo ano, com replay | passou | — |

**Achados de ambiguidade, decididos pelo Anderson** e escritos na spec antes do código: spec 003,
Clarifications de 2026-11-25, FR-012 e FR-013, data-model e contrato dos eventos; spec 001, FR-006, FR-007
e FR-008, e data-model.

1. **Tipos errados (05, 10):** motivo novo `FIELD_TYPE_INVALID` para `title`, `producers` ou `studios`
   presente com outro tipo que texto. `null` vale ausente em todos os campos. Assim, título e produtores
   nulos continuam `TITLE_MISSING` e `PRODUCERS_MISSING`, e `type` nulo é um filme.
2. **Rigor do JSON (07, 11, 12):** propriedade repetida é `NOT_JSON`, pela detecção estrita do Jackson. O
   BOM continua `NOT_JSON`, porque a tolerância é só do arquivo CSV. O ano com expoente continua
   `YEAR_NOT_NUMERIC`, porque só o token de inteiro é aceito.
3. **Unicode (15, 17, 19):** o tamanho é contado em unidades UTF-16, como o H2 mede, e o limite fica igual
   nos dois bancos. Os espaços em volta são os caracteres de U+0001 a U+0020; NBSP e U+200B fazem parte
   do texto. A tabulação e as quebras de linha separam o `and`.
4. **U+0000 nas pontas (13):** a spec 001 (FR-006, "contém") e o data-model da spec 003 ("removido pelo
   `trim` … não chega a ser rejeitado") se contradiziam. Vale a FR-006: o U+0000 é rejeitado em qualquer
   posição, e o data-model foi corrigido.
5. **Marcador (25, 26):** o `count` é validado como inteiro JSON ≥ 0, com o motivo novo `COUNT_INVALID`,
   mas não é barreira. O caso 25 só acontece com escrita indevida no tópico. O contrato agora diz que a
   readiness é um sinal operacional, não uma barreira de segurança, e que a proteção do tópico é do
   broker (ACLs), fora do escopo.

**Correções em `src/main`:**
- `MovieFieldRules.strip`: a regra única dos espaços em volta, usada pelo CSV e pelo Kafka;
- `MovieFieldRules.tooLong`: o tamanho passa a ser `String.length`;
- `MovieCsvReader`: sem o `setTrim` do Commons CSV, que usa o `String.trim`; os campos passam pelo
  `strip`;
- `MovieEventParser`: `STRICT_DUPLICATE_DETECTION`, `FIELD_TYPE_INVALID` e `COUNT_INVALID`;
- `EventRejectionReason`: os dois motivos novos.

Essas mudanças valem também para a carga direta. Um campo do CSV com U+0000 nas pontas, ou com 251 a 500
caracteres fora do plano básico, passa a ser rejeitado com aviso. Antes, ele era gravado sem o U+0000 ou
interrompia a carga com um erro do H2. O `movielist.csv` não tem nenhum dos dois, e o resultado de
referência não muda.

**Desvios dos oráculos dos casos:**
- Caso 14: o oráculo escreve os dois produtores como `José` em NFC. Na resposta, um deles vem em NFD. O
  NFD vem primeiro pela FR-015 da spec 001 (`e` < `é`), e o IT confere os nomes reais.
- Caso 23: o `runId` da "execução anterior" é um UUID novo, com a serialização do publicador. Para o
  consumidor, outra execução é qualquer `runId` diferente.
- Caso 29: roda no perfil `postgres-it` (`KafkaCase29RestartReappliesCsvPostgresIT`), como os outros
  testes de reinício. O H2 do modo padrão é em memória e não sobrevive ao reinício.

**Ponto em aberto, sem decisão:** no CSV, o léxico do Commons CSV (`ignoreSurroundingSpaces`) também
ignora, nas pontas, os espaços Unicode do `Character.isWhitespace`, como o EM SPACE (U+2003). No Kafka,
eles fazem parte do texto. A divergência já existia antes deste bolt e não aparece no `movielist.csv` nem
nos casos do testador (NBSP e U+200B são iguais nos dois caminhos). Por isso a spec não afirma nada sobre
esses espaços.

## 2026-11-25 — Pipeline do PR do Marco 3: endereço do broker vazando entre os testes pela propriedade de sistema

- **Falha:** no commit `230dffc`, o job `build` reprovou nos dois eventos. No `push`, só o
  `KafkaBrokerLateStartIT` falhou, em 2,4 s: o broker tardio não tinha os tópicos. No `pull_request`, ele
  falhou do mesmo jeito, e o `KafkaEquivalenceIT` também falhou, três vezes, sem readiness 200 em 60 s. As
  duas falhas acontecem na primeira execução do Failsafe, a de log em texto, com 261 testes. A execução com
  log em JSON (85 testes) passou nos dois eventos, porque não roda essas classes.
- **Causa** (a mesma nas duas classes e nos dois eventos):
  - todo `EmbeddedKafkaKraftBroker` do spring-kafka-test 4.1.2 grava o próprio endereço na propriedade de
    sistema `spring.kafka.bootstrap-servers` ao subir. O valor padrão do `bootstrapServersProperty` do
    `@EmbeddedKafka` é esse nome. O comportamento vale para todo broker embutido: os dos contextos
    `KafkaScenarioIT`, o compartilhado e os criados à mão;
  - essa propriedade de sistema vence o `bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS:...}` do
    `application-kafka.yml`. Por isso, toda aplicação que um IT sobe com `SpringApplication` se ligava ao
    último broker criado na JVM, e não ao que o teste passou em `KAFKA_BOOTSTRAP_SERVERS`;
  - `KafkaBrokerLateStartIT`: o broker do contexto do `KafkaConsumerGroupIT`, que roda antes, continuava no
    ar no cache do Spring. A aplicação criou os tópicos e publicou nele em vez de esperar o broker tardio;
  - `KafkaEquivalenceIT`: o broker compartilhado já existia, criado antes pelo `KafkaStructuralFailureIT`.
    A propriedade guardava o endereço do broker do `KafkaIdempotencyIT`, que o `@DirtiesContext` já tinha
    fechado. A aplicação nunca ficava pronta.
- **O que muda no runner:** a ordem das classes. O Failsafe roda na ordem do sistema de arquivos:
  - no Windows (NTFS), ela é alfabética. O `KafkaBrokerLateStartIT` roda antes de qualquer
    `@EmbeddedKafka`, quando a propriedade ainda não existe. O `KafkaEquivalenceIT` é o primeiro a pedir o
    broker compartilhado, e a criação dele regrava a propriedade com o endereço certo. Ele passava por
    sorte, às vezes ligado a outro broker que ainda estava no ar;
  - no runner (ext4), a ordem segue o hash dos nomes e mudou até entre os checkouts do `push` e do
    `pull_request`.

  Não é problema de porta, de paralelismo nem de prazo.
- **Diagnóstico anterior corrigido:** na rodada passada, atribuí a falha do `KafkaBrokerLateStartIT` a uma
  corrida na porta com um IT em paralelo. O log mostra as classes rodando em sequência, e a falha voltou
  com a porta reservada.
- **Correção**, sem mudar nenhum prazo:
  - `KafkaTestSupport.BROKERS_PROPERTY` (`awards.test.embedded-kafka.brokers`) recebe o endereço em vez da
    propriedade do Boot;
  - o `KafkaScenarioIT` usa `bootstrapServersProperty = BROKERS_PROPERTY`. O broker compartilhado, o
    `startBroker()` e o broker tardio passam pelo novo `KafkaTestSupport.newBroker()`, que faz o mesmo;
  - o `KafkaTestSupport.properties()` reprova com uma mensagem clara se a `spring.kafka.bootstrap-servers`
    estiver definida na JVM. Um broker novo criado sem esses cuidados quebra na hora, em vez de ligar a
    aplicação ao broker errado em silêncio.
- **Comprovação:** a falha foi reproduzida localmente, forçando a ordem com
  `-Dfailsafe.runOrder=reversealphabetical`. Sem a correção:
  - com `KafkaConsumerGroupIT` e `KafkaBrokerLateStartIT`, o segundo falha na mesma asserção, em 2,6 s;
  - com `KafkaStructuralFailureIT`, `KafkaIdempotencyIT` e `KafkaEquivalenceIT`, o último falha três
    vezes, sem readiness 200 em 60 s (193 s no total, contra 191 s no runner).

  Com a correção, as cinco classes passam nessa ordem, e o log mostra a aplicação do `KafkaEquivalenceIT`
  ligada ao broker compartilhado. O `./mvnw verify` completo passa.

## 2026-11-25 — Pipeline do PR do Marco 3: lz4-java e snappy-java com correção

- **Falha:** a porta da imagem (Trivy, alta e crítica com versão corrigida) reprovou o `54cae85` com três
  vulnerabilidades altas em bibliotecas que o `kafka-clients` 4.2.2 traz em runtime e que o BOM do Spring
  Boot 4.1.2 não gerencia:
  - `lz4-java` 1.11.2: CVE-2026-106451, corrigida na 1.11.4;
  - `snappy-java` 1.1.10.7: CVE-2026-108106 e CVE-2026-93451, corrigidas na 1.1.10.9.

  Elas entraram com a ingestão por Kafka, neste marco. O job `dependencies`, que analisa o repositório, não
  as apontou. A causa disso não foi investigada.
- **Correção:** o `pom.xml` fixa as versões no `dependencyManagement`, com a última correção de cada linha:
  `lz4-java` 1.11.4 e `snappy-java` 1.1.10.12. As versões ficam em propriedades, com um comentário para
  retirá-las quando o `kafka-clients` trouxer versões corrigidas.
- **Comprovação:** os ITs de Kafka passam no `./mvnw verify`. O Trivy 0.70.0, com a mesma porta do
  pipeline, não acha nada nas 103 bibliotecas do jar, extraídas como na imagem. Nos jars antigos, ele acha
  as três vulnerabilidades. A comprovação definitiva é o job `image` (Princípio VI).

## 2026-11-25 — Pipeline do PR do Marco 3: porta reservada no KafkaBrokerLateStartIT

> **Diagnóstico corrigido:** a causa descrita abaixo estava errada. Os ITs não rodam em paralelo, e a
> falha voltou com a porta reservada. A causa real está na entrada "Pipeline do PR do Marco 3: endereço do
> broker vazando entre os testes pela propriedade de sistema". A reserva da porta foi mantida, porque não
> faz mal e tira a janela entre escolher a porta e usá-la.

- **Falha:** o job `build` reprovou o `54cae85` no `KafkaBrokerLateStartIT`, em 2,2 s: o broker que sobe
  depois não tinha os tópicos da aplicação.
- **Causa:** uma corrida na porta, no teste, e não na aplicação. O teste escolhia uma porta livre abrindo e
  fechando um `ServerSocket`, e só ligava o repassador nela depois de subir a aplicação e o broker tardio.
  Nesse intervalo, a porta ficava livre, e o Failsafe roda outros ITs de Kafka em paralelo na mesma JVM. No
  pipeline, outro broker embutido ocupou a porta. O log mostra a aplicação criando os tópicos e publicando
  os 206 filmes nesse outro broker, 1,3 s depois de subir. Por isso a readiness ficou pronta sem o broker
  tardio, e a verificação dos tópicos falhou.
- **Correção:** o `TcpForwarder` reserva a porta desde o início (`reserve()`). Enquanto não há destino, ele
  fecha cada conexão logo depois de aceitá-la, como um serviço fora do ar. Depois, `forwardTo()` o liga ao
  broker. A porta não fica livre em nenhum momento.
- **Comprovação:** o IT passa sozinho e na suíte completa. Sem a recriação dos tópicos do apontamento 2, ele
  continua reprovando, porque a readiness não chega a 200 em 90 s. Assim o teste ainda prova o caso de
  borda "Broker indisponível".

## 2026-11-25 — Pipeline do PR do Marco 3: porta do CodeQL com exceções registradas

- **Falha:** o job `codeql` reprovou o `54cae85` com os dois alertas de severidade alta já analisados na
  entrada "Alertas do CodeQL no PR do Marco 3, dispensados como falso positivo". Dispensá-los no GitHub não
  resolve, porque a porta de severidade lê o SARIF da análise, e não o estado dos alertas.
- **Correção:** a porta passa a ser o `.github/scripts/codeql-gate.sh`, com as exceções em
  `.github/codeql-exceptions.txt`, no mesmo padrão do `unchanged-tests-exceptions.txt`:
  - cada linha tem o id da regra, o caminho do arquivo e o título da entrada do `decisoes.md` que justifica;
  - a porta ignora só os achados listados e reprova se uma exceção citar uma entrada que não existe;
  - o resumo do job mostra os achados ignorados numa tabela própria, com a justificativa.
- **Exceções registradas:** os ids e caminhos foram conferidos no SARIF da análise do PR:
  - `java/spring-disabled-csrf-protection` no `SecurityConfiguration` (severidade 8,8);
  - `java/user-controlled-bypass` no `TokenAuthenticationFilter` (severidade 7,8).
- **Comprovação local**, com o SARIF do PR: com as exceções, a porta passa e lista os dois achados como
  ignorados. Sem elas, ou com um título inexistente, ela reprova. A comprovação definitiva é a execução do
  pipeline (Princípio VI).

## 2026-11-25 — Pipeline do PR do Marco 3: byte NUL no decisoes.md e verificação nova na política

- **Falha:** o job `workflow-policy` reprovou o commit `54cae85`. A entrada do apontamento 3 tinha um byte
  NUL literal no lugar do texto `\u0000`. Com ele, o `grep` passou a tratar o `docs/ai/decisoes.md` como
  binário, e o `check-unchanged-tests.sh` não achou o título da exceção do `ApiDocsIT`.
- **Correção:** o byte virou o texto `\u0000` entre crases (commit `25e4b38`).
- **Prevenção:** o `check-text-files.sh`, num passo novo do job de política, reprova qualquer byte NUL em
  arquivo de texto versionado. Um arquivo com NUL de propósito precisa ser declarado `binary` no
  `.gitattributes` e aparece na saída do passo. Hoje só o cenário `nul-character.csv` está nessa situação.
  O contrato do pipeline da spec 003 descreve a regra.
- **Comprovação:** o script passa no HEAD e reprova o `54cae85`, apontando o `decisoes.md`. Ele foi rodado
  numa cópia de trabalho temporária desse commit.

## 2026-11-25 — Alertas do CodeQL no PR do Marco 3, dispensados como falso positivo

O job `codeql` abriu dois alertas de severidade alta no PR. O responsável os dispensa como falso positivo,
pelos motivos abaixo. Nenhum código mudou.

- **"Disabled Spring CSRF protection"** (`java/spring-disabled-csrf-protection`), no `SecurityConfiguration`:
  - o CSRF só é explorável quando o navegador anexa sozinho uma credencial ambiente (cookie de sessão ou
    autenticação Basic) a uma requisição forjada por outro site;
  - a API é sem estado, não cria sessão, não emite cookie e tem `formLogin`, `httpBasic` e `logout`
    desligados. A única credencial é o token no cabeçalho `Authorization: Bearer`, que o navegador não envia
    sozinho, e o CORS não está liberado para nenhuma origem;
  - os endpoints protegidos são só de leitura.

  A justificativa foi acrescentada ao ADR 0017, que antes só dizia "sem CSRF". O ADR também registra que o
  CSRF precisa voltar se a API passar a usar cookie ou sessão.
- **"User-controlled bypass of sensitive method"** (`java/user-controlled-bypass`), no
  `TokenAuthenticationFilter`:
  - a regra aponta uma condição que depende de um dado da requisição e decide se uma ação sensível
    acontece. Aqui, a condição é a própria verificação de autenticação: o cabeçalho `Authorization` é
    comparado com o token esperado, e só um valor igual autentica a requisição. Decidir pelo cabeçalho é
    exatamente o que um filtro de autenticação faz;
  - não há desvio: sem o cabeçalho, ou com outro valor, o filtro não autentica, e a autorização nega com
    401. Isso é comprovado pelo `AuthEnabledIT` (sem token, token errado, outro esquema, espaço a mais,
    outra caixa) e pelo `AuthProblemDetailsIT`;
  - a comparação é feita em tempo constante, sobre o SHA-256 dos dois valores (research R11, ADR 0017), e
    nenhum caminho autentica sem a comparação.
- **Onde fica a dispensa:** em `.github/codeql-exceptions.txt`, que a porta de severidade do job `codeql`
  lê, com uma linha por alerta (regra, arquivo e esta entrada). Dispensar o alerta na interface do code
  scanning do GitHub não basta, porque a porta lê o SARIF da análise (ver a entrada "Pipeline do PR do Marco
  3: porta do CodeQL com exceções registradas").

## 2026-11-25 — Revisão do Codex no PR do Marco 3: evento com caractere nulo (apontamento 3)

- **Apontamento (aceito):** o PostgreSQL não aceita o caractere nulo (U+0000) em texto. Um evento com
  `\u0000` passava pela validação, a recusa do banco entrava na nova tentativa sem limite (R9) e travava a
  partição para sempre: nenhum evento seguinte era gravado.
- **Correção em duas camadas**, com as specs primeiro (Princípio IV):
  - **validação:** nova regra `NUL_CHARACTER` na `MovieFieldRules`, comum ao CSV e ao Kafka (título,
    estúdios e produtores). As specs 001 (FR-006 e o data-model, motivo 9) e 003 (FR-012, caso de borda e
    data-model) registram a origem, como no `FIELD_TOO_LONG`. Na carga direta, uma linha com o caractere
    nulo passa a ser ignorada com aviso. No H2 ela era aceita, mas o PostgreSQL a recusaria. Um caractere
    nulo nas pontas é removido pelo `trim`, como no CSV, e não chega ao banco;
  - **classificação:** uma recusa do banco por dado inválido, com SQLState das classes 22 (dado) e 23
    (restrição de integridade), é permanente. O evento vai ao tópico de erros com o motivo
    `DATABASE_REJECTED` e o SQLState, sem nova tentativa. As demais falhas continuam com a nova tentativa,
    como a tabela inexistente (42P01) e a perda de conexão. A FR-015a e a R9 registram a regra, e o ADR 0016
    foi atualizado.
- **Comprovação:**
  - `NulCharacterIT` (carga direta): três linhas com o caractere nulo no título, nos estúdios e nos
    produtores são rejeitadas com aviso, e a carga continua;
  - `KafkaInvalidEventsIT`: o 17º caso, com o caractere nulo no meio do título, vai ao tópico de erros como
    `NUL_CHARACTER`;
  - `DatabaseRejectionPostgresIT` (`postgres-it`): o teste muda o esquema com a aplicação no ar, para que
    só o banco recuse, sem passar pela validação. Um `CHECK` dá o SQLState 23514, e a coluna `studios`
    reduzida a 5 caracteres dá o 22001. Os dois eventos vão ao tópico de erros com `DATABASE_REJECTED` e o
    SQLState, o evento válido seguinte é gravado, e o log não mostra nova tentativa;
  - `RetryPostgresIT` continua verde (42P01, com a nova tentativa).

  Antes da correção, o `DatabaseRejectionPostgresIT` falhou: as recusas foram tentadas de novo sem parar,
  e o evento válido nunca foi gravado. O `NulCharacterIT` e o `KafkaInvalidEventsIT` também falharam.
- **Tempos depois dos três apontamentos:**
  - `./mvnw verify`: **181 s** sem `clean`, ainda dentro do limite de 2× da R15 (192 s), com 261 + 85
    testes e cobertura de 97,35%. Os ITs novos somam cerca de 12 s, a maior parte no
    `KafkaBrokerLateStartIT`, que sobe um broker depois da aplicação;
  - `./mvnw -Ppostgres-it verify`: **75 s**, com 8 testes.

## 2026-11-25 — Revisão do Codex no PR do Marco 3: tópicos depois de falha do broker (apontamento 2)

- **Apontamento (aceito):** com `spring.kafka.admin.fail-fast: false`, se o broker estiver fora na subida, o
  `KafkaAdmin` não cria os tópicos e nada os recria depois. O publicador só reenviava os eventos. Como o
  compose desliga a criação automática de tópicos, a publicação falharia para sempre depois de o broker
  voltar.
- **Correção:**
  - a spec veio primeiro: o caso de borda "Broker indisponível" descreve a recuperação. A cada nova
    tentativa, a aplicação tenta criar os tópicos que faltam e, quando o broker volta, publica, consome a
    carga e fica pronta, sem reinício;
  - antes de cada nova tentativa de envio, o `MovieEventPublisher` chama `KafkaAdmin.initialize()`, que cria
    os `NewTopic` que ainda não existem. Com o broker ainda fora, a falha só é registrada, e a tentativa
    seguinte repete (R8).
- **Comprovação, por IT:** o `KafkaBrokerLateStartIT` reproduz o caso.
  1. A aplicação sobe apontando para uma porta livre, onde nada escuta: o broker está fora de verdade.
  2. O teste confere a readiness 503.
  3. Sobe o broker embutido com `auto.create.topics.enable=false`, como no compose.
  4. Confere a readiness 200, o resultado de referência e os dois tópicos criados.

  Sem a correção, o IT falhou: a readiness não chegou a 200 em 90 s. Com ela, passou em 22 s.
- **Por que há um repassador TCP no teste:** o `EmbeddedKafkaKraftBroker` do Spring Kafka 4.1.2 guarda o
  `kafkaPorts`, mas não o usa ao subir o cluster KRaft (conferido no bytecode), e o broker sempre sobe numa
  porta aleatória. O `TcpForwarder` de `src/test/` escuta na porta que a aplicação usa e só leva a conexão
  inicial ao broker. Daí em diante, os clientes falam direto com o broker, pelo endereço que ele anuncia.
  Não é um dublê: só copia bytes, sem ler nem alterar o protocolo. Por isso não foi preciso a alternativa
  de comprovar num passo do `compose-test.sh`.

## 2026-11-25 — Revisão do Codex no PR do Marco 3: grupo do consumidor (apontamento 1)

- **Apontamento (aceito):** o `@KafkaListener(id = "awards-movies")` usa o id como grupo (`idIsGroup`, que é
  verdadeiro por padrão) e ignora `KAFKA_CONSUMER_GROUP`. A FR-003 pedia toda a configuração por variável
  de ambiente, e o grupo estava fixo no código.
- **Por que os testes não pegaram:** cada classe de teste tem o seu broker, então todas usavam o mesmo grupo
  fixo sem conflito. Nenhum teste conferia o grupo registrado no broker.
- **Correção:**
  - a spec veio primeiro: a FR-003 agora cita o grupo do consumidor;
  - o ouvinte passou a usar `idIsGroup = false`, e o grupo vem de `spring.kafka.consumer.group-id`
    (`KAFKA_CONSUMER_GROUP`, padrão `awards`);
  - o `clientIdPrefix = "awards-movies"` ficou explícito. Sem ele, o `client.id` do Kafka seria derivado do
    grupo, e a série de atraso das métricas e o dashboard mudariam de nome conforme a variável. O
    `client_id` passa a ser `awards-movies-<n>`, e o `KafkaMetricsIT`, o dashboard, a R19 e o contrato
    operacional foram atualizados.
- **Comprovação:** o `KafkaConsumerGroupIT` sobe com `KAFKA_CONSUMER_GROUP=grupo-configurado-it` e confere
  no broker, pelo `AdminClient` real, que esse é o grupo registrado, com um membro, e que `awards-movies`
  não aparece. Antes da correção, ele falhou com o grupo `awards-movies`.
- **Efeito no compose:** com o padrão, o grupo passa a ser `awards`. Um volume do Kafka criado antes da
  correção tem os offsets no grupo antigo, então o consumidor novo relê o tópico desde o começo. Isso é
  inofensivo, porque a gravação é idempotente.

## 2026-11-25 — Tempo do `./mvnw verify` do Marco 3 (correção da etapa 2)

- **Origem:** o `verify` passou de 96 s na linha de base para cerca de 4 min, acima do limite de 2× da
  research R15 (entrada da etapa 2). O responsável não aceitou os 4 min nem o corte de casos, e pediu a
  redução sem perder cobertura.
- **Medição** (`./mvnw verify` sem `clean`, com o código já compilado, na mesma máquina):

  | | Tempo | Razão sobre a linha de base (96 s) |
  | --- | --- | --- |
  | Antes | 249 s | 2,6× |
  | Depois | **161 s** | **1,68×**, dentro do limite de 2× |

  Os ITs do Kafka passaram de cerca de 130 s para 59 s, somadas as duas execuções do Failsafe. A cobertura
  de linhas foi de 96,32% para 97,06%.
- **O que mudou, sem tirar nenhum caso:**
  - **perfil de teste `kafkatest`** (`src/test/resources/application-kafkatest.yml`, fora do JAR): prazos
    curtos dos clientes Kafka (`max.block.ms`, `request.timeout.ms`, `delivery.timeout.ms`,
    `default.api.timeout.ms` e os prazos do `KafkaAdmin`). Todos os ITs do Kafka o ligam junto com o perfil
    `kafka`. A produção não muda;
  - **`KafkaInvalidEventsIT` numa única execução:** os 16 eventos inválidos são publicados de uma vez,
    seguidos de um evento válido. Como o tópico tem uma partição, quando o válido aparece na listagem,
    todos os inválidos já foram processados sem travar o consumo. Cada caso é conferido pela chave no tópico
    de erros, com uma mensagem própria por caso:
    - os bytes originais;
    - o motivo e os cabeçalhos;
    - o aviso no log, agora comparado com o offset do próprio evento, o que é mais forte que antes.

    Passou de 16 s para 4 s por execução. O número de testes caiu de 270 para 255 (e de 96 para 81 na
    execução `json-logs`) só porque as 16 invocações parametrizadas viraram um teste com os mesmos 16 casos;
  - **reaproveitamento do contexto:** o `@DirtiesContext` saiu da base `KafkaScenarioIT`.
    - As classes que só leem e têm as mesmas propriedades compartilham o contexto e o broker. O
      `KafkaReferenceFileIT` reaproveita o do `KafkaMetricsIT` e passou de 5,7 s para 0,6 s.
    - As classes que mudam dados ou a readiness declaram `@DirtiesContext` e uma propriedade própria
      (`awards.test.context`): ganham um contexto só delas e nunca sujam o de outra.
    - Os testes com `SpringApplication` (`KafkaEquivalenceIT` e `KafkaStructuralFailureIT`) passaram a
      usar um broker embutido compartilhado na JVM, com tópicos e grupo próprios por classe;
  - **esperas fixas trocadas por condições determinísticas, com a mesma comprovação:**
    - `KafkaStructuralFailureIT`: o tópico vazio é conferido pelos offsets, sem esperar 3 s por caso. Foi
      de 8 s para 1 s;
    - `KafkaReadinessIT`: depois do marcador de outra execução, um filme sentinela, cuja gravação prova que o
      marcador já foi processado, no lugar de 3 s de espera. Foi de 8,6 s para 3,6 s;
    - `KafkaIdempotencyIT`: os 206 eventos são reenviados sem esperar cada confirmação, com um `flush` no
      fim. Foi de 9,4 s para 4,2 s.
- **O que sobrou de mais caro**, se um dia for preciso ir além:
  - `KafkaBrokerUnavailableIT`, cerca de 9 s: a subida sem broker espera o `KafkaAdmin`, e a observação
    dura 5 s;
  - `KafkaEquivalenceIT`, cerca de 8 s: seis subidas da aplicação.

  Reduzir esses dois exigiria encurtar a observação ou comparar menos arquivos, ou seja, tirar
  comprovação, e por isso não foi feito.
- **Verificações:** `./mvnw verify` verde, `./mvnw -Ppostgres-it verify` verde (7 testes, com o
  `RestartPostgresIT` e o `RetryPostgresIT` no perfil de teste) e `check-unchanged-tests.sh v0.2.0` verde.

## 2026-11-24 — Fechamento do bolt do Marco 3 (etapa 5)

- **ADRs (FR-037):**
  - de 0013 a 0019, no formato da spec 002, cada um citando a sua entrada neste registro;
  - o 0017 cita o mecanismo de comparação do token e registra, como decisão intencional, os cabeçalhos
    `X-Content-Type-Options` e `X-Frame-Options` em todas as respostas e o `Cache-Control` fora dos
    endpoints de negócio;
  - o 0004 ganhou os quatro casos de `SpringApplication` (emenda 1.5.0) e o perfil dedicado (emenda
    1.7.0);
  - o índice foi atualizado.
- **README (FR-036):**
  - o modo Docker: serviços, portas, carga por Kafka, publicação de um vencedor novo e leitura do tópico
    de erros;
  - as credenciais de demonstração do compose;
  - a autenticação, com a matriz de acesso e o token de demonstração, definido só no README, que é o
    único lugar do repositório com esse valor;
  - os testes do Marco 3 e o perfil `postgres-it`;
  - os jobs `postgres` e `compose`;
  - a tabela de requisitos do Marco 3;
  - a arquitetura do modo Docker e as decisões do Marco 3.

  As pendências deixadas pela emenda 1.5.0 na seção de testes e na tabela de rastreabilidade foram
  resolvidas. A seção "Avalie em 2 minutos" não mudou.
- **Clone limpo (T089):** num clone novo do commit da etapa 4, só com o JDK:
  - `./mvnw verify` verde em 275 s, com 270 + 96 testes;
  - `./mvnw spring-boot:run` respondeu o resultado de referência e a listagem do README, com o `ETag` e
    sem `Cache-Control`;
  - com `API_TOKEN` igual ao token de demonstração: 401 sem token e com token errado, 200 com ele, 401 nas
    métricas sem token, e a readiness e o Swagger UI públicos. O token não apareceu no log.

  As seções 3 e 4 do quickstart (PostgreSQL e compose) foram verificadas pelo `postgres-it` e pelo
  `compose-test.sh` locais, nas etapas 2 a 4. Os comandos rodaram na porta 18081, porque a 8080 da
  máquina estava ocupada por um processo do usuário.
- **Verificado localmente, com o Docker:**
  - `./mvnw -Ppostgres-it verify`: 7 testes, inclusive a nova tentativa quando a gravação falha;
  - `compose-test.sh`: todos os passos, inclusive a queda real do banco (7b), com os passos 1 a 4 em
    28 s.
- **Pendências** (exigem o push ou o PR, que são do responsável):
  - T020, T051 e T084: os jobs `postgres` e `compose` e os demais no pipeline;
  - T050: o testador adversário (Codex) na ingestão por Kafka;
  - T093: `postgres` e `compose` como checks obrigatórios da `main`, com o bypass desligado.

  Fica também a decisão sobre o tempo do `./mvnw verify`, cerca de 4 min (entrada da etapa 2).
- **Pontos A3, N3 e C4 da segunda análise:**
  - A3: o `DefaultModeIsolationIT` e o `NoGeneratedPasswordIT` já passavam antes da implementação e
    ficaram como guardas;
  - N3: o `OPTIONS` do Marco 2 responde 2xx com `Allow`, verificado e fixado no
    `AuthDisabledByDefaultIT`;
  - C4: a única propriedade usada por teste que restou é a `publish-completion`, documentada no ADR 0014.
    A `create-error-topic` não foi necessária.

## 2026-11-24 — Etapa 4 do Marco 3: compose, verificações e ajustes

- **Imagens (T074):** as versões estáveis mais recentes no Docker Hub, fixadas por tag e digest:
  - `apache/kafka-native:4.4.0` e `apache/kafka:4.4.0` (o `kafka-cli`, sob demanda);
  - `prom/prometheus:v3.16.0`;
  - `grafana/grafana:13.3.2`;
  - `postgres:18-alpine`, com o mesmo digest do job `postgres`.

  O cliente da aplicação (kafka-clients 4.2.2) conversa com o broker 4.4.
- **Healthchecks (R17, verificado):** a imagem nativa do Kafka tem `nc`, e o healthcheck é
  `nc -z localhost 9092`. O Prometheus tem `sh`, `wget` e `printf`, e o Grafana tem `wget`. Nenhuma saída
  alternativa foi necessária.
- **Volume do Kafka:** a imagem roda com o usuário `appuser`. Um volume num caminho que não existe na
  imagem seria criado com dono root, então o volume `kafkadata` fica em `/home/appuser`, que já pertence ao
  `appuser`, com `KAFKA_LOG_DIRS=/home/appuser/kraft-logs`. O `CLUSTER_ID` é fixo, para que o volume sirva
  a um container recriado.
- **Token do Prometheus (R18, verificado):** o `credentials_file` vazio é aceito com a autenticação
  desligada, e a coleta funciona. Com `API_TOKEN` definido, o alvo volta a `up` com o token.
- **Imagem da aplicação na política:** o serviço `app` é construído pelo próprio compose (`build: .`) e
  não tem digest. A regra do `check-workflows.sh` vale para as imagens baixadas.
- **Passo 7b (FR-015a, ajuste):** uma queda curta do PostgreSQL não chega a gerar falha, porque o pool de
  conexões (Hikari) espera até 30 s por uma conexão. O passo passou a manter o banco fora até o log da
  aplicação mostrar `Falha ao gravar o evento`, prova de que a gravação falhou de verdade e foi tentada de
  novo, e só então o religa. O filme é gravado, e o tópico de erros não recebe o evento.
- **Git Bash:** o `compose-test.sh` exporta `MSYS_NO_PATHCONV=1`, para que `/opt/kafka/bin/...` não vire
  um caminho do Windows. No runner Linux, isso não muda nada.
- **Execução local (T081):**
  - `compose-test.sh` verde, com todos os passos de 0 a 8 e o 7b;
  - os passos de 1 a 4 levaram 28 s, para um limite de 180 s (SC-008);
  - rodou com `APP_PORT=18080`, porque a porta 8080 da máquina estava ocupada por um processo Java do
    usuário, fora da sessão.

## 2026-11-24 — Etapa 3 do Marco 3: verificações e ajustes da autenticação

- **Nome da autoconfiguração (verificado):** no Boot 4.1.2, é
  `org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration`. Ela está excluída
  no `application.yml` e no `application-kafka.yml`, e o `NoGeneratedPasswordIT` confere que o log não
  traz senha gerada.
- **`OPTIONS` no Marco 2 (N3 da segunda análise, verificado):** antes do Spring Security, `OPTIONS` nos
  endpoints de negócio respondia 2xx com `Allow`. O `AuthDisabledByDefaultIT` fixa esse comportamento com
  o token desligado. Com o token ligado, a resposta é 401.
- **`Cache-Control` no 401 (R12, ajuste):**
  - o `DelegatingRequestMatcherHeaderWriter` previsto decide só pelo caminho e deixava o 401 dos
    endpoints de negócio sem os cabeçalhos de cache, ao contrário do contrato operacional;
  - foi trocado por um `HeaderWriter` próprio, que aplica o `CacheControlHeadersWriter` fora dos
    endpoints de negócio e em todo 401. O status é conhecido, porque os cabeçalhos são escritos quando a
    resposta é confirmada;
  - os endpoints de negócio continuam sem `Cache-Control` nas respostas 200, 304, 400 e 405.
- **`CacheHeadersIT` em duas classes:** a tarefa previa classes aninhadas, uma para cada estado do token.
  Ficaram duas classes, `CacheHeadersIT` e `CacheHeadersWithTokenIT`, porque cada estado do token precisa
  de um contexto próprio, e a base `ScenarioIT` já faz isso por classe.
- **Validador de contrato:** o `ContractValidator` valida só a resposta, e não o requisito de segurança da
  requisição. O `security: [{}, {bearerAuth: []}]` do contrato 1.2.0 não muda os ITs dos Marcos 1 e 2, e
  o validador não precisou de sobrecarga (R13).
- **Prova de que os Marcos 1 e 2 não mudaram (R14):**
  - o `AuthDisabledByDefaultIT` é a guarda contra `API_TOKEN` definido;
  - o `check-unchanged-tests.sh v0.2.0` passa, com os 59 ITs da tag sem alteração e o `ApiDocsIT` como
    única exceção (só a constante `SPEC_CONTRACT`);
  - o script foi testado com um IT alterado (reprova), com o `ApiDocsIT` alterado (passa) e com uma
    exceção de título inexistente (reprova).
- **Tempo do `./mvnw clean verify`:** 227 s, depois de encurtar os prazos do `KafkaBrokerUnavailableIT`.

## 2026-11-24 — Etapa 2 do Marco 3: verificações e decisões da ingestão por Kafka

- **Nome da autoconfiguração (R2, verificado):** no Boot 4.1.2, são
  `org.springframework.boot.kafka.autoconfigure.KafkaAutoConfiguration` e
  `...kafka.autoconfigure.metrics.KafkaMetricsAutoConfiguration`, excluídas no `application.yml` e
  liberadas no `application-kafka.yml`. Versões: Spring Kafka 4.1.2 e kafka-clients 4.2.2.
- **Nome inválido de tópico (T034c, verificado):**
  - um tópico de erros com um nome que o Kafka não aceita **não derruba a subida**. Com
    `spring.kafka.admin.fail-fast: false`, o `KafkaAdmin` registra o `InvalidTopicException` e a aplicação
    fica pronta;
  - o `KafkaErrorTopicUnavailableIT` usa esse nome, e cada envio é recusado de verdade pelo cliente;
  - a propriedade alternativa `awards.ingestion.create-error-topic` não foi necessária e foi descartada:
    menos uma configuração usada só por teste.
- **Broker fora do ar (T034b, verificado):** a aplicação sobe sem o broker, com a liveness 200 e a
  readiness 503. A subida, com os prazos do `KafkaAdmin`, e o encerramento, com envios pendentes, levam
  cerca de 15 s. O teste encurta os prazos do `KafkaAdmin` e do produtor só para ele.
- **Série de atraso do consumidor (R19, verificado):** a série que aparece logo depois da carga é
  `kafka_consumer_fetch_manager_records_lag_max`, por cliente (`client_id=consumer-awards-movies-<n>`). A
  série por partição só aparece depois da atualização periódica do Micrometer. Com um consumidor e uma
  partição, o máximo do cliente é o atraso do tópico. A R19 e o contrato operacional foram atualizados.
- **Captura de saída por teste:** o resumo da publicação é registrado num fio próprio, durante o
  primeiro teste da classe, e o `OutputCaptureExtension` descarta a captura de cada teste no fim dele.
  Por isso, a verificação do resumo ficou só no `PublisherSummaryIT`, que tem um único teste, e saiu do
  `KafkaInvalidLinesIT`, que confere o tópico e os avisos.
- **Tempo do `./mvnw verify` acima do limite da R15:** 256 s sem `clean`, contra 96 s na linha de base,
  ou seja, 2,7× (limite de 2×). A R15 previa compartilhar o broker entre as classes, mas a medição mostra
  que o broker não é o custo: uma classe inteira, como o `KafkaUpdateIT`, leva 2,9 s com broker e
  contexto. O tempo está em poucos testes que esperam de propósito:
  - `KafkaBrokerUnavailableIT`, cerca de 20 s;
  - `KafkaInvalidEventsIT`, 16 casos, cerca de 18 s em cada uma das duas execuções do Failsafe;
  - `KafkaReadinessIT`, 3 s de espera pelo marcador errado;
  - `KafkaEquivalenceIT`, seis subidas.

  Foram feitos dois ajustes: o produtor dos testes passou a ser reutilizado e a leitura passou a ser feita
  sem grupo de consumidores, o que tirou cerca de 20 s do `KafkaInvalidEventsIT`. O resto foi mantido.
  **Pendente de decisão do responsável:** aceitar os cerca de 4 min do `verify`, ou cortar casos.
- **Nova tentativa quando a gravação falha (FR-015a):** o `RetryPostgresIT` passou no `postgres-it`
  local, com a mensagem `Falha ao gravar o evento … nova tentativa` no log, e o filme foi gravado depois
  de a tabela ser devolvida. A queda real do banco fica para o passo 7b do compose (etapa 4).
- **Validação comum (I1 da segunda análise):** o `FIELD_TOO_LONG` está na `MovieFieldRules`, usada pelo
  leitor do CSV e pelo parser de eventos. O `FieldTooLongIT` (carga direta) e o `KafkaEquivalenceIT` (com
  o arquivo `field-too-long.csv`) passam.

## 2026-11-24 — Segunda análise da spec 003 (`/speckit-analyze`)

O responsável aceitou as correções, com os ajustes abaixo. Elas foram aplicadas na spec, no plano, na
research, nos contratos e nas tarefas. A spec 001 também mudou (I1).

- **C1, nova tentativa quando a gravação falha:**
  - nova FR-015a e o `RetryPostgresIT` no `postgres-it`, que renomeia a tabela `movie` e a devolve num
    `finally`. Ele é `@Isolated` e roda sem paralelismo;
  - a spec e a R9 dizem o que o teste comprova de fato: a nova tentativa quando a gravação falha;
  - a queda real do banco continua comprovada pelo passo 7b do compose.
- **C2, comparação do token:**
  - a FR-024 saiu da spec, e o número ficou reservado;
  - a comparação em tempo constante virou decisão de design na R11, com o mecanismo
    (`MessageDigest.isEqual` sobre SHA-256 em UTF-8);
  - o ADR 0017 cita o mecanismo, e a "Revisão de segurança" o confere.
- **C3, checks obrigatórios:** nova tarefa T093, um pedido ao responsável depois da primeira execução do
  pipeline no PR e antes do merge:
  - incluir `postgres` e `compose` como checks obrigatórios;
  - conferir os seis jobs existentes;
  - deixar desligado o bypass para administradores.
- **I1, `FIELD_TOO_LONG` sem exceção:** a pedido do responsável, a diferença não foi documentada. A
  regra de tamanho máximo passou para a `MovieFieldRules` e vale nos dois caminhos.
  - **Custo, avaliado como baixo:** uma regra, um motivo no `RejectionReason` e um IT novo
    (`FieldTooLongIT`).
  - **Efeito no Marco 1:** uma linha do CSV com campo maior que a coluna deixa de interromper a subida
    com um erro do banco. Ela passa a ser ignorada com aviso, e a spec 001 (FR-006) e o data-model 001
    foram atualizados.
  - Nenhum IT do Marco 1 depende desse caso, e o `KafkaEquivalenceIT` passa a incluir um arquivo com
    esse caso.
  - As diferenças de representação do evento (`winner` booleano, `year` inteiro JSON) foram para a
    FR-010 e a FR-012.
- **I2:** a linha de exceção do `ApiDocsIT` cita o título exato, com as crases.
- **G1:** novo `KafkaBrokerUnavailableIT` (liveness 200 e readiness 503 com o broker fora).
- **G2:** novo `KafkaErrorTopicUnavailableIT`. Antes, a tarefa verifica se um nome inválido de tópico
  derruba a subida; se derrubar, usa um tópico válido não criado.
- **G3:** o passo 7 do compose usa `down` sem `-v` e `up`, e não mais `restart app`.
- **A1:** o `OPTIONS` segue a matriz de acesso, e o CORS fica para o Marco 6.
- **A2:** o valor do token de demonstração é definido só no README (T088), e o quickstart o cita sem
  repeti-lo.
- **I3:** nova FR-022a, com os cabeçalhos de proteção e a ausência de `Cache-Control` nos endpoints de
  negócio, e a definição de "como no Marco 2" na FR-019.
- **I4:** a saúde geral entra na FR-022.
- **I5:** o `RestartPostgresIT` faz 4 subidas (3 reinícios), como o SC-004.
- **Não aplicados**, porque o responsável não os pediu: I6 (deriva entre a estrutura do plano e as
  tarefas, só em parte resolvida pelas mudanças acima), I7 ("aceitas" × "publicadas"), A3 (o T036
  aceita que o `DefaultModeIsolationIT` passe) e C4 (propriedade `publish-completion` usada por teste).

## 2026-11-24 — Exceção do `ApiDocsIT` à regra dos testes sem alteração

- **Origem:** plano 003. O `ApiDocsIT` (Marco 2, tag `v0.2.0`) compara o `/openapi.yaml` publicado, byte
  a byte, com `specs/002-operacao/contracts/openapi.yaml`. O contrato 1.2.0 (autenticação opcional e
  resposta 401, spec 003 FR-026) faz esse teste falhar. Pela R14 do plano, os ITs dos Marcos 1 e 2 não
  podem mudar.
- **Alternativas apresentadas ao responsável:**
  - **A:** trocar só a constante `SPEC_CONTRACT` para o contrato da spec 003, como o Marco 2 fez ao
    trocar a spec 001 pela 002;
  - **B:** sobrescrever o contrato da spec 002 com o 1.2.0;
  - **C:** não publicar o 1.2.0.
- **Decisão do responsável:** opção A, sem mudar nenhuma asserção. O `ApiDocsIT` é a única linha de
  `.github/unchanged-tests-exceptions.txt`, com o título desta entrada. O `check-unchanged-tests.sh`
  aceita a mudança desse arquivo e reprova a de qualquer outro IT da tag.
- **Rejeitadas:**
  - B: o artefato de uma spec já entregue passaria a descrever o marco seguinte;
  - C: a FR-026 deixaria de ser cumprida.

## 2026-11-24 — Cabeçalhos de proteção do Spring Security em todas as respostas

- **Origem:** plano 003, research R12. O Spring Security acrescenta `Cache-Control`, `Pragma`,
  `Expires`, `X-Content-Type-Options` e `X-Frame-Options` a toda resposta. Os três primeiros anulariam o
  ETag, e o Marco 2 não mandava `Cache-Control`.
- **Decisão do responsável:**
  - o `Cache-Control` do Security fica fora dos dois endpoints de negócio (pedido do plano);
  - `X-Content-Type-Options` e `X-Frame-Options` ficam em **todas** as respostas, com o token ligado ou
    desligado;
  - o ADR da autenticação os registra como decisão intencional (spec 003, FR-037, e item do checklist).
- **Leitura da FR-019** ("comportamento idêntico ao do Marco 2"): status, corpo, ETag, 304 e os
  cabeçalhos que o Marco 2 já mandava continuam iguais. Os dois cabeçalhos de proteção são acréscimos
  que não mudam nada do que os testes dos Marcos 1 e 2 verificam.
- **Rejeitada:** desligar também esses dois escritores, para uma igualdade literal de cabeçalhos. Isso
  removeria proteção sem nenhum ganho para o ETag nem para o contrato.

## 2026-11-24 — Emenda 1.7.0 da constituição: comprovação de comportamento que depende de infraestrutura

- **Origem:** Constitution Check do plano 003:
  - o Princípio VI exigia o teste de integração de cada comportamento "no `./mvnw verify`";
  - o Princípio II põe os testes de PostgreSQL num perfil Maven separado, fora do `verify` padrão;
  - o plano tinha lido os dois juntos e pedido a confirmação do responsável.
- **Decisão do responsável:** emenda MINOR do Princípio VI. O comportamento da aplicação que depende de
  infraestrutura indisponível no modo padrão, como o PostgreSQL, é comprovado por testes de integração
  num perfil Maven dedicado, executado obrigatoriamente no pipeline. Todo o resto continua exigindo teste
  no `./mvnw verify`.
- **Detalhes da redação:**
  - os testes do perfil seguem o Princípio I (contexto completo, sem mocks, HTTP);
  - o comportamento comum aos dois modos continua testado no `verify`, com H2;
  - uma dependência com substituto embutido sem Docker, como o broker Kafka do Spring, não entra na
    exceção.
- **Impacto:**
  - 1.6.0 → 1.7.0 (MINOR): é regra nova, e nenhum requisito que cumpria a versão anterior deixa de
    cumprir;
  - foram alinhados a constituição (Princípio VI e Sync Impact Report), o `AGENTS.md` (rastreabilidade e
    seção Testes), a spec 003 (FR-038) e o plano 003 (Constitution Check). O checklist da spec 003 também
    foi atualizado.

## 2026-11-24 — Emenda 1.6.0 da constituição: credenciais de demonstração no compose local

- **Origem:** clarificação da spec 003 (Marco 3). O compose precisa de usuário e senha para o
  PostgreSQL e o Grafana, e o Princípio VII só permitia o token de demonstração no repositório.
- **Alternativas apresentadas ao responsável:**
  - sem credencial: PostgreSQL só na rede interna do compose, sem senha, e Grafana com acesso anônimo;
  - senhas de demonstração, documentadas no README;
  - `.env` não versionado, com um passo obrigatório antes do `docker compose up`.
- **Decisão do responsável:** senhas de demonstração, com emenda do Princípio VII antes do plano. Elas
  são permitidas só no compose de desenvolvimento local e precisam cumprir quatro condições:
  - aparecem só nos arquivos do compose local e na configuração que ele monta;
  - são identificadas como de demonstração no README e no próprio arquivo;
  - podem ser sobrescritas por variável de ambiente;
  - nunca são usadas em ambiente publicado.

  O token de demonstração continua como estava.
- **Motivo:** é a forma mais comum de um compose local subir com um único comando, num clone limpo. Uma
  senha de demonstração restrita à máquina de quem executa não protege nada de valor.
- **Ponto interpretado na redação:** o estágio do pipeline que sobe o mesmo compose num runner
  temporário (spec 003, FR-035) foi declarado como não sendo ambiente publicado. Sem isso, o teste do
  compose no pipeline violaria a condição "só no compose local".
- **Impacto:** MINOR (1.5.0 → 1.6.0), porque amplia o permitido e não proíbe nada que valia antes.
  Foram alinhados a constituição (Princípio VII, motivo e Sync Impact Report), o `AGENTS.md` (seção
  Segurança), a spec 003 (Clarifications e FR-033) e o checklist dela. O README só ganha a
  identificação das credenciais quando o compose existir (spec 003, FR-036).

## 2026-11-24 — T050 do Marco 2 (SC-008) não executada

- **Situação:** a T050, que prova que o pipeline reprova (SC-008), não foi executada antes do merge do
  PR do Marco 2 nem da tag `v0.2.0`. O branch temporário de verificação não chegou a ser criado, e
  nenhum dos sete defeitos foi enviado ao pipeline.
- **Motivo (decisão do responsável):** prazo de entrega. A verificação exige um push e uma execução
  do pipeline por defeito, e isso não cabia no prazo do marco.
- **O que está comprovado:** os demais requisitos de pipeline (FR-028, FR-028b e FR-029) foram
  comprovados pelas execuções verdes do workflow `CI` no branch `marco/2-operacao` e no PR. Depois do
  merge, o pipeline também validou o primeiro pull request do Dependabot (grupo
  `maven-minor-e-patch`, "Bump the maven-minor-e-patch group with 4 updates"), o que confirma que o
  Dependabot está ativo e que os PRs dele passam pelo mesmo portão.
- **O que fica sem comprovação:** que cada job reprova diante do defeito correspondente. As execuções
  verdes mostram que o pipeline aprova código correto, não que ele recusa código com defeito. O único
  portão já visto falhando de propósito é o da cobertura mínima, localmente, com
  `-Djacoco.line.minimum=0.99` (T041).
- **Impacto:** a T050 fica marcada como não executada em `specs/002-operacao/tasks.md`, com referência
  a esta entrada. O SC-008 continua aberto e pode ser comprovado depois, seguindo o roteiro da T050 sem
  mudança, num branch temporário a partir da `main`. Quando isso acontecer, o resultado entra numa
  entrada "Verificação do SC-008", como a T050 prevê.

## 2026-11-24 — Emenda 1.5.0 da constituição: `SpringApplication` para controlar o ciclo de vida

- **Origem:** revisão do Codex no PR do Marco 2. O `EtagAcrossRunsIT` e o `ReadinessAfterLoadIT` usam
  `SpringApplication` em execuções bem-sucedidas, e a constituição 1.4.1 só permitia isso para falha de
  inicialização. **Aceito:** o apontamento estava correto.
- **Decisão do responsável:** emendar o Princípio I, em vez de reescrever os testes. Os dois precisam
  controlar o ciclo de vida: o primeiro compara duas execuções, o segundo observa a ordem da subida, e
  nenhum dos dois cabe num `@SpringBootTest`. A cláusula passa a valer sempre que o teste precisa
  controlar o ciclo de vida da aplicação real: falha de inicialização, múltiplas execuções, ordem da
  subida e saída completa da subida. Continua sem mocks. MINOR (1.4.1 → 1.5.0): amplia o permitido e não
  proíbe nada que valia antes.
- **Dois pontos levados ao responsável antes da redação:**
  - *HTTP real:* o pedido dizia "continua com chamadas HTTP reais", mas o `ReadinessAfterLoadIT` não faz
    chamada HTTP, e a ordem da subida não é observável por HTTP (a probe só responde depois que o
    servidor sobe). **Escolhido:** o resultado é verificado por HTTP real sempre que o comportamento é
    observável por HTTP; a ordem da subida e o próprio log são verificados pelos eventos reais e pela
    saída capturada. O teste não muda.
  - *Testes não citados na revisão:* o `JsonLogFormatIT` e o `TextLogFormatIT` também usam
    `SpringApplication` em subidas bem-sucedidas, para ler o log desde a primeira linha. **Escolhido:**
    cobri-los com o caso "saída completa da subida" e citar a cláusula no javadoc deles.
- **Mudanças:** constituição (cláusula e Sync Impact Report), `AGENTS.md` (regras invioláveis) e o
  javadoc do `EtagAcrossRunsIT`, do `ReadinessAfterLoadIT`, do `JsonLogFormatIT`, do `TextLogFormatIT` e
  do `ApplicationStarter`. Nenhuma linha de código de teste ou de produção mudou.
- **Pendente (papel de documentação):** o README (seção de testes e tabela de rastreabilidade) e o ADR
  0004 ainda dizem que só os testes de falha de inicialização usam `SpringApplication`.

## 2026-11-24 — T037: healthcheck com readiness em 503

- **Pergunta (research R8):** o `wget` do BusyBox da imagem termina com código diferente de zero quando
  a readiness responde 503? Se não terminasse, o container ficaria `healthy` antes da carga do CSV.
- **Experimento, sem alterar a aplicação:** um container da própria imagem rodou um servidor temporário
  com o `nc` do BusyBox, respondendo um status fixo na 8080. Um segundo container da imagem entrou na
  rede do primeiro (`--network container:`) e rodou o comando do `HEALTHCHECK` exatamente como
  `docker inspect` o lê da imagem.
- **Resultado:** com 503, o código de saída foi 1 (`wget: server returned error: HTTP/1.1 503`). Com
  200, foi 0. Nos dois casos, o servidor registrou `GET /actuator/health/readiness`, então o `PORT` da
  imagem foi expandido. O healthcheck não muda, e a T037 foi concluída (hadolint e variável de log vazia
  já verificados antes).
- O script do experimento ficou fora do repositório: é uma verificação única de uma premissa da imagem
  base. Se o Dependabot trocar a base, os passos 1 e 7 do teste de fumaça continuam provando o
  `healthy`, mas não o caso do 503.

## 2026-11-24 — hadolint DL3025 no `CMD` do healthcheck

- **Falha:** o job `dockerfile-lint` reprovou o `Dockerfile:44` com a regra DL3025 ("Use arguments JSON
  notation for CMD and ENTRYPOINT arguments"). O alvo era o `CMD` do `HEALTHCHECK`, em forma shell; o
  `ENTRYPOINT` já estava em forma exec.
- **Decisão:** corrigir, e não ignorar a regra. Ela se aplica ao nosso caso: a notação JSON com o shell
  explícito (`["sh", "-c", "wget ... || exit 1"]`) mantém a expansão do `PORT` a cada verificação, que
  era o único motivo da forma shell. Nenhum `# hadolint ignore` foi necessário, e o pipeline continua
  sem regra desligada.
- **Verificação local:** com a mesma imagem do hadolint que a action fixada usa
  (`ghcr.io/hadolint/hadolint:v2.15.1-debian`, pelo digest) e `--failure-threshold warning`, o
  `Dockerfile` anterior reprova com a DL3025, e o novo passa. Depois do rebuild, o teste de fumaça deu
  12 OK, 0 FALHA e o passo 13 PULADO. O passo 7 (`PORT=9090`) ficou `healthy`, o que prova a expansão.
- A spec mudou antes do código: research R8 e ADR 0011.

## 2026-11-24 — Teste de fumaça local e correções do script

Com o Docker ligado, a imagem foi construída e o `smoke-test.sh` rodou no Git Bash do Windows (Docker
26.0.0). A primeira execução expôs defeitos do próprio script, e não da imagem:

- **Porta fixa no host:** havia um processo Java local na 8080 do host. O passo 1 falhou, e os passos 2
  e 3 "passaram" chamando esse processo, e não o container: falso positivo. **Correção:** cada container
  publica a porta numa porta aleatória do host, só em `127.0.0.1`, lida com `docker port`.
- **Passos dependentes sem o container:** os passos 4 e 6 também passaram com o container parado. O 4
  aceitou um `id -u` vazio, e o 6 "parou" um container que não rodava. **Correção:** os passos 2 a 6
  exigem o container do passo 1 rodando e falham com esse motivo. O passo 4 exige um uid numérico
  diferente de 0. Com uma imagem inexistente, os passos 1 a 6 agora falham.
- **Contexto de build no Git Bash:** o `check-docker-context.sh` exporta `MSYS_NO_PATHCONV=1` e passava
  o caminho POSIX ao `docker.exe`, que não o encontra. **Correção:** no Git Bash, o contexto é convertido
  com `cygpath -w`, como já era feito nos bind mounts.

A spec mudou antes do código (research R14). Depois das correções, o teste de fumaça deu 12 OK, 0 FALHA
e 1 PULADO, com o mesmo processo ainda ocupando a 8080. O passo 13 foi pulado porque o `chmod 000` não
vale no bind mount do Docker Desktop; no pipeline, ele precisa rodar (T047). O log do container saiu em
JSON (33 linhas, todas válidas no `jq`), no formato ECS aninhado, com
`LOGGING_STRUCTURED_FORMAT_CONSOLE=ecs` vindo da imagem. Com a variável vazia, saiu em texto (passo 12).
O `jq` 1.8.1 veio da release oficial, com o checksum conferido, para o diretório temporário da sessão,
fora do repositório.

## 2026-11-24 — Fechamento do bolt do Marco 2 (etapa 6)

- **README (FR-034):** atualizado com o formato do log, a imagem Docker, o ETag e o 304, os endpoints
  operacionais, as duas execuções do Failsafe e a cobertura, o pipeline e os critérios de reprovação, a
  tabela dos requisitos do Marco 2, as decisões e o índice dos ADRs. As seções do Marco 1 não foram
  reescritas.
- **Clone limpo (T060):** num clone novo do branch, a seção "Avalie em 2 minutos" funcionou só com o
  JDK: `verify` verde, a aplicação subiu em cerca de 5 s, os dois `curl` responderam o esperado, e o log
  trouxe o resumo, sem banner. O `verify` levou 273 s no clone novo e 75 s na cópia de trabalho já
  compilada, e o README registra os dois tempos.
- **Rastreabilidade (T061):** cada requisito de comportamento da spec 002 (FR-001 a FR-020) aparece no
  `@DisplayName` de pelo menos um IT do Marco 2. *Observação para o responsável:* os IDs FR-001 a FR-020
  existem nas duas specs, com significados diferentes. Hoje, quem desfaz a ambiguidade é o pacote do
  teste (`etag/`, `health/`, `metrics/` e `logging/` são da spec 002). Um prefixo por spec nos
  `@DisplayName` resolveria isso, mas mudaria os nomes dos testes do Marco 1, e por isso não foi feito sem
  decisão.
- **Pendências do bolt:** dependem do push do branch, que é do responsável:
  - T037 e T038: hadolint, `wget` com 503, variável de log vazia, build e teste de fumaça, que vão para
    os jobs `dockerfile-lint` e `image`;
  - T047 a T050: pipeline verde, conferências da execução, Dependabot ativo e a verificação do SC-008 no
    branch temporário.

  O daemon do Docker não estava rodando na máquina de desenvolvimento, então nada da imagem foi
  verificado localmente.

## 2026-11-24 — Decisões de arquitetura sem entrada própria (ADRs)

Registro feito junto com os ADRs da etapa 5 do Marco 2 (FR-031). As decisões abaixo foram tomadas nos
planos 001 e 002 e nas etapas do Marco 2, mas não tinham entrada aqui. Cada uma é resumida, e o detalhe
fica no ADR e na research.

### Stack: Java 25, Spring Boot 4.1.2 e Maven com wrapper (ADR 0001)

- **Decisão (plano 001, research R1):** Java 25 (LTS), Spring Boot 4.1.2 e Maven 3.9.16 pelo Maven
  Wrapper 3.3.4, na distribuição `only-script`.
- **Motivo:** o Boot 4.1.2 era a última versão estável (a 4.2.0 estava em RC). O wrapper permite subir e
  testar só com o JDK (Princípio II), sem um JAR binário no repositório.
- **Alternativas rejeitadas:** Boot 4.2.0-RC2 e Maven 4 RC, que não eram estáveis; Boot 3.5.x, que não
  era o "estável atual"; wrapper com JAR.

### H2 com JPA e esquema por Flyway (ADR 0002)

- **Decisão (plano 001, research R5):** H2 em memória no modo padrão, JPA com `ddl-auto=validate` e
  esquema versionado pelo Flyway (`V1__create_schema.sql`), com três tabelas (`movie`, `producer` e
  `movie_producer`) e consultas em JPQL, sem SQL nativo.
- **Motivo:** o avaliador roda sem nada instalado, e o mesmo esquema e as mesmas consultas servem ao
  PostgreSQL do Marco 3. A unicidade de ano + título também fica no banco.
- **Alternativas rejeitadas:** tabela única com os produtores em texto; `ddl-auto=create`, que a
  constituição proíbe.

### Erros em Problem Details (ADR 0006)

- **Decisão (plano 001, research R7):** os erros da API saem em Problem Details (RFC 9457), com
  `spring.mvc.problemdetails.enabled` e um `@RestControllerAdvice` que converte o parâmetro inválido em
  400 com a propriedade `invalidParams` (`[{name, reason}]`). O 405 também sai em Problem Details, com o
  cabeçalho `Allow`.
- **Motivo:** é o formato padrão para erros HTTP, que o próprio Spring produz, e a constituição o exige.
- **Alternativas rejeitadas:** um corpo de erro próprio; o corpo de erro padrão do Boot, que não segue a
  RFC.

### ETag derivado do conteúdo (ADR 0007)

- **Decisão (plano 002, research R4; etapa 1):** ETag forte, `"0"` mais o MD5 do corpo, nas respostas
  2xx de GET dos dois endpoints de negócio, pelo filtro `ContentEtagFilter`, e 304 na revalidação,
  inclusive com `If-None-Match: *`.
- **Motivo:** os dados não mudam durante a execução, e os corpos são pequenos. Um ETag do corpo não exige
  estado novo nem mudança nos controllers. O filtro próprio existe porque o `ShallowEtagHeaderFilter` do
  Spring não trata o `*` (entrada da etapa 1, abaixo).
- **Alternativas rejeitadas:** ETag por "versão" dos dados; ETag fraco; `Cache-Control` com `max-age`.

### Liveness e readiness pelo Actuator (ADR 0008)

- **Decisão (plano 002, research R1 e R2; etapa 2):** probes do Actuator em `/actuator/health/liveness` e
  `/actuator/health/readiness`, na mesma porta da API, sem detalhes. A readiness fica positiva no
  `ApplicationReadyEvent`, que o ciclo de vida do Boot garante depois da carga do CSV.
- **Motivo:** é o caminho padrão do framework, sem código próprio. A ordem "carga → pronta" já é garantida
  pelo ciclo de vida.
- **Alternativas rejeitadas:** controllers próprios de saúde; um `HealthIndicator` com uma flag de "carga
  concluída"; uma porta separada para o Actuator.

### Métricas no formato do Prometheus (ADR 0009)

- **Decisão (plano 002, research R3; etapa 2):** Micrometer com o registro do Prometheus, em
  `/actuator/prometheus`. A métrica `http.server.requests` tem histograma de 1 ms a 10 s, e o rótulo
  `application` vai em todas as séries.
- **Motivo:** o histograma permite calcular percentis e a fração abaixo de um limite no Prometheus, que é
  a base do SLO do Marco 4.
- **Alternativas rejeitadas:** percentis calculados na aplicação, que não se agregam entre instâncias.

### Imagem de container (ADR 0011)

- **Decisão (plano 002, research R8; etapa 3):** Dockerfile multi-stage, com
  `eclipse-temurin:25-jdk-alpine` no build e `eclipse-temurin:25-jre-alpine` na execução, fixados por
  digest. O JAR é extraído em camadas, o usuário é 10001, o healthcheck usa a readiness, e o `ENTRYPOINT`
  é em forma exec, com a memória no próprio `ENTRYPOINT` e não em `JAVA_TOOL_OPTIONS`.
- **Motivo:** é a menor imagem JRE oficial que ainda tem shell para o healthcheck com `PORT`. O digest
  torna o build reprodutível. A JVM como PID 1 recebe o `SIGTERM`.
- **Alternativas rejeitadas:** distroless, que não tem shell; runtime por `jlink`; Buildpacks; rodar os
  testes dentro do build da imagem.

## 2026-11-24 — Etapa 2 do Marco 2: `groups` na saúde geral, banner e formato ECS

- **`groups` na saúde geral (FR-011):**
  - *Origem:* implementador, no T030. Com os probes ligados, o `/actuator/health` responde
    `{"status":"UP","groups":["liveness","readiness"]}`. O Boot 4.1.2 sempre inclui a lista de grupos, e
    nenhuma propriedade a esconde. A FR-011 dizia "só o estado";
  - *Decisão (responsável):* aceitar a lista. A FR-011 e o US2-6 foram ajustados antes do código: a saúde
    geral pode trazer, além do `status`, só `groups` com exatamente `liveness` e `readiness`. O
    `HealthProbesIT` exige exatamente esses dois campos;
  - *Motivo:* são os nomes dos dois probes públicos, já publicados no contrato operacional, sem
    componentes, banco, caminhos nem versões;
  - *Alternativas rejeitadas:* esconder `groups` com código próprio; não expor a saúde geral. As duas
    exigiriam código para esconder nomes que já são públicos.
- **Banner com o JSON ligado (correção de uma premissa):**
  - *Observado:* com o logging estruturado ligado, o Boot 4.1.2 já não escreve o banner (0 ocorrências na
    execução `json-logs`, contra 68 na execução em texto). A segunda análise da spec supunha que o banner
    quebraria o JSON, e isso não vale para esta versão;
  - *Efeito na decisão:* a decisão de desligar o banner em todas as execuções continua, pelo motivo
    principal dado pelo responsável (o banner não traz informação). O efeito real do
    `spring.main.banner-mode: off` é tirá-lo do log em texto do `./mvnw`, provado pelo `TextLogFormatIT`.
    A FR-020 e a research R6 foram corrigidas.
- **Formato ECS aninhado:** o Boot 4.1.2 escreve os campos ECS aninhados
  (`{"log":{"level":…},"process":{"thread":{"name":…}}}`), e não com a chave pontuada. O `LogOutput` dos
  testes lê os dois jeitos. Research R6 e data-model atualizados.
- **Content type do Actuator:** `application/vnd.spring-boot.actuator.v3+json`. Para o Spring, ele não é
  compatível com `application/json`, então o teste confere o sufixo `+json`. Contrato operacional
  corrigido.
- **`ReadinessInvalidLinesIT` com `@DirtiesContext`:** ele usa o mesmo CSV do `InvalidLinesIT`, que lê o
  log da inicialização. O contexto é fechado no fim da classe, pelo mesmo motivo do `EmptyLoadIT` na
  etapa 1.
- **Itens "verificar" resolvidos:** R1, porque o starter do Actuator já traz as métricas; R3, porque
  `management.metrics.tags.application` funciona e o `MetricsConfiguration` não foi necessário.
- **Resultado da etapa:** `./mvnw verify` verde. Execução padrão com 189 ITs (5 pulados, que são o
  `JsonLogFormatIT`, só da `json-logs`); execução `json-logs` com 55 ITs de 12 classes; 75 s no total.
  Conferência manual com o JAR nos dois formatos.

## 2026-11-23 — Etapa 1 do Marco 2: filtro de ETag próprio e contexto do `EmptyLoadIT`

- **Filtro de ETag próprio (research R4):**
  - *Origem:* implementador, no T012. Com o `ShallowEtagHeaderFilter` do Spring, o teste
    `IntervalsEtagIT.wildcardIsNotModified` falhou: `If-None-Match: *` devolvia 200 com corpo. A FR-003
    e a RFC 9110 (seção 13.1.2) exigem 304, porque `*` casa com qualquer representação atual. Os demais
    casos (valor único, lista, forma fraca, malformado, 400 e 405 sem ETag) passaram com o filtro do
    Spring;
  - *Decisão (implementador, para revisão do responsável no PR):* um filtro próprio,
    `ContentEtagFilter`, que faz o mesmo que o do Spring (corpo guardado, ETag forte `"0" + MD5`, só em
    2xx de GET e HEAD) e compara com as classes do Spring (`ETag.parse` e `ETag.compare`, comparação
    fraca), tratando o `*` explicitamente. O formato do ETag é o mesmo;
  - *Alternativas rejeitadas:* estender o `ShallowEtagHeaderFilter`, cujo método que decide o 304 é
    privado; tirar o `*` da spec, que contrariaria a RFC 9110.
- **`EmptyLoadIT` com contexto próprio:**
  - *Origem:* o `EmptyLoadIT` (Marco 1) falhou ao rodar com o `EtagEmptyIT`, que usa o mesmo
    `header-only.csv`. O pacote `etag` roda antes de `ingestion`, então o contexto já existia no cache,
    e o resumo da carga não aparecia mais na saída capturada pelo `EmptyLoadIT`;
  - *Decisão (implementador):* as duas classes internas do `EmptyLoadIT` ganharam
    `@DirtiesContext(classMode = BEFORE_CLASS)`, o mesmo padrão que o `DefaultFileIT` já usa. Nenhum
    resultado esperado mudou;
  - *Alternativa rejeitada:* dar ao `EtagEmptyIT` um CSV próprio. Esconderia a fragilidade, que é de
    quem lê o log da inicialização.
- **Resultado da etapa:** `./mvnw verify` verde, com 160 ITs (136 do Marco 1 e 24 novos).

## 2026-11-23 — Correções da segunda análise da spec 002

- **Origem:** segunda rodada do `/speckit-analyze` da spec 002, sem achados críticos. O responsável
  aprovou as correções de H1, H2, M1, M2, M3 e M4. Os achados de nível LOW continuam fora.
- **H1, memória da JVM no `ENTRYPOINT`, e não em `JAVA_TOOL_OPTIONS`:**
  - *Problema:* com `JAVA_TOOL_OPTIONS` definida, a JVM sempre escreve `Picked up JAVA_TOOL_OPTIONS:
    ...` no stderr, fora do log. Essa linha não é JSON e quebraria o SC-005 e o passo 5 do teste de
    fumaça no container;
  - *Decisão:* `-XX:MaxRAMPercentage=75` vai direto no `ENTRYPOINT` em forma exec, e a imagem não define
    `JAVA_TOOL_OPTIONS`;
  - *Impacto:* research R8, `contracts/container.md`, data-model e T036.
- **H2, banner desligado em todas as execuções (opção b):**
  - *Decisão (responsável):* `spring.main.banner-mode: off` no `application.yml`, em todas as execuções
    e nos dois formatos;
  - *Motivo:* o banner não traz informação. Ele é escrito direto no stdout, fora do logging, e em JSON
    viraria um evento com desenho ASCII (com `banner-mode: log`) ou linhas soltas que não são JSON (com o
    padrão `console`);
  - *Alternativas rejeitadas:* (a) `banner-mode: log`, que poria o desenho ASCII dentro da `message` de
    um evento JSON; (c) desligar só na imagem, que deixaria o JSON quebrado em qualquer outra execução
    com a variável, contra a FR-020;
  - *Efeito no Marco 1:* o log do `./mvnw spring-boot:run` perde o banner, e o resto do texto não muda.
    O SC-005 passou a dizer "mesmo texto do Marco 1, sem o banner";
  - *Efeito nos testes:* os testes de US4 da etapa 2 passam a falhar antes da implementação (pelo
    banner), e a mudança no `application.yml` os faz passar;
  - *Impacto:* spec (clarificação, FR-020 e SC-005), research R6, data-model, `contracts/container.md`,
    quickstart, plano e tarefas T024, T025, T026, T028 e T054.
- **M1, referências aninhadas da `trivy-action`:**
  - *Contexto:* o ataque de 2026 à cadeia do Trivy atingiu o `aquasecurity/setup-trivy`, que a
    `trivy-action` usa por dentro. O hash do nosso `uses:` não fixa o que uma action composta chama;
  - *Verificação (implementador, 2026-11-23, API pública do GitHub, porque o `gh` não está instalado):*
    - `trivy-action` v0.36.0, de 2026-04-22: `setup-trivy@3fb12ec…` (v0.2.6) e `actions/cache@27d5ce7…`
      (v5.0.5), os dois por hash;
    - `setup-trivy` nesse hash: `actions/cache/restore` e `actions/cache/save` (`9255dc7…`, v5.0.1) e
      `actions/checkout` (`8e8c483…`, v6.0.1), por hash. O `install.sh` vem de um commit fixo
      (`75c4dc0…`);
    - o `install.sh` confere o SHA-256 do binário contra o arquivo de checksums da release;
  - *Decisão (responsável):* como toda a cadeia aninhada está por hash, a `trivy-action` fica, fixada na
    v0.36.0 ou numa versão posterior que mantenha isso. Se uma versão tiver referência aninhada por tag
    ou branch, a action é trocada pela instalação do binário oficial do Trivy, com o checksum conferido;
  - *Revisão contínua:* todo PR do Dependabot que atualize uma action composta repete a verificação;
  - *Impacto:* research R10, R12 e R13, plano (riscos) e T042, que também passa a aceitar `curl` na API
    pública no lugar do `gh`.
- **M2, `check-workflows.sh` sem o `yq`:**
  - *Decisão (responsável):* sem o `yq` no `PATH`, o script termina com código 2 e uma mensagem clara,
    sem pular nem passar sem verificar;
  - a verificação oficial é a do job `workflow-policy` do pipeline;
  - *Impacto:* T040 e T045.
- **M3, T018(b):** a prova de que os dados já estão disponíveis passa a ser `totalElements` igual a 206,
  porque a listagem devolve no máximo 100 itens por página.
- **M4, base da sonda do `check-docker-context.sh`:**
  - *Decisão (responsável):* a sonda usa a imagem do estágio de build, lida do próprio `Dockerfile`, e
    não um `busybox` com digest próprio;
  - *Motivo:* o Dependabot só atualiza os digests do `Dockerfile`, e um digest só no script ficaria
    desatualizado sem aviso;
  - *Impacto:* T033a e research R13 e R14.

## 2026-11-23 — Correções do `/speckit-analyze` da spec 002

- **Origem:** análise cruzada de spec, plano e tarefas da spec 002, antes da implementação. O
  responsável aprovou as correções de C1, G1, G2, A1, A2, A3, I1 e G3. A U1 tem entrada própria, abaixo.
  Os achados de nível LOW ficaram de fora.
- **C1, comprovação do workflow pelo pipeline (crítico, sem emenda):**
  - *Problema:* parte da FR-028b (permissões, nenhum segredo, nenhuma publicação) e a FR-029
    (Dependabot) eram comprovadas pela revisão do PR. Para requisitos de pipeline, o Princípio VI pede a
    execução do pipeline;
  - *Decisão (responsável):* estender o script da T040, que passa a ser o `check-workflows.sh`. Ele
    verifica hash de commit, `permissions`, `secrets.`, `push: true` e `docker push`, e a configuração
    do Dependabot. O job `pinning` vira `workflow-policy`;
  - *Alternativa rejeitada:* emendar o Princípio VI para aceitar a revisão.
- **G1, conteúdo da imagem e `.dockerignore`:** dois passos novos no teste de fumaça. O passo 10 confere
  que não há código-fonte, Maven nem compilador na imagem final. O passo 11 roda o
  `check-docker-context.sh`, que constrói uma imagem descartável com o mesmo contexto e confere a lista de
  permissão (FR-021 e FR-026).
- **G2, CodeQL reprova o pipeline:**
  - *Decisão (responsável):* o defeito que prova a regra (uma classe com SQL concatenado, regra
    `java/sql-injection`) **não pode ficar no código do marco**;
  - a prova acontece no branch temporário da T050, que nunca é mergeado e é apagado depois, local e
    remotamente, sem cherry-pick, merge nem rebase a partir dele;
  - entra no marco só o registro do resultado: branch, links das execuções, job vermelho por defeito e a
    confirmação de que o branch foi apagado;
  - a SC-008 ganhou as categorias "achado alto na análise de código" e "workflow fora da política".
- **G3, arquivo montado sem permissão de leitura:**
  - *Decisão (responsável):* o teste precisa funcionar no Windows e no Linux, ou ser pulado com motivo
    explícito onde a permissão não se aplica;
  - *Como:*
    - o `smoke-test.sh` converte os caminhos dos bind mounts no Git Bash (`cygpath -w`,
      `MSYS_NO_PATHCONV=1`);
    - o passo 13 começa com uma sonda: um container com o usuário 10001 tenta ler o arquivo com
      `chmod 000`. Se não lê, o passo roda. Se lê, a permissão do host não vale no container (NTFS no
      Git Bash, Docker Desktop), e o passo é pulado com `PULADO`, o motivo, o sistema e a versão do
      Docker;
    - no pipeline (`CI=true`), no runner Linux, uma sonda que lê é falha, e o caso de borda nunca fica
      sem prova;
  - *Motivo da sonda:* o critério é o comportamento observado, e não o nome do sistema. Um Linux em que
    a permissão não valha também é detectado.
- **A1, sobrescrever o formato do log no container:** a T037 verifica se um
  `LOGGING_STRUCTURED_FORMAT_CONSOLE` vazio volta o log para texto, e o passo 12 do teste de fumaça
  comprova. Se o valor vazio falhar, a alternativa vai ao responsável antes de qualquer mudança na spec.
- **A2, A3 e I1 (redação e alinhamento):**
  - FR-015 e US3-5: o valor fixo da rota de caminhos inexistentes pode ser compartilhado com os recursos
    estáticos, e o status 404 distingue os dois casos;
  - US1-7: arquivos "de resultados diferentes" dão ETags diferentes;
  - estrutura e matriz do plano alinhadas às tarefas (`EtagEmptyIT`, `ReadinessInvalidLinesIT`,
    `PrometheusText` e os scripts novos).
- **Impacto:** spec 002 (FR-015, FR-032, US1-7, US3-5, SC-008 e casos de borda), plano (estrutura, matriz
  e riscos), research R3, R9, R12 e R14, `contracts/ci-pipeline.md`, quickstart e `tasks.md` (T023,
  T033, T033a novo, T034, T037, T039, T040, T043, T045, T047, T049 e T050).

## 2026-11-23 — Resposta 500 fora da FR-006 (achado U1 do `/speckit-analyze` da spec 002)

- **Origem:** achado U1 da análise da spec 002. A FR-006 exigia que as respostas 400, 405 **e 500**
  ficassem sem `ETag`, e um caso de borda exigia que um 500 aparecesse nas métricas e no log. Nenhuma
  tarefa comprovava o 500, porque a API não tem caminho que produza esse status sem um defeito ou um
  mock.
- **Decisão (responsável):** opção A da análise. A FR-006 passa a cobrir só 400 e 405, e o caso de
  borda do 500 saiu da spec. A research R4 registra que:
  - o filtro de ETag só age em respostas 2xx;
  - as métricas registram qualquer status, inclusive 500, pelo comportamento do próprio Spring;
  - não há requisito próprio para o 500.
- **Motivo:** um requisito sem comprovação não é entregue (Princípio VI). Provocar um 500 só para
  testá-lo exigiria um defeito ou um mock, contra o Princípio I. O comportamento continua garantido pelo
  framework.
- **Alternativas rejeitadas:**
  - B, manter o 500 como "garantido por construção", sem teste: violaria o Princípio VI;
  - C, provocar um 500 real com `SHUTDOWN` no H2 num IT próprio: comprovaria o 500, mas com um teste que
    derruba o banco para forçar um estado que a API não produz em uso normal.
- **Impacto:** spec 002 (clarificação, FR-006 e casos de borda), research R4 e data-model. As tarefas não
  mudam: o T002(i) e o T003(e) já cobrem 405 e 400.

## 2026-11-23 — Plano 002: um bolt por marco, falha de inicialização no JSON e Princípio V

Três decisões do responsável na revisão do plano 002, antes das tarefas.

### 1. Um bolt por marco, com etapas por assunto

- **Origem:** a research R16 do plano 002 dividia o Marco 2 em seis bolts, cada um com branch e PR, um
  por papel e assunto.
- **Decisão (responsável):** um único bolt por marco. O Marco 2 é entregue no branch
  `marco/2-operacao`, com um único PR, dividido em seis etapas por assunto e com um commit por etapa.
  Os papéis (implementador, infraestrutura e documentação) são os responsáveis por etapa, e não bolts
  separados.
- **Motivo:**
  - um extra só entra completo, e o pipeline só serve de portão de merge quando tudo o que ele verifica
    já existe;
  - PRs intermediários deixariam o marco pela metade na `main`;
  - um commit por etapa mantém a revisão por assunto dentro do PR único, como nos bolts 5 e 6 do Marco 1
    (entrada "Bolts 5 e 6 entregues juntos").
- **Alternativa rejeitada:** seis bolts com branch e PR próprios, como na primeira versão da R16.
- **Regras mantidas:** cada etapa começa pelos testes e termina com `./mvnw verify` verde; o limite de
  três agentes e o dono único de cada arquivo compartilhado valem por etapa; PR, merge e tag continuam
  com o responsável.
- **Impacto:** research R16, plano 002 (resumo e riscos), quickstart e a história 6 da spec ("portão de
  merge do bolt do marco").

### 2. Evento JSON da falha de inicialização sem rastro obrigatório

- **Origem:** a US4-3 da spec 002 pedia a mensagem **e a pilha** da exceção no mesmo evento JSON, usando
  a falha por arquivo ausente como exemplo. O plano apontou que o Boot não registra a pilha quando um
  `FailureAnalyzer` trata a falha, que é o caso do `MovieFileFailureAnalyzer`, e previa procurar outra
  falha, sem analisador, só para ter uma pilha.
- **Decisão (responsável):** o evento JSON da falha de inicialização traz a mensagem de erro. O rastro
  da exceção só aparece quando existe, no mesmo evento, porque o analisador de falhas o substitui por uma
  mensagem clara.
- **Motivo:** a mensagem clara do Marco 1 (spec 001, FR-005 e SC-006) é o comportamento desejado.
  Provocar uma falha artificial só para mostrar uma pilha testaria o formato do Boot, e não a aplicação.
- **Alternativa rejeitada:** manter a pilha obrigatória e testar com uma falha que nenhum analisador
  trata.
- **Impacto:** spec 002 (clarificação, US4-3 e FR-017), research R6 e o risco correspondente do plano,
  que saiu.

### 3. Princípio V: ETag, health e métricas ligados no `./mvnw` (emenda 1.4.1)

- **Origem:** o plano 002 deixou ETag, health e métricas ligados no modo padrão, sem perfil nem chave, e
  registrou isso como uma leitura do Princípio V. O texto do princípio listava o ETag entre os extras que
  "MUST ser ativados só pelo seu perfil ou pela sua configuração".
- **Decisão (responsável):** aceitar a leitura do plano e emendar o texto (PATCH, 1.4.0 → 1.4.1):
  - extras com infraestrutura própria (PostgreSQL, Kafka, Prometheus e Grafana em execução, front-end,
    imagem e deploy) continuam ativados só pelo seu perfil ou pela sua configuração;
  - extras da API sem infraestrutura própria (listagem, Swagger UI, ETag, health e métricas) podem ficar
    ligados no modo padrão, desde que não alterem o comportamento avaliado;
  - a autenticação continua desligada por padrão (Princípio VII).
- **Motivo:**
  - ETag, health e métricas não mudam o contrato de intervalos, os resultados nem os testes do
    enunciado. A suíte do Marco 1 passa sem alteração (SC-002 da spec 002);
  - o briefing já põe esses itens na camada "Extra na API", que roda no `./mvnw`;
  - o objetivo do isolamento é não exigir infraestrutura no modo padrão, e esses extras não têm nenhuma.
- **Por que PATCH:** a emenda esclarece a intenção do texto, sem princípio novo nem regra removida. A
  listagem e o Swagger UI do Marco 1 já ficavam ligados no modo padrão.
- **Alternativa rejeitada:** uma propriedade de liga e desliga para cada extra da API: configuração sem
  uso, contra a regra de simplicidade do próprio Princípio V.
- **Impacto:** constituição 1.4.1, com o Sync Impact Report atualizado, e o Constitution Check do plano
  002. Spec 001 e `AGENTS.md` sem mudança.

### 4. README no escopo do Marco 2, como etapa 6 do bolt

- **Origem:** o pedido de tarefas do responsável ordena as etapas como ETag, observabilidade,
  Dockerfile, pipeline e Dependabot, ADRs e atualização do README. A spec 002 deixava o README fora do
  escopo, e a R16 juntava logs e JaCoCo numa etapa própria.
- **Decisão (responsável, aplicada pelo implementador antes das tarefas, Princípio IV):**
  - a spec ganhou a FR-034 (o que o README passa a documentar), comprovada por revisão, com a seção
    "Revisão do README" no checklist (Princípio VI, 1.4.0);
  - a R16 passou a ter as seis etapas do pedido: os logs entram na etapa de observabilidade, e o JaCoCo
    na de pipeline, com o implementador como responsável pelo `pom.xml`.
- **Impacto:** spec 002 (FR-032, FR-034 e premissas), checklist, research R16, plano (resumo e matriz) e
  `tasks.md`.

## 2026-11-23 — Emenda 1.4.0 da constituição: comprovação por tipo de requisito (Princípio VI)

- **Origem:** clarificação da spec 002. O Princípio VI exigia que todo requisito apontasse para um
  teste de integração. A spec 002 tem requisitos que não são comportamento da aplicação: ADRs (FR-030 e
  FR-031), imagem e pipeline (FR-021 a FR-029). A decisão de comprovar os ADRs por revisão (entrada
  abaixo) deixava a spec em desacordo com o princípio.
- **Decisão (responsável):** emenda MINOR no Princípio VI. Cada requisito aponta para a forma de
  comprovação adequada ao seu tipo:
  - **comportamento da aplicação:** teste de integração, como antes (Princípio I);
  - **documentação** (README, ADRs e guias): revisão no pull request, com item no checklist da spec;
  - **pipeline e imagem** (teste de fumaça do container, análise de segurança, cobertura mínima e lint
    do Dockerfile): execução do pipeline no GitHub Actions.

  Requisito de comportamento da aplicação continua exigindo teste de integração e não pode ser
  comprovado só por revisão ou pelo pipeline.
- **Motivo:**
  - o conteúdo de um documento só uma leitura avalia, e um teste confirmaria só que as seções existem;
  - o container e o pipeline só existem no GitHub Actions. Um teste de integração no `./mvnw verify`
    não os exercita, e trazê-los para lá exigiria Docker no modo padrão, contra o Princípio II;
  - sem a emenda, a spec teria de criar testes de fachada ou deixar requisitos sem comprovação.
- **Alternativas rejeitadas:**
  - manter o Princípio VI e abrir exceções em cada spec: a spec não pode abrir exceção a um princípio,
    o mesmo problema que levou à emenda 1.2.0;
  - verificar ADRs por um estágio do pipeline: teste de forma, não de conteúdo (entrada abaixo).
- **Impacto:**
  - constituição 1.3.0 → 1.4.0 (MINOR, compatível: todo requisito que cumpria a versão anterior
    continua cumprindo), com o Sync Impact Report atualizado;
  - `AGENTS.md`, regra de rastreabilidade do fluxo de trabalho;
  - spec 002: a FR-032 cita o Princípio VI;
  - spec 001: nenhuma mudança, porque todos os seus requisitos são de comportamento.

## 2026-11-23 — ADRs comprovados por revisão (spec 002)

- **Origem:** clarificação da spec 002. O Princípio VI pede que cada requisito aponte para um teste, e
  os ADRs (FR-030 e FR-031) são documentos, sem comportamento a testar.
- **Decisão (responsável):** os ADRs são comprovados por revisão no pull request, pela seção "Revisão
  dos ADRs" do checklist da spec (`specs/002-operacao/checklists/requirements.md`). Ela confere três
  pontos:
  - cada ADR tem contexto, decisão, alternativas consideradas e consequências;
  - cada ADR cita a entrada correspondente neste arquivo;
  - há um ADR para cada decisão de arquitetura listada na FR-030.

  Não há verificação automatizada no pipeline.
- **Motivo:** o que importa num ADR é o conteúdo, e só uma leitura avalia isso. Um teste no pipeline só
  confirmaria que as seções existem.
- **Alternativas rejeitadas:**
  - estágio do pipeline que confere a existência e as seções dos ADRs: um teste de forma, não de
    conteúdo;
  - registrar a decisão só aqui, sem emenda: deixaria a spec em desacordo com o Princípio VI. Foi
    substituída pela emenda 1.4.0 (entrada acima).
- **Efeito:** as decisões da FR-030 que ainda não têm entrada aqui (por exemplo, a stack e o Problem
  Details, decididos no plano 001) ganham a entrada junto com o ADR (FR-031).
- **Impacto:** spec 002 (clarificação de 2026-11-23, história 7, FR-031, FR-032 e SC-009) e o checklist
  da spec.

## 2026-11-23 — Critérios de reprovação do pipeline: segurança e cobertura (spec 002)

- **Origem:** clarificação da spec 002. A FR-028 pedia análise de segurança e cobertura no pipeline,
  sem dizer o que reprova.
- **Decisão (responsável):**
  - **segurança:** achados de severidade alta ou crítica reprovam o pipeline. Na análise de
    dependências e na da imagem, contam só as vulnerabilidades com versão corrigida disponível. Achados
    médios e baixos aparecem no relatório, sem reprovar;
  - **cobertura:** medida pelos testes de integração (JaCoCo sobre a execução do Failsafe), com um
    mínimo de linhas abaixo da cobertura atual medida, com piso de 80% (valor final de 90%, abaixo).
- **Medição (implementador):**
  - commit `39b3188` (fim do Marco 1, `main`), JaCoCo 0.8.14 chamado pela linha de comando
    (`prepare-agent-integration`, `verify` e `report-integration`), sem alterar o `pom.xml`;
  - 136 ITs, sem falhas nem testes pulados;
  - **linhas: 330 de 340, 97,06%**; ramos: 109 de 115, 94,78%;
  - as 10 linhas não cobertas estão no `main` da `AwardsApplication`, em construtores e métodos
    exigidos pelo JPA nas entidades `Movie` e `Producer` e em uma linha de cada um de
    `InvalidParameterException`, `MovieCsvReader`, `MovieFileProperties` e `MovieFileLoader`.
- **Valor escolhido (responsável):** **90% de linhas**, acima do piso de 80%. O implementador havia
  proposto 95%, e o responsável ajustou para 90% porque:
  - uma margem de 2 pontos é pequena para 340 linhas: 7 linhas novas sem cobertura já bastariam para
    reprovar;
  - o Marco 3 acrescenta código que só roda no perfil Docker (PostgreSQL e Kafka), fora do `./mvnw
    verify` padrão, e que portanto reduz a cobertura medida.
- **Motivo:**
  - uma vulnerabilidade alta sem versão corrigida não tem ação possível no bolt, e reprovar por ela
    travaria o merge sem ganho;
  - os achados médios e baixos ficam visíveis no relatório, para decisão caso a caso;
  - a cobertura vem dos testes de integração porque, pelo Princípio I, eles são os únicos testes do
    back-end. Um mínimo tirado da medição evita um número arbitrário.
- **Alternativas rejeitadas:**
  - cobertura só relatada: não impediria que um bolt entregasse código sem teste, contra o Princípio VI;
  - mínimo fixo de 80%: deixaria cair 17 pontos sem nenhum aviso;
  - mínimo de 95%, proposto pelo implementador: margem pequena demais, pelos motivos acima;
  - reprovar também por vulnerabilidades sem correção: bloquearia por algo que o projeto não consegue
    resolver.
- **Impacto:** spec 002 (clarificação de 2026-11-23, história 6, FR-028, FR-028a e SC-008). O plugin
  entra no `pom.xml` no bolt do pipeline, pelo plano.

## 2026-11-23 — Emenda 1.3.0 da constituição: log em texto no `./mvnw`, JSON no container

- **Origem:** clarificação da spec 002 (Marco 2, operação). A constituição 1.1.0 dizia que, a partir do
  Marco 2, "valem os logs estruturados em JSON", sem separar a execução local da execução em container.
  Com isso, o `./mvnw spring-boot:run` do avaliador passaria a mostrar JSON no terminal.
- **Decisão (responsável):**
  - na execução local padrão (`./mvnw spring-boot:run`), o log continua no texto padrão do framework;
  - o JSON é ligado por uma variável de ambiente. O Dockerfile define essa variável na imagem, e ela
    também liga o JSON em qualquer outra execução;
  - a constituição foi emendada: os logs em JSON valem para a execução em container e para os ambientes
    operacionais, e a execução local padrão usa texto.
- **Motivo:**
  - quem avalia roda o `./mvnw` e lê o log no terminal, onde o texto é legível e o JSON não é;
  - o JSON existe para ser lido por coletores de log, que só estão presentes no container e nos
    ambientes operacionais;
  - com uma só variável, o mesmo artefato serve aos dois usos, e o formato é escolhido no ambiente, e
    não no build.
- **Alternativas rejeitadas:**
  - JSON sempre, com texto opcional por variável: cumpria a redação anterior, mas piorava a leitura do
    terminal justamente no modo que o avaliador usa;
  - JSON sempre, sem opção de texto: mesmo problema, e sem como voltar ao texto;
  - formato escolhido por perfil do Spring: misturaria o formato do log com a escolha de banco e de
    ingestão, que é o papel dos perfis.
- **Impacto:**
  - constituição 1.2.0 → 1.3.0 (MINOR), seção "Restrições técnicas e de segurança" > Observabilidade;
  - `AGENTS.md`, linha "Logs" da tabela de stack;
  - spec 002: clarificação de 2026-11-23, história 4, FR-017, FR-018, FR-020, FR-024a, FR-030 e SC-005.
    Os testes que verificam o log passam a valer nos dois formatos;
  - spec 001: nenhuma mudança.

## 2026-11-22 — Três ambiguidades apontadas pelo testador adversário

- **Origem:** rodada do Codex como testador adversário do Marco 1, com os casos em
  `docs/ai/adversario/`. O testador escreveu os CSVs a partir da spec, sem ler o código de produção.
  Os casos 12 e 13 foram entregues sem resultado esperado, porque a spec não determinava uma resposta
  única. O caso 09 usa anos (0, 2147483648, 4294967296) que a spec não proibia.
- **Decisões (responsável):**
  1. **`and` sem diferenciar maiúsculas (caso 12).** `AND`, `And` e `aNd` são separadores, como `and`.
     A regra vale só para o separador: os nomes continuam comparados exatamente como escritos (FR-008).
     Spec: FR-007.
  2. **Ordem Unicode no desempate por nome (caso 13).** Os nomes empatados são ordenados por
     `String.compareTo`, caractere a caractere, independente de locale. Spec: FR-015.
  3. **Ano válido de 1 a 9999 (caso 09).** Fora dessa faixa, a linha é rejeitada como inválida, com
     aviso no log. Spec: FR-006, FR-016b e o motivo novo `YEAR_OUT_OF_RANGE` do `data-model.md`.
- **Motivos:**
  - (1) a spec já não diferencia maiúsculas no `winner` nem no cabeçalho, e um `AND` no meio de nomes é separador para
    quem lê o arquivo. Tratar `Iris AND Nilo` como um único produtor inventaria um nome;
  - (2) a ordem por código de caractere é determinística, não depende da máquina do avaliador e é a
    ordem natural da linguagem. Uma colação linguística mudaria com o locale da JVM;
  - (3) o enunciado trata de anos de premiação. A faixa elimina o ano 0 e os valores que estouram
    `int`, sem exigir tipos maiores no banco e no contrato.
- **Alternativas rejeitadas:**
  - `and` só em minúsculas, como estava: o caso 12 mostrou que o resultado mudaria só pela caixa do
    separador;
  - colação com `java.text.Collator`: o resultado dependeria do locale;
  - anos sem limite, com `long` ou `BigInteger`: exigiria mudar o esquema e o tipo do contrato para um
    caso que não representa uma premiação real.
- **Efeito sobre os casos:** os resultados esperados dos casos 12 e 13 foram escritos pelo implementador
  a partir das decisões (1) e (2), e o README dos casos registra isso.
- **Caso 09 diverge do testador (responsável):** com a decisão (3), as linhas dos anos 0, 2147483647,
  2147483648 e 4294967296 são rejeitadas. Só sobram os anos 1 (`Orion`) e 2 (`Nova`), de produtores
  diferentes, então o esperado passa a ser `min` e `max` vazios. O `expected.json` original do testador
  fica intacto em `docs/ai/adversario/`, como registro da rodada. O esperado ajustado existe só na cópia
  de `src/test/resources/scenarios/adversario/`, que é a usada pelo `Case09ExtremeYearsIT`.
- **O que o caso 09 revelou:** o leitor aceitava o ano 0. O padrão `\d{1,9}` só verificava os dígitos,
  sem faixa. Com o arquivo do testador, a aplicação respondia com `Nova` de 0 a 2 em `min` e em `max`.
  Os anos de 10 dígitos já eram rejeitados, como "ano não numérico", uma mensagem enganosa. Agora todos
  saem com o motivo `YEAR_OUT_OF_RANGE`.
- **Impacto:** spec (sessão 2026-11-22, FR-006, FR-007, FR-015, FR-016b e casos de borda),
  `data-model.md`, research R3 e R4, matriz do `plan.md` e 13 ITs novos em
  `src/test/java/com/outsera/awards/adversario/`.

## 2026-11-22 — Decisões do Marco 1 que divergem do briefing (T082)

Registro consolidado no fechamento do marco. As decisões foram tomadas na clarificação da spec
(sessão de 2026-11-18) e no plano 001. Os detalhes ficam na spec e na research.

- **Linha inválida é ignorada com aviso, e a carga continua (FR-006).**
  - *Briefing:* "a carga inteira é rejeitada", marcado como decisão em aberto (`briefing.md` e
    `decisoes-planejamento.md`).
  - *Decisão (responsável, na clarificação):* só os erros estruturais (arquivo ausente, cabeçalho ou
    separador inválido) impedem a inicialização. Uma linha inválida é ignorada, com aviso no log (número
    da linha e motivo), e a carga termina com um resumo de lidas, aceitas e rejeitadas.
  - *Efeito:* uma linha ruim não derruba a aplicação nem esconde o resultado das linhas boas, e o aviso
    com o número da linha continua mostrando o problema.
  - *Alternativa rejeitada:* rejeitar a carga inteira, como no briefing.
- **Duplicata identificada por ano + título (FR-006d).**
  - *Briefing:* não tratava linhas repetidas.
  - *Decisão (responsável, na clarificação):* ano e título, sem os espaços em volta, identificam o
    filme. A primeira ocorrência prevalece, e as repetições são ignoradas com aviso.
  - *Efeito:* sem isso, uma linha vencedora repetida geraria um intervalo 0 falso para os seus produtores.
  - *Alternativa rejeitada:* contar cada linha como um filme.
- **Cabeçalho sem diferenciar maiúsculas, mas com ordem fixa (FR-003).**
  - *Briefing:* cabeçalho `year;title;studios;producers;winner`, sem dizer se a comparação é exata.
  - *Decisão (responsável, na clarificação):* `Year;Title;...` é aceito, e uma ordem diferente, uma
    coluna faltando ou uma a mais impedem a inicialização.
  - *Efeito:* a diferença de maiúsculas não muda o sentido do arquivo. Já uma ordem diferente indica um
    arquivo de outro formato, e aceitá-la exigiria mapear colunas por nome, sem pedido do enunciado.
  - *Alternativas rejeitadas:* comparação exata, que recusaria um arquivo inofensivo, e colunas em
    qualquer ordem.
- **Contrato em `/openapi.yaml`, e não em `/v3/api-docs` (research R8).**
  - *Briefing:* `GET /v3/api-docs` como contrato OpenAPI.
  - *Decisão (plano 001, research R8):* o contrato estático, escrito antes do código, é a única fonte
    e é publicado em `/openapi.yaml`. No bolt 6, isso levou a tirar o springdoc do projeto (entrada
    "Swagger UI pelo webjar, sem springdoc", abaixo).
  - *Motivo:* o briefing exige que o contrato seja a fonte do Swagger UI e da validação das respostas.
    Um contrato gerado pelo código seria um segundo contrato, que poderia divergir do desenhado.
  - *Alternativa rejeitada:* contrato gerado por anotações em `/v3/api-docs`.

## 2026-11-22 — Swagger UI pelo webjar, sem springdoc (bolt 6)

- **Origem:** implementador, na T076. Com `springdoc.api-docs.enabled=false`, o `/swagger-ui.html`
  respondia 404. Nas classes do springdoc 3.2.0, o `SwaggerConfig` (Swagger UI) tem
  `@ConditionalOnBean(SpringDocConfiguration.class)`, e a `SpringDocConfiguration` tem
  `@ConditionalOnProperty("springdoc.api-docs.enabled", matchIfMissing = true)`. Ou seja, nessa versão
  o Swagger UI do springdoc exige a geração automática do contrato ligada.
- **Decisão (responsável):** tirar o springdoc e servir o Swagger UI pelo webjar
  `org.webjars:swagger-ui` 5.34.0 (a mesma versão que o springdoc 3.2.0 traria), com o
  `webjars-locator-lite` gerenciado pelo Boot e uma página estática `static/swagger-ui.html` que aponta
  para `/openapi.yaml`.
- **Motivo:** mantém o contrato estático como fonte única (research R8). Não há contrato gerado pelo
  código, `/v3/api-docs` responde 404 e nada varre os controllers na inicialização.
- **Alternativas rejeitadas:**
  - springdoc com `api-docs` ligado: publicaria em `/v3/api-docs` um segundo contrato, gerado pelo
    código, que poderia divergir do estático;
  - springdoc com `api-docs` ligado e nenhum caminho gerado (`paths-to-exclude=/**`): `/v3/api-docs`
    continuaria publicado, vazio.
- **Fim de linha:** o `ApiDocsIT` compara `/openapi.yaml` byte a byte com o contrato de `specs/`. Nesta
  cópia de trabalho, o arquivo de `specs/` estava com CRLF, extraído antes do `.gitattributes`
  (`eol=lf`), e o teste falhava. O conteúdo versionado tem LF nos dois arquivos, então o arquivo foi
  extraído de novo. Outros 19 arquivos da cópia de trabalho estão na mesma situação e não foram
  tocados. Num clone novo, todos vêm com LF.
- **Impacto:** research R1 e R8, plano (stack e estrutura), T074 a T076 e `pom.xml`.

## 2026-11-22 — Detalhes da listagem de filmes (bolt 5)

- **Origem:** implementador, ao escrever os testes do bolt 5. A spec não dizia o que acontece com uma
  página além da última. Com `page=2147483647`, o deslocamento (`page` × `size`) passa de
  `Integer.MAX_VALUE`, o Spring Data recusa a consulta e a resposta seria 500.
- **Decisão (implementador, para revisão do responsável no PR):**
  - a spec ganhou um caso de borda antes do código (Princípio IV): uma página além da última responde
    200 com `content` vazio e os totais reais, e um `page` que não cabe em 32 bits é inválido (400);
  - quando o deslocamento passa do limite, o `MovieQueryService` consulta só a contagem;
  - todos os parâmetros inválidos da consulta aparecem em `invalidParams`, e não só o primeiro;
  - o `ResponseTimeIT` usa `@DirtiesContext`, para que a primeira chamada medida aconteça num contexto
    sem nenhuma requisição anterior, incluindo a inicialização preguiçosa do Spring MVC (SC-005).
- **Alternativas rejeitadas:**
  - recusar com 400 uma página além da última: o FR-021 aceita qualquer `page` maior ou igual a 0, e o
    US3-6 já responde 200 com lista vazia quando não há resultado;
  - limitar `page` a um máximo arbitrário: seria uma regra sem origem na spec.
- **T067:** os ITs de carga do bolt 2 (inclusive o `BomVariationsIT`, criado depois da T067) passaram a
  verificar os filmes por `GET /api/movies`, e o `JdbcTemplate` saiu da `ScenarioIT`. No
  `HarmlessVariationsIT`, a consulta à tabela `producer` não tem equivalente na listagem, que devolve os
  produtores como texto. O nome sem espaços em volta continua provado pelos intervalos
  (`Producer A` 1990→1991, classe `WithVariations`).
- **Impacto:** caso de borda novo na spec, research R7, T063 e T071.

## 2026-11-22 — Bolts 5 e 6 entregues juntos

- **Decisão (responsável):** os bolts 5 (listagem) e 6 (Swagger UI e fechamento do Marco 1) são
  entregues em um único branch, `bolt/5-6-listagem-swagger`, e em um único pull request, com um commit
  por etapa:
  1. fase 6 — listagem (T062 a T073);
  2. fase 7 — Swagger UI (T074 a T077);
  3. fase 8 — fechamento (T078 a T083).
- **Regras mantidas:** cada etapa começa pelos testes, que precisam falhar antes da implementação, e
  termina com `./mvnw verify` verde antes do commit. O PR, o merge e a tag continuam com o responsável.
- **Alternativa rejeitada:** um branch e um PR por bolt, como previa o mapeamento original do
  `tasks.md`.
- **Impacto:** mapeamento bolt → fase, checkpoints das fases 6 e 7, dependências e notas do
  `specs/001-producer-intervals-api/tasks.md`. Nenhuma mudança na spec nem no contrato.

## 2026-11-22 — Validador do contrato com a coordenada nova da Atlassian (T055)

- **Origem:** implementador, no bolt 4. Ao resolver
  `com.atlassian.oai:swagger-request-validator-core:3.0.0`, o Maven avisa que o artifact foi relocado
  pelo próprio projeto para `com.atlassian.oai:openapi-request-validator-core:3.0.0`.
- **Decisão (implementador, para revisão do responsável no PR):** declarar no `pom.xml` a coordenada
  nova, na mesma versão 3.0.0 da research R8. O código e os pacotes Java são os mesmos
  (`com.atlassian.oai.validator`), então o plano B (`networknt/json-schema-validator`) não foi
  necessário.
- **Exclusão:** o `jakarta.validation-api` que chega pelo `swagger-core` foi excluído, só nessa
  dependência de teste. Sem a exclusão, o Spring tentava montar um Bean Validation nos testes e
  registrava `Failed to set up a Bean Validation provider`, um comportamento que não existe em
  produção. Com a exclusão, o classpath de teste volta a ser igual ao de produção e os 78 testes
  passam.
- **Controle:** o `IntervalsContractIT` inclui um caso que adultera a resposta real com um campo a
  mais e exige que o validador a recuse (`additionalProperties: false`), para provar que a validação
  não é vazia.
- **T060:** nenhum ajuste foi necessário no `ProducerIntervalController` nem nos records. O controller
  mínimo do bolt 3 já cumpria o formato, e o T056 e o T057 passaram antes da implementação do bolt 4.
  Só o T058 falhou antes do T059, por falta do contrato em `static/`.

## 2026-11-22 — Anos do `disjoint-producers.csv` fora do `movielist.csv` (T041)

- **Origem:** conflito encontrado pelo implementador no bolt 3. A T041 pedia que o `Producer B`
  vencesse em 1960 e 2010, mas o SC-004a exige um arquivo sem nenhum ano em comum com o arquivo
  fornecido, que cobre 1980 a 2019.
- **Decisão (responsável):** seguir a spec, que tem prioridade sobre a task. O `Producer B` vence em
  1925 e 1975, e o intervalo continua 50. O texto da T041 foi corrigido.
- **Alternativa rejeitada:** manter 1960–2010. O cenário deixaria de cumprir o SC-004a.

## 2026-11-22 — Uma vitória a mais no `harmless-variations.csv` (T048)

- **Origem:** a T048 pede que as 4 linhas do `harmless-variations.csv` gerem ao menos um intervalo, e o
  arquivo do bolt 2 não tinha nenhum produtor com duas vitórias.
- **Decisão (implementador):** o filme de 1991 (`winner` igual a ` Yes `) passa a ser do
  `Producer A`, o mesmo do filme de 1990 (`YES`, com espaços em volta do nome). O intervalo
  `Producer A` 1990→1991 só aparece se o BOM, os espaços e as duas variações de `yes` forem tratados
  corretamente. Os testes do bolt 2 sobre esse arquivo continuam valendo sem alteração.

## 2026-11-22 — `ProducerNamesIT` absorvido pelo `ProducerSeparatorsIT` (T043)

- **Decisão (planejada no T022b):** os casos de separação de nomes passam a ser verificados por HTTP no
  `ProducerSeparatorsIT`, e o `ProducerNamesIT` e o `producer-names.csv`, que verificavam pelo banco,
  foram removidos (Princípio I).
- **Como os nomes aparecem nos intervalos:** cada forma está em duas vitórias de anos seguidos, e os
  nomes que se repetem entre formas estão em anos também seguidos. Todo intervalo correto vale 1, e
  `min` e `max` trazem a lista completa de nomes. Um nome separado errado vira um item a mais ou a
  menos, e dois nomes unidos por engano, ou um nome contado duas vezes no mesmo filme, geram um
  intervalo diferente de 1.

## 2026-11-22 — Separação de produtores em etapas, no lugar da regex (segunda revisão do Codex)

- **Origem:** segundo comentário do Codex, como revisor, no pull request do bolt 2, sobre a mesma regex
  do `ProducerNameParser`. A regex `\s*,\s*(?:and(?=\s|$)\s*)?|(?:^|\s+)and(?=\s|$)\s*`, adotada após a
  primeira revisão (entrada "`and` no fim do campo de produtores", de 2026-11-19), consome o espaço
  depois de um `and`, e o `and` seguinte deixa de ser reconhecido:
  - `Producer A and and Producer B` vira `Producer A` e `and Producer B`;
  - `and and` vira o produtor `and`, e a linha é aceita em vez de rejeitada.

  Os dois casos foram reproduzidos com testes antes da correção.
- **Decisão (responsável):** aceitar e, por ser o segundo defeito na mesma regex, trocá-la por uma
  separação em etapas:
  1. dividir o campo pelas vírgulas;
  2. em cada parte, a palavra `and` isolada (separada por espaços) é separador;
  3. o nome é o trecho original entre os separadores, então o texto interno é preservado;
  4. as partes vazias são descartadas.
- **Motivo:** cada etapa é simples de ler e de conferir caso a caso. Como o `and` é reconhecido pela
  palavra, e não pelos espaços em volta, os casos de início e fim do campo (primeira revisão) e de `and`
  repetido (segunda revisão) deixam de precisar de tratamento especial.
- **Alternativas rejeitadas:**
  - ajustar a regex de novo, por exemplo com lookbehind: seria a terceira correção na mesma expressão;
  - dividir por espaços e juntar as palavras com um espaço: normalizaria os espaços internos dos nomes,
    o que a FR-008 proíbe.
- **Verificação:**
  - o novo `ProducerNamesIT` (T022b) cobre todos os casos da research R4, mais `and and` (linha
    rejeitada, sem produtores), `Producer A and and Producer B` (2 produtores) e um nome com espaços
    internos duplos. Os casos antigos já passavam com a regex anterior e continuam passando;
  - a regex antiga e o parser novo foram comparados nas 206 linhas do `movielist.csv` do enunciado,
    sem nenhuma diferença.
- **Impacto:** FR-007 da spec (exemplos com `and` repetido), research R4 (decisão, histórico das duas
  revisões e casos), T030 (texto da separação), nova T022b e T043 (absorve o `producer-names.csv`, e o
  `ProducerNamesIT` sai nesse bolt). A verificação pelo banco é temporária (Princípio I), porque a
  listagem devolve os produtores como texto e não prova a separação.

## 2026-11-22 — BOM removido do `Reader`, antes do Commons CSV (revisão do Codex)

- **Origem:** comentário do Codex, como revisor, no pull request do bolt 2. O leitor removia o BOM só
  do primeiro campo, depois de o Commons CSV já ter analisado a linha. Isso é tarde demais em dois
  casos:
  - BOM seguido de cabeçalho entre aspas (`"year";...`): com o BOM na frente, o Commons CSV não
    reconhece a aspa, o campo vira `"year"` literal e a inicialização falha com `INVALID_HEADER`;
  - BOM seguido de linha em branco: a linha contém só o BOM, deixa de ser uma linha em branco e vira
    o cabeçalho, o que também dá `INVALID_HEADER`.

  Os dois cenários foram reproduzidos com testes antes da correção.
- **Decisão (responsável):** aceitar. O BOM é removido do `Reader` antes de ele ser entregue ao Commons
  CSV, lendo o primeiro caractere com `mark`/`reset`. O tratamento no primeiro campo foi retirado.
- **Motivo:** a FR-005a manda aceitar o BOM sem aviso, qualquer que seja o resto do arquivo. Tirar o BOM
  antes da análise faz o Commons CSV ver o arquivo exatamente como ele seria sem o BOM.
- **Alternativa rejeitada:** `BOMInputStream`, do Commons IO. O Commons IO só chega como dependência
  transitiva do Commons CSV, e usá-lo direto exigiria declará-lo no `pom.xml`: uma dependência nova só
  para um caractere.
- **Impacto:** research R3, tarefa T031 (a remoção do BOM sai da validação do cabeçalho e vai para a
  leitura) e nova tarefa T022a, com o `BomVariationsIT` e os cenários `bom-quoted-header.csv` e
  `bom-blank-line.csv`. A matriz do `plan.md` (FR-005a e FR-006c) foi atualizada.

## 2026-11-22 — Linha só com espaços e arquivo que não pode ser analisado (R3, T031)

- **Origem:** sonda do implementador com o Commons CSV 1.14.1, no início do bolt 2. O
  `ignoreEmptyLines` só pula linhas totalmente vazias. Uma linha só com espaços vira um registro de
  uma coluna vazia, que seria rejeitado por `COLUMN_COUNT`, contrariando a FR-005a ("linhas em branco
  em qualquer posição" são aceitas sem aviso). A spec também não dizia o que fazer com aspas sem
  fechamento ou com bytes fora de UTF-8.
- **Decisão (implementador, a confirmar pelo responsável no PR):**
  - um registro de uma coluna vazia é tratado como linha em branco;
  - aspas sem fechamento e bytes que não são UTF-8 válido dão erro estrutural `UNREADABLE`.
- **Motivo:** a linha só com espaços é uma linha em branco para quem edita o arquivo. No segundo caso,
  o leitor não consegue separar as linhas com segurança, e decodificar com substituição gravaria nomes
  corrompidos sem aviso.
- **Alternativa rejeitada:** rejeitar só a linha com aspas abertas. O Commons CSV lê até o fim do
  arquivo procurando o fechamento, então não há como saber onde a linha termina.
- **Impacto:** research R3 e tarefas T012, T013, T015 e T031, com dois cenários novos de falha de
  inicialização (`unterminated-quote.csv` e `invalid-encoding.csv`) e uma linha só com espaços no
  `harmless-variations.csv`.

## 2026-11-22 — Critério "sem `WARN`" restrito ao carregador do CSV (T014 e T015)

- **Origem:** observação do implementador ao fim do bolt 1. A saída do `./mvnw verify` já trazia
  `WARN` e `WARNING` que não vêm da aplicação: o aviso do Flyway sobre a versão do H2 (2.4.240, mais
  nova que a testada) e o aviso de agente dinâmico do Mockito. Com o critério "o log não contém
  `WARN`", o T014 e o T015 falhariam por causa de bibliotecas, e não do carregador.
- **Decisão (responsável):** o T014 e o T015 verificam apenas as linhas `WARN` emitidas pelo
  carregador do CSV (logger `MovieFileLoader`, pacote `com.outsera.awards.ingestion`), e não o log
  inteiro. O aviso do Flyway sobre a versão do H2 é mantido, sem supressão.
- **Motivo:** o que os testes precisam provar é que o carregador não rejeitou nem reclamou de
  nenhuma linha. Avisos de terceiros não dizem nada sobre isso e podem mudar a cada versão.
- **Alternativa rejeitada:** suprimir os avisos de terceiros na configuração de log. Esconderia
  informação útil e acoplaria o teste a uma lista de avisos externos.
- **Impacto:** texto do T014 e do T015 em `specs/001-producer-intervals-api/tasks.md`, com uma nota
  explicando o critério.

## 2026-11-22 — Mockito excluído do `spring-boot-starter-test`

- **Origem:** a mesma observação acima. O Mockito vem junto com o `spring-boot-starter-test` e emite
  um aviso de agente dinâmico ao se acoplar à JVM.
- **Decisão (responsável):** excluir `mockito-core` e `mockito-junit-jupiter` do
  `spring-boot-starter-test` no `pom.xml` (nova tarefa T011a, no início do bolt 2).
- **Motivo:** o Princípio I proíbe mocks, então o Mockito não tem uso no projeto. Tirá-lo do
  classpath transforma a regra em erro de compilação e elimina o aviso.
- **Alternativa rejeitada:** manter o Mockito e configurar o agente (`-javaagent`) no Failsafe para
  silenciar o aviso. Manteria disponível uma ferramenta que o projeto proíbe.
- **Impacto:** `pom.xml` e `tasks.md` (T011a). Nenhuma mudança na spec nem no contrato.

## 2026-11-19 — Emenda 1.1.0 da constituição: observabilidade a partir do Marco 2

- **Origem:** achado H1 do `/speckit-analyze` da spec 001. A constituição pedia logs em JSON e
  readiness, mas a spec do Marco 1 punha observabilidade fora do escopo.
- **Decisão (responsável):** emendar a constituição. Os logs estruturados em JSON e os probes de
  liveness e readiness passam a valer a partir do marco de observabilidade (Marco 2).
- **Motivo:**
  - o Marco 1 se limita ao que o enunciado avalia, e o enunciado não pede nenhum dos dois itens;
  - antecipá-los acrescentaria dependências e configuração ao modo padrão, que é o que o avaliador
    executa;
  - a garantia da readiness já existe no Marco 1, porque a carga termina antes de o servidor aceitar
    conexões (FR-001).
- **Alternativa rejeitada:** incluir logs em JSON e readiness no Marco 1.
- **Impacto:**
  - versão 1.0.0 → 1.1.0 (MINOR);
  - o `AGENTS.md` foi atualizado;
  - nenhuma mudança na spec 001;
  - no Marco 2, os testes que verificam mensagens de log precisam continuar passando com o formato
    JSON.

## 2026-11-19 — Emenda 1.2.0 da constituição: Princípio I reescrito

- **Origem:** achado C1′ do segundo `/speckit-analyze`. A exceção dos testes de carga tinha sido
  registrada na FR-027 da spec, mas a spec não pode abrir exceção a um princípio da constituição, que
  tem prioridade maior.
- **Decisão (responsável):** emendar a constituição, e não a spec. Novo texto do Princípio I:
  - todo teste é de integração, com `@SpringBootTest` e o contexto completo da aplicação, sem mocks;
  - o resultado é verificado preferencialmente por HTTP, e por log ou banco somente enquanto o
    comportamento não tem endpoint exposto;
  - os testes de falha de inicialização rodam a aplicação real com `SpringApplication`, sem mocks,
    e verificam a exceção e o log, porque um contexto que não sobe não pode ser verificado por
    `@SpringBootTest`.
- **Motivo:** permitir que cada comportamento seja testado no bolt em que nasce, mesmo antes de ter
  endpoint (como a carga do CSV), sem teste unitário e sem exceções espalhadas pelas specs.
- **Alternativas rejeitadas** para a falha de inicialização:
  - um `@SpringBootTest` externo que sobe a aplicação com falha por dentro: cumpre a letra do
    princípio, mas o contexto externo não teria função;
  - executar classes `@SpringBootTest` pelo `EngineTestKit`: acrescenta complexidade sem ganho.
- **Impacto:**
  - versão 1.1.0 → 1.2.0 (MINOR, compatível: todo teste que cumpria a versão anterior continua
    cumprindo);
  - a FR-027 deixou de ter exceções e só referencia o Princípio I;
  - o plano, as tarefas, a research e o `AGENTS.md` foram alinhados.

## 2026-11-19 — `and` no fim do campo de produtores (revisão do Codex)

- **Origem:** comentário do Codex, como revisor, no pull request do bolt 2. O leitor de CSV aplica
  `trim` no campo antes do parser de nomes (T031), então ` , and ` chega como `, and`. A regex da
  research R4, `\s*,\s*(?:and\s+)?|\s+and\s+`, exigia espaço depois do `and` e deixava `and` como
  nome de produtor. Com isso, a linha deixava de ser rejeitada e o caso `PRODUCERS_MISSING`
  (` , and `) do T017 falhava. O mesmo acontecia com `Producer A and`, que virava um único nome.
- **Decisão (responsável):** aceitar. O `and` passa a ser separador como palavra inteira, também no
  início e no fim do campo, com a regex `\s*,\s*(?:and(?=\s|$)\s*)?|(?:^|\s+)and(?=\s|$)\s*`. Nomes
  que contêm `and`, como `Anderson`, `Robbinsand` e `andrew`, continuam inteiros.
- **Motivo:** o parser não pode depender dos espaços em volta do campo, porque o `trim` do leitor os
  remove. A regex nova foi executada contra os casos antigos e os novos, que estão listados na R4.
- **Alternativa rejeitada:** tirar o `trim` do leitor de CSV. A regra continuaria frágil, e os outros
  campos (ano, título, `winner`) dependem do `trim`.
- **Impacto:** a R4, a T030 (regex), a T031 (nota sobre o `trim`) e as FR-007 e FR-009 da spec foram
  atualizadas. O T017 continua com o caso ` , and ` e passa a ser atendido. O T043 ganhou o caso
  `Producer A and`, que deve resultar em 1 produtor.
- **Briefing:** continua dizendo "`and` cercada de espaços" e não foi alterado. Ele é o registro do
  planejamento, e a fonte da verdade é a spec (FR-007).

## 2026-11-19 — Testes de carga antes dos endpoints (substituída pela emenda 1.2.0)

- **Origem:** achado C2 do `/speckit-analyze`. O Princípio I e a FR-027 falam em testes que "chamam
  os endpoints por HTTP", mas o bolt 2 (carga do CSV) ainda não tem endpoint de negócio.
- **Decisão (responsável):**
  - no bolt 2, os ITs de carga verificam o resultado pelo log e pelo banco, com a aplicação real no
    ar (`@SpringBootTest`);
  - nos bolts 3 e 5, os mesmos ITs ganham a verificação por HTTP;
  - os testes de falha de inicialização verificam a exceção e a mensagem, porque não chega a existir
    servidor.

  A exceção foi registrada primeiro na FR-027 da spec 001. Foi retirada de lá e passou para o
  Princípio I da constituição (emenda 1.2.0, acima).
- **Motivo:** manter os bolts pequenos e com os testes antes da implementação, sem criar testes
  unitários nem mocks.
- **Controle:** a tarefa T078 confere, no fechamento do marco, que todo IT de `ingestion/` (menos o
  `StartupFailureIT`) faz pelo menos uma chamada HTTP.
