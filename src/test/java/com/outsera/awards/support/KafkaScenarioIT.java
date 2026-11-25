package com.outsera.awards.support;

import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

/**
 * Base dos testes com a ingestão por Kafka num contexto {@code @SpringBootTest} (spec 003; research R15).
 *
 * <p>Cada contexto tem o seu broker embutido (KRaft, sem Docker; Princípio II). O banco é o H2 do modo
 * padrão: o perfil {@code kafka} troca só a origem dos dados (FR-002). O perfil {@code kafkatest}, que só
 * existe em {@code src/test/resources}, encurta os prazos dos clientes Kafka.
 *
 * <p><strong>Reaproveitamento do contexto:</strong> classes que só leem e têm as mesmas propriedades
 * compartilham o contexto (e o broker) pelo cache do Spring. Uma classe que muda os dados ou o estado da
 * readiness declara {@code @DirtiesContext} e uma propriedade própria ({@code awards.test.context}),
 * para ter um contexto só dela, fechado no fim, e nunca sujar o de outra.
 */
@ActiveProfiles({"kafka", "kafkatest"})
// O endereço vai para BROKERS_PROPERTY, e não para a spring.kafka.bootstrap-servers padrão, que vazaria
// para as aplicações que outros testes sobem na mesma JVM (ver KafkaTestSupport.BROKERS_PROPERTY).
@EmbeddedKafka(partitions = 1, bootstrapServersProperty = KafkaTestSupport.BROKERS_PROPERTY)
@TestPropertySource(properties = "KAFKA_BOOTSTRAP_SERVERS=${spring.embedded.kafka.brokers}")
public abstract class KafkaScenarioIT extends ScenarioIT {

    protected static final String MOVIES_TOPIC = "awards.movies";
    protected static final String ERRORS_TOPIC = "awards.movies.errors";
    private static final Pattern TOTAL = Pattern.compile("\"totalElements\":(\\d+)");

    @Autowired
    private EmbeddedKafkaBroker broker;

    protected String brokers() {
        return broker.getBrokersAsString();
    }

    /** Status da readiness (spec 002, FR-008). */
    protected int readinessStatus() {
        return http.get().uri("/actuator/health/readiness").exchange().returnResult().getStatus().value();
    }

    /** Espera a carga por Kafka terminar: a readiness responde 200 (FR-013). */
    protected void awaitReady() {
        Http.await("readiness 200", Duration.ofSeconds(60), () -> readinessStatus() == 200);
    }

    /** {@code totalElements} da listagem com a {@code query} dada (sem o {@code ?}). */
    protected long total(String query) {
        String body = http.get().uri(MOVIES + "?size=1" + (query.isEmpty() ? "" : "&" + query)).exchange()
                .expectStatus().isOk()
                .expectBody(String.class).returnResult().getResponseBody();
        Matcher matcher = TOTAL.matcher(body);
        if (!matcher.find()) {
            throw new AssertionError("listagem sem totalElements: " + body);
        }
        return Long.parseLong(matcher.group(1));
    }

    /** Envia um evento de filme JSON ao tópico de filmes. */
    protected void sendMovie(String key, String json) {
        KafkaTestSupport.sendJson(brokers(), MOVIES_TOPIC, key, json);
    }

    /** Espera {@code condition} por até {@code seconds}. */
    protected static void await(String description, int seconds, java.util.function.BooleanSupplier condition) {
        Http.await(description, Duration.ofSeconds(seconds), condition);
    }
}
