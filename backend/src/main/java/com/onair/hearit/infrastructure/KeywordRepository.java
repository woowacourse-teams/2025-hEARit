package com.onair.hearit.infrastructure;

import com.onair.hearit.domain.Keyword;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface KeywordRepository extends JpaRepository<Keyword, Long> {

    @Query("SELECT k FROM Keyword k")
    Page<Keyword> findAllForAdmin(Pageable pageable);
}
