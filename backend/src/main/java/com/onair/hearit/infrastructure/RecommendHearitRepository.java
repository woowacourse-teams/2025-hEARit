package com.onair.hearit.infrastructure;

import com.onair.hearit.domain.RecommendHearit;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RecommendHearitRepository extends JpaRepository<RecommendHearit, Long> {

    @Query("SELECT rh FROM RecommendHearit rh WHERE rh.recommendDate <= :recommendDate ORDER BY rh.recommendDate DESC LIMIT :size")
    List<RecommendHearit> findByRecentRecommendDateLimitN(@Param("recommendDate") LocalDate recommendDate,
                                                          @Param("size") int size);
}
