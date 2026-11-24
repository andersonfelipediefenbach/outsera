package com.outsera.awards.postgres;

import com.outsera.awards.support.PostgresSupport;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * A ordem da listagem (ano e depois título) é a mesma no H2 e no PostgreSQL, inclusive com acentos e com
 * maiúsculas e minúsculas misturadas (spec 003, FR-008; research R4). Usa o arquivo do caso 13 do
 * testador adversário, o mesmo do {@code Case13AccentAndCaseOrderIT}.
 */
class ListingOrderPostgresIT {

    private static final String CSV = "classpath:scenarios/adversario/13-ordenacao-com-acentos-e-caixa.csv";
    private static final String SCHEMA = PostgresSupport.newSchema(ListingOrderPostgresIT.class);

    @AfterAll
    static void dropSchema() {
        PostgresSupport.dropSchema(SCHEMA);
    }

    @Test
    @DisplayName("S003 FR-008 ordem da listagem com acentos e caixa igual no H2 e no PostgreSQL")
    void sameOrderOnBothDatabases() {
        EquivalencePostgresIT.assertSameResponses(CSV, SCHEMA,
                List.of("/api/movies?size=100", "/api/producers/intervals"));
    }
}
