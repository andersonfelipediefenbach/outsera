package com.outsera.awards.postgres;

import static org.assertj.core.api.Assertions.assertThat;

import com.outsera.awards.support.ApplicationStarter;
import com.outsera.awards.support.ApplicationStarter.Result;
import com.outsera.awards.support.Http;
import com.outsera.awards.support.KafkaTestSupport;
import com.outsera.awards.support.KafkaTestSupport.Topics;
import com.outsera.awards.support.PostgresSupport;
import com.outsera.awards.support.PrometheusText;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.kafka.test.EmbeddedKafkaBroker;

/**
 * Quando a gravação de um evento válido falha, o evento não é descartado nem vai ao tópico de erros: a
 * gravação é tentada de novo até dar certo (spec 003, FR-015a; research R9).
 *
 * <p>O teste prova a <strong>nova tentativa quando a gravação falha</strong>: a tabela {@code movie} do
 * esquema da classe é renomeada e depois devolvida, num {@code finally}. Ele não prova a queda real do
 * banco, que é comprovada pelo passo 7b do teste do compose. É {@link Isolated}, e a execução
 * {@code postgres} do Failsafe não usa paralelismo: ele nunca roda ao mesmo tempo que outro IT.
 */
@Isolated
class RetryPostgresIT {

    private static EmbeddedKafkaBroker broker;

    @BeforeAll
    static void startBroker() {
        broker = KafkaTestSupport.startBroker();
    }

    @AfterAll
    static void stopBroker() {
        broker.destroy();
    }

    private static final String SCHEMA = PostgresSupport.newSchema(RetryPostgresIT.class);

    @AfterAll
    static void dropSchema() {
        PostgresSupport.dropSchema(SCHEMA);
    }

    private static double rejected(int port) {
        return PrometheusText.sum(PrometheusText.parse(Http.get(port, "/actuator/prometheus").text()),
                "awards_ingestion_events_total", sample -> "rejected".equals(sample.label("outcome")));
    }

    @Test
    @DisplayName("S003 FR-015a nova tentativa quando a gravação falha, sem tópico de erros")
    void retriesWhenTheWriteFails() {
        String brokers = broker.getBrokersAsString();
        Topics topics = KafkaTestSupport.topics(RetryPostgresIT.class);
        List<String> args = new ArrayList<>(PostgresSupport.args(SCHEMA, "postgres,kafka,kafkatest"));
        KafkaTestSupport.properties(brokers, topics).forEach((name, value) -> args.add("--" + name + "=" + value));

        Result result = ApplicationStarter.startAndCall("classpath:scenarios/header-only.csv", args, port -> {
            Http.awaitReady(port, Duration.ofSeconds(60));
            double rejectedBefore = rejected(port);
            PostgresSupport.execute(SCHEMA, "ALTER TABLE movie RENAME TO movie_retry_off");
            try {
                KafkaTestSupport.sendJson(brokers, topics.movies(), "2032|Durante a Falha", "{\"year\":2032,"
                        + "\"title\":\"Durante a Falha\",\"studios\":null,\"producers\":\"Producer R\",\"winner\":true}");
                Thread.sleep(5000);
                assertThat(Http.get(port, "/actuator/health/readiness").status())
                        .as("readiness durante a falha").isEqualTo(200);
                assertThat(KafkaTestSupport.read(brokers, topics.errors(), 0, Duration.ofSeconds(2)))
                        .as("tópico de erros durante a falha").isEmpty();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                PostgresSupport.execute(SCHEMA, "ALTER TABLE movie_retry_off RENAME TO movie");
            }
            Http.await("filme gravado depois da falha", Duration.ofSeconds(60),
                    () -> Http.get(port, "/api/movies?year=2032").text().contains("Durante a Falha"));
            assertThat(rejected(port)).isEqualTo(rejectedBefore);
            assertThat(KafkaTestSupport.read(brokers, topics.errors(), 0, Duration.ofSeconds(2))).isEmpty();
        });
        assertThat(result.failed()).isFalse();
        assertThat(result.output()).contains("Falha ao gravar o evento");
    }
}
