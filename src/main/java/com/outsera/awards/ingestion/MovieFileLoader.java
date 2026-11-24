package com.outsera.awards.ingestion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Carga direta do arquivo de filmes na inicialização (FR-001), no modo padrão e no perfil
 * {@code postgres}. Com o perfil {@code kafka}, ela não roda: o tópico é o único caminho de entrada dos
 * dados (spec 003, FR-009 e US1-6).
 *
 * <p>Roda em {@link #afterSingletonsInstantiated()}: depois do Flyway e dos repositórios, e antes de o
 * servidor web começar a aceitar conexões (research R2). Uma exceção aqui interrompe a inicialização.
 */
@Component
@Profile("!kafka")
public class MovieFileLoader implements SmartInitializingSingleton {

    private static final Logger log = LoggerFactory.getLogger(MovieFileLoader.class);

    private final MovieFileReading reading;
    private final MovieImportService importService;

    public MovieFileLoader(MovieFileReading reading, MovieImportService importService) {
        this.reading = reading;
        this.importService = importService;
    }

    @Override
    public void afterSingletonsInstantiated() {
        MovieCsvReader.Result result = reading.read();
        LoadReport report = result.report();
        importService.importMovies(result.rows());
        log.info("Carga do arquivo {} concluída: {} linhas lidas, {} aceitas, {} rejeitadas",
                reading.path(), report.linesRead(), report.accepted(), report.rejected());
    }
}
