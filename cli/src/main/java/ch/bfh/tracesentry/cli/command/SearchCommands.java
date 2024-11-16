package ch.bfh.tracesentry.cli.command;

import ch.bfh.tracesentry.cli.adapter.DaemonAdapter;
import ch.bfh.tracesentry.lib.dto.SearchResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
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
    public String search(@ShellOption String path) {
        if (!daemonAdapter.checkStatus()) return "daemon is not running";

        try {
            Path absolutePath = Paths.get(path).toAbsolutePath();
            ResponseEntity<SearchResponseDTO> searchResponse = daemonAdapter.search(absolutePath);
            var body = Objects.requireNonNull(searchResponse.getBody());
            List<String> foundRelativePaths = body
                    .getFiles()
                    .stream()
                    .map(absoluteFilePath -> absoluteFilePath.replaceFirst(Pattern.quote(absolutePath + FileSystems.getDefault().getSeparator()), ""))
                    .toList();
            String outputPaths = String.join("\n", foundRelativePaths);
            return "Listing " + body.getNumberOfFiles() + " files in " + absolutePath + ":\n" + outputPaths;
        } catch (Exception e) {
            return "error searching";
        }
    }
}
