package ch.bfh.tracesentry.lib.dto;

import java.util.List;

public record SearchResponseDTO(int numberOfFiles, List<String> files) {
}
