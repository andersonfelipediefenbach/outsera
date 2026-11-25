package com.outsera.awards.security;

import static com.outsera.awards.security.CacheHeadersIT.assertBusinessHeaders;
import static com.outsera.awards.security.CacheHeadersIT.assertProtection;
import static org.assertj.core.api.Assertions.assertThat;

import com.outsera.awards.support.TokenScenarioIT;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.client.EntityExchangeResult;

/**
 * Cabeçalhos de cache e de proteção com o token ligado (spec 003, FR-022a e FR-021; research R12).
 */
class CacheHeadersWithTokenIT extends TokenScenarioIT {

    @Test
    @DisplayName("S003 FR-022a FR-021 com o token: negócio sem cache, com ETag e 304; 401 e métricas com cache")
    void headersWithTheToken() {
        for (String path : new String[] {INTERVALS, MOVIES}) {
            EntityExchangeResult<byte[]> ok = http.get().uri(path).header(HttpHeaders.AUTHORIZATION, bearer())
                    .exchange().expectStatus().isOk().expectBody(byte[].class).returnResult();
            assertBusinessHeaders(ok);
            EntityExchangeResult<byte[]> notModified = http.get().uri(path)
                    .header(HttpHeaders.AUTHORIZATION, bearer())
                    .header(HttpHeaders.IF_NONE_MATCH, ok.getResponseHeaders().getETag()).exchange()
                    .expectStatus().isNotModified().expectBody(byte[].class).returnResult();
            assertBusinessHeaders(notModified);

            HttpHeaders unauthorized = http.get().uri(path).exchange().expectStatus().isUnauthorized()
                    .returnResult().getResponseHeaders();
            assertProtection(unauthorized);
            assertThat(unauthorized.getCacheControl()).contains("no-store");
        }
        HttpHeaders metrics = http.get().uri("/actuator/prometheus").header(HttpHeaders.AUTHORIZATION, bearer())
                .exchange().expectStatus().isOk().returnResult().getResponseHeaders();
        assertProtection(metrics);
        assertThat(metrics.getCacheControl()).contains("no-store");
    }
}
