package numberrangesummarizer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Readable, single-output unit suite for the directional summarizer
 * (preserve input order, compress +1/-1 runs only).
 * Failure paths are asserted and  * shown as “caught expected …” so
 * they pass while demonstrating robustness.
 */
@DisplayName("Readable unit suite: NumberRangeSummarizer")
class NumberRangeSummarizerTest {

    private final NumberRangeSummarizer s = new NumberRangeSummarizerImpl();

    @Test
    void readableUnitSuite() {
        List<String> failures = new ArrayList<>();

        // ---------------- collect() ----------------
        System.out.println("\n── UNIT cases: collect() ─────────────────────────────────");
        int i = 1;
        i = passOrFail(failures, i, "preserves input order",
                () -> assertIterableEquals(
                        Arrays.asList(3, 2, 1, 2, 3),
                        s.collect("3, 2, 1, 2, 3")
                ), "3, 2, 1, 2, 3", "3, 2, 1, 2, 3");

        i = passOrFail(failures, i, "parses negatives",
                () -> assertIterableEquals(
                        Arrays.asList(-2, -1, 0, 1),
                        s.collect("-2,-1,0,1")
                ), "-2,-1,0,1", "-2, -1, 0, 1");

        i = passOrFail(failures, i, "blank input → empty collection",
                () -> {
                    assertTrue(s.collect("").isEmpty());
                    assertTrue(s.collect("   ").isEmpty());
                }, "(blank)", "(empty)");

        // expected failure path: bad token
        i = passOrFailExpectedThrow(failures, i,
                "caught expected NumberFormatException for bad token",
                NumberFormatException.class,
                () -> s.collect("1,a,3"));

        endBlock("UNIT collect()", failures.isEmpty());

        // ---------------- summarizeCollection() ----------------
        System.out.println("\n── UNIT cases: summarizeCollection() ──────────────────────");
        int j = 1;
        // expected failure path: null collection
        j = passOrFailExpectedThrow(failures, j,
                "caught expected IllegalArgumentException for null input",
                IllegalArgumentException.class,
                () -> s.summarizeCollection(null));

        j = passOrFail(failures, j, "empty collection → empty string",
                () -> assertEquals("", s.summarizeCollection(s.collect(""))),
                "[]", "");

        j = passOrFail(failures, j, "ascending run 1..4 → 1-4",
                () -> assertEquals("1-4", s.summarizeCollection(s.collect("1,2,3,4"))),
                "1,2,3,4", "1-4");

        j = passOrFail(failures, j, "descending run 4..1 → 4-1",
                () -> assertEquals("4-1", s.summarizeCollection(s.collect("4,3,2,1"))),
                "4,3,2,1", "4-1");

        j = passOrFail(failures, j, "jump breaks run (1,3,4 → 1, 3-4)",
                () -> assertEquals("1, 3-4", s.summarizeCollection(s.collect("1,3,4"))),
                "1,3,4", "1, 3-4");

        j = passOrFail(failures, j, "prompt example (up then down) → 1-4, 6-2",
                () -> assertEquals("1-4, 6-2", s.summarizeCollection(s.collect("1,2,3,4,6,5,4,3,2"))),
                "1,2,3,4,6,5,4,3,2", "1-4, 6-2");

        j = passOrFail(failures, j, "up then down → 2-3, 5-4",
                () -> assertEquals("2-3, 5-4", s.summarizeCollection(s.collect("2,3,5,4"))),
                "2,3,5,4", "2-3, 5-4");

        j = passOrFail(failures, j, "duplicates break run (1,1,2,3 → 1, 1-3)",
                () -> assertEquals("1, 1-3", s.summarizeCollection(s.collect("1,1,2,3"))),
                "1,1,2,3", "1, 1-3");

        j = passOrFail(failures, j, "negatives descending → -1--3",
                () -> assertEquals("-1--3", s.summarizeCollection(s.collect("-1,-2,-3"))),
                "-1,-2,-3", "-1--3");

        j = passOrFail(failures, j, "cross zero then jump → -2-1, 5",
                () -> assertEquals("-2-1, 5", s.summarizeCollection(s.collect("-2,-1,0,1,5"))),
                "-2,-1,0,1,5", "-2-1, 5");

        endBlock("UNIT summarizeCollection()", failures.isEmpty());

        // ---------------- Integration ----------------
        System.out.println("\n── UNIT cases: integration ────────────────────────────────");
        int k = 1;
        k = passOrFail(failures, k, "collect → summarize (1,2,3,5,4 → 1-3, 5-4)",
                () -> assertEquals("1-3, 5-4", s.summarizeCollection(s.collect("1,2,3,5,4"))),
                "1,2,3,5,4", "1-3, 5-4");
        endBlock("UNIT integration", failures.isEmpty());

        // ---------------- Performance (light) ----------------
        System.out.println("\n── UNIT cases: performance (light) ────────────────────────");
        int m = 1;
        m = passOrFail(failures, m, "large input summarized (starts with 1-500, 1000-…)",
                () -> {
                    StringBuilder sb = new StringBuilder();
                    for (int a = 1; a <= 500; a++) {
                        if (a > 1) sb.append(",");
                        sb.append(a);
                    }
                    for (int a = 1000; a >= 501; a--) {
                        sb.append(",").append(a);
                    }
                    String input = sb.toString();
                    Collection<Integer> nums = s.collect(input);
                    String out = s.summarizeCollection(nums);
                    assertNotNull(out);
                    assertTrue(out.startsWith("1-500, 1000-"));
                },
                "1..500 then 1000..501", "starts with 1-500, 1000-…");
        endBlock("UNIT performance", failures.isEmpty());

        // Final assertion after printing the entire report
        if (!failures.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append("Unexpected failing unit case(s): ").append(failures.size()).append("\n");
            for (String f : failures) sb.append("  - ").append(f).append("\n");
            fail(sb.toString());
        }
    }

    // ---------- helpers for printing ----------

    private static int passOrFail(List<String> failures,
                                  int idx,
                                  String description,
                                  Runnable assertion,
                                  String inputPreview,
                                  String expectedPreview) {
        try {
            assertion.run();
            System.out.println("  [PASS] [" + idx + "] " + description + "  ->  " + expectedPreview);
        } catch (AssertionError | RuntimeException t) {
            // Print a cross with quick context, and remember to fail at the end
            System.out.println("  [FAIL] [" + idx + "] " + description);
            System.out.println("      expected: " + expectedPreview);
            System.out.println("      input   : " + inputPreview);
            System.out.println("      error   : " + t.getMessage());
            failures.add("[" + idx + "] " + description);
        }
        return idx + 1;
    }

    private static int passOrFailExpectedThrow(List<String> failures,
                                               int idx,
                                               String description,
                                               Class<? extends Throwable> expectedType,
                                               Runnable codeThatThrows) {
        boolean threw = false;
        Throwable caught = null;
        try {
            codeThatThrows.run();
        } catch (Throwable t) {
            caught = t;
            threw = expectedType.isInstance(t);
        }
        if (threw) {
            // Treat as pass: we intentionally wanted this path to throw
            System.out.println("  [PASS] [" + idx + "] " + description);
        } else {
            System.out.println("  [FAIL] [" + idx + "] " + description);
            System.out.println("      expected: " + expectedType.getSimpleName());
            System.out.println("      actual  : " + (caught == null ? "no exception" : caught.getClass().getSimpleName()));
            failures.add("[" + idx + "] " + description + " (expected " + expectedType.getSimpleName() + ")");
        }
        return idx + 1;
    }

    // Prints the summary for the section
    private static void endBlock(String label, boolean passed) {
        if (passed) {
            System.out.println("[PASS] " + label + ": all cases passed");
        } else {
            System.out.println("[FAIL] " + label + ": some cases failed (see above)");
        }
    }
}
