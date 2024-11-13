package ch.bfh.tracesentry.daemon.domain.repo;

import ch.bfh.tracesentry.daemon.domain.model.Node;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NodeRepository extends JpaRepository<Node, Integer> {
}
