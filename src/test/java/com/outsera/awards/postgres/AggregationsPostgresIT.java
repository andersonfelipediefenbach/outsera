package com.outsera.awards.postgres;

import com.outsera.awards.support.PostgresSupport;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * As duas agregações dão a mesma resposta no H2 e no PostgreSQL com o arquivo fornecido (spec 006, FR-001 e
 * FR-002; research R7): o {@code group by ... having} e a consulta dos estúdios precisam ser portáveis. Roda só
 * no perfil {@code postgres-it}.
 */
class AggregationsPostgresIT {

    private static final String SCHEMA = PostgresSupport.newSchema(AggregationsPostgresIT.class);

    @AfterAll
    static void dropSchema() {
        PostgresSupport.dropSchema(SCHEMA);
    }

    @Test
    @DisplayName("S006 FR-001 FR-002 agregações iguais no H2 e no PostgreSQL com o arquivo fornecido")
    void sameAggregationsOnBothDatabases() {
        EquivalencePostgresIT.assertSameResponses("classpath:movielist.csv", SCHEMA,
                List.of("/api/aggregations/years-with-multiple-winners", "/api/aggregations/studios-with-win-count"));
    }
}
