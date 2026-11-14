package kmp;
import java.util.ArrayList;
import java.util.List;
public class KMP {
    public static class Result {
        public final List<Integer> indices;
        public final long comparisons;
        public final long preprocessingOps;
        Result(List<Integer> indices, long comparisons, long preprocessingOps) {
            this.indices = indices;
            this.comparisons = comparisons;
            this.preprocessingOps = preprocessingOps;
        }
    }
    static int[] buildLps(String pattern, long[] counter) {
        int m = pattern.length();
        int[] lps = new int[m];
        int len = 0;
        int i = 1;
        while (i < m) {
            counter[0]++;
            if (pattern.charAt(i) == pattern.charAt(len)) {
                len++;
                lps[i] = len;
                i++;
            } else if (len > 0) {
                len = lps[len - 1];
            } else {
                lps[i] = 0;
                i++;
            }
        }
        return lps;
    }

    public static Result search(String text, String pattern) {
        List<Integer> result = new ArrayList<>();
        int n = text.length();
        int m = pattern.length();
        if (m == 0 || m > n) {
            return new Result(result, 0, 0);
        }

        long[] prep = {0};
        int[] lps = buildLps(pattern, prep);

        long comparisons = 0;
        int i = 0;
        int j = 0;
        while (i < n) {
            comparisons++;
            if (text.charAt(i) == pattern.charAt(j)) {
                i++;
                j++;
                if (j == m) {
                    result.add(i - m);
                    j = lps[j - 1];
                }
            } else if (j > 0) {
                j = lps[j - 1];
            } else {
                i++;
            }
        }
        return new Result(result, comparisons, prep[0]);
    }

    public static long[] searchAll(String text, List<String> patterns) {
        long matches = 0;
        long operations = 0;
        for (String p : patterns) {
            Result r = search(text, p);
            matches += r.indices.size();
            operations += r.comparisons + r.preprocessingOps;
        }
        return new long[]{matches, operations};
    }
}
