package com.onair.hearit.core.infrastructure.jpa;

import com.onair.hearit.core.domain.Hearit;
import com.onair.hearit.core.infrastructure.projection.HearitClusterStatisticsProjection;
import com.onair.hearit.core.infrastructure.projection.HearitWithPlayTimeProjection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HearitRepository extends JpaRepository<Hearit, Long> {

    @Query("""
                SELECT DISTINCT h
                FROM Hearit h
                LEFT JOIN FETCH h.category c
                LEFT JOIN FETCH h.sources s
                WHERE h.id = :id
            """)
    Optional<Hearit> findByIdWithCategoryAndSources(@Param("id") Long id);

    @Query("SELECT h FROM Hearit h JOIN FETCH h.category WHERE h.id = :id")
    Optional<Hearit> findWithCategoryById(Long id);

    @Query(
            value = """
                    SELECT h.* FROM (
                        SELECT * FROM hearit
                        WHERE MATCH(title) AGAINST(:searchTerm IN BOOLEAN MODE)
                        UNION
                        SELECT h.* FROM hearit h
                        JOIN hearit_keyword hk ON h.id = hk.hearit_id
                        JOIN keyword k ON hk.keyword_id = k.id
                        WHERE MATCH(k.name) AGAINST(:searchTerm IN BOOLEAN MODE)
                    ) h
                    ORDER BY h.created_at DESC
                    """,
            countQuery = """
                    SELECT COUNT(*) FROM (
                        SELECT h.id FROM hearit h
                        WHERE MATCH(h.title) AGAINST(:searchTerm IN BOOLEAN MODE)
                        UNION
                        SELECT h.id FROM hearit h
                        JOIN hearit_keyword hk ON h.id = hk.hearit_id
                        JOIN keyword k ON hk.keyword_id = k.id
                        WHERE MATCH(k.name) AGAINST(:searchTerm IN BOOLEAN MODE)
                    ) AS total_count
                    """,
            nativeQuery = true
    )
    Page<Hearit> searchByTerm(@Param("searchTerm") String searchTerm, Pageable pageable);

    @Query("""
            SELECT h
            FROM Hearit h
            WHERE h.category.id = :categoryId
            ORDER BY h.createdAt DESC
            LIMIT :size
            """)
    List<Hearit> findByCategory(@Param("categoryId") Long categoryId, @Param("size") int size);

    @Query("""
            SELECT h
            FROM Hearit h
            JOIN FETCH h.category
            ORDER BY h.createdAt DESC
            """)
    Page<Hearit> findAll(Pageable pageable);

    @Query("SELECT h FROM Hearit h JOIN FETCH h.category WHERE h.id IN :hearitIds")
    List<Hearit> findAllByIdIn(List<Long> hearitIds);

    @Query("""
            SELECT h AS hearit, ph.lastPlayTime AS lastPlayTime
            FROM Hearit h
            LEFT JOIN PlayingHistory ph ON h.id = ph.hearitId AND ph.userUuid = :userUuid
            JOIN FETCH h.category
            WHERE (:categoryId IS NULL OR h.category.id = :categoryId)
            """)
    Page<HearitWithPlayTimeProjection> findWithPlayTimeBy(
            @Param("categoryId") Long categoryId,
            @Param("userUuid") UUID userUuid,
            Pageable pageable
    );

    @Query("""
            SELECT
                h.id AS hearitId,
                h.viewCount AS viewCount,
                COUNT(DISTINCT r.id) AS likeCount,
                COUNT(DISTINCT b.id) AS bookmarkCount,
                COALESCE(ph_stats.avgTime, 0) AS avgPlayTime,
                COALESCE(ph_stats.compRate, 0) AS completionRate,
                h.createdAt AS createdAt
            FROM Hearit h
            LEFT JOIN Reaction r ON r.hearit = h AND r.type = 'LIKE'
            LEFT JOIN Bookmark b ON b.hearit = h
            LEFT JOIN (
                SELECT
                    ph.hearitId AS hId,
                    AVG(ph.lastPlayTime) AS avgTime,
                    AVG(CASE WHEN ph.isFinished = true THEN 1.0 ELSE 0.0 END) AS compRate
                FROM PlayingHistory ph
                GROUP BY ph.hearitId
            ) ph_stats ON ph_stats.hId = h.id
            GROUP BY h.id, h.viewCount, h.createdAt, ph_stats.avgTime, ph_stats.compRate
            ORDER BY h.id""")
    Page<HearitClusterStatisticsProjection> findClusterStatistics(Pageable pageable);

    @Query("""
            SELECT h.id
            FROM Hearit h
            WHERE (COALESCE(:ids, NULL) IS NULL OR h.id NOT IN :ids)
            ORDER BY RAND()
            LIMIT :limit
            """)
    List<Long> findRandomIdsExcludingIds(@Param("ids") Set<Long> ids,
                                         @Param("limit") int limit);

    @Modifying
    @Query(value = """
                UPDATE hearit
                SET view_count = view_count + 1
                WHERE id = :hearitId
            """, nativeQuery = true)
    int increaseViewCount(@Param("hearitId") Long hearitId);
}
