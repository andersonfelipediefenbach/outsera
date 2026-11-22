package com.outsera.awards.support;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Lê, na saída capturada, as linhas emitidas pelo carregador do CSV.
 *
 * <p>Só as linhas do logger {@code MovieFileLoader} (pacote {@code com.outsera.awards.ingestion})
 * contam. Avisos de outras bibliotecas, como o do Flyway sobre a versão do H2, ficam de fora (decisão de
 * 2026-11-22 em {@code docs/ai/decisoes.md}). O teste não importa a classe de produção, para compilar
 * antes da implementação: o logger é reconhecido pelo nome no texto do log.
 */
public final class LoaderLog {

    private static final String LOADER_LOGGER = "MovieFileLoader";

    private static final Pattern SUMMARY = Pattern.compile(
            "Carga do arquivo (\\S+) concluída: (\\d+) linhas lidas, (\\d+) aceitas, (\\d+) rejeitadas");

    private LoaderLog() {
    }

    /** O resumo da carga, lido da única linha {@code Carga do arquivo ... concluída}. */
    public record Summary(String path, int read, int accepted, int rejected) {
    }

    /** Linhas de nível {@code WARN} emitidas pelo carregador do CSV. */
    public static List<String> loaderWarnings(String output) {
        return output.lines()
                .filter(line -> line.contains(" WARN ") && line.contains(LOADER_LOGGER))
                .toList();
    }

    /** O resumo da carga. Falha se a saída não tiver a linha de resumo. */
    public static Summary summary(String output) {
        Matcher matcher = SUMMARY.matcher(output);
        if (!matcher.find()) {
            throw new AssertionError("A saída não tem o resumo da carga (\"Carga do arquivo ... concluída\")");
        }
        return new Summary(matcher.group(1), Integer.parseInt(matcher.group(2)),
                Integer.parseInt(matcher.group(3)), Integer.parseInt(matcher.group(4)));
    }
}
