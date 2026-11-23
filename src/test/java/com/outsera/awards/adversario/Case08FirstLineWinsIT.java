package com.outsera.awards.adversario;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

/** Caso 08 do testador: duplicatas com dados conflitantes; a primeira linha aceita prevalece. */
@TestPropertySource(properties = "movies.csv.path=classpath:" + AdversarialCaseIT.DIR + Case08FirstLineWinsIT.CASE + ".csv")
class Case08FirstLineWinsIT extends AdversarialCaseIT {

    static final String CASE = "08-primeira-linha-aceita";

    @Test
    @DisplayName("Adversário 08 FR-004 FR-006d FR-013 primeira linha aceita prevalece")
    void matchesExpected() {
        assertMatchesExpected(CASE);
    }
}
