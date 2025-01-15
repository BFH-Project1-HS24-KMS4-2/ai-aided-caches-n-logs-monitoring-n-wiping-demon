package ch.bfh.tracesentry.cli.util;

import java.util.List;

public class ShellLines {

    /**
     * Joins all lines of a shell output and trims them.
     * This is needed because the shell test client has a internal line length limit,
     * which cannot be changed in the current version of the shell test client.
     * @param shellLines List returned by ShellTestClient.screen().lines()
     * @return joined String of all trimmed lines
     */
    public static String join(List<String> shellLines) {
        var lines = shellLines.stream().map(s -> {
            int lengthBefore = s.length();
            String trimmed = s.trim();
            if (lengthBefore != trimmed.length()) {
                trimmed += "\n";
            }
            return trimmed;
        }).toList();

        return String.join("", lines);
    }
}
