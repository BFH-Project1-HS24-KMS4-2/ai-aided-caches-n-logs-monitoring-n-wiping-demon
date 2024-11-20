package ch.bfh.tracesentry.daemon.facade;

import ch.bfh.tracesentry.daemon.domain.model.Node;
import ch.bfh.tracesentry.daemon.domain.model.Snapshot;
import ch.bfh.tracesentry.daemon.domain.service.MonitoringDomainService;
import ch.bfh.tracesentry.lib.dto.MonitoredChangesDTO;
import ch.bfh.tracesentry.lib.dto.MonitoredPathDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public void createMonitoring(@RequestBody String path) {
        monitoringDomainService.createMonitoring(path);
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
    public MonitoredChangesDTO getMonitoring(@PathVariable Integer id) {
        List<Snapshot> snapshots = monitoringDomainService.getAllSnapshotsOfMonitoredPathOrdered(id);
        if (snapshots.size() < 2) {
            throw new RuntimeException("Not found 2 snapshots for path " + id);
        }

        final Snapshot previousSnapshot = snapshots.get(1);
        final Snapshot subsequentSnapshot = snapshots.get(0);

        final List<Node> changes = monitoringDomainService.getChangesOfSnapshotComparedToPredecessor(subsequentSnapshot.getId());
        final List<Node> deletions = monitoringDomainService.getDeletionsOfSnapshotComparedToPredecessor(previousSnapshot.getId());

        return new MonitoredChangesDTO(
                previousSnapshot.getTimestamp(),
                subsequentSnapshot.getTimestamp(),
                changes.stream().map(Node::getPath).toList(),
                deletions.stream().map(Node::getPath).toList()
        );
    }
}
