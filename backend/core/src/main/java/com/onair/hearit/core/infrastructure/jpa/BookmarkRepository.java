package com.onair.hearit.core.infrastructure.jpa;

import com.onair.hearit.core.domain.Bookmark;
import com.onair.hearit.core.domain.Hearit;
import com.onair.hearit.core.domain.Member;
import com.onair.hearit.core.infrastructure.projection.BookmarkWithPlayingHistoryProjection;
import java.util.List;
import java.util.Optional;
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
                AND ph.userUuid = :userUuid
            WHERE b.member.uuid = :userUuid
                AND (:isFinished IS NULL OR
                    (:isFinished = true AND ph.isFinished = :isFinished) OR
                    (:isFinished = false AND (ph.isFinished = false OR ph IS NULL)))
            """)
    Page<BookmarkWithPlayingHistoryProjection> findFilteredByMember(@Param("userUuid") String userUuid,
                                                                    @Param("isFinished") Boolean isFinished,
                                                                    Pageable pageable);

    Optional<Bookmark> findByHearitAndMember(Hearit hearit, Member member);

    List<Bookmark> findAllByHearitInAndMember(List<Hearit> hearits, Member member);

    @Query("""
                SELECT b.hearit.category.id AS categoryId, COUNT(b) AS count
                FROM Bookmark b
                WHERE b.member.id = :memberId
                GROUP BY b.hearit.category.id
            """)
    List<CategoryBookmarkCount> countMemberBookmarksByCategoryId(@Param("memberId") Long memberId);

    boolean existsByHearitAndMember(Hearit hearit, Member member);
}
