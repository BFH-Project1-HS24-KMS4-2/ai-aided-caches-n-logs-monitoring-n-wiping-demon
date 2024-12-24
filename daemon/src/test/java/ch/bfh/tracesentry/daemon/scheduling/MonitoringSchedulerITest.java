package ch.bfh.tracesentry.daemon.scheduling;

import ch.bfh.tracesentry.daemon.domain.model.MonitoredPath;
import ch.bfh.tracesentry.daemon.domain.repo.MonitoredPathRepository;
import ch.bfh.tracesentry.daemon.domain.repo.NodeRepository;
import ch.bfh.tracesentry.daemon.domain.repo.SnapshotRepository;
import ch.bfh.tracesentry.lib.model.SearchMode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

/**
 * Test class for {@link MonitoringScheduler}.
 * This covers the entire execution flow of the scheduler
 * (including the creation of snapshots and the creation of the tree).
 */
@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class MonitoringSchedulerITest {

    private MonitoringScheduler monitoringScheduler;

    @Autowired
    private MonitoredPathRepository monitoredPathRepository;

    @Autowired
    private SnapshotRepository snapshotRepository;

    @Autowired
    private NodeRepository nodeRepository;

    @BeforeEach
    public void setup() {
        monitoringScheduler = new MonitoringScheduler(monitoredPathRepository, nodeRepository, snapshotRepository);
    }

    @Test
    public void testCreateSnapshot(@TempDir Path rootDir) throws IOException {
        // given
        var tempDirStructure = createTempDirStructure(rootDir);

        var createdAt = LocalDate.of(2024, 11, 1);
        monitoredPathRepository.save(
                new MonitoredPath()
                        .path(rootDir.toString())
                        .mode(SearchMode.CACHE)
                        .noSubdirs(false)
                        .createdAt(createdAt));

        // when
        monitoringScheduler.createSnapshots();

        var snapshots = snapshotRepository.findAll();
        var snapshot = snapshots.getFirst();
        var nodes = nodeRepository.findAll();

        var rootDirNode = nodes.stream().filter(node -> node.getParent() == null).findFirst().orElseThrow();
        var cacheFileNode = nodes.stream().filter(node -> node.getPath().equals(tempDirStructure.cacheFile.toString())).findFirst().orElseThrow();
        var logsDirNode = nodes.stream().filter(node -> node.getPath().equals(tempDirStructure.logsDir.toString())).findFirst().orElseThrow();

        // then

        // monitored path creation
        Assertions.assertEquals(1, monitoredPathRepository.findAll().size());

        // snapshot creation
        Assertions.assertEquals(1, snapshots.size());
        Assertions.assertEquals(rootDir.toString(), snapshot.getMonitoredPath().getPath());
        Assertions.assertEquals(createdAt, snapshot.getMonitoredPath().getCreatedAt());

        // tree creation
        Assertions.assertEquals(3, nodes.size());

        Assertions.assertNull(rootDirNode.getParent());
        Assertions.assertEquals(rootDirNode.getSnapshot(), snapshot);
        Assertions.assertTrue(rootDirNode.getChildren().containsAll(List.of(cacheFileNode, logsDirNode)));
        Assertions.assertEquals(rootDirNode.getPath(), rootDir.toString());
        Assertions.assertFalse(rootDirNode.isHasChanged());
        Assertions.assertFalse(rootDirNode.isDeletedInNextSnapshot());

        Assertions.assertEquals(cacheFileNode.getParent(), rootDirNode);
        Assertions.assertEquals(cacheFileNode.getSnapshot(), snapshot);
        Assertions.assertEquals(cacheFileNode.getChildren(), List.of());
        Assertions.assertEquals(cacheFileNode.getPath(), tempDirStructure.cacheFile.toString());
        Assertions.assertFalse(cacheFileNode.isHasChanged());
        Assertions.assertFalse(cacheFileNode.isDeletedInNextSnapshot());

        Assertions.assertEquals(logsDirNode.getParent(), rootDirNode);
        Assertions.assertEquals(logsDirNode.getSnapshot(), snapshot);
        Assertions.assertEquals(logsDirNode.getChildren(), List.of());
        Assertions.assertEquals(logsDirNode.getPath(), tempDirStructure.logsDir.toString());
        Assertions.assertFalse(logsDirNode.isHasChanged());
        Assertions.assertFalse(logsDirNode.isDeletedInNextSnapshot());
    }

    @Test
    public void testCreationDetection(@TempDir Path rootDir) throws IOException {
        // given
        var tempDirStructure = createTempDirStructure(rootDir);

        var createdAt = LocalDate.of(2024, 11, 1);
        var monitoredPath = monitoredPathRepository.save(
                new MonitoredPath()
                        .path(rootDir.toString())
                        .mode(SearchMode.CACHE)
                        .noSubdirs(false)
                        .createdAt(createdAt));

        // when
        monitoringScheduler.createSnapshots();

        // creation
        Files.createFile(rootDir.resolve("newCache.txt"));
        monitoringScheduler.createSnapshots();

        var snapshots = snapshotRepository.findAll();
        var currentSnapshotOptional = snapshotRepository.findFirstByMonitoredPathIdOrderByTimestampDesc(monitoredPath.getId());
        var currentSnapshot = currentSnapshotOptional.orElseThrow();
        var newNodes = nodeRepository.findAllBySnapshotId(currentSnapshot.getId());

        var rootDirNode = newNodes.stream().filter(node -> node.getParent() == null).findFirst().orElseThrow();
        var cacheFileNode = newNodes.stream().filter(node -> node.getPath().equals(tempDirStructure.cacheFile.toString())).findFirst().orElseThrow();
        var logsDirNode = newNodes.stream().filter(node -> node.getPath().equals(tempDirStructure.logsDir.toString())).findFirst().orElseThrow();
        var newCacheFileNode = newNodes.stream().filter(node -> node.getPath().equals(rootDir.resolve("newCache.txt").toString())).findFirst().orElseThrow();

        // then

        // monitored path creation
        Assertions.assertEquals(1, monitoredPathRepository.findAll().size());

        // snapshot creation
        Assertions.assertEquals(2, snapshots.size());
        Assertions.assertEquals(rootDir.toString(), currentSnapshot.getMonitoredPath().getPath());
        Assertions.assertEquals(createdAt, currentSnapshot.getMonitoredPath().getCreatedAt());

        // tree creation
        Assertions.assertEquals(4, newNodes.size());

        Assertions.assertNull(rootDirNode.getParent());
        Assertions.assertEquals(rootDirNode.getSnapshot(), currentSnapshot);
        Assertions.assertTrue(rootDirNode.getChildren().containsAll(List.of(cacheFileNode, logsDirNode, newCacheFileNode)));
        Assertions.assertEquals(rootDirNode.getPath(), rootDir.toString());
        Assertions.assertTrue(rootDirNode.isHasChanged());
        Assertions.assertFalse(rootDirNode.isDeletedInNextSnapshot());

        Assertions.assertEquals(cacheFileNode.getParent(), rootDirNode);
        Assertions.assertEquals(cacheFileNode.getSnapshot(), currentSnapshot);
        Assertions.assertEquals(cacheFileNode.getChildren(), List.of());
        Assertions.assertEquals(cacheFileNode.getPath(), tempDirStructure.cacheFile.toString());
        Assertions.assertFalse(cacheFileNode.isHasChanged());
        Assertions.assertFalse(cacheFileNode.isDeletedInNextSnapshot());

        Assertions.assertEquals(logsDirNode.getParent(), rootDirNode);
        Assertions.assertEquals(logsDirNode.getSnapshot(), currentSnapshot);
        Assertions.assertEquals(logsDirNode.getChildren(), List.of());
        Assertions.assertEquals(logsDirNode.getPath(), tempDirStructure.logsDir.toString());
        Assertions.assertFalse(logsDirNode.isHasChanged());
        Assertions.assertFalse(logsDirNode.isDeletedInNextSnapshot());

        Assertions.assertEquals(newCacheFileNode.getParent(), rootDirNode);
        Assertions.assertEquals(newCacheFileNode.getSnapshot(), currentSnapshot);
        Assertions.assertEquals(newCacheFileNode.getChildren(), List.of());
        Assertions.assertEquals(newCacheFileNode.getPath(), rootDir.resolve("newCache.txt").toString());
        Assertions.assertTrue(newCacheFileNode.isHasChanged());
        Assertions.assertFalse(newCacheFileNode.isDeletedInNextSnapshot());
    }

    @Test
    public void testChangeDetection(@TempDir Path rootDir) throws IOException {
        // given
        var tempDirStructure = createTempDirStructure(rootDir);

        var createdAt = LocalDate.of(2024, 11, 1);
        var monitoredPath = monitoredPathRepository.save(
                new MonitoredPath()
                        .path(rootDir.toString())
                        .mode(SearchMode.CACHE)
                        .noSubdirs(false)
                        .createdAt(createdAt));

        // when
        monitoringScheduler.createSnapshots();

        // change
        Files.writeString(tempDirStructure.cacheFile, "change");
        monitoringScheduler.createSnapshots();

        var snapshots = snapshotRepository.findAll();
        var currentSnapshotOptional = snapshotRepository.findFirstByMonitoredPathIdOrderByTimestampDesc(monitoredPath.getId());
        var currentSnapshot = currentSnapshotOptional.orElseThrow();
        var newNodes = nodeRepository.findAllBySnapshotId(currentSnapshot.getId());

        var rootDirNode = newNodes.stream().filter(node -> node.getParent() == null).findFirst().orElseThrow();
        var cacheFileNode = newNodes.stream().filter(node -> node.getPath().equals(tempDirStructure.cacheFile.toString())).findFirst().orElseThrow();
        var logsDirNode = newNodes.stream().filter(node -> node.getPath().equals(tempDirStructure.logsDir.toString())).findFirst().orElseThrow();

        // then

        // monitored path creation
        Assertions.assertEquals(1, monitoredPathRepository.findAll().size());

        // snapshot creation
        Assertions.assertEquals(2, snapshots.size());
        Assertions.assertEquals(rootDir.toString(), currentSnapshot.getMonitoredPath().getPath());
        Assertions.assertEquals(createdAt, currentSnapshot.getMonitoredPath().getCreatedAt());

        // tree creation
        Assertions.assertEquals(3, newNodes.size());

        Assertions.assertNull(rootDirNode.getParent());
        Assertions.assertEquals(rootDirNode.getSnapshot(), currentSnapshot);
        Assertions.assertTrue(rootDirNode.getChildren().containsAll(List.of(cacheFileNode, logsDirNode)));
        Assertions.assertEquals(rootDirNode.getPath(), rootDir.toString());
        Assertions.assertTrue(rootDirNode.isHasChanged()); // root dir has changed
        Assertions.assertFalse(rootDirNode.isDeletedInNextSnapshot());

        Assertions.assertEquals(cacheFileNode.getParent(), rootDirNode);
        Assertions.assertEquals(cacheFileNode.getSnapshot(), currentSnapshot);
        Assertions.assertEquals(cacheFileNode.getChildren(), List.of());
        Assertions.assertEquals(cacheFileNode.getPath(), tempDirStructure.cacheFile.toString());
        Assertions.assertTrue(cacheFileNode.isHasChanged()); // cache file has changed
        Assertions.assertFalse(cacheFileNode.isDeletedInNextSnapshot());

        Assertions.assertEquals(logsDirNode.getParent(), rootDirNode);
        Assertions.assertEquals(logsDirNode.getSnapshot(), currentSnapshot);
        Assertions.assertEquals(logsDirNode.getChildren(), List.of());
        Assertions.assertEquals(logsDirNode.getPath(), tempDirStructure.logsDir.toString());
        Assertions.assertFalse(logsDirNode.isHasChanged()); // logs dir has not changed
        Assertions.assertFalse(logsDirNode.isDeletedInNextSnapshot());
    }

    @Test
    public void testDeletionDetection(@TempDir Path rootDir) throws IOException {
        // given
        var tempDirStructure = createTempDirStructure(rootDir);

        var createdAt = LocalDate.of(2024, 11, 1);
        var monitoredPath = monitoredPathRepository.save(
                new MonitoredPath()
                        .path(rootDir.toString())
                        .mode(SearchMode.CACHE)
                        .noSubdirs(false)
                        .createdAt(createdAt));

        // when
        monitoringScheduler.createSnapshots();

        // deletion
        Files.delete(tempDirStructure.cacheFile);
        monitoringScheduler.createSnapshots();

        var snapshots = snapshotRepository.findAllByMonitoredPathIdOrderByTimestampDesc(monitoredPath.getId());
        var oldSnapshotOptional = snapshots.stream().skip(1).findFirst();
        var currentSnapshotOptional = snapshots.stream().findFirst();

        var oldSnapshot = oldSnapshotOptional.orElseThrow();
        var currentSnapshot = currentSnapshotOptional.orElseThrow();

        var oldNodes = nodeRepository.findAllBySnapshotId(oldSnapshot.getId());
        var newNodes = nodeRepository.findAllBySnapshotId(currentSnapshot.getId());

        var oldRootDirNode = nodeRepository.findAll().stream().filter(node -> node.getParent() == null).findFirst().orElseThrow();

        var rootDirNode = newNodes.stream().filter(node -> node.getParent() == null).findFirst().orElseThrow();
        var logsDirNode = newNodes.stream().filter(node -> node.getPath().equals(tempDirStructure.logsDir.toString())).findFirst().orElseThrow();
        var oldCacheFileNode = nodeRepository.findAll().stream().filter(node -> node.getPath().equals(tempDirStructure.cacheFile.toString())).findFirst().orElseThrow();

        // then

        // monitored path creation
        Assertions.assertEquals(1, monitoredPathRepository.findAll().size());

        // snapshot creation
        Assertions.assertEquals(2, snapshots.size());
        Assertions.assertEquals(rootDir.toString(), currentSnapshot.getMonitoredPath().getPath());
        Assertions.assertEquals(createdAt, currentSnapshot.getMonitoredPath().getCreatedAt());

        // tree creation
        Assertions.assertEquals(3, oldNodes.size());
        Assertions.assertEquals(2, newNodes.size());

        Assertions.assertNull(rootDirNode.getParent());
        Assertions.assertEquals(rootDirNode.getSnapshot(), currentSnapshot);
        Assertions.assertEquals(rootDirNode.getChildren(), List.of(logsDirNode));
        Assertions.assertEquals(rootDirNode.getPath(), rootDir.toString());
        Assertions.assertTrue(rootDirNode.isHasChanged());
        Assertions.assertFalse(rootDirNode.isDeletedInNextSnapshot());

        Assertions.assertEquals(oldCacheFileNode.getParent(), oldRootDirNode);
        Assertions.assertEquals(oldCacheFileNode.getSnapshot(), oldSnapshot);
        Assertions.assertEquals(oldCacheFileNode.getChildren(), List.of());
        Assertions.assertEquals(oldCacheFileNode.getPath(), tempDirStructure.cacheFile.toString());
        Assertions.assertFalse(oldCacheFileNode.isHasChanged());
        // cache file has been deleted (marked as deleted in previous snapshot)
        Assertions.assertTrue(oldCacheFileNode.isDeletedInNextSnapshot());

        Assertions.assertEquals(logsDirNode.getParent(), rootDirNode);
        Assertions.assertEquals(logsDirNode.getSnapshot(), currentSnapshot);
        Assertions.assertEquals(logsDirNode.getChildren(), List.of());
        Assertions.assertEquals(logsDirNode.getPath(), tempDirStructure.logsDir.toString());
        Assertions.assertFalse(logsDirNode.isHasChanged());
        Assertions.assertFalse(logsDirNode.isDeletedInNextSnapshot());
    }

    private record TempDirStructure(Path cacheFile, Path logsDir, Path someLogs) { }

    private static TempDirStructure createTempDirStructure(Path rootDir) throws IOException {
        Path cacheFile = rootDir.resolve("cache.txt");
        Files.createFile(cacheFile);
        Files.writeString(cacheFile, "12345");
        Path logsDir = rootDir.resolve("logs");
        Files.createDirectory(logsDir);
        Path someLogs = logsDir.resolve("someLogs.txt");
        Files.createFile(someLogs);
        Files.writeString(someLogs, "some logs");
        return new TempDirStructure(cacheFile, logsDir, someLogs);
    }
}
