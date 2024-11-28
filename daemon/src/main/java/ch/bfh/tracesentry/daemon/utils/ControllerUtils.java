package ch.bfh.tracesentry.daemon.utils;

import ch.bfh.tracesentry.daemon.exception.BadRequestException;
import ch.bfh.tracesentry.daemon.exception.UnprocessableException;
import ch.bfh.tracesentry.lib.model.SearchMode;

import java.io.File;
import java.nio.file.Path;
import java.util.regex.Pattern;

public final class ControllerUtils {

    private ControllerUtils() {
    }

    public static boolean pathContainsString(Path path, String value) {
        return path.getFileName().toString().toLowerCase().contains(value);
    }

    public static File parseDirectory(String path) {
        File dirToSearch = new File(path);

        if (!dirToSearch.exists()) throw new UnprocessableException("Search Path does not exist.");
        if (!dirToSearch.isDirectory()) throw new UnprocessableException("Search Path is not a directory.");
        return dirToSearch;
    }

    public static SearchMode parseSearchMode(String mode) {
        try {
            if (mode.isEmpty()) return SearchMode.FULL;
            return SearchMode.valueOf(mode.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid search mode.");
        }
    }

    public static Pattern parsePattern(String pattern, SearchMode searchMode) {
        if (searchMode == SearchMode.PATTERN) {
            if (pattern.isEmpty()) {
                throw new BadRequestException("Pattern mode requires a pattern.");
            }
            try {
                return Pattern.compile(pattern);
            } catch (Exception e) {
                throw new BadRequestException("Invalid pattern.");
            }
        } else {
            if (pattern != null && !pattern.isEmpty()) {
                throw new BadRequestException("Pattern is only allowed in pattern mode.");
            }
            return Pattern.compile(".*");
        }
    }
}
