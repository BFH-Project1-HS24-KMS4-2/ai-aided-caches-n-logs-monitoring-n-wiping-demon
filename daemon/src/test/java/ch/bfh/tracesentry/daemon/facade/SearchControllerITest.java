package ch.bfh.tracesentry.daemon.facade;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
class SearchControllerITest {

    @ParameterizedTest
    @ValueSource(strings = {"explicit", "implicit"})
    void shouldReturnFoundLogAndCacheFiles(String searchModeTest) {
        String uri;
        Path path = Paths.get("src", "test", "resources", "home");
        if ("explicit".equals(searchModeTest)) {
            uri = "/search?path=" + path + "&mode=full";
        } else {
            uri = "/search?path=" + path;
        }

        WebTestClient
                .bindToServer()
                .baseUrl("http://localhost:8087/")
                .build()
                .get()
                .uri(uri)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("numberOfFiles").isEqualTo(4)
                .jsonPath("files")
                .value(files -> assertThat((List<String>) files)
                        .containsExactlyInAnyOrder(
                                Paths.get("src", "test", "resources", "home", "test.log").toString(),
                                Paths.get("src", "test", "resources", "home", "test-LOG.txt").toString(),
                                Paths.get("src", "test", "resources", "home", "Cache", "Cache-info.txt").toString(),
                                Paths.get("src", "test", "resources", "home", "Cache", "test.cache").toString()
                        ));
    }

    @Test
    void shouldReturnUnprocessableWhenSearchPathNotDirectory() {
        WebTestClient
                .bindToServer()
                .baseUrl("http://localhost:8087/")
                .build()
                .get()
                .uri("/search?path=" + Paths.get("src", "test", "resources", "home", "test.txt"))
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    void shouldReturnUnprocessableWhenSearchPathIsEmpty() {
        WebTestClient
                .bindToServer()
                .baseUrl("http://localhost:8087/")
                .build()
                .get()
                .uri("/search?path=")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    void shouldReturnUnprocessableWhenSearchPathDoesNotExist() {
        WebTestClient
                .bindToServer()
                .baseUrl("http://localhost:8087/")
                .build()
                .get()
                .uri("/search?path=" + Paths.get("src", "test", "resources", "notexisting", "test"))
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    void shouldReturnFoundLogFiles() {
        WebTestClient
                .bindToServer()
                .baseUrl("http://localhost:8087/")
                .build()
                .get()
                .uri("/search?path=" + Paths.get("src", "test", "resources", "home") + "&mode=log")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("numberOfFiles").isEqualTo(2)
                .jsonPath("files")
                .value(files -> assertThat((List<String>) files)
                        .containsExactlyInAnyOrder(
                                Paths.get("src", "test", "resources", "home", "test.log").toString(),
                                Paths.get("src", "test", "resources", "home", "test-LOG.txt").toString()
                        ));
    }

    @Test
    void shouldReturnFoundCacheFiles() {
        WebTestClient
                .bindToServer()
                .baseUrl("http://localhost:8087/")
                .build()
                .get()
                .uri("/search?path=" + Paths.get("src", "test", "resources", "home") + "&mode=cache")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("numberOfFiles").isEqualTo(2)
                .jsonPath("files")
                .value(files -> assertThat((List<String>) files)
                        .containsExactlyInAnyOrder(
                                Paths.get("src", "test", "resources", "home", "Cache", "Cache-info.txt").toString(),
                                Paths.get("src", "test", "resources", "home", "Cache", "test.cache").toString()
                        ));
    }

    @Test
    void shouldRejectInvalidPattern() {
        WebTestClient
                .bindToServer()
                .baseUrl("http://localhost:8087/")
                .build()
                .get()
                .uri("/search?path=" + Paths.get("src", "test", "resources", "home") + "&pattern=*.txt")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void shouldRequirePatternForPatternMode() {
        WebTestClient
                .bindToServer()
                .baseUrl("http://localhost:8087/")
                .build()
                .get()
                .uri("/search?path=" + Paths.get("src", "test", "resources", "home") + "&mode=pattern")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void shouldReturnFilesWithPattern() {
        WebTestClient
                .bindToServer()
                .baseUrl("http://localhost:8087/")
                .build()
                .get()
                .uri("/search?path=" + Paths.get("src", "test", "resources", "home") + "&mode=pattern&pattern=.*\\.cookie")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("numberOfFiles").isEqualTo(1)
                .jsonPath("files")
                .value(files -> assertThat((List<String>) files)
                        .containsExactlyInAnyOrder(
                                Paths.get("src", "test", "resources", "home", "Cache", "test.cookie").toString()
                        ));
    }

    @Test
    void shouldRequirePatternModeForPattern() {
        WebTestClient
                .bindToServer()
                .baseUrl("http://localhost:8087/")
                .build()
                .get()
                .uri("/search?path=" + Paths.get("src", "test", "resources", "home") + "&pattern=.*\\.cookie")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void shouldReturnFilesWithoutSubdirectories() {
        WebTestClient
                .bindToServer()
                .baseUrl("http://localhost:8087/")
                .build()
                .get()
                .uri("/search?path=" + Paths.get("src", "test", "resources", "home") + "&no-subdirs=true")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("numberOfFiles").isEqualTo(2)
                .jsonPath("files")
                .value(files -> assertThat((List<String>) files)
                        .containsExactlyInAnyOrder(
                                Paths.get("src", "test", "resources", "home", "test.log").toString(),
                                Paths.get("src", "test", "resources", "home", "test-LOG.txt").toString()
                        ));
    }
}
