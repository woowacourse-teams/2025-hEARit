package com.onair.hearit.application;

import com.onair.hearit.domain.Category;
import com.onair.hearit.domain.Hearit;
import com.onair.hearit.dto.request.CategoryListCondition;
import com.onair.hearit.dto.request.CategorySearchCondition;
import com.onair.hearit.dto.response.CategoryResponse;
import com.onair.hearit.dto.response.HearitSearchResponse;
import com.onair.hearit.infrastructure.CategoryRepository;
import com.onair.hearit.infrastructure.HearitRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final HearitRepository hearitRepository;

    public List<CategoryResponse> getCategories(CategoryListCondition condition) {
        Pageable pageable = PageRequest.of(condition.page(), condition.size());
        Page<Category> categories = categoryRepository.findAll(pageable);
        return categories.stream()
                .map(CategoryResponse::from)
                .toList();
    }

    public List<HearitSearchResponse> findHearitsByCategory(CategorySearchCondition condition) {
        Pageable pageable = PageRequest.of(condition.page(), condition.size());
        Page<Hearit> result = hearitRepository.findByCategoryIdOrderByCreatedAtDesc(condition.categoryId(), pageable);
        return result.stream()
                .map(HearitSearchResponse::from)
                .toList();
    }
}
