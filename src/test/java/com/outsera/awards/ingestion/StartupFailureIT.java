package com.outsera.awards.ingestion;

import static org.assertj.core.api.Assertions.assertThat;

import com.outsera.awards.support.ApplicationStarter;
import com.outsera.awards.support.ApplicationStarter.Result;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Erros estruturais do arquivo impedem a aplicação de subir, com uma mensagem que identifica o arquivo
 * e o motivo (FR-005, SC-006). Cada teste executa a aplicação real com {@code SpringApplication}
 * (Princípio I, falha de inicialização), sem mocks.
 */
class StartupFailureIT {

    private static final String EXPECTED_HEADER = "year;title;studios;producers;winner";

    @Test
    @DisplayName("FR-005 SC-006 arquivo inexistente")
    void missingFile() {
        Result result = assertStartupFails("file:./nao-existe.csv", "NOT_FOUND");
        assertThat(result.output()).contains("file:./nao-existe.csv");
    }

    @Test
    @DisplayName("FR-005 arquivo vazio")
    void emptyFile() {
        assertStartupFails("classpath:scenarios/empty-file.csv", "UNREADABLE");
    }

    @Test
    @DisplayName("FR-005 aspas sem fechamento")
    void unterminatedQuote() {
        assertStartupFails("classpath:scenarios/unterminated-quote.csv", "UNREADABLE");
    }

    @Test
    @DisplayName("FR-005 arquivo fora de UTF-8")
    void invalidEncoding() {
        Result result = assertStartupFails("classpath:scenarios/invalid-encoding.csv", "UNREADABLE");
        assertThat(result.output()).contains("UTF-8");
    }

    @Test
    @DisplayName("FR-005 separador inválido")
    void commaSeparator() {
        Result result = assertStartupFails("classpath:scenarios/comma-separated.csv", "INVALID_SEPARATOR");
        assertThat(result.output()).contains("\";\"");
    }

    @Test
    @DisplayName("FR-003 FR-005 cabeçalho com as colunas em outra ordem")
    void headerWrongOrder() {
        assertInvalidHeader("classpath:scenarios/header-wrong-order.csv", "title;year;studios;producers;winner");
    }

    @Test
    @DisplayName("FR-003 FR-005 cabeçalho com coluna faltando")
    void headerMissingColumn() {
        assertInvalidHeader("classpath:scenarios/header-missing-column.csv", "year;title;studios;producers");
    }

    @Test
    @DisplayName("FR-003 FR-005 cabeçalho com coluna a mais")
    void headerExtraColumn() {
        assertInvalidHeader("classpath:scenarios/header-extra-column.csv",
                "year;title;studios;producers;winner;extra");
    }

    private static void assertInvalidHeader(String csvPath, String foundHeader) {
        Result result = assertStartupFails(csvPath, "INVALID_HEADER");
        assertThat(result.output()).contains(EXPECTED_HEADER).contains(foundHeader);
    }

    /**
     * A inicialização falha, o {@code FailureAnalyzer} gera o bloco "APPLICATION FAILED TO START" e a
     * mensagem cita o caminho do arquivo e o motivo.
     */
    private static Result assertStartupFails(String csvPath, String reason) {
        Result result = ApplicationStarter.start(csvPath);
        assertThat(result.failed()).as("a aplicação não deveria subir com %s", csvPath).isTrue();
        assertThat(result.output())
                .contains("APPLICATION FAILED TO START")
                .contains(csvPath)
                .contains(reason);
        return result;
    }
}
