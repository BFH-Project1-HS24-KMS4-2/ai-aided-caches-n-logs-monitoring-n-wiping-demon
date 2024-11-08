package ch.bfh.tracesentry.daemon.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MonitoredPathRepository extends JpaRepository<MonitoredPath, Integer> {
}
