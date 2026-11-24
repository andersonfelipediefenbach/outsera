package com.outsera.awards.etag;

import static com.outsera.awards.support.IntervalItem.item;
import static org.assertj.core.api.Assertions.assertThat;

import com.outsera.awards.support.ScenarioIT;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.EntityExchangeResult;

/**
 * ETag e requisições condicionais em {@code GET /api/producers/intervals}, com o arquivo padrão
 * (FR-001 a FR-006; spec 002, história 1).
 */
class IntervalsEtagIT extends ScenarioIT {

    /** ETag forte: {@code "0"} seguido de 32 dígitos hexadecimais, entre aspas (research R4). */
    static final String STRONG_ETAG = "^\"0[0-9a-f]{32}\"$";

    private EntityExchangeResult<byte[]> get(String ifNoneMatch) {
        var request = http.get().uri(INTERVALS);
        if (ifNoneMatch != null) {
            request = request.header(HttpHeaders.IF_NONE_MATCH, ifNoneMatch);
        }
        return request.exchange().expectBody(byte[].class).returnResult();
    }

    private String currentEtag() {
        EntityExchangeResult<byte[]> result = get(null);
        assertThat(result.getStatus().value()).isEqualTo(200);
        return result.getResponseHeaders().getETag();
    }

    private void assertNotModified(EntityExchangeResult<byte[]> result, String etag) {
        assertThat(result.getStatus().value()).isEqualTo(304);
        assertThat(result.getResponseBody()).isNullOrEmpty();
        assertThat(result.getResponseHeaders().getETag()).isEqualTo(etag);
    }

    private void assertFullResponse(EntityExchangeResult<byte[]> result, String etag) {
        assertThat(result.getStatus().value()).isEqualTo(200);
        assertThat(result.getResponseBody()).isNotEmpty();
        assertThat(result.getResponseHeaders().getETag()).isEqualTo(etag);
    }

    @Test
    @DisplayName("FR-001 resposta 200 traz ETag forte")
    void okHasStrongEtag() {
        assertThat(currentEtag()).isNotNull().matches(STRONG_ETAG);
    }

    @Test
    @DisplayName("FR-005 corpo idêntico ao do Marco 1")
    void bodyIsUnchanged() {
        assertIntervals(
                List.of(item("Joel Silver", 1, 1990, 1991)),
                List.of(item("Matthew Vaughn", 13, 2002, 2015)));
    }

    @Test
    @DisplayName("FR-003 If-None-Match igual ao ETag atual dá 304 sem corpo")
    void sameEtagIsNotModified() {
        String etag = currentEtag();
        assertNotModified(get(etag), etag);
    }

    @Test
    @DisplayName("FR-003 If-None-Match com lista que contém o ETag atual dá 304")
    void listWithCurrentEtagIsNotModified() {
        String etag = currentEtag();
        assertNotModified(get("\"outro\", " + etag), etag);
    }

    @Test
    @DisplayName("FR-003 If-None-Match na forma fraca dá 304")
    void weakFormIsNotModified() {
        String etag = currentEtag();
        assertNotModified(get("W/" + etag), etag);
    }

    @Test
    @DisplayName("FR-003 If-None-Match com * dá 304")
    void wildcardIsNotModified() {
        String etag = currentEtag();
        assertNotModified(get("*"), etag);
    }

    @Test
    @DisplayName("FR-004 If-None-Match diferente dá 200 com corpo")
    void differentEtagIsOk() {
        String etag = currentEtag();
        assertFullResponse(get("\"outro\""), etag);
    }

    @Test
    @DisplayName("FR-004 If-None-Match malformado ou vazio dá 200 com corpo")
    void malformedOrEmptyIsOk() {
        String etag = currentEtag();
        assertFullResponse(get("abc"), etag);
        assertFullResponse(get(""), etag);
    }

    @Test
    @DisplayName("FR-006 POST com If-None-Match continua 405, sem ETag")
    void methodNotAllowedHasNoEtag() {
        EntityExchangeResult<byte[]> result = http.post().uri(INTERVALS)
                .header(HttpHeaders.IF_NONE_MATCH, "*")
                .exchange()
                .expectStatus().isEqualTo(405)
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody(byte[].class)
                .returnResult();
        assertThat(result.getResponseHeaders().getETag()).isNull();
    }
}
