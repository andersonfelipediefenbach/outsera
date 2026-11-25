package com.outsera.awards.kafka;

import static org.assertj.core.api.Assertions.assertThat;

import com.outsera.awards.support.KafkaScenarioIT;
import com.outsera.awards.support.KafkaTestSupport;
import com.outsera.awards.support.KafkaTestSupport.Record;
import com.outsera.awards.support.PrometheusText;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

/**
 * Cada evento inválido vai ao tópico de erros com os bytes originais e o motivo, sem ser gravado e sem
 * travar o consumo (spec 003, FR-015, FR-017, US3-3, US3-4 e SC-006). Os motivos são os do
 * data-model.md, inclusive os de conteúdo, que são as mesmas regras da linha do CSV.
 *
 * <p>Os 16 casos são publicados numa única execução, cada um com a sua chave, seguidos de um evento
 * válido. O tópico tem uma partição e é consumido em ordem: quando o evento válido aparece na listagem,
 * todos os inválidos já foram processados sem travar o consumo. Depois, cada caso é conferido no tópico de
 * erros pela chave, com uma mensagem própria por caso.
 */
// Muda os dados ou a readiness: contexto próprio, fechado no fim (KafkaScenarioIT).
@DirtiesContext
@TestPropertySource(properties = {"movies.csv.path=classpath:scenarios/header-only.csv", "awards.test.context=KafkaInvalidEventsIT"})
@Tag("log")
class KafkaInvalidEventsIT extends KafkaScenarioIT {

    private static final String MARKER = "conteudo-do-evento";

    /** Um evento inválido: a chave, o código do motivo esperado e os bytes enviados. */
    private record Case(String key, String reason, byte[] payload) {
    }

    private static List<Case> cases() {
        return List.of(
                new Case("invalido-01", "NOT_UTF8", new byte[] {(byte) 0xC3, (byte) 0x28}),
                new Case("invalido-02", "NOT_JSON", utf8("não é json " + MARKER)),
                new Case("invalido-03", "NOT_JSON", utf8("[1, 2, \"" + MARKER + "\"]")),
                new Case("invalido-04", "TYPE_UNKNOWN", movie("\"type\":\"other\",", "2000", "\"" + MARKER + "\"", "\"P\"", "true")),
                new Case("invalido-05", "RUN_ID_MISSING", utf8("{\"type\":\"load-completed\",\"count\":1,\"note\":\"" + MARKER + "\"}")),
                new Case("invalido-06", "YEAR_MISSING", movie("", null, "\"" + MARKER + "\"", "\"P\"", "true")),
                new Case("invalido-07", "YEAR_NOT_NUMERIC", movie("", "\"1990\"", "\"" + MARKER + "\"", "\"P\"", "true")),
                new Case("invalido-08", "YEAR_NOT_NUMERIC", movie("", "1990.5", "\"" + MARKER + "\"", "\"P\"", "true")),
                new Case("invalido-09", "YEAR_OUT_OF_RANGE", movie("", "0", "\"" + MARKER + "\"", "\"P\"", "true")),
                new Case("invalido-10", "YEAR_OUT_OF_RANGE", movie("", "10000", "\"" + MARKER + "\"", "\"P\"", "true")),
                new Case("invalido-11", "TITLE_MISSING", movie("", "2000", "\"   \"", "\"" + MARKER + "\"", "true")),
                new Case("invalido-12", "PRODUCERS_MISSING", movie("", "2000", "\"" + MARKER + "\"", "\" , and \"", "true")),
                new Case("invalido-13", "WINNER_INVALID", movie("", "2000", "\"" + MARKER + "\"", "\"P\"", "\"yes\"")),
                new Case("invalido-14", "WINNER_INVALID", movie("", "2000", "\"" + MARKER + "\"", "\"P\"", null)),
                new Case("invalido-15", "FIELD_TOO_LONG", movie("", "2000", "\"" + MARKER + "T".repeat(501) + "\"", "\"P\"", "true")),
                new Case("invalido-16", "FIELD_TOO_LONG", movie("", "2000", "\"" + MARKER + "\"", "\"" + "P".repeat(1001) + "\"", "true")));
    }

    @Test
    @DisplayName("S003 FR-015 US3-3 US3-4 SC-006 FR-017 os 16 tipos de evento inválido vão ao tópico de erros")
    void invalidEventsGoToTheErrorTopic(CapturedOutput output) {
        awaitReady();
        List<Case> cases = cases();
        double rejectedBefore = rejected();

        for (Case invalid : cases) {
            KafkaTestSupport.send(brokers(), MOVIES_TOPIC, invalid.key(), invalid.payload());
        }
        // Um evento válido depois de todos os inválidos é gravado: nenhum deles travou o consumo (US3-4).
        sendMovie("2101|Valido", "{\"year\":2101,\"title\":\"Valido\",\"studios\":null,\"producers\":\"P\",\"winner\":false}");
        await("evento válido seguinte gravado", 10, () -> total("year=2101") == 1);

        Map<String, Record> errors = KafkaTestSupport.read(brokers(), ERRORS_TOPIC, cases.size(), Duration.ofSeconds(10))
                .stream().collect(Collectors.toMap(Record::key, Function.identity()));
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(errors).as("registros no tópico de erros").hasSize(cases.size());
        for (Case invalid : cases) {
            Record error = errors.get(invalid.key());
            softly.assertThat(error).as("%s (%s) no tópico de erros", invalid.key(), invalid.reason()).isNotNull();
            if (error == null) {
                continue;
            }
            softly.assertThat(error.value()).as("%s: bytes originais", invalid.key()).isEqualTo(invalid.payload());
            softly.assertThat(error.headers().get("awards-error-reason")).as("%s: motivo", invalid.key())
                    .startsWith(invalid.reason() + ":");
            softly.assertThat(error.headers()).as("%s: cabeçalhos", invalid.key()).containsKeys("awards-error-at",
                    "awards-source-topic", "awards-source-partition", "awards-source-offset");
            softly.assertThat(error.headers().get("awards-source-topic")).as("%s: tópico de origem", invalid.key())
                    .isEqualTo(MOVIES_TOPIC);
            // O aviso do log cita a posição do próprio evento e o motivo dele.
            softly.assertThat(output.getAll()).as("%s: aviso no log", invalid.key()).contains(
                    "Evento rejeitado (tópico " + MOVIES_TOPIC + ", partição 0, offset "
                            + error.headers().get("awards-source-offset") + "): " + invalid.reason() + ":");
        }
        softly.assertThat(output.getAll()).as("o log não traz o conteúdo dos eventos").doesNotContain(MARKER);
        softly.assertThat(total("")).as("só o evento válido foi gravado").isEqualTo(1);
        softly.assertThat(rejected()).as("contador de rejeitados (FR-017)").isEqualTo(rejectedBefore + cases.size());
        softly.assertAll();
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
