package com.outsera.awards.ingestion;

import static org.assertj.core.api.Assertions.assertThat;

import com.outsera.awards.support.LoaderLog;
import com.outsera.awards.support.LoaderLog.Summary;
import com.outsera.awards.support.ScenarioIT;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.test.context.TestPropertySource;

/**
 * Uma linha inválida é ignorada com aviso, e a carga continua (FR-006, FR-006a). Há um caso por linha
 * inválida do {@code invalid-lines.csv} (FR-006c). A linha 7 está em branco, para provar que a
 * numeração é a da linha física (cabeçalho = 1).
 */
@TestPropertySource(properties = "movies.csv.path=classpath:scenarios/invalid-lines.csv")
class InvalidLinesIT extends ScenarioIT {

    @ParameterizedTest(name = "FR-006 FR-006a linha {0} rejeitada: {1}")
    @CsvSource(delimiter = '|', textBlock = """
            4  | número de colunas diferente de 5 (encontradas 4)
            5  | número de colunas diferente de 5 (encontradas 6)
            6  | ano vazio
            8  | ano não numérico (19x0)
            9  | ano não numérico (-1990)
            10 | título vazio
            11 | produtores vazios
            12 | produtores vazios
            13 | winner diferente de yes ou vazio (no)
            14 | ano fora da faixa de 1 a 9999 (0)
            15 | ano fora da faixa de 1 a 9999 (10000)
            """)
    void invalidLineIsRejectedWithWarning(int line, String reason, CapturedOutput output) {
        assertThat(LoaderLog.loaderWarnings(output.getAll()))
                .anyMatch(warning -> warning.endsWith("Linha " + line + " rejeitada: " + reason));
    }

    @Test
    @DisplayName("FR-006 a carga continua")
    void loadContinues(CapturedOutput output) {
        Summary summary = LoaderLog.summary(output.getAll());
        assertThat(summary.read()).isEqualTo(13);
        assertThat(summary.accepted()).isEqualTo(2);
        assertThat(summary.rejected()).isEqualTo(11);
        assertThat(LoaderLog.loaderWarnings(output.getAll())).hasSize(11);
        assertThat(allMovies().titles()).containsExactly("Filme Válido 1", "Filme Válido 2");
    }
}
