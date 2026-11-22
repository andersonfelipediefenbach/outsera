package com.outsera.awards.ingestion;

import static org.assertj.core.api.Assertions.assertThat;

import com.outsera.awards.support.LoaderLog;
import com.outsera.awards.support.LoaderLog.Summary;
import com.outsera.awards.support.ScenarioIT;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.test.context.TestPropertySource;

/** Um arquivo válido sem nenhum filme aceito não impede a aplicação de subir (casos de borda). */
class EmptyLoadIT {

    @Nested
    @DisplayName("Edge só cabeçalho")
    @TestPropertySource(properties = "movies.csv.path=classpath:scenarios/header-only.csv")
    class HeaderOnly extends ScenarioIT {

        @Test
        @DisplayName("a aplicação sobe, sem linhas lidas e sem filmes")
        void startsWithNoMovies(CapturedOutput output) {
            Summary summary = LoaderLog.summary(output.getAll());
            assertThat(summary.read()).isZero();
            assertThat(summary.accepted()).isZero();
            assertThat(jdbc.queryForObject("select count(*) from movie", Integer.class)).isZero();
        }

        @Test
        @DisplayName("FR-018 Edge arquivo sem linhas válidas")
        void emptyIntervals() {
            assertIntervals(List.of(), List.of());
        }
    }

    @Nested
    @DisplayName("Edge todas inválidas")
    @TestPropertySource(properties = "movies.csv.path=classpath:scenarios/all-invalid.csv")
    class AllInvalid extends ScenarioIT {

        @Test
        @DisplayName("a aplicação sobe, com 0 aceitas e sem filmes")
        void startsWithNoMovies(CapturedOutput output) {
            Summary summary = LoaderLog.summary(output.getAll());
            assertThat(summary.read()).isEqualTo(3);
            assertThat(summary.accepted()).isZero();
            assertThat(summary.rejected()).isEqualTo(3);
            assertThat(jdbc.queryForObject("select count(*) from movie", Integer.class)).isZero();
        }

        @Test
        @DisplayName("FR-018 Edge arquivo sem linhas válidas")
        void emptyIntervals() {
            assertIntervals(List.of(), List.of());
        }
    }
}
