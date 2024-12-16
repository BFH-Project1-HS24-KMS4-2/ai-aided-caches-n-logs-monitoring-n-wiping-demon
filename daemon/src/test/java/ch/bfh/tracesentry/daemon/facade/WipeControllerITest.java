package ch.bfh.tracesentry.daemon.facade;

import ch.bfh.tracesentry.lib.dto.WipeFileDTO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class WipeControllerITest {

    @Test
    public void testFileToWipeDoesNotExist(@TempDir Path tempDir) {

        WipeFileDTO wipeFileDTO = new WipeFileDTO();

        wipeFileDTO.setPath(tempDir.resolve("test.txt").toAbsolutePath().toString());
        wipeFileDTO.setRemove(false);

        WebTestClient
                .bindToServer()
                .baseUrl("http://localhost:8087/wipe")
                .build()
                .post()
                .bodyValue(wipeFileDTO)
                .exchange()
                .expectStatus().value(status -> Assertions.assertEquals(status, HttpStatus.UNPROCESSABLE_ENTITY.value()));
    }

    @Test
    public void testWipeFileContents(@TempDir Path tempDir) throws IOException {

        Path fileToWipeItsContents = tempDir.resolve("test.txt");
        Files.createFile(fileToWipeItsContents);
        Files.writeString(fileToWipeItsContents, "test");

        WipeFileDTO wipeFileDTO = new WipeFileDTO();
        wipeFileDTO.setPath(fileToWipeItsContents.toAbsolutePath().toString());
        wipeFileDTO.setRemove(false);

        WebTestClient
                .bindToServer()
                .baseUrl("http://localhost:8087/wipe")
                .build()
                .post()
                .bodyValue(wipeFileDTO)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    public void testWipeFile(@TempDir Path tempDir) throws IOException {

        Path fileToWipe = tempDir.resolve("test.txt");
        Files.createFile(fileToWipe);

        WipeFileDTO wipeFileDTO = new WipeFileDTO();
        wipeFileDTO.setPath(fileToWipe.toAbsolutePath().toString());
        wipeFileDTO.setRemove(true);

        WebTestClient
                .bindToServer()
                .baseUrl("http://localhost:8087/wipe")
                .build()
                .post()
                .bodyValue(wipeFileDTO)
                .exchange()
                .expectStatus().isOk();

        assertThat(Files.exists(fileToWipe)).isFalse();
    }
}
