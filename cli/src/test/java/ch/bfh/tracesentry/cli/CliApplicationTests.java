package ch.bfh.tracesentry.cli;

import ch.bfh.tracesentry.cli.adapter.DaemonAdapter;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.shell.test.ShellAssertions;
import org.springframework.shell.test.ShellTestClient;
import org.springframework.shell.test.autoconfigure.ShellTest;
import org.springframework.test.annotation.DirtiesContext;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;

@ShellTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class CliApplicationTests {

    @Autowired
    ShellTestClient client;

    @MockBean
    DaemonAdapter daemonAdapter;

    @Test
    void test() {
        Mockito.when(daemonAdapter.checkStatus()).thenReturn(true);

        ShellTestClient.InteractiveShellSession session = client
                .interactive()
                .run();

        await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
            ShellAssertions.assertThat(session.screen())
                    .containsText("shell");
        });

        session.write(session.writeSequence().text("status").carriageReturn().build());
        await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
            ShellAssertions.assertThat(session.screen())
                    .containsText("daemon is running");
        });
    }
}
