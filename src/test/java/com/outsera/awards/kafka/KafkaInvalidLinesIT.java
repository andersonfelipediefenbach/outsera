package com.outsera.awards.kafka;

import static org.assertj.core.api.Assertions.assertThat;

import com.outsera.awards.support.KafkaScenarioIT;
import com.outsera.awards.support.KafkaTestSupport;
import com.outsera.awards.support.LoaderLog;
import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.test.context.TestPropertySource;

/**
 * Com o perfil {@code kafka}, o publicador lê o arquivo com as mesmas regras do Marco 1: as linhas
 * inválidas não são publicadas e geram os mesmos avisos, e o resumo da publicação traz as contagens
 * (spec 003, FR-009 e US1-4). Os pares linha e motivo são os do {@code InvalidLinesIT}.
 */
@TestPropertySource(properties = "movies.csv.path=classpath:scenarios/invalid-lines.csv")
@Tag("log")
class KafkaInvalidLinesIT extends KafkaScenarioIT {

    @ParameterizedTest(name = "S003 FR-009 US1-4 linha {0} rejeitada: {1}")
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
        awaitReady();
        assertThat(LoaderLog.loaderWarnings(output.getAll()))
                .anyMatch(warning -> warning.endsWith("Linha " + line + " rejeitada: " + reason));
    }

    @Test
    @DisplayName("S003 FR-009 US1-4 só as linhas aceitas são publicadas, e a aplicação fica pronta")
    void onlyAcceptedLinesArePublished() {
        awaitReady();
        // O resumo da publicação sai num fio próprio, durante o primeiro teste da classe, e a captura de
        // saída de cada teste é descartada no fim dele. O resumo é verificado pelo PublisherSummaryIT,
        // que tem um único teste. Aqui, a prova é o próprio tópico.
        // Dois filmes e o evento de carga concluída.
        assertThat(KafkaTestSupport.read(brokers(), MOVIES_TOPIC, 3, Duration.ofSeconds(20))).hasSize(3);
        assertThat(allMovies().titles()).containsExactly("Filme Válido 1", "Filme Válido 2");
    }
}
