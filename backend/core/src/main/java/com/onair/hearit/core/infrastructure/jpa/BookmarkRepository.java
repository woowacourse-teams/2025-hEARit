package com.onair.hearit.core.infrastructure.jpa;

import com.onair.hearit.core.domain.Bookmark;
import com.onair.hearit.core.domain.Hearit;
import com.onair.hearit.core.infrastructure.projection.BookmarkWithPlayingHistoryProjection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    @Query("""
            SELECT
                b AS bookmark,
                ph AS playingHistory
            FROM Bookmark b
            JOIN FETCH b.hearit h
            JOIN FETCH h.category c
            LEFT JOIN PlayingHistory ph
                ON ph.hearitId = h.id
                AND ph.userUuid = :memberUuid
            WHERE b.memberUuid = :memberUuid
                AND (:isFinished IS NULL OR
                    (:isFinished = true AND ph.isFinished = :isFinished) OR
                    (:isFinished = false AND (ph.isFinished = false OR ph IS NULL)))
            """)
    Page<BookmarkWithPlayingHistoryProjection> findFilteredByMember(@Param("memberUuid") UUID memberUuid,
                                                                    @Param("isFinished") Boolean isFinished,
                                                                    Pageable pageable);

    Optional<Bookmark> findByHearitAndMemberUuid(Hearit hearit, UUID memberUuid);

    List<Bookmark> findAllByHearitInAndMemberUuid(List<Hearit> hearits, UUID memberUuid);

    @Query("""
                SELECT b.hearit.category.id AS categoryId, COUNT(b) AS count
                FROM Bookmark b
                WHERE b.memberUuid = :memberUuid
                GROUP BY b.hearit.category.id
            """)
    List<CategoryBookmarkCount> countMemberBookmarksByCategoryId(@Param("memberUuid") UUID memberUuid);

    boolean existsByHearitAndMemberUuid(Hearit hearit, UUID memberUuid);
}
