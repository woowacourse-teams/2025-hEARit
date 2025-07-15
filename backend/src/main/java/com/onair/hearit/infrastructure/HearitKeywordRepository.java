package com.onair.hearit.infrastructure;

import com.onair.hearit.domain.HearitKeyword;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HearitKeywordRepository extends JpaRepository<HearitKeyword, Long> {
}
