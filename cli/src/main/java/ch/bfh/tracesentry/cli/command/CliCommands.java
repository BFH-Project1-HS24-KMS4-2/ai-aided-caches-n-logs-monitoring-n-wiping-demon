package ch.bfh.tracesentry.cli.command;

import ch.bfh.tracesentry.cli.adapter.DaemonAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.io.IOException;
import java.net.ServerSocket;

@ShellComponent
public class CliCommands {
	private final DaemonAdapter daemonAdapter;

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
	}

    @ShellMethod(key = "run")
	public void run(@ShellOption String path) {
		var running = daemonAdapter.checkStatus();

		if (isPortInUse(8087) && !running) {
			System.err.println("daemon is not running but port is in use");
			return;
		}

		if (running) {
			System.out.println("daemon is already running");
			return;
		}

		ProcessBuilder processBuilder = new ProcessBuilder("java", "-jar", path);

		try {
			Process process = processBuilder.start();
			process.onExit().thenAccept(p -> System.exit(0));
		} catch (Exception e) {
			System.err.println("error starting daemon");
		}
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