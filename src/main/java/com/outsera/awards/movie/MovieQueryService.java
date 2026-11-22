package com.outsera.awards.movie;

import com.outsera.awards.web.InvalidParameterException;
import com.outsera.awards.web.InvalidParameterException.InvalidParam;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Listagem de filmes com paginação e filtros (FR-019 a FR-023, research R7).
 *
 * <p>Os parâmetros chegam como texto e são validados aqui, porque o conversor padrão do Spring aceitaria
 * {@code yes}, {@code on} e {@code 1} como booleanos. Todos os parâmetros inválidos da consulta são
 * informados juntos, na ordem {@code year}, {@code winner}, {@code page} e {@code size}.
 */
@Service
public class MovieQueryService {

    static final int DEFAULT_PAGE = 0;
    static final int DEFAULT_SIZE = 20;
    static final int MAX_SIZE = 100;

    private static final Pattern YEAR = Pattern.compile("\\d{1,9}");
    private static final Pattern NON_NEGATIVE_INTEGER = Pattern.compile("\\d+");

    private final MovieRepository repository;

    public MovieQueryService(MovieRepository repository) {
        this.repository = repository;
    }

    /** Parâmetros nulos são ausentes e assumem o padrão. */
    @Transactional(readOnly = true)
    public MoviePage list(String year, String winner, String page, String size) {
        List<InvalidParam> invalid = new ArrayList<>();
        Integer yearFilter = parseYear(year, invalid);
        Boolean winnerFilter = parseWinner(winner, invalid);
        int pageNumber = parseInt("page", page, DEFAULT_PAGE, 0, Integer.MAX_VALUE,
                "deve ser um inteiro maior ou igual a 0", invalid);
        int pageSize = parseInt("size", size, DEFAULT_SIZE, 1, MAX_SIZE,
                "deve ser um inteiro de 1 a " + MAX_SIZE, invalid);
        if (!invalid.isEmpty()) {
            throw new InvalidParameterException(invalid);
        }

        // O Spring Data recusa um deslocamento acima de Integer.MAX_VALUE. Uma página assim fica além da
        // última, então só a contagem é consultada (caso de borda da spec, FR-021).
        if ((long) pageNumber * pageSize > Integer.MAX_VALUE) {
            long total = repository.countFiltered(yearFilter, winnerFilter);
            return MoviePage.of(List.of(), pageNumber, pageSize, total);
        }

        Page<Movie> result = repository.findFiltered(yearFilter, winnerFilter, PageRequest.of(pageNumber, pageSize));
        return MoviePage.of(result.map(MovieItem::of).getContent(), pageNumber, pageSize, result.getTotalElements());
    }

    private static Integer parseYear(String value, List<InvalidParam> invalid) {
        if (value == null) {
            return null;
        }
        if (!YEAR.matcher(value).matches()) {
            invalid.add(new InvalidParam("year", "deve ser um ano com até 9 dígitos, sem sinal"));
            return null;
        }
        return Integer.valueOf(value);
    }

    private static Boolean parseWinner(String value, List<InvalidParam> invalid) {
        if (value == null) {
            return null;
        }
        return switch (value.toLowerCase(Locale.ROOT)) {
            case "true" -> Boolean.TRUE;
            case "false" -> Boolean.FALSE;
            default -> {
                invalid.add(new InvalidParam("winner", "deve ser true ou false"));
                yield null;
            }
        };
    }

    private static int parseInt(String name, String value, int defaultValue, int min, int max, String reason,
            List<InvalidParam> invalid) {
        if (value == null) {
            return defaultValue;
        }
        if (NON_NEGATIVE_INTEGER.matcher(value).matches()) {
            try {
                int parsed = Integer.parseInt(value);
                if (parsed >= min && parsed <= max) {
                    return parsed;
                }
            } catch (NumberFormatException tooLarge) {
                // Não cabe em 32 bits: inválido, como qualquer valor fora da faixa.
            }
        }
        invalid.add(new InvalidParam(name, reason));
        return defaultValue;
    }
}
