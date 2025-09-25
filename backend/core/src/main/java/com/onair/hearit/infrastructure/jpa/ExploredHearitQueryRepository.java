package com.onair.hearit.infrastructure.jpa;

import com.onair.hearit.domain.Hearit;
import com.onair.hearit.infrastructure.projection.ExploredHearitProjection;
import java.util.List;
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
    List<ExploredHearitProjection> findExploredHearits(@Param("userUuid") String userUuid,
                                                       @Param("cursorId") Long cursorId,
                                                       Pageable pageable);
}
