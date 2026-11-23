package com.outsera.awards.adversario;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

/** Caso 03 do testador: vários filmes no mesmo ano geram intervalos 0 repetidos, sem duplicatas. */
@TestPropertySource(properties = "movies.csv.path=classpath:" + AdversarialCaseIT.DIR + Case03ZeroWithMultiplicityIT.CASE + ".csv")
class Case03ZeroWithMultiplicityIT extends AdversarialCaseIT {

    static final String CASE = "03-zero-com-multiplicidade";

    @Test
    @DisplayName("Adversário 03 FR-006d FR-010 FR-013 FR-014 intervalos zero com multiplicidade")
    void matchesExpected() {
        assertMatchesExpected(CASE);
    }
}
