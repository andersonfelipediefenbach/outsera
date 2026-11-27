package com.outsera.awards.aggregations;

import static org.assertj.core.api.Assertions.assertThat;

import com.outsera.awards.support.ScenarioIT;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.client.EntityExchangeResult;

/**
 * ETag e requisições condicionais nas duas agregações, como nos demais endpoints de negócio (spec 006, FR-003;
 * US4-6), com o arquivo fornecido.
 */
class AggregationsEtagIT extends ScenarioIT {

    /** ETag forte derivado do conteúdo, no mesmo formato do {@code IntervalsEtagIT} (spec 002, FR-001). */
    private static final String STRONG_ETAG = "^\"0[0-9a-f]{32}\"$";

    private EntityExchangeResult<byte[]> get(String path, String ifNoneMatch) {
        var request = http.get().uri(path);
        if (ifNoneMatch != null) {
            request = request.header(HttpHeaders.IF_NONE_MATCH, ifNoneMatch);
        }
        return request.exchange().expectBody(byte[].class).returnResult();
    }

    @ParameterizedTest(name = "S006 FR-003 US4-6 {0} traz ETag forte e responde 304 ao mesmo ETag")
    @ValueSource(strings = {AggregationsReferenceIT.YEARS, AggregationsReferenceIT.STUDIOS})
    void etagAndNotModified(String path) {
        EntityExchangeResult<byte[]> first = get(path, null);
        assertThat(first.getStatus().value()).isEqualTo(200);
        String etag = first.getResponseHeaders().getETag();
        assertThat(etag).isNotNull().matches(STRONG_ETAG);

        EntityExchangeResult<byte[]> again = get(path, etag);
        assertThat(again.getStatus().value()).isEqualTo(304);
        assertThat(again.getResponseBody()).isNullOrEmpty();
        assertThat(again.getResponseHeaders().getETag()).isEqualTo(etag);
    }

    @ParameterizedTest(name = "S006 FR-003 {0} com outro ETag responde 200 com corpo")
    @ValueSource(strings = {AggregationsReferenceIT.YEARS, AggregationsReferenceIT.STUDIOS})
    void otherEtagIsOk(String path) {
        EntityExchangeResult<byte[]> result = get(path, "\"outro\"");
        assertThat(result.getStatus().value()).isEqualTo(200);
        assertThat(result.getResponseBody()).isNotEmpty();
    }
}
