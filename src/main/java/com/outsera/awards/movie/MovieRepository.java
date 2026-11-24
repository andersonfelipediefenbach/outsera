package com.outsera.awards.movie;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MovieRepository extends JpaRepository<Movie, Long> {

    /**
     * O filme identificado por ano e título (spec 001, FR-006d), para a gravação com atualização (spec
     * 003, FR-014; research R5). Consulta derivada, portável entre H2 e PostgreSQL.
     */
    Optional<Movie> findByYearAndTitle(int year, String title);

    /**
     * Filmes com os filtros opcionais (nulo = sem filtro), ordenados por ano e título (FR-020, FR-022).
     * JPQL portável entre H2 e PostgreSQL. A ordem vem da consulta, e o {@code Pageable} não deve trazer
     * ordenação.
     */
    @Query(value = """
            select m from Movie m
            where (:year is null or m.year = :year)
              and (:winner is null or m.winner = :winner)
            order by m.year, m.title""",
            countQuery = """
            select count(m) from Movie m
            where (:year is null or m.year = :year)
              and (:winner is null or m.winner = :winner)""")
    Page<Movie> findFiltered(@Param("year") Integer year, @Param("winner") Boolean winner, Pageable pageable);

    /** Quantidade de filmes com os mesmos filtros de {@link #findFiltered}. */
    @Query("""
            select count(m) from Movie m
            where (:year is null or m.year = :year)
              and (:winner is null or m.winner = :winner)""")
    long countFiltered(@Param("year") Integer year, @Param("winner") Boolean winner);
}
