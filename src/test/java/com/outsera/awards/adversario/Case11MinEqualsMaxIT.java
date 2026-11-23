package com.outsera.awards.adversario;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

/** Caso 11 do testador: todos os pares têm o mesmo intervalo e aparecem nas duas listas. */
@TestPropertySource(properties = "movies.csv.path=classpath:" + AdversarialCaseIT.DIR + Case11MinEqualsMaxIT.CASE + ".csv")
class Case11MinEqualsMaxIT extends AdversarialCaseIT {

    static final String CASE = "11-minimo-igual-ao-maximo";

    @Test
    @DisplayName("Adversário 11 FR-012 FR-014 FR-015 mínimo igual ao máximo")
    void matchesExpected() {
        assertMatchesExpected(CASE);
    }
}
