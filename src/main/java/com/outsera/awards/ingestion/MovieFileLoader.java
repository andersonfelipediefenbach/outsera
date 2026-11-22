package com.outsera.awards.ingestion;

import com.outsera.awards.ingestion.MovieFileException.Reason;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

/**
 * Carrega o arquivo de filmes na inicialização (FR-001).
 *
 * <p>Roda em {@link #afterSingletonsInstantiated()}: depois do Flyway e dos repositórios, e antes de o
 * servidor web começar a aceitar conexões (research R2). Uma exceção aqui interrompe a inicialização.
 */
@Component
public class MovieFileLoader implements SmartInitializingSingleton {

    private static final Logger log = LoggerFactory.getLogger(MovieFileLoader.class);

    private final MovieFileProperties properties;
    private final MovieCsvReader reader;
    private final MovieImportService importService;

    public MovieFileLoader(MovieFileProperties properties, MovieCsvReader reader,
            MovieImportService importService) {
        this.properties = properties;
        this.reader = reader;
        this.importService = importService;
    }

    @Override
    public void afterSingletonsInstantiated() {
        String path = properties.path();
        Resource resource = properties.resource();
        if (!resource.exists()) {
            throw new MovieFileException(Reason.NOT_FOUND, path, null, "o arquivo não existe");
        }
        if (!resource.isReadable()) {
            throw new MovieFileException(Reason.UNREADABLE, path, null, "o caminho não é um arquivo legível");
        }

        MovieCsvReader.Result result = reader.read(resource, path);
        LoadReport report = result.report();
        for (Rejection rejection : report.rejections()) {
            log.warn("Linha {} rejeitada: {}", rejection.lineNumber(), rejection.describe());
        }
        importService.importMovies(result.rows());
        log.info("Carga do arquivo {} concluída: {} linhas lidas, {} aceitas, {} rejeitadas",
                path, report.linesRead(), report.accepted(), report.rejected());
    }
}
