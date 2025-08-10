package com.onair.hearit.infrastructure;

import com.onair.hearit.domain.Bookmark;
import com.onair.hearit.domain.Member;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    Page<Bookmark> findAllByMemberOrderByCreatedAtDesc(Member member, Pageable pageable);

    Optional<Bookmark> findByHearitIdAndMemberId(Long hearitId, Long memberId);

    @Query("""
                SELECT b.hearit.category.id AS categoryId, COUNT(b) AS count
                FROM Bookmark b
                WHERE b.member.id = :memberId
                GROUP BY b.hearit.category.id
            """)
    List<CategoryBookmarkCount> countBookmarksByCategoryId(@Param("memberId") Long memberId);

    boolean existsByHearitIdAndMemberId(Long hearitId, Long memberId);
}
