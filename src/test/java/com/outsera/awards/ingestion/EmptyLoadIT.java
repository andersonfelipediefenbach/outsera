package com.outsera.awards.ingestion;

import static org.assertj.core.api.Assertions.assertThat;

import com.outsera.awards.support.LoaderLog;
import com.outsera.awards.support.LoaderLog.Summary;
import com.outsera.awards.support.MoviesBody;
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
        @DisplayName("Edge a aplicação sobe, sem linhas lidas e com a listagem vazia")
        void startsWithNoMovies(CapturedOutput output) {
            Summary summary = LoaderLog.summary(output.getAll());
            assertThat(summary.read()).isZero();
            assertThat(summary.accepted()).isZero();
            assertNoMovies(movies(""));
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
        @DisplayName("Edge a aplicação sobe, com 0 aceitas e a listagem vazia")
        void startsWithNoMovies(CapturedOutput output) {
            Summary summary = LoaderLog.summary(output.getAll());
            assertThat(summary.read()).isEqualTo(3);
            assertThat(summary.accepted()).isZero();
            assertThat(summary.rejected()).isEqualTo(3);
            assertNoMovies(movies(""));
        }

        @Test
        @DisplayName("FR-018 Edge arquivo sem linhas válidas")
        void emptyIntervals() {
            assertIntervals(List.of(), List.of());
        }
    }

    /** A listagem responde 200, com {@code content} vazio e {@code totalElements} 0. */
    private static void assertNoMovies(MoviesBody movies) {
        assertThat(movies.content()).isEmpty();
        assertThat(movies.totalElements()).isZero();
    }
}
