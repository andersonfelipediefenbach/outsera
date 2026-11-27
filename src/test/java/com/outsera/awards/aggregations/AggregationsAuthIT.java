package com.outsera.awards.aggregations;

import static org.assertj.core.api.Assertions.assertThat;

import com.outsera.awards.support.TokenScenarioIT;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.EntityExchangeResult;

/**
 * Com a autenticação ligada, as duas agregações exigem o token, como os demais endpoints de negócio (spec 006,
 * FR-003; US4-5). Roda no perfil {@code extras}: fica fora do {@code default-mode-its.txt}.
 */
class AggregationsAuthIT extends TokenScenarioIT {

    @ParameterizedTest(name = "S006 FR-003 US4-5 {0} sem token ou com token errado responde 401 em Problem Details")
    @ValueSource(strings = {AggregationsReferenceIT.YEARS, AggregationsReferenceIT.STUDIOS})
    void withoutTheTokenIsUnauthorized(String path) {
        for (String authorization : new String[] {null, "Bearer errado"}) {
            var request = http.get().uri(path);
            if (authorization != null) {
                request = request.header(HttpHeaders.AUTHORIZATION, authorization);
            }
            EntityExchangeResult<byte[]> result = request.exchange().expectBody(byte[].class).returnResult();
            assertThat(result.getStatus().value()).as("%s com %s", path, authorization).isEqualTo(401);
            assertThat(result.getResponseHeaders().getContentType()).isNotNull()
                    .satisfies(type -> assertThat(type.isCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)).isTrue());
            assertThat(result.getResponseHeaders().getFirst(HttpHeaders.WWW_AUTHENTICATE)).startsWith("Bearer");
        }
    }

    @ParameterizedTest(name = "S006 FR-003 US4-5 {0} com o token certo responde 200")
    @ValueSource(strings = {AggregationsReferenceIT.YEARS, AggregationsReferenceIT.STUDIOS})
    void withTheTokenIsOk(String path) {
        assertThat(status(path, bearer())).isEqualTo(200);
    }
}
