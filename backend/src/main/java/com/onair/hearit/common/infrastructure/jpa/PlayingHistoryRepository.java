package com.onair.hearit.common.infrastructure.jpa;

import com.onair.hearit.common.domain.PlayingHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlayingHistoryRepository extends JpaRepository<PlayingHistory, Long> {
}
