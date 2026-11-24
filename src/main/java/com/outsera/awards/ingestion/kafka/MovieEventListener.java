package com.outsera.awards.ingestion.kafka;

import com.outsera.awards.ingestion.MovieImportService;
import com.outsera.awards.ingestion.kafka.MovieEventParser.Completed;
import com.outsera.awards.ingestion.kafka.MovieEventParser.Movie;
import com.outsera.awards.ingestion.kafka.MovieEventParser.Parsed;
import com.outsera.awards.ingestion.kafka.MovieEventParser.Rejected;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.nio.charset.StandardCharsets;
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
 *   <li>Falha ao gravar (banco indisponível, por exemplo): a exceção sobe ao {@code DefaultErrorHandler},
 *       que tenta de novo sem avançar o offset (FR-015a).</li>
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

    @KafkaListener(id = "awards-movies", topics = "${awards.ingestion.topic-movies}")
    public void onEvent(ConsumerRecord<String, byte[]> record) {
        Parsed parsed = MovieEventParser.parse(record.value());
        switch (parsed) {
            case Movie movie -> {
                importService.record(movie.row());
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

    private static void header(ProducerRecord<String, byte[]> record, String name, String value) {
        record.headers().add(name, value.getBytes(StandardCharsets.UTF_8));
    }
}
