package com.outsera.awards.kafka;

import static org.assertj.core.api.Assertions.assertThat;

import com.outsera.awards.support.KafkaScenarioIT;
import com.outsera.awards.support.PrometheusText;
import com.outsera.awards.support.PrometheusText.Sample;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

/**
 * As métricas trazem os eventos gravados e rejeitados e o atraso do consumidor no tópico de filmes (spec
 * 003, FR-017; research R19). O nome da série de atraso é o que o Micrometer publica com o Spring Kafka
 * do Boot 4.1.2, e o dashboard usa o mesmo: o atraso máximo do cliente consumidor do ouvinte
 * {@code awards-movies}. Com um consumidor e uma partição, é o atraso do tópico de filmes. A série por
 * partição só aparece depois da atualização periódica do Micrometer, e por isso não é usada.
 */
@TestPropertySource(properties = "movies.csv.path=classpath:movielist.csv")
class KafkaMetricsIT extends KafkaScenarioIT {

    static final String LAG = "kafka_consumer_fetch_manager_records_lag_max";
    static final String CONSUMER_CLIENT = "consumer-awards-movies";

    private List<Sample> samples() {
        String text = http.get().uri("/actuator/prometheus").exchange().expectStatus().isOk()
                .expectBody(String.class).returnResult().getResponseBody();
        return PrometheusText.parse(text);
    }

    @Test
    @DisplayName("S003 FR-017 eventos gravados, rejeitados e atraso do consumidor nas métricas")
    void ingestionMetrics() {
        awaitReady();
        await("206 eventos gravados nas métricas", 10, () -> PrometheusText.sum(samples(),
                "awards_ingestion_events_total", sample -> "recorded".equals(sample.label("outcome"))) == 206);
        List<Sample> samples = samples();
        assertThat(PrometheusText.find(samples, "awards_ingestion_events_total",
                sample -> "rejected".equals(sample.label("outcome")))).singleElement()
                .satisfies(sample -> assertThat(sample.value()).isZero());
        assertThat(PrometheusText.find(samples, LAG, sample -> sample.label("client_id") != null
                        && sample.label("client_id").startsWith(CONSUMER_CLIENT)))
                .as("série %s do consumidor %s; séries de atraso encontradas: %s", LAG, CONSUMER_CLIENT,
                        samples.stream().filter(sample -> sample.name().contains("lag"))
                                .map(sample -> sample.name() + sample.labels()).toList())
                .isNotEmpty();
    }
}
