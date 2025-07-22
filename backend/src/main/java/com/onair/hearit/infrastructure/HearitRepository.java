package com.onair.hearit.infrastructure;

import com.onair.hearit.domain.Hearit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HearitRepository extends JpaRepository<Hearit, Long> {

    @Query("SELECT h FROM Hearit h ORDER BY function('RAND')")
    Page<Hearit> findRandom(Pageable pageable);

    @Query("""
            SELECT h
            FROM Hearit h
            WHERE LOWER(h.title) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
            ORDER BY h.createdAt DESC
            """)
    Page<Hearit> findLatestByTitle(@Param("searchTerm") String searchTerm, Pageable pageable);

    Page<Hearit> findByCategoryIdOrderByCreatedAtDesc(Long categoryId, Pageable pageable);

    @Query("""
            SELECT h
            FROM Hearit h
            JOIN HearitKeyword hk ON hk.hearit = h
            WHERE hk.keyword.id = :keywordId
            ORDER BY h.createdAt DESC
            """)
    Page<Hearit> findLatestByKeywordId(@Param("keywordId") Long keywordId, Pageable pageable);
}
