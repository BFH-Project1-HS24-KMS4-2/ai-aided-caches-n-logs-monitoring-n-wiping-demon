package ch.bfh.tracesentry.cli.command;

import ch.bfh.tracesentry.cli.adapter.DaemonAdapter;
import ch.bfh.tracesentry.cli.util.ShellLines;
import ch.bfh.tracesentry.lib.dto.CreateMonitorPathDTO;
import ch.bfh.tracesentry.lib.dto.MonitorPathDTO;
import ch.bfh.tracesentry.lib.model.SearchMode;
import ch.bfh.tracesentry.cli.util.ShellLines;
import ch.bfh.tracesentry.lib.dto.MonitoredChangesDTO;
import ch.bfh.tracesentry.lib.dto.MonitoredPathDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.shell.test.ShellTestClient;
import org.springframework.shell.test.autoconfigure.AutoConfigureShell;
import org.springframework.shell.test.autoconfigure.AutoConfigureShellTestClient;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
public class MonitorCommandsIT {

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
    void testMonitorAdd() {
        String absolutePath = "C:\\Users\\user\\Desktop\\path\\to\\dir";
        String shellParam = absolutePath.replace("\\", "\\\\");

        ResponseEntity<Void> responseEntity = ResponseEntity.status(201).build();

        when(restTemplate.postForEntity(
                eq(DaemonAdapter.BASE_URL + "monitored-path"),
                any(CreateMonitorPathDTO.class),
                eq(Void.class)))
                .thenReturn(responseEntity);

        ShellTestClient.NonInteractiveShellSession session = client
                .nonInterative("monitor", "add", shellParam)
                .run();

        await().atMost(1, TimeUnit.SECONDS).untilAsserted(() -> {
            String joinedLines = ShellLines.join(session.screen().lines());
            assertThat(joinedLines).startsWith("Successfully added " + absolutePath + " to the monitoring database.");
        });
    }

    @Test
    void testMonitorAdd422() {
        String absolutePath = "C:\\Users\\user\\Desktop\\path\\to\\dir";
        String shellParam = absolutePath.replace("\\", "\\\\");

        ResponseEntity<Void> responseEntity = ResponseEntity.status(422).build();

        when(restTemplate.postForEntity(
                eq(DaemonAdapter.BASE_URL + "monitored-path"),
                any(CreateMonitorPathDTO.class),
                eq(Void.class)))
                .thenReturn(responseEntity);

        ShellTestClient.NonInteractiveShellSession session = client
                .nonInterative("monitor", "add", shellParam)
                .run();

        await().atMost(1, TimeUnit.SECONDS).untilAsserted(() -> {
            String joinedLines = ShellLines.join(session.screen().lines());
            assertThat(joinedLines).startsWith("Error: " + absolutePath + " could not be added to the monitoring database.");
        });
    }

    @Test
    void testMonitorAdd409() {
        String absolutePath = "C:\\Users\\user\\Desktop\\path\\to\\dir";
        String shellParam = absolutePath.replace("\\", "\\\\");

        ResponseEntity<Void> responseEntity = ResponseEntity.status(409).build();

        when(restTemplate.postForEntity(
                eq(DaemonAdapter.BASE_URL + "monitored-path"),
                any(CreateMonitorPathDTO.class),
                eq(Void.class)))
                .thenReturn(responseEntity);

        ShellTestClient.NonInteractiveShellSession session = client
                .nonInterative("monitor", "add", shellParam)
                .run();

        await().atMost(1, TimeUnit.SECONDS).untilAsserted(() -> {
            String joinedLines = ShellLines.join(session.screen().lines());
            assertThat(joinedLines).startsWith("Error: " + absolutePath + " is already being monitored.");
        });
    }

    @Test
    void testMonitorList() {
        List<MonitoredPathDTO> monitoredPathDTOList = new ArrayList<>();
        monitoredPathDTOList.add(new MonitoredPathDTO(3112, "C:\\Users\\CoolDude", SearchMode.FULL, null, false, LocalDate.of(2023, 11, 8)));
        monitorPathDTOList.add(new MonitorPathDTO(3112, "C:\\Users\\CoolDude", SearchMode.PATTERN, "*.txt", false, LocalDate.of(2023, 11, 8)));
        monitoredPathDTOList.add(new MonitoredPathDTO(202, "C:\\Users", SearchMode.LOG, null, true, LocalDate.of(2024, 12, 8)));
        ParameterizedTypeReference<List<MonitoredPathDTO>> responseType =
                new ParameterizedTypeReference<>() {
                };

        when(restTemplate.exchange(DaemonAdapter.BASE_URL + "monitored-path", HttpMethod.GET, null, responseType))
                .thenReturn(ResponseEntity.ok().body(monitoredPathDTOList));

        ShellTestClient.NonInteractiveShellSession session = client
                .nonInterative("monitor", "list")
                .run();

        await().atMost(1, TimeUnit.SECONDS).untilAsserted(() -> {
            String joinedLines = ShellLines.join(session.screen().lines());
            assertThat(joinedLines).contains("3112", "C:\\Users\\CoolDude", "FULL", "2023-11-08");
            assertThat(joinedLines).contains("3112", "C:\\Users\\CoolDude", "PATTERN", "*.txt", "2023-11-08");
            assertThat(joinedLines).contains("202", "C:\\Users", "LOG", "true", "2024-12-08");
        });
    }

    @Test
    void testMonitorListEmpty() {
        List<MonitoredPathDTO> monitoredPathDTOList = new ArrayList<>();

        ParameterizedTypeReference<List<MonitoredPathDTO>> responseType =
                new ParameterizedTypeReference<>() {
                };

        when(restTemplate.exchange(DaemonAdapter.BASE_URL + "monitored-path", HttpMethod.GET, null, responseType))
                .thenReturn(ResponseEntity.ok().body(monitoredPathDTOList));

        ShellTestClient.NonInteractiveShellSession session = client
                .nonInterative("monitor", "list")
                .run();

        await().atMost(1, TimeUnit.SECONDS).untilAsserted(() -> {
            String joinedLines = ShellLines.join(session.screen().lines());
            assertThat(joinedLines).startsWith("No paths are currently being monitored.");
        });
    }

    @Test
    void testMonitorRemove() {
        int id = 3112;

        ResponseEntity<Void> responseEntity = ResponseEntity.status(204).build();

        when(restTemplate.exchange(DaemonAdapter.BASE_URL + "monitored-path/" + id, HttpMethod.DELETE, null, Void.class))
                .thenReturn(responseEntity);

        ShellTestClient.NonInteractiveShellSession session = client
                .nonInterative("monitor", "remove", String.valueOf(id))
                .run();

        await().atMost(1, TimeUnit.SECONDS).untilAsserted(() -> {
            String joinedLines = ShellLines.join(session.screen().lines());
            assertThat(joinedLines).startsWith("Successfully removed path with ID " + id + " from the monitoring database.");
        });
    }

    @Test
    void testMonitorRemove404() {
        int id = 9999;

        ResponseEntity<Void> responseEntity = ResponseEntity.status(404).build();

        when(restTemplate.exchange(DaemonAdapter.BASE_URL + "monitored-path/" + id, HttpMethod.DELETE, null, Void.class))
                .thenReturn(responseEntity);

        ShellTestClient.NonInteractiveShellSession session = client
                .nonInterative("monitor", "remove", String.valueOf(id))
                .run();

        await().atMost(1, TimeUnit.SECONDS).untilAsserted(() -> {
            String joinedLines = ShellLines.join(session.screen().lines());
            assertThat(joinedLines).startsWith("Error: No monitored path found with ID " + id + ".");
        });
    }

    @Test
    void shouldOutputMonitoredChanges() {
        final int id = 5;
        when(restTemplate.getForEntity(DaemonAdapter.BASE_URL + "monitored-path/" + id + "/changes", MonitoredChangesDTO.class))
                .thenReturn(ResponseEntity.of(Optional.of(new MonitoredChangesDTO(
                        "/test",
                        LocalDateTime.of(2024, 12, 1, 15, 30),
                        LocalDateTime.of(2024, 12, 1, 17, 30),
                        List.of("/test/changed.txt"),
                        List.of("/test/deleted.txt")
                ))));

        ShellTestClient.NonInteractiveShellSession session = client
                .nonInterative("monitor", "compare", String.valueOf(id))
                .run();

        await().atMost(1, TimeUnit.SECONDS).untilAsserted(() -> {
            final String output = ShellLines.join(session.screen().lines());
            assertThat(output).startsWith("""
                    Listing comparison of /test from 01.12.2024 15:30:00 to 01.12.2024 17:30:00...
                    Changed files:
                    changed.txt
                    
                    Deleted files:
                    deleted.txt
                    """);
        });
    }
}
