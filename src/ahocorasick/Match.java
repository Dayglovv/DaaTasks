package ahocorasick;
public class Match {
    public final int patternIndex;
    public final int start;

    public Match(int patternIndex, int start) {
        this.patternIndex = patternIndex;
        this.start = start;
    }

    @Override
    public String toString() {
        return "pattern " + patternIndex + " at " + start;
    }
}
