package com.outsera.awards.kafka;

import static org.assertj.core.api.Assertions.assertThat;

import com.outsera.awards.support.KafkaScenarioIT;
import com.outsera.awards.support.KafkaTestSupport;
import com.outsera.awards.support.KafkaTestSupport.Record;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

/**
 * A readiness só fica positiva quando o consumidor processa o evento de carga concluída desta execução
 * (spec 003, FR-013, US1-3; research R10). O teste segura o marcador
 * ({@code awards.ingestion.publish-completion=false}) e o envia ele mesmo, pelo broker real, com o
 * {@code runId} lido do cabeçalho {@code awards-run-id} dos eventos publicados.
 */
@TestPropertySource(properties = {
        "movies.csv.path=classpath:movielist.csv",
        "awards.ingestion.publish-completion=false"})
class KafkaReadinessIT extends KafkaScenarioIT {

    private int status(String path) {
        return http.get().uri(path).exchange().returnResult().getStatus().value();
    }

    private void sendCompletion(String runId) {
        sendMovie("load-completed", "{\"type\":\"load-completed\",\"runId\":\"" + runId + "\",\"count\":206}");
    }

    @Test
    @DisplayName("S003 FR-013 US1-3 FR-016 US3-5 readiness só com o marcador desta execução")
    void readinessWaitsForTheCompletionOfThisRun() {
        await("206 filmes gravados", 60, () -> total("") == 206);
        assertThat(readinessStatus()).as("readiness antes do marcador").isEqualTo(503);
        assertThat(status("/actuator/health")).as("saúde geral antes do marcador").isEqualTo(503);
        assertThat(status("/actuator/health/liveness")).as("liveness").isEqualTo(200);

        List<Record> movies = KafkaTestSupport.read(brokers(), MOVIES_TOPIC, 206, Duration.ofSeconds(30));
        String runId = movies.getFirst().headers().get("awards-run-id");
        assertThat(runId).isNotBlank();

        sendCompletion(UUID.randomUUID().toString());
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        assertThat(readinessStatus()).as("readiness com o marcador de outra execução").isEqualTo(503);

        sendCompletion(runId);
        await("readiness 200 com o marcador desta execução", 5, () -> readinessStatus() == 200);

        // Depois de pronta, eventos novos ou inválidos não mudam a readiness (FR-016, US3-5).
        sendMovie("2040|Depois da Carga",
                "{\"year\":2040,\"title\":\"Depois da Carga\",\"studios\":null,\"producers\":\"P\",\"winner\":false}");
        sendMovie("x", "{\"year\":0}");
        await("evento novo gravado", 10, () -> total("year=2040") == 1);
        assertThat(readinessStatus()).isEqualTo(200);
    }
}
