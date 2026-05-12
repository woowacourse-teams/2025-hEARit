package com.onair.hearit.core.infrastructure.jpa;

import com.onair.hearit.core.domain.Series;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeriesRepository extends JpaRepository<Series, Long> {
}
