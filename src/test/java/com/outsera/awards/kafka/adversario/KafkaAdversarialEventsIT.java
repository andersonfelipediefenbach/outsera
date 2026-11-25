package com.outsera.awards.kafka.adversario;

import static com.outsera.awards.support.IntervalItem.item;

import com.outsera.awards.support.IntervalItem;
import com.outsera.awards.support.IntervalsBody;
import com.outsera.awards.support.KafkaTestSupport;
import com.outsera.awards.support.KafkaTestSupport.Record;
import com.outsera.awards.support.MovieJson;
import com.outsera.awards.support.MoviesBody;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 * Os 24 casos do testador adversário que não mexem no marcador, na readiness nem na subida, numa única
 * aplicação e num único broker (casos 01 a 21, 27, 28 e 30; spec 004, FR-035). É a mesma consolidação do
 * {@code KafkaInvalidEventsIT} do Marco 3 (docs/ai/decisoes.md, "Tempo do ./mvnw verify do Marco 3"). Os
 * casos do marcador, da readiness e da subida (22 a 26 e 29) têm contexto próprio.
 *
 * <p>Cada caso roda em sequência, com chaves ({@code cNN/...}) e títulos ({@code CNN ...}) próprios. Só os
 * títulos que são o próprio dado testado (500 letras, emojis, só NBSP) ficam sem o prefixo, e eles já são
 * únicos. Cada conferência é uma {@link SoftAssertions} com a mensagem do caso, e uma exceção num caso vira
 * uma falha dele, sem impedir os seguintes.
 *
 * <p><strong>Isolamento:</strong>
 * <ul>
 *   <li>filmes: o caso confere só o que mudou na listagem desde o início dele (filmes novos ou
 *       alterados), e que nenhum filme anterior foi alterado;</li>
 *   <li>tópico de erros: o caso confere só os registros novos, que têm de ser exatamente os dele;</li>
 *   <li>intervalos: no fim de cada caso, todo vencedor gravado é reenviado como indicado, pelo próprio
 *       caminho de atualização (spec 003, FR-014). Assim, os intervalos do caso seguinte só têm os
 *       vencedores dele, e a conferência exata de {@code min} e {@code max} continua por caso.</li>
 * </ul>
 */
@DirtiesContext
@TestPropertySource(properties = {KafkaAdversarialIT.EMPTY_CSV, "awards.test.context=KafkaAdversarialEventsIT"})
class KafkaAdversarialEventsIT extends KafkaAdversarialIT {

    private static final JsonMapper JSON = JsonMapper.builder().build();
    private static final String NBSP = " ";
    private static final String ZWSP = "​";
    private static final String CLAPPER = "🎬";
    private static final String NFC_JOSE = "José";
    private static final String NFD_JOSE = "José";

    private SoftAssertions softly;
    private CapturedOutput output;
    private String id;
    private List<MovieJson> moviesAtStart;
    private int errorsAtStart;

    @Test
    @DisplayName("S004 FR-035 S003 FR-010 a FR-016 SC-004 SC-006 S001 FR-006 a FR-015 casos 01 a 21, 27, 28 e 30 do testador adversário numa única aplicação")
    void adversarialEventCases(CapturedOutput output) {
        this.output = output;
        softly = new SoftAssertions();
        awaitReady();
        run("01", this::case01TruncatedJson);
        run("02", this::case02ConcatenatedJson);
        run("03", this::case03InvalidUtf8);
        run("04", this::case04MissingFields);
        run("05", this::case05TextFieldTypes);
        run("06", this::case06WinnerTypes);
        run("07", this::case07YearTypes);
        run("08", this::case08YearLimits);
        run("09", this::case09ExtraFieldsAndKeys);
        run("10", this::case10EventType);
        run("11", this::case11DuplicateKeys);
        run("12", this::case12JsonBom);
        run("13", this::case13NulCharacter);
        run("14", this::case14UnicodeNormalization);
        run("15", this::case15InvisibleSpaces);
        run("16", this::case16AsciiLengths);
        run("17", this::case17UnicodeLength);
        run("18", this::case18Separators);
        run("19", this::case19TabAroundAnd);
        run("20", this::case20IdenticalReplay);
        run("21", this::case21UpdateAndReplay);
        run("27", this::case27TiesAndOrder);
        run("28", this::case28InvalidUpdate);
        run("30", this::case30IdenticalTies);
        softly.assertThat(readinessStatus()).as("readiness depois de todos os casos").isEqualTo(200);
        softly.assertAll();
    }

    // ---------------------------------------------------------------------------------------------------
    // Casos

    /** Caso 01: JSON truncado, vazio e raiz de tipo incorreto. */
    private void case01TruncatedJson() {
        String truncated = "{\"year\":2001,\"title\":\"" + t("Truncado") + "\",\"producers\":\"Iris\",\"winner\":true";
        String array = "[{\"year\":2001,\"title\":\"" + t("Em array") + "\",\"producers\":\"Iris\",\"winner\":true}]";
        String trailingComma = "{\"year\":2001,\"title\":\"" + t("Virgula final") + "\",\"producers\":\"Iris\",\"winner\":true,}";
        sendBase();
        sendCase("truncado", truncated);
        sendCase("array", array);
        sendCase("nulo-json", "null");
        sendBytes(k("zero-bytes"), new byte[0]);
        sendCase("virgula-final", trailingComma);
        sendControl();
        awaitConsumed();
        expectErrors(rejected(k("truncado"), "NOT_JSON", truncated), rejected(k("array"), "NOT_JSON", array),
                rejected(k("nulo-json"), "NOT_JSON", "null"), rejected(k("zero-bytes"), "NOT_JSON", new byte[0]),
                rejected(k("virgula-final"), "NOT_JSON", trailingComma));
        expectBaseAndControlOnly();
    }

    /** Caso 02: dois objetos JSON no mesmo valor. */
    private void case02ConcatenatedJson() {
        String twoRoots = "{\"year\":2001,\"title\":\"" + t("Primeira") + "\",\"producers\":\"Iris\",\"winner\":true}"
                + "{\"year\":2001,\"title\":\"" + t("Segunda") + "\",\"producers\":\"Iris\",\"winner\":true}";
        sendBase();
        sendCase("duas-raizes", twoRoots);
        sendControl();
        awaitConsumed();
        expectErrors(rejected(k("duas-raizes"), "NOT_JSON", twoRoots));
        expectBaseAndControlOnly();
    }

    /** Caso 03: UTF-8 inválido dentro de uma string. */
    private void case03InvalidUtf8() {
        byte[] invalid = bytes(utf8("{\"year\":2001,\"title\":\"" + t("X")), hex("C328"),
                utf8("Y\",\"producers\":\"Iris\",\"winner\":true}"));
        sendBase();
        sendBytes(k("utf8"), invalid);
        sendControl();
        awaitConsumed();
        expectErrors(rejected(k("utf8"), "NOT_UTF8", invalid));
        expectBaseAndControlOnly();
    }

    /** Caso 04: campos obrigatórios ausentes. */
    private void case04MissingFields() {
        String noYear = "{\"title\":\"" + t("sem-ano") + "\",\"producers\":\"Iris\",\"winner\":true}";
        String noTitle = "{\"year\":2001,\"producers\":\"Iris\",\"winner\":true}";
        String noProducers = "{\"year\":2001,\"title\":\"" + t("sem-produtores") + "\",\"winner\":true}";
        String noWinner = "{\"year\":2001,\"title\":\"" + t("sem-winner") + "\",\"producers\":\"Iris\"}";
        sendBase();
        sendCase("sem-ano", noYear);
        sendCase("sem-titulo", noTitle);
        sendCase("sem-produtores", noProducers);
        sendCase("sem-winner", noWinner);
        sendControl();
        awaitConsumed();
        expectErrors(rejected(k("sem-ano"), "YEAR_MISSING", noYear), rejected(k("sem-titulo"), "TITLE_MISSING", noTitle),
                rejected(k("sem-produtores"), "PRODUCERS_MISSING", noProducers),
                rejected(k("sem-winner"), "WINNER_INVALID", noWinner));
        expectBaseAndControlOnly();
    }

    /**
     * Caso 05: título, produtores e estúdios com tipos errados. Achado decidido pelo Anderson: tipo não
     * textual é {@code FIELD_TYPE_INVALID}, e {@code null} vale ausente.
     */
    private void case05TextFieldTypes() {
        String titleNumber = "{\"year\":2001,\"title\":17,\"producers\":\"Iris\",\"winner\":true}";
        String producersArray = "{\"year\":2001,\"title\":\"" + t("produtores-array") + "\",\"producers\":[\"Iris\"],\"winner\":true}";
        String studiosObject = "{\"year\":2001,\"title\":\"" + t("estudios-objeto") + "\",\"producers\":\"Iris\","
                + "\"winner\":true,\"studios\":{\"nome\":\"X\"}}";
        String titleNull = "{\"year\":2001,\"title\":null,\"producers\":\"Iris\",\"winner\":true}";
        String producersNull = "{\"year\":2001,\"title\":\"" + t("produtores-nulos") + "\",\"producers\":null,\"winner\":true}";
        sendBase();
        sendCase("titulo-numero", titleNumber);
        sendCase("produtores-array", producersArray);
        sendCase("estudios-objeto", studiosObject);
        sendCase("titulo-nulo", titleNull);
        sendCase("produtores-nulos", producersNull);
        sendControl();
        awaitConsumed();
        expectErrors(rejected(k("titulo-numero"), "FIELD_TYPE_INVALID", titleNumber),
                rejected(k("produtores-array"), "FIELD_TYPE_INVALID", producersArray),
                rejected(k("estudios-objeto"), "FIELD_TYPE_INVALID", studiosObject),
                rejected(k("titulo-nulo"), "TITLE_MISSING", titleNull),
                rejected(k("produtores-nulos"), "PRODUCERS_MISSING", producersNull));
        expectBaseAndControlOnly();
    }

    /** Caso 06: representações ambíguas de {@code winner}. */
    private void case06WinnerTypes() {
        List<String> values = List.of("\"yes\"", "\" YES \"", "\"true\"", "\"false\"", "\"\"", "1", "0", "null");
        List<Rejected> expected = new ArrayList<>();
        sendBase();
        for (int i = 0; i < values.size(); i++) {
            String json = "{\"year\":2001,\"title\":\"" + t("winner-" + i) + "\",\"producers\":\"Iris\",\"winner\":"
                    + values.get(i) + "}";
            sendCase("winner-" + i, json);
            expected.add(rejected(k("winner-" + i), "WINNER_INVALID", json));
        }
        sendControl();
        awaitConsumed();
        expectErrors(expected.toArray(Rejected[]::new));
        expectBaseAndControlOnly();
    }

    /**
     * Caso 07: ano textual, fracionário, decimal integral, com expoente e booleano. Achado do expoente
     * decidido pelo Anderson: só o token de inteiro JSON é aceito.
     */
    private void case07YearTypes() {
        String text = "{\"year\":\"2001\",\"title\":\"" + t("ano-string") + "\",\"producers\":\"Iris\",\"winner\":true}";
        String fraction = "{\"year\":2001.9,\"title\":\"" + t("Fracao") + "\",\"producers\":\"Iris\",\"winner\":true}";
        String decimal = "{\"year\":2001.0,\"title\":\"" + t("Decimal integral") + "\",\"producers\":\"Iris\",\"winner\":true}";
        String exponent = "{\"year\":2.001e3,\"title\":\"" + t("Expoente") + "\",\"producers\":\"Iris\",\"winner\":true}";
        String bool = "{\"year\":true,\"title\":\"" + t("ano-bool") + "\",\"producers\":\"Iris\",\"winner\":true}";
        sendBase();
        sendCase("ano-string", text);
        sendCase("ano-fracao", fraction);
        sendCase("ano-integral-decimal", decimal);
        sendCase("ano-expoente", exponent);
        sendCase("ano-bool", bool);
        sendControl();
        awaitConsumed();
        expectErrors(rejected(k("ano-string"), "YEAR_NOT_NUMERIC", text),
                rejected(k("ano-fracao"), "YEAR_NOT_NUMERIC", fraction),
                rejected(k("ano-integral-decimal"), "YEAR_NOT_NUMERIC", decimal),
                rejected(k("ano-expoente"), "YEAR_NOT_NUMERIC", exponent),
                rejected(k("ano-bool"), "YEAR_NOT_NUMERIC", bool));
        expectBaseAndControlOnly();
    }

    /** Caso 08: anos 1 e 9999, fora da faixa e maiores que um inteiro de 32 e 64 bits. */
    private void case08YearLimits() {
        String zero = "{\"year\":0,\"title\":\"" + t("Zero") + "\",\"producers\":\"Iris\",\"winner\":true}";
        String above = "{\"year\":10000,\"title\":\"" + t("Acima") + "\",\"producers\":\"Iris\",\"winner\":true}";
        String negative = "{\"year\":-1,\"title\":\"" + t("Negativo") + "\",\"producers\":\"Iris\",\"winner\":true}";
        String huge = "{\"year\":2147485649,\"title\":\"" + t("Grande") + "\",\"producers\":\"Iris\",\"winner\":true}";
        String immense = "{\"year\":99999999999999999999999999999999999999999999,\"title\":\"" + t("Imenso")
                + "\",\"producers\":\"Iris\",\"winner\":true}";
        sendCase("9999|Ultimo", "{\"year\":9999,\"title\":\"" + t("Ultimo") + "\",\"producers\":\"Iris\",\"winner\":true}");
        sendCase("0|Zero", zero);
        sendCase("10000|Acima", above);
        sendCase("-1|Negativo", negative);
        sendCase("ano-gigante", huge);
        sendCase("ano-imenso", immense);
        sendCase("1|Primeiro", "{\"year\":1,\"title\":\"" + t("Primeiro") + "\",\"producers\":\"Iris\",\"winner\":true}");
        awaitConsumed();
        expectErrors(rejected(k("0|Zero"), "YEAR_OUT_OF_RANGE", zero), rejected(k("10000|Acima"), "YEAR_OUT_OF_RANGE", above),
                rejected(k("-1|Negativo"), "YEAR_OUT_OF_RANGE", negative),
                rejected(k("ano-gigante"), "YEAR_OUT_OF_RANGE", huge),
                rejected(k("ano-imenso"), "YEAR_OUT_OF_RANGE", immense));
        expectMovies(movie(1, t("Primeiro"), null, "Iris", true), movie(9999, t("Ultimo"), null, "Iris", true));
        List<IntervalItem> iris = List.of(item("Iris", 9998, 1, 9999));
        expectIntervals(iris, iris);
    }

    /** Caso 09: campos extras, {@code type} explícito e chave que não identifica o filme. */
    private void case09ExtraFieldsAndKeys() {
        sendCase("chave-alheia", "{\"type\":\"movie\",\"year\":2000,\"title\":\"" + t("Base") + "\",\"studios\":\"Estudio A\","
                + "\"producers\":\"Iris\",\"winner\":true,\"extra\":{\"winner\":false,\"year\":\"inválido\"},\"id\":99}");
        awaitConsumed();
        expectMovies(movie(2000, t("Base"), "Estudio A", "Iris", true));
        sendCase("chave-alheia", "{\"year\":2000,\"title\":\"" + t("Outro") + "\",\"studios\":null,\"producers\":\"Iris\","
                + "\"winner\":true,\"count\":[1,2]}");
        sendBytes(null, utf8("{\"year\":2000,\"title\":\"" + t("Base") + "\",\"studios\":\"\",\"producers\":\"Iris\","
                + "\"winner\":true,\"runId\":123}"));
        awaitConsumed();
        expectErrors();
        expectMovies(movie(2000, t("Base"), null, "Iris", true), movie(2000, t("Outro"), null, "Iris", true));
        List<IntervalItem> iris = List.of(item("Iris", 0, 2000, 2000));
        expectIntervals(iris, iris);
    }

    /**
     * Caso 10: {@code type} desconhecido, de tipo errado e nulo. Achado decidido pelo Anderson:
     * {@code TYPE_UNKNOWN} para o que não é {@code movie} nem {@code load-completed}; {@code null} vale ausente.
     */
    private void case10EventType() {
        String uppercase = "{\"year\":2001,\"title\":\"" + t("tipo-estranho") + "\",\"producers\":\"Iris\",\"winner\":true,"
                + "\"type\":\"Movie\"}";
        String number = "{\"year\":2001,\"title\":\"" + t("tipo-numero") + "\",\"producers\":\"Iris\",\"winner\":true,\"type\":7}";
        sendBase();
        sendCase("tipo-estranho", uppercase);
        sendCase("tipo-numero", number);
        sendCase("tipo-nulo", "{\"year\":2001,\"title\":\"" + t("tipo-nulo") + "\",\"producers\":\"Iris\",\"winner\":true,"
                + "\"type\":null}");
        sendControl();
        awaitConsumed();
        expectErrors(rejected(k("tipo-estranho"), "TYPE_UNKNOWN", uppercase), rejected(k("tipo-numero"), "TYPE_UNKNOWN", number));
        expectMovies(base(), movie(2001, t("tipo-nulo"), null, "Iris", true), control());
        expectIrisWonIn2001();
    }

    /** Caso 11: propriedades repetidas. Achado decidido pelo Anderson: {@code NOT_JSON}. */
    private void case11DuplicateKeys() {
        String duplicateA = "{\"year\":2001,\"title\":\"" + t("Dup A") + "\",\"producers\":\"Iris\",\"winner\":true,\"winner\":false}";
        String duplicateB = "{\"year\":2001,\"title\":\"" + t("Dup B") + "\",\"producers\":\"Iris\",\"winner\":false,\"winner\":true}";
        sendBase();
        sendCase("dup-winner-a", duplicateA);
        sendCase("dup-winner-b", duplicateB);
        sendControl();
        awaitConsumed();
        expectErrors(rejected(k("dup-winner-a"), "NOT_JSON", duplicateA), rejected(k("dup-winner-b"), "NOT_JSON", duplicateB));
        expectBaseAndControlOnly();
    }

    /** Caso 12: BOM UTF-8 antes do objeto. Achado decidido pelo Anderson: {@code NOT_JSON}. */
    private void case12JsonBom() {
        byte[] withBom = bytes(hex("EFBBBF"),
                utf8("{\"year\":2001,\"title\":\"" + t("BOM") + "\",\"producers\":\"Iris\",\"winner\":true}"));
        sendBase();
        sendBytes(k("2001|BOM"), withBom);
        sendControl();
        awaitConsumed();
        expectErrors(rejected(k("2001|BOM"), "NOT_JSON", withBom));
        expectBaseAndControlOnly();
    }

    /**
     * Caso 13: U+0000 escapado nos campos, inclusive nas pontas, e NUL literal. Conflito entre a spec 001 e o
     * data-model da spec 003 decidido pelo Anderson: rejeitado em qualquer posição.
     */
    private void case13NulCharacter() {
        String nulTitle = "{\"year\":2001,\"title\":\"\\u0000" + t("X") + "\",\"producers\":\"Iris\",\"winner\":true}";
        String nulStudios = "{\"year\":2001,\"title\":\"" + t("X") + "\",\"studios\":\"A\\u0000B\",\"producers\":\"Iris\",\"winner\":true}";
        String nulProducers = "{\"year\":2001,\"title\":\"" + t("X") + "\",\"producers\":\"Iris\\u0000\",\"winner\":true}";
        byte[] literal = bytes(utf8("{\"year\":2001,\"title\":\"" + t("A")), hex("00"),
                utf8("B\",\"producers\":\"Iris\",\"winner\":true}"));
        sendBase();
        sendCase("nul-titulo", nulTitle);
        sendCase("nul-estudios", nulStudios);
        sendCase("nul-produtores", nulProducers);
        sendBytes(k("nul-literal"), literal);
        sendControl();
        awaitConsumed();
        expectErrors(rejected(k("nul-titulo"), "NUL_CHARACTER", nulTitle),
                rejected(k("nul-estudios"), "NUL_CHARACTER", nulStudios),
                rejected(k("nul-produtores"), "NUL_CHARACTER", nulProducers),
                rejected(k("nul-literal"), "NOT_JSON", literal));
        expectBaseAndControlOnly();
    }

    /**
     * Caso 14: NFC e NFD em títulos e nomes, sem normalização. O oráculo do caso escreve os dois produtores
     * em NFC; na resposta, o NFD vem antes pela FR-015 da spec 001 ({@code e} < {@code é}).
     */
    private void case14UnicodeNormalization() {
        sendCase("2000|Café", "{\"year\":2000,\"title\":\"" + t("Café") + "\",\"producers\":\"" + NFC_JOSE
                + "\",\"winner\":true}");
        // Escapes JSON (barra invertida, u, 0301): o JSON decodifica a marca combinante.
        sendCase("nfd", "{\"year\":2000,\"title\":\"" + t("Cafe\\u0301") + "\",\"producers\":\"Jose\\u0301\",\"winner\":true}");
        sendCase("nfd-final", "{\"year\":2002,\"title\":\"" + t("Fim D") + "\",\"producers\":\"Jose\\u0301\",\"winner\":true}");
        sendCase("2002|Fim C", "{\"year\":2002,\"title\":\"" + t("Fim C") + "\",\"producers\":\"" + NFC_JOSE + "\",\"winner\":true}");
        awaitConsumed();
        expectErrors();
        expectMovies(movie(2000, t("Café"), null, NFC_JOSE, true), movie(2000, t("Café"), null, NFD_JOSE, true),
                movie(2002, t("Fim D"), null, NFD_JOSE, true), movie(2002, t("Fim C"), null, NFC_JOSE, true));
        List<IntervalItem> both = List.of(item(NFD_JOSE, 2, 2000, 2002), item(NFC_JOSE, 2, 2000, 2002));
        expectIntervals(both, both);
    }

    /**
     * Caso 15: NBSP, U+200B e espaço ASCII. Achado decidido pelo Anderson: só U+0001 a U+0020 são espaços em
     * volta; NBSP e U+200B são texto. O título só com NBSP é o dado testado e fica sem prefixo.
     */
    private void case15InvisibleSpaces() {
        String asciiOnly = "{\"year\":2001,\"title\":\"   \",\"producers\":\"Iris\",\"winner\":true}";
        sendBase();
        sendCase("so-nbsp", "{\"year\":2001,\"title\":\"" + NBSP + "\",\"producers\":\"Iris\",\"winner\":true}");
        sendCase("produtor-zwsp", "{\"year\":2001,\"title\":\"" + t("produtor-zwsp") + "\",\"producers\":\"" + ZWSP
                + "\",\"winner\":true}");
        sendCase("iris-nbsp", "{\"year\":2001,\"title\":\"" + t("iris-nbsp") + "\",\"producers\":\"" + NBSP + "Iris" + NBSP
                + "\",\"winner\":true}");
        sendCase("so-ascii", asciiOnly);
        sendControl();
        awaitConsumed();
        expectErrors(rejected(k("so-ascii"), "TITLE_MISSING", asciiOnly));
        expectMovies(base(), movie(2001, NBSP, null, "Iris", true), movie(2001, t("produtor-zwsp"), null, ZWSP, true),
                movie(2001, t("iris-nbsp"), null, NBSP + "Iris" + NBSP, true), control());
        // Iris vence em 2000, 2001 e 2002. O produtor U+200B e o Iris com NBSP têm uma vitória cada.
        expectIrisWonIn2001();
    }

    /** Caso 16: fronteiras ASCII. O título de 500 letras é o dado testado e fica sem prefixo. */
    private void case16AsciiLengths() {
        String title501 = event2001("A".repeat(501), "Iris", true, null);
        String studios1001 = event2001(t("estudios-1001"), "Iris", true, "S".repeat(1001));
        String producers1001 = event2001(t("produtores-1001"), "P".repeat(500) + "," + "Q".repeat(500), true, null);
        String name501 = event2001(t("nome-501"), "N".repeat(501), true, null);
        sendBase();
        sendCase("titulo-500", event2001("A".repeat(500), "Iris", false, null));
        sendCase("titulo-501", title501);
        sendCase("estudios-1000", event2001(t("estudios-1000"), "Iris", false, "S".repeat(1000)));
        sendCase("estudios-1001", studios1001);
        sendCase("produtores-1000", event2001(t("produtores-1000"), "P".repeat(499) + "," + "Q".repeat(500), false, null));
        sendCase("produtores-1001", producers1001);
        sendCase("nome-500", event2001(t("nome-500"), "N".repeat(500), false, null));
        sendCase("nome-501", name501);
        sendControl();
        awaitConsumed();
        List<Record> errors = expectErrors(rejected(k("titulo-501"), "FIELD_TOO_LONG", title501),
                rejected(k("estudios-1001"), "FIELD_TOO_LONG", studios1001),
                rejected(k("produtores-1001"), "FIELD_TOO_LONG", producers1001),
                rejected(k("nome-501"), "FIELD_TOO_LONG", name501));
        // O motivo identifica o campo: título, estúdios, texto dos produtores e nome individual.
        softly.assertThat(reason(errors, k("titulo-501"))).as(c("motivo do título")).contains("título com 501");
        softly.assertThat(reason(errors, k("estudios-1001"))).as(c("motivo dos estúdios")).contains("estúdios com 1001");
        softly.assertThat(reason(errors, k("produtores-1001"))).as(c("motivo dos produtores")).contains("produtores com 1001");
        softly.assertThat(reason(errors, k("nome-501"))).as(c("motivo do nome")).contains("nome de produtor com 501");
        expectMovies(base(), movie(2001, "A".repeat(500), null, "Iris", false),
                movie(2001, t("estudios-1000"), "S".repeat(1000), "Iris", false),
                movie(2001, t("produtores-1000"), null, "P".repeat(499) + "," + "Q".repeat(500), false),
                movie(2001, t("nome-500"), null, "N".repeat(500), false), control());
        expectControlIntervals();
    }

    /**
     * Caso 17: emoji e a unidade da contagem. Achado decidido pelo Anderson: unidades UTF-16, como o H2. Os
     * títulos de emojis são o dado testado e ficam sem prefixo.
     */
    private void case17UnicodeLength() {
        String emoji251 = titled2001(CLAPPER.repeat(251), true);
        String emoji500 = titled2001(CLAPPER.repeat(500), true);
        String emoji501 = titled2001(CLAPPER.repeat(501), true);
        sendBase();
        sendCase("emoji-curto", "{\"year\":2001,\"title\":\"" + CLAPPER + "\",\"producers\":\"" + CLAPPER
                + "\",\"winner\":false,\"studios\":\"" + CLAPPER + "\"}");
        sendCase("emoji-250", titled2001(CLAPPER.repeat(250), false));
        sendCase("emoji-251", emoji251);
        sendCase("emoji-500", emoji500);
        sendCase("emoji-501", emoji501);
        sendControl();
        awaitConsumed();
        expectErrors(rejected(k("emoji-251"), "FIELD_TOO_LONG", emoji251), rejected(k("emoji-500"), "FIELD_TOO_LONG", emoji500),
                rejected(k("emoji-501"), "FIELD_TOO_LONG", emoji501));
        expectMovies(base(), movie(2001, CLAPPER, CLAPPER, CLAPPER, false), movie(2001, CLAPPER.repeat(250), null, "Iris", false),
                control());
        expectControlIntervals();
    }

    /** Caso 18: vírgulas, {@code and} repetido e com caixa mista, espaços e nomes embutidos. */
    private void case18Separators() {
        String messy = " and Iris, , Nilo AND aNd Eva, and Anderson, Robbinsand, andrew, Iris, and ";
        String clean = "Iris, Nilo, Eva, Anderson, Robbinsand, andrew";
        String noNames = "{\"year\":2001,\"title\":\"" + t("Sem nomes") + "\",\"producers\":\", and and , ,\",\"winner\":true}";
        sendCase("2000|Um", "{\"year\":2000,\"title\":\"" + t("Um") + "\",\"producers\":\"" + messy + "\",\"winner\":true}");
        sendCase("2002|Dois", "{\"year\":2002,\"title\":\"" + t("Dois") + "\",\"producers\":\"" + clean + "\",\"winner\":true}");
        sendCase("2001|Sem nomes", noNames);
        awaitConsumed();
        expectErrors(rejected(k("2001|Sem nomes"), "PRODUCERS_MISSING", noNames));
        // A listagem traz o texto da coluna como recebido, sem os espaços em volta.
        expectMovies(movie(2000, t("Um"), null, messy.strip(), true), movie(2002, t("Dois"), null, clean, true));
        List<IntervalItem> six = List.of(item("Anderson", 2, 2000, 2002), item("Eva", 2, 2000, 2002),
                item("Iris", 2, 2000, 2002), item("Nilo", 2, 2000, 2002), item("Robbinsand", 2, 2000, 2002),
                item("andrew", 2, 2000, 2002));
        expectIntervals(six, six);
    }

    /** Caso 19: tabulação em volta do {@code and}. Achado decidido pelo Anderson: separa os nomes. */
    private void case19TabAroundAnd() {
        sendBase();
        // Escape JSON da tabulação: o valor decodificado tem tabulações reais.
        sendCase("and-tab", "{\"year\":2001,\"title\":\"" + t("Tabs") + "\",\"producers\":\"Iris\\tand\\tNilo\",\"winner\":true}");
        sendControl();
        awaitConsumed();
        expectErrors();
        expectMovies(base(), movie(2001, t("Tabs"), null, "Iris\tand\tNilo", true), control());
        // Iris vence em 2000, 2001 e 2002; Nilo tem uma vitória e fica fora.
        expectIrisWonIn2001();
    }

    /** Caso 20: reentrega byte a byte antes e depois de outra vitória, com o ETag. */
    private void case20IdenticalReplay() {
        String baseTwice = "{\"year\":2000,\"title\":\"" + t("Base") + "\",\"producers\":\"Iris, Iris\",\"winner\":true}";
        sendCase("2000|Base", baseTwice);
        sendCase("2000|Base", baseTwice);
        awaitConsumed();
        expectMovies(movie(2000, t("Base"), null, "Iris, Iris", true));
        expectIntervals(List.of(), List.of());
        sendControl();
        awaitConsumed();
        expectControlIntervals();
        String etag = etag();
        sendCase("2000|Base", baseTwice);
        awaitConsumed();
        softly.assertThat(conditionalStatus(etag)).as(c("GET condicional depois da reentrega")).isEqualTo(304);
        softly.assertThat(etag()).as(c("ETag depois da reentrega")).isEqualTo(etag);
        expectControlIntervals();
        expectErrors();
        expectMovies(movie(2000, t("Base"), null, "Iris, Iris", true), control());
    }

    /** Caso 21: atualização pelo título com espaços em volta, troca de vínculos e replay do estado antigo. */
    private void case21UpdateAndReplay() {
        String baseA = "{\"year\":2000,\"title\":\"" + t("Base") + "\",\"producers\":\"Iris\",\"winner\":true,\"studios\":\"A\"}";
        sendCase("2000|Base", baseA);
        sendControl();
        awaitConsumed();
        expectControlIntervals();
        String etagWithIris = etag();
        sendCase("chave-nova", "{\"year\":2000,\"title\":\" " + t("Base") + " \",\"studios\":\"B\",\"producers\":\"Nilo\",\"winner\":false}");
        awaitConsumed();
        expectMovies(movie(2000, t("Base"), "B", "Nilo", false), control());
        expectIntervals(List.of(), List.of());
        sendCase("chave-nova", "{\"year\":2000,\"title\":\" " + t("Base") + " \",\"studios\":\"B\",\"producers\":\"Nilo\",\"winner\":true}");
        awaitConsumed();
        expectMovies(movie(2000, t("Base"), "B", "Nilo", true), control());
        expectIntervals(List.of(), List.of());
        String etagEmpty = etag();
        sendCase("2000|Base", baseA);
        awaitConsumed();
        softly.assertThat(conditionalStatus(etagEmpty)).as(c("GET condicional com o ETag da resposta vazia")).isEqualTo(200);
        softly.assertThat(etag()).as(c("ETag do corpo final, igual ao de antes das atualizações")).isEqualTo(etagWithIris)
                .isNotEqualTo(etagEmpty);
        expectControlIntervals();
        expectErrors();
        expectMovies(movie(2000, t("Base"), "A", "Iris", true), control());
    }

    /** Caso 27: empates em min e max, vitórias no mesmo ano e ordem Unicode dos nomes. */
    private void case27TiesAndOrder() {
        String four = "Ana, Zeca, ana, Álvaro";
        sendCase("2010|Tarde", event(2010, t("Tarde"), four, true));
        sendCase("2002|Meio", event(2002, t("Meio"), four, true));
        sendCase("2000|Duas", event(2000, t("Duas"), four, true));
        sendCase("2000|Uma", event(2000, t("Uma"), four, true));
        sendCase("2001|Indicacao", event(2001, t("Indicacao"), four, false));
        sendCase("1999|Outra base", event(1999, t("Outra base"), "Nilo", true));
        sendCase("1999|Outra no mesmo ano", event(1999, t("Outra no mesmo ano"), "Nilo", true));
        sendCase("2001|Outro meio", event(2001, t("Outro meio"), "Nilo", true));
        sendCase("2009|Outro fim", event(2009, t("Outro fim"), "Nilo", true));
        awaitConsumed();
        expectErrors();
        expectMovies(movie(2010, t("Tarde"), null, four, true), movie(2002, t("Meio"), null, four, true),
                movie(2000, t("Duas"), null, four, true), movie(2000, t("Uma"), null, four, true),
                movie(2001, t("Indicacao"), null, four, false), movie(1999, t("Outra base"), null, "Nilo", true),
                movie(1999, t("Outra no mesmo ano"), null, "Nilo", true), movie(2001, t("Outro meio"), null, "Nilo", true),
                movie(2009, t("Outro fim"), null, "Nilo", true));
        List<IntervalItem> min = List.of(item("Nilo", 0, 1999, 1999), item("Ana", 0, 2000, 2000),
                item("Zeca", 0, 2000, 2000), item("ana", 0, 2000, 2000), item("Álvaro", 0, 2000, 2000));
        List<IntervalItem> max = List.of(item("Nilo", 8, 2001, 2009), item("Ana", 8, 2002, 2010),
                item("Zeca", 8, 2002, 2010), item("ana", 8, 2002, 2010), item("Álvaro", 8, 2002, 2010));
        expectIntervals(min, max);
    }

    /** Caso 28: uma atualização inválida preserva o filme existente. */
    private void case28InvalidUpdate() {
        String invalidUpdate = "{\"year\":2000,\"title\":\"" + t("Base") + "\",\"studios\":\"B\",\"producers\":\" , and \",\"winner\":false}";
        sendCase("2000|Base", "{\"year\":2000,\"title\":\"" + t("Base") + "\",\"producers\":\"Iris\",\"winner\":true,\"studios\":\"A\"}");
        sendControl();
        awaitConsumed();
        expectControlIntervals();
        String etag = etag();
        sendCase("2000|Base", invalidUpdate);
        awaitConsumed();
        expectMovies(movie(2000, t("Base"), "A", "Iris", true), control());
        expectControlIntervals();
        softly.assertThat(etag()).as(c("ETag depois da atualização inválida")).isEqualTo(etag);
        sendCase("2001|Novo", "{\"year\":2001,\"title\":\"" + t("Novo") + "\",\"producers\":\"Iris\",\"winner\":true}");
        awaitConsumed();
        softly.assertThat(conditionalStatus(etag)).as(c("GET condicional com o ETag anterior")).isEqualTo(200);
        softly.assertThat(etag()).as(c("ETag do corpo final")).isNotEqualTo(etag);
        expectErrors(rejected(k("2000|Base"), "PRODUCERS_MISSING", invalidUpdate));
        expectMovies(movie(2000, t("Base"), "A", "Iris", true), movie(2001, t("Novo"), null, "Iris", true), control());
        expectIrisWonIn2001();
    }

    /** Caso 30: três vitórias no mesmo ano dão dois pares idênticos. */
    private void case30IdenticalTies() {
        IntervalItem zero = item("Iris", 0, 2001, 2001);
        sendCase("2001|Um", event(2001, t("Um"), "Iris", true));
        sendCase("2001|Dois", event(2001, t("Dois"), "Iris", true));
        awaitConsumed();
        expectIntervals(List.of(zero), List.of(zero));
        sendCase("2001|Tres", event(2001, t("Tres"), "Iris", true));
        awaitConsumed();
        expectIntervals(List.of(zero, zero), List.of(zero, zero));
        sendCase("2001|Dois", event(2001, t("Dois"), "Iris", true));
        awaitConsumed();
        expectIntervals(List.of(zero, zero), List.of(zero, zero));
        expectErrors();
        expectMovies(movie(2001, t("Um"), null, "Iris", true), movie(2001, t("Dois"), null, "Iris", true),
                movie(2001, t("Tres"), null, "Iris", true));
    }

    // ---------------------------------------------------------------------------------------------------
    // Execução e isolamento de cada caso

    private void run(String caseId, Runnable body) {
        id = caseId;
        moviesAtStart = allMoviesPaged();
        errorsAtStart = (int) KafkaTestSupport.count(brokers(), ERRORS_TOPIC);
        try {
            body.run();
            softly.assertThat(readinessStatus()).as(c("readiness")).isEqualTo(200);
        } catch (RuntimeException | AssertionError e) {
            softly.fail(c("interrompido: " + e), e);
        }
        retireWinners();
    }

    /** Reenvia todo vencedor gravado como indicado, para que os intervalos do caso seguinte só tenham os dele. */
    private void retireWinners() {
        List<MovieJson> winners = allMoviesPaged().stream().filter(MovieJson::winner).toList();
        for (MovieJson winner : winners) {
            ObjectNode node = JSON.createObjectNode().put("year", winner.year()).put("title", winner.title())
                    .put("studios", winner.studios()).put("producers", winner.producers()).put("winner", false);
            sendBytes("isolamento", JSON.writeValueAsBytes(node));
        }
        if (!winners.isEmpty()) {
            awaitConsumed();
        }
    }

    private List<MovieJson> allMoviesPaged() {
        List<MovieJson> movies = new ArrayList<>();
        for (int page = 0; ; page++) {
            MoviesBody body = movies("size=100&page=" + page);
            movies.addAll(body.content());
            if (page + 1 >= body.totalPages()) {
                return movies;
            }
        }
    }

    // ---------------------------------------------------------------------------------------------------
    // Nomes e eventos do caso

    /** Chave do caso: {@code cNN/<chave>}. */
    private String k(String key) {
        return "c" + id + "/" + key;
    }

    /** Título do caso: {@code CNN <título>}. */
    private String t(String title) {
        return "C" + id + " " + title;
    }

    /** Mensagem da asserção, com o caso. */
    private String c(String description) {
        return "caso " + id + ": " + description;
    }

    private void sendCase(String key, String json) {
        send(k(key), json);
    }

    private void sendBase() {
        sendCase("2000|Base", "{\"year\":2000,\"title\":\"" + t("Base") + "\",\"producers\":\"Iris\",\"winner\":true}");
    }

    private void sendControl() {
        sendCase("2002|Controle", "{\"year\":2002,\"title\":\"" + t("Controle") + "\",\"producers\":\"Iris\",\"winner\":true}");
    }

    private MovieJson base() {
        return movie(2000, t("Base"), null, "Iris", true);
    }

    private MovieJson control() {
        return movie(2002, t("Controle"), null, "Iris", true);
    }

    private static String event(int year, String title, String producers, boolean winner) {
        return "{\"year\":" + year + ",\"title\":\"" + title + "\",\"producers\":\"" + producers + "\",\"winner\":" + winner + "}";
    }

    private static String event2001(String title, String producers, boolean winner, String studios) {
        return "{\"year\":2001,\"title\":\"" + title + "\",\"producers\":\"" + producers + "\",\"winner\":" + winner
                + (studios == null ? "" : ",\"studios\":\"" + studios + "\"") + "}";
    }

    private static String titled2001(String title, boolean winner) {
        return "{\"year\":2001,\"title\":\"" + title + "\",\"producers\":\"Iris\",\"winner\":" + winner + "}";
    }

    // ---------------------------------------------------------------------------------------------------
    // Conferências do caso, todas pela SoftAssertions comum

    /**
     * Os registros novos do tópico de erros são exatamente as rejeições dadas, cada uma com os bytes
     * originais, o motivo, os cabeçalhos de origem, o offset do evento no tópico de filmes e o aviso no log
     * (spec 003, FR-015). Devolve os registros novos.
     */
    private List<Record> expectErrors(Rejected... expected) {
        long total = KafkaTestSupport.count(brokers(), ERRORS_TOPIC);
        List<Record> all = total == 0 ? List.of()
                : KafkaTestSupport.read(brokers(), ERRORS_TOPIC, (int) total, Duration.ofSeconds(10));
        List<Record> errors = all.subList(Math.min(errorsAtStart, all.size()), all.size());
        List<Record> sources = KafkaTestSupport.read(brokers(), MOVIES_TOPIC,
                (int) KafkaTestSupport.count(brokers(), MOVIES_TOPIC), Duration.ofSeconds(10));
        softly.assertThat(errors).as(c("registros novos no tópico de erros")).hasSize(expected.length);
        for (Rejected rejection : expected) {
            List<Record> matching = errors.stream().filter(error -> Objects.equals(error.key(), rejection.key())).toList();
            softly.assertThat(matching).as(c(rejection.key() + " (" + rejection.reason() + ") no tópico de erros")).hasSize(1);
            if (matching.size() != 1) {
                continue;
            }
            Record error = matching.getFirst();
            String key = rejection.key();
            softly.assertThat(error.value()).as(c(key + ": bytes originais")).isEqualTo(rejection.value());
            softly.assertThat(error.headers().get("awards-error-reason")).as(c(key + ": motivo"))
                    .startsWith(rejection.reason() + ":");
            softly.assertThat(error.headers()).as(c(key + ": cabeçalhos")).containsKeys("awards-error-at",
                    "awards-source-topic", "awards-source-partition", "awards-source-offset");
            softly.assertThat(error.headers().get("awards-source-topic")).as(c(key + ": tópico")).isEqualTo(MOVIES_TOPIC);
            softly.assertThat(error.headers().get("awards-source-partition")).as(c(key + ": partição")).isEqualTo("0");
            String offset = error.headers().get("awards-source-offset");
            List<Long> sourceOffsets = sources.stream()
                    .filter(source -> Objects.equals(source.key(), key) && Arrays.equals(source.value(), rejection.value()))
                    .map(Record::offset).toList();
            softly.assertThat(sourceOffsets).as(c(key + ": offset do evento de origem"))
                    .contains(offset == null ? -1L : Long.parseLong(offset));
            softly.assertThat(output.getAll()).as(c(key + ": aviso no log")).contains("Evento rejeitado (tópico "
                    + MOVIES_TOPIC + ", partição 0, offset " + offset + "): " + rejection.reason() + ":");
        }
        return errors;
    }

    private static String reason(List<Record> errors, String key) {
        return errors.stream().filter(error -> key.equals(error.key())).map(error -> error.headers().get("awards-error-reason"))
                .findFirst().orElse(null);
    }

    /**
     * Os filmes novos ou alterados desde o início do caso são exatamente estes, e nenhum filme que já existia
     * mudou ou sumiu.
     */
    private void expectMovies(MovieJson... expected) {
        List<MovieJson> now = allMoviesPaged();
        List<MovieJson> changed = new ArrayList<>(now);
        moviesAtStart.forEach(changed::remove);
        List<MovieJson> gone = new ArrayList<>(moviesAtStart);
        now.forEach(gone::remove);
        softly.assertThat(changed).as(c("filmes gravados ou alterados pelo caso")).containsExactlyInAnyOrder(expected);
        softly.assertThat(gone).as(c("filmes de casos anteriores alterados")).isEmpty();
    }

    private void expectBaseAndControlOnly() {
        expectMovies(base(), control());
        expectControlIntervals();
    }

    private void expectIntervals(List<IntervalItem> min, List<IntervalItem> max) {
        softly.assertThat(intervals()).as(c("intervalos")).isEqualTo(new IntervalsBody(min, max));
    }

    private void expectControlIntervals() {
        List<IntervalItem> iris = List.of(item("Iris", 2, 2000, 2002));
        expectIntervals(iris, iris);
    }

    private void expectIrisWonIn2001() {
        List<IntervalItem> iris = List.of(item("Iris", 1, 2000, 2001), item("Iris", 1, 2001, 2002));
        expectIntervals(iris, iris);
    }

    private String etag() {
        return http.get().uri(INTERVALS).exchange().returnResult().getResponseHeaders().getETag();
    }

    private int conditionalStatus(String etag) {
        return http.get().uri(INTERVALS).header("If-None-Match", etag).exchange().returnResult().getStatus().value();
    }
}
