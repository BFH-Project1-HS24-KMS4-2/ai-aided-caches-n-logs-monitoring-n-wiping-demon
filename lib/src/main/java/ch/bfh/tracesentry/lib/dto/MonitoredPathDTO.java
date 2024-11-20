package ch.bfh.tracesentry.lib.dto;

import java.time.LocalDate;

public class MonitoredPathDTO {

    private Integer id;
    private String path;
    private LocalDate createdAt;

    public MonitoredPathDTO() {
    }

    public MonitoredPathDTO(Integer id, String path, LocalDate createdAt) {
        this.id = id;
        this.path = path;
        this.createdAt = createdAt;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }

    public MonitoredPathDTO id(Integer id) {
        this.id = id;
        return this;
    }

    public MonitoredPathDTO createdAt(LocalDate createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public MonitoredPathDTO path(String path) {
        this.path = path;
        return this;
    }
}
