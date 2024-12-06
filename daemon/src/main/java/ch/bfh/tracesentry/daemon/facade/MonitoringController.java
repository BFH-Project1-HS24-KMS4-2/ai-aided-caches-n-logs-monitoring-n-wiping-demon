package ch.bfh.tracesentry.daemon.facade;

import ch.bfh.tracesentry.daemon.domain.service.MonitoringDomainService;
import ch.bfh.tracesentry.daemon.exception.UnprocessableException;
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
        monitoringDomainService.createMonitoring(dirToSearch.getAbsolutePath(), searchMode, patternToMatch.pattern(), dto.isNoSubdirs());
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

    @GetMapping("{id}/changes")
    public MonitoredChangesDTO getMonitoredChanges(@PathVariable Integer id,
                                                   @RequestParam(name = "start", defaultValue = "1") Integer startSnapshotNbr,
                                                   @RequestParam(name = "end", defaultValue = "2") Integer endSnapshotNbr) {
        final int startIdx = startSnapshotNbr - 1;
        final int endIdx = endSnapshotNbr - 1;

        if (startIdx >= endIdx || startIdx < 0) {
            throw new UnprocessableException("Start index needs to be smaller than the end index and not negative");
        }

        final List<SnapshotDTO> snapshots = monitoringDomainService.getSnapshotsOf(id);
        if (startIdx > snapshots.size() - 2 || endIdx > snapshots.size() - 1) {
            throw new UnprocessableException("Not enough snapshots existing at the moment for this range");
        }

        final List<SnapshotComparisonDTO> snapshotComparison = monitoringDomainService.getSnapshotComparison(id, startIdx, endIdx);

        return new MonitoredChangesDTO(
                monitoringDomainService.getMonitoredPath(id).getPath(),
                snapshots.get(startIdx).getTimestamp(),
                snapshots.get(endIdx).getTimestamp(),
                snapshotComparison
        );
    }

    @GetMapping("/{monitoredPathId}/snapshots")
    public List<SnapshotDTO> getSnapshots(@PathVariable Integer monitoredPathId) {
        return monitoringDomainService.getSnapshotsOf(monitoredPathId);
    }

}
