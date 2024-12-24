package ch.bfh.tracesentry.daemon.facade;

import ch.bfh.tracesentry.daemon.search.SearchStrategyFactory;
import ch.bfh.tracesentry.daemon.exception.BadRequestException;
import ch.bfh.tracesentry.daemon.exception.UnprocessableException;
import ch.bfh.tracesentry.lib.dto.SearchResponseDTO;
import ch.bfh.tracesentry.lib.model.SearchMode;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import static ch.bfh.tracesentry.daemon.utils.ControllerUtils.*;


@RestController
public class SearchController {

    @GetMapping("search")
    public SearchResponseDTO search(
            @RequestParam("path") String startDirPath,
            @RequestParam(value = "mode", defaultValue = "") String mode,
            @RequestParam(value = "pattern", defaultValue = "") String pattern,
            @RequestParam(value = "no-subdirs", defaultValue = "false") boolean noSubdirs
    ) {
        File dirToSearch = parseDirectory(startDirPath);
        SearchMode searchMode = parseSearchMode(mode);
        Pattern patternToMatch = parsePattern(pattern, searchMode);

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
                    if (SearchStrategyFactory.create(searchMode, patternToMatch).matches(path)) {
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
}
