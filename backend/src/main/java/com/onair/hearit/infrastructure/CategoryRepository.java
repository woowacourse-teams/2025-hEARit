package com.onair.hearit.infrastructure;

import com.onair.hearit.domain.Category;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    @Query("SELECT c FROM Category c ORDER BY c.id ASC LIMIT :size")
    List<Category> findOldest(@Param("size") int size);
}
