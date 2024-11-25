package ch.bfh.tracesentry.lib.dto;

import ch.bfh.tracesentry.lib.model.SearchMode;

public class CreateMonitorPathDTO {

    private String path;
    private SearchMode mode;
    private String pattern;
    private boolean noSubdirs;

    public CreateMonitorPathDTO() {
    }

    public CreateMonitorPathDTO(String path, SearchMode mode, boolean noSubdirs) {
        this.path = path;
        this.mode = mode;
        this.noSubdirs = noSubdirs;
    }

    public CreateMonitorPathDTO(String path, SearchMode mode, boolean noSubdirs, String pattern) {
        this.path = path;
        this.mode = mode;
        this.noSubdirs = noSubdirs;
        this.pattern = pattern;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CreateMonitorPathDTO that = (CreateMonitorPathDTO) o;
        return noSubdirs == that.noSubdirs && path.equals(that.path) && mode == that.mode && pattern.equals(that.pattern);
    }
}
