package com.outsera.awards.support;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Lê, na saída capturada, os eventos emitidos pelo carregador do CSV, em texto ou em JSON (spec 002,
 * FR-018). A saída é lida pelo {@link LogOutput}, e os métodos devolvem as <b>mensagens</b>, e não as
 * linhas cruas: assim, as verificações do Marco 1 (por exemplo,
 * {@code endsWith("Linha N rejeitada: ...")}) valem nos dois formatos.
 *
 * <p>Só os eventos do logger {@code MovieFileLoader} (pacote {@code com.outsera.awards.ingestion})
 * contam. Avisos de outras bibliotecas, como o do Flyway sobre a versão do H2, ficam de fora (decisão de
 * 2026-11-22 em {@code docs/ai/decisoes.md}). O teste não importa a classe de produção, para compilar
 * antes da implementação: o logger é reconhecido pelo nome.
 */
public final class LoaderLog {

    private static final String LOADER_LOGGER = "MovieFileLoader";

    private static final Pattern SUMMARY = Pattern.compile(
            "Carga do arquivo (\\S+) concluída: (\\d+) linhas lidas, (\\d+) aceitas, (\\d+) rejeitadas");

    private LoaderLog() {
    }

    /** O resumo da carga, lido da única mensagem {@code Carga do arquivo ... concluída}. */
    public record Summary(String path, int read, int accepted, int rejected) {
    }

    /** Mensagens dos eventos de nível {@code WARN} emitidos pelo carregador do CSV. */
    public static List<String> loaderWarnings(String output) {
        return LogOutput.parse(output).stream()
                .filter(event -> "WARN".equals(event.level()))
                .filter(event -> event.logger() != null && event.logger().endsWith(LOADER_LOGGER))
                .map(LogOutput.Event::message)
                .toList();
    }

    /** O resumo da carga. Falha se a saída não tiver a mensagem de resumo. */
    public static Summary summary(String output) {
        for (String message : LogOutput.messages(output)) {
            Matcher matcher = SUMMARY.matcher(message);
            if (matcher.find()) {
                return new Summary(matcher.group(1), Integer.parseInt(matcher.group(2)),
                        Integer.parseInt(matcher.group(3)), Integer.parseInt(matcher.group(4)));
            }
        }
        throw new AssertionError("A saída não tem o resumo da carga (\"Carga do arquivo ... concluída\")");
    }
}
