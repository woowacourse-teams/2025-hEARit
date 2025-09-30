package com.onair.hearit.app.category.application;

import com.onair.hearit.app.common.dto.request.PagingRequest;
import com.onair.hearit.app.common.dto.response.PagedResponse;
import com.onair.hearit.app.category.dto.CategoryResponse;
import com.onair.hearit.core.domain.Category;
import com.onair.hearit.core.infrastructure.jpa.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public PagedResponse<CategoryResponse> getCategories(PagingRequest pagingRequest) {
        Pageable pageable = PageRequest.of(pagingRequest.page(), pagingRequest.size());
        Page<Category> categories = categoryRepository.findAll(pageable);
        Page<CategoryResponse> categoryDtos = categories.map(CategoryResponse::from);
        return PagedResponse.from(categoryDtos);
    }
}
