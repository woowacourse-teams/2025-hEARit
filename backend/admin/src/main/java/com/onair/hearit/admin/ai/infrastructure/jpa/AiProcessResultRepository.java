package com.onair.hearit.admin.ai.infrastructure.jpa;

import com.onair.hearit.admin.ai.domain.AiProcessResult;
import com.onair.hearit.admin.ai.domain.ProcessStatus;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AiProcessResultRepository extends JpaRepository<AiProcessResult, Long> {


    List<AiProcessResult> findByStatusNotAndExpiresAtBefore(
            ProcessStatus status, LocalDateTime expiresAt);


    List<AiProcessResult> findByStatusOrderByCreatedAtDesc(ProcessStatus status);


    @Query("SELECT r FROM AiProcessResult r WHERE r.createdAt >= :since ORDER BY r.createdAt DESC")
    List<AiProcessResult> findRecentResults(@Param("since") LocalDateTime since);

    default List<AiProcessResult> findPendingReview() {
        return findByStatusOrderByCreatedAtDesc(ProcessStatus.COMPLETED);
    }
}
