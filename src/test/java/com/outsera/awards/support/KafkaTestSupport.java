package com.outsera.awards.support;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.EmbeddedKafkaKraftBroker;

/**
 * Produtor e consumidor Kafka <strong>reais</strong>, ligados ao broker embutido dos testes (spec 003,
 * research R15). Não é um dublê: os eventos passam pelo broker de verdade, e a aplicação os consome como
 * consumiria os de qualquer outro cliente.
 *
 * <p>Cada classe de teste usa tópicos e grupo próprios, para que nenhum teste veja os eventos de outro.
 */
public final class KafkaTestSupport {

    private KafkaTestSupport() {
    }

    /** Tópicos e grupo de uma classe de teste. */
    public record Topics(String movies, String errors, String group) {
    }

    /** Um registro lido de um tópico. */
    public record Record(String key, byte[] value, Map<String, String> headers, long offset) {

        public String text() {
            return new String(value, StandardCharsets.UTF_8);
        }
    }

    /**
     * Sobe um broker embutido (KRaft, um nó) para os testes que rodam a aplicação com
     * {@code SpringApplication}, fora de um contexto de teste do Spring. Quem chama o fecha com
     * {@link EmbeddedKafkaBroker#destroy()} no fim da classe.
     */
    public static EmbeddedKafkaBroker startBroker() {
        EmbeddedKafkaBroker broker = new EmbeddedKafkaKraftBroker(1, 1);
        broker.afterPropertiesSet();
        return broker;
    }

    private static volatile EmbeddedKafkaBroker shared;

    /**
     * Broker embutido compartilhado pelos testes que rodam a aplicação com {@code SpringApplication} na
     * mesma JVM. Ele sobe uma vez e é fechado no fim da JVM. Cada classe continua com tópicos e grupo
     * próprios ({@link #topics(Class)}), então nenhuma vê os eventos de outra.
     */
    public static EmbeddedKafkaBroker sharedBroker() {
        if (shared == null) {
            synchronized (KafkaTestSupport.class) {
                if (shared == null) {
                    EmbeddedKafkaBroker broker = startBroker();
                    Runtime.getRuntime().addShutdownHook(new Thread(broker::destroy, "embedded-kafka-shutdown"));
                    shared = broker;
                }
            }
        }
        return shared;
    }

    public static Topics topics(Class<?> testClass) {
        String name = testClass.getSimpleName().toLowerCase(Locale.ROOT);
        return new Topics("awards.movies." + name, "awards.movies.errors." + name, "awards-" + name);
    }

    /** Propriedades que ligam a aplicação ao broker e aos tópicos da classe (sem o perfil). */
    public static Map<String, String> properties(String brokers, Topics topics) {
        Map<String, String> properties = new LinkedHashMap<>();
        properties.put("KAFKA_BOOTSTRAP_SERVERS", brokers);
        properties.put("KAFKA_TOPIC_MOVIES", topics.movies());
        properties.put("KAFKA_TOPIC_ERRORS", topics.errors());
        properties.put("KAFKA_CONSUMER_GROUP", topics.group());
        return properties;
    }

    /**
     * Argumentos de linha de comando com os perfis {@code kafka} e {@code kafkatest} (prazos curtos dos
     * clientes, só nos testes) e as {@link #properties}.
     */
    public static List<String> args(String brokers, Topics topics) {
        List<String> args = new ArrayList<>();
        args.add("--spring.profiles.active=kafka,kafkatest");
        properties(brokers, topics).forEach((name, value) -> args.add("--" + name + "=" + value));
        return args;
    }

    /** Envia {@code value} como está e espera a confirmação do broker. */
    public static void send(String brokers, String topic, String key, byte[] value) {
        sendWithHeaders(brokers, topic, key, value, Map.of());
    }

    /** Envia um texto JSON em UTF-8. */
    public static void sendJson(String brokers, String topic, String key, String json) {
        send(brokers, topic, key, json.getBytes(StandardCharsets.UTF_8));
    }

    /** Produtores reutilizados por endereço de broker: criar um por envio custa a busca de metadados. */
    private static final Map<String, KafkaProducer<String, byte[]>> PRODUCERS = new ConcurrentHashMap<>();

    /**
     * Envia todos os registros sem esperar cada confirmação, e espera todas no fim (`flush`). A ordem é
     * preservada, porque o tópico tem uma partição e o produtor é idempotente.
     */
    public static void sendAll(String brokers, String topic, List<Record> records) {
        KafkaProducer<String, byte[]> producer = PRODUCERS.computeIfAbsent(brokers, KafkaTestSupport::producer);
        List<java.util.concurrent.Future<?>> sent = new ArrayList<>();
        for (Record record : records) {
            sent.add(producer.send(new ProducerRecord<>(topic, record.key(), record.value())));
        }
        producer.flush();
        try {
            for (java.util.concurrent.Future<?> future : sent) {
                future.get(30, TimeUnit.SECONDS);
            }
        } catch (ExecutionException | TimeoutException e) {
            throw new IllegalStateException("falha ao enviar para " + topic, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }

    /**
     * Quantidade de registros do tópico, pelos offsets das partições, sem esperar nenhum prazo. Um tópico
     * que não existe tem zero registros.
     */
    public static long count(String brokers, String topic) {
        Properties config = new Properties();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers);
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class);
        config.put(ConsumerConfig.ALLOW_AUTO_CREATE_TOPICS_CONFIG, "false");
        try (KafkaConsumer<String, byte[]> consumer = new KafkaConsumer<>(config)) {
            List<PartitionInfo> info = consumer.listTopics(Duration.ofSeconds(10)).get(topic);
            if (info == null) {
                return 0;
            }
            List<TopicPartition> partitions = info.stream()
                    .map(partition -> new TopicPartition(topic, partition.partition())).toList();
            Map<TopicPartition, Long> end = consumer.endOffsets(partitions, Duration.ofSeconds(10));
            Map<TopicPartition, Long> begin = consumer.beginningOffsets(partitions, Duration.ofSeconds(10));
            return partitions.stream().mapToLong(partition -> end.get(partition) - begin.get(partition)).sum();
        }
    }

    /** Envia com cabeçalhos e espera a confirmação. */
    public static void sendWithHeaders(String brokers, String topic, String key, byte[] value,
            Map<String, String> headers) {
        KafkaProducer<String, byte[]> producer = PRODUCERS.computeIfAbsent(brokers, KafkaTestSupport::producer);
        ProducerRecord<String, byte[]> record = new ProducerRecord<>(topic, key, value);
        headers.forEach((name, headerValue) ->
                record.headers().add(name, headerValue.getBytes(StandardCharsets.UTF_8)));
        try {
            producer.send(record).get(30, TimeUnit.SECONDS);
        } catch (ExecutionException | TimeoutException e) {
            throw new IllegalStateException("falha ao enviar para " + topic, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }

    private static KafkaProducer<String, byte[]> producer(String brokers) {
        Properties config = new Properties();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);
        config.put(ProducerConfig.ACKS_CONFIG, "all");
        return new KafkaProducer<>(config);
    }

    /**
     * Lê o tópico desde o começo até ter {@code expected} registros ou o prazo acabar, e devolve o que
     * leu. Com {@code expected} zero, lê durante todo o prazo (para conferir que o tópico ficou vazio).
     */
    public static List<Record> read(String brokers, String topic, int expected, Duration timeout) {
        Properties config = new Properties();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers);
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class);
        List<Record> records = new ArrayList<>();
        long deadline = System.nanoTime() + timeout.toNanos();
        // Sem grupo de consumidores: as partições são atribuídas direto, sem esperar o rebalanceamento.
        try (KafkaConsumer<String, byte[]> consumer = new KafkaConsumer<>(config)) {
            List<TopicPartition> partitions = new ArrayList<>();
            while (partitions.isEmpty() && System.nanoTime() < deadline) {
                List<PartitionInfo> info = consumer.partitionsFor(topic, Duration.ofSeconds(1));
                if (info != null) {
                    info.forEach(partition -> partitions.add(new TopicPartition(topic, partition.partition())));
                }
            }
            if (partitions.isEmpty()) {
                return records;
            }
            consumer.assign(partitions);
            consumer.seekToBeginning(partitions);
            while (System.nanoTime() < deadline && (expected == 0 || records.size() < expected)) {
                for (ConsumerRecord<String, byte[]> record : consumer.poll(Duration.ofMillis(200))) {
                    Map<String, String> headers = new LinkedHashMap<>();
                    for (Header header : record.headers()) {
                        headers.put(header.key(), new String(header.value(), StandardCharsets.UTF_8));
                    }
                    records.add(new Record(record.key(), record.value(), headers, record.offset()));
                }
            }
        }
        return records;
    }
}
