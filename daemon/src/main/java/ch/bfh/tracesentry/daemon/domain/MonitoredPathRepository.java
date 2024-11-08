package ch.bfh.tracesentry.daemon.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MonitoredPathRepository extends JpaRepository<MonitoredPath, Integer> {
    boolean existsByPath(String path);
    Optional<MonitoredPath> findByPath(String path);
}
