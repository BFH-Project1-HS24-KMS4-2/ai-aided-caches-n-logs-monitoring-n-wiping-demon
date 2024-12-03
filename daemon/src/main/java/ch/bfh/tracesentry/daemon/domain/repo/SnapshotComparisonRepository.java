package ch.bfh.tracesentry.daemon.domain.repo;

import ch.bfh.tracesentry.lib.dto.SnapshotComparisonDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;

@Repository
public class SnapshotComparisonRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public List<SnapshotComparisonDTO> getSnapshotComparisons(Integer monitoredPathId, Integer startIdx, Integer endIdx) {
        final String query = """
                select group_concat(snapshot_id) as snapshot_ids,
                       path,
                       group_concat(comparison) as comparison
                from
                    (select id from snapshot
                               where monitored_path_id = :mp_id
                               order by timestamp
                               limit :limit offset :offset
                    ) as snapshot
                join
                        (select snapshot_id, path, case
                            when has_changed then 'CHANGED'
                            when deleted_in_next_snapshot then 'DELETED'
                            else 'NOTHING' end comparison
                         from snapshot_node
                         where comparison != 'NOTHING'
                        ) as snapshot_node
                on snapshot_node.snapshot_id = snapshot.id
                group by path;
                """;

        List<Object[]> results = entityManager.createNativeQuery(query)
                .setParameter("mp_id", monitoredPathId)
                .setParameter("limit", endIdx - startIdx + 1)
                .setParameter("offset", startIdx)
                .getResultList();

        return results.stream()
                .map(result -> new SnapshotComparisonDTO(
                                Arrays.stream(((String) result[0]).split(",")).map(Integer::valueOf).toList(),
                                (String) result[1],
                                Arrays.asList(((String) result[2]).split(","))
                        )
                ).toList();
    }

}
