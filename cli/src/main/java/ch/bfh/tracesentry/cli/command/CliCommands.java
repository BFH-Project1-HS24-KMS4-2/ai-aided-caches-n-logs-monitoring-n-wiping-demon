package ch.bfh.tracesentry.cli.command;

import ch.bfh.tracesentry.cli.adapter.DaemonAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

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
                    .files()
                    .stream()
                    .map(f -> f.startsWith(path) ? f.replaceFirst(Pattern.quote(path), "") : f)
                    .toList();
            String joined = String.join("\n", files);
            return "Listing " + body.numberOfFiles() + " files in " + path + ":\n" + joined;
        } catch (Exception e) {
            return "error searching";
        }
    }

    @ShellMethod(key = "monitor add")
    public String monitorAdd(@ShellOption String path) {
        if (!daemonAdapter.checkStatus()) return "daemon is not running";

        try {
            var monitorResponse = daemonAdapter.monitorAdd(path);
            return "";//monitorResponse.message();
        } catch (Exception e) {
            return "error adding monitor";
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
