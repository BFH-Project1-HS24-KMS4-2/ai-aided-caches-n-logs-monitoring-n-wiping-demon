package ch.bfh.tracesentry.daemon.facade;

import ch.bfh.tracesentry.daemon.domain.model.MonitoredPath;
import ch.bfh.tracesentry.daemon.domain.model.Node;
import ch.bfh.tracesentry.daemon.domain.model.Snapshot;
import ch.bfh.tracesentry.daemon.domain.repo.MonitoredPathRepository;
import ch.bfh.tracesentry.daemon.domain.repo.NodeRepository;
import ch.bfh.tracesentry.daemon.domain.repo.SnapshotRepository;
import ch.bfh.tracesentry.lib.dto.CreateMonitorPathDTO;
import ch.bfh.tracesentry.lib.model.SearchMode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.nio.file.Paths;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class MonitoringControllerITest {

    @Autowired
    private MonitoredPathRepository monitoredPathRepository;

    @Autowired
    private SnapshotRepository snapshotRepository;

    @Autowired
    private NodeRepository nodeRepository;

    @Test
    public void testGetMonitoredPaths() {
        var path = "C:\\Users\\test\\Desktop";
        var createdAt = LocalDate.of(2024, 11, 1);
        monitoredPathRepository.save(
                new MonitoredPath()
                        .path(path)
                        .mode(SearchMode.PATTERN)
                        .noSubdirs(false)
                        .pattern("*.txt")
                        .createdAt(createdAt));
        WebTestClient
                .bindToServer()
                .baseUrl("http://localhost:8087/monitored-path")
                .build()
                .get()
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].path").isEqualTo(path)
                .jsonPath("$[0].mode").isEqualTo(SearchMode.PATTERN.toString())
                .jsonPath("$[0].noSubdirs").isEqualTo(false)
                .jsonPath("$[0].pattern").isEqualTo("*.txt")
                .jsonPath("$[0].createdAt").isEqualTo(createdAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
    }

    @Test
    public void testCreateMonitoring() {
        var path = "/";
        var dto = new CreateMonitorPathDTO(path, SearchMode.FULL, false);
        WebTestClient
                .bindToServer()
                .baseUrl("http://localhost:8087/monitored-path")
                .build()
                .post()
                .bodyValue(dto)
                .exchange()
                .expectStatus().isCreated();
        var absolutePath = Paths.get(path).toAbsolutePath().toString();
        var monitoredPath = monitoredPathRepository.findByPath(absolutePath);
        Assertions.assertNotNull(monitoredPath);
        Assertions.assertTrue(monitoredPath.isPresent());
        var monitorPathDTO = monitoredPath.get();
        Assertions.assertEquals(1, monitorPathDTO.getId());
        Assertions.assertEquals(absolutePath, monitorPathDTO.getPath());
        Assertions.assertEquals(SearchMode.FULL, monitorPathDTO.getMode());
        Assertions.assertFalse(monitorPathDTO.isNoSubdirs());
        Assertions.assertEquals(Pattern.compile(".*").toString(), monitorPathDTO.compilePattern().toString());
        Assertions.assertEquals(LocalDate.now(), monitorPathDTO.getCreatedAt());
    }

    @Test
    public void testUniquePath() {
        var path = "/";
        var dto = new CreateMonitorPathDTO(path, SearchMode.FULL, false);
        WebTestClient
                .bindToServer()
                .baseUrl("http://localhost:8087/monitored-path")
                .build()
                .post()
                .bodyValue(dto)
                .exchange()
                .expectStatus().isCreated();
        WebTestClient
                .bindToServer()
                .baseUrl("http://localhost:8087/monitored-path")
                .build()
                .post()
                .bodyValue(dto)
                .exchange()
                .expectStatus().value(status -> Assertions.assertEquals(status, HttpStatus.CONFLICT.value()));
    }

    @Test
    public void testNonExistingPath() {
        var path = "C:\\Ordner\\CON";
        var dto = new CreateMonitorPathDTO(path, SearchMode.FULL, false);
        WebTestClient
                .bindToServer()
                .baseUrl("http://localhost:8087/monitored-path")
                .build()
                .post()
                .bodyValue(dto)
                .exchange()
                .expectStatus().value(status -> Assertions.assertEquals(status, HttpStatus.UNPROCESSABLE_ENTITY.value()));
    }

    @Test
    void testDeleteNotFoundMonitoringPath() {
        var path = "demo";
        var saved = monitoredPathRepository.save(
                new MonitoredPath()
                        .path(path)
                        .createdAt(LocalDate.now()));

        monitoredPathRepository.deleteById(saved.getId());

        WebTestClient
                .bindToServer()
                .baseUrl("http://localhost:8087/monitored-path")
                .build()
                .delete()
                .uri("/1")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void testDeleteMonitoringPath() {
        var path = "demo";
        var saved = monitoredPathRepository.save(
                new MonitoredPath()
                        .path(path)
                        .createdAt(LocalDate.now()));

        WebTestClient
                .bindToServer()
                .baseUrl("http://localhost:8087/monitored-path")
                .build()
                .delete()
                .uri("/" + saved.getId())
                .exchange()
                .expectStatus().isNoContent();
        Assertions.assertFalse(monitoredPathRepository.existsById(saved.getId()));
    }

    @Test
    void shouldReturnUnprocessableResponseWhenNotEnoughSnapshots() {
        final MonitoredPath monitoredPath = monitoredPathRepository.save(new MonitoredPath("/test", SearchMode.FULL, null, false));

        WebTestClient
                .bindToServer()
                .baseUrl("http://localhost:8087/monitored-path/")
                .build()
                .get()
                .uri(monitoredPath.getId() + "/changes")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectBody()
                .jsonPath("$.status").isEqualTo(422)
                .jsonPath("$.message").isEqualTo("Not enough snapshots existing at the moment for this range");
    }

    // todo: insert snapshot ids to snapshot list in cli

    @Test
    void shouldReturnComparison() {
        // when
        final MonitoredPath monitoredPath = monitoredPathRepository.save(new MonitoredPath("/test", SearchMode.FULL, null, false));

        final LocalDateTime previousSnapshotCreation = LocalDateTime.of(2024, 12, 1, 15, 30);
        final Snapshot previousSnapshot = snapshotRepository.save(new Snapshot(Timestamp.valueOf(previousSnapshotCreation), monitoredPath));

        final LocalDateTime subsequentSnapshotCreation = LocalDateTime.of(2024, 12, 1, 17, 30);
        final Snapshot subsequentSnapshot = snapshotRepository.save(new Snapshot(Timestamp.valueOf(subsequentSnapshotCreation), monitoredPath));

        nodeRepository.save(new Node(previousSnapshot, "/test/cache.txt", true, false));
        nodeRepository.save(new Node(previousSnapshot, "/test/log/logs.txt", true, false));

        nodeRepository.save(new Node(subsequentSnapshot, "/test/cache.txt", false, true));
        nodeRepository.save(new Node(subsequentSnapshot, "/test/log/logs.txt", false, true));

        // then
        WebTestClient
                .bindToServer()
                .baseUrl("http://localhost:8087/monitored-path/")
                .build()
                .get()
                .uri(monitoredPath.getId() + "/changes")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.monitoredPath").isEqualTo("/test")
                .jsonPath("$.startSnapshotCreation").<String>value(s -> LocalDateTime.parse(s).isEqual(subsequentSnapshotCreation))
                .jsonPath("$.endSnapshotCreation").<String>value(s -> LocalDateTime.parse(s).isEqual(previousSnapshotCreation))
                .jsonPath("$.comparison[0].path").isEqualTo("/test/cache.txt")
                .jsonPath("$.comparison[0].snapshotIds").<List<Integer>>value(l -> assertThat(l).containsExactly(previousSnapshot.getId(), subsequentSnapshot.getId()))
                .jsonPath("$.comparison[0].comparison").<List<String>>value(l -> assertThat(l).containsExactly("CHANGED", "LAST TRACK"))
                .jsonPath("$.comparison[1].path").isEqualTo("/test/log/logs.txt")
                .jsonPath("$.comparison[1].snapshotIds").<List<Integer>>value(l -> assertThat(l).containsExactly(previousSnapshot.getId(), subsequentSnapshot.getId()))
                .jsonPath("$.comparison[1].comparison").<List<String>>value(l -> assertThat(l).containsExactly("CHANGED", "LAST TRACK"));
    }

    @Test
    void shouldReturnSnapshots() {
        // when
        final MonitoredPath monitoredPath = monitoredPathRepository.save(new MonitoredPath("/test", SearchMode.FULL, null, false));

        final LocalDateTime localDateTime = LocalDateTime.of(2024, 12, 1, 15, 30);
        final Snapshot snapshot = snapshotRepository.save(new Snapshot(Timestamp.valueOf(localDateTime), monitoredPath));

        // then
        WebTestClient
                .bindToServer()
                .baseUrl("http://localhost:8087/monitored-path/")
                .build()
                .get()
                .uri(monitoredPath.getId() + "/snapshots")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].id").isEqualTo(snapshot.getId())
                .jsonPath("$[0].timestamp").<String>value(s -> LocalDateTime.parse(s).isEqual(localDateTime));
    }
}
