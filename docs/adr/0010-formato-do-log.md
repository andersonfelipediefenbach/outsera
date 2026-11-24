# 0010. Log em texto no `./mvnw` e JSON por variável, ligado na imagem

- **Status:** Aceito
- **Data:** 2026-11-23
- **Origem:** [`docs/ai/decisoes.md`](../ai/decisoes.md), entradas "Emenda 1.3.0 da constituição: log em
  texto no `./mvnw`, JSON no container", "Correções da segunda análise da spec 002" (banner) e "Etapa 2
  do Marco 2: `groups` na saúde geral, banner e formato ECS"; research R6 da spec 002.

## Contexto

O briefing pedia logs estruturados em JSON. O avaliador, porém, roda o `./mvnw spring-boot:run` e lê o
log no terminal, onde o JSON é ilegível. O JSON serve aos coletores de log, que só existem na execução em
container e nos ambientes operacionais.

## Decisão

- Logging estruturado nativo do Spring Boot, no formato ECS, ligado pela variável
  `LOGGING_STRUCTURED_FORMAT_CONSOLE=ecs`, que o próprio Boot entende.
- Sem a variável, o log é o texto padrão do framework. A imagem define a variável, e quem roda o
  container pode sobrescrevê-la.
- O banner fica desligado em todas as execuções (`spring.main.banner-mode: off`), porque não traz
  informação.
- Os testes que verificam o log rodam nos dois formatos: uma segunda execução do Failsafe, `json-logs`,
  numa JVM própria, roda de novo as classes com `@Tag("log")` com o JSON ligado.

## Alternativas consideradas

- JSON sempre, inclusive no `./mvnw`: pioraria a leitura no modo que o avaliador usa.
- Formato escolhido por perfil do Spring: misturaria o formato do log com a escolha de banco e ingestão.
- `logstash-logback-encoder`, ou uma variável própria mapeada no `application.yml`: dependência ou
  indireção a mais para o mesmo resultado.
- Ligar o JSON num `@SpringBootTest`: o Logback é global na JVM, e o formato vazaria para os testes
  seguintes.

## Consequências

- A constituição foi emendada para a 1.3.0.
- O Boot 4.1.2 escreve o ECS com campos aninhados (`{"log":{"level":…}}`) e já omite o banner com o JSON
  ligado. Os testes leem o log pelo `LogOutput`, que aceita os dois jeitos.
- O `./mvnw verify` roda as classes de log duas vezes: são 55 testes a mais, de 12 classes, na
  execução `json-logs`.
