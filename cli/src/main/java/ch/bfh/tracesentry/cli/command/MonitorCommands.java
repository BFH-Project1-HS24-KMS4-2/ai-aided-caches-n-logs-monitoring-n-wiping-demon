package ch.bfh.tracesentry.cli.command;

import ch.bfh.tracesentry.cli.adapter.DaemonAdapter;
import ch.bfh.tracesentry.cli.command.parameters.annotations.ValidPattern;
import ch.bfh.tracesentry.cli.command.parameters.annotations.ValidSearchMode;
import ch.bfh.tracesentry.cli.command.parameters.validators.PatternValidator;
import ch.bfh.tracesentry.lib.dto.MonitoredChangesDTO;
import ch.bfh.tracesentry.lib.dto.MonitoredPathDTO;
import ch.bfh.tracesentry.lib.dto.SnapshotComparisonDTO;
import ch.bfh.tracesentry.lib.exception.ErrorResponse;
import ch.bfh.tracesentry.lib.model.SearchMode;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.springframework.shell.table.ArrayTableModel;
import org.springframework.shell.table.BorderStyle;
import org.springframework.shell.table.TableBuilder;
import org.springframework.shell.table.TableModel;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import static ch.bfh.tracesentry.cli.util.Output.formatDateTime;

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
    public String monitorAdd(
            @ShellOption(help = "The path to monitor for files in. Can be relative or absolute.")
            @NotBlank
            String path,
            @ShellOption(help = "The monitoring mode to use. Can be: LOG, CACHE, FULL, PATTERN.", defaultValue = "full")
            @ValidSearchMode
            String mode,
            @ShellOption(help = "The pattern which the file must match. Only used in PATTERN mode.", defaultValue = "")
            @ValidPattern
            String pattern,
            @ShellOption(help = "Do not monitor  subdirectories.", value = {"--no-subdirs"}, defaultValue = "false")
            boolean noSubdirs

    ) {
        if (!daemonAdapter.checkStatus()) return "daemon is not running";
        var errorMessage = "Error: " + path + " could not be added to the monitoring database.";
        var conflictMessage = "Error: " + path + " is already being monitored.";
        try {
            SearchMode searchMode = SearchMode.valueOf(mode.toUpperCase());

            ResponseEntity<Void> monitorResponse;
            if (PatternValidator.isValidPatternOccurrence(pattern, searchMode)) {
                monitorResponse = daemonAdapter.monitorAdd(path, searchMode, noSubdirs, Pattern.compile(pattern));
            } else {
                monitorResponse = daemonAdapter.monitorAdd(path, searchMode, noSubdirs);
            }

            if (monitorResponse.getStatusCode().is2xxSuccessful()) {
                return "Successfully added " + path + " to the monitoring database.";
            } else if (monitorResponse.getStatusCode().value() == 409) {
                return conflictMessage;
            } else {
                return errorMessage;
            }
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 409) {
                return conflictMessage;
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
            String[][] data = buildTableData(body);
            TableModel model = new ArrayTableModel(data);
            TableBuilder tableBuilder = new TableBuilder(model);
            tableBuilder.addFullBorder(BorderStyle.fancy_light);
            return tableBuilder.build().render(120);
        } catch (Exception e) {
            return "Error: could not list monitored paths.";
        }
    }

    @ShellMethod(key = "monitor remove", value = "Remove a path from the monitoring database.")
    public String monitorRemove(@ShellOption int id) {
        if (!daemonAdapter.checkStatus()) return "daemon is not running";
        try {
            var response = daemonAdapter.monitorRemove(id);
            if (response.getStatusCode().is2xxSuccessful()) {
                return "Successfully removed path with ID " + id + " from the monitoring database.";
            } else {
                throw new RuntimeException();
            }
        } catch (Exception e) {
            return "Error: No monitored path found with ID " + id + ".";
        }
    }

    private String[][] buildTableData(List<MonitoredPathDTO> body) {
        String[][] data = new String[body.size() + 1][6];
        data[0] = new String[]{"ID", "path", "mode", "pattern", "no-subdirs", "created at"};
        String[][] content = body.stream()
                .map(m ->
                        new String[]{
                                String.format("%04d", m.getId()),
                                m.getPath(),
                                m.getMode().toString(),
                                m.getPattern(),
                                String.valueOf(m.isNoSubdirs()),
                                m.getCreatedAt().toString()
                        }
                ).toArray(String[][]::new);
        System.arraycopy(content, 0, data, 1, content.length);
        return data;
    }

    @ShellMethod(key = "monitor compare", value = "Compare snapshots of a monitored path")
    public String monitorCompare(@ShellOption(help = "The id of the monitored path, from where the snapshots get compared") int id,
                                 @ShellOption(help = "The index of the newer snapshot to begin the comparison from", defaultValue = "1") int start,
                                 @ShellOption(help = "The index of the older snapshot to end the comparison on", defaultValue = "2") int end) {
        if (!daemonAdapter.checkStatus()) return "daemon is not running";
        try {
            final MonitoredChangesDTO monitoredChanges = Objects.requireNonNull(daemonAdapter.getMonitoredChanges(id, start, end).getBody());

            int dataTableLines = 0;
            for (SnapshotComparisonDTO snapshotComparison : monitoredChanges.getComparison()) {
                dataTableLines += snapshotComparison.getSnapshotIds().size();
            }

            String[][] data = new String[dataTableLines + 1][3];
            data[0] = new String[]{"Path", "Snapshot IDs", "Comparison"};
            String[][] model = monitoredChanges.getComparison().stream()
                    .map(sc ->
                            new String[]{
                                    sc.getPath(),
                                    String.join("\n", sc.getSnapshotIds().stream().map(Object::toString).toList()),
                                    String.join("\n", sc.getComparison())
                            }
                    ).toArray(String[][]::new);
            System.arraycopy(model, 0, data, 1, model.length);
            TableModel tableModel = new ArrayTableModel(data);
            TableBuilder tableBuilder = new TableBuilder(tableModel);
            tableBuilder.addFullBorder(BorderStyle.fancy_light);

            final String table = tableBuilder.build().render(120);

            return "Listing comparison of " + monitoredChanges.getMonitoredPath() + " from "
                    + formatDateTime(monitoredChanges.getEndSnapshotCreation())
                    + " to " + formatDateTime(monitoredChanges.getStartSnapshotCreation()) + "...\n"
                    + table;
        } catch (RestClientResponseException e) {
            final ErrorResponse errorResponse = Objects.requireNonNull(e.getResponseBodyAs(ErrorResponse.class));
            return "Error: " + errorResponse.getMessage();
        } catch (Exception e) {
            return "Error: could not compare snapshots.";
        }
    }

    @ShellMethod(key = "monitor snapshots", value = "List all snapshots of a monitored path")
    public String monitorSnapshots(@ShellOption int id) {
        if (!daemonAdapter.checkStatus()) {
            return "daemon is not running";
        }
        try {
            var response = daemonAdapter.getSnapshotsOf(id);
            var body = Objects.requireNonNull(response.getBody());
            if (body.isEmpty()) {
                return "No snapshots found for monitored path with ID " + id + ".";
            }
            String[][] data = new String[body.size() + 1][3];
            data[0] = new String[]{"ID", "Timestamp"};
            String[][] model = body.stream()
                    .map(s ->
                            new String[]{
                                    String.format("%04d", s.getId()),
                                    formatDateTime(s.getTimestamp())
                            }
                    ).toArray(String[][]::new);
            System.arraycopy(model, 0, data, 1, model.length);
            TableModel tableModel = new ArrayTableModel(data);
            TableBuilder tableBuilder = new TableBuilder(tableModel);
            tableBuilder.addFullBorder(BorderStyle.fancy_light);
            return tableBuilder.build().render(120);
        } catch (RestClientResponseException e) {
            if (e.getStatusCode() == HttpStatusCode.valueOf(404)) {
                return "No monitored path found with ID " + id + ".";
            } else {
                return "Error: could not list snapshots.";
            }
        } catch (Exception e) {
            return "Error: could not list snapshots.";
        }
    }
}
