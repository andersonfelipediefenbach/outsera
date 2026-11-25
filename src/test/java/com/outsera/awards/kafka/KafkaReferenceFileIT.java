package com.outsera.awards.kafka;

import static org.assertj.core.api.Assertions.assertThat;

import com.outsera.awards.support.IntervalItem;
import com.outsera.awards.support.KafkaScenarioIT;
import com.outsera.awards.support.KafkaTestSupport;
import com.outsera.awards.support.KafkaTestSupport.Record;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.test.context.TestPropertySource;

/**
 * Com o perfil {@code kafka}, o arquivo fornecido é publicado no tópico e consumido pelo mesmo serviço
 * de gravação, e a carga direta não roda (spec 003, US1-1, US1-6, FR-009 a FR-012).
 */
@TestPropertySource(properties = "movies.csv.path=classpath:movielist.csv")
class KafkaReferenceFileIT extends KafkaScenarioIT {

    @Test
    @DisplayName("S003 US1-1 FR-012 resultado de referência pela ingestão por Kafka")
    void referenceResult() {
        awaitReady();
        assertIntervals(
                List.of(new IntervalItem("Joel Silver", 1, 1990, 1991)),
                List.of(new IntervalItem("Matthew Vaughn", 13, 2002, 2015)));
    }

    @Test
    @DisplayName("S003 FR-010 FR-011 um evento por filme, com chave e runId, e depois o de carga concluída")
    void oneEventPerMovieThenTheCompletion() {
        awaitReady();
        List<Record> records = KafkaTestSupport.read(brokers(), MOVIES_TOPIC, 207, Duration.ofSeconds(30));
        assertThat(records).hasSize(207);
        List<Record> movies = records.subList(0, 206);
        Record completion = records.get(206);
        String runId = movies.getFirst().headers().get("awards-run-id");
        assertThat(runId).isNotBlank();
        assertThat(movies).allSatisfy(record -> {
            assertThat(record.key()).matches("\\d+\\|.+");
            assertThat(record.headers()).containsEntry("awards-run-id", runId);
            assertThat(record.text()).contains("\"winner\":");
        });
        assertThat(movies.getFirst().key()).isEqualTo("1980|Can't Stop the Music");
        assertThat(completion.key()).isEqualTo("load-completed");
        assertThat(completion.text())
                .contains("\"type\":\"load-completed\"")
                .contains("\"runId\":\"" + runId + "\"")
                .contains("\"count\":206");
    }

    @Test
    @DisplayName("S003 US1-6 FR-009 a carga direta não roda com o perfil kafka")
    void directLoadDoesNotRun(CapturedOutput output) {
        awaitReady();
        assertThat(output.getAll()).doesNotContain("Carga do arquivo classpath:movielist.csv concluída");
    }
}
