package com.onair.hearit.core.infrastructure.jpa;

import com.onair.hearit.core.domain.HearitCluster;
import java.util.List;
import java.util.Set;
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
            SELECT hc.hearitId
            FROM HearitCluster hc
            WHERE (COALESCE(:clusterIds, NULL) IS NULL OR hc.clusterId IN :clusterIds)
            ORDER BY RAND()
            LIMIT :limit
            """)
    List<Long> findRandomHearitIdsByClusters(@Param("clusterIds") List<Integer> clusterIds,
                                             @Param("limit") int limit);

    @Query("""
            SELECT hc.hearitId
            FROM HearitCluster hc
            WHERE (COALESCE(:clusterIds, NULL) IS NULL OR hc.clusterId NOT IN :clusterIds)
            ORDER BY RAND()
            LIMIT :limit
            """)
    List<Long> findRandomHearitIdsExcludingClusters(@Param("clusterIds") List<Integer> clusterIds,
                                                    @Param("limit") int limit);
}
