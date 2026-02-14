package com.onair.hearit.core.infrastructure.jpa;

import com.onair.hearit.core.domain.Hearit;
import com.onair.hearit.core.infrastructure.projection.ExploredHearitProjection;
import com.onair.hearit.core.infrastructure.projection.ExploredHearitScoreProjection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ExploredHearitQueryRepository extends JpaRepository<Hearit, Long> {

    @Query("""
            SELECT
                h AS hearit,
                es.cursorId AS cursorId
            FROM ExploreScore es
            JOIN Hearit h ON es.hearitId = h.id
            WHERE es.userUuid = :userUuid
              AND es.cursorId > :cursorId
            ORDER BY es.cursorId ASC
            """)
    List<ExploredHearitProjection> findExploredHearits(@Param("userUuid") UUID userUuid,
                                                       @Param("cursorId") Long cursorId,
                                                       Pageable pageable);

    @Query("""
            SELECT
                h AS hearit,
                es.score AS score
            FROM ExploreScore es
            JOIN Hearit h ON es.hearitId = h.id
            WHERE es.userUuid = :userUuid
            ORDER BY es.score DESC, h.id DESC
            """)
    List<ExploredHearitScoreProjection> findExploredHearitsByScore(@Param("userUuid") UUID userUuid,
                                                                    Pageable pageable);

    @Query("""
            SELECT
                h AS hearit,
                es.score AS score
            FROM ExploreScore es
            JOIN Hearit h ON es.hearitId = h.id
            WHERE es.userUuid = :userUuid
              AND (es.score < :score OR (es.score = :score AND h.id < :hearitId))
            ORDER BY es.score DESC, h.id DESC
            """)
    List<ExploredHearitScoreProjection> findExploredHearitsAfterScoreCursor(
            @Param("userUuid") UUID userUuid,
            @Param("score") double score,
            @Param("hearitId") long hearitId,
            Pageable pageable);
}
