package com.outsera.awards.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.outsera.awards.support.TokenScenarioIT;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.web.servlet.client.EntityExchangeResult;

/**
 * O 401 é um Problem Details, igual para o token ausente e para o token errado, sem revelar nenhum dos
 * dois valores (spec 003, FR-023 e FR-025).
 */
class AuthProblemDetailsIT extends TokenScenarioIT {

    private EntityExchangeResult<String> unauthorized(String authorization) {
        var request = http.get().uri(INTERVALS);
        if (authorization != null) {
            request = request.header(HttpHeaders.AUTHORIZATION, authorization);
        }
        return request.exchange()
                .expectStatus().isUnauthorized()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
                .expectHeader().valueEquals(HttpHeaders.WWW_AUTHENTICATE, "Bearer realm=\"awards\"")
                .expectBody(String.class).returnResult();
    }

    @Test
    @DisplayName("S003 FR-023 FR-025 401 em Problem Details, igual sem token e com token errado")
    void problemDetails() {
        String sent = "Bearer token-enviado-errado";
        String missing = unauthorized(null).getResponseBody();
        String wrong = unauthorized(sent).getResponseBody();

        http.get().uri(INTERVALS).exchange().expectBody().json("""
                {"type":"about:blank","title":"Unauthorized","status":401,
                 "detail":"Token ausente ou inválido","instance":"/api/producers/intervals"}""",
                JsonCompareMode.STRICT);
        assertThat(wrong).isEqualTo(missing);
        assertThat(missing).doesNotContain(TOKEN).doesNotContain("token-enviado-errado");
    }
}
