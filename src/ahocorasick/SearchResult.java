package ahocorasick;
import java.util.List;
public class SearchResult {
    public final List<Match> matches;
    public final long failureSteps;
    public final long charsProcessed;

    SearchResult(List<Match> matches, long failureSteps, long charsProcessed) {
        this.matches = matches;
        this.failureSteps = failureSteps;
        this.charsProcessed = charsProcessed;
    }
}
