package ch.bfh.tracesentry.cli.command;

import ch.bfh.tracesentry.cli.adapter.DaemonAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;

@ShellComponent
@ShellCommandGroup("Admin Commands")
public class AdminCommands {

    private final static String TRACE_SENTRY_DIR_ENV = "TRACE_SENTRY_DIR";
    private final static String DELIMITER = System.getProperty("os.name").startsWith("Windows") ? "\\" : "/";
    private final static String DAEMON_JAR = "daemon.jar";
    private final static String INFERRED_DAEMON_LOCATION = System.getenv(TRACE_SENTRY_DIR_ENV) + DELIMITER + DAEMON_JAR;
    private final static int DAEMON_PORT = 8087;

    private final DaemonAdapter daemonAdapter;

    @Autowired
    public AdminCommands(DaemonAdapter daemonAdapter) {
        this.daemonAdapter = daemonAdapter;
    }

    @ShellMethod(key = "status", value = "Check if the daemon is running.")
    @SuppressWarnings("unused")
    public String status() {
        return daemonAdapter.checkStatus() ? "daemon is running" : "daemon is not running";
    }

    @ShellMethod(key = "kill", value = "Kill the daemon.")
    @SuppressWarnings("unused")

    public String kill() {
        if (!daemonAdapter.checkStatus()) return "daemon is not running";
        var killed = daemonAdapter.kill();
        if (killed) {
            return "daemon killed";
        } else {
            return "error killing daemon";
        }
    }

    @ShellMethod(key = "run", value = "Run the daemon.")
    @SuppressWarnings("unused")

    public String run(@ShellOption(defaultValue = "") String path) {
        String jarPath = path;

        if (path.isEmpty()) {
            jarPath = INFERRED_DAEMON_LOCATION;
        }

        var running = daemonAdapter.checkStatus();

        if (isPortInUse() && !running) {
            return "daemon is not running but port is in use";
        }

        if (running) {
            return "daemon is already running";
        }

        File daemonJar = new File(jarPath);
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

    private static boolean isPortInUse() {
        try (ServerSocket serverSocket = new ServerSocket(AdminCommands.DAEMON_PORT)) {
            serverSocket.setReuseAddress(true);
            return false;
        } catch (IOException e) {
            return true;
        }
    }
}
