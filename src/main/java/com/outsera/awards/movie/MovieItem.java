package com.outsera.awards.movie;

/**
 * Filme da listagem (FR-019). {@code studios} é nulo quando a coluna veio vazia (FR-006e), e
 * {@code producers} é o texto da coluna como no arquivo.
 */
public record MovieItem(int year, String title, String studios, String producers, boolean winner) {

    static MovieItem of(Movie movie) {
        return new MovieItem(movie.getYear(), movie.getTitle(), movie.getStudios(), movie.getProducersRaw(),
                movie.isWinner());
    }
}
