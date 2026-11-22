package com.outsera.awards.ingestion;

import java.util.List;

/**
 * Uma linha válida do arquivo, pronta para ser gravada pelo {@link MovieImportService}.
 *
 * @param lineNumber    linha física no arquivo
 * @param year          ano do filme
 * @param title         título, sem espaços em volta
 * @param studios       estúdios, ou nulo quando a coluna vem vazia (FR-006e)
 * @param producersRaw  texto da coluna de produtores, sem espaços em volta
 * @param producerNames nomes já separados, sem vazios e sem repetidos (FR-007 a FR-010)
 * @param winner        {@code true} quando {@code winner} é {@code yes} (FR-004)
 */
public record MovieRow(long lineNumber, int year, String title, String studios, String producersRaw,
        List<String> producerNames, boolean winner) {

    public MovieRow {
        producerNames = List.copyOf(producerNames);
    }
}
