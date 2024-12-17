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

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
        when(restTemplate.postForEntity(
                eq(DaemonAdapter.BASE_URL + "wipe"),
                any(WipeFileDTO.class),
                eq(Void.class)))
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
        when(restTemplate.postForEntity(
                eq(DaemonAdapter.BASE_URL + "wipe"),
                any(WipeFileDTO.class),
                eq(Void.class)))
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
        when(restTemplate.postForEntity(
                eq(DaemonAdapter.BASE_URL + "wipe"),
                any(WipeFileDTO.class),
                eq(Void.class)))
                .thenReturn(ResponseEntity.status(422).build());

        ShellTestClient.NonInteractiveShellSession session = client
                .nonInterative("wipe", "notexisting.log")
                .run();

        await().atMost(1, TimeUnit.SECONDS).untilAsserted(() -> {
            String joinedLines = ShellLines.join(session.screen().lines());
            assertThat(joinedLines).startsWith("Error: File could not be processed.");
        });
    }
}
