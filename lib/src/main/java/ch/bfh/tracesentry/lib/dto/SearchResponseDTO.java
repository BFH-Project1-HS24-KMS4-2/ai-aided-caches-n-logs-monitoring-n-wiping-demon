package ch.bfh.tracesentry.lib.dto;

import java.util.List;

public class SearchResponseDTO {
    private int numberOfFiles;
    private List<String> files;

    public SearchResponseDTO() {
    }

    public SearchResponseDTO(int numberOfFiles, List<String> files) {
        this.numberOfFiles = numberOfFiles;
        this.files = files;
    }

    public int getNumberOfFiles() {
        return numberOfFiles;
    }

    public void setNumberOfFiles(int numberOfFiles) {
        this.numberOfFiles = numberOfFiles;
    }

    public List<String> getFiles() {
        return files;
    }

    public void setFiles(List<String> files) {
        this.files = files;
    }
}
