package com.outsera.awards.ingestion;

import java.util.List;

/**
 * Resultado de uma carga (FR-006b). Linhas em branco não contam como lidas (FR-005a).
 *
 * @param linesRead  linhas de dados lidas
 * @param accepted   linhas aceitas
 * @param rejected   linhas rejeitadas
 * @param rejections as rejeições, na ordem do arquivo
 */
public record LoadReport(int linesRead, int accepted, int rejected, List<Rejection> rejections) {

    public LoadReport {
        rejections = List.copyOf(rejections);
    }
}
