package com.outsera.awards.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.outsera.awards.support.ScenarioIT;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.client.EntityExchangeResult;

/**
 * Cabeçalhos de cache e de proteção com o token desligado (spec 003, FR-022a, FR-019; research R12).
 * Com o token ligado, a mesma verificação está no {@link CacheHeadersWithTokenIT}.
 */
class CacheHeadersIT extends ScenarioIT {

    static final String NOSNIFF = "nosniff";
    static final String DENY = "DENY";

    /** Resposta de negócio: com ETag, sem cabeçalhos de cache, com os de proteção. */
    static void assertBusinessHeaders(EntityExchangeResult<?> result) {
        HttpHeaders headers = result.getResponseHeaders();
        assertThat(headers.getETag()).as("ETag").isNotNull();
        assertThat(headers.get(HttpHeaders.CACHE_CONTROL)).as("Cache-Control").isNull();
        assertThat(headers.get(HttpHeaders.PRAGMA)).as("Pragma").isNull();
        assertThat(headers.get(HttpHeaders.EXPIRES)).as("Expires").isNull();
        assertProtection(headers);
    }

    static void assertProtection(HttpHeaders headers) {
        assertThat(headers.getFirst("X-Content-Type-Options")).isEqualTo(NOSNIFF);
        assertThat(headers.getFirst("X-Frame-Options")).isEqualTo(DENY);
    }

    @Test
    @DisplayName("S003 FR-022a FR-019 negócio sem cabeçalhos de cache, com ETag, 304 e cabeçalhos de proteção")
    void businessEndpoints() {
        for (String path : new String[] {INTERVALS, MOVIES}) {
            EntityExchangeResult<byte[]> ok = http.get().uri(path).exchange()
                    .expectStatus().isOk().expectBody(byte[].class).returnResult();
            assertBusinessHeaders(ok);
            EntityExchangeResult<byte[]> notModified = http.get().uri(path)
                    .header(HttpHeaders.IF_NONE_MATCH, ok.getResponseHeaders().getETag()).exchange()
                    .expectStatus().isNotModified().expectBody(byte[].class).returnResult();
            assertBusinessHeaders(notModified);
        }
    }

    @Test
    @DisplayName("S003 FR-022a cabeçalhos de proteção e de cache fora dos endpoints de negócio")
    void otherEndpoints() {
        for (String path : new String[] {"/actuator/health/readiness", "/openapi.yaml", "/actuator/prometheus"}) {
            HttpHeaders headers = http.get().uri(path).exchange().expectStatus().isOk()
                    .returnResult().getResponseHeaders();
            assertProtection(headers);
            assertThat(headers.getCacheControl()).as("Cache-Control de %s", path).contains("no-store");
        }
    }
}
