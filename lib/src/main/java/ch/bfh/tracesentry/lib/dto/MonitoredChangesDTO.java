package ch.bfh.tracesentry.lib.dto;

import java.sql.Timestamp;
import java.util.List;

public class MonitoredChangesDTO {

    private Timestamp previousSnapshot;
    private Timestamp subsequentSnapshot;
    private List<String> changedPaths;
    private List<String> deletedPaths;

    public MonitoredChangesDTO(Timestamp previousSnapshot, Timestamp subsequentSnapshot, List<String> changedPaths, List<String> deletedPaths) {
        this.previousSnapshot = previousSnapshot;
        this.subsequentSnapshot = subsequentSnapshot;
        this.changedPaths = changedPaths;
        this.deletedPaths = deletedPaths;
    }

    public MonitoredChangesDTO() {}

    public Timestamp getPreviousSnapshot() {
        return previousSnapshot;
    }

    public void setPreviousSnapshot(Timestamp previousSnapshot) {
        this.previousSnapshot = previousSnapshot;
    }

    public Timestamp getSubsequentSnapshot() {
        return subsequentSnapshot;
    }

    public void setSubsequentSnapshot(Timestamp subsequentSnapshot) {
        this.subsequentSnapshot = subsequentSnapshot;
    }

    public List<String> getChangedPaths() {
        return changedPaths;
    }

    public void setChangedPaths(List<String> changedPaths) {
        this.changedPaths = changedPaths;
    }

    public List<String> getDeletedPaths() {
        return deletedPaths;
    }

    public void setDeletedPaths(List<String> deletedPaths) {
        this.deletedPaths = deletedPaths;
    }
}
