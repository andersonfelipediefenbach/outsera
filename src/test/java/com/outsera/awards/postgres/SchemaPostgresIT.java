package com.outsera.awards.postgres;

import static org.assertj.core.api.Assertions.assertThat;

import com.outsera.awards.support.IntervalItem;
import com.outsera.awards.support.PostgresSupport;
import com.outsera.awards.support.ScenarioIT;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * O esquema do modo padrão sobe no PostgreSQL pelas mesmas migrações do Flyway, e o resultado de
 * referência é o mesmo (spec 003, FR-004 a FR-006, US2-1 e US2-2). Roda só no perfil Maven
 * {@code postgres-it} (Princípio VI, constituição 1.7.0).
 */
@ActiveProfiles("postgres")
class SchemaPostgresIT extends ScenarioIT {

    private static final String SCHEMA = PostgresSupport.newSchema(SchemaPostgresIT.class);

    @Autowired
    private DataSource dataSource;

    @DynamicPropertySource
    static void postgres(DynamicPropertyRegistry registry) {
        for (Map.Entry<String, String> entry : PostgresSupport.properties(SCHEMA).entrySet()) {
            registry.add(entry.getKey(), entry::getValue);
        }
    }

    @AfterAll
    static void dropSchema() {
        PostgresSupport.dropSchema(SCHEMA);
    }

    @Test
    @DisplayName("S003 FR-004 FR-005 US2-1 a V1 do Flyway cria o esquema no PostgreSQL e o validate passa")
    void flywayCreatesTheSchema() {
        PostgresSupport.assertIsPostgres(dataSource);
        // A migração não tem endpoint: a verificação é pelo banco (Princípio I).
        List<Map<String, Object>> history = new JdbcTemplate(dataSource).queryForList(
                "select version, success from " + SCHEMA + ".flyway_schema_history where version is not null");
        assertThat(history).hasSize(1);
        assertThat(history.getFirst()).containsEntry("version", "1").containsEntry("success", true);
        // A subida passou pelo ddl-auto: validate: se o esquema divergisse do mapeamento, o contexto não subiria.
    }

    @Test
    @DisplayName("S003 FR-006 US2-2 resultado de referência no PostgreSQL")
    void referenceResult() {
        assertIntervals(
                List.of(new IntervalItem("Joel Silver", 1, 1990, 1991)),
                List.of(new IntervalItem("Matthew Vaughn", 13, 2002, 2015)));
        assertThat(movies("size=1").totalElements()).isEqualTo(206);
    }
}
