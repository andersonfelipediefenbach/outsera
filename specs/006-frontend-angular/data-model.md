# Modelo de dados: Marco 6

Nenhuma tabela e nenhuma migração nova. As agregações são calculadas a partir de `movie` (ano, estúdios e
vencedor), que não muda. O esquema das respostas está em [`contracts/openapi.yaml`](contracts/openapi.yaml).

## Respostas novas da API

| Resposta | Campo | Tipo | Regra |
| --- | --- | --- | --- |
| `YearsWithMultipleWinners` | `years` | lista de `YearWinnerCount` | ordem crescente de `year`; vazia sem anos elegíveis (FR-001, FR-003) |
| `YearWinnerCount` | `year` | inteiro | ano de `movie.release_year` |
| | `winnerCount` | inteiro ≥ 2 | filmes com `winner = true` naquele ano |
| `StudiosWithWinCount` | `studios` | lista de `StudioWinCount` | `winCount` decrescente e depois `name` (`String.compareTo`); vazia sem vencedores (FR-002, FR-003) |
| `StudioWinCount` | `name` | texto, não vazio | um item do campo `studios` separado por vírgula, sem espaços nas pontas, sem normalizar a grafia |
| | `winCount` | inteiro ≥ 1 | filmes vencedores distintos em que o estúdio aparece |

### Regras de separação dos estúdios (`StudioNameParser`)

1. `null` (coluna vazia no CSV, spec 001 FR-006e) → nenhum estúdio.
2. Separa por `,`; cada parte com `strip()`; partes vazias saem (`"A,,B"` e `"A, "` → `A`, `B`).
3. Um conjunto por filme: `"A, A"` conta uma vez para `A`.
4. Sem separar por ` and ` (premissa da spec: o arquivo fornecido não usa).
5. Maiúsculas e acentos são mantidos: `MGM` e `Mgm` são estúdios diferentes.

### Valores de referência (arquivo fornecido)

- Anos: `1986 → 2`, `1990 → 2`, `2015 → 2`.
- Estúdios (início): `Columbia Pictures 7`, `Paramount Pictures 6`, `Warner Bros. 5`, `20th Century Fox 4`,
  `MGM 3`.

## Estado do front

| Elemento | Campos | Regras |
| --- | --- | --- |
| Estado de painel (`PanelState<T>`) | `status` ∈ {`carregando`, `dados`, `vazio`, `erro`}, `dados?: T`, `erro?: string` | começa em `carregando`; `vazio` quando a lista da API vem vazia; `erro` com "tentar de novo", que volta a `carregando` (FR-009) |
| Busca por ano (Dashboard) | `ano: string` | válido se `^\d+$`; inválido desabilita a busca e mostra a mensagem (FR-008) |
| Filtro da lista | `ano?: number`, `vencedor?: 'sim' \| 'nao'`, `pagina: number ≥ 1` | lido da URL; valor inválido descartado; mudar filtro volta `pagina` a 1 (FR-011, FR-013) |
| Página da lista | `itens`, `pagina`, `totalPaginas`, `totalItens` | da resposta `MoviePage` (`page + 1`, `totalPages`, `totalElements`); botões sem destino desabilitados (FR-012) |

### Tradução entre a URL do front e a API

| URL do front (`/filmes`) | API (`/api/movies`) |
| --- | --- |
| `ano=1990` | `year=1990` |
| `vencedor=sim` / `vencedor=nao` / ausente | `winner=true` / `winner=false` / ausente |
| `pagina=3` / ausente | `page=2` / `page=0` |
| (fixo) | `size=10` |

Busca do Dashboard: `GET /api/movies?year={ano}&winner=true&size=100` (FR-006).

## Recursos novos no GCP

| Recurso | Nome | Atributos | Criado por |
| --- | --- | --- | --- |
| Conta de execução do front | `awards-web-runtime@outsera-awards.iam.gserviceaccount.com` | nenhum papel no projeto | script |
| Serviço | `awards-web` | conta `awards-web-runtime`; `min-instances=0`, `max-instances=1`; acesso anônimo; criado com a imagem de exemplo | script (criação); pipeline (revisões) |
| Revisão | `awards-web-<sufixo>` | imagem por digest; `API_URL` com a URL pública do `awards`; tag `c-<sha curto>` enquanto é conferida | pipeline |
| Imagem | `southamerica-east1-docker.pkg.dev/outsera-awards/awards/awards-web:<sha do commit>` | digest do manifesto OCI do job `web` | pipeline |

### Vínculos de IAM novos

| Membro | Papel | Recurso | Motivo |
| --- | --- | --- | --- |
| `github-deployer` | `roles/run.developer` | serviço `awards-web` | implantar revisões e mudar o tráfego só desse serviço |
| `github-deployer` | `roles/iam.serviceAccountUser` | conta `awards-web-runtime` | implantar revisões que rodam com essa conta |
| `allUsers` | `roles/run.invoker` | serviço `awards-web` | front público, como a API (FR-027) |

O pool, o provedor, a conta `github-deployer` e o repositório `awards` são os do Marco 5, sem mudança.

## Estados da revisão do `awards-web`

Iguais aos do `awards` no Marco 5: criada sem tráfego com tag → conferida pela URL da tag → 100% do tráfego,
com as tags antigas removidas → conferida pela URL pública. Reprovada na URL da tag, nunca recebe tráfego
(FR-029).
