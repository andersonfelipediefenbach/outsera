package com.outsera.awards.kafka;

import static org.assertj.core.api.Assertions.assertThat;

import com.outsera.awards.support.ApplicationStarter;
import com.outsera.awards.support.Http;
import com.outsera.awards.support.KafkaTestSupport;
import com.outsera.awards.support.KafkaTestSupport.Topics;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.kafka.test.EmbeddedKafkaBroker;

/**
 * O mesmo arquivo pela carga direta e pela ingestão por Kafka produz respostas idênticas, byte a byte e
 * com o mesmo ETag, sem exceção (spec 003, FR-018, US1-2 e SC-001). Usa {@code SpringApplication} pela
 * cláusula "múltiplas execuções" do Princípio I (constituição 1.5.0), com o broker embutido real.
 */
class KafkaEquivalenceIT {

    private static EmbeddedKafkaBroker broker;

    @BeforeAll
    static void startBroker() {
        broker = KafkaTestSupport.startBroker();
    }

    @AfterAll
    static void stopBroker() {
        broker.destroy();
    }

    private static final List<String> PATHS = List.of(
            "/api/producers/intervals",
            "/api/movies?size=100&page=0",
            "/api/movies?size=100&page=1",
            "/api/movies?size=100&page=2",
            "/api/movies?winner=true",
            "/api/movies?year=1990");

    @ParameterizedTest(name = "S003 FR-018 US1-2 SC-001 carga direta e Kafka idênticas: {0}")
    @ValueSource(strings = {
            "classpath:movielist.csv",
            "classpath:scenarios/adversario/13-ordenacao-com-acentos-e-caixa.csv",
            "classpath:scenarios/field-too-long.csv"})
    @DisplayName("S003 FR-018 US1-2 SC-001 carga direta e Kafka idênticas")
    void directAndKafkaLoadsAreEquivalent(String csvPath) {
        // Tópicos e grupo próprios para cada arquivo, para que um não veja os eventos do outro.
        Topics base = KafkaTestSupport.topics(KafkaEquivalenceIT.class);
        String suffix = "." + Integer.toHexString(csvPath.hashCode());
        Topics topics = new Topics(base.movies() + suffix, base.errors() + suffix, base.group() + suffix);

        Map<String, Http.Response> direct = ApplicationStarter.runAndCall(csvPath, port -> Http.snapshot(port, PATHS));
        Map<String, Http.Response> kafka = ApplicationStarter.runAndCall(csvPath,
                KafkaTestSupport.args(broker.getBrokersAsString(), topics), port -> {
                    Http.awaitReady(port, Duration.ofSeconds(60));
                    return Http.snapshot(port, PATHS);
                });

        for (String path : PATHS) {
            assertThat(kafka.get(path).text()).as("corpo de %s", path).isEqualTo(direct.get(path).text());
            assertThat(kafka.get(path).body()).as("bytes de %s", path).isEqualTo(direct.get(path).body());
            assertThat(kafka.get(path).etag()).as("ETag de %s", path).isPresent().isEqualTo(direct.get(path).etag());
        }
    }
}
