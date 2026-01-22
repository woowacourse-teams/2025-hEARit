package com.onair.hearit.core.infrastructure.jpa;

import com.onair.hearit.core.domain.PlayingHistory;
import com.onair.hearit.core.infrastructure.projection.CategoryPlayingHistoryCount;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PlayingHistoryRepository extends JpaRepository<PlayingHistory, Long> {

    Optional<PlayingHistory> findByHearitIdAndUserUuid(Long hearitId, UUID userUuid);

    @Query("""
            SELECT ph
            FROM PlayingHistory ph
            WHERE ph.userUuid = :userUuid
            ORDER BY ph.updatedAt DESC
            LIMIT :size
            """)
    List<PlayingHistory> findByUserUuidOrderByUpdatedAtDesc(@Param("userUuid") UUID userUuid,
                                                            @Param("size") int size);

    @Query("""
            SELECT h.category.id AS categoryId, count(ph.id) as count
            FROM PlayingHistory ph
            JOIN Hearit h on ph.hearitId = h.id
            WHERE ph.userUuid = :userUuid
            GROUP BY h.category.id
            """)
    List<CategoryPlayingHistoryCount> countPlayingHistoriesByCategory(@Param("userUuid") String userUuid);

    List<PlayingHistory> findByUserUuidAndHearitIdIn(UUID userUuid, List<Long> hearitIds);
}
