package com.outsera.awards.adversario;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

/** Caso 10 do testador: créditos repetidos, duplicatas e indicações, sem nenhuma segunda vitória. */
@TestPropertySource(properties = "movies.csv.path=classpath:" + AdversarialCaseIT.DIR + Case10RepeatsWithoutSecondWinIT.CASE + ".csv")
class Case10RepeatsWithoutSecondWinIT extends AdversarialCaseIT {

    static final String CASE = "10-repeticoes-sem-segunda-vitoria";

    @Test
    @DisplayName("Adversário 10 FR-006d FR-010 FR-011 FR-018 repetições sem segunda vitória")
    void matchesExpected() {
        assertMatchesExpected(CASE);
    }
}
