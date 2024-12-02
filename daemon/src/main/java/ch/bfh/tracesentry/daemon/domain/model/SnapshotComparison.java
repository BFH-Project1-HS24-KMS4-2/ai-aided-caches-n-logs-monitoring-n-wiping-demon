package ch.bfh.tracesentry.daemon.domain.model;

import java.util.List;

public class SnapshotComparison {

    private List<Integer> snapshotIds;
    private String path;
    private List<String> comparison;

    public SnapshotComparison(){}

    public SnapshotComparison(List<Integer> snapshotIds, String path, List<String> comparison) {
        this.snapshotIds = snapshotIds;
        this.path = path;
        this.comparison = comparison;
    }

    public List<Integer> getSnapshotIds() {
        return snapshotIds;
    }

    public void setSnapshotIds(List<Integer> snapshotIds) {
        this.snapshotIds = snapshotIds;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<String> getComparison() {
        return comparison;
    }

    public void setComparison(List<String> comparison) {
        this.comparison = comparison;
    }
}
