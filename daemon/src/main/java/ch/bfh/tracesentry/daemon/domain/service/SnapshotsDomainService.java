package ch.bfh.tracesentry.daemon.domain.service;

import ch.bfh.tracesentry.daemon.domain.model.MonitoredPath;
import ch.bfh.tracesentry.daemon.domain.repo.MonitoredPathRepository;
import ch.bfh.tracesentry.daemon.domain.repo.SnapshotComparisonRepository;
import ch.bfh.tracesentry.daemon.domain.repo.SnapshotRepository;
import ch.bfh.tracesentry.daemon.exception.NotFoundException;
import ch.bfh.tracesentry.lib.dto.SnapshotComparisonDTO;
import ch.bfh.tracesentry.lib.dto.SnapshotDTO;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SnapshotsDomainService {

    private final MonitoredPathRepository monitoredPathRepository;
    private final ModelMapper modelMapper;
    private final SnapshotRepository snapshotRepository;
    private final SnapshotComparisonRepository snapshotComparisonRepository;

    @Autowired
    public SnapshotsDomainService(MonitoredPathRepository monitoredPathRepository,
                                  ModelMapper modelMapper,
                                  SnapshotRepository snapshotRepository,
                                  SnapshotComparisonRepository snapshotComparisonRepository) {
        this.monitoredPathRepository = monitoredPathRepository;
        this.modelMapper = modelMapper;
        this.snapshotRepository = snapshotRepository;
        this.snapshotComparisonRepository = snapshotComparisonRepository;
    }

    public List<SnapshotDTO> getSnapshotsOf(Integer monitoredPathId) {
        if (!monitoredPathRepository.existsById(monitoredPathId)) {
            throw new NotFoundException("MonitoredPath does not exist");
        }
        return snapshotRepository.findAllByMonitoredPathIdOrderByTimestampDesc(monitoredPathId)
                .stream()
                .map(snapshot -> modelMapper.map(snapshot, SnapshotDTO.class))
                .toList();
    }

    public List<SnapshotComparisonDTO> getSnapshotComparison(Integer mpId, Integer startIdx, Integer endIdx) {
        return snapshotComparisonRepository.getSnapshotComparisons(mpId, startIdx, endIdx);
    }

    public MonitoredPath getMonitoredPath(Integer id) {
        return monitoredPathRepository.findById(id).orElseThrow(
                () -> new NotFoundException("MonitoredPath does not exist")
        );
    }
}
