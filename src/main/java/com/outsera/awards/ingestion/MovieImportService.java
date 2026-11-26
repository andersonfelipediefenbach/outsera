package com.outsera.awards.ingestion;

import com.outsera.awards.movie.Movie;
import com.outsera.awards.movie.MovieRepository;
import com.outsera.awards.producer.Producer;
import com.outsera.awards.producer.ProducerRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.FlushModeType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Serviço único de gravação de filmes (Princípio V). A carga direta do arquivo e o consumidor Kafka usam
 * este serviço: o perfil muda só a origem dos dados.
 *
 * <p>A gravação é idempotente por ano e título, com atualização (spec 003, FR-014; research R5): um filme
 * que ainda não existe é criado, e um que já existe passa a ter os valores da gravação nova. Gravar a
 * mesma linha de novo não muda nada, e um reinício sobre um banco persistente não duplica filmes.
 */
@Service
public class MovieImportService {

    private final MovieRepository movies;
    private final ProducerRepository producers;
    private final EntityManager entityManager;

    public MovieImportService(MovieRepository movies, ProducerRepository producers, EntityManager entityManager) {
        this.movies = movies;
        this.producers = producers;
        this.entityManager = entityManager;
    }

    /** Carga direta: grava todas as linhas numa única transação, como no Marco 1. */
    @Transactional
    public void importMovies(List<MovieRow> rows) {
        // O leitor já deduplica os filmes, e produtores criados na carga ficam no mapa local.
        // As consultas não precisam sincronizar as alterações a cada linha.
        FlushModeType previousFlushMode = entityManager.getFlushMode();
        entityManager.setFlushMode(FlushModeType.COMMIT);
        try {
            Map<String, Producer> byName = new HashMap<>();
            rows.forEach(row -> record(row, byName));
        } finally {
            entityManager.setFlushMode(previousFlushMode);
        }
    }

    /** Cria ou atualiza o filme da linha, criando os produtores que ainda não existem. */
    @Transactional
    public void record(MovieRow row) {
        record(row, new HashMap<>());
    }

    /** {@code byName} guarda os produtores já resolvidos na mesma transação. */
    private void record(MovieRow row, Map<String, Producer> byName) {
        List<Producer> movieProducers = row.producerNames().stream()
                .map(name -> byName.computeIfAbsent(name, this::findOrCreate))
                .toList();
        movies.findByYearAndTitle(row.year(), row.title()).ifPresentOrElse(
                movie -> movie.update(row.studios(), row.producersRaw(), row.winner(), movieProducers),
                () -> movies.save(new Movie(row.year(), row.title(), row.studios(), row.producersRaw(),
                        row.winner(), movieProducers)));
    }

    private Producer findOrCreate(String name) {
        return producers.findByName(name).orElseGet(() -> producers.save(new Producer(name)));
    }
}
