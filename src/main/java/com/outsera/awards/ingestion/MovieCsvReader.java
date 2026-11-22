package com.outsera.awards.ingestion;

import com.outsera.awards.ingestion.MovieFileException.Reason;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

/**
 * Lê e valida o arquivo de filmes (FR-003 a FR-006e, research R3).
 *
 * <p>Erros estruturais lançam {@link MovieFileException}. Linhas inválidas viram {@link Rejection} no
 * {@link LoadReport}, e a leitura continua.
 */
@Component
public class MovieCsvReader {

    static final List<String> EXPECTED_HEADER = List.of("year", "title", "studios", "producers", "winner");
    static final String EXPECTED_HEADER_TEXT = String.join(";", EXPECTED_HEADER);

    private static final char BOM = '﻿';
    private static final Pattern YEAR = Pattern.compile("\\d{1,9}");

    private static final CSVFormat FORMAT = CSVFormat.DEFAULT.builder()
            .setDelimiter(';')
            .setQuote('"')
            .setIgnoreEmptyLines(true)
            .setTrim(true)
            .setIgnoreSurroundingSpaces(true)
            .get();

    /** Linhas válidas e o relatório da carga. */
    public record Result(List<MovieRow> rows, LoadReport report) {
    }

    /** Chave de identidade de um filme: ano + título, sem espaços em volta (FR-006d). */
    private record MovieKey(int year, String title) {
    }

    /**
     * Lê o arquivo inteiro.
     *
     * @param resource o arquivo, já verificado como existente
     * @param path     o caminho como foi configurado, para as mensagens
     */
    public Result read(Resource resource, String path) {
        try (Reader reader = utf8Reader(resource); CSVParser parser = CSVParser.parse(reader, FORMAT)) {
            return read(parser, path);
        } catch (UncheckedIOException ex) {
            throw unreadable(path, ex.getCause());
        } catch (IOException ex) {
            throw unreadable(path, ex);
        }
    }

    private Result read(CSVParser parser, String path) {
        Iterator<CSVRecord> records = parser.iterator();
        validateHeader(nextNonBlank(records, path), path);

        List<MovieRow> rows = new ArrayList<>();
        List<Rejection> rejections = new ArrayList<>();
        Map<MovieKey, Long> accepted = new HashMap<>();
        int linesRead = 0;
        while (records.hasNext()) {
            CSVRecord record = records.next();
            long lineNumber = parser.getCurrentLineNumber();
            if (isBlank(record)) {
                continue;
            }
            linesRead++;
            Rejection rejection = validate(record, lineNumber, accepted);
            if (rejection != null) {
                rejections.add(rejection);
                continue;
            }
            MovieRow row = toRow(record, lineNumber);
            accepted.put(new MovieKey(row.year(), row.title()), lineNumber);
            rows.add(row);
        }
        return new Result(rows, new LoadReport(linesRead, rows.size(), rejections.size(), rejections));
    }

    /**
     * Abre o arquivo em UTF-8 estrito e descarta o BOM, se houver, antes de o Commons CSV ler qualquer
     * caractere. Com o BOM na frente, o Commons CSV não reconheceria a aspa de um cabeçalho entre aspas,
     * e uma linha só com o BOM deixaria de ser uma linha em branco (research R3; revisão do Codex no PR
     * do bolt 2).
     */
    private static Reader utf8Reader(Resource resource) throws IOException {
        CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT);
        BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), decoder));
        try {
            reader.mark(1);
            if (reader.read() != BOM) {
                reader.reset();
            }
        } catch (IOException ex) {
            reader.close();
            throw ex;
        }
        return reader;
    }

    private static CSVRecord nextNonBlank(Iterator<CSVRecord> records, String path) {
        while (records.hasNext()) {
            CSVRecord record = records.next();
            if (!isBlank(record)) {
                return record;
            }
        }
        throw new MovieFileException(Reason.UNREADABLE, path, null, "o arquivo está vazio, sem cabeçalho");
    }

    private static void validateHeader(CSVRecord header, String path) {
        List<String> columns = header.toList();
        String found = String.join(";", columns);

        if (columns.size() == 1 && columns.getFirst().contains(",")) {
            throw new MovieFileException(Reason.INVALID_SEPARATOR, path, found,
                    "o cabeçalho tem uma única coluna separada por vírgulas; o separador de colunas esperado é \";\"");
        }
        boolean matches = columns.size() == EXPECTED_HEADER.size();
        for (int i = 0; matches && i < columns.size(); i++) {
            matches = columns.get(i).equalsIgnoreCase(EXPECTED_HEADER.get(i));
        }
        if (!matches) {
            throw new MovieFileException(Reason.INVALID_HEADER, path, found,
                    "o cabeçalho esperado é \"" + EXPECTED_HEADER_TEXT + "\", nessa ordem, mas o encontrado é \""
                            + found + "\"");
        }
    }

    /** Verifica as regras na ordem de {@code data-model.md} e devolve o primeiro motivo, ou nulo. */
    private static Rejection validate(CSVRecord record, long lineNumber, Map<MovieKey, Long> accepted) {
        if (record.size() != EXPECTED_HEADER.size()) {
            return new Rejection(lineNumber, RejectionReason.COLUMN_COUNT, "encontradas " + record.size());
        }
        String year = record.get(0);
        if (year.isEmpty()) {
            return new Rejection(lineNumber, RejectionReason.YEAR_MISSING, null);
        }
        if (!YEAR.matcher(year).matches()) {
            return new Rejection(lineNumber, RejectionReason.YEAR_NOT_NUMERIC, year);
        }
        String title = record.get(1);
        if (title.isEmpty()) {
            return new Rejection(lineNumber, RejectionReason.TITLE_MISSING, null);
        }
        if (ProducerNameParser.parse(record.get(3)).isEmpty()) {
            return new Rejection(lineNumber, RejectionReason.PRODUCERS_MISSING, null);
        }
        String winner = record.get(4);
        if (!winner.isEmpty() && !winner.equalsIgnoreCase("yes")) {
            return new Rejection(lineNumber, RejectionReason.WINNER_INVALID, winner);
        }
        Long original = accepted.get(new MovieKey(Integer.parseInt(year), title));
        if (original != null) {
            return new Rejection(lineNumber, RejectionReason.DUPLICATE, "mesmo ano e título da linha " + original);
        }
        return null;
    }

    private static MovieRow toRow(CSVRecord record, long lineNumber) {
        String studios = record.get(2);
        String producers = record.get(3);
        return new MovieRow(lineNumber, Integer.parseInt(record.get(0)), record.get(1),
                studios.isEmpty() ? null : studios, producers, ProducerNameParser.parse(producers),
                record.get(4).equalsIgnoreCase("yes"));
    }

    /**
     * Uma linha só com espaços chega como um registro de uma coluna vazia, e é tratada como linha em
     * branco (FR-005a, research R3).
     */
    private static boolean isBlank(CSVRecord record) {
        return record.size() == 1 && record.get(0).isBlank();
    }

    private static MovieFileException unreadable(String path, Throwable cause) {
        String detail = cause instanceof CharacterCodingException
                ? "o arquivo não está em UTF-8 válido"
                : "não foi possível ler o arquivo: " + cause.getMessage();
        return new MovieFileException(Reason.UNREADABLE, path, null, detail, cause);
    }
}
