package com.outsera.awards.adversario;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

/** Caso 07 do testador: linhas inválidas entre vitórias, uma delas com a chave de uma vencedora posterior. */
@TestPropertySource(properties = "movies.csv.path=classpath:" + AdversarialCaseIT.DIR + Case07InvalidBetweenWinnersIT.CASE + ".csv")
class Case07InvalidBetweenWinnersIT extends AdversarialCaseIT {

    static final String CASE = "07-invalidas-entre-vencedores";

    @Test
    @DisplayName("Adversário 07 FR-006 FR-006d FR-011 linhas inválidas entre vencedores")
    void matchesExpected() {
        assertMatchesExpected(CASE);
    }
}
