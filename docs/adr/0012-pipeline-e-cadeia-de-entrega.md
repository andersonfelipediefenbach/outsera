# 0012. Pipeline com actions por hash, portas de segurança e cobertura e Dependabot

- **Status:** Aceito
- **Data:** 2026-11-23
- **Origem:** [`docs/ai/decisoes.md`](../ai/decisoes.md), entradas "Critérios de reprovação do pipeline:
  segurança e cobertura (spec 002)", "Correções do `/speckit-analyze` da spec 002" (C1, G1, G2) e
  "Correções da segunda análise da spec 002" (M1, M2, M4); research R7 e R9 a R14 da spec 002.

## Contexto

O fluxo de trabalho exige merge só com o pipeline verde, e não havia pipeline. O briefing cita os ataques
de 2026 à cadeia de entrega, e o responsável apontou o ataque ao `setup-trivy`, que a `trivy-action` usa
por dentro.

## Decisão

- Um workflow, `.github/workflows/ci.yml`, em todo push e pull request, com os jobs:
  - `workflow-policy`, que confere a própria política do workflow;
  - `build`, com `./mvnw verify` e a cobertura;
  - `codeql`, `dependencies` e `dockerfile-lint`;
  - `image`, com o build da imagem, a análise de vulnerabilidades dela e o teste de fumaça.
- O workflow não usa segredos e não publica nenhuma imagem.
- **Actions fixadas por hash de commit.** As referências aninhadas das actions compostas e Docker
  (`trivy-action` e `hadolint-action`) também foram conferidas, por hash ou digest. O
  `check-workflows.sh` reprova:
  - qualquer `uses:` sem hash;
  - `permissions` diferente de `contents: read`, com `security-events: write` só no `codeql`;
  - referência a `secrets.`;
  - publicação de imagem;
  - Dependabot incompleto.
- **Portas:**
  - CodeQL reprova achados com `security-severity` de 7,0 ou mais.
  - Trivy, nas dependências e na imagem, reprova `HIGH` e `CRITICAL` só quando há versão corrigida. Os
    achados médios e baixos aparecem no relatório.
  - hadolint reprova avisos e erros.
  - JaCoCo, sobre os testes de integração, exige 90% de linhas, também no `./mvnw verify` local.
- **Dependabot** semanal para `maven`, `github-actions` e `docker`.

## Alternativas consideradas

- Actions por tag: é exatamente o vetor dos ataques de 2026.
- OWASP Dependency-Check: lento e precisa de uma chave da API do NVD, um segredo.
- `dependency-review-action`: só roda em PR e não filtra por correção disponível.
- Cobertura só relatada, ou mínimo de 95%: a primeira não barra código sem teste; a segunda tem margem
  pequena demais para o Marco 3.
- Verificar os ADRs no pipeline: seria um teste de forma, e não de conteúdo. Eles são comprovados por
  revisão.

## Consequências

- O pipeline verde comprova os requisitos de pipeline e de imagem (Princípio VI, constituição 1.4.0).
- Todo PR do Dependabot que atualize uma action composta repete a verificação das referências aninhadas.
- A prova de que cada porta reprova (SC-008) acontece num branch temporário, que nunca é mergeado. Nenhum
  defeito fica no código do marco.
