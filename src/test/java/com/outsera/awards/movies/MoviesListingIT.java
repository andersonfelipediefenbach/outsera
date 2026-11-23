package com.outsera.awards.movies;

import static org.assertj.core.api.Assertions.assertThat;

import com.outsera.awards.support.MovieJson;
import com.outsera.awards.support.MoviesBody;
import com.outsera.awards.support.ScenarioIT;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.context.TestPropertySource;

/**
 * Listagem paginada de filmes, com filtros por ano e vencedor (FR-019 a FR-022). O
 * {@code listing-sample.csv} tem 25 filmes de 1981 a 1985, 5 por ano e 1 vencedor por ano, com as
 * linhas fora de ordem no arquivo.
 */
@TestPropertySource(properties = "movies.csv.path=classpath:scenarios/listing-sample.csv")
class MoviesListingIT extends ScenarioIT {

    /** Ordem esperada (ano, depois título). Os títulos começam por letras distintas, sem depender de collation. */
    private static final List<String> ORDERED_TITLES = List.of(
            "Amber Sky", "Broken Arrow", "Crimson Tide", "Dark Water", "Empty Room",
            "Fallen Star", "Golden Gate", "Hidden Path", "Iron Will", "Jade Moon",
            "Kings Road", "Lost City", "Midnight Run", "Northern Lights", "Open Sea",
            "Paper Moon", "Quiet Storm", "Red River", "Silent Hill", "Twin Peaks",
            "Under Fire", "Velvet Rope", "Winter Night", "Yellow Bird", "Zé Ninguém");

    @Test
    @DisplayName("FR-019 FR-021 página padrão")
    void defaultPage() {
        MoviesBody body = movies("");
        assertThat(body.content()).hasSize(20);
        assertThat(body.page()).isZero();
        assertThat(body.size()).isEqualTo(20);
        assertThat(body.totalElements()).isEqualTo(25);
        assertThat(body.totalPages()).isEqualTo(2);
    }

    @Test
    @DisplayName("FR-021 segunda página")
    void secondPage() {
        MoviesBody body = movies("page=1");
        assertThat(body.content()).hasSize(5);
        assertThat(body.page()).isEqualTo(1);
        assertThat(body.titles()).containsExactlyElementsOf(ORDERED_TITLES.subList(20, 25));
    }

    @Test
    @DisplayName("FR-021 tamanho de página informado")
    void customSize() {
        MoviesBody body = movies("page=2&size=7");
        assertThat(body.titles()).containsExactlyElementsOf(ORDERED_TITLES.subList(14, 21));
        assertThat(body.size()).isEqualTo(7);
        assertThat(body.totalPages()).isEqualTo(4);
    }

    @Test
    @DisplayName("FR-021 Edge página além da última")
    void pageBeyondTheLast() {
        for (String page : List.of("2", "2147483647")) {
            MoviesBody body = movies("page=" + page);
            assertThat(body.content()).as("page=%s", page).isEmpty();
            assertThat(body.page()).as("page=%s", page).isEqualTo(Integer.parseInt(page));
            assertThat(body.totalElements()).as("page=%s", page).isEqualTo(25);
            assertThat(body.totalPages()).as("page=%s", page).isEqualTo(2);
        }
    }

    @Test
    @DisplayName("FR-020 filtro por ano")
    void filterByYear() {
        MoviesBody body = movies("year=1983");
        assertThat(body.content()).hasSize(5).allSatisfy(m -> assertThat(m.year()).isEqualTo(1983));
        assertThat(body.totalElements()).isEqualTo(5);
    }

    @Test
    @DisplayName("FR-020 filtro por vencedor")
    void filterByWinner() {
        MoviesBody winners = movies("winner=true");
        assertThat(winners.content()).hasSize(5).allSatisfy(m -> assertThat(m.winner()).isTrue());
        assertThat(winners.titles())
                .containsExactly("Amber Sky", "Golden Gate", "Midnight Run", "Silent Hill", "Yellow Bird");

        MoviesBody others = movies("winner=false&size=100");
        assertThat(others.content()).hasSize(20).allSatisfy(m -> assertThat(m.winner()).isFalse());
        assertThat(others.totalElements()).isEqualTo(20);
    }

    @Test
    @DisplayName("FR-020 filtro por vencedor sem diferenciar maiúsculas")
    void winnerIgnoresCase() {
        assertThat(movies("winner=TRUE").totalElements()).isEqualTo(5);
        assertThat(movies("winner=False").totalElements()).isEqualTo(20);
    }

    @Test
    @DisplayName("FR-020 filtros combinados")
    void combinedFilters() {
        MoviesBody body = movies("year=1983&winner=true");
        assertThat(body.titles()).containsExactly("Midnight Run");
        assertThat(body.totalElements()).isEqualTo(1);
        assertThat(body.totalPages()).isEqualTo(1);
    }

    @Test
    @DisplayName("FR-022 ordenação ano e título")
    void orderedByYearThenTitle() {
        MoviesBody body = allMovies();
        assertThat(body.titles()).containsExactlyElementsOf(ORDERED_TITLES);
        assertThat(body.content()).extracting(MovieJson::year).isSorted();
    }

    @Test
    @DisplayName("US3-6 filtro sem resultado")
    void filterWithoutResults() {
        MoviesBody body = movies("year=1900");
        assertThat(body.content()).isEmpty();
        assertThat(body.totalElements()).isZero();
        assertThat(body.totalPages()).isZero();
    }

    @Test
    @DisplayName("FR-006e estúdios vazio vira null")
    void emptyStudiosIsNull() {
        Map<String, Object> body = http.get().uri(MOVIES + "?year=1981").exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<Map<String, Object>>() { })
                .returnResult()
                .getResponseBody();
        List<Map<String, Object>> content = asItems(body.get("content"));
        Map<String, Object> darkWater = content.stream()
                .filter(m -> "Dark Water".equals(m.get("title")))
                .findFirst()
                .orElseThrow();
        // O campo existe e vale null: nem string vazia, nem ausente (contrato: studios é obrigatório).
        assertThat(darkWater).containsEntry("studios", null);
        assertThat(content).filteredOn(m -> m != darkWater)
                .hasSize(4)
                .allSatisfy(m -> assertThat(m.get("studios")).isEqualTo("Studio A"));
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> asItems(Object content) {
        return (List<Map<String, Object>>) content;
    }

    @Test
    @DisplayName("FR-019 produtores e estúdios como texto do arquivo")
    void producersAndStudiosAsInTheFile() {
        MovieJson movie = allMovies().byTitle("Golden Gate");
        assertThat(movie.year()).isEqualTo(1982);
        assertThat(movie.studios()).isEqualTo("Studio North, Studio South");
        assertThat(movie.producers()).isEqualTo("Producer One, Producer Two and Producer Three");
        assertThat(movie.winner()).isTrue();
    }

    @Test
    @DisplayName("Edge UTF-8 preservado")
    void utf8IsPreserved() {
        MovieJson movie = allMovies().byTitle("Zé Ninguém");
        assertThat(movie.title()).isEqualTo("Zé Ninguém");
        assertThat(movie.year()).isEqualTo(1985);
    }
}
