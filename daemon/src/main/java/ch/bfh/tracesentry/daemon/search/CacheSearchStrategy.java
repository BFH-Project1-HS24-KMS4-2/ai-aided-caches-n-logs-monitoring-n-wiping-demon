package ch.bfh.tracesentry.daemon.search;

import java.util.List;

import static ch.bfh.tracesentry.daemon.common.Constants.CACHE_SEARCH_STRING;

public class CacheSearchStrategy extends SubStringSearchStrategy {
    public CacheSearchStrategy() {
        super(List.of(CACHE_SEARCH_STRING));
    }
}
