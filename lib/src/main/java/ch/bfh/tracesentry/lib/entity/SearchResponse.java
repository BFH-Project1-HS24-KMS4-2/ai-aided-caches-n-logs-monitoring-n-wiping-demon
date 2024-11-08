package ch.bfh.tracesentry.lib.entity;

import java.util.List;

public record SearchResponse(int numberOfFiles, List<String> files) {
}
