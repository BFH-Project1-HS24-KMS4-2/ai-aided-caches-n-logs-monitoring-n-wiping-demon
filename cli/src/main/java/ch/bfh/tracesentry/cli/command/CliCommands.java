package ch.bfh.tracesentry.cli.command;

import ch.bfh.tracesentry.cli.adapter.DaemonAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.springframework.web.client.HttpClientErrorException;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

@ShellComponent
public class CliCommands {
    private final DaemonAdapter daemonAdapter;
    private final static int DAEMON_PORT = 8087;

    @Autowired
    public CliCommands(DaemonAdapter daemonAdapter) {
        this.daemonAdapter = daemonAdapter;
    }

    @ShellMethod(key = "status")
    public String status() {
        return daemonAdapter.checkStatus() ? "daemon is running" : "daemon is not running";
    }

    @ShellMethod(key = "kill")
    public String kill() {
        if (!daemonAdapter.checkStatus()) return "daemon is not running";
        var killed = daemonAdapter.kill();
        if (killed) {
            return "daemon killed";
        } else {
            return "error killing daemon";
        }
    }

    @ShellMethod(key = "run")
    public String run(@ShellOption String path) {
        var running = daemonAdapter.checkStatus();

        if (isPortInUse() && !running) {
            return "daemon is not running but port is in use";
        }

        if (running) {
            return "daemon is already running";
        }

        File daemonJar = new File(path);
        if (!daemonJar.exists()) {
            return "File does not exist";
        }

        if (!daemonJar.getName().endsWith(".jar")) {
            return "Not a jar file";
        }

        ProcessBuilder processBuilder = new ProcessBuilder("java", "-jar", daemonJar.getPath());

        try {
            Process process = processBuilder.start();
            return "daemon started with pid: " + process.pid();
        } catch (Exception e) {
            return "error starting daemon";
        }
    }

    @ShellMethod(key = "search")
    public String search(@ShellOption String path) {
        if (!daemonAdapter.checkStatus()) return "daemon is not running";

        try {
            var searchResponse = daemonAdapter.search(path);
            var body = Objects.requireNonNull(searchResponse.getBody());
            List<String> files = body
                    .getFiles()
                    .stream()
                    .map(f -> f.startsWith(path) ? f.replaceFirst(Pattern.quote(path), "") : f)
                    .toList();
            String joined = String.join("\n", files);
            return "Listing " + body.getNumberOfFiles() + " files in " + path + ":\n" + joined;
        } catch (Exception e) {
            return "error searching";
        }
    }

    @ShellMethod(key = "monitor add")
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

    @ShellMethod(key = "monitor list")
    public String monitorList() {
        if (!daemonAdapter.checkStatus()) return "daemon is not running";
        try {
            var response = daemonAdapter.monitorList();
            var body = Objects.requireNonNull(response.getBody());
            if (body.isEmpty()) {
                return "No paths are currently being monitored.";
            }
            return  "ID   | Added      | Path\n" +
                    "-----|------------|------------------------------------------\n" +
                    body.stream()
                            .map(m -> String.format("%04d | %s | %-24s", m.getId(), m.getCreatedAt(), m.getPath()))
                            .reduce("", (a, b) -> a + b + "\n");

        } catch (Exception e) {
            return "Error: could not list monitored paths.";
        }
    }

    private static boolean isPortInUse() {
        try (ServerSocket serverSocket = new ServerSocket(CliCommands.DAEMON_PORT)) {
            serverSocket.setReuseAddress(true);
            return false;
        } catch (IOException e) {
            return true;
        }
    }
}
