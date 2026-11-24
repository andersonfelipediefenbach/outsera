package com.outsera.awards.metrics;

import static org.assertj.core.api.Assertions.assertThat;

import com.outsera.awards.support.PrometheusText;
import com.outsera.awards.support.PrometheusText.Sample;
import com.outsera.awards.support.ScenarioIT;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.EntityExchangeResult;

/**
 * Métricas de requisição no formato do Prometheus (spec 002, FR-012 a FR-016; história 3;
 * contracts/operational-endpoints.md). As contagens são comparadas por diferença, antes e depois das
 * chamadas, para não depender da ordem dos testes nem de outras classes que usam o mesmo contexto.
 */
class MetricsIT extends ScenarioIT {

    private static final String PROMETHEUS = "/actuator/prometheus";
    private static final String COUNT = "http_server_requests_seconds_count";
    private static final String BUCKET = "http_server_requests_seconds_bucket";

    private String scrapeText() {
        EntityExchangeResult<byte[]> result = http.get().uri(PROMETHEUS).exchange()
                .expectStatus().isOk()
                .expectBody(byte[].class)
                .returnResult();
        return new String(result.getResponseBody(), StandardCharsets.UTF_8);
    }

    private List<Sample> scrape() {
        return PrometheusText.parse(scrapeText());
    }

    private static Predicate<Sample> labels(String... keyValues) {
        return sample -> {
            for (int i = 0; i < keyValues.length; i += 2) {
                if (!keyValues[i + 1].equals(sample.label(keyValues[i]))) {
                    return false;
                }
            }
            return true;
        };
    }

    private void call(String uri) {
        http.get().uri(uri).exchange().expectBody(byte[].class).returnResult();
    }

    private static Set<String> uris(List<Sample> samples) {
        return PrometheusText.find(samples, COUNT, s -> true).stream()
                .map(s -> s.label("uri")).collect(Collectors.toSet());
    }

    @Test
    @DisplayName("FR-012 US3-1 endpoint de métricas em texto do Prometheus")
    void prometheusFormat() {
        http.get().uri(INTERVALS).exchange().expectStatus().isOk();
        EntityExchangeResult<byte[]> result = http.get().uri(PROMETHEUS).exchange()
                .expectStatus().isOk()
                .expectBody(byte[].class)
                .returnResult();
        assertThat(result.getResponseHeaders().getContentType()).isNotNull()
                .satisfies(type -> assertThat(type.isCompatibleWith(MediaType.TEXT_PLAIN)
                        || type.getSubtype().contains("openmetrics")).isTrue());
        String text = new String(result.getResponseBody(), StandardCharsets.UTF_8);
        assertThat(PrometheusText.hasType(text, "http_server_requests_seconds", "histogram")).isTrue();
    }

    @Test
    @DisplayName("FR-013 US3-2 N chamadas aos intervalos aumentam a contagem em N")
    void countIncreasesByN() {
        Predicate<Sample> intervalsOk = labels("uri", INTERVALS, "method", "GET", "status", "200",
                "outcome", "SUCCESS");
        double before = PrometheusText.sum(scrape(), COUNT, intervalsOk);
        for (int i = 0; i < 5; i++) {
            http.get().uri(INTERVALS).exchange().expectStatus().isOk();
        }
        assertThat(PrometheusText.sum(scrape(), COUNT, intervalsOk) - before).isEqualTo(5.0);
    }

    @Test
    @DisplayName("FR-013 US3-3 400 e 405 aparecem na série do endpoint, como erro do cliente")
    void clientErrorsAreRecorded() {
        Predicate<Sample> bad = labels("uri", MOVIES, "method", "GET", "status", "400", "outcome", "CLIENT_ERROR");
        Predicate<Sample> notAllowed = labels("method", "POST", "status", "405", "outcome", "CLIENT_ERROR");
        List<Sample> before = scrape();
        double badBefore = PrometheusText.sum(before, COUNT, bad);
        double notAllowedBefore = PrometheusText.sum(before, COUNT, notAllowed);

        http.get().uri(MOVIES + "?winner=talvez").exchange().expectStatus().isBadRequest();
        http.post().uri(MOVIES).exchange().expectStatus().isEqualTo(405);

        List<Sample> after = scrape();
        assertThat(PrometheusText.sum(after, COUNT, bad) - badBefore).isEqualTo(1.0);
        assertThat(PrometheusText.sum(after, COUNT, notAllowed) - notAllowedBefore).isEqualTo(1.0);
    }

    @Test
    @DisplayName("FR-013 US3-4 consultas com parâmetros diferentes caem na série do caminho")
    void queryStringDoesNotCreateSeries() {
        Predicate<Sample> movies = labels("uri", MOVIES, "method", "GET", "status", "200");
        double before = PrometheusText.sum(scrape(), COUNT, movies);
        call(MOVIES + "?year=1990");
        call(MOVIES + "?page=3");
        List<Sample> after = scrape();
        assertThat(PrometheusText.sum(after, COUNT, movies) - before).isEqualTo(2.0);
        assertThat(uris(after)).noneMatch(uri -> uri.contains("?") || uri.contains("year=") || uri.contains("page="));
    }

    @Test
    @DisplayName("FR-015 US3-5 caminhos inexistentes caem num único valor de rota, com status 404")
    void unknownPathsShareOneRoute() {
        List<Sample> before = scrape();
        Map<String, Double> notFoundBefore = notFoundByUri(before);

        call("/nao-existe-1");
        call("/nao-existe-2");
        call("/x/y/z");

        List<Sample> after = scrape();
        Map<String, Double> notFoundAfter = notFoundByUri(after);
        Map<String, Double> increased = new HashMap<>();
        notFoundAfter.forEach((uri, count) -> {
            double delta = count - notFoundBefore.getOrDefault(uri, 0.0);
            if (delta > 0) {
                increased.put(uri, delta);
            }
        });
        assertThat(increased).as("séries 404 que cresceram").hasSize(1);
        assertThat(increased.values()).containsExactly(3.0);
        assertThat(uris(after)).noneMatch(uri -> uri.contains("nao-existe") || uri.contains("/x/y/z"));
    }

    private static Map<String, Double> notFoundByUri(List<Sample> samples) {
        return PrometheusText.find(samples, COUNT, labels("status", "404")).stream()
                .collect(Collectors.groupingBy(s -> s.label("uri"), Collectors.summingDouble(Sample::value)));
    }

    @Test
    @DisplayName("US3-6 revalidação com 304 é registrada no status 304, fora da taxa de erro")
    void notModifiedIsRecorded() {
        String etag = http.get().uri(INTERVALS).exchange().expectBody(byte[].class).returnResult()
                .getResponseHeaders().getETag();
        Predicate<Sample> notModified = labels("uri", INTERVALS, "status", "304");
        double before = PrometheusText.sum(scrape(), COUNT, notModified);

        http.get().uri(INTERVALS).header(HttpHeaders.IF_NONE_MATCH, etag).exchange().expectStatus().isNotModified();

        List<Sample> after = scrape();
        assertThat(PrometheusText.sum(after, COUNT, notModified) - before).isEqualTo(1.0);
        assertThat(PrometheusText.find(after, COUNT, notModified))
                .allSatisfy(s -> assertThat(s.label("outcome")).isNotIn("CLIENT_ERROR", "SERVER_ERROR"));
    }

    @Test
    @DisplayName("FR-014 US3-7 latência exposta em faixas cumulativas, com +Inf")
    void latencyHistogramIsCumulative() {
        http.get().uri(INTERVALS).exchange().expectStatus().isOk();
        List<Sample> buckets = PrometheusText.find(scrape(), BUCKET,
                labels("uri", INTERVALS, "method", "GET", "status", "200"));
        assertThat(buckets).extracting(s -> s.label("le")).contains("+Inf").hasSizeGreaterThan(5);

        List<Sample> ordered = buckets.stream()
                .sorted(Comparator.comparingDouble(s -> leValue(s.label("le"))))
                .toList();
        for (int i = 1; i < ordered.size(); i++) {
            assertThat(ordered.get(i).value()).as("faixa le=%s", ordered.get(i).label("le"))
                    .isGreaterThanOrEqualTo(ordered.get(i - 1).value());
        }
    }

    private static double leValue(String le) {
        return "+Inf".equals(le) ? Double.POSITIVE_INFINITY : Double.parseDouble(le);
    }

    @Test
    @DisplayName("FR-016 métricas da JVM e do processo, todas com o rótulo application")
    void jvmMetricsWithApplicationLabel() {
        http.get().uri(INTERVALS).exchange().expectStatus().isOk();
        List<Sample> samples = scrape();
        assertThat(PrometheusText.find(samples, COUNT, s -> true))
                .isNotEmpty()
                .allSatisfy(s -> assertThat(s.label("application")).isEqualTo("awards"));
        for (String metric : List.of("jvm_memory_used_bytes", "jvm_threads_live_threads", "process_uptime_seconds")) {
            assertThat(PrometheusText.find(samples, metric, labels("application", "awards")))
                    .as("métrica %s com application=awards", metric)
                    .isNotEmpty();
        }
    }
}
