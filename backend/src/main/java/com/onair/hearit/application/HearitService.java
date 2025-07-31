package com.onair.hearit.application;

import com.onair.hearit.common.exception.custom.NotFoundException;
import com.onair.hearit.domain.Bookmark;
import com.onair.hearit.domain.Category;
import com.onair.hearit.domain.Hearit;
import com.onair.hearit.domain.Keyword;
import com.onair.hearit.dto.request.PagingRequest;
import com.onair.hearit.dto.response.GroupedHearitsWithCategoryResponse;
import com.onair.hearit.dto.response.HearitDetailResponse;
import com.onair.hearit.dto.response.PagedResponse;
import com.onair.hearit.dto.response.RandomHearitResponse;
import com.onair.hearit.dto.response.RecommendHearitResponse;
import com.onair.hearit.infrastructure.BookmarkRepository;
import com.onair.hearit.infrastructure.CategoryRepository;
import com.onair.hearit.infrastructure.HearitKeywordRepository;
import com.onair.hearit.infrastructure.HearitRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HearitService {

    private static final int RECOMMEND_HEARIT_COUNT = 5;
    private static final int CATEGORY_COUNT = 3;
    private static final int HEARITS_PER_CATEGORY = 5;

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
        Optional<Bookmark> bookmarkOptional = bookmarkRepository.findByHearitIdAndMemberId(hearit.getId(), memberId);
        if (bookmarkOptional.isPresent()) {
            return RandomHearitResponse.fromWithBookmark(hearit, bookmarkOptional.get());
        }
        return RandomHearitResponse.from(hearit);
    }

    public List<RecommendHearitResponse> getRecommendedHearits() {
        return hearitRepository.findRandom(RECOMMEND_HEARIT_COUNT).stream()
                .map(RecommendHearitResponse::from)
                .toList();
    }

    //TODO 사용자에 맞는 카테고리 추천
    public List<GroupedHearitsWithCategoryResponse> getGroupedHearitsByCategory() {
        List<Category> categories = categoryRepository.findOldest(CATEGORY_COUNT);
        return categories.stream()
                .map(this::mapToGroupedHearitsResponse)
                .toList();
    }

    private GroupedHearitsWithCategoryResponse mapToGroupedHearitsResponse(Category category) {
        List<Hearit> hearits = hearitRepository.findByCategory(category.getId(), HEARITS_PER_CATEGORY);
        return GroupedHearitsWithCategoryResponse.from(category, hearits);
    }
}
