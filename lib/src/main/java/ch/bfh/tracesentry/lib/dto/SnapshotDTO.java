package ch.bfh.tracesentry.lib.dto;


import java.sql.Timestamp;

public class SnapshotDTO {

    private Integer id;
    private Timestamp timestamp;

    public SnapshotDTO() {
    }

    public SnapshotDTO(Integer id, Timestamp timestamp) {
        this.id = id;
        this.timestamp = timestamp;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}
