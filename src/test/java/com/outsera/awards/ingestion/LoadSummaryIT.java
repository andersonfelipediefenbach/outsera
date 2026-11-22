package com.outsera.awards.ingestion;

import static org.assertj.core.api.Assertions.assertThat;

import com.outsera.awards.support.ScenarioIT;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.test.context.TestPropertySource;

/** Ao terminar a carga, o log traz o resumo de linhas lidas, aceitas e rejeitadas (FR-006b, SC-006a). */
@TestPropertySource(properties = "movies.csv.path=classpath:scenarios/load-summary.csv")
class LoadSummaryIT extends ScenarioIT {

    @Test
    @DisplayName("FR-006b SC-006a resumo")
    void summaryMatchesTheRealCount(CapturedOutput output) {
        assertThat(output.getAll()).contains(
                "Carga do arquivo classpath:scenarios/load-summary.csv concluída: "
                        + "10 linhas lidas, 8 aceitas, 2 rejeitadas");
        assertThat(movies("").totalElements()).isEqualTo(8);
    }
}
