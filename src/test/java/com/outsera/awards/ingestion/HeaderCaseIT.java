package com.outsera.awards.ingestion;

import static org.assertj.core.api.Assertions.assertThat;

import com.outsera.awards.support.LoaderLog;
import com.outsera.awards.support.LoaderLog.Summary;
import com.outsera.awards.support.ScenarioIT;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.test.context.TestPropertySource;

/** Os nomes das colunas do cabeçalho são comparados sem diferenciar maiúsculas (FR-003, FR-005a). */
@TestPropertySource(properties = "movies.csv.path=classpath:scenarios/header-case.csv")
class HeaderCaseIT extends ScenarioIT {

    @Test
    @DisplayName("FR-003 FR-005a cabeçalho com maiúsculas diferentes")
    void headerWithDifferentCaseIsAccepted(CapturedOutput output) {
        Summary summary = LoaderLog.summary(output.getAll());
        assertThat(summary.read()).isEqualTo(3);
        assertThat(summary.accepted()).isEqualTo(3);
        assertThat(summary.rejected()).isZero();
        assertThat(LoaderLog.loaderWarnings(output.getAll())).isEmpty();
        assertThat(movies("").totalElements()).isEqualTo(3);
    }
}
