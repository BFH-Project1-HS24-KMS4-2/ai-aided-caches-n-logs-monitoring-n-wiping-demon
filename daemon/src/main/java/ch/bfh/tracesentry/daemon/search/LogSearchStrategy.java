package ch.bfh.tracesentry.daemon.search;

import java.util.List;

import static ch.bfh.tracesentry.daemon.common.Constants.LOG_SEARCH_STRING;

public class LogSearchStrategy extends SubStringSearchStrategy {
    public LogSearchStrategy() {
        super(List.of(LOG_SEARCH_STRING));
    }
}
