package com.onair.hearit.infrastructure;

import com.onair.hearit.domain.Category;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findTop3ByOrderByIdAsc();
}
