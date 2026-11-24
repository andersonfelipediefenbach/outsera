package com.outsera.awards.support;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import javax.sql.DataSource;

/**
 * Apoio dos testes {@code *PostgresIT} (spec 003, perfil Maven {@code postgres-it}; research R16).
 *
 * <p>A conexão vem de {@code DB_URL}, {@code DB_USER} e {@code DB_PASSWORD}, como no perfil
 * {@code postgres} da aplicação. Cada classe usa um esquema próprio, criado pelo Flyway e apagado no fim,
 * para que nenhum teste veja os dados de outro. É um PostgreSQL real, sem dublê (Princípio I).
 */
public final class PostgresSupport {

    private PostgresSupport() {
    }

    public static String url() {
        return env("DB_URL", "jdbc:postgresql://localhost:5432/awards");
    }

    public static String user() {
        return env("DB_USER", "awards");
    }

    public static String password() {
        return env("DB_PASSWORD", "");
    }

    /** Nome de esquema novo para a classe: {@code it_<classe>_<8 caracteres aleatórios>}. */
    public static String newSchema(Class<?> testClass) {
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        return ("it_" + testClass.getSimpleName() + "_" + suffix).toLowerCase(Locale.ROOT);
    }

    /** URL de conexão com o esquema como esquema corrente. */
    public static String urlFor(String schema) {
        String base = url();
        return base + (base.contains("?") ? "&" : "?") + "currentSchema=" + schema;
    }

    /**
     * Propriedades que sobem a aplicação no perfil {@code postgres}, no esquema dado. O banco é
     * configurado pelas mesmas variáveis do {@code application-postgres.yml}.
     */
    public static Map<String, String> properties(String schema) {
        return Map.of(
                "DB_URL", urlFor(schema),
                "DB_USER", user(),
                "DB_PASSWORD", password(),
                "spring.flyway.default-schema", schema,
                "spring.flyway.create-schemas", "true",
                "spring.jpa.properties.hibernate.default_schema", schema);
    }

    /** Argumentos de linha de comando com o perfil {@code postgres} e as {@link #properties(String)}. */
    public static List<String> args(String schema) {
        return args(schema, "postgres");
    }

    /** Como {@link #args(String)}, com os perfis dados (por exemplo, {@code postgres,kafka}). */
    public static List<String> args(String schema, String profiles) {
        List<String> args = new java.util.ArrayList<>();
        args.add("--spring.profiles.active=" + profiles);
        properties(schema).forEach((name, value) -> args.add("--" + name + "=" + value));
        return args;
    }

    /** Falha se o banco não é o PostgreSQL: sem o perfil, a aplicação subiria no H2 e o teste passaria. */
    public static void assertIsPostgres(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            assertThat(connection.getMetaData().getDatabaseProductName()).isEqualTo("PostgreSQL");
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    /** Executa um comando SQL de teste no esquema (fora da aplicação; por exemplo, renomear uma tabela). */
    public static void execute(String schema, String sql) {
        try (Connection connection = DriverManager.getConnection(urlFor(schema), user(), password());
                Statement statement = connection.createStatement()) {
            statement.execute(sql);
        } catch (SQLException e) {
            throw new IllegalStateException(sql, e);
        }
    }

    /** Apaga o esquema da classe. É SQL de teste, fora da aplicação. */
    public static void dropSchema(String schema) {
        execute(schema, "DROP SCHEMA IF EXISTS " + schema + " CASCADE");
    }

    private static String env(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null ? defaultValue : value;
    }
}
