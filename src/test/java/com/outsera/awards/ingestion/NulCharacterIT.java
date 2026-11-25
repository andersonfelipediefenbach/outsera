package com.outsera.awards.ingestion;

import static org.assertj.core.api.Assertions.assertThat;

import com.outsera.awards.support.IntervalItem;
import com.outsera.awards.support.LoaderLog;
import com.outsera.awards.support.LoaderLog.Summary;
import com.outsera.awards.support.ScenarioIT;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.test.context.TestPropertySource;

/**
 * Uma linha com o caractere nulo (U+0000) no título, nos estúdios ou nos produtores é ignorada com aviso,
 * e a carga continua (spec 001, FR-006 e FR-006c, motivo {@code NUL_CHARACTER}, acrescentado pela spec 003
 * a partir de um apontamento do Codex no PR do Marco 3). O PostgreSQL não aceita esse caractere em texto, e
 * a mesma regra vale para os eventos do Kafka (spec 003, FR-012).
 */
@TestPropertySource(properties = "movies.csv.path=classpath:scenarios/nul-character.csv")
@Tag("log")
class NulCharacterIT extends ScenarioIT {

    @ParameterizedTest(name = "S003 FR-012 001 FR-006 linha {0} rejeitada: {1}")
    @CsvSource(delimiter = '|', textBlock = """
            3 | um campo contém o caractere nulo (U+0000) (título)
            4 | um campo contém o caractere nulo (U+0000) (estúdios)
            5 | um campo contém o caractere nulo (U+0000) (produtores)
            """)
    void nulCharacterIsRejectedWithWarning(int line, String reason, CapturedOutput output) {
        assertThat(LoaderLog.loaderWarnings(output.getAll()))
                .anyMatch(warning -> warning.endsWith("Linha " + line + " rejeitada: " + reason));
    }

    @Test
    @DisplayName("S003 FR-012 001 FR-006 a carga continua sem as linhas com o caractere nulo")
    void loadContinues(CapturedOutput output) {
        Summary summary = LoaderLog.summary(output.getAll());
        assertThat(summary.read()).isEqualTo(5);
        assertThat(summary.accepted()).isEqualTo(2);
        assertThat(summary.rejected()).isEqualTo(3);
        assertThat(allMovies().titles()).containsExactly("Valido Um", "Valido Dois");
        assertIntervals(
                List.of(new IntervalItem("Producer Um", 4, 2001, 2005)),
                List.of(new IntervalItem("Producer Um", 4, 2001, 2005)));
    }
}
