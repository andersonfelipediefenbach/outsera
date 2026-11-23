package com.outsera.awards.adversario;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

/**
 * Caso 13 do testador: nomes empatados que diferem em acento e em caixa. O {@code expected.json} foi
 * escrito pelo implementador a partir da decisão da sessão 2026-11-22 (FR-015, ordem Unicode de
 * {@code String.compareTo}), e não pelo testador.
 */
@TestPropertySource(properties = "movies.csv.path=classpath:" + AdversarialCaseIT.DIR + Case13AccentAndCaseOrderIT.CASE + ".csv")
class Case13AccentAndCaseOrderIT extends AdversarialCaseIT {

    static final String CASE = "13-ordenacao-com-acentos-e-caixa";

    @Test
    @DisplayName("Adversário 13 FR-008 FR-015 ordenação Unicode com acentos e caixa")
    void matchesExpected() {
        assertMatchesExpected(CASE);
    }
}
