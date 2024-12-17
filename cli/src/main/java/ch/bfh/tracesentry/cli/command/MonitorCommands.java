package ch.bfh.tracesentry.cli.command;

import ch.bfh.tracesentry.cli.adapter.DaemonAdapter;
import ch.bfh.tracesentry.cli.command.parameters.annotations.ValidPattern;
import ch.bfh.tracesentry.cli.command.parameters.annotations.ValidSearchMode;
import ch.bfh.tracesentry.cli.command.parameters.validators.PatternValidator;
import ch.bfh.tracesentry.lib.dto.MonitoredPathDTO;
import ch.bfh.tracesentry.lib.model.SearchMode;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
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

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

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
}
