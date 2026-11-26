import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Random;

/**
 * Gerador de CSV grande e determinístico (spec 004, FR-014 a FR-016 e FR-019; research R6;
 * contracts/operacao.md, "Gerador de CSV"; data-model.md, "Arquivo gerado").
 *
 * <pre>
 * java tools/csv-generator/GenerateMovies.java [--movies N] [--seed S] --output ARQUIVO
 * </pre>
 *
 * <p>Arquivo único, sem dependências, rodado direto pelo {@code java} do JDK 25. Não entra no build do
 * Maven nem na imagem.
 *
 * <ul>
 *   <li><strong>Determinismo:</strong> toda escolha vem de um único {@code java.util.Random(seed)}, cujo
 *       algoritmo é fixado pela especificação da plataforma. A saída é UTF-8 sem BOM, com {@code \n}
 *       explícito, então os mesmos parâmetros dão o mesmo SHA-256 em qualquer JVM e sistema
 *       operacional.</li>
 *   <li><strong>Formato:</strong> o do enunciado, com o cabeçalho {@code year;title;studios;producers;winner}.
 *       Títulos {@code Movie <n>}, únicos, então a chave (ano, título) nunca se repete. Anos de 1900 a
 *       2099, estúdios de uma lista fixa e produtores {@code Producer 00001} em diante, separados por
 *       {@code ", "}, {@code " and "} ou {@code ", and "}. Cerca de 20% vencedores.</li>
 *   <li><strong>Casos plantados</strong> (com 8 filmes ou mais): dois produtores com duas vitórias no mesmo
 *       ano, em anos diferentes entre si (empate em {@code min}, intervalo 0), e dois com vitórias em 1900
 *       e 2099 (empate em {@code max}, intervalo 199). Um produtor sorteado nunca vence duas vezes no
 *       mesmo ano nem em 1900 e 2099, então {@code min} e {@code max} são exatamente os plantados.</li>
 *   <li><strong>Escrita atômica:</strong> num arquivo temporário da mesma pasta, movido no fim. Um erro de
 *       escrita apaga o temporário e sai com 1. Parâmetro inválido sai com 2, sem criar arquivo.</li>
 * </ul>
 */
public final class GenerateMovies {

    private static final int MIN_MOVIES = 1;
    private static final int MAX_MOVIES = 2_000_000;
    private static final int DEFAULT_MOVIES = 100_000;
    private static final long DEFAULT_SEED = 20261125L;
    private static final int FIRST_YEAR = 1900;
    private static final int LAST_YEAR = 2099;
    private static final int YEARS = LAST_YEAR - FIRST_YEAR + 1;
    private static final int MAX_POOL = 99_999;

    private static final String[] STUDIOS = {"Aurora Pictures", "Blue Harbor Films", "Cinder Studios",
            "Delta Frame", "Eastlight Media", "Fable Works", "Granite Screen", "Horizon Reel"};
    private static final String[] SEPARATORS = {", ", " and ", ", and "};

    /** Os casos plantados: produtor e ano de cada uma das 8 vitórias. */
    private static final String MIN_A = "Planted Min A";
    private static final String MIN_B = "Planted Min B";
    private static final String MAX_C = "Planted Max C";
    private static final String MAX_D = "Planted Max D";
    private static final String[] PLANTED_PRODUCERS = {MIN_A, MIN_A, MIN_B, MIN_B, MAX_C, MAX_C, MAX_D, MAX_D};
    private static final int[] PLANTED_YEARS = {1950, 1950, 1960, 1960, FIRST_YEAR, LAST_YEAR, FIRST_YEAR, LAST_YEAR};

    private GenerateMovies() {
    }

    private record Options(int movies, long seed, Path output) {
    }

    /** Parâmetro inválido: uma linha no erro padrão e saída 2. */
    private static final class UsageException extends Exception {
        UsageException(String message) {
            super(message);
        }
    }

    public static void main(String[] args) {
        Options options;
        try {
            options = parse(args);
        } catch (UsageException e) {
            System.err.println("GenerateMovies: " + e.getMessage()
                    + " (uso: java tools/csv-generator/GenerateMovies.java [--movies N] [--seed S] --output ARQUIVO)");
            System.exit(2);
            return;
        }
        Path temporary = null;
        try {
            temporary = Files.createTempFile(options.output().getParent(), ".GenerateMovies-", ".tmp");
            write(options, temporary);
            // No Linux, o createTempFile cria o arquivo só para o dono (rw-------), e o ATOMIC_MOVE mantém a
            // permissão. A aplicação no container roda com outro usuário e não conseguiria ler o arquivo
            // montado (job load, commit 127584c). Fica legível por todos, como um arquivo comum.
            if (Files.getFileStore(temporary).supportsFileAttributeView("posix")) {
                Files.setPosixFilePermissions(temporary, PosixFilePermissions.fromString("rw-r--r--"));
            }
            String sha256 = sha256(temporary);
            Files.move(temporary, options.output(), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            System.out.println(options.output() + ": " + options.movies() + " filmes, SHA-256 " + sha256);
        } catch (IOException e) {
            if (temporary != null) {
                try {
                    Files.deleteIfExists(temporary);
                } catch (IOException ignored) {
                    // O erro original é o que importa.
                }
            }
            System.err.println("GenerateMovies: erro de escrita em " + options.output() + ": " + e.getMessage());
            System.exit(1);
        }
    }

    private static Options parse(String[] args) throws UsageException {
        Integer movies = null;
        Long seed = null;
        Path output = null;
        for (int i = 0; i < args.length; i += 2) {
            String name = args[i];
            if (i + 1 >= args.length) {
                throw new UsageException("falta o valor de " + name);
            }
            String value = args[i + 1];
            switch (name) {
                case "--movies" -> {
                    if (movies != null) {
                        throw new UsageException("--movies repetido");
                    }
                    movies = parseMovies(value);
                }
                case "--seed" -> {
                    if (seed != null) {
                        throw new UsageException("--seed repetido");
                    }
                    try {
                        seed = Long.parseLong(value);
                    } catch (NumberFormatException e) {
                        throw new UsageException("--seed precisa ser um inteiro de 64 bits: " + value);
                    }
                }
                case "--output" -> {
                    if (output != null) {
                        throw new UsageException("--output repetido");
                    }
                    output = Path.of(value).toAbsolutePath().normalize();
                }
                default -> throw new UsageException("parâmetro desconhecido: " + name);
            }
        }
        if (output == null) {
            throw new UsageException("--output é obrigatório");
        }
        Path parent = output.getParent();
        if (parent == null || !Files.isDirectory(parent)) {
            throw new UsageException("a pasta de " + output + " não existe");
        }
        if (Files.isDirectory(output)) {
            throw new UsageException(output + " é uma pasta");
        }
        return new Options(movies == null ? DEFAULT_MOVIES : movies, seed == null ? DEFAULT_SEED : seed, output);
    }

    private static int parseMovies(String value) throws UsageException {
        if (!value.matches("\\d{1,7}")) {
            throw new UsageException("--movies precisa ser um inteiro de " + MIN_MOVIES + " a " + MAX_MOVIES + ": " + value);
        }
        int movies = Integer.parseInt(value);
        if (movies < MIN_MOVIES || movies > MAX_MOVIES) {
            throw new UsageException("--movies precisa ser um inteiro de " + MIN_MOVIES + " a " + MAX_MOVIES + ": " + value);
        }
        return movies;
    }

    private static void write(Options options, Path file) throws IOException {
        int movies = options.movies();
        int planted = movies >= PLANTED_PRODUCERS.length ? PLANTED_PRODUCERS.length : 0;
        int random = movies - planted;
        int pool = Math.min(MAX_POOL, Math.max(50, movies / 4));
        // Anos de vitória de cada produtor sorteado: 200 bits (4 longs) por produtor, sem coleção iterada.
        long[] wins = new long[pool * 4];
        Random rnd = new Random(options.seed());
        int[] chosen = new int[3];
        try (BufferedWriter out = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            out.write("year;title;studios;producers;winner");
            out.write('\n');
            for (int n = 1; n <= random; n++) {
                int year = FIRST_YEAR + rnd.nextInt(YEARS);
                String studios = STUDIOS[rnd.nextInt(STUDIOS.length)];
                if (rnd.nextInt(4) == 0) {
                    String second = STUDIOS[rnd.nextInt(STUDIOS.length)];
                    if (!second.equals(studios)) {
                        studios = studios + ", " + second;
                    }
                }
                int count = 1 + rnd.nextInt(3);
                for (int i = 0; i < count; i++) {
                    int candidate;
                    boolean repeated;
                    do {
                        candidate = rnd.nextInt(pool);
                        repeated = false;
                        for (int j = 0; j < i; j++) {
                            repeated |= chosen[j] == candidate;
                        }
                    } while (repeated);
                    chosen[i] = candidate;
                }
                StringBuilder producers = new StringBuilder(name(chosen[0]));
                for (int i = 1; i < count; i++) {
                    producers.append(SEPARATORS[rnd.nextInt(SEPARATORS.length)]).append(name(chosen[i]));
                }
                boolean winner = rnd.nextInt(5) == 0 && canWin(wins, chosen, count, year);
                if (winner) {
                    for (int i = 0; i < count; i++) {
                        mark(wins, chosen[i], year);
                    }
                }
                line(out, year, n, studios, producers.toString(), winner);
            }
            for (int i = 0; i < planted; i++) {
                line(out, PLANTED_YEARS[i], random + i + 1, STUDIOS[0], PLANTED_PRODUCERS[i], true);
            }
        }
    }

    private static String name(int index) {
        // Locale.ROOT: os dígitos não dependem do locale da máquina.
        return String.format(Locale.ROOT, "Producer %05d", index + 1);
    }

    /**
     * Um produtor sorteado não pode vencer duas vezes no mesmo ano (daria intervalo 0) nem vencer em 1900 e
     * em 2099 (daria o intervalo máximo), para que {@code min} e {@code max} sejam só os plantados.
     */
    private static boolean canWin(long[] wins, int[] producers, int count, int year) {
        for (int i = 0; i < count; i++) {
            int producer = producers[i];
            if (won(wins, producer, year)
                    || (year == FIRST_YEAR && won(wins, producer, LAST_YEAR))
                    || (year == LAST_YEAR && won(wins, producer, FIRST_YEAR))) {
                return false;
            }
        }
        return true;
    }

    private static boolean won(long[] wins, int producer, int year) {
        int bit = year - FIRST_YEAR;
        return (wins[producer * 4 + bit / 64] & (1L << (bit % 64))) != 0;
    }

    private static void mark(long[] wins, int producer, int year) {
        int bit = year - FIRST_YEAR;
        wins[producer * 4 + bit / 64] |= 1L << (bit % 64);
    }

    private static void line(BufferedWriter out, int year, int number, String studios, String producers, boolean winner)
            throws IOException {
        out.write(Integer.toString(year));
        out.write(';');
        out.write("Movie ");
        out.write(Integer.toString(number));
        out.write(';');
        out.write(studios);
        out.write(';');
        out.write(producers);
        out.write(';');
        if (winner) {
            out.write("yes");
        }
        out.write('\n');
    }

    private static String sha256(Path file) throws IOException {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
        try (InputStream in = Files.newInputStream(file)) {
            byte[] buffer = new byte[1 << 16];
            for (int read = in.read(buffer); read >= 0; read = in.read(buffer)) {
                digest.update(buffer, 0, read);
            }
        }
        return HexFormat.of().formatHex(digest.digest());
    }
}
