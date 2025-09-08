package com.onair.hearit.common.infrastructure.jpa;

import com.onair.hearit.common.domain.PlayingHistory;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlayingHistoryRepository extends JpaRepository<PlayingHistory, Long> {

    Optional<PlayingHistory> findByHearitIdAndMemberId(Long hearitId, Long memberId);

    boolean existsByHearitIdAndMemberId(Long hearitId, Long memberId);

    List<PlayingHistory> findByMemberIdAndHearitIdIn(Long memberId, List<Long> hearitIds);
}
