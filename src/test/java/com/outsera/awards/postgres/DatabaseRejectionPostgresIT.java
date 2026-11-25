package com.outsera.awards.postgres;

import static org.assertj.core.api.Assertions.assertThat;

import com.outsera.awards.support.ApplicationStarter;
import com.outsera.awards.support.ApplicationStarter.Result;
import com.outsera.awards.support.Http;
import com.outsera.awards.support.KafkaTestSupport;
import com.outsera.awards.support.KafkaTestSupport.Record;
import com.outsera.awards.support.KafkaTestSupport.Topics;
import com.outsera.awards.support.PostgresSupport;
import com.outsera.awards.support.PrometheusText;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;

/**
 * Uma recusa do banco por dado inválido é permanente: o evento vai ao tópico de erros com o motivo
 * {@code DATABASE_REJECTED} e o SQLState, sem nova tentativa, e o evento válido seguinte é gravado (spec
 * 003, FR-015a; research R9; apontamento do Codex no PR do Marco 3).
 *
 * <p>Os dois eventos passam pela validação da aplicação, e só o banco os recusa. O teste muda o esquema da
 * classe por JDBC, com a aplicação já no ar, e devolve o esquema num {@code finally}:
 * <ul>
 *   <li>uma restrição {@code CHECK} que o título viola: SQLState 23514, classe 23 (integridade);</li>
 *   <li>a coluna {@code studios} reduzida a 5 caracteres: SQLState 22001, classe 22 (dado).</li>
 * </ul>
 * É {@link Isolated}, como o {@code RetryPostgresIT}, porque muda o esquema.
 */
@Isolated
class DatabaseRejectionPostgresIT {

    private static final String SCHEMA = PostgresSupport.newSchema(DatabaseRejectionPostgresIT.class);
    private static final String CHECK_TITLE = "Recusado pela Restricao";

    @AfterAll
    static void dropSchema() {
        PostgresSupport.dropSchema(SCHEMA);
    }

    private static double rejected(int port) {
        return PrometheusText.sum(PrometheusText.parse(Http.get(port, "/actuator/prometheus").text()),
                "awards_ingestion_events_total", sample -> "rejected".equals(sample.label("outcome")));
    }

    @Test
    @DisplayName("S003 FR-015a recusa do banco por dado inválido vai ao tópico de erros, sem nova tentativa")
    void permanentDatabaseRejection() {
        String brokers = KafkaTestSupport.sharedBroker().getBrokersAsString();
        Topics topics = KafkaTestSupport.topics(DatabaseRejectionPostgresIT.class);
        List<String> args = new ArrayList<>(PostgresSupport.args(SCHEMA, "postgres,kafka,kafkatest"));
        KafkaTestSupport.properties(brokers, topics).forEach((name, value) -> args.add("--" + name + "=" + value));

        Result result = ApplicationStarter.startAndCall("classpath:scenarios/header-only.csv", args, port -> {
            Http.awaitReady(port, Duration.ofSeconds(60));
            double rejectedBefore = rejected(port);
            PostgresSupport.execute(SCHEMA, "ALTER TABLE movie ADD CONSTRAINT it_check_title CHECK (title <> '"
                    + CHECK_TITLE + "')");
            PostgresSupport.execute(SCHEMA, "ALTER TABLE movie ALTER COLUMN studios TYPE VARCHAR(5)");
            try {
                KafkaTestSupport.sendJson(brokers, topics.movies(), "2050|" + CHECK_TITLE, "{\"year\":2050,"
                        + "\"title\":\"" + CHECK_TITLE + "\",\"studios\":null,\"producers\":\"P\",\"winner\":true}");
                KafkaTestSupport.sendJson(brokers, topics.movies(), "2051|Estudio Comprido", "{\"year\":2051,"
                        + "\"title\":\"Estudio Comprido\",\"studios\":\"Estudio Comprido\",\"producers\":\"P\","
                        + "\"winner\":true}");
                KafkaTestSupport.sendJson(brokers, topics.movies(), "2052|Valido Depois", "{\"year\":2052,"
                        + "\"title\":\"Valido Depois\",\"studios\":null,\"producers\":\"P\",\"winner\":false}");
                Http.await("evento válido depois das recusas", Duration.ofSeconds(15),
                        () -> Http.get(port, "/api/movies?year=2052").text().contains("\"title\":\"Valido Depois\""));
            } finally {
                PostgresSupport.execute(SCHEMA, "ALTER TABLE movie DROP CONSTRAINT IF EXISTS it_check_title");
                PostgresSupport.execute(SCHEMA, "ALTER TABLE movie ALTER COLUMN studios TYPE VARCHAR(1000)");
            }

            Map<String, Record> errors = KafkaTestSupport.read(brokers, topics.errors(), 2, Duration.ofSeconds(10))
                    .stream().collect(Collectors.toMap(Record::key, Function.identity()));
            assertThat(errors).as("registros no tópico de erros").hasSize(2);
            assertThat(errors.get("2050|" + CHECK_TITLE).headers().get("awards-error-reason"))
                    .startsWith("DATABASE_REJECTED:").contains("23514");
            assertThat(errors.get("2051|Estudio Comprido").headers().get("awards-error-reason"))
                    .startsWith("DATABASE_REJECTED:").contains("22001");
            assertThat(Http.totalMovies(port)).as("só o evento válido foi gravado").isEqualTo(1);
            assertThat(rejected(port)).isEqualTo(rejectedBefore + 2);
        });
        assertThat(result.failed()).isFalse();
        assertThat(result.output()).as("sem nova tentativa para a recusa permanente")
                .doesNotContain("Falha ao gravar o evento");
    }
}
