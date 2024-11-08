package ch.bfh.tracesentry.lib.dto;

import java.time.LocalDate;

public class MonitorPathDTO {

    private Integer id;
    private String path;
    private LocalDate createdAt;

    public MonitorPathDTO() {
    }

    public MonitorPathDTO(Integer id, String path, LocalDate createdAt) {
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

    public MonitorPathDTO id(Integer id) {
        this.id = id;
        return this;
    }

    public MonitorPathDTO createdAt(LocalDate createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public MonitorPathDTO path(String path) {
        this.path = path;
        return this;
    }
}
