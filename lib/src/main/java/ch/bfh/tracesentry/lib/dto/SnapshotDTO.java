package ch.bfh.tracesentry.lib.dto;

import java.time.LocalDateTime;

public class SnapshotDTO {

    private Integer id;
    private LocalDateTime timestamp;

    public SnapshotDTO() {
    }

    public SnapshotDTO(Integer id, LocalDateTime timestamp) {
        this.id = id;
        this.timestamp = timestamp;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
