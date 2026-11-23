package com.outsera.awards.adversario;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

/**
 * Caso 12 do testador: {@code AND} e {@code aNd} como separadores. O {@code expected.json} foi escrito
 * pelo implementador a partir da decisão da sessão 2026-11-22 (FR-007), e não pelo testador.
 */
@TestPropertySource(properties = "movies.csv.path=classpath:" + AdversarialCaseIT.DIR + Case12AndSeparatorCaseIT.CASE + ".csv")
class Case12AndSeparatorCaseIT extends AdversarialCaseIT {

    static final String CASE = "12-caixa-do-separador-and";

    @Test
    @DisplayName("Adversário 12 FR-007 FR-008 and sem diferenciar maiúsculas")
    void matchesExpected() {
        assertMatchesExpected(CASE);
    }
}
