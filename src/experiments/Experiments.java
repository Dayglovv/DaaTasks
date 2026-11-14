package experiments;
import ahocorasick.AhoCorasick;
import ahocorasick.Match;
import ahocorasick.SearchResult;
import kmp.KMP;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
public class Experiments {
    static final long SEED = 42;
    static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz";
    static final int RUNS = 5;
    static final int[] PATTERN_COUNTS = {10, 100, 1000};
    static final int[] TEXT_LENGTHS = {1000, 10_000, 100_000};
    static final int MIN_PATTERN_LENGTH = 5;
    static final int MAX_PATTERN_LENGTH = 12;

    static final String AC_TOTAL = "Aho-Corasick (build + search)";
    static final String AC_BUILD = "Aho-Corasick (build only)";
    static final String AC_SEARCH = "Aho-Corasick (search only)";
    static final String KMP_ALL = "KMP (once per pattern)";
    private interface Task {
        long[] run();
    }

    private static class Row {
        String algorithm;
        int patterns;
        int textLength;
        long[] times = new long[RUNS];
        double avg;
        long[] counters;
    }
    private static long sink = 0;
    public static void run(String outputDir) throws IOException {
        collect();
        List<Row> rows = collect();
        writeResults(rows, outputDir);
        printTable(rows);
        System.out.println("(sink = " + sink + ")");
    }

    private static List<Row> collect() {
        List<Row> rows = new ArrayList<>();
        for (int n : TEXT_LENGTHS) {
            final String text = StringGenerator.random(n, ALPHABET, SEED);
            for (int k : PATTERN_COUNTS) {
                final List<String> patterns = StringGenerator.patterns(text, k, MIN_PATTERN_LENGTH, MAX_PATTERN_LENGTH,
                        ALPHABET, SEED + k);
                final AhoCorasick prebuilt = new AhoCorasick(patterns, ALPHABET);
                checkSameResults(prebuilt, patterns, text);

                rows.add(measure(AC_TOTAL, k, n, () -> {
                    AhoCorasick ac = new AhoCorasick(patterns, ALPHABET);
                    SearchResult r = ac.search(text);
                    sink += r.matches.size();
                    return new long[]{ac.getBuildOperations() + r.charsProcessed + r.failureSteps, r.matches.size(), ac.getNodeCount()};
                }));
                rows.add(measure(AC_BUILD, k, n, () -> {
                    AhoCorasick ac = new AhoCorasick(patterns, ALPHABET);
                    sink += ac.getNodeCount();
                    return new long[]{ac.getBuildOperations(), -1, ac.getNodeCount()};
                }));
                rows.add(measure(AC_SEARCH, k, n, () -> {
                    SearchResult r = prebuilt.search(text);
                    sink += r.matches.size();
                    return new long[]{r.charsProcessed + r.failureSteps, r.matches.size(), -1};
                }));
                rows.add(measure(KMP_ALL, k, n, () -> {
                    long[] r = KMP.searchAll(text, patterns);
                    sink += r[0];
                    return new long[]{r[1], r[0], -1};
                }));
            }
        }
        return rows;
    }
    private static void checkSameResults(AhoCorasick ac, List<String> patterns, String text) {
        List<List<Integer>> fromAc = new ArrayList<>();
        for (int i = 0; i < patterns.size(); i++) {
            fromAc.add(new ArrayList<>());
        }
        for (Match m : ac.search(text).matches) {
            fromAc.get(m.patternIndex).add(m.start);
        }
        for (int i = 0; i < patterns.size(); i++) {
            Collections.sort(fromAc.get(i));
            if (!fromAc.get(i).equals(KMP.search(text, patterns.get(i)).indices)) {
                throw new IllegalStateException("Aho-Corasick and KMP disagree for pattern " + patterns.get(i));
            }
        }
    }

    private static Row measure(String algorithm, int k, int n, Task task) {
        long warmStart = System.nanoTime();
        int it = 0;
        while (it < 5 || (System.nanoTime() - warmStart < 100_000_000L && it < 20_000)) {
            task.run();
            it++;
        }
        System.gc();

        Row row = new Row();
        row.algorithm = algorithm;
        row.patterns = k;
        row.textLength = n;
        long sum = 0;
        for (int i = 0; i < RUNS; i++) {
            long start = System.nanoTime();
            long[] counters = task.run();
            long end = System.nanoTime();
            row.times[i] = end - start;
            row.counters = counters;
            sum += row.times[i];
        }
        row.avg = sum / (double) RUNS;
        return row;
    }

    private static String fmt(long v) {
        return v < 0 ? "-" : String.valueOf(v);
    }

    private static double avgOf(List<Row> rows, String algorithm, int k, int n) {
        for (Row r : rows) {
            if (r.algorithm.equals(algorithm) && r.patterns == k && r.textLength == n) {
                return r.avg;
            }
        }
        throw new IllegalStateException("row not found");
    }

    private static void writeResults(List<Row> rows, String outputDir) throws IOException {
        Path dir = Paths.get(outputDir);
        Files.createDirectories(dir);

        try (PrintWriter out = new PrintWriter(dir.resolve("results.csv").toFile())) {
            out.println("algorithm,patterns,text_length,run1_ns,run2_ns,run3_ns,run4_ns,run5_ns,avg_ns,operations,matches,trie_nodes");
            for (Row r : rows) {
                out.print(r.algorithm + "," + r.patterns + "," + r.textLength);
                for (long t : r.times) {
                    out.print("," + t);
                }
                out.println("," + String.format(Locale.US, "%.1f", r.avg) + "," + fmt(r.counters[0]) + ","
                        + fmt(r.counters[1]) + "," + fmt(r.counters[2]));
            }
        }

        try (PrintWriter out = new PrintWriter(dir.resolve("results.md").toFile())) {
            out.println("### All measurements");
            out.println();
            out.println("| Patterns k | Text length n | Algorithm | Avg time (ns) | Operations | Matches | Trie nodes |");
            out.println("|---:|---:|---|---:|---:|---:|---:|");
            for (Row r : rows) {
                out.println("| " + r.patterns + " | " + r.textLength + " | " + r.algorithm + " | "
                        + String.format(Locale.US, "%,.0f", r.avg) + " | " + r.counters[0] + " | "
                        + fmt(r.counters[1]) + " | " + fmt(r.counters[2]) + " |");
            }
            out.println();
            out.println("### KMP time divided by Aho-Corasick (build + search) time");
            out.println();
            out.println("Values above 1 mean that Aho-Corasick is faster.");
            out.println();
            out.println("| Patterns k \\ Text length n | 1000 | 10000 | 100000 |");
            out.println("|---:|---:|---:|---:|");
            for (int k : PATTERN_COUNTS) {
                out.print("| " + k + " |");
                for (int n : TEXT_LENGTHS) {
                    double ratio = avgOf(rows, KMP_ALL, k, n) / avgOf(rows, AC_TOTAL, k, n);
                    out.print(" " + String.format(Locale.US, "%.2f", ratio) + " |");
                }
                out.println();
            }
        }
    }

    private static void printTable(List<Row> rows) {
        System.out.printf("%7s %9s  %-30s %15s %12s %9s%n", "k", "n", "Algorithm", "Avg time (ns)", "Operations", "Matches");
        for (Row r : rows) {
            System.out.printf("%7d %9d  %-30s %15.0f %12d %9d%n", r.patterns, r.textLength, r.algorithm, r.avg,
                    r.counters[0], r.counters[1]);
        }
    }
}
