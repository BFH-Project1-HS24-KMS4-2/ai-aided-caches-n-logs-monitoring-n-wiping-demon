package ch.bfh.tracesentry.daemon.dto;

import java.util.List;

public record SearchDTO(int numberOfFiles, List<String> files) {
}
