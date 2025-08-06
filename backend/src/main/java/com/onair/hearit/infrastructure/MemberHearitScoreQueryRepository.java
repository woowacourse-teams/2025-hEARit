package com.onair.hearit.infrastructure;

import com.onair.hearit.domain.Hearit;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemberHearitScoreQueryRepository extends JpaRepository<Hearit, Long> {

    @Query(value = """
            SELECT h.*
            FROM member_explore_score mes
            JOIN hearit h ON mes.hearit_id = h.id
            WHERE mes.member_id = :memberId
              AND mes.cursor_id > :cursorId
            ORDER BY mes.cursor_id ASC
            LIMIT :size
            """, nativeQuery = true)
    List<Hearit> findExploredHearits(@Param("memberId") Long memberId,
                                     @Param("cursorId") Long cursorId,
                                     @Param("size") int size);

    @Query(value = """
            SELECT h.*
            FROM member_explore_score mes
            JOIN hearit h ON mes.hearit_id = h.id
            WHERE mes.member_id IS NULL
              AND mes.cursor_id > :cursorId
            ORDER BY mes.cursor_id ASC
            LIMIT :size
            """, nativeQuery = true)
    List<Hearit> findExploredHearitsForGuest(@Param("cursorId") Long cursorId, @Param("size") int size);
}
