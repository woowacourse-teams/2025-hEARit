package com.onair.hearit.core.infrastructure.jpa;

import com.onair.hearit.core.domain.HearitCluster;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClusteredHearitRepository extends JpaRepository<HearitCluster, Long> {
}
