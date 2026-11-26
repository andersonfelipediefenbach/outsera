# Feature Specification: Marco 5 — Publicação da API no Google Cloud Run

**Feature Branch**: `marco/5-deploy`

**Created**: 2026-11-26

**Status**: Draft

**Input**: User description: "Marco 5: deploy no Google Cloud Run, num único bolt. Projeto GCP outsera-awards, região southamerica-east1. Só o modo padrão (H2, carga do CSV), com a mesma imagem do pipeline, publicada no Artifact Registry. max-instances=1 e min-instances=0, o que mantém válido o cache em memória do ADR 0026 e limita o custo. Autenticação desligada, como no modo padrão. Deploy automático a cada merge na main, autenticado por OIDC com Workload Identity Federation, restrito a este repositório e à branch main, sem chaves no GitHub. Toda a configuração do GCP (APIs, Artifact Registry, conta de serviço com permissões mínimas, pool e provedor de identidade) fica num script versionado e idempotente, rodado pelo Claude Code com o gcloud depois do meu login. O pipeline confere a URL publicada depois do deploy (readiness e resultado de referência). O README ganha a URL pública e a seção do Marco 5. Nada muda no ./mvnw verify do modo padrão."

## Clarifications

### Session 2026-11-26

- Q: O que acontece com o tráfego quando uma revisão nova sobe mas não passa na conferência? → A: A revisão
  nova é implantada **sem tráfego** e conferida pela URL própria dela. O tráfego só passa para ela depois de a
  conferência aprovar, e a URL pública é conferida de novo depois da troca. Uma revisão reprovada nunca recebe
  tráfego, e a anterior continua servindo. US3, FR-014, FR-015 e FR-015a.
- Q: (análise, 2026-11-26) As métricas ficam públicas na URL publicada, já que a autenticação está desligada?
  → A: Não. A publicação define `MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=health`, que mantém só o que os
  probes usam; `/actuator/prometheus` responde 404 na URL pública. Decisão registrada no ADR 0027. FR-004 e
  FR-004a.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Avaliador usa a API publicada sem instalar nada (Priority: P1)

O avaliador abre o README, encontra a URL pública da API e chama `GET /api/producers/intervals` (ou abre o
Swagger UI) direto do navegador ou do terminal. Recebe o resultado de referência do arquivo fornecido, no
contrato exato do PDF, sem clonar o repositório, sem JDK e sem token.

**Why this priority**: é o valor do marco. Sem a URL funcionando, nenhuma outra parte (pipeline, script,
identidade) tem o que mostrar.

**Independent Test**: com o serviço publicado, uma chamada anônima à URL do README devolve 200 com Joel Silver
em `min` (1990–1991) e Matthew Vaughn em `max` (2002–2015), e a readiness devolve positivo.

**Acceptance Scenarios**:

1. **Given** o serviço publicado e sem nenhuma instância ativa, **When** o avaliador chama a URL de intervalos,
   **Then** recebe 200 com o resultado de referência, mesmo que a primeira resposta demore alguns segundos
   (partida a frio).
2. **Given** o serviço publicado, **When** qualquer pessoa chama os endpoints de negócio sem token, **Then**
   recebe a resposta normal, sem 401, como no modo padrão.
3. **Given** o serviço publicado, **When** o avaliador compara a resposta com a de `./mvnw spring-boot:run`
   local, **Then** o corpo é idêntico campo a campo.
4. **Given** o serviço publicado, **When** o avaliador abre o Swagger UI na URL pública, **Then** a
   documentação carrega e o "Try it out" de intervalos funciona.

---

### User Story 2 - Cada merge na main publica a nova versão sozinho, sem chaves guardadas (Priority: P1)

O Anderson faz o merge de um pull request na `main`. O pipeline roda os jobs de sempre; com todos verdes,
publica no registro de imagens a **mesma** imagem que acabou de passar no teste de fumaça e na análise de
vulnerabilidades, e implanta essa imagem no serviço. A autenticação no GCP usa a identidade de curta duração
emitida pelo GitHub para aquela execução, aceita só para este repositório e para a branch `main`. Não existe
chave de conta de serviço no GitHub nem no repositório.

**Why this priority**: sem a publicação automática, a URL envelhece a cada merge e deixa de mostrar o que
está na `main`. Sem a federação de identidade, a publicação exigiria uma chave de longa duração, o que a
constituição (Princípio VII) e a política do workflow proíbem.

**Independent Test**: um merge na `main` produz uma execução do pipeline em que o job de publicação fica
verde e a revisão nova do serviço aponta para a imagem pelo digest gerado naquela execução; um push em outra
branch ou um pull request não chega ao job de publicação, e a condição do provedor de identidade, conferida no
GCP, só aceita este repositório (pelo ID) e `refs/heads/main`.

**Acceptance Scenarios**:

1. **Given** um merge na `main` com todos os jobs do pipeline verdes, **When** o pipeline termina, **Then** a
   imagem testada está no registro, identificada pelo digest e pelo commit, e o serviço roda uma revisão nova
   com esse digest.
2. **Given** um push em outra branch ou um pull request (inclusive de fork), **When** o pipeline roda,
   **Then** o job de publicação não executa, e nada é publicado.
3. **Given** o provedor de identidade configurado, **When** a condição dele é lida no GCP, **Then** ela
   exige o ID deste repositório, o ID do dono e `refs/heads/main`, de modo que um token de outro repositório
   ou de outra branch não é aceito. A política do workflow impede qualquer job além do de publicação de pedir
   o token, então essa prova é pela condição, e não por uma tentativa real.
4. **Given** qualquer job anterior vermelho na `main`, **When** o pipeline termina, **Then** nada é
   publicado nem implantado.
5. **Given** dois ou mais merges seguidos na `main`, **When** os pipelines rodam, **Then** as publicações
   acontecem uma de cada vez, a ordem nunca se inverte, nenhuma é interrompida no meio e o último commit
   mergeado fica publicado. Um commit intermediário pode não ser publicado: o GitHub mantém só a execução
   pendente mais nova (research R7).

---

### User Story 3 - O pipeline confere a URL pública depois de cada publicação (Priority: P1)

O pipeline implanta a revisão nova sem tráfego e a chama pela URL própria dela, como um avaliador faria:
espera a readiness positiva e confere o resultado de referência no endpoint de intervalos. Só com a
conferência aprovada o tráfego passa para a revisão nova, e a URL pública é conferida de novo. Se algo não
confere dentro do tempo limite, o tráfego continua na revisão anterior, o job fica vermelho e o motivo aparece
no resumo da execução.

**Why this priority**: uma implantação que "terminou" não prova que a URL do README funciona. A conferência é
a comprovação do marco pelo Princípio VI (estágio do pipeline).

**Independent Test**: com uma revisão que não serve o resultado de referência, o job de conferência reprova;
com a revisão correta, aprova e registra no resumo a URL, o digest, a readiness e o resultado obtido.

**Acceptance Scenarios**:

1. **Given** uma revisão nova implantada sem tráfego, **When** a conferência roda na URL própria da
   revisão, **Then** ela espera a readiness positiva e depois confere Joel Silver em `min` (1, 1990, 1991) e
   Matthew Vaughn em `max` (13, 2002, 2015), sem campos extras.
2. **Given** a conferência da revisão aprovada, **When** o pipeline passa o tráfego para ela, **Then** a
   mesma conferência roda na URL pública e aprova.
3. **Given** a revisão nova que não responde ou responde diferente dentro do tempo limite, **When** a
   conferência termina, **Then** o tráfego continua 100% na revisão anterior, a URL pública segue servindo o
   resultado de referência e o job fica vermelho, com a resposta recebida no resumo.
4. **Given** a partida a frio do serviço, **When** a conferência faz a primeira chamada, **Then** a espera
   tolera essa demora sem reprovar.
5. **Given** a conferência na URL pública reprovou depois da troca, **When** o Anderson olha a execução,
   **Then** encontra no resumo o que falhou e, no runbook, como voltar o tráfego para a revisão anterior.

---

### User Story 4 - A configuração do GCP é reproduzível a partir do repositório (Priority: P2)

Toda a preparação do projeto no GCP (APIs, registro de imagens, contas de serviço com permissões mínimas,
pool e provedor de identidade, acesso público ao serviço) está num script versionado. Depois do login do
Anderson no `gcloud`, o Claude Code roda o script; rodá-lo de novo não muda nada nem falha. Quem lê o script
sabe exatamente quais permissões existem e por quê.

**Why this priority**: é o que torna a publicação auditável e refazível. Fica atrás das três primeiras porque
roda uma vez, antes delas, e o valor visível ao avaliador está na URL.

**Independent Test**: rodar o script duas vezes seguidas no projeto: a segunda execução termina com sucesso e
informa que cada recurso já existia, sem criar duplicatas nem alterar permissões.

**Acceptance Scenarios**:

1. **Given** o projeto `outsera-awards` com faturamento ativo e o login do Anderson no `gcloud`, **When** o
   script roda pela primeira vez, **Then** ativa as APIs, cria o registro na região `southamerica-east1`, as
   contas de serviço, o pool e o provedor de identidade restritos a este repositório e à `main`, e concede só
   as permissões listadas na spec.
2. **Given** o script já rodado, **When** ele roda de novo, **Then** termina com sucesso e não altera nenhum
   recurso.
3. **Given** um login sem permissão suficiente ou o projeto errado ativo, **When** o script roda, **Then**
   para antes de mudar qualquer coisa, com mensagem clara do que falta.
4. **Given** o script concluído, **When** ele termina, **Then** imprime os identificadores não secretos que o
   workflow precisa (provedor de identidade, conta de serviço, registro), sem nenhuma chave.

---

### User Story 5 - O README apresenta a demonstração publicada (Priority: P2)

O README ganha a URL pública em destaque e a seção do Marco 5 entre os extras: o que é publicado (só o modo
padrão), como a publicação acontece, por que há no máximo uma instância, o aviso da partida a frio e os
limites da demonstração.

**Why this priority**: sem a URL no README, o avaliador não acha a demonstração; é documentação, comprovada
por revisão.

**Independent Test**: revisão do README no pull request, com o item correspondente no checklist da spec.

**Acceptance Scenarios**:

1. **Given** o README publicado, **When** o avaliador lê "Avalie em 2 minutos", **Then** encontra a URL
   pública de intervalos e do Swagger UI, com o aviso de que a primeira chamada pode demorar alguns segundos.
2. **Given** a seção do Marco 5, **When** o avaliador a lê, **Then** entende o que é publicado, como, com
   quais garantias e o que fica de fora (Kafka, PostgreSQL, Grafana e autenticação ligada).

---

### Edge Cases

- **Partida a frio com min-instances=0:** a primeira chamada sobe a instância e carrega o CSV. A readiness só
  fica positiva depois da carga, e o serviço só recebe tráfego na revisão nova depois disso.
- **Revisão nova que não sobe** (por exemplo, a carga falha): o serviço continua servindo a revisão anterior,
  e o job de publicação fica vermelho.
- **Revisão nova que sobe mas responde errado:** como ela é implantada sem tráfego, a conferência da US3
  reprova pela URL própria dela, e o tráfego nunca sai da revisão anterior. O job fica vermelho.
- **Primeira publicação:** o script cria o serviço com uma revisão de exemplo (research R4), então sempre
  existe uma revisão anterior. A primeira publicação pelo pipeline segue o mesmo fluxo e substitui a de
  exemplo só depois de conferida.
- **Pico de chamadas com max-instances=1:** as chamadas além da capacidade da instância esperam ou recebem
  429/503 da plataforma; nenhuma segunda instância é criada. Isso é aceito para a demonstração.
- **Merge só de documentação:** o pipeline roda inteiro e publica de novo a imagem desse commit; o resultado
  da URL não muda.
- **Pull request de fork ou push em outra branch:** nunca chega ao job de publicação, e o provedor de
  identidade recusaria a troca mesmo que chegasse.
- **Reexecução manual do pipeline na `main`:** publica de novo a imagem daquele commit; a operação é segura
  de repetir.
- **Registro acumulando imagens:** as imagens antigas são removidas por política de retenção, para o custo não
  crescer a cada merge.
- **Faturamento desligado ou orçamento estourado:** a publicação falha no pipeline com a mensagem do GCP; o
  alerta de orçamento é configurado pelo Anderson, fora do script.
- **Mudança do comportamento de cache:** com uma única instância e sem gravações depois da carga no modo
  padrão, o limite do ADR 0026 continua atendido; nenhuma mudança no cache é necessária.

## Requirements *(mandatory)*

### Functional Requirements

**Serviço publicado**

- **FR-001**: O serviço MUST rodar no Cloud Run do projeto `outsera-awards`, região `southamerica-east1`, só
  no **modo padrão**: H2 em memória e carga do CSV fornecido, sem nenhum perfil ou configuração de Kafka,
  PostgreSQL ou autenticação.
- **FR-002**: O serviço MUST rodar a **mesma imagem** construída e verificada no pipeline daquele commit
  (teste de fumaça e porta de vulnerabilidades da imagem), referenciada pelo digest, e não uma imagem
  reconstruída depois.
- **FR-003**: Cada revisão do serviço MUST ter no máximo **uma** instância e no mínimo **zero**, e fora da
  troca de revisão só uma revisão recebe tráfego. Na troca, a revisão antiga e a nova podem ter uma instância
  cada por alguns instantes; no modo padrão isso não afeta o cache do ADR 0026, porque cada instância tem o
  próprio H2 e não grava nada depois da carga (research R5). O limite também mantém o custo baixo.
- **FR-004**: Os endpoints de negócio, o Swagger UI, o contrato OpenAPI e os probes de saúde (`/actuator/health`,
  liveness e readiness) MUST responder a chamadas anônimas: a autenticação fica desligada, como no modo padrão
  (Princípio VII), e o serviço aceita invocação sem identidade do GCP.
- **FR-004a**: As métricas MUST NOT ficar expostas na URL pública: a publicação define
  `MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=health`, que mantém só o que os probes usam, e
  `/actuator/prometheus` responde 404. A configuração do modo padrão e da imagem não muda.
- **FR-005**: A porta MUST vir da variável `PORT` fornecida pela plataforma, e o log MUST sair em JSON, como a
  imagem já define para a execução em container (constituição, Observabilidade). A única variável que a
  publicação acrescenta é a da FR-004a.
- **FR-006**: Uma revisão nova MUST receber tráfego só depois da readiness positiva (fim da carga do CSV), e a
  liveness MUST usar o probe de liveness da aplicação.
- **FR-007**: A resposta de `GET /api/producers/intervals` na URL pública MUST ser idêntica à do modo padrão
  local com o mesmo arquivo, no contrato exato do PDF (Princípio III).

**Publicação pelo pipeline**

- **FR-008**: A publicação MUST rodar automaticamente a cada push na `main` deste repositório (merge de pull
  request), só depois de todos os checks obrigatórios da `main` ficarem verdes (hoje, os 10 do Marco 4; a
  política do workflow confere a lista, que acompanha os checks obrigatórios), e MUST NOT rodar em pull requests, em
  outras branches nem em forks.
- **FR-009**: A autenticação do pipeline no GCP MUST usar OIDC com Workload Identity Federation. O provedor de
  identidade MUST aceitar só tokens deste repositório e da branch `main`. MUST NOT existir chave de conta de
  serviço no GitHub, no repositório ou nos artefatos.
- **FR-010**: A imagem MUST ser publicada no Artifact Registry do projeto, na mesma região, identificada pelo
  commit, e o serviço MUST ser implantado pelo digest dessa publicação.
- **FR-011**: As publicações MUST ser serializadas: uma execução nova espera a anterior terminar e não a
  cancela, a ordem nunca se inverte e o último commit mergeado fica publicado. Com várias execuções na fila,
  as intermediárias podem ser descartadas.
- **FR-012**: As permissões de escrita do pipeline (identidade federada e publicação de imagem) MUST existir só
  no job de publicação. A política do workflow (`check-workflows.sh`) MUST ser ajustada para permitir
  exatamente essas exceções nesse job e continuar reprovando qualquer outra, inclusive referência a
  `secrets.`. As actions novas MUST ser fixadas por hash de commit.
- **FR-013**: Os identificadores usados pelo workflow (projeto, região, provedor de identidade, conta de
  serviço, registro e serviço) MUST ser valores não secretos, versionados ou em variáveis do repositório,
  nunca em segredos.

**Conferência depois da publicação**

- **FR-014**: Cada revisão nova MUST ser implantada **sem tráfego** e conferida pela URL própria dela:
  esperar a readiness positiva, com tolerância para a partida a frio, e conferir o resultado de referência
  (Joel Silver em `min`, 1990–1991; Matthew Vaughn em `max`, 2002–2015), sem campos além dos do contrato.
- **FR-015**: Só com essa conferência aprovada o pipeline MUST passar 100% do tráfego para a revisão nova e
  repetir a mesma conferência na URL pública. Se qualquer das duas não passa dentro do tempo limite, o job MUST
  ficar vermelho, com a resposta recebida no resumo da execução.
- **FR-015a**: Uma revisão reprovada na conferência pela URL própria MUST NOT receber tráfego: a revisão
  anterior continua servindo a URL pública sem interrupção. A volta do tráfego depois de uma falha na URL
  pública (já com a troca feita) é manual, pelo runbook (FR-024).
- **FR-016**: O resumo da execução MUST registrar a URL publicada, o digest da imagem, a revisão e o resultado
  da conferência.

**Configuração do GCP**

- **FR-017**: Toda a configuração do GCP MUST estar num script versionado e **idempotente**: ativação das APIs,
  criação do registro, das contas de serviço, do pool e do provedor de identidade, concessão de permissões e
  acesso público ao serviço. Rodá-lo de novo MUST terminar com sucesso sem alterar o que já existe.
- **FR-018**: As permissões MUST ser as mínimas:
  - a conta de serviço de **publicação**, usada pelo pipeline, só pode publicar imagens no registro do projeto
    e implantar revisões do serviço com a conta de serviço de execução;
  - a conta de serviço de **execução**, usada pelo serviço, não recebe nenhum papel no projeto;
  - a federação só permite que identidades deste repositório e da `main` usem a conta de publicação.
  Cada permissão concedida MUST vir com o motivo no próprio script.
- **FR-019**: O script MUST validar, antes de mudar qualquer coisa, que o login está ativo e que o projeto é o
  `outsera-awards`, e MUST parar com mensagem clara se algo faltar.
- **FR-020**: O script MUST rodar com o `gcloud` depois do login interativo do Anderson. Nenhuma credencial é
  gravada no repositório, no script nem na evidência de IA.
- **FR-021**: O registro MUST ter política de retenção que remove as imagens antigas, com ou sem tag, depois
  de 30 dias, e sempre mantém as 10 mais recentes, necessárias para voltar a uma revisão anterior.

**Modo padrão e documentação**

- **FR-022**: Nada MUST mudar no `./mvnw verify` do modo padrão nem no `./mvnw -Pextras verify`: nenhum IT
  novo, removido ou alterado, e nenhuma configuração nova exigida para subir a aplicação localmente.
- **FR-023**: O README MUST trazer a URL pública (intervalos e Swagger UI) em "Avalie em 2 minutos", com o
  aviso da partida a frio, e a seção do Marco 5 entre os extras (o que é publicado, como, o limite de uma
  instância e o que fica de fora), além dos requisitos do Marco 5 na seção de requisitos.
- **FR-024**: A decisão de publicar no Cloud Run com uma instância e a federação de identidade MUST virar um
  ADR curto em `docs/adr/`, e o runbook MUST ganhar o procedimento de voltar o tráfego para a revisão anterior.
- **FR-025**: A mudança em relação ao briefing (publicação a cada merge na `main`, e não por tag de versão)
  MUST ser registrada em `docs/ai/decisoes.md`.

**Comprovação (Princípio VI)**

- **FR-026**: Os requisitos MUST ser comprovados assim:
  - FR-001 a FR-016 (inclusive FR-004a e FR-015a): execução do pipeline na `main` (jobs de publicação e de
    conferência) e conferência da URL pública depois do merge, inclusive o 404 de `/actuator/prometheus`;
    a restrição da federação é comprovada pela condição do provedor, conferida no GCP depois do script, e
    pelo job de publicação pulado no pull request;
  - FR-017 a FR-021: duas execuções do script, com a saída da segunda registrada, e revisão do script no PR;
  - FR-022: os dois comandos Maven verdes, sem alteração nos ITs (`check-unchanged-tests.sh`);
  - FR-023 a FR-025: revisão no PR, com item no checklist da spec.

  Este marco não tem comportamento novo da aplicação, então não pede IT novo.

### Key Entities

- **Serviço publicado**: a API no Cloud Run, com a URL pública, os limites de instância e a revisão ativa.
- **Revisão**: uma implantação imutável do serviço, ligada a um digest de imagem; o tráfego aponta para uma
  revisão por vez.
- **Imagem publicada**: a imagem verificada no pipeline, no registro do projeto, identificada pelo commit e
  pelo digest.
- **Identidade federada**: o pool e o provedor que trocam o token OIDC do GitHub, restrito ao repositório e à
  `main`, pela conta de serviço de publicação.
- **Contas de serviço**: a de publicação (pipeline) e a de execução (serviço), cada uma com o mínimo.
- **Script de configuração**: o arquivo versionado e idempotente que cria tudo isso.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Um avaliador sem nada instalado obtém o resultado de referência pela URL do README em até 30
  segundos, contando a partida a frio, e em até 2 segundos com a instância ativa.
- **SC-002**: Depois de cada merge na `main` com o pipeline verde, a URL pública passa a servir o commit mais
  recente da `main`, conferida pelo próprio pipeline, sem nenhum passo manual.
- **SC-003**: Zero chaves de conta de serviço existem no projeto GCP para a conta de publicação, e zero
  referências a segredos existem no workflow.
- **SC-004**: Uma segunda execução do script de configuração termina com sucesso sem criar, remover ou alterar
  nenhum recurso.
- **SC-005**: Do merge na `main` à URL conferida, o tempo adicional da publicação e da conferência fica abaixo
  de 10 minutos.
- **SC-006**: Com o serviço ocioso, o custo é zero instâncias em execução (mínimo 0); cada revisão nunca passa
  de uma instância (máximo 1).
- **SC-007**: O `./mvnw verify` do modo padrão e o `./mvnw -Pextras verify` continuam verdes, com os mesmos ITs
  de antes do marco.

## Assumptions

- O projeto `outsera-awards` já existe, com faturamento ativo; o cartão e o alerta de orçamento são passos do
  Anderson, fora do script (briefing).
- O Anderson faz o login interativo no `gcloud`; o Claude Code roda o script nessa sessão autenticada.
- O repositório é `andersonfelipediefenbach/outsera`, e a restrição da federação usa o repositório e a
  referência `refs/heads/main` do token do GitHub.
- A imagem atual já roda como não root, lê `PORT` e liga o log em JSON por variável que ela própria define;
  não há mudança no Dockerfile além do necessário para a plataforma.
- Os recursos da instância seguem o padrão da plataforma com memória suficiente para a JVM e a carga do CSV
  (por exemplo, 1 vCPU e 1 GiB), ajustados no plano.
- No modo padrão não há gravação depois da carga, e o serviço roda uma instância; o ADR 0026 continua válido
  sem mudança. Escalar além de uma instância fica fora do escopo.
- Fora do escopo: domínio próprio, Kafka, PostgreSQL, Grafana e autenticação ligada no Cloud Run, publicação
  no GHCR, assinatura e SBOM da imagem (adiados no Marco 4) e ambientes de homologação.
- Os ADRs 0020 a 0025 estão reservados para os itens adiados do Marco 4; o ADR deste marco usa o próximo
  número livre depois do 0026.
