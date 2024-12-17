package ch.bfh.tracesentry.daemon.facade;

import ch.bfh.tracesentry.daemon.exception.UnprocessableException;
import ch.bfh.tracesentry.lib.dto.WipeFileDTO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

@RestController
public class WipeController {

    @PostMapping("wipe")
    public void wipe(@RequestBody WipeFileDTO dto) {
        try {
            Path path = Path.of(dto.getPath());
            if (dto.isRemove()) {
                Files.delete(path);
            } else {
                clearFile(path);
            }
        } catch (Exception e) {
            throw new UnprocessableException("File to wipe could not be processed");
        }
    }

    private void clearFile(Path path) throws IOException {
        try (var writer = Files.newBufferedWriter(path, StandardOpenOption.TRUNCATE_EXISTING)) {
        }
    }
}
