package com.outsera.awards.ingestion.kafka;

import com.outsera.awards.ingestion.MovieImportService;
import com.outsera.awards.ingestion.kafka.MovieEventParser.Completed;
import com.outsera.awards.ingestion.kafka.MovieEventParser.Movie;
import com.outsera.awards.ingestion.kafka.MovieEventParser.Parsed;
import com.outsera.awards.ingestion.kafka.MovieEventParser.Rejected;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Consumidor do tópico de filmes (spec 003, FR-012 a FR-017; research R9).
 *
 * <ul>
 *   <li>Evento de filme válido: gravado pelo serviço único do modo padrão (Princípio V).</li>
 *   <li>Evento de carga concluída: entregue ao indicador da readiness.</li>
 *   <li>Evento inválido: não é gravado. Vai ao tópico de erros com os bytes originais e o motivo nos
 *       cabeçalhos, e o consumo segue.</li>
 *   <li>Falha passageira ao gravar (banco indisponível, por exemplo): a exceção sobe ao
 *       {@code DefaultErrorHandler}, que tenta de novo sem avançar o offset (FR-015a).</li>
 *   <li>Recusa permanente do banco por dado inválido (SQLState das classes 22 e 23): o evento vai ao
 *       tópico de erros com o motivo {@code DATABASE_REJECTED}, sem nova tentativa (FR-015a).</li>
 * </ul>
 *
 * <p>O log nunca traz o conteúdo do evento, só o motivo e a posição.
 */
@Component
@Profile("kafka")
public class MovieEventListener {

    static final String REASON_HEADER = "awards-error-reason";

    private static final Logger log = LoggerFactory.getLogger(MovieEventListener.class);
    private static final long ERROR_SEND_TIMEOUT_SECONDS = 10;

    private final MovieImportService importService;
    private final IngestionReadiness readiness;
    private final KafkaTemplate<String, byte[]> kafka;
    private final KafkaIngestionProperties properties;
    private final Counter recorded;
    private final Counter rejected;

    public MovieEventListener(MovieImportService importService, IngestionReadiness readiness,
            KafkaTemplate<String, byte[]> kafka, KafkaIngestionProperties properties, MeterRegistry registry) {
        this.importService = importService;
        this.readiness = readiness;
        this.kafka = kafka;
        this.properties = properties;
        this.recorded = counter(registry, "recorded");
        this.rejected = counter(registry, "rejected");
    }

    private static Counter counter(MeterRegistry registry, String outcome) {
        return Counter.builder("awards.ingestion.events")
                .description("Eventos de filme gravados e enviados ao tópico de erros")
                .tag("outcome", outcome)
                .register(registry);
    }

    /**
     * O grupo é o de {@code spring.kafka.consumer.group-id} ({@code KAFKA_CONSUMER_GROUP}), e não o id do
     * ouvinte ({@code idIsGroup = false}; spec 003, FR-003). O prefixo do {@code client.id} é fixo, para que
     * a série de atraso das métricas e o dashboard não dependam do grupo configurado.
     */
    @KafkaListener(id = "awards-movies", idIsGroup = false, clientIdPrefix = "awards-movies",
            topics = "${awards.ingestion.topic-movies}")
    public void onEvent(ConsumerRecord<String, byte[]> record) {
        Parsed parsed = MovieEventParser.parse(record.value());
        switch (parsed) {
            case Movie movie -> {
                try {
                    importService.record(movie.row());
                } catch (RuntimeException e) {
                    String sqlState = permanentSqlState(e);
                    if (sqlState == null) {
                        throw e; // passageira: o DefaultErrorHandler tenta de novo (FR-015a)
                    }
                    reject(record, new Rejected(EventRejectionReason.DATABASE_REJECTED, "SQLState " + sqlState));
                    return;
                }
                recorded.increment();
            }
            case Completed completed -> readiness.complete(completed.event());
            case Rejected rejection -> reject(record, rejection);
        }
    }

    private void reject(ConsumerRecord<String, byte[]> record, Rejected rejection) {
        String reason = rejection.describe();
        log.warn("Evento rejeitado (tópico {}, partição {}, offset {}): {}",
                record.topic(), record.partition(), record.offset(), reason);
        ProducerRecord<String, byte[]> error = new ProducerRecord<>(properties.topicErrors(), record.key(),
                record.value());
        header(error, REASON_HEADER, reason);
        header(error, "awards-error-at", Instant.now().toString());
        header(error, "awards-source-topic", record.topic());
        header(error, "awards-source-partition", String.valueOf(record.partition()));
        header(error, "awards-source-offset", String.valueOf(record.offset()));
        try {
            kafka.send(error).get(ERROR_SEND_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Falha ao enviar o evento rejeitado ao tópico de erros {} (tópico {}, partição {}, offset {}): "
                    + "interrompido", properties.topicErrors(), record.topic(), record.partition(), record.offset());
        } catch (Exception e) {
            log.warn("Falha ao enviar o evento rejeitado ao tópico de erros {} (tópico {}, partição {}, offset {}): {}",
                    properties.topicErrors(), record.topic(), record.partition(), record.offset(), e.toString());
        }
        rejected.increment();
    }

    /**
     * O SQLState de uma recusa permanente do banco, ou nulo se a falha é passageira. Permanentes são as
     * classes 22 (dado inválido) e 23 (restrição de integridade): a nova tentativa daria o mesmo erro e
     * travaria a partição. As demais, como a tabela inexistente (42P01) e a perda de conexão (08xxx),
     * continuam com a nova tentativa (spec 003, FR-015a; research R9; apontamento do Codex no PR do Marco 3).
     */
    static String permanentSqlState(Throwable failure) {
        for (Throwable cause = failure; cause != null; cause = cause.getCause() == cause ? null : cause.getCause()) {
            if (cause instanceof SQLException sql && sql.getSQLState() != null
                    && (sql.getSQLState().startsWith("22") || sql.getSQLState().startsWith("23"))) {
                return sql.getSQLState();
            }
        }
        return null;
    }

    private static void header(ProducerRecord<String, byte[]> record, String name, String value) {
        record.headers().add(name, value.getBytes(StandardCharsets.UTF_8));
    }
}
