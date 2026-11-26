package com.outsera.awards.aggregations.adversario;

import static org.assertj.core.api.Assertions.assertThat;

import com.outsera.awards.support.ContractValidator;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.client.EntityExchangeResult;

/**
 * Caso 27 do testador adversário: revalidação separada das duas agregações, com o arquivo fornecido, pelo ETag
 * da própria rota, por uma lista com a forma fraca e por {@code *} (spec 006, FR-003 e FR-004).
 */
class FrontCase27AggregationsEtagIT extends FrontAdversarialCaseIT {

    private EntityExchangeResult<byte[]> get(String path, String ifNoneMatch) {
        var request = http.get().uri(path);
        if (ifNoneMatch != null) {
            request = request.header(HttpHeaders.IF_NONE_MATCH, ifNoneMatch);
        }
        return request.exchange().expectBody(byte[].class).returnResult();
    }

    @ParameterizedTest(name = "S006 adversário caso 27 FR-003 {0}: 200 com ETag, e 304 sem corpo para o próprio ETag, a lista com W/ e *")
    @ValueSource(strings = {YEARS, STUDIOS})
    void revalidation(String path) {
        EntityExchangeResult<byte[]> first = get(path, null);
        assertThat(first.getStatus().value()).isEqualTo(200);
        ContractValidator.assertValidGet(path, first);
        String etag = first.getResponseHeaders().getETag();
        assertThat(etag).isNotNull();

        SoftAssertions soft = new SoftAssertions();
        for (String header : new String[] {etag, "\"caso-27-distinta\", W/" + etag, "*"}) {
            EntityExchangeResult<byte[]> again = get(path, header);
            soft.assertThat(again.getStatus().value()).as("%s com If-None-Match %s", path, header).isEqualTo(304);
            soft.assertThat(again.getResponseBody()).as("corpo do 304 com %s", header).isNullOrEmpty();
            soft.assertThat(again.getResponseHeaders().getETag()).as("ETag do 304 com %s", header).isEqualTo(etag);
        }
        soft.assertAll();
    }

    @ParameterizedTest(name = "S006 adversário caso 27 FR-003 o ETag de uma agregação não vale para a outra ({0})")
    @ValueSource(strings = {YEARS, STUDIOS})
    void etagIsPerRoute(String path) {
        String other = path.equals(YEARS) ? STUDIOS : YEARS;
        String otherEtag = get(other, null).getResponseHeaders().getETag();
        assertThat(get(path, otherEtag).getStatus().value()).isEqualTo(200);
    }
}
