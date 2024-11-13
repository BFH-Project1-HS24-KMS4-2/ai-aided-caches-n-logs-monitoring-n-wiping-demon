package ch.bfh.tracesentry.daemon.domain.service;

import ch.bfh.tracesentry.daemon.domain.model.MonitoredPath;
import ch.bfh.tracesentry.daemon.domain.repo.MonitoredPathRepository;
import ch.bfh.tracesentry.daemon.exception.ConflictException;
import ch.bfh.tracesentry.daemon.exception.NotFoundException;
import ch.bfh.tracesentry.daemon.exception.UnprocessableException;
import ch.bfh.tracesentry.lib.dto.MonitorPathDTO;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;

@Service
public class MonitoringDomainService {
    private static final Logger LOG = LoggerFactory.getLogger(MonitoringDomainService.class);
    private final MonitoredPathRepository monitoredPathRepository;
    private final ModelMapper modelMapper;

    @Autowired
    public MonitoringDomainService(MonitoredPathRepository monitoredPathRepository, ModelMapper modelMapper) {
        this.monitoredPathRepository = monitoredPathRepository;
        this.modelMapper = modelMapper;
    }


    public void createMonitoring(String path) {
        var monitoredPath = new MonitoredPath(path);
        File dirToSearch = new File(path);

        if (!dirToSearch.isDirectory()) {
            LOG.info("Path to search is not a directory or does not exist.");
            throw new UnprocessableException("Path to search is not a directory or does not exist.");
        }
        if (monitoredPathRepository.existsByPath(path)) {
            LOG.info("Path already exists");
            throw new ConflictException("Path already exists");
        }
        try {
            monitoredPathRepository.save(monitoredPath);
        } catch (Exception e) {
            LOG.error("Error while saving monitored path", e);
        }
    }

    public List<MonitorPathDTO> getMonitoredPaths() {
        return monitoredPathRepository
                .findAll()
                .stream()
                .map(path -> modelMapper.map(path, MonitorPathDTO.class))
                .toList();
    }

    public void deleteMonitoring(Integer id) {
        if (!monitoredPathRepository.existsById(id)) {
            throw new NotFoundException("Path does not exist");
        }
        monitoredPathRepository.deleteById(id);
    }
}
