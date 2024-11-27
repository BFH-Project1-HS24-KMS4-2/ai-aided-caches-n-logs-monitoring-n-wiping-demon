package ch.bfh.tracesentry.daemon.facade;

import ch.bfh.tracesentry.daemon.domain.model.Node;
import ch.bfh.tracesentry.daemon.domain.model.Snapshot;
import ch.bfh.tracesentry.daemon.domain.service.MonitoringDomainService;
import ch.bfh.tracesentry.daemon.exception.UnprocessableException;
import ch.bfh.tracesentry.lib.dto.MonitoredChangesDTO;
import ch.bfh.tracesentry.lib.dto.MonitoredPathDTO;
import ch.bfh.tracesentry.lib.dto.CreateMonitorPathDTO;
import ch.bfh.tracesentry.lib.dto.MonitorPathDTO;
import ch.bfh.tracesentry.lib.model.SearchMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.io.File;
import java.util.List;
import java.util.regex.Pattern;

import static ch.bfh.tracesentry.daemon.utils.ControllerUtils.*;

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
    public MonitoredChangesDTO getMonitoredChanges(@PathVariable Integer id) {
        List<Snapshot> snapshots = monitoringDomainService.getAllSnapshotsOfMonitoredPathOrdered(id);
        if (snapshots.size() < 2) {
            throw new UnprocessableException("Not found two snapshots to compare");
        }

        final Snapshot previousSnapshot = snapshots.get(1);
        final Snapshot subsequentSnapshot = snapshots.get(0);

        final List<Node> changes = monitoringDomainService.getChangesOfSnapshotComparedToPredecessor(subsequentSnapshot.getId());
        final List<Node> deletions = monitoringDomainService.getDeletionsOfSnapshotComparedToPredecessor(previousSnapshot.getId());

        return new MonitoredChangesDTO(
                previousSnapshot.getMonitoredPath().getPath(),
                dateFromTimestamp(previousSnapshot.getTimestamp()),
                dateFromTimestamp(subsequentSnapshot.getTimestamp()),
                changes.stream().map(Node::getPath).toList(),
                deletions.stream().map(Node::getPath).toList()
        );
    }

    private static LocalDateTime dateFromTimestamp(Timestamp timestamp) {
        return timestamp.toLocalDateTime();
    }
}
