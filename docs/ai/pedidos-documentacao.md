# Pedidos ao papel de documentação

Mudanças pedidas por outros papéis em arquivos que pertencem ao papel de documentação (AGENTS.md,
"Papéis e propriedade"). Cada pedido fica aberto até ser atendido.

## 2026-11-22 — Tabela "Requisitos do enunciado → testes" no README (T083, aberto)

- **De:** implementador, no fechamento do Marco 1 (bolts 5 e 6).
- **Pedido:** adicionar ao `README.md` a seção "Requisitos do enunciado → testes", com a tabela abaixo
  (Princípio VI da constituição). A tabela é derivada da matriz de rastreabilidade de
  `specs/001-producer-intervals-api/plan.md`. A T083 só fecha quando a tabela estiver no README.
- **Conteúdo:**

| Item do enunciado | Requisitos | Teste(s) de integração |
| --- | --- | --- |
| Carga do CSV na inicialização, antes de o servidor aceitar conexões | FR-001, FR-002 | `DefaultFileIT` |
| Formato de `min` e `max` (`producer`, `interval`, `previousWin`, `followingWin`) | FR-017, FR-025 | `IntervalsShapeIT`, `IntervalsContractIT` |
| Empates: todos os empatados em `min` e em `max` | FR-014, FR-015 | `TiesMinIT`, `TiesMaxIT`, `IntervalEdgeCasesIT` |
| Listas vazias quando nenhum produtor venceu duas vezes | FR-018 | `NoRepeatWinnersIT`, `IntervalsShapeIT`, `EmptyLoadIT` |
| Resultado de referência com o arquivo fornecido (Joel Silver em `min`, Matthew Vaughn em `max`) | SC-001 | `ReferenceFileIT` |
| Somente testes de integração (item 4.2) | FR-027, Princípio I | todos os testes são `*IT` com `@SpringBootTest` e sem mocks, executados pelo Failsafe. A exceção é o `StartupFailureIT`, que sobe a aplicação real com `SpringApplication`. Não há teste unitário (auditoria da T078) |
| Execução só com o wrapper, sem Docker (item 4.3) | FR-026 | `SmokeIT`, mais `./mvnw verify` e `./mvnw spring-boot:run` em clone limpo (T081) |
