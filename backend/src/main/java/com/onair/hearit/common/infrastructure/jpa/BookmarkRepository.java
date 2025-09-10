package com.onair.hearit.common.infrastructure.jpa;

import com.onair.hearit.common.domain.Bookmark;
import com.onair.hearit.common.domain.Hearit;
import com.onair.hearit.common.domain.Member;
import com.onair.hearit.common.infrastructure.dto.BookmarkWithPlaytimeProjection;
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
                    ph.lastPlayTime AS lastPlayTime
                FROM Bookmark b
                JOIN FETCH b.hearit h
                JOIN FETCH h.category c
                LEFT JOIN PlayingHistory ph
                    ON ph.hearitId = h.id AND ph.memberId = :memberId
                WHERE b.member.id = :memberId
                ORDER BY b.createdAt DESC
            """)
    Page<BookmarkWithPlaytimeProjection> findAllByMemberOrderByRecent(@Param("memberId") Long memberId,
                                                                      Pageable pageable);

    Optional<Bookmark> findByHearitAndMember(Hearit hearit, Member member);

    @Query("""
                SELECT b.hearit.category.id AS categoryId, COUNT(b) AS count
                FROM Bookmark b
                WHERE b.member.id = :memberId
                GROUP BY b.hearit.category.id
            """)
    List<CategoryBookmarkCount> countMemberBookmarksByCategoryId(@Param("memberId") Long memberId);

    boolean existsByHearitAndMember(Hearit hearit, Member member);
}
