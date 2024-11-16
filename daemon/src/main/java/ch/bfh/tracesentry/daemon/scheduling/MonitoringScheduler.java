
package ch.bfh.tracesentry.daemon.scheduling;

import ch.bfh.tracesentry.daemon.domain.model.MerkleTree;
import ch.bfh.tracesentry.daemon.domain.model.MonitoredPath;
import ch.bfh.tracesentry.daemon.domain.model.Snapshot;
import ch.bfh.tracesentry.daemon.domain.repo.MonitoredPathRepository;
import ch.bfh.tracesentry.daemon.domain.repo.NodeRepository;
import ch.bfh.tracesentry.daemon.domain.repo.SnapshotRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.Instant;

@Component
public class MonitoringScheduler {
    private static final Logger LOG = LoggerFactory.getLogger(MonitoringScheduler.class);

    private final MonitoredPathRepository monitoredPathRepository;
    private final NodeRepository nodeRepository;
    private final SnapshotRepository snapshotRepository;

    @Autowired
    public MonitoringScheduler(MonitoredPathRepository monitoredPathRepository, NodeRepository nodeRepository, SnapshotRepository snapshotRepository) {
        this.monitoredPathRepository = monitoredPathRepository;
        this.nodeRepository = nodeRepository;
        this.snapshotRepository = snapshotRepository;
    }

    @Scheduled(fixedRate = 50000)
    public void createSnapshots() {
        monitoredPathRepository.findAll().forEach(this::createSnapshot);
    }

    @Transactional
    protected void createSnapshot(MonitoredPath monitoredPath) {
        var snapshot = new Snapshot();
        snapshot.setTimestamp(Timestamp.from(Instant.now()));
        snapshot.setMonitoredPath(monitoredPath);
        final var tree = MerkleTree.create(monitoredPath.getPath(), snapshot);
        snapshot = snapshotRepository.save(snapshot);
        nodeRepository.saveAll(tree.getLinearizedNodes());
        LOG.info("Created snapshot for path \"{}\" with id \"{}\" at \"{}\" with \"{}\" nodes",
                monitoredPath.getPath(),
                snapshot.getId(),
                snapshot.getTimestamp(),
                tree.getLinearizedNodes().size());
    }
}
