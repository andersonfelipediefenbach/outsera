# 0001. Stack: Java 25, Spring Boot 4.1.2 e Maven com wrapper

- **Status:** Aceito
- **Data:** 2026-11-18
- **Origem:** [`docs/ai/decisoes.md`](../ai/decisoes.md), entrada "Decisões de arquitetura sem entrada
  própria (ADRs)", seção "Stack"; research R1 da spec 001.

## Contexto

O enunciado pede uma API que suba e seja testada só com o build do projeto (item 4.3), e o avaliador
roda o modo padrão sem nada instalado além do JDK. A constituição pede a versão estável atual do
framework e o wrapper do Maven versionado.

## Decisão

- Java 25 (LTS).
- Spring Boot 4.1.2.
- Maven 3.9.16 pelo Maven Wrapper 3.3.4, na distribuição `only-script`.

## Alternativas consideradas

- Spring Boot 4.2.0-RC2: não era estável.
- Spring Boot 3.5.x: é da linha anterior, e não o estável atual.
- Maven 4 RC: não era estável.
- Wrapper com o JAR binário: deixaria um binário no repositório sem necessidade.

## Consequências

- `./mvnw spring-boot:run` e `./mvnw verify` funcionam com o JDK 25 em qualquer sistema operacional.
- O Boot 4 traz mudanças de API em relação ao 3 (por exemplo, `RestTestClient` nos testes e Jackson 3),
  e algumas bibliotecas de terceiros precisaram de ajuste (ADR 0005).
- A atualização das versões fica com o Dependabot (ADR 0012).
