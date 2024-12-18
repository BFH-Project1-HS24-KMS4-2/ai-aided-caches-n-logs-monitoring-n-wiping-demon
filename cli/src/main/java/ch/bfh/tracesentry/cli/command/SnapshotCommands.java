package ch.bfh.tracesentry.cli.command;

import ch.bfh.tracesentry.cli.adapter.DaemonAdapter;
import ch.bfh.tracesentry.cli.util.Output;
import ch.bfh.tracesentry.lib.dto.MonitoredChangesDTO;
import ch.bfh.tracesentry.lib.exception.ErrorResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.springframework.shell.table.ArrayTableModel;
import org.springframework.shell.table.BorderStyle;
import org.springframework.shell.table.TableBuilder;
import org.springframework.shell.table.TableModel;
import org.springframework.web.client.RestClientResponseException;

import java.util.Objects;
import java.util.function.Supplier;

import static ch.bfh.tracesentry.cli.util.Output.formatDateTime;

@ShellComponent
@ShellCommandGroup("Snapshot Commands")
public class SnapshotCommands {
    private final DaemonAdapter daemonAdapter;

    @Autowired
    public SnapshotCommands(DaemonAdapter daemonAdapter) {
        this.daemonAdapter = daemonAdapter;
    }

    @ShellMethod(key = "snapshots compare", value = "Compare snapshots of a monitored path")
    public String snapshotsCompare(@ShellOption(help = "The id of the monitored path, from where the snapshots get compared") int id,
                                 @ShellOption(help = "The index of the newer snapshot to begin the comparison from", defaultValue = "1") int start,
                                 @ShellOption(help = "The index of the older snapshot to end the comparison on", defaultValue = "2") int end) {
        if (!daemonAdapter.checkStatus()) return "daemon is not running";
        try {
            final MonitoredChangesDTO monitoredChanges = Objects.requireNonNull(daemonAdapter.getMonitoredChanges(id, start, end).getBody());

            String[][] data = new String[monitoredChanges.getComparison().size() + 1][3];
            data[0] = new String[]{"Path", "Snapshot IDs", "Comparison"};
            String[][] model = monitoredChanges.getComparison().stream()
                    .map(sc ->
                            new String[]{
                                    Output.formatFilePath(sc.getPath(), monitoredChanges.getMonitoredPath()),
                                    String.join("\n", sc.getSnapshotIds().stream().map(Object::toString).toList()),
                                    String.join("\n", sc.getComparison())
                            }
                    ).toArray(String[][]::new);
            System.arraycopy(model, 0, data, 1, model.length);
            TableModel tableModel = new ArrayTableModel(data);
            TableBuilder tableBuilder = new TableBuilder(tableModel);
            tableBuilder.addFullBorder(BorderStyle.oldschool);

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

    @ShellMethod(key = "snapshots list", value = "List all snapshots of a monitored path")
    public String snapshotsList(@ShellOption int id) {
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
            data[0] = new String[]{"Number", "Timestamp", "ID"};

            final Supplier<Integer> snapshotCounter = new Supplier<>() {
                private int count = 1;

                @Override
                public Integer get() {
                    return count++;
                }
            };

            String[][] model = body.stream()
                    .map(s -> new String[]{
                            String.valueOf(snapshotCounter.get()),
                            formatDateTime(s.getTimestamp()),
                            String.valueOf(s.getId())
                    }
                    ).toArray(String[][]::new);
            System.arraycopy(model, 0, data, 1, model.length);
            TableModel tableModel = new ArrayTableModel(data);
            TableBuilder tableBuilder = new TableBuilder(tableModel);
            tableBuilder.addFullBorder(BorderStyle.oldschool);
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
