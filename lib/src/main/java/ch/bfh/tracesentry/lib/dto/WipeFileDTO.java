package ch.bfh.tracesentry.lib.dto;

public class WipeFileDTO {

    private String path;
    private boolean remove;

    public WipeFileDTO() {
    }

    public WipeFileDTO(String path, boolean remove) {
        this.path = path;
        this.remove = remove;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isRemove() {
        return remove;
    }

    public void setRemove(boolean remove) {
        this.remove = remove;
    }
}
