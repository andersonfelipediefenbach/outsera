package com.outsera.awards.support;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.kafka.clients.producer.ProducerInterceptor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

/**
 * Transporte de teste que retém registros do publicador da aplicação antes de chegarem ao tópico de
 * filmes (spec 004, FR-035; casos do testador adversário que pedem "retenha o marcador").
 *
 * <p>É um {@link ProducerInterceptor} do próprio Kafka, ligado só nos testes por
 * {@code spring.kafka.producer.properties.interceptor.classes}. Não substitui nenhuma classe da aplicação:
 * o publicador envia como sempre, e o registro retido vai, com chave, valor e cabeçalhos intactos, ao
 * tópico {@code <tópico de filmes>.held} do mesmo broker. O teste o lê de lá e o solta no tópico de filmes
 * quando o caso pede, pelo broker real.
 *
 * <p>Configuração, pelas propriedades do produtor:
 * <ul>
 *   <li>{@value #TOPIC}: o tópico de filmes. Registros de outros tópicos, como o de erros, passam direto;</li>
 *   <li>{@value #MODE}: {@code completion} retém só o evento de carga concluída (chave
 *       {@code load-completed}); {@code after-first} deixa passar o primeiro registro e retém os demais.</li>
 * </ul>
 */
public class HoldingProducerInterceptor implements ProducerInterceptor<String, byte[]> {

    public static final String MODE = "awards.test.hold.mode";
    public static final String TOPIC = "awards.test.hold.topic";
    public static final String CLASS_PROPERTY =
            "spring.kafka.producer.properties.interceptor.classes=" + "com.outsera.awards.support.HoldingProducerInterceptor";

    private static final String COMPLETION_KEY = "load-completed";

    private final AtomicInteger seen = new AtomicInteger();
    private String mode;
    private String topic;

    /** O tópico em que os registros retidos de {@code topic} ficam. */
    public static String heldTopic(String topic) {
        return topic + ".held";
    }

    /** Propriedades de teste que ligam a retenção no modo dado para o tópico dado. */
    public static String[] properties(String mode, String topic) {
        return new String[] {CLASS_PROPERTY, "spring.kafka.producer.properties." + MODE + "=" + mode,
                "spring.kafka.producer.properties." + TOPIC + "=" + topic};
    }

    @Override
    public void configure(Map<String, ?> configs) {
        mode = String.valueOf(configs.get(MODE));
        topic = String.valueOf(configs.get(TOPIC));
    }

    @Override
    public ProducerRecord<String, byte[]> onSend(ProducerRecord<String, byte[]> record) {
        if (!record.topic().equals(topic) || !hold(record)) {
            return record;
        }
        return new ProducerRecord<>(heldTopic(topic), null, record.timestamp(), record.key(), record.value(),
                record.headers());
    }

    private boolean hold(ProducerRecord<String, byte[]> record) {
        return switch (mode) {
            case "completion" -> COMPLETION_KEY.equals(record.key());
            case "after-first" -> seen.getAndIncrement() > 0;
            default -> false;
        };
    }

    @Override
    public void onAcknowledgement(RecordMetadata metadata, Exception exception) {
    }

    @Override
    public void close() {
    }
}
