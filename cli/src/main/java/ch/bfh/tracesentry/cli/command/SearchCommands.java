package ch.bfh.tracesentry.cli.command;

import ch.bfh.tracesentry.cli.adapter.DaemonAdapter;
import ch.bfh.tracesentry.cli.command.parameters.annotations.ValidPattern;
import ch.bfh.tracesentry.cli.command.parameters.annotations.ValidSearchMode;
import ch.bfh.tracesentry.cli.command.parameters.validators.PatternValidator;
import ch.bfh.tracesentry.cli.util.Output;
import ch.bfh.tracesentry.lib.model.SearchMode;
import ch.bfh.tracesentry.lib.dto.SearchResponseDTO;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.io.File;
import java.util.Objects;
import java.util.regex.Pattern;

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
            @NotBlank
            String path,
            @ShellOption(help = "The search mode to use. Can be: LOG, CACHE, FULL, PATTERN.", defaultValue = "full")
            @ValidSearchMode
            String mode,
            @ShellOption(help = "The pattern to search for. Only used in PATTERN mode.", defaultValue = "")
            @ValidPattern
            String pattern,
            @ShellOption(help = "Do not search in subdirectories.", value = {"--no-subdirs"}, defaultValue = "false")
            boolean noSubdirs
    ) {
        if (!daemonAdapter.checkStatus()) return "daemon is not running";
        try {
            StringBuilder output = new StringBuilder();

            final String canonicalPath = new File(path).getCanonicalPath();
            SearchMode searchMode = SearchMode.valueOf(mode.toUpperCase());

            ResponseEntity<SearchResponseDTO> searchResponse;
            if (PatternValidator.isValidPatternOccurrence(pattern, searchMode)) {
                searchResponse = daemonAdapter.search(canonicalPath, searchMode, noSubdirs, Pattern.compile(pattern));
            } else {
                searchResponse = daemonAdapter.search(canonicalPath, searchMode, noSubdirs);
            }

            var body = Objects.requireNonNull(searchResponse.getBody());
            var parsed = Output.formatFilePaths(body.getFiles(), canonicalPath);

            output.append("Listing ").append(body.getNumberOfFiles()).append(" files in ").append(canonicalPath).append(":\n").append(parsed);
            return output.toString();
        } catch (IllegalArgumentException e) {
            return e.getMessage();
        } catch (Exception ignored) {
            return "An error occurred while searching for files";
        }
    }

}
