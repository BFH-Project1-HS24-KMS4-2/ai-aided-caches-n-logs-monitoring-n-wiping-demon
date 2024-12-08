package ch.bfh.tracesentry.lib.dto;

import java.time.LocalDateTime;
import java.util.List;

public class MonitoredChangesDTO {

    private String monitoredPath;
    private LocalDateTime previousSnapshotCreation;
    private LocalDateTime subsequentSnapshotCreation;
    private List<String> changedPaths;
    private List<String> deletedPaths;

    public MonitoredChangesDTO(String monitoredPath,
                               LocalDateTime previousSnapshotCreation,
                               LocalDateTime subsequentSnapshotCreation,
                               List<String> changedPaths,
                               List<String> deletedPaths) {
        this.monitoredPath = monitoredPath;
        this.previousSnapshotCreation = previousSnapshotCreation;
        this.subsequentSnapshotCreation = subsequentSnapshotCreation;
        this.changedPaths = changedPaths;
        this.deletedPaths = deletedPaths;
    }

    public MonitoredChangesDTO() {
    }

    public String getMonitoredPath() {
        return monitoredPath;
    }

    public void setMonitoredPath(String monitoredPath) {
        this.monitoredPath = monitoredPath;
    }

    public LocalDateTime getPreviousSnapshotCreation() {
        return previousSnapshotCreation;
    }

    public void setPreviousSnapshotCreation(LocalDateTime previousSnapshotCreation) {
        this.previousSnapshotCreation = previousSnapshotCreation;
    }

    public LocalDateTime getSubsequentSnapshotCreation() {
        return subsequentSnapshotCreation;
    }

    public void setSubsequentSnapshotCreation(LocalDateTime subsequentSnapshotCreation) {
        this.subsequentSnapshotCreation = subsequentSnapshotCreation;
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
