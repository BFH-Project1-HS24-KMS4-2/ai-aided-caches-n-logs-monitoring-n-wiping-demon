package ch.bfh.tracesentry.daemon.facade;

import ch.bfh.tracesentry.daemon.dto.SearchDTO;
import ch.bfh.tracesentry.daemon.exception.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;


@RestController
public class DomainLogicController {
    private static final Logger LOG = LoggerFactory.getLogger(DomainLogicController.class);

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<String> handleBadRequestException(BadRequestException e) {
        LOG.error(e.getMessage());
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @GetMapping("search")
    public SearchDTO search(@RequestParam("path") String startDirPath) {
        File dirToSearch = new File(startDirPath);
        if (!dirToSearch.isDirectory()) {
            throw new BadRequestException("Path to search is not a directory or does not exist.");
        }

        List<String> files = new ArrayList<>();

        try {
            Files.walkFileTree(dirToSearch.toPath(), new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult preVisitDirectory(Path path, BasicFileAttributes attrs) {
                    return FileVisitResult.CONTINUE;
                }
                @Override
                public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) {
                    if (containsString(path, "cache") || containsString(path, "log")) {
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
        return new SearchDTO(files.size(), files);
    }

    private static boolean containsString(Path path, String value) {
        return path.getFileName().toString().toLowerCase().contains(value);
    }

    @PostMapping("/monitor")
    @ResponseStatus()
    public ResponseEntity<String> status() {
        return new ResponseEntity<>(null, null, 201);
    }
}
