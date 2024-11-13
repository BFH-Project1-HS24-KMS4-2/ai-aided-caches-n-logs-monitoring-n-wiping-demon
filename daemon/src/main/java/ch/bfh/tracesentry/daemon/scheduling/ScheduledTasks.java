
package ch.bfh.tracesentry.daemon.scheduling;

import ch.bfh.tracesentry.daemon.domain.model.MonitoredPath;
import ch.bfh.tracesentry.daemon.domain.model.Node;
import ch.bfh.tracesentry.daemon.domain.model.Snapshot;
import ch.bfh.tracesentry.daemon.domain.repo.MonitoredPathRepository;
import ch.bfh.tracesentry.daemon.domain.repo.NodeRepository;
import ch.bfh.tracesentry.daemon.domain.repo.SnapshotRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.sql.Timestamp;
import java.time.Instant;

@Component
public class ScheduledTasks {
    private static final Logger LOG = LoggerFactory.getLogger(ScheduledTasks.class);

    private final MonitoredPathRepository monitoredPathRepository;
    private final NodeRepository nodeRepository;
    private final SnapshotRepository snapshotRepository;

    @Autowired
    public ScheduledTasks(MonitoredPathRepository monitoredPathRepository, NodeRepository nodeRepository, SnapshotRepository snapshotRepository) {
        this.monitoredPathRepository = monitoredPathRepository;
        this.nodeRepository = nodeRepository;
        this.snapshotRepository = snapshotRepository;
    }

    @Scheduled(fixedRate = 50000)
    public void reportCurrentTime() {
        MonitoredPath newMonitoredPath = new MonitoredPath("C:\\Users\\test"); // would be already in db
        monitoredPathRepository.save(newMonitoredPath);

        var monitoredFiles = monitoredPathRepository.findAll();

        monitoredFiles.forEach(monitoredPath -> {
            Snapshot snapshot = new Snapshot();
            snapshot.setTimestamp(Timestamp.from(Instant.now()));
            snapshot.setMonitoredPath(monitoredPath);
            snapshotRepository.save(snapshot);
            Node node = new Node();
            File dirToSearch = new File(monitoredPath.getPath());
            node.setHash("");
            node.setPath(monitoredPath.getPath());
            node.setSnapshot(snapshot);
            nodeRepository.save(node);
        });
    }
}
