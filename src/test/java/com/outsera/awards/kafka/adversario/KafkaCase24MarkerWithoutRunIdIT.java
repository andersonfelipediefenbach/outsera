package com.outsera.awards.kafka.adversario;

import static org.assertj.core.api.Assertions.assertThat;

import com.outsera.awards.support.KafkaTestSupport.Record;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

/**
 * Caso 24 do testador adversário: marcador sem {@code runId} e um filme com a chave {@code load-completed}.
 * O que decide o tipo do evento é o valor, não a chave.
 */
@DirtiesContext
@TestPropertySource(properties = {KafkaAdversarialIT.EMPTY_CSV, KafkaAdversarialIT.HOLD_CLASS,
        KafkaAdversarialIT.HOLD_COMPLETION, KafkaAdversarialIT.HOLD_TOPIC,
        "awards.test.context=KafkaCase24MarkerWithoutRunIdIT"})
class KafkaCase24MarkerWithoutRunIdIT extends KafkaAdversarialIT {

    private static final String NO_RUN_ID = "{\"type\":\"load-completed\",\"count\":0}";

    @Test
    @DisplayName("S004 FR-035 S003 FR-012 FR-013 FR-015 SC-003 caso 24: marcador sem runId é RUN_ID_MISSING e a chave load-completed não faz de um filme um marcador")
    void markerWithoutRunIdIsRejectedAndTheKeyDoesNotDecideTheType(CapturedOutput output) {
        Record current = held(1).getFirst();
        send("load-completed", NO_RUN_ID);
        send("load-completed", BASE);
        awaitConsumed();
        assertThat(readinessStatus()).as("readiness sem o marcador desta execução").isEqualTo(503);
        assertMovies(movie(2000, "Base", null, "Iris", true));
        assertIntervals(List.of(), List.of());

        release(current);
        awaitConsumed();
        assertReady();
        send("2002|Controle", CONTROL);
        awaitConsumed();

        assertErrors(output, rejected("load-completed", "RUN_ID_MISSING", NO_RUN_ID));
        assertMovies(movie(2000, "Base", null, "Iris", true), movie(2002, "Controle", null, "Iris", true));
        assertReady();
        assertControlIntervals();
    }
}
