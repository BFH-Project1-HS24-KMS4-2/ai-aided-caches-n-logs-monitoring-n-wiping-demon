package ch.bfh.tracesentry.lib.dto;

import java.time.LocalDateTime;
import java.util.List;

public class MonitoredChangesDTO {

    private String monitoredPath;
    private LocalDateTime startSnapshotCreation;
    private LocalDateTime endSnapshotCreation;
    private List<SnapshotComparisonDTO> comparison;

    public MonitoredChangesDTO(String monitoredPath,
                               LocalDateTime startSnapshotCreation,
                               LocalDateTime endSnapshotCreation,
                               List<SnapshotComparisonDTO> comparison) {
        this.monitoredPath = monitoredPath;
        this.startSnapshotCreation = startSnapshotCreation;
        this.endSnapshotCreation = endSnapshotCreation;
        this.comparison = comparison;
    }

    public MonitoredChangesDTO() {
    }

    public String getMonitoredPath() {
        return monitoredPath;
    }

    public void setMonitoredPath(String monitoredPath) {
        this.monitoredPath = monitoredPath;
    }

    public LocalDateTime getStartSnapshotCreation() {
        return startSnapshotCreation;
    }

    public void setStartSnapshotCreation(LocalDateTime startSnapshotCreation) {
        this.startSnapshotCreation = startSnapshotCreation;
    }

    public LocalDateTime getEndSnapshotCreation() {
        return endSnapshotCreation;
    }

    public void setEndSnapshotCreation(LocalDateTime endSnapshotCreation) {
        this.endSnapshotCreation = endSnapshotCreation;
    }

    public List<SnapshotComparisonDTO> getComparison() {
        return comparison;
    }

    public void setComparison(List<SnapshotComparisonDTO> comparison) {
        this.comparison = comparison;
    }

}
