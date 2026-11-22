package com.outsera.awards.ingestion;

import org.springframework.boot.diagnostics.AbstractFailureAnalyzer;
import org.springframework.boot.diagnostics.FailureAnalysis;

/**
 * Converte o {@link MovieFileException} no bloco "APPLICATION FAILED TO START", com uma mensagem que
 * permite achar a causa sem consultar o código (FR-005, SC-006, research R11). Registrado em
 * {@code META-INF/spring.factories}.
 */
public class MovieFileFailureAnalyzer extends AbstractFailureAnalyzer<MovieFileException> {

    @Override
    protected FailureAnalysis analyze(Throwable rootFailure, MovieFileException cause) {
        String foundHeader = cause.getFoundHeader() == null ? "(não lido)" : cause.getFoundHeader();
        String description = """
                Não foi possível carregar o arquivo de filmes.

                    Arquivo: %s
                    Motivo: %s
                    Detalhe: %s
                    Cabeçalho encontrado: %s""".formatted(
                cause.getPath(), cause.getReason(), cause.getDetail(), foundHeader);
        String action = """
                Informe um arquivo CSV existente, em UTF-8, com o separador ";" e o cabeçalho \
                "%s", nessa ordem. O caminho do arquivo é configurado pela variável de ambiente \
                MOVIES_CSV_PATH (propriedade movies.csv.path).""".formatted(MovieCsvReader.EXPECTED_HEADER_TEXT);
        return new FailureAnalysis(description, action, cause);
    }
}
