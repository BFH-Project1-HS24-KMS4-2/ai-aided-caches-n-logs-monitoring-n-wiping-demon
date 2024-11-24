package ch.bfh.tracesentry.cli.util;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public final class Output {

    private Output() {
    }

    public static String formatFilePaths(List<String> filePaths, String dirPath) {
        return filePaths
                .stream()
                .map(filePath -> Path.of(dirPath).relativize(Paths.get(filePath)).toString())
                .collect(Collectors.joining("\n"));
    }

    public static String formatTimestamp(Timestamp timestamp) {
        return timestamp.toLocalDateTime().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"));
    }

}
