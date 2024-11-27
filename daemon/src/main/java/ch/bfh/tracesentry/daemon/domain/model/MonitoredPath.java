package ch.bfh.tracesentry.daemon.domain.model;

import ch.bfh.tracesentry.lib.model.SearchMode;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.regex.Pattern;

@Entity
@Table(name = "monitored_path")
public class MonitoredPath {

    public MonitoredPath() {
        this.createdAt = LocalDate.now(); // could also be set automatically by jpa auditing or db
    }

    public MonitoredPath(String path, SearchMode mode, @Nullable String pattern, boolean noSubdirs) {
        this.path = path;
        this.mode = mode;
        this.pattern = pattern;
        this.noSubdirs = noSubdirs;
        this.createdAt = LocalDate.now();
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String path;

    private SearchMode mode;

    @Nullable
    private String pattern;

    private boolean noSubdirs;

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

    public void setMode(SearchMode mode) {
        this.mode = mode;
    }

    public SearchMode getMode() {
        return mode;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public Pattern getPattern() {
        try {
            return pattern == null ? null : Pattern.compile(pattern);
        } catch (Exception e) {
            return null;
        }
    }

    public void setNoSubdirs(boolean noSubdirs) {
        this.noSubdirs = noSubdirs;
    }

    public boolean isNoSubdirs() {
        return noSubdirs;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public MonitoredPath path(String path) {
        this.path = path;
        return this;
    }

    public MonitoredPath mode(SearchMode mode) {
        this.mode = mode;
        return this;
    }

    public MonitoredPath pattern(@Nullable String pattern) {
        this.pattern = pattern;
        return this;
    }

    public MonitoredPath noSubdirs(boolean noSubdirs) {
        this.noSubdirs = noSubdirs;
        return this;
    }

    public MonitoredPath createdAt(LocalDate createdAt) {
        this.createdAt = createdAt;
        return this;
    }
}
