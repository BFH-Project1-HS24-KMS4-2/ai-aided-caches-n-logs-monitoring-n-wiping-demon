package ch.bfh.tracesentry.cli.command;

import ch.bfh.tracesentry.cli.adapter.DaemonAdapter;
import ch.bfh.tracesentry.cli.command.parameters.annotations.ValidSearchMode;
import ch.bfh.tracesentry.cli.command.parameters.model.SearchMode;
import ch.bfh.tracesentry.lib.dto.SearchResponseDTO;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.nio.file.Path;
import java.nio.file.Paths;
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
            @ShellOption(help = "The path to search for files in. Can be relative or absolute.")
            @NotBlank String path,
            @ShellOption(help = "The search mode to use. Can be: LOG, CACHE, FULL, PATTERN.", defaultValue = "full")
            @ValidSearchMode String mode
    ) {
        if (!daemonAdapter.checkStatus()) return "daemon is not running";
        try {
            StringBuilder output = new StringBuilder();
            SearchMode searchMode = SearchMode.valueOf(mode.toUpperCase());
            Path absolutePath = Paths.get(path).toAbsolutePath();

            var searchResponse = daemonAdapter.search(absolutePath);
            var body = Objects.requireNonNull(searchResponse.getBody());
            var parsed = parseFoundPaths(body, absolutePath);

            output.append("Listing ").append(body.getNumberOfFiles()).append(" files in ").append(absolutePath).append(":\n").append(parsed);

            return output.toString();
        } catch (IllegalArgumentException e) {
            return e.getMessage();
        } catch (Exception ignored) {
            return "An error occurred while searching for files";
        }
    }

    private String parseFoundPaths(SearchResponseDTO searchResponse, Path absolutePath) throws NullPointerException {
        var relativizedPaths = searchResponse.
                getFiles().stream().
                map(absoluteFilePath -> absolutePath.relativize(Paths.get(absoluteFilePath)).toString()).toList();
        return String.join("\n", relativizedPaths);
    }
}
