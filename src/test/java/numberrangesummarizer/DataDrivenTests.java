package numberrangesummarizer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Data-driven tests with readable output:
 *  - Grouped by file type (YAML / JSON / TXT)
 *  - Prints a check or cross per case
 *  - Aggregates failures so the full list is visible in CI logs
 *
 * This makes quick regressions more obvious while still failing the build
 * if any case doesn't match expectations.
 */
@DisplayName("Data-driven tests (+1/-1 runs, preserve order)")
class DataDrivenTests {

    private NumberRangeSummarizer summarizer;

    @BeforeEach
    void setUp() {
        // Keep the test using exactly the same implementation as production.
        summarizer = new NumberRangeSummarizerImpl();
    }

    // ---------------- YAML ----------------

    @Test
    @DisplayName("YAML: test-cases.yaml")
    void yamlCases() throws IOException {
        System.out.println("\n── YAML cases: test-cases.yaml ───────────────────────────");
        List<String> failures = new ArrayList<>();

        try (InputStream is = resource("test-cases.yaml")) {
            assertNotNull(is, "Missing resource: test-cases.yaml (put it in src/test/resources)");
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            JsonNode root = mapper.readTree(is);
            assertNotNull(root, "test-cases.yaml is empty or invalid YAML");

            JsonNode cases = root.get("testCases");
            assertNotNull(cases, "YAML must contain a top-level array 'testCases'");

            int idx = 1;
            for (JsonNode c : cases) {
                String description = c.path("description").asText("(no description)");
                String input = c.path("input").asText(null);
                String expected = c.path("expected").asText(null);

                if (input == null || expected == null) {
                    // If the case is malformed, mark it as a failure but continue testing others.
                    failures.add("Case " + idx + " invalid (missing 'input' or 'expected'): " + description);
                    printCross(idx, description, "(invalid case)", "(invalid case)");
                    idx++;
                    continue;
                }

                String actual = runSummarizer(input);
                if (expected.equals(actual)) {
                    printTick(idx, description, expected);
                } else {
                    printCross(idx, description, expected, actual);
                    failures.add(formatFailure(idx, description, input, expected, actual));
                }
                idx++;
            }
        }

        endBlockAndAssert("YAML", failures);
    }

    // ---------------- JSON ----------------

    @Test
    @DisplayName("JSON: test-cases.json")
    void jsonCases() throws IOException {
        System.out.println("\n── JSON cases: test-cases.json ───────────────────────────");
        List<String> failures = new ArrayList<>();

        try (InputStream is = resource("test-cases.json")) {
            assertNotNull(is, "Missing resource: test-cases.json");
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(is);
            assertNotNull(root, "test-cases.json is empty or invalid JSON");

            JsonNode cases = root.get("testCases");
            assertNotNull(cases, "JSON must contain a top-level array 'testCases'");

            int idx = 1;
            for (JsonNode c : cases) {
                String description = c.path("description").asText("(no description)");
                String input = c.path("input").asText(null);
                String expected = c.path("expected").asText(null);

                if (input == null || expected == null) {
                    failures.add("Case " + idx + " invalid (missing 'input' or 'expected'): " + description);
                    printCross(idx, description, "(invalid case)", "(invalid case)");
                    idx++;
                    continue;
                }

                String actual = runSummarizer(input);
                if (expected.equals(actual)) {
                    printTick(idx, description, expected);
                } else {
                    printCross(idx, description, expected, actual);
                    failures.add(formatFailure(idx, description, input, expected, actual));
                }
                idx++;
            }
        }

        endBlockAndAssert("JSON", failures);
    }

    // ---------------- TXT ----------------

    @Test
    @DisplayName("TXT: test-cases.txt")
    void txtCases() throws IOException {
        System.out.println("\n── TXT cases: test-cases.txt ─────────────────────────────");
        List<String> failures = new ArrayList<>();

        try (InputStream is = resource("test-cases.txt")) {
            assertNotNull(is, "Missing resource: test-cases.txt");

            // Read all non-empty, non-comment lines, then consume them as input/expected pairs.
            BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            List<String> lines = new ArrayList<>();
            for (String line; (line = br.readLine()) != null; ) {
                String trimmed = line.trim();
                if (!trimmed.isEmpty() && !trimmed.startsWith("#")) {
                    lines.add(trimmed);
                }
            }

            assertTrue(lines.size() % 2 == 0,
                "test-cases.txt must have pairs of lines: input then expected");

            int idx = 1;
            for (int i = 0; i < lines.size(); i += 2) {
                String input = lines.get(i);
                String expected = lines.get(i + 1);
                String description = "txt case " + idx;

                String actual = runSummarizer(input);
                if (expected.equals(actual)) {
                    printTick(idx, description, expected);
                } else {
                    printCross(idx, description, expected, actual);
                    failures.add(formatFailure(idx, description, input, expected, actual));
                }
                idx++;
            }
        }

        endBlockAndAssert("TXT", failures);
    }

    // ---------------- helpers ----------------

    /**
     * Wrapper so all three file readers run the same pipeline.
     */
    private String runSummarizer(String input) {
        Collection<Integer> numbers = summarizer.collect(input);
        return summarizer.summarizeCollection(numbers);
    }

    private InputStream resource(String name) {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
    }

    private static void printTick(int idx, String description, String expected) {
        // Using plain text so the output stays readable in any CI environment.
        System.out.println("  [PASS] [" + idx + "] " + description + "  ->  " + expected);
    }

    private static void printCross(int idx, String description, String expected, String actual) {
        System.out.println("  [FAIL] [" + idx + "] " + description);
        System.out.println("      expected: " + expected);
        System.out.println("      actual  : " + actual);
    }

    private static String formatFailure(int idx, String description, String input, String expected, String actual) {
        return "[" + idx + "] " + description + " | input=" + input
                + " | expected=" + expected + " | actual=" + actual;
    }

    private static void endBlockAndAssert(String label, List<String> failures) {
        if (failures.isEmpty()) {
            System.out.println("[PASS] " + label + ": all cases passed");
        } else {
            System.out.println("[FAIL] " + " " + label + ": " + failures.size() + " failed case(s)");
            for (String f : failures) {
                System.out.println("    - " + f);
            }
            // Fail once, after printing everything, so the CI log is complete.
            fail(label + " had failing cases: " + failures.size());
        }
    }
}
