package com.outsera.awards.web;

import static org.assertj.core.api.Assertions.assertThat;

import com.outsera.awards.support.ScenarioIT;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

/**
 * Os endpoints de negócio só aceitam GET. Outro verbo responde 405 em Problem Details, com o cabeçalho
 * {@code Allow} (FR-023a, item 4.1 do enunciado: nível 2 de Richardson).
 */
class HttpMethodsIT extends ScenarioIT {

    @ParameterizedTest(name = "FR-023a {0} {1} responde 405")
    @CsvSource(delimiter = '|', textBlock = """
            POST   | /api/producers/intervals
            PUT    | /api/producers/intervals
            DELETE | /api/producers/intervals
            POST   | /api/movies
            PUT    | /api/movies
            DELETE | /api/movies
            """)
    void unsupportedMethodIsRejected(String method, String path) {
        var result = http.method(HttpMethod.valueOf(method)).uri(path).exchange()
                .expectStatus().isEqualTo(405)
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.status").isEqualTo(405)
                .jsonPath("$.instance").isEqualTo(path)
                .returnResult();

        assertThat(result.getResponseHeaders().get(HttpHeaders.ALLOW))
                .as("cabeçalho Allow de %s %s", method, path)
                .isNotNull()
                .anySatisfy(allow -> assertThat(allow).contains("GET"))
                .noneSatisfy(allow -> assertThat(allow).containsAnyOf("POST", "PUT", "DELETE"));
    }
}
