package ch.bfh.tracesentry.cli.command;

import ch.bfh.tracesentry.cli.adapter.DaemonAdapter;
import ch.bfh.tracesentry.lib.dto.MonitoredChangesDTO;
import ch.bfh.tracesentry.lib.exception.ErrorResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientResponseException;

import java.util.Objects;

import static ch.bfh.tracesentry.cli.util.Output.formatDateTime;
import static ch.bfh.tracesentry.cli.util.Output.formatFilePaths;

@ShellComponent
@ShellCommandGroup("Monitor Commands")
public class MonitorCommands {
    private final DaemonAdapter daemonAdapter;

    @Autowired
    public MonitorCommands(DaemonAdapter daemonAdapter) {
        this.daemonAdapter = daemonAdapter;
    }

    @ShellMethod(key = "monitor add", value = "Add a path to the monitoring database.")
    @SuppressWarnings("unused")
    public String monitorAdd(@ShellOption String path) {
        if (!daemonAdapter.checkStatus()) return "daemon is not running";
        var errorMessage = "Error: " + path + " could not be added to the monitoring database.";
        try {
            var response = daemonAdapter.monitorAdd(path);
            if (response.getStatusCode().is2xxSuccessful()) {
                return "Successfully added " + path + " to the monitoring database.";
            } else {
                return errorMessage;
            }
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 409) {
                return "Error: " + path + " is already being monitored.";
            }
            return errorMessage;
        } catch (Exception e) {
            return errorMessage;
        }
    }

    @ShellMethod(key = "monitor list", value = "List all paths currently being monitored.")
    @SuppressWarnings("unused")
    public String monitorList() {
        if (!daemonAdapter.checkStatus()) return "daemon is not running";
        try {
            var response = daemonAdapter.monitorList();
            var body = Objects.requireNonNull(response.getBody());
            if (body.isEmpty()) {
                return "No paths are currently being monitored.";
            }
            return "ID   | Added      | Path\n" +
                    "-----|------------|------------------------------------------\n" +
                    body.stream()
                            .map(m -> String.format("%04d | %s | %-24s", m.getId(), m.getCreatedAt(), m.getPath()))
                            .reduce("", (a, b) -> a + b + "\n");

        } catch (Exception e) {
            return "Error: could not list monitored paths.";
        }
    }

    @ShellMethod(key = "monitor remove", value = "Remove a path from the monitoring database.")
    @SuppressWarnings("unused")
    public String monitorRemove(@ShellOption int id) {
        if (!daemonAdapter.checkStatus()) return "daemon is not running";
        try {
            var response = daemonAdapter.monitorRemove(id);
            if (response.getStatusCode().is2xxSuccessful()) {
                return "Successfully removed path with ID " + id + " from the monitoring database.";
            } else {
                throw new Exception();
            }
        } catch (Exception e) {
            return "Error: No monitored path found with ID " + id + ".";
        }
    }

    @ShellMethod(key = "monitor compare", value = "Compare snapshots of a monitored path")
    public String monitorCompare(@ShellOption int id) {
        if (!daemonAdapter.checkStatus()) return "daemon is not running";
        try {
            MonitoredChangesDTO monitoredChanges = Objects.requireNonNull(daemonAdapter.getMonitoredChanges(id).getBody());
            return "Listing comparison of " + monitoredChanges.getMonitoredPath() + " from "
                    + formatDateTime(monitoredChanges.getPreviousSnapshotCreation())
                    + " to " + formatDateTime(monitoredChanges.getSubsequentSnapshotCreation()) + "...\n"
                    + "Changed files:\n"
                    + (monitoredChanges.getChangedPaths().isEmpty() ? "-" : formatFilePaths(monitoredChanges.getChangedPaths(), monitoredChanges.getMonitoredPath())) + "\n\n"
                    + "Deleted files:\n"
                    + (monitoredChanges.getDeletedPaths().isEmpty() ? "-" : formatFilePaths(monitoredChanges.getDeletedPaths(), monitoredChanges.getMonitoredPath()));
        } catch (RestClientResponseException e) {
            final ErrorResponse errorResponse = Objects.requireNonNull(e.getResponseBodyAs(ErrorResponse.class));
            return "Error: " + errorResponse.getMessage();
        } catch (Exception e) {
            return "Error: could not compare snapshots.";
        }
    }

}
