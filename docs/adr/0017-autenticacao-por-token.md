# 0017. Autenticação por token com Spring Security, desligada por padrão

- **Status:** Aceito
- **Data:** 2026-11-24
- **Origem:** [`docs/ai/decisoes.md`](../ai/decisoes.md), entradas "Cabeçalhos de proteção do Spring
  Security em todas as respostas", "Exceção do `ApiDocsIT` à regra dos testes sem alteração", "Segunda
  análise da spec 003" e "Etapa 3 do Marco 3"; research R11 a R14.

## Contexto

A autenticação existe e é testada, mas fica desligada por padrão, porque o avaliador chama a API sem token
(Princípio VII). Com ela ligada, os endpoints de negócio e o de métricas exigem o token. As probes e a
documentação continuam públicas. O ETag do Marco 2 precisa continuar útil.

## Decisão

- `spring-boot-starter-security`, com uma `SecurityFilterChain` sem estado: sem CSRF, `formLogin`,
  `httpBasic`, `logout`, cache de requisição nem sessão. O usuário gerado pelo Boot fica excluído
  (`UserDetailsServiceAutoConfiguration`).
- O token vem de `API_TOKEN`. Vazio ou ausente, tudo fica liberado e nenhum filtro de token é registrado.
- Com o token ligado:
  - `/api/producers/intervals`, `/api/movies` e `/actuator/prometheus` exigem
    `Authorization: Bearer <token>`, em qualquer método;
  - o resto continua público: probes, saúde geral, Swagger UI, contrato, webjars, `/error` e caminhos
    inexistentes.
- **Comparação em tempo constante** (decisão de design; era a FR-024 da spec, retirada na segunda
  análise): `MessageDigest.isEqual(sha256(recebido), sha256(esperado))`, em UTF-8. O `isEqual` do JDK
  confere todos os bytes sem sair no primeiro diferente. Os dois resumos têm sempre 32 bytes, então o tempo
  não depende do tamanho nem do conteúdo do token recebido. `String.equals` e `Arrays.equals` não são
  usados. A "Revisão de segurança" do checklist da spec confere esse mecanismo.
- **401** em Problem Details, igual para o token ausente e para o token errado, com
  `WWW-Authenticate: Bearer realm="awards"`. Nenhum dos dois valores aparece na resposta nem no log.
- **Cabeçalhos, como decisão intencional:**
  - `X-Content-Type-Options: nosniff` e `X-Frame-Options: DENY` aparecem em **todas** as respostas, com o
    token ligado ou desligado. São um acréscimo em relação ao Marco 2 que não muda nada do que os testes
    dos Marcos 1 e 2 verificam;
  - o `Cache-Control` do Spring Security fica **fora dos endpoints de negócio**, que respondem sem
    `Cache-Control`, `Pragma` e `Expires`, como no Marco 2, para que o ETag e o 304 continuem úteis;
  - nas demais respostas e em todo 401, os cabeçalhos de cache do framework continuam.
- Contrato OpenAPI 1.2.0, com `security: [{}, {bearerAuth: []}]` (autenticação opcional) e a resposta 401.

## Alternativas consideradas

- `oauth2ResourceServer` com JWT: exige emissor e chaves, sem necessidade.
- `httpBasic`: exige usuário e senha.
- Um filtro de servlet sem Spring Security: resolveria a comparação, mas sem a cadeia, a negação padrão e os
  cabeçalhos de proteção.
- Desligar também `X-Content-Type-Options` e `X-Frame-Options`, para uma igualdade literal de cabeçalhos
  com o Marco 2: tiraria proteção sem ganho (decisão do responsável).

## Consequências

- Sem `API_TOKEN`, a suíte dos Marcos 1 e 2 passa sem alteração. O `AuthDisabledByDefaultIT` falha se a
  variável estiver definida na execução padrão, e o `check-unchanged-tests.sh` confere os ITs da tag
  `v0.2.0`. O `ApiDocsIT` é a única exceção, e só a constante do caminho do contrato mudou.
- O tempo constante não tem teste de integração confiável. Ele é comprovado por revisão.
- O único token no repositório é o de demonstração do README. Os testes geram tokens aleatórios.
