package com.outsera.awards.support;

import java.util.List;

/** Corpo de {@code GET /api/movies}, lido do JSON da resposta (FR-019, FR-021). */
public record MoviesBody(List<MovieJson> content, int page, int size, long totalElements, int totalPages) {

    /** Títulos da página, na ordem da resposta. */
    public List<String> titles() {
        return content.stream().map(MovieJson::title).toList();
    }

    /** O único filme da página com o título dado. */
    public MovieJson byTitle(String title) {
        List<MovieJson> found = content.stream().filter(m -> m.title().equals(title)).toList();
        if (found.size() != 1) {
            throw new AssertionError("esperado 1 filme com o título " + title + ", encontrados " + found.size());
        }
        return found.getFirst();
    }
}
