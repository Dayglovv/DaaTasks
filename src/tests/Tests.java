package tests;
import ahocorasick.AhoCorasick;
import ahocorasick.Match;
import ahocorasick.SearchResult;
import experiments.StringGenerator;
import kmp.KMP;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
public class Tests {
    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static int passed = 0;
    private static int failed = 0;
    public static void main(String[] args) {
        System.out.println("===== Trie and failure links =====");
        testTrieAndLinks();
        System.out.println("===== Search: small cases =====");
        testSmallCases();
        System.out.println("===== Special cases =====");
        testSpecialCases();
        System.out.println("===== Random cross-checks =====");
        testRandom();

        System.out.println();
        System.out.println("Passed: " + passed + ", failed: " + failed);
        if (failed > 0) {
            System.exit(1);
        }
    }
    static void testTrieAndLinks() {
        AhoCorasick ac = new AhoCorasick(Arrays.asList("he", "she", "his", "hers"), LOWER);
        check("Trie of {he, she, his, hers} has 10 nodes (shared prefix h is stored once)", ac.getNodeCount() == 10);
        check("Trie has a node for 'her' and no node for 'hes'", ac.findNode("her") != -1 && ac.findNode("hes") == -1);
        check("fail(h) = root", ac.failureLabel("h").equals(""));
        check("fail(he) = root ('e' is not a node)", ac.failureLabel("he").equals(""));
        check("fail(her) = root ('er', 'r' are not nodes)", ac.failureLabel("her").equals(""));
        check("fail(hers) = s", ac.failureLabel("hers").equals("s"));
        check("fail(hi) = root", ac.failureLabel("hi").equals(""));
        check("fail(his) = s", ac.failureLabel("his").equals("s"));
        check("fail(s) = root", ac.failureLabel("s").equals(""));
        check("fail(sh) = h", ac.failureLabel("sh").equals("h"));
        check("fail(she) = he", ac.failureLabel("she").equals("he"));
        check("output link of 'she' is 'he' (he is a suffix of she and a pattern)", "he".equals(ac.outputLinkLabel("she")));
        check("output link of 'hers' is none", ac.outputLinkLabel("hers") == null);
        check("output link of 'his' is none", ac.outputLinkLabel("his") == null);
        AhoCorasick chain = new AhoCorasick(Arrays.asList("a", "aa", "aaa"), "a");
        check("fail(aaa) = aa and fail(aa) = a and fail(a) = root",
                chain.failureLabel("aaa").equals("aa") && chain.failureLabel("aa").equals("a") && chain.failureLabel("a").equals(""));
        check("output link of 'aaa' is 'aa', of 'aa' is 'a'",
                "aa".equals(chain.outputLinkLabel("aaa")) && "a".equals(chain.outputLinkLabel("aa")));
    }
    static void testSmallCases() {
        checkMatches("single pattern", Arrays.asList("ab"), "ababab", Arrays.asList("ab@0", "ab@2", "ab@4"));
        checkMatches("classic ushers: she, he and hers", Arrays.asList("he", "she", "his", "hers"), "ushers",
                Arrays.asList("he@2", "hers@2", "she@1"));
        checkMatches("pattern inside another pattern is found too (output links)", Arrays.asList("a", "aa", "aaa"), "aaaa",
                Arrays.asList("a@0", "a@1", "a@2", "a@3", "aa@0", "aa@1", "aa@2", "aaa@0", "aaa@1"));
        checkMatches("example with a, ab, bab, bc, bca, c, caa", Arrays.asList("a", "ab", "bab", "bc", "bca", "c", "caa"), "abccab",
                Arrays.asList("a@0", "a@4", "ab@0", "ab@4", "bc@1", "c@2", "c@3"));
        checkMatches("pattern at the beginning", Arrays.asList("abc", "xyz"), "abcdef", Arrays.asList("abc@0"));
        checkMatches("pattern at the end", Arrays.asList("def", "xyz"), "abcdef", Arrays.asList("def@3"));
        checkMatches("pattern in the middle", Arrays.asList("cd"), "abcdef", Arrays.asList("cd@2"));
        checkMatches("overlapping occurrences of the same pattern", Arrays.asList("aba"), "abababa", Arrays.asList("aba@0", "aba@2", "aba@4"));
        checkMatches("one pattern is a prefix of another", Arrays.asList("ab", "abc"), "xabcx", Arrays.asList("ab@1", "abc@1"));
        checkMatches("one pattern is a suffix of another", Arrays.asList("bc", "abc"), "xabcx", Arrays.asList("abc@1", "bc@2"));
        checkMatches("patterns are found at different places", Arrays.asList("ab", "cd", "ef"), "abxcdxef", Arrays.asList("ab@0", "cd@3", "ef@6"));
        checkMatches("failure link is needed (abcd and bce in abce)", Arrays.asList("abcd", "bce"), "abce", Arrays.asList("bce@1"));
    }
    static void testSpecialCases() {
        checkMatches("no pattern occurs in the text", Arrays.asList("xyz", "qq"), "abcdefg", new ArrayList<String>());
        checkMatches("empty text", Arrays.asList("abc"), "", new ArrayList<String>());
        checkMatches("empty list of patterns", new ArrayList<String>(), "abc", new ArrayList<String>());
        checkMatches("pattern equal to the whole text", Arrays.asList("abc"), "abc", Arrays.asList("abc@0"));
        checkMatches("pattern longer than the text", Arrays.asList("abcd"), "abc", new ArrayList<String>());
        checkMatches("one-character patterns", Arrays.asList("a", "b"), "abca", Arrays.asList("a@0", "a@3", "b@1"));
        checkMatches("the same pattern given twice: both indexes are reported", Arrays.asList("ab", "ab"), "ab", Arrays.asList("ab@0", "ab@0"));
        AhoCorasick ac = new AhoCorasick(Arrays.asList("abc"), "abc");
        List<String> got = toStrings(ac, Arrays.asList("abc"), "abxabc");
        check("character outside the alphabet in the text is handled (abxabc -> abc@3)", got.equals(Arrays.asList("abc@3")));
        check("empty pattern is rejected", throwsIllegalArgument(() -> new AhoCorasick(Arrays.asList("ab", ""), LOWER)));
        check("pattern with a character outside the alphabet is rejected",
                throwsIllegalArgument(() -> new AhoCorasick(Arrays.asList("ab1"), LOWER)));
        check("alphabet with a repeated character is rejected", throwsIllegalArgument(() -> new AhoCorasick(Arrays.asList("a"), "aab")));
        String text = StringGenerator.random(2000, "ab", 5);
        SearchResult r = new AhoCorasick(Arrays.asList("aab", "abab", "bbb", "aaaa"), "ab").search(text);
        check("failure steps <= text length", r.failureSteps <= r.charsProcessed);
    }

    static void testRandom() {
        Random rnd = new Random(1);
        boolean smallOk = true;
        for (int test = 0; test < 500; test++) {
            String text = StringGenerator.random(rnd.nextInt(60), "ab", rnd.nextLong());
            int k = 1 + rnd.nextInt(6);
            List<String> patterns = new ArrayList<>();
            for (int i = 0; i < k; i++) {
                patterns.add(StringGenerator.random(1 + rnd.nextInt(5), "ab", rnd.nextLong()));
            }
            AhoCorasick ac = new AhoCorasick(patterns, "ab");
            if (!toStrings(ac, patterns, text).equals(bruteForce(patterns, text))) {
                smallOk = false;
            }
        }
        check("Random: Aho-Corasick == brute force on 500 small cases (alphabet ab, duplicates allowed)", smallOk);
        boolean kmpOk = true;
        for (int test = 0; test < 10; test++) {
            String text = StringGenerator.random(5000, "abcd", 100 + test);
            List<String> patterns = StringGenerator.patterns(text, 50, 2, 6, "abcd", 200 + test);
            AhoCorasick ac = new AhoCorasick(patterns, "abcd");
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
                    kmpOk = false;
                }
            }
        }
        check("Random: for every pattern the positions are the same as from KMP (10 texts of length 5000, 50 patterns)", kmpOk);
        List<String> gen = StringGenerator.patterns(StringGenerator.random(1000, LOWER, 42), 1000, 5, 12, LOWER, 42);
        check("Generator: 1000 different patterns", new HashSet<>(gen).size() == 1000);
    }
    static void checkMatches(String name, List<String> patterns, String text, List<String> expected) {
        AhoCorasick ac = new AhoCorasick(patterns, LOWER);
        List<String> got = toStrings(ac, patterns, text);
        List<String> exp = new ArrayList<>(expected);
        Collections.sort(exp);
        boolean ok = got.equals(exp) && got.equals(bruteForce(patterns, text));
        check("Match: " + name + " -> " + exp, ok);
    }

    static List<String> toStrings(AhoCorasick ac, List<String> patterns, String text) {
        List<String> result = new ArrayList<>();
        for (Match m : ac.search(text).matches) {
            result.add(patterns.get(m.patternIndex) + "@" + m.start);
        }
        Collections.sort(result);
        return result;
    }
    static List<String> bruteForce(List<String> patterns, String text) {
        List<String> result = new ArrayList<>();
        for (String p : patterns) {
            for (int i = 0; i + p.length() <= text.length(); i++) {
                int j = 0;
                while (j < p.length() && text.charAt(i + j) == p.charAt(j)) {
                    j++;
                }
                if (j == p.length()) {
                    result.add(p + "@" + i);
                }
            }
        }
        Collections.sort(result);
        return result;
    }

    static boolean throwsIllegalArgument(Runnable r) {
        try {
            r.run();
        } catch (IllegalArgumentException e) {
            return true;
        }
        return false;
    }

    static void check(String name, boolean condition) {
        if (condition) {
            passed++;
            System.out.println("  PASS  " + name);
        } else {
            failed++;
            System.out.println("  FAIL  " + name);
        }
    }
}
