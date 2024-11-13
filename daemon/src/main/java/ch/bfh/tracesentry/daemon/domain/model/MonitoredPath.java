package ch.bfh.tracesentry.daemon.domain.model;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "monitored_path")
public class MonitoredPath {

    public MonitoredPath() {
        this.createdAt = LocalDate.now(); // could also be set automatically by jpa auditing or db
    }

    public MonitoredPath(String path) {
        this.path = path;
        this.createdAt = LocalDate.now();
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String path;

    private LocalDate createdAt;

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public MonitoredPath path(String path) {
        this.path = path;
        return this;
    }

    public MonitoredPath createdAt(LocalDate createdAt) {
        this.createdAt = createdAt;
        return this;
    }
}
