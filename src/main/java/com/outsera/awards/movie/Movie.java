package com.outsera.awards.movie;

import com.outsera.awards.producer.Producer;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/** Filme do arquivo, identificado por ano + título (FR-006d). */
@Entity
@Table(name = "movie")
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "release_year", nullable = false)
    private int year;

    @Column(name = "title", nullable = false, length = 500)
    private String title;

    /** Opcional (FR-006e): nulo quando a coluna vem vazia. */
    @Column(name = "studios", length = 1000)
    private String studios;

    /** Texto da coluna de produtores como no arquivo, depois do {@code trim}. */
    @Column(name = "producers", nullable = false, length = 1000)
    private String producersRaw;

    @Column(name = "winner", nullable = false)
    private boolean winner;

    /** Em um {@code Set}, para que um produtor repetido no mesmo filme gere um único vínculo (FR-010). */
    @ManyToMany
    @JoinTable(name = "movie_producer",
            joinColumns = @JoinColumn(name = "movie_id"),
            inverseJoinColumns = @JoinColumn(name = "producer_id"))
    private Set<Producer> producers = new LinkedHashSet<>();

    protected Movie() {
        // Exigido pelo JPA.
    }

    public Movie(int year, String title, String studios, String producersRaw, boolean winner,
            Collection<Producer> producers) {
        this.year = year;
        this.title = title;
        this.studios = studios;
        this.producersRaw = producersRaw;
        this.winner = winner;
        this.producers.addAll(producers);
    }

    public Long getId() {
        return id;
    }

    public int getYear() {
        return year;
    }

    public String getTitle() {
        return title;
    }

    public String getStudios() {
        return studios;
    }

    public String getProducersRaw() {
        return producersRaw;
    }

    public boolean isWinner() {
        return winner;
    }

    public Set<Producer> getProducers() {
        return Collections.unmodifiableSet(producers);
    }
}
