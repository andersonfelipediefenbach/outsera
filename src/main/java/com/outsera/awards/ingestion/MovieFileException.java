package com.outsera.awards.ingestion;

/**
 * Erro estrutural do arquivo de filmes. Interrompe a inicialização e é apresentado pelo
 * {@link MovieFileFailureAnalyzer} (FR-005, research R11).
 */
public class MovieFileException extends RuntimeException {

    /** Motivo do erro estrutural (data-model.md). */
    public enum Reason {
        NOT_FOUND,
        UNREADABLE,
        INVALID_SEPARATOR,
        INVALID_HEADER
    }

    private final Reason reason;
    private final String path;
    private final String foundHeader;
    private final String detail;

    public MovieFileException(Reason reason, String path, String foundHeader, String detail) {
        this(reason, path, foundHeader, detail, null);
    }

    public MovieFileException(Reason reason, String path, String foundHeader, String detail, Throwable cause) {
        super("Arquivo de filmes " + path + " inválido (" + reason + "): " + detail, cause);
        this.reason = reason;
        this.path = path;
        this.foundHeader = foundHeader;
        this.detail = detail;
    }

    public Reason getReason() {
        return reason;
    }

    public String getPath() {
        return path;
    }

    /** O cabeçalho lido do arquivo, com as colunas unidas por {@code ;}, ou nulo se não foi lido. */
    public String getFoundHeader() {
        return foundHeader;
    }

    /** Explicação legível do motivo. */
    public String getDetail() {
        return detail;
    }
}
