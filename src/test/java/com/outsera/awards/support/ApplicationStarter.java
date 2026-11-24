package com.outsera.awards.support;

import com.outsera.awards.AwardsApplication;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Executa a {@link AwardsApplication} real com {@link SpringApplication}, para os testes que controlam o
 * ciclo de vida da aplicação (cláusula do Princípio I, constituição 1.5.0): falha de inicialização,
 * múltiplas execuções, ordem da subida e saída completa da subida. Não é um dublê: é a aplicação
 * completa subindo com o CSV indicado.
 *
 * <p>A saída do console é duplicada durante a execução: continua aparecendo no log do build e também
 * fica guardada no resultado, para o teste verificar a mensagem do {@code FailureAnalyzer}.
 */
public final class ApplicationStarter {

    private ApplicationStarter() {
    }

    /** Resultado de uma tentativa de inicialização. */
    public record Result(Throwable failure, String output) {

        public boolean failed() {
            return failure != null;
        }

        /**
         * As mensagens do log (com as pilhas, quando houver), decodificadas pelo {@link LogOutput}, uma
         * por evento e separadas por quebra de linha. É o texto que os testes verificam nos dois formatos
         * (spec 002, FR-018): no JSON, as aspas e quebras de linha da mensagem chegam escapadas na saída
         * crua.
         */
        public String messages() {
            return String.join("\n", LogOutput.messages(output));
        }
    }

    /**
     * Sobe a aplicação com {@code --server.port=0} e {@code --movies.csv.path=<csvPath>}. Se ela subir,
     * o contexto é fechado logo em seguida e {@link Result#failure()} vem nulo.
     */
    public static Result start(String csvPath) {
        return start(csvPath, new ApplicationListener<?>[0]);
    }

    /**
     * Como {@link #start(String)}, registrando os {@code listeners} na {@link SpringApplication} antes do
     * {@code run}. Os ouvintes são reais e recebem os eventos reais do ciclo de vida (por exemplo, a
     * mudança de prontidão; spec 002, FR-009).
     */
    public static Result start(String csvPath, ApplicationListener<?>... listeners) {
        return start(csvPath, List.of(), listeners);
    }

    /**
     * Como {@link #start(String, ApplicationListener...)}, com argumentos extras na linha de comando (por
     * exemplo, o perfil e o banco do PostgreSQL ou o endereço do broker; spec 003).
     */
    public static Result start(String csvPath, List<String> extraArgs, ApplicationListener<?>... listeners) {
        return startAndCall(csvPath, extraArgs, port -> { }, listeners);
    }

    /**
     * Sobe a aplicação capturando a saída desde a primeira linha, executa {@code whileUp} com a porta
     * enquanto ela está no ar e fecha o contexto. Uma falha da subida fica em {@link Result#failure()}.
     * Uma falha do próprio {@code whileUp}, como uma asserção, é relançada depois de restaurar a saída.
     */
    public static Result startAndCall(String csvPath, List<String> extraArgs, IntConsumer whileUp,
            ApplicationListener<?>... listeners) {
        PrintStream originalOut = System.out;
        PrintStream originalErr = System.err;
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        System.setOut(tee(originalOut, captured));
        System.setErr(tee(originalErr, captured));
        Throwable failure = null;
        RuntimeException callFailure = null;
        AssertionError assertionFailure = null;
        try {
            SpringApplication application = new SpringApplication(AwardsApplication.class);
            application.addListeners(listeners);
            try (ConfigurableApplicationContext context = application.run(args(csvPath, extraArgs))) {
                try {
                    whileUp.accept(port(context));
                } catch (AssertionError ex) {
                    assertionFailure = ex;
                } catch (RuntimeException ex) {
                    callFailure = ex;
                }
            }
        } catch (Throwable ex) {
            failure = ex;
        } finally {
            System.out.flush();
            System.err.flush();
            System.setOut(originalOut);
            System.setErr(originalErr);
        }
        if (assertionFailure != null) {
            throw assertionFailure;
        }
        if (callFailure != null) {
            throw callFailure;
        }
        return new Result(failure, captured.toString(StandardCharsets.UTF_8));
    }

    /**
     * Sobe a aplicação real com {@code --server.port=0} e {@code --movies.csv.path=<csvPath>}, executa
     * {@code call} com a porta em que ela atende e fecha o contexto no fim, mesmo se {@code call} lançar
     * exceção. Serve para comparar respostas HTTP entre execuções separadas (por exemplo, o ETag com
     * arquivos diferentes).
     */
    public static <T> T runAndCall(String csvPath, IntFunction<T> call) {
        return runAndCall(csvPath, List.of(), call);
    }

    /** Como {@link #runAndCall(String, IntFunction)}, com argumentos extras na linha de comando. */
    public static <T> T runAndCall(String csvPath, List<String> extraArgs, IntFunction<T> call) {
        return runWithContext(csvPath, extraArgs, (context, port) -> call.apply(port));
    }

    /**
     * Como {@link #runAndCall(String, List, IntFunction)}, entregando também o contexto da aplicação real,
     * para verificações que o HTTP não observa (por exemplo, que o banco é mesmo o PostgreSQL).
     */
    public static <T> T runWithContext(String csvPath, List<String> extraArgs,
            BiFunction<ConfigurableApplicationContext, Integer, T> call) {
        SpringApplication application = new SpringApplication(AwardsApplication.class);
        try (ConfigurableApplicationContext context = application.run(args(csvPath, extraArgs))) {
            return call.apply(context, port(context));
        }
    }

    private static String[] args(String csvPath, List<String> extraArgs) {
        List<String> args = new ArrayList<>();
        args.add("--server.port=0");
        args.add("--movies.csv.path=" + csvPath);
        args.addAll(extraArgs);
        return args.toArray(String[]::new);
    }

    private static int port(ConfigurableApplicationContext context) {
        return Integer.parseInt(context.getEnvironment().getRequiredProperty("local.server.port"));
    }

    private static PrintStream tee(PrintStream original, OutputStream copy) {
        OutputStream both = new OutputStream() {
            @Override
            public void write(int b) {
                original.write(b);
                synchronized (copy) {
                    try {
                        copy.write(b);
                    } catch (java.io.IOException ex) {
                        throw new java.io.UncheckedIOException(ex);
                    }
                }
            }

            @Override
            public void write(byte[] b, int off, int len) {
                original.write(b, off, len);
                synchronized (copy) {
                    try {
                        copy.write(b, off, len);
                    } catch (java.io.IOException ex) {
                        throw new java.io.UncheckedIOException(ex);
                    }
                }
            }

            @Override
            public void flush() {
                original.flush();
            }
        };
        return new PrintStream(both, true, StandardCharsets.UTF_8);
    }
}
