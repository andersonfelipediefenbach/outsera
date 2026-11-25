package com.outsera.awards.postgres;

import static org.assertj.core.api.Assertions.assertThat;

import com.outsera.awards.support.ApplicationStarter;
import com.outsera.awards.support.Http;
import com.outsera.awards.support.KafkaTestSupport;
import com.outsera.awards.support.KafkaTestSupport.Topics;
import com.outsera.awards.support.PostgresSupport;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.test.EmbeddedKafkaBroker;

/**
 * Quatro subidas (três reinícios) no modo Docker, sobre o mesmo PostgreSQL e o mesmo broker (spec 003,
 * US2-3, US2-4, US2-6 e SC-004). A cada subida, o arquivo é publicado de novo. Um filme novo recebido
 * por evento continua, e um filme do arquivo alterado por evento volta ao valor do arquivo
 * (Clarifications, 2026-11-24). Usa {@code SpringApplication} pela cláusula "múltiplas execuções" do
 * Princípio I.
 */
class RestartPostgresIT {

    private static EmbeddedKafkaBroker broker;

    @BeforeAll
    static void startBroker() {
        broker = KafkaTestSupport.startBroker();
    }

    @AfterAll
    static void stopBroker() {
        broker.destroy();
    }

    private static final String CSV = "classpath:movielist.csv";
    private static final String SCHEMA = PostgresSupport.newSchema(RestartPostgresIT.class);
    private static final Pattern ITEM = Pattern.compile("\\{\"year\":(\\d+),\"title\":\"((?:[^\"\\\\]|\\\\.)*)\"");
    private static final Pattern WINNER = Pattern.compile("\"title\":\"Can't Stop the Music\".*?\"winner\":(true|false)");

    @AfterAll
    static void dropSchema() {
        PostgresSupport.dropSchema(SCHEMA);
    }

    private record State(long total, boolean newMoviePresent, boolean cantStopWinner, List<String> keys) {
    }

    private static List<String> args(EmbeddedKafkaBroker broker) {
        Topics topics = KafkaTestSupport.topics(RestartPostgresIT.class);
        List<String> args = new ArrayList<>(PostgresSupport.args(SCHEMA, "postgres,kafka,kafkatest"));
        KafkaTestSupport.properties(broker.getBrokersAsString(), topics)
                .forEach((name, value) -> args.add("--" + name + "=" + value));
        return args;
    }

    private static State observe(int port) {
        List<String> keys = new ArrayList<>();
        for (int page = 0; page < 3; page++) {
            Matcher item = ITEM.matcher(Http.get(port, "/api/movies?size=100&page=" + page).text());
            while (item.find()) {
                keys.add(item.group(1) + "|" + item.group(2));
            }
        }
        Matcher winner = WINNER.matcher(Http.get(port, "/api/movies?year=1980&size=100").text());
        return new State(Http.totalMovies(port), Http.totalMovies(port) > 0 && keys.contains("2030|Filme Novo"),
                winner.find() && Boolean.parseBoolean(winner.group(1)), keys);
    }

    @Test
    @DisplayName("S003 US2-3 US2-4 US2-6 SC-004 três reinícios sobre o mesmo PostgreSQL e o mesmo broker")
    void restartsKeepNewMoviesAndReapplyTheFile() {
        String brokers = broker.getBrokersAsString();
        String topic = KafkaTestSupport.topics(RestartPostgresIT.class).movies();

        ApplicationStarter.runAndCall(CSV, args(broker), port -> {
            Http.awaitReady(port, Duration.ofSeconds(60));
            KafkaTestSupport.sendJson(brokers, topic, "2030|Filme Novo", "{\"year\":2030,\"title\":\"Filme Novo\","
                    + "\"studios\":null,\"producers\":\"Joel Silver\",\"winner\":true}");
            KafkaTestSupport.sendJson(brokers, topic, "1980|Can't Stop the Music", "{\"year\":1980,"
                    + "\"title\":\"Can't Stop the Music\",\"studios\":\"Associated Film Distribution\","
                    + "\"producers\":\"Allan Carr\",\"winner\":false}");
            Http.await("filme novo e atualização gravados", Duration.ofSeconds(10), () -> {
                State state = observe(port);
                return state.newMoviePresent() && !state.cantStopWinner();
            });
            return null;
        });

        for (int run = 2; run <= 4; run++) {
            State state = ApplicationStarter.runAndCall(CSV, args(broker), port -> {
                Http.awaitReady(port, Duration.ofSeconds(60));
                return observe(port);
            });
            assertThat(state.newMoviePresent()).as("execução %d: o filme novo continua (US2-4)", run).isTrue();
            assertThat(state.cantStopWinner()).as("execução %d: o arquivo foi reaplicado (US2-6)", run).isTrue();
            assertThat(state.total()).as("execução %d: total (SC-004)", run).isEqualTo(207);
            Set<String> unique = new HashSet<>(state.keys());
            assertThat(state.keys()).as("execução %d: nenhum filme repetido", run).hasSize(unique.size()).hasSize(207);
        }
    }
}
