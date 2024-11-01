package ch.bfh.tracesentry.cli;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.shell.test.ShellAssertions;
import org.springframework.shell.test.ShellTestClient;
import org.springframework.shell.test.autoconfigure.AutoConfigureShell;
import org.springframework.shell.test.autoconfigure.AutoConfigureShellTestClient;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureShell
@AutoConfigureShellTestClient
public class CliApplicationTests {
    @Autowired
    private ShellTestClient client;

    @MockBean
    private RestTemplate restTemplate;


    @Test
    void testStatus() {
        when(restTemplate.getForObject(DaemonAdapter.BASE_URL + "status", String.class))
                .thenReturn("tracesentry");

        ShellTestClient.NonInteractiveShellSession session = client
                .nonInterative("status")
                .run();

        await().atMost(101, TimeUnit.MILLISECONDS).untilAsserted(() -> {
            ShellAssertions.assertThat(session.screen())
                    .containsText("daemon is running");
        });
    }
}