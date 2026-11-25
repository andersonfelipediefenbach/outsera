package com.outsera.awards.ingestion.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.ExponentialBackOff;

/**
 * Tópicos e tratamento de erros da ingestão por Kafka (spec 003; research R6 e R9).
 *
 * <p>Os dois tópicos têm uma partição: o evento de carga concluída é consumido depois de todos os filmes
 * publicados antes dele. O {@link DefaultErrorHandler} trata só as exceções que escapam do ouvinte, ou
 * seja, as falhas de gravação: tenta de novo sem limite de tentativas, com espera crescente até 30 s, sem
 * avançar o offset, para não perder o filme (FR-015a). Os eventos inválidos não chegam a ele: o ouvinte
 * os envia ao tópico de erros.
 */
@Configuration(proxyBeanMethods = false)
@Profile("kafka")
public class KafkaIngestionConfiguration {

    private static final Logger log = LoggerFactory.getLogger(KafkaIngestionConfiguration.class);

    @Bean
    NewTopic moviesTopic(KafkaIngestionProperties properties) {
        return TopicBuilder.name(properties.topicMovies()).partitions(1).replicas(1).build();
    }

    @Bean
    NewTopic errorsTopic(KafkaIngestionProperties properties) {
        return TopicBuilder.name(properties.topicErrors()).partitions(1).replicas(1).build();
    }

    @Bean
    DefaultErrorHandler ingestionErrorHandler() {
        ExponentialBackOff backOff = new ExponentialBackOff(500, 2.0);
        backOff.setMaxInterval(30_000);
        DefaultErrorHandler handler = new DefaultErrorHandler(backOff);
        handler.setRetryListeners((record, ex, attempt) -> log.warn(
                "Falha ao gravar o evento (tópico {}, partição {}, offset {}), tentativa {}; nova tentativa: {}",
                record.topic(), record.partition(), record.offset(), attempt, rootMessage(ex)));
        return handler;
    }

    private static String rootMessage(Throwable ex) {
        Throwable root = ex;
        while (root.getCause() != null && root.getCause() != root) {
            root = root.getCause();
        }
        return root.getClass().getSimpleName();
    }
}
