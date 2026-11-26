package com.outsera.awards.ingestion.kafka;

import com.outsera.awards.ingestion.LoadReport;
import com.outsera.awards.ingestion.MovieCsvReader;
import com.outsera.awards.ingestion.MovieFileReading;
import com.outsera.awards.ingestion.MovieRow;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.InterruptException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
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
 *
 * <p><strong>Envio dos filmes:</strong> assíncrono, sem esperar cada confirmação. O publicador junta as
 * confirmações de um bloco, espera todas e só então segue. O produtor é idempotente ({@code acks=all} e
 * {@code enable.idempotence=true}) e o tópico tem uma partição, então a ordem dos eventos se mantém. Com o
 * envio síncrono, um por vez, os 100 mil filmes do teste de carga levavam cerca de 10 minutos (spec 004,
 * T021; docs/ai/decisoes.md).
 *
 * <p><strong>Marcador:</strong> enviado por último, de forma síncrona, só depois de todos os filmes
 * confirmados. Uma tentativa em que algum envio falha não publica o marcador. Depois da espera, a próxima
 * tentativa recomeça do primeiro evento sem confirmação. Os eventos reenviados caem na gravação
 * idempotente por ano e título (spec 003, FR-014).
 *
 * <p>O {@code linger.ms} e o {@code batch.size} valem só para o produtor do publicador, uma cópia da
 * fábrica do Boot. O produtor do consumidor, que envia ao tópico de erros, não muda.
 *
 * <p>A cada nova tentativa, pede ao {@link KafkaAdmin} que crie os tópicos que ainda não existem. Com o
 * broker fora na subida, a criação dos tópicos falha (com {@code fail-fast} desligado), e o broker pode não
 * criá-los sozinho, como no compose, que desliga a criação automática. Sem isso, a publicação falharia para
 * sempre depois de o broker voltar (spec 003, caso de borda "Broker indisponível"; apontamento do Codex no
 * PR do Marco 3).
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

    /** Filmes enviados antes de esperar as confirmações: limita a memória com os arquivos grandes. */
    private static final int BLOCK = 10_000;

    /** Só no produtor do publicador: lotes maiores e uma espera curta para enchê-los. */
    private static final Map<String, Object> PUBLISHER_OVERRIDES = Map.of(
            ProducerConfig.ACKS_CONFIG, "all",
            ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true,
            ProducerConfig.LINGER_MS_CONFIG, 20,
            ProducerConfig.BATCH_SIZE_CONFIG, 256 * 1024);

    private final MovieFileReading reading;
    private final ProducerFactory<String, byte[]> producerFactory;
    private final KafkaTemplate<String, byte[]> kafka;
    private final KafkaIngestionProperties properties;
    private final IngestionReadiness readiness;
    private final KafkaAdmin admin;

    private volatile MovieCsvReader.Result result;
    private volatile Thread worker;

    public MovieEventPublisher(MovieFileReading reading, ProducerFactory<String, byte[]> producerFactory,
            KafkaIngestionProperties properties, IngestionReadiness readiness, KafkaAdmin admin) {
        this.reading = reading;
        this.producerFactory = producerFactory.copyWithConfigurationOverride(PUBLISHER_OVERRIDES);
        this.kafka = new KafkaTemplate<>(this.producerFactory);
        this.properties = properties;
        this.readiness = readiness;
        this.admin = admin;
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
    public void destroy() throws Exception {
        Thread thread = worker;
        if (thread != null) {
            thread.interrupt();
        }
        if (producerFactory instanceof DisposableBean disposable) {
            disposable.destroy();
        }
    }

    private void publish() {
        List<MovieRow> rows = result.rows();
        String runId = readiness.runId();
        try {
            sendMovies(rows, runId);
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

    /**
     * Envia os filmes sem esperar cada confirmação, em blocos de {@value #BLOCK}, e espera todas as
     * confirmações de cada bloco. Com uma falha, espera com tempo crescente e recomeça do primeiro evento
     * sem confirmação; o marcador só sai depois de todos confirmados.
     */
    private void sendMovies(List<MovieRow> rows, String runId) throws InterruptedException {
        int confirmed = 0;
        long backoff = 500;
        while (confirmed < rows.size()) {
            int end = Math.min(rows.size(), confirmed + BLOCK);
            int failedAt = sendBlock(rows, confirmed, end, runId);
            if (failedAt < 0) {
                confirmed = end;
                backoff = 500;
                continue;
            }
            confirmed = failedAt;
            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedException();
            }
            log.warn("Falha ao publicar no tópico {} a partir do evento {} de {}; nova tentativa em {} ms",
                    properties.topicMovies(), failedAt + 1, rows.size(), backoff);
            Thread.sleep(backoff);
            backoff = Math.min(backoff * 2, MAX_BACKOFF.toMillis());
            ensureTopics();
        }
    }

    /** Envia os eventos de {@code from} a {@code to} e devolve o índice do primeiro sem confirmação, ou -1. */
    private int sendBlock(List<MovieRow> rows, int from, int to, String runId) throws InterruptedException {
        List<CompletableFuture<?>> sent = new ArrayList<>(to - from);
        int failedAt = -1;
        String failure = null;
        for (int i = from; i < to; i++) {
            MovieRow row = rows.get(i);
            try {
                sent.add(kafka.send(record(row.year() + "|" + row.title(), movieJson(row), runId)));
            } catch (RuntimeException e) {
                // O envio pode falhar antes de devolver a confirmação (metadados do tópico fora do prazo).
                failedAt = i;
                failure = e.getMessage();
                break;
            }
        }
        // O flush só volta quando todos os registros enviados terminaram, com sucesso ou com erro (ou com o
        // delivery.timeout.ms), então as confirmações abaixo já estão prontas.
        try {
            kafka.flush();
        } catch (InterruptException e) {
            throw new InterruptedException();
        }
        for (int i = 0; i < sent.size(); i++) {
            try {
                sent.get(i).get(SEND_TIMEOUT.toSeconds(), TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                throw e;
            } catch (Exception e) {
                if (failedAt < 0 || from + i < failedAt) {
                    failedAt = from + i;
                    failure = e.getMessage();
                }
            }
        }
        if (failedAt >= 0) {
            log.warn("Envio ao tópico {} sem confirmação: {}", properties.topicMovies(), failure);
        }
        return failedAt;
    }

    /** Envia e espera a confirmação; com falha, tenta de novo com espera crescente até 30 s. */
    private void send(String key, byte[] value, String runId) throws InterruptedException {
        long backoff = 500;
        while (true) {
            try {
                kafka.send(record(key, value, runId)).get(SEND_TIMEOUT.toSeconds(), TimeUnit.SECONDS);
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
                ensureTopics();
            }
        }
    }

    private ProducerRecord<String, byte[]> record(String key, byte[] value, String runId) {
        ProducerRecord<String, byte[]> record = new ProducerRecord<>(properties.topicMovies(), key, value);
        record.headers().add(RUN_ID_HEADER, runId.getBytes(StandardCharsets.UTF_8));
        return record;
    }

    /**
     * Cria os tópicos que ainda não existem ({@code NewTopic} de {@link KafkaIngestionConfiguration}). Com
     * o broker ainda fora, o {@code KafkaAdmin} só registra a falha, e a próxima tentativa repete.
     */
    private void ensureTopics() {
        try {
            if (admin.initialize()) {
                log.info("Tópicos da ingestão conferidos no broker: {} e {}", properties.topicMovies(),
                        properties.topicErrors());
            }
        } catch (RuntimeException e) {
            log.warn("Falha ao criar os tópicos da ingestão; nova tentativa na próxima publicação: {}", e.getMessage());
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
