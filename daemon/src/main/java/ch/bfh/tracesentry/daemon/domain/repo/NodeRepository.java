package ch.bfh.tracesentry.daemon.domain.repo;

import ch.bfh.tracesentry.daemon.domain.model.Node;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NodeRepository extends JpaRepository<Node, Integer> {
    List<Node> findAllBySnapshotId(Integer snapshotId);
    List<Node> findAllBySnapshotIdAndHasChangedTrue(Integer snapshotId);
    List<Node> findAllBySnapshotIdAndDeletedInNextSnapshotTrue(Integer snapshotId);
}
