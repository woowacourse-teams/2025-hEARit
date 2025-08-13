package com.onair.hearit.infrastructure;

import com.onair.hearit.domain.Hearit;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ExploredHearitQueryRepository extends JpaRepository<Hearit, Long> {

    @Query(value = """
            SELECT h.*
            FROM explore_score es
            JOIN hearit h ON es.hearit_id = h.id
            WHERE es.member_id = :memberId
              AND es.cursor_id > :cursorId
            ORDER BY es.cursor_id ASC
            LIMIT :size
            """, nativeQuery = true)
    List<Hearit> findExploredHearitsForMember(@Param("memberId") Long memberId,
                                              @Param("cursorId") Long cursorId,
                                              @Param("size") int size);

    @Query(value = """
            SELECT h.*
            FROM explore_score es
            JOIN hearit h ON es.hearit_id = h.id
            WHERE es.member_id = -1
              AND es.cursor_id > :cursorId
            ORDER BY es.cursor_id ASC
            LIMIT :size
            """, nativeQuery = true)
    List<Hearit> findExploredHearitsForGuest(@Param("cursorId") Long cursorId, @Param("size") int size);
}
