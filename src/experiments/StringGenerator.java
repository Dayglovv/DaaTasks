package experiments;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
public class StringGenerator {
    public static String random(int length, String alphabet, long seed) {
        Random rnd = new Random(seed);
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(alphabet.charAt(rnd.nextInt(alphabet.length())));
        }
        return sb.toString();
    }

    public static List<String> patterns(String text, int k, int minLen, int maxLen, String alphabet, long seed) {
        Random rnd = new Random(seed);
        Set<String> set = new LinkedHashSet<>();
        int fromText = k / 2;
        while (set.size() < fromText) {
            int len = minLen + rnd.nextInt(maxLen - minLen + 1);
            int start = rnd.nextInt(text.length() - len + 1);
            set.add(text.substring(start, start + len));
        }
        while (set.size() < k) {
            int len = minLen + rnd.nextInt(maxLen - minLen + 1);
            StringBuilder sb = new StringBuilder(len);
            for (int i = 0; i < len; i++) {
                sb.append(alphabet.charAt(rnd.nextInt(alphabet.length())));
            }
            set.add(sb.toString());
        }
        return new ArrayList<>(set);
    }
}
