package ch.bfh.tracesentry.cli.command;

import ch.bfh.tracesentry.cli.adapter.DaemonAdapter;
import ch.bfh.tracesentry.cli.util.ShellLines;
import ch.bfh.tracesentry.lib.dto.SearchResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.shell.test.ShellTestClient;
import org.springframework.shell.test.autoconfigure.AutoConfigureShell;
import org.springframework.shell.test.autoconfigure.AutoConfigureShellTestClient;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureShell
@AutoConfigureShellTestClient
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class SearchCommandsIT {

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
    void shouldOutputFoundFiles() {
        final Path relSearchPath = Paths.get("test");
        when(restTemplate.getForEntity(
                DaemonAdapter.BASE_URL + "search?path=" + relSearchPath.toAbsolutePath() + "&mode=full",
                SearchResponseDTO.class)
        )
                .thenReturn(ResponseEntity.ok().body(new SearchResponseDTO(2, List.of(
                                        Paths.get("test", "log.txt").toAbsolutePath().toString(),
                                        Paths.get("test", "cache", "cache.txt").toAbsolutePath().toString()
                                ))
                        )
                );

        ShellTestClient.NonInteractiveShellSession session = client
                .nonInterative("search", relSearchPath.toString())
                .run();

        await().atMost(1, TimeUnit.SECONDS).untilAsserted(() -> {
            String joinedLines = ShellLines.join(session.screen().lines());
            assertThat(joinedLines).startsWith(
                    "Listing 2 files in " + relSearchPath.toAbsolutePath() + ":\n"
                            + "log.txt\n"
                            + Paths.get("cache", "cache.txt")
            );
        });
    }

    @ParameterizedTest
    @ValueSource(strings = {"log", "cache", "full"}) // pattern is tested separately
    void shouldAcceptValidSearchModes(String mode) {
        final Path relSearchPath = Paths.get("test");
        String url = DaemonAdapter.BASE_URL + "search?path=" + relSearchPath.toAbsolutePath() + "&mode=" + mode;

        when(restTemplate.getForEntity(url, SearchResponseDTO.class))
                .thenReturn(ResponseEntity.ok().body(new SearchResponseDTO(0, List.of()))
                );

        ShellTestClient.NonInteractiveShellSession session = client
                .nonInterative("search", relSearchPath.toString(), "--mode", mode)
                .run();

        await().atMost(1, TimeUnit.SECONDS).untilAsserted(() -> {
            String joinedLines = ShellLines.join(session.screen().lines());
            assertThat(joinedLines).startsWith(
                    "Listing 0 files in " + relSearchPath.toAbsolutePath() + ":\n"
            );
        });
    }

    @Test
    void shouldAcceptValidPattern() {
        final Path relSearchPath = Paths.get("test");
        when(restTemplate.getForEntity(
                DaemonAdapter.BASE_URL + "search?path=" + relSearchPath.toAbsolutePath() + "&mode=pattern&pattern=" + URLEncoder.encode(".*\\.cookie", StandardCharsets.UTF_8),
                SearchResponseDTO.class)
        )
                .thenReturn(ResponseEntity.ok().body(new SearchResponseDTO(2, List.of(
                                        Paths.get("test", "cookie1.cookie").toAbsolutePath().toString(),
                                        Paths.get("test", "cookie2.cookie").toAbsolutePath().toString()
                                ))
                        )
                );

        ShellTestClient.NonInteractiveShellSession session = client
                .nonInterative("search", relSearchPath.toString(), "--mode", "pattern", "--pattern", ".*\\\\.cookie")
                .run();

        await().atMost(1, TimeUnit.SECONDS).untilAsserted(() -> {
            String joinedLines = ShellLines.join(session.screen().lines());
            assertThat(joinedLines).startsWith(
                    "Listing 2 files in " + relSearchPath.toAbsolutePath() + ":\n"
                            + "cookie1.cookie\n"
                            + "cookie2.cookie"
            );
        });
    }

    @Test
    void shouldRejectInvalidSearchModes() {
        final Path relSearchPath = Paths.get("test", "dir");
        ShellTestClient.NonInteractiveShellSession session = client
                .nonInterative("search", relSearchPath.toString(), "--mode", "any")
                .run();

        await().atMost(1, TimeUnit.SECONDS).untilAsserted(() -> {
            String joinedLines = ShellLines.join(session.screen().lines());
            assertThat(joinedLines).contains("Invalid mode. Use: log, cache, full, pattern.");
        });
    }

    @Test
    void shouldRequirePatternForPatternMode() {
        final Path relSearchPath = Paths.get("test", "dir");
        ShellTestClient.NonInteractiveShellSession session = client
                .nonInterative("search", relSearchPath.toString(), "--mode", "pattern")
                .run();

        await().atMost(1, TimeUnit.SECONDS).untilAsserted(() -> {
            String joinedLines = ShellLines.join(session.screen().lines());
            assertThat(joinedLines).contains("Option '--pattern' is required for mode 'pattern'.");
        });
    }

    @Test
    void shouldRejectInvalidPattern() {
        final Path relSearchPath = Paths.get("test", "dir");
        ShellTestClient.NonInteractiveShellSession session = client
                .nonInterative("search", relSearchPath.toString(), "--mode", "pattern", "--pattern", "*")
                .run();

        await().atMost(1, TimeUnit.SECONDS).untilAsserted(() -> {
            String joinedLines = ShellLines.join(session.screen().lines());
            assertThat(joinedLines).contains("Invalid value for option '--pattern'.");
        });
    }

    @ParameterizedTest
    @ValueSource(strings = {"log", "cache", "full"})
    void shouldRejectPatternForOtherModes(String mode) {
        final Path relSearchPath = Paths.get("test", "dir");
        ShellTestClient.NonInteractiveShellSession session = client
                .nonInterative("search", relSearchPath.toString(), "--mode", mode, "--pattern", ".*\\.cookie")
                .run();

        await().atMost(1, TimeUnit.SECONDS).untilAsserted(() -> {
            String joinedLines = ShellLines.join(session.screen().lines());
            assertThat(joinedLines).contains("Option '--pattern' must not be set for mode '" + mode + "'.");
        });
    }
}
