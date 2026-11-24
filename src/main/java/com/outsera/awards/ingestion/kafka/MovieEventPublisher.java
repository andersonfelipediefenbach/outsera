package com.outsera.awards.ingestion.kafka;

import com.outsera.awards.ingestion.LoadReport;
import com.outsera.awards.ingestion.MovieCsvReader;
import com.outsera.awards.ingestion.MovieFileReading;
import com.outsera.awards.ingestion.MovieRow;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 * Publicador do arquivo de filmes no perfil {@code kafka} (spec 003, FR-009 a FR-011; research R8).
 *
 * <p>Lê o arquivo na subida, no mesmo ponto da carga direta: os erros estruturais interrompem a subida
 * antes do servidor, e os avisos de linha rejeitada são os do Marco 1. Depois do
 * {@link ApplicationReadyEvent}, publica num fio próprio um evento por linha aceita e o evento de carga
 * concluída. Com o broker fora do ar, tenta de novo com espera crescente, sem derrubar a aplicação: ela
 * fica viva e não pronta.
 */
@Component
@Profile("kafka")
public class MovieEventPublisher implements SmartInitializingSingleton, ApplicationListener<ApplicationReadyEvent>,
        DisposableBean {

    static final String RUN_ID_HEADER = "awards-run-id";
    static final String COMPLETION_KEY = "load-completed";

    private static final Logger log = LoggerFactory.getLogger(MovieEventPublisher.class);
    private static final JsonMapper JSON = JsonMapper.builder().build();
    private static final Duration SEND_TIMEOUT = Duration.ofSeconds(30);
    private static final Duration MAX_BACKOFF = Duration.ofSeconds(30);

    private final MovieFileReading reading;
    private final KafkaTemplate<String, byte[]> kafka;
    private final KafkaIngestionProperties properties;
    private final IngestionReadiness readiness;

    private volatile MovieCsvReader.Result result;
    private volatile Thread worker;

    public MovieEventPublisher(MovieFileReading reading, KafkaTemplate<String, byte[]> kafka,
            KafkaIngestionProperties properties, IngestionReadiness readiness) {
        this.reading = reading;
        this.kafka = kafka;
        this.properties = properties;
        this.readiness = readiness;
    }

    @Override
    public void afterSingletonsInstantiated() {
        result = reading.read();
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        Thread thread = new Thread(this::publish, "awards-publisher");
        thread.setDaemon(true);
        worker = thread;
        thread.start();
    }

    @Override
    public void destroy() {
        Thread thread = worker;
        if (thread != null) {
            thread.interrupt();
        }
    }

    private void publish() {
        List<MovieRow> rows = result.rows();
        String runId = readiness.runId();
        try {
            for (MovieRow row : rows) {
                send(row.year() + "|" + row.title(), movieJson(row), runId);
            }
            if (properties.publishCompletion()) {
                ObjectNode completion = JSON.createObjectNode()
                        .put("type", "load-completed")
                        .put("runId", runId)
                        .put("count", rows.size());
                send(COMPLETION_KEY, JSON.writeValueAsBytes(completion), runId);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.info("Publicação do arquivo {} interrompida pelo encerramento da aplicação", reading.path());
            return;
        }
        LoadReport report = result.report();
        log.info("Publicação do arquivo {} concluída: {} linhas lidas, {} publicadas, {} rejeitadas",
                reading.path(), report.linesRead(), report.accepted(), report.rejected());
    }

    /** Envia e espera a confirmação; com falha, tenta de novo com espera crescente até 30 s. */
    private void send(String key, byte[] value, String runId) throws InterruptedException {
        long backoff = 500;
        while (true) {
            ProducerRecord<String, byte[]> record = new ProducerRecord<>(properties.topicMovies(), key, value);
            record.headers().add(RUN_ID_HEADER, runId.getBytes(StandardCharsets.UTF_8));
            try {
                kafka.send(record).get(SEND_TIMEOUT.toSeconds(), TimeUnit.SECONDS);
                return;
            } catch (InterruptedException e) {
                throw e;
            } catch (Exception e) {
                if (Thread.currentThread().isInterrupted()) {
                    throw new InterruptedException();
                }
                log.warn("Falha ao publicar no tópico {}; nova tentativa em {} ms: {}",
                        properties.topicMovies(), backoff, e.getMessage());
                Thread.sleep(backoff);
                backoff = Math.min(backoff * 2, MAX_BACKOFF.toMillis());
            }
        }
    }

    /** {@code {year, title, studios, producers, winner}}, com {@code winner} booleano (FR-010). */
    private static byte[] movieJson(MovieRow row) {
        ObjectNode node = JSON.createObjectNode()
                .put("year", row.year())
                .put("title", row.title())
                .put("studios", row.studios())
                .put("producers", row.producersRaw())
                .put("winner", row.winner());
        return JSON.writeValueAsBytes(node);
    }
}
