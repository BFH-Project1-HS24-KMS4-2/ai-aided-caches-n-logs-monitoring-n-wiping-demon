package ch.bfh.tracesentry.daemon.domain.model;

import jakarta.persistence.*;

import java.sql.Timestamp;

@Entity
@Table(name = "snapshot")
public class Snapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Timestamp timestamp;

    @ManyToOne
    @JoinColumn(name = "monitored_path_id")
    private MonitoredPath monitoredPath;

    public Snapshot() {

    }

    public Snapshot(Integer id, Timestamp timestamp) {
        this.id = id;
        this.timestamp = timestamp;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setMonitoredPath(MonitoredPath monitoredPath) {
        this.monitoredPath = monitoredPath;
    }

    public MonitoredPath getMonitoredPath() {
        return monitoredPath;
    }
}
