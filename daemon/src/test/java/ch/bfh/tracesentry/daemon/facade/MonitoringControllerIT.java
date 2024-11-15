package ch.bfh.tracesentry.daemon.facade;

import ch.bfh.tracesentry.daemon.domain.MonitoredPath;
import ch.bfh.tracesentry.daemon.domain.MonitoredPathRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
//language=h2
@Sql(statements = "create table if not exists monitored_path (" +
        "    id integer primary key auto_increment," +
        "    path text not null unique," +
        "    created_at date not null" +
        ");")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class MonitoringControllerIT {

    @Autowired
    private MonitoredPathRepository monitoredPathRepository;


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

    @Test void testDeleteNotFoundMonitoringPath() {
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

    @Test void testDeleteMonitoringPath() {
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
}
