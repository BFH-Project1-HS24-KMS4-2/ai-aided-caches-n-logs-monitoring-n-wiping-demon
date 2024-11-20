package ch.bfh.tracesentry.daemon.facade;

import ch.bfh.tracesentry.daemon.exception.BadRequest;
import ch.bfh.tracesentry.daemon.exception.UnprocessableException;
import ch.bfh.tracesentry.lib.dto.SearchResponseDTO;
import ch.bfh.tracesentry.lib.model.SearchMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;


@RestController
public class SearchController {

    private static final Logger LOG = LoggerFactory.getLogger(SearchController.class);
    private static final String CACHE_SEARCH_STRING = "cache";
    private static final String LOG_SEARCH_STRING = "log";

    @GetMapping("search")
    public SearchResponseDTO search(
            @RequestParam("path") String startDirPath,
            @RequestParam(value = "mode", defaultValue = "") String mode,
            @RequestParam(value = "pattern", defaultValue = "") String pattern,
            @RequestParam(value = "no-subdirs", defaultValue = "false") boolean noSubdirs
    ) {
        File dirToSearch = new File(startDirPath);

        if (!dirToSearch.exists()) unprocessableException("Search Path does not exist.");
        if (!dirToSearch.isDirectory()) unprocessableException("Search Path is not a directory.");

        SearchMode searchMode = getSearchMode(mode);
        Pattern patternToMatch = getPattern(pattern, searchMode);

        List<String> files = new ArrayList<>();

        try {
            Files.walkFileTree(dirToSearch.toPath(), new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult preVisitDirectory(Path path, BasicFileAttributes attrs) {
                    if (noSubdirs && !Objects.equals(path, dirToSearch.toPath())) {
                        return FileVisitResult.SKIP_SUBTREE;
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) {
                    if ((searchMode == SearchMode.FULL && (containsString(path, CACHE_SEARCH_STRING) || containsString(path, LOG_SEARCH_STRING))) ||
                            (searchMode == SearchMode.CACHE && (containsString(path, CACHE_SEARCH_STRING))) ||
                            (searchMode == SearchMode.LOG && (containsString(path, LOG_SEARCH_STRING))) ||
                            (searchMode == SearchMode.PATTERN && patternToMatch.matcher(path.getFileName().toString()).find())) {

                        files.add(path.toString());
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException e) {
                    return FileVisitResult.SKIP_SUBTREE;
                }
            });
        } catch (IOException e) {
            throw new InternalError("Error while searching for files.");
        }
        return new SearchResponseDTO(files.size(), files);
    }

    private static void unprocessableException(String message) {
        LOG.error(message);
        throw new UnprocessableException(message);
    }

    private static boolean containsString(Path path, String value) {
        return path.getFileName().toString().toLowerCase().contains(value);
    }

    private static SearchMode getSearchMode(String mode) {
        try {
            if (mode.isEmpty()) return SearchMode.FULL;
            return SearchMode.valueOf(mode.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequest("Invalid search mode.");
        }
    }

    private static Pattern getPattern(String pattern, SearchMode searchMode) {
        if (searchMode == SearchMode.PATTERN) {
            if (pattern.isEmpty()) {
                throw new BadRequest("Pattern mode requires a pattern.");
            }
            try {
                return Pattern.compile(pattern);
            } catch (Exception e) {
                throw new BadRequest("Invalid pattern.");
            }
        } else {
            if (!pattern.isEmpty()) {
                throw new BadRequest("Pattern is only allowed in pattern mode.");
            }
            return Pattern.compile(".*");
        }
    }
}
