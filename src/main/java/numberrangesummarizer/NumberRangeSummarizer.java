package numberrangesummarizer;

import java.util.Collection;

/**
 * Summarizing a sequence of integers into compact ranges.
 *
 * We intentionally preserve the original input order and only compress
 * strictly monotonic runs where each step is exactly +1 or -1.
 * Any other step (including duplicates or jumps like +2/-3) breaks the run.
 *
 * Examples:
 *   1,2,3,4,6,5,4,3,2  -> "1-4, 6-2"
 *   2,3,5,4            -> "2-3, 5-4"
 */
public interface NumberRangeSummarizer {

    /**
     * Parse a comma-separated string into integers while preserving the
     * sequence order exactly as provided.
     *
     * Design notes:
     * - Doe not sort or globally de-duplicate here, because the summarization logic 
     *  relies on the original order to detect directional runs.
     * - Trim whitespace around tokens and reject anything non-numeric.
     *
     * @param input CSV of integers. May be null or blank.
     * @return a collection of integers in the same order as the input
     * @throws NumberFormatException if any token is not a valid integer
     */
    Collection<Integer> collect(String input);

    /**
     * Compress the given sequence into a comma-delimited summary string by grouping 
     * strictly monotonic runs (every consecutive step is +1 or -1).
     *
     * Behavior:
     * - Null collection is considered programmer error (IllegalArgumentException).
     * - Empty collection summarizes to an empty string.
     * - Ascending and descending runs are both supported.
     * - Duplicates and jumps break the current run cleanly.
     *
     * @param input the sequence to summarize (must not be null)
     * @return a compact summary like "1-4, 6-2" or "" if input is empty
     */
    String summarizeCollection(Collection<Integer> input);
}
