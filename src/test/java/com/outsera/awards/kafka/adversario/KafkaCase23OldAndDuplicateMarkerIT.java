package com.outsera.awards.kafka.adversario;

import static org.assertj.core.api.Assertions.assertThat;

import com.outsera.awards.support.KafkaTestSupport.Record;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

/**
 * Caso 23 do testador adversário: marcador de uma execução anterior, repetido, e o marcador atual
 * entregue duas vezes. O {@code runId} anterior é outro UUID, com a mesma serialização do publicador: para
 * o consumidor, um marcador de outra execução é qualquer {@code runId} diferente do desta.
 */
@DirtiesContext
@TestPropertySource(properties = {KafkaAdversarialIT.EMPTY_CSV, KafkaAdversarialIT.HOLD_CLASS,
        KafkaAdversarialIT.HOLD_COMPLETION, KafkaAdversarialIT.HOLD_TOPIC,
        "awards.test.context=KafkaCase23OldAndDuplicateMarkerIT"})
class KafkaCase23OldAndDuplicateMarkerIT extends KafkaAdversarialIT {

    @Test
    @DisplayName("S004 FR-035 S003 FR-013 FR-016 SC-003 caso 23: marcador antigo não libera e o marcador atual repetido não muda nada")
    void oldMarkerIsIgnoredAndCurrentReplayIsHarmless(CapturedOutput output) {
        Record current = held(1).getFirst();
        String old = "{\"type\":\"load-completed\",\"runId\":\"" + UUID.randomUUID() + "\",\"count\":0}";
        send("load-completed", old);
        send("load-completed", old);
        awaitConsumed();
        assertThat(readinessStatus()).as("readiness depois dos marcadores antigos").isEqualTo(503);
        assertMovies();
        assertIntervals(List.of(), List.of());

        release(current);
        awaitConsumed();
        assertReady();

        send("2000|Base", BASE);
        awaitConsumed();
        assertReady();
        release(current);
        awaitConsumed();
        assertReady();
        assertMovies(movie(2000, "Base", null, "Iris", true));
        assertIntervals(List.of(), List.of());

        send("2002|Controle", CONTROL);
        awaitConsumed();
        assertReady();
        assertErrors(output);
        assertMovies(movie(2000, "Base", null, "Iris", true), movie(2002, "Controle", null, "Iris", true));
        assertControlIntervals();
    }
}
