package ch.bfh.tracesentry.cli.command;

import ch.bfh.tracesentry.cli.adapter.DaemonAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.shell.test.ShellAssertions;
import org.springframework.shell.test.ShellTestClient;
import org.springframework.shell.test.autoconfigure.AutoConfigureShell;
import org.springframework.shell.test.autoconfigure.AutoConfigureShellTestClient;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureShell
@AutoConfigureShellTestClient
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class AdminCommandsIT {

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
    void testStatusIfRunning() {
        ShellTestClient.NonInteractiveShellSession session = client
                .nonInterative("status")
                .run();

        await().atMost(1, TimeUnit.SECONDS).untilAsserted(() -> ShellAssertions.assertThat(session.screen())
                .containsText("daemon is running"));
    }

    @Test
    void testStatusIfNotRunning() {
        when(restTemplate.getForObject(DaemonAdapter.BASE_URL + "status", String.class))
                .thenThrow(new RestClientException("daemon is not running"));

        ShellTestClient.NonInteractiveShellSession session = client
                .nonInterative("status")
                .run();

        await().atMost(1, TimeUnit.SECONDS).untilAsserted(() -> ShellAssertions.assertThat(session.screen())
                .containsText("daemon is not running"));
    }
}
