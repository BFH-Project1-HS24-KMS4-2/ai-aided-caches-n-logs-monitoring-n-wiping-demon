package ch.bfh.tracesentry.daemon.search;

import java.nio.file.Path;
import java.util.regex.Pattern;

public class PatternSearchStrategy implements SearchStrategy {
    private final Pattern pattern;

    public PatternSearchStrategy(Pattern pattern) {
        this.pattern = pattern;
    }

    @Override
    public boolean matches(Path path) {
        return pattern.matcher(path.getFileName().toString()).find();
    }
}
