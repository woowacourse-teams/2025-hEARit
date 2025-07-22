package com.onair.hearit.infrastructure;

import com.onair.hearit.domain.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    @Query("SELECT c FROM Category c")
    Page<Category> findAllForAdmin(Pageable pageable);
}
