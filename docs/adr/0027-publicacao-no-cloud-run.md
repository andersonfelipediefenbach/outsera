# 0027. Publicação no Cloud Run, com uma instância e identidade federada

- **Status:** Aceito
- **Data:** 2026-11-26
- **Origem:** [`docs/ai/decisoes.md`](../ai/decisoes.md), entradas do Marco 5;
  [spec 005](../../specs/005-deploy-cloud-run/spec.md) e a research R1 a R11 do
  [plano](../../specs/005-deploy-cloud-run/plan.md).

## Contexto

O Marco 5 publica a API para o avaliador usar sem instalar nada. O briefing escolheu o Cloud Run pela cota
gratuita mensal. O repositório e toda a evidência são públicos, então a publicação não pode depender de uma
chave guardada no GitHub (Princípio VII). O que vai para a nuvem é o modo padrão: H2 em memória, carga do
CSV e autenticação desligada.

## Decisão

- **Cloud Run** no projeto `outsera-awards`, região `southamerica-east1`, com **0 a 1 instância por
  revisão**, 1 vCPU, 1 GiB, impulso de CPU na partida e cobrança por requisição. Com o serviço ocioso, o custo
  é zero.
- **Só o modo padrão.** A única variável acrescentada é `MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=health`:
  - mantém `/actuator/health` e os grupos de liveness e readiness, que os probes usam;
  - fecha `/actuator/prometheus`, que passa a responder 404 na URL pública.

  Com a autenticação desligada, as métricas ficariam abertas na internet, e nenhum Prometheus coleta a
  aplicação no Cloud Run. O modo padrão local e a imagem não mudam.
- **Publicação a cada push na `main`,** pelo job `deploy`, depois dos 10 checks obrigatórios. Não é por tag
  de versão, como previa o briefing.
- **A mesma imagem testada, pelo digest, sem rebuild.**
  - O job `image` gera, num só build, a imagem do Docker, que passa pelo teste de fumaça e pelo Trivy, e um
    tar OCI do mesmo resultado.
  - O job `deploy` envia esse tar ao Artifact Registry com `skopeo copy --preserve-digests`, confere o
    digest antes e depois do envio e implanta por `@sha256:`.
- **Workload Identity Federation, sem chaves:**
  - o provedor OIDC só aceita tokens do repositório e do dono pelos IDs numéricos, e de `refs/heads/main`;
  - a conta de publicação tem os papéis no serviço, no repositório de imagens e na conta de execução, e
    nenhum no projeto;
  - a conta de execução não tem papel nenhum.
- **Revisão sem tráfego até ser conferida.**
  1. A revisão nova sobe com uma tag própria e é conferida pela URL dela: readiness, resultado de
     referência, Swagger e métricas fechadas.
  2. Só então recebe o tráfego, e a URL pública é conferida de novo.
  3. Uma revisão reprovada nunca recebe tráfego.
- **Tudo o que existe no GCP é criado pelo [`deploy/gcp/setup.sh`](../../deploy/gcp/setup.sh),**
  idempotente, inclusive o serviço, com a imagem pública de exemplo. Isso permite dar à conta de publicação
  papéis só no serviço e deixar o acesso público (`allUsers` como invocador) fora do alcance do pipeline.

## Alternativas consideradas

- **Chave JSON da conta de serviço num secret do GitHub:** proibida pelo Princípio VII e pela política do
  workflow.
- **Reconstruir a imagem no job `deploy`, ou `docker save` com `docker push`:** seria outra imagem, ou o digest
  poderia mudar com a recompressão das camadas.
- **Action `deploy-cloudrun`:** juntaria implantação e troca de tráfego num passo e seria mais uma dependência.
  Os três passos `gcloud` explícitos deixam a conferência entre eles.
- **Promover direto e voltar o tráfego se a conferência falhar:** a revisão ruim ficaria na URL pública por
  alguns segundos.
- **`roles/run.admin` para a conta de publicação, ou papéis no projeto:** permitiriam ao pipeline mudar o IAM
  do serviço, por exemplo para torná-lo privado ou expor outro serviço.
- **Métricas públicas, como no modo padrão local:** expõem dados internos de operação sem quem os colete.

## Consequências

- **Duas instâncias por instantes na troca.** O `max-instances=1` vale por revisão, então a antiga e a nova
  podem ter uma instância cada. Isso não fere o [ADR 0026](0026-intervalos-guardados-entre-leituras.md): no
  modo padrão, cada instância tem o próprio H2 e não grava nada depois da carga. Um modo com gravações
  contínuas no Cloud Run, como o Kafka, exigiria rever o ADR 0026 antes.
- **Partida a frio com o mínimo de 0 instância.** A primeira chamada depois de um período ocioso espera a
  JVM e a carga do CSV. O README avisa, e a conferência mede esse tempo (SC-001).
- **Merges seguidos.** As publicações não se cancelam. Com várias na fila, o GitHub mantém só a mais nova, e
  o último commit da `main` sempre fica publicado.
- **A política do workflow (`check-workflows.sh`) ganhou exceções estreitas:**
  - `id-token: write` e o `skopeo` só no job `deploy`;
  - regras que exigem os 10 checks, a concorrência sem cancelamento e nenhum build no job.
- **A URL só serve a API depois da primeira publicação pelo pipeline.** Até lá, o serviço responde com a
  imagem de exemplo.
