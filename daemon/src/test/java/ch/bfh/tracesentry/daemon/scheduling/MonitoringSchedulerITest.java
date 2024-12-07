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
        Path cacheFile = rootDir.resolve("cache.txt");
        Files.createFile(cacheFile);
        Files.writeString(cacheFile, "12345");
        Path logsDir = rootDir.resolve("logs");
        Files.createDirectory(logsDir);
        Path someLogs = logsDir.resolve("someLogs.txt");
        Files.createFile(someLogs);
        Files.writeString(someLogs, "some logs");

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
        var cacheFileNode = nodes.stream().filter(node -> node.getPath().equals(cacheFile.toString())).findFirst().orElseThrow();
        var logsDirNode = nodes.stream().filter(node -> node.getPath().equals(logsDir.toString())).findFirst().orElseThrow();

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
        Assertions.assertEquals(rootDirNode.getChildren(), List.of(cacheFileNode, logsDirNode));
        Assertions.assertEquals(rootDirNode.getPath(), rootDir.toString());
        Assertions.assertTrue(rootDirNode.isHasChanged());
        Assertions.assertFalse(rootDirNode.isDeletedInNextSnapshot());

        Assertions.assertEquals(cacheFileNode.getParent(), rootDirNode);
        Assertions.assertEquals(cacheFileNode.getSnapshot(), snapshot);
        Assertions.assertEquals(cacheFileNode.getChildren(), List.of());
        Assertions.assertEquals(cacheFileNode.getPath(), cacheFile.toString());
        Assertions.assertTrue(cacheFileNode.isHasChanged());
        Assertions.assertFalse(cacheFileNode.isDeletedInNextSnapshot());

        Assertions.assertEquals(logsDirNode.getParent(), rootDirNode);
        Assertions.assertEquals(logsDirNode.getSnapshot(), snapshot);
        Assertions.assertEquals(logsDirNode.getChildren(), List.of());
        Assertions.assertEquals(logsDirNode.getPath(), logsDir.toString());
        Assertions.assertTrue(logsDirNode.isHasChanged());
        Assertions.assertFalse(logsDirNode.isDeletedInNextSnapshot());
    }
}
