package ch.bfh.tracesentry.lib.dto;

import java.time.LocalDate;

public record MonitorPathDTO(Integer id, String path, LocalDate createdAt) {
}
