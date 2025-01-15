package ch.bfh.tracesentry.daemon.facade;

import ch.bfh.tracesentry.daemon.domain.service.InspectionService;
import ch.bfh.tracesentry.daemon.exception.UnprocessableException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;


@RestController
public class InspectController {

    private final InspectionService inspectionService;

    @Autowired
    public InspectController(InspectionService inspectionService) {
        this.inspectionService = inspectionService;
    }

    @GetMapping("inspect")
    public String inspect(
            @RequestParam("path") String path
    ) {
        String fileContent;
        try {
            fileContent = Files.readString(Path.of(path), Charset.defaultCharset());
        } catch (Exception ignored) {
            throw new UnprocessableException("Could not read file");
        }
        return inspectionService.inspect(fileContent, path);
    }
}
