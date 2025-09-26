package com.onair.hearit.infrastructure.jpa;

import com.onair.hearit.domain.PlayingHistory;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PlayingHistoryRepository extends JpaRepository<PlayingHistory, Long> {

    Optional<PlayingHistory> findByHearitIdAndMemberId(Long hearitId, Long memberId);

    @Query("""
            SELECT ph
            FROM PlayingHistory ph
            WHERE ph.memberId = :memberId
            ORDER BY ph.updatedAt DESC
            LIMIT :size
            """)
    List<PlayingHistory> findByMemberIdOrderByUpdatedAtDesc(@Param("memberId") Long memberId, @Param("size") int size);

    List<PlayingHistory> findByMemberIdAndHearitIdIn(Long memberId, List<Long> hearitIds);
}
