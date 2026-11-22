package com.outsera.awards.producer;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProducerRepository extends JpaRepository<Producer, Long> {

    /** Busca pelo nome exato, diferenciando maiúsculas (FR-008). */
    Optional<Producer> findByName(String name);

    /**
     * Uma linha por produtor de cada filme vencedor (FR-011). Dois filmes do mesmo produtor no mesmo ano
     * dão duas linhas (FR-013). JPQL portável entre H2 e PostgreSQL.
     */
    @Query("""
            select new com.outsera.awards.producer.ProducerWin(p.name, m.year)
            from Movie m join m.producers p
            where m.winner = true""")
    List<ProducerWin> findWins();
}
