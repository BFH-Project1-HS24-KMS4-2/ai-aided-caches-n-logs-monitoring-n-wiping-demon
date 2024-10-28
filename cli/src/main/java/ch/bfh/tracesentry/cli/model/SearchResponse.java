package ch.bfh.tracesentry.cli.model;

import java.util.List;

public record SearchResponse(int numberOfFiles, List<String> files) {
}
