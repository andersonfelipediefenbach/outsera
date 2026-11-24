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
 * Uma linha com um campo maior que a coluna do esquema é ignorada com aviso, como as outras linhas
 * inválidas, e a carga continua (spec 001, FR-006 e FR-006c, motivo {@code FIELD_TOO_LONG}, acrescentado
 * pela spec 003 para que a carga direta e a ingestão por Kafka usem a mesma validação; spec 003, FR-018).
 */
@TestPropertySource(properties = "movies.csv.path=classpath:scenarios/field-too-long.csv")
@Tag("log")
class FieldTooLongIT extends ScenarioIT {

    @ParameterizedTest(name = "S003 FR-018 001 FR-006 linha {0} rejeitada: {1}")
    @CsvSource(delimiter = '|', textBlock = """
            4 | um campo excede o tamanho da coluna (título com 501 caracteres; máximo 500)
            5 | um campo excede o tamanho da coluna (estúdios com 1001 caracteres; máximo 1000)
            6 | um campo excede o tamanho da coluna (produtores com 1002 caracteres; máximo 1000)
            7 | um campo excede o tamanho da coluna (nome de produtor com 501 caracteres; máximo 500)
            """)
    void longFieldIsRejectedWithWarning(int line, String reason, CapturedOutput output) {
        assertThat(LoaderLog.loaderWarnings(output.getAll()))
                .anyMatch(warning -> warning.endsWith("Linha " + line + " rejeitada: " + reason));
    }

    @Test
    @DisplayName("S003 FR-018 001 FR-006 a carga continua, e um título de exatamente 500 caracteres é aceito")
    void loadContinues(CapturedOutput output) {
        Summary summary = LoaderLog.summary(output.getAll());
        assertThat(summary.read()).isEqualTo(7);
        assertThat(summary.accepted()).isEqualTo(3);
        assertThat(summary.rejected()).isEqualTo(4);
        assertThat(allMovies().titles()).containsExactly("Valido Um", "Valido Dois", "X".repeat(500));
        assertIntervals(
                List.of(new IntervalItem("Producer Um", 6, 2001, 2007)),
                List.of(new IntervalItem("Producer Um", 6, 2001, 2007)));
    }
}
