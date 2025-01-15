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
import jakarta.persistence.EntityManager;
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

    @Autowired
    private EntityManager entityManager;

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
    // @Test // uncomment to run
    public void testMonitoringOverLongPeriod() {
        var monitorings = List.of(
                "/home/luca/tracesentry-1.0.2",
                "/opt/idea-IU-242.22855.74",
                "/opt/vivaldi",
                "/tmp",
                "/etc",
                "/usr/local",
                "/usr/share/vim",
                "/dev",
                "/opt/az/lib/python3.12/email",
                "/opt/android-studio");

        var createdAt = LocalDate.of(2024, 11, 1);

        final Long dbSizeBefore = (Long) entityManager.createNativeQuery("SELECT MEMORY_USED() AS memory_used;").getSingleResult();

        monitoredPathRepository.saveAllAndFlush(
                monitorings.stream()
                        .map(path -> new MonitoredPath()
                                .path(path)
                                .mode(SearchMode.CACHE)
                                .noSubdirs(false)
                                .createdAt(createdAt))
                        .toList());

        var start = Instant.now();
        // simulates monitoring over a month
        for (int i = 0; i < 720; i++) {
            monitoringScheduler.createSnapshots();
        }
        var end = Instant.now();

        final Long dbSizeAfter = (Long) entityManager.createNativeQuery("SELECT MEMORY_USED() AS memory_used;").getSingleResult();

        LOG.info("DB Memory Used in KB: before: {}, after: {}", dbSizeBefore, dbSizeAfter);
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
    // @ParameterizedTest // uncomment to run
    // @ValueSource(strings = {
    //         "/home/luca", "/opt/idea-IU-242.22855.74"}
    // )
    public void testSearch(String path) {
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();

        var start = Instant.now();
        var dto = searchController.search(path, "", "", false);
        var end = Instant.now();


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
    }
}
