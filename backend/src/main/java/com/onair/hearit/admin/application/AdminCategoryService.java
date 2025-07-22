package com.onair.hearit.admin.application;

import com.onair.hearit.admin.dto.request.CategoryCreateRequest;
import com.onair.hearit.admin.dto.response.CategoryInfoResponse;
import com.onair.hearit.admin.dto.response.PagedResponse;
import com.onair.hearit.domain.Category;
import com.onair.hearit.infrastructure.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminCategoryService {

    private final CategoryRepository categoryRepository;

    public PagedResponse<CategoryInfoResponse> getPageCategories(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Category> pageKeywords = categoryRepository.findAll(pageable);
        Page<CategoryInfoResponse> dtoPage = pageKeywords.map(CategoryInfoResponse::from);
        return PagedResponse.from(dtoPage);
    }

    public CategoryInfoResponse addCategory(CategoryCreateRequest request) {
        Category category = new Category(request.name(), request.colorCode());
        Category saved = categoryRepository.save(category);
        return CategoryInfoResponse.from(saved);
    }
}
