package ch.bfh.tracesentry.daemon.domain.service;

public interface InspectionService {
    String inspect(String fileContent, String filePath);
}
