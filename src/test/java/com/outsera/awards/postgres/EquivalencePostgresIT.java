package com.outsera.awards.postgres;

import static org.assertj.core.api.Assertions.assertThat;

import com.outsera.awards.support.ApplicationStarter;
import com.outsera.awards.support.Http;
import com.outsera.awards.support.PostgresSupport;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * O mesmo arquivo dá as mesmas respostas, byte a byte e com o mesmo ETag, no H2 e no PostgreSQL (spec
 * 003, FR-006 e SC-001). Usa {@code SpringApplication} pela cláusula "múltiplas execuções" do Princípio I
 * (constituição 1.5.0): duas subidas reais, comparadas por HTTP.
 */
class EquivalencePostgresIT {

    static final List<String> PATHS = List.of(
            "/api/producers/intervals",
            "/api/movies?size=100&page=0",
            "/api/movies?size=100&page=1",
            "/api/movies?size=100&page=2",
            "/api/movies?winner=true",
            "/api/movies?year=1990");

    private static final String SCHEMA = PostgresSupport.newSchema(EquivalencePostgresIT.class);

    @AfterAll
    static void dropSchema() {
        PostgresSupport.dropSchema(SCHEMA);
    }

    @Test
    @DisplayName("S003 FR-006 SC-001 arquivo fornecido: respostas idênticas no H2 e no PostgreSQL")
    void sameResponsesOnBothDatabases() {
        assertSameResponses("classpath:movielist.csv", SCHEMA, PATHS);
    }

    /** Sobe no H2 e no PostgreSQL com o mesmo arquivo e compara corpo e ETag de cada caminho. */
    static void assertSameResponses(String csvPath, String schema, List<String> paths) {
        Map<String, Http.Response> h2 = ApplicationStarter.runAndCall(csvPath, port -> Http.snapshot(port, paths));
        Map<String, Http.Response> postgres = ApplicationStarter.runWithContext(csvPath,
                PostgresSupport.args(schema), (context, port) -> {
                    PostgresSupport.assertIsPostgres(context.getBean(DataSource.class));
                    return Http.snapshot(port, paths);
                });
        for (String path : paths) {
            assertThat(postgres.get(path).text()).as("corpo de %s", path).isEqualTo(h2.get(path).text());
            assertThat(postgres.get(path).body()).as("bytes de %s", path).isEqualTo(h2.get(path).body());
            assertThat(postgres.get(path).etag()).as("ETag de %s", path).isPresent().isEqualTo(h2.get(path).etag());
        }
    }
}
