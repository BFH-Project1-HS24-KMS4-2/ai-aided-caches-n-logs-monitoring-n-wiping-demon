package ch.bfh.tracesentry.daemon.performance;

import ch.bfh.tracesentry.daemon.domain.model.MonitoredPath;
import ch.bfh.tracesentry.daemon.domain.repo.MonitoredPathRepository;
import ch.bfh.tracesentry.daemon.domain.repo.NodeRepository;
import ch.bfh.tracesentry.daemon.domain.repo.SnapshotRepository;
import ch.bfh.tracesentry.daemon.facade.SearchController;
import ch.bfh.tracesentry.daemon.scheduling.MonitoringScheduler;
import ch.bfh.tracesentry.lib.model.SearchMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

/**
 * Non-functional (and non-assertive) performance test for core daemon functionality.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class PerformanceTest {

    private static final Logger LOG = LoggerFactory.getLogger(PerformanceTest.class);

    private MonitoringScheduler monitoringScheduler;

    @Autowired
    private MonitoredPathRepository monitoredPathRepository;

    @Autowired
    private SnapshotRepository snapshotRepository;

    @Autowired
    private NodeRepository nodeRepository;

    @Autowired
    private SearchController searchController;

    @BeforeEach
    public void setup() {
        monitoringScheduler = new MonitoringScheduler(monitoredPathRepository, nodeRepository, snapshotRepository);
    }

    @Test
    public void testChangeDetection() {
        var path = "C:\\Users\\Janic Scherer\\IdeaProjects\\";

        var createdAt = LocalDate.of(2024, 11, 1);
        monitoredPathRepository.save(
                new MonitoredPath()
                        .path(path)
                        .mode(SearchMode.CACHE)
                        .noSubdirs(false)
                        .createdAt(createdAt));

        monitoringScheduler.createSnapshots();
        LOG.info("Snapshots created");
    }

    /**
     * Outputs simple metrics about the search operation.
     * Intended usage is just to get a rough idea of the performance of the search operation.
     */
    @Test
    public void testSearch() {
        var path = "C:\\Users\\Janic Scherer\\IdeaProjects\\";
        var start = System.currentTimeMillis();
        Runtime runtime = Runtime.getRuntime();
        var startMemory = runtime.totalMemory() - runtime.freeMemory();
        var dto = searchController.search(path, "", "", false);
        var end = System.currentTimeMillis();
        var endMemory = runtime.totalMemory() - runtime.freeMemory();
        LOG.info("Search took: {}ms", end - start);
        LOG.info("Files found: {}", dto.getNumberOfFiles());
        LOG.info("Memory used: {}MiB", (endMemory - startMemory) / 1024 / 1024);
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
