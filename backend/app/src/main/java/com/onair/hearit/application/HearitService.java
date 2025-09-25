package com.onair.hearit.application;

import com.onair.hearit.application.recommend.RecommendHearitStrategy;
import com.onair.hearit.dto.request.PagingRequest;
import com.onair.hearit.dto.response.HearitDetailResponse;
import com.onair.hearit.dto.response.HearitOfCategoryResponse;
import com.onair.hearit.dto.response.HearitsWithRecommendCategoryResponse;
import com.onair.hearit.dto.response.PagedResponse;
import com.onair.hearit.dto.response.RecommendHearitResponse;
import com.onair.hearit.domain.Bookmark;
import com.onair.hearit.domain.Category;
import com.onair.hearit.domain.Hearit;
import com.onair.hearit.domain.HearitKeyword;
import com.onair.hearit.domain.Keyword;
import com.onair.hearit.domain.Member;
import com.onair.hearit.domain.PlayingHistory;
import com.onair.hearit.domain.UserInfo;
import com.onair.hearit.exception.custom.NotFoundException;
import com.onair.hearit.exception.custom.UnauthenticatedException;
import com.onair.hearit.infrastructure.projection.HearitWithPlayTimeProjection;
import com.onair.hearit.infrastructure.jpa.BookmarkRepository;
import com.onair.hearit.infrastructure.jpa.CategoryRepository;
import com.onair.hearit.infrastructure.jpa.HearitKeywordRepository;
import com.onair.hearit.infrastructure.jpa.HearitRepository;
import com.onair.hearit.infrastructure.jpa.MemberRepository;
import com.onair.hearit.infrastructure.jpa.PlayingHistoryRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
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

    public HearitDetailResponse getHearitDetail(Long hearitId, UserInfo userInfo) {
        Hearit hearit = getHearitById(hearitId);
        List<Keyword> keywords = hearitKeywordRepository.findKeywordsByHearitId(hearit.getId());
        if (userInfo == null || userInfo.isGuest()) {
            return HearitDetailResponse.of(hearit, keywords, null, null);
        }

        Member member = getMemberByUserInfo(userInfo);
        Long bookmarkId = bookmarkRepository.findByHearitAndMember(hearit, member)
                .map(Bookmark::getId)
                .orElse(null);
        Long lastPlayTime = calculateLastPlayTime(hearit, member);
        return HearitDetailResponse.of(hearit, keywords, lastPlayTime, bookmarkId);
    }

    private Hearit getHearitById(Long hearitId) {
        return hearitRepository.findWithCategoryById(hearitId)
                .orElseThrow(() -> new NotFoundException("hearitId", hearitId.toString()));
    }

    private Long calculateLastPlayTime(Hearit hearit, Member member) {
        Optional<Long> optionalLastPlayTime = playingHistoryRepository.findByHearitIdAndMemberId(hearit.getId(),
                        member.getId())
                .map(PlayingHistory::getLastPlayTime);
        if (optionalLastPlayTime.isEmpty()) {
            return null;
        }
        Long lastPlayTime = optionalLastPlayTime.get();
        Long remainingMillis = hearit.getPlayTime() * 1000 - lastPlayTime; // playTime(s), lastPlayTime(ms)
        if (remainingMillis <= 5000) {
            return 0L;
        }
        return lastPlayTime;
    }

    public List<RecommendHearitResponse> getRecommendedHearits() {
        List<Hearit> recommendHearits = recommendHearitStrategy.getRecommendHearit(RECOMMEND_HEARIT_COUNT);
        return recommendHearits.stream()
                .map(RecommendHearitResponse::from)
                .toList();
    }

    public List<HearitsWithRecommendCategoryResponse> getHearitsWithRecommendCategory(UserInfo userInfo) {
        List<Category> recommendCategories = getRecommendCategories(userInfo);
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

    private List<Category> getRecommendCategories(UserInfo userInfo) {
        if (userInfo == null || userInfo.isGuest()) {
            return new ArrayList<>();
        }
        Member member = getMemberByUserInfo(userInfo);
        return categoryRepository.findTopCategoriesByMemberBookmarks(member.getId(), RECOMMEND_CATEGORY_COUNT);

    }

    private Member getMemberByUserInfo(UserInfo userInfo) {
        if (userInfo == null || userInfo.isGuest()) {
            throw new UnauthenticatedException();
        }
        return getMemberById(userInfo.getMemberId());
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

    public PagedResponse<HearitOfCategoryResponse> getHearitsByCategory(
            Long categoryId,
            PagingRequest pagingRequest,
            UserInfo userInfo) {
        Pageable pageable = PageRequest.of(pagingRequest.page(), pagingRequest.size());

        Long memberId = (userInfo == null || userInfo.isGuest()) ? null : userInfo.getMemberId();
        Page<HearitWithPlayTimeProjection> hearitsWithPlayTime =
                hearitRepository.findWithPlayTimeByCategoryId(categoryId, memberId, pageable);
        List<Hearit> hearits = hearitsWithPlayTime.getContent().stream()
                .map(HearitWithPlayTimeProjection::getHearit)
                .toList();
        List<Long> hearitIds = hearits.stream().map(Hearit::getId).toList();
        Map<Long, List<Keyword>> keywordsMap = getKeywordsMap(hearitIds);
        Page<HearitOfCategoryResponse> response = hearitsWithPlayTime.map(projection -> {
            Hearit hearit = projection.getHearit();
            Long lastPlayTime = projection.getLastPlayTime();
            List<Keyword> keywords = keywordsMap.getOrDefault(hearit.getId(), Collections.emptyList());
            return HearitOfCategoryResponse.from(hearit, keywords, lastPlayTime);
        });
        return PagedResponse.from(response);
    }

    private Map<Long, List<Keyword>> getKeywordsMap(List<Long> hearitIds) {
        List<HearitKeyword> hearitKeywords = hearitKeywordRepository.findByHearitIdIn(hearitIds);
        return hearitKeywords.stream()
                .collect(Collectors.groupingBy(
                        hk -> hk.getHearit().getId(),
                        Collectors.mapping(HearitKeyword::getKeyword, Collectors.toList())
                ))
                .entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().stream()
                                .limit(KEYWORDS_PER_CATEGORIZED_HEARIT)
                                .toList()
                ));
    }
}
