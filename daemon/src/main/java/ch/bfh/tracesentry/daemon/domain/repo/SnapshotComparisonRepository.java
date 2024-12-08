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
                select group_concat(result.snapshot_id),
                       result.path,
                       group_concat(result.comparison)
                from (select sn.snapshot_id, sn.path, sn.comparison from
                            (select s.id from snapshot as s
                                       where s.monitored_path_id = :mp_id
                                       order by s.timestamp desc
                                       limit :limit offset :offset
                            ) as s
                join
                        (select sn.snapshot_id, sn.path, case
                            when sn.has_changed then 'CHANGED'
                            when sn.deleted_in_next_snapshot then 'LAST TRACK'
                            else 'NOTHING' end as comparison
                         from snapshot_node sn
                         where sn.has_changed = true or
                               sn.deleted_in_next_snapshot = true
                        ) as sn
                on sn.snapshot_id = s.id
                order by sn.path, sn.snapshot_id
                ) as result
                group by result.path;
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
