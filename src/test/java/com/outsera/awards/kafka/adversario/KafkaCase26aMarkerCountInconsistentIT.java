package com.outsera.awards.kafka.adversario;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

/** Caso 26, variante A: {@code count} 1 com nenhum filme publicado. O inteiro é válido e informativo. */
@DirtiesContext
@TestPropertySource(properties = {KafkaAdversarialIT.EMPTY_CSV, KafkaAdversarialIT.HOLD_CLASS,
        KafkaAdversarialIT.HOLD_COMPLETION, KafkaAdversarialIT.HOLD_TOPIC,
        "awards.test.context=KafkaCase26aMarkerCountInconsistentIT"})
class KafkaCase26aMarkerCountInconsistentIT extends MarkerCountCase {

    @Test
    @DisplayName("S004 FR-035 S003 FR-011 FR-013 SC-003 caso 26-A: count inteiro inconsistente é informativo e libera a readiness")
    void inconsistentIntegerCountReleasesReadiness(CapturedOutput output) {
        run(output, "1", null);
    }
}
