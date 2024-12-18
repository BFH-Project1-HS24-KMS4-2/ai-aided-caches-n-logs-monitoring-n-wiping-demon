package ch.bfh.tracesentry.cli.command;

import ch.bfh.tracesentry.cli.adapter.DaemonAdapter;
import ch.bfh.tracesentry.cli.util.ShellLines;
import ch.bfh.tracesentry.lib.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.shell.test.ShellTestClient;
import org.springframework.shell.test.autoconfigure.AutoConfigureShell;
import org.springframework.shell.test.autoconfigure.AutoConfigureShellTestClient;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureShell
@AutoConfigureShellTestClient
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class SnapshotsCommandsITest {

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
    void shouldOutputMonitoredChanges() {
        final int id = 5;
        final int start = 1;
        final int end = 10;
        when(restTemplate.getForEntity(DaemonAdapter.BASE_URL + "monitored-path/" + id + "/snapshots/changes?start=" + start + "&end=" + end, MonitoredChangesDTO.class))
                .thenReturn(ResponseEntity.ok(new MonitoredChangesDTO(
                        "/test",
                        LocalDateTime.of(2024, 12, 1, 17, 30),
                        LocalDateTime.of(2024, 12, 1, 15, 30),
                        List.of(
                                new SnapshotComparisonDTO(List.of(4, 6), "/test/cache.txt", List.of("CHANGED", "LAST TRACK")),
                                new SnapshotComparisonDTO(List.of(2, 8), "/test/log/log.txt", List.of("CHANGED", "CHANGED"))
                        )
                )));

        ShellTestClient.NonInteractiveShellSession session = client
                .nonInterative("snapshots", "compare", String.valueOf(id), String.valueOf(start), String.valueOf(end))
                .run();

        await().atMost(1, TimeUnit.SECONDS).untilAsserted(() -> {
            final String output = ShellLines.join(session.screen().lines());
            assertThat(output).startsWith("""
                    Listing comparison of /test from 01.12.2024 15:30:00 to 01.12.2024 17:30:00...
                    ┌───────────┬────────────┬──────────┐
                    │Path       │Snapshot IDs│Comparison│
                    ├───────────┼────────────┼──────────┤
                    │cache.txt  │4           │CHANGED   │
                    │           │6           │LAST TRACK│
                    ├───────────┼────────────┼──────────┤
                    """);
        });
    }

    @Test
    void shouldOutputSnapshots() {
        final int id = 5;
        LocalDateTime timestamp1 = LocalDateTime.of(2024, 12, 1, 20, 25);
        LocalDateTime timestamp2 = timestamp1.plusHours(1);

        when(restTemplate.exchange(
                DaemonAdapter.BASE_URL + "monitored-path/" + id + "/snapshots",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<SnapshotDTO>>() {
                }))
                .thenReturn(ResponseEntity.ok(List.of(
                        new SnapshotDTO(2, timestamp2),
                        new SnapshotDTO(1, timestamp1)
                        )));

        ShellTestClient.NonInteractiveShellSession session = client
                .nonInterative("snapshots", "list", String.valueOf(id))
                .run();

        await().atMost(1, TimeUnit.SECONDS).untilAsserted(() -> {
            final String output = ShellLines.join(session.screen().lines());
            assertThat(output).startsWith("""
                    ┌──────┬───────────────────┬──┐
                    │Number│Timestamp          │ID│
                    ├──────┼───────────────────┼──┤
                    │1     │01.12.2024 21:25:00│2 │
                    ├──────┼───────────────────┼──┤
                    │2     │01.12.2024 20:25:00│1 │
                    └──────┴───────────────────┴──┘
                    """);
        });
    }

    @Test
    void shouldOutputWarningOnNoMonitoredPath() {
        final int id = 5;
        when(restTemplate.exchange(DaemonAdapter.BASE_URL + "monitored-path/" + id + "/snapshots",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<SnapshotDTO>>() {
                }))
                .thenThrow(new RestClientResponseException("", HttpStatus.NOT_FOUND.value(), "Not found", null, null, null));

        ShellTestClient.NonInteractiveShellSession session = client
                .nonInterative("snapshots", "list", String.valueOf(id))
                .run();

        await().atMost(1, TimeUnit.SECONDS).untilAsserted(() ->
                assertThat(ShellLines.join(session.screen().lines()).trim())
                        .contains("No monitored path found with ID " + id + "."));
    }
}
