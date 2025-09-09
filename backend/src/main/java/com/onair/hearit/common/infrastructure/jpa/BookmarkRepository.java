package com.onair.hearit.common.infrastructure.jpa;

import com.onair.hearit.common.domain.Bookmark;
import com.onair.hearit.common.domain.Hearit;
import com.onair.hearit.common.domain.Member;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    @Query("""
                SELECT b
                FROM Bookmark b
                JOIN FETCH b.hearit
                JOIN FETCH b.hearit.category
                WHERE b.member = :member
                ORDER BY b.createdAt DESC
            """)
    Page<Bookmark> findAllByMemberOrderByRecent(@Param("member") Member member, Pageable pageable);

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
