package ch.bfh.tracesentry.cli.command;

import ch.bfh.tracesentry.cli.adapter.DaemonAdapter;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.io.File;


@ShellComponent
@ShellCommandGroup("Inspect Commands")
public class InspectCommands {

    private final DaemonAdapter daemonAdapter;

    @Autowired
    public InspectCommands(DaemonAdapter daemonAdapter) {
        this.daemonAdapter = daemonAdapter;
    }

    @ShellMethod(key = "inspect", value = "Inspect a file for its intended purpose and a general assessment")
    @SuppressWarnings("unused")
    public String inspect(
            @ShellOption(help = "The path to the file to inspect.")
            @NotBlank
            String path
    ) {
        if (!daemonAdapter.checkStatus()) return "daemon is not running";

        try {
            File fileToInspect = new File(path);
            if (!new File(path).exists()) return "Error: File not found.";
            return daemonAdapter.inspect(fileToInspect.getCanonicalPath());
        } catch (Exception ignored) {
            return "Error: Failed to inspect file. Make sure the file is accessible, the connection to the internet is working and the environment variable OPENAI_API_KEY is set.";
        }
    }

}
