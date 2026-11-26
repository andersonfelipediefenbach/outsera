 ▐▛███▛█   Claude Code v2.1.332
▝▜██████▀  Opus 5.5 · Claude Pro
 ▝▝   ▝▝   D:\Repo\PESSOAIS\outsera-test\outsera


❯ /clear                                                                        

❯ /speckit-specify Marco 5: deploy no Google Cloud Run, num único bolt. Projeto 
  GCP outsera-awards, região southamerica-east1. Só o modo padrão (H2, carga    
  do CSV), com a mesma imagem do pipeline, publicada no Artifact Registry.      
  max-instances=1 e min-instances=0, o que mantém válido o cache em memória do  
  ADR 0026 e limita o custo. Autenticação desligada, como no modo padrão.       
  Deploy automático a cada merge na main, autenticado por OIDC com Workload     
  Identity Federation, restrito a este repositório e à branch main, sem chaves  
  no GitHub. Toda a configuração do GCP (APIs, Artifact Registry, conta de      
  serviço com permissões mínimas, pool e provedor de identidade) fica num       
  script versionado e idempotente, rodado pelo Claude Code com o gcloud depois  
  do meu login. O pipeline confere a URL publicada depois do deploy (readiness  
  e resultado de referência). O README ganha a URL pública e a seção do Marco   
  5. Nada muda no ./mvnw verify do modo padrão.                                 

● Skill(speckit-specify)
  ⎿  Loaded skill

  Read 1 file, ran 3 shell commands (ctrl+o to expand)

● Estou lendo o pipeline e a política de workflow para que a spec leve em conta
  as restrições de hoje (nenhuma publicação e nenhum segredo).

  Ran 2 shell commands (ctrl+o to expand)

● Escrevendo a spec em specs/005-deploy-cloud-run/.

● Write(specs\005-deploy-cloud-run\spec.md)
  ⎿  Wrote 312 lines to specs\005-deploy-cloud-run\spec.md
      1 # Feature Specification: Marco 5 — Publicação da API no Google Cl
        oud Run
      2 
      3 **Feature Branch**: `marco/5-deploy`
      4 
      5 **Created**: 2026-11-26
      6 
      7 **Status**: Draft
      8 
      9 **Input**: User description: "Marco 5: deploy no Google Cloud Run
     … +304 lines (~20 KB) (ctrl+o to expand)

  Ran 1 shell command (ctrl+o to expand)

● Write(specs\005-deploy-cloud-run\checklists\requirements.md)
  ⎿  Wrote 37 lines to
     specs\005-deploy-cloud-run\checklists\requirements.md
       1 # Specification Quality Checklist: Marco 5 — Publicação da API n
         o Google Cloud Run
       2 
       3 **Purpose**: Validate specification completeness and quality bef
         ore proceeding to planning
       4 **Created**: 2026-11-26
       5 **Feature**: [spec.md](../spec.md)
       6 
       7 ## Content Quality
       8 
     … +29 lines (ctrl+o to expand)

● Write(.specify\feature.json)
  ⎿  Added 1 line, removed 1 line
      1 -{ "feature_directory": "specs/004-acabamento-operacional" }     
      1 +{ "feature_directory": "specs/005-deploy-cloud-run" }           

● Escrevi a spec. Ficou uma decisão em aberto que é sua.

  - Diretório: specs/005-deploy-cloud-run/ (branch marco/5-deploy), com
    .specify/feature.json apontando para ele.
  - Spec: specs/005-deploy-cloud-run/spec.md. São 5 histórias, 26 requisitos e 7
    critérios de sucesso.
  - Checklist: specs/005-deploy-cloud-run/checklists/requirements.md. Todos os
    itens passam, menos um: ainda há um marcador [NEEDS CLARIFICATION].

  Pontos que a spec definiu e que você talvez queira revisar:
  - Política do workflow: o check-workflows.sh hoje bloqueia qualquer publicação
    de imagem, permissão de escrita fora do codeql e referência a secrets.. A
    FR-012 abre exceções só no job de publicação: o token de identidade
    (id-token: write) e o push da imagem. A proibição de secrets. continua
    valendo.
  - Gatilho: a FR-025 manda registrar em docs/ai/decisoes.md que o briefing
    previa publicação por tag de versão e você pediu publicação a cada merge na
    main.
  - Contas de serviço: são duas. A de publicação só envia imagens e implanta
    revisões. A de execução não recebe nenhum papel no projeto. O acesso público
    ao serviço fica no script de configuração, não no pipeline.
  - Registro de imagens: ganhou uma política de retenção, para o custo não
    crescer a cada merge.
  - Comprovação (Princípio VI): o marco não muda o comportamento da aplicação,
    então não tem IT novo. A comprovação vem do pipeline, das duas execuções do
    script e da revisão no PR.
  - ADR e orçamento: o ADR novo fica com o próximo número depois do 0026. O
    alerta de orçamento continua sendo passo seu, fora do script.

  Question 1: Revisão que sobe mas falha na conferência

  Context: "Revisão nova que sobe mas responde errado: a conferência da US3
  reprova. [NEEDS CLARIFICATION: o pipeline devolve o tráfego à revisão anterior
  automaticamente ou só reprova o job e o Anderson decide pelo runbook?]"

  What we need to know: o que acontece com o tráfego quando a URL publicada não
  passa na conferência.

  Option: A
  Answer: Implantar sem tráfego, conferir pela URL da própria revisão e só
    então mover o tráfego
  Implications: Uma revisão ruim nunca chega à URL pública. A conferência
    final na URL pública continua. O job fica um pouco mais longo
  ────────────────────────────────────────
  Option: B
  Answer: Mover o tráfego, conferir e, se falhar, voltar o tráfego
    automaticamente para a revisão anterior
  Implications: O tráfego vai para a revisão ruim por alguns segundos. A conta
    de publicação precisa poder mudar o tráfego (ela já pode, ao implantar)
  ────────────────────────────────────────
  Option: C
  Answer: Só reprovar o job. Você volta o tráfego pelo runbook
  Implications: É o mais simples. A URL pode ficar quebrada até você agir
  ────────────────────────────────────────
  Option: Custom
  Answer: Descreva outra regra
  Implications: —

  Recomendo a A: a URL do README nunca fica quebrada e a conta de publicação não
  precisa de nenhuma permissão a mais.

  Your choice: aguardando a sua resposta

✻ Brewed for 2m 53s · done 14:40

❯ opcao 1                                                                       

● Update(specs\005-deploy-cloud-run\spec.md)
  ⎿  Added 9 lines
       8 
       9  **Input**: User description: "Marco 5: deploy no Google Cloud R
          un, num único bolt. Projeto GCP outsera-awards, região southame
          rica-east1. Só o modo padrão (H2, carga do CSV), com a mesma im
          agem do pipeline, publicada no Artifact Registry. max-instances
          =1 e min-instances=0, o que mantém válido o cache em memória do
           ADR 0026 e limita o custo. Autenticação desligada, como no mod
          o padrão. Deploy automático a cada merge na main, autenticado p
          or OIDC com Workload Identity Federation, restrito a este repos
          itório e à branch main, sem chaves no GitHub. Toda a configuraç
          ão do GCP (APIs, Artifact Registry, conta de serviço com permis
          sões mínimas, pool e provedor de identidade) fica num script ve
          rsionado e idempotente, rodado pelo Claude Code com o gcloud de
          pois do meu login. O pipeline confere a URL publicada depois do
           deploy (readiness e resultado de referência). O README ganha a
           URL pública e a seção do Marco 5. Nada muda no ./mvnw verify d
          o modo padrão."
      10 
      11 +## Clarifications                                              
      12 +                                                               
      13 +### Session 2026-11-26                                         
      14 +                                                               
      15 +- Q: O que acontece com o tráfego quando uma revisão nova sobe 
         +mas não passa na conferência? → A: A revisão                   
      16 +  nova é implantada **sem tráfego** e conferida pela URL própri
         +a dela. O tráfego só passa para ela depois de a                
      17 +  conferência aprovar, e a URL pública é conferida de novo depo
         +is da troca. Uma revisão reprovada nunca recebe                
      18 +  tráfego, e a anterior continua servindo. US3, FR-014, FR-015 
         +e FR-015a.                                                     
      19 +                                                               
      20  ## User Scenarios & Testing *(mandatory)*
      21 
      22  ### User Story 1 - Avaliador usa a API publicada sem instalar n
          ada (Priority: P1)

● Update(specs\005-deploy-cloud-run\spec.md)
  ⎿  Added 5 lines, removed 3 lines
      80 
      81  ### User Story 3 - O pipeline confere a URL pública depois de c
          ada publicação (Priority: P1)
      82 
      83 -Depois de implantar, o pipeline chama a URL pública como um ava
         -liador faria: espera a readiness positiva e                    
      84 -confere o resultado de referência no endpoint de intervalos. Se
         - algo não confere dentro do tempo limite, o job                
      85 -fica vermelho e o motivo aparece no resumo da execução.        
      83 +O pipeline implanta a revisão nova sem tráfego e a chama pela U
         +RL própria dela, como um avaliador faria:                      
      84 +espera a readiness positiva e confere o resultado de referência
         + no endpoint de intervalos. Só com a                           
      85 +conferência aprovada o tráfego passa para a revisão nova, e a U
         +RL pública é conferida de novo. Se algo não                    
      86 +confere dentro do tempo limite, o tráfego continua na revisão a
         +nterior, o job fica vermelho e o motivo aparece                
      87 +no resumo da execução.                                         
      88 
      89  **Why this priority**: uma implantação que "terminou" não prova
           que a URL do README funciona. A conferência é
      90  a comprovação do marco pelo Princípio VI (estágio do pipeline).

● Update(specs\005-deploy-cloud-run\spec.md)
  ⎿  Added 11 lines, removed 8 lines
       94 
       95  **Acceptance Scenarios**:
       96 
       97 -1. **Given** uma implantação concluída, **When** a conferência
          - roda, **Then** ela espera a readiness positiva               
       98 -   na URL pública e depois confere Joel Silver em `min` (1, 19
          -90, 1991) e Matthew Vaughn em `max` (13, 2002,                
       99 -   2015), sem campos extras.                                  
      100 -2. **Given** a URL que não responde ou responde diferente dent
          -ro do tempo limite, **When** a conferência                    
      101 -   termina, **Then** o job fica vermelho, com a resposta receb
          -ida no resumo.                                                
      102 -3. **Given** a partida a frio do serviço, **When** a conferênc
          -ia faz a primeira chamada, **Then** a espera                  
       97 +1. **Given** uma revisão nova implantada sem tráfego, **When**
          + a conferência roda na URL própria da                         
       98 +   revisão, **Then** ela espera a readiness positiva e depois 
          +confere Joel Silver em `min` (1, 1990, 1991) e                
       99 +   Matthew Vaughn em `max` (13, 2002, 2015), sem campos extras
          +.                                                             
      100 +2. **Given** a conferência da revisão aprovada, **When** o pip
          +eline passa o tráfego para ela, **Then** a                    
      101 +   mesma conferência roda na URL pública e aprova.            
      102 +3. **Given** a revisão nova que não responde ou responde difer
          +ente dentro do tempo limite, **When** a                       
      103 +   conferência termina, **Then** o tráfego continua 100% na re
          +visão anterior, a URL pública segue servindo o                
      104 +   resultado de referência e o job fica vermelho, com a respos
          +ta recebida no resumo.                                        
      105 +4. **Given** a partida a frio do serviço, **When** a conferênc
          +ia faz a primeira chamada, **Then** a espera                  
      106     tolera essa demora sem reprovar.
      104 -4. **Given** a conferência reprovou, **When** o Anderson olha 
          -a execução, **Then** encontra no resumo o                     
      105 -   que falhou e, no runbook, como voltar o tráfego para a revi
          -são anterior.                                                 
      107 +5. **Given** a conferência na URL pública reprovou depois da t
          +roca, **When** o Anderson olha a execução,                    
      108 +   **Then** encontra no resumo o que falhou e, no runbook, com
          +o voltar o tráfego para a revisão anterior.                   
      109 
      110  ---
      111 

● Update(specs\005-deploy-cloud-run\spec.md)
  ⎿  Added 4 lines, removed 3 lines
      163    fica positiva depois da carga, e o serviço só recebe tráfego
            na revisão nova depois disso.
      164  - **Revisão nova que não sobe** (por exemplo, a carga falha):
           o serviço continua servindo a revisão anterior,
      165    e o job de publicação fica vermelho.
      166 -- **Revisão nova que sobe mas responde errado:** a conferência
          - da US3 reprova. [NEEDS CLARIFICATION: o                      
      167 -  pipeline devolve o tráfego à revisão anterior automaticament
          -e ou só reprova o job e o Anderson decide pelo                
      168 -  runbook?]                                                   
      166 +- **Revisão nova que sobe mas responde errado:** como ela é im
          +plantada sem tráfego, a conferência da US3                    
      167 +  reprova pela URL própria dela, e o tráfego nunca sai da revi
          +são anterior. O job fica vermelho.                            
      168 +- **Primeira publicação, sem revisão anterior:** não há tráfeg
          +o a preservar; a revisão é conferida pela URL                 
      169 +  própria e só então recebe o tráfego.                        
      170  - **Pico de chamadas com max-instances=1:** as chamadas além d
           a capacidade da instância esperam ou recebem
      171    429/503 da plataforma; nenhuma segunda instância é criada. I
           sso é aceito para a demonstração.
      172  - **Merge só de documentação:** o pipeline roda inteiro e publ
           ica de novo a imagem desse commit; o resultado

● Update(specs\005-deploy-cloud-run\spec.md)
  ⎿  Added 9 lines, removed 5 lines
      228 
      229  **Conferência depois da publicação**
      230 
      231 -- **FR-014**: Depois de cada implantação, o pipeline MUST conf
          -erir a URL pública: esperar a readiness positiva,             
      232 -  com tolerância para a partida a frio, e conferir o resultado
          - de referência (Joel Silver em `min`, 1990–1991;              
      233 -  Matthew Vaughn em `max`, 2002–2015), sem campos além dos do 
          -contrato.                                                     
      234 -- **FR-015**: Se a conferência não passa dentro do tempo limit
          -e, o job MUST ficar vermelho, com a resposta                  
      235 -  recebida no resumo da execução. O tratamento da revisão com 
          -falha segue o caso de borda correspondente.                   
      231 +- **FR-014**: Cada revisão nova MUST ser implantada **sem tráf
          +ego** e conferida pela URL própria dela:                      
      232 +  esperar a readiness positiva, com tolerância para a partida 
          +a frio, e conferir o resultado de referência                  
      233 +  (Joel Silver em `min`, 1990–1991; Matthew Vaughn em `max`, 2
          +002–2015), sem campos além dos do contrato.                   
      234 +- **FR-015**: Só com essa conferência aprovada o pipeline MUST
          + passar 100% do tráfego para a revisão nova e                 
      235 +  repetir a mesma conferência na URL pública. Se qualquer das 
          +duas não passa dentro do tempo limite, o job MUST             
      236 +  ficar vermelho, com a resposta recebida no resumo da execuçã
          +o.                                                            
      237 +- **FR-015a**: Uma revisão reprovada na conferência pela URL p
          +rópria MUST NOT receber tráfego: a revisão                    
      238 +  anterior continua servindo a URL pública sem interrupção. A 
          +volta do tráfego depois de uma falha na URL                   
      239 +  pública (já com a troca feita) é manual, pelo runbook (FR-02
          +4).                                                           
      240  - **FR-016**: O resumo da execução MUST registrar a URL public
           ada, o digest da imagem, a revisão e o resultado
      241    da conferência.
      242 

● Update(specs\005-deploy-cloud-run\spec.md)
  ⎿  Added 1 line, removed 1 line
      273  **Comprovação (Princípio VI)**
      274 
      275  - **FR-026**: Os requisitos MUST ser comprovados assim:
      276 -  - FR-001 a FR-016: execução do pipeline na `main` (jobs de p
          -ublicação e de conferência), mais a recusa da                 
      276 +  - FR-001 a FR-016 (inclusive FR-015a): execução do pipeline 
          +na `main` (jobs de publicação e de conferência), mais a recusa
          + da                                                           
      277      federação fora da `main` comprovada pela condição do prove
           dor, conferida no script e no resumo;
      278    - FR-017 a FR-021: duas execuções do script, com a saída da
           segunda registrada, e revisão do script no PR;
      279    - FR-022: os dois comandos Maven verdes, sem alteração nos I
           Ts (`check-unchanged-tests.sh`);

● Update(specs\005-deploy-cloud-run\checklists\requirements.md)
  ⎿  Added 1 line, removed 1 line
      13 
      14  ## Requirement Completeness
      15 
      16 -- [ ] No [NEEDS CLARIFICATION] markers remain                  
      16 +- [x] No [NEEDS CLARIFICATION] markers remain                  
      17  - [x] Requirements are testable and unambiguous
      18  - [x] Success criteria are measurable
      19  - [x] Success criteria are technology-agnostic (no implementati
          on details)

● Update(specs\005-deploy-cloud-run\checklists\requirements.md)
  ⎿  Added 2 lines, removed 1 line
      34  - O marco é de infraestrutura: Cloud Run, Artifact Registry, OI
          DC e Workload Identity Federation são o próprio
      35    escopo pedido, e não detalhes de implementação. Os critérios
          de sucesso ficam no que o avaliador observa (URL,
      36    tempo, custo, ausência de chaves).
      37 -- Pendente: 1 marcador [NEEDS CLARIFICATION] (o que fazer com u
         -ma revisão que sobe mas falha na conferência).                 
      37 +- Clarificação de 2026-11-26 resolvida: revisão nova implantada
         + sem tráfego, conferida pela URL própria e só                  
      38 +  então promovida (FR-014, FR-015 e FR-015a). Todos os itens pa
         +ssam.                                                          

● A spec está pronta para o /speckit-plan. A sua escolha, a opção A, está
  registrada em specs/005-deploy-cloud-run/spec.md numa seção Clarifications
  nova.

  O que a opção A mudou na spec:
  - Antes do tráfego: cada revisão nova sobe sem tráfego e é conferida pela URL
    própria dela (readiness e resultado de referência) (FR-014).
  - Depois da conferência: se ela aprova, 100% do tráfego passa para a revisão
    nova e a mesma conferência roda na URL pública (FR-015).
  - Quando a conferência reprova: a revisão reprovada nunca recebe tráfego e a
    anterior continua servindo (FR-015a). Se a falha vier só na URL pública, já
    depois da troca, você volta o tráfego à mão, seguindo o runbook.
  - História 3 e casos de borda: a história ganhou os cenários "promove e
    confere de novo" e "reprovada não recebe tráfego". Entrou nos casos de borda
    a primeira publicação, quando ainda não há revisão anterior.

  O checklist em specs/005-deploy-cloud-run/checklists/requirements.md passa em
  todos os itens e não sobrou nenhum [NEEDS CLARIFICATION]. Não existe
  .specify/extensions.yml, então nenhum hook rodou.

  Não fiz commit. Na working tree estão a pasta specs/005-deploy-cloud-run/, o
  .specify/feature.json alterado e o docs/ai/prompts.md, que já estava
  modificado antes da sessão.

✻ Cooked for 35s · done 14:42

❯ /speckit-plan Siga a constituição e as decisões dos marcos anteriores.        
  Actions fixadas por hash de commit (google-github-actions/auth, setup-gcloud  
  e deploy-cloudrun, se usadas, com as referências aninhadas conferidas). A     
  imagem publicada é a mesma do job image, identificada pelo digest, sem        
  rebuild no job de deploy. O script de configuração do GCP é bash, idempotente 
  (rodar duas vezes não muda nada nem falha), com set -euo pipefail e saída     
  OK/FALHA por passo, como os scripts anteriores. Nenhum segredo no repositório 
  nem nos secrets do GitHub: só identificadores públicos (número do projeto,    
  nome do pool e do provedor, e-mail da conta de serviço), que podem ficar como 
  variáveis do repositório ou no próprio workflow. O job de deploy roda só em   
  push na main e depende de todos os checks obrigatórios. Divida o bolt em      
  etapas, cada uma terminando com o ./mvnw verify verde.                        
● Skill(speckit-plan)
  ⎿  Loaded skill

  Ran 4 shell commands (ctrl+o to expand)

● Fixei as versões das actions do Google. Agora estou conferindo se elas são
  actions JavaScript ou composite, para saber se têm referências aninhadas.

  Ran 1 shell command (ctrl+o to expand)

● As duas actions do Google rodam em node24 e não têm uses: aninhado. Estou
  vendo os scripts e a documentação que já existem para seguir o mesmo padrão.

  Ran 3 shell commands (ctrl+o to expand)

● Já tenho contexto suficiente. Agora vou escrever a research.

● Write(specs\005-deploy-cloud-run\research.md)
  ⎿  Wrote 217 lines to specs\005-deploy-cloud-run\research.md
       1 # Research: Marco 5 — Publicação no Cloud Run
       2 
       3 Decisões técnicas do plano. Cada uma tem a decisão, o motivo e a
         s alternativas recusadas. As referências a
       4 requisitos são da [spec](spec.md).
       5 
       6 ## R1. Mesma imagem do job `image`, sem rebuild (FR-002, FR-010)
       7 
       8 - **Decisão:** o job `image` passa a gerar a imagem com **duas s
         aídas do mesmo build** do Buildx: `type=docker`
     … +209 lines (ctrl+o to expand)

● Write(specs\005-deploy-cloud-run\data-model.md)
  ⎿  Wrote 50 lines to specs\005-deploy-cloud-run\data-model.md
       1 # Data Model: Marco 5 — recursos do GCP e do pipeline
       2 
       3 Não há entidade nova na aplicação. O "modelo" deste marco é o co
         njunto de recursos que o script cria e que o
       4 pipeline usa. Todos os nomes abaixo são públicos e versionados (
         FR-013).
       5 
       6 ## Recursos do GCP (projeto `outsera-awards`, região `southameri
         ca-east1`)
       7 
     … +43 lines (ctrl+o to expand)

● Write(specs\005-deploy-cloud-run\contracts\setup-script.md)
  ⎿  Wrote 65 lines to specs\005-deploy-cloud-run\contracts\setup-script.md
      1 # Contrato: `deploy/gcp/setup.sh`
      2 
      3 Script de configuração do GCP (FR-017 a FR-021; research R3, R4 e
         R9).
      4 
      5 ## Uso
      6 
      7 ```bash
      8 deploy/gcp/setup.sh            # aplica a configuração (idempoten
        te)
     … +57 lines (ctrl+o to expand)

● Write(specs\005-deploy-cloud-run\contracts\verify-deployment.md)
  ⎿  Wrote 27 lines to
     specs\005-deploy-cloud-run\contracts\verify-deployment.md
      1 # Contrato: `.github/scripts/verify-deployment.sh`
      2 
      3 Conferência da URL publicada (FR-014 a FR-016; research R6 e R11)
        .
      4 
      5 ## Uso
      6 
      7 ```bash
      8 .github/scripts/verify-deployment.sh <URL base> [espera máxima em
         segundos, padrão 180]
     … +19 lines (ctrl+o to expand)

● Write(specs\005-deploy-cloud-run\contracts\ci-deploy.md)
  ⎿  Wrote 53 lines to specs\005-deploy-cloud-run\contracts\ci-deploy.md
       1 # Contrato: pipeline de publicação (`.github/workflows/ci.yml`)
       2 
       3 Mudanças no workflow de CI (FR-002, FR-008 a FR-016; research R1
         , R2, R6, R7, R8 e R10).
       4 
       5 ## Topo do workflow
       6 
       7 - `permissions: contents: read` (sem mudança).
       8 - `concurrency.cancel-in-progress: ${{ github.ref != 'refs/heads
         /main' }}`. Na `main`, nada é cancelado.
     … +45 lines (ctrl+o to expand)

● Write(specs\005-deploy-cloud-run\contracts\workflow-policy.md)
  ⎿  Wrote 24 lines to
     specs\005-deploy-cloud-run\contracts\workflow-policy.md
      1 # Contrato: política do workflow (`.github/scripts/check-workflow
        s.sh`)
      2 
      3 Mudanças na política (FR-012; research R8). As regras 1, 2, 4, 6,
         7 e 8 não mudam.
      4 
      5 | Regra | Reprova quando |
      6 | --- | --- |
      7 | 3 | um job pede escrita, exceto `security-events: write` no `co
        deql` e `id-token: write` no `deploy` |
     … +17 lines (ctrl+o to expand)

  Searched for 1 pattern (ctrl+o to expand)

● Update(specs\005-deploy-cloud-run\contracts\verify-deployment.md)
  ⎿  Added 1 line, removed 1 line
      18  | --- | --- | --- |
      19  | 1 | Readiness | `GET /actuator/health/readiness` devolve 200
          com `{"status":"UP"}` dentro da espera máxima; tenta a cada 3 s
          , com tempo limite de 10 s por chamada (partida a frio) |
      20  | 2 | Resultado de referência | `GET /api/producers/intervals`
          devolve 200, `application/json`, e o corpo, ordenado com `jq -S
          `, é igual à `REFERENCE` do `smoke-test.sh` (chaves exatas, sem
           campos extras) |
      21 -| 3 | Swagger UI | `GET /swagger-ui/index.html` devolve 200 |  
      21 +| 3 | Swagger UI e contrato | `GET /swagger-ui.html`, seguindo 
         +redirecionamentos, devolve 200, e `GET /openapi.yaml` devolve 2
         +00 |                                                           
      22 
      23  ## Saída
      24 

● Write(specs\005-deploy-cloud-run\quickstart.md)
  ⎿  Wrote 87 lines to specs\005-deploy-cloud-run\quickstart.md
       1 # Quickstart: validar o Marco 5
       2 
       3 Roteiro de validação de ponta a ponta. Os detalhes ficam nos con
         tratos:
       4 [`setup-script.md`](contracts/setup-script.md), [`ci-deploy.md`]
         (contracts/ci-deploy.md),
       5 [`verify-deployment.md`](contracts/verify-deployment.md) e [`wor
         kflow-policy.md`](contracts/workflow-policy.md).
       6 
       7 ## 0. Pré-requisitos (Anderson)
     … +80 lines (ctrl+o to expand)

● Contratos e quickstart prontos. Falta o plan.md, que reúne tudo e divide o
  bolt em etapas.

● Write(specs\005-deploy-cloud-run\plan.md)
  ⎿  Added 134 lines, removed 73 lines
        1 -# Implementation Plan: [FEATURE]                              
        1 +# Implementation Plan: Marco 5 — Publicação da API no Google C
          +loud Run                                                      
        2 
        3 -**Branch**: `[###-feature-name]` | **Date**: [DATE] | **Spec**
          -: [link]                                                      
        3 +**Branch**: `marco/5-deploy` | **Date**: 2026-11-26 | **Spec**
          +: [spec.md](spec.md)                                          
        4 
        5 -**Input**: Feature specification from `/specs/[###-feature-nam
          -e]/spec.md`                                                   
        5 +**Input**: Feature specification from `specs/005-deploy-cloud-
          +run/spec.md`                                                  
        6 
        7 -**Note**: This template is filled in by the `/speckit-plan` co
          -mmand; its definition describes the execution workflow.       
        8 -                                                              
        7  ## Summary
        8 
       11 -[Extract from feature spec: primary requirement + technical ap
          -proach from research]                                         
        9 +A API passa a ser publicada no Cloud Run (`outsera-awards`, `s
          +outhamerica-east1`), só no modo padrão, com 0 a 1             
       10 +instância e acesso anônimo. A publicação é feita pelo pipeline
          + a cada push na `main`, depois dos 10 checks                  
       11 +obrigatórios.                                                 
       12 
       13 +- **Identidade:** o pipeline se autentica por Workload Identit
          +y Federation, restrita ao repositório (pelo ID                
       14 +  numérico) e à `main`, sem chaves.                           
       15 +- **Imagem:** é a mesma do job `image`. O build gera, numa úni
          +ca execução, a imagem testada no Docker e um tar              
       16 +  OCI; o tar segue como artefato e chega ao Artifact Registry 
          +com o digest preservado (research R1).                        
       17 +- **Implantação:** cada revisão sobe sem tráfego, é conferida 
          +pela URL própria, recebe o tráfego e é conferida              
       18 +  de novo na URL pública (research R6).                       
       19 +- **Configuração do GCP:** fica num script bash idempotente, q
          +ue o Claude Code roda depois do login do                      
       20 +  Anderson. O script também cria o serviço, para que as permis
          +sões fiquem no nível do serviço e do                          
       21 +  repositório, e não do projeto (research R4).                
       22 +- **O que não muda:** o código Java, os ITs e os dois comandos
          + Maven.                                                       
       23 +                                                              
       24  ## Technical Context
       25 
       15 -<!--                                                          
       16 -  ACTION REQUIRED: Replace the content in this section with th
          -e technical details                                           
       17 -  for the project. The structure here is presented in advisory
          - capacity to guide                                            
       18 -  the iteration process.                                      
       19 --->                                                           
       26 +**Language/Version**: Bash (scripts de configuração e de verif
          +icação); YAML do GitHub Actions. A aplicação                  
       27 +continua em Java 25 e Spring Boot 4.1.2, sem mudança.         
       28 
       21 -**Language/Version**: [e.g., Python 3.11, Swift 5.9, Rust 1.75
          - or NEEDS CLARIFICATION]                                      
       29 +**Primary Dependencies**: Google Cloud CLI (versão fixada no `
          +setup-gcloud`); `google-github-actions/auth`                  
       30 +v3.0.0 e `setup-gcloud` v3.0.1; `actions/download-artifact` v8
          +.0.2; `skopeo` e `jq` do runner; Docker                       
       31 +Buildx (já usado).                                            
       32 
       23 -**Primary Dependencies**: [e.g., FastAPI, UIKit, LLVM or NEEDS
          - CLARIFICATION]                                               
       33 +**Storage**: H2 em memória dentro da instância (modo padrão); 
          +Artifact Registry para as imagens.                            
       34 
       25 -**Storage**: [if applicable, e.g., PostgreSQL, CoreData, files
          - or N/A]                                                      
       35 +**Testing**: nenhum IT novo (FR-022, FR-026). Comprovação pelo
          + pipeline (jobs `workflow-policy`, `image` e                  
       36 +`deploy`), pelas duas execuções do script e pelo `verify-deplo
          +yment.sh` contra a imagem local, com um caso que              
       37 +aprova e outro que reprova.                                   
       38 
       27 -**Testing**: [e.g., pytest, XCTest, cargo test or NEEDS CLARIF
          -ICATION]                                                      
       39 +**Target Platform**: Cloud Run (serviço totalmente gerenciado)
          +, `linux/amd64`; runners `ubuntu-latest`.                     
       40 
       29 -**Target Platform**: [e.g., Linux server, iOS 15+, WASM or NEE
          -DS CLARIFICATION]                                             
       41 +**Project Type**: serviço web; este marco é de entrega (infrae
          +strutura e pipeline).                                         
       42 
       31 -**Project Type**: [e.g., library/cli/web-service/mobile-app/co
          -mpiler/desktop-app or NEEDS CLARIFICATION]                    
       43 +**Performance Goals**: resultado de referência em até 30 s com
          + partida a frio e em até 2 s com a instância                  
       44 +ativa (SC-001); job `deploy` abaixo de 10 min (SC-005).       
       45 
       33 -**Performance Goals**: [domain-specific, e.g., 1000 req/s, 10k
          - lines/sec, 60 fps or NEEDS CLARIFICATION]                    
       46 +**Constraints**: sem segredos e sem chaves (FR-009, FR-013); a
          +ctions fixadas por hash, sem `uses:` aninhado não             
       47 +conferido; sem rebuild no `deploy`; permissões mínimas; custo 
          +zero com o serviço ocioso (SC-006); o modo                    
       48 +padrão intacto.                                               
       49 
       35 -**Constraints**: [domain-specific, e.g., <200ms p95, <100MB me
          -mory, offline-capable or NEEDS CLARIFICATION]                 
       50 +**Scale/Scope**: 1 serviço, 1 instância no máximo, 1 repositór
          +io de imagens, 2 contas de serviço, 1 pool e                  
       51 +1 provedor; 2 scripts novos e mudanças em 1 workflow e 1 scrip
          +t de política.                                                
       52 
       37 -**Scale/Scope**: [domain-specific, e.g., 10k users, 1M LOC, 50
          - screens or NEEDS CLARIFICATION]                              
       38 -                                                              
       53  ## Constitution Check
       54 
       41 -*GATE: Must pass before Phase 0 research. Re-check after Phase
          - 1 design.*                                                   
       55 +*GATE: avaliado antes da Phase 0 e de novo depois da Phase 1.*
       56 
       43 -[Gates determined based on constitution file]                 
       57 +| Princípio | Situação | Como o plano atende |                
       58 +| --- | --- | --- |                                           
       59 +| I. Só testes de integração | ✅ | Nenhum teste novo na API e
          + nenhum teste unitário. Não há comportamento novo da aplicação
          +. O resultado de referência é conferido por HTTP na URL public
          +ada |                                                         
       60 +| II. Execução só com `./mvnw` | ✅ | Nada muda no `pom.xml`, 
          +nos ITs ou na configuração. Cada etapa termina com `./mvnw ver
          +ify` e `./mvnw -Pextras verify` verdes, sem Docker |          
       61 +| III. Contrato idêntico | ✅ | A conferência compara o corpo 
          +exato com a `REFERENCE` do teste de fumaça, sem campos extras 
          +(FR-007, FR-014) |                                            
       62 +| IV. Spec-anchored | ✅ | A spec e a clarificação vêm antes d
          +o código. As divergências do briefing (publicação por merge, e
          + não por tag) são registradas em `docs/ai/decisoes.md` (FR-025
          +) |                                                           
       63 +| V. Extras isolados | ✅ | O deploy é um extra com infraestru
          +tura própria, ativado só no job da `main`. Fica fora do modo p
          +adrão e não muda a imagem. Sem Kubernetes, sem imagem nativa e
          + sem tracing |                                                
       64 +| VI. Rastreabilidade | ✅ | Pipeline e imagem são comprovados
          + pelo pipeline (jobs `image` e `deploy`). O script, pelas duas
          + execuções registradas. A documentação, pelo checklist da spec
          +. Não há requisito de comportamento sem IT |                  
       65 +| VII. Sem segredos | ✅ | OIDC e WIF, sem chave. Só identific
          +adores públicos, versionados no workflow. `secrets.` continua 
          +proibido pela política. Autenticação desligada, como no modo p
          +adrão. Nenhuma credencial de demonstração no ambiente publicad
          +o |                                                           
       66 +| Cadeia de entrega | ✅ | As três actions novas são fixadas p
          +or hash e conferidas sem `uses:` aninhado (research R2). A ima
          +gem continua multi-stage, não root e sem mudança |            
       67 +| Observabilidade | ✅ | O JSON já é ligado pela variável que 
          +a imagem define. Os probes de startup e liveness usam a readin
          +ess e a liveness da aplicação |                               
       68 +| Fluxo | ✅ | Bolt único no branch `marco/5-deploy`, em etapa
          +s. PR, merge e tags são do Anderson. O script é rodado pelo Cl
          +aude Code só depois do login do Anderson |                    
       69 
       70 +**Pós-design (depois da Phase 1):** sem violações. Dois pontos
          + de atenção, sem exceção constitucional:                      
       71 +                                                              
       72 +- A política do workflow é ampliada (research R8). A ampliação
          + é estreita: só o job `deploy` ganha escrita                  
       73 +  (`id-token`) e publicação, e novas regras impedem rebuild e 
          +exigem os 10 checks.                                          
       74 +- `max-instances=1` é por revisão, então duas instâncias podem
          + coexistir por instantes na troca. No modo padrão             
       75 +  isso não fere o ADR 0026 (research R5), e o ADR do marco reg
          +istra o motivo.                                               
       76 +                                                              
       77  ## Project Structure
       78 
       79  ### Documentation (this feature)
       80 
       81  ```text
       50 -specs/[###-feature]/                                          
       51 -├── plan.md              # This file (/speckit-plan command ou
          -tput)                                                         
       52 -├── research.md          # Phase 0 output (/speckit-plan comma
          -nd)                                                           
       53 -├── data-model.md        # Phase 1 output (/speckit-plan comma
          -nd)                                                           
       54 -├── quickstart.md        # Phase 1 output (/speckit-plan comma
          -nd)                                                           
       55 -├── contracts/           # Phase 1 output (/speckit-plan comma
          -nd)                                                           
       56 -└── tasks.md             # Phase 2 output (/speckit-tasks comm
          -and - NOT created by /speckit-plan)                           
       82 +specs/005-deploy-cloud-run/                                   
       83 +├── spec.md                                                   
       84 +├── plan.md                    # este arquivo                 
       85 +├── research.md                # R1 a R11                     
       86 +├── data-model.md              # recursos do GCP, IAM e estado
          +s da revisão                                                  
       87 +├── quickstart.md              # roteiro de validação         
       88 +├── contracts/                                                
       89 +│   ├── setup-script.md        # deploy/gcp/setup.sh          
       90 +│   ├── verify-deployment.md   # .github/scripts/verify-deploy
          +ment.sh                                                       
       91 +│   ├── ci-deploy.md           # jobs image (alterado) e deplo
          +y (novo)                                                      
       92 +│   └── workflow-policy.md     # check-workflows.sh (regras 3,
          + 5, 9 e 10)                                                   
       93 +├── checklists/                                               
       94 +│   └── requirements.md                                       
       95 +└── tasks.md                   # /speckit-tasks               
       96  ```
       97 
       98  ### Source Code (repository root)
       60 -<!--                                                          
       61 -  ACTION REQUIRED: Replace the placeholder tree below with the
          - concrete layout                                              
       62 -  for this feature. Delete unused options and expand the chose
          -n structure with                                              
       63 -  real paths (e.g., apps/admin, packages/something). The deliv
          -ered plan must                                                
       64 -  not include Option labels.                                  
       65 --->                                                           
       99 
      100  ```text
       68 -# [REMOVE IF UNUSED] Option 1: Single project (DEFAULT)       
       69 -src/                                                          
       70 -├── models/                                                   
       71 -├── services/                                                 
       72 -├── cli/                                                      
       73 -└── lib/                                                      
      101 +deploy/gcp/                                                   
      102 +├── setup.sh                   # novo: configuração idempotent
          +e do GCP                                                      
      103 +└── cleanup-policy.json        # novo: retenção do Artifact Re
          +gistry                                                        
      104 +.github/                                                      
      105 +├── workflows/ci.yml           # alterado: concorrência do top
          +o, job image (saída OCI e digest), job deploy                 
      106 +└── scripts/                                                  
      107 +    ├── check-workflows.sh     # alterado: regras 3, 5, 9 e 10
      108 +    └── verify-deployment.sh   # novo: readiness, referência, 
          +Swagger e contrato                                            
      109 +docs/                                                         
      110 +├── adr/0027-publicacao-no-cloud-run.md   # novo              
      111 +├── adr/README.md              # alterado: índice             
      112 +├── runbook.md                 # alterado: volta do tráfego e 
          +falha de publicação                                           
      113 +└── ai/decisoes.md             # alterado: publicação por merg
          +e; serviço criado pelo script                                 
      114 +README.md                      # alterado: URL em "Avalie em 2
          + minutos", seção e requisitos do Marco 5                      
      115 +```                                                           
      116 
       75 -tests/                                                        
       76 -├── contract/                                                 
       77 -├── integration/                                              
       78 -└── unit/                                                     
      117 +**Structure Decision**: o script do GCP fica em `deploy/gcp/`,
          + que o `.dockerignore` exclui por padrão (lista de            
      118 +permissão), separado dos scripts do pipeline em `.github/scrip
          +ts/`. O `verify-deployment.sh` fica com os                    
      119 +scripts do pipeline, ao lado do `smoke-test.sh`, de onde vem a
          + `REFERENCE`. Nenhum arquivo em `src/` muda.                  
      120 
       80 -# [REMOVE IF UNUSED] Option 2: Web application (when "frontend
          -" + "backend" detected)                                       
       81 -backend/                                                      
       82 -├── src/                                                      
       83 -│   ├── models/                                               
       84 -│   ├── services/                                             
       85 -│   └── api/                                                  
       86 -└── tests/                                                    
      121 +## Etapas do bolt                                             
      122 
       88 -frontend/                                                     
       89 -├── src/                                                      
       90 -│   ├── components/                                           
       91 -│   ├── pages/                                                
       92 -│   └── services/                                             
       93 -└── tests/                                                    
      123 +Cada etapa termina com `./mvnw verify` e `./mvnw -Pextras veri
          +fy` verdes, com o resultado real informado, e                 
      124 +tem commits próprios. Os arquivos compartilhados seguem o dono
          + da rodada (AGENTS.md): o workflow e os scripts               
      125 +são do papel de infraestrutura; o README e os ADRs, do de docu
          +mentação, por pedido registrado se for outra                  
      126 +sessão.                                                       
      127 
       95 -# [REMOVE IF UNUSED] Option 3: Mobile + API (when "iOS/Android
          -" detected)                                                   
       96 -api/                                                          
       97 -└── [same as backend above]                                   
      128 +1. **Etapa 1 — Configuração do GCP** (FR-017 a FR-021; US4)   
      129 +   - `deploy/gcp/setup.sh` e `cleanup-policy.json`, conforme [
          +`contracts/setup-script.md`](contracts/setup-script.md).      
      130 +   - O Anderson faz o login. O Claude Code roda o script duas 
          +vezes, e a saída das duas execuções fica nas                  
      131 +     tarefas, com a segunda sem mudanças.                     
      132 +   - Conferências manuais do quickstart (sem chaves e com a co
          +ndição do provedor).                                          
      133 +   - Os identificadores impressos alimentam a etapa 3.        
      134 +   - Fecha com os dois `./mvnw` verdes.                       
      135 +2. **Etapa 2 — Política do workflow e imagem como artefato** (
          +FR-002, FR-010 parcial e FR-012)                              
      136 +   - `check-workflows.sh` com as regras 3, 5, 9 e 10. Os sete 
          +casos negativos de                                            
      137 +     [`contracts/workflow-policy.md`](contracts/workflow-polic
          +y.md) são conferidos.                                         
      138 +   - Concorrência do topo sem cancelamento na `main`.         
      139 +   - Job `image` com as duas saídas, o passo do digest e a ide
          +ntidade da imagem testada, e o artefato `awards-oci`          
      140 +     só em push na `main`.                                    
      141 +   - O PR mostra o `workflow-policy` e o `image` verdes.      
      142 +   - Fecha com os dois `./mvnw` verdes.                       
      143 +3. **Etapa 3 — Job `deploy` e conferência** (FR-001 a FR-016; 
          +US1 a US3)                                                    
      144 +   - `verify-deployment.sh`, testado contra a imagem local nos
          + dois casos do quickstart.                                    
      145 +   - Job `deploy` conforme [`contracts/ci-deploy.md`](contract
          +s/ci-deploy.md), com a versão do `gcloud`                     
      146 +     fixada e os flags de probe conferidos nela (research R5).
      147 +   - No PR, o job aparece como pulado. A prova real vem com o 
          +merge.                                                        
      148 +   - Fecha com os dois `./mvnw` verdes.                       
      149 +4. **Etapa 4 — Documentação** (FR-023 a FR-025; US5)          
      150 +   - README: URL em "Avalie em 2 minutos", com o aviso da part
          +ida a frio; seção do Marco 5; requisitos do                   
      151 +     Marco 5.                                                 
      152 +   - ADR 0027; índice dos ADRs; runbook (volta do tráfego e pu
          +blicação com falha).                                          
      153 +   - `docs/ai/decisoes.md`.                                   
      154 +   - Itens de revisão no checklist da spec.                   
      155 +   - Fecha com os dois `./mvnw` verdes.                       
      156 +5. **Depois do merge** (Anderson faz o merge)                 
      157 +   - A execução da `main` publica e confere.                  
      158 +   - O resumo do job `deploy` é a comprovação de FR-001 a FR-0
          +16 e SC-001, SC-002, SC-005 e SC-006. O                       
      159 +     link dela é registrado na spec, que passa a `Implemented`
          +, num commit seguinte.                                        
      160 +   - Sessão exportada.                                        
      161 
       99 -ios/ or android/                                              
      100 -└── [platform-specific structure: feature modules, UI flows, p
          -latform tests]                                                
      101 -```                                                           
      162 +## Riscos                                                     
      163 
      103 -**Structure Decision**: [Document the selected structure and r
          -eference the real                                             
      104 -directories captured above]                                   
      164 +| Risco | Mitigação |                                         
      165 +| --- | --- |                                                 
      166 +| Um comando `gcloud run` pede uma permissão de projeto além d
          +as do serviço | Acrescentar só o papel de leitura que faltar (
          +`roles/run.viewer`), com o motivo no script (research R4) |   
      167 +| O Docker do runner usa o armazenamento do containerd, e o `I
          +d` muda de significado | A conferência aceita o digest da conf
          +iguração ou o do manifesto, e nenhum outro (research R1) |    
      168 +| O `skopeo` some da imagem do runner | O job confere a presen
          +ça e falha com mensagem clara. A alternativa é o `crane`, numa
          + mudança revisada |                                           
      169 +| A primeira execução real só acontece depois do merge | Scrip
          +t e verificação testados antes (etapas 1 e 3). Se a primeira p
          +ublicação falhar, o tráfego continua na revisão de exemplo, e 
          +a correção vem num PR seguinte |                              
      170 +| Custo inesperado | 0 a 1 instância, cobrança por requisição,
          + retenção de imagens e o alerta de orçamento do Anderson |    
      171 
      172  ## Complexity Tracking
      173 
      108 -> **Fill ONLY if Constitution Check has violations that must b
          -e justified**                                                 
      109 -                                                              
      110 -| Violation | Why Needed | Simpler Alternative Rejected Becaus
          -e |                                                           
      111 -|-----------|------------|------------------------------------
          --|                                                            
      112 -| [e.g., 4th project] | [current need] | [why 3 projects insuf
          -ficient] |                                                    
      113 -| [e.g., Repository pattern] | [specific problem] | [why direc
          -t DB access insufficient] |                                   
      174 +Sem violações da constituição. Nada a justificar.             

● O plano do Marco 5 está pronto. O bolt ficou em 4 etapas, cada uma terminando
  com ./mvnw verify e ./mvnw -Pextras verify verdes, mais o passo depois do
  merge. A Constitution Check passa antes e depois do design, sem nenhuma
  exceção a justificar.

  O setup-plan.ps1 devolveu 005-deploy-cloud-run como branch, mas o branch de
  trabalho é marco/5-deploy, que é o que está no plano. Escrevi:

  - specs/005-deploy-cloud-run/plan.md
  - specs/005-deploy-cloud-run/research.md (decisões R1 a R11)
  - specs/005-deploy-cloud-run/data-model.md
  - specs/005-deploy-cloud-run/quickstart.md
  - specs/005-deploy-cloud-run/contracts/ com setup-script.md,
    verify-deployment.md, ci-deploy.md e workflow-policy.md

  Decisões principais:

  - Imagem sem rebuild: o job image faz um único build com duas saídas: a
    awards:ci, que passa pelo teste de fumaça e pelo Trivy, e um tar OCI com o
    mesmo resultado. O job confere que as duas são a mesma imagem e expõe o
    digest. No job deploy, o skopeo copy --preserve-digests envia o tar ao
    registro, e o job confere o digest antes e depois do envio. A implantação
    usa @sha256:.
  - Actions novas, fixadas por hash:

  | Action                             | Versão | Hash      |
  |------------------------------------|--------|-----------|
  | google-github-actions/auth         | v3.0.0 | 7c6bc770… |
  | google-github-actions/setup-gcloud | v3.0.1 | aa5489c8… |
  | actions/download-artifact          | v8.0.2 | 9000827c… |
    As três rodam em node24 e não têm uses: aninhado. Conferi pelo action.yml de
    cada hash.
  - Sem deploy-cloudrun: o fluxo sem tráfego, conferência e promoção fica em
    comandos gcloud explícitos. Não precisa de mais uma action.
  - Identidade federada: a condição do provedor usa o ID numérico do repositório
    e do dono, mais refs/heads/main. Assim, renomear o repositório não abre
    brecha.
  - Permissões só no serviço e no repositório:
    - a conta de publicação recebe run.developer no serviço,
      artifactregistry.writer no repositório e serviceAccountUser na conta de
      execução;
    - a conta de execução não recebe nenhum papel;
    - o acesso público (allUsers) é concedido pelo script, e o pipeline não
      consegue mudá-lo.
  - O serviço é criado pelo script, com a imagem de exemplo do Cloud Run. É isso
    que permite dar as permissões no serviço e não no projeto. A URL fica
    conhecida antes do merge, para entrar no README.
  - Job deploy:
    - só roda em push na main deste repositório;
    - depende dos 10 checks obrigatórios;
    - tem grupo de concorrência próprio, sem cancelamento;
    - pede só contents: read e id-token: write;
    - os identificadores públicos ficam no env: do workflow, versionados.
  - Política do workflow ampliada: o id-token: write e o envio da imagem passam
    a ser permitidos só no job deploy. Duas regras novas no check-workflows.sh
    proíbem rebuild e exigem os 10 checks. A proibição de secrets. continua. O
    plano lista sete casos negativos que precisam reprovar.
  - Concorrência do topo: hoje ela cancelaria uma publicação no meio quando
    chega outro merge. Passa a não cancelar na main.
  - Script de configuração (deploy/gcp/setup.sh): cada passo confere o estado
    atual e só age se ele diverge. A saída é OK (já existia | criado | ajustado 
    | restaurado) ou FALHA, e a segunda execução tem que terminar com zero
    mudanças. Ele não usa jq, porque o jq não está instalado nesta máquina.

  Riscos registrados no plano:

  - Algum comando gcloud run pode pedir uma permissão de leitura no nível do
    projeto. A correção prevista é o papel mínimo que faltar, roles/run.viewer,
    e não run.admin.
  - Os flags de probe do gcloud ainda precisam ser conferidos na versão fixada,
    na etapa 3. A alternativa é um YAML versionado do serviço.
  - A primeira publicação real só acontece depois do merge. Se ela falhar, o
    tráfego continua na revisão de exemplo.

  O que depende de você:

  - Antes da etapa 1: o Google Cloud CLI não está instalado nesta máquina.
    Instale e, no prompt, rode ! gcloud auth login e ! gcloud config set project
    outsera-awards.
  - Opcional: criar uma regra de branch "só main" no ambiente cloud-run, nas
    configurações do repositório, como camada a mais de proteção.

  Não existe .specify/extensions.yml, então nenhum hook rodou. Ainda não há
  código nem commit. O próximo passo é o /speckit-tasks.

✻ Cogitated for 6m 3s · done 14:49

!  gcloud config list                                                           
  ⎿  /usr/bin/bash: gcloud: command not found
  ⎿  Interrupted · What should Claude do instead?

!  gcloud --version                                                             
  ⎿  Python n�o foi encontrado; executar sem argumentos para instalar do Mi
     crosoft Store ou desabilitar este atalho em Configura��es > Aplicativo
     s > Configura��es avan�adas do aplicativo > Aliases de execu��o do apl
     icativo.

● O gcloud já aparece no PATH, mas o script bash dele está chamando o atalho
  python da Microsoft Store em vez do Python que vem com o SDK. Vou procurar a
  instalação.

  Ran 2 shell commands (ctrl+o to expand)
  ⎿  1 agent type available
  ⎿  5 skills available
● O gcloud roda com o Python do SDK: versão 594.0.1, você já está logado e o
  projeto ativo é outsera-awards. Vou anotar essa particularidade do Windows no
  plano, pois o setup.sh vai precisar dela. · summary

● Update(specs\005-deploy-cloud-run\research.md)
  ⎿  Added 4 lines, removed 1 line
      173  - **Local:** `deploy/gcp/setup.sh` (fora do contexto da imagem
            pela lista de permissão do `.dockerignore`).
      174    Bash, `set -euo pipefail`, saída `OK`/`FALHA` por passo, com
            resumo no fim, como o `smoke-test.sh`.
      175    Precisa só do `gcloud` e do `curl`; não usa `jq` (lê os valo
           res com `--format=value(...)`). No Git Bash do
      176 -  Windows, as saídas do `gcloud` têm o `\r` removido.         
      176 +  Windows, as saídas do `gcloud` têm o `\r` removido. Nesse am
          +biente, o `gcloud` em bash procura `python` no PATH           
      177 +  e encontra o atalho da Microsoft Store (`Python não foi enco
          +ntrado`). Por isso, sem `CLOUDSDK_PYTHON` definido,           
      178 +  o script aponta para o Python do próprio SDK (`<sdk>/platfor
          +m/bundledpython/python.exe`, achado a partir de               
      179 +  `command -v gcloud`). Conferido em 2026-11-26 com o Google C
          +loud SDK 594.0.1.                                             
      180  - **Idempotência por convergência:** cada passo lê o estado at
           ual e só age se ele diverge do desejado.
      181    - Resultado `OK (já existia)` quando nada muda e `OK (criado
           )` ou `OK (ajustado)` quando muda.
      182    - A segunda execução tem que terminar com todos os passos em
            `OK (já existia)` (SC-004). O script termina

● Update(specs\005-deploy-cloud-run\research.md)
  ⎿  Added 2 lines, removed 1 line
      46  - **`deploy-cloudrun` não é usada.** A implantação sem tráfego,
           a conferência pela URL da revisão e a troca de
      47    tráfego (FR-014 e FR-015) são três passos `gcloud` explícitos
          . A action juntaria os passos e acrescentaria
      48    mais uma dependência fixada sem ganho.
      49 -- **`setup-gcloud` com versão fixada** (`version` com o número 
         -exato do `gcloud`, registrado no workflow), para               
      49 +- **`setup-gcloud` com versão fixada** (`version` com o número 
         +exato do `gcloud`, registrado no workflow; a                   
      50 +  mesma usada localmente na etapa 1, `594.0.1`, salvo motivo re
         +gistrado), para                                                
      51    que um `gcloud` novo do runner não mude os flags sem aviso. A
          tualizar é uma mudança revisada no PR.
      52 
      53  ## R3. Federação de identidade restrita ao repositório e à `mai
          n` (FR-009)

● O gcloud estava instalado. O erro vinha de outra coisa: no Git Bash, o gcloud
  procura python no PATH e encontra o atalho da Microsoft Store, não o Python
  que vem no SDK. Com CLOUDSDK_PYTHON apontando para o Python do próprio SDK,
  ele funciona:

  - versão 594.0.1;
  - conta ativa: a sua;
  - projeto ativo: outsera-awards.

  Para os seus comandos ! funcionarem, defina a variável uma vez para o seu
  usuário do Windows e reinicie o terminal do Claude Code:

  ! setx CLOUDSDK_PYTHON "C:\Program Files (x86)\Google\Cloud
  SDK\google-cloud-sdk\platform\bundledpython\python.exe"

  Outro caminho é desligar os atalhos python.exe e python3.exe em Configurações
  > Aplicativos > Configurações avançadas do aplicativo > Aliases de execução do
  aplicativo.

  O setup.sh não depende disso: quando a variável não estiver definida, ele
  mesmo aponta para o Python do SDK. Registrei isso na decisão R9 de
  research.md. Também fixei a 594.0.1 como versão candidata do gcloud no
  pipeline, na R2.

  O login e o projeto já estão prontos para a etapa 1. O próximo passo é o
  /speckit-tasks.

✻ Cooked for 37s · done 15:15

!  echo 'export CLOUDSDK_PYTHON="/c/Program Files (x86)/Google/Cloud            
  SDK/google-cloud-sdk/platform/bundledpython/python.exe"' >> ~/.bashrc         
  ⎿  (Bash completed with no output)

  Ran 2 shell commands (ctrl+o to expand)

● A linha entrou no ~/.bashrc e funciona: carregando o arquivo, o gcloud 
  --version responde 594.0.1.

  Neste terminal do Claude Code ainda não vale sozinho, porque o shell foi
  preparado quando a sessão começou e não relê o ~/.bashrc. Passa a valer quando
  você reiniciar o Claude Code. Até lá, eu rodo source ~/.bashrc antes dos
  comandos gcloud, e o setup.sh resolve isso por conta própria.

  O export aparece duas vezes no fim do ~/.bashrc. Não causa problema, mas se
  quiser pode apagar uma das linhas.

✻ Churned for 17s · done 15:16

❯ /speckit-tasks Cada etapa termina com o ./mvnw verify verde e um commit.      
Comprovações locais só quando forem rápidas; o que o pipeline comprova não      
precisa ser repetido na máquina. Marque como pedido ao Anderson só o que        
depende de mim: o push, o merge e configurações no GitHub. A execução do        
setup.sh com o gcloud é sua, já com o meu login. Pare só nesses pontos ou se    
algo falhar.                                                                    

  Ran 1 shell command (ctrl+o to expand)

● Write(specs\005-deploy-cloud-run\tasks.md)
  ⎿  Wrote 372 lines to specs\005-deploy-cloud-run\tasks.md
       1 ---
       2 
       3 description: "Tarefas do Marco 5: publicação da API no Google Cl
         oud Run"
       4 ---
       5 
       6 # Tasks: Marco 5 — Publicação da API no Google Cloud Run
       7 
       8 **Input**: documentos de `specs/005-deploy-cloud-run/` (spec, pl
         an, research, data-model, contracts e quickstart)
     … +364 lines (ctrl+o to expand)

● Gerei o specs/005-deploy-cloud-run/tasks.md, com 40 tarefas (T001 a T040) em 6
  fases. As quatro etapas do bolt terminam cada uma com ./mvnw verify e ./mvnw 
  -Pextras verify verdes e um commit.

  ┌────────────────┬───────────┬───────────┬────────────────────────────────┐
  │      Fase      │  Tarefas  │ Histórias │          Comprovação           │
  ├────────────────┼───────────┼───────────┼────────────────────────────────┤
  │ 1. Setup       │ T001      │ —         │ política de limpeza do         │
  │                │           │           │ registro                       │
  ├────────────────┼───────────┼───────────┼────────────────────────────────┤
  │                │           │           │ eu rodo o setup.sh duas vezes  │
  │ 2. Etapa 1:    │ T002–T011 │ US4       │ com o seu login; a segunda tem │
  │ GCP            │           │           │  que terminar sem nenhuma      │
  │                │           │           │ mudança                        │
  ├────────────────┼───────────┼───────────┼────────────────────────────────┤
  │ 3. Etapa 2:    │           │           │ check-workflows.sh local, com  │
  │ política e     │ T012–T019 │ US2       │ o yq baixado para o            │
  │ artefato       │           │           │ scratchpad; o resto vem do     │
  │                │           │           │ pipeline do PR                 │
  ├────────────────┼───────────┼───────────┼────────────────────────────────┤
  │                │           │           │ verify-deployment.sh contra a  │
  │ 4. Etapa 3:    │ T020–T027 │ US1, US2, │ aplicação rodando localmente,  │
  │ job deploy     │           │  US3      │ sem Docker, num caso que       │
  │                │           │           │ aprova e noutro que reprova    │
  ├────────────────┼───────────┼───────────┼────────────────────────────────┤
  │ 5. Etapa 4:    │ T028–T034 │ US5       │ revisão no PR, com o checklist │
  │ documentação   │           │           │  checklists/revisao.md         │
  ├────────────────┼───────────┼───────────┼────────────────────────────────┤
  │ 6. PR, merge e │ T035–T040 │ todas     │ execução real do pipeline na   │
  │  main          │           │           │ main                           │
  └────────────────┴───────────┴───────────┴────────────────────────────────┘

  Por história, sem contar a fase 6: US4 tem 10 tarefas, US2 tem 11, US3 tem 3,
  US1 tem 2 e US5 tem 7.

  Conferências na máquina: ficaram só as rápidas, que são T010, T018, T021, T022
  e T026. Build da imagem, envio, implantação e conferência na nuvem ficam por
  conta do pipeline.

  Paradas marcadas [Anderson]:
  - T035: push e abertura do PR;
  - T036: regra de branch do ambiente cloud-run, opcional;
  - T037: merge;
  - T039: push do commit que passa a spec para Implemented;
  - T040: exportação da sessão.

  Também paro se algo falhar.

  Ajustes que entraram nas tarefas:
  - Evidências: as saídas ficam em specs/005-deploy-cloud-run/evidencias/. O
    script não imprime o e-mail da conta ativa, porque essa saída vira evidência
    pública.
  - Commit da T039: a T039 acontece depois do merge. Como ninguém faz commit
    direto na main, ela vai num branch novo, com push seu.
  - Plano B dos probes: se o gcloud 594.0.1 não tiver os flags de probe, a T022
    troca a implantação por um deploy/gcp/service.yaml e ajusta a T024 e a T025.

  Paralelismo: a etapa 4 tem cinco tarefas em arquivos diferentes que podem
  rodar juntas (T028 e T030 a T033). Nas etapas 2 e 3 também há pares
  independentes.

  MVP: as etapas 1 a 3 já publicam e conferem a URL. A etapa 4 entra no mesmo
  PR, porque um extra só entra completo.

  Todas as tarefas seguem o formato - [ ] Txxx [P?] [USn], com o caminho dos
  arquivos. As da fase 1 e da fase 6 não levam rótulo de história. Não existe
  .specify/extensions.yml, então nenhum hook rodou.

  O próximo passo é o /speckit-implement, começando pela etapa 1.

✻ Worked for 2m 23s · done 15:19

❯ /speckit-analyze                                                              

  Searched for 2 patterns, ran 1 shell command · 1 failed (ctrl+o to expand)

● Relatório da análise de especificação: Marco 5

  Não há problema crítico nem violação da constituição. A implementação pode
  começar depois de dois ajustes nos artefatos: a política de retenção (achado
  C1) e o texto que fala em "toda publicação" (I1).

  ID: C1
  Categoria: Cobertura
  Gravidade: HIGH
  Onde: spec FR-021; research R9; tasks T001
  Problema: A política de limpeza tem só duas regras: manter as 10 mais
    recentes e apagar as imagens sem tag com mais de 7 dias. Como toda imagem
    recebe a tag do commit, a regra de apagar quase nunca se aplica. As
    imagens se acumulam sem limite, e o custo cresce a cada merge. A FR-021
    não é atendida
  Recomendação: Acrescentar uma regra que apague qualquer imagem (com ou sem
    tag) com mais de 30 dias. A regra que mantém as 10 mais recentes continua
    valendo por cima dela
  ────────────────────────────────────────
  ID: I1
  Categoria: Inconsistência
  Gravidade: MEDIUM
  Onde: spec US2 cenário 5 e SC-002; research R7
  Problema: A spec diz que as publicações acontecem "uma de cada vez, na ordem
    dos merges" e que 100% dos merges terminam com a URL servindo o commit. A
    R7 admite que, com três ou mais merges seguidos, a execução intermediária
    pendente é descartada pelo GitHub e não é publicada
  Recomendação: Ajustar a spec: a ordem nunca se inverte, nenhuma publicação é
    interrompida e o último commit sempre fica publicado. Um commit
    intermediário pode não chegar a ser publicado
  ────────────────────────────────────────
  ID: I2
  Categoria: Inconsistência
  Gravidade: MEDIUM
  Onde: spec FR-003; research R5
  Problema: A FR-003 diz "no máximo uma instância". A R5 registra que o limite
    vale por revisão, e que na troca a revisão antiga e a nova podem ter uma
    instância cada por alguns instantes
  Recomendação: Na FR-003, deixar explícito: no máximo uma instância por
    revisão, e uma revisão com tráfego por vez fora da troca. Citar a R5
  ────────────────────────────────────────
  ID: U1
  Categoria: Subespecificação
  Gravidade: MEDIUM
  Onde: spec US2 (teste independente e cenário 3); quickstart passo 6; FR-026
  Problema: A spec pede que uma tentativa de obter a identidade fora da main
    seja recusada pelo GCP. Nenhuma tarefa faz essa tentativa, e a política do
    workflow, de propósito, impede qualquer job além do deploy de pedir o
    token
  Recomendação: Alinhar o texto à prova real: a condição do provedor conferida
    na T010 e o job deploy pulado no PR (T035). Tirar "tentativa recusada" do
    teste independente
  ────────────────────────────────────────
  ID: C2
  Categoria: Cobertura
  Gravidade: MEDIUM
  Onde: spec SC-001; tasks T038
  Problema: Nenhuma tarefa mede o resultado de referência em até 30 s na
    partida a frio e em até 2 s com a instância ativa. O verify-deployment.sh
    registra só o tempo até a readiness
  Recomendação: Na T020, registrar no resumo o tempo de cada chamada. Na T038,
    conferir os dois limites, a partir do resumo ou de duas chamadas locais
    com curl -w '%{time_total}'
  ────────────────────────────────────────
  ID: C3
  Categoria: Cobertura
  Gravidade: LOW
  Onde: spec SC-006; tasks T038
  Problema: A T038 confere o máximo de 1 instância, mas não o mínimo de 0, que
    garante custo zero com o serviço ocioso
  Recomendação: Conferir também o mínimo de instâncias igual a 0
  ────────────────────────────────────────
  ID: G1
  Categoria: Governança
  Gravidade: MEDIUM
  Onde: AGENTS.md (papéis); plan, "Etapas do bolt"; tasks T016–T034
  Problema: O AGENTS.md diz que o workflow é do papel de infraestrutura e que
    README e ADRs são do papel de documentação, cada um numa sessão separada.
    As tarefas mandam esta sessão editar tudo. A constituição exige um único
    dono por arquivo em cada rodada
  Recomendação: Registrar no plano que, nesta rodada, esta sessão é a única
    dona de ci.yml, check-workflows.sh, README.md, ADRs e runbook, e que
    nenhuma outra sessão trabalha neles ao mesmo tempo
  ────────────────────────────────────────
  ID: S1
  Categoria: Segurança
  Gravidade: LOW
  Onde: spec FR-004; application.yml
  Problema: Com a autenticação desligada, /actuator/prometheus fica público na
    internet, como já acontece no modo padrão. As métricas não têm dado
    sensível, mas a exposição pública é uma decisão implícita
  Recomendação: Registrar a decisão de forma explícita no ADR 0027 (T030), ou
    não expor as métricas no Cloud Run
  ────────────────────────────────────────
  ID: A1
  Categoria: Ambiguidade
  Gravidade: LOW
  Onde: spec, caso de borda "primeira publicação"
  Problema: O caso de borda diz que na primeira publicação não há revisão
    anterior. Pelo plano (R4), o script cria o serviço com a imagem de
    exemplo, então sempre há uma revisão anterior
  Recomendação: Reescrever o caso de borda: a primeira publicação substitui a
    revisão de exemplo
  ────────────────────────────────────────
  ID: A2
  Categoria: Ambiguidade
  Gravidade: LOW
  Onde: tasks T014
  Problema: A regra 9 compara o if do job deploy como texto exato, o que é
    frágil: o mesmo if escrito com ${{ }} ou com outro espaçamento reprovaria
  Recomendação: Normalizar o texto antes de comparar (tirar ${{ }} e espaços
    nas pontas), ou aceitar as duas formas
  ────────────────────────────────────────
  ID: A3
  Categoria: Ambiguidade
  Gravidade: LOW
  Onde: spec FR-008; tasks T014
  Problema: A FR-008 pede todos os jobs do pipeline, e a regra 9 confere uma
    lista fixa de 10. Um job obrigatório novo no futuro passaria pela regra
    sem estar em needs
  Recomendação: Registrar no comentário da regra que a lista acompanha os
    checks obrigatórios da main
  ────────────────────────────────────────
  ID: L1
  Categoria: Estilo
  Gravidade: LOW
  Onde: redirecionamento do /swagger-ui.html
  Problema: Atrás do proxy do Cloud Run, o redirecionamento do Swagger pode
    sair com http://. Funciona, porque o Cloud Run devolve para https, mas
    custa um salto a mais
  Recomendação: Nenhuma ação agora. Se incomodar, a correção seria
    server.forward-headers-strategy, só que isso muda a configuração do modo
    padrão e fica fora do escopo

  O openapi.yaml não declara servers. Por isso o "Try it out" do Swagger chama a
  própria origem e funciona na URL pública (US1, cenário 4).

  Cobertura

  ┌─────────────────┬──────────┬─────────────────┬────────────────────────┐
  │    Requisito    │   Tem    │     Tarefas     │       Observação       │
  │                 │ tarefa?  │                 │                        │
  ├─────────────────┼──────────┼─────────────────┼────────────────────────┤
  │ FR-001          │ Sim      │ T024, T038      │                        │
  ├─────────────────┼──────────┼─────────────────┼────────────────────────┤
  │ FR-002          │ Sim      │ T017, T023      │                        │
  ├─────────────────┼──────────┼─────────────────┼────────────────────────┤
  │ FR-003          │ Sim      │ T005, T024,     │ ver I2                 │
  │                 │          │ T038            │                        │
  ├─────────────────┼──────────┼─────────────────┼────────────────────────┤
  │ FR-004          │ Sim      │ T006, T020      │ ver S1                 │
  ├─────────────────┼──────────┼─────────────────┼────────────────────────┤
  │ FR-005          │ Sim      │ T024            │ o JSON já vem da       │
  │                 │          │                 │ imagem; nada a fazer   │
  ├─────────────────┼──────────┼─────────────────┼────────────────────────┤
  │ FR-006          │ Sim      │ T024            │                        │
  ├─────────────────┼──────────┼─────────────────┼────────────────────────┤
  │ FR-007          │ Sim      │ T020, T025      │                        │
  ├─────────────────┼──────────┼─────────────────┼────────────────────────┤
  │ FR-008          │ Sim      │ T014, T023      │ ver A3                 │
  ├─────────────────┼──────────┼─────────────────┼────────────────────────┤
  │ FR-009          │ Sim      │ T004, T006,     │ ver U1                 │
  │                 │          │ T023            │                        │
  ├─────────────────┼──────────┼─────────────────┼────────────────────────┤
  │ FR-010          │ Sim      │ T017, T023      │                        │
  ├─────────────────┼──────────┼─────────────────┼────────────────────────┤
  │ FR-011          │ Sim      │ T016, T023      │ ver I1                 │
  ├─────────────────┼──────────┼─────────────────┼────────────────────────┤
  │ FR-012          │ Sim      │ T012–T015,      │                        │
  │                 │          │ T018, T026      │                        │
  ├─────────────────┼──────────┼─────────────────┼────────────────────────┤
  │ FR-013          │ Sim      │ T023            │                        │
  ├─────────────────┼──────────┼─────────────────┼────────────────────────┤
  │ FR-014, FR-015, │ Sim      │ T020, T024,     │                        │
  │  FR-015a        │          │ T025            │                        │
  ├─────────────────┼──────────┼─────────────────┼────────────────────────┤
  │ FR-016          │ Sim      │ T025            │                        │
  ├─────────────────┼──────────┼─────────────────┼────────────────────────┤
  │ FR-017 a FR-020 │ Sim      │ T002–T010       │                        │
  ├─────────────────┼──────────┼─────────────────┼────────────────────────┤
  │ FR-021          │ Parcial  │ T001, T003      │ ver C1                 │
  ├─────────────────┼──────────┼─────────────────┼────────────────────────┤
  │ FR-022          │ Sim      │ T011, T019,     │                        │
  │                 │          │ T027, T034      │                        │
  ├─────────────────┼──────────┼─────────────────┼────────────────────────┤
  │ FR-023          │ Sim      │ T028, T029      │                        │
  ├─────────────────┼──────────┼─────────────────┼────────────────────────┤
  │ FR-024          │ Sim      │ T030, T031      │                        │
  ├─────────────────┼──────────┼─────────────────┼────────────────────────┤
  │ FR-025          │ Sim      │ T032            │                        │
  ├─────────────────┼──────────┼─────────────────┼────────────────────────┤
  │ FR-026          │ Sim      │ T033, T038      │                        │
  ├─────────────────┼──────────┼─────────────────┼────────────────────────┤
  │ SC-001          │ Não      │ —               │ ver C2                 │
  ├─────────────────┼──────────┼─────────────────┼────────────────────────┤
  │ SC-002          │ Sim      │ T038            │ ver I1                 │
  ├─────────────────┼──────────┼─────────────────┼────────────────────────┤
  │ SC-003          │ Sim      │ T003, T010,     │                        │
  │                 │          │ T018            │                        │
  ├─────────────────┼──────────┼─────────────────┼────────────────────────┤
  │ SC-004          │ Sim      │ T009            │                        │
  ├─────────────────┼──────────┼─────────────────┼────────────────────────┤
  │ SC-005          │ Sim      │ T025, T038      │                        │
  ├─────────────────┼──────────┼─────────────────┼────────────────────────┤
  │ SC-006          │ Parcial  │ T038            │ ver C3                 │
  ├─────────────────┼──────────┼─────────────────┼────────────────────────┤
  │ SC-007          │ Sim      │ T011, T019,     │                        │
  │                 │          │ T027, T034      │                        │
  └─────────────────┴──────────┴─────────────────┴────────────────────────┘

  Constituição: nenhuma violação dos Princípios I a VII. O G1 é governança do
  AGENTS.md: a regra de dono único por arquivo é cumprida desde que fique
  registrado de quem é cada arquivo nesta rodada.

  Tarefas sem requisito: nenhuma. A T001 serve à FR-021. As tarefas T035 a T037
  e T040 são do fluxo do bolt.

  Métricas:
  - Requisitos: 34 (27 FR, com a FR-015a, e 7 SC).
  - Tarefas: 40.
  - Cobertura: 97%. Só o SC-001 não tem tarefa; FR-021 e SC-006 estão parciais.
  - Ambiguidades: 3. Duplicações: 0. Críticos: 0.

  Próximos passos

  1. Antes do /speckit-implement: corrigir o C1 na T001 e na R9. O ideal é
     corrigir junto o I1, o I2 e o U1 na spec. São edições de texto, sem mudar o
     desenho.
  2. Durante a implementação: o C2 e o C3 entram na T020 e na T038; o G1 vira
     uma linha no plano; o S1 entra no ADR 0027 (T030).
  3. O resto (A1, A2, A3 e L1) é opcional.

  Quer que eu proponha as edições concretas para os sete achados principais (C1,
  I1, I2, U1, C2, G1 e S1)? Só aplico depois do seu ok.

  Não existe .specify/extensions.yml, então nenhum hook rodou.

✻ Churned for 1m 14s · done 15:23

❯ Não precisa me mostrar as propostas: aplique todos os achados como você       
  recomendou (C1, I1, I2, U1, C2, C3, G1, A1, A2 e A3), com uma exceção. No S1, 
  não exponha as métricas no Cloud Run: o deploy define                         
  MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=health, que mantém só o que as      
  sondas usam. Registre a decisão no ADR 0027 e confira na T038 que             
  /actuator/prometheus responde 404 na URL pública. Depois faça o commit da     
  spec e comece o /speckit-implement pela etapa 1.                              

● Vou aplicar os achados nos artefatos, começando pela spec.

● Update(specs\005-deploy-cloud-run\spec.md)
  ⎿  Added 4 lines, removed 2 lines
      73     identidade federada do projeto, **Then** o GCP recusa a troc
          a.
      74  4. **Given** qualquer job anterior vermelho na `main`, **When**
           o pipeline termina, **Then** nada é
      75     publicado nem implantado.
      76 -5. **Given** dois merges seguidos na `main`, **When** os pipeli
         -nes rodam, **Then** as publicações acontecem                   
      77 -   uma de cada vez, na ordem dos merges, e nenhuma é interrompi
         -da no meio.                                                    
      76 +5. **Given** dois ou mais merges seguidos na `main`, **When** o
         +s pipelines rodam, **Then** as publicações                     
      77 +   acontecem uma de cada vez, a ordem nunca se inverte, nenhuma
         + é interrompida no meio e o último commit                      
      78 +   mergeado fica publicado. Um commit intermediário pode não se
         +r publicado: o GitHub mantém só a execução                     
      79 +   pendente mais nova (research R7).                           
      80 
      81  ---
      82 

● Update(specs\005-deploy-cloud-run\spec.md)
  ⎿  Added 2 lines, removed 2 lines
      59 
      60  **Independent Test**: um merge na `main` produz uma execução do
           pipeline em que o job de publicação fica
      61  verde e a revisão nova do serviço aponta para a imagem pelo dig
          est gerado naquela execução; um push em outra
      62 -branch ou um pull request não chega ao job de publicação, e uma
         - tentativa de obter a identidade fora da                       
      63 -`main` é recusada pelo GCP.                                    
      62 +branch ou um pull request não chega ao job de publicação, e a c
         +ondição do provedor de identidade, conferida no                
      63 +GCP, só aceita este repositório (pelo ID) e `refs/heads/main`. 
      64 
      65  **Acceptance Scenarios**:
      66 

● Update(specs\005-deploy-cloud-run\spec.md)
  ⎿  Added 4 lines, removed 2 lines
      69     com esse digest.
      70  2. **Given** um push em outra branch ou um pull request (inclus
          ive de fork), **When** o pipeline roda,
      71     **Then** o job de publicação não executa, e nada é publicado
          .
      72 -3. **Given** um workflow rodando em outro repositório ou em out
         -ra branch, **When** ele tenta obter a                          
      73 -   identidade federada do projeto, **Then** o GCP recusa a troc
         -a.                                                             
      72 +3. **Given** o provedor de identidade configurado, **When** a c
         +ondição dele é lida no GCP, **Then** ela                       
      73 +   exige o ID deste repositório, o ID do dono e `refs/heads/mai
         +n`, de modo que um token de outro repositório                  
      74 +   ou de outra branch não é aceito. A política do workflow impe
         +de qualquer job além do de publicação de pedir                 
      75 +   o token, então essa prova é pela condição, e não por uma ten
         +tativa real.                                                   
      76  4. **Given** qualquer job anterior vermelho na `main`, **When**
           o pipeline termina, **Then** nada é
      77     publicado nem implantado.
      78  5. **Given** dois ou mais merges seguidos na `main`, **When** o
          s pipelines rodam, **Then** as publicações

● Update(specs\005-deploy-cloud-run\spec.md)
  ⎿  Added 3 lines, removed 2 lines
      169    e o job de publicação fica vermelho.
      170  - **Revisão nova que sobe mas responde errado:** como ela é im
           plantada sem tráfego, a conferência da US3
      171    reprova pela URL própria dela, e o tráfego nunca sai da revi
           são anterior. O job fica vermelho.
      172 -- **Primeira publicação, sem revisão anterior:** não há tráfeg
          -o a preservar; a revisão é conferida pela URL                 
      173 -  própria e só então recebe o tráfego.                        
      172 +- **Primeira publicação:** o script cria o serviço com uma rev
          +isão de exemplo (research R4), então sempre                   
      173 +  existe uma revisão anterior. A primeira publicação pelo pipe
          +line segue o mesmo fluxo e substitui a de                     
      174 +  exemplo só depois de conferida.                             
      175  - **Pico de chamadas com max-instances=1:** as chamadas além d
           a capacidade da instância esperam ou recebem
      176    429/503 da plataforma; nenhuma segunda instância é criada. I
           sso é aceito para a demonstração.
      177  - **Merge só de documentação:** o pipeline roda inteiro e publ
           ica de novo a imagem desse commit; o resultado

● Update(specs\005-deploy-cloud-run\spec.md)
  ⎿  Added 10 lines, removed 5 lines
      199  - **FR-002**: O serviço MUST rodar a **mesma imagem** construí
           da e verificada no pipeline daquele commit
      200    (teste de fumaça e porta de vulnerabilidades da imagem), ref
           erenciada pelo digest, e não uma imagem
      201    reconstruída depois.
      202 -- **FR-003**: O serviço MUST ter no máximo **uma** instância e
          - no mínimo **zero**. Essa é a condição que                    
      203 -  mantém válido o cache em memória do ADR 0026 e limita o cust
          -o.                                                            
      204 -- **FR-004**: Os endpoints de negócio, o Swagger UI e os endpo
          -ints operacionais públicos do modo padrão MUST                
      205 -  responder a chamadas anônimas: a autenticação fica desligada
          -, como no modo padrão (Princípio VII), e o                    
      206 -  serviço aceita invocação sem identidade do GCP.             
      202 +- **FR-003**: Cada revisão do serviço MUST ter no máximo **uma
          +** instância e no mínimo **zero**, e fora da                  
      203 +  troca de revisão só uma revisão recebe tráfego. Na troca, a 
          +revisão antiga e a nova podem ter uma instância               
      204 +  cada por alguns instantes; no modo padrão isso não afeta o c
          +ache do ADR 0026, porque cada instância tem o                 
      205 +  próprio H2 e não grava nada depois da carga (research R5). O
          + limite também mantém o custo baixo.                          
      206 +- **FR-004**: Os endpoints de negócio, o Swagger UI, o contrat
          +o OpenAPI e os probes de saúde (`/actuator/health`,           
      207 +  liveness e readiness) MUST responder a chamadas anônimas: a 
          +autenticação fica desligada, como no modo padrão              
      208 +  (Princípio VII), e o serviço aceita invocação sem identidade
          + do GCP.                                                      
      209 +- **FR-004a**: As métricas MUST NOT ficar expostas na URL públ
          +ica: a publicação define                                      
      210 +  `MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=health`, que mant
          +ém só o que os probes usam, e                                 
      211 +  `/actuator/prometheus` responde 404. A configuração do modo 
          +padrão e da imagem não muda.                                  
      212  - **FR-005**: A porta MUST vir da variável `PORT` fornecida pe
           la plataforma, e o log MUST sair em JSON, como a
      213    imagem já define para a execução em container (constituição,
            Observabilidade).
      214  - **FR-006**: Uma revisão nova MUST receber tráfego só depois
           da readiness positiva (fim da carga do CSV), e a

● Update(specs\005-deploy-cloud-run\spec.md)
  ⎿  Added 2 lines, removed 1 line
      219  **Publicação pelo pipeline**
      220 
      221  - **FR-008**: A publicação MUST rodar automaticamente a cada p
           ush na `main` deste repositório (merge de pull
      222 -  request), só depois de todos os jobs do pipeline ficarem ver
          -des, e MUST NOT rodar em pull requests, em                    
      222 +  request), só depois de todos os checks obrigatórios da `main
          +` ficarem verdes (hoje, os 10 do Marco 4; a                   
      223 +  política do workflow confere a lista, que acompanha os check
          +s obrigatórios), e MUST NOT rodar em pull requests, em        
      224    outras branches nem em forks.
      225  - **FR-009**: A autenticação do pipeline no GCP MUST usar OIDC
            com Workload Identity Federation. O provedor de
      226    identidade MUST aceitar só tokens deste repositório e da bra
           nch `main`. MUST NOT existir chave de conta de

● Update(specs\005-deploy-cloud-run\spec.md)
  ⎿  Added 2 lines, removed 1 line
      228  - **FR-010**: A imagem MUST ser publicada no Artifact Registry
            do projeto, na mesma região, identificada pelo
      229    commit, e o serviço MUST ser implantado pelo digest dessa pu
           blicação.
      230  - **FR-011**: As publicações MUST ser serializadas: uma execuç
           ão nova espera a anterior terminar e não a
      231 -  cancela.                                                    
      231 +  cancela, a ordem nunca se inverte e o último commit mergeado
          + fica publicado. Com várias execuções na fila,                
      232 +  as intermediárias podem ser descartadas.                    
      233  - **FR-012**: As permissões de escrita do pipeline (identidade
            federada e publicação de imagem) MUST existir só
      234    no job de publicação. A política do workflow (`check-workflo
           ws.sh`) MUST ser ajustada para permitir
      235    exatamente essas exceções nesse job e continuar reprovando q
           ualquer outra, inclusive referência a

● Update(specs\005-deploy-cloud-run\spec.md)
  ⎿  Added 2 lines, removed 2 lines
      267    `outsera-awards`, e MUST parar com mensagem clara se algo fa
           ltar.
      268  - **FR-020**: O script MUST rodar com o `gcloud` depois do log
           in interativo do Anderson. Nenhuma credencial é
      269    gravada no repositório, no script nem na evidência de IA.
      270 -- **FR-021**: O registro MUST ter política de retenção que rem
          -ove as imagens antigas, mantendo as recentes                  
      271 -  necessárias para voltar a uma revisão anterior.             
      270 +- **FR-021**: O registro MUST ter política de retenção que rem
          +ove as imagens antigas, com ou sem tag, depois                
      271 +  de 30 dias, e sempre mantém as 10 mais recentes, necessárias
          + para voltar a uma revisão anterior.                          
      272 
      273  **Modo padrão e documentação**
      274 
● Update(specs\005-deploy-cloud-run\spec.md)
  ⎿  Added 4 lines, removed 2 lines
      285  **Comprovação (Princípio VI)**
      286 
      287  - **FR-026**: Os requisitos MUST ser comprovados assim:
      288 -  - FR-001 a FR-016 (inclusive FR-015a): execução do pipeline 
          -na `main` (jobs de publicação e de conferência), mais a recusa
          - da                                                           
      289 -    federação fora da `main` comprovada pela condição do prove
          -dor, conferida no script e no resumo;                         
      288 +  - FR-001 a FR-016 (inclusive FR-004a e FR-015a): execução do
          + pipeline na `main` (jobs de publicação e de                  
      289 +    conferência) e conferência da URL pública depois do merge,
          + inclusive o 404 de `/actuator/prometheus`;                   
      290 +    a restrição da federação é comprovada pela condição do pro
          +vedor, conferida no GCP depois do script, e                   
      291 +    pelo job de publicação pulado no pull request;            
      292    - FR-017 a FR-021: duas execuções do script, com a saída da
           segunda registrada, e revisão do script no PR;
      293    - FR-022: os dois comandos Maven verdes, sem alteração nos I
           Ts (`check-unchanged-tests.sh`);
      294    - FR-023 a FR-025: revisão no PR, com item no checklist da s
           pec.

● Update(specs\005-deploy-cloud-run\spec.md)
  ⎿  Added 2 lines, removed 2 lines
      321    nenhum recurso.
      322  - **SC-005**: Do merge na `main` à URL conferida, o tempo adic
           ional da publicação e da conferência fica abaixo
      323    de 10 minutos.
      324 -- **SC-006**: Com o serviço ocioso, o custo é zero instâncias 
          -em execução; o serviço nunca passa de uma                     
      325 -  instância.                                                  
      324 +- **SC-006**: Com o serviço ocioso, o custo é zero instâncias 
          +em execução (mínimo 0); cada revisão nunca passa              
      325 +  de uma instância (máximo 1).                                
      326  - **SC-007**: O `./mvnw verify` do modo padrão e o `./mvnw -Pe
           xtras verify` continuam verdes, com os mesmos ITs
      327    de antes do marco.
      328 

● Update(specs\005-deploy-cloud-run\spec.md)
  ⎿  Added 2 lines, removed 2 lines
      313 
      314  - **SC-001**: Um avaliador sem nada instalado obtém o resultad
           o de referência pela URL do README em até 30
      315    segundos, contando a partida a frio, e em até 2 segundos com
            a instância ativa.
      316 -- **SC-002**: 100% dos merges na `main` com o pipeline verde t
          -erminam com a URL pública servindo o commit                   
      317 -  mergeado, conferida pelo próprio pipeline, sem nenhum passo 
          -manual.                                                       
      316 +- **SC-002**: Depois de cada merge na `main` com o pipeline ve
          +rde, a URL pública passa a servir o commit mais               
      317 +  recente da `main`, conferida pelo próprio pipeline, sem nenh
          +um passo manual.                                              
      318  - **SC-003**: Zero chaves de conta de serviço existem no proje
           to GCP para a conta de publicação, e zero
      319    referências a segredos existem no workflow.
      320  - **SC-004**: Uma segunda execução do script de configuração t
           ermina com sucesso sem criar, remover ou alterar

● Update(specs\005-deploy-cloud-run\spec.md)
  ⎿  Couldn't edit file

● Update(specs\005-deploy-cloud-run\spec.md)
  ⎿  Added 4 lines
      16    nova é implantada **sem tráfego** e conferida pela URL própri
          a dela. O tráfego só passa para ela depois de a
      17    conferência aprovar, e a URL pública é conferida de novo depo
          is da troca. Uma revisão reprovada nunca recebe
      18    tráfego, e a anterior continua servindo. US3, FR-014, FR-015
          e FR-015a.
      19 +- Q: (análise, 2026-11-26) As métricas ficam públicas na URL pu
         +blicada, já que a autenticação está desligada?                 
      20 +  → A: Não. A publicação define `MANAGEMENT_ENDPOINTS_WEB_EXPOS
         +URE_INCLUDE=health`, que mantém só o que os                    
      21 +  probes usam; `/actuator/prometheus` responde 404 na URL públi
         +ca. Decisão registrada no ADR 0027. FR-004 e                   
      22 +  FR-004a.                                                     
      23 
      24  ## User Scenarios & Testing *(mandatory)*
      25 

● Update(specs\005-deploy-cloud-run\spec.md)
  ⎿  Added 2 lines, removed 1 line
      214    `MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=health`, que mant
           ém só o que os probes usam, e
      215    `/actuator/prometheus` responde 404. A configuração do modo
           padrão e da imagem não muda.
      216  - **FR-005**: A porta MUST vir da variável `PORT` fornecida pe
           la plataforma, e o log MUST sair em JSON, como a
      217 -  imagem já define para a execução em container (constituição,
          - Observabilidade).                                            
      217 +  imagem já define para a execução em container (constituição,
          + Observabilidade). A única variável que a                     
      218 +  publicação acrescenta é a da FR-004a.                       
      219  - **FR-006**: Uma revisão nova MUST receber tráfego só depois
           da readiness positiva (fim da carga do CSV), e a
      220    liveness MUST usar o probe de liveness da aplicação.
      221  - **FR-007**: A resposta de `GET /api/producers/intervals` na
           URL pública MUST ser idêntica à do modo padrão

● Agora a research, os contratos, o plano e as tarefas.

● Update(specs\005-deploy-cloud-run\research.md)
  ⎿  Added 7 lines, removed 3 lines
      104    - **startup:** `GET /actuator/health/readiness`, período de
           2 s, até 60 falhas (cerca de 2 min). O Cloud Run
      105      só manda tráfego à instância depois disso (FR-006);
      106    - **liveness:** `GET /actuator/health/liveness`, período de
           30 s.
      107 -- Nenhuma variável nova. A imagem já define `LOGGING_STRUCTURE
          -D_FORMAT_CONSOLE=ecs` (JSON) e o Cloud Run                    
      108 -  define `PORT`. `API_TOKEN`, perfis e `MOVIES_CSV_PATH` **não
          -** são definidos: vale o modo padrão (FR-001,                 
      109 -  FR-004).                                                    
      107 +- **Uma única variável nova:** `MANAGEMENT_ENDPOINTS_WEB_EXPOS
          +URE_INCLUDE=health` (`--set-env-vars`), que                   
      108 +  sobrepõe o `include: health,prometheus` do `application.yml`
          + só no Cloud Run. Ficam `/actuator/health` e os               
      109 +  grupos de liveness e readiness, que os probes usam, e `/actu
          +ator/prometheus` passa a responder 404 na URL                 
      110 +  pública (FR-004a). Com a autenticação desligada, as métricas
          + ficariam abertas na internet, e não há                       
      111 +  Prometheus coletando no Cloud Run. O modo padrão e a imagem 
          +não mudam.                                                    
      112 +- A imagem já define `LOGGING_STRUCTURED_FORMAT_CONSOLE=ecs` (
          +JSON), e o Cloud Run define `PORT`. `API_TOKEN`,              
      113 +  perfis e `MOVIES_CSV_PATH` **não** são definidos: vale o mod
          +o padrão (FR-001, FR-004).                                    
      114  - **1 vCPU, 1 GiB e o impulso de CPU na partida:** a JVM usa 7
           5% da memória (`MaxRAMPercentage`), e o impulso
      115    encurta a partida a frio (SC-001), sem custo com o serviço o
           cioso. Cobrança por requisição (padrão), sem CPU
      116    sempre alocada.

● Update(specs\005-deploy-cloud-run\research.md)
  ⎿  Added 1 line, removed 1 line
      114  - **1 vCPU, 1 GiB e o impulso de CPU na partida:** a JVM usa 7
           5% da memória (`MaxRAMPercentage`), e o impulso
      115    encurta a partida a frio (SC-001), sem custo com o serviço o
           cioso. Cobrança por requisição (padrão), sem CPU
      116    sempre alocada.
      117 -- **`max-instances=1` é por revisão.** Na troca, a revisão ant
          -iga e a nova podem ter uma instância cada por                 
      117 +- **`max-instances=1` é por revisão** (FR-003). Na troca, a re
          +visão antiga e a nova podem ter uma instância cada por        
      118    alguns instantes. No modo padrão isso não quebra o ADR 0026:
            cada instância tem o próprio H2 e não grava
      119    nada depois da carga, então não há invalidação entre instânc
           ias a perder. Fora da troca, o tráfego vai para
      120    uma revisão só, com no máximo uma instância.

● Update(specs\005-deploy-cloud-run\research.md)
  ⎿  Added 3 lines, removed 2 lines
      195    (`gcloud billing projects describe`); IDs do repositório lid
           os da API do GitHub. Qualquer falha encerra
      196    antes do primeiro passo de escrita, com `FALHA` e o motivo.
      197  - **Passos:** APIs (`run`, `artifactregistry`, `iam`, `iamcred
           entials`, `sts`); repositório Docker `awards`
      198 -  com a política de limpeza versionada (`deploy/gcp/cleanup-po
          -licy.json`: mantém as 10 versões mais recentes e              
      199 -  apaga as sem tag com mais de 7 dias); as duas contas de serv
          -iço; o pool, o provedor e a condição; o serviço               
      198 +  com a política de limpeza versionada (`deploy/gcp/cleanup-po
          +licy.json`: uma regra `DELETE` para qualquer                  
      199 +  versão, com ou sem tag, com mais de 30 dias, e uma regra `KE
          +EP` para as 10 mais recentes, que prevalece sobre             
      200 +  a de apagar; só a `DELETE` de imagens sem tag não bastaria, 
          +porque toda imagem recebe a tag do commit); as duas contas de 
          +serviço; o pool, o provedor e a condição; o serviço           
      201    com a imagem de exemplo (só se não existir); os vínculos de
           IAM da R4; e a impressão dos identificadores
      202    públicos (número do projeto, provedor completo, e-mail da co
           nta de publicação, registro e URL do serviço).
      203  - **Nenhuma credencial** é criada, baixada ou impressa. O scri
           pt confere que a conta de publicação não tem

● Update(specs\005-deploy-cloud-run\research.md)
  ⎿  Added 4 lines, removed 1 line
      147    Um fork que copia o workflow nem chega a pedir o token; se p
           edisse, o GCP recusaria (R3).
      148  - `needs:` com **os 10 checks obrigatórios da `main`** (Marco
           4, T064): `workflow-policy`, `build`,
      149    `postgres`, `codeql`, `dependencies`, `dockerfile-lint`, `im
           age`, `alert-rules`, `load` e `compose`. A
      150 -  política do workflow (R8) confere essa lista.               
      150 +  política do workflow (R8) confere essa lista. Um check obrig
          +atório novo exige atualizar a lista na política               
      151 +  e no `needs`, e o comentário da regra 9 diz isso.           
      152 +- A comparação do `if` na política normaliza o texto: tira `${
          +{ }}` e os espaços nas pontas e reduz espaços                 
      153 +  repetidos a um só. Assim, as duas formas aceitas pelo GitHub
          + passam, e qualquer outra condição reprova.                   
      154  - **Concorrência:** a do topo hoje é `cancel-in-progress: true
           ` por ref, o que cancelaria uma publicação no meio
      155    quando chega outro merge. Ela passa a `cancel-in-progress: $
           {{ github.ref != 'refs/heads/main' }}`. O job
      156    `deploy` ganha o grupo próprio `deploy-cloud-run`, com `canc
           el-in-progress: false`.

● Update(specs\005-deploy-cloud-run\research.md)
  ⎿  Added 4 lines, removed 1 line
      134     são removidas, para os endereços antigos não continuarem ac
           essíveis.
      135  4. O mesmo script na URL pública do serviço. Se reprova aqui,
           o job fica vermelho, e o runbook descreve a
      136     volta à revisão anterior.
      137 -5. O resumo da execução recebe a URL, o digest, a revisão e o 
          -resultado de cada conferência (FR-016).                       
      137 +5. O resumo da execução recebe a URL, o digest, a revisão e o 
          +resultado de cada conferência (FR-016), com o                 
      138 +   tempo de cada chamada. Na conferência pela URL da revisão, 
          +a primeira chamada pega a partida a frio, e o                 
      139 +   tempo dela comprova os 30 s do SC-001. A chamada de interva
          +los na URL pública, com a instância já ativa,                 
      140 +   comprova os 2 s.                                           
      141 
      142  - A referência é a mesma `REFERENCE` do `smoke-test.sh`, compa
           rada com `jq -S`, com as chaves exatas do
      143    contrato.

● Update(specs\005-deploy-cloud-run\contracts\verify-deployment.md)
  ⎿  Added 2 lines, removed 1 line
      22 
      23  ## Saída
      24 
      25 -- Uma linha `OK`/`FALHA` por passo. Em `FALHA`, o status e o co
         -rpo recebidos, truncados em 2 KB.                              
      25 +- Uma linha `OK`/`FALHA` por passo, com o tempo de cada chamada
         + (`curl -w '%{time_total}'`) e o tempo da                      
      26 +  primeira chamada até a readiness (SC-001). Em `FALHA`, o stat
         +us e o corpo recebidos, truncados em 2 KB.                     
      27  - Com `GITHUB_STEP_SUMMARY` definido, uma tabela com a URL, cad
          a passo e o tempo até a readiness.
      28  - Código de saída `0` se os três passos aprovam, `1` se algum f
          alha e `2` se falta uma ferramenta.

● Update(specs\005-deploy-cloud-run\contracts\verify-deployment.md)
  ⎿  Added 4 lines
      19  | 1 | Readiness | `GET /actuator/health/readiness` devolve 200
          com `{"status":"UP"}` dentro da espera máxima; tenta a cada 3 s
          , com tempo limite de 10 s por chamada (partida a frio) |
      20  | 2 | Resultado de referência | `GET /api/producers/intervals`
          devolve 200, `application/json`, e o corpo, ordenado com `jq -S
          `, é igual à `REFERENCE` do `smoke-test.sh` (chaves exatas, sem
           campos extras) |
      21  | 3 | Swagger UI e contrato | `GET /swagger-ui.html`, seguindo
          redirecionamentos, devolve 200, e `GET /openapi.yaml` devolve 2
          00 |
      22 +| 4 | Métricas fechadas (só com `--expect-no-metrics`) | `GET /
         +actuator/prometheus` devolve 404 (FR-004a) |                   
      23 
      24 +O passo 4 é ligado pela opção `--expect-no-metrics`, usada no p
         +ipeline e na T038. Na conferência local contra                 
      25 +`./mvnw`, ele fica desligado, porque o modo padrão expõe as mét
         +ricas.                                                         
      26 +                                                               
      27  ## Saída
      28 
      29  - Uma linha `OK`/`FALHA` por passo, com o tempo de cada chamada
           (`curl -w '%{time_total}'`) e o tempo da

● Update(specs\005-deploy-cloud-run\contracts\ci-deploy.md)
  ⎿  Added 3 lines, removed 2 lines
      40  6. Envio: `skopeo copy --preserve-digests oci-archive:awards-oc
          i.tar docker://$GCP_REGISTRY/awards:${{ github.sha }}`,
      41     autenticado por um token de acesso do `gcloud` passado por `
          stdin`. Depois, a conferência de que o digest no
      42     registro é o mesmo.
      43 -7. `gcloud run deploy` com `--no-traffic --tag=c-<sha curto>` e
         - os parâmetros da research R5.                                 
      44 -8. `verify-deployment.sh` na URL da tag.                       
      43 +7. `gcloud run deploy` com `--no-traffic --tag=c-<sha curto>`, 
         +os parâmetros da research R5 e                                 
      44 +   `--set-env-vars=MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=he
         +alth`.                                                         
      45 +8. `verify-deployment.sh --expect-no-metrics` na URL da tag.   
      46  9. `gcloud run services update-traffic` com 100% para a revisão
           e a remoção das tags anteriores.
      47  10. `verify-deployment.sh` na URL pública.
      48  11. Resumo: URL, digest, revisão, resultado das conferências e
          duração (`if: always()`).

● Update(specs\005-deploy-cloud-run\contracts\ci-deploy.md)
  ⎿  Added 1 line, removed 1 line
      44     `--set-env-vars=MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=he
          alth`.
      45  8. `verify-deployment.sh --expect-no-metrics` na URL da tag.
      46  9. `gcloud run services update-traffic` com 100% para a revisão
           e a remoção das tags anteriores.
      47 -10. `verify-deployment.sh` na URL pública.                     
      47 +10. `verify-deployment.sh --expect-no-metrics` na URL pública. 
      48  11. Resumo: URL, digest, revisão, resultado das conferências e
          duração (`if: always()`).
      49 
      50  ## Garantias

● Update(specs\005-deploy-cloud-run\contracts\workflow-policy.md)
  ⎿  Added 1 line, removed 1 line
       6  | --- | --- |
       7  | 3 | um job pede escrita, exceto `security-events: write` no `
          codeql` e `id-token: write` no `deploy` |
       8  | 5 | `push: true` no `docker/build-push-action` ou `docker pus
          h` em qualquer job; `skopeo copy` (ou `skopeo` com destino `doc
          ker://`) fora do job `deploy` |
       9 -| 9 | existe o job `deploy` e: o `if` não é exatamente o do con
         -trato `ci-deploy.md`; falta um dos 10 checks em `needs`; a conc
         -orrência não é o grupo `deploy-cloud-run` com `cancel-in-progre
         -ss: false`; ou há `docker/build-push-action`, `docker build` ou
         - `docker buildx build` no job |                                
       9 +| 9 | existe o job `deploy` e: o `if`, normalizado (sem `${{ }}
         +`, sem espaços nas pontas e com espaços repetidos reduzidos), n
         +ão é o do contrato `ci-deploy.md`; falta um dos 10 checks em `n
         +eeds`; a concorrência não é o grupo `deploy-cloud-run` com `can
         +cel-in-progress: false`; ou há `docker/build-push-action`, `doc
         +ker build` ou `docker buildx build` no job |                   
      10  | 10 | o `concurrency.cancel-in-progress` do topo é `true` fixo
           |
      11 
      12  ## Casos negativos conferidos na etapa 2

● Update(specs\005-deploy-cloud-run\contracts\setup-script.md)
  ⎿  Added 1 line, removed 1 line
      44  | --- | --- | --- |
      45  | 0 | `gcloud` e `curl` disponíveis; conta ativa; projeto exist
          e e está `ACTIVE`; faturamento ligado; IDs do repositório e do
          dono lidos do GitHub | verificação (sem escrita) |
      46  | 1 | APIs ativadas | escrita |
      47 -| 2 | Repositório `awards` e política de limpeza | escrita |   
      47 +| 2 | Repositório `awards` e política de limpeza (apaga versões
         + com mais de 30 dias e mantém as 10 mais recentes) | escrita | 
      48  | 3 | Conta de execução | escrita |
      49  | 4 | Conta de publicação, sem chaves gerenciadas pelo usuário
          | escrita e verificação |
      50  | 5 | Pool `github` | escrita |

● Update(specs\005-deploy-cloud-run\plan.md)
  ⎿  Added 7 lines, removed 3 lines
      121  ## Etapas do bolt
      122 
      123  Cada etapa termina com `./mvnw verify` e `./mvnw -Pextras veri
           fy` verdes, com o resultado real informado, e
      124 -tem commits próprios. Os arquivos compartilhados seguem o dono
          - da rodada (AGENTS.md): o workflow e os scripts               
      125 -são do papel de infraestrutura; o README e os ADRs, do de docu
          -mentação, por pedido registrado se for outra                  
      126 -sessão.                                                       
      124 +tem commits próprios.                                         
      125 
      126 +**Donos nesta rodada (AGENTS.md, um dono por arquivo compartil
          +hado):** esta sessão do Claude Code acumula os                
      127 +papéis de implementador, de infraestrutura e de documentação d
          +o Marco 5 e é a **única dona**, nesta rodada, de              
      128 +`.github/workflows/ci.yml`, `.github/scripts/check-workflows.s
          +h`, `.github/scripts/verify-deployment.sh`,                   
      129 +`deploy/gcp/`, `README.md`, `docs/adr/`, `docs/runbook.md` e `
          +docs/ai/decisoes.md`. Nenhuma outra sessão                    
      130 +trabalha nesses arquivos ao mesmo tempo. A revisão do PR conti
          +nua com o Codex.                                              
      131 +                                                              
      132  1. **Etapa 1 — Configuração do GCP** (FR-017 a FR-021; US4)
      133     - `deploy/gcp/setup.sh` e `cleanup-policy.json`, conforme [
           `contracts/setup-script.md`](contracts/setup-script.md).
      134     - O Anderson faz o login. O Claude Code roda o script duas
           vezes, e a saída das duas execuções fica nas

● Update(specs\005-deploy-cloud-run\plan.md)
  ⎿  Added 3 lines, removed 1 line
      72  - A política do workflow é ampliada (research R8). A ampliação
          é estreita: só o job `deploy` ganha escrita
      73    (`id-token`) e publicação, e novas regras impedem rebuild e e
          xigem os 10 checks.
      74  - `max-instances=1` é por revisão, então duas instâncias podem
          coexistir por instantes na troca. No modo padrão
      75 -  isso não fere o ADR 0026 (research R5), e o ADR do marco regi
         -stra o motivo.                                                 
      75 +  isso não fere o ADR 0026 (FR-003, research R5), e o ADR do ma
         +rco registra o motivo.                                         
      76 +- As métricas ficam fechadas no Cloud Run por uma variável da p
         +ublicação, a única nova                                        
      77 +  (`MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=health`; FR-004a)
         +, sem mudar o modo padrão nem a imagem.                        
      78 
      79  ## Project Structure
      80 

● Agora as tarefas (T001, T014, T020, T024, T025, T030 e T038).
● Update(specs\005-deploy-cloud-run\tasks.md)
  ⎿  Added 4 lines, removed 3 lines
      35 
      36  **Purpose**: estrutura dos arquivos novos
      37 
      38 -- [ ] T001 Criar `deploy/gcp/cleanup-policy.json` com a polític
         -a de limpeza da research R9: uma regra `KEEP` com              
      39 -  `mostRecentVersions.keepCount: 10` e uma regra `DELETE` para 
         -`tagState: UNTAGGED` com `olderThan: 7d`, no                   
      40 -  formato aceito por `gcloud artifacts repositories set-cleanup
         --policies --policy=<arquivo>`. Confirmar que o                 
      38 +- [ ] T001 Criar `deploy/gcp/cleanup-policy.json` com a polític
         +a de limpeza da research R9 (FR-021): uma regra                
      39 +  `DELETE` com `tagState: ANY` e `olderThan: 30d`, e uma regra 
         +`KEEP` com `mostRecentVersions.keepCount: 10`,                 
      40 +  que prevalece sobre a de apagar, no formato aceito por       
      41 +  `gcloud artifacts repositories set-cleanup-policies --policy=
         +<arquivo>`. Confirmar que o                                    
      42    `.dockerignore` (lista de permissão) já deixa `deploy/` fora
          do contexto da imagem, sem mudá-lo.
      43 
      44  ---

● Update(specs\005-deploy-cloud-run\tasks.md)
  ⎿  Added 4 lines, removed 2 lines
      140    qualquer job, reprovar qualquer passo `run` com `skopeo` for
           a do job `deploy`. A regra percorre os jobs
      141    (`.jobs | to_entries[]`) para saber o job de cada passo.
      142  - [ ] T014 [US2] Em `.github/scripts/check-workflows.sh`, regr
           a 9 (só quando existe o job `deploy`):
      143 -  - o `if` é exatamente `github.event_name == 'push' && github
          -.ref == 'refs/heads/main' && github.repository == 'andersonfel
          -ipediefenbach/outsera'`;                                      
      143 +  - o `if`, normalizado (sem `${{ }}`, sem espaços nas pontas 
          +e com espaços repetidos reduzidos a um), é                    
      144 +    `github.event_name == 'push' && github.ref == 'refs/heads/
          +main' && github.repository == 'andersonfelipediefenbach/outser
          +a'`;                                                          
      145    - os `needs` contêm `workflow-policy`, `build`, `postgres`,
           `codeql`, `dependencies`, `dockerfile-lint`,
      145 -    `image`, `alert-rules`, `load` e `compose`;               
      146 +    `image`, `alert-rules`, `load` e `compose`, com um comentá
          +rio dizendo que a lista acompanha os checks                   
      147 +    obrigatórios da `main` e muda junto com eles (FR-008);    
      148    - a `concurrency` é `group: deploy-cloud-run` com `cancel-in
           -progress: false`;
      149    - não há `docker/build-push-action` em `uses`, nem `docker b
           uild` ou `docker buildx build` em `run`.
      150  - [ ] T015 [US2] Em `.github/scripts/check-workflows.sh`, regr
           a 10: reprovar se o `concurrency.cancel-in-progress`

● Update(specs\005-deploy-cloud-run\tasks.md)
  ⎿  Added 4 lines, removed 1 line
      193 
      194  - [ ] T020 [P] [US3] Criar `.github/scripts/verify-deployment.
           sh` (executável, `set -euo pipefail`), conforme
      195    [`contracts/verify-deployment.md`](contracts/verify-deployme
           nt.md):
      196 -  - uso `verify-deployment.sh <URL base> [espera máxima, padrã
          -o 180]`;                                                      
      196 +  - uso `verify-deployment.sh [--expect-no-metrics] <URL base>
          + [espera máxima, padrão 180]`;                                
      197 +  - o tempo de cada chamada (`curl -w '%{time_total}'`) e o te
          +mpo da primeira chamada até a readiness na saída              
      198 +    e no resumo (SC-001);                                     
      199 +  - passo 4, só com `--expect-no-metrics`: `/actuator/promethe
          +us` com 404 (FR-004a);                                        
      200    - código 2 se faltar `curl` ou `jq`;
      201    - passo 1: readiness 200 com `{"status":"UP"}`, a cada 3 s,
           com `--max-time 10` por chamada, até a espera
      202      máxima;

● Update(specs\005-deploy-cloud-run\tasks.md)
  ⎿  Added 2 lines, removed 1 line
      247      `--cpu-boost --port=8080 --no-traffic --tag="c-${GITHUB_SH
           A::7}"` e os probes da research R5;
      248    - startup em `/actuator/health/readiness`, com período de 2
           s e limite de 60 falhas; liveness em
      249      `/actuator/health/liveness`, com período de 30 s;
      250 -  - nenhuma variável de ambiente nova;                        
      250 +  - uma única variável de ambiente nova, `--set-env-vars=MANAG
          +EMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=health`                  
      251 +    (FR-004a), que fecha as métricas e mantém os probes;      
      252    - ler o nome da revisão e a URL da tag com `gcloud run servi
           ces describe --format`.
      253  - [ ] T025 [US3] No job `deploy`, passos 8 a 11:
      254    - `verify-deployment.sh "<URL da tag>"`;

● Update(specs\005-deploy-cloud-run\tasks.md)
  ⎿  Added 2 lines, removed 2 lines
      251      (FR-004a), que fecha as métricas e mantém os probes;
      252    - ler o nome da revisão e a URL da tag com `gcloud run servi
           ces describe --format`.
      253  - [ ] T025 [US3] No job `deploy`, passos 8 a 11:
      254 -  - `verify-deployment.sh "<URL da tag>"`;                    
      254 +  - `verify-deployment.sh --expect-no-metrics "<URL da tag>"`;
      255    - `gcloud run services update-traffic "$GCP_SERVICE" --to-re
           visions=<revisão>=100`, com a remoção das tags
      256      anteriores (`--remove-tags`);
      257 -  - `verify-deployment.sh "$SERVICE_URL"`;                    
      257 +  - `verify-deployment.sh --expect-no-metrics "$SERVICE_URL"`;
      258    - o resumo com `if: always()`: URL, digest, revisão, resulta
           do das duas conferências e duração (FR-016);
      259    - nenhum `secrets.` e nenhum build.
      260  - [ ] T026 [US2] Conferência local rápida da política com o jo
           b `deploy`:

● Update(specs\005-deploy-cloud-run\tasks.md)
  ⎿  Added 4 lines, removed 2 lines
      286    - o job `deploy` na seção "Pipeline".
      287  - [ ] T030 [P] [US5] Criar `docs/adr/0027-publicacao-no-cloud-
           run.md` (contexto, decisão, alternativas e
      288    consequências: Cloud Run com 0 a 1 instância, WIF restrito p
           elo ID, a mesma imagem por digest sem rebuild,
      289 -  sem tráfego antes da conferência, o serviço criado pelo scri
          -pt e a convivência de duas revisões na troca) e               
      290 -  incluí-lo no índice de `docs/adr/README.md`.                
      289 +  sem tráfego antes da conferência, o serviço criado pelo scri
          +pt, a convivência de duas revisões na troca e as              
      290 +  métricas fechadas na URL pública com `MANAGEMENT_ENDPOINTS_W
          +EB_EXPOSURE_INCLUDE=health`, com o motivo:                    
      291 +  autenticação desligada e nenhum Prometheus coletando no Clou
          +d Run) e incluí-lo no índice de                               
      292 +  `docs/adr/README.md`.                                       
      293  - [ ] T031 [P] [US5] Em `docs/runbook.md`, seção "Publicação n
           o Cloud Run":
      294    - falha antes da promoção: nada a fazer na URL, e como ler o
            resumo;
      295    - falha depois da promoção: `gcloud run services update-traf
           fic awards --region=southamerica-east1 --to-revisions=<anterio
           r>=100`

● Update(specs\005-deploy-cloud-run\tasks.md)
  ⎿  Added 6 lines, removed 3 lines
      330  - [ ] T038 Conferir a execução da `main`:
      331    - o job `deploy` verde, com o resumo mostrando o digest, a r
           evisão, as duas conferências e a duração abaixo
      332      de 10 min;
      333 -  - `verify-deployment.sh <URL pública>` rodado localmente (rá
          -pido), saindo com código 0;                                   
      334 -  - `gcloud run services describe awards --region=southamerica
          --east1` mostrando a imagem pelo digest do resumo              
      335 -    e `maxScale` igual a 1;                                   
      333 +  - `verify-deployment.sh --expect-no-metrics <URL pública>` r
          +odado localmente (rápido), saindo com código 0,               
      334 +    com `/actuator/prometheus` em 404 na URL pública (FR-004a)
          +;                                                             
      335 +  - SC-001: no resumo, a primeira chamada da conferência pela 
          +URL da revisão (partida a frio) até o resultado de            
      336 +    referência em até 30 s, e a chamada de intervalos na URL p
          +ública em até 2 s;                                            
      337 +  - `gcloud run services describe awards --region=southamerica
          +-east1` mostrando a imagem pelo digest do resumo,             
      338 +    `maxScale` igual a 1 e `minScale` igual a 0 ou ausente, qu
          +e significa 0 (SC-006);                                       
      339    - se o job falhar, parar, diagnosticar e propor a correção n
           um commit novo (research R4, risco de permissão
      340      de leitura).
      341  - [ ] T039 Em `specs/005-deploy-cloud-run/spec.md`, passar o `
           Status` a `Implemented`, com o link da execução

● Update(specs\005-deploy-cloud-run\quickstart.md)
  ⎿  Added 5 lines, removed 3 lines
      76  5. `gcloud run services describe awards --region=southamerica-e
          ast1 --format='value(spec.template.spec.containers[0].image)'`
      77     mostra a imagem pelo digest do resumo.
      78 
      79 -## 6. Recusa fora da `main`                                    
      79 +## 6. Restrição à `main`                                       
      80 
      81  - No PR, o job `deploy` aparece como pulado.
      82 -- A condição do provedor (passo 1) só aceita `refs/heads/main` 
         -do repositório pelo ID. Um token de outra branch               
      83 -  é recusado pelo STS do GCP. O registro dessa prova fica nas t
         -arefas.                                                        
      82 +- A condição do provedor, conferida no passo 1 (T010), só aceit
         +a `refs/heads/main` do repositório e do dono                   
      83 +  pelos IDs. Não há tentativa real com outra branch: a política
         + do workflow impede qualquer outro job de pedir                
      84 +  o token.                                                     
      85 +- Na URL pública, `/actuator/prometheus` responde 404 (FR-004a)
         +.                                                              
      86 
      87  ## 7. Recomendado (Anderson, nas configurações do repositório)
      88 

● Agora o commit da spec, sem o docs/ai/prompts.md, que é gravado pelo hook.

  Ran 2 shell commands (ctrl+o to expand)

● Skill(speckit-implement)
  ⎿  Loaded skill

● A checklist do requirements.md está completo, com 16 itens marcados e sendo o
  único que existe até agora. Antes de escrever o script, vou verificar o estado
  atual do projeto no gcloud, só com comandos de leitura. · summary

  Ran 2 shell commands (ctrl+o to expand)

● O projeto está ACTIVE, com faturamento ligado e o número 2564509384, mas
  nenhuma das APIs necessárias está ativada. Agora vou escrever a política de
  limpeza e o script. · summary

● Write(deploy\gcp\cleanup-policy.json)
  ⎿  Wrote 12 lines to deploy\gcp\cleanup-policy.json
       1 [
       2   {
       3     "name": "apaga-com-mais-de-30-dias",
       4     "action": { "type": "Delete" },
       5     "condition": { "tagState": "any", "olderThan": "30d" }
       6   },
       7   {
       8     "name": "mantem-as-10-mais-recentes",
       9     "action": { "type": "Keep" },
      10     "mostRecentVersions": { "keepCount": 10 }
     … +2 lines (ctrl+o to expand)

● Write(deploy\gcp\setup.sh)
  ⎿  Wrote 340 lines to deploy\gcp\setup.sh
       1 #!/usr/bin/env bash
       2 # Configuração do GCP para a publicação no Cloud Run (spec 005, 
         FR-017 a FR-021; research R3, R4 e R9;
       3 # contracts/setup-script.md).
       4 #
       5 # Uso: deploy/gcp/setup.sh
       6 #
       7 # Idempotente por convergência: cada passo lê o estado atual e s
         ó age se ele diverge do desejado. A segunda
       8 # execução seguida termina com todos os passos em "OK (já existi
     … +333 lines (~16 KB) (ctrl+o to expand)

● Ajustes antes de rodar: o script para no primeiro FALHA, porque os passos
  dependem uns dos outros, e tirei o --condition=None, que nem todo
  add-iam-policy-binding aceita.

● Bash(f=deploy/gcp/setup.sh && sed -i 's/ --condition=None \\$/ \\/' $f && grep
      -n 'condition=None' $f; python3 - 2>/dev/null; perl -0pi -e 's/(    echo
      "==> passo \$…)
  ⎿  sed: -e expression #1, char 26: unterminated `s' command
     run_step() { # passo, descrição, função
       STEP_LABELS=(); FAIL_REASON=""
     … +32 lines (ctrl+o to expand)
  ⎿  Updated deploy\gcp\setup.sh +2 -4
      92      echo "==> passo $1: FALHA - $2: ${FAIL_REASON:-erro do gclo
          ud (ver acima)}"
      93    fi
      94    SUMMARY+=("passo $1: $result - $2")
      95 +  # Os passos dependem uns dos outros: o primeiro FALHA encerra
         +.                                                              
      96 +  if [[ "$result" == "FALHA" ]]; then finish; fi               
      97  }
      98  finish() {
      99    echo
     ...
      326  }
      327 
      328  run_step 0 "ferramentas, conta, projeto, faturamento e IDs do 
           repositório" step0
      327 -if (( FAILED > 0 )); then                                     
      328 -  echo "Nada foi alterado: o passo 0 falhou." >&2             
      329 -  finish                                                      
      330 -fi                                                            
      329  run_step 1 "APIs ativadas" step1
      330  run_step 2 "repositório de imagens $REPOSITORY e política de l
           impeza" step2
      331  run_step 3 "conta de execução $RUNTIME_SA" step3

  Searched for 1 pattern, read 1 file (ctrl+o to expand)

● Update(deploy\gcp\setup.sh)
  ⎿  Added 1 line, removed 1 line
      114    if grep -qxF "$role"$'\t'"$member" <<<"$current"; then
      115      return 0
      116    fi
      117 -  gc_write "${addcmd[@]}" --member="$member" --role="$role" --
          -condition=None \                                              
      117 +  gc_write "${addcmd[@]}" --member="$member" --role="$role" \ 
      118      || fail "não foi possível conceder $role a $member em $wha
           t" || return 1
      119    changed criado "$role para $member em $what"
      120  }

● T001 a T007 estão escritos. Agora a T008, a primeira execução do script.

● Bash(source ~/.bashrc && mkdir -p specs/005-deploy-cloud-run/evidencias &&
      deploy/gcp/setup.sh 2>&1 | tee
      specs/005-deploy-cloud-run/evidencias/setup-execucao-1.txt;…)
  ⎿  --- passo 0: ferramentas, conta, projeto, faturamento e IDs do reposit
     ório
         conta ativa: ok
     … +99 lines (ctrl+o to expand)
  ⎿  Created specs\005-deploy-cloud-run\evidencias\setup-execucao-1.txt
     +101
        1 +--- passo 0: ferramentas, conta, projeto, faturamento e IDs do
          + repositório                                                  
        2 +    conta ativa: ok                                           
        3 +    projeto outsera-awards ACTIVE, número 2564509384          
        4 +    faturamento ligado                                        
        5 +    andersonfelipediefenbach/outsera: repository_id 1462943252
          +, repository_owner_id 31636415                                
        6 +==> passo 0: OK (já existia) - ferramentas, conta, projeto, fa
          +turamento e IDs do repositório                                
        7 +--- passo 1: APIs ativadas                                    
        8 +Operation "operations/acf.p2-2564509384-46b28cfc-4c77-431a-a8c
          +a-8af4b4a75be6" finished successfully.                        
        9 +    criado: API run.googleapis.com ativada                    
       10 +    criado: API artifactregistry.googleapis.com ativada       
       11 +Operation "operations/acat.p2-2564509384-3919926f-490a-4884-93
          +55-acd936811b83" finished successfully.                       
       12 +    criado: API iam.googleapis.com ativada                    
       13 +    criado: API iamcredentials.googleapis.com ativada         
       14 +Operation "operations/acat.p2-2564509384-8186b22a-45e0-492f-a2
          +a3-e8d0e71d4b43" finished successfully.                       
       15 +    criado: API sts.googleapis.com ativada                    
       16 +==> passo 1: OK (criado) - APIs ativadas                      
       17 +--- passo 2: repositório de imagens awards e política de limpe
          +za                                                            
       18 +Create request issued for: [awards]                           
       19 +Waiting for operation [projects/outsera-awards/locations/south
          +america-east1/operations/569ab1b8-9c5c-40e0-939b-01c63ee3b39d]
          + to complete...                                               
       20 +..............................................................
          +..............................................................
          +..........................done.                               
       21 +Created repository [awards].                                  
       22 +    criado: repositório awards em southamerica-east1          
       23 +Listing items under project outsera-awards, location southamer
          +ica-east1, repository awards.                                 
       24 +                                                              
       25 +Dry run is disabled.                                          
       26 +Encryption: Google-managed key                                
       27 +Repository Size: 0.000MB                                      
       28 +Updated repository [awards].                                  
       29 +Dry run is disabled.                                          
       30 +Listing items under project outsera-awards, location southamer
          +ica-east1, repository awards.                                 
       31 +                                                              
       32 +Dry run is disabled.                                          
       33 +    ajustado: política de limpeza (30 dias; mantém as 10 mais 
          +recentes)                                                     
       34 +==> passo 2: OK (criado, ajustado) - repositório de imagens aw
          +ards e política de limpeza                                    
       35 +--- passo 3: conta de execução awards-runtime                 
       36 +Created service account [awards-runtime].                     
       37 +Service account email: awards-runtime@outsera-awards.iam.gserv
          +iceaccount.com                                                
       38 +    criado: conta de serviço awards-runtime@outsera-awards.iam
          +.gserviceaccount.com                                          
       39 +==> passo 3: OK (criado) - conta de execução awards-runtime   
       40 +--- passo 4: conta de publicação github-deployer, sem chaves  
     … +61 lines (ctrl+o to expand)
  ⎿  (timeout 10m)

● A primeira execução passou, e a URL determinística
  https://awards-2564509384.southamerica-east1.run.app é a que deve ir para o
  README e o workflow. Vou fazer o passo 9 imprimir as duas URLs e conferir se a
  determinística está listada no serviço. · summary

● Update(deploy\gcp\setup.sh)
  ⎿  Added 8 lines, removed 2 lines
      311 
      312  SERVICE_URL=""
      313  step9() {
      314 -  SERVICE_URL="$(gc run services describe "$SERVICE" --region=
          -"$REGION" --format='value(status.url)')" \                    
      315 -    || fail "não foi possível ler a URL do serviço" || return 
          -1                                                             
      314 +  # A URL determinística (serviço-número.região.run.app) é a q
          +ue vai para o README e para o workflow; o                     
      315 +  # status.url do gcloud devolve a outra forma, com hash. As d
          +uas estão nas URLs do serviço.                                
      316 +  local urls hashed                                           
      317 +  SERVICE_URL="https://$SERVICE-$PROJECT_NUMBER.$REGION.run.ap
          +p"                                                            
      318 +  urls="$(gc run services describe "$SERVICE" --region="$REGIO
          +N" --format='value(metadata.annotations."run.googleapis.com/ur
          +ls")')" \                                                     
      319 +    || fail "não foi possível ler as URLs do serviço" || retur
          +n 1                                                           
      320 +  grep -qF "\"$SERVICE_URL\"" <<<"$urls" || fail "a URL $SERVI
          +CE_URL não está entre as do serviço: $urls" || return 1       
      321 +  hashed="$(gc run services describe "$SERVICE" --region="$REG
          +ION" --format='value(status.url)')"                           
      322    echo
      323    echo "GCP_PROJECT_ID=$PROJECT_ID"
      324    echo "GCP_PROJECT_NUMBER=$PROJECT_NUMBER"

● Update(deploy\gcp\setup.sh)  ⎿  Added 1 line
      329    echo "GCP_REGISTRY=$REGION-docker.pkg.dev/$PROJECT_ID/$REPOS
           ITORY"
      330    echo "GCP_SERVICE=$SERVICE"
      331    echo "URL=$SERVICE_URL"
      332 +  echo "URL_ALTERNATIVA=$hashed"                              
      333  }
      334 
      335  run_step 0 "ferramentas, conta, projeto, faturamento e IDs do 
           repositório" step0

  Ran 1 shell command (ctrl+o to expand)

● Este é o formato que o passo 9 espera. Agora a T009, a segunda execução, que
  não pode mudar nada.

● Bash(source ~/.bashrc && deploy/gcp/setup.sh 2>&1 | tee
      specs/005-deploy-cloud-run/evidencias/setup-execucao-2.txt; echo
      "codigo=${PIPESTATUS[0]}")
  ⎿  --- passo 0: ferramentas, conta, projeto, faturamento e IDs do reposit
     ório
         conta ativa: ok
     … +53 lines (ctrl+o to expand)
  ⎿  Created specs\005-deploy-cloud-run\evidencias\setup-execucao-2.txt +55
       1 +--- passo 0: ferramentas, conta, projeto, faturamento e IDs do 
         +repositório                                                    
       2 +    conta ativa: ok                                            
       3 +    projeto outsera-awards ACTIVE, número 2564509384           
       4 +    faturamento ligado                                         
       5 +    andersonfelipediefenbach/outsera: repository_id 1462943252,
         + repository_owner_id 31636415                                  
       6 +==> passo 0: OK (já existia) - ferramentas, conta, projeto, fat
         +uramento e IDs do repositório                                  
       7 +--- passo 1: APIs ativadas                                     
       8 +==> passo 1: OK (já existia) - APIs ativadas                   
       9 +--- passo 2: repositório de imagens awards e política de limpez
         +a                                                              
      10 +Listing items under project outsera-awards, location southameri
         +ca-east1, repository awards.                                   
      11 +                                                               
      12 +Dry run is disabled.                                           
      13 +Encryption: Google-managed key                                 
      14 +Repository Size: 0.000MB                                       
      15 +==> passo 2: OK (já existia) - repositório de imagens awards e 
         +política de limpeza                                            
      16 +--- passo 3: conta de execução awards-runtime                  
      17 +==> passo 3: OK (já existia) - conta de execução awards-runtime
      18 +--- passo 4: conta de publicação github-deployer, sem chaves   
      19 +    github-deployer@outsera-awards.iam.gserviceaccount.com sem 
         +chaves gerenciadas pelo usuário                                
      20 +==> passo 4: OK (já existia) - conta de publicação github-deplo
         +yer, sem chaves                                                
      21 +--- passo 5: pool de identidade github                         
      22 +==> passo 5: OK (já existia) - pool de identidade github       
      23 +--- passo 6: provedor OIDC github-actions (repositório e main) 
      24 +    condição: assertion.repository_id == '1462943252' && assert
         +ion.repository_owner_id == '31636415' && assertion.ref == 'refs
         +/heads/main'                                                   
      25 +==> passo 6: OK (já existia) - provedor OIDC github-actions (re
         +positório e main)                                              
      26 +--- passo 7: serviço awards no Cloud Run                       
      27 +==> passo 7: OK (já existia) - serviço awards no Cloud Run     
      28 +--- passo 8: vínculos de IAM com permissões mínimas            
      29 +==> passo 8: OK (já existia) - vínculos de IAM com permissões m
         +ínimas                                                         
      30 +--- passo 9: identificadores públicos                          
      31 +                                                               
      32 +GCP_PROJECT_ID=outsera-awards                                  
      33 +GCP_PROJECT_NUMBER=2564509384                                  
      34 +GCP_REGION=southamerica-east1                                  
      35 +GCP_WIF_PROVIDER=projects/2564509384/locations/global/workloadI
         +dentityPools/github/providers/github-actions                   
      36 +GCP_DEPLOY_SA=github-deployer@outsera-awards.iam.gserviceaccoun
         +t.com                                                          
      37 +GCP_RUNTIME_SA=awards-runtime@outsera-awards.iam.gserviceaccoun
         +t.com                                                          
      38 +GCP_REGISTRY=southamerica-east1-docker.pkg.dev/outsera-awards/a
         +wards                                                          
      39 +GCP_SERVICE=awards                                             
      40 +URL=https://awards-2564509384.southamerica-east1.run.app       
     … +15 lines (ctrl+o to expand)
  ⎿  (timeout 10m)

● A segunda execução terminou com código 0 e zero mudanças, o que atende o
  SC-004. Agora a T010, as conferências rápidas.

● Bash(source ~/.bashrc &&
      out=specs/005-deploy-cloud-run/evidencias/setup-conferencias.txt && {
      echo "# Conferências da etapa 1 (T010), $(date -u +%Y-%m-%dT%H:%MZ)"…)
  ⎿  # Conferências da etapa 1 (T010), 2026-11-26T18:38Z
     # A execução 1 rodou antes de o passo 9 passar a imprimir a URL determ
     inística; a execução 2 já tem a versão final do script.
     … +18 lines (ctrl+o to expand)
  ⎿  Created specs\005-deploy-cloud-run\evidencias\setup-conferencias.txt
     +17
       1 +# Conferências da etapa 1 (T010), 2026-11-26T18:38Z            
       2 +# A execução 1 rodou antes de o passo 9 passar a imprimir a URL
         + determinística; a execução 2 já tem a versão final do script. 
       3 +                                                               
       4 +## Chaves gerenciadas pelo usuário da github-deployer (esperado
         +: nenhuma linha)                                               
       5 +(fim da lista)                                                 
       6 +                                                               
       7 +## Condição do provedor github-actions                         
       8 +assertion.repository_id == '1462943252' && assertion.repository
         +_owner_id == '31636415' && assertion.ref == 'refs/heads/main'  
       9 +                                                               
      10 +## Acesso anônimo à URL pública (imagem de exemplo)            
      11 +https://awards-2564509384.southamerica-east1.run.app -> HTTP 20
         +0                                                              
      12 +                                                               
      13 +## Papéis da conta de execução no projeto (esperado: nenhum)   
      14 +(fim da lista)                                                 
      15 +                                                               
      16 +## Papéis da github-deployer no projeto (esperado: nenhum; os p
         +apéis ficam no serviço, no repositório e na conta de execução) 
      17 +(fim da lista)                                                 
  ⎿  (timeout 3m)

● Todas as conferências batem. Confiro que nenhuma evidência tem e-mail ou
  token, marco as tarefas e rodo os dois comandos Maven (T011).

● Bash(grep -rniE 'gmail|@[a-z0-9.-]+\.(com|br)\b|ya29\.|token'
      specs/005-deploy-cloud-run/evidencias/ | grep -v 'iam.gserviceaccount.com'
      | grep -viE 'token.actions.g…)
  ⎿  fim da busca
     10
  ⎿  Updated specs\005-deploy-cloud-run\tasks.md +10 -10
      35 
      36  **Purpose**: estrutura dos arquivos novos
      37 
      38 -- [ ] T001 Criar `deploy/gcp/cleanup-policy.json` com a polític
         -a de limpeza da research R9 (FR-021): uma regra                
      38 +- [X] T001 Criar `deploy/gcp/cleanup-policy.json` com a polític
         +a de limpeza da research R9 (FR-021): uma regra                
      39    `DELETE` com `tagState: ANY` e `olderThan: 30d`, e uma regra
          `KEEP` com `mostRecentVersions.keepCount: 10`,
      40    que prevalece sobre a de apagar, no formato aceito por
      41    `gcloud artifacts repositories set-cleanup-policies --policy=
          <arquivo>`. Confirmar que o
     ...
      51  **Independent Test**: duas execuções seguidas de `deploy/gcp/se
          tup.sh`. A segunda termina com código 0, todos os
      52  passos em `OK (já existia)` e zero mudanças (SC-004).
      53 
      54 -- [ ] T002 [US4] Criar `deploy/gcp/setup.sh` (executável, `#!/u
         -sr/bin/env bash`, `set -euo pipefail`), conforme               
      54 +- [X] T002 [US4] Criar `deploy/gcp/setup.sh` (executável, `#!/u
         +sr/bin/env bash`, `set -euo pipefail`), conforme               
      55    [`contracts/setup-script.md`](contracts/setup-script.md):
      56    - constantes no topo: `PROJECT_ID=outsera-awards`, `REGION=so
          uthamerica-east1`,
      57      `GITHUB_REPO=andersonfelipediefenbach/outsera`, `REPOSITORY
          =awards`, `SERVICE=awards`, `POOL=github`,
     ...
      67      sai o `PROJECT_NUMBER`; `gcloud billing projects describe`
          com `billingEnabled=True`; `repository_id` e
      68      `owner.id` lidos de `https://api.github.com/repos/$GITHUB_R
          EPO` com `curl` e extraídos sem `jq` (por
      69      exemplo, com `sed`), validados como números. Qualquer falha
           termina com código 1 antes do passo 1.
      70 -- [ ] T003 [US4] Em `deploy/gcp/setup.sh`, passos 1 a 4 (resear
         -ch R9):                                                        
      70 +- [X] T003 [US4] Em `deploy/gcp/setup.sh`, passos 1 a 4 (resear
         +ch R9):                                                        
      71    - passo 1: ativar `run`, `artifactregistry`, `iam`, `iamcrede
          ntials` e `sts` (`.googleapis.com`) só se
      72      faltarem na lista de `gcloud services list --enabled`;
      73    - passo 2: criar o repositório Docker `awards` em `southameri
          ca-east1`, se não existir, e aplicar
     ...
      76    - passo 4: criar a conta `github-deployer` (nome de exibição
          "Publicação pelo GitHub Actions"), se não
      77      existir, e dar `FALHA` se `gcloud iam service-accounts keys
           list --managed-by=user` listar alguma chave
      78      (SC-003).
      79 -- [ ] T004 [US4] Em `deploy/gcp/setup.sh`, passos 5 e 6 (resear
         -ch R3):                                                        
      79 +- [X] T004 [US4] Em `deploy/gcp/setup.sh`, passos 5 e 6 (resear
         +ch R3):                                                        
      80    - pool `github` (global): criar se não existir e restaurar (`
          undelete`) se estiver `DELETED`;
      81    - provedor OIDC `github-actions`: emissor `https://token.acti
          ons.githubusercontent.com`, mapeamento
      82      `google.subject=assertion.sub,attribute.repository=assertio
          n.repository,attribute.repository_id=assertion.repository_id,at
          tribute.repository_owner_id=assertion.repository_owner_id,attri
          bute.ref=assertion.ref`
     ...
      84      `assertion.repository_id == '<id>' && assertion.repository_
          owner_id == '<id do dono>' && assertion.ref == 'refs/heads/main
          '`;
      85    - criar se não existir, restaurar se `DELETED` e atualizar (`
          OK (ajustado)`) se o mapeamento ou a condição
      86      divergirem.
      87 -- [ ] T005 [US4] Em `deploy/gcp/setup.sh`, passo 7 (research R4
         - e R5): se o serviço `awards` não existir, criar               
      87 +- [X] T005 [US4] Em `deploy/gcp/setup.sh`, passo 7 (research R4
         + e R5): se o serviço `awards` não existir, criar               
      88    com
      89    `gcloud run deploy awards --image=$PLACEHOLDER_IMAGE --region
          =$REGION --service-account=awards-runtime@… --min-instances=0 -
          -max-instances=1 --cpu=1 --memory=1Gi --no-allow-unauthenticate
          d`.
      90    Se existir, não implantar nada: só conferir que a conta de ex
          ecução é a `awards-runtime` e dar `FALHA` se não
     … +30 lines (ctrl+o to expand)

  Ran 1 shell command (ctrl+o to expand)

● ./mvnw verify saiu com código 0, e as linhas ERROR são dos testes de falha de
  inicialização, como esperado. Vou contar os testes nos relatórios e rodar o
  perfil extras em seguida. · summary

  Ran 1 shell command (ctrl+o to expand)

● Os dois comandos passaram. O extras tem 10 testes pulados. Isso não vem deste
  marco, porque nenhum código ou IT mudou, mas vou ver quais são antes do
  commit.

  Ran 1 shell command (ctrl+o to expand)

● Os 10 pulados são do JsonLogFormatIT, que roda de fato na execução json-logs
  (170 testes). É o desenho do Marco 4, não um problema novo. Agora o commit da
  etapa 1.

● Bash(sed -i -E 's/^- \[ \] (T011) /- [X] \1 /'
      specs/005-deploy-cloud-run/tasks.md && git add deploy/gcp
      specs/005-deploy-cloud-run/evidencias specs/005-deploy-cloud…)
  ⎿  warning: in the working copy of 'specs/005-deploy-cloud-run/evidencias
     /setup-execucao-1.txt', CRLF will be replaced by LF the next time Git
     touches it
     … +7 lines (ctrl+o to expand)
  ⎿  Updated specs\005-deploy-cloud-run\tasks.md +1 -1
      117    - `gcloud iam service-accounts keys list --managed-by=user`
           da `github-deployer`, com a lista vazia;
      118    - a `attributeCondition` do provedor;
      119    - `curl -s -o /dev/null -w '%{http_code}' <URL>`, com 200 da
            página de exemplo e acesso anônimo.
      120 -- [ ] T011 [US4] Rodar `./mvnw verify` e `./mvnw -Pextras veri
          -fy`, informar o resultado real e fazer o commit               
      120 +- [X] T011 [US4] Rodar `./mvnw verify` e `./mvnw -Pextras veri
          +fy`, informar o resultado real e fazer o commit               
      121    "Marco 5, etapa 1: configuração idempotente do GCP" com `dep
           loy/gcp/` e `specs/005-deploy-cloud-run/evidencias/`.
      122 
      123  **Checkpoint**: o GCP está configurado, e a URL do serviço e o
           s identificadores públicos são conhecidos.

● A etapa 1 (T001 a T011) está pronta e commitada em dd434c9, com o commit da
  spec antes dela (7d4b27a). As duas execuções do setup.sh terminaram com código
  0: a primeira fez 17 mudanças e a segunda nenhuma.

  Ajustes da análise: apliquei os dez achados na spec, no plano, na research,
  nos contratos, no quickstart e nas tarefas, e os commitei junto com a spec. No
  S1 ficou como você pediu: a publicação define
  MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=health, que entrou na spec como
  FR-004a, e a T038 confere que /actuator/prometheus responde 404. O registro no
  ADR 0027 está previsto na T030, da etapa 4.

  O que já existe no GCP:
  - as 5 APIs ativadas;
  - o repositório de imagens awards, com a retenção de 30 dias que mantém as 10
    imagens mais recentes;
  - as contas github-deployer (publicação) e awards-runtime (execução);
  - o pool github e o provedor github-actions, com a condição
    assertion.repository_id == '1462943252' && assertion.repository_owner_id == 
    '31636415' && assertion.ref == 'refs/heads/main';
  - o serviço awards, com 0 a 1 instância e servindo a imagem de exemplo;
  - os 5 vínculos de IAM, todos no serviço, no repositório ou na conta de
    execução, e nenhum no projeto.

  Conferências da T010: a conta de publicação não tem chaves, nenhuma das duas
  contas tem papel no projeto e a URL responde 200 para acesso anônimo.

  URL pública: https://awards-2564509384.southamerica-east1.run.app. O
  status.url do gcloud devolve outra forma, com hash. Por isso o script passou a
  imprimir as duas e a conferir que a determinística está entre as URLs do
  serviço. A primeira execução gravada ainda é de antes dessa mudança, e
  registrei isso no arquivo de conferências.

  Evidências: ficam em specs/005-deploy-cloud-run/evidencias/, com as duas
  execuções e as conferências. Procurei e-mail e token nelas e não encontrei
  nada. O andersonfelipediefenbach que aparece é o dono do repositório, que já é
  público.

  Testes: ./mvnw verify passou com 209 testes, sem falhas. ./mvnw -Pextras 
  verify também passou:

  ┌───────────┬────────┬────────┬─────────┐
  │ Execução  │ Testes │ Falhas │ Pulados │
  ├───────────┼────────┼────────┼─────────┤
  │ padrão    │ 209    │ 0      │ 0       │
  ├───────────┼────────┼────────┼─────────┤
  │ extras    │ 122    │ 0      │ 10      │
  ├───────────┼────────┼────────┼─────────┤
  │ json-logs │ 170    │ 0      │ 0       │
  └───────────┴────────┴────────┴─────────┘

  Os 10 pulados são do JsonLogFormatIT, que roda de fato na execução json-logs.
  Isso já era assim no Marco 4.

  O docs/ai/prompts.md, gravado pelo hook, continua fora dos commits.

  Parei no fim da etapa 1, como combinado. A próxima é a etapa 2, de T012 a
  T019: a política do workflow e a imagem testada como artefato OCI.

✻ Brewed for 17m 59s · done 15:47

❯ Siga direto pelas etapas 2, 3 e 4, sem parar entre elas. Pare só na T035      
  (push e PR) ou se algo falhar. Localmente, rode só o ./mvnw verify padrão em  
  cada etapa; o -Pextras e o resto ficam para o pipeline.                       

  Read 1 file (ctrl+o to expand)

● Antes de escrever a etapa 3, vou conferir os flags do gcloud 594.0.1. É só ler
  o --help para escrever os comandos certos, não é um teste local.

  Ran 2 shell commands (ctrl+o to expand)

● Todos os flags estão presentes, e o service.yaml alternativo não é necessário.
  Começo a etapa 2 pela política do workflow (T012 a T015). · summary

● Update(.github\scripts\check-workflows.sh)
  ⎿  Added 4 lines, removed 2 lines
       7  # Verifica, em <raiz>/.github (padrão: a raiz do repositório):
       8  #   1. todo `uses:` está fixado por hash de commit de 40 caract
          eres;
       9  #   2. o `permissions` do topo de cada workflow é exatamente `c
          ontents: read`;
      10 -#   3. nenhum job pede escrita, exceto `security-events: write`
         - no job `codeql`;                                              
      10 +#   3. nenhum job pede escrita, exceto `security-events: write`
         + no job `codeql` e `id-token: write` no job                    
      11 +#      `deploy` (spec 005, FR-012);                            
      12  #   4. não há referência a `secrets.`;
      12 -#   5. não há publicação de imagem (`push: true` no build-push-
         -action ou `docker push`);                                      
      13 +#   5. não há publicação de imagem (`push: true` no build-push-
         +action ou `docker push`), e o `skopeo`, que                    
      14 +#      envia a imagem testada ao registro, só aparece no job `d
         +eploy` (spec 005, FR-012);                                     
      15  #   6. o dependabot.yml tem version 2 e os ecossistemas maven, 
          github-actions, docker e docker-compose,
      16  #      com directory "/" e schedule.interval (docker-compose: s
          pec 003, FR-034);
      17  #   7. toda imagem do compose.yaml e dos service containers dos
           workflows está fixada por digest

● Update(.github\scripts\check-workflows.sh)
  ⎿  Added 6 lines, removed 1 line
      17  #   7. toda imagem do compose.yaml e dos service containers dos
           workflows está fixada por digest
      18  #      (@sha256: com 64 caracteres hexadecimais; spec 003, FR-0
          34; research R17);
      19  #   8. nenhum service container de workflow declara POSTGRES_PA
          SSWORD: o job postgres usa trust, e a
      20 -#      credencial de demonstração só pode estar no compose (con
         -stituição 1.6.0; research R16).                                
      20 +#      credencial de demonstração só pode estar no compose (con
         +stituição 1.6.0; research R16);                                
      21 +#   9. se existe o job `deploy`: o `if` (normalizado) é o do co
         +ntrato, os `needs` têm os checks                               
      22 +#      obrigatórios da main, a concorrência é o grupo `deploy-c
         +loud-run` sem cancelamento e não há build                      
      23 +#      de imagem no job (spec 005, FR-008, FR-011 e FR-012; con
         +tracts/workflow-policy.md);                                    
      24 +#  10. o `concurrency.cancel-in-progress` do topo não é `true` 
         +fixo, para não cancelar uma publicação na                      
      25 +#      main (spec 005, FR-011).                                
      26  #
      27  # É a verificação oficial do job `workflow-policy`. Precisa do 
          yq (mikefarah/yq v4), que já vem nos
      28  # runners ubuntu-latest. Códigos de saída: 0 ok, 1 violação, 2 
          ferramenta ausente.

● Update(.github\scripts\check-workflows.sh)
  ⎿  Added 11 lines
      50 
      51  PINNED='^[^@[:space:]]+@[0-9a-f]{40}$'
      52  DIGEST='^[^@[:space:]]+@sha256:[0-9a-f]{64}$'
      53 +                                                               
      54 +# Regra 9 (spec 005; contracts/ci-deploy.md). A lista acompanha
         + os checks obrigatórios da main (Marco 4,                      
      55 +# T064): um check obrigatório novo entra aqui e nos `needs` do 
         +job deploy, no mesmo commit (FR-008).                          
      56 +DEPLOY_IF="github.event_name == 'push' && github.ref == 'refs/h
         +eads/main' && github.repository == 'andersonfelipediefenbach/ou
         +tsera'"                                                        
      57 +REQUIRED_CHECKS=(workflow-policy build postgres codeql dependen
         +cies dockerfile-lint image alert-rules load compose)           
      58 +                                                               
      59 +# Normaliza uma expressão de `if`: sem ${{ }}, sem espaços nas 
         +pontas e com espaços repetidos reduzidos.                      
      60 +normalize_if() {                                               
      61 +  printf '%s' "$1" | tr '\n' ' ' \                             
      62 +    | sed -E 's/^[[:space:]]*\$\{\{[[:space:]]*//; s/[[:space:]
         +]*\}\}[[:space:]]*$//; s/[[:space:]]+/ /g; s/^ //; s/ $//'     
      63 +}                                                              
      64  for wf in "${WORKFLOWS[@]}"; do
      65    name="${wf#"$ROOT"/}"
      66 

● Update(.github\scripts\check-workflows.sh)
  ⎿  Added 1 line, removed 1 line
      74    top="$(yqr -o=json -I=0 '.permissions // null' "$wf")"
      75    [[ "$top" == '{"contents":"read"}' ]] || violation "$name" "p
          ermissions do topo deveria ser exatamente 'contents: read' (enc
          ontrado: $top)"
      76 
      77 -  # 3. Escrita só em security-events, só no job codeql.        
      77 +  # 3. Escrita só em security-events, no job codeql, e em id-to
         +ken, no job deploy.                                            
      78    while IFS= read -r job; do
      79      [[ -z "$job" ]] && continue
      80      kind="$(yqr -r ".jobs.\"$job\".permissions | tag" "$wf")"

● Update(.github\scripts\check-workflows.sh)
  ⎿  Added 4 lines, removed 1 line
      87        if [[ "$job" == "codeql" && "$key" == "security-events" &
          & "$value" == "write" ]]; then
      88          continue
      89        fi
      90 -      violation "$name" "job $job pede '$key: $value' (só o job
         - codeql pode ter security-events: write)"                      
      90 +      if [[ "$job" == "deploy" && "$key" == "id-token" && "$val
         +ue" == "write" ]]; then                                        
      91 +        continue                                               
      92 +      fi                                                       
      93 +      violation "$name" "job $job pede '$key: $value' (só o job
         + codeql pode ter security-events: write, e só o job deploy, id-
         +token: write)"                                                 
      94      done < <(yqr -r ".jobs.\"$job\".permissions | to_entries[] 
          | .key + \"=\" + .value" "$wf")
      95    done < <(yqr -r '.jobs | to_entries[] | select(.value.permiss
          ions != null) | .key' "$wf")
      96 

● Update(.github\scripts\check-workflows.sh)
  ⎿  Added 30 lines
      106    if yqr -r '.. | select(tag == "!!map" and has("run")) | .run
           ' "$wf" | grep -q 'docker push'; then
      107      violation "$name" "passo run com docker push"
      108    fi
      109 +  while IFS= read -r job; do                                  
      110 +    [[ -z "$job" ]] && continue                               
      111 +    violation "$name" "job $job usa skopeo (o envio de imagem 
          +ao registro só pode estar no job deploy)"                     
      112 +  done < <(yqr -r '.jobs | to_entries[] | select(.key != "depl
          +oy") | select([.value.steps[]?.run // "" | test("(^|[^[:alnum:
          +]_-])skopeo([^[:alnum:]_-]|$)")] | any) | .key' "$wf")        
      113 
      114 +  # 9. Job deploy: condição, dependências, concorrência e nenh
          +um build.                                                     
      115 +  if [[ "$(yqr -r '.jobs | has("deploy")' "$wf")" == "true" ]]
          +; then                                                        
      116 +    deploy_if="$(normalize_if "$(yqr -r '.jobs.deploy.if // ""
          +' "$wf")")"                                                   
      117 +    [[ "$deploy_if" == "$DEPLOY_IF" ]] \                      
      118 +      || violation "$name" "job deploy com if diferente do con
          +trato (encontrado: '$deploy_if'; esperado: '$DEPLOY_IF')"     
      119 +    needs="$(yqr -r '[.jobs.deploy.needs] | flatten | .[] // "
          +"' "$wf")"                                                    
      120 +    for check in "${REQUIRED_CHECKS[@]}"; do                  
      121 +      grep -qxF "$check" <<<"$needs" || violation "$name" "job
          + deploy sem o check obrigatório '$check' em needs"            
      122 +    done                                                      
      123 +    group="$(yqr -r '.jobs.deploy.concurrency.group // ""' "$w
          +f")"                                                          
      124 +    cancel="$(yqr -o=json '.jobs.deploy.concurrency."cancel-in
          +-progress"' "$wf")"                                           
      125 +    [[ "$group" == "deploy-cloud-run" && "$cancel" == "false" 
          +]] \                                                          
      126 +      || violation "$name" "job deploy deveria ter concurrency
          +.group 'deploy-cloud-run' e cancel-in-progress: false (encontr
          +ado: '$group', $cancel)"                                      
      127 +    if [[ -n "$(yqr -r '.jobs.deploy.steps[]?.uses // "" | sel
          +ect(test("^docker/build-push-action@"))' "$wf")" ]]; then     
      128 +      violation "$name" "job deploy com docker/build-push-acti
          +on (a imagem publicada é a do job image, sem rebuild)"        
      129 +    fi                                                        
      130 +    if yqr -r '.jobs.deploy.steps[]?.run // ""' "$wf" | grep -
          +qE 'docker[[:space:]]+(buildx[[:space:]]+)?build'; then       
      131 +      violation "$name" "job deploy com docker build (a imagem
          + publicada é a do job image, sem rebuild)"                    
      132 +    fi                                                        
      133 +  fi                                                          
      134 +                                                              
      135 +  # 10. A concorrência do topo não pode cancelar sempre (cance
          +laria uma publicação na main).                                
      136 +  if [[ "$(yqr -o=json '.concurrency."cancel-in-progress"' "$w
          +f")" == "true" ]]; then                                       
      137 +    violation "$name" "concurrency.cancel-in-progress do topo 
          +é true fixo (use uma expressão que não cancele na main)"      
      138 +  fi                                                          
      139 +                                                              
      140    # 7 e 8. Service containers: imagem por digest e sem POSTGRE
           S_PASSWORD.
      141    while IFS= read -r image; do
      142      [[ -z "$image" ]] && continue

● Agora o ci.yml: a concorrência do topo (T016) e o job image (T017).

  Read 1 file (ctrl+o to expand)
● Update(.github\workflows\ci.yml)
  ⎿  Added 6 lines, removed 3 lines
      1  # Pipeline de CI (spec 002, US6 e FR-028 a FR-029; research R9 a
          R14; contracts/ci-pipeline.md).
      2 -# Toda action é fixada por hash de commit (FR-028b). Nenhum segr
        -edo é usado, e nenhuma imagem é                                 
      3 -# publicada. A política é verificada pelo job workflow-policy (.
        -github/scripts/check-workflows.sh).                             
      2 +# Toda action é fixada por hash de commit (FR-028b). Nenhum segr
        +edo é usado. A única publicação é a do job                      
      3 +# deploy, só em push na main, com a imagem testada no job image 
        +e identidade federada sem chaves (spec 005;                     
      4 +# contracts/ci-deploy.md). A política é verificada pelo job work
        +flow-policy (.github/scripts/check-workflows.sh).               
      5  name: CI
      6 
      7  on:
     ...
      11  permissions:
      12    contents: read
      13 
      14 +# Fora da main, uma execução nova cancela a anterior do mesmo r
         +ef. Na main, nada é cancelado, para não                        
      15 +# interromper uma publicação no meio (spec 005, FR-011; researc
         +h R7).                                                         
      16  concurrency:
      17    group: ci-${{ github.workflow }}-${{ github.ref }}
      15 -  cancel-in-progress: true                                     
      18 +  cancel-in-progress: ${{ github.ref != 'refs/heads/main' }}   
      19 
      20  jobs:
      21    workflow-policy:

● Update(.github\workflows\ci.yml)
  ⎿  Added 44 lines, removed 4 lines
      232      needs: dockerfile-lint
      233      runs-on: ubuntu-latest
      234      timeout-minutes: 30
      235 +    outputs:                                                  
      236 +      # Digest do manifesto da imagem testada, que o job deplo
          +y publica sem rebuild (spec 005, FR-002).                     
      237 +      digest: ${{ steps.digest.outputs.digest }}              
      238      steps:
      239        - uses: actions/checkout@3d3c42e5aac5ba805825da76410c181
           273ba90b1 # v7.0.1
      240        - name: Contexto de build contra a lista de permissão do
            .dockerignore
      241          run: .github/scripts/check-docker-context.sh
      242        - uses: docker/setup-buildx-action@f87e5991a6d7451dcb8d9
           637bfbc97413f497069 # v4.4.1
      240 -      - name: Build da imagem (sem publicação)                
      243 +      # Um único build com duas saídas: a awards:ci no Docker 
          +(teste de fumaça e Trivy) e o mesmo resultado em              
      244 +      # layout OCI, que segue como artefato para o job deploy.
          + Sem atestados, o tar tem um único manifesto                  
      245 +      # linux/amd64 (spec 005, research R1). Nada é publicado 
          +aqui.                                                         
      246 +      - name: Build da imagem (sem publicação; Docker e tar OC
          +I do mesmo build)                                             
      247          uses: docker/build-push-action@c3c9e263c25d99ce0380d00
           2d59b67737d91b0dc # v7.4.0
      248          with:
      249            context: .
      244 -          push: false                                         
      245 -          load: true                                          
      246 -          tags: awards:ci                                     
      250 +          provenance: false                                   
      251 +          sbom: false                                         
      252 +          outputs: |                                          
      253 +            type=docker,name=awards:ci                        
      254 +            type=oci,dest=${{ runner.temp }}/awards-oci.tar   
      255            cache-from: type=gha
      256            cache-to: type=gha,mode=max
      257 +      - name: Digest do manifesto e identidade da imagem testa
          +da                                                            
      258 +        id: digest                                            
      259 +        env:                                                  
      260 +          OCI_TAR: ${{ runner.temp }}/awards-oci.tar          
      261 +        run: |                                                
      262 +          set -euo pipefail                                   
      263 +          index="$(tar -xOf "$OCI_TAR" index.json)"           
      264 +          count="$(jq '.manifests | length' <<<"$index")"     
      265 +          media="$(jq -r '.manifests[0].mediaType' <<<"$index"
          +)"                                                            
      266 +          if [[ "$count" != "1" || "$media" != "application/vn
          +d.oci.image.manifest.v1+json" ]]; then                        
      267 +            echo "::error::o tar OCI deveria ter um único mani
          +festo de imagem (encontrado: $count, $media)"                 
      268 +            exit 1                                            
      269 +          fi                                                  
      270 +          digest="$(jq -r '.manifests[0].digest' <<<"$index")"
      271 +          manifest="$(tar -xOf "$OCI_TAR" "blobs/sha256/${dige
          +st#sha256:}")"                                                
      272 +          config="$(jq -r '.config.digest' <<<"$manifest")"   
      273 +          # Armazenamento clássico do Docker: o Id é o digest 
          +da configuração; com o do containerd, o do manifesto.         
      274 +          id="$(docker image inspect awards:ci --format '{{.Id
          +}}')"                                                         
      275 +          if [[ "$id" != "$config" && "$id" != "$digest" ]]; t
          +hen                                                           
      276 +            echo "::error::a awards:ci ($id) não é a imagem do
          + tar OCI (configuração $config, manifesto $digest)"           
      277 +            exit 1                                            
      278 +          fi                                                  
      279 +          echo "digest=$digest" >> "$GITHUB_OUTPUT"           
      280 +          { echo "### Imagem testada"; echo; echo "- manifesto
          +: \`$digest\`"; echo "- configuração: \`$config\`"; echo "- aw
          +ards:ci: \`$id\`"; } >> "$GITHUB_STEP_SUMMARY"                
      281        - name: Teste de fumaça (13 passos; menos de 2 minutos a
           té o resultado de referência)
      282          run: .github/scripts/smoke-test.sh awards:ci
      283 +      - name: Imagem testada como artefato para o job deploy (
          +só em push na main)                                           
      284 +        if: github.event_name == 'push' && github.ref == 'refs
          +/heads/main'                                                  
      285 +        uses: actions/upload-artifact@cf430e030ddbb5b0abf93d22
          +962f4752f3646cd9 # v7.0.2                                     
      286 +        with:                                                 
      287 +          name: awards-oci                                    
      288 +          path: ${{ runner.temp }}/awards-oci.tar             
      289 +          retention-days: 1                                   
      290 +          if-no-files-found: error                            
      291        - name: Relatório da imagem (todas as severidades, sem r
           eprovar)
      292          if: always()
      293          uses: aquasecurity/trivy-action@ed142fd0673e97e23eac54
           620cfb913e5ce36c25 # v0.36.0

● Vou extrair o tar para um diretório em vez de por nome de arquivo, e desligar
  a compressão do artefato, já que as camadas já estão comprimidas. · summary

● Update(.github\workflows\ci.yml)
  ⎿  Added 11 lines, removed 6 lines
      260            OCI_TAR: ${{ runner.temp }}/awards-oci.tar
      261          run: |
      262            set -euo pipefail
      263 -          index="$(tar -xOf "$OCI_TAR" index.json)"           
      264 -          count="$(jq '.manifests | length' <<<"$index")"     
      265 -          media="$(jq -r '.manifests[0].mediaType' <<<"$index"
          -)"                                                            
      263 +          layout="$RUNNER_TEMP/awards-oci"                    
      264 +          mkdir -p "$layout" && tar -xf "$OCI_TAR" -C "$layout
          +"                                                             
      265 +          count="$(jq '.manifests | length' "$layout/index.jso
          +n")"                                                          
      266 +          media="$(jq -r '.manifests[0].mediaType' "$layout/in
          +dex.json")"                                                   
      267            if [[ "$count" != "1" || "$media" != "application/vn
           d.oci.image.manifest.v1+json" ]]; then
      268              echo "::error::o tar OCI deveria ter um único mani
           festo de imagem (encontrado: $count, $media)"
      269              exit 1
      270            fi
      270 -          digest="$(jq -r '.manifests[0].digest' <<<"$index")"
      271 -          manifest="$(tar -xOf "$OCI_TAR" "blobs/sha256/${dige
          -st#sha256:}")"                                                
      272 -          config="$(jq -r '.config.digest' <<<"$manifest")"   
      271 +          digest="$(jq -r '.manifests[0].digest' "$layout/inde
          +x.json")"                                                     
      272 +          config="$(jq -r '.config.digest' "$layout/blobs/sha2
          +56/${digest#sha256:}")"                                       
      273 +          platform="$(jq -r '.architecture + "/" + .os' "$layo
          +ut/blobs/sha256/${config#sha256:}")"                          
      274 +          if [[ "$platform" != "amd64/linux" ]]; then         
      275 +            echo "::error::a imagem deveria ser linux/amd64 (e
          +ncontrado: $platform)"                                        
      276 +            exit 1                                            
      277 +          fi                                                  
      278            # Armazenamento clássico do Docker: o Id é o digest 
           da configuração; com o do containerd, o do manifesto.
      279            id="$(docker image inspect awards:ci --format '{{.Id
           }}')"
      280            if [[ "$id" != "$config" && "$id" != "$digest" ]]; t
           hen

● Update(.github\workflows\ci.yml)
  ⎿  Added 2 lines
      292            name: awards-oci
      293            path: ${{ runner.temp }}/awards-oci.tar
      294            retention-days: 1
      295 +          # As camadas já vêm comprimidas no tar OCI.         
      296 +          compression-level: 0                                
      297            if-no-files-found: error
      298        - name: Relatório da imagem (todas as severidades, sem r
           eprovar)
      299          if: always()

● Antes do ./mvnw verify da etapa 2, vou checar a sintaxe bash do script de
  política e validar o ci.yml como YAML, só de forma estática. · summary

  Ran 5 shell commands (ctrl+o to expand)

● O YAML é válido e a ordem dos passos está certa (os acentos só saem trocados
  no console). Agora o ./mvnw verify da etapa 2.

  Read 1 file, ran 1 shell command (ctrl+o to expand)

● Update(specs\005-deploy-cloud-run\tasks.md)
  ⎿  Added 11 lines, removed 1 line
      167    - passo `actions/upload-artifact@cf430e030ddbb5b0abf93d22962
           f4752f3646cd9 # v7.0.2` com
      168      `name: awards-oci`, o tar, `retention-days: 1` e `if-no-fi
           les-found: error`, condicionado a
      169      `github.event_name == 'push' && github.ref == 'refs/heads/
           main'`.
      170 -- [ ] T018 [US2] Conferência local rápida da política:        
      170 +  - *Feito com três ajustes:*                                 
      171 +    - o passo do digest roda logo depois do build, antes do te
          +ste de fumaça, porque o resultado é o mesmo e a               
      172 +      falha aparece mais cedo;                                
      173 +    - o tar é extraído num diretório, em vez de ler cada arqui
          +vo com `tar -xOf`, para não depender do                       
      174 +      prefixo `./`;                                           
      175 +    - o passo também confere que a plataforma é `linux/amd64`,
          + e o artefato vai com `compression-level: 0`.                 
      176 +- [ ] T018 [US2] **Não executada localmente, por decisão do re
          +sponsável (2026-11-26):** localmente, cada                    
      177 +  etapa roda só o `./mvnw verify` padrão, e o resto fica para 
          +o pipeline. O workflow real é conferido pelo job              
      178 +  `workflow-policy` no PR. Os casos negativos não têm execução
          + no pipeline e ficam sem prova registrada; a                  
      179 +  revisão do PR confere as regras 3, 5, 9 e 10 no script. Feit
          +o localmente: `bash -n` do script e a leitura                 
      180 +  do `ci.yml` como YAML válido. Texto original da tarefa:     
      181    - baixar o `yq` v4 (binário do Windows, versão fixada, SHA-2
           56 conferido com o `checksums` do release) para o
      182      scratchpad e pô-lo no PATH só nesse comando;
      183    - rodar `.github/scripts/check-workflows.sh`, que precisa pa
           ssar;

● Bash(sed -i -E 's/^- \[ \] (T01[2-7]|T019) /- [X] \1 /'
      specs/005-deploy-cloud-run/tasks.md && grep -E '^- \[.\] T01'
      specs/005-deploy-cloud-run/tasks.md | cut -c1-1…)
  ⎿  - [X] T010 [
     - [X] T011 [
     - [X] T012 [
     … +9 lines (ctrl+o to expand)
  ⎿  Updated specs\005-deploy-cloud-run\tasks.md +7 -7
      133  [`contracts/workflow-policy.md`](contracts/workflow-policy.md)
           . Os casos 3 a 5 dependem do job `deploy` e ficam
      134  na etapa 3. No PR, o `workflow-policy` e o `image` ficam verde
           s (comprovação do pipeline, depois do push).
      135 
      136 -- [ ] T012 [P] [US2] Em `.github/scripts/check-workflows.sh`, 
          -regra 3: aceitar `id-token: write` só no job                  
      136 +- [X] T012 [P] [US2] Em `.github/scripts/check-workflows.sh`, 
          +regra 3: aceitar `id-token: write` só no job                  
      137    `deploy`, mantendo `security-events: write` só no `codeql`.
           Atualizar o cabeçalho de comentários com a lista
      138    de regras.
      139 -- [ ] T013 [US2] Em `.github/scripts/check-workflows.sh`, regr
          -a 5: além de `push: true` e `docker push` em                  
      139 +- [X] T013 [US2] Em `.github/scripts/check-workflows.sh`, regr
          +a 5: além de `push: true` e `docker push` em                  
      140    qualquer job, reprovar qualquer passo `run` com `skopeo` for
           a do job `deploy`. A regra percorre os jobs
      141    (`.jobs | to_entries[]`) para saber o job de cada passo.
      142 -- [ ] T014 [US2] Em `.github/scripts/check-workflows.sh`, regr
          -a 9 (só quando existe o job `deploy`):                        
      142 +- [X] T014 [US2] Em `.github/scripts/check-workflows.sh`, regr
          +a 9 (só quando existe o job `deploy`):                        
      143    - o `if`, normalizado (sem `${{ }}`, sem espaços nas pontas
           e com espaços repetidos reduzidos a um), é
      144      `github.event_name == 'push' && github.ref == 'refs/heads/
           main' && github.repository == 'andersonfelipediefenbach/outser
           a'`;
      145    - os `needs` contêm `workflow-policy`, `build`, `postgres`,
           `codeql`, `dependencies`, `dockerfile-lint`,
     ...
      147      obrigatórios da `main` e muda junto com eles (FR-008);
      148    - a `concurrency` é `group: deploy-cloud-run` com `cancel-in
           -progress: false`;
      149    - não há `docker/build-push-action` em `uses`, nem `docker b
           uild` ou `docker buildx build` em `run`.
      150 -- [ ] T015 [US2] Em `.github/scripts/check-workflows.sh`, regr
          -a 10: reprovar se o `concurrency.cancel-in-progress`          
      150 +- [X] T015 [US2] Em `.github/scripts/check-workflows.sh`, regr
          +a 10: reprovar se o `concurrency.cancel-in-progress`          
      151    do topo for o booleano `true`.
      152 -- [ ] T016 [US2] Em `.github/workflows/ci.yml`, trocar o `canc
          -el-in-progress: true` do topo por                             
      152 +- [X] T016 [US2] Em `.github/workflows/ci.yml`, trocar o `canc
          +el-in-progress: true` do topo por                             
      153    `cancel-in-progress: ${{ github.ref != 'refs/heads/main' }}`
            e atualizar o comentário do cabeçalho (o
      154    workflow passa a publicar na `main`).
      155 -- [ ] T017 [US2] Em `.github/workflows/ci.yml`, job `image` ([
          -`contracts/ci-deploy.md`](contracts/ci-deploy.md)):           
      155 +- [X] T017 [US2] Em `.github/workflows/ci.yml`, job `image` ([
          +`contracts/ci-deploy.md`](contracts/ci-deploy.md)):           
      156    - no `docker/build-push-action`, trocar `load: true` e `tags
           ` por
      157      `outputs: | type=docker,name=awards:ci` e `type=oci,dest=$
           {{ runner.temp }}/awards-oci.tar`, e acrescentar
      158      `provenance: false` e `sbom: false`;
     ...
      185      `compose.yaml` e o `dependabot.yml` para um diretório temp
           orário, aplicar o caso e rodar
      186      `check-workflows.sh <raiz temporária>`, que precisa reprov
           ar com a mensagem da regra;
      187    - gravar a saída em `specs/005-deploy-cloud-run/evidencias/p
           olitica-etapa-2.txt`.
      188 -- [ ] T019 [US2] Rodar `./mvnw verify` e `./mvnw -Pextras veri
          -fy`, informar o resultado real e fazer o commit               
      188 +- [X] T019 [US2] Rodar `./mvnw verify` e `./mvnw -Pextras veri
          +fy`, informar o resultado real e fazer o commit               
      189    "Marco 5, etapa 2: política do workflow e imagem testada com
           o artefato OCI".
      190 
      191  **Checkpoint**: a política está pronta para o job `deploy`, e
           a imagem testada sai do job `image` com o digest.
  ⎿  PostToolUse:Bash says: Tip: Run /ultrareview before you push to catch bugs 
     with a cloud-based multi-agent review — 3 free reviews left.

● A etapa 2 está commitada (ded0c9c). Agora a etapa 3: primeiro vejo como o
  teste de fumaça compara a referência, para o verify-deployment.sh (T020)
  seguir o mesmo critério.

  Ran 1 shell command (ctrl+o to expand)

● Write(.github\scripts\verify-deployment.sh)
  ⎿  Wrote 149 lines to .github\scripts\verify-deployment.sh
       1 #!/usr/bin/env bash
       2 # Conferência da URL publicada (spec 005, FR-004a, FR-014 a FR-0
         16 e SC-001; research R6;
       3 # contracts/verify-deployment.md).
       4 #
       5 # Uso: .github/scripts/verify-deployment.sh [--expect-no-metrics
         ] <URL base> [espera máxima em segundos]
       6 #
       7 # Roda no job deploy, contra a URL da revisão (antes de ela rece
         ber tráfego) e contra a URL pública (depois),
     … +142 lines (ctrl+o to expand)

● Dois ajustes no fetch: a saída do -w vinha duplicada quando o curl falhava, e
  o limite de 10 s cortaria a primeira chamada da readiness em partidas a frio,
  distorcendo a medição do SC-001. Vou fazer a readiness esperar até 60 s por
  chamada. · summary

● Update(.github\scripts\verify-deployment.sh)
  ⎿  Added 6 lines, removed 3 lines
      50  }
      51 
      52  # GET com status, tempo total e Content-Type; o corpo fica em $
          TMP/body. Imprime "status tempo tipo".
      53 +# O tempo limite por chamada é 10 s, ou CALL_TIMEOUT. Na readin
         +ess, 60 s: na partida a frio, o Cloud Run segura               
      54 +# a primeira chamada até a instância subir, e o tempo dela é o 
         +da partida (SC-001).                                           
      55  fetch() { # caminho, [opções extras do curl...]
      54 -  local path="$1"; shift                                       
      55 -  curl -sS -o "$TMP/body" --max-time 10 -w '%{http_code} %{time
         -_total} %{content_type}' "$@" "$BASE_URL$path" 2>"$TMP/err" \  
      56 -    || printf '000 0 -'                                        
      56 +  local path="$1" out; shift                                   
      57 +  out="$(curl -sS -o "$TMP/body" --max-time "${CALL_TIMEOUT:-10
         +}" -w '%{http_code} %{time_total} %{content_type}' \           
      58 +    "$@" "$BASE_URL$path" 2>"$TMP/err")" || true               
      59 +  printf '%s' "${out:-000 0 -}"                                
      60  }
      61  show_body() {
      62    echo "    corpo recebido (até 2 KB):"

● Update(.github\scripts\verify-deployment.sh)
  ⎿  Added 1 line, removed 1 line
      72    local start now out status time first_time=""
      73    start="$(date +%s)"
      74    while :; do
      75 -    out="$(fetch /actuator/health/readiness)"                  
      75 +    out="$(CALL_TIMEOUT=60 fetch /actuator/health/readiness)"  
      76      read -r status time _ <<<"$out"
      77      [[ -z "$first_time" ]] && first_time="$time"
      78      if [[ "$status" == "200" ]] && [[ "$(jq -r '.status // ""'
          "$TMP/body" 2>/dev/null)" == "UP" ]]; then

● Update(specs\005-deploy-cloud-run\contracts\verify-deployment.md)
  ⎿  Added 1 line, removed 1 line
      16 
      17  | N | Passo | Aprova quando |
      18  | --- | --- | --- |
      19 -| 1 | Readiness | `GET /actuator/health/readiness` devolve 200 
         -com `{"status":"UP"}` dentro da espera máxima; tenta a cada 3 s
         -, com tempo limite de 10 s por chamada (partida a frio) |      
      19 +| 1 | Readiness | `GET /actuator/health/readiness` devolve 200 
         +com `{"status":"UP"}` dentro da espera máxima; tenta a cada 3 s
         +, com tempo limite de 60 s por chamada, porque o Cloud Run segu
         +ra a primeira chamada durante a partida a frio, e o tempo dela 
         +é o da partida (SC-001); nos demais passos, 10 s |             
      20  | 2 | Resultado de referência | `GET /api/producers/intervals`
          devolve 200, `application/json`, e o corpo, ordenado com `jq -S
          `, é igual à `REFERENCE` do `smoke-test.sh` (chaves exatas, sem
           campos extras) |
      21  | 3 | Swagger UI e contrato | `GET /swagger-ui.html`, seguindo
          redirecionamentos, devolve 200, e `GET /openapi.yaml` devolve 2
          00 |
      22  | 4 | Métricas fechadas (só com `--expect-no-metrics`) | `GET /
          actuator/prometheus` devolve 404 (FR-004a) |

● Agora o job deploy no ci.yml (T023 a T025), no fim do arquivo, depois do
  compose.

  Read 1 file (ctrl+o to expand)

● Update(.github\workflows\ci.yml)
  ⎿  Added 137 lines
      386        - uses: actions/checkout@3d3c42e5aac5ba805825da76410c181
           273ba90b1 # v7.0.1
      387        - name: Teste do compose (passos 0 a 8, 4b e 7b)
      388          run: .github/scripts/compose-test.sh
      389 +                                                              
      390 +  deploy:                                                     
      391 +    # Publicação no Cloud Run (spec 005, FR-001 a FR-016; rese
          +arch R1 a R7 e R10; contracts/ci-deploy.md). Só               
      392 +    # em push na main deste repositório, depois dos 10 checks 
          +obrigatórios. A imagem é a do job image, pelo                 
      393 +    # digest, sem rebuild. A identidade é federada (OIDC e Wor
          +kload Identity Federation), sem chaves e sem                  
      394 +    # secrets: os valores abaixo são identificadores públicos,
          + impressos pelo deploy/gcp/setup.sh. A revisão                
      395 +    # nova sobe sem tráfego, é conferida pela URL própria e só
          + então recebe o tráfego (clarificação de                      
      396 +    # 2026-11-26). A lista de needs acompanha os checks obriga
          +tórios da main (regra 9 do check-workflows.sh).               
      397 +    name: Publicação no Cloud Run e conferência da URL        
      398 +    if: github.event_name == 'push' && github.ref == 'refs/hea
          +ds/main' && github.repository == 'andersonfelipediefenbach/out
          +sera'                                                         
      399 +    needs: [workflow-policy, build, postgres, codeql, dependen
          +cies, dockerfile-lint, image, alert-rules, load, compose]     
      400 +    runs-on: ubuntu-latest                                    
      401 +    timeout-minutes: 15                                       
      402 +    permissions:                                              
      403 +      contents: read                                          
      404 +      id-token: write                                         
      405 +    concurrency:                                              
      406 +      group: deploy-cloud-run                                 
      407 +      cancel-in-progress: false                               
      408 +    environment:                                              
      409 +      name: cloud-run                                         
      410 +      url: https://awards-2564509384.southamerica-east1.run.ap
          +p                                                             
      411 +    env:                                                      
      412 +      GCP_PROJECT_ID: outsera-awards                          
      413 +      GCP_PROJECT_NUMBER: '2564509384'                        
      414 +      GCP_REGION: southamerica-east1                          
      415 +      GCP_WIF_PROVIDER: projects/2564509384/locations/global/w
          +orkloadIdentityPools/github/providers/github-actions          
      416 +      GCP_DEPLOY_SA: github-deployer@outsera-awards.iam.gservi
          +ceaccount.com                                                 
      417 +      GCP_RUNTIME_SA: awards-runtime@outsera-awards.iam.gservi
          +ceaccount.com                                                 
      418 +      GCP_REGISTRY: southamerica-east1-docker.pkg.dev/outsera-
          +awards/awards                                                 
      419 +      GCP_SERVICE: awards                                     
      420 +      SERVICE_URL: https://awards-2564509384.southamerica-east
          +1.run.app                                                     
      421 +      IMAGE_DIGEST: ${{ needs.image.outputs.digest }}         
      422 +    steps:                                                    
      423 +      - name: Início da publicação                            
      424 +        run: echo "DEPLOY_START=$(date +%s)" >> "$GITHUB_ENV" 
      425 +      - uses: actions/checkout@3d3c42e5aac5ba805825da76410c181
          +273ba90b1 # v7.0.1                                            
      426 +      - uses: actions/download-artifact@9000827ccba6bdab643e8b
          +6fd33ac0654aef8333 # v8.0.2                                   
      427 +        with:                                                 
      428 +          name: awards-oci                                    
      429 +          path: ${{ runner.temp }}/oci                        
      430 +      - name: O artefato é a imagem testada no job image      
      431 +        run: |                                                
      432 +          set -euo pipefail                                   
      433 +          if [[ ! "$IMAGE_DIGEST" =~ ^sha256:[0-9a-f]{64}$ ]];
          + then                                                         
      434 +            echo "::error::digest do job image ausente ou invá
          +lido: '$IMAGE_DIGEST'"                                        
      435 +            exit 1                                            
      436 +          fi                                                  
      437 +          layout="$RUNNER_TEMP/oci-layout"                    
      438 +          mkdir -p "$layout" && tar -xf "$RUNNER_TEMP/oci/awar
          +ds-oci.tar" -C "$layout"                                      
      439 +          found="$(jq -r '.manifests[0].digest' "$layout/index
          +.json")"                                                      
      440 +          if [[ "$found" != "$IMAGE_DIGEST" ]]; then          
      441 +            echo "::error::o artefato tem o manifesto $found, 
          +e o job image testou $IMAGE_DIGEST"                           
      442 +            exit 1                                            
      443 +          fi                                                  
      444 +          echo "Artefato confere com a imagem testada: $IMAGE_
          +DIGEST"                                                       
      445 +      - uses: google-github-actions/auth@7c6bc770dae815cd3e89e
          +e6cdf493a5fab2cc093 # v3.0.0                                  
      446 +        with:                                                 
      447 +          workload_identity_provider: ${{ env.GCP_WIF_PROVIDER
          + }}                                                           
      448 +          service_account: ${{ env.GCP_DEPLOY_SA }}           
      449 +      - uses: google-github-actions/setup-gcloud@aa5489c8933f4
          +cc7a4f7d45035b3b1440c9c10db # v3.0.1                          
      450 +        with:                                                 
      451 +          version: '594.0.1'                                  
      452 +      - name: Envio da imagem testada ao Artifact Registry, co
          +m o digest preservado                                         
      453 +        run: |                                                
      454 +          set -euo pipefail                                   
      455 +          if ! command -v skopeo >/dev/null 2>&1; then        
      456 +            echo "::error::o skopeo não está no runner (resear
          +ch R1)"                                                       
      457 +            exit 1                                            
      458 +          fi                                                  
      459 +          # O token de acesso vai por stdin e nunca é impresso
          +.                                                             
      460 +          gcloud auth print-access-token | skopeo login -u oau
          +th2accesstoken --password-stdin "$GCP_REGION-docker.pkg.dev"  
      461 +          ref="docker://$GCP_REGISTRY/awards:$GITHUB_SHA"     
      462 +          skopeo copy --preserve-digests "oci-archive:$RUNNER_
          +TEMP/oci/awards-oci.tar" "$ref"                               
      463 +          pushed="$(skopeo inspect --format '{{.Digest}}' "$re
          +f")"                                                          
      464 +          if [[ "$pushed" != "$IMAGE_DIGEST" ]]; then         
      465 +            echo "::error::o registro tem $pushed, e a imagem 
          +testada é $IMAGE_DIGEST"                                      
      466 +            exit 1                                            
      467 +          fi                                                  
      468 +          echo "Imagem no registro: $GCP_REGISTRY/awards@$push
          +ed"                                                           
      469 +      - name: Implantação sem tráfego, com tag própria        
      470 +        id: deploy                                            
      471 +        run: |                                                
      472 +          set -euo pipefail                                   
      473 +          tag="c-${GITHUB_SHA::7}"                            
      474 +          # Só o modo padrão (FR-001): nenhum perfil, nenhum t
          +oken. A única variável nova fecha as métricas na              
      475 +          # URL pública e mantém os probes (FR-004a). 0 a 1 in
          +stância por revisão (FR-003).                                 
      476 +          gcloud run deploy "$GCP_SERVICE" --quiet --project="
          +$GCP_PROJECT_ID" --region="$GCP_REGION" \                     
      477 +            --image="$GCP_REGISTRY/awards@$IMAGE_DIGEST" \    
      478 +            --service-account="$GCP_RUNTIME_SA" \             
      479 +            --min-instances=0 --max-instances=1 --cpu=1 --memo
          +ry=1Gi --cpu-boost --port=8080 \                              
      480 +            --set-env-vars=MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_I
          +NCLUDE=health \                                               
      481 +            --startup-probe=httpGet.path=/actuator/health/read
          +iness,httpGet.port=8080,periodSeconds=2,failureThreshold=60,ti
          +meoutSeconds=1 \                                              
      482 +            --liveness-probe=httpGet.path=/actuator/health/liv
          +eness,httpGet.port=8080,periodSeconds=30,timeoutSeconds=3 \   
      483 +            --no-traffic --tag="$tag"                         
      484 +          gcloud run services describe "$GCP_SERVICE" --projec
          +t="$GCP_PROJECT_ID" --region="$GCP_REGION" --format=json > "$R
          +UNNER_TEMP/service.json"                                      
      485 +          revision="$(jq -r '.status.latestCreatedRevisionName
          +' "$RUNNER_TEMP/service.json")"                               
      486 +          tag_url="$(jq -r --arg t "$tag" '.status.traffic[]? 
          +| select(.tag == $t) | .url' "$RUNNER_TEMP/service.json")"    
      487 +          if [[ -z "$revision" || -z "$tag_url" ]]; then      
      488 +            echo "::error::revisão ($revision) ou URL da tag $
          +tag ($tag_url) não encontradas"                               
      489 +            exit 1                                            
      490 +          fi                                                  
      491 +          { echo "tag=$tag"; echo "revision=$revision"; echo "
          +tag_url=$tag_url"; } >> "$GITHUB_OUTPUT"                      
      492 +          echo "Revisão $revision, sem tráfego, em $tag_url"  
      493 +      - name: Conferência pela URL da revisão, antes do tráfeg
          +o                                                             
      494 +        run: .github/scripts/verify-deployment.sh --expect-no-
          +metrics "${{ steps.deploy.outputs.tag_url }}"                 
      495 +      - name: Tráfego para a revisão conferida                
      496 +        env:                                                  
      497 +          TAG: ${{ steps.deploy.outputs.tag }}                
      498 +          REVISION: ${{ steps.deploy.outputs.revision }}      
      499 +        run: |                                                
      500 +          set -euo pipefail                                   
      501 +          # As tags das revisões anteriores saem, para os ende
          +reços antigos não continuarem acessíveis.                     
      502 +          old_tags="$(jq -r --arg t "$TAG" '[.status.traffic[]
          +? | select(.tag != null and .tag != $t) | .tag] | unique | joi
          +n(",")' "$RUNNER_TEMP/service.json")"                         
      503 +          args=(--to-revisions="$REVISION=100")               
      504 +          if [[ -n "$old_tags" ]]; then args+=(--remove-tags="
          +$old_tags"); fi                                               
      505 +          gcloud run services update-traffic "$GCP_SERVICE" --
          +quiet --project="$GCP_PROJECT_ID" --region="$GCP_REGION" "${ar
          +gs[@]}"                                                       
      506 +      - name: Conferência pela URL pública                    
      507 +        run: .github/scripts/verify-deployment.sh --expect-no-
          +metrics "$SERVICE_URL"                                        
      508 +      - name: Resumo da publicação                            
      509 +        if: always()                                          
      510 +        env:                                                  
      511 +          REVISION: ${{ steps.deploy.outputs.revision }}      
      512 +          TAG_URL: ${{ steps.deploy.outputs.tag_url }}        
      513 +        run: |                                                
      514 +          {                                                   
      515 +            echo "### Publicação no Cloud Run"                
      516 +            echo                                              
      517 +            echo "| Item | Valor |"                           
      518 +            echo "| --- | --- |"                              
      519 +            echo "| URL pública | $SERVICE_URL |"             
      520 +            echo "| Imagem | \`$GCP_REGISTRY/awards@$IMAGE_DIG
          +EST\` |"                                                      
      521 +            echo "| Revisão | ${REVISION:-não criada} |"      
      522 +            echo "| URL da revisão | ${TAG_URL:-não criada} |"
      523 +            echo "| Resultado do job | ${{ job.status }} |"   
      524 +            echo "| Duração | $(( $(date +%s) - ${DEPLOY_START
          +:-$(date +%s)} )) s |"                                        
      525 +          } >> "$GITHUB_STEP_SUMMARY"                         

● Update(.github\workflows\ci.yml)
  ⎿  Added 3 lines, removed 1 line
      491            { echo "tag=$tag"; echo "revision=$revision"; echo "
           tag_url=$tag_url"; } >> "$GITHUB_OUTPUT"
      492            echo "Revisão $revision, sem tráfego, em $tag_url"
      493        - name: Conferência pela URL da revisão, antes do tráfeg
           o
      494 -        run: .github/scripts/verify-deployment.sh --expect-no-
          -metrics "${{ steps.deploy.outputs.tag_url }}"                 
      494 +        env:                                                  
      495 +          TAG_URL: ${{ steps.deploy.outputs.tag_url }}        
      496 +        run: .github/scripts/verify-deployment.sh --expect-no-
          +metrics "$TAG_URL"                                            
      497        - name: Tráfego para a revisão conferida
      498          env:
      499            TAG: ${{ steps.deploy.outputs.tag }}

● Bash(chmod +x .github/scripts/verify-deployment.sh && bash -n
      .github/scripts/verify-deployment.sh && echo BASH_OK && source ~/.bashrc
      && SDK="$(dirname "$(command -…)
  ⎿  BASH_OK
     YAML_OK ['workflow-policy', 'build', 'postgres', 'codeql', 'dependenci
     es', 'dockerfile-lint', 'image', 'alert-rules', 'load', 'compose', 'de
     … +28 lines (ctrl+o to expand)
  ⎿  Created specs\005-deploy-cloud-run\evidencias\gcloud-flags.txt +24
       1 +# T022: flags do gcloud 594.0.1 usados pelo job deploy (gcloud 
         +run deploy --help e update-traffic --help), 2026-11-26         
       2 +Google Cloud SDK 594.0.1                                       
       3 +          --cpu=CPU --depends-on=[CONTAINER,...] --gpu=GPU     
       4 +          --liveness-probe=[KEY=VALUE,...] --memory=MEMORY --po
         +rt=PORT                                                        
       5 +          --startup-probe=[KEY=VALUE,...]                      
       6 +     --[no-]cpu-boost                                          
       7 +     --max-instances=MAX_INSTANCES                             
       8 +     --min-instances=MIN_INSTANCES                             
       9 +     --service-account=SERVICE_ACCOUNT                         
      10 +     --tag=TAG                                                 
      11 +     --no-traffic                                              
      12 +     --cpu=CPU                                                 
      13 +     --liveness-probe=[KEY=VALUE,...]                          
      14 +     --memory=MEMORY                                           
      15 +     --port=PORT                                               
      16 +     --startup-probe=[KEY=VALUE,...]                           
      17 +       --set-env-vars=[KEY=VALUE,...]                          
      18 +          --to-revisions=myservice-s5sxn=10,myservice-cp9kw=90 
      19 +          --to-revisions=myservice-s5sxn=20                    
      20 +          --to-revisions=myservice-cp9kw=100                   
      21 +    --to-revisions by the string "LATEST". For example, to set 
         +10% of traffic                                                 
      22 +          --to-revisions=LATEST=10                             
      23 +         --remove-tags=[TAG,...]                               
      24 +       --to-revisions=[REVISION-NAME=PERCENTAGE,...]           

● A sintaxe dos scripts e o YAML estão válidos, e os campos que a regra 9
  confere batem com o contrato. Também não há nenhuma referência a secrets..
  Agora o ./mvnw verify da etapa 3.

  Searched for 1 pattern, read 1 file, ran 1 shell command (ctrl+o to expand)

● Update(specs\005-deploy-cloud-run\tasks.md)  ⎿  Added 3 lines, removed 1 line
      217    - em `FALHA`, o status e o corpo truncados em 2 KB;
      218    - com `GITHUB_STEP_SUMMARY` definido, uma tabela com a URL,
           os passos e o tempo até a readiness;
      219    - código 0 ou 1.
      220 -- [ ] T021 [US3] Conferência local rápida do script, sem Docke
          -r. O `jq` não está instalado: usar um binário                 
      220 +- [ ] T021 [US3] **Não executada localmente, por decisão do re
          +sponsável (2026-11-26):** o script é comprovado               
      221 +  pelo job `deploy` depois do merge, nas duas conferências. Fe
          +ito localmente: `bash -n`. Texto original da                  
      222 +  tarefa: conferência local rápida do script, sem Docker. O `j
          +q` não está instalado: usar um binário                        
      223    baixado para o scratchpad, com versão fixada e SHA-256 confe
           rido. Sequência:
      224    - `./mvnw -q -DskipTests package` uma vez;
      225    - subir o JAR com `PORT=18080` em segundo plano e rodar `ver
           ify-deployment.sh http://127.0.0.1:18080 120`,

● Update(specs\005-deploy-cloud-run\tasks.md)
  ⎿  Added 2 lines
      233    `--tag`. Se faltar algum flag de probe, aplicar a alternativ
           a da research R5: criar
      234    `deploy/gcp/service.yaml` e usar `gcloud run services replac
           e`, ajustando T024 e T025. Registrar o resultado
      235    em `specs/005-deploy-cloud-run/evidencias/gcloud-flags.txt`.
      236 +  - *Feito:* todos os flags existem na 594.0.1, inclusive `--s
          +et-env-vars` e, no `update-traffic`,                          
      237 +    `--to-revisions` e `--remove-tags`. O `service.yaml` alter
          +nativo não foi necessário.                                    
      238  - [ ] T023 [US2] Em `.github/workflows/ci.yml`, criar o job `d
           eploy` com o cabeçalho do contrato
      239    [`ci-deploy.md`](contracts/ci-deploy.md):
      240    - `name`, `if`, `needs` (os 10 checks), `permissions` (`cont
           ents: read` e `id-token: write`), `concurrency`

● Update(specs\005-deploy-cloud-run\tasks.md)
  ⎿  Added 9 lines, removed 1 line
      271    - `verify-deployment.sh --expect-no-metrics "$SERVICE_URL"`;
      272    - o resumo com `if: always()`: URL, digest, revisão, resulta
           do das duas conferências e duração (FR-016);
      273    - nenhum `secrets.` e nenhum build.
      274 -- [ ] T026 [US2] Conferência local rápida da política com o jo
          -b `deploy`:                                                   
      274 +  - *Feito com três ajustes:*                                 
      275 +    - as URLs passam aos scripts por `env`, e não por expressã
          +o no `run`;                                                   
      276 +    - a readiness espera até 60 s por chamada, para medir a pa
          +rtida a frio (SC-001);                                        
      277 +    - o `environment.url` usa a URL literal.                  
      278 +- [ ] T026 [US2] **Não executada localmente, por decisão do re
          +sponsável (2026-11-26):** o workflow real é                   
      279 +  conferido pelo job `workflow-policy` no PR. Os casos negativ
          +os 3 a 5 ficam sem prova registrada. Feito                    
      280 +  localmente: a leitura do `ci.yml` como YAML válido, com o `i
          +f`, os `needs`, a concorrência e as permissões do             
      281 +  job `deploy` iguais aos do contrato, e nenhuma referência a 
          +`secrets.`. Texto original da tarefa:                         
      282 +  conferência local rápida da política com o job `deploy`:    
      283    - o `check-workflows.sh` real passa;
      284    - os casos 3, 4 e 5 de [`contracts/workflow-policy.md`](cont
           racts/workflow-policy.md), numa cópia temporária,
      285      reprovam com a mensagem da regra 9;

● Update(specs\005-deploy-cloud-run\tasks.md)
  ⎿  Added 1 line, removed 1 line
      284    - os casos 3, 4 e 5 de [`contracts/workflow-policy.md`](cont
           racts/workflow-policy.md), numa cópia temporária,
      285      reprovam com a mensagem da regra 9;
      286    - a saída é anexada a `specs/005-deploy-cloud-run/evidencias
           /politica-etapa-3.txt`.
      287 -- [ ] T027 [US2] Rodar `./mvnw verify` e `./mvnw -Pextras veri
          -fy`, informar o resultado real e fazer o commit               
      287 +- [ ] T027 [US2] Rodar `./mvnw verify` (o `-Pextras` fica para
          + o pipeline, por decisão do responsável), informar o resultado
          + real e fazer o commit                                        
      288    "Marco 5, etapa 3: job deploy com conferência pela URL da re
           visão e pela URL pública".
      289 
      290  **Checkpoint**: o pipeline está pronto para publicar na `main`
           . No PR, o job `deploy` aparece como pulado.

● Bash(sed -i -E 's/^- \[ \] (T020|T022|T023|T024|T025|T027) /- [X] \1 /'
      specs/005-deploy-cloud-run/tasks.md && git add --chmod=+x
      .github/scripts/verify-deployment.s…)
  ⎿  910415d Marco 5, etapa 3: job deploy com conferência pela URL da revis
     ão e pela URL pública
  ⎿  Updated specs\005-deploy-cloud-run\tasks.md +6 -6
      201  CSV de cenário (comprovação local rápida, sem Docker). A publi
           cação real é comprovada pelo pipeline depois do
      202  merge (Phase 6).
      203 
      204 -- [ ] T020 [P] [US3] Criar `.github/scripts/verify-deployment.
          -sh` (executável, `set -euo pipefail`), conforme               
      204 +- [X] T020 [P] [US3] Criar `.github/scripts/verify-deployment.
          +sh` (executável, `set -euo pipefail`), conforme               
      205    [`contracts/verify-deployment.md`](contracts/verify-deployme
           nt.md):
      206    - uso `verify-deployment.sh [--expect-no-metrics] <URL base>
            [espera máxima, padrão 180]`;
      207    - o tempo de cada chamada (`curl -w '%{time_total}'`) e o te
           mpo da primeira chamada até a readiness na saída
     ...
      228    - rodar `verify-deployment.sh http://127.0.0.1:18080 60`, qu
           e precisa sair com código 1 e o passo 2 em
      229      `FALHA`;
      230    - parar a aplicação e gravar as duas saídas em `specs/005-de
           ploy-cloud-run/evidencias/verify-local.txt`.
      231 -- [ ] T022 [P] [US1] Conferir na versão local do `gcloud` (594
          -.0.1, a que o workflow fixa) que o                            
      231 +- [X] T022 [P] [US1] Conferir na versão local do `gcloud` (594
          +.0.1, a que o workflow fixa) que o                            
      232    `gcloud run deploy --help` tem `--startup-probe`, `--livenes
           s-probe`, `--cpu-boost`, `--no-traffic` e
      233    `--tag`. Se faltar algum flag de probe, aplicar a alternativ
           a da research R5: criar
      234    `deploy/gcp/service.yaml` e usar `gcloud run services replac
           e`, ajustando T024 e T025. Registrar o resultado
      235    em `specs/005-deploy-cloud-run/evidencias/gcloud-flags.txt`.
      236    - *Feito:* todos os flags existem na 594.0.1, inclusive `--s
           et-env-vars` e, no `update-traffic`,
      237      `--to-revisions` e `--remove-tags`. O `service.yaml` alter
           nativo não foi necessário.
      238 -- [ ] T023 [US2] Em `.github/workflows/ci.yml`, criar o job `d
          -eploy` com o cabeçalho do contrato                            
      238 +- [X] T023 [US2] Em `.github/workflows/ci.yml`, criar o job `d
          +eploy` com o cabeçalho do contrato                            
      239    [`ci-deploy.md`](contracts/ci-deploy.md):
      240    - `name`, `if`, `needs` (os 10 checks), `permissions` (`cont
           ents: read` e `id-token: write`), `concurrency`
      241      (`deploy-cloud-run`, sem cancelamento), `timeout-minutes:
           15` e `environment` (`name: cloud-run` e
     ...
      255        `gcloud auth print-access-token | skopeo login -u oauth2
           accesstoken --password-stdin southamerica-east1-docker.pkg.dev
           `,
      256        depois `skopeo copy --preserve-digests oci-archive:… doc
           ker://$GCP_REGISTRY/awards:${{ github.sha }}` e
      257        conferir o digest no registro com `skopeo inspect --form
           at '{{.Digest}}'`.
      258 -- [ ] T024 [US1] No job `deploy`, passo 7:                    
      258 +- [X] T024 [US1] No job `deploy`, passo 7:                    
      259    - `gcloud run deploy "$GCP_SERVICE" --image="$GCP_REGISTRY/a
           wards@$DIGEST" --region="$GCP_REGION"`,
      260      com `--service-account="$GCP_RUNTIME_SA" --min-instances=0
            --max-instances=1 --cpu=1 --memory=1Gi`,
      261      `--cpu-boost --port=8080 --no-traffic --tag="c-${GITHUB_SH
           A::7}"` e os probes da research R5;
     ...
      264    - uma única variável de ambiente nova, `--set-env-vars=MANAG
           EMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=health`
      265      (FR-004a), que fecha as métricas e mantém os probes;
      266    - ler o nome da revisão e a URL da tag com `gcloud run servi
           ces describe --format`.
      267 -- [ ] T025 [US3] No job `deploy`, passos 8 a 11:              
      267 +- [X] T025 [US3] No job `deploy`, passos 8 a 11:              
      268    - `verify-deployment.sh --expect-no-metrics "<URL da tag>"`;
      269    - `gcloud run services update-traffic "$GCP_SERVICE" --to-re
           visions=<revisão>=100`, com a remoção das tags
      270      anteriores (`--remove-tags`);
     … +8 lines (ctrl+o to expand)

● A etapa 3 está commitada (910415d). Agora a etapa 4, a documentação. Começo
  lendo as seções do README que vou mudar.

  Searched for 2 patterns, read 5 files (ctrl+o to expand)

● Registrei a ordem do decisoes.md (mais nova primeiro); agora escrevo o ADR
  0027 e depois o índice, runbook, decisões, checklist e README. · summary

● Write(docs\adr\0027-publicacao-no-cloud-run.md)
  ⎿  Wrote 76 lines to docs\adr\0027-publicacao-no-cloud-run.md
      1 # 0027. Publicação no Cloud Run, com uma instância e identidade f
        ederada
      2 
      3 - **Status:** Aceito
      4 - **Data:** 2026-11-26
      5 - **Origem:** [`docs/ai/decisoes.md`](../ai/decisoes.md), entrada
        s do Marco 5;
      6   [spec 005](../../specs/005-deploy-cloud-run/spec.md) e a resear
        ch R1 a R11 do
      7   [plano](../../specs/005-deploy-cloud-run/plan.md).
     … +69 lines (ctrl+o to expand)

● Update(docs\adr\README.md)
  ⎿  Added 6 lines, removed 2 lines
      31  | [0018](0018-compose-com-observabilidade.md) | Compose com Pos
          tgreSQL, Kafka nativo, Prometheus e Grafana | Aceito | 2026-11-
          24 |
      32  | [0019](0019-testes-com-postgresql-em-perfil-dedicado.md) | Te
          stes que dependem do PostgreSQL num perfil Maven dedicado | Ace
          ito | 2026-11-24 |
      33  | [0026](0026-intervalos-guardados-entre-leituras.md) | Interva
          los guardados entre as leituras, invalidados a cada gravação (u
          ma instância) | Aceito | 2026-11-26 |
      34 +| [0027](0027-publicacao-no-cloud-run.md) | Publicação no Cloud
         + Run, com uma instância por revisão e identidade federada sem c
         +haves | Aceito | 2026-11-26 |                                  
      35 
      36 +Os números 0020 a 0025 estão reservados para as decisões adiada
         +s do Marco 4 (spec 004, FR-037).                               
      37 +                                                               
      38  A comprovação destes registros é a revisão do pull request, pel
          a seção "Revisão dos ADRs" do checklist de
      39  cada spec: [`specs/002-operacao/checklists/requirements.md`](..
          /../specs/002-operacao/checklists/requirements.md)
      37 -para os ADRs 0001 a 0012 e [`specs/003-modo-docker/checklists/r
         -equirements.md`](../../specs/003-modo-docker/checklists/require
         -ments.md)                                                      
      38 -para os ADRs 0013 a 0019 e a atualização do 0004 (Princípio VI)
         -.                                                              
      40 +para os ADRs 0001 a 0012, [`specs/003-modo-docker/checklists/re
         +quirements.md`](../../specs/003-modo-docker/checklists/requirem
         +ents.md)                                                       
      41 +para os ADRs 0013 a 0019 e a atualização do 0004, e [`specs/005
         +-deploy-cloud-run/checklists/revisao.md`](../../specs/005-deplo
         +y-cloud-run/checklists/revisao.md)                             
      42 +para o ADR 0027 (Princípio VI).                                

● Update(docs\runbook.md)
  ⎿  Added 2 lines, removed 1 line
      1  # Runbook dos alertas
      2 
      3 -Uma seção por alerta do Prometheus do compose (`observability/pr
        -ometheus/rules/slo.yml`). Cada alerta                           
      3 +Uma seção por alerta do Prometheus do compose (`observability/pr
        +ometheus/rules/slo.yml`), mais a                                
      4 +[publicação no Cloud Run](#publicação-no-cloud-run) (Marco 5). C
        +ada alerta                                                      
      5  traz o link da sua seção em `annotations.runbook_url`. Os objeti
         vos e o orçamento estão em
      6  [`slo.md`](slo.md). Não há Alertmanager: os alertas aparecem em
         <http://localhost:9090/alerts> e no
      7  dashboard do Grafana.

● Update(docs\runbook.md)
  ⎿  Added 59 lines
       87  - Erro na subida: corrigir a causa no log (por exemplo, o cami
           nho em `MOVIES_CSV_PATH`) e subir de novo.
       88  - Token divergente: subir o compose com o mesmo `API_TOKEN` pa
           ra a aplicação e o Prometheus
       89    (`API_TOKEN=<token> docker compose up -d`).
       90 +                                                              
       91 +## Publicação no Cloud Run                                    
       92 +                                                              
       93 +Job `deploy` do pipeline, só em push na `main` (spec 005; [ADR
          + 0027](adr/0027-publicacao-no-cloud-run.md)).                 
       94 +O serviço é o `awards`, no projeto `outsera-awards`, região `s
          +outhamerica-east1`, com a URL pública                         
       95 +<https://awards-2564509384.southamerica-east1.run.app>. Os com
          +andos abaixo precisam do login de quem tem                    
       96 +acesso ao projeto (`gcloud auth login`). O pipeline não tem ch
          +ave: ele usa a identidade federada.                           
       97 +                                                              
       98 +```bash                                                       
       99 +gcloud run revisions list --service=awards --region=southameri
          +ca-east1 --project=outsera-awards                             
      100 +gcloud run services describe awards --region=southamerica-east
          +1 --project=outsera-awards \                                  
      101 +  --format='yaml(status.traffic)'                             
      102 +.github/scripts/verify-deployment.sh --expect-no-metrics https
          +://awards-2564509384.southamerica-east1.run.app               
      103 +```                                                           
      104 +                                                              
      105 +**Como ler a execução:** o resumo do job `deploy` traz a URL, 
          +o digest da imagem, a revisão, a URL da revisão               
      106 +e a tabela de cada conferência. Cada tabela tem o status e o t
          +empo de cada chamada.                                         
      107 +                                                              
      108 +### Falha antes da troca de tráfego                           
      109 +                                                              
      110 +Se o job parou até o passo "Conferência pela URL da revisão, a
          +ntes do tráfego", **a URL pública não mudou**.                
      111 +Ela continua servindo a revisão anterior.                     
      112 +                                                              
      113 +- **Envio da imagem ou implantação:**                         
      114 +  - erro de permissão (`PERMISSION_DENIED`): rodar `deploy/gcp
          +/setup.sh` de novo, que reaplica os vínculos                  
      115 +    que faltarem. Se faltar um papel de leitura do projeto que
          + o script não concede, acrescentar só esse                    
      116 +    papel ao script, com o motivo (spec 005, research R4);    
      117 +  - revisão que não sobe (o startup probe não chega a 200): ve
          +r o log com                                                   
      118 +    `gcloud run services logs read awards --region=southameric
          +a-east1 --project=outsera-awards --limit=200`.                
      119 +- **Conferência pela URL da revisão:** a revisão existe, mas n
          +ão recebe tráfego. O resumo mostra o passo que                
      120 +  falhou e o corpo recebido. Corrigir num PR novo. Não é preci
          +so voltar nada.                                               
      121 +                                                              
      122 +### Falha depois da troca de tráfego                          
      123 +                                                              
      124 +Se o passo "Conferência pela URL pública" falhou, a revisão no
          +va já recebe o tráfego. Para voltar à anterior:               
      125 +                                                              
      126 +1. Achar a revisão anterior, a segunda da lista, por ordem de 
          +criação:                                                      
      127 +                                                              
      128 +   ```bash                                                    
      129 +   gcloud run revisions list --service=awards --region=southam
          +erica-east1 --project=outsera-awards \                        
      130 +     --sort-by=~metadata.creationTimestamp --format='value(met
          +adata.name)' --limit=2                                        
      131 +   ```                                                        
      132 +                                                              
      133 +2. Passar o tráfego para ela:                                 
      134 +                                                              
      135 +   ```bash                                                    
      136 +   gcloud run services update-traffic awards --region=southame
          +rica-east1 --project=outsera-awards \                         
      137 +     --to-revisions=<revisão anterior>=100                    
      138 +   ```                                                        
      139 +                                                              
      140 +3. Conferir com o `verify-deployment.sh` na URL pública e regi
          +strar o incidente. A correção vem num PR novo. A              
      141 +   próxima publicação da `main` volta a promover só depois das
          + duas conferências.                                           
      142 +                                                              
      143 +### Publicar de novo                                          
      144 +                                                              
      145 +- Reexecutar o job `deploy` na execução da `main` (re-run no G
          +itHub Actions). A operação é segura de repetir:               
      146 +  a imagem é a do mesmo commit e o fluxo é o mesmo.           
      147 +- O artefato da imagem (`awards-oci`) fica guardado por 1 dia.
          + Depois disso, a reexecução falha no download, e              
      148 +  é preciso reexecutar a execução inteira da `main`, que recon
          +strói e testa a imagem.                                       

● Update(docs\ai\decisoes.md)
  ⎿  Added 43 lines
       3  Sugestões e decisões relevantes, aceitas ou rejeitadas, com o m
          otivo (item 5.2 do enunciado). As
       4  decisões de arquitetura ficam em ADRs, em `docs/adr/`.
       5 
       6 +## 2026-11-26 — Marco 5: publicação no Cloud Run               
       7 +                                                               
       8 +Decisões da [spec 005](../../specs/005-deploy-cloud-run/spec.md
         +), do plano e da análise. O resumo de                          
       9 +arquitetura está no [ADR 0027](../adr/0027-publicacao-no-cloud-
         +run.md).                                                       
      10 +                                                               
      11 +- **Publicação a cada merge na `main`, e não por tag de versão*
         +* (FR-025). O briefing previa a publicação por                 
      12 +  tag, depois da entrega. O responsável decidiu publicar a cada
         + push na `main`, depois dos 10 checks                          
      13 +  obrigatórios, para a URL mostrar sempre o que está na `main`.
         + O briefing não foi alterado: ele é o registro                 
      14 +  do planejamento.                                             
      15 +- **Revisão sem tráfego até ser conferida** (clarificação, opçã
         +o A, escolhida pelo responsável). A revisão                    
      16 +  nova é conferida pela URL própria e só depois recebe o tráfeg
         +o.                                                             
      17 +  - Recusada a promoção direta com volta automática: deixaria a
         + revisão ruim na URL pública por alguns                        
      18 +    segundos.                                                  
      19 +  - Recusado o "só reprovar o job": a URL ficaria quebrada até 
         +uma ação manual.                                               
      20 +- **O serviço é criado pelo `deploy/gcp/setup.sh`, com a imagem
         + pública de exemplo do Cloud Run.** Isso                       
      21 +  permite dar à conta de publicação `roles/run.developer` só no
         + serviço, e não no projeto, e deixa o acesso                   
      22 +  público (`allUsers` como invocador) fora do alcance do pipeli
         +ne. Até a primeira publicação, a URL serve a                   
      23 +  página de exemplo.                                           
      24 +- **Métricas fechadas na URL pública** (análise, achado S1; dec
         +isão do responsável). A publicação define                      
      25 +  `MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=health`, e `/actua
         +tor/prometheus` responde 404 (FR-004a).                        
      26 +  Recusado expor as métricas como no modo padrão local: com a a
         +utenticação desligada, elas ficariam abertas                   
      27 +  na internet, sem um Prometheus para coletá-las.              
      28 +- **`google-github-actions/deploy-cloudrun` não é usada.** Os p
         +assos `gcloud` explícitos permitem implantar                   
      29 +  sem tráfego, conferir e só então trocar o tráfego. Ficaram a 
         +`auth` (v3.0.0) e a `setup-gcloud` (v3.0.1),                   
      30 +  além da `download-artifact` (v8.0.2), todas fixadas por hash 
         +e sem `uses:` aninhado (research R2).                          
      31 +- **Identificadores públicos no `env` do job, e não em variávei
         +s do repositório** (research R10). Número do                   
      32 +  projeto, provedor de identidade e e-mails das contas não são 
         +segredos. No workflow, ficam versionados e                     
      33 +  visíveis na revisão, sem passo manual no GitHub.             
      34 +- **A política do workflow foi ampliada de forma estreita** (FR
         +-012):                                                         
      35 +  - `id-token: write` e o `skopeo` passam a ser permitidos só n
         +o job `deploy`;                                                
      36 +  - duas regras novas exigem, no job, os 10 checks obrigatórios
         +, a concorrência sem cancelamento e nenhum                     
      37 +    build;                                                     
      38 +  - a concorrência do topo não pode cancelar sempre;           
      39 +  - a proibição de `secrets.` não mudou.                       
      40 +- **Retenção do registro de 30 dias, mantendo as 10 imagens mai
         +s recentes** (análise, achado C1). A regra de                  
      41 +  apagar só as imagens sem tag não bastaria, porque toda imagem
         + recebe a tag do commit.                                       
      42 +- **Conferências locais reduzidas** (decisão do responsável, du
         +rante a implementação). Localmente, cada etapa                 
      43 +  rodou só o `./mvnw verify` padrão. O `./mvnw -Pextras verify`
         + e as conferências de política e de script                     
      44 +  (T018, T021 e T026) ficaram para o pipeline. Os casos negativ
         +os da política não têm execução registrada.                    
      45 +- **No Windows, o `gcloud` em bash precisa do `CLOUDSDK_PYTHON`
         +** apontando para o Python do próprio SDK. Sem                 
      46 +  ele, o atalho `python` da Microsoft Store responde no lugar, 
         +e o comando falha. O `setup.sh` define a                       
      47 +  variável quando ela falta (research R9).                     
      48 +                                                               
      49  ## 2026-11-26 — Proteção da main depois do Marco 4
      50 
      51  O ruleset `main` está ativo para a branch padrão, com a lista d
          e bypass vazia. Para concluir o T064

● Write(specs\005-deploy-cloud-run\checklists\revisao.md)
  ⎿  Wrote 50 lines to specs\005-deploy-cloud-run\checklists\revisao.md
       1 # Checklist de revisão: Marco 5 — Publicação no Cloud Run
       2 
       3 **Purpose**: o que o revisor confere no pull request para compro
         var os requisitos de documentação
       4 (FR-023, FR-024 e FR-025), conforme o Princípio VI.
       5 **Created**: 2026-11-26
       6 **Feature**: [spec.md](../spec.md)
       7 
       8 Os itens são marcados pelo revisor ou pelo responsável no PR, e
         não pelo implementador.
     … +42 lines (ctrl+o to expand)

● Agora o README.

● Update(README.md)
  ⎿  Added 13 lines
      57 
      58  Documentação interativa: <http://localhost:8080/swagger-ui.html
          >.
      59 
      60 +**Ou sem instalar nada: a demonstração publicada.** A mesma API
         +, no modo padrão e com o arquivo                               
      61 +fornecido, roda no Google Cloud Run. Ela é publicada pelo pipel
         +ine a cada merge na `main` (Marco 5,                           
      62 +[seção 3](#marco-5-publicação-no-cloud-run-opcional)).         
      63 +                                                               
      64 +```bash                                                        
      65 +curl -s https://awards-2564509384.southamerica-east1.run.app/ap
         +i/producers/intervals                                          
      66 +```                                                            
      67 +                                                               
      68 +- Swagger UI: <https://awards-2564509384.southamerica-east1.run
         +.app/swagger-ui.html>.                                         
      69 +- **A primeira chamada pode levar alguns segundos.** O serviço 
         +fica com zero instâncias quando ninguém o                      
      70 +  usa, e a primeira chamada espera a aplicação subir e carregar
         + o CSV. As seguintes respondem na hora.                        
      71 +- É uma demonstração: uma instância só, sem autenticação e sem 
         +garantia de disponibilidade.                                   
      72 +                                                               
      73  ---
      74 
      75  ## 3. Como executar

● Update(README.md)
  ⎿  Added 49 lines
      372  | ADRs 0020 a 0025 das decisões do Marco 4 | FR-037 (etapa 6)
           | adiado; as decisões estão em [`docs/ai/decisoes.md`](docs/ai
           /decisoes.md) |
      373  | Várias instâncias da aplicação | ADR 0026 | o cache dos inte
           rvalos precisa passar a um contador no banco ou a um evento co
           mpartilhado |
      374 
      375 +### Marco 5: publicação no Cloud Run (opcional)               
      376 +                                                              
      377 +A API está publicada em <https://awards-2564509384.southameric
          +a-east1.run.app>. Nada muda no modo padrão                    
      378 +local nem no `./mvnw verify` (spec 005, FR-022).              
      379 +                                                              
      380 +**O que é publicado.**                                        
      381 +- Só o modo padrão: H2 em memória, a carga do `movielist.csv` 
          +e a autenticação desligada. Kafka,                            
      382 +  PostgreSQL, Prometheus, Grafana e a autenticação ligada fica
          +m no compose local.                                           
      383 +- A imagem é **a mesma** que o pipeline testou (teste de fumaç
          +a e Trivy), identificada pelo digest e                        
      384 +  publicada no Artifact Registry do projeto, sem rebuild.     
      385 +- As métricas ficam fechadas na URL pública: `/actuator/promet
          +heus` responde 404. A publicação liga só o                    
      386 +  `health`, que os probes usam.                               
      387 +                                                              
      388 +**Como a publicação acontece.** O job `deploy` roda só em push
          + na `main` deste repositório, depois dos 10                   
      389 +checks obrigatórios:                                          
      390 +                                                              
      391 +1. Baixa a imagem testada do job `image` e confere o digest.  
      392 +2. Entra no GCP por **Workload Identity Federation**, sem chav
          +e nenhuma no GitHub ou no repositório. O                      
      393 +   provedor de identidade só aceita tokens deste repositório e
          + do dono, pelos IDs numéricos, e da branch                    
      394 +   `main`.                                                    
      395 +3. Envia a imagem ao registro com o digest preservado.        
      396 +4. Implanta uma revisão nova **sem tráfego** e a confere pela 
          +URL própria dela: readiness, resultado de                     
      397 +   referência, Swagger e métricas fechadas, com o [`verify-dep
          +loyment.sh`](.github/scripts/verify-deployment.sh).           
      398 +5. Só então passa o tráfego para ela e confere a URL pública d
          +e novo. Uma revisão reprovada nunca recebe                    
      399 +   tráfego.                                                   
      400 +                                                              
      401 +O resumo do job traz a URL, o digest, a revisão e o tempo de c
          +ada chamada. Se algo falhar depois da troca, o                
      402 +[runbook](docs/runbook.md#publicação-no-cloud-run) mostra como
          + voltar à revisão anterior.                                   
      403 +                                                              
      404 +**Uma instância por revisão (0 a 1).** O mínimo de zero instân
          +cias mantém o custo em zero com o serviço                     
      405 +ocioso, e por isso a primeira chamada passa pela partida a fri
          +o. O máximo de uma mantém válido o cache dos                  
      406 +intervalos do [ADR 0026](docs/adr/0026-intervalos-guardados-en
          +tre-leituras.md). Na troca de revisão, a antiga               
      407 +e a nova podem ter uma instância cada por alguns instantes. No
          + modo padrão isso não importa: cada instância                 
      408 +tem o próprio H2 e não grava nada depois da carga.            
      409 +                                                              
      410 +**Configuração do GCP.** Tudo o que existe no projeto vem do [
          +`deploy/gcp/setup.sh`](deploy/gcp/setup.sh):                  
      411 +- as APIs;                                                    
      412 +- o registro de imagens, com retenção de 30 dias que mantém as
          + 10 imagens mais recentes;                                    
      413 +- as contas de publicação e de execução;                      
      414 +- o pool e o provedor de identidade;                          
      415 +- o serviço;                                                  
      416 +- os vínculos de IAM.                                         
      417 +                                                              
      418 +O script é idempotente: a segunda execução seguida não muda na
          +da (saídas em                                                 
      419 +[`specs/005-deploy-cloud-run/evidencias/`](specs/005-deploy-cl
          +oud-run/evidencias/)). A conta de publicação                  
      420 +tem papéis só no serviço, no registro e na conta de execução, 
          +e não pode mudar quem invoca o serviço. A conta               
      421 +de execução não tem papel nenhum. A decisão completa está no  
      422 +[ADR 0027](docs/adr/0027-publicacao-no-cloud-run.md).         
      423 +                                                              
      424  ## 4. Endpoints
      425 
      426  O contrato completo (versão 1.2.0, com ETag e a autenticação o
           pcional) está em

● Update(README.md)
  ⎿  Added 4 lines, removed 3 lines
      639 
      640  ### Pipeline
      641 
      642 -O [GitHub Actions](.github/workflows/ci.yml) roda em todo push
          - e pull request, sem segredos e sem                           
      643 -publicar a imagem. Toda action é fixada por hash de commit. O 
          -contrato completo está em                                     
      644 -[`specs/002-operacao/contracts/ci-pipeline.md`](specs/002-oper
          -acao/contracts/ci-pipeline.md).                               
      642 +O [GitHub Actions](.github/workflows/ci.yml) roda em todo push
          + e pull request, sem segredos. Toda action é                  
      643 +fixada por hash de commit. A única publicação é a do job `depl
          +oy`, só em push na `main` (Marco 5). O contrato               
      644 +completo está em [`specs/002-operacao/contracts/ci-pipeline.md
          +`](specs/002-operacao/contracts/ci-pipeline.md)               
      645 +e, para a publicação, em [`specs/005-deploy-cloud-run/contract
          +s/ci-deploy.md`](specs/005-deploy-cloud-run/contracts/ci-deplo
          +y.md).                                                        
      646 
      647  | Job | O que faz | Reprova quando |
      648  | --- | --- | --- |

● Update(README.md)
  ⎿  Added 1 line, removed 1 line
      646 
      647  | Job | O que faz | Reprova quando |
      648  | --- | --- | --- |
      649 -| `workflow-policy` | Confere a política do workflow, do Depen
          -dabot e do compose, e que os testes dos Marcos 1 e 2 não mudar
          -am desde a `v0.2.0` | Action sem hash de commit, permissão alé
          -m de `contents: read` (exceto `security-events: write` no `cod
          -eql`), uso de `secrets.`, publicação de imagem, Dependabot inc
          -ompleto, imagem do compose ou de service container sem digest,
          - senha de banco no workflow, ou um IT da `v0.2.0` alterado sem
          - exceção registrada |                                         
      649 +| `workflow-policy` | Confere a política do workflow, do Depen
          +dabot e do compose, e que os testes dos Marcos 1 e 2 não mudar
          +am desde a `v0.2.0` | Action sem hash de commit, permissão alé
          +m de `contents: read` (exceto `security-events: write` no `cod
          +eql` e `id-token: write` no `deploy`), uso de `secrets.`, publ
          +icação de imagem fora do `deploy`, job `deploy` sem os 10 chec
          +ks em `needs`, com outra condição, com cancelamento ou com bui
          +ld, Dependabot incompleto, imagem do compose ou de service con
          +tainer sem digest, senha de banco no workflow, ou um IT da `v0
          +.2.0` alterado sem exceção registrada |                       
      650  | `build` | `./mvnw -Pextras verify` (enunciado, extras e log
           em JSON), cobertura no resumo e relatórios como artefato | Qua
           lquer teste falha, ou a cobertura de linhas fica abaixo de 90%
            |
      651  | `postgres` | `./mvnw -Ppostgres-it verify` contra um Postgre
           SQL 18 (service container, collation `C`, sem credencial) | Qu
           alquer `*PostgresIT` falha |
      652  | `compose` | [`compose-test.sh`](.github/scripts/compose-test
           .sh): sobe o compose e confere a referência, o Prometheus, o d
           ashboard, as regras do SLO carregadas sem alerta disparando, u
           m vencedor novo pelo Kafka em até 5 s, o tópico de erros, a pe
           rsistência depois de `down`, a queda real do banco e o token |
            Qualquer passo falha, ou a subida até o dashboard passa de 3
           min |

● Update(README.md)
  ⎿  Added 2 lines, removed 1 line
      655  | `alert-rules` | `promtool check rules`, `check config` e `te
           st rules` das regras do SLO, com a imagem do Prometheus do com
           pose | Regra inválida ou teste de regra falhando |
      656  | `load` | [`load-test.sh`](.github/scripts/load-test.sh): ger
           a o CSV de 100 mil filmes duas vezes e compara os SHA-256, med
           e a carga direta e a carga por Kafka e roda o k6 | Hashes dife
           rentes, linha rejeitada, readiness fora do prazo, ou p99 e tax
           a de erro acima dos limites |
      657  | `dockerfile-lint` | hadolint | Aviso ou erro |
      658 -| `image` | Contexto de build, imagem, teste de fumaça (13 pas
          -sos) e Trivy na imagem | Contexto fora do `.dockerignore`, ima
          -gem que não sobe ou não responde o resultado de referência, ou
          - vulnerabilidade `HIGH` ou `CRITICAL` com correção na imagem |
      658 +| `image` | Contexto de build, imagem (no Docker e em tar OCI,
          + do mesmo build), digest, teste de fumaça (13 passos) e Trivy 
          +na imagem. Em push na `main`, guarda o tar para o `deploy` | C
          +ontexto fora do `.dockerignore`, tar com mais de um manifesto 
          +ou fora de `linux/amd64`, imagem do Docker diferente da do tar
          +, imagem que não sobe ou não responde o resultado de referênci
          +a, ou vulnerabilidade `HIGH` ou `CRITICAL` com correção na ima
          +gem |                                                         
      659 +| `deploy` | Só em push na `main`, depois dos 10 checks: envia
          + a imagem testada ao Artifact Registry pelo digest, implanta s
          +em tráfego, confere pela URL da revisão, passa o tráfego e con
          +fere a URL pública ([Marco 5](#marco-5-publicação-no-cloud-run
          +-opcional)). Não é check obrigatório, porque só roda depois do
          + merge | Digest diferente do testado, falha de identidade ou d
          +e permissão, revisão que não sobe, ou conferência reprovada (r
          +eadiness, resultado de referência, Swagger ou métricas abertas
          +) |                                                           
      660 
      661  Achados médios e baixos, e vulnerabilidades sem correção, apar
           ecem no relatório sem reprovar. O
      662  [Dependabot](.github/dependabot.yml) abre PRs semanais para Ma
           ven, GitHub Actions, a imagem base e as

● Update(README.md)
  ⎿  Added 16 lines
      745  | Inventário efetivo no job de dependências | FR-032, FR-033,
           SC-010 | Job `dependencies` e o registro da causa em `docs/ai/
           decisoes.md` |
      746  | Testador adversário na ingestão por Kafka | FR-035 | `KafkaA
           dversarialEventsIT` e os ITs `KafkaCase*` (job `build`), `Kafk
           aCase29RestartReappliesCsvPostgresIT` (job `postgres`) |
      747 
      748 +### Requisitos do Marco 5 (publicação no Cloud Run)           
      749 +                                                              
      750 +Também fora do enunciado. O marco não muda o comportamento da 
          +aplicação, então não tem IT novo. Cada requisito              
      751 +é comprovado pela execução do pipeline, pelas execuções do scr
          +ipt ou pela revisão (Princípio VI). A spec está               
      752 +em [`specs/005-deploy-cloud-run/spec.md`](specs/005-deploy-clo
          +ud-run/spec.md).                                              
      753 +                                                              
      754 +| Extra | Requisitos | Como é comprovado |                    
      755 +| --- | --- | --- |                                           
      756 +| Serviço no modo padrão, com 0 a 1 instância por revisão, pro
          +bes e métricas fechadas | FR-001 a FR-007, FR-004a, SC-001, SC
          +-006 | Job `deploy` na `main` (as duas conferências do `verify
          +-deployment.sh`, com o 404 de `/actuator/prometheus` e os temp
          +os de cada chamada) e a leitura do serviço depois do merge |  
      757 +| A mesma imagem testada, pelo digest, sem rebuild | FR-002, F
          +R-010 | Jobs `image` (digest e identidade da imagem) e `deploy
          +` (digest do artefato e do registro) |                        
      758 +| Publicação só em push na `main`, depois dos checks, serializ
          +ada e sem chaves | FR-008, FR-009, FR-011 a FR-013, SC-002, SC
          +-003 | Job `workflow-policy` (regras 3, 4, 5, 9 e 10), o job `
          +deploy` pulado nos PRs e a condição do provedor de identidade 
          +conferida no GCP ([evidências](specs/005-deploy-cloud-run/evid
          +encias/setup-conferencias.txt)) |                             
      759 +| Revisão sem tráfego até ser conferida | FR-014 a FR-016, FR-
          +015a | Job `deploy`: a conferência pela URL da revisão antes d
          +a troca de tráfego e o resumo da execução |                   
      760 +| Configuração do GCP idempotente, com permissões mínimas | FR
          +-017 a FR-021, SC-004 | As duas execuções do `deploy/gcp/setup
          +.sh` (a segunda com zero mudanças) em [`evidencias/`](specs/00
          +5-deploy-cloud-run/evidencias/) e a revisão do script no PR | 
      761 +| Modo padrão sem mudança | FR-022, SC-007 | Jobs `build` e `w
          +orkflow-policy` (`check-unchanged-tests.sh`) |                
      762 +| README, ADR 0027, runbook e decisões | FR-023 a FR-025 | Rev
          +isão do PR, por [`specs/005-deploy-cloud-run/checklists/revisa
          +o.md`](specs/005-deploy-cloud-run/checklists/revisao.md) |    
      763 +                                                              
      764  ---
      765 
      766  ## 7. Regras de negócio

● Update(README.md)
  ⎿  Added 10 lines
      962  - **Testes com PostgreSQL num perfil Maven dedicado**, obrigat
           ório no pipeline (constituição 1.7.0;
      963    ADR 0019).
      964 
      965 +Do Marco 5 (ADR 0027):                                        
      966 +                                                              
      967 +- **Cloud Run com 0 a 1 instância por revisão,** só no modo pa
          +drão e com as métricas fechadas na URL                        
      968 +  pública.                                                    
      969 +- **Publicação a cada merge na `main`, pela mesma imagem testa
          +da,** identificada pelo digest e sem rebuild.                 
      970 +- **Identidade federada sem chaves,** restrita ao repositório 
          +e à `main` pelos IDs numéricos, com papéis só                 
      971 +  no serviço, no registro e na conta de execução.             
      972 +- **Revisão sem tráfego até ser conferida pela URL própria,** 
          +para que uma revisão ruim nunca chegue à URL                  
      973 +  pública.                                                    
      974 +                                                              
      975  ---
      976 
      977  ## 10. Uso de IA

● Update(README.md)  ⎿  Added 1 line
      1007  | 5 e 6 | Listagem, Swagger UI e fechamento, num único PR |
      1008  | Marco 2 | Um único bolt, com seis etapas e um commit por et
            apa: ETag, observabilidade, Dockerfile, pipeline e Dependabot
            , ADRs e README ([spec 002](specs/002-operacao/spec.md)) |
      1009  | Marco 3 | Um único bolt, com cinco etapas e um commit por e
            tapa: PostgreSQL e perfil `docker`, Kafka, autenticação, comp
            ose com observabilidade, e ADRs com o README ([spec 003](spec
            s/003-modo-docker/spec.md)) |
      1010 +| Marco 5 | Um único bolt, com quatro etapas e um commit por 
           +etapa: configuração do GCP pelo script, política do workflow 
           +e imagem como artefato, job `deploy` e documentação ([spec 00
           +5](specs/005-deploy-cloud-run/spec.md)) |                    
      1011 
      1012  **Papéis:**
      1013 

● Update(README.md)
  ⎿  Added 3 lines, removed 2 lines
      1051  ├── observability/               # Prometheus (com as regras
            do SLO em rules/) e provisionamento e dashboard do Grafana
      1052  ├── load/intervals.js            # roteiro do k6, com os limi
            tes do SLO (Marco 4)
      1053  ├── tools/csv-generator/         # gerador de CSV grande e de
            terminístico, só com o JDK (Marco 4)
      1054 +├── deploy/gcp/                  # configuração idempotente d
           +o GCP (setup.sh) e retenção do registro (Marco 5)            
      1055  ├── .github/
      1055 -│   ├── workflows/ci.yml         # pipeline de CI            
      1056 -│   ├── scripts/                 # política do workflow, cont
           -exto da imagem, fumaça, compose e ITs inalterados            
      1056 +│   ├── workflows/ci.yml         # pipeline de CI e publicaçã
           +o no Cloud Run (job deploy)                                  
      1057 +│   ├── scripts/                 # política do workflow, cont
           +exto da imagem, fumaça, compose, conferência da publicação e 
           +ITs inalterados                                              
      1058  │   ├── unchanged-tests-exceptions.txt # ITs da v0.2.0 que po
            dem mudar, cada um com a decisão que autoriza
      1059  │   └── dependabot.yml           # atualizações de Maven, Act
            ions, imagem base e imagens do compose
      1060  ├── .gitattributes               # fim de linha LF no reposit
            ório

● Update(README.md)
  ⎿  Added 1 line
      1076  ├── specs/003-modo-docker/       # Marco 3: spec, plano, rese
            arch, contratos e tarefas
      1077  │   └── contracts/               # OpenAPI 1.2.0 (fonte única
            ), eventos do Kafka, compose e pipeline
      1078  ├── specs/004-acabamento-operacional/ # Marco 4: spec, plano,
             research, contratos e tarefas
      1079 +├── specs/005-deploy-cloud-run/  # Marco 5: spec, plano, rese
           +arch, contratos, tarefas e evidências do GCP                 
      1080  ├── src/main/java/com/outsera/awards/
      1081  │   ├── AwardsApplication.java   # classe principal
      1082  │   ├── ingestion/               # leitura, validação e carga
             do CSV; kafka/: ingestão por Kafka (perfil kafka)

● Update(README.md)
  ⎿  Added 1 line, removed 1 line
      1101  │   └── SmokeIT.java
      1102  ├── src/test/resources/scenarios/  # um CSV por cenário de te
            ste
      1103  ├── docs/slo.md                  # objetivos do SLO, orçament
            o e alertas (Marco 4)
      1104 -├── docs/runbook.md              # o que fazer quando cada al
           -erta dispara (Marco 4)                                       
      1104 +├── docs/runbook.md              # o que fazer quando cada al
           +erta dispara (Marco 4) e quando a publicação falha (Marco 5) 
      1105  ├── docs/adr/                    # decisões de arquitetura (A
            DRs), com índice
      1106  └── docs/ai/                     # evidência do uso de IA (ve
            r docs/ai/README.md)
      1107  ```

  Ran 1 shell command (ctrl+o to expand)

● Os comandos do runbook existem na versão 594.0.1. Agora o ./mvnw verify da
  etapa 4 e o commit.

  Ran 1 shell command (ctrl+o to expand)

● Bash(f=specs/005-deploy-cloud-run/tasks.md && sed -i -E 's/^- \[ \]
      (T02[89]|T03[0-4]) /- [X] \1 /' $f && sed -i 's/^- \[X\] T034 \[US5\]
      Rodar `.\/mvnw verify` e `.…)
  ⎿  - [X] T028 [P] [US5] Em `README.md`, seção "2. Avalie em 2 minutos": a
      URL pública de intervalos e do Swagg
     - [X] T029 [US5] Em `README.md`:
     … +20 lines (ctrl+o to expand)
  ⎿  Updated specs\005-deploy-cloud-run\tasks.md +7 -7
      297 
      298  **Independent Test**: revisão no PR, guiada pelos itens de `ch
           ecklists/revisao.md`.
      299 
      300 -- [ ] T028 [P] [US5] Em `README.md`, seção "2. Avalie em 2 min
          -utos": a URL pública de intervalos e do Swagger UI            
      300 +- [X] T028 [P] [US5] Em `README.md`, seção "2. Avalie em 2 min
          +utos": a URL pública de intervalos e do Swagger UI            
      301    (da T009), com o aviso de que a primeira chamada pode levar
           alguns segundos (partida a frio) e um `curl` de
      302    exemplo.
      303 -- [ ] T029 [US5] Em `README.md`:                              
      303 +- [X] T029 [US5] Em `README.md`:                              
      304    - subseção "Marco 5: publicação no Cloud Run (opcional)", de
           pois da do Marco 4: o que é publicado (só o modo
      305      padrão), o fluxo sem tráfego, conferência e promoção, a fe
           deração sem chaves, o limite de uma instância e
      306      o motivo (ADR 0026), o que fica de fora e o `deploy/gcp/se
           tup.sh`;
      307    - "Requisitos do Marco 5" na seção 6, com a comprovação de c
           ada requisito (FR-026);
      308    - o job `deploy` na seção "Pipeline".
      309 -- [ ] T030 [P] [US5] Criar `docs/adr/0027-publicacao-no-cloud-
          -run.md` (contexto, decisão, alternativas e                    
      309 +- [X] T030 [P] [US5] Criar `docs/adr/0027-publicacao-no-cloud-
          +run.md` (contexto, decisão, alternativas e                    
      310    consequências: Cloud Run com 0 a 1 instância, WIF restrito p
           elo ID, a mesma imagem por digest sem rebuild,
      311    sem tráfego antes da conferência, o serviço criado pelo scri
           pt, a convivência de duas revisões na troca e as
      312    métricas fechadas na URL pública com `MANAGEMENT_ENDPOINTS_W
           EB_EXPOSURE_INCLUDE=health`, com o motivo:
      313    autenticação desligada e nenhum Prometheus coletando no Clou
           d Run) e incluí-lo no índice de
      314    `docs/adr/README.md`.
      315 -- [ ] T031 [P] [US5] Em `docs/runbook.md`, seção "Publicação n
          -o Cloud Run":                                                 
      315 +- [X] T031 [P] [US5] Em `docs/runbook.md`, seção "Publicação n
          +o Cloud Run":                                                 
      316    - falha antes da promoção: nada a fazer na URL, e como ler o
            resumo;
      317    - falha depois da promoção: `gcloud run services update-traf
           fic awards --region=southamerica-east1 --to-revisions=<anterio
           r>=100`
      318      e como achar a revisão anterior;
      319    - como reexecutar a publicação.
      320 -- [ ] T032 [P] [US5] Em `docs/ai/decisoes.md`, as entradas:   
      320 +- [X] T032 [P] [US5] Em `docs/ai/decisoes.md`, as entradas:   
      321    - publicação a cada merge na `main`, e não por tag, como pre
           via o briefing (FR-025);
      322    - opção A da clarificação (sem tráfego antes da conferência)
           ;
      323    - serviço criado pelo script, com a imagem de exemplo;
      324    - `deploy-cloudrun` não usada;
      325    - identificadores no `env` do workflow, e não em variáveis d
           o repositório;
      326    - política do workflow ampliada.
      327 -- [ ] T033 [P] [US5] Criar `specs/005-deploy-cloud-run/checkli
          -sts/revisao.md`, com os itens que o revisor                   
      327 +- [X] T033 [P] [US5] Criar `specs/005-deploy-cloud-run/checkli
          +sts/revisao.md`, com os itens que o revisor                   
      328    confere em FR-023, FR-024 e FR-025:
      329    - a URL e o aviso no README;
      330    - a seção e os requisitos do Marco 5;
     … +8 lines (ctrl+o to expand)

● As etapas 2, 3 e 4 estão prontas e commitadas, e parei na T035: o push e a
  abertura do PR são seus. Em cada etapa rodei só o ./mvnw verify, que passou
  com 209 testes e nenhuma falha. O -Pextras fica para o pipeline, como você
  pediu.

  ┌─────────┬────────────────────────────────────────────────────────────────┐
  │ Commit  │                             Etapa                              │
  ├─────────┼────────────────────────────────────────────────────────────────┤
  │ ded0c9c │ 2: política do workflow e imagem testada como artefato OCI     │
  ├─────────┼────────────────────────────────────────────────────────────────┤
  │ 910415d │ 3: job deploy, com conferência pela URL da revisão e pela URL  │
  │         │ pública                                                        │
  ├─────────┼────────────────────────────────────────────────────────────────┤
  │ 6e876e0 │ 4: README, ADR 0027, runbook, decisões e checklist de revisão  │
  └─────────┴────────────────────────────────────────────────────────────────┘

  Etapa 2:
  - check-workflows.sh: o id-token: write e o skopeo ficam permitidos só no job
    deploy. A regra 9 exige, nesse job, o if normalizado, os 10 checks em needs,
    a concorrência sem cancelamento e nenhum build. A regra 10 impede que a
    concorrência do topo cancele sempre.
  - ci.yml: na main, nenhuma execução é cancelada. O job image faz um único
    build com duas saídas, a awards:ci e um tar OCI. Ele confere que o tar tem
    um único manifesto linux/amd64 e que a awards:ci é a mesma imagem. Depois
    expõe o digest e, em push na main, guarda o tar como artefato.

  Etapa 3:
  - Flags do gcloud: antes de escrever o job, conferi no --help da 594.0.1 que
    todos os flags usados existem. O service.yaml alternativo não foi
    necessário.
  - verify-deployment.sh: confere a readiness, o resultado de referência, o
    Swagger e, com --expect-no-metrics, o 404 de /actuator/prometheus. Registra
    o tempo de cada chamada. A readiness espera até 60 s por chamada, para medir
    a partida a frio.
  - Job deploy: baixa o artefato, confere o digest, entra no GCP pela identidade
    federada, envia a imagem com o skopeo preservando o digest e implanta sem
    tráfego. Em seguida confere pela URL da revisão, passa o tráfego e confere
    de novo pela URL pública.

  Não rodou localmente, por decisão sua: os casos negativos da política (T018 e
  T026) e o teste local do verify-deployment.sh (T021). Localmente, só conferi a
  sintaxe bash e que o ci.yml é um YAML válido. Registrei essa redução nas
  tarefas e em docs/ai/decisoes.md. Os casos negativos da política ficam sem
  nenhuma prova executada.

  O que mais pode falhar no PR, porque nunca rodou:
  - as expressões yq novas do check-workflows.sh, já que não tenho o yq aqui. Se
    houver erro, ele aparece no job workflow-policy;
  - as duas saídas do build no job image, que aparecem no próprio job.

  Riscos que só aparecem depois do merge:
  - O job deploy só roda na main, então um erro nele só aparece depois do merge.
    Se acontecer, a URL continua servindo a revisão anterior, que hoje é a
    página de exemplo.
  - O risco que mais me preocupa é o de permissão. Algum comando gcloud run pode
    pedir uma leitura no nível do projeto, além dos papéis que a conta de
    publicação tem no serviço. Nesse caso, a correção prevista é acrescentar só
    o papel que faltar.

  Seus passos agora:
  1. T035: push do branch marco/5-deploy e abertura do PR. Quando os checks
     terminarem, me avise para eu conferir.
  2. T036 (opcional): no ambiente cloud-run, criar a regra de branch "só main".
  3. T037: o merge, depois da revisão do Codex.

  Depois do merge, eu faço a T038 (conferir a execução da main e a URL pública)
  e a T039 (passar a spec para Implemented).

  O docs/ai/prompts.md, gravado pelo hook, continua fora dos commits.

✻ Worked for 17m 30s · done 16:08

❯ Antes do push, rode os casos negativos da política (T018 e T026) e o          
  check-workflows.sh no HEAD num contêiner com o yq, como no Marco 3 (alpine    
  com yq v4). Cada caso negativo precisa reprovar, e o HEAD precisa passar. São 
  segundos, e é a proteção que impede outro job de obter a identidade do GCP.   
  Registre o resultado no decisoes.md, corrigindo a entrada que diz que ficaram 
  sem prova, e faça o commit. Depois me avise.                                  

  Ran 1 shell command (ctrl+o to expand)

● O Docker está instalado, mas o daemon não está rodando. Vou procurar como o
  Marco 3 rodou o yq e, em seguida, abrir o Docker Desktop.

  Searched for 2 patterns, listed 1 directory (ctrl+o to expand)

● Não achei o comando exato do Marco 3. Vou usar uma imagem alpine com o yq-go
  (mikefarah v4) e o bash, instalados pelo apk. Primeiro abro o Docker Desktop.

  Ran 1 shell command (ctrl+o to expand)

● O daemon subiu. Agora escrevo o roteiro de teste no scratchpad. Ele roda
  dentro do contêiner: confere o HEAD e aplica cada caso negativo numa cópia
  temporária do .github.

  Made 1 scratchpad edit +76, ran 1 shell command (ctrl+o to expand)

● Os 14 casos batem: o HEAD passa e os 12 negativos reprovam com a mensagem de
  cada regra. Agora gravo a evidência com a imagem, o commit e o roteiro. 
  · summary

● Bash(SPU="/c/Users/ander/AppData/Local/Temp/claude/cd9697c376913fb9/673a0a37-7
      d72-488d-8a3d-ed87641cc041/scratchpad" &&
      out=specs/005-deploy-cloud-run/evidencias/pol…)
  ⎿  136 specs/005-deploy-cloud-run/evidencias/politica-workflow.txt
     0
  ⎿  Created specs\005-deploy-cloud-run\evidencias\politica-workflow.txt
     +136
        1 +# Política do workflow: HEAD e casos negativos (spec 005, T018
          + e T026), 2026-11-26T19:19Z                                   
        2 +# Commit conferido: 6e876e0 (check-workflows.sh e ci.yml sem m
          +udança depois dele)                                           
        3 +# Contêiner: alpine:3.22 (alpine@sha256:49340b346798875b5592e5
          +d420241f2f9cfba5f47e62ebb1446a1b34734e898f), com bash e yq-go 
          +do apk                                                        
        4 +# Comando: docker run --rm -v <repo>:/repo:ro -v <roteiro>:/sp
          +:ro alpine:3.22 sh -c 'apk add --no-cache -q bash yq-go && bas
          +h /sp/policy-cases.sh'                                        
        5 +# Código de saída do roteiro: 0                               
        6 +                                                              
        7 +yq: yq (https://github.com/mikefarah/yq/) version v4.47.2     
        8 +bash: 5.2.37(1)-release                                       
        9 +                                                              
       10 +--- HEAD                                                      
       11 +      check-workflows.sh: 1 workflow(s), o dependabot.yml e o 
          +compose.yaml dentro da política.                              
       12 +==> HEAD: OK - passa na política (código 0)                   
       13 +                                                              
       14 +--- Casos negativos do contrato (cada um precisa reprovar com 
          +a mensagem da regra)                                          
       15 +==> caso 1: OK - id-token: write no job build (regra 3) (códig
          +o 1, esperado 1)                                              
       16 +      check-workflows.sh: 1 violação(ões) da política:        
       17 +        - .github/workflows/ci.yml: job build pede 'id-token: 
          +write' (só o job codeql pode ter security-events: write, e só 
          +o job deploy, id-token: write)                                
       18 +==> caso 2: OK - skopeo copy num passo do job image (regra 5) 
          +(código 1, esperado 1)                                        
       19 +      check-workflows.sh: 1 violação(ões) da política:        
       20 +        - .github/workflows/ci.yml: job image usa skopeo (o en
          +vio de imagem ao registro só pode estar no job deploy)        
       21 +==> caso 3: OK - job deploy sem compose em needs (regra 9) (có
          +digo 1, esperado 1)                                           
       22 +      check-workflows.sh: 1 violação(ões) da política:        
       23 +        - .github/workflows/ci.yml: job deploy sem o check obr
          +igatório 'compose' em needs                                   
       24 +==> caso 4: OK - job deploy com docker/build-push-action (regr
          +a 9) (código 1, esperado 1)                                   
       25 +      check-workflows.sh: 1 violação(ões) da política:        
       26 +        - .github/workflows/ci.yml: job deploy com docker/buil
          +d-push-action (a imagem publicada é a do job image, sem rebuil
          +d)                                                            
       27 +==> caso 5: OK - job deploy com if só pela ref (regra 9) (códi
          +go 1, esperado 1)                                             
       28 +      check-workflows.sh: 1 violação(ões) da política:        
       29 +        - .github/workflows/ci.yml: job deploy com if diferent
          +e do contrato (encontrado: 'github.ref == 'refs/heads/main''; 
          +esperado: 'github.event_name == 'push' && github.ref == 'refs/
          +heads/main' && github.repository == 'andersonfelipediefenbach/
          +outsera'')                                                    
       30 +==> caso 6: OK - secrets.FOO no job deploy (regra 4) (código 1
          +, esperado 1)                                                 
       31 +      check-workflows.sh: 1 violação(ões) da política:        
       32 +        - .github/workflows/ci.yml: referência a secrets.: 405
          +:      FOO: ${{ secrets.FOO }}                                
       33 +==> caso 7: OK - cancel-in-progress: true no topo (regra 10) (
          +código 1, esperado 1)                                         
       34 +      check-workflows.sh: 1 violação(ões) da política:        
       35 +        - .github/workflows/ci.yml: concurrency.cancel-in-prog
          +ress do topo é true fixo (use uma expressão que não cancele na
          + main)                                                        
       36 +                                                              
       37 +--- Casos extras (outra forma de obter a identidade ou de burl
          +ar o job deploy)                                              
       38 +==> caso 8: OK - permissions: write-all num job (regra 3) (cód
          +igo 1, esperado 1)                                            
       39 +      check-workflows.sh: 1 violação(ões) da política:        
       40 +        - .github/workflows/ci.yml: job image declara permissi
          +ons 'write-all' (use um mapa com permissões explícitas)       
     … +96 lines (ctrl+o to expand)

● Agora corrijo a entrada de decisoes.md e as tarefas T018 e T026.

● Update(docs\ai\decisoes.md)
  ⎿  Added 17 lines, removed 3 lines
      39    - a proibição de `secrets.` não mudou.
      40  - **Retenção do registro de 30 dias, mantendo as 10 imagens mai
          s recentes** (análise, achado C1). A regra de
      41    apagar só as imagens sem tag não bastaria, porque toda imagem
           recebe a tag do commit.
      42 -- **Conferências locais reduzidas** (decisão do responsável, du
         -rante a implementação). Localmente, cada etapa                 
      43 -  rodou só o `./mvnw verify` padrão. O `./mvnw -Pextras verify`
         - e as conferências de política e de script                     
      44 -  (T018, T021 e T026) ficaram para o pipeline. Os casos negativ
         -os da política não têm execução registrada.                    
      42 +- **Conferências locais reduzidas, com a política conferida ant
         +es do push** (decisões do responsável, durante                 
      43 +  a implementação).                                            
      44 +  - Localmente, cada etapa rodou só o `./mvnw verify` padrão. O
         + `./mvnw -Pextras verify` e o teste local do                   
      45 +    `verify-deployment.sh` (T021) ficaram para o pipeline. O sc
         +ript é comprovado pelas duas conferências do                   
      46 +    job `deploy`.                                              
      47 +  - A política do workflow (T018 e T026) foi conferida antes do
         + push, a pedido do responsável. Ela é a                        
      48 +    proteção que impede outro job de obter a identidade do GCP.
      49 +  - A conferência rodou num contêiner `alpine:3.22` com o `yq` 
         +4.47.2 (mikefarah) e o `bash` do `apk`, sobre o                
      50 +    commit `6e876e0`:                                          
      51 +    - o `check-workflows.sh` passou no HEAD;                   
      52 +    - os 7 casos negativos do contrato e 5 casos extras reprova
         +ram, cada um com a mensagem da sua regra. Os                   
      53 +      extras são `write-all` num job, `id-token: write` num job
         + novo, cancelamento no job `deploy`,                           
      54 +      `docker build` e `docker push` no `deploy`;              
      55 +    - o mesmo `if` do `deploy`, entre `${{ }}` e com espaços a 
         +mais, passou (normalização).                                   
      56 +  - O resultado está em                                        
      57 +    [`specs/005-deploy-cloud-run/evidencias/politica-workflow.t
         +xt`](../../specs/005-deploy-cloud-run/evidencias/politica-workf
         +low.txt),                                                      
      58 +    com o roteiro usado.                                       
      59  - **No Windows, o `gcloud` em bash precisa do `CLOUDSDK_PYTHON`
          ** apontando para o Python do próprio SDK. Sem
      60    ele, o atalho `python` da Microsoft Store responde no lugar,
          e o comando falha. O `setup.sh` define a
      61    variável quando ela falta (research R9).

  Read 1 file (ctrl+o to expand)

● Update(specs\005-deploy-cloud-run\tasks.md)
  ⎿  Added 9 lines, removed 5 lines
      173      - o tar é extraído num diretório, em vez de ler cada arqui
           vo com `tar -xOf`, para não depender do
      174        prefixo `./`;
      175      - o passo também confere que a plataforma é `linux/amd64`,
            e o artefato vai com `compression-level: 0`.
      176 -- [ ] T018 [US2] **Não executada localmente, por decisão do re
          -sponsável (2026-11-26):** localmente, cada                    
      177 -  etapa roda só o `./mvnw verify` padrão, e o resto fica para 
          -o pipeline. O workflow real é conferido pelo job              
      178 -  `workflow-policy` no PR. Os casos negativos não têm execução
          - no pipeline e ficam sem prova registrada; a                  
      179 -  revisão do PR confere as regras 3, 5, 9 e 10 no script. Feit
          -o localmente: `bash -n` do script e a leitura                 
      180 -  do `ci.yml` como YAML válido. Texto original da tarefa:     
      176 +- [X] T018 [US2] **Feita antes do push, a pedido do responsáve
          +l, junto com a T026.** Rodou num contêiner                    
      177 +  `alpine:3.22` com o `yq` 4.47.2 e o `bash`, e não com o biná
          +rio do Windows, sobre o commit `6e876e0`                      
      178 +  ([`evidencias/politica-workflow.txt`](evidencias/politica-wo
          +rkflow.txt)):                                                 
      179 +  - o HEAD passou;                                            
      180 +  - os casos 1, 2, 6 e 7 reprovaram com a mensagem da regra;  
      181 +  - os casos 3 a 5 (T026) e mais 5 casos extras também reprova
          +ram;                                                          
      182 +  - o `if` normalizado passou.                                
      183 +                                                              
      184 +  Texto original da tarefa:                                   
      185    - baixar o `yq` v4 (binário do Windows, versão fixada, SHA-2
           56 conferido com o `checksums` do release) para o
      186      scratchpad e pô-lo no PATH só nesse comando;
      187    - rodar `.github/scripts/check-workflows.sh`, que precisa pa
           ssar;

● Update(specs\005-deploy-cloud-run\tasks.md)
  ⎿  Added 3 lines, removed 4 lines
      279      - as URLs passam aos scripts por `env`, e não por expressã
           o no `run`;
      280      - a readiness espera até 60 s por chamada, para medir a pa
           rtida a frio (SC-001);
      281      - o `environment.url` usa a URL literal.
      282 -- [ ] T026 [US2] **Não executada localmente, por decisão do re
          -sponsável (2026-11-26):** o workflow real é                   
      283 -  conferido pelo job `workflow-policy` no PR. Os casos negativ
          -os 3 a 5 ficam sem prova registrada. Feito                    
      284 -  localmente: a leitura do `ci.yml` como YAML válido, com o `i
          -f`, os `needs`, a concorrência e as permissões do             
      285 -  job `deploy` iguais aos do contrato, e nenhuma referência a 
          -`secrets.`. Texto original da tarefa:                         
      282 +- [X] T026 [US2] **Feita antes do push, a pedido do responsáve
          +l, junto com a T018.** Os casos 3, 4 e 5                      
      283 +  reprovaram com a mensagem da regra 9, e o HEAD passou       
      284 +  ([`evidencias/politica-workflow.txt`](evidencias/politica-wo
          +rkflow.txt)). Texto original da tarefa:                       
      285    conferência local rápida da política com o job `deploy`:
      286    - o `check-workflows.sh` real passa;
      287    - os casos 3, 4 e 5 de [`contracts/workflow-policy.md`](cont
           racts/workflow-policy.md), numa cópia temporária,

● Update(README.md)
  ⎿  Added 1 line, removed 1 line
      755  | --- | --- | --- |
      756  | Serviço no modo padrão, com 0 a 1 instância por revisão, pro
           bes e métricas fechadas | FR-001 a FR-007, FR-004a, SC-001, SC
           -006 | Job `deploy` na `main` (as duas conferências do `verify
           -deployment.sh`, com o 404 de `/actuator/prometheus` e os temp
           os de cada chamada) e a leitura do serviço depois do merge |
      757  | A mesma imagem testada, pelo digest, sem rebuild | FR-002, F
           R-010 | Jobs `image` (digest e identidade da imagem) e `deploy
           ` (digest do artefato e do registro) |
      758 -| Publicação só em push na `main`, depois dos checks, serializ
          -ada e sem chaves | FR-008, FR-009, FR-011 a FR-013, SC-002, SC
          --003 | Job `workflow-policy` (regras 3, 4, 5, 9 e 10), o job `
          -deploy` pulado nos PRs e a condição do provedor de identidade 
          -conferida no GCP ([evidências](specs/005-deploy-cloud-run/evid
          -encias/setup-conferencias.txt)) |                             
      758 +| Publicação só em push na `main`, depois dos checks, serializ
          +ada e sem chaves | FR-008, FR-009, FR-011 a FR-013, SC-002, SC
          +-003 | Job `workflow-policy` (regras 3, 4, 5, 9 e 10), com os 
          +casos negativos conferidos antes do push ([evidência](specs/00
          +5-deploy-cloud-run/evidencias/politica-workflow.txt)), o job `
          +deploy` pulado nos PRs e a condição do provedor de identidade 
          +conferida no GCP ([evidência](specs/005-deploy-cloud-run/evide
          +ncias/setup-conferencias.txt)) |                              
      759  | Revisão sem tráfego até ser conferida | FR-014 a FR-016, FR-
           015a | Job `deploy`: a conferência pela URL da revisão antes d
           a troca de tráfego e o resumo da execução |
      760  | Configuração do GCP idempotente, com permissões mínimas | FR
           -017 a FR-021, SC-004 | As duas execuções do `deploy/gcp/setup
           .sh` (a segunda com zero mudanças) em [`evidencias/`](specs/00
           5-deploy-cloud-run/evidencias/) e a revisão do script no PR |
      761  | Modo padrão sem mudança | FR-022, SC-007 | Jobs `build` e `w
           orkflow-policy` (`check-unchanged-tests.sh`) |

  Ran 1 shell command (ctrl+o to expand)

● A política do workflow está conferida e commitada em 1a3889c, e o branch pode
  ir para o push. Os 14 resultados saíram como esperado: o HEAD passa e cada
  caso negativo reprova com a mensagem da sua regra.

  Rodei o check-workflows.sh num contêiner alpine:3.22 com o yq 4.47.2
  (mikefarah) e o bash, sobre o commit 6e876e0, que é o último que mexe no
  script e no ci.yml. Abri o Docker Desktop para isso.

  HEAD: passou, com código 0.

  Casos negativos do contrato: todos reprovaram, com código 1.

  ┌──────┬─────────────────────────────────────────┬───────┐
  │ Caso │          O que muda no ci.yml           │ Regra │
  ├──────┼─────────────────────────────────────────┼───────┤
  │ 1    │ id-token: write no job build            │ 3     │
  ├──────┼─────────────────────────────────────────┼───────┤
  │ 2    │ skopeo copy no job image                │ 5     │
  ├──────┼─────────────────────────────────────────┼───────┤
  │ 3    │ job deploy sem compose em needs         │ 9     │
  ├──────┼─────────────────────────────────────────┼───────┤
  │ 4    │ job deploy com docker/build-push-action │ 9     │
  ├──────┼─────────────────────────────────────────┼───────┤
  │ 5    │ job deploy com o if só pela ref         │ 9     │
  ├──────┼─────────────────────────────────────────┼───────┤
  │ 6    │ secrets.FOO no job deploy               │ 4     │
  ├──────┼─────────────────────────────────────────┼───────┤
  │ 7    │ cancel-in-progress: true no topo        │ 10    │
  └──────┴─────────────────────────────────────────┴───────┘

  Casos extras, de outras formas de obter a identidade ou de burlar o job
  deploy: também reprovaram.
  - permissions: write-all num job;
  - id-token: write num job novo;
  - cancel-in-progress: true no job deploy;
  - docker build no job deploy;
  - docker push no job deploy.

  Normalização: o mesmo if do deploy, entre ${{ }} e com espaços a mais, passou.

  Onde ficou registrado:
  - Evidência: specs/005-deploy-cloud-run/evidencias/politica-workflow.txt, com
    o digest da imagem, o commit, o comando, a saída completa e o roteiro usado.
    O roteiro em si fica fora do repositório, como os exemplos do Marco 2.
  - docs/ai/decisoes.md: corrigi a entrada que dizia que os casos ficaram sem
    prova. Ela agora registra a conferência e aponta para a evidência.
  - tasks.md: a T018 e a T026 estão concluídas. A T021, o teste local do
    verify-deployment.sh, continua para o pipeline.
  - README: a linha da política em "Requisitos do Marco 5" aponta para a
    evidência.

  Não rodei o ./mvnw verify neste commit, porque ele só mexe em documentação e
  evidência. O docs/ai/prompts.md, gravado pelo hook, continua fora dos commits.

  Pode fazer o push do marco/5-deploy e abrir o PR (T035). Quando os checks
  terminarem, me avise para eu conferir.

✻ Cogitated for 2m 19s · done 16:20

❯ Junto com a T039, ajuste o contrato (openapi.yaml), subindo para 1.2.1:       
  1. Encurte o info.description para duas frases: o que a API faz e que os      
  dados vêm do CSV carregado na inicialização. O ETag/304 e a autenticação      
  ficam no README, que já os explica.                                           
  2. Declare a autenticação como opcional: security com uma alternativa vazia   
  ({}) além do bearer, para o Swagger não sugerir que o login é obrigatório. Na 
  descrição do esquema de segurança, uma frase: "Opcional. Exigido só quando a  
  variável API_TOKEN está definida."                                            
  3. Confira que nenhum texto do contrato usa < > sem escape (o "Bearer         
  <token>" sumiu na renderização).                                              
     Rode o ./mvnw verify (o ApiDocsIT valida o contrato) e o                   
  check-unchanged-tests.sh. O deploy da main depois do merge publica a versão   
  nova; confira na URL pública.                                                 

● Antes de mexer no contrato, vou conferir onde estamos: se o merge aconteceu e
  o que a execução da main fez.
  Ran 1 shell command (ctrl+o to expand)

● O PR #14 foi mergeado (4aeb67e). Vou ver o resultado da execução da main pela
  API pública do GitHub, porque o gh não está instalado aqui.

  Ran 2 shell commands (ctrl+o to expand)

● The main run finished all green, with deploy passing all 13 steps in 111 s.
  Next I'm doing T038 on my side: checking the GCP service and running
  verify-deployment.sh against the public URL, using the alpine container since
  jq isn't installed here. · summary

  Ran 3 shell commands (ctrl+o to expand)

● A URL pública passou nos 4 passos, mas a primeira chamada de 1,2 s mostra que
  a instância já estava ativa, então ainda não medi a partida a frio do SC-001.
  Vou buscar esse tempo de subida no Cloud Logging, já que a API do GitHub não
  fornece o log sem autenticação. · summary

  Ran 1 shell command (ctrl+o to expand)

● O arranque a frio cumpre o SC-001: a nova revisão awards-00002-had passou o
  probe em cerca de 18,5 s, abaixo do limite de 30 s, e o CSV carregou as 206
  linhas. Agora vou preparar o branch da T039, porque o docs/ai/prompts.md local
  e o commit e9595f7 da main alteram o mesmo ficheiro, e primeiro verifico se a
  versão local já contém a da main. · summary

  Ran 1 shell command (ctrl+o to expand)

● A versão local contém a da main e só acrescenta linhas. Guardo a cópia local,
  avanço o branch marco/5-deploy até a main (fast-forward) e devolvo o arquivo
  local.

  Ran 1 shell command (ctrl+o to expand)

● O branch está em 4aeb67e, e o prompts.md só tem as linhas novas. Agora vejo o
  contrato e os testes que o leem.

  Read 1 file, ran 1 shell command (ctrl+o to expand)

● O contrato já traz a alternativa vazia antes do bearerAuth nas duas operações,
  então o item 2 depende só da frase do esquema de segurança. Vou ver os testes
  que leem o contrato e o que o pom.xml faz com ele. · summary

  Ran 1 shell command (ctrl+o to expand)

● ApiDocsSecurityIT asserts info.version == "1.2.0", so bumping to 1.2.1 means
  changing a Marco 3 test that check-unchanged-tests.sh protects. Next I'll read
  how that script works, its exception list, and what ApiDocsIT compares. 
  · summary

  Searched for 1 pattern, ran 1 shell command (ctrl+o to expand)

● O ApiDocsIT já compara o contrato publicado com o de
  specs/003-modo-docker/contracts/ e tem exceção registrada. Vou verificar se o
  ApiDocsSecurityIT também existe na tag v0.3.0, porque isso decide se ele está
  protegido. · summary

  Ran 1 shell command (ctrl+o to expand)

● Encontrei um bloqueio na versão 1.2.1: o ApiDocsSecurityIT do Marco 3 confere
  info.version == "1.2.0", e o check-unchanged-tests.sh v0.3.0 reprova a mudança
  sem exceção registrada. Como a exceção do ApiDocsIT foi uma decisão sua no
  contrato 1.2.0, preciso que você escolha como seguir antes que eu mexa nisso. 
  · summary

● Answered Claude's questions:
  ⎿  · O ApiDocsSecurityIT (Marco 3, protegido pela tag v0.3.0) confere 
     info.version == "1.2.0" no contrato. Subir para 1.2.1 obriga a mudar esse 
     IT, e o check-unchanged-tests.sh reprova sem exceção registrada. Como 
     seguir? → Exceção mínima (Recomendado)

● Vou seguir a ordem: spec 005 com o requisito 1.2.1, contrato novo copiado para
  static/, troca mínima nos dois ITs, um IT novo protegendo contra < e >,
  depois registro e rodar ./mvnw verify. · summary

  Searched for 1 pattern (ctrl+o to expand)

● Primeiro a spec (T039 e o requisito novo do contrato).

  Searched for 1 pattern, read 1 file (ctrl+o to expand)

● A FR-022 da spec 005 diz que nenhum IT é novo, removido ou alterado. O ajuste
  do contrato muda isso, então a FR-022 precisa ser emendada junto com o
  requisito novo, antes do código.

● Update(specs\005-deploy-cloud-run\spec.md)
  ⎿  Added 9 lines, removed 1 line
       4 
       5  **Created**: 2026-11-26
       6 
       7 -**Status**: Draft                                              
       7 +**Status**: Implemented (2026-11-26). A publicação foi comprova
         +da na `main` pela                                              
       8 +[execução 45885778155](https://github.com/andersonfelipediefenb
         +ach/outsera/actions/runs/45885778155) do merge do              
       9 +PR #14 (commit `4aeb67e`):                                     
      10 +- os 10 checks ficaram verdes, e o job `deploy` passou nos 13 p
         +assos em 111 s;                                                
      11 +- a revisão `awards-00002-had` roda a imagem                   
      12 +  `sha256:2a6e248b0586b07f756920c4da2e14b245d07e4dd41655c5eb743
         +a54f2bb2bf6` com 100% do tráfego.                              
      13 
      14 +A conferência depois do merge está em [`evidencias/publicacao-m
         +ain.txt`](evidencias/publicacao-main.txt). O                   
      15 +ajuste do contrato para a versão 1.2.1 (FR-027) entrou depois, 
         +no fechamento do marco.                                        
      16 +                                                               
      17  **Input**: User description: "Marco 5: deploy no Google Cloud R
          un, num único bolt. Projeto GCP outsera-awards, região southame
          rica-east1. Só o modo padrão (H2, carga do CSV), com a mesma im
          agem do pipeline, publicada no Artifact Registry. max-instances
          =1 e min-instances=0, o que mantém válido o cache em memória do
           ADR 0026 e limita o custo. Autenticação desligada, como no mod
          o padrão. Deploy automático a cada merge na main, autenticado p
          or OIDC com Workload Identity Federation, restrito a este repos
          itório e à branch main, sem chaves no GitHub. Toda a configuraç
          ão do GCP (APIs, Artifact Registry, conta de serviço com permis
          sões mínimas, pool e provedor de identidade) fica num script ve
          rsionado e idempotente, rodado pelo Claude Code com o gcloud de
          pois do meu login. O pipeline confere a URL publicada depois do
           deploy (readiness e resultado de referência). O README ganha a
           URL pública e a seção do Marco 5. Nada muda no ./mvnw verify d
          o modo padrão."
      18 
      19  ## Clarifications

● Update(specs\005-deploy-cloud-run\spec.md)
  ⎿  Added 5 lines
      28    → A: Não. A publicação define `MANAGEMENT_ENDPOINTS_WEB_EXPOS
          URE_INCLUDE=health`, que mantém só o que os
      29    probes usam; `/actuator/prometheus` responde 404 na URL públi
          ca. Decisão registrada no ADR 0027. FR-004 e
      30    FR-004a.
      31 +- Q: (fechamento, 2026-11-26) Com o Swagger UI público, o contr
         +ato muda? → A: Sim, para a versão 1.2.1,                       
      32 +  pedida pelo responsável. A descrição fica em duas frases, a a
         +utenticação fica declarada como opcional e                     
      33 +  nenhum texto usa `<` ou `>` sem escape. O `Bearer <token>` su
         +mia na renderização. O `ApiDocsSecurityIT`,                    
      34 +  protegido pela `v0.3.0`, muda só no número da versão, com exc
         +eção registrada (opção do responsável).                        
      35 +  FR-022 e FR-027.                                             
      36 
      37  ## User Scenarios & Testing *(mandatory)*
      38 

● Update(specs\005-deploy-cloud-run\spec.md)
  ⎿  Added 14 lines
      300  - **FR-025**: A mudança em relação ao briefing (publicação a c
           ada merge na `main`, e não por tag de versão)
      301    MUST ser registrada em `docs/ai/decisoes.md`.
      302 
      303 +**Contrato publicado (fechamento do marco)**                  
      304 +                                                              
      305 +- **FR-027**: O contrato OpenAPI MUST passar à versão **1.2.1*
          +*, com o arquivo-fonte em                                     
      306 +  `specs/005-deploy-cloud-run/contracts/openapi.yaml` e a cópi
          +a publicada em `/openapi.yaml` idêntica a ele.                
      307 +  Os contratos das specs 001 a 003 ficam como registro. Os pat
          +hs, os parâmetros, as respostas e os esquemas                 
      308 +  não mudam (Princípio III), e mudam só três textos:          
      309 +  - o `info.description` fica com duas frases: o que a API faz
          + e que os dados vêm do CSV carregado na                       
      310 +    inicialização. O ETag, o 304 e a autenticação ficam no REA
          +DME;                                                          
      311 +  - a autenticação fica declarada como opcional: o `security` 
          +de cada operação tem a alternativa vazia                      
      312 +    (`{}`) além do `bearerAuth`, e a descrição do `bearerAuth`
          + é exatamente "Opcional. Exigido só quando a                  
      313 +    variável API_TOKEN está definida.";                       
      314 +  - nenhuma `description` nem `summary` do contrato contém `<`
          + ou `>`, que o Swagger UI interpreta como HTML                
      315 +    e some na renderização.                                   
      316 +                                                              
      317  **Comprovação (Princípio VI)**
      318 
      319  - **FR-026**: Os requisitos MUST ser comprovados assim:

● Update(specs\005-deploy-cloud-run\spec.md)
  ⎿  Added 9 lines, removed 3 lines
      322      a restrição da federação é comprovada pela condição do pro
           vedor, conferida no GCP depois do script, e
      323      pelo job de publicação pulado no pull request;
      324    - FR-017 a FR-021: duas execuções do script, com a saída da
           segunda registrada, e revisão do script no PR;
      325 -  - FR-022: os dois comandos Maven verdes, sem alteração nos I
          -Ts (`check-unchanged-tests.sh`);                              
      326 -  - FR-023 a FR-025: revisão no PR, com item no checklist da s
          -pec.                                                          
      325 +  - FR-022: os dois comandos Maven verdes, sem alteração nos I
          +Ts fora das exceções registradas                              
      326 +    (`check-unchanged-tests.sh`);                             
      327 +  - FR-023 a FR-025: revisão no PR, com item no checklist da s
          +pec;                                                          
      328 +  - FR-027: o `ApiDocsIT`, com o contrato publicado idêntico a
          +o da spec 005, o `ApiDocsSecurityIT`, com a                   
      329 +    versão 1.2.1, e o IT novo `ContractTextIT`, com a descriçã
          +o do `bearerAuth`, as duas frases do                          
      330 +    `info.description` e nenhum `<` ou `>` nos textos. Os três
          + estão no `./mvnw verify`, e a URL pública é                  
      331 +    conferida depois do merge.                                
      332 
      328 -  Este marco não tem comportamento novo da aplicação, então nã
          -o pede IT novo.                                               
      333 +  A publicação (FR-001 a FR-021) não tem comportamento novo da
          + aplicação, então não pede IT novo. O IT novo é               
      334 +  o do contrato (FR-027).                                     
      335 
      336  ### Key Entities
      337 

  Searched for 1 pattern (ctrl+o to expand)

● Update(specs\005-deploy-cloud-run\spec.md)
  ⎿  Added 6 lines, removed 2 lines
      290 
      291  **Modo padrão e documentação**
      292 
      293 -- **FR-022**: Nada MUST mudar no `./mvnw verify` do modo padrã
          -o nem no `./mvnw -Pextras verify`: nenhum IT                  
      294 -  novo, removido ou alterado, e nenhuma configuração nova exig
          -ida para subir a aplicação localmente.                        
      293 +- **FR-022**: A publicação MUST NOT mudar nada no `./mvnw veri
          +fy` do modo padrão nem no                                     
      294 +  `./mvnw -Pextras verify`: nenhum IT novo, removido ou altera
          +do por causa dela, e nenhuma configuração nova                
      295 +  exigida para subir a aplicação localmente. A única mudança n
          +os ITs do marco é a do contrato 1.2.1 (FR-027):               
      296 +  - um IT novo, o `ContractTextIT`;                           
      297 +  - a constante do contrato da spec no `ApiDocsIT`, que já tin
          +ha exceção;                                                   
      298 +  - o número da versão no `ApiDocsSecurityIT`, com exceção nov
          +a registrada em `docs/ai/decisoes.md`.                        
      299  - **FR-023**: O README MUST trazer a URL pública (intervalos e
            Swagger UI) em "Avalie em 2 minutos", com o
      300    aviso da partida a frio, e a seção do Marco 5 entre os extra
           s (o que é publicado, como, o limite de uma
      301    instância e o que fica de fora), além dos requisitos do Marc
           o 5 na seção de requisitos.

● Agora o contrato 1.2.1 em specs/005-deploy-cloud-run/contracts/, a partir do
  1.2.0 publicado.

● Bash(mkdir -p specs/005-deploy-cloud-run/contracts && cp
      specs/003-modo-docker/contracts/openapi.yaml
      specs/005-deploy-cloud-run/contracts/openapi.yaml && file specs…)  ⎿  specs/005-deploy-cloud-run/contracts/openapi.yaml: Unicode text, UTF-8
      text
  ⎿  Created specs\005-deploy-cloud-run\contracts\openapi.yaml +235
        1 +openapi: 3.1.0                                                
        2 +info:                                                         
        3 +  title: Golden Raspberry Awards API                          
        4 +  version: 1.2.0                                              
        5 +  description: >                                              
        6 +    Intervalos entre prêmios consecutivos de produtores na cat
          +egoria Pior Filme e listagem dos filmes                       
        7 +    indicados e vencedores. Os dados vêm do arquivo CSV carreg
          +ado na inicialização. As respostas 200                        
        8 +    trazem ETag, e uma requisição com If-None-Match igual ao E
          +Tag atual recebe 304 sem corpo. A                             
        9 +    autenticação é opcional e fica desligada por padrão. Com a
          + variável de ambiente API_TOKEN definida,                     
       10 +    os dois endpoints exigem o cabeçalho Authorization: Bearer
          + <token>; sem ele, ou com outro valor, a                      
       11 +    resposta é 401 em Problem Details.                        
       12 +paths:                                                        
       13 +  /api/producers/intervals:                                   
       14 +    get:                                                      
       15 +      operationId: getProducerIntervals                       
       16 +      summary: Produtores com o menor e o maior intervalo entr
          +e duas vitórias consecutivas                                  
       17 +      description: >                                          
       18 +        Considera só filmes vencedores e pares de vitórias viz
          +inhas de cada produtor. Todos os empatados                    
       19 +        entram na lista, ordenados por previousWin e depois po
          +r nome. Sem produtor com duas vitórias,                       
       20 +        as duas listas vêm vazias.                            
       21 +      tags: [producers]                                       
       22 +      security:                                               
       23 +        - {}                                                  
       24 +        - bearerAuth: []                                      
       25 +      parameters:                                             
       26 +        - $ref: '#/components/parameters/IfNoneMatch'         
       27 +      responses:                                              
       28 +        '200':                                                
       29 +          description: Intervalos mínimo e máximo             
       30 +          headers:                                            
       31 +            ETag:                                             
       32 +              $ref: '#/components/headers/ETag'               
       33 +          content:                                            
       34 +            application/json:                                 
       35 +              schema:                                         
       36 +                $ref: '#/components/schemas/ProducerIntervals'
       37 +              examples:                                       
       38 +                arquivoFornecido:                             
       39 +                  summary: Resultado com o arquivo fornecido n
          +o enunciado                                                   
       40 +                  value:                                      
     … +195 lines (ctrl+o to expand)

  Read 1 file (ctrl+o to expand)

● Update(specs\005-deploy-cloud-run\contracts\openapi.yaml)
  ⎿  Added 2 lines, removed 6 lines
       1  openapi: 3.1.0
       2  info:
       3    title: Golden Raspberry Awards API
       4 -  version: 1.2.0                                               
       4 +  version: 1.2.1                                               
       5    description: >
       6      Intervalos entre prêmios consecutivos de produtores na cate
          goria Pior Filme e listagem dos filmes
       7 -    indicados e vencedores. Os dados vêm do arquivo CSV carrega
         -do na inicialização. As respostas 200                          
       8 -    trazem ETag, e uma requisição com If-None-Match igual ao ET
         -ag atual recebe 304 sem corpo. A                               
       9 -    autenticação é opcional e fica desligada por padrão. Com a 
         -variável de ambiente API_TOKEN definida,                       
      10 -    os dois endpoints exigem o cabeçalho Authorization: Bearer 
         -<token>; sem ele, ou com outro valor, a                        
      11 -    resposta é 401 em Problem Details.                         
       7 +    indicados e vencedores. Os dados vêm do arquivo CSV carrega
         +do na inicialização.                                           
       8  paths:
       9    /api/producers/intervals:
      10      get:

● Update(specs\005-deploy-cloud-run\contracts\openapi.yaml)
  ⎿  Added 1 line, removed 1 line
      120      bearerAuth:
      121        type: http
      122        scheme: bearer
      123 -      description: Token definido em API_TOKEN. Sem a variável
          -, a autenticação fica desligada.                              
      123 +      description: Opcional. Exigido só quando a variável API_
          +TOKEN está definida.                                          
      124    parameters:
      125      IfNoneMatch:
      126        name: If-None-Match

● Bash(c=specs/005-deploy-cloud-run/contracts/openapi.yaml && echo "linhas com <
      ou >:" && grep -nE '[<>]' $c || echo "(nenhuma)"; cp $c
      src/main/resources/static/open…)
  ⎿  linhas com < ou >:
     5:  description: >
     13:      description: >
     … +8 lines (ctrl+o to expand)
  ⎿  Updated src\main\resources\static\openapi.yaml +3 -7
       1  openapi: 3.1.0
       2  info:
       3    title: Golden Raspberry Awards API
       4 -  version: 1.2.0                                               
       4 +  version: 1.2.1                                               
       5    description: >
       6      Intervalos entre prêmios consecutivos de produtores na cate
          goria Pior Filme e listagem dos filmes
       7 -    indicados e vencedores. Os dados vêm do arquivo CSV carrega
         -do na inicialização. As respostas 200                          
       8 -    trazem ETag, e uma requisição com If-None-Match igual ao ET
         -ag atual recebe 304 sem corpo. A                               
       9 -    autenticação é opcional e fica desligada por padrão. Com a 
         -variável de ambiente API_TOKEN definida,                       
      10 -    os dois endpoints exigem o cabeçalho Authorization: Bearer 
         -<token>; sem ele, ou com outro valor, a                        
      11 -    resposta é 401 em Problem Details.                         
       7 +    indicados e vencedores. Os dados vêm do arquivo CSV carrega
         +do na inicialização.                                           
       8  paths:
       9    /api/producers/intervals:
      10      get:
     ...
      120      bearerAuth:
      121        type: http
      122        scheme: bearer
      127 -      description: Token definido em API_TOKEN. Sem a variável
          -, a autenticação fica desligada.                              
      123 +      description: Opcional. Exigido só quando a variável API_
          +TOKEN está definida.                                          
      124    parameters:
      125      IfNoneMatch:
      126        name: If-None-Match

● Os únicos > que sobraram são o indicador de bloco do YAML (description: >),
  não texto, e a cópia publicada é idêntica à da spec. Agora a troca mínima nos
  dois ITs.

  Read 2 files (ctrl+o to expand)

● Update(src\test\java\com\outsera\awards\docs\ApiDocsIT.java)
  ⎿  Added 2 lines, removed 2 lines
      27 
      28      private static final String SWAGGER_UI = "/swagger-ui.html"
          ;
      29      private static final String CONTRACT = "/openapi.yaml";
      30 -    /** Contrato 1.2.0, com a autenticação opcional (spec 003, 
         -FR-026). Os das specs 001 e 002 ficam como registro. */        
      31 -    private static final Path SPEC_CONTRACT = Path.of("specs", 
         -"003-modo-docker", "contracts", "openapi.yaml");               
      30 +    /** Contrato 1.2.1 (spec 005, FR-027). Os das specs 001 a 0
         +03 ficam como registro. */                                     
      31 +    private static final Path SPEC_CONTRACT = Path.of("specs", 
         +"005-deploy-cloud-run", "contracts", "openapi.yaml");          
      32 
      33      @Test
      34      @DisplayName("FR-024 Swagger UI disponível")

● Update(src\test\java\com\outsera\awards\docs\ApiDocsSecurityIT.java)
  ⎿  Added 3 lines, removed 3 lines
      10  import org.yaml.snakeyaml.Yaml;
      11 
      12  /**
      13 - * O contrato publicado é o 1.2.0, com a autenticação opcional 
         -e a resposta 401 nos dois endpoints de                         
      14 - * negócio (spec 003, FR-026; contracts/openapi-1.2.0.md). O {@
         -code ApiDocsIT} continua conferindo que o                      
      15 - * contrato publicado é idêntico ao da spec.                   
      13 + * O contrato publicado é o 1.2.1 (spec 005, FR-027), com a aut
         +enticação opcional e a resposta 401 nos                        
      14 + * dois endpoints de negócio (spec 003, FR-026; contracts/opena
         +pi-1.2.0.md). O {@code ApiDocsIT} continua                     
      15 + * conferindo que o contrato publicado é idêntico ao da spec.  
      16   */
      17  class ApiDocsSecurityIT extends ScenarioIT {
      18 

● Update(src\test\java\com\outsera\awards\docs\ApiDocsSecurityIT.java)
  ⎿  Added 2 lines, removed 2 lines
      24      }
      25 
      26      @Test
      27 -    @DisplayName("S003 FR-026 contrato 1.2.0 com bearerAuth opc
         -ional e 401 nos endpoints de negócio")                         
      27 +    @DisplayName("S003 FR-026 contrato 1.2.1 com bearerAuth opc
         +ional e 401 nos endpoints de negócio")                         
      28      @SuppressWarnings("unchecked")
      29      void contractDocumentsTheAuthentication() {
      30          Map<String, Object> root = contract();
      31 -        assertThat((Map<String, Object>) root.get("info")).cont
         -ainsEntry("version", "1.2.0");                                 
      31 +        assertThat((Map<String, Object>) root.get("info")).cont
         +ainsEntry("version", "1.2.1");                                 
      32          Map<String, Object> schemes = (Map<String, Object>) ((M
          ap<String, Object>) root.get("components"))
      33                  .get("securitySchemes");
      34          assertThat((Map<String, Object>) schemes.get("bearerAut
          h"))

● Agora o IT novo. Antes, vejo a base ScenarioIT para seguir o padrão dos outros
  ITs de docs/.

  Searched for 1 pattern (ctrl+o to expand)

● Write(src\test\java\com\outsera\awards\docs\ContractTextIT.java)
  ⎿  Wrote 80 lines to
     src\test\java\com\outsera\awards\docs\ContractTextIT.java
       1 package com.outsera.awards.docs;
       2 
       3 import static org.assertj.core.api.Assertions.assertThat;
       4 
       5 import com.outsera.awards.support.ScenarioIT;
       6 import java.util.ArrayList;
       7 import java.util.List;
       8 import java.util.Map;
       9 import org.junit.jupiter.api.DisplayName;
      10 import org.junit.jupiter.api.Test;
     … +70 lines (ctrl+o to expand)

  Read 1 file, ran 1 shell command (ctrl+o to expand)

● Bash(printf '\n# Marco 5, no modo padrão (spec 005,
      FR-027)\ncom/outsera/awards/docs/ContractTextIT.java\n' >>
      src/test/default-mode-its.txt && tail -4 src/test/defa…)
  ⎿  com/outsera/awards/isolation/DefaultModeIsolationIT.java

     # Marco 5, no modo padrão (spec 005, FR-027)
     com/outsera/awards/docs/ContractTextIT.java
  ⎿  Updated src\test\default-mode-its.txt +3
      72  com/outsera/awards/security/CacheHeadersIT.java
      73  com/outsera/awards/security/NoGeneratedPasswordIT.java
      74  com/outsera/awards/isolation/DefaultModeIsolationIT.java
      75 +                                                               
      76 +# Marco 5, no modo padrão (spec 005, FR-027)                   
      77 +com/outsera/awards/docs/ContractTextIT.java                    

● Agora a entrada no decisoes.md e a linha da exceção. O título da linha precisa
  bater exatamente com o da entrada.

● Update(docs\ai\decisoes.md)
  ⎿  Added 28 lines
       3  Sugestões e decisões relevantes, aceitas ou rejeitadas, com o m
          otivo (item 5.2 do enunciado). As
       4  decisões de arquitetura ficam em ADRs, em `docs/adr/`.
       5 
       6 +## 2026-11-26 — Contrato 1.2.1 e exceção do `ApiDocsSecurityIT`
         + à regra dos testes sem alteração                              
       7 +                                                               
       8 +- **Origem:** pedido do responsável no fechamento do Marco 5 (s
         +pec 005, FR-027). Com o Swagger UI público na                  
       9 +  URL do Cloud Run, o contrato passa à versão 1.2.1, com três a
         +justes de texto:                                               
      10 +  - o `info.description` fica com duas frases. O ETag, o 304 e 
         +a autenticação ficam no README;                                
      11 +  - a descrição do `bearerAuth` passa a "Opcional. Exigido só q
         +uando a variável API_TOKEN está definida.";                    
      12 +  - nenhum texto tem `<` ou `>` sem escape: o `Bearer <token>` 
         +do 1.2.0 sumia na renderização do Swagger UI.                  
      13 +                                                               
      14 +  O `security` de cada operação já tinha a alternativa vazia (`
         +{}`) antes do `bearerAuth` desde o 1.2.0, e                    
      15 +  ficou como estava. Paths, parâmetros, respostas e esquemas nã
         +o mudam (Princípio III).                                       
      16 +- **Conflito com a regra dos ITs sem alteração:** o `ApiDocsSec
         +urityIT` (Marco 3, tag `v0.3.0`) confere                       
      17 +  `info.version == "1.2.0"`. Subir a versão obriga a mudá-lo, e
         + o `check-unchanged-tests.sh v0.3.0`                           
      18 +  reprovaria.                                                  
      19 +- **Alternativas apresentadas ao responsável:**                
      20 +  - **exceção mínima:** trocar só a versão no `ApiDocsSecurityI
         +T`;                                                            
      21 +  - **manter o 1.2.0:** fazer os ajustes de texto sem subir o n
         +úmero;                                                         
      22 +  - **não ajustar o contrato agora.**                          
      23 +- **Decisão do responsável:** exceção mínima.                  
      24 +  - O `ApiDocsSecurityIT` muda só no literal da versão e nos do
         +is textos que a citam (o `@DisplayName` e o                    
      25 +    javadoc). Nenhuma outra asserção muda, e ele ganha uma linh
         +a em `.github/unchanged-tests-exceptions.txt`                  
      26 +    com o título desta entrada.                                
      27 +  - O contrato-fonte passa a `specs/005-deploy-cloud-run/contra
         +cts/openapi.yaml`, e o 1.2.0 da spec 003 fica                  
      28 +    como registro, como os das specs 001 e 002.                
      29 +  - O `ApiDocsIT`, que já tinha exceção ("Exceção do `ApiDocsIT
         +` à regra dos testes sem alteração"), troca só                 
      30 +    a constante `SPEC_CONTRACT`, como no Marco 3.              
      31 +- **Teste novo:** o `ContractTextIT` (modo padrão) confere as d
         +uas frases do `info.description`, a descrição                  
      32 +  do `bearerAuth` e que nenhuma `description`, `summary` ou `ti
         +tle` do contrato contém `<` ou `>`.                            
      33 +                                                               
      34  ## 2026-11-26 — Marco 5: publicação no Cloud Run
      35 
      36  Decisões da [spec 005](../../specs/005-deploy-cloud-run/spec.md
          ), do plano e da análise. O resumo de

● Bash(printf '%s\n'
      'src/test/java/com/outsera/awards/docs/ApiDocsSecurityIT.java Contrato
      1.2.1 e exceção do `ApiDocsSecurityIT` à regra dos testes sem alteração'
      >>…)
  ⎿  src/test/java/com/outsera/awards/docs/ApiDocsIT.java Exceção do `ApiDo
     csIT` à regra dos testes sem alteração
     src/test/java/com/outsera/awards/docs/ApiDocsSecurityIT.java Contrato
     … +2 lines (ctrl+o to expand)
  ⎿  Updated .github\unchanged-tests-exceptions.txt +1
      2  # docs/ai/decisoes.md que autoriza a mudança (spec 003, research
          R14).
      3  # Formato: <caminho> <título da entrada, exatamente como no deci
         soes.md>
      4  src/test/java/com/outsera/awards/docs/ApiDocsIT.java Exceção do
         `ApiDocsIT` à regra dos testes sem alteração
      5 +src/test/java/com/outsera/awards/docs/ApiDocsSecurityIT.java Con
        +trato 1.2.1 e exceção do `ApiDocsSecurityIT` à regra dos testes 
        +sem alteração                                                   

● Agora as menções ao contrato no README.

  Searched for 1 pattern, read 1 file, ran 1 shell command (ctrl+o to expand)

● Update(README.md)
  ⎿  Added 8 lines, removed 5 lines
      423 
      424  ## 4. Endpoints
      425 
      426 -O contrato completo (versão 1.2.0, com ETag e a autenticação o
          -pcional) está em                                              
      427 -[`specs/003-modo-docker/contracts/openapi.yaml`](specs/003-mod
          -o-docker/contracts/openapi.yaml), publicado                   
      426 +O contrato completo (versão 1.2.1, com ETag e a autenticação o
          +pcional) está em                                              
      427 +[`specs/005-deploy-cloud-run/contracts/openapi.yaml`](specs/00
          +5-deploy-cloud-run/contracts/openapi.yaml), publicado         
      428  em <http://localhost:8080/openapi.yaml> e navegável no Swagger
            UI, em
      429 -<http://localhost:8080/swagger-ui.html>. Os dos Marcos 1 e 2 f
          -icam em                                                       
      430 -[`specs/001-producer-intervals-api/contracts/openapi.yaml`](sp
          -ecs/001-producer-intervals-api/contracts/openapi.yaml)        
      431 -e [`specs/002-operacao/contracts/openapi.yaml`](specs/002-oper
          -acao/contracts/openapi.yaml), como registro.                  
      429 +<http://localhost:8080/swagger-ui.html>. Os dos Marcos 1 a 3 f
          +icam em                                                       
      430 +[`specs/001-producer-intervals-api/contracts/openapi.yaml`](sp
          +ecs/001-producer-intervals-api/contracts/openapi.yaml),       
      431 +[`specs/002-operacao/contracts/openapi.yaml`](specs/002-operac
          +ao/contracts/openapi.yaml) e                                  
      432 +[`specs/003-modo-docker/contracts/openapi.yaml`](specs/003-mod
          +o-docker/contracts/openapi.yaml) (1.2.0), como                
      433 +registro. A 1.2.1 muda só textos: a descrição geral em duas fr
          +ases e a autenticação descrita como opcional                  
      434 +(spec 005, FR-027).                                           
      435 
      436  | Método e caminho | O que faz |
      437  | --- | --- |

● Update(README.md)
  ⎿  Added 1 line, removed 1 line
      607  | `logging/` | Log em texto sem a variável, JSON ECS com ela,
           banner desligado e nenhum cabeçalho `Authorization` no log |
      608  | `isolation/` | Sem perfil, o Kafka não é configurado: nenhum
           a métrica nem cliente Kafka (Marco 3) |
      609  | `kafka/` | Ingestão por Kafka com o broker embutido: resulta
           do de referência, equivalência byte a byte com a carga direta,
            readiness só com o evento de carga concluída, linhas inválida
           s, falhas estruturais, vencedor novo em até 5 s, atualização,
           idempotência, cada tipo de evento inválido no tópico de erros
           (inclusive o caractere nulo, que o PostgreSQL não aceita), tóp
           ico de erros fora do ar, broker fora do ar e métricas (Marco 3
           ) |
      610 -| `security/` | Autenticação desligada (guarda do `API_TOKEN`)
          - e ligada: 401 em Problem Details, endpoints públicos, token f
          -ora do log, sem senha gerada, cabeçalhos de cache e de proteçã
          -o e o contrato 1.2.0 (Marco 3) |                              
      610 +| `security/` | Autenticação desligada (guarda do `API_TOKEN`)
          + e ligada: 401 em Problem Details, endpoints públicos, token f
          +ora do log, sem senha gerada, cabeçalhos de cache e de proteçã
          +o e o contrato publicado (1.2.0 no Marco 3, 1.2.1 desde o Marc
          +o 5) |                                                        
      611  | `postgres/` | Só no perfil `postgres-it`: esquema, equivalên
           cia e ordem entre o H2 e o PostgreSQL, reinícios, nova tentati
           va quando a gravação falha e recusa permanente do banco por da
           do inválido, que vai ao tópico de erros (Marco 3) |
      612  | `support/` | Base comum (`ScenarioIT`, `KafkaScenarioIT`, `T
           okenScenarioIT`) e apoios (aplicação real por `SpringApplicati
           on`, HTTP, Kafka e PostgreSQL reais, leitura do log, do Promet
           heus e do contrato), sem testes próprios |
      613 

● Update(README.md)
  ⎿  Added 1 line, removed 1 line
      1077  ├── specs/002-operacao/          # Marco 2: spec, plano, rese
            arch, contratos e tarefas
      1078  │   └── contracts/               # OpenAPI 1.1.0 (registro),
            endpoints operacionais, imagem e pipeline
      1079  ├── specs/003-modo-docker/       # Marco 3: spec, plano, rese
            arch, contratos e tarefas
      1080 -│   └── contracts/               # OpenAPI 1.2.0 (fonte única
           -), eventos do Kafka, compose e pipeline                      
      1080 +│   └── contracts/               # OpenAPI 1.2.0 (registro), 
           +eventos do Kafka, compose e pipeline                         
      1081  ├── specs/004-acabamento-operacional/ # Marco 4: spec, plano,
             research, contratos e tarefas
      1082  ├── specs/005-deploy-cloud-run/  # Marco 5: spec, plano, rese
            arch, contratos, tarefas e evidências do GCP
      1083  ├── src/main/java/com/outsera/awards/

● Update(README.md)
  ⎿  Added 1 line
      1080  │   └── contracts/               # OpenAPI 1.2.0 (registro),
            eventos do Kafka, compose e pipeline
      1081  ├── specs/004-acabamento-operacional/ # Marco 4: spec, plano,
             research, contratos e tarefas
      1082  ├── specs/005-deploy-cloud-run/  # Marco 5: spec, plano, rese
            arch, contratos, tarefas e evidências do GCP
      1083 +│   └── contracts/               # OpenAPI 1.2.1 (fonte única
           +), publicação, script do GCP e política do workflow          
      1084  ├── src/main/java/com/outsera/awards/
      1085  │   ├── AwardsApplication.java   # classe principal
      1086  │   ├── ingestion/               # leitura, validação e carga
             do CSV; kafka/: ingestão por Kafka (perfil kafka)

● Update(README.md)
  ⎿  Added 1 line
      763  | Configuração do GCP idempotente, com permissões mínimas | FR
           -017 a FR-021, SC-004 | As duas execuções do `deploy/gcp/setup
           .sh` (a segunda com zero mudanças) em [`evidencias/`](specs/00
           5-deploy-cloud-run/evidencias/) e a revisão do script no PR |
      764  | Modo padrão sem mudança | FR-022, SC-007 | Jobs `build` e `w
           orkflow-policy` (`check-unchanged-tests.sh`) |
      765  | README, ADR 0027, runbook e decisões | FR-023 a FR-025 | Rev
           isão do PR, por [`specs/005-deploy-cloud-run/checklists/revisa
           o.md`](specs/005-deploy-cloud-run/checklists/revisao.md) |
      766 +| Contrato 1.2.1: descrição em duas frases, autenticação opcio
          +nal e nenhum `<` ou `>` nos textos | FR-027 | `ContractTextIT`
          +, `ApiDocsSecurityIT` (versão) e `ApiDocsIT` (publicado idênti
          +co ao da spec 005) |                                          
      767 
      768  ---
      769 
● Agora a evidência da publicação (T038). Depois rodo o ./mvnw verify e o
  check-unchanged-tests.sh contra as duas tags.

● Bash(source ~/.bashrc &&
      out=specs/005-deploy-cloud-run/evidencias/publicacao-main.txt && {
      echo "# Publicação na main conferida depois do merge (spec 005, T038),
      $(…)
  ⎿  # Publicação na main conferida depois do merge (spec 005, T038), 2026-
     11-26T20:14Z
     … +69 lines (ctrl+o to expand)
  ⎿  Created specs\005-deploy-cloud-run\evidencias\publicacao-main.txt +65
       1 +# Publicação na main conferida depois do merge (spec 005, T038)
         +, 2026-11-26T20:14Z                                            
       2 +                                                               
       3 +## Execução da main                                            
       4 +Merge do PR #14, commit 4aeb67e: https://github.com/andersonfel
         +ipediefenbach/outsera/actions/runs/45885778155 (conclusão: succ
         +ess)                                                           
       5 +Jobs (conclusão e duração, pela API pública do GitHub):        
       6 +  Política do workflow, do Dependabot e do compose             
         +  success 7s                                                   
       7 +  Build, testes e cobertura                                    
         +  success 202s                                                 
       8 +  Lint do Dockerfile (hadolint)                                
         +  success 7s                                                   
       9 +  Análise de segurança do código (CodeQL)                      
         +  success 194s                                                 
      10 +  Vulnerabilidades das dependências (Trivy)                    
         +  success 59s                                                  
      11 +  Regras de alerta do SLO (promtool)                           
         +  success 9s                                                   
      12 +  Testes com PostgreSQL                                        
         +  success 80s                                                  
      13 +  Teste de carga (k6) e carga inicial com o CSV gerado         
         +  success 390s                                                 
      14 +  Imagem, vulnerabilidades da imagem e teste de fumaça         
         +  success 158s                                                 
      15 +  Compose com PostgreSQL, Kafka, Prometheus e Grafana          
         +  success 199s                                                 
      16 +  Publicação no Cloud Run e conferência da URL                 
         +  success 111s                                                 
      17 +       1 Set up job: success                                   
      18 +       2 Início da publicação: success                         
      19 +       3 Run actions/checkout@3d3c42e5aac5ba805825da76410c18127
         +3ba90b1: success                                               
      20 +       4 Run actions/download-artifact@9000827ccba6bdab643e8b6f
         +d33ac0654aef8333: success                                      
      21 +       5 O artefato é a imagem testada no job image: success   
      22 +       6 Run google-github-actions/auth@7c6bc770dae815cd3e89ee6
         +cdf493a5fab2cc093: success                                     
      23 +       7 Run google-github-actions/setup-gcloud@aa5489c8933f4cc
         +7a4f7d45035b3b1440c9c10db: success                             
      24 +       8 Envio da imagem testada ao Artifact Registry, com o di
         +gest preservado: success                                       
      25 +       9 Implantação sem tráfego, com tag própria: success     
      26 +      10 Conferência pela URL da revisão, antes do tráfego: suc
         +cess                                                           
      27 +      11 Tráfego para a revisão conferida: success             
      28 +      12 Conferência pela URL pública: success                 
      29 +      13 Resumo da publicação: success                         
      30 +                                                               
      31 +## Serviço no Cloud Run (gcloud run services describe)         
      32 +imagem: southamerica-east1-docker.pkg.dev/outsera-awards/awards
         +/awards@sha256:2a6e248b0586b07f756920c4da2e14b245d07e4dd41655c5
         +eb743a54f2bb2bf6                                               
      33 +maxScale: 1; minScale: ausente (ausente = 0)                   
      34 +startup-cpu-boost: true                                        
      35 +env: [{'name': 'MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE', 'va
         +lue': 'health'}]                                               
      36 +startupProbe: {'failureThreshold': 60, 'httpGet': {'path': '/ac
         +tuator/health/readiness', 'port': 8080}, 'periodSeconds': 2, 't
         +imeoutSeconds': 1}                                             
      37 +livenessProbe: {'failureThreshold': 3, 'httpGet': {'path': '/ac
         +tuator/health/liveness', 'port': 8080}, 'periodSeconds': 30, 't
         +imeoutSeconds': 3}                                             
      38 +serviceAccount: awards-runtime@outsera-awards.iam.gserviceaccou
         +nt.com                                                         
      39 +traffic: [('awards-00002-had', 100, 'c-4aeb67e')]              
      40 +imagem no registro com a tag do commit: awards:4aeb67ec8ccbdd04
         +994166ab97fd87a328471a90 -> sha256:2a6e248b0586b07f756920c4da2e
         +14b245d07e4dd41655c5eb743a54f2bb2bf6 (o mesmo digest da revisão
         +)                                                              
     … +25 lines (ctrl+o to expand)
  ⎿  (timeout 2m)

● A evidência está registrada: o "conclu?da" no log é só um problema de
  codificação do console do gcloud no Windows, e o texto original diz
  "concluída". Agora rodo ./mvnw verify e check-unchanged-tests.sh nas duas
  tags. · summary

  Ran 1 shell command (ctrl+o to expand)

● Os testes passam todos (212 no total, sem falhas), e o check de testes
  inalterados também passa nas versões v0.2.0 e v0.3.0. Agora atualizo as
  tarefas e faço dois commits, um para T038/T039 e outro para o contrato. 
  · summary

  Searched for 1 pattern, read 1 file (ctrl+o to expand)

● Update(specs\005-deploy-cloud-run\tasks.md)
  ⎿  Added 39 lines, removed 1 line
      363      `maxScale` igual a 1 e `minScale` igual a 0 ou ausente, qu
           e significa 0 (SC-006);
      364    - se o job falhar, parar, diagnosticar e propor a correção n
           um commit novo (research R4, risco de permissão
      365      de leitura).
      366 -- [ ] T039 Em `specs/005-deploy-cloud-run/spec.md`, passar o `
          -Status` a `Implemented`, com o link da execução               
      366 +  - *Feito* ([`evidencias/publicacao-main.txt`](evidencias/pub
          +licacao-main.txt)):                                           
      367 +    - a [execução 45885778155](https://github.com/andersonfeli
          +pediefenbach/outsera/actions/runs/45885778155)                
      368 +      ficou toda verde, e o job `deploy` passou nos 13 passos 
          +em 111 s. A permissão de leitura do projeto                   
      369 +      não fez falta;                                          
      370 +    - a revisão `awards-00002-had` está com 100% do tráfego e 
          +a imagem pelo digest do registro, com                         
      371 +      `maxScale` 1 e `minScale` ausente (0);                  
      372 +    - a partida a frio levou cerca de 18,5 s, da instância cri
          +ada ao probe de startup aprovado, pelo Cloud                  
      373 +      Logging. O resumo do job não é acessível pela API sem au
          +tenticação;                                                   
      374 +    - o `verify-deployment.sh --expect-no-metrics` passou na U
          +RL pública, com os intervalos em 0,14 s e o                   
      375 +      `/actuator/prometheus` em 404.                          
      376 +- [X] T039 Em `specs/005-deploy-cloud-run/spec.md`, passar o `
          +Status` a `Implemented`, com o link da execução               
      377    da `main` que comprovou a publicação. Fazer o commit num bra
           nch novo, já que commits na `main` não são
      378    permitidos, e pedir o push ao Anderson [Anderson].
      379 +  - *Feito no próprio `marco/5-deploy`,* avançado por fast-for
          +ward até a `main` (`4aeb67e`), como no fechamento             
      380 +    do Marco 4. Nenhum commit vai direto para a `main`.       
      381  - [ ] T040 [Anderson] Exportar a sessão (`/export`) para `docs
           /ai/sessoes/`.
      382 
      383  ---
      384 
      385 +## Phase 7: Contrato 1.2.1 (FR-027), no fechamento do marco   
      386 +                                                              
      387 +**Purpose**: o pedido do responsável depois do merge. Com o Sw
          +agger UI público, o contrato muda só em                       
      388 +textos.                                                       
      389 +                                                              
      390 +- [X] T041 Spec 005 primeiro: a FR-027, a emenda da FR-022 e a
          + clarificação do fechamento.                                  
      391 +- [X] T042 Criar `specs/005-deploy-cloud-run/contracts/openapi
          +.yaml` (1.2.1), a partir do 1.2.0 da spec 003:                
      392 +  - o `info.description` com duas frases;                     
      393 +  - a descrição do `bearerAuth` igual a "Opcional. Exigido só 
          +quando a variável API_TOKEN está definida.";                  
      394 +  - o `security` de cada operação sem mudança, porque já tinha
          + o `{}` antes do `bearerAuth`;                                
      395 +  - nenhum `<` ou `>` nos textos.                             
      396 +                                                              
      397 +  Copiar o arquivo, idêntico, para `src/main/resources/static/
          +openapi.yaml`.                                                
      398 +- [X] T043 Testes, com a exceção mínima escolhida pelo respons
          +ável:                                                         
      399 +  - no `ApiDocsIT`, a constante `SPEC_CONTRACT` passa à spec 0
          +05 (ele já tinha exceção);                                    
      400 +  - no `ApiDocsSecurityIT`, só a versão, no literal, no `@Disp
          +layName` e no javadoc, com a linha nova em                    
      401 +    `.github/unchanged-tests-exceptions.txt` e a entrada em `d
          +ocs/ai/decisoes.md`;                                          
      402 +  - IT novo `ContractTextIT`, incluído em `src/test/default-mo
          +de-its.txt`.                                                  
      403 +- [X] T044 README: o contrato 1.2.1 na seção 4, a estrutura e 
          +os requisitos do Marco 5.                                     
      404 +- [X] T045 `./mvnw verify` verde (212 testes, os 3 novos do `C
          +ontractTextIT`), e o `check-unchanged-tests.sh`               
      405 +  verde na `v0.2.0` (59 ITs) e na `v0.3.0` (95 ITs), com 2 exc
          +eções.                                                        
      406 +- [ ] T046 Depois do merge: conferir na URL pública que `/open
          +api.yaml` está na versão 1.2.1, com a descrição               
      407 +  nova do `bearerAuth` e sem `<` ou `>`.                      
      408 +                                                              
      409 +---                                                           
      410 +                                                              
      411  ## Dependencies & Execution Order
      412 
      413  - **Phase 1 → Phase 2 (etapa 1):** o script usa a `cleanup-pol
           icy.json`.

● Bash(f=specs/005-deploy-cloud-run/tasks.md && sed -i -E 's/^- \[ \]
      (T035|T037|T038) /- [X] \1 /' $f && grep -E '^- \[.\] T0(3[5-9]|4[0-6])'
      $f | cut -c1-60)
  ⎿  - [X] T035 [Anderson] Fazer o push do branch `marco/5-deploy
     - [ ] T036 [Anderson] Opcional, nas configurações do repos
     - [X] T037 [Anderson] Fazer o merge do PR na `main`, depois
     … +9 lines (ctrl+o to expand)
  ⎿  Updated specs\005-deploy-cloud-run\tasks.md +3 -3
      346 
      347  **Purpose**: a comprovação pelo pipeline (FR-001 a FR-016 e SC
           -001, SC-002, SC-005 e SC-006)
      348 
      349 -- [ ] T035 [Anderson] Fazer o push do branch `marco/5-deploy` 
          -e abrir o PR. O implementador para aqui e                     
      349 +- [X] T035 [Anderson] Fazer o push do branch `marco/5-deploy` 
          +e abrir o PR. O implementador para aqui e                     
      350    confere o resultado quando o Anderson avisar: os 10 checks v
           erdes, com o `workflow-policy` e o `image`
      351    (passo do digest) comprovando a etapa 2, e o job `deploy` pu
           lado.
      352  - [ ] T036 [Anderson] Opcional, nas configurações do repositór
           io no GitHub: no ambiente `cloud-run` (criado
      353    automaticamente na primeira execução do job, ou antes, à mão
           ), a regra de branch "só `main`" (research R3).
      354 -- [ ] T037 [Anderson] Fazer o merge do PR na `main`, depois da
          - revisão do Codex e das correções aceitas.                    
      355 -- [ ] T038 Conferir a execução da `main`:                     
      354 +- [X] T037 [Anderson] Fazer o merge do PR na `main`, depois da
          + revisão do Codex e das correções aceitas.                    
      355 +- [X] T038 Conferir a execução da `main`:                     
      356    - o job `deploy` verde, com o resumo mostrando o digest, a r
           evisão, as duas conferências e a duração abaixo
      357      de 10 min;
      358    - `verify-deployment.sh --expect-no-metrics <URL pública>` r
           odado localmente (rápido), saindo com código 0,

  Ran 1 shell command (ctrl+o to expand)

● O contrato 1.2.1 e o fechamento da publicação estão commitados no
  marco/5-deploy, prontos para o push. O ./mvnw verify passou com 212 testes, e
  o check-unchanged-tests.sh passou nas duas tags. A conferência do contrato
  novo na URL pública só dá para fazer depois do merge (T046).

  Publicação na main (T038):
  - A execução 45885778155 do merge do PR #14 ficou toda verde. O job deploy
    passou nos 13 passos em 111 s, e a permissão de leitura do projeto que eu
    temia não fez falta.
  - A revisão awards-00002-had recebe 100% do tráfego e roda a mesma imagem do
    registro (sha256:2a6e…bf6), com no mínimo 0 e no máximo 1 instância.
  - A partida a frio levou cerca de 18,5 s, abaixo dos 30 s do SC-001. Tirei
    esse tempo do log do Cloud Run, porque o resumo do job não sai pela API do
    GitHub sem autenticação.
  - Rodei o verify-deployment.sh contra a URL pública e passou: os intervalos
    respondem em 0,14 s e /actuator/prometheus dá 404.
  - A evidência está em
    specs/005-deploy-cloud-run/evidencias/publicacao-main.txt.

  Contrato 1.2.1 (FR-027, com T039):
  - Spec primeiro: a spec 005 passou a Implemented, ganhou a FR-027 e teve a
    FR-022 emendada, porque ela proibia mudar qualquer IT.
  - Novo arquivo-fonte: o contrato fica em
    specs/005-deploy-cloud-run/contracts/openapi.yaml, e o /openapi.yaml
    publicado é idêntico a ele. O 1.2.0 da spec 003 fica como registro.
  - Item 1: o info.description ficou com as duas frases pedidas.
  - Item 2: o security de cada operação já tinha o {} antes do bearerAuth desde
    o 1.2.0. Mantive como estava e troquei só a descrição do esquema pela frase
    que você pediu. O Swagger UI mostra o botão "Authorize" sempre que existe um
    esquema de segurança, mesmo opcional; isso o contrato não consegue evitar.
  - Item 3: o único <token> do contrato estava no info.description e saiu com o
    item 1. Os > que sobraram são só o indicador de bloco do YAML.

  Testes:
  - No ApiDocsIT, mudou só a constante que aponta para o contrato da spec. Ele
    já tinha exceção.
  - No ApiDocsSecurityIT, mudou só a versão (1.2.0 → 1.2.1) e os dois textos que
    a citam. A exceção nova está em .github/unchanged-tests-exceptions.txt e em
    docs/ai/decisoes.md, como você escolheu.
  - O ContractTextIT é novo e roda no modo padrão. Ele confere as duas frases da
    descrição, a descrição do bearerAuth e que nenhum texto do contrato tem <
    ou >.
  - O check-unchanged-tests.sh passa contra a v0.2.0 (59 ITs) e a v0.3.0 (95
    ITs), com 2 exceções.

  Commits:
  - 0fa8420: evidência da publicação (T038).
  - 855e2e2: spec Implemented e contrato 1.2.1. A spec e o contrato estão no
    mesmo commit porque as duas mudanças são no mesmo spec.md.

  Seus passos:
  1. Push do marco/5-deploy e o PR. Avancei o branch até a main por
     fast-forward, como no fechamento do Marco 4.
  2. Depois do merge, eu confiro na URL pública que o /openapi.yaml está na
     1.2.1 (T046).
  3. A exportação da sessão (T040).
  4. Opcional: a regra de branch "só main" no ambiente cloud-run (T036).

  O docs/ai/prompts.md continua fora dos commits, gravado pelo hook.

✻ Cooked for 10m 15s · done 17:17