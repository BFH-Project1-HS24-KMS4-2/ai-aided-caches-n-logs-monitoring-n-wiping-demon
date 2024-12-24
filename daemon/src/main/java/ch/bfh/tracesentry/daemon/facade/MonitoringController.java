package ch.bfh.tracesentry.daemon.facade;

import ch.bfh.tracesentry.daemon.domain.service.MonitoringDomainService;
import ch.bfh.tracesentry.lib.dto.*;
import ch.bfh.tracesentry.lib.model.SearchMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

import static ch.bfh.tracesentry.daemon.utils.ControllerUtils.parseDirectory;
import static ch.bfh.tracesentry.daemon.utils.ControllerUtils.parsePattern;

@RestController
@RequestMapping("/monitored-path")
public class MonitoringController {

    private final MonitoringDomainService monitoringDomainService;

    @Autowired
    public MonitoringController(MonitoringDomainService monitoringDomainService) {
        this.monitoringDomainService = monitoringDomainService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void createMonitoring(@RequestBody CreateMonitorPathDTO dto) throws IOException {
        File dirToSearch = parseDirectory(dto.getPath());
        SearchMode searchMode = dto.getMode();
        Pattern patternToMatch = parsePattern(dto.getPattern(), searchMode);
        monitoringDomainService.createMonitoring(dirToSearch.getAbsolutePath(), searchMode, patternToMatch, dto.isNoSubdirs());
    }

    @GetMapping
    public List<MonitoredPathDTO> getMonitoredPaths() {
        return monitoringDomainService.getMonitoredPaths();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMonitoring(@PathVariable Integer id) {
        monitoringDomainService.deleteMonitoring(id);
    }
}
