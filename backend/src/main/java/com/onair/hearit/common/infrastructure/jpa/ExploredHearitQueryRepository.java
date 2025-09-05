package com.onair.hearit.common.infrastructure.jpa;

import com.onair.hearit.common.domain.Hearit;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ExploredHearitQueryRepository extends JpaRepository<Hearit, Long> {

    @Query(value = """
            SELECT h.*
            FROM explore_score es
            JOIN hearit h ON es.hearit_id = h.id
            WHERE es.user_uuid = :userUuid
              AND es.cursor_id > :cursorId
            ORDER BY es.cursor_id ASC
            LIMIT :size
            """, nativeQuery = true)
    List<Hearit> findExploredHearits(@Param("userUuid") String userUuid,
                                              @Param("cursorId") Long cursorId,
                                              @Param("size") int size);
}
