package com.outsera.awards.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.outsera.awards.support.ScenarioIT;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.servlet.client.EntityExchangeResult;

/**
 * Sem {@code API_TOKEN}, a autenticação fica desligada e tudo se comporta como no Marco 2 (spec 003,
 * FR-019, FR-022, US4-1 e SC-002; research R14).
 *
 * <p>É também a <strong>guarda</strong> da suíte dos Marcos 1 e 2: se a execução padrão do Failsafe
 * tivesse {@code API_TOKEN} definido, os testes antigos rodariam com a autenticação ligada, e as falhas
 * pareceriam defeitos deles.
 */
class AuthDisabledByDefaultIT extends ScenarioIT {

    @Autowired
    private Environment environment;

    @Test
    @DisplayName("S003 SC-002 guarda: a execução padrão roda sem API_TOKEN")
    void defaultRunHasNoToken() {
        String fromEnvironment = System.getenv("API_TOKEN");
        assertThat(fromEnvironment == null || fromEnvironment.isEmpty())
                .as("API_TOKEN definido no ambiente: a suíte dos Marcos 1 e 2 estaria rodando com a autenticação ligada")
                .isTrue();
        assertThat(environment.getProperty("awards.security.token", "")).isEmpty();
    }

    @ParameterizedTest(name = "S003 FR-019 FR-022 US4-1 {0} sem token")
    @ValueSource(strings = {
            "/api/producers/intervals", "/api/movies", "/actuator/prometheus", "/actuator/health",
            "/actuator/health/liveness", "/actuator/health/readiness", "/openapi.yaml"})
    void everythingIsPublic(String path) {
        EntityExchangeResult<byte[]> result = http.get().uri(path).exchange()
                .expectBody(byte[].class).returnResult();
        assertThat(result.getStatus().value()).isEqualTo(200);
        assertThat(result.getResponseHeaders().get(HttpHeaders.WWW_AUTHENTICATE)).isNull();
    }

    @Test
    @DisplayName("S003 FR-019 um cabeçalho Authorization qualquer é ignorado")
    void authorizationHeaderIsIgnored() {
        for (String path : List.of(INTERVALS, MOVIES)) {
            http.get().uri(path).header(HttpHeaders.AUTHORIZATION, "Bearer qualquer").exchange()
                    .expectStatus().isOk();
        }
    }

    @Test
    @DisplayName("S003 FR-019 OPTIONS responde como no Marco 2")
    void optionsAsInMilestoneTwo() {
        for (String path : List.of(INTERVALS, MOVIES)) {
            EntityExchangeResult<byte[]> result = http.method(HttpMethod.OPTIONS).uri(path).exchange()
                    .expectBody(byte[].class).returnResult();
            assertThat(result.getStatus().is2xxSuccessful()).as("OPTIONS %s: %s", path, result.getStatus()).isTrue();
            assertThat(result.getResponseHeaders().getAllow()).contains(HttpMethod.GET);
        }
    }
}
