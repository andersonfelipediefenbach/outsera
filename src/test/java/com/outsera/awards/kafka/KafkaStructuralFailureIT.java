package com.outsera.awards.kafka;

import static org.assertj.core.api.Assertions.assertThat;

import com.outsera.awards.support.ApplicationStarter;
import com.outsera.awards.support.ApplicationStarter.Result;
import com.outsera.awards.support.KafkaTestSupport;
import com.outsera.awards.support.KafkaTestSupport.Topics;
import java.time.Duration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.kafka.test.EmbeddedKafkaBroker;

/**
 * Com o perfil {@code kafka}, um erro estrutural no arquivo impede a subida com a mesma mensagem do
 * Marco 1, e nada é publicado (spec 003, FR-009 e US1-5). Usa {@code SpringApplication} pela cláusula
 * "falha de inicialização" do Princípio I.
 */
@Tag("log")
class KafkaStructuralFailureIT {

    private static EmbeddedKafkaBroker broker;

    @BeforeAll
    static void startBroker() {
        broker = KafkaTestSupport.startBroker();
    }

    @AfterAll
    static void stopBroker() {
        broker.destroy();
    }

    @ParameterizedTest(name = "S003 FR-009 US1-5 {0}: {1}")
    @CsvSource({
            "file:./nao-existe.csv, NOT_FOUND",
            "classpath:scenarios/header-wrong-order.csv, INVALID_HEADER"})
    @DisplayName("S003 FR-009 US1-5 erro estrutural impede a subida e nada é publicado")
    void structuralErrorStopsTheStartup(String csvPath, String reason) {
        Topics topics = KafkaTestSupport.topics(KafkaStructuralFailureIT.class);
        String brokers = broker.getBrokersAsString();

        Result result = ApplicationStarter.start(csvPath, KafkaTestSupport.args(brokers, topics));

        assertThat(result.failed()).as("a aplicação não deveria subir com %s", csvPath).isTrue();
        assertThat(result.messages())
                .contains("APPLICATION FAILED TO START")
                .contains(csvPath)
                .contains(reason);
        assertThat(KafkaTestSupport.read(brokers, topics.movies(), 0, Duration.ofSeconds(3))).isEmpty();
    }
}
