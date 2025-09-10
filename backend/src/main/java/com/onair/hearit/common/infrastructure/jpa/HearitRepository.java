package com.onair.hearit.common.infrastructure.jpa;

import com.onair.hearit.common.domain.Hearit;
import com.onair.hearit.common.infrastructure.dto.HearitWithPlayTimeProjection;
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
    List<Hearit> findByCategory(@Param("categoryId") Long categoryId, @Param("size") int size);

    @Query("SELECT h FROM Hearit h JOIN FETCH h.category WHERE h.id IN :hearitIds")
    List<Hearit> findAllByIdInWithCategory(@Param("hearitIds") List<Long> hearitIds);

    @Query("""
            SELECT h
            FROM Hearit h
            JOIN FETCH h.category
            ORDER BY h.createdAt DESC
            """)
    Page<Hearit> findAll(Pageable pageable);

    List<Hearit> findAllByIdIn(List<Long> hearitIds);

    @Query("""
            SELECT h AS hearit, ph.lastPlayTime AS lastPlayTime
            FROM Hearit h
            LEFT JOIN PlayingHistory ph ON h.id = ph.hearitId AND ph.memberId = :memberId
            WHERE h.category.id = :categoryId
            ORDER BY h.createdAt DESC
            """)
    Page<HearitWithPlayTimeProjection> findWithPlayTimeByCategoryId(
            @Param("categoryId") Long categoryId,
            @Param("memberId") Long memberId,
            Pageable pageable
    );
}
