package ch.bfh.tracesentry.daemon.facade;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.nio.file.Paths;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
class SearchControllerTest {

    @Test
    void shouldReturnBadRequestWhenSearchPathNotDirectory() {
        WebTestClient
                .bindToServer()
                .baseUrl("http://localhost:8087/")
                .build()
                .get()
                .uri("/search?path=src/test/resources/home/test.txt")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(String.class).isEqualTo("Search Path is not a directory.");
    }

    @Test
    void shouldReturnNotFoundWhenSearchPathIsEmpty() {
        WebTestClient
                .bindToServer()
                .baseUrl("http://localhost:8087/")
                .build()
                .get()
                .uri("/search?path=")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(String.class).isEqualTo("Search Path does not exist.");
    }

    @Test
    void shouldReturnNotFoundWhenSearchPathDoesNotExist() {
        WebTestClient
                .bindToServer()
                .baseUrl("http://localhost:8087/")
                .build()
                .get()
                .uri("/search?path=src/test/resources/notexisting/test")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(String.class).isEqualTo("Search Path does not exist.");
    }

    @Test
    void shouldReturnFoundLogAndCacheFiles() {
        WebTestClient
                .bindToServer()
                .baseUrl("http://localhost:8087/")
                .build()
                .get()
                .uri("/search?path=src/test/resources/home")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("numberOfFiles").isEqualTo(4)
                .jsonPath("files")
                        .value(files -> assertThat((List<String>) files)
                        .containsExactlyInAnyOrder(
                                Paths.get("src/test/resources/home/test.log").toString(),
                                Paths.get("src/test/resources/home/test-LOG.txt").toString(),
                                Paths.get("src/test/resources/home/Cache/Cache-info.txt").toString(),
                                Paths.get("src/test/resources/home/Cache/test.cache").toString()
                        ));
    }

    @Test
    void shouldDisplayStatus() {
        WebTestClient
                .bindToServer()
                .baseUrl("http://localhost:8087/")
                .build()
                .get()
                .uri("/status")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).isEqualTo("tracesentry");
    }
}
