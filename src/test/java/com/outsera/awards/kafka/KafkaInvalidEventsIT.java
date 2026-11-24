package com.outsera.awards.kafka;

import static org.assertj.core.api.Assertions.assertThat;

import com.outsera.awards.support.KafkaScenarioIT;
import com.outsera.awards.support.KafkaTestSupport;
import com.outsera.awards.support.KafkaTestSupport.Record;
import com.outsera.awards.support.PrometheusText;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.test.context.TestPropertySource;

/**
 * Cada evento inválido vai ao tópico de erros com os bytes originais e o motivo, sem ser gravado e sem
 * travar o consumo (spec 003, FR-015, FR-017, US3-3, US3-4 e SC-006). Os motivos são os do
 * data-model.md, inclusive os de conteúdo, que são as mesmas regras da linha do CSV.
 */
@TestPropertySource(properties = "movies.csv.path=classpath:scenarios/header-only.csv")
@Tag("log")
class KafkaInvalidEventsIT extends KafkaScenarioIT {

    private static final AtomicInteger SEQUENCE = new AtomicInteger(2100);
    private static final String MARKER = "conteudo-do-evento";

    @BeforeEach
    void ready() {
        awaitReady();
    }

    static Stream<Arguments> invalidEvents() {
        return Stream.of(
                Arguments.of("NOT_UTF8", new byte[] {(byte) 0xC3, (byte) 0x28}),
                Arguments.of("NOT_JSON", utf8("não é json " + MARKER)),
                Arguments.of("NOT_JSON", utf8("[1, 2, \"" + MARKER + "\"]")),
                Arguments.of("TYPE_UNKNOWN", movie("\"type\":\"other\",", "2000", "\"" + MARKER + "\"", "\"P\"", "true")),
                Arguments.of("RUN_ID_MISSING", utf8("{\"type\":\"load-completed\",\"count\":1,\"note\":\"" + MARKER + "\"}")),
                Arguments.of("YEAR_MISSING", movie("", null, "\"" + MARKER + "\"", "\"P\"", "true")),
                Arguments.of("YEAR_NOT_NUMERIC", movie("", "\"1990\"", "\"" + MARKER + "\"", "\"P\"", "true")),
                Arguments.of("YEAR_NOT_NUMERIC", movie("", "1990.5", "\"" + MARKER + "\"", "\"P\"", "true")),
                Arguments.of("YEAR_OUT_OF_RANGE", movie("", "0", "\"" + MARKER + "\"", "\"P\"", "true")),
                Arguments.of("YEAR_OUT_OF_RANGE", movie("", "10000", "\"" + MARKER + "\"", "\"P\"", "true")),
                Arguments.of("TITLE_MISSING", movie("", "2000", "\"   \"", "\"" + MARKER + "\"", "true")),
                Arguments.of("PRODUCERS_MISSING", movie("", "2000", "\"" + MARKER + "\"", "\" , and \"", "true")),
                Arguments.of("WINNER_INVALID", movie("", "2000", "\"" + MARKER + "\"", "\"P\"", "\"yes\"")),
                Arguments.of("WINNER_INVALID", movie("", "2000", "\"" + MARKER + "\"", "\"P\"", null)),
                Arguments.of("FIELD_TOO_LONG", movie("", "2000", "\"" + MARKER + "T".repeat(501) + "\"", "\"P\"", "true")),
                Arguments.of("FIELD_TOO_LONG", movie("", "2000", "\"" + MARKER + "\"", "\"" + "P".repeat(1001) + "\"", "true")));
    }

    @ParameterizedTest(name = "S003 FR-015 US3-3 US3-4 SC-006 FR-017 {0} vai ao tópico de erros")
    @MethodSource("invalidEvents")
    void invalidEventGoesToTheErrorTopic(String reason, byte[] payload, CapturedOutput output) {
        int sequence = SEQUENCE.incrementAndGet();
        String key = "invalido-" + sequence;
        double rejectedBefore = rejected();
        // Cada caso anterior deixou exatamente um registro no tópico de erros.
        int errorsBefore = sequence - 2101;

        KafkaTestSupport.send(brokers(), MOVIES_TOPIC, key, payload);
        // Um evento válido logo depois do inválido é gravado (US3-4, SC-006).
        sendMovie(sequence + "|Valido", "{\"year\":" + sequence + ",\"title\":\"Valido\",\"studios\":null,"
                + "\"producers\":\"P\",\"winner\":false}");
        await("evento válido seguinte gravado", 5, () -> total("year=" + sequence) == 1);

        List<Record> errors = KafkaTestSupport.read(brokers(), ERRORS_TOPIC, errorsBefore + 1, Duration.ofSeconds(10));
        Record error = errors.stream().filter(record -> key.equals(record.key())).findFirst()
                .orElseThrow(() -> new AssertionError("o tópico de erros não recebeu " + key));
        assertThat(error.value()).as("bytes originais").isEqualTo(payload);
        assertThat(error.headers().get("awards-error-reason")).startsWith(reason + ":");
        assertThat(error.headers()).containsKeys("awards-error-at", "awards-source-topic",
                "awards-source-partition", "awards-source-offset");
        assertThat(error.headers().get("awards-source-topic")).isEqualTo(MOVIES_TOPIC);

        assertThat(output.getAll()).contains("Evento rejeitado").contains(reason);
        assertThat(output.getAll()).as("o log não traz o conteúdo do evento").doesNotContain(MARKER);
        assertThat(total("")).as("o evento inválido não foi gravado").isEqualTo(SEQUENCE.get() - 2100);
        assertThat(rejected()).isEqualTo(rejectedBefore + 1);
    }

    private double rejected() {
        String metrics = http.get().uri("/actuator/prometheus").exchange().expectStatus().isOk()
                .expectBody(String.class).returnResult().getResponseBody();
        return PrometheusText.sum(PrometheusText.parse(metrics), "awards_ingestion_events_total",
                sample -> "rejected".equals(sample.label("outcome")));
    }

    private static byte[] utf8(String text) {
        return text.getBytes(StandardCharsets.UTF_8);
    }

    /** Evento de filme com os valores JSON dados; um valor nulo omite o campo. */
    private static byte[] movie(String prefix, String year, String title, String producers, String winner) {
        StringBuilder json = new StringBuilder("{").append(prefix);
        if (year != null) {
            json.append("\"year\":").append(year).append(',');
        }
        json.append("\"title\":").append(title).append(",\"studios\":null,\"producers\":").append(producers);
        if (winner != null) {
            json.append(",\"winner\":").append(winner);
        }
        return utf8(json.append('}').toString());
    }
}
