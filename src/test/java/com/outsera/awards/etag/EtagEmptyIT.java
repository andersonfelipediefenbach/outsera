package com.outsera.awards.etag;

import static org.assertj.core.api.Assertions.assertThat;

import com.outsera.awards.support.ScenarioIT;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.client.EntityExchangeResult;

/** Arquivo só com o cabeçalho: os intervalos vazios também têm ETag e revalidação (caso de borda). */
@TestPropertySource(properties = "movies.csv.path=classpath:scenarios/header-only.csv")
class EtagEmptyIT extends ScenarioIT {

    @Test
    @DisplayName("FR-001 FR-003 intervalos vazios têm ETag e respondem 304 na revalidação")
    void emptyIntervalsHaveEtag() {
        assertIntervals(List.of(), List.of());

        EntityExchangeResult<byte[]> first = http.get().uri(INTERVALS).exchange()
                .expectBody(byte[].class).returnResult();
        String etag = first.getResponseHeaders().getETag();
        assertThat(etag).isNotNull().matches(IntervalsEtagIT.STRONG_ETAG);

        EntityExchangeResult<byte[]> second = http.get().uri(INTERVALS)
                .header(HttpHeaders.IF_NONE_MATCH, etag)
                .exchange().expectBody(byte[].class).returnResult();
        assertThat(second.getStatus().value()).isEqualTo(304);
        assertThat(second.getResponseBody()).isNullOrEmpty();
    }
}
