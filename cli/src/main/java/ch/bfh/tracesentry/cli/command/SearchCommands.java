package ch.bfh.tracesentry.cli.command;

import ch.bfh.tracesentry.cli.adapter.DaemonAdapter;
import ch.bfh.tracesentry.cli.command.validation.annotations.ValidSearchMode;
import ch.bfh.tracesentry.lib.dto.SearchResponseDTO;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.shell.command.annotation.ExceptionResolver;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

@ShellComponent
@ShellCommandGroup("Search Commands")
public class SearchCommands {

    private final DaemonAdapter daemonAdapter;

    @Autowired
    public SearchCommands(DaemonAdapter daemonAdapter) {
        this.daemonAdapter = daemonAdapter;
    }

    @ShellMethod(key = "search", value = "Search for files in a given path.")
    @SuppressWarnings("unused")
    public String search(
            @ShellOption @NotBlank String path,
            @ShellOption @ValidSearchMode String mode
    ) {
        if (!daemonAdapter.checkStatus()) return "daemon is not running";
        try {
            Path absolutePath = Paths.get(path).toAbsolutePath();
            ResponseEntity<SearchResponseDTO> searchResponse = daemonAdapter.search(absolutePath);
            var body = Objects.requireNonNull(searchResponse.getBody());
            List<String> foundRelativePaths = parseFoundPaths(body.getFiles(), absolutePath);
            String outputPaths = String.join("\n", foundRelativePaths);
            return "Listing " + body.getNumberOfFiles() + " files in " + absolutePath + ":\n" + outputPaths;
        } catch (IllegalArgumentException e) {
            return e.getMessage();
        } catch (Exception ignored) {
            return "An error occurred while searching for files";
        }
    }

    private List<String> parseFoundPaths(List<String> foundPaths, Path absolutePath) {
        return foundPaths.stream()
                .map(absoluteFilePath -> absolutePath.relativize(Paths.get(absoluteFilePath)).toString())
                .toList();
    }
}
