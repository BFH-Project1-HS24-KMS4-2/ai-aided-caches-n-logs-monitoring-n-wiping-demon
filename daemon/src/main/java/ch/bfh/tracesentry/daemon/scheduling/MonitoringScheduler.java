
package ch.bfh.tracesentry.daemon.scheduling;

import ch.bfh.tracesentry.daemon.domain.model.MerkleTree;
import ch.bfh.tracesentry.daemon.domain.model.MonitoredPath;
import ch.bfh.tracesentry.daemon.domain.model.Node;
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
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

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
        var start = System.currentTimeMillis();
        var snapshot = new Snapshot();
        snapshot.setTimestamp(Timestamp.from(Instant.now()));
        snapshot.setMonitoredPath(monitoredPath);
        final var tree = MerkleTree.create(monitoredPath, snapshot);
        var lastSnapshot = snapshotRepository.findFirstByMonitoredPathIdOrderByTimestampDesc(monitoredPath.getId());
        snapshot = snapshotRepository.save(snapshot);
        compareWithOldSnapshot(tree, lastSnapshot);
        var end = System.currentTimeMillis();
        LOG.info("Created snapshot for path \"{}\" with id \"{}\" at \"{}\" with \"{}\" nodes in \"{}\"ms",
                monitoredPath.getPath(),
                snapshot.getId(),
                snapshot.getTimestamp(),
                tree.getLinearizedNodes().size(),
                end - start);
    }

    private void compareWithOldSnapshot(MerkleTree tree, final Optional<Snapshot> lastSnapshot) {
        if (lastSnapshot.isPresent()) {
            var oldNodes = nodeRepository.findAllBySnapshotId(lastSnapshot.get().getId());
            compareNode(tree.getRoot(), oldNodes);
            markDeletedNodes(oldNodes, tree);
        } else {
            tree.getLinearizedNodes().forEach(n -> n.setHasChanged(true));
        }
        nodeRepository.saveAll(tree.getLinearizedNodes());
    }

    private void compareNode(Node parent, final List<Node> old) {
        final String parentPath = parent.getPath();
        Node oldNode = null;
        final int oldSize = old.size();
        for (int i = 0; i < oldSize; i++) {
            if (parentPath.equals(old.get(i).getPath())) {
                oldNode = old.remove(i);
                break;
            }
        }
        if (oldNode == null) {
            parent.setHasChanged(true);
        } else {
            parent.setHasChanged(!oldNode.getHash().equals(parent.getHash()));
            parent.getChildren().forEach(c -> compareNode(c, old));
        }
    }

    private void markDeletedNodes(List<Node> oldNodes, MerkleTree tree) {
        HashSet<Node> deletedNodes = new HashSet<>(oldNodes);
        tree.getLinearizedNodes().forEach(t -> deletedNodes.removeIf(d -> d.getPath().equals(t.getPath())));
        deletedNodes.forEach(d -> d.setDeletedInNextSnapshot(true));
        nodeRepository.saveAll(deletedNodes);
    }
}
