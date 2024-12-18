package ch.bfh.tracesentry.daemon.domain.service;

import ch.bfh.tracesentry.daemon.domain.model.MonitoredPath;
import ch.bfh.tracesentry.daemon.domain.repo.MonitoredPathRepository;
import ch.bfh.tracesentry.daemon.exception.ConflictException;
import ch.bfh.tracesentry.daemon.exception.NotFoundException;
import ch.bfh.tracesentry.daemon.exception.UnprocessableException;
import ch.bfh.tracesentry.lib.dto.MonitoredPathDTO;
import ch.bfh.tracesentry.lib.model.SearchMode;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Service
public class MonitoringDomainService {

    private static final Logger LOG = LoggerFactory.getLogger(MonitoringDomainService.class);

    private final MonitoredPathRepository monitoredPathRepository;
    private final ModelMapper modelMapper;

    @Autowired
    public MonitoringDomainService(MonitoredPathRepository monitoredPathRepository,
                                   ModelMapper modelMapper) {
        this.monitoredPathRepository = monitoredPathRepository;
        this.modelMapper = modelMapper;
    }

    public void createMonitoring(String path, SearchMode mode, String pattern, boolean noSubdirs) throws IOException {
        final File dirToSearch = new File(path);
        final String canonicalPath = dirToSearch.getCanonicalPath();
        var monitoredPath = new MonitoredPath(canonicalPath, mode, pattern, noSubdirs);

        if (!dirToSearch.isDirectory()) {
            LOG.info("Path to search is not a directory or does not exist.");
            throw new UnprocessableException("Path to search is not a directory or does not exist.");
        }
        if (monitoredPathRepository.existsByPath(canonicalPath)) {
            LOG.info("Path already exists");
            throw new ConflictException("Path already exists");
        }
        try {
            monitoredPathRepository.save(monitoredPath);
        } catch (Exception e) {
            LOG.error("Error while saving monitored path", e);
        }
    }

    public List<MonitoredPathDTO> getMonitoredPaths() {
        return monitoredPathRepository
                .findAll()
                .stream()
                .map(monitoredPath -> modelMapper.map(monitoredPath, MonitoredPathDTO.class))
                .toList();
    }

    public void deleteMonitoring(Integer id) {
        if (!monitoredPathRepository.existsById(id)) {
            throw new NotFoundException("MonitoredPath does not exist");
        }
        monitoredPathRepository.deleteById(id);
    }
}
