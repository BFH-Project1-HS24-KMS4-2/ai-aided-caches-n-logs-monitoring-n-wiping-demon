package ch.bfh.tracesentry.daemon.domain;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "monitored_path")
public class MonitoredPath {

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

    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }
}
