package com.outsera.awards.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.outsera.awards.support.TokenScenarioIT;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

/**
 * Com o token ligado, os endpoints de negócio e o de métricas exigem {@code Authorization: Bearer}, e o
 * resto continua público (spec 003, FR-020 a FR-022, US4-2 a US4-5; contracts/operational-endpoints.md).
 */
class AuthEnabledIT extends TokenScenarioIT {

    @ParameterizedTest(name = "S003 FR-020 FR-021 US4-2 US4-4 {0} exige o token")
    @ValueSource(strings = {"/api/producers/intervals", "/api/movies", "/actuator/prometheus"})
    void protectedEndpointsRequireTheToken(String path) {
        assertThat(status(path, null)).as("sem token").isEqualTo(401);
        assertThat(status(path, "Bearer errado")).as("token errado").isEqualTo(401);
        assertThat(status(path, "Basic dXNlcjpwYXNz")).as("outro esquema").isEqualTo(401);
        assertThat(status(path, "Bearer  " + TOKEN)).as("espaço a mais").isEqualTo(401);
        assertThat(status(path, "Bearer " + TOKEN.toUpperCase())).as("outra caixa").isEqualTo(401);
        assertThat(status(path, "bearer " + TOKEN)).as("esquema em minúsculas").isEqualTo(401);
        assertThat(status(path, bearer())).as("token certo").isEqualTo(200);
    }

    @Test
    @DisplayName("S003 FR-021 US4-3 com o token, a resposta é a do Marco 2, com ETag e 304")
    void withTheTokenTheResponseIsTheMilestoneTwoOne() {
        String body = http.get().uri(INTERVALS).header(HttpHeaders.AUTHORIZATION, bearer()).exchange()
                .expectStatus().isOk()
                .expectBody(String.class).returnResult().getResponseBody();
        assertThat(body).contains("Joel Silver").contains("Matthew Vaughn");
        for (String path : new String[] {INTERVALS, MOVIES}) {
            String etag = http.get().uri(path).header(HttpHeaders.AUTHORIZATION, bearer()).exchange()
                    .expectStatus().isOk()
                    .returnResult().getResponseHeaders().getETag();
            assertThat(etag).isNotNull();
            http.get().uri(path).header(HttpHeaders.AUTHORIZATION, bearer())
                    .header(HttpHeaders.IF_NONE_MATCH, etag).exchange()
                    .expectStatus().isNotModified();
        }
    }

    @Test
    @DisplayName("S003 FR-021 POST e OPTIONS sem token num endpoint protegido recebem 401")
    void otherMethodsWithoutTokenGet401() {
        http.post().uri(MOVIES).exchange().expectStatus().isUnauthorized();
        for (String path : new String[] {INTERVALS, MOVIES}) {
            http.method(HttpMethod.OPTIONS).uri(path).exchange().expectStatus().isUnauthorized();
        }
    }

    @ParameterizedTest(name = "S003 FR-022 US4-5 {0} continua público")
    @ValueSource(strings = {
            "/actuator/health", "/actuator/health/liveness", "/actuator/health/readiness", "/openapi.yaml",
            "/caminho-que-nao-existe"})
    void publicEndpoints(String path) {
        int status = status(path, null);
        assertThat(status).as("GET %s sem token", path).isNotEqualTo(401);
        assertThat(status).isEqualTo(path.equals("/caminho-que-nao-existe") ? 404 : 200);
    }

    @Test
    @DisplayName("S003 FR-022 US4-5 Swagger UI e os arquivos do webjar continuam públicos")
    void swaggerUiIsPublic() {
        String page = http.get().uri("/swagger-ui.html").exchange()
                .expectStatus().isOk()
                .expectBody(String.class).returnResult().getResponseBody();
        Matcher asset = Pattern.compile("(?:src|href)=\"(/webjars/[^\"]+)\"").matcher(page);
        assertThat(asset.find()).isTrue();
        int status = status(asset.group(1), null);
        assertThat(status).as("GET %s sem token", asset.group(1)).isIn(200, 301, 302);
    }
}
