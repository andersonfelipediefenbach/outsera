package com.outsera.awards.postgres;

import static org.assertj.core.api.Assertions.assertThat;

import com.outsera.awards.support.ApplicationStarter;
import com.outsera.awards.support.Http;
import com.outsera.awards.support.IntervalItem;
import com.outsera.awards.support.PostgresSupport;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

/**
 * Carga direta sobre um PostgreSQL persistente, sem Kafka (spec 003, FR-002 e FR-014; US2-3 e US2-5): o
 * mesmo arquivo carregado de novo não falha nem duplica, e um arquivo alterado atualiza os filmes. Três
 * subidas reais sobre o mesmo esquema (cláusula "múltiplas execuções" do Princípio I).
 */
class RestartDirectPostgresIT {

    private static final String BASE = "classpath:scenarios/postgres/restart-base.csv";
    private static final String UPDATED = "classpath:scenarios/postgres/restart-updated.csv";
    private static final String SCHEMA = PostgresSupport.newSchema(RestartDirectPostgresIT.class);

    @AfterAll
    static void dropSchema() {
        PostgresSupport.dropSchema(SCHEMA);
    }

    private record State(long total, String intervals) {
    }

    private static State run(String csvPath) {
        return ApplicationStarter.runAndCall(csvPath, PostgresSupport.args(SCHEMA),
                port -> new State(Http.totalMovies(port), Http.get(port, "/api/producers/intervals").text()));
    }

    @Test
    @DisplayName("S003 FR-002 FR-014 US2-3 US2-5 reinícios da carga direta sobre o mesmo PostgreSQL")
    void restartsDoNotDuplicateAndUpdate() throws Exception {
        String baseIntervals = IntervalItem.json(
                List.of(new IntervalItem("Producer A", 5, 2000, 2005)),
                List.of(new IntervalItem("Producer A", 5, 2000, 2005)));

        State first = run(BASE);
        State second = run(BASE);
        assertThat(first.total()).isEqualTo(4);
        assertThat(second.total()).isEqualTo(4);
        JSONAssert.assertEquals(baseIntervals, first.intervals(), JSONCompareMode.STRICT);
        JSONAssert.assertEquals(baseIntervals, second.intervals(), JSONCompareMode.STRICT);

        State updated = run(UPDATED);
        assertThat(updated.total()).isEqualTo(4);
        JSONAssert.assertEquals(IntervalItem.json(
                        List.of(new IntervalItem("Producer A", 2, 2003, 2005)),
                        List.of(new IntervalItem("Producer A", 3, 2000, 2003))),
                updated.intervals(), JSONCompareMode.STRICT);
    }
}
