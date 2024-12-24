package ch.bfh.tracesentry.daemon.performance;

import ch.bfh.tracesentry.daemon.domain.model.MerkleTree;
import ch.bfh.tracesentry.daemon.domain.model.MonitoredPath;
import ch.bfh.tracesentry.daemon.domain.repo.MonitoredPathRepository;
import ch.bfh.tracesentry.daemon.domain.repo.NodeRepository;
import ch.bfh.tracesentry.daemon.domain.repo.SnapshotComparisonRepository;
import ch.bfh.tracesentry.daemon.domain.repo.SnapshotRepository;
import ch.bfh.tracesentry.daemon.facade.SearchController;
import ch.bfh.tracesentry.daemon.scheduling.MonitoringScheduler;
import ch.bfh.tracesentry.lib.model.SearchMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * Non-functional (and non-assertive) performance test for core daemon functionality.
 * Intended usage: getting a rough idea of the performance of key operations.
 * <p>
 * Always comment @Test annotation again before committing and pushing (to not run the test in the CI pipeline).
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
    private SnapshotComparisonRepository snapshotComparisonRepository;

    @Autowired
    private NodeRepository nodeRepository;

    @Autowired
    private SearchController searchController;

    @BeforeEach
    public void setup() {
        monitoringScheduler = new MonitoringScheduler(monitoredPathRepository, nodeRepository, snapshotRepository);
    }

    /**
     * Outputs simple metrics about the monitoring (and comparison) operation
     * (might take multiple minutes depending on the monitored paths).
     * <p>
     * A node (dir or file) can manually be changed/added/removed while the test is running to also test the comparison functionality.
     * There are 720 snapshot creations, simulating a month of monitoring.
     **/
    //@Test
    public void testMonitoringOverLongPeriod() {
        var monitorings = List.of(
                "C:\\Users\\Janic Scherer\\AppData\\Roaming\\discord\\Cache",
                "C:\\\\Users\\\\Janic Scherer\\\\AppData\\\\Roaming\\\\discord\\\\logs",
                "C:\\Users\\Janic Scherer\\AppData\\Roaming\\Docker Desktop\\Cache",
                "C:\\Users\\Janic Scherer\\AppData\\Roaming\\Microsoft",
                "C:\\Users\\Janic Scherer\\AppData\\Roaming\\NVIDIA",
                "C:\\ProgramData\\VMware\\logs",
                "C:\\ProgramData\\Package Cache",
                "C:\\ProgramData\\Apple",
                "C:\\ProgramData\\LogiOptionsPlus",
                "C:\\ProgramData\\LGHUB");

        var createdAt = LocalDate.of(2024, 11, 1);

        monitoredPathRepository.saveAll(
                monitorings.stream()
                        .map(path -> new MonitoredPath()
                                .path(path)
                                .mode(SearchMode.CACHE)
                                .noSubdirs(false)
                                .createdAt(createdAt))
                        .toList());

        Runtime runtime = Runtime.getRuntime();
        var startMemory = runtime.totalMemory() - runtime.freeMemory();

        var start = Instant.now();
        // simulates monitoring over a month
        for (int i = 0; i < 720; i++) {
            monitoringScheduler.createSnapshots();
        }
        var end = Instant.now();

        var endMemory = runtime.totalMemory() - runtime.freeMemory();

        logApproxMemoryUsage(startMemory, endMemory);
        LOG.info("Snapshots created: {}", snapshotRepository.count());
        LOG.info("Nodes created: {}", nodeRepository.count());
        var duration = Duration.between(start, end);
        LOG.info("Monitoring took: {}min, average snapshot creation time: {}ms",
                duration.toMinutes(),
                duration.toMillis() / 720);

        var startComp1 = Instant.now();
        var comparisons = snapshotComparisonRepository.getSnapshotComparisons(1, 0, 720);
        var endComp1 = Instant.now();
        LOG.info("Comparisons: {}", comparisons.toString());
        LOG.info("Comparisons took: {}ms", Duration.between(startComp1, endComp1).toMillis());
    }

    /**
     * Outputs simple metrics about the search operation.
     */
    //@ParameterizedTest
    //@ValueSource(strings = {
    //        "C:\\Windows\\Temp\\",
    //        "C:\\Users\\Janic Scherer\\IdeaProjects\\"}
    //)
    public void testSearch(String path) {
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        Runtime runtime = Runtime.getRuntime();

        var start = Instant.now();
        var startMemory = runtime.totalMemory() - runtime.freeMemory();
        var dto = searchController.search(path, "", "", false);
        var end = Instant.now();
        var endMemory = runtime.totalMemory() - runtime.freeMemory();


        var tree = new MerkleTree(new MonitoredPath()
                .path(path)
                .mode(SearchMode.FULL)
                .pattern(".*"),
                null);
        LOG.info("Tree depth: {}", tree.calculateDepth(tree.getRoot()));
        LOG.info("Tree size: {}", tree.getLinearizedNodes().size());
        LOG.info("Operating System: {} {}", osBean.getName(), osBean.getVersion());
        LOG.info("Architecture: {}", osBean.getArch());
        LOG.info("Available Processors (CPU cores): {}", osBean.getAvailableProcessors());
        LOG.info("Search took: {}ms", Duration.between(start, end).toMillis());
        LOG.info("Files found: {}", dto.getNumberOfFiles());
        logApproxMemoryUsage(startMemory, endMemory);
    }

    private static void logApproxMemoryUsage(long startMemory, long endMemory) {
        LOG.info("Approx. memory used: {}MiB", (endMemory - startMemory) / 1024 / 1024);
    }
}
