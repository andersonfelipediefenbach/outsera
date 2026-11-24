package com.outsera.awards.support;

import com.outsera.awards.AwardsApplication;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.function.IntFunction;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Executa a {@link AwardsApplication} real com {@link SpringApplication}, para os testes de falha de
 * inicialização (cláusula própria do Princípio I). Não é um dublê: é a aplicação completa subindo com
 * o CSV indicado.
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
        PrintStream originalOut = System.out;
        PrintStream originalErr = System.err;
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        System.setOut(tee(originalOut, captured));
        System.setErr(tee(originalErr, captured));
        Throwable failure = null;
        try {
            SpringApplication application = new SpringApplication(AwardsApplication.class);
            application.addListeners(listeners);
            try (ConfigurableApplicationContext ignored =
                    application.run("--server.port=0", "--movies.csv.path=" + csvPath)) {
                // A aplicação subiu: o contexto é fechado pelo try-with-resources.
            }
        } catch (Throwable ex) {
            failure = ex;
        } finally {
            System.out.flush();
            System.err.flush();
            System.setOut(originalOut);
            System.setErr(originalErr);
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
        SpringApplication application = new SpringApplication(AwardsApplication.class);
        try (ConfigurableApplicationContext context =
                application.run("--server.port=0", "--movies.csv.path=" + csvPath)) {
            int port = Integer.parseInt(context.getEnvironment().getRequiredProperty("local.server.port"));
            return call.apply(port);
        }
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
