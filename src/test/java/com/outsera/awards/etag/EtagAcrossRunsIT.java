package com.outsera.awards.etag;

import static org.assertj.core.api.Assertions.assertThat;

import com.outsera.awards.support.ApplicationStarter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * O ETag é derivado do conteúdo: o mesmo arquivo dá o mesmo ETag em execuções separadas, e arquivos com
 * resultados diferentes dão ETags diferentes (FR-002, US1-7). Cada execução é a aplicação real, subida
 * com {@code SpringApplication} e chamada por HTTP.
 *
 * <p>Usa {@code SpringApplication}, e não {@code @SpringBootTest}, pela cláusula de controle do ciclo de
 * vida do Princípio I (constituição 1.5.0), caso "múltiplas execuções": o teste precisa de duas subidas
 * separadas da aplicação para comparar os ETags. Sem mocks, e o ETag é lido da resposta HTTP real.
 */
class EtagAcrossRunsIT {

    private static final String REFERENCE = "classpath:movielist.csv";
    private static final String DISJOINT = "classpath:scenarios/disjoint-producers.csv";

    private static String intervalsEtag(String csvPath) {
        return ApplicationStarter.runAndCall(csvPath, port -> {
            HttpRequest request = HttpRequest.newBuilder(
                    URI.create("http://localhost:" + port + "/api/producers/intervals")).GET().build();
            try (HttpClient client = HttpClient.newHttpClient()) {
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                assertThat(response.statusCode()).isEqualTo(200);
                return response.headers().firstValue("ETag").orElseThrow(
                        () -> new AssertionError("resposta sem ETag com " + csvPath));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(e);
            }
        });
    }

    @Test
    @DisplayName("FR-002 US1-7 o mesmo arquivo dá o mesmo ETag em execuções separadas")
    void sameFileSameEtag() {
        assertThat(intervalsEtag(REFERENCE)).isEqualTo(intervalsEtag(REFERENCE));
    }

    @Test
    @DisplayName("FR-002 US1-7 arquivos de resultados diferentes dão ETags diferentes")
    void differentResultsDifferentEtags() {
        assertThat(intervalsEtag(REFERENCE)).isNotEqualTo(intervalsEtag(DISJOINT));
    }
}
