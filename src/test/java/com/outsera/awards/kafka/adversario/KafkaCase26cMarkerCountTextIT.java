package com.outsera.awards.kafka.adversario;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

/** Caso 26, variante C: {@code count} como texto ({@code "0"}). */
@DirtiesContext
@TestPropertySource(properties = {KafkaAdversarialIT.EMPTY_CSV, KafkaAdversarialIT.HOLD_CLASS,
        KafkaAdversarialIT.HOLD_COMPLETION, KafkaAdversarialIT.HOLD_TOPIC,
        "awards.test.context=KafkaCase26cMarkerCountTextIT"})
class KafkaCase26cMarkerCountTextIT extends MarkerCountCase {

    @Test
    @DisplayName("S004 FR-035 S003 FR-011 FR-013 FR-015 caso 26-C: count textual é COUNT_INVALID e não libera a readiness")
    void textualCountIsRejected(CapturedOutput output) {
        run(output, "\"0\"", "COUNT_INVALID");
    }
}
