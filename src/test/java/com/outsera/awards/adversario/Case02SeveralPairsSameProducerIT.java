package com.outsera.awards.adversario;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

/** Caso 02 do testador: o mesmo produtor tem dois mínimos e dois máximos. */
@TestPropertySource(properties = "movies.csv.path=classpath:" + AdversarialCaseIT.DIR + Case02SeveralPairsSameProducerIT.CASE + ".csv")
class Case02SeveralPairsSameProducerIT extends AdversarialCaseIT {

    static final String CASE = "02-varios-pares-do-mesmo-produtor";

    @Test
    @DisplayName("Adversário 02 FR-012 FR-014 FR-015 FR-016 vários pares do mesmo produtor")
    void matchesExpected() {
        assertMatchesExpected(CASE);
    }
}
