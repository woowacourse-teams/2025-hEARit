package com.onair.hearit.application;

import com.onair.hearit.common.exception.custom.NotFoundException;
import com.onair.hearit.domain.Bookmark;
import com.onair.hearit.domain.Category;
import com.onair.hearit.domain.Hearit;
import com.onair.hearit.dto.request.PagingRequest;
import com.onair.hearit.dto.response.HearitDetailResponse;
import com.onair.hearit.dto.response.HomeCategoryHearitResponse;
import com.onair.hearit.dto.response.PagedResponse;
import com.onair.hearit.dto.response.RandomHearitResponse;
import com.onair.hearit.dto.response.RecommendHearitResponse;
import com.onair.hearit.infrastructure.BookmarkRepository;
import com.onair.hearit.infrastructure.CategoryRepository;
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
    private static final int CATEGORY_HEARIT_COUNT = 5;
    private static final List<Long> FIXED_CATEGORY_IDS = List.of(2L, 3L, 4L); // Android, Spring, Java

    private final HearitRepository hearitRepository;
    private final BookmarkRepository bookmarkRepository;
    private final CategoryRepository categoryRepository;

    public HearitDetailResponse getHearitDetail(Long hearitId, Long memberId) {
        Hearit hearit = getHearitById(hearitId);
        Optional<Bookmark> bookmarkOptional = bookmarkRepository.findByHearitIdAndMemberId(hearitId, memberId);
        if (bookmarkOptional.isPresent()) {
            return HearitDetailResponse.fromWithBookmark(hearit, bookmarkOptional.get());
        }
        return HearitDetailResponse.from(hearit);
    }

    private Hearit getHearitById(Long hearitId) {
        return hearitRepository.findById(hearitId)
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
        Pageable pageable = PageRequest.of(0, RECOMMEND_HEARIT_COUNT);
        return hearitRepository.findRandom(pageable).stream()
                .map(RecommendHearitResponse::from)
                .toList();
    }

    public List<HomeCategoryHearitResponse> getHomeCategoryHearits() {
        Pageable pageable = PageRequest.of(0, CATEGORY_HEARIT_COUNT);
        List<Category> categories = categoryRepository.findTop3ByOrderByIdAsc();
        return categories.stream()
                .map(category -> {
                    List<Hearit> hearits = hearitRepository.findTop5ByCategory(category.getId(), pageable);
                    return HomeCategoryHearitResponse.from(category, hearits);
                })
                .toList();
    }
}
