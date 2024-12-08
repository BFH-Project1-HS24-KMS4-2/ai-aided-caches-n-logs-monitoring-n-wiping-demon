package ch.bfh.tracesentry.daemon.search;

import java.nio.file.Path;
import java.util.List;

import static ch.bfh.tracesentry.daemon.utils.ControllerUtils.pathContainsString;

public class SubStringSearchStrategy implements SearchStrategy {
    private final List<String> substrings;

    public SubStringSearchStrategy(List<String> substrings) {
        if (substrings == null || substrings.isEmpty()) {
            throw new IllegalArgumentException("Substrings cannot be null or empty");
        }
        this.substrings = substrings;
    }

    @Override
    public boolean matches(Path path) {
        for (String substring : substrings) {
            if (pathContainsString(path, substring)) {
                return true;
            }
        }
        return false;
    }
}
