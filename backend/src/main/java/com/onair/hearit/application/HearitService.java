package com.onair.hearit.application;

import com.onair.hearit.common.exception.custom.NotFoundException;
import com.onair.hearit.domain.Bookmark;
import com.onair.hearit.domain.Category;
import com.onair.hearit.domain.Hearit;
import com.onair.hearit.domain.Keyword;
import com.onair.hearit.dto.request.PagingRequest;
import com.onair.hearit.dto.response.HearitDetailResponse;
import com.onair.hearit.dto.response.HearitOfCategoryResponse;
import com.onair.hearit.dto.response.HearitsWithRecommendCategoryResponse;
import com.onair.hearit.dto.response.PagedResponse;
import com.onair.hearit.dto.response.RandomHearitResponse;
import com.onair.hearit.dto.response.RecommendHearitResponse;
import com.onair.hearit.infrastructure.BookmarkRepository;
import com.onair.hearit.infrastructure.CategoryRepository;
import com.onair.hearit.infrastructure.HearitKeywordRepository;
import com.onair.hearit.infrastructure.HearitRepository;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HearitService {

    private static final int RECOMMEND_HEARIT_COUNT = 5;
    private static final int GROUPED_CATEGORY_COUNT = 3;
    private static final int HEARITS_PER_GROUPED_CATEGORY = 5;
    private static final int KEYWORDS_PER_CATEGORIZED_HEARIT = 3;
    private static final int KEYWORDS_PER_HEARIT_FOR_RANDOM = 5;

    private final HearitRepository hearitRepository;
    private final BookmarkRepository bookmarkRepository;
    private final HearitKeywordRepository hearitKeywordRepository;
    private final CategoryRepository categoryRepository;

    public HearitDetailResponse getHearitDetail(Long hearitId, Long memberId) {
        Hearit hearit = getHearitById(hearitId);
        List<Keyword> keywords = hearitKeywordRepository.findKeywordsByHearitId(hearit.getId());
        Optional<Bookmark> bookmarkOptional = bookmarkRepository.findByHearitIdAndMemberId(hearitId, memberId);
        if (bookmarkOptional.isPresent()) {
            return HearitDetailResponse.fromWithBookmark(hearit, bookmarkOptional.get(), keywords);
        }
        return HearitDetailResponse.from(hearit, keywords);
    }

    private Hearit getHearitById(Long hearitId) {
        return hearitRepository.findWithCategoryById(hearitId)
            .orElseThrow(() -> new NotFoundException("hearitId", hearitId.toString()));
    }

    public PagedResponse<RandomHearitResponse> getRandomHearits(Long memberId, PagingRequest pagingRequest) {
        Pageable pageable = PageRequest.of(pagingRequest.page(), pagingRequest.size());
        Page<Hearit> hearits = hearitRepository.findRandom(pageable);
        Page<RandomHearitResponse> hearitDtos = hearits.map(hearit -> toRandomHearitResponse(hearit, memberId));
        return PagedResponse.from(hearitDtos);
    }

    private RandomHearitResponse toRandomHearitResponse(Hearit hearit, Long memberId) {
        List<Keyword> keywords = hearitKeywordRepository.findRecentKeywordsByHearitId(hearit.getId(),
            KEYWORDS_PER_HEARIT_FOR_RANDOM);
        Optional<Bookmark> bookmarkOptional = bookmarkRepository.findByHearitIdAndMemberId(hearit.getId(), memberId);
        if (bookmarkOptional.isPresent()) {
            return RandomHearitResponse.fromWithBookmark(hearit, bookmarkOptional.get(), keywords);
        }
        return RandomHearitResponse.from(hearit, keywords);
    }

    public List<RecommendHearitResponse> getRecommendedHearits() {
        return hearitRepository.findRandom(RECOMMEND_HEARIT_COUNT).stream()
            .map(RecommendHearitResponse::from)
            .toList();
    }

    public List<HearitsWithRecommendCategoryResponse> getHearitsWithRecommendCategory(Long memberId) {
        List<Category> recommendCategories =
            categoryRepository.findTopCategoriesByMemberBookmarks(memberId, GROUPED_CATEGORY_COUNT);
        if (recommendCategories.size() < GROUPED_CATEGORY_COUNT) {
            List<Long> randomCategoryIds = pickTodayRandomCategoryIds(recommendCategories,
                GROUPED_CATEGORY_COUNT - recommendCategories.size());
            List<Category> randomCategories = categoryRepository.findAllByIdIn(randomCategoryIds);
            recommendCategories.addAll(randomCategories);
        }
        return recommendCategories.stream()
            .map(this::toGroupedHearitsResponseByCategory)
            .toList();
    }

    private List<Long> pickTodayRandomCategoryIds(List<Category> recommendCategories, int count) {
        long seed = LocalDate.now().toEpochDay();
        List<Long> categoryIds = getAllCategoryIdsWithoutRecommend(recommendCategories);
        Collections.shuffle(categoryIds, new Random(seed));
        return categoryIds.subList(0, count);
    }

    private List<Long> getAllCategoryIdsWithoutRecommend(List<Category> recommendCategories) {
        List<Long> recommendCategoryIds = recommendCategories.stream().map(Category::getId).toList();
        List<Long> categoryIds = categoryRepository.findAllIds();
        categoryIds.removeAll(recommendCategoryIds);
        return categoryIds;
    }

    private HearitsWithRecommendCategoryResponse toGroupedHearitsResponseByCategory(Category category) {
        List<Hearit> hearits = hearitRepository.findByCategory(category.getId(), HEARITS_PER_GROUPED_CATEGORY);
        return HearitsWithRecommendCategoryResponse.from(category, hearits);
    }

    public PagedResponse<HearitOfCategoryResponse> getHearitsByCategory(Long categoryId, PagingRequest pagingRequest) {
        Pageable pageable = PageRequest.of(pagingRequest.page(), pagingRequest.size());
        Page<Hearit> hearits = hearitRepository.findByCategoryIdOrderByCreatedAtDesc(categoryId, pageable);
        Page<HearitOfCategoryResponse> hearitResponses = hearits.map(this::toHearitOfCategoryResponse);
        return PagedResponse.from(hearitResponses);
    }

    private HearitOfCategoryResponse toHearitOfCategoryResponse(Hearit hearit) {
        List<Keyword> keywords = hearitKeywordRepository.findRecentKeywordsByHearitId(hearit.getId(),
            KEYWORDS_PER_CATEGORIZED_HEARIT);
        return HearitOfCategoryResponse.from(hearit, keywords);
    }
}
