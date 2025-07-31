package com.onair.hearit.infrastructure;

import com.onair.hearit.domain.Hearit;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HearitRepository extends JpaRepository<Hearit, Long> {

    @Query("SELECT h FROM Hearit h JOIN FETCH h.category WHERE h.id = :id")
    Optional<Hearit> findWithCategoryById(Long id);

    @Query("SELECT h FROM Hearit h ORDER BY function('RAND')")
    Page<Hearit> findRandom(Pageable pageable);

    @Query("SELECT h FROM Hearit h ORDER BY function('RAND') LIMIT :limit")
    List<Hearit> findRandom(@Param("limit") int limit);

    Page<Hearit> findByCategoryIdOrderByCreatedAtDesc(Long categoryId, Pageable pageable);

    @Query(value = """
            SELECT DISTINCT h.*
            FROM hearit h
            JOIN hearit_keyword hk ON h.id = hk.hearit_id
            JOIN keyword k ON hk.keyword_id = k.id
            WHERE
                LOWER(h.title) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
                OR LOWER(k.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
            ORDER BY h.created_at DESC
            """, nativeQuery = true)
    Page<Hearit> searchByTerm(@Param("searchTerm") String searchTerm, Pageable pageable);

    @Query("""
            SELECT h 
            FROM Hearit h 
            WHERE h.category.id = :categoryId 
            ORDER BY h.createdAt DESC 
            LIMIT :size
            """)
    List<Hearit> findByCategory(@Param("categoryId") Long categoryId, @Param("limit") int size);
}
