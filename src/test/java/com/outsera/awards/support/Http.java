package com.outsera.awards.support;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Chamadas HTTP reais à aplicação em execução, para os testes de múltiplas execuções (spec 003), em que
 * não há {@code RestTestClient} injetado. Não é um dublê: fala com o servidor real pela porta dele.
 */
public final class Http {

    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    private static final Pattern TOTAL_ELEMENTS = Pattern.compile("\"totalElements\":(\\d+)");

    private Http() {
    }

    /** Resposta crua: status, corpo em bytes e o {@code ETag}, quando houver. */
    public record Response(int status, byte[] body, Optional<String> etag, Map<String, List<String>> headers) {

        public String text() {
            return new String(body, StandardCharsets.UTF_8);
        }

        public Optional<String> header(String name) {
            return headers.entrySet().stream()
                    .filter(entry -> entry.getKey().equalsIgnoreCase(name))
                    .flatMap(entry -> entry.getValue().stream())
                    .findFirst();
        }
    }

    /** {@code GET http://localhost:<port><path>}, com os cabeçalhos dados em pares nome, valor. */
    public static Response get(int port, String path, String... headers) {
        return send(port, "GET", path, headers);
    }

    /** Requisição com o método dado, sem corpo. */
    public static Response send(int port, String method, String path, String... headers) {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .timeout(Duration.ofSeconds(30))
                .method(method, HttpRequest.BodyPublishers.noBody());
        for (int i = 0; i + 1 < headers.length; i += 2) {
            builder.header(headers[i], headers[i + 1]);
        }
        try {
            HttpResponse<byte[]> response = CLIENT.send(builder.build(), HttpResponse.BodyHandlers.ofByteArray());
            return new Response(response.statusCode(), response.body(),
                    response.headers().firstValue("ETag"), response.headers().map());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }

    /**
     * Corpo e ETag de cada caminho, na ordem dada, exigindo status 200. É o retrato que os testes de
     * equivalência comparam entre execuções (spec 003, FR-006, FR-008 e FR-018).
     */
    public static Map<String, Response> snapshot(int port, List<String> paths) {
        Map<String, Response> responses = new LinkedHashMap<>();
        for (String path : paths) {
            Response response = get(port, path);
            if (response.status() != 200) {
                throw new AssertionError("GET " + path + " respondeu " + response.status() + ": " + response.text());
            }
            responses.put(path, response);
        }
        return responses;
    }

    /** {@code totalElements} de {@code GET /api/movies}. */
    public static long totalMovies(int port, String... headers) {
        Response response = get(port, "/api/movies?size=1", headers);
        Matcher matcher = TOTAL_ELEMENTS.matcher(response.text());
        if (response.status() != 200 || !matcher.find()) {
            throw new AssertionError("listagem inesperada: " + response.status() + " " + response.text());
        }
        return Long.parseLong(matcher.group(1));
    }

    /** Repete {@code condition} até ela ser verdadeira ou o prazo acabar, e falha no fim do prazo. */
    public static void await(String description, Duration timeout, BooleanSupplier condition) {
        long deadline = System.nanoTime() + timeout.toNanos();
        while (true) {
            try {
                if (condition.getAsBoolean()) {
                    return;
                }
            } catch (RuntimeException ignored) {
                // A condição ainda não pode ser avaliada (por exemplo, a conexão foi recusada).
            }
            if (System.nanoTime() > deadline) {
                throw new AssertionError("prazo de " + timeout.toSeconds() + " s esgotado esperando: " + description);
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(e);
            }
        }
    }

    /** Espera a readiness responder 200 (spec 002, FR-009; spec 003, FR-013). */
    public static void awaitReady(int port, Duration timeout) {
        await("readiness 200", timeout, () -> get(port, "/actuator/health/readiness").status() == 200);
    }
}
