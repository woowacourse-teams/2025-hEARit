package com.onair.hearit.infrastructure;

import com.onair.hearit.domain.Hearit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HearitRepository extends JpaRepository<Hearit, Long> {
}
