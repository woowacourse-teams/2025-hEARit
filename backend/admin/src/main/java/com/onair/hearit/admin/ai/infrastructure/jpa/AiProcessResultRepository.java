package com.onair.hearit.admin.ai.infrastructure.jpa;

import com.onair.hearit.admin.ai.domain.AiProcessResult;
import com.onair.hearit.admin.ai.domain.ProcessStatus;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AiProcessResultRepository extends JpaRepository<AiProcessResult, Long> {

    /**
     * 만료된 미확인 결과 조회 (스케줄러용)
     */
    List<AiProcessResult> findByStatusNotAndExpiresAtBefore(
            ProcessStatus status, LocalDateTime expiresAt);

    /**
     * 특정 상태의 결과 목록 조회
     */
    List<AiProcessResult> findByStatusOrderByCreatedAtDesc(ProcessStatus status);

    /**
     * 최근 N일간 처리 결과 조회 (통계용)
     */
    @Query("SELECT r FROM AiProcessResult r WHERE r.createdAt >= :since ORDER BY r.createdAt DESC")
    List<AiProcessResult> findRecentResults(@Param("since") LocalDateTime since);

    /**
     * 검토 대기 중인 결과 목록 조회
     */
    default List<AiProcessResult> findPendingReview() {
        return findByStatusOrderByCreatedAtDesc(ProcessStatus.COMPLETED);
    }
}
