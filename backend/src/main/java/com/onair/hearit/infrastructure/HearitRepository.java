package com.onair.hearit.infrastructure;

import com.onair.hearit.domain.Hearit;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface HearitRepository extends JpaRepository<Hearit, Long> {

    @Query("SELECT h FROM Hearit h")
    Page<Hearit> findAllForAdmin(Pageable pageable);

    @Query("SELECT h FROM Hearit h ORDER BY function('RAND')")
    List<Hearit> findRandom(Pageable pageable);
}
