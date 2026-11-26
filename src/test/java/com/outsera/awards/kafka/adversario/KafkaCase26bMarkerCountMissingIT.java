package com.outsera.awards.kafka.adversario;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

/** Caso 26, variante B: marcador sem {@code count}. */
@DirtiesContext
@TestPropertySource(properties = {KafkaAdversarialIT.EMPTY_CSV, KafkaAdversarialIT.HOLD_CLASS,
        KafkaAdversarialIT.HOLD_COMPLETION, KafkaAdversarialIT.HOLD_TOPIC,
        "awards.test.context=KafkaCase26bMarkerCountMissingIT"})
class KafkaCase26bMarkerCountMissingIT extends MarkerCountCase {

    @Test
    @DisplayName("S004 FR-035 S003 FR-011 FR-013 FR-015 caso 26-B: marcador sem count é COUNT_INVALID e não libera a readiness")
    void missingCountIsRejected(CapturedOutput output) {
        run(output, null, "COUNT_INVALID");
    }
}
