package ch.bfh.tracesentry.daemon.domain.repo;

import ch.bfh.tracesentry.daemon.domain.model.Snapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SnapshotRepository extends JpaRepository<Snapshot, Integer> {
    Optional<Snapshot> findFirstByMonitoredPathIdOrderByTimestampDesc(Integer monitoredPathId);
    List<Snapshot> findAllByMonitoredPathIdOrderByTimestampDesc(Integer monitoredPathId);
}
