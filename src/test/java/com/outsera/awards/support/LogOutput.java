package com.outsera.awards.support;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Lê a saída capturada do console como uma lista de eventos de log, nos dois formatos do Marco 2
 * (spec 002, FR-017 e FR-020; research R6):
 *
 * <ul>
 *   <li>uma linha que começa com {@code {} é um evento JSON no formato ECS (chaves pontuadas, como
 *       {@code "log.level"}, ou aninhadas, como {@code "log": {"level": ...}});
 *   <li>as demais seguem o padrão de texto do Boot
 *       ({@code <data> <NÍVEL> <pid> --- [<app>] [<thread>] <logger> : <mensagem>}); uma linha que não
 *       casa com o padrão continua o evento anterior (mensagem com várias linhas ou pilha).
 * </ul>
 *
 * <p>Assim, os testes verificam o conteúdo do log da mesma forma nos dois formatos (FR-018).
 */
public final class LogOutput {

    /** Um evento de log. {@code level} e {@code logger} são nulos numa linha solta, fora de um evento. */
    public record Event(String timestamp, String level, String logger, String thread, String message,
            String stackTrace) {

        /** Mensagem e, se houver, a pilha, numa só string. */
        public String text() {
            return stackTrace == null ? message : message + "\n" + stackTrace;
        }
    }

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private static final Pattern TEXT_EVENT = Pattern.compile(
            "^(\\d{4}-\\d{2}-\\d{2}T\\S+)\\s+(TRACE|DEBUG|INFO|WARN|ERROR)\\s+\\d+\\s+---\\s+\\[[^\\]]*]\\s+"
                    + "\\[\\s*([^\\]]*)]\\s+(\\S+)\\s*:\\s?(.*)$");

    private LogOutput() {
    }

    /** A linha é um evento JSON (começa com {@code {}, depois de espaços). */
    public static boolean isJson(String line) {
        return line.stripLeading().startsWith("{");
    }

    /** Os eventos da saída, na ordem em que aparecem. Linhas em branco são ignoradas. */
    public static List<Event> parse(String output) {
        List<Event> events = new ArrayList<>();
        StringBuilder continuation = null;
        Event open = null;
        for (String line : output.split("\\R")) {
            if (line.isBlank()) {
                continue;
            }
            if (isJson(line)) {
                open = close(events, open, continuation);
                continuation = null;
                events.add(fromJson(line));
                continue;
            }
            Matcher text = TEXT_EVENT.matcher(line);
            if (text.matches()) {
                close(events, open, continuation);
                open = new Event(text.group(1), text.group(2), text.group(4), text.group(3).strip(),
                        text.group(5), null);
                continuation = new StringBuilder();
            } else if (open != null) {
                continuation.append('\n').append(line);
            } else {
                events.add(new Event(null, null, null, null, line, null));
            }
        }
        close(events, open, continuation);
        return events;
    }

    /** Mensagens (com a pilha, quando houver) de todos os eventos, uma por evento. */
    public static List<String> messages(String output) {
        return parse(output).stream().map(Event::text).toList();
    }

    private static Event close(List<Event> events, Event open, StringBuilder continuation) {
        if (open != null) {
            String rest = continuation == null ? "" : continuation.toString();
            events.add(new Event(open.timestamp(), open.level(), open.logger(), open.thread(),
                    open.message() + rest, null));
        }
        return null;
    }

    private static Event fromJson(String line) {
        JsonNode node = JSON.readTree(line);
        return new Event(field(node, "@timestamp"), field(node, "log.level"), field(node, "log.logger"),
                field(node, "process.thread.name"), field(node, "message"), field(node, "error.stack_trace"));
    }

    /** O campo ECS, com a chave pontuada ({@code "log.level"}) ou aninhada ({@code log -> level}). */
    public static String field(JsonNode node, String name) {
        JsonNode flat = node.get(name);
        if (flat != null && !flat.isNull()) {
            return flat.asString();
        }
        JsonNode current = node;
        for (String part : name.split("\\.")) {
            current = current == null ? null : current.get(part);
        }
        return current == null || current.isNull() ? null : current.asString();
    }

    /** Lê uma linha JSON (para os testes que conferem os campos do evento). */
    public static JsonNode json(String line) {
        return JSON.readTree(line);
    }
}
