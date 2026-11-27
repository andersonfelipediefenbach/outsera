package com.outsera.awards.aggregations.adversario;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

/** Caso 19, segunda execução: dois estúdios só, nenhum ano com dois vencedores (spec 006, FR-001 a FR-003). */
@TestPropertySource(properties = "movies.csv.path=classpath:scenarios/adversario-front/19b-agregacoes-vazias-e-dois-estudios.csv")
class FrontCase19bTwoStudiosIT extends FrontAdversarialCaseIT {

    @Test
    @DisplayName("S006 adversário caso 19 FR-002 só Alfa e Beta, sem anos e sem intervalos")
    void twoStudios() {
        assertBody(YEARS, "{\"years\":[]}");
        assertBody(STUDIOS, "{\"studios\":[{\"name\":\"Alfa\",\"winCount\":1},{\"name\":\"Beta\",\"winCount\":1}]}");
        assertBody(INTERVALS, "{\"min\":[],\"max\":[]}");
    }
}
