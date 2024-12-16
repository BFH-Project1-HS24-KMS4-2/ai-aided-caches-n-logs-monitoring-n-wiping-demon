package ch.bfh.tracesentry.cli.command;

import ch.bfh.tracesentry.cli.adapter.DaemonAdapter;
import ch.bfh.tracesentry.cli.util.ShellLines;
import ch.bfh.tracesentry.lib.dto.WipeFileDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.shell.test.ShellTestClient;
import org.springframework.shell.test.autoconfigure.AutoConfigureShell;
import org.springframework.shell.test.autoconfigure.AutoConfigureShellTestClient;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureShell
@AutoConfigureShellTestClient
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class WipeCommandsITest {

    @Autowired
    private ShellTestClient client;

    @MockBean
    private RestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        when(restTemplate.getForObject(DaemonAdapter.BASE_URL + "status", String.class))
                .thenReturn("tracesentry");
    }

    @Test
    void testClearFileContents() {
        var dto = new WipeFileDTO("test.log", false);
        when(restTemplate.postForEntity(DaemonAdapter.BASE_URL + "wipe", dto, Void.class))
                .thenReturn(ResponseEntity.ok().build());

        ShellTestClient.NonInteractiveShellSession session = client
                .nonInterative("wipe", "test.log")
                .run();

        await().atMost(1, TimeUnit.SECONDS).untilAsserted(() -> {
            String joinedLines = ShellLines.join(session.screen().lines());
            assertThat(joinedLines).startsWith("Successfully cleared file.");
        });
    }

    @Test
    void testRemoveFile() {
        var dto = new WipeFileDTO("test.log", true);
        when(restTemplate.postForEntity(DaemonAdapter.BASE_URL + "wipe", dto, Void.class))
                .thenReturn(ResponseEntity.ok().build());

        ShellTestClient.NonInteractiveShellSession session = client
                .nonInterative("wipe", "test.log", "--remove")
                .run();

        await().atMost(1, TimeUnit.SECONDS).untilAsserted(() -> {
            String joinedLines = ShellLines.join(session.screen().lines());
            assertThat(joinedLines).startsWith("Successfully removed file.");
        });
    }

    @Test
    void testClearNotExistingFile() {
        var dto = new WipeFileDTO("notexisting.log", false);
        when(restTemplate.postForEntity(DaemonAdapter.BASE_URL + "wipe", dto, Void.class))
                .thenReturn(ResponseEntity.notFound().build());

        ShellTestClient.NonInteractiveShellSession session = client
                .nonInterative("wipe", "notexisting.log")
                .run();

        await().atMost(1, TimeUnit.SECONDS).untilAsserted(() -> {
            String joinedLines = ShellLines.join(session.screen().lines());
            assertThat(joinedLines).startsWith("Error: File could not be processed.");
        });
    }
}
