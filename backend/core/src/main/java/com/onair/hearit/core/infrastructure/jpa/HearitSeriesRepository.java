package com.onair.hearit.core.infrastructure.jpa;

import com.onair.hearit.core.domain.Hearit;
import com.onair.hearit.core.domain.HearitSeries;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HearitSeriesRepository extends JpaRepository<HearitSeries, Long> {

    @Query("SELECT hs.hearit FROM HearitSeries hs JOIN FETCH hs.hearit.category WHERE hs.series.id = :seriesId ORDER BY hs.hearit.createdAt DESC")
    List<Hearit> findHearitsBySeriesIdOrderByCreatedAtDesc(@Param("seriesId") Long seriesId);

    boolean existsByHearitIdAndSeriesId(Long hearitId, Long seriesId);

    void deleteByHearitIdAndSeriesId(Long hearitId, Long seriesId);
}
