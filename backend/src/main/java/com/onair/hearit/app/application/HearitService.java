package com.onair.hearit.app.application;

import com.onair.hearit.app.application.recommend.RecommendHearitStrategy;
import com.onair.hearit.app.dto.request.PagingRequest;
import com.onair.hearit.app.dto.response.HearitDetailResponse;
import com.onair.hearit.app.dto.response.HearitOfCategoryResponse;
import com.onair.hearit.app.dto.response.HearitsWithRecommendCategoryResponse;
import com.onair.hearit.app.dto.response.PagedResponse;
import com.onair.hearit.app.dto.response.RecommendHearitResponse;
import com.onair.hearit.auth.domain.UserContext;
import com.onair.hearit.common.domain.Bookmark;
import com.onair.hearit.common.domain.Category;
import com.onair.hearit.common.domain.Hearit;
import com.onair.hearit.common.domain.Keyword;
import com.onair.hearit.common.domain.Member;
import com.onair.hearit.common.domain.PlayingHistory;
import com.onair.hearit.common.exception.custom.NotFoundException;
import com.onair.hearit.common.exception.custom.UnauthorizedException;
import com.onair.hearit.common.infrastructure.jpa.BookmarkRepository;
import com.onair.hearit.common.infrastructure.jpa.CategoryRepository;
import com.onair.hearit.common.infrastructure.jpa.HearitKeywordRepository;
import com.onair.hearit.common.infrastructure.jpa.HearitRepository;
import com.onair.hearit.common.infrastructure.jpa.MemberRepository;
import com.onair.hearit.common.infrastructure.jpa.PlayingHistoryRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
    private static final int RECOMMEND_CATEGORY_COUNT = 3;
    private static final int HEARITS_PER_RECOMMENDED_CATEGORY = 5;
    private static final int KEYWORDS_PER_CATEGORIZED_HEARIT = 3;

    private final HearitRepository hearitRepository;
    private final MemberRepository memberRepository;
    private final BookmarkRepository bookmarkRepository;
    private final HearitKeywordRepository hearitKeywordRepository;
    private final CategoryRepository categoryRepository;
    private final PlayingHistoryRepository playingHistoryRepository;
    private final RecommendHearitStrategy recommendHearitStrategy;

    public HearitDetailResponse getHearitDetail(Long hearitId, UserContext userContext) {
        Hearit hearit = getHearitById(hearitId);
        List<Keyword> keywords = hearitKeywordRepository.findKeywordsByHearitId(hearit.getId());
        if (userContext == null || userContext.isGuest()) {
            return HearitDetailResponse.of(hearit, keywords, null, null);
        }

        Member member = getMemberByUserContext(userContext);
        Long bookmarkId = bookmarkRepository.findByHearitAndMember(hearit, member)
                .map(Bookmark::getId)
                .orElse(null);
        Long lastPlayTime = playingHistoryRepository.findByHearitIdAndMemberId(hearit.getId(), member.getId())
                .map(PlayingHistory::getLastPlayTime)
                .orElse(null);
        return HearitDetailResponse.of(hearit, keywords, lastPlayTime, bookmarkId);
    }

    private Hearit getHearitById(Long hearitId) {
        return hearitRepository.findWithCategoryById(hearitId)
                .orElseThrow(() -> new NotFoundException("hearitId", hearitId.toString()));
    }

    public List<RecommendHearitResponse> getRecommendedHearits() {
        List<Hearit> recommendHearits = recommendHearitStrategy.getRecommendHearit(RECOMMEND_HEARIT_COUNT);
        return recommendHearits.stream()
                .map(RecommendHearitResponse::from)
                .toList();
    }

    public List<HearitsWithRecommendCategoryResponse> getHearitsWithRecommendCategory(UserContext userContext) {
        List<Category> recommendCategories = getRecommendCategories(userContext);
        if (recommendCategories.size() < RECOMMEND_CATEGORY_COUNT) {
            int extraCount = RECOMMEND_CATEGORY_COUNT - recommendCategories.size();
            List<Long> randomCategoryIds = pickTodayRandomCategoryIds(recommendCategories, extraCount);
            List<Category> randomCategories = categoryRepository.findAllById(randomCategoryIds);
            recommendCategories.addAll(randomCategories);
        }
        return recommendCategories.stream()
                .map(this::toHearitsWithRecommendedWithCategory)
                .toList();
    }

    private List<Category> getRecommendCategories(UserContext userContext) {
        if (userContext == null || userContext.isGuest()) {
            return new ArrayList<>();
        }
        Member member = getMemberByUserContext(userContext);
        return categoryRepository.findTopCategoriesByMemberBookmarks(member.getId(), RECOMMEND_CATEGORY_COUNT);

    }

    private Member getMemberByUserContext(UserContext userContext) {
        if (userContext == null || userContext.isGuest()) {
            throw new UnauthorizedException("로그인한 회원이 아닙니다.");
        }
        return getMemberById(userContext.memberId());
    }

    private Member getMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("memberId", memberId.toString()));
    }


    private List<Long> pickTodayRandomCategoryIds(List<Category> recommendCategories, int count) {
        long seed = LocalDate.now().toEpochDay();
        List<Long> categoryIds = getAllCategoryIdsWithoutRecommend(recommendCategories);
        List<Long> mutableCategoryIds = new ArrayList<>(categoryIds);
        Collections.shuffle(mutableCategoryIds, new Random(seed));
        return mutableCategoryIds.subList(0, count);
    }

    private List<Long> getAllCategoryIdsWithoutRecommend(List<Category> recommendCategories) {
        List<Long> recommendCategoryIds = recommendCategories.stream().map(Category::getId).toList();
        List<Long> categoryIds = categoryRepository.findAllIds();
        return categoryIds.stream()
                .filter(id -> !recommendCategoryIds.contains(id))
                .toList();
    }

    private HearitsWithRecommendCategoryResponse toHearitsWithRecommendedWithCategory(Category category) {
        List<Hearit> hearits = hearitRepository.findByCategory(category.getId(), HEARITS_PER_RECOMMENDED_CATEGORY);
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
