package ch.bfh.tracesentry.daemon.facade;

import ch.bfh.tracesentry.daemon.dto.SearchDTO;
import ch.bfh.tracesentry.daemon.exception.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;


@RestController
public class DomainLogicController {
    private static final Logger LOG = LoggerFactory.getLogger(DomainLogicController.class);

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<String> handleBadRequestException(BadRequestException e) {
        LOG.error(e.getMessage());
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @GetMapping("search")
    public SearchDTO search(@RequestParam("path") String startDirPath) throws IOException {
        File dir = new File(startDirPath);
        if (!dir.isDirectory()) {
            throw new BadRequestException("Path to search is not a directory or does not exist");
        }

        try (Stream<Path> paths = Files.find(dir.toPath(), Integer.MAX_VALUE, (path, attributes) ->
                attributes.isRegularFile() && (containsString(path, "log") || containsString(path, "cache"))
        )) {
            List<String> files = paths.map(Path::toString).toList();
            return new SearchDTO(files.size(), files);
        }
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
