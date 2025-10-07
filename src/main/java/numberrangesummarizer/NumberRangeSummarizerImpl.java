package numberrangesummarizer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Implementation that preserves input order and compresses only strictly
 * monotonic runs with a step of +1 or -1. Any other step (including 0)
 * breaks the run and starts a new segment.
 *
 * Intentionally do not sort or globally de-duplicate. 
 * Only compress true runs in the exact sequence direction.
 */
public final class NumberRangeSummarizerImpl implements NumberRangeSummarizer {

    @Override
    public Collection<Integer> collect(final String input) {
        // Treat null/blank as "no numbers". This keeps the callers simple.
        if (input == null || input.trim().isEmpty()) {
            return new ArrayList<>();
        }

        // Parse tokens in order, rejecting anything that isn't a valid int.
        // Does not sort or remove duplicates here.
        final List<Integer> out = new ArrayList<>();
        Arrays.stream(input.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .forEach(token -> {
                try {
                    out.add(Integer.valueOf(token));
                } catch (NumberFormatException e) {
                    throw new NumberFormatException("Invalid integer token: '" + token + "'");
                }
            });

        return out;
    }

    @Override
    public String summarizeCollection(final Collection<Integer> input) {
        // Checks if input in null.
        if (input == null) {
            throw new IllegalArgumentException("input must not be null");
        }
        if (input.isEmpty()) {
            return "";
        }

        // O(1) index access for a simple single pass.
        final List<Integer> seq = (input instanceof List)
                ? (List<Integer>) input
                : new ArrayList<>(input);

        // Turn the list into monotonic runs, then renders those runs.
        final List<Run> runs = toRuns(seq);
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < runs.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(runs.get(i).toDisplay());
        }
        return sb.toString();
    }

    /**
     * Convert the raw sequence into runs where each consecutive difference is either +1 or -1.
     *
     * Implementation details:
     * - Keeps track of the current "step" once established (+1 or -1).
     * - If the next difference matches the step, the run continues.
     * - Otherwise (duplicate, jump, or direction change), we close the current run and 
     *   start a fresh one at the current value.
     */
    private List<Run> toRuns(final List<Integer> seq) {
        final List<Run> runs = new ArrayList<>();

        // Make the start the first value of the sequence.
        int start = seq.get(0);
        int prev = start;

        // "step" is null until we see the first valid +1/-1 difference.
        Integer step = null;

        for (int i = 1; i < seq.size(); i++) {
            final int cur = seq.get(i);
            final int diff = cur - prev;

            if (step == null) {
                // We haven't committed to a direction yet. Only +1/-1 starts a run.
                if (diff == 1 || diff == -1) {
                    step = diff;
                    prev = cur;
                } else {
                    // Duplicate or jump: output the singleton and move on.
                    runs.add(new Run(start, prev));
                    start = cur;
                    prev = cur;
                    step = null;
                }
            } else {
                // We have a direction; only exact matches continue the run.
                if (diff == step.intValue()) {
                    prev = cur;
                } else {
                    // Direction changed or a jump happened: close and restart.
                    runs.add(new Run(start, prev));
                    start = cur;
                    prev = cur;
                    step = null;
                }
            }
        }

        // Flush the final run that was in progress.
        runs.add(new Run(start, prev));
        return runs;
    }

    /**
     * Closed interval representing one run. May be ascending (from < to),
     * descending (from > to), or a singleton (from == to).
     *
     * Encapsulated as a tiny value object so formatting is kept in one place.
     */
    static final class Run {
        private final int from;
        private final int to;

        Run(final int from, final int to) {
            this.from = from;
            this.to = to;
        }

        /**
         * Render the run in compact form. Singletons show as "n"; ranges as "a-b".
         */
        String toDisplay() {
            return (from == to) ? String.valueOf(from) : (from + "-" + to);
        }
    }
}