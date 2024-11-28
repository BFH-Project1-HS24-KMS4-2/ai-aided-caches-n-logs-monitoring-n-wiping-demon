package ch.bfh.tracesentry.daemon.search;

import ch.bfh.tracesentry.lib.model.SearchMode;

import java.util.regex.Pattern;

public class SearchStrategyFactory {
    public static SearchStrategy create(SearchMode searchMode, Pattern pattern) {
        return switch (searchMode) {
            case FULL -> new FullSearchStrategy();
            case CACHE -> new CacheSearchStrategy();
            case LOG -> new LogSearchStrategy();
            case PATTERN -> new PatternSearchStrategy(pattern);
        };
    }
}
