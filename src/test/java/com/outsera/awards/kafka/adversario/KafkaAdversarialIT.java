package com.outsera.awards.kafka.adversario;

import static org.assertj.core.api.Assertions.assertThat;

import com.outsera.awards.support.HoldingProducerInterceptor;
import com.outsera.awards.support.IntervalItem;
import com.outsera.awards.support.KafkaScenarioIT;
import com.outsera.awards.support.KafkaTestSupport;
import com.outsera.awards.support.KafkaTestSupport.Record;
import com.outsera.awards.support.MovieJson;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterEach;
import org.springframework.boot.test.system.CapturedOutput;

/**
 * Base dos casos do testador adversário (Codex) na ingestão por Kafka (spec 004, FR-035; research R13;
 * T050 da spec 003). Os casos foram criados a partir da spec 003 e do contrato dos eventos, sem ler o
 * código de produção, e ficam registrados em {@code docs/ai/decisoes.md}, na entrada "Testador
 * adversário: ingestão por Kafka".
 *
 * <p>Os casos sobem a aplicação completa com o broker embutido e o H2, sem mocks, e publicam os eventos pelo
 * broker real. Os que não mexem no marcador, na readiness nem na subida dividem uma aplicação
 * ({@link KafkaAdversarialEventsIT}); os demais têm contexto próprio. A verificação é por HTTP (listagem, intervalos e readiness). O tópico de erros é lido pelo
 * broker, porque é a saída que o contrato define para os eventos rejeitados (spec 003, FR-015).
 *
 * <p>O fim de cada sequência é medido pelo offset que o consumidor da aplicação confirmou no grupo: o
 * ouvinte confirma cada registro depois de gravá-lo ou de enviá-lo ao tópico de erros ({@code ack-mode:
 * record}), então, com o offset confirmado no fim do tópico, todos os eventos já foram processados.
 */
abstract class KafkaAdversarialIT extends KafkaScenarioIT {

    /** CSV só com o cabeçalho: o publicador manda só o marcador, com {@code count} 0 (fixture do README). */
    static final String EMPTY_CSV = "movies.csv.path=classpath:scenarios/header-only.csv";

    /** CSV de duas linhas dos casos 22 e 25: Base (2000) e Controle (2002), vencedores de Iris. */
    static final String TWO_LINES_CSV = "movies.csv.path=classpath:scenarios/kafka/adversario-two-lines.csv";

    /** Retém o marcador do publicador no tópico {@code awards.movies.held} ({@link HoldingProducerInterceptor}). */
    static final String HOLD_COMPLETION = "spring.kafka.producer.properties." + HoldingProducerInterceptor.MODE
            + "=completion";

    /** Deixa passar só o primeiro registro do publicador e retém os demais. */
    static final String HOLD_AFTER_FIRST = "spring.kafka.producer.properties." + HoldingProducerInterceptor.MODE
            + "=after-first";

    static final String HOLD_TOPIC = "spring.kafka.producer.properties." + HoldingProducerInterceptor.TOPIC + "="
            + MOVIES_TOPIC;

    static final String HOLD_CLASS = HoldingProducerInterceptor.CLASS_PROPERTY;

    /** Grupo padrão do consumidor da aplicação ({@code KAFKA_CONSUMER_GROUP}). */
    private static final String GROUP = "awards";

    static final String BASE = "{\"year\":2000,\"title\":\"Base\",\"producers\":\"Iris\",\"winner\":true}";
    static final String CONTROL = "{\"year\":2002,\"title\":\"Controle\",\"producers\":\"Iris\",\"winner\":true}";

    /** Uma rejeição esperada: a chave, o código do motivo e os bytes originais. */
    record Rejected(String key, String reason, byte[] value) {
    }

    static Rejected rejected(String key, String reason, byte[] value) {
        return new Rejected(key, reason, value);
    }

    static Rejected rejected(String key, String reason, String json) {
        return new Rejected(key, reason, utf8(json));
    }

    static byte[] utf8(String text) {
        return text.getBytes(StandardCharsets.UTF_8);
    }

    /** Bytes dados em hexadecimal, dois dígitos por byte (notação {@code @HEX:...@} do README dos casos). */
    static byte[] hex(String digits) {
        return HexFormat.of().parseHex(digits);
    }

    /** Concatena trechos de bytes, na ordem. */
    static byte[] bytes(byte[]... parts) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (byte[] part : parts) {
            out.writeBytes(part);
        }
        return out.toByteArray();
    }

    static MovieJson movie(int year, String title, String studios, String producers, boolean winner) {
        return new MovieJson(year, title, studios, producers, winner);
    }

    /** Envia um texto JSON em UTF-8 ao tópico de filmes e espera a confirmação do broker. */
    void send(String key, String json) {
        sendBytes(key, utf8(json));
    }

    /** Envia os bytes como estão. Uma chave nula vai como chave ausente no Kafka ({@code @NULL@}). */
    void sendBytes(String key, byte[] value) {
        KafkaTestSupport.send(brokers(), MOVIES_TOPIC, key, value);
    }

    /** Cliente de administração reaproveitado pelo teste inteiro: criar um por espera custa a conexão. */
    private Admin admin;

    @AfterEach
    void closeAdmin() {
        if (admin != null) {
            admin.close();
            admin = null;
        }
    }

    /** Espera o consumidor da aplicação confirmar todos os registros que estão no tópico de filmes. */
    void awaitConsumed() {
        long end = KafkaTestSupport.count(brokers(), MOVIES_TOPIC);
        if (admin == null) {
            admin = Admin.create(Map.of(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, brokers()));
        }
        await("consumidor confirmando o offset " + end, 30, () -> committed(admin) >= end);
    }

    private static long committed(Admin admin) {
        try {
            Map<TopicPartition, OffsetAndMetadata> offsets = admin.listConsumerGroupOffsets(GROUP)
                    .partitionsToOffsetAndMetadata().get(5, TimeUnit.SECONDS);
            OffsetAndMetadata offset = offsets.get(new TopicPartition(MOVIES_TOPIC, 0));
            return offset == null ? -1 : offset.offset();
        } catch (Exception e) {
            return -1;
        }
    }

    /** Os registros retidos pelo {@link HoldingProducerInterceptor}, na ordem em que o publicador os enviou. */
    List<Record> held(int expected) {
        List<Record> records = KafkaTestSupport.read(brokers(), HoldingProducerInterceptor.heldTopic(MOVIES_TOPIC),
                expected, Duration.ofSeconds(30));
        assertThat(records).as("registros retidos do publicador").hasSize(expected);
        return records;
    }

    /** Solta um registro retido no tópico de filmes, com a chave, o valor e os cabeçalhos do publicador. */
    void release(Record record) {
        KafkaTestSupport.sendWithHeaders(brokers(), MOVIES_TOPIC, record.key(), record.value(), record.headers());
    }

    /** O {@code runId} desta execução, lido do cabeçalho {@code awards-run-id} de um registro do publicador. */
    static String runId(Record record) {
        String runId = record.headers().get("awards-run-id");
        assertThat(runId).as("cabeçalho awards-run-id do publicador").isNotBlank();
        return runId;
    }

    /**
     * O tópico de erros tem exatamente as rejeições dadas, cada uma com os bytes originais, o motivo, os
     * cabeçalhos de origem (com o offset do próprio evento no tópico de filmes) e o aviso no log (spec 003,
     * FR-015; README dos casos, passo 4).
     */
    void assertErrors(CapturedOutput output, Rejected... expected) {
        long count = KafkaTestSupport.count(brokers(), ERRORS_TOPIC);
        List<Record> errors = count == 0 ? List.of()
                : KafkaTestSupport.read(brokers(), ERRORS_TOPIC, (int) count, Duration.ofSeconds(10));
        List<Record> sources = KafkaTestSupport.read(brokers(), MOVIES_TOPIC,
                (int) KafkaTestSupport.count(brokers(), MOVIES_TOPIC), Duration.ofSeconds(10));
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(errors).as("registros no tópico de erros").hasSize(expected.length);
        for (Rejected rejection : expected) {
            List<Record> matching = errors.stream().filter(error -> Objects.equals(error.key(), rejection.key()))
                    .toList();
            softly.assertThat(matching).as("%s (%s) no tópico de erros", rejection.key(), rejection.reason()).hasSize(1);
            if (matching.size() != 1) {
                continue;
            }
            Record error = matching.getFirst();
            softly.assertThat(error.value()).as("%s: bytes originais", rejection.key()).isEqualTo(rejection.value());
            softly.assertThat(error.headers().get("awards-error-reason")).as("%s: motivo", rejection.key())
                    .startsWith(rejection.reason() + ":");
            softly.assertThat(error.headers()).as("%s: cabeçalhos", rejection.key()).containsKeys("awards-error-at",
                    "awards-source-topic", "awards-source-partition", "awards-source-offset");
            softly.assertThat(error.headers().get("awards-source-topic")).as("%s: tópico", rejection.key())
                    .isEqualTo(MOVIES_TOPIC);
            softly.assertThat(error.headers().get("awards-source-partition")).as("%s: partição", rejection.key())
                    .isEqualTo("0");
            String offset = error.headers().get("awards-source-offset");
            List<Long> sourceOffsets = sources.stream()
                    .filter(source -> Objects.equals(source.key(), rejection.key())
                            && Arrays.equals(source.value(), rejection.value()))
                    .map(Record::offset).toList();
            softly.assertThat(sourceOffsets).as("%s: offset do evento de origem", rejection.key())
                    .contains(offset == null ? -1L : Long.parseLong(offset));
            softly.assertThat(output.getAll()).as("%s: aviso no log", rejection.key()).contains(
                    "Evento rejeitado (tópico " + MOVIES_TOPIC + ", partição 0, offset " + offset + "): "
                            + rejection.reason() + ":");
        }
        softly.assertAll();
    }

    /** A listagem tem exatamente estes filmes, comparados sem depender da ordem da resposta. */
    void assertMovies(MovieJson... expected) {
        Comparator<MovieJson> order = Comparator.comparingInt(MovieJson::year).thenComparing(MovieJson::title);
        List<MovieJson> actual = allMovies().content().stream().sorted(order).toList();
        assertThat(actual).as("filmes gravados").containsExactlyElementsOf(Arrays.stream(expected).sorted(order).toList());
    }

    /** Os intervalos de controle dos casos: Iris de 2000 a 2002, nas duas listas. */
    void assertControlIntervals() {
        List<IntervalItem> iris = List.of(IntervalItem.item("Iris", 2, 2000, 2002));
        assertIntervals(iris, iris);
    }

    /** Os dois pares de Iris de tamanho 1 (2000–2001 e 2001–2002), nas duas listas. */
    void assertIrisWonIn2001() {
        List<IntervalItem> iris = List.of(IntervalItem.item("Iris", 1, 2000, 2001),
                IntervalItem.item("Iris", 1, 2001, 2002));
        assertIntervals(iris, iris);
    }

    void assertReady() {
        assertThat(readinessStatus()).as("readiness").isEqualTo(200);
    }
}
