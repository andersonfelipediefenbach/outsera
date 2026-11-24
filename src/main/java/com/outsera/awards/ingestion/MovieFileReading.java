package com.outsera.awards.ingestion;

import com.outsera.awards.ingestion.MovieFileException.Reason;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

/**
 * Leitura do arquivo de filmes, comum à carga direta e ao publicador do Kafka (spec 003, FR-009; research
 * R8): verifica o arquivo, lê com o {@link MovieCsvReader} e registra um aviso por linha rejeitada. Os
 * erros estruturais lançam {@link MovieFileException}, que interrompe a subida (spec 001, FR-005).
 *
 * <p>Os avisos saem com o logger do {@link MovieFileLoader}, como no Marco 1, nos dois caminhos de carga.
 */
@Component
public class MovieFileReading {

    private static final Logger log = LoggerFactory.getLogger(MovieFileLoader.class);

    private final MovieFileProperties properties;
    private final MovieCsvReader reader;

    public MovieFileReading(MovieFileProperties properties, MovieCsvReader reader) {
        this.properties = properties;
        this.reader = reader;
    }

    /** O caminho como foi configurado, para as mensagens. */
    public String path() {
        return properties.path();
    }

    /** Lê o arquivo inteiro e registra os avisos das linhas rejeitadas. */
    public MovieCsvReader.Result read() {
        String path = properties.path();
        Resource resource = properties.resource();
        if (!resource.exists()) {
            throw new MovieFileException(Reason.NOT_FOUND, path, null, "o arquivo não existe");
        }
        if (!resource.isReadable()) {
            throw new MovieFileException(Reason.UNREADABLE, path, null, "o caminho não é um arquivo legível");
        }
        MovieCsvReader.Result result = reader.read(resource, path);
        for (Rejection rejection : result.report().rejections()) {
            log.warn("Linha {} rejeitada: {}", rejection.lineNumber(), rejection.describe());
        }
        return result;
    }
}
