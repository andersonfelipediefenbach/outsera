package com.outsera.awards.ingestion.kafka;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuração da ingestão por Kafka (spec 003, FR-003; research R6, R9 e R10).
 *
 * @param topicMovies       tópico de filmes ({@code KAFKA_TOPIC_MOVIES})
 * @param topicErrors       tópico de erros ({@code KAFKA_TOPIC_ERRORS})
 * @param publishCompletion se o publicador envia o evento de carga concluída ao fim da publicação
 */
@ConfigurationProperties("awards.ingestion")
public record KafkaIngestionProperties(String topicMovies, String topicErrors, boolean publishCompletion) {
}
