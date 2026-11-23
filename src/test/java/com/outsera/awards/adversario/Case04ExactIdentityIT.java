package com.outsera.awards.adversario;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

/** Caso 04 do testador: nomes contidos, em outra caixa ou com espaço interno a mais são distintos. */
@TestPropertySource(properties = "movies.csv.path=classpath:" + AdversarialCaseIT.DIR + Case04ExactIdentityIT.CASE + ".csv")
class Case04ExactIdentityIT extends AdversarialCaseIT {

    static final String CASE = "04-identidade-exata";

    @Test
    @DisplayName("Adversário 04 FR-008 FR-011 FR-012 identidade exata dos nomes")
    void matchesExpected() {
        assertMatchesExpected(CASE);
    }
}
