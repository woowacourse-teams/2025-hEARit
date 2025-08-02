package com.onair.hearit.application;

import com.onair.hearit.common.exception.custom.NotFoundException;
import com.onair.hearit.domain.Category;
import com.onair.hearit.domain.Hearit;
import com.onair.hearit.dto.request.PagingRequest;
import com.onair.hearit.dto.response.CategoryResponse;
import com.onair.hearit.dto.response.HearitSearchResponse;
import com.onair.hearit.dto.response.PagedResponse;
import com.onair.hearit.infrastructure.CategoryRepository;
import com.onair.hearit.infrastructure.HearitRepository;
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

    public PagedResponse<CategoryResponse> getCategories(PagingRequest pagingRequest) {
        Pageable pageable = PageRequest.of(pagingRequest.page(), pagingRequest.size());
        Page<Category> categories = categoryRepository.findAll(pageable);
        Page<CategoryResponse> categoryDtos = categories.map(CategoryResponse::from);
        return PagedResponse.from(categoryDtos);
    }

    public PagedResponse<HearitSearchResponse> getHearitsByCategory(Long categoryId, PagingRequest pagingRequest) {
        validateCategoryExists(categoryId);
        Pageable pageable = PageRequest.of(pagingRequest.page(), pagingRequest.size());
        Page<Hearit> hearits = hearitRepository.findByCategoryIdOrderByCreatedAtDesc(categoryId, pageable);
        Page<HearitSearchResponse> hearitDtos = hearits.map(HearitSearchResponse::from);
        return PagedResponse.from(hearitDtos);
    }

    private void validateCategoryExists(Long categoryId) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new NotFoundException("카테고리", String.valueOf(categoryId));
        }
    }
}
