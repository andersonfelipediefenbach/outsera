package com.outsera.awards.etag;

import static org.assertj.core.api.Assertions.assertThat;

import com.outsera.awards.support.ScenarioIT;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.client.EntityExchangeResult;

/**
 * ETag e requisições condicionais em {@code GET /api/movies}, com os 25 filmes do
 * {@code listing-sample.csv} (FR-001 a FR-006; spec 002, história 1).
 */
@TestPropertySource(properties = "movies.csv.path=classpath:scenarios/listing-sample.csv")
class MoviesEtagIT extends ScenarioIT {

    private static final String WINNERS_5 = "?winner=true&size=5";

    private EntityExchangeResult<byte[]> get(String query, String ifNoneMatch) {
        var request = http.get().uri(MOVIES + query);
        if (ifNoneMatch != null) {
            request = request.header(HttpHeaders.IF_NONE_MATCH, ifNoneMatch);
        }
        return request.exchange().expectBody(byte[].class).returnResult();
    }

    private String etagOf(String query) {
        EntityExchangeResult<byte[]> result = get(query, null);
        assertThat(result.getStatus().value()).as("GET %s", query).isEqualTo(200);
        return result.getResponseHeaders().getETag();
    }

    @Test
    @DisplayName("FR-001 página da listagem traz ETag forte")
    void pageHasEtag() {
        assertThat(etagOf(WINNERS_5)).isNotNull().matches(IntervalsEtagIT.STRONG_ETAG);
    }

    @Test
    @DisplayName("FR-002 a mesma consulta repetida tem o mesmo ETag")
    void sameQuerySameEtag() {
        assertThat(etagOf(WINNERS_5)).isEqualTo(etagOf(WINNERS_5));
    }

    @Test
    @DisplayName("FR-003 US1-4 a mesma consulta com o ETag recebido dá 304 sem corpo")
    void sameQueryIsNotModified() {
        String etag = etagOf(WINNERS_5);
        EntityExchangeResult<byte[]> result = get(WINNERS_5, etag);
        assertThat(result.getStatus().value()).isEqualTo(304);
        assertThat(result.getResponseBody()).isNullOrEmpty();
        assertThat(result.getResponseHeaders().getETag()).isEqualTo(etag);
    }

    @Test
    @DisplayName("FR-002 US1-5 o ETag de uma consulta não vale para outra com outro resultado")
    void otherQueryIsOkWithOtherEtag() {
        String etag = etagOf(WINNERS_5);
        for (String other : new String[] {"?winner=true&size=6", "?winner=true&size=5&page=1",
                "?winner=false&size=5"}) {
            EntityExchangeResult<byte[]> result = get(other, etag);
            assertThat(result.getStatus().value()).as("GET %s", other).isEqualTo(200);
            assertThat(result.getResponseBody()).as("corpo de %s", other).isNotEmpty();
            assertThat(result.getResponseHeaders().getETag()).as("ETag de %s", other)
                    .isNotNull().isNotEqualTo(etag);
        }
    }

    @Test
    @DisplayName("FR-006 parâmetro inválido com If-None-Match continua 400, sem ETag")
    void badRequestHasNoEtag() {
        EntityExchangeResult<byte[]> result = get("?winner=talvez", "*");
        assertThat(result.getStatus().value()).isEqualTo(400);
        assertThat(result.getResponseHeaders().getContentType()).isNotNull()
                .satisfies(type -> assertThat(type.isCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)).isTrue());
        assertThat(result.getResponseHeaders().getETag()).isNull();
    }

    @Test
    @DisplayName("FR-001 FR-003 página além da última tem ETag e responde 304 na revalidação")
    void pageBeyondLastHasEtag() {
        String query = "?page=2&size=20";
        EntityExchangeResult<byte[]> first = get(query, null);
        assertThat(first.getStatus().value()).isEqualTo(200);
        assertThat(movies("page=2&size=20").content()).isEmpty();
        String etag = first.getResponseHeaders().getETag();
        assertThat(etag).isNotNull();

        assertThat(get(query, etag).getStatus().value()).isEqualTo(304);
    }
}
