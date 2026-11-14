import ahocorasick.AhoCorasick;
import ahocorasick.Match;
import ahocorasick.SearchResult;
import experiments.Experiments;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
public class Main {
    public static void main(String[] args) throws IOException {
        String mode = args.length > 0 ? args[0] : "all";
        if (mode.equals("demo") || mode.equals("all")) {
            demo();
        }
        if (mode.equals("experiments") || mode.equals("all")) {
            System.out.println("\n=== Experiments (alphabet a-z, seed 42, 5 runs each) ===");
            Experiments.run("results/tables");
            System.out.println("Tables saved to results/tables/");
        }
    }
    static void demo() {
        List<String> patterns = Arrays.asList("he", "she", "his", "hers");
        String text = "ushers";
        AhoCorasick ac = new AhoCorasick(patterns, "abcdefghijklmnopqrstuvwxyz");
        System.out.println("patterns: " + patterns);
        System.out.println("trie nodes (without the root): " + (ac.getNodeCount() - 1));
        System.out.println("\nfailure links and output links:");
        System.out.print(ac.describe());
        System.out.println("\ntext: " + text);
        SearchResult r = ac.search(text);
        for (Match m : r.matches) {
            System.out.println("  found \"" + patterns.get(m.patternIndex) + "\" at position " + m.start);
        }
        System.out.println("failure links followed in the search: " + r.failureSteps + " (text length " + r.charsProcessed + ")");
    }
}
