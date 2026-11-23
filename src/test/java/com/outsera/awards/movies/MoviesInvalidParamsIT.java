package com.outsera.awards.movies;

import com.outsera.awards.support.ScenarioIT;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;

/**
 * Parâmetros inválidos na listagem respondem 400 em Problem Details (RFC 9457), com o parâmetro e o
 * motivo em {@code invalidParams} (FR-023, US3-5).
 */
@TestPropertySource(properties = "movies.csv.path=classpath:scenarios/listing-sample.csv")
class MoviesInvalidParamsIT extends ScenarioIT {

    @ParameterizedTest(name = "FR-023 {0}={1} responde 400")
    @CsvSource(delimiter = '|', textBlock = """
            year   | abc
            year   | -1
            year   | 19.5
            year   | 1234567890
            winner | yes
            winner | 1
            page   | -1
            page   | x
            page   | 2147483648
            size   | 0
            size   | 101
            size   | abc
            """)
    void invalidParameter(String name, String value) {
        http.get().uri(MOVIES + "?" + name + "=" + value).exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.title").isEqualTo("Bad Request")
                .jsonPath("$.detail").isEqualTo("Parâmetro inválido na consulta")
                .jsonPath("$.instance").isEqualTo(MOVIES)
                .jsonPath("$.invalidParams.length()").isEqualTo(1)
                .jsonPath("$.invalidParams[0].name").isEqualTo(name)
                .jsonPath("$.invalidParams[0].reason").isNotEmpty();
    }

    @Test
    @DisplayName("FR-023 motivo do winner inválido")
    void winnerReason() {
        http.get().uri(MOVIES + "?winner=yes").exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.invalidParams[0].reason").isEqualTo("deve ser true ou false");
    }

    @Test
    @DisplayName("FR-023 todos os parâmetros inválidos aparecem, na ordem year, winner, page e size")
    void allInvalidParametersAreListed() {
        http.get().uri(MOVIES + "?size=0&page=-1&winner=sim&year=abc").exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.invalidParams.length()").isEqualTo(4)
                .jsonPath("$.invalidParams[0].name").isEqualTo("year")
                .jsonPath("$.invalidParams[1].name").isEqualTo("winner")
                .jsonPath("$.invalidParams[2].name").isEqualTo("page")
                .jsonPath("$.invalidParams[3].name").isEqualTo("size");
    }

    @Test
    @DisplayName("FR-023 parâmetro desconhecido é ignorado")
    void unknownParameterIsIgnored() {
        http.get().uri(MOVIES + "?foo=bar").exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.totalElements").isEqualTo(25);
    }
}
