package com.outsera.awards.isolation;

import static org.assertj.core.api.Assertions.assertThat;

import com.outsera.awards.support.ApplicationStarter;
import com.outsera.awards.support.ApplicationStarter.Result;
import com.outsera.awards.support.Http;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Sem perfil, o Kafka não é configurado (spec 003, FR-001; Princípio V; research R2): nenhuma métrica do
 * Kafka nem da ingestão e nenhum cliente Kafka no log da subida. Usa {@code SpringApplication} pela
 * cláusula "saída completa da subida" do Princípio I (constituição 1.5.0).
 */
@Tag("log")
class DefaultModeIsolationIT {

    @Test
    @DisplayName("S003 FR-001 modo padrão sem Kafka configurado")
    void kafkaIsNotConfiguredWithoutTheProfile() {
        Result result = ApplicationStarter.startAndCall("classpath:movielist.csv", List.of(), port -> {
            String metrics = Http.get(port, "/actuator/prometheus").text();
            assertThat(metrics.lines())
                    .noneMatch(line -> line.startsWith("kafka_"))
                    .noneMatch(line -> line.startsWith("awards_ingestion_"));
            assertThat(Http.get(port, "/api/producers/intervals").text())
                    .contains("Joel Silver").contains("Matthew Vaughn");
        });
        assertThat(result.failed()).isFalse();
        assertThat(result.output())
                .doesNotContain("AdminClientConfig")
                .doesNotContain("ProducerConfig")
                .doesNotContain("ConsumerConfig");
    }
}
