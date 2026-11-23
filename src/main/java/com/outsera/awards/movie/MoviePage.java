package com.outsera.awards.movie;

import java.util.List;

/**
 * Página da listagem (FR-019, FR-021), com os campos do contrato. Não serializa o {@code Page} do Spring,
 * cujo formato JSON não é estável (research R7).
 */
public record MoviePage(List<MovieItem> content, int page, int size, long totalElements, int totalPages) {

    static MoviePage of(List<MovieItem> content, int page, int size, long totalElements) {
        int totalPages = (int) ((totalElements + size - 1) / size);
        return new MoviePage(List.copyOf(content), page, size, totalElements, totalPages);
    }
}
