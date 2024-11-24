package ch.bfh.tracesentry.daemon.facade;

import ch.bfh.tracesentry.daemon.domain.model.MonitoredPath;
import ch.bfh.tracesentry.daemon.domain.model.Node;
import ch.bfh.tracesentry.daemon.domain.model.Snapshot;
import ch.bfh.tracesentry.daemon.domain.repo.MonitoredPathRepository;
import ch.bfh.tracesentry.daemon.domain.repo.NodeRepository;
import ch.bfh.tracesentry.daemon.domain.repo.SnapshotRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
//language=h2
@Sql(statements = """
        CREATE TABLE IF NOT EXISTS monitored_path (
                                        id INTEGER PRIMARY KEY auto_increment,
                                        path TEXT NOT NULL UNIQUE,
                                        created_at DATE NOT NULL
        );
        
        CREATE TABLE IF NOT EXISTS snapshot (
                                  id INTEGER PRIMARY KEY auto_increment,
                                  monitored_path_id INTEGER NOT NULL,
                                  timestamp TIMESTAMP NOT NULL,
                                  FOREIGN KEY (monitored_path_id) REFERENCES monitored_path (id)
        );
        
        CREATE TABLE IF NOT EXISTS snapshot_node (
                                       id INTEGER PRIMARY KEY auto_increment,
                                       snapshot_id INTEGER NOT NULL,
                                       parent_id INTEGER,
                                       hash TEXT,
                                       path TEXT NOT NULL,
                                       has_changed BOOLEAN NOT NULL,
                                       deleted_in_next_snapshot BOOLEAN NOT NULL,
                                       FOREIGN KEY (snapshot_id) REFERENCES snapshot (id),
                                       FOREIGN KEY (parent_id) REFERENCES snapshot_node(id)
        );
        
        CREATE INDEX IF NOT EXISTS idx_snapshot_id ON snapshot_node(snapshot_id);
        """)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class MonitoringControllerIT {

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
                .jsonPath("$[0].createdAt").isEqualTo(createdAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
    }

    @Test
    public void testCreateMonitoring() {
        var path = "/";
        WebTestClient
                .bindToServer()
                .baseUrl("http://localhost:8087/monitored-path")
                .build()
                .post()
                .bodyValue(path)
                .exchange()
                .expectStatus().isCreated();
        var monitoredPath = monitoredPathRepository.findByPath(path);
        Assertions.assertNotNull(monitoredPath);
        Assertions.assertTrue(monitoredPath.isPresent());
        var monitorPathDTO = monitoredPath.get();
        Assertions.assertEquals(1, monitorPathDTO.getId());
        Assertions.assertEquals(path, monitorPathDTO.getPath());
        Assertions.assertEquals(LocalDate.now(), monitorPathDTO.getCreatedAt());
    }

    @Test
    public void testUniquePath() {
        var path = "/";
        WebTestClient
                .bindToServer()
                .baseUrl("http://localhost:8087/monitored-path")
                .build()
                .post()
                .bodyValue(path)
                .exchange()
                .expectStatus().isCreated();
        WebTestClient
                .bindToServer()
                .baseUrl("http://localhost:8087/monitored-path")
                .build()
                .post()
                .bodyValue(path)
                .exchange()
                .expectStatus().value(status -> Assertions.assertEquals(status, HttpStatus.CONFLICT.value()));
    }

    @Test
    public void testNonExistingPath() {
        var path = "C:\\Ordner\\CON";
        WebTestClient
                .bindToServer()
                .baseUrl("http://localhost:8087/monitored-path")
                .build()
                .post()
                .bodyValue(path)
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
    void shouldReturnUnprocessableResponseWhenNotEnoughSnapshots(){
        WebTestClient
                .bindToServer()
                .baseUrl("http://localhost:8087/monitored-path")
                .build()
                .get()
                .uri("/5/changes")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    void shouldReturnComparison() {
        // when
        final MonitoredPath monitoredPath = monitoredPathRepository.save(new MonitoredPath("/test"));

        final LocalDateTime previousSnapshotCreation = LocalDateTime.of(2024, 12, 1, 15, 30);
        final Snapshot previousSnapshot = snapshotRepository.save(new Snapshot(Timestamp.valueOf(previousSnapshotCreation), monitoredPath));

        final LocalDateTime subsequentSnapshotCreation = LocalDateTime.of(2024, 12, 1, 17, 30);
        final Snapshot subsequentSnapshot = snapshotRepository.save(new Snapshot(Timestamp.valueOf(subsequentSnapshotCreation), monitoredPath));

        final Node deletedNode = new Node();
        deletedNode.setSnapshot(previousSnapshot);
        deletedNode.setPath("/test/deleted.txt");
        deletedNode.setDeletedInNextSnapshot(true);
        nodeRepository.save(deletedNode);

        final Node addedNode = new Node();
        addedNode.setSnapshot(subsequentSnapshot);
        addedNode.setPath("/test/added.txt");
        addedNode.setHasChanged(true);
        nodeRepository.save(addedNode);

        // then
        WebTestClient
                .bindToServer()
                .baseUrl("http://localhost:8087/monitored-path")
                .build()
                .get()
                .uri("/1/changes")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.monitoredPath").isEqualTo("/test")
                .jsonPath("$.previousSnapshotCreation").<String>value(s -> LocalDateTime.parse(s).isEqual(previousSnapshotCreation))
                .jsonPath("$.subsequentSnapshotCreation").<String>value(s -> LocalDateTime.parse(s).isEqual(subsequentSnapshotCreation))
                .jsonPath("$.changedPaths[0]").isEqualTo("/test/added.txt")
                .jsonPath("$.deletedPaths[0]").isEqualTo("/test/deleted.txt");
    }
}
