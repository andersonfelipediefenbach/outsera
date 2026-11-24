package com.outsera.awards.ingestion.kafka;

/**
 * Evento de carga concluída (spec 003, FR-011; contracts/kafka-events.md).
 *
 * @param runId identifica a execução que publicou
 * @param count filmes publicados nessa execução
 */
public record LoadCompletedEvent(String runId, long count) {
}
