package numberrangesummarizer;

import java.util.Collection;
import java.util.Scanner;

/**
 * Interactive mode for manual testing.
 *
 * You can run it with no args and type lines, or pass a single CSV as an arg.
 * This CLI uses the same summarizer implementation as production.
 */
public class NumberRangeSummarizerCLI {

    private final NumberRangeSummarizer summarizer = new NumberRangeSummarizerImpl();

    public static void main(String[] args) {
        NumberRangeSummarizerCLI cli = new NumberRangeSummarizerCLI();

        // If a single line is passed as an argument, just process it and exit.
        if (args.length > 0) {
            cli.processAndPrint(args[0]);
            return;
        }

        // Otherwise, enter a loop.
        System.out.println("\n");
        System.out.println(
            "╔═══════════════════════════════════════════════════╗");
        System.out.println(
            "║     Number Range Summarizer - Interactive Mode    ║");
        System.out.println(
            "╚═══════════════════════════════════════════════════╝");
        System.out.println("Enter comma-delimited numbers (or 'quit' to exit)");
        System.out.println("Example: 1,3,6,7,8,12,13,14,15");
        System.out.println();

        try (Scanner sc = new Scanner(System.in)) {
            while (true) {
                System.out.print("Input: ");
                if (!sc.hasNextLine()) {
                    break; // EOF (Ctrl+D / Ctrl+Z)
                }
                String line = sc.nextLine();
                if (line == null) {
                    break;
                }
                String trimmed = line.trim();
                if (trimmed.isEmpty()) {
                    continue; // ignore empty lines to reduce noise
                }
                if ("quit".equalsIgnoreCase(trimmed)) {
                    break;
                }
                cli.processAndPrint(trimmed);
                System.out.println();
            }
        }
    }

    /**
     * Helper that wires together collect() and summarizeCollection(), and
     * catches common exceptions to print a message instead of a stacktrace.
     */
    private void processAndPrint(String input) {
        try {
            Collection<Integer> numbers = summarizer.collect(input);
            String result = summarizer.summarizeCollection(numbers);
            System.out.println("Result: " + result);
        } catch (NumberFormatException e) {
            System.err.println("Error: " + e.getMessage());
        } catch (Exception e) {
            // Keep this broad so the CLI never explodes in the user’s face.
            System.err.println("Unexpected error: " + e.getMessage());
        }
    }
}
