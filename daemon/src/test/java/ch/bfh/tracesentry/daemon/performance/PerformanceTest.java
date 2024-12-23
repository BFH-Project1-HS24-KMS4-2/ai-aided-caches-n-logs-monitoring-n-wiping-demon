package ch.bfh.tracesentry.daemon.performance;

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
import java.time.LocalDate;
import java.util.List;

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
    private SnapshotComparisonRepository snapshotComparisonRepository;

    @Autowired
    private NodeRepository nodeRepository;

    @Autowired
    private SearchController searchController;

    @BeforeEach
    public void setup() {
        monitoringScheduler = new MonitoringScheduler(monitoredPathRepository, nodeRepository, snapshotRepository);
    }

    // Remove comment out to run the test locally. Always comment annotation before committing and pushing (to not run the test on the pipeline).
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

        var start = System.currentTimeMillis();
        // simulates monitoring over a month
        for (int i = 0; i < 720; i++) {
            monitoringScheduler.createSnapshots();
        }
        var end = System.currentTimeMillis();

        var endMemory = runtime.totalMemory() - runtime.freeMemory();

        LOG.info("Memory used: {}GiB", (endMemory - startMemory) / 1024 / 1024 / 1024);
        LOG.info("Snapshots created: {}", snapshotRepository.count());
        LOG.info("Nodes created: {}", nodeRepository.count());
        LOG.info("Monitoring took: {}min, average: {}s", (end - start) / 1000 / 60, (end - start) / 1000 / 720);
        LOG.info(snapshotComparisonRepository.getSnapshotComparisons(1, 0, 720).toString());
    }

    /**
     * Outputs simple metrics about the search operation.
     * Intended usage: getting a rough idea of the performance of the search operation.
     */
    //@ParameterizedTest
    //@ValueSource(strings = {
    //        "C:\\Windows\\Temp\\",
    //        "C:\\Users\\Janic Scherer\\IdeaProjects\\"}
    //)
    public void testSearch(String path) {
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        Runtime runtime = Runtime.getRuntime();

        var start = System.currentTimeMillis();
        var startMemory = runtime.totalMemory() - runtime.freeMemory();
        var dto = searchController.search(path, "", "", false);
        var end = System.currentTimeMillis();
        var endMemory = runtime.totalMemory() - runtime.freeMemory();

        LOG.info("Operating System: {} {}", osBean.getName(), osBean.getVersion());
        LOG.info("Architecture: {}", osBean.getArch());
        LOG.info("Available Processors (CPU cores): {}", osBean.getAvailableProcessors());
        LOG.info("Search took: {}ms", end - start);
        LOG.info("Files found: {}", dto.getNumberOfFiles());
        LOG.info("Memory used: {}MiB", (endMemory - startMemory) / 1024 / 1024);
    }
}
