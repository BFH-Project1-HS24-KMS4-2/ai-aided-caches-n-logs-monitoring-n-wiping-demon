package ch.bfh.tracesentry.daemon.search;

import java.util.List;

import static ch.bfh.tracesentry.daemon.common.Constants.CACHE_SEARCH_STRING;
import static ch.bfh.tracesentry.daemon.common.Constants.LOG_SEARCH_STRING;

public class FullSearchStrategy extends SubStringSearchStrategy {
    public FullSearchStrategy() {
        super(List.of(CACHE_SEARCH_STRING, LOG_SEARCH_STRING));
    }
}
