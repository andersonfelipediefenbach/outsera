package com.outsera.awards.adversario;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

/** Caso 06 do testador: BOM, linhas em branco, cabeçalho em caixa mista, espaços e acentos. */
@TestPropertySource(properties = "movies.csv.path=classpath:" + AdversarialCaseIT.DIR + Case06BomSpacesUtf8IT.CASE + ".csv")
class Case06BomSpacesUtf8IT extends AdversarialCaseIT {

    static final String CASE = "06-bom-espacos-e-utf8";

    @Test
    @DisplayName("Adversário 06 FR-003 FR-004 FR-005a FR-006e BOM, espaços e UTF-8")
    void matchesExpected() {
        assertMatchesExpected(CASE);
    }
}
