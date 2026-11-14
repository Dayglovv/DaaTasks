package ahocorasick;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
public class AhoCorasick {
    private final String[] patterns;
    private final int sigma;
    private final int[] charIndex;
    private int[] next;
    private int[] fail;
    private int[] dictLink;
    private int[] parent;
    private char[] parentChar;
    private final List<List<Integer>> ids = new ArrayList<>();
    private boolean[] isEnd;
    private int[][] endIds;
    private int nodeCount;

    private long insertSteps;
    private long failureBuildSteps;

    public AhoCorasick(List<String> patternList, String alphabet) {
        this.patterns = patternList.toArray(new String[0]);
        this.sigma = alphabet.length();

        int maxChar = 0;
        for (int i = 0; i < sigma; i++) {
            maxChar = Math.max(maxChar, alphabet.charAt(i));
        }
        charIndex = new int[maxChar + 1];
        Arrays.fill(charIndex, -1);
        for (int i = 0; i < sigma; i++) {
            if (charIndex[alphabet.charAt(i)] != -1) {
                throw new IllegalArgumentException("Alphabet has a repeated character: " + alphabet.charAt(i));
            }
            charIndex[alphabet.charAt(i)] = i;
        }

        int totalLength = 0;
        for (String p : patterns) {
            if (p.isEmpty()) {
                throw new IllegalArgumentException("Empty pattern is not allowed");
            }
            for (int i = 0; i < p.length(); i++) {
                if (indexOf(p.charAt(i)) == -1) {
                    throw new IllegalArgumentException("Pattern \"" + p + "\" has a character that is not in the alphabet");
                }
            }
            totalLength += p.length();
        }

        int capacity = 1 + totalLength;
        next = new int[capacity * sigma];
        Arrays.fill(next, -1);
        fail = new int[capacity];
        dictLink = new int[capacity];
        parent = new int[capacity];
        parentChar = new char[capacity];
        nodeCount = 1; // node 0 = root
        ids.add(null);

        buildTrie();
        buildFailureLinks();
        flattenOutputs();
    }
    private int indexOf(char ch) {
        return ch < charIndex.length ? charIndex[ch] : -1;
    }
    private void buildTrie() {
        for (int id = 0; id < patterns.length; id++) {
            int node = 0;
            for (int i = 0; i < patterns[id].length(); i++) {
                insertSteps++;
                char ch = patterns[id].charAt(i);
                int c = indexOf(ch);
                int child = next[node * sigma + c];
                if (child == -1) {
                    child = nodeCount++;
                    next[node * sigma + c] = child;
                    parent[child] = node;
                    parentChar[child] = ch;
                    ids.add(null);
                }
                node = child;
            }
            if (ids.get(node) == null) {
                ids.set(node, new ArrayList<>());
            }
            ids.get(node).add(id);
        }
    }

    private void buildFailureLinks() {
        int[] queue = new int[nodeCount];
        int head = 0;
        int tail = 0;
        for (int c = 0; c < sigma; c++) {
            int v = next[c];
            if (v != -1) {
                fail[v] = 0;
                dictLink[v] = 0;
                queue[tail++] = v;
            }
        }
        while (head < tail) {
            int u = queue[head++];
            for (int c = 0; c < sigma; c++) {
                int v = next[u * sigma + c];
                if (v == -1) {
                    continue;
                }
                int f = fail[u];
                while (f != 0 && next[f * sigma + c] == -1) {
                    f = fail[f];
                    failureBuildSteps++;
                }
                int target = next[f * sigma + c];
                fail[v] = (target != -1) ? target : 0;

                dictLink[v] = (ids.get(fail[v]) != null) ? fail[v] : dictLink[fail[v]];
                queue[tail++] = v;
            }
        }
    }

    private void flattenOutputs() {
        isEnd = new boolean[nodeCount];
        endIds = new int[nodeCount][];
        for (int node = 0; node < nodeCount; node++) {
            List<Integer> list = ids.get(node);
            if (list != null) {
                isEnd[node] = true;
                endIds[node] = new int[list.size()];
                for (int i = 0; i < list.size(); i++) {
                    endIds[node][i] = list.get(i);
                }
            }
        }
    }


    public SearchResult search(String text) {
        List<Match> matches = new ArrayList<>();
        int state = 0;
        long failureSteps = 0;
        int n = text.length();

        for (int i = 0; i < n; i++) {
            int c = indexOf(text.charAt(i));
            if (c == -1) {
                state = 0;
                continue;
            }
            while (state != 0 && next[state * sigma + c] == -1) {
                state = fail[state];
                failureSteps++;
            }
            int to = next[state * sigma + c];
            if (to != -1) {
                state = to;
            }
            int node = isEnd[state] ? state : dictLink[state];
            while (node != 0) {
                for (int id : endIds[node]) {
                    matches.add(new Match(id, i - patterns[id].length() + 1));
                }
                node = dictLink[node];
            }
        }
        return new SearchResult(matches, failureSteps, n);
    }
    public int getNodeCount() {
        return nodeCount;
    }
    public int getPatternCount() {
        return patterns.length;
    }
    public long getBuildOperations() {
        return insertSteps + failureBuildSteps;
    }
    public String labelOf(int node) {
        StringBuilder sb = new StringBuilder();
        while (node != 0) {
            sb.append(parentChar[node]);
            node = parent[node];
        }
        return sb.reverse().toString();
    }
    public int findNode(String s) {
        int node = 0;
        for (int i = 0; i < s.length(); i++) {
            int c = indexOf(s.charAt(i));
            if (c == -1) {
                return -1;
            }
            node = next[node * sigma + c];
            if (node == -1) {
                return -1;
            }
        }
        return node;
    }
    public String failureLabel(String s) {
        int node = findNode(s);
        if (node == -1) {
            throw new IllegalArgumentException("No such node in the trie: " + s);
        }
        return labelOf(fail[node]);
    }
    public String outputLinkLabel(String s) {
        int node = findNode(s);
        if (node == -1) {
            throw new IllegalArgumentException("No such node in the trie: " + s);
        }
        return dictLink[node] == 0 ? null : labelOf(dictLink[node]);
    }
    public String describe() {
        StringBuilder sb = new StringBuilder();
        for (int node = 1; node < nodeCount; node++) {
            sb.append(String.format("%-8s fail -> %-6s", "\"" + labelOf(node) + "\"", "\"" + labelOf(fail[node]) + "\""));
            if (ids.get(node) != null) {
                sb.append("  [end of pattern]");
            }
            if (dictLink[node] != 0) {
                sb.append("  output link -> \"").append(labelOf(dictLink[node])).append("\"");
            }
            sb.append('\n');
        }
        return sb.toString();
    }
}
