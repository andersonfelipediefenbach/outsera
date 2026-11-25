 ▐▛███▛█   Claude Code v2.1.327
▝▜██████▀  Opus 5.5 · Claude Pro
 ▝▝   ▝▝   D:\Repo\PESSOAIS\outsera-test\outsera


❯ /clear                                                                        

❯ /speckit-specify Marco 4: acabamento operacional, num único bolt. Nada muda   
  no modo padrão do enunciado (./mvnw, H2, só testes de integração), e cada     
  item tem a comprovação do seu tipo pelo Princípio VI. Escopo:                 
  1. Alertas de SLO no Prometheus do compose (disponibilidade e latência do     
  endpoint de intervalos), com regras versionadas e verificadas no pipeline.    
  2. Teste de carga com k6 contra o compose, com limites de latência e de erro  
  que reprovam o job, rodando no pipeline.                                      
  3. Dados gerados: um gerador de CSV grande e determinístico para o k6 e para  
  medir a carga inicial, sem substituir o movielist.csv padrão.                 
  4. Cache de AOT (Spring Boot e JVM) na imagem, com o tempo de subida medido   
  antes e depois.                                                               
  5. SBOM da imagem e assinatura (cosign keyless no Actions), sem publicação da 
  imagem neste marco.                                                           
  6. Dev container para abrir o projeto pronto (JDK 25, Maven, Docker).         
     Inclua também as pendências do marco 3: o registro da proteção da main     
  (T093), a investigação de por que o job de dependências do Trivy não apontou  
  as transitivas do kafka-clients (com correção) e a execução do testador       
  adversário na ingestão por Kafka. O README ganha a seção do marco 4 entre os  
  extras.                                                                       

● Skill(speckit-specify)
  ⎿  Loaded skill

  Ran 3 shell commands (ctrl+o to expand)

● Vou ler a spec 003 para seguir o estilo e o contexto do marco 3.

  Read 1 file, ran 5 shell commands (ctrl+o to expand)

● Contexto lido (constituição 1.7.0, spec 003, pipeline, Dockerfile, briefing).
  Agora escrevo a spec 004.

  Ran 1 shell command (ctrl+o to expand)

● Write(specs\004-acabamento-operacional\spec.md)
  ⎿  Wrote 479 lines to specs\004-acabamento-operacional\spec.md
      1 # Feature Specification: Marco 4 — Acabamento operacional: alerta
        s de SLO, carga, dados gerados, cache AOT, assinatura e dev conta
        iner
      2 
      3 **Feature Branch**: `marco/4-acabamento`
      4 
      5 **Created**: 2026-11-25
      6 
      7 **Status**: Draft
      8 
     … +471 lines (ctrl+o to expand)

● Spec escrita com dois pontos em aberto. Pergunto antes de fechar o checklist.

● Answered Claude's questions:
  ⎿  · Cache AOT da imagem (spec 004, US4 e FR-020): o processamento AOT do 
     Spring (spring-boot:process-aot) fixa na construção as condições de perfil 
     e de propriedade, e o liga e desliga do Kafka e do PostgreSQL é feito 
     justamente por perfil e propriedade. Qual é o escopo do cache? → Só o cache
     AOT da JVM (Recommended)
     · Assinatura sem publicação (spec 004, US5 e FR-025): o cosign keyless 
     assina normalmente uma imagem num registro. Sem publicar a imagem, o que é 
     assinado e onde a assinatura fica? → Registro temporário no runner 
     (Recommended)

● Atualizo a spec com as duas respostas.

● Update(specs\004-acabamento-operacional\spec.md)
  ⎿  Added 13 lines
       8 
       9  **Input**: User description: "Marco 4: acabamento operacional,
          num único bolt. Nada muda no modo padrão do enunciado (./mvnw,
          H2, só testes de integração), e cada item tem a comprovação do
          seu tipo pelo Princípio VI. Escopo: 1. Alertas de SLO no Promet
          heus do compose (disponibilidade e latência do endpoint de inte
          rvalos), com regras versionadas e verificadas no pipeline. 2. T
          este de carga com k6 contra o compose, com limites de latência
          e de erro que reprovam o job, rodando no pipeline. 3. Dados ger
          ados: um gerador de CSV grande e determinístico para o k6 e par
          a medir a carga inicial, sem substituir o movielist.csv padrão.
           4. Cache de AOT (Spring Boot e JVM) na imagem, com o tempo de
          subida medido antes e depois. 5. SBOM da imagem e assinatura (c
          osign keyless no Actions), sem publicação da imagem neste marco
          . 6. Dev container para abrir o projeto pronto (JDK 25, Maven,
          Docker). Inclua também as pendências do marco 3: o registro da
          proteção da main (T093), a investigação de por que o job de dep
          endências do Trivy não apontou as transitivas do kafka-clients
          (com correção) e a execução do testador adversário na ingestão
          por Kafka. O README ganha a seção do marco 4 entre os extras."
      10 
      11 +## Clarifications                                              
      12 +                                                               
      13 +### Session 2026-11-25                                         
      14 +                                                               
      15 +- Q: O cache AOT inclui o processamento AOT do Spring, que fixa
         + na construção as condições de perfil e de                     
      16 +  propriedade? → A: Não. Só o cache AOT da JVM, gerado por uma 
         +execução de treino da aplicação no build da                    
      17 +  imagem. A escolha de perfil continua na execução, e a mesma i
         +magem serve ao modo padrão e ao compose.                       
      18 +  US4-6, FR-020.                                               
      19 +- Q: Sem publicar a imagem, o que é assinado e onde a assinatur
         +a fica? → A: A imagem vai a um registro                        
      20 +  temporário dentro do job do pipeline, é assinada lá e recebe 
         +o SBOM como atestado assinado. A verificação                   
      21 +  confere a identidade como imagem. O registro é descartado no 
         +fim do job; só a entrada no registro de                        
      22 +  transparência é pública. US5-6, FR-025.                      
      23 +                                                               
      24  ## User Scenarios & Testing *(mandatory)*
      25 
      26  Os atores são:

● Update(specs\004-acabamento-operacional\spec.md)
  ⎿  Added 3 lines, removed 4 lines
      156     sobe, **Then** ela sobe sem o cache, com um aviso no log, e
            continua funcionando.
      157  5. **Given** a máquina sem Docker, **When** `./mvnw verify` e
           `./mvnw spring-boot:run` rodam, **Then** o
      158     comportamento e os testes são os do Marco 3.
      159 -6. **Given** a escolha do cache AOT, **When** o escopo é defin
          -ido, **Then** [NEEDS CLARIFICATION: o cache                   
      160 -   inclui o processamento AOT do Spring (que fixa na construçã
          -o as condições de perfil e de propriedade,                    
      161 -   como o liga e desliga do Kafka e do PostgreSQL), ou só o ca
          -che AOT da JVM, gerado por uma execução de                    
      162 -   treino da aplicação, mantendo a escolha de perfil na execuç
          -ão?]                                                          
      159 +6. **Given** a mesma imagem com o cache, **When** ela sobe com
          + e sem o perfil Docker e com a                                
      160 +   autenticação ligada e desligada, **Then** cada combinação l
          +iga exatamente o que ligava no Marco 3: o                     
      161 +   cache não fixa perfil nem propriedade na construção (Clarif
          +ications, 2026-11-25).                                        
      162 
      163  ---
      164 

● Update(specs\004-acabamento-operacional\spec.md)
  ⎿  Added 3 lines, removed 3 lines
      188     enviada a um registro público.
      189  5. **Given** um pull request vindo de um fork, sem a permissão
            de identidade do workflow, **When** o
      190     pipeline roda, **Then** a assinatura é pulada com o motivo
           no resumo, e os demais jobs rodam.
      191 -6. **Given** a forma de assinar sem publicar, **When** o escop
          -o é definido, **Then** [NEEDS CLARIFICATION:                  
      192 -   o que é assinado e onde a assinatura fica: o arquivo da ima
          -gem e o SBOM como arquivos (sem registro),                    
      193 -   ou a imagem num registro temporário dentro do runner, verif
          -icável como imagem e descartado no fim?]                      
      191 +6. **Given** a imagem num registro temporário dentro do job, *
          +*When** ela é assinada e recebe o SBOM                        
      192 +   como atestado, **Then** a verificação da assinatura e a do 
          +atestado passam como imagem, pelo digest, e                   
      193 +   o registro é descartado no fim do job (Clarifications, 2026
          +-11-25).                                                      
      194 
      195  ---
      196 

● Update(specs\004-acabamento-operacional\spec.md)
  ⎿  Added 4 lines, removed 1 line
      347  **Cache AOT**
      348 
      349  - **FR-020**: A imagem MUST trazer um cache AOT gerado no próp
           rio build da imagem, sem mudar o
      350 -  comportamento da aplicação em nenhum perfil. Escopo do cache
          -: [NEEDS CLARIFICATION: ver US4, cenário 6].                  
      350 +  comportamento da aplicação em nenhum perfil. O cache é só o 
          +da JVM, gerado por uma execução de treino                     
      351 +  da aplicação no build. O processamento AOT do Spring MUST NO
          +T ser usado, porque fixaria na construção as                  
      352 +  condições de perfil e de propriedade que ligam o Kafka, o Po
          +stgreSQL e a autenticação (Clarifications,                    
      353 +  2026-11-25). A mesma imagem MUST servir ao modo padrão e ao 
          +compose.                                                      
      354  - **FR-021**: A imagem com o cache MUST passar no teste de fum
           aça do Marco 2 e no teste do compose do
      355    Marco 3 sem alteração dos passos.
      356  - **FR-022**: O pipeline MUST medir o tempo da subida até a re
           adiness da imagem sem e com o cache, nas

● Update(specs\004-acabamento-operacional\spec.md)
  ⎿  Added 3 lines, removed 1 line
      364    bibliotecas Java com as versões efetivas, e guardá-lo como a
           rtefato da execução.
      365  - **FR-025**: O pipeline MUST assinar sem chave guardada, com
           a identidade do workflow emitida para a
      366    execução, e MUST verificar a assinatura em seguida, aceitand
           o só a identidade deste repositório e deste
      367 -  workflow. O que é assinado: [NEEDS CLARIFICATION: ver US5, c
          -enário 6].                                                    
      367 +  workflow. A imagem MUST ser enviada a um registro temporário
          + dentro do próprio job, assinada pelo                         
      368 +  digest e receber o SBOM como atestado assinado; a verificaçã
          +o MUST conferir a assinatura e o atestado                     
      369 +  como imagem. O registro temporário é descartado no fim do jo
          +b (Clarifications, 2026-11-25).                               
      370  - **FR-026**: Nenhum job deste marco MUST enviar a imagem a um
            registro público. A permissão de identidade
      371    MUST ficar só no job que assina, e a política do workflow (M
           arco 2) MUST continuar reprovando qualquer
      372    publicação.

● Write(specs\004-acabamento-operacional\checklists\requirements.md)
  ⎿  Wrote 56 lines to
     specs\004-acabamento-operacional\checklists\requirements.md
       1 # Specification Quality Checklist: Marco 4 — Acabamento operacio
         nal
       2 
       3 **Purpose**: Validate specification completeness and quality bef
         ore proceeding to planning
       4 **Created**: 2026-11-25
       5 **Feature**: [spec.md](../spec.md)
       6 
       7 ## Content Quality
       8 
     … +48 lines (ctrl+o to expand)

  Ran 1 shell command (ctrl+o to expand)

● A spec do Marco 4 está escrita em specs/004-acabamento-operacional/spec.md, e
  o checklist de qualidade passou em todos os itens. Não fiz commit.

  - Diretório: specs/004-acabamento-operacional/, com .specify/feature.json
    apontando para ele. O branch continua marco/4-acabamento.
  - Histórias: sete, cobrindo tudo o que você pediu:
    - US1 (P1): alertas de SLO, com uma página de SLO e um runbook ligados a
      eles;
    - US2 (P1): teste de carga com k6 que reprova o job;
    - US3 (P2): gerador de CSV determinístico;
    - US4 (P2): cache AOT na imagem, com o tempo de subida medido antes e
      depois;
    - US5 (P2): SBOM e assinatura sem publicar a imagem;
    - US6 (P3): dev container;
    - US7 (P1): as pendências do Marco 3 (T093, Trivy e testador adversário).
  - Requisitos: 38. A FR-038 diz como cada um é comprovado pelo Princípio VI:
    - pela suíte do ./mvnw verify, para o modo padrão e as correções do testador
      adversário;
    - por um estágio do pipeline, para regras, carga, gerador, cache,
      assinatura, dev container e Trivy;
    - por revisão no PR, para SLO, runbook, README, ADRs e registros.

  Suas duas respostas, já na spec:
  - Cache AOT: só o da JVM, gerado por uma execução de treino no build da
    imagem. O AOT do Spring fica proibido, porque fixaria os perfis na
    construção. A mesma imagem serve ao modo padrão e ao compose.
  - Assinatura: a imagem vai a um registro temporário dentro do job, é assinada
    pelo digest e recebe o SBOM como atestado. O registro é descartado no fim;
    só a entrada no registro de transparência é pública.

  O que eu supus sem perguntar (está em Assumptions, dá para mudar no
  /speckit-clarify):
  - Os números do SLO e os limites do k6 saem da medição feita no próprio bolt,
    como pede o briefing. O limite do pipeline pode ser mais folgado que o
    objetivo do SLO.
  - Os alertas só aparecem no Prometheus. Alertmanager e notificações ficam de
    fora.
  - O k6 roda contra o compose completo, com PostgreSQL e Kafka.
  - O arquivo gerado tem 100 mil filmes por padrão.
  - O gerador é uma ferramenta do repositório, sem teste unitário. Ele é
    comprovado pelo pipeline e por subir a aplicação com o arquivo gerado.
  - Para o Trivy, a spec pede investigar a causa e corrigir o job. Ela exige que
    o resumo mostre lz4-java e snappy-java no inventário e que o job fique
    vermelho uma vez contra uma versão vulnerável. Esse teste e o resultado
    ficam registrados em docs/ai/decisoes.md.

  Do seu lado:
  - T093: configurar a proteção da main. A lista de checks obrigatórios agora
    inclui os jobs novos deste marco.
  - Testador adversário: acionar o Codex na ingestão por Kafka (FR-035).
  - Mudança de escopo: incluí a página de SLO e o runbook, que você não pediu de
    forma explícita. O briefing liga cada alerta a um runbook, então os alertas
    ficariam sem destino.

  Próximo passo: /speckit-plan, ou /speckit-clarify se quiser revisar alguma das
  suposições antes.

✻ Worked for 4m 17s · done 12:02

❯ Suposições aceitas, com três ajustes na spec:                                 
  1. O pipeline comprova o determinismo do gerador: gera o arquivo duas vezes   
  com a mesma semente e compara os hashes.                                      
  2. O teste do Trivy contra uma versão vulnerável segue o roteiro da T050 do   
  Marco 2: branch temporário, nunca mergeado, apagado no fim, sem nenhum commit 
  voltando ao branch do marco.                                                  
  3. Deixe explícito que o objetivo do SLO e os limites do k6 vêm da medição    
  num runner compartilhado, e que o limite do pipeline é mais folgado por causa 
  do ruído do runner, para o job não ficar instável. Registre a medição e a     
  folga escolhida em docs/ai/decisoes.md.                                       
     Depois disso, siga para o /speckit-plan.                                   

  Ran 1 shell command (ctrl+o to expand)

● Aplico os três ajustes na spec.

● Update(specs\004-acabamento-operacional\spec.md)
  ⎿  Added 3 lines, removed 2 lines
      341    padrão. Ele é passado pela variável do caminho do CSV, que j
           á existe (spec 001, FR-002).
      342  - **FR-018**: O tempo da carga inicial do arquivo gerado MUST
           ser medido pela carga direta e pela ingestão
      343    por Kafka e registrado no README, com o número de linhas e o
            ambiente da medição.
      344 -- **FR-019**: O pipeline MUST gerar o arquivo duas vezes com o
          -s mesmos parâmetros e reprovar se os resumos                  
      345 -  criptográficos forem diferentes.                            
      344 +- **FR-019**: O pipeline MUST comprovar o determinismo do gera
          +dor: gera o arquivo duas vezes, com o mesmo                   
      345 +  número de filmes e a mesma semente, calcula o hash de cada u
          +m e reprova se forem diferentes. Os dois                      
      346 +  hashes MUST aparecer no resumo da execução.                 
      347 
      348  **Cache AOT**
      349 

● Update(specs\004-acabamento-operacional\spec.md)
  ⎿  Added 12 lines, removed 2 lines
      393    transitivas e as versões efetivas depois da gestão de versõe
           s do build. O resumo do job MUST mostrar que
      394    `lz4-java` e `snappy-java` estão no inventário.
      395  - **FR-034**: A correção da FR-033 MUST ser comprovada uma vez
            contra uma versão vulnerável conhecida (por
      396 -  exemplo, `lz4-java` 1.11.2), com o job vermelho, e o resulta
          -do registrado em `docs/ai/decisoes.md`. A                     
      397 -  versão vulnerável MUST NOT ficar no `main`.                 
      396 +  exemplo, `lz4-java` 1.11.2), com o job de dependências verme
          +lho. A verificação segue o roteiro da T050 do                 
      397 +  Marco 2 (spec 002):                                         
      398 +  - é feita num branch temporário criado a partir do branch do
          + marco, que **nunca é mergeado** e é                          
      399 +    **apagado** no fim, local e remotamente;                  
      400 +  - nenhum commit desse branch volta ao branch do marco: nada 
          +de cherry-pick, merge ou rebase a partir                      
      401 +    dele, e a versão vulnerável não fica no código do marco ne
          +m na `main`;                                                  
      402 +  - o push do branch temporário é feito pelo Anderson ou com a
          + autorização dele;                                            
      403 +  - depois de apagar o branch, `git branch -a` e o log do bran
          +ch do marco confirmam que nenhum commit dele                  
      404 +    ficou.                                                    
      405 +                                                              
      406 +  O registro em `docs/ai/decisoes.md` (nome do branch, link da
          + execução, job vermelho, resultado esperado e                 
      407 +  obtido, confirmação de que o branch foi apagado) é a única c
          +oisa da verificação que entra no marco.                       
      408  - **FR-035**: O testador adversário (Codex) MUST ser acionado
           pelo Anderson para a ingestão por Kafka (T050
      409    da spec 003), criando eventos de ataque a partir da spec 003
            e do contrato dos eventos, sem ler o código
      410    de produção. Cada falha encontrada MUST virar teste de integ
           ração permanente no `./mvnw verify`, com o
● Update(specs\004-acabamento-operacional\spec.md)
  ⎿  Added 3 lines, removed 2 lines
      245     dependências transitivas de runtime com as versões efetivas
           , como `lz4-java` e `snappy-java` trazidas
      246     pelo `kafka-clients`.
      247  4. **Given** uma versão de dependência transitiva com vulnerab
           ilidade alta ou crítica e versão corrigida
      248 -   (por exemplo, `lz4-java` 1.11.2), **When** o job de dependê
          -ncias roda contra ela, numa verificação feita                 
      249 -   uma vez e registrada, **Then** o job fica vermelho.        
      248 +   (por exemplo, `lz4-java` 1.11.2), num branch temporário que
          + nunca é mergeado e é apagado no fim (roteiro                 
      249 +   da T050 do Marco 2), **When** o job de dependências roda co
          +ntra ela, **Then** o job fica vermelho, e                     
      250 +   nenhum commit do branch temporário volta ao branch do marco
          +.                                                             
      251  5. **Given** o testador adversário (Codex) acionado pelo Ander
           son, **When** ele cria eventos de ataque a
      252     partir da spec 003 e do contrato dos eventos, sem ler o cód
           igo de produção, **Then** cada falha
      253     encontrada vira teste de integração permanente no `./mvnw v
           erify`, com a correção, e o resultado (com

● Update(specs\004-acabamento-operacional\spec.md)
  ⎿  Added 5 lines
      323  - **FR-011**: O teste de carga MUST ter limites de latência e
           de taxa de erro. Passar de qualquer limite
      324    MUST encerrar o teste com falha e deixar o job vermelho. Os
           limites MUST estar versionados junto do teste
      325    e explicados na página do SLO.
      326 +- **FR-011a**: O objetivo do SLO e os limites do teste de carg
          +a MUST vir da medição do teste de carga num                   
      327 +  runner compartilhado do GitHub Actions, o mesmo tipo de máqu
          +ina em que o job roda. Os limites do pipeline                 
      328 +  MUST ser mais folgados que o objetivo do SLO, com uma folga 
          +escolhida para absorver o ruído do runner                     
      329 +  compartilhado e o job não ficar instável. A medição (número 
          +de execuções, valores de cada uma e                           
      330 +  dispersão) e a folga escolhida, com o motivo, MUST ser regis
          +tradas em `docs/ai/decisoes.md`.                              
      331  - **FR-012**: O teste de carga MUST conferir o formato das res
           postas dos intervalos (campos exatos do
      332    enunciado) e contar como erro qualquer resposta fora do form
           ato ou com status diferente do esperado.
      333  - **FR-013**: O pipeline MUST rodar o teste de carga num job p
           róprio, que sobe o compose com o arquivo

● Update(specs\004-acabamento-operacional\spec.md)
  ⎿  Added 4 lines, removed 3 lines
      483 
      484  - O modo padrão continua sendo o avaliado (Princípio II). Nada
            deste marco é exigido para subir ou testar
      485    com `./mvnw`.
      486 -- Os valores dos objetivos do SLO são definidos depois da prim
          -eira medição do teste de carga, como pede o                   
      487 -  briefing. O limite do teste de carga no pipeline pode ser ma
          -is folgado que o objetivo do SLO, por causa                   
      488 -  da variação do runner; a página do SLO explica a diferença. 
      486 +- Os valores dos objetivos do SLO são definidos depois da medi
          +ção do teste de carga num runner                              
      487 +  compartilhado, como pede o briefing. O limite do teste de ca
          +rga no pipeline é mais folgado que o objetivo                 
      488 +  do SLO, por causa do ruído do runner compartilhado, para o j
          +ob não ficar instável (FR-011a). A página do                  
      489 +  SLO explica a diferença, e `docs/ai/decisoes.md` registra a 
          +medição e a folga.                                            
      490  - Os alertas são avaliados e exibidos pelo Prometheus do compo
           se. Notificação (Alertmanager, e-mail,
      491    chat) fica fora do escopo.
      492  - O teste de carga roda contra o compose completo (PostgreSQL
           e Kafka), que é o ambiente operacional do

● Update(specs\004-acabamento-operacional\spec.md)
  ⎿  Added 3 lines, removed 2 lines
      436    - correções do testador adversário (FR-035): teste de integr
           ação no `./mvnw verify`;
      437    - regras de alerta, teste de carga, gerador, cache AOT, SBOM
           , assinatura, dev container e job de
      438      dependências (FR-008 a FR-016, FR-019 a FR-030, FR-033): e
           stágio do pipeline, indicado no plano;
      439 -  - SLO, runbook, README, ADRs e registros em `docs/ai/decisoe
          -s.md` (FR-003, FR-007, FR-018, FR-031,                        
      440 -    FR-032, FR-034, FR-036, FR-037): revisão no pull request, 
          -com seção própria no checklist da spec.                       
      439 +  - SLO, runbook, README, ADRs e registros em `docs/ai/decisoe
          +s.md` (FR-003, FR-007, FR-011a, FR-018,                       
      440 +    FR-031, FR-032, FR-034, FR-036, FR-037): revisão no pull r
          +equest, com seção própria no checklist da                     
      441 +    spec.                                                     
      442 
      443  ### Key Entities
      444 

● Update(specs\004-acabamento-operacional\checklists\requirements.md)
  ⎿  Added 3 lines, removed 1 line
      43  - [ ] ADRs (FR-037): um por decisão do marco, no formato dos an
          teriores.
      44  - [ ] `docs/ai/decisoes.md`: proteção da `main` com a lista de
          checks e o bypass desligado (FR-031); causa
      45    da lacuna do job de dependências, com a evidência (FR-032); v
          erificação única contra a versão vulnerável
      46 -  (FR-034); resultado do testador adversário (FR-035). Sem dado
         - pessoal.                                                      
      46 +  (FR-034), feita em branch temporário apagado, sem commit de v
         +olta ao marco; resultado do testador                           
      47 +  adversário (FR-035); medição do k6 no runner compartilhado e 
         +folga escolhida para os limites do pipeline                    
      48 +  (FR-011a). Sem dado pessoal.                                 
      49 
      50  ## Notes
      51 

● Ajustes feitos (FR-019, FR-034 e US7-4, FR-011a nova e Assumptions). Agora o
  /speckit-plan.

● Skill(speckit-plan)
  ⎿  Loaded skill

  Ran 5 shell commands (ctrl+o to expand)

● Monto o plano: falta conferir a árvore de pastas do briefing e o script de
  fumaça.

  Ran 2 shell commands (ctrl+o to expand)

● Agora escrevo a research (decisões técnicas R1 a R14).

● Write(specs\004-acabamento-operacional\research.md)
  ⎿  Wrote 297 lines to specs\004-acabamento-operacional\research.md
      1 # Research: Marco 4 — Acabamento operacional
      2 
      3 **Spec**: [spec.md](spec.md) | **Plano**: [plan.md](plan.md) | **
        Data**: 2026-11-25
      4 
      5 Cada item traz a decisão, o motivo e as alternativas. Os pontos q
        ue dependem do comportamento exato de
      6 uma versão estão marcados como **verificar**, com o que fazer se
        o resultado for outro. Nenhum item muda
      7 o código da API em `src/main`, o `pom.xml` ou os `application*.ym
     … +291 lines (~21 KB) (ctrl+o to expand)

  Ran 1 shell command (ctrl+o to expand)

● Research pronta. Agora data-model, contratos e quickstart.

● Write(specs\004-acabamento-operacional\data-model.md)
  ⎿  Wrote 84 lines to specs\004-acabamento-operacional\data-model.md
      1 # Data Model: Marco 4 — Acabamento operacional
      2 
      3 **Spec**: [spec.md](spec.md) | **Research**: [research.md](resear
        ch.md)
      4 
      5 O marco não muda o esquema do banco, as entidades JPA nem o contr
        ato da API. As "entidades" abaixo são
      6 artefatos versionados ou gerados pelo pipeline, com os campos e a
        s regras que os testes e as revisões
      7 conferem.
     … +77 lines (ctrl+o to expand)

● Write(specs\004-acabamento-operacional\contracts\ci-pipeline.md)
  ⎿  Wrote 41 lines to
     specs\004-acabamento-operacional\contracts\ci-pipeline.md
       1 # Contrato: pipeline do Marco 4
       2 
       3 Complementa `specs/003-modo-docker/contracts/ci-pipeline.md`. Os
          jobs do Marco 3 continuam, com as
       4 mudanças abaixo. Toda action nova é fixada por hash de commit, c
         om as referências aninhadas conferidas
       5 (spec 002, R10). Toda imagem nova é fixada por tag e digest.
       6 
       7 ## Jobs novos
       8 
     … +33 lines (ctrl+o to expand)

● Write(specs\004-acabamento-operacional\contracts\operacao.md)
  ⎿  Wrote 77 lines to
     specs\004-acabamento-operacional\contracts\operacao.md
      1 # Contrato: artefatos de operação do Marco 4
      2 
      3 Interfaces que o operador, o mantenedor e o colaborador usam dire
        tamente.
      4 
      5 ## Gerador de CSV
      6 
      7 ```text
      8 java tools/csv-generator/GenerateMovies.java [--movies N] [--seed
         S] --output ARQUIVO
     … +69 lines (ctrl+o to expand)

● Write(specs\004-acabamento-operacional\quickstart.md)
  ⎿  Wrote 106 lines to specs\004-acabamento-operacional\quickstart.md
       1 # Quickstart: validação do Marco 4
       2 
       3 Roteiro para conferir o marco de ponta a ponta. Os detalhes de c
         ada interface estão em
       4 [contracts/operacao.md](contracts/operacao.md) e [contracts/ci-p
         ipeline.md](contracts/ci-pipeline.md).
       5 
       6 ## 1. Modo padrão, só com o JDK (FR-001, SC-001)
       7 
       8 ```bash
     … +98 lines (ctrl+o to expand)

● Por fim, o plan.md.

● Write(specs\004-acabamento-operacional\plan.md)
  ⎿  Added 204 lines, removed 71 lines
        1 -# Implementation Plan: [FEATURE]                              
        1 +# Implementation Plan: Marco 4 — Acabamento operacional: alert
          +as de SLO, carga, dados gerados, cache AOT, assinatura e dev c
          +ontainer                                                      
        2 
        3 -**Branch**: `[###-feature-name]` | **Date**: [DATE] | **Spec**
          -: [link]                                                      
        3 +**Branch**: `marco/4-acabamento` | **Date**: 2026-11-25 | **Sp
          +ec**: [spec.md](spec.md)                                      
        4 
        5 -**Input**: Feature specification from `/specs/[###-feature-nam
          -e]/spec.md`                                                   
        5 +**Input**: Feature specification from `/specs/004-acabamento-o
          +peracional/spec.md`                                           
        6 
        7 -**Note**: This template is filled in by the `/speckit-plan` co
          -mmand; its definition describes the execution workflow.       
        8 -                                                              
        7  ## Summary
        8 
       11 -[Extract from feature spec: primary requirement + technical ap
          -proach from research]                                         
        9 +O Marco 4 fecha a operação e a cadeia de entrega sem tocar no 
          +modo padrão: nenhuma mudança em `src/main`,                   
       10 +no `pom.xml` ou nos `application*.yml`, salvo as correções que
          + o testador adversário exigir (R13).                          
       11 
       12 +- **SLO e alertas:** os dois SLOs do endpoint de intervalos us
          +am a métrica que já existe desde o Marco 2.                   
       13 +  A latência usa um bucket existente do histograma (R1). Os al
          +ertas medem a taxa de consumo do orçamento em                 
       14 +  várias janelas, mais o `AwardsAppDown`, cada um com o link d
          +o runbook (R2). As regras ficam em                            
       15 +  `observability/prometheus/rules/`, montadas no Prometheus do
          + compose. O `promtool` da mesma imagem                        
       16 +  verifica e testa as regras no job `alert-rules`, e o `compos
          +e-test.sh` confere que elas carregaram e não                  
       17 +  disparam (R3).                                              
       18 +- **Teste de carga:** k6 como serviço `k6` do compose no perfi
          +l `load`, com limites por endpoint e uma                      
       19 +  métrica de erros de contrato (R4). Os limites e o SLO vêm de
          + ao menos 5 execuções no runner compartilhado,                
       20 +  com folga proposta de 2× nos limites do pipeline, registrada
          + em `docs/ai/decisoes.md` (R5, FR-011a).                      
       21 +- **Dados gerados:** `tools/csv-generator/GenerateMovies.java`
          +, de arquivo único, roda com o `java` do JDK e                
       22 +  usa `java.util.Random(seed)`. A saída é UTF-8 com `\n`, escr
          +ita de forma atômica, com os empates e o                      
       23 +  intervalo 0 plantados (R6). O job `load` gera o arquivo duas
          + vezes e compara os SHA-256. Ele também mede                  
       24 +  a carga direta e a carga por Kafka com 100 mil filmes (R7). 
       25 +- **Cache AOT:** só o da JVM 25 (clarificação). O `extract` de
          +ixa de usar o `--launcher` (classpath de                      
       26 +  JARs), o treino roda no estágio final com `spring.context.ex
          +it=onRefresh` e o `ENTRYPOINT` passa a usar                   
       27 +  `-XX:AOTCache`. O `startup-benchmark.sh` mede a mesma imagem
          + com `AOTMode=off` e com o cache, em 7                        
       28 +  repetições, e publica a mediana (R8).                       
       29 +- **SBOM e assinatura:** o SBOM CycloneDX vem do Trivy (R9). O
          + job `supply-chain` usa um registro                           
       30 +  temporário em `localhost:5000`, faz o `cosign sign` e o `att
          +est` keyless e verifica com a identidade certa                
       31 +  e com uma errada. Só ele tem `id-token: write` e só ele faz 
          +push, e só para `localhost:5000/`. A política                 
       32 +  do workflow é ampliada para isso, com casos que precisam fal
          +har (R10).                                                    
       33 +- **Dev container:** imagem de Java 25 fixada por digest, com 
          +docker-in-docker, o `./mvnw` e as portas                      
       34 +  encaminhadas. O job `devcontainer` roda o `verify` dentro de
          +le, e o Dependabot ganha o ecossistema                        
       35 +  `devcontainers` (R11).                                      
       36 +- **Pendências do Marco 3:**                                  
       37 +  - o job `dependencies` passa a analisar as dependências de r
          +untime copiadas pelo Maven, e não o                           
       38 +    `pom.xml`. A causa da lacuna é investigada e registrada, e
          + a correção é comprovada num branch                           
       39 +    temporário apagado (R12);                                 
       40 +  - o testador adversário roda na ingestão por Kafka (R13);   
       41 +  - a proteção da `main` passa a ter 12 checks e é registrada 
          +(R14).                                                        
       42 +- **Entrega:** um bolt e um PR, em seis etapas:               
       43 +  1. pendências de código (testador adversário e Trivy);      
       44 +  2. gerador e teste de carga;                                
       45 +  3. SLO e alertas, com os números da etapa 2;                
       46 +  4. cache AOT;                                               
       47 +  5. SBOM e assinatura;                                       
       48 +  6. dev container, ADRs, README e registros.                 
       49 +                                                              
       50  ## Technical Context
       51 
       15 -<!--                                                          
       16 -  ACTION REQUIRED: Replace the content in this section with th
          -e technical details                                           
       17 -  for the project. The structure here is presented in advisory
          - capacity to guide                                            
       18 -  the iteration process.                                      
       19 --->                                                           
       52 +**Language/Version**: Java 25 (OpenJDK 25.0.2) na API e no ger
          +ador; JavaScript (ES2015+, runtime do k6) no                  
       53 +roteiro de carga; bash nos scripts do pipeline; YAML do Promet
          +heus                                                          
       54 
       21 -**Language/Version**: [e.g., Python 3.11, Swift 5.9, Rust 1.75
          - or NEEDS CLARIFICATION]                                      
       55 +**Primary Dependencies**: sem dependência nova no `pom.xml`. F
          +erramentas novas, todas fixadas:                              
       56 +- `grafana/k6` (imagem, tag e digest, serviço do compose);    
       57 +- `registry:3` (imagem, digest, *service container* do `supply
          +-chain`);                                                     
       58 +- `sigstore/cosign-installer` e `devcontainers/ci` (actions po
          +r hash);                                                      
       59 +- `mcr.microsoft.com/devcontainers/java` (digest) e o recurso 
          +`docker-in-docker` (versão exata);                            
       60 +- `promtool` da imagem do Prometheus que já está no compose;  
       61 +- Trivy, pela action que já está no pipeline, também para o SB
          +OM                                                            
       62 
       23 -**Primary Dependencies**: [e.g., FastAPI, UIKit, LLVM or NEEDS
          - CLARIFICATION]                                               
       63 +**Storage**: sem mudança (H2 no modo padrão; PostgreSQL no com
          +pose). O CSV gerado fica em                                   
       64 +`target/generated-data/`, fora do controle de versão          
       65 
       25 -**Storage**: [if applicable, e.g., PostgreSQL, CoreData, files
          - or N/A]                                                      
       66 +**Testing**:                                                  
       67 +- `./mvnw verify` sem mudança, mais os ITs do testador adversá
          +rio, se houver falhas;                                        
       68 +- `promtool test rules` para os alertas;                      
       69 +- k6 com limites para a carga;                                
       70 +- scripts bash no pipeline para o gerador, a subida, a assinat
          +ura e o dev container;                                        
       71 +- sem teste unitário na API                                   
       72 
       27 -**Testing**: [e.g., pytest, XCTest, cargo test or NEEDS CLARIF
          -ICATION]                                                      
       73 +**Target Platform**: runners `ubuntu-latest` (compartilhados);
          + Docker Compose v2; JVM 25 em qualquer                        
       74 +sistema operacional para o `./mvnw` e o gerador; dev container
          + em VS Code ou no `devcontainer` CLI                          
       75 
       29 -**Target Platform**: [e.g., Linux server, iOS 15+, WASM or NEE
          -DS CLARIFICATION]                                             
       76 +**Project Type**: web-service (API REST, módulo Maven único), 
          +mais infraestrutura de observabilidade,                       
       77 +cadeia de entrega e ferramentas do repositório                
       78 
       31 -**Project Type**: [e.g., library/cli/web-service/mobile-app/co
          -mpiler/desktop-app or NEEDS CLARIFICATION]                    
       79 +**Performance Goals**:                                        
       80 +- objetivos do SLO e limites do k6 definidos pela medição (R5)
          +;                                                             
       81 +- subida com cache mais rápida que sem ele, pela mediana (SC-0
          +07);                                                          
       82 +- carga de 100 mil filmes sem linha rejeitada nos dois caminho
          +s (SC-006)                                                    
       83 
       33 -**Performance Goals**: [domain-specific, e.g., 1000 req/s, 10k
          - lines/sec, 60 fps or NEEDS CLARIFICATION]                    
       84 +**Constraints**:                                              
       85 +- `./mvnw` sem Docker (Princípio II);                         
       86 +- contrato de intervalos idêntico (Princípio III);            
       87 +- os ITs dos Marcos 1 a 3 sem alteração (`check-unchanged-test
          +s.sh` contra `v0.2.0` e `v0.3.0`);                            
       88 +- nenhuma publicação de imagem;                               
       89 +- actions por hash e imagens por digest;                      
       90 +- nenhum segredo (a assinatura é keyless)                     
       91 
       35 -**Constraints**: [domain-specific, e.g., <200ms p95, <100MB me
          -mory, offline-capable or NEEDS CLARIFICATION]                 
       92 +**Scale/Scope**: 3 alertas e 2 SLOs; 1 roteiro de carga com 2 
          +cenários; 1 gerador; 4 jobs novos e 4                         
       93 +alterados; 1 dev container; 6 ADRs novos                      
       94 
       37 -**Scale/Scope**: [domain-specific, e.g., 10k users, 1M LOC, 50
          - screens or NEEDS CLARIFICATION]                              
       95 +Nenhum item ficou como NEEDS CLARIFICATION. As duas escolhas d
          +e escopo foram clarificadas na spec                           
       96 +(2026-11-25). Os pontos que dependem do comportamento exato da
          +s versões estão marcados como **verificar**                   
       97 +na [research.md](research.md) (R3, R5, R7, R8, R10 e R11), cad
          +a um com o que fazer se o resultado for                       
       98 +outro.                                                        
       99 
      100  ## Constitution Check
      101 
      102  *GATE: Must pass before Phase 0 research. Re-check after Phase
            1 design.*
      103 
       43 -[Gates determined based on constitution file]                 
      104 +| Princípio | Como o plano atende | Pré-design | Pós-design | 
      105 +| --- | --- | --- | --- |                                     
      106 +| I. Somente testes de integração | Nenhum teste unitário novo
          + na API. O gerador é ferramenta do repositório, fora do back-e
          +nd, comprovado pelo pipeline e pela carga real (spec, Assumpti
          +ons). As falhas do testador adversário viram ITs com o broker 
          +embutido, sem mocks, verificados por HTTP (R13) | ✅ | ✅ |   
      107 +| II. Execução só com `./mvnw` | Nada em `src/main`, `pom.xml`
          + ou `application*.yml` muda para os extras. O k6, o `promtool`
          +, o cosign e o dev container rodam só no compose, na imagem ou
          + no pipeline. O gerador precisa só do JDK | ✅ | ✅ |         
      108 +| III. Contrato idêntico | O contrato não muda. O k6 confere o
          + formato exato em toda resposta (FR-012). A imagem com o cache
          + passa no mesmo teste de fumaça, com o resultado de referência
          + | ✅ | ✅ |                                                  
      109 +| IV. Spec-anchored | Duas clarificações e três ajustes do res
          +ponsável entraram na spec antes do plano. Correção do testador
          + que mude comportamento atualiza a spec 003 antes do código (R
          +13) | ✅ | ✅ |                                               
      110 +| V. Extras isolados, simplicidade | k6 no perfil `load` do co
          +mpose; o Alertmanager fica de fora; o cache é só da JVM (sem A
          +OT do Spring, que fixaria perfis); a assinatura fica sem publi
          +cação; o SBOM sai da ferramenta que já existe; nenhum plugin n
          +ovo no `pom.xml` | ✅ | ✅ |                                  
      111 +| VI. Rastreabilidade (1.7.0) | Matriz abaixo. Modo padrão: su
          +íte existente. Correções do adversário: IT no `./mvnw verify`.
          + Regras, carga, gerador, imagem, assinatura, dev container e T
          +rivy: jobs. SLO, runbook, README, ADRs e registros: checklist 
          +da spec | ✅ | ✅ |                                           
      112 +| VII. Sem segredos | Nenhum `secrets.*`: a assinatura usa o t
          +oken OIDC da execução. O k6 usa o token de demonstração só qua
          +ndo `API_TOKEN` é definido. O dev container não define credenc
          +iais. Nenhum dado pessoal nos registros | ✅ | ✅ |           
      113 +| Observabilidade (1.3.0) | Readiness e log sem mudança. O avi
          +so da JVM com cache inválido só aparece nesse caso (R8, verifi
          +car) | ✅ | ✅ |                                              
      114 +| Cadeia de entrega | Actions novas por hash, com as referênci
          +as aninhadas conferidas; imagens novas por digest; imagem mult
          +i-stage com usuário não root mantida. A política só passa a pe
          +rmitir `id-token: write` e push para `localhost:5000/`, e só n
          +o `supply-chain`, com casos negativos (R10) | ✅ | ✅ |       
      115 
      116 +**Resultado:** nenhuma violação. A ampliação da política do wo
          +rkflow é uma mudança do `check-workflows.sh`                  
      117 +(Marco 2) e não da constituição. A regra "as actions fixadas p
          +or hash, a imagem multi-stage e não root" não                 
      118 +muda. O "Complexity Tracking" não se aplica.                  
      119 +                                                              
      120 +## Rastreabilidade (Princípio VI)                             
      121 +                                                              
      122 +| Requisito | Comprovação |                                   
      123 +| --- | --- |                                                 
      124 +| FR-001, FR-002, SC-001 | job `build` (`./mvnw verify`, suíte
          + sem mudança); `workflow-policy` (`check-unchanged-tests.sh` c
          +ontra `v0.2.0` e `v0.3.0`) |                                  
      125 +| FR-003, FR-007, FR-011a | checklist "Revisão da documentação
          +" (SLO, runbook e registro da medição e da folga) |           
      126 +| FR-004, FR-006, FR-008, SC-002 | job `alert-rules` |        
      127 +| FR-005 | `slo.test.yml` (5xx ruim; 2xx, 304 e 4xx bons; alvo
          + fora) no job `alert-rules` |                                 
      128 +| FR-009, SC-003 | passo novo do `compose-test.sh`, job `compo
          +se` |                                                         
      129 +| FR-010 a FR-013, SC-004 | job `load` (k6 com limites); verif
          +icação única com limite impossível, registrada |              
      130 +| FR-014 a FR-016, FR-019, SC-005, SC-006 | job `load` (dois S
          +HA-256 iguais; carga sem linhas rejeitadas nos dois caminhos) 
          +|                                                             
      131 +| FR-017 | job `build` (suíte sem mudança com o `movielist.csv
          +`); `check-docker-context.sh` (o gerado fora da imagem) |     
      132 +| FR-018 | job `load` (tempos no resumo); checklist (números n
          +o README) |                                                   
      133 +| FR-020, FR-021, FR-023 | job `image` (fumaça) e job `compose
          +` (perfil docker com a mesma imagem) |                        
      134 +| FR-022, SC-007 | job `image` (`startup-benchmark.sh`); check
          +list (números no README) |                                    
      135 +| FR-024 a FR-027, SC-008 | job `supply-chain`; `workflow-poli
          +cy` (política ampliada, com os casos negativos testados) |    
      136 +| FR-028 a FR-030, SC-009 | job `devcontainer`; `workflow-poli
          +cy` (Dependabot `devcontainers`) |                            
      137 +| FR-031, SC-011 | checklist (registro da proteção) e página d
          +o PR com os 12 checks obrigatórios |                          
      138 +| FR-032, FR-034 | checklist (causa, evidência e verificação ú
          +nica em branch apagado) |                                     
      139 +| FR-033, SC-010 | job `dependencies` (inventário com `lz4-jav
          +a` e `snappy-java` no resumo) |                               
      140 +| FR-035 | ITs `S004` em `kafka/adversario/` no job `build`; c
          +hecklist (registro do resultado) |                            
      141 +| FR-036, FR-037 | checklist "Revisão da documentação" |      
      142 +                                                              
      143  ## Project Structure
      144 
      145  ### Documentation (this feature)
      146 
      147  ```text
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
      148 +specs/004-acabamento-operacional/                             
      149 +├── plan.md                 # Este arquivo                    
      150 +├── research.md             # Fase 0 (R1 a R14)               
      151 +├── data-model.md           # Fase 1                          
      152 +├── quickstart.md           # Fase 1                          
      153 +├── contracts/                                                
      154 +│   ├── ci-pipeline.md      # jobs novos e alterados, checks o
          +brigatórios                                                   
      155 +│   └── operacao.md         # gerador, compose, alertas, image
          +m, assinatura, dev container                                  
      156 +├── checklists/                                               
      157 +│   └── requirements.md     # inclui "Revisão da documentação"
      158 +└── tasks.md                # Fase 2 (/speckit-tasks)         
      159  ```
      160 
      161  ### Source Code (repository root)
       60 -<!--                                                          
       61 -  ACTION REQUIRED: Replace the placeholder tree below with the
          - concrete layout                                              
       62 -  for this feature. Delete unused options and expand the chose
          -n structure with                                              
       63 -  real paths (e.g., apps/admin, packages/something). The deliv
          -ered plan must                                                
       64 -  not include Option labels.                                  
       65 --->                                                           
      162 
      163 +Só o que muda ou é novo.                                      
      164 +                                                              
      165  ```text
       68 -# [REMOVE IF UNUSED] Option 1: Single project (DEFAULT)       
       69 -src/                                                          
       70 -├── models/                                                   
       71 -├── services/                                                 
       72 -├── cli/                                                      
       73 -└── lib/                                                      
      166 +.devcontainer/                                                
      167 +└── devcontainer.json                      # novo (R11)       
      168 +.github/                                                      
      169 +├── dependabot.yml                         # + devcontainers  
      170 +├── workflows/ci.yml                       # + alert-rules, lo
          +ad, supply-chain, devcontainer; dependencies, image, workflow-
          +policy alterados                                              
      171 +└── scripts/                                                  
      172 +    ├── check-workflows.sh                 # regras 3, 5 e 6 a
          +mpliadas (R10, R11)                                           
      173 +    ├── compose-test.sh                    # + passo dos alert
          +as (R3)                                                       
      174 +    ├── load-test.sh                       # novo (R4 a R7)   
      175 +    └── startup-benchmark.sh               # novo (R8)        
      176 +Dockerfile                                 # extract sem --lau
          +ncher, treino AOT, ENTRYPOINT (R8)                            
      177 +compose.yaml                               # + volume das regr
          +as no prometheus; + serviço k6 (perfil load)                  
      178 +compose.load.yaml                          # novo: CSV gerado 
          +montado no app                                                
      179 +load/                                                         
      180 +└── intervals.js                           # novo (R4)        
      181 +observability/prometheus/                                     
      182 +├── prometheus.yml                         # + rule_files     
      183 +└── rules/                                                    
      184 +    ├── slo.yml                            # novo (R1, R2)    
      185 +    └── slo.test.yml                       # novo (R3)        
      186 +tools/csv-generator/                                          
      187 +└── GenerateMovies.java                    # novo (R6)        
      188 +docs/                                                         
      189 +├── slo.md                                 # novo (papel de do
          +cumentação)                                                   
      190 +├── runbook.md                             # novo (papel de do
          +cumentação)                                                   
      191 +├── adr/0020 a 0025-*.md                   # novos (papel de d
          +ocumentação)                                                  
      192 +└── ai/decisoes.md                         # registros de R5, 
          +R12, R13, R14                                                 
      193 +README.md                                  # seção do Marco 4 
          +(papel de documentação)                                       
      194 +src/test/java/com/outsera/awards/kafka/adversario/   # só se o
          + testador adversário achar falhas (R13)                       
      195 +```                                                           
      196 
       75 -tests/                                                        
       76 -├── contract/                                                 
       77 -├── integration/                                              
       78 -└── unit/                                                     
      197 +ADRs propostos:                                               
      198 +- 0020: SLO e alertas por taxa de consumo do orçamento;       
      199 +- 0021: teste de carga com k6 no pipeline, com a folga sobre o
          + runner compartilhado;                                        
      200 +- 0022: gerador de CSV determinístico;                        
      201 +- 0023: cache AOT da JVM, sem o AOT do Spring;                
      202 +- 0024: SBOM e assinatura keyless num registro temporário;    
      203 +- 0025: dev container e inventário efetivo no job de dependênc
          +ias.                                                          
      204 
       80 -# [REMOVE IF UNUSED] Option 2: Web application (when "frontend
          -" + "backend" detected)                                       
       81 -backend/                                                      
       82 -├── src/                                                      
       83 -│   ├── models/                                               
       84 -│   ├── services/                                             
       85 -│   └── api/                                                  
       86 -└── tests/                                                    
      205 +**Structure Decision**: módulo Maven único, sem mudança. As fe
          +rramentas ficam em pastas próprias na raiz                    
      206 +(`tools/`, `load/`, `.devcontainer/`), fora do build do Maven 
          +e do contexto da imagem. O `.dockerignore`                    
      207 +é uma lista de permissão e já as exclui.                      
      208 
       88 -frontend/                                                     
       89 -├── src/                                                      
       90 -│   ├── components/                                           
       91 -│   ├── pages/                                                
       92 -│   └── services/                                             
       93 -└── tests/                                                    
      209 +**Propriedade dos arquivos (AGENTS.md):**                     
      210 +- **Implementador:** as correções do testador adversário (`src
          +/`).                                                          
      211 +- **Infraestrutura e SRE:** `Dockerfile`, `compose*.yaml`, `ob
          +servability/`, `.github/`, `.devcontainer/`,                  
      212 +  `load/` e `tools/`.                                         
      213 +- **Documentação:** `docs/slo.md`, `docs/runbook.md`, `docs/ad
          +r/` e `README.md`.                                            
      214 
       95 -# [REMOVE IF UNUSED] Option 3: Mobile + API (when "iOS/Android
          -" detected)                                                   
       96 -api/                                                          
       97 -└── [same as backend above]                                   
      215 +Os registros em `docs/ai/decisoes.md` são de quem fez cada ver
          +ificação.                                                     
      216 
       99 -ios/ or android/                                              
      100 -└── [platform-specific structure: feature modules, UI flows, p
          -latform tests]                                                
      101 -```                                                           
      217 +## Ordem das etapas                                           
      218 
      103 -**Structure Decision**: [Document the selected structure and r
          -eference the real                                             
      104 -directories captured above]                                   
      219 +1. **Pendências de código:**                                  
      220 +   - pedido ao Anderson para acionar o testador adversário (R1
          +3);                                                           
      221 +   - investigação e correção do job `dependencies` (R12);     
      222 +   - pedido da verificação única em branch temporário.        
      223 +2. **Gerador e carga:**                                       
      224 +   - gerador e `compose.load.yaml`;                           
      225 +   - roteiro do k6 com limites provisórios;                   
      226 +   - job `load`;                                              
      227 +   - 5 execuções no runner. É o primeiro ponto que exige push,
          + que é do Anderson.                                           
      228 +3. **SLO e alertas:**                                         
      229 +   - números a partir da etapa 2;                             
      230 +   - regras e testes das regras;                              
      231 +   - job `alert-rules` e passo novo do `compose-test.sh`;     
      232 +   - limites finais do k6;                                    
      233 +   - registro da medição e da folga.                          
      234 +4. **Cache AOT:** Dockerfile, `startup-benchmark.sh` e as muda
          +nças do job `image`.                                          
      235 +5. **SBOM e assinatura:** política ampliada, com os casos nega
          +tivos, e o job `supply-chain`.                                
      236 +6. **Fechamento:**                                            
      237 +   - dev container e job `devcontainer`;                      
      238 +   - página do SLO, runbook, ADRs e README;                   
      239 +   - pedido da proteção da `main` com os 12 checks (R14);     
      240 +   - `./mvnw verify` final e Status da spec.                  
      241 
      242 +A etapa 3 depende da 2, porque os números vêm da medição. As e
          +tapas 4, 5 e 6 são independentes entre si.                    
      243 +                                                              
      244  ## Complexity Tracking
      245 
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
      246 +Não se aplica: nenhuma violação da constituição.              

● Ajustei a spec com os seus três pontos e escrevi o plano do Marco 4, com a
  research, o modelo de dados, dois contratos e o quickstart. Não rodei nada nem
  fiz commit.

  Os três ajustes:
  1. Gerador: a FR-019 manda o pipeline gerar o arquivo duas vezes com a mesma
     semente, comparar os hashes e mostrar os dois no resumo.
  2. Trivy contra versão vulnerável: a FR-034 e o cenário US7-4 seguem o roteiro
     da T050 do Marco 2. O branch temporário (verificacao/trivy-transitivas)
     nunca é mergeado, é apagado nos dois lados e nenhum commit dele volta ao
     marco.
  3. Números do SLO e do k6: a FR-011a nova diz que eles vêm da medição num
     runner compartilhado. O limite do pipeline é mais folgado por causa do
     ruído, e a medição e a folga vão para docs/ai/decisoes.md.

  Decisões do plano que pedem a sua atenção:
  - Nada muda na API: o marco não toca src/main, pom.xml nem os
    application*.yml. A exceção são as correções que o testador adversário
    exigir. Por isso o SLO de latência usa um limite que já existe no histograma
    de métricas, por exemplo cerca de 112 ms (R1). Se você preferir um número
    redondo, como 100 ms, isso exige uma linha nova no application.yml. Deixei
    como opção.
  - Medição para o SLO e o k6 (R5): ao menos 5 execuções do job de carga, com
    limites provisórios largos até existirem números. Proponho folga de 2× sobre
    a maior medição. A folga final é sua.
  - Política do pipeline: a regra que proíbe publicar imagem passa a aceitar uma
    exceção. O job de assinatura pode enviar a imagem só para o registro
    temporário em localhost:5000 e é o único com permissão de identidade. A
    política ganha casos que precisam falhar, para provar que nada mais escapa
    (R10).
  - Cache AOT: a imagem passa a subir com java -jar application.jar em vez do
    JarLauncher. O cache da JVM exige o classpath em arquivos JAR (R8).
  - Correção do Trivy (R12): o job de dependências passa a analisar as
    dependências de runtime que o Maven copia, e não mais o pom.xml. A causa da
    falha ainda é uma hipótese e será investigada na etapa 1.

  Estrutura: quatro jobs novos (alert-rules, load, supply-chain, devcontainer) e
  quatro alterados. A proteção da main passa a exigir 12 checks. São seis
  etapas, e a etapa 3 (alertas) depende dos números da etapa 2 (carga).

  Ficam com você:
  - acionar o testador adversário;
  - o push do branch temporário do Trivy;
  - os pushes das 5 execuções de medição;
  - a proteção da main com os 12 checks.

  Próximo passo: /speckit-tasks.

  Arquivos em specs/004-acabamento-operacional/:
  - plan.md
  - research.md
  - data-model.md
  - quickstart.md
  - contracts/ci-pipeline.md
  - contracts/operacao.md

✻ Cogitated for 6m 38s · done 12:19

❯ /speckit-tasks Aceito as decisões do plano: o SLO de latência usa o limite    
que já existe no histograma (cerca de 112 ms), e a folga do pipeline é de 2×    
sobre a maior medição. As 5 execuções de medição do job de carga são feitas com 
Re-run jobs no mesmo commit, sem commits vazios. Divida em etapas, cada uma     
terminando com o ./mvnw verify verde e um commit.                               

  Ran 1 shell command (ctrl+o to expand)

● Primeiro registro no plano e na research as decisões que você aceitou, depois
  gero as tarefas.

  Ran 1 shell command (ctrl+o to expand)

● Update(specs\004-acabamento-operacional\plan.md)
  ⎿  Added 12 lines
      241 
      242  A etapa 3 depende da 2, porque os números vêm da medição. As e
           tapas 4, 5 e 6 são independentes entre si.
      243 
      244 +## Decisões do responsável sobre o plano (2026-11-25)         
      245 +                                                              
      246 +A registrar em `docs/ai/decisoes.md`, no commit da etapa 1.   
      247 +                                                              
      248 +1. **Limite do SLO de latência:** é o bucket que já existe no 
          +histograma, cerca de 112 ms                                   
      249 +   (`le="0.111848106"`), sem mudar o `application.yml` (R1).  
      250 +2. **Folga do pipeline:** os limites do k6 são 2× a maior medi
          +ção de cada percentil, arredondados para                      
      251 +   cima (R5).                                                 
      252 +3. **Execuções de medição:** as 5 são feitas com "Re-run jobs"
          + do job `load` no mesmo commit, sem                           
      253 +   commits vazios (R5).                                       
      254 +4. **Etapas:** cada etapa termina com o `./mvnw verify` verde 
          +e um commit.                                                  
      255 +                                                              
      256  ## Complexity Tracking
      257 
      258  Não se aplica: nenhuma violação da constituição.
● Update(specs\004-acabamento-operacional\research.md)
  ⎿  Added 3 lines, removed 2 lines
       96 
       97  - **Decisão:** a medição é feita no próprio job `load`, num ru
           nner `ubuntu-latest` compartilhado, com o
       98    roteiro de R4 e limites provisórios largos (p95 até 2 s, err
           o até 5%), para que o job não reprove antes de
       99 -  existirem números. São ao menos **5 execuções** do job, em h
          -orários diferentes. Para cada uma, o resumo do                
      100 -  k6 dá a vazão, o p95, o p99 e a taxa de erro por endpoint.  
       99 +  existirem números. São ao menos **5 execuções** do job `load
          +` **no mesmo commit**, a primeira pelo push e                 
      100 +  as outras por "Re-run jobs", sem commits vazios (decisão do 
          +responsável, 2026-11-25). Para cada uma, o                    
      101 +  resumo do k6 dá a vazão, o p95, o p99 e a taxa de erro por e
          +ndpoint.                                                      
      102    - **Objetivo do SLO:** disponibilidade de 99,5% em 28 dias (
           o compose não tem redundância; o número é
      103      conservador) e latência com o bucket do histograma logo ac
           ima do **maior p99** medido nos intervalos
      104      (R1).

● Update(specs\004-acabamento-operacional\research.md)
  ⎿  Added 2 lines, removed 2 lines
      102    - **Objetivo do SLO:** disponibilidade de 99,5% em 28 dias (
           o compose não tem redundância; o número é
      103      conservador) e latência com o bucket do histograma logo ac
           ima do **maior p99** medido nos intervalos
      104      (R1).
      105 -  - **Limite do pipeline:** a **maior** medição de cada percen
          -til multiplicada por uma folga, proposta em                   
      106 -    2×, arredondada para cima, e taxa de erro abaixo de 1%. A 
          -folga é mais larga que o objetivo do SLO por                  
      105 +  - **Limite do pipeline:** a **maior** medição de cada percen
          +til multiplicada por 2 (folga aceita pelo                     
      106 +    responsável em 2026-11-25), arredondada para cima, e taxa 
          +de erro abaixo de 1%. A folga é mais larga que o objetivo do S
          +LO por                                                        
      107      causa do ruído do runner compartilhado (CPU disputada e di
           sco variável), para o job não ficar instável.
      108    - O registro em `docs/ai/decisoes.md` traz as 5 execuções (l
           ink, valores), a dispersão (menor, maior e
      109      mediana), o objetivo escolhido e a folga, com o motivo. A
           página do SLO explica a diferença entre o

● Update(specs\004-acabamento-operacional\research.md)
  ⎿  Added 3 lines, removed 2 lines
      108    - O registro em `docs/ai/decisoes.md` traz as 5 execuções (l
           ink, valores), a dispersão (menor, maior e
      109      mediana), o objetivo escolhido e a folga, com o motivo. A
           página do SLO explica a diferença entre o
      110      objetivo e o limite do pipeline.
      111 -- **Verificar:** se a dispersão entre as execuções passar de 2
          -×, aumentar a folga ou reduzir a taxa de                      
      112 -  chegada, e registrar a mudança. A folga final é decisão do A
          -nderson, a partir do registro.                                
      111 +- **Verificar:** se a dispersão entre as execuções passar de 2
          +× (a maior sobre a menor), a folga de 2× não                  
      112 +  basta: parar e levar ao Anderson, com os números, a escolha 
          +entre aumentar a folga e reduzir a taxa de                    
      113 +  chegada (Princípio IV).                                     
      114  - **Alternativas:** fixar os números antes da medição (o brief
           ing proíbe); medir na máquina local (não é o
      115    ambiente do job).
      116 

● Update(specs\004-acabamento-operacional\research.md)
  ⎿  Added 7 lines, removed 2 lines
      25    bucket.
      26  - **Alternativas:**
      27    - `management.metrics.distribution.slo` com um limite redondo
           (100 ms): muda a configuração e a saída do
      28 -    `/actuator/prometheus` no modo padrão, de dono diferente (i
         -mplementador). Rejeitada para manter o                         
      29 -    marco sem mudança na API; fica como opção se o Anderson pre
         -ferir um número redondo.                                       
      28 +    `/actuator/prometheus` no modo padrão, de dono diferente (i
         +mplementador). Rejeitada pelo responsável em                   
      29 +    2026-11-25: o limite do SLO é o bucket de cerca de 112 ms. 
      30 +- **Verificar:** o valor exato do bucket na saída real do `/act
         +uator/prometheus` (`le="0.111848106"`). Se o                   
      31 +  Micrometer da versão em uso gerar outro limite perto de 112 m
         +s, usar o que aparece na saída e atualizar                     
      32 +  esta research, a página do SLO e as regras.                  
      33 +- **Verificar:** se o maior p99 medido dos intervalos (R5) pass
         +ar de 112 ms, parar e levar ao Anderson: o                     
      34 +  objetivo de latência precisaria de outro bucket.             
      35    - `histogram_quantile` no alerta: dá um percentil estimado, e
           não a fração de requisições ruins que o
      36      orçamento de erro precisa.
      37 

● Update(specs\004-acabamento-operacional\research.md)
  ⎿  Added 2 lines, removed 2 lines
      105    as outras por "Re-run jobs", sem commits vazios (decisão do
           responsável, 2026-11-25). Para cada uma, o
      106    resumo do k6 dá a vazão, o p95, o p99 e a taxa de erro por e
           ndpoint.
      107    - **Objetivo do SLO:** disponibilidade de 99,5% em 28 dias (
           o compose não tem redundância; o número é
      108 -    conservador) e latência com o bucket do histograma logo ac
          -ima do **maior p99** medido nos intervalos                    
      109 -    (R1).                                                     
      108 +    conservador) e latência de 99% das requisições dos interva
          +los até o bucket de cerca de 112 ms (R1),                     
      109 +    confirmado pelo **maior p99** medido estar abaixo desse li
          +mite.                                                         
      110    - **Limite do pipeline:** a **maior** medição de cada percen
           til multiplicada por 2 (folga aceita pelo
      111      responsável em 2026-11-25), arredondada para cima, e taxa
           de erro abaixo de 1%. A folga é mais larga que o objetivo do S
           LO por
      112      causa do ruído do runner compartilhado (CPU disputada e di
           sco variável), para o job não ficar instável.

● Agora o tasks.md, em seis etapas, cada uma fechando com ./mvnw verify e um
  commit.

● Write(specs\004-acabamento-operacional\tasks.md)
  ⎿  Wrote 466 lines to specs\004-acabamento-operacional\tasks.md
       1 ---
       2 
       3 description: "Tarefas do Marco 4, num único bolt com seis etapas
          (um commit por etapa, cada uma com o ./mvnw verify verde)"
       4 ---
       5 
       6 # Tasks: Marco 4 — Acabamento operacional: alertas de SLO, carga
         , dados gerados, cache AOT, assinatura e dev container
       7 
       8 **Input**: Design documents from `/specs/004-acabamento-operacio
     … +459 lines (~47 KB) (ctrl+o to expand)

● Corrijo os casos plantados (zero e empate em min juntos) e as referências
  cruzadas.

  Ran 1 shell command (ctrl+o to expand)

● Sem Python na máquina; faço as mesmas correções com o Edit.

● Update(specs\004-acabamento-operacional\tasks.md)
  ⎿  Added 1 line, removed 1 line
      148    - formato: "UTF-8 sem BOM, `\n`, separador `;`, cabeçalho `y
           ear;title;studios;producers;winner`". Escrever com `Files.newB
           ufferedWriter(..., UTF_8)` e `\n` explícito, nunca `println` n
           em `%n`;
      149    - chave: "(`year`, `title`) única, sem duplicatas". Títulos
           `Movie <n>`, anos de 1900 a 2099, estúdios de uma lista fixa,
           produtores `Producer 00001` a `Producer NNNNN`, com os separad
           ores `, `, ` and ` e `, and ` sorteados. Cerca de 20% vencedor
           es (`yes`); os demais com `winner` vazio;
      150    - aleatoriedade só por `new java.util.Random(seed)`, sem `Ma
           th.random`, `ThreadLocalRandom`, `HashMap` iterado nem nada de
           pendente de sistema;
      151 -  - casos plantados: "empate em `min`, empate em `max`, produt
          -or com duas vitórias no mesmo ano". Dois produtores próprios c
          -om o mesmo intervalo, maior que qualquer outro (o maior interv
          -alo possível na faixa de anos); dois com o mesmo intervalo mín
          -imo; um com duas vitórias no mesmo ano (intervalo 0). Os produ
          -tores sorteados não ganham duas vezes no mesmo ano nem passam 
          -desses limites;                                               
      151 +  - casos plantados: "dois produtores com duas vitórias no mes
          +mo ano (empate em `min`, intervalo 0); dois com vitórias em 19
          +00 e 2099 (empate em `max`)". Os dois do `min` vencem em anos 
          +diferentes entre si. Os produtores sorteados nunca vencem duas
          + vezes no mesmo ano e nunca vencem em 1900 e em 2099 ao mesmo 
          +tempo, para que `min` e `max` sejam exatamente os plantados;  
      152    - nenhum campo passa de 500 caracteres nem tem U+0000 (spec
           001, FR-006);
      153    - escrita num arquivo temporário na mesma pasta, movido com
           `ATOMIC_MOVE` no fim. Em erro de escrita, apagar o temporário
           e sair com 1;
      154    - no fim, imprimir o caminho, o número de filmes e o SHA-256
            (`MessageDigest`).

● Update(specs\004-acabamento-operacional\tasks.md)
  ⎿  Added 1 line, removed 1 line
      153    - escrita num arquivo temporário na mesma pasta, movido com
           `ATOMIC_MOVE` no fim. Em erro de escrita, apagar o temporário
           e sair com 1;
      154    - no fim, imprimir o caminho, o número de filmes e o SHA-256
            (`MessageDigest`).
      155  - [ ] T015 [US3] Conferir o T014 localmente: duas gerações com
            os padrões dão o mesmo SHA-256; com `--seed 1`, o hash é outr
           o; `--movies 0`, `--movies 2000001`, `--movies x` e sem `--out
           put` saem com 2 e não criam arquivo. Anotar o SHA-256 do padrã
           o gerado no Windows (Git Bash), para o README (SC-005).
      156 -- [ ] T016 [US3] Subir `./mvnw spring-boot:run` com `MOVIES_CS
          -V_PATH` apontando para o arquivo gerado (**verificar** se o va
          -lor aceita um caminho de arquivo comum ou precisa do prefixo `
          -file:`; anotar a forma certa para o README). Conferir: o resum
          -o da carga com 100000 lidas, 100000 aceitas e 0 rejeitadas; a 
          -readiness 200; e nos intervalos, os dois empatados em `max`, o
          -s dois em `min` e o intervalo 0 em `min`, todos no formato do 
          -enunciado. Se o intervalo 0 for o único `min`, ajustar a spec 
          -dos casos plantados no data-model antes de mudar o gerador (os
          - dois empates e o zero não cabem juntos em `min`; manter o zer
          -o e o empate em `max`, e um empate no **segundo** menor interv
          -alo não é observável). **Verificar** e registrar a escolha na 
          -R6.                                                           
      156 +- [ ] T016 [US3] Subir `./mvnw spring-boot:run` com `MOVIES_CS
          +V_PATH` apontando para o arquivo gerado (**verificar** se o va
          +lor aceita um caminho de arquivo comum ou precisa do prefixo `
          +file:`; anotar a forma certa para o README). Conferir: o resum
          +o da carga com 100000 lidas, 100000 aceitas e 0 rejeitadas; a 
          +readiness 200; `min` com exatamente os dois produtores plantad
          +os, intervalo 0, ordenados por `previousWin`; `max` com exatam
          +ente os dois produtores plantados, de 1900 a 2099, ordenados p
          +elo nome; tudo no formato do enunciado (FR-016, US3-3, US3-4).
      157  - [ ] T017 [P] [US2] Criar `compose.load.yaml`: sobrepõe só o
           serviço `app`, montando `./target/generated-data/movies.csv` e
           m `/data/movies.csv:ro` e definindo `MOVIES_CSV_PATH` com a fo
           rma do T016. Comentário no topo com a spec 004, FR-013, e o us
           o.
      158  - [ ] T018 [US2] Em `compose.yaml`, acrescentar o serviço `k6`
            (R4): imagem `grafana/k6:<versão estável>@sha256:<digest>`, `
           profiles: [load]`, `depends_on: app: condition: service_health
           y`, volume `./load:/scripts:ro` e `./target/load:/out`, `envir
           onment` com `BASE_URL: http://app:8080`, `API_TOKEN: ${API_TOK
           EN:-}` e as variáveis dos limites do T019 repassadas (`K6_*: $
           {K6_*:-}`), e `command: ["run", "/scripts/intervals.js"]`. **V
           erificar** se o usuário da imagem consegue escrever em `/out`
           montado; se não, `user:` com o UID do runner, documentado.
      159  - [ ] T019 [US2] Criar `load/intervals.js` (R4; data-model.md,
            "Cenário de carga"):

● Update(specs\004-acabamento-operacional\tasks.md)
  ⎿  Added 1 line, removed 1 line
      211  - [ ] T032 [US1] Em `.github/scripts/compose-test.sh`, acresce
           ntar depois do passo 4 o passo `4b. Regras do SLO carregadas e
            nenhum alerta do SLO disparando` (FR-009, SC-003): em `http:/
           /localhost:9090/api/v1/rules`, conferir com `jq` que os grupos
            do `slo.yml` estão presentes e que nenhuma regra tem `health`
            diferente de `ok` nem `lastError`; em `/api/v1/alerts`, confe
           rir que nenhum alerta com o rótulo `slo` está `firing`. Atuali
           zar o cabeçalho do script e o nome do passo no job `compose` (
           `passos 0 a 8, 4b e 7b`).
      212  - [ ] T033 [US1] Limites finais do k6 (R5, FR-011a): em `load/
           intervals.js`, trocar os padrões provisórios do T019 por 2× a
           maior medição de cada percentil do T025, arredondada para cima
            (em ms inteiros), e `K6_MAX_ERROR_RATE=0.01`. Comentário com
           a origem dos números e o link da entrada do T034.
      213  - [ ] T034 [US1] Registrar em `docs/ai/decisoes.md`, numa entr
           ada "Medição do teste de carga e folga dos limites do pipeline
           " (FR-011a): as 5 execuções do T025 (links e valores por endpo
           int), o menor, o maior e a mediana, o tipo do runner, os objet
           ivos do SLO (disponibilidade de 99,5% e 99% até o bucket do T0
           26, em 28 dias), os limites finais do T033, a folga de 2× e o
           motivo (ruído do runner compartilhado, para o job não ficar in
           stável).
      214 -- [ ] T035 [US1] Rodar localmente com o Docker: `.github/scrip
          -ts/compose-test.sh`, com todos os passos `OK`, inclusive o 4b.
          - Depois, a seção 4 de `specs/004-acabamento-operacional/quicks
          -tart.md`: com `docker compose stop app`, o `AwardsAppDown` fic
          -a `firing` em até 2 min, e o `runbook_url` aponta para `docs/r
          -unbook.md#awardsappdown`. A âncora só existe depois do T057, e
          -ntão conferir o formato agora e o destino na etapa 6. Também `
          -load-test.sh` com os limites finais: `OK`. Sem o Docker, `PULA
          -DO` com o motivo.                                             
      214 +- [ ] T035 [US1] Rodar localmente com o Docker: `.github/scrip
          +ts/compose-test.sh`, com todos os passos `OK`, inclusive o 4b.
          + Depois, a seção 4 de `specs/004-acabamento-operacional/quicks
          +tart.md`: com `docker compose stop app`, o `AwardsAppDown` fic
          +a `firing` em até 2 min, e o `runbook_url` aponta para `docs/r
          +unbook.md#awardsappdown`. A âncora só existe depois do T060, e
          +ntão conferir o formato agora e o destino na etapa 6. Também `
          +load-test.sh` com os limites finais: `OK`. Sem o Docker, `PULA
          +DO` com o motivo.                                             
      215  - [ ] T036 [US1] Rodar `./mvnw verify`: verde, sem Docker. Inf
           ormar o resultado real.
      216  - [ ] T037 [US1] Commit `Etapa 3: alertas de SLO no Prometheus
            e limites do k6 pela medição` com T026 a T036. Na descrição:
           o `le` do T026, a forma do `promtool` do T027 e o resultado do
            T035.
      217 

● Update(specs\004-acabamento-operacional\tasks.md)
  ⎿  Added 1 line, removed 1 line
      261  - [ ] T045 [US4] Rodar `./mvnw verify` e `./mvnw spring-boot:r
           un` (resultado de referência): os dois como no Marco 3, sem Do
           cker (US4-5). Informar o resultado real.
      262  - [ ] T046 [US4] Commit `Etapa 4: cache AOT da JVM na imagem e
            medição da subida` com T038 a T045. Na descrição: as anotaçõe
           s do T040 e a tabela do T042.
      263 
      264 -**Checkpoint**: US4 completa; os números do pipeline vêm do jo
          -b `image` (T060).                                             
      264 +**Checkpoint**: US4 completa; os números do pipeline vêm do jo
          +b `image` (T063).                                             
      265 
      266  ---
      267 

● Update(specs\004-acabamento-operacional\tasks.md)
  ⎿  Added 1 line, removed 1 line
      307    **Verificar** se o cosign aceita o registro HTTP em `localho
           st` sem flag; se não, usar `--allow-insecure-registry` e `--al
           low-http-registry` e atualizar a R10. Comentário do job com a
           spec 004, FR-024 a FR-027.
      308  - [ ] T049 [US5] Em `.github/workflows/ci.yml`, job `image`: a
           crescentar o passo `Assinatura pulada (pull request de fork)`,
            com `if: github.event_name == 'pull_request' && github.event.
           pull_request.head.repo.full_name != github.repository`, que es
           creve no `$GITHUB_STEP_SUMMARY` o motivo: a execução não tem a
            permissão de identidade, e a assinatura roda nos pushes e nos
            PRs do próprio repositório (FR-027).
      309  - [ ] T050 [US5] Rodar `.github/scripts/check-workflows.sh` co
           m o `ci.yml` novo: passa. Repetir os casos negativos do T047:
           continuam falhando.
      310 -- [ ] T051 [US5] Conferir localmente o SBOM: `docker run --rm 
          --v /var/run/docker.sock:/var/run/docker.sock aquasec/trivy:<ve
          -rsão do T003> image --format cyclonedx --output - awards:local
          - | jq` contém `lz4-java` 1.11.4 e `snappy-java` 1.1.10.12, e o
          -s pacotes do Alpine. A assinatura keyless só roda no Actions: 
          -`PULADO` local, com a comprovação no T060.                    
      310 +- [ ] T051 [US5] Conferir localmente o SBOM: `docker run --rm 
          +-v /var/run/docker.sock:/var/run/docker.sock aquasec/trivy:<ve
          +rsão do T003> image --format cyclonedx --output - awards:local
          + | jq` contém `lz4-java` 1.11.4 e `snappy-java` 1.1.10.12, e o
          +s pacotes do Alpine. A assinatura keyless só roda no Actions: 
          +`PULADO` local, com a comprovação no T063.                    
      311  - [ ] T052 [US5] Rodar `./mvnw verify`: verde, sem Docker. Inf
           ormar o resultado real.
      312  - [ ] T053 [US5] Commit `Etapa 5: SBOM e assinatura keyless nu
           m registro temporário` com T047 a T052. Na descrição: os pares
            tag → hash e tag → digest, a conferência das referências anin
           hadas do `cosign-installer`, os casos do T047 e o resultado do
            T051.
      313 

● Update(specs\004-acabamento-operacional\tasks.md)
  ⎿  Added 1 line, removed 1 line
      311  - [ ] T052 [US5] Rodar `./mvnw verify`: verde, sem Docker. Inf
           ormar o resultado real.
      312  - [ ] T053 [US5] Commit `Etapa 5: SBOM e assinatura keyless nu
           m registro temporário` com T047 a T052. Na descrição: os pares
            tag → hash e tag → digest, a conferência das referências anin
           hadas do `cosign-installer`, os casos do T047 e o resultado do
            T051.
      313 
      314 -**Checkpoint**: US5 completa, com a comprovação no pipeline (T
          -060).                                                         
      314 +**Checkpoint**: US5 completa, com a comprovação no pipeline (T
          +063).                                                         
      315 
      316  ---
      317 

● Update(specs\004-acabamento-operacional\tasks.md)
  ⎿  Added 1 line, removed 1 line
      337    - comentário (JSONC) com a spec 004, FR-028 e FR-029.
      338  - [ ] T056 [US6] Em `.github/dependabot.yml`, acrescentar o ec
           ossistema `devcontainers`, com `directory: /` e `schedule.inte
           rval: weekly`, e o comentário da spec 004. Rodar `.github/scri
           pts/check-workflows.sh`: passa.
      339  - [ ] T057 [US6] Em `.github/workflows/ci.yml`, criar o job `d
           evcontainer` (FR-030): `timeout-minutes: 30`, `actions/checkou
           t` e `devcontainers/ci` fixado por hash, com as referências an
           inhadas conferidas, `push: never` e `runCmd: java -version &&
           ./mvnw -v && docker version && ./mvnw -B verify`. Comentário c
           om a spec 004, FR-028 a FR-030. Rodar `.github/scripts/check-w
           orkflows.sh`: passa (o `push: never` não é publicação; **verif
           icar** que a regra 5 não o confunde com `push: true`).
      340 -- [ ] T058 [US6] Localmente, se houver o Docker e o Node: `npx
          - --yes @devcontainers/cli@<versão> up --workspace-folder .` e 
          -`... exec --workspace-folder . bash -lc "java -version && ./mv
          -nw -v && docker version && ./mvnw -B verify"`; depois, dentro 
          -do container, `docker compose up -d --build --wait` e o result
          -ado de referência pela porta encaminhada (US6-3, US6-4). Sem a
          -s ferramentas, `PULADO` com o motivo; a comprovação fica no T0
          -60.                                                           
      340 +- [ ] T058 [US6] Localmente, se houver o Docker e o Node: `npx
          + --yes @devcontainers/cli@<versão> up --workspace-folder .` e 
          +`... exec --workspace-folder . bash -lc "java -version && ./mv
          +nw -v && docker version && ./mvnw -B verify"`; depois, dentro 
          +do container, `docker compose up -d --build --wait` e o result
          +ado de referência pela porta encaminhada (US6-3, US6-4). Sem a
          +s ferramentas, `PULADO` com o motivo; a comprovação fica no T0
          +63.                                                           
      341 
      342  **Checkpoint**: US6 completa, com a comprovação no pipeline (T
           060).
      343 

● Update(specs\004-acabamento-operacional\tasks.md)
  ⎿  Added 1 line, removed 1 line
      339  - [ ] T057 [US6] Em `.github/workflows/ci.yml`, criar o job `d
           evcontainer` (FR-030): `timeout-minutes: 30`, `actions/checkou
           t` e `devcontainers/ci` fixado por hash, com as referências an
           inhadas conferidas, `push: never` e `runCmd: java -version &&
           ./mvnw -v && docker version && ./mvnw -B verify`. Comentário c
           om a spec 004, FR-028 a FR-030. Rodar `.github/scripts/check-w
           orkflows.sh`: passa (o `push: never` não é publicação; **verif
           icar** que a regra 5 não o confunde com `push: true`).
      340  - [ ] T058 [US6] Localmente, se houver o Docker e o Node: `npx
            --yes @devcontainers/cli@<versão> up --workspace-folder .` e
           `... exec --workspace-folder . bash -lc "java -version && ./mv
           nw -v && docker version && ./mvnw -B verify"`; depois, dentro
           do container, `docker compose up -d --build --wait` e o result
           ado de referência pela porta encaminhada (US6-3, US6-4). Sem a
           s ferramentas, `PULADO` com o motivo; a comprovação fica no T0
           63.
      341 
      342 -**Checkpoint**: US6 completa, com a comprovação no pipeline (T
          -060).                                                         
      342 +**Checkpoint**: US6 completa, com a comprovação no pipeline (T
          +063).                                                         
      343 
      344  ---
      345 

● Update(specs\004-acabamento-operacional\tasks.md)
  ⎿  Added 1 line, removed 1 line
      116 
      117    Registrar em `docs/ai/decisoes.md`, numa entrada "Verificaçã
           o do job de dependências com lz4-java 1.11.2", o nome do branc
           h, o link da execução, o job vermelho, o resultado esperado e
           o obtido, e a confirmação de que o branch foi apagado. O regis
           tro entra no commit da etapa 6.
      118 
      119 -**Checkpoint**: US7 completa, salvo T012 (push) e a proteção d
          -a `main` (T061).                                              
      119 +**Checkpoint**: US7 completa, salvo T012 (push) e a proteção d
          +a `main` (T064).                                              
      120 
      121  ---
      122 

● Update(specs\004-acabamento-operacional\data-model.md)
  ⎿  Added 1 line, removed 1 line
      55  | parâmetros | `--movies` (1 a 2.000.000; padrão 100.000), `--s
          eed` (padrão 20261125), `--output` |
      56  | formato | UTF-8 sem BOM, `\n`, separador `;`, cabeçalho `year
          ;title;studios;producers;winner` |
      57  | chave | (`year`, `title`) única, sem duplicatas |
      58 -| casos plantados | empate em `min`, empate em `max`, produtor 
         -com duas vitórias no mesmo ano |                               
      58 +| casos plantados | dois produtores com duas vitórias no mesmo 
         +ano (empate em `min`, intervalo 0); dois com vitórias em 1900 e
         + 2099 (empate em `max`) |                                      
      59  | invariante | mesmo `--movies` e `--seed` → mesmo SHA-256 em q
          ualquer sistema operacional |
      60  | local | fora do controle de versão (`target/generated-data/`)
          , nunca embarcado na imagem |
      61 

● Update(specs\004-acabamento-operacional\research.md)
  ⎿  Added 3 lines, removed 2 lines
      131    - Conteúdo: anos de 1900 a 2099, títulos `Movie <n>` únicos
           por ano, estúdios de uma lista fixa,
      132      produtores de um conjunto de nomes sintéticos (`Producer 0
           0001`, …), com separadores por vírgula, por
      133      ` and ` e por `, and` sorteados. Cerca de 20% vencedores.
           Para exercitar as regras (FR-016), o gerador
      134 -    planta casos fixos, calculados a partir da semente: dois p
          -rodutores empatados no menor intervalo, dois                  
      135 -    empatados no maior e um produtor com duas vitórias no mesm
          -o ano (intervalo 0, que vira o `min`).                        
      134 +    planta casos fixos: dois produtores, cada um com duas vitó
          +rias no mesmo ano (empate em `min`, com                       
      135 +    intervalo 0), e dois com vitórias em 1900 e 2099 (empate e
          +m `max`). Os sorteados nunca repetem esses                    
      136 +    padrões, para que `min` e `max` sejam exatamente os planta
          +dos.                                                          
      137    - Limites: `--movies` de 1 a 2.000.000; fora disso, ou com p
           arâmetro desconhecido, sai com código 2 e
      138      mensagem clara. Sem `--seed`, usa a semente padrão documen
           tada.
      139    - Nenhum campo passa dos tamanhos máximos do esquema, nem te
           m caractere nulo (spec 001, FR-006).

● O tasks.md do Marco 4 está pronto: 68 tarefas em seis etapas, cada uma
  terminando com ./mvnw verify verde e um commit. Ainda não implementei nada nem
  fiz commit.

  Suas decisões, já no plano e na research:
  - O SLO de latência usa o bucket existente de cerca de 112 ms.
  - Os limites do k6 são 2× a maior medição.
  - As 5 medições são 1 push e 4 "Re-run jobs" no mesmo commit, sem commits
    vazios.

  Etapas:

  ┌──────────────────┬─────────────┬───────────┬────────────────────────────┐
  │      Etapa       │  Histórias  │  Tarefas  │           Commit           │
  ├──────────────────┼─────────────┼───────────┼────────────────────────────┤
  │ Preparação       │ —           │ T001–T002 │ nenhum                     │
  ├──────────────────┼─────────────┼───────────┼────────────────────────────┤
  │                  │             │           │ Etapa 1: pendências do     │
  │ 1. Pendências do │ US7         │ T003–T012 │ Marco 3 e inventário       │
  │  Marco 3         │             │           │ efetivo no job de          │
  │                  │             │           │ dependências               │
  ├──────────────────┼─────────────┼───────────┼────────────────────────────┤
  │ 2. Gerador e     │             │           │ Etapa 2: gerador de CSV    │
  │ carga            │ US3, US2    │ T013–T025 │ determinístico e teste de  │
  │                  │             │           │ carga com k6               │
  ├──────────────────┼─────────────┼───────────┼────────────────────────────┤
  │                  │             │           │ Etapa 3: alertas de SLO no │
  │ 3. SLO e alertas │ US1         │ T026–T037 │  Prometheus e limites do   │
  │                  │             │           │ k6 pela medição            │
  ├──────────────────┼─────────────┼───────────┼────────────────────────────┤
  │                  │             │           │ Etapa 4: cache AOT da JVM  │
  │ 4. Cache AOT     │ US4         │ T038–T046 │ na imagem e medição da     │
  │                  │             │           │ subida                     │
  ├──────────────────┼─────────────┼───────────┼────────────────────────────┤
  │ 5. SBOM e        │             │           │ Etapa 5: SBOM e assinatura │
  │ assinatura       │ US5         │ T047–T053 │  keyless num registro      │
  │                  │             │           │ temporário                 │
  ├──────────────────┼─────────────┼───────────┼────────────────────────────┤
  │ 6. Dev container │ US6 e       │           │ Etapa 6: dev container,    │
  │  e documentação  │ fechamento  │ T054–T068 │ SLO, runbook, ADRs e       │
  │                  │             │           │ README do Marco 4          │
  └──────────────────┴─────────────┴───────────┴────────────────────────────┘

  Por história: US7 tem 10 tarefas, US3 e US2 juntas 13, US1 12, US4 9, US5 7,
  US6 5, e o fechamento 10.

  Ordem e dependências:
  - Cada etapa começa por uma verificação que precisa falhar antes da
    implementação (T003, T013, T027, T038, T047 e T054).
  - A etapa 3 depende de você. Ela só começa depois do T025: o push da etapa 2 e
    os 4 "Re-run jobs". Paro e trago os números se a maior medição passar de 2×
    a menor, se o p99 dos intervalos passar de 112 ms ou se alguma execução
    tiver erro acima de 1%.
  - As etapas 4 e 5 seguem em sequência, porque as duas mexem no job image do
    ci.yml.
  - Paralelo: o gerador e o compose.load.yaml (T014 e T017), e a página de SLO,
    o runbook e os ADRs (T059 a T061).
  - MVP: a etapa 1 sozinha já pode ser revisada, porque corrige a falha de
    segurança do job de dependências.

  Correção no design: o plano previa empate em min junto com um produtor de
  intervalo 0, e os dois não cabem juntos, porque o 0 sempre ganha. Agora o
  gerador planta dois produtores com duas vitórias no mesmo ano, que empatam em
  min com intervalo 0, e dois com vitórias em 1900 e 2099, que empatam em max. A
  research e o data-model foram ajustados.

  Ficam com você:
  - acionar o testador adversário (T005);
  - o push do branch temporário do Trivy (T012);
  - o push e os "Re-run jobs" da medição (T025);
  - conferir os 12 checks no PR (T063);
  - configurar a proteção da main com os 12 checks (T064).

  Formato: todas as tarefas seguem o padrão de checkbox, ID e rótulo, com [USn]
  nas fases das histórias. As de preparação e de documentação ficam sem rótulo,
  como pede o template.

  Próximo passo: /speckit-analyze para conferir a coerência entre spec, plano e
  tarefas, ou direto /speckit-implement.

✻ Brewed for 5m 4s · done 14:07
❯ /speckit-analyze                                                              

  Ran 1 shell command (ctrl+o to expand)

● Análise de consistência: spec, plano e tarefas do Marco 4

  A análise não achou nenhum problema crítico. Achei um problema alto (H1), seis
  médios e três baixos. Todos os 39 requisitos têm ao menos uma tarefa. Os
  problemas ficam na forma de comprovar alguns deles. Não alterei nenhum
  arquivo.

  ID: H1
  Categoria: Comprovação (Princípio VI)
  Severidade: HIGH
  Onde: spec FR-029; plano (Rastreabilidade: FR-028 a FR-030 → job
    devcontainer); tasks T057, T058
  Problema: A FR-029 exige que o compose suba dentro do dev container e que as
    portas fiquem acessíveis da máquina. O job devcontainer só roda java,
    mvnw, docker version e o verify. O compose e as portas só são conferidos
    no T058, que pode ficar PULADO. Pelo Princípio VI, um requisito sem
    comprovação não está entregue.
  Recomendação: Incluir no runCmd do T057 o docker compose up -d --build
    --wait e o resultado de referência. Comprovar as portas encaminhadas por
    revisão, com um item no checklist.
  ────────────────────────────────────────
  ID: M1
  Categoria: Comprovação
  Severidade: MEDIUM
  Onde: spec FR-023 e US4-4; plano (FR-023 → job image); tasks T040
  Problema: O plano diz que o job image comprova que a imagem sobe com um
    cache inválido. Nenhum passo do pipeline testa isso: a verificação é
    manual, no T040.
  Recomendação: Levar o caso do T040 (cache cortado com head -c) para um passo
    do startup-benchmark.sh ou do teste de fumaça.
  ────────────────────────────────────────
  ID: M2
  Categoria: Cobertura
  Severidade: MEDIUM
  Onde: spec US2-5; tasks T019, T021
  Problema: O T019 envia o token quando ele existe, mas nenhuma tarefa roda o
    k6 com a autenticação ligada. Também nenhuma confere que um 401 conta como
    erro.
  Recomendação: Acrescentar ao T021 uma execução local com API_TOKEN definido:
    com o token, 200. Com o token do k6 trocado, o contract_errors sobe e o
    limite reprova.
  ────────────────────────────────────────
  ID: M3
  Categoria: Inconsistência
  Severidade: MEDIUM
  Onde: research R6 ("o pipeline confere que o hash do Linux é o mesmo" do
    Windows); tasks T013, T015, T063
  Problema: A research diz que o pipeline compara o hash do Linux com o do
    Windows. As tarefas só comparam as duas gerações entre si, e a comparação
    com o Windows é manual, no T063.
  Recomendação: Guardar o SHA-256 do T015 como esperado no load-test.sh (passo
    1), para que um hash diferente reprove.
  ────────────────────────────────────────
  ID: M4
  Categoria: Comprovação
  Severidade: MEDIUM
  Onde: spec SC-006 ("nos dois caminhos"); tasks T013, passo 3
  Problema: Na carga por Kafka, o passo 3 confere só que os intervalos não vêm
    vazios. Ele não confere zero linhas rejeitadas, como o passo 2 faz na
    carga direta.
  Recomendação: No passo 3, ler o resumo do publicador em docker compose logs
    app e exigir zero rejeitadas.
  ────────────────────────────────────────
  ID: M5
  Categoria: Subespecificação
  Severidade: MEDIUM
  Onde: tasks T048, passo 3; research R10
  Problema: O Trivy roda contra localhost:5000/awards@<digest>, um registro
    HTTP. Pode ser preciso ligar o acesso inseguro (TRIVY_INSECURE), e não há
    nenhum "verificar" para isso. A imagem também não é carregada no Docker
    local (load ausente).
  Recomendação: Acrescentar um "verificar" no T048: usar TRIVY_INSECURE=true
    ou load: true e analisar awards:ci, registrando a escolha na R10.
  ────────────────────────────────────────
  ID: M6
  Categoria: Inconsistência
  Severidade: MEDIUM
  Onde: plano, Constitution Check ("imagens novas por digest"); research R11 e
    T055 (recurso docker-in-docker por versão exata)
  Problema: Todo o projeto fixa as imagens por digest, mas o recurso do dev
    container é um artefato OCI fixado só por versão. A constituição não exige
    o digest para recursos, mas a convenção do plano diz "toda imagem nova".
  Recomendação: Fixar o recurso por @sha256: também, ou registrar a exceção no
    plano.
  ────────────────────────────────────────
  ID: L1
  Categoria: Rastreabilidade
  Severidade: LOW
  Onde: plano (FR-010 a FR-013, "verificação única com limite impossível,
    registrada"); T021
  Problema: O teste com o limite impossível vai só para a descrição do commit,
    e não para docs/ai/decisoes.md. As outras verificações únicas vão para lá.
  Recomendação: Registrar o resultado na entrada do T034.
  ────────────────────────────────────────
  ID: L2
  Categoria: Ambiguidade
  Severidade: LOW
  Onde: spec FR-005; T028
  Problema: A spec não diz se as respostas 4xx e 304 entram no total da
    latência. O T028 usa todas as requisições.
  Recomendação: Dizer isso na FR-005 ou na página do SLO (T059).
  ────────────────────────────────────────
  ID: L3
  Categoria: Subespecificação
  Severidade: LOW
  Onde: spec, Edge Cases ("o README informa o limite medido" do heap)
  Problema: Nenhuma tarefa mede o limite de memória do gerador e da carga.
  Recomendação: Anotar no T021 até que tamanho a carga sobe sem erro de
    memória, ou retirar a frase da spec.

  Cobertura por requisito

  ┌─────────────────┬──────────┬──────────────────────────────┬────────────┐
  │    Requisito    │   Tem    │           Tarefas            │ Observação │
  │                 │ tarefa?  │                              │            │
  ├─────────────────┼──────────┼──────────────────────────────┼────────────┤
  │ FR-001, FR-002  │ Sim      │ T008, T010, T023, T036,      │            │
  │                 │          │ T045, T052, T067             │            │
  ├─────────────────┼──────────┼──────────────────────────────┼────────────┤
  │ FR-003, FR-007  │ Sim      │ T059, T060                   │            │
  ├─────────────────┼──────────┼──────────────────────────────┼────────────┤
  │ FR-004 a        │ Sim      │ T027 a T031                  │            │
  │ FR-006, FR-008  │          │                              │            │
  ├─────────────────┼──────────┼──────────────────────────────┼────────────┤
  │ FR-009          │ Sim      │ T032, T035                   │            │
  ├─────────────────┼──────────┼──────────────────────────────┼────────────┤
  │ FR-010 a FR-013 │ Sim      │ T013, T018 a T021            │ US2-5: M2  │
  ├─────────────────┼──────────┼──────────────────────────────┼────────────┤
  │ FR-011a         │ Sim      │ T025, T033, T034             │            │
  ├─────────────────┼──────────┼──────────────────────────────┼────────────┤
  │ FR-014 a        │ Sim      │ T013 a T017, T022            │ M3         │
  │ FR-017, FR-019  │          │                              │            │
  ├─────────────────┼──────────┼──────────────────────────────┼────────────┤
  │ FR-018          │ Sim      │ T013, T062                   │ M4         │
  ├─────────────────┼──────────┼──────────────────────────────┼────────────┤
  │ FR-020 a FR-022 │ Sim      │ T038 a T043                  │            │
  ├─────────────────┼──────────┼──────────────────────────────┼────────────┤
  │ FR-023          │ Parcial  │ T040                         │ M1         │
  ├─────────────────┼──────────┼──────────────────────────────┼────────────┤
  │ FR-024 a FR-027 │ Sim      │ T047 a T051                  │ M5         │
  ├─────────────────┼──────────┼──────────────────────────────┼────────────┤
  │ FR-028, FR-030  │ Sim      │ T054 a T057                  │            │
  ├─────────────────┼──────────┼──────────────────────────────┼────────────┤
  │ FR-029          │ Parcial  │ T058                         │ H1         │
  ├─────────────────┼──────────┼──────────────────────────────┼────────────┤
  │ FR-031 a FR-035 │ Sim      │ T003 a T007, T012, T064      │            │
  ├─────────────────┼──────────┼──────────────────────────────┼────────────┤
  │ FR-036, FR-037  │ Sim      │ T061, T062                   │            │
  ├─────────────────┼──────────┼──────────────────────────────┼────────────┤
  │ FR-038          │ Sim      │ plano (Rastreabilidade) e    │            │
  │                 │          │ checklist                    │            │
  ├─────────────────┼──────────┼──────────────────────────────┼────────────┤
  │ SC-001 a SC-011 │ Sim      │ T063 junta a conferência no  │ SC-006: M4 │
  │                 │          │ pipeline                     │            │
  └─────────────────┴──────────┴──────────────────────────────┴────────────┘

  Constituição: nenhuma violação.
  - Princípio I: os únicos testes da API são os ITs do testador adversário. O
    gerador é ferramenta do repositório, não código do back-end.
  - Princípio II: nada é exigido do modo padrão.
  - Princípio VI: só os casos H1 e M1.
  - Princípio VII: a assinatura não usa chave, e nenhum segredo entra.

  Tarefas sem requisito: só T001 e T002, a preparação, o que é esperado.

  Métricas
  - Requisitos: 39 (38 FRs mais a FR-011a) e 11 critérios de sucesso.
  - Tarefas: 68.
  - Cobertura: 100% dos requisitos têm ao menos uma tarefa. FR-023 e FR-029 têm
    só comprovação manual.
  - Ambiguidades: 1. Duplicações: 0. Críticos: 0.

  Próximos passos

  Dá para seguir para o /speckit-implement. Ainda assim, recomendo corrigir o H1
  antes, porque pelo Princípio VI a FR-029 ficaria sem comprovação e não
  contaria como entregue. Os médios são ajustes pequenos no tasks.md e na
  R6/R10. Eles não mudam a spec, salvo o L2 e o L3.

  Quer que eu sugira as edições concretas para o H1 e os seis médios?

✻ Churned for 1m 9s · done 14:10

❯ Aplique o H1 como recomendado: o job devcontainer sobe o compose com docker   
  compose up -d --build --wait e confere o resultado de referência; as portas   
  encaminhadas ficam como item de revisão no checklist. Aplique também os seis  
  MEDIUM e os três LOW como recomendados. Se algum deles mudar o que a spec     
  promete, em vez de só como comprovar, me mostre antes de aplicar. Depois rode 
  o /speckit-analyze de novo e me mostre o resultado. Não faça commit.          

● Nenhum dos dez ajustes muda o que a spec promete. Todos mudam só como cada
  requisito é comprovado, então aplico direto:

  - O L2 vai para a página do SLO, que a FR-003 já obriga a dizer o que é
    requisição boa e ruim.
  - O L3 vira uma medição que cumpre a frase que já está na spec.

● Update(specs\004-acabamento-operacional\tasks.md)
  ⎿  Added 1 line, removed 1 line
      336    - nenhum `containerEnv` com segredo;
      337    - comentário (JSONC) com a spec 004, FR-028 e FR-029.
      338  - [ ] T056 [US6] Em `.github/dependabot.yml`, acrescentar o ec
           ossistema `devcontainers`, com `directory: /` e `schedule.inte
           rval: weekly`, e o comentário da spec 004. Rodar `.github/scri
           pts/check-workflows.sh`: passa.
      339 -- [ ] T057 [US6] Em `.github/workflows/ci.yml`, criar o job `d
          -evcontainer` (FR-030): `timeout-minutes: 30`, `actions/checkou
          -t` e `devcontainers/ci` fixado por hash, com as referências an
          -inhadas conferidas, `push: never` e `runCmd: java -version && 
          -./mvnw -v && docker version && ./mvnw -B verify`. Comentário c
          -om a spec 004, FR-028 a FR-030. Rodar `.github/scripts/check-w
          -orkflows.sh`: passa (o `push: never` não é publicação; **verif
          -icar** que a regra 5 não o confunde com `push: true`).        
      339 +- [ ] T057 [US6] Em `.github/workflows/ci.yml`, criar o job `d
          +evcontainer` (FR-030): `timeout-minutes: 30`, `actions/checkou
          +t` e `devcontainers/ci` fixado por hash, com as referências an
          +inhadas conferidas, `push: never` e `runCmd: java -version && 
          +./mvnw -v && docker version && ./mvnw -B verify && docker comp
          +ose up -d --build --wait && curl -fsS http://localhost:8080/ap
          +i/producers/intervals | jq -e '.min[0].producer == "Joel Silve
          +r" and .min[0].previousWin == 1990 and .min[0].followingWin ==
          + 1991 and .max[0].producer == "Matthew Vaughn" and .max[0].pre
          +viousWin == 2002 and .max[0].followingWin == 2015' && docker c
          +ompose down -v`. Isso comprova o compose dentro do dev contain
          +er (FR-029). **Verificar** se o `jq` e o `curl` existem na ima
          +gem do dev container. Se não existirem, instalá-los no `postCr
          +eateCommand` do T055. As portas encaminhadas para a máquina nã
          +o são observáveis no runner e ficam como item de revisão no ch
          +ecklist (T066). Comentário com a spec 004, FR-028 a FR-030. Ro
          +dar `.github/scripts/check-workflows.sh`: passa (o `push: neve
          +r` não é publicação; **verificar** que a regra 5 não o confund
          +e com `push: true`).                                          
      340  - [ ] T058 [US6] Localmente, se houver o Docker e o Node: `npx
            --yes @devcontainers/cli@<versão> up --workspace-folder .` e
           `... exec --workspace-folder . bash -lc "java -version && ./mv
           nw -v && docker version && ./mvnw -B verify"`; depois, dentro
           do container, `docker compose up -d --build --wait` e o result
           ado de referência pela porta encaminhada (US6-3, US6-4). Sem a
           s ferramentas, `PULADO` com o motivo; a comprovação fica no T0
           63.
      341 
      342  **Checkpoint**: US6 completa, com a comprovação no pipeline (T
           063).

● Update(specs\004-acabamento-operacional\tasks.md)
  ⎿  Added 1 line, removed 1 line
      370    - no `image`: as medianas sem e com o cache (SC-007);
      371    - no `load`: os dois hashes iguais (e iguais ao do T015), os
            tempos de carga e os limites finais respeitados (SC-004 a SC-
           006);
      372    - no `supply-chain`: o digest, o índice do Rekor, a verifica
           ção certa `OK` e a errada falhando como esperado, e o SBOM nos
            artefatos (SC-008);
      373 -  - no `devcontainer`: o `verify` verde (SC-009);             
      373 +  - no `devcontainer`: o `verify` verde, o compose pronto e o 
          +resultado de referência (SC-009, FR-029);                     
      374    - no `compose`: o passo 4b `OK` (SC-003).
      375 
      376    Copiar os números para o README (T062).

● Update(specs\004-acabamento-operacional\tasks.md)
  ⎿  Added 1 line, removed 1 line
      380 
      381    Conferir na página do PR que os 12 aparecem como obrigatório
           s. Registrar em `docs/ai/decisoes.md`, numa entrada "Proteção
           da main depois do Marco 4", a lista configurada e a confirmaçã
           o do bypass desligado, sem nenhum dado pessoal.
      382  - [ ] T065 Rodar as seções 1 a 5 e 7 de `specs/004-acabamento-
           operacional/quickstart.md`, num clone limpo do branch: só com
           o JDK, a seção 1 e a 2; com o Docker, as demais. Anotar os res
           ultados e os `PULADO` na descrição do commit.
      383 -- [ ] T066 Registrar em `docs/ai/decisoes.md` o fechamento do 
          -bolt: o resultado do T012 (entrada própria), do T063 e do T065
          -, os `PULADO`, o estado do T005 e as pendências. Marcar na seç
          -ão "Revisão da documentação" de `specs/004-acabamento-operacio
          -nal/checklists/requirements.md` os itens conferidos.          
      383 +- [ ] T066 Registrar em `docs/ai/decisoes.md` o fechamento do 
          +bolt: o resultado do T012 (entrada própria), do T063 e do T065
          +, os `PULADO`, o estado do T005 e as pendências. Marcar na seç
          +ão "Revisão da documentação" de `specs/004-acabamento-operacio
          +nal/checklists/requirements.md` os itens conferidos, inclusive
          + o das portas encaminhadas do dev container (FR-029), a partir
          + do T058 ou de uma abertura manual no VS Code.                
      384  - [ ] T067 Rodar `./mvnw verify`: verde, sem Docker. Informar
           o resultado real. Atualizar o **Status** de `specs/004-acabame
           nto-operacional/spec.md` para `Implemented`, listando o que ai
           nda depende de push ou do Anderson. Atualizar o **Status** de
           `specs/003-modo-docker/spec.md`, retirando as pendências fecha
           das (T050 e T093 da spec 003).
      385  - [ ] T068 Commit `Etapa 6: dev container, SLO, runbook, ADRs
           e README do Marco 4` com T054 a T067. Avisar o Anderson de que
            o bolt está pronto para o PR. Depois do merge, a sessão é exp
           ortada para `docs/ai/sessoes/`.
      386 

● Update(specs\004-acabamento-operacional\tasks.md)
  ⎿  Added 2 lines
      238    - sair com `FALHA` se a mediana com o cache não for menor (S
           C-007);
      239    - sair com `FALHA` se o log do modo com o cache tiver um avi
           so de que o cache não pôde ser usado.
      240 
      241 +  Depois das repetições, um passo `Cache inválido não impede a
          + subida` (FR-023, US4-4): `docker run -d --entrypoint sh <imag
          +em> -c 'head -c 4096 app.aot > /tmp/ruim.aot && exec java -XX:
          +MaxRAMPercentage=75 -XX:AOTCache=/tmp/ruim.aot -jar applicatio
          +n.jar'`. A readiness precisa chegar a 200 em até 120 s, e o lo
          +g precisa ter o aviso da JVM sobre o cache. Se não houver avis
          +o ou se a readiness não chegar, `FALHA`.                      
      242 +                                                              
      243    Rodar contra a imagem atual: precisa **falhar** (`applicatio
           n.jar` não existe na imagem do Marco 3).
      244 
      245  ### Implementação da etapa 4

● Update(specs\004-acabamento-operacional\tasks.md)
  ⎿  Added 1 line, removed 1 line
      254  - [ ] T040 [US4] **Verificar** os pontos da R8 e anotar na des
           crição do commit:
      255    - se o treino com `onRefresh` sai antes da carga do CSV e da
            abertura da porta: ler o log do `RUN` no `docker build --prog
           ress=plain`. Se o cache ficar sem ganho de tempo (T042), troca
           r pelo treino até a readiness descrito na R8 e atualizar a R8;
      256    - o tamanho do `app.aot` (`docker run --rm --entrypoint ls <
           imagem> -l app.aot`);
      257 -  - o cache inválido: `docker run --rm --entrypoint sh <imagem
          -> -c 'head -c 4096 app.aot > /tmp/ruim.aot && exec java -XX:Ma
          -xRAMPercentage=75 -XX:AOTCache=/tmp/ruim.aot -jar application.
          -jar'` sobe até a readiness, com um aviso da JVM no log (FR-023
          -, US4-4);                                                     
      257 +  - o texto exato do aviso da JVM com o cache inválido, que o 
          +passo do T038 procura no log. Se o texto da JVM 25 for outro q
          +ue o previsto, ajustar o padrão do script;                    
      258    - que o `.github/scripts/smoke-test.sh` não depende do `JarL
           auncher` nem do caminho das classes. Se depender, ajustar só a
            referência, sem mudar os passos.
      259  - [ ] T041 [US4] Rodar localmente `hadolint` (`docker run --rm
            -i hadolint/hadolint < Dockerfile`, com a configuração do job
            `dockerfile-lint`), `docker build -t awards:local .`, `.githu
           b/scripts/smoke-test.sh awards:local` e `.github/scripts/compo
           se-test.sh`: todos passam (FR-021, US4-1, US4-2, US4-6).
      260  - [ ] T042 [US4] Rodar `.github/scripts/startup-benchmark.sh a
           wards:local 7`: precisa passar, com a mediana com o cache meno
           r. Anotar a tabela, identificada como da máquina local.

● Update(specs\004-acabamento-operacional\tasks.md)
  ⎿  Added 1 line, removed 1 line
      258    - que o `.github/scripts/smoke-test.sh` não depende do `JarL
           auncher` nem do caminho das classes. Se depender, ajustar só a
            referência, sem mudar os passos.
      259  - [ ] T041 [US4] Rodar localmente `hadolint` (`docker run --rm
            -i hadolint/hadolint < Dockerfile`, com a configuração do job
            `dockerfile-lint`), `docker build -t awards:local .`, `.githu
           b/scripts/smoke-test.sh awards:local` e `.github/scripts/compo
           se-test.sh`: todos passam (FR-021, US4-1, US4-2, US4-6).
      260  - [ ] T042 [US4] Rodar `.github/scripts/startup-benchmark.sh a
           wards:local 7`: precisa passar, com a mediana com o cache meno
           r. Anotar a tabela, identificada como da máquina local.
      261 -- [ ] T043 [US4] Em `.github/workflows/ci.yml`, job `image`: d
          -epois do teste de fumaça, o passo `Subida sem e com o cache AO
          -T (7 repetições, mediana)` com `run: .github/scripts/startup-b
          -enchmark.sh awards:ci 7` (FR-022). Atualizar o comentário do j
          -ob com a spec 004, FR-020 a FR-023.                           
      261 +- [ ] T043 [US4] Em `.github/workflows/ci.yml`, job `image`: d
          +epois do teste de fumaça, o passo `Subida sem e com o cache AO
          +T (7 repetições, mediana) e cache inválido` com `run: .github/
          +scripts/startup-benchmark.sh awards:ci 7` (FR-022, FR-023). At
          +ualizar o comentário do job com a spec 004, FR-020 a FR-023.  
      262  - [ ] T044 [US4] Conferir com o relatório do Trivy da imagem (
           local, com a versão do T003) que o cache não trouxe pacote nov
           o, e rodar `.github/scripts/check-workflows.sh` e `.github/scr
           ipts/check-docker-context.sh`: passam.
      263  - [ ] T045 [US4] Rodar `./mvnw verify` e `./mvnw spring-boot:r
           un` (resultado de referência): os dois como no Marco 3, sem Do
           cker (US4-5). Informar o resultado real.
      264  - [ ] T046 [US4] Commit `Etapa 4: cache AOT da JVM na imagem e
            medição da subida` com T038 a T045. Na descrição: as anotaçõe
           s do T040 e a tabela do T042.

● Update(specs\004-acabamento-operacional\tasks.md)
  ⎿  Added 1 line, removed 1 line
      132  ### Verificação da etapa 2 (escrever primeiro; precisa falhar)
      133 
      134  - [ ] T013 [US2] Criar `.github/scripts/load-test.sh` (contrac
           ts/ci-pipeline.md, "Scripts"; R4 a R7). Uso: `load-test.sh <im
           agem> [filmes]`, com 100000 filmes por padrão. Precisa de `doc
           ker`, `curl`, `jq` e `sha256sum`; sem eles, sai com 2. Passos,
            cada um com `OK`, `FALHA` ou `PULADO`:
      135 -  1. gerar `target/generated-data/a.csv` e `target/generated-d
          -ata/movies.csv` com `java tools/csv-generator/GenerateMovies.j
          -ava --movies <filmes> --output ...`, os dois com a semente pad
          -rão. Comparar os SHA-256: diferentes é `FALHA`. Escrever os do
          -is hashes no resumo (FR-019);                                 
      135 +  1. gerar `target/generated-data/a.csv` e `target/generated-d
          +ata/movies.csv` com `java tools/csv-generator/GenerateMovies.j
          +ava --movies <filmes> --output ...`, os dois com a semente pad
          +rão. Comparar os SHA-256: diferentes é `FALHA`. Escrever os do
          +is hashes no resumo (FR-019). Com os parâmetros padrão (100000
          + filmes e a semente padrão), comparar também com a constante `
          +EXPECTED_SHA256_DEFAULT` do script, que guarda o hash gerado n
          +o Windows no T015. Diferente é `FALHA` (SC-005, research R6). 
          +Com outro número de filmes, essa comparação é `PULADO` com o m
          +otivo, e esse `PULADO` não é falha mesmo com `CI=true`, porque
          + o job usa sempre os padrões;                                 
      136    2. carga direta: `docker run -d` da imagem no modo padrão, c
           om `movies.csv` montado só leitura e `MOVIES_CSV_PATH` apontan
           do para ele. Medir do `docker run` à readiness 200, consultand
           o a cada 0,5 s, com limite de 600 s. No log do container, o re
           sumo da carga precisa ter zero rejeitadas. Os intervalos preci
           sam ter `min` e `max` não vazios. Remover o container (FR-018,
            SC-006);
      137    3. carga por Kafka: `docker compose -f compose.yaml -f compo
           se.load.yaml up -d --build --wait`. Medir do `State.StartedAt`
            do container `app` (`docker inspect`) à readiness 200, e conf
           erir os intervalos não vazios;
      138    4. `docker compose -f compose.yaml -f compose.load.yaml --pr
           ofile load run --rm k6`. O código de saída do k6 é o do passo
           (99 = limite ultrapassado);

● Update(specs\004-acabamento-operacional\tasks.md)
  ⎿  Added 1 line, removed 1 line
      134  - [ ] T013 [US2] Criar `.github/scripts/load-test.sh` (contrac
           ts/ci-pipeline.md, "Scripts"; R4 a R7). Uso: `load-test.sh <im
           agem> [filmes]`, com 100000 filmes por padrão. Precisa de `doc
           ker`, `curl`, `jq` e `sha256sum`; sem eles, sai com 2. Passos,
            cada um com `OK`, `FALHA` ou `PULADO`:
      135    1. gerar `target/generated-data/a.csv` e `target/generated-d
           ata/movies.csv` com `java tools/csv-generator/GenerateMovies.j
           ava --movies <filmes> --output ...`, os dois com a semente pad
           rão. Comparar os SHA-256: diferentes é `FALHA`. Escrever os do
           is hashes no resumo (FR-019). Com os parâmetros padrão (100000
            filmes e a semente padrão), comparar também com a constante `
           EXPECTED_SHA256_DEFAULT` do script, que guarda o hash gerado n
           o Windows no T015. Diferente é `FALHA` (SC-005, research R6).
           Com outro número de filmes, essa comparação é `PULADO` com o m
           otivo, e esse `PULADO` não é falha mesmo com `CI=true`, porque
            o job usa sempre os padrões;
      136    2. carga direta: `docker run -d` da imagem no modo padrão, c
           om `movies.csv` montado só leitura e `MOVIES_CSV_PATH` apontan
           do para ele. Medir do `docker run` à readiness 200, consultand
           o a cada 0,5 s, com limite de 600 s. No log do container, o re
           sumo da carga precisa ter zero rejeitadas. Os intervalos preci
           sam ter `min` e `max` não vazios. Remover o container (FR-018,
            SC-006);
      137 -  3. carga por Kafka: `docker compose -f compose.yaml -f compo
          -se.load.yaml up -d --build --wait`. Medir do `State.StartedAt`
          - do container `app` (`docker inspect`) à readiness 200, e conf
          -erir os intervalos não vazios;                                
      137 +  3. carga por Kafka: `docker compose -f compose.yaml -f compo
          +se.load.yaml up -d --build --wait`. Medir do `State.StartedAt`
          + do container `app` (`docker inspect`) à readiness 200. Confer
          +ir os intervalos não vazios. No `docker compose logs app`, o r
          +esumo da leitura do publicador precisa ter zero rejeitadas e o
          + mesmo número de aceitas do passo 2 (SC-006, nos dois caminhos
          +);                                                            
      138    4. `docker compose -f compose.yaml -f compose.load.yaml --pr
           ofile load run --rm k6`. O código de saída do k6 é o do passo
           (99 = limite ultrapassado);
      139    5. escrever no `$GITHUB_STEP_SUMMARY` (ou na saída, fora do
           pipeline) os dois tempos de carga, o número de linhas, o `RUNN
           ER_NAME` e o `summary.md` do k6. Copiar `summary.json`, `summa
           ry.md` e os logs para `target/load/`;
      140    6. sempre, num `trap`: `docker compose ... down -v` e remoçã
           o do container do passo 2.

● Update(specs\004-acabamento-operacional\tasks.md)
  ⎿  Added 1 line, removed 1 line
      152    - nenhum campo passa de 500 caracteres nem tem U+0000 (spec
           001, FR-006);
      153    - escrita num arquivo temporário na mesma pasta, movido com
           `ATOMIC_MOVE` no fim. Em erro de escrita, apagar o temporário
           e sair com 1;
      154    - no fim, imprimir o caminho, o número de filmes e o SHA-256
            (`MessageDigest`).
      155 -- [ ] T015 [US3] Conferir o T014 localmente: duas gerações com
          - os padrões dão o mesmo SHA-256; com `--seed 1`, o hash é outr
          -o; `--movies 0`, `--movies 2000001`, `--movies x` e sem `--out
          -put` saem com 2 e não criam arquivo. Anotar o SHA-256 do padrã
          -o gerado no Windows (Git Bash), para o README (SC-005).       
      155 +- [ ] T015 [US3] Conferir o T014 localmente: duas gerações com
          + os padrões dão o mesmo SHA-256; com `--seed 1`, o hash é outr
          +o; `--movies 0`, `--movies 2000001`, `--movies x` e sem `--out
          +put` saem com 2 e não criam arquivo. Anotar o SHA-256 do padrã
          +o gerado no Windows (Git Bash), para o README (SC-005), e grav
          +á-lo na constante `EXPECTED_SHA256_DEFAULT` do `.github/script
          +s/load-test.sh` (T013, passo 1).                              
      156  - [ ] T016 [US3] Subir `./mvnw spring-boot:run` com `MOVIES_CS
           V_PATH` apontando para o arquivo gerado (**verificar** se o va
           lor aceita um caminho de arquivo comum ou precisa do prefixo `
           file:`; anotar a forma certa para o README). Conferir: o resum
           o da carga com 100000 lidas, 100000 aceitas e 0 rejeitadas; a
           readiness 200; `min` com exatamente os dois produtores plantad
           os, intervalo 0, ordenados por `previousWin`; `max` com exatam
           ente os dois produtores plantados, de 1900 a 2099, ordenados p
           elo nome; tudo no formato do enunciado (FR-016, US3-3, US3-4).
      157  - [ ] T017 [P] [US2] Criar `compose.load.yaml`: sobrepõe só o
           serviço `app`, montando `./target/generated-data/movies.csv` e
           m `/data/movies.csv:ro` e definindo `MOVIES_CSV_PATH` com a fo
           rma do T016. Comentário no topo com a spec 004, FR-013, e o us
           o.
      158  - [ ] T018 [US2] Em `compose.yaml`, acrescentar o serviço `k6`
            (R4): imagem `grafana/k6:<versão estável>@sha256:<digest>`, `
           profiles: [load]`, `depends_on: app: condition: service_health
           y`, volume `./load:/scripts:ro` e `./target/load:/out`, `envir
           onment` com `BASE_URL: http://app:8080`, `API_TOKEN: ${API_TOK
           EN:-}` e as variáveis dos limites do T019 repassadas (`K6_*: $
           {K6_*:-}`), e `command: ["run", "/scripts/intervals.js"]`. **V
           erificar** se o usuário da imagem consegue escrever em `/out`
           montado; se não, `user:` com o UID do runner, documentado.

● Update(specs\004-acabamento-operacional\tasks.md)
  ⎿  Added 5 lines, removed 1 line
      164    - `Authorization: Bearer ${API_TOKEN}` só quando `API_TOKEN`
            não é vazio;
      165    - `handleSummary`: grava `/out/summary.json` e `/out/summary
           .md` (vazão, p95, p99 e taxa de erro por endpoint) e mantém o
           resumo de texto na saída padrão.
      166  - [ ] T020 [US2] Em `.github/workflows/ci.yml`, criar o job `l
           oad` (contracts/ci-pipeline.md): `needs: dockerfile-lint`, `ti
           meout-minutes: 30`, `permissions` do topo, `actions/checkout`
           e `actions/setup-java` (os mesmos hashes), o build da imagem c
           omo no job `image` (`load: true`, `tags: awards:ci`, cache `gh
           a`), `run: .github/scripts/load-test.sh awards:ci` e `actions/
           upload-artifact` (o mesmo hash) de `target/load/` com `if: alw
           ays()`, nome `relatorios-load`. Comentário com a spec 004, FR-
           010 a FR-019.
      167 -- [ ] T021 [US2] Rodar `.github/scripts/load-test.sh awards:lo
          -cal` localmente com o Docker: todos os passos `OK`. Rodar de n
          -ovo com `K6_P95_INTERVALS_MS=1`: o passo 4 precisa sair com 99
          - e o script com `FALHA` (SC-004). Anotar os dois resultados e 
          -os tempos de carga local, identificados como da máquina local,
          - na descrição do commit. Se o Docker não estiver disponível, `
          -PULADO` com o motivo; a comprovação fica no pipeline.         
      167 +- [ ] T021 [US2] Rodar `.github/scripts/load-test.sh awards:lo
          +cal` localmente com o Docker: todos os passos `OK`. Rodar de n
          +ovo com `K6_P95_INTERVALS_MS=1`: o passo 4 precisa sair com 99
          + e o script com `FALHA` (SC-004). Também com o Docker:        
      168 +  - **autenticação (US2-5):** com o compose e `API_TOKEN` defi
          +nido com um valor gerado na hora, nunca impresso, rodar o k6 c
          +om o mesmo `API_TOKEN`: as respostas são 200 e o k6 sai com 0.
          + Rodar o k6 com outro token: o `contract_errors` sobe, porque 
          +o 401 não é 200, e o k6 sai com 99, sem medir o 401 como latên
          +cia boa;                                                      
      169 +  - **memória (caso de borda da spec):** pelo passo 2 (carga d
          +ireta) do `load-test.sh`, com 500000, 1000000 e 2000000 filmes
          +, anotar o maior tamanho que fica pronto sem erro de memória c
          +om a configuração da imagem. Esse é o limite que o README info
          +rma (T062).                                                   
      170 +                                                              
      171 +  Anotar todos os resultados e os tempos de carga local, ident
          +ificados como da máquina local, na descrição do commit. O resu
          +ltado do limite impossível vai também para a entrada do T034. 
          +Se o Docker não estiver disponível, `PULADO` com o motivo; a c
          +omprovação fica no pipeline.                                  
      172  - [ ] T022 [US2] Rodar `.github/scripts/check-workflows.sh` (a
            imagem do k6 por digest e a action do job `load` por hash) e
           `.github/scripts/check-docker-context.sh` (nada de `tools/`, `
           load/` ou `target/generated-data/` no contexto da imagem): os
           dois precisam passar.
      173  - [ ] T023 [US2] Rodar `./mvnw verify`: verde, sem Docker. Inf
           ormar o resultado real.
      174  - [ ] T024 [US2] Commit `Etapa 2: gerador de CSV determinístic
           o e teste de carga com k6` com T013 a T023. Na descrição: os p
           ares tag → digest e tag → hash, o SHA-256 do T015 e os resulta
           dos do T021.

● Update(specs\004-acabamento-operacional\tasks.md)
  ⎿  Added 1 line, removed 1 line
      214  - [ ] T031 [US1] Em `.github/workflows/ci.yml`, criar o job `a
           lert-rules` (contracts/ci-pipeline.md): `timeout-minutes: 5`,
           `actions/checkout` e os três comandos do T030, cada um num pas
           so com nome próprio. Comentário com a spec 004, FR-004, FR-006
            e FR-008.
      215  - [ ] T032 [US1] Em `.github/scripts/compose-test.sh`, acresce
           ntar depois do passo 4 o passo `4b. Regras do SLO carregadas e
            nenhum alerta do SLO disparando` (FR-009, SC-003): em `http:/
           /localhost:9090/api/v1/rules`, conferir com `jq` que os grupos
            do `slo.yml` estão presentes e que nenhuma regra tem `health`
            diferente de `ok` nem `lastError`; em `/api/v1/alerts`, confe
           rir que nenhum alerta com o rótulo `slo` está `firing`. Atuali
           zar o cabeçalho do script e o nome do passo no job `compose` (
           `passos 0 a 8, 4b e 7b`).
      216  - [ ] T033 [US1] Limites finais do k6 (R5, FR-011a): em `load/
           intervals.js`, trocar os padrões provisórios do T019 por 2× a
           maior medição de cada percentil do T025, arredondada para cima
            (em ms inteiros), e `K6_MAX_ERROR_RATE=0.01`. Comentário com
           a origem dos números e o link da entrada do T034.
      217 -- [ ] T034 [US1] Registrar em `docs/ai/decisoes.md`, numa entr
          -ada "Medição do teste de carga e folga dos limites do pipeline
          -" (FR-011a): as 5 execuções do T025 (links e valores por endpo
          -int), o menor, o maior e a mediana, o tipo do runner, os objet
          -ivos do SLO (disponibilidade de 99,5% e 99% até o bucket do T0
          -26, em 28 dias), os limites finais do T033, a folga de 2× e o 
          -motivo (ruído do runner compartilhado, para o job não ficar in
          -stável).                                                      
      217 +- [ ] T034 [US1] Registrar em `docs/ai/decisoes.md`, numa entr
          +ada "Medição do teste de carga e folga dos limites do pipeline
          +" (FR-011a): as 5 execuções do T025 (links e valores por endpo
          +int), o menor, o maior e a mediana, o tipo do runner, os objet
          +ivos do SLO (disponibilidade de 99,5% e 99% até o bucket do T0
          +26, em 28 dias), os limites finais do T033, a folga de 2× e o 
          +motivo (ruído do runner compartilhado, para o job não ficar in
          +stável). Incluir a verificação única do limite impossível do T
          +021 (`K6_P95_INTERVALS_MS=1`, saída 99 do k6 e `FALHA` do scri
          +pt) e a execução com o token errado, como prova de que os limi
          +tes reprovam o job (SC-004).                                  
      218  - [ ] T035 [US1] Rodar localmente com o Docker: `.github/scrip
           ts/compose-test.sh`, com todos os passos `OK`, inclusive o 4b.
            Depois, a seção 4 de `specs/004-acabamento-operacional/quicks
           tart.md`: com `docker compose stop app`, o `AwardsAppDown` fic
           a `firing` em até 2 min, e o `runbook_url` aponta para `docs/r
           unbook.md#awardsappdown`. A âncora só existe depois do T060, e
           ntão conferir o formato agora e o destino na etapa 6. Também `
           load-test.sh` com os limites finais: `OK`. Sem o Docker, `PULA
           DO` com o motivo.
      219  - [ ] T036 [US1] Rodar `./mvnw verify`: verde, sem Docker. Inf
           ormar o resultado real.
      220  - [ ] T037 [US1] Commit `Etapa 3: alertas de SLO no Prometheus
            e limites do k6 pela medição` com T026 a T036. Na descrição:
           o `le` do T026, a forma do `promtool` do T027 e o resultado do
            T035.

● Update(specs\004-acabamento-operacional\tasks.md)
  ⎿  Added 1 line, removed 1 line
      302    - passos:
      303      1. `actions/checkout` e `docker/setup-buildx-action` (os m
           esmos hashes), com `driver-opts: network=host`, para o buildx
           alcançar `localhost:5000`;
      304      2. `docker/build-push-action` (o mesmo hash) com `push: tr
           ue`, `tags: localhost:5000/awards:ci` e o cache `gha`. Guardar
            `steps.<id>.outputs.digest`;
      305 -    3. Trivy (o mesmo hash) com `scan-type: image`, `image-ref
          -: localhost:5000/awards@<digest>`, `format: cyclonedx` e `outp
          -ut: sbom.cdx.json`. Depois, um passo `jq` que **falha** se `lz
          -4-java` ou `snappy-java` não estiverem no SBOM (FR-024);      
      305 +    3. Trivy (o mesmo hash) com `scan-type: image`, `image-ref
          +: localhost:5000/awards@<digest>`, `format: cyclonedx` e `outp
          +ut: sbom.cdx.json`. **Verificar** se o Trivy lê o registro HTT
          +P em `localhost`. Se não ler, há duas saídas: definir `TRIVY_I
          +NSECURE: "true"` no `env` do passo, ou acrescentar `load: true
          +` ao build e analisar `localhost:5000/awards:ci` pelo Docker l
          +ocal, conferindo que o `RepoDigests` é o mesmo digest. Registr
          +ar a escolha na R10. Depois, um passo `jq` que **falha** se `l
          +z4-java` ou `snappy-java` não estiverem no SBOM (FR-024);     
      306      4. `sigstore/cosign-installer` fixado por hash, com as ref
           erências aninhadas conferidas e anotadas;
      307      5. `cosign sign --yes localhost:5000/awards@<digest>` e `c
           osign attest --yes --type cyclonedx --predicate sbom.cdx.json
           localhost:5000/awards@<digest>`;
      308      6. `cosign verify` e `cosign verify-attestation --type cyc
           lonedx`, com `--certificate-identity "https://github.com/${{ g
           ithub.repository }}/.github/workflows/ci.yml@${{ github.ref }}
           "` e `--certificate-oidc-issuer https://token.actions.githubus
           ercontent.com`;

● Update(specs\004-acabamento-operacional\tasks.md)
  ⎿  Added 1 line, removed 1 line
      336 
      337  - [ ] T055 [US6] Criar `.devcontainer/devcontainer.json` (R11;
            contracts/operacao.md, "Dev container"):
      338    - **verificar** se existe a variante do JDK 25 da imagem `mc
           r.microsoft.com/devcontainers/java`. Se existir, usá-la por ta
           g e digest. Se não, usar `mcr.microsoft.com/devcontainers/base
           ` com o recurso `ghcr.io/devcontainers/features/java` (`versio
           n: 25`, `jdkDistro: tem`, `installMaven: false`) e atualizar a
            R11;
      339 -  - recurso `ghcr.io/devcontainers/features/docker-in-docker` 
          -com a versão exata (`:<x.y.z>`), com o compose v2;            
      339 +  - recurso `ghcr.io/devcontainers/features/docker-in-docker` 
          +fixado por digest (`@sha256:<digest>`, resolvido com `docker b
          +uildx imagetools inspect ghcr.io/devcontainers/features/docker
          +-in-docker:<x.y.z>` ou `oras manifest fetch`), com a versão `<
          +x.y.z>` num comentário e o compose v2. Se o recurso do Java fo
          +r usado (alternativa acima), também por digest;               
      340    - `forwardPorts: [8080, 9090, 3000]`, com `portsAttributes`
           nomeando a API, o Prometheus e o Grafana;
      341    - `postCreateCommand: ./mvnw -B -q dependency:go-offline`;
      342    - nenhum `containerEnv` com segredo;

● Update(specs\004-acabamento-operacional\tasks.md)
  ⎿  Added 1 line, removed 1 line
      354  **Purpose**: a documentação do marco, os registros e a confirm
           ação no pipeline. Papel de documentação para
      355  os arquivos de `docs/` e o README.
      356 
      357 -- [ ] T059 [P] Criar `docs/slo.md` (FR-003; data-model.md, "Ob
          -jetivo de SLO"): os dois objetivos (disponibilidade de 99,5% e
          - 99% das requisições dos intervalos até o bucket do T026, most
          -rando o valor exato), a janela de 28 dias, "ruim = 5xx ou alvo
          - sem coleta; boa = 2xx, 304 e 4xx", a fórmula do orçamento e d
          -as taxas de consumo, a origem dos números (link para a entrada
          - do T034), os limites do k6 e a folga de 2× sobre a maior medi
          -ção, com o motivo, e a tabela dos alertas com os links do runb
          -ook.                                                          
      357 +- [ ] T059 [P] Criar `docs/slo.md` (FR-003; data-model.md, "Ob
          +jetivo de SLO"): os dois objetivos (disponibilidade de 99,5% e
          + 99% das requisições dos intervalos até o bucket do T026, most
          +rando o valor exato), a janela de 28 dias, "ruim = 5xx ou alvo
          + sem coleta; boa = 2xx, 304 e 4xx", e, na latência, que o tota
          +l é o de todas as requisições aos intervalos, com qualquer sta
          +tus (como no T028), a fórmula do orçamento e das taxas de cons
          +umo, a origem dos números (link para a entrada do T034), os li
          +mites do k6 e a folga de 2× sobre a maior medição, com o motiv
          +o, e a tabela dos alertas com os links do runbook.            
      358  - [ ] T060 [P] Criar `docs/runbook.md` (FR-007): uma seção por
            alerta, com o título igual ao nome do alerta, para a âncora e
           m minúsculas do `runbook_url` funcionar (`#awardsintervalsavai
           labilitybudgetburn`, `#awardsintervalslatencybudgetburn`, `#aw
           ardsappdown`). Cada seção traz o que o alerta significa, o que
            conferir (painéis do dashboard, `docker compose logs app`, re
           adiness, atraso do consumidor, PostgreSQL e Kafka) e o que faz
           er. Conferir que as três âncoras abrem a seção certa no GitHub
            (conclui o T035).
      359  - [ ] T061 [P] Criar os ADRs `docs/adr/0020-slo-e-alertas-por-
           consumo-do-orcamento.md`, `0021-teste-de-carga-com-k6-no-pipel
           ine.md`, `0022-gerador-de-csv-deterministico.md`, `0023-cache-
           aot-da-jvm.md`, `0024-sbom-e-assinatura-keyless-sem-publicacao
           .md` e `0025-dev-container-e-inventario-efetivo-de-dependencia
           s.md`, no formato dos anteriores (FR-037), cada um com o conte
           xto, a decisão, as alternativas rejeitadas da research e as co
           nsequências. Atualizar `docs/adr/README.md` com os seis.
      360  - [ ] T062 Atualizar o `README.md` (FR-018, FR-036):

● Update(specs\004-acabamento-operacional\tasks.md)
  ⎿  Added 1 line, removed 1 line
      361    - seção do Marco 4 entre os extras, depois do Marco 3. Ela c
           obre:
      362      - alertas e SLO, com links para `docs/slo.md` e `docs/runb
           ook.md`;
      363      - teste de carga (como rodar, os números de uma execução v
           erde do job `load` e o link da execução);
      364 -    - gerador de CSV (uso, o SHA-256 do padrão no Linux e no W
          -indows do T015, os tempos de carga direta e por Kafka do job `
          -load`);                                                       
      364 +    - gerador de CSV (uso, o SHA-256 do padrão no Linux e no W
          +indows do T015, os tempos de carga direta e por Kafka do job `
          +load`, e o maior tamanho que carrega sem erro de memória, medi
          +do no T021);                                                  
      365      - cache AOT (as medianas do job `image`);
      366      - SBOM e assinatura (o que é assinado, onde fica o SBOM, c
           omo conferir a entrada no Rekor, sem publicação da imagem);
      367      - dev container (como abrir e o requisito de Docker aninha
           do);

● Update(specs\004-acabamento-operacional\plan.md)
  ⎿  Added 2 lines, removed 1 line
      130  | FR-014 a FR-016, FR-019, SC-005, SC-006 | job `load` (dois S
           HA-256 iguais; carga sem linhas rejeitadas nos dois caminhos)
           |
      131  | FR-017 | job `build` (suíte sem mudança com o `movielist.csv
           `); `check-docker-context.sh` (o gerado fora da imagem) |
      132  | FR-018 | job `load` (tempos no resumo); checklist (números n
           o README) |
      133 -| FR-020, FR-021, FR-023 | job `image` (fumaça) e job `compose
          -` (perfil docker com a mesma imagem) |                        
      133 +| FR-020, FR-021 | job `image` (fumaça) e job `compose` (perfi
          +l docker com a mesma imagem) |                                
      134 +| FR-023 | job `image` (passo de cache inválido do `startup-be
          +nchmark.sh`) |                                                
      135  | FR-022, SC-007 | job `image` (`startup-benchmark.sh`); check
           list (números no README) |
      136  | FR-024 a FR-027, SC-008 | job `supply-chain`; `workflow-poli
           cy` (política ampliada, com os casos negativos testados) |
      137  | FR-028 a FR-030, SC-009 | job `devcontainer`; `workflow-poli
           cy` (Dependabot `devcontainers`) |

● Update(specs\004-acabamento-operacional\plan.md)
  ⎿  Added 2 lines, removed 1 line
      134  | FR-023 | job `image` (passo de cache inválido do `startup-be
           nchmark.sh`) |
      135  | FR-022, SC-007 | job `image` (`startup-benchmark.sh`); check
           list (números no README) |
      136  | FR-024 a FR-027, SC-008 | job `supply-chain`; `workflow-poli
           cy` (política ampliada, com os casos negativos testados) |
      137 -| FR-028 a FR-030, SC-009 | job `devcontainer`; `workflow-poli
          -cy` (Dependabot `devcontainers`) |                            
      137 +| FR-028, FR-030, SC-009 | job `devcontainer` (`verify`); `wor
          +kflow-policy` (Dependabot `devcontainers`) |                  
      138 +| FR-029 | job `devcontainer` (compose pronto e resultado de r
          +eferência dentro do container); checklist (portas encaminhadas
          + para a máquina) |                                            
      139  | FR-031, SC-011 | checklist (registro da proteção) e página d
           o PR com os 12 checks obrigatórios |
      140  | FR-032, FR-034 | checklist (causa, evidência e verificação ú
           nica em branch apagado) |
      141  | FR-033, SC-010 | job `dependencies` (inventário com `lz4-jav
           a` e `snappy-java` no resumo) |
● Update(specs\004-acabamento-operacional\plan.md)
  ⎿  Added 1 line, removed 1 line
      56  - `grafana/k6` (imagem, tag e digest, serviço do compose);
      57  - `registry:3` (imagem, digest, *service container* do `supply-
          chain`);
      58  - `sigstore/cosign-installer` e `devcontainers/ci` (actions por
           hash);
      59 -- `mcr.microsoft.com/devcontainers/java` (digest) e o recurso `
         -docker-in-docker` (versão exata);                              
      59 +- `mcr.microsoft.com/devcontainers/java` e o recurso `docker-in
         +-docker`, ambos por digest;                                    
      60  - `promtool` da imagem do Prometheus que já está no compose;
      61  - Trivy, pela action que já está no pipeline, também para o SBO
          M
      62 

● Update(specs\004-acabamento-operacional\research.md)
  ⎿  Added 3 lines, removed 2 lines
      238 
      239  - **Decisão:** `.devcontainer/devcontainer.json` com:
      240    - a imagem `mcr.microsoft.com/devcontainers/java` na variant
           e do JDK 25, fixada por digest;
      241 -  - o recurso `ghcr.io/devcontainers/features/docker-in-docker
          -`, fixado por versão exata (com o compose                     
      242 -    v2);                                                      
      241 +  - o recurso `ghcr.io/devcontainers/features/docker-in-docker
          +`, fixado por digest (`@sha256:`), com a                      
      242 +    versão num comentário e o compose v2. É a mesma convenção 
          +das imagens, porque o recurso também é um                     
      243 +    artefato OCI;                                             
      244    - sem o recurso do Maven: o projeto usa o `./mvnw` (FR-028);
      245    - `forwardPorts` 8080, 9090 e 3000 (FR-029);
      246    - `postCreateCommand: ./mvnw -B -q dependency:go-offline`, p
           ara o primeiro `verify` não baixar tudo;

● Update(specs\004-acabamento-operacional\research.md)
  ⎿  Added 4 lines, removed 2 lines
      245    - `forwardPorts` 8080, 9090 e 3000 (FR-029);
      246    - `postCreateCommand: ./mvnw -B -q dependency:go-offline`, p
           ara o primeiro `verify` não baixar tudo;
      247    - `containerEnv` vazio de segredos.
      248 -- **Pipeline (FR-030):** job `devcontainer` com `devcontainers
          -/ci` (fixado por hash) rodando `java                          
      249 -  -version && ./mvnw -v && docker version && ./mvnw -B verify`
          - dentro do container.                                         
      248 +- **Pipeline (FR-029, FR-030):** job `devcontainer` com `devco
          +ntainers/ci` (fixado por hash) rodando                        
      249 +  dentro do container `java -version`, `./mvnw -v`, `docker ve
          +rsion`, `./mvnw -B verify`, `docker compose                   
      250 +  up -d --build --wait` e a conferência do resultado de referê
          +ncia. As portas encaminhadas para a máquina                   
      251 +  não existem no runner e são conferidas na revisão (checklist
          +).                                                            
      252  - **Dependabot:** ganha o ecossistema `devcontainers`, e o `ch
           eck-workflows.sh` passa a exigi-lo (regra 6).
      253  - **Verificar:** que existe a variante do JDK 25 da imagem `de
           vcontainers/java`. Se não existir, usar a
      254    imagem base `mcr.microsoft.com/devcontainers/base` com o rec
           urso `ghcr.io/devcontainers/features/java`

● Update(specs\004-acabamento-operacional\research.md)
  ⎿  Added 2 lines, removed 1 line
      179    -XX:AOTMode=off -jar application.jar` para o "sem" e o `ENTR
           YPOINT` normal para o "com". 7 repetições
      180    alternadas de cada, tempo do início do container à readiness
            200 e o `Started ... in X seconds` do log;
      181    a mediana de cada uma vai ao resumo do job `image`, e uma ex
           ecução verde, ao README. O script reprova se
      182 -  a mediana com cache não for menor (SC-007).                 
      182 +  a mediana com cache não for menor (SC-007). Um último passo 
          +sobe a imagem com um cache cortado e exige a                  
      183 +  readiness 200 e o aviso da JVM no log (FR-023), para que o j
          +ob `image` comprove a FR-023.                                 
      184  - **Verificar:**
      185    - se o `spring.context.exit=onRefresh` sai antes da carga do
            CSV e da abertura da porta. Se a carga
      186      rodar no refresh e o treino ficar lento, ou se o cache fic
           ar pequeno demais para ganhar tempo, treinar

● Update(specs\004-acabamento-operacional\contracts\ci-pipeline.md)
  ⎿  Added 1 line, removed 1 line
      11  | `alert-rules` | — | `contents: read` | `promtool check rules`
          , `check config` e `test rules`, com a imagem do Prometheus do
          compose | regra inválida ou teste de regra falhando | FR-004, F
          R-006, FR-008, SC-002 |
      12  | `load` | `dockerfile-lint` | `contents: read` | gera o CSV du
          as vezes e compara os SHA-256; mede a carga direta; sobe o comp
          ose com o CSV gerado; mede a carga por Kafka; roda o k6; public
          a o resumo e os artefatos; derruba o compose | hashes diferente
          s; linha rejeitada; readiness fora do tempo; limite do k6 ultra
          passado | FR-010 a FR-019, SC-004 a SC-006 |
      13  | `supply-chain` | `dockerfile-lint` | `contents: read`, `id-to
          ken: write` | registro temporário; build com push só para `loca
          lhost:5000`; SBOM; `cosign sign` e `attest`; verificação com a
          identidade certa e com uma errada | qualquer passo; a verificaç
          ão com identidade errada passando | FR-024 a FR-027, SC-008 |
      14 -| `devcontainer` | — | `contents: read` | constrói o dev contai
         -ner e roda `java -version`, `./mvnw -v`, `docker version` e `./
         -mvnw -B verify` dentro dele | qualquer comando | FR-028 a FR-03
         -0, SC-009 |                                                    
      14 +| `devcontainer` | — | `contents: read` | constrói o dev contai
         +ner e roda dentro dele `java -version`, `./mvnw -v`, `docker ve
         +rsion`, `./mvnw -B verify`, `docker compose up -d --build --wai
         +t` e a conferência do resultado de referência | qualquer comand
         +o | FR-028 a FR-030, SC-009 (as portas encaminhadas ficam na re
         +visão) |                                                       
      15 
      16  O `supply-chain` tem `if:` que o pula em pull request de fork (
          FR-027).
      17 

● Update(specs\004-acabamento-operacional\contracts\ci-pipeline.md)
  ⎿  Added 1 line, removed 1 line
      21  | --- | --- | --- |
      22  | `workflow-policy` | `check-workflows.sh`: `id-token: write` s
          ó no `supply-chain`; push só no `supply-chain` e só para `local
          host:5000/`; Dependabot com `devcontainers`. `check-unchanged-t
          ests.sh` também contra `v0.3.0` | FR-001, FR-026, FR-028 |
      23  | `dependencies` | analisa `target/runtime-deps` (copiado pelo
          `maven-dependency-plugin`, escopo runtime) em vez do `pom.xml`;
           resumo com as versões de `lz4-java` e `snappy-java` | FR-033,
          SC-010 |
      24 -| `image` | imagem com o cache AOT; teste de fumaça sem mudança
         - de passos; medição da subida sem e com o cache (7 repetições, 
         -mediana no resumo; reprova se a mediana com cache não for menor
         -); aviso no resumo quando o `supply-chain` é pulado | FR-020 a 
         -FR-023, FR-027, SC-007 |                                       
      24 +| `image` | imagem com o cache AOT; teste de fumaça sem mudança
         + de passos; medição da subida sem e com o cache (7 repetições, 
         +mediana no resumo; reprova se a mediana com cache não for menor
         +); subida com um cache cortado até a readiness, com o aviso da 
         +JVM no log; aviso no resumo quando o `supply-chain` é pulado | 
         +FR-020 a FR-023, FR-027, SC-007 |                              
      25  | `compose` | passo novo: regras carregadas sem erro em `/api/v
          1/rules` e nenhum alerta `slo` disparando em `/api/v1/alerts` |
           FR-009, SC-003 |
      26 
      27  ## Checks obrigatórios da `main` depois do marco (FR-031)

● Update(specs\004-acabamento-operacional\checklists\requirements.md)
  ⎿  Added 3 lines
      41    carga inicial, subida sem e com o cache), como verificar a as
          sinatura e como abrir o dev container; tabela
      42    de requisitos com o Marco 4; "Avalie em 2 minutos" ainda só c
          om o JDK.
      43  - [ ] ADRs (FR-037): um por decisão do marco, no formato dos an
          teriores.
      44 +- [ ] Dev container, portas encaminhadas (FR-029): com o dev co
         +ntainer aberto e o compose no ar, a API                        
      45 +  (8080), o Prometheus (9090) e o Grafana (3000) abrem na máqui
         +na. O job `devcontainer` comprova o compose                    
      46 +  e o resultado de referência dentro do container, mas não o en
         +caminhamento para a máquina.                                   
      47  - [ ] `docs/ai/decisoes.md`: proteção da `main` com a lista de
          checks e o bypass desligado (FR-031); causa
      48    da lacuna do job de dependências, com a evidência (FR-032); v
          erificação única contra a versão vulnerável
      49    (FR-034), feita em branch temporário apagado, sem commit de v
          olta ao marco; resultado do testador

● Update(specs\004-acabamento-operacional\tasks.md)
  ⎿  Added 1 line, removed 1 line
      132  ### Verificação da etapa 2 (escrever primeiro; precisa falhar)
      133 
      134  - [ ] T013 [US2] Criar `.github/scripts/load-test.sh` (contrac
           ts/ci-pipeline.md, "Scripts"; R4 a R7). Uso: `load-test.sh <im
           agem> [filmes]`, com 100000 filmes por padrão. Precisa de `doc
           ker`, `curl`, `jq` e `sha256sum`; sem eles, sai com 2. Passos,
            cada um com `OK`, `FALHA` ou `PULADO`:
      135 -  1. gerar `target/generated-data/a.csv` e `target/generated-d
          -ata/movies.csv` com `java tools/csv-generator/GenerateMovies.j
          -ava --movies <filmes> --output ...`, os dois com a semente pad
          -rão. Comparar os SHA-256: diferentes é `FALHA`. Escrever os do
          -is hashes no resumo (FR-019). Com os parâmetros padrão (100000
          - filmes e a semente padrão), comparar também com a constante `
          -EXPECTED_SHA256_DEFAULT` do script, que guarda o hash gerado n
          -o Windows no T015. Diferente é `FALHA` (SC-005, research R6). 
          -Com outro número de filmes, essa comparação é `PULADO` com o m
          -otivo, e esse `PULADO` não é falha mesmo com `CI=true`, porque
          - o job usa sempre os padrões;                                 
      135 +  1. gerar `target/generated-data/a.csv` e `target/generated-d
          +ata/movies.csv` com `java tools/csv-generator/GenerateMovies.j
          +ava --movies <filmes> --output ...`, os dois com a semente pad
          +rão. Comparar os SHA-256: diferentes é `FALHA`. Escrever os do
          +is hashes no resumo (FR-019). Com os parâmetros padrão (100000
          + filmes e a semente padrão), comparar também com a constante `
          +EXPECTED_SHA256_DEFAULT` do script, que guarda o hash gerado n
          +o Windows no T015. Diferente é `FALHA` (SC-005, research R6). 
          +Com outro número de filmes, essa comparação é `PULADO` com o m
          +otivo. Com `CI=true`, o script recusa um número de filmes dife
          +rente do padrão (saída 2), então no pipeline a comparação semp
          +re roda, e a regra "`PULADO` é falha no pipeline" continua val
          +endo;                                                         
      136    2. carga direta: `docker run -d` da imagem no modo padrão, c
           om `movies.csv` montado só leitura e `MOVIES_CSV_PATH` apontan
           do para ele. Medir do `docker run` à readiness 200, consultand
           o a cada 0,5 s, com limite de 600 s. No log do container, o re
           sumo da carga precisa ter zero rejeitadas. Os intervalos preci
           sam ter `min` e `max` não vazios. Remover o container (FR-018,
            SC-006);
      137    3. carga por Kafka: `docker compose -f compose.yaml -f compo
           se.load.yaml up -d --build --wait`. Medir do `State.StartedAt`
            do container `app` (`docker inspect`) à readiness 200. Confer
           ir os intervalos não vazios. No `docker compose logs app`, o r
           esumo da leitura do publicador precisa ter zero rejeitadas e o
            mesmo número de aceitas do passo 2 (SC-006, nos dois caminhos
           );
      138    4. `docker compose -f compose.yaml -f compose.load.yaml --pr
           ofile load run --rm k6`. O código de saída do k6 é o do passo
           (99 = limite ultrapassado);

● Ajustes aplicados. Agora rodo o /speckit-analyze de novo.

● Skill(speckit-analyze)
  ⎿  Loaded skill

  Ran 1 shell command (ctrl+o to expand)

● Análise de consistência do Marco 4, segunda rodada

  Apliquei os dez ajustes e rodei o /speckit-analyze de novo. Não sobrou nenhum
  problema crítico, alto ou médio, e apareceram dois baixos novos. Nenhum ajuste
  mudou o que a spec promete, só a forma de comprovar, e o spec.md não foi
  tocado. Não fiz commit.

  O que mudou

  ID: H1
  Onde: T057, T063, T066, plano, R11, ci-pipeline.md, checklist
  Mudança: O job devcontainer sobe o compose com docker compose up -d --build
    --wait, confere Joel Silver e Matthew Vaughn com jq e derruba o compose.
    As portas encaminhadas para a máquina viraram item de revisão no
    checklist.
  ────────────────────────────────────────
  ID: M1
  Onde: T038, T040, T043, plano, R8, ci-pipeline.md
  Mudança: O startup-benchmark.sh ganhou um passo que sobe a imagem com o
    cache cortado e exige readiness 200 e o aviso da JVM no log. Com isso, o
    job image passa a comprovar a FR-023. O T040 ficou só para anotar o texto
    exato do aviso.
  ────────────────────────────────────────
  ID: M2
  Onde: T021
  Mudança: Execução do k6 com a autenticação ligada: com o token certo, saída
    0; com o errado, o 401 conta como erro e o k6 sai com 99.
  ────────────────────────────────────────
  ID: M3
  Onde: T013 (passo 1), T015
  Mudança: O hash gerado no Windows fica na constante EXPECTED_SHA256_DEFAULT
    do script, e um hash diferente reprova o pipeline. No pipeline, o script
    recusa um número de filmes diferente do padrão, para a comparação nunca
    ficar PULADO.
  ────────────────────────────────────────
  ID: M4
  Onde: T013 (passo 3)
  Mudança: Na carga por Kafka, o script também exige zero linhas rejeitadas no
    log do publicador e o mesmo número de aceitas da carga direta.
  ────────────────────────────────────────
  ID: M5
  Onde: T048 (passo 3)
  Mudança: Novo "verificar" sobre o Trivy ler o registro HTTP: TRIVY_INSECURE
    ou load: true, com a escolha registrada na R10.
  ────────────────────────────────────────
  ID: M6
  Onde: T055, R11, plano
  Mudança: O recurso docker-in-docker passa a ser fixado por digest, com a
    versão num comentário.
  ────────────────────────────────────────
  ID: L1
  Onde: T021, T034
  Mudança: O teste com o limite impossível e o teste com o token errado vão
    para docs/ai/decisoes.md, na entrada da medição.
  ────────────────────────────────────────
  ID: L2
  Onde: T059
  Mudança: A página do SLO diz que, na latência, o total conta todas as
    requisições aos intervalos, com qualquer status.
  ────────────────────────────────────────
  ID: L3
  Onde: T021, T062
  Mudança: A carga direta é medida com 500 mil, 1 milhão e 2 milhões de
    filmes, e o maior tamanho que sobe sem erro de memória vai para o README.

  Resultado da nova análise

  ID: L4
  Categoria: Subespecificação
  Severidade: LOW
  Onde: tasks T021 (memória) e T013
  Problema: O T021 manda medir a memória rodando só o passo 2 do load-test.sh,
    mas o T013 não prevê rodar um passo isolado.
  Recomendação: Acrescentar ao T013 uma opção --only-direct (ou uma variável
    LOAD_STEPS) para rodar só a carga direta, sem o compose.
  ────────────────────────────────────────
  ID: L5
  Categoria: Subespecificação
  Severidade: LOW
  Onde: tasks T021 (token errado), T018
  Problema: O serviço k6 recebe o mesmo API_TOKEN do compose. O T021 não diz
    como mandar um token diferente só ao k6.
  Recomendação: Indicar no T021: docker compose ... --profile load run --rm -e
    API_TOKEN=<outro> k6.

  Cobertura

  ┌───────────────┬──────────┬────────────────────┬─────────────────────────┐
  │   Requisito   │   Tem    │      Tarefas       │       Observação        │
  │               │ tarefa?  │                    │                         │
  ├───────────────┼──────────┼────────────────────┼─────────────────────────┤
  │ FR-001 a      │          │                    │ SC-005 e SC-006         │
  │ FR-022 e      │ Sim      │ ver a primeira     │ passaram a ser          │
  │ FR-011a       │          │ análise            │ comprovados pelo        │
  │               │          │                    │ pipeline                │
  ├───────────────┼──────────┼────────────────────┼─────────────────────────┤
  │ FR-023        │ Sim      │ T038, T043         │ antes era manual (M1)   │
  ├───────────────┼──────────┼────────────────────┼─────────────────────────┤
  │ FR-024 a      │          │                    │                         │
  │ FR-028,       │ Sim      │ T047 a T057        │                         │
  │ FR-030        │          │                    │                         │
  ├───────────────┼──────────┼────────────────────┼─────────────────────────┤
  │               │          │ T057 (job) e T066  │                         │
  │ FR-029        │ Sim      │ (checklist das     │ antes era só local (H1) │
  │               │          │ portas)            │                         │
  ├───────────────┼──────────┼────────────────────┼─────────────────────────┤
  │ FR-031 a      │ Sim      │ ver a primeira     │                         │
  │ FR-038        │          │ análise            │                         │
  └───────────────┴──────────┴────────────────────┴─────────────────────────┘

  Constituição: nenhuma violação.
  - Princípio VI: a FR-023 e a FR-029 agora têm comprovação do tipo certo. Para
    o encaminhamento de portas, que o runner não alcança, a comprovação é a
    revisão.
  - Princípios I, II e VII: sem mudança.

  Tarefas sem requisito: só T001 e T002, a preparação.

  Métricas
  - Requisitos: 39 (FRs) e 11 critérios de sucesso.
  - Tarefas: 68, sem mudança de número.
  - Cobertura: 100%, todos com comprovação do tipo certo.
  - Ambiguidades: 0. Duplicações: 0.
  - Críticos: 0. Altos: 0. Médios: 0. Baixos: 2.

  Próximos passos

  Dá para seguir para o /speckit-implement. O L4 e o L5 são detalhes de como
  rodar e posso resolvê-los quando chegar no T013 e no T021, ou aplicá-los agora
  no tasks.md.

  Quer que eu aplique o L4 e o L5 antes de começar?

✻ Cogitated for 2m 8s · done 14:14

❯ Sim, aplique o L4 e o L5 agora no tasks.md. Não faça commit.                  

● Update(specs\004-acabamento-operacional\tasks.md)
  ⎿  Added 2 lines
      139    5. escrever no `$GITHUB_STEP_SUMMARY` (ou na saída, fora do
           pipeline) os dois tempos de carga, o número de linhas, o `RUNN
           ER_NAME` e o `summary.md` do k6. Copiar `summary.json`, `summa
           ry.md` e os logs para `target/load/`;
      140    6. sempre, num `trap`: `docker compose ... down -v` e remoçã
           o do container do passo 2.
      141 
      142 +  Opção `--only-direct`, como terceiro argumento: roda só os p
          +assos 1 e 2 (geração e carga direta) e o passo 5, sem o compos
          +e nem o k6. Ela serve à medição de memória do T021. Com `CI=tr
          +ue`, a opção é recusada (saída 2): o job `load` roda sempre to
          +dos os passos.                                                
      143 +                                                              
      144    Rodar antes de existir o gerador: precisa sair com `FALHA` n
           o passo 1.
      145 
      146  ### Implementação da etapa 2

● Update(specs\004-acabamento-operacional\tasks.md)
  ⎿  Added 1 line, removed 1 line
      131 
      132  ### Verificação da etapa 2 (escrever primeiro; precisa falhar)
      133 
      134 -- [ ] T013 [US2] Criar `.github/scripts/load-test.sh` (contrac
          -ts/ci-pipeline.md, "Scripts"; R4 a R7). Uso: `load-test.sh <im
          -agem> [filmes]`, com 100000 filmes por padrão. Precisa de `doc
          -ker`, `curl`, `jq` e `sha256sum`; sem eles, sai com 2. Passos,
          - cada um com `OK`, `FALHA` ou `PULADO`:                       
      134 +- [ ] T013 [US2] Criar `.github/scripts/load-test.sh` (contrac
          +ts/ci-pipeline.md, "Scripts"; R4 a R7). Uso: `load-test.sh <im
          +agem> [filmes] [--only-direct]`, com 100000 filmes por padrão.
          + Precisa de `docker`, `curl`, `jq` e `sha256sum`; sem eles, sa
          +i com 2. Passos, cada um com `OK`, `FALHA` ou `PULADO`:       
      135    1. gerar `target/generated-data/a.csv` e `target/generated-d
           ata/movies.csv` com `java tools/csv-generator/GenerateMovies.j
           ava --movies <filmes> --output ...`, os dois com a semente pad
           rão. Comparar os SHA-256: diferentes é `FALHA`. Escrever os do
           is hashes no resumo (FR-019). Com os parâmetros padrão (100000
            filmes e a semente padrão), comparar também com a constante `
           EXPECTED_SHA256_DEFAULT` do script, que guarda o hash gerado n
           o Windows no T015. Diferente é `FALHA` (SC-005, research R6).
           Com outro número de filmes, essa comparação é `PULADO` com o m
           otivo. Com `CI=true`, o script recusa um número de filmes dife
           rente do padrão (saída 2), então no pipeline a comparação semp
           re roda, e a regra "`PULADO` é falha no pipeline" continua val
           endo;
      136    2. carga direta: `docker run -d` da imagem no modo padrão, c
           om `movies.csv` montado só leitura e `MOVIES_CSV_PATH` apontan
           do para ele. Medir do `docker run` à readiness 200, consultand
           o a cada 0,5 s, com limite de 600 s. No log do container, o re
           sumo da carga precisa ter zero rejeitadas. Os intervalos preci
           sam ter `min` e `max` não vazios. Remover o container (FR-018,
            SC-006);
      137    3. carga por Kafka: `docker compose -f compose.yaml -f compo
           se.load.yaml up -d --build --wait`. Medir do `State.StartedAt`
            do container `app` (`docker inspect`) à readiness 200. Confer
           ir os intervalos não vazios. No `docker compose logs app`, o r
           esumo da leitura do publicador precisa ter zero rejeitadas e o
            mesmo número de aceitas do passo 2 (SC-006, nos dois caminhos
           );

● Update(specs\004-acabamento-operacional\tasks.md)
  ⎿  Added 1 line, removed 1 line
      167    - `handleSummary`: grava `/out/summary.json` e `/out/summary
           .md` (vazão, p95, p99 e taxa de erro por endpoint) e mantém o
           resumo de texto na saída padrão.
      168  - [ ] T020 [US2] Em `.github/workflows/ci.yml`, criar o job `l
           oad` (contracts/ci-pipeline.md): `needs: dockerfile-lint`, `ti
           meout-minutes: 30`, `permissions` do topo, `actions/checkout`
           e `actions/setup-java` (os mesmos hashes), o build da imagem c
           omo no job `image` (`load: true`, `tags: awards:ci`, cache `gh
           a`), `run: .github/scripts/load-test.sh awards:ci` e `actions/
           upload-artifact` (o mesmo hash) de `target/load/` com `if: alw
           ays()`, nome `relatorios-load`. Comentário com a spec 004, FR-
           010 a FR-019.
      169  - [ ] T021 [US2] Rodar `.github/scripts/load-test.sh awards:lo
           cal` localmente com o Docker: todos os passos `OK`. Rodar de n
           ovo com `K6_P95_INTERVALS_MS=1`: o passo 4 precisa sair com 99
            e o script com `FALHA` (SC-004). Também com o Docker:
      170 -  - **autenticação (US2-5):** com o compose e `API_TOKEN` defi
          -nido com um valor gerado na hora, nunca impresso, rodar o k6 c
          -om o mesmo `API_TOKEN`: as respostas são 200 e o k6 sai com 0.
          - Rodar o k6 com outro token: o `contract_errors` sobe, porque 
          -o 401 não é 200, e o k6 sai com 99, sem medir o 401 como latên
          -cia boa;                                                      
      170 +  - **autenticação (US2-5):** com o compose e `API_TOKEN` defi
          +nido com um valor gerado na hora, nunca impresso, rodar o k6 c
          +om o mesmo `API_TOKEN`: as respostas são 200 e o k6 sai com 0.
          + Rodar o k6 com outro token, sobrescrevendo só a variável do s
          +erviço `k6`: `docker compose -f compose.yaml -f compose.load.y
          +aml --profile load run --rm -e API_TOKEN=<outro valor gerado n
          +a hora> k6`. O `app` continua com o token original. O `contrac
          +t_errors` sobe, porque o 401 não é 200, e o k6 sai com 99, sem
          + medir o 401 como latência boa;                               
      171    - **memória (caso de borda da spec):** pelo passo 2 (carga d
           ireta) do `load-test.sh`, com 500000, 1000000 e 2000000 filmes
           , anotar o maior tamanho que fica pronto sem erro de memória c
           om a configuração da imagem. Esse é o limite que o README info
           rma (T062).
      172 
      173    Anotar todos os resultados e os tempos de carga local, ident
           ificados como da máquina local, na descrição do commit. O resu
           ltado do limite impossível vai também para a entrada do T034.
           Se o Docker não estiver disponível, `PULADO` com o motivo; a c
           omprovação fica no pipeline.

● Update(specs\004-acabamento-operacional\tasks.md)
  ⎿  Added 1 line, removed 1 line
      168  - [ ] T020 [US2] Em `.github/workflows/ci.yml`, criar o job `l
           oad` (contracts/ci-pipeline.md): `needs: dockerfile-lint`, `ti
           meout-minutes: 30`, `permissions` do topo, `actions/checkout`
           e `actions/setup-java` (os mesmos hashes), o build da imagem c
           omo no job `image` (`load: true`, `tags: awards:ci`, cache `gh
           a`), `run: .github/scripts/load-test.sh awards:ci` e `actions/
           upload-artifact` (o mesmo hash) de `target/load/` com `if: alw
           ays()`, nome `relatorios-load`. Comentário com a spec 004, FR-
           010 a FR-019.
      169  - [ ] T021 [US2] Rodar `.github/scripts/load-test.sh awards:lo
           cal` localmente com o Docker: todos os passos `OK`. Rodar de n
           ovo com `K6_P95_INTERVALS_MS=1`: o passo 4 precisa sair com 99
            e o script com `FALHA` (SC-004). Também com o Docker:
      170    - **autenticação (US2-5):** com o compose e `API_TOKEN` defi
           nido com um valor gerado na hora, nunca impresso, rodar o k6 c
           om o mesmo `API_TOKEN`: as respostas são 200 e o k6 sai com 0.
            Rodar o k6 com outro token, sobrescrevendo só a variável do s
           erviço `k6`: `docker compose -f compose.yaml -f compose.load.y
           aml --profile load run --rm -e API_TOKEN=<outro valor gerado n
           a hora> k6`. O `app` continua com o token original. O `contrac
           t_errors` sobe, porque o 401 não é 200, e o k6 sai com 99, sem
            medir o 401 como latência boa;
      171 -  - **memória (caso de borda da spec):** pelo passo 2 (carga d
          -ireta) do `load-test.sh`, com 500000, 1000000 e 2000000 filmes
          -, anotar o maior tamanho que fica pronto sem erro de memória c
          -om a configuração da imagem. Esse é o limite que o README info
          -rma (T062).                                                   
      171 +  - **memória (caso de borda da spec):** com `.github/scripts/
          +load-test.sh awards:local <filmes> --only-direct` para 500000,
          + 1000000 e 2000000 filmes, anotar o maior tamanho que fica pro
          +nto sem erro de memória com a configuração da imagem. Esse é o
          + limite que o README informa (T062).                          
      172 
      173    Anotar todos os resultados e os tempos de carga local, ident
           ificados como da máquina local, na descrição do commit. O resu
           ltado do limite impossível vai também para a entrada do T034.
           Se o Docker não estiver disponível, `PULADO` com o motivo; a c
           omprovação fica no pipeline.
      174  - [ ] T022 [US2] Rodar `.github/scripts/check-workflows.sh` (a
            imagem do k6 por digest e a action do job `load` por hash) e
           `.github/scripts/check-docker-context.sh` (nada de `tools/`, `
           load/` ou `target/generated-data/` no contexto da imagem): os
           dois precisam passar.

● Apliquei o L4 e o L5 no tasks.md, sem commit.

  - L4 (T013 e T021): o load-test.sh ganha a opção --only-direct. Com ela, o
    script só gera o arquivo, faz a carga direta e escreve o resumo, sem compose
    e sem k6. A medição de memória do T021 passa a usar load-test.sh 
    awards:local <filmes> --only-direct com 500 mil, 1 milhão e 2 milhões de
    filmes. No pipeline (CI=true), o script recusa a opção e sai com 2, então o
    job load sempre roda todos os passos.
  - L5 (T021): o teste com o token errado sobrescreve só a variável do k6, com
    docker compose -f compose.yaml -f compose.load.yaml --profile load run --rm 
    -e API_TOKEN=<outro valor gerado na hora> k6. O app continua com o token
    original.

  Com isso, a análise fica sem nenhum problema em aberto. O tasks.md continua
  com 68 tarefas. Próximo passo: /speckit-implement, a partir da etapa 1.

✻ Churned for 17s · done 14:23

❯ export docs/ai/sessoes/12-marco-4.md                                          

  Ran 1 shell command (ctrl+o to expand)

● Não exportei a sessão: o export é um comando nativo do Claude Code, e eu não
  consigo executá-lo por você. As outras sessões em docs/ai/sessoes/ (de 00 a
  11) são saídas reais desse comando, e uma transcrição escrita por mim não
  seria equivalente.

  Para gerar o arquivo, digite no prompt, com a barra:

  /export docs/ai/sessoes/12-marco-4.md

  Lembretes:
  - Pelo AGENTS.md, a exportação da sessão de um bolt vem depois do merge, junto
    com o pipeline verde. Se esta for só a sessão de planejamento do Marco 4
    (spec, plano, tarefas e análises), um nome como 12-marco-4-planejamento.md
    deixa espaço para a sessão de implementação depois.
  - Antes de versionar, confira que não há dado pessoal no arquivo. O e-mail não
    apareceu nesta conversa, mas o cabeçalho do export mostra o caminho local
    D:\Repo\PESSOAIS\..., como nos arquivos anteriores.

✻ Churned for 16s · done 14:25

❯ /export                                                                       
  ⎿  Export cancelled