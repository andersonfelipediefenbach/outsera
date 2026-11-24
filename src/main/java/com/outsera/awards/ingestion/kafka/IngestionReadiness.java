package com.outsera.awards.ingestion.kafka;

import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.boot.health.contributor.Status;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Indicador de saúde {@code ingestion}, que entra no grupo da readiness no perfil {@code kafka} (spec 003,
 * FR-013; research R10). Fica {@code OUT_OF_SERVICE} até o consumidor processar o evento de carga
 * concluída com o {@code runId} desta execução, e {@code UP} depois. Um marcador de outra execução é
 * ignorado.
 */
@Component("ingestionHealthIndicator")
@Profile("kafka")
public class IngestionReadiness implements HealthIndicator {

    private static final Logger log = LoggerFactory.getLogger(IngestionReadiness.class);

    private final String runId = UUID.randomUUID().toString();
    private volatile boolean completed;

    /** Identifica a publicação desta execução. */
    public String runId() {
        return runId;
    }

    /** Recebe um evento de carga concluída consumido do tópico. */
    public void complete(LoadCompletedEvent event) {
        if (runId.equals(event.runId())) {
            if (!completed) {
                completed = true;
                log.info("Carga por Kafka concluída: {} filmes publicados pela execução {}", event.count(), runId);
            }
        } else {
            log.debug("Evento de carga concluída de outra execução ignorado: {}", event.runId());
        }
    }

    @Override
    public Health health() {
        return completed ? Health.up().build() : Health.status(Status.OUT_OF_SERVICE).build();
    }
}
