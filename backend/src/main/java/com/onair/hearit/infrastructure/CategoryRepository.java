package com.onair.hearit.infrastructure;

import com.onair.hearit.domain.Category;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    @Query("SELECT c.id FROM Category c")
    List<Long> findAllIds();

    List<Category> findAllByIdIn(List<Long> selectedIds);

    @Query("""
        SELECT b.hearit.category AS category, COUNT(b) AS bookmarkCount
        FROM Bookmark b
        WHERE b.member.id = :memberId
        GROUP BY b.hearit.category
        ORDER BY bookmarkCount DESC
        LIMIT :size""")
    List<Category> findTopCategoriesByMemberBookmarks(@Param("memberId") Long memberId, @Param("size") int size);
}
