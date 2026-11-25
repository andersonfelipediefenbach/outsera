package com.outsera.awards.kafka.adversario;

import static org.assertj.core.api.Assertions.assertThat;

import com.outsera.awards.support.ApplicationStarter;
import com.outsera.awards.support.Http;
import com.outsera.awards.support.HoldingProducerInterceptor;
import com.outsera.awards.support.IntervalItem;
import com.outsera.awards.support.KafkaTestSupport;
import com.outsera.awards.support.KafkaTestSupport.Record;
import com.outsera.awards.support.KafkaTestSupport.Topics;
import com.outsera.awards.support.MovieJson;
import com.outsera.awards.support.MoviesBody;
import com.outsera.awards.support.PostgresSupport;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import tools.jackson.databind.json.JsonMapper;

/**
 * Caso 29 do testador adversário: o reinício reaplica o filme do CSV e mantém o filme novo recebido de um
 * cliente (spec 003, FR-011, FR-013, FR-014, FR-016, SC-004 e US2-4 a US2-6). Duas execuções sobre o mesmo
 * PostgreSQL e o mesmo broker, pela cláusula "múltiplas execuções" do Princípio I. Na segunda, uma cópia do
 * marcador da primeira chega antes da carga, e o marcador atual fica retido
 * ({@link HoldingProducerInterceptor}) até a readiness ser conferida.
 */
class KafkaCase29RestartReappliesCsvPostgresIT {

    private static final String CSV = "classpath:scenarios/kafka/adversario-two-lines.csv";
    private static final String SCHEMA = PostgresSupport.newSchema(KafkaCase29RestartReappliesCsvPostgresIT.class);
    private static final Topics TOPICS = KafkaTestSupport.topics(KafkaCase29RestartReappliesCsvPostgresIT.class);
    private static final JsonMapper JSON = JsonMapper.builder().build();

    private static EmbeddedKafkaBroker broker;

    @BeforeAll
    static void startBroker() {
        broker = KafkaTestSupport.startBroker();
    }

    @AfterAll
    static void cleanUp() {
        broker.destroy();
        PostgresSupport.dropSchema(SCHEMA);
    }

    private static List<String> args(boolean holdCompletion) {
        List<String> args = new ArrayList<>(PostgresSupport.args(SCHEMA, "postgres,kafka,kafkatest"));
        KafkaTestSupport.properties(broker.getBrokersAsString(), TOPICS)
                .forEach((name, value) -> args.add("--" + name + "=" + value));
        if (holdCompletion) {
            for (String property : HoldingProducerInterceptor.properties("completion", TOPICS.movies())) {
                args.add("--" + property);
            }
        }
        return args;
    }

    private static List<MovieJson> movies(int port) {
        MoviesBody body = JSON.readValue(Http.get(port, "/api/movies?size=100").text(), MoviesBody.class);
        return body.content().stream()
                .sorted(Comparator.comparingInt(MovieJson::year).thenComparing(MovieJson::title)).toList();
    }

    private static String intervals(int port) {
        return Http.get(port, "/api/producers/intervals").text();
    }

    private static void send(String key, String json) {
        KafkaTestSupport.sendJson(broker.getBrokersAsString(), TOPICS.movies(), key, json);
    }

    /** Espera o consumidor da aplicação confirmar todos os registros do tópico de filmes. */
    private static void awaitConsumed() {
        String brokers = broker.getBrokersAsString();
        long end = KafkaTestSupport.count(brokers, TOPICS.movies());
        try (Admin admin = Admin.create(Map.of(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, brokers))) {
            Http.await("consumidor confirmando o offset " + end, Duration.ofSeconds(30), () -> {
                try {
                    OffsetAndMetadata offset = admin.listConsumerGroupOffsets(TOPICS.group())
                            .partitionsToOffsetAndMetadata().get(5, TimeUnit.SECONDS)
                            .get(new TopicPartition(TOPICS.movies(), 0));
                    return offset != null && offset.offset() >= end;
                } catch (Exception e) {
                    return false;
                }
            });
        }
    }

    private static MovieJson movie(int year, String title, String studios, String producers, boolean winner) {
        return new MovieJson(year, title, studios, producers, winner);
    }

    @Test
    @DisplayName("S004 FR-035 S003 FR-011 FR-013 FR-014 FR-016 SC-004 US2-4 a US2-6 caso 29: o reinício reaplica o CSV, mantém o filme externo e ignora o marcador antigo")
    void restartReappliesTheFileAndKeepsExternalMovies() {
        String brokers = broker.getBrokersAsString();
        Record oldMarker = ApplicationStarter.runAndCall(CSV, args(false), port -> {
            Http.awaitReady(port, Duration.ofSeconds(60));
            assertThat(intervals(port)).isEqualTo(IntervalItem.json(List.of(IntervalItem.item("Iris", 2, 2000, 2002)),
                    List.of(IntervalItem.item("Iris", 2, 2000, 2002))));
            send("2000|Base", "{\"year\":2000,\"title\":\"Base\",\"producers\":\"Nilo\",\"winner\":false,\"studios\":\"B\"}");
            awaitConsumed();
            assertThat(intervals(port)).as("primeira execução, depois da atualização").isEqualTo("{\"min\":[],\"max\":[]}");
            send("2001|Externo", "{\"year\":2001,\"title\":\"Externo\",\"producers\":\"Iris\",\"winner\":true}");
            awaitConsumed();
            assertThat(movies(port)).containsExactly(movie(2000, "Base", "B", "Nilo", false),
                    movie(2001, "Externo", null, "Iris", true), movie(2002, "Controle", null, "Iris", true));
            List<IntervalItem> one = List.of(IntervalItem.item("Iris", 1, 2001, 2002));
            assertThat(intervals(port)).isEqualTo(IntervalItem.json(one, one));
            return KafkaTestSupport.read(brokers, TOPICS.movies(), 5, Duration.ofSeconds(10)).stream()
                    .filter(record -> "load-completed".equals(record.key())).findFirst().orElseThrow();
        });

        // A cópia do marcador da primeira execução chega antes da carga da segunda.
        KafkaTestSupport.sendWithHeaders(brokers, TOPICS.movies(), oldMarker.key(), oldMarker.value(), oldMarker.headers());
        ApplicationStarter.runAndCall(CSV, args(true), port -> {
            List<Record> held = KafkaTestSupport.read(brokers, HoldingProducerInterceptor.heldTopic(TOPICS.movies()), 1,
                    Duration.ofSeconds(60));
            assertThat(held).as("marcador da segunda execução retido").hasSize(1);
            Http.await("carga da segunda execução gravada", Duration.ofSeconds(30),
                    () -> movies(port).contains(movie(2000, "Base", null, "Iris", true)));
            awaitConsumed();
            assertThat(Http.get(port, "/actuator/health/readiness").status())
                    .as("readiness com o marcador antigo e sem o atual").isEqualTo(503);

            KafkaTestSupport.sendWithHeaders(brokers, TOPICS.movies(), held.getFirst().key(), held.getFirst().value(),
                    held.getFirst().headers());
            Http.awaitReady(port, Duration.ofSeconds(10));
            assertThat(movies(port)).as("Base volta ao CSV e o Externo continua").containsExactly(
                    movie(2000, "Base", null, "Iris", true), movie(2001, "Externo", null, "Iris", true),
                    movie(2002, "Controle", null, "Iris", true));
            List<IntervalItem> pairs = List.of(IntervalItem.item("Iris", 1, 2000, 2001),
                    IntervalItem.item("Iris", 1, 2001, 2002));
            assertThat(intervals(port)).isEqualTo(IntervalItem.json(pairs, pairs));
            return null;
        });
        assertThat(KafkaTestSupport.count(brokers, TOPICS.errors())).as("registros no tópico de erros").isZero();
    }
}
