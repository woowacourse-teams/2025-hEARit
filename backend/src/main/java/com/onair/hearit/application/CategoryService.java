package com.onair.hearit.application;

import com.onair.hearit.common.exception.custom.NotFoundException;
import com.onair.hearit.domain.Category;
import com.onair.hearit.dto.response.CategoryResponse;
import com.onair.hearit.infrastructure.CategoryRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<CategoryResponse> getCategories() {
        List<Category> categories = categoryRepository.findAll();
        return categories.stream()
                .map(CategoryResponse::from)
                .toList();
    }

    public CategoryResponse getCategory(final Long id) {
        Category category = findCategory(id);
        return CategoryResponse.from(category);
    }

    private Category findCategory(final Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("categoryId", String.valueOf(id)));
    }
}
