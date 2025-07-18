package com.onair.hearit.application;

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
                .map(category -> CategoryResponse.from(category))
                .toList();
    }
}
