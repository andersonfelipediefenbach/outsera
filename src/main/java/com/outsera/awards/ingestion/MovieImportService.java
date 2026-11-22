package com.outsera.awards.ingestion;

import com.outsera.awards.movie.Movie;
import com.outsera.awards.movie.MovieRepository;
import com.outsera.awards.producer.Producer;
import com.outsera.awards.producer.ProducerRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Serviço único de gravação de filmes (Princípio V). A carga do arquivo usa este serviço, e no Marco 3
 * o consumidor Kafka vai usar o mesmo: o perfil muda só a origem dos dados.
 */
@Service
public class MovieImportService {

    private final MovieRepository movies;
    private final ProducerRepository producers;

    public MovieImportService(MovieRepository movies, ProducerRepository producers) {
        this.movies = movies;
        this.producers = producers;
    }

    /** Grava os filmes e os vínculos com os produtores, criando os produtores que ainda não existem. */
    @Transactional
    public void importMovies(List<MovieRow> rows) {
        Map<String, Producer> byName = new HashMap<>();
        for (MovieRow row : rows) {
            List<Producer> movieProducers = row.producerNames().stream()
                    .map(name -> byName.computeIfAbsent(name, this::findOrCreate))
                    .toList();
            movies.save(new Movie(row.year(), row.title(), row.studios(), row.producersRaw(), row.winner(),
                    movieProducers));
        }
    }

    private Producer findOrCreate(String name) {
        return producers.findByName(name).orElseGet(() -> producers.save(new Producer(name)));
    }
}
