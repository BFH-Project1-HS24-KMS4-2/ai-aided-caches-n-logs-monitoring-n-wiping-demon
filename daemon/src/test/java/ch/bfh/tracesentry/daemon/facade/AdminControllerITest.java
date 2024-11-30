package ch.bfh.tracesentry.daemon.facade;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
public class AdminControllerITest {
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
