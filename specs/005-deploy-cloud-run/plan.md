# Implementation Plan: Marco 5 — Publicação da API no Google Cloud Run

**Branch**: `marco/5-deploy` | **Date**: 2026-11-26 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `specs/005-deploy-cloud-run/spec.md`

## Summary

A API passa a ser publicada no Cloud Run (`outsera-awards`, `southamerica-east1`), só no modo padrão, com 0 a 1
instância e acesso anônimo. A publicação é feita pelo pipeline a cada push na `main`, depois dos 10 checks
obrigatórios.

- **Identidade:** o pipeline se autentica por Workload Identity Federation, restrita ao repositório (pelo ID
  numérico) e à `main`, sem chaves.
- **Imagem:** é a mesma do job `image`. O build gera, numa única execução, a imagem testada no Docker e um tar
  OCI; o tar segue como artefato e chega ao Artifact Registry com o digest preservado (research R1).
- **Implantação:** cada revisão sobe sem tráfego, é conferida pela URL própria, recebe o tráfego e é conferida
  de novo na URL pública (research R6).
- **Configuração do GCP:** fica num script bash idempotente, que o Claude Code roda depois do login do
  Anderson. O script também cria o serviço, para que as permissões fiquem no nível do serviço e do
  repositório, e não do projeto (research R4).
- **O que não muda:** o código Java, os ITs e os dois comandos Maven.

## Technical Context

**Language/Version**: Bash (scripts de configuração e de verificação); YAML do GitHub Actions. A aplicação
continua em Java 25 e Spring Boot 4.1.2, sem mudança.

**Primary Dependencies**: Google Cloud CLI (versão fixada no `setup-gcloud`); `google-github-actions/auth`
v3.0.0 e `setup-gcloud` v3.0.1; `actions/download-artifact` v8.0.2; `skopeo` e `jq` do runner; Docker
Buildx (já usado).

**Storage**: H2 em memória dentro da instância (modo padrão); Artifact Registry para as imagens.

**Testing**: nenhum IT novo (FR-022, FR-026). Comprovação pelo pipeline (jobs `workflow-policy`, `image` e
`deploy`), pelas duas execuções do script e pelo `verify-deployment.sh` contra a imagem local, com um caso que
aprova e outro que reprova.

**Target Platform**: Cloud Run (serviço totalmente gerenciado), `linux/amd64`; runners `ubuntu-latest`.

**Project Type**: serviço web; este marco é de entrega (infraestrutura e pipeline).

**Performance Goals**: resultado de referência em até 30 s com partida a frio e em até 2 s com a instância
ativa (SC-001); job `deploy` abaixo de 10 min (SC-005).

**Constraints**: sem segredos e sem chaves (FR-009, FR-013); actions fixadas por hash, sem `uses:` aninhado não
conferido; sem rebuild no `deploy`; permissões mínimas; custo zero com o serviço ocioso (SC-006); o modo
padrão intacto.

**Scale/Scope**: 1 serviço, 1 instância no máximo, 1 repositório de imagens, 2 contas de serviço, 1 pool e
1 provedor; 2 scripts novos e mudanças em 1 workflow e 1 script de política.

## Constitution Check

*GATE: avaliado antes da Phase 0 e de novo depois da Phase 1.*

| Princípio | Situação | Como o plano atende |
| --- | --- | --- |
| I. Só testes de integração | ✅ | Nenhum teste novo na API e nenhum teste unitário. Não há comportamento novo da aplicação. O resultado de referência é conferido por HTTP na URL publicada |
| II. Execução só com `./mvnw` | ✅ | Nada muda no `pom.xml`, nos ITs ou na configuração. Cada etapa termina com `./mvnw verify` e `./mvnw -Pextras verify` verdes, sem Docker |
| III. Contrato idêntico | ✅ | A conferência compara o corpo exato com a `REFERENCE` do teste de fumaça, sem campos extras (FR-007, FR-014) |
| IV. Spec-anchored | ✅ | A spec e a clarificação vêm antes do código. As divergências do briefing (publicação por merge, e não por tag) são registradas em `docs/ai/decisoes.md` (FR-025) |
| V. Extras isolados | ✅ | O deploy é um extra com infraestrutura própria, ativado só no job da `main`. Fica fora do modo padrão e não muda a imagem. Sem Kubernetes, sem imagem nativa e sem tracing |
| VI. Rastreabilidade | ✅ | Pipeline e imagem são comprovados pelo pipeline (jobs `image` e `deploy`). O script, pelas duas execuções registradas. A documentação, pelo checklist da spec. Não há requisito de comportamento sem IT |
| VII. Sem segredos | ✅ | OIDC e WIF, sem chave. Só identificadores públicos, versionados no workflow. `secrets.` continua proibido pela política. Autenticação desligada, como no modo padrão. Nenhuma credencial de demonstração no ambiente publicado |
| Cadeia de entrega | ✅ | As três actions novas são fixadas por hash e conferidas sem `uses:` aninhado (research R2). A imagem continua multi-stage, não root e sem mudança |
| Observabilidade | ✅ | O JSON já é ligado pela variável que a imagem define. Os probes de startup e liveness usam a readiness e a liveness da aplicação |
| Fluxo | ✅ | Bolt único no branch `marco/5-deploy`, em etapas. PR, merge e tags são do Anderson. O script é rodado pelo Claude Code só depois do login do Anderson |

**Pós-design (depois da Phase 1):** sem violações. Dois pontos de atenção, sem exceção constitucional:

- A política do workflow é ampliada (research R8). A ampliação é estreita: só o job `deploy` ganha escrita
  (`id-token`) e publicação, e novas regras impedem rebuild e exigem os 10 checks.
- `max-instances=1` é por revisão, então duas instâncias podem coexistir por instantes na troca. No modo padrão
  isso não fere o ADR 0026 (FR-003, research R5), e o ADR do marco registra o motivo.
- As métricas ficam fechadas no Cloud Run por uma variável da publicação, a única nova
  (`MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=health`; FR-004a), sem mudar o modo padrão nem a imagem.

## Project Structure

### Documentation (this feature)

```text
specs/005-deploy-cloud-run/
├── spec.md
├── plan.md                    # este arquivo
├── research.md                # R1 a R11
├── data-model.md              # recursos do GCP, IAM e estados da revisão
├── quickstart.md              # roteiro de validação
├── contracts/
│   ├── setup-script.md        # deploy/gcp/setup.sh
│   ├── verify-deployment.md   # .github/scripts/verify-deployment.sh
│   ├── ci-deploy.md           # jobs image (alterado) e deploy (novo)
│   └── workflow-policy.md     # check-workflows.sh (regras 3, 5, 9 e 10)
├── checklists/
│   └── requirements.md
└── tasks.md                   # /speckit-tasks
```

### Source Code (repository root)

```text
deploy/gcp/
├── setup.sh                   # novo: configuração idempotente do GCP
└── cleanup-policy.json        # novo: retenção do Artifact Registry
.github/
├── workflows/ci.yml           # alterado: concorrência do topo, job image (saída OCI e digest), job deploy
└── scripts/
    ├── check-workflows.sh     # alterado: regras 3, 5, 9 e 10
    └── verify-deployment.sh   # novo: readiness, referência, Swagger e contrato
docs/
├── adr/0027-publicacao-no-cloud-run.md   # novo
├── adr/README.md              # alterado: índice
├── runbook.md                 # alterado: volta do tráfego e falha de publicação
└── ai/decisoes.md             # alterado: publicação por merge; serviço criado pelo script
README.md                      # alterado: URL em "Avalie em 2 minutos", seção e requisitos do Marco 5
```

**Structure Decision**: o script do GCP fica em `deploy/gcp/`, que o `.dockerignore` exclui por padrão (lista de
permissão), separado dos scripts do pipeline em `.github/scripts/`. O `verify-deployment.sh` fica com os
scripts do pipeline, ao lado do `smoke-test.sh`, de onde vem a `REFERENCE`. Nenhum arquivo em `src/` muda.

## Etapas do bolt

Cada etapa termina com `./mvnw verify` e `./mvnw -Pextras verify` verdes, com o resultado real informado, e
tem commits próprios.

**Donos nesta rodada (AGENTS.md, um dono por arquivo compartilhado):** esta sessão do Claude Code acumula os
papéis de implementador, de infraestrutura e de documentação do Marco 5 e é a **única dona**, nesta rodada, de
`.github/workflows/ci.yml`, `.github/scripts/check-workflows.sh`, `.github/scripts/verify-deployment.sh`,
`deploy/gcp/`, `README.md`, `docs/adr/`, `docs/runbook.md` e `docs/ai/decisoes.md`. Nenhuma outra sessão
trabalha nesses arquivos ao mesmo tempo. A revisão do PR continua com o Codex.

1. **Etapa 1 — Configuração do GCP** (FR-017 a FR-021; US4)
   - `deploy/gcp/setup.sh` e `cleanup-policy.json`, conforme [`contracts/setup-script.md`](contracts/setup-script.md).
   - O Anderson faz o login. O Claude Code roda o script duas vezes, e a saída das duas execuções fica nas
     tarefas, com a segunda sem mudanças.
   - Conferências manuais do quickstart (sem chaves e com a condição do provedor).
   - Os identificadores impressos alimentam a etapa 3.
   - Fecha com os dois `./mvnw` verdes.
2. **Etapa 2 — Política do workflow e imagem como artefato** (FR-002, FR-010 parcial e FR-012)
   - `check-workflows.sh` com as regras 3, 5, 9 e 10. Os sete casos negativos de
     [`contracts/workflow-policy.md`](contracts/workflow-policy.md) são conferidos.
   - Concorrência do topo sem cancelamento na `main`.
   - Job `image` com as duas saídas, o passo do digest e a identidade da imagem testada, e o artefato `awards-oci`
     só em push na `main`.
   - O PR mostra o `workflow-policy` e o `image` verdes.
   - Fecha com os dois `./mvnw` verdes.
3. **Etapa 3 — Job `deploy` e conferência** (FR-001 a FR-016; US1 a US3)
   - `verify-deployment.sh`, testado contra a imagem local nos dois casos do quickstart.
   - Job `deploy` conforme [`contracts/ci-deploy.md`](contracts/ci-deploy.md), com a versão do `gcloud`
     fixada e os flags de probe conferidos nela (research R5).
   - No PR, o job aparece como pulado. A prova real vem com o merge.
   - Fecha com os dois `./mvnw` verdes.
4. **Etapa 4 — Documentação** (FR-023 a FR-025; US5)
   - README: URL em "Avalie em 2 minutos", com o aviso da partida a frio; seção do Marco 5; requisitos do
     Marco 5.
   - ADR 0027; índice dos ADRs; runbook (volta do tráfego e publicação com falha).
   - `docs/ai/decisoes.md`.
   - Itens de revisão no checklist da spec.
   - Fecha com os dois `./mvnw` verdes.
5. **Depois do merge** (Anderson faz o merge)
   - A execução da `main` publica e confere.
   - O resumo do job `deploy` é a comprovação de FR-001 a FR-016 e SC-001, SC-002, SC-005 e SC-006. O
     link dela é registrado na spec, que passa a `Implemented`, num commit seguinte.
   - Sessão exportada.

## Riscos

| Risco | Mitigação |
| --- | --- |
| Um comando `gcloud run` pede uma permissão de projeto além das do serviço | Acrescentar só o papel de leitura que faltar (`roles/run.viewer`), com o motivo no script (research R4) |
| O Docker do runner usa o armazenamento do containerd, e o `Id` muda de significado | A conferência aceita o digest da configuração ou o do manifesto, e nenhum outro (research R1) |
| O `skopeo` some da imagem do runner | O job confere a presença e falha com mensagem clara. A alternativa é o `crane`, numa mudança revisada |
| A primeira execução real só acontece depois do merge | Script e verificação testados antes (etapas 1 e 3). Se a primeira publicação falhar, o tráfego continua na revisão de exemplo, e a correção vem num PR seguinte |
| Custo inesperado | 0 a 1 instância, cobrança por requisição, retenção de imagens e o alerta de orçamento do Anderson |

## Complexity Tracking

Sem violações da constituição. Nada a justificar.
