package com.onair.hearit.core.infrastructure.jpa;

import com.onair.hearit.core.domain.Category;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    @Query("SELECT c.id FROM Category c")
    List<Long> findAllIds();

    @Query("""
        SELECT b.hearit.category AS category, COUNT(b) AS bookmarkCount
        FROM Bookmark b
        WHERE b.memberUuid = :memberUuid
            AND b.hearit.category.id NOT IN :excludedCategoryIds
        GROUP BY b.hearit.category
        ORDER BY bookmarkCount DESC
        LIMIT :size
        """)
    List<Category> findTopCategoriesByMemberBookmarks(
            @Param("memberUuid") UUID memberUuid,
            @Param("size") int size,
            @Param("excludedCategoryIds") List<Long> excludedCategoryIds
    );

    @Query("SELECT c.id FROM Category c WHERE c.id NOT IN :excludedIds")
    List<Long> findIdsWithoutExcludedIds(@Param("excludedIds") List<Long> excludedIds);

    Optional<Category> findByName(String name);
}
