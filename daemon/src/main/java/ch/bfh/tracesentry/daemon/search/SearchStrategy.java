package ch.bfh.tracesentry.daemon.search;

import java.nio.file.Path;

public interface SearchStrategy {
    boolean matches(Path path);
}
