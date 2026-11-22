package com.outsera.awards.producer;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Produtor, identificado pelo nome exatamente como escrito no arquivo, depois do {@code trim}. A
 * comparação diferencia maiúsculas e não corrige grafia (FR-008).
 */
@Entity
@Table(name = "producer")
public class Producer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true, length = 500)
    private String name;

    protected Producer() {
        // Exigido pelo JPA.
    }

    public Producer(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
