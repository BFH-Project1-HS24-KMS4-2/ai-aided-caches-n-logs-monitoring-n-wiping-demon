package ch.bfh.tracesentry.cli.command;

import ch.bfh.tracesentry.cli.adapter.DaemonAdapter;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.io.File;

@ShellComponent
@ShellCommandGroup("Wipe Commands")
public class WipeCommands {

    private final DaemonAdapter daemonAdapter;

    @Autowired
    public WipeCommands(DaemonAdapter daemonAdapter) {
        this.daemonAdapter = daemonAdapter;
    }

    @ShellMethod(key = "wipe", value = "Wipe the content of a file or the file itself.")
    public String wipe(
            @ShellOption(help = "The path to the file to wipe.")
            @NotBlank
            String path,
            @ShellOption(help = "If this Flag is set, the file will not be cleared but deleted.", defaultValue = "false")
            boolean remove
    ) {
        if (!daemonAdapter.checkStatus()) return "daemon is not running";
        String defaultErrorMessage = "Error: Failed to wipe file.";

        try {
            var canonicalPath = new File(path).getCanonicalPath();
            ResponseEntity<Void> response = daemonAdapter.wipe(canonicalPath, remove);
            if (response.getStatusCode().is2xxSuccessful()) {
                return remove ? "Successfully removed file." : "Successfully cleared file.";
            } else if (response.getStatusCode().equals(HttpStatus.UNPROCESSABLE_ENTITY)) {
                return "Error: File could not be processed.";
            } else {
                return defaultErrorMessage;
            }
        } catch (Exception e) {
            return defaultErrorMessage;
        }
    }
}
