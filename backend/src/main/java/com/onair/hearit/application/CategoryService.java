package com.onair.hearit.application;

import com.onair.hearit.domain.Category;
import com.onair.hearit.domain.Hearit;
import com.onair.hearit.domain.Keyword;
import com.onair.hearit.dto.request.PagingRequest;
import com.onair.hearit.dto.response.CategoryResponse;
import com.onair.hearit.dto.response.HearitOfCategoryResponse;
import com.onair.hearit.dto.response.PagedResponse;
import com.onair.hearit.infrastructure.CategoryRepository;
import com.onair.hearit.infrastructure.HearitKeywordRepository;
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

    private static final int KEYWORDS_PER_HEARIT = 3;

    private final CategoryRepository categoryRepository;
    private final HearitRepository hearitRepository;
    private final HearitKeywordRepository hearitKeywordRepository;

    public PagedResponse<CategoryResponse> getCategories(PagingRequest pagingRequest) {
        Pageable pageable = PageRequest.of(pagingRequest.page(), pagingRequest.size());
        Page<Category> categories = categoryRepository.findAll(pageable);
        Page<CategoryResponse> categoryDtos = categories.map(CategoryResponse::from);
        return PagedResponse.from(categoryDtos);
    }

    public PagedResponse<HearitOfCategoryResponse> getHearitsByCategory(Long categoryId,
                                                                        PagingRequest pagingRequest) {
        Pageable pageable = PageRequest.of(pagingRequest.page(), pagingRequest.size());
        Page<Hearit> hearits = hearitRepository.findByCategoryIdOrderByCreatedAtDesc(categoryId, pageable);
        Page<HearitOfCategoryResponse> hearitResponses = hearits.map(this::toCategoryHearitResponse);
        return PagedResponse.from(hearitResponses);
    }

    private HearitOfCategoryResponse toCategoryHearitResponse(Hearit hearit) {
        List<Keyword> keywords = hearitKeywordRepository.findKeywordsByHearitId(hearit.getId(), KEYWORDS_PER_HEARIT);
        return HearitOfCategoryResponse.from(hearit, keywords);
    }
}
