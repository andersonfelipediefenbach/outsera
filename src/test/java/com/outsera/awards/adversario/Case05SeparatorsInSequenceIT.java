package com.outsera.awards.adversario;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

/** Caso 05 do testador: vírgulas vazias, {@code and} repetido, no início e no fim do campo. */
@TestPropertySource(properties = "movies.csv.path=classpath:" + AdversarialCaseIT.DIR + Case05SeparatorsInSequenceIT.CASE + ".csv")
class Case05SeparatorsInSequenceIT extends AdversarialCaseIT {

    static final String CASE = "05-separadores-em-sequencia";

    @Test
    @DisplayName("Adversário 05 FR-007 FR-009 FR-010 separadores em sequência")
    void matchesExpected() {
        assertMatchesExpected(CASE);
    }
}
