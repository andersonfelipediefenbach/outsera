package com.outsera.awards.adversario;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

/** Caso 01 do testador: três empatados no mínimo e três no máximo, fora de ordem no arquivo. */
@TestPropertySource(properties = "movies.csv.path=classpath:" + AdversarialCaseIT.DIR + Case01TiesAtBothEndsIT.CASE + ".csv")
class Case01TiesAtBothEndsIT extends AdversarialCaseIT {

    static final String CASE = "01-empates-nas-duas-pontas";

    @Test
    @DisplayName("Adversário 01 FR-012 FR-014 FR-015 FR-016 empates no mínimo e no máximo")
    void matchesExpected() {
        assertMatchesExpected(CASE);
    }
}
