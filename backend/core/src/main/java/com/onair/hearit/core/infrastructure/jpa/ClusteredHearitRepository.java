package com.onair.hearit.core.infrastructure.jpa;

import com.onair.hearit.core.domain.HearitCluster;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClusteredHearitRepository extends JpaRepository<HearitCluster, Long> {

    @Query("""
            SELECT hc.clusterId
            FROM PlayingHistory ph JOIN HearitCluster hc ON ph.hearitId = hc.hearitId
            WHERE ph.userUuid = :userUuid
            GROUP BY hc.clusterId
            ORDER BY COUNT(ph.id) DESC
            LIMIT :size
            """)
    List<Integer> findTopClusterIdsByUser(@Param("userUuid") UUID userUuid, @Param("size") int size);

    @Query("""
            SELECT hc.clusterId
            FROM HearitCluster hc
            WHERE hc.clusterId NOT IN :excludedIds
            GROUP BY hc.clusterId
            ORDER BY RAND()
            LIMIT :size
            """)
    List<Integer> findRandomClusterIdsExcluding(@Param("excludedIds") List<Integer> excludedIds,
                                                @Param("size") int size);

    @Query(value = """
            SELECT hearit_id FROM hearit_cluster
            WHERE cluster_id = :clusterId
            ORDER BY RAND()
            LIMIT :limit
            """, nativeQuery = true)
    List<Long> findRandomHearitIdsByCluster(@Param("clusterId") int clusterId, @Param("limit") int limit);
}
