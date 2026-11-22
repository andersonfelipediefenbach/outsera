package com.outsera.awards.producer;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProducerRepository extends JpaRepository<Producer, Long> {

    /** Busca pelo nome exato, diferenciando maiúsculas (FR-008). */
    Optional<Producer> findByName(String name);
}
