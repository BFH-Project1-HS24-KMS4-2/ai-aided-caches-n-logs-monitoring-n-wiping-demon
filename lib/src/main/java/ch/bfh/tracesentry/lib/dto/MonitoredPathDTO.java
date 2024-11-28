package ch.bfh.tracesentry.lib.dto;

import ch.bfh.tracesentry.lib.model.SearchMode;

import java.time.LocalDate;

public class MonitoredPathDTO {

    private Integer id;
    private String path;
    private SearchMode mode;
    private String pattern;
    private boolean noSubdirs;
    private LocalDate createdAt;

    public MonitoredPathDTO() {
    }

    public MonitoredPathDTO(Integer id, String path, SearchMode mode, String pattern, boolean noSubdirs, LocalDate createdAt) {
        this.id = id;
        this.path = path;
        this.mode = mode;
        this.pattern = pattern;
        this.noSubdirs = noSubdirs;
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

    public SearchMode getMode() {
        return mode;
    }

    public void setMode(SearchMode mode) {
        this.mode = mode;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public boolean isNoSubdirs() {
        return noSubdirs;
    }

    public void setNoSubdirs(boolean noSubdirs) {
        this.noSubdirs = noSubdirs;
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
