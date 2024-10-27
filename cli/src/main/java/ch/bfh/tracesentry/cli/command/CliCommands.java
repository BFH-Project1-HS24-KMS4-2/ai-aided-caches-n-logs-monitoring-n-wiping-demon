package ch.bfh.tracesentry.cli.command;

import ch.bfh.tracesentry.cli.adapter.DaemonAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.io.IOException;
import java.net.ServerSocket;
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
	public void status() {
		var running = daemonAdapter.checkStatus();
		if (running) {
			System.out.println("daemon is running");
		} else {
			System.out.println("daemon is not running");
		}
		System.exit(0); // TODO check if theres a better solution
	}

	@ShellMethod(key = "kill")
	public void kill() {
		var running = daemonAdapter.checkStatus();
		if (!running) {
			System.out.println("daemon is not running");
		} else {
			var killed = daemonAdapter.kill();
			if (killed) {
				System.out.println("daemon killed");
			} else {
				System.err.println("error killing daemon");
			}
		}
		System.exit(0);
	}

    @ShellMethod(key = "run")
	public void run(@ShellOption String path) {
		var running = daemonAdapter.checkStatus();

		if (isPortInUse(DAEMON_PORT) && !running) {
			System.err.println("daemon is not running but port is in use");
			System.exit(0);
			return;
		}

		if (running) {
			System.out.println("daemon is already running");
			System.exit(0);
			return;
		}

		ProcessBuilder processBuilder = new ProcessBuilder("java", "-jar", path);

		try {
			Process process = processBuilder.start();
			process.onExit().thenAccept(p -> System.exit(0));
			System.out.println("daemon started");
		} catch (Exception e) {
			System.err.println("error starting daemon");
		}
		System.exit(0);
	}

	@ShellMethod(key = "search")
	public void search(@ShellOption String path) {
		var running = daemonAdapter.checkStatus();
		if (!running) {
			System.err.println("daemon is not running");
			System.exit(0);
			return;
		}

		var searchResponse = daemonAdapter.search(path);
		if (searchResponse == null) {
			System.err.println("error searching");
		} else {
			System.out.println("Listing " + searchResponse.numberOfFiles() + " files in " + path);
			searchResponse.files().forEach(f -> System.out.println(f.startsWith(path) ? f.replaceFirst(Pattern.quote(path), "") : f));
		}
		System.exit(0);
	}

	public static boolean isPortInUse(int port) {
		try (ServerSocket serverSocket = new ServerSocket(port)) {
			serverSocket.setReuseAddress(true);
			return false;
		} catch (IOException e) {
			return true;
		}
	}
}