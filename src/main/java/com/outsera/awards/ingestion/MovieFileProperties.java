package com.outsera.awards.ingestion;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

/**
 * Caminho do arquivo de filmes ({@code movies.csv.path}, variável {@code MOVIES_CSV_PATH}; FR-002).
 *
 * @param path valor configurado, como foi escrito
 */
@ConfigurationProperties("movies.csv")
public record MovieFileProperties(String path) {

    private static final String CLASSPATH_PREFIX = "classpath:";
    private static final String FILE_PREFIX = "file:";

    /**
     * Resolve o recurso (research R10). Os prefixos {@code classpath:} e {@code file:} são respeitados.
     * Um valor sem prefixo é um caminho no sistema de arquivos, e não um recurso do servlet context.
     */
    public Resource resource() {
        if (path.startsWith(CLASSPATH_PREFIX) || path.startsWith(FILE_PREFIX)) {
            return new DefaultResourceLoader().getResource(path);
        }
        return new FileSystemResource(path);
    }
}
