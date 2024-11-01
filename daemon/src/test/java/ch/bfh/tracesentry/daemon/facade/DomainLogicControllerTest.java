package ch.bfh.tracesentry.daemon.facade;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class DomainLogicControllerTest {

    @Test
    void shouldReturnBadRequestWhenSearchPathNotDirectory() {
        WebTestClient
                .bindToServer()
                .baseUrl("http://localhost:8087/")
                .build()
                .get()
                .uri("/search?path=test.txt")
                .exchange()
                .expectStatus().isBadRequest();
    }

    private static String getPathByOS(String path) {
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            return path.replace("/", "\\");
        }
        return path;
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
                                getPathByOS("src/test/resources/home/test.log"),
                                getPathByOS("src/test/resources/home/test-LOG.txt"),
                                getPathByOS("src/test/resources/home/Cache/Cache-info.txt"),
                                getPathByOS("src/test/resources/home/Cache/test.cache")
                        ));
    }

}
