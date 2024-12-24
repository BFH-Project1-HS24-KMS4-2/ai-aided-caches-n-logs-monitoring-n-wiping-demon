package ch.bfh.tracesentry.daemon.domain.repo;

import ch.bfh.tracesentry.daemon.domain.model.MonitoredPath;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MonitoredPathRepository extends JpaRepository<MonitoredPath, Integer> {
    boolean existsByPath(String path);
    boolean existsById(Integer id);
    Optional<MonitoredPath> findByPath(String path);
}
