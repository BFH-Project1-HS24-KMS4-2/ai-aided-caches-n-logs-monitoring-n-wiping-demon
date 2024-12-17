package ch.bfh.tracesentry.daemon.facade;

import ch.bfh.tracesentry.daemon.domain.service.SnapshotsDomainService;
import ch.bfh.tracesentry.daemon.exception.UnprocessableException;
import ch.bfh.tracesentry.lib.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/monitored-path/{monitoredPathId}/snapshots")
public class SnapshotsController {

    private final SnapshotsDomainService snapshotsDomainService;

    @Autowired
    public SnapshotsController(SnapshotsDomainService snapshotsDomainService) {
        this.snapshotsDomainService = snapshotsDomainService;
    }

    @GetMapping
    public List<SnapshotDTO> getSnapshots(@PathVariable Integer monitoredPathId) {
        return snapshotsDomainService.getSnapshotsOf(monitoredPathId);
    }

    @GetMapping("/changes")
    public MonitoredChangesDTO getMonitoredChanges(@PathVariable Integer monitoredPathId,
                                                   @RequestParam(name = "start", defaultValue = "1") Integer startSnapshotNbr,
                                                   @RequestParam(name = "end", defaultValue = "2") Integer endSnapshotNbr) {
        final int startIdx = startSnapshotNbr - 1;
        final int endIdx = endSnapshotNbr - 1;

        if (startIdx >= endIdx || startIdx < 0) {
            throw new UnprocessableException("Start index needs to be smaller than the end index and not negative.");
        }

        final List<SnapshotDTO> snapshots = snapshotsDomainService.getSnapshotsOf(monitoredPathId);
        if (startIdx > snapshots.size() - 2 || endIdx > snapshots.size() - 1) {
            throw new UnprocessableException("Not enough snapshots existing at the moment for this range.");
        }

        final List<SnapshotComparisonDTO> snapshotComparison = snapshotsDomainService.getSnapshotComparison(monitoredPathId, startIdx, endIdx);

        return new MonitoredChangesDTO(
                snapshotsDomainService.getMonitoredPath(monitoredPathId).getPath(),
                snapshots.get(startIdx).getTimestamp(),
                snapshots.get(endIdx).getTimestamp(),
                snapshotComparison
        );
    }
}
