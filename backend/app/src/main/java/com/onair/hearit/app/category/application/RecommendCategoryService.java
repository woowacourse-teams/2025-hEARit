package com.onair.hearit.app.category.application;

import com.onair.hearit.app.exception.custom.NotFoundException;
import com.onair.hearit.core.domain.Category;
import com.onair.hearit.core.domain.Member;
import com.onair.hearit.core.domain.UserInfo;
import com.onair.hearit.core.infrastructure.jpa.CategoryRepository;
import com.onair.hearit.core.infrastructure.jpa.MemberRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RecommendCategoryService {

    private static final String IT_TREND_CATEGORY_NAME = "IT 트렌드";
    private static final int USER_BASED_RECOMMEND_COUNT = 3;
    private static final int TOTAL_RECOMMEND_COUNT = 5;

    private final CategoryRepository categoryRepository;
    private final MemberRepository memberRepository;

    public List<Category> getRecommendedCategories(UserInfo userInfo) {
        List<Category> recommendations = new ArrayList<>();
        recommendations.add(getItTrendCategory());
        if (userInfo.isMember()) {
            recommendations.addAll(getUserBasedRecommendations(userInfo, recommendations));
        }
        recommendations.addAll(getRandomRecommendations(recommendations));
        return recommendations;
    }

    private Category getItTrendCategory() {
        return categoryRepository.findByName(IT_TREND_CATEGORY_NAME)
                .orElseThrow(() -> new NotFoundException("category", IT_TREND_CATEGORY_NAME));
    }

    private List<Category> getUserBasedRecommendations(UserInfo userInfo, List<Category> alreadyRecommended) {
        Member member = getMemberById(userInfo.getMemberId());
        List<Long> excludedIds = alreadyRecommended.stream()
                .map(Category::getId)
                .toList();
        return categoryRepository.findTopCategoriesByMemberBookmarks(
                member.getId(),
                USER_BASED_RECOMMEND_COUNT,
                excludedIds
        );
    }

    private Member getMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("memberId", memberId.toString()));
    }

    private List<Category> getRandomRecommendations(List<Category> alreadyRecommended) {
        int remainingCount = TOTAL_RECOMMEND_COUNT - alreadyRecommended.size();
        if (remainingCount <= 0) {
            return Collections.emptyList();
        }
        return pickRandomCategories(alreadyRecommended, remainingCount);
    }

    private List<Category> pickRandomCategories(List<Category> recommendCategories, int count) {
        List<Long> categoryIds = findCandidateIdsForRandomPick(recommendCategories);
        List<Long> mutableCategoryIds = new ArrayList<>(categoryIds);
        Collections.shuffle(mutableCategoryIds, new Random());

        int pickCount = Math.min(count, mutableCategoryIds.size());
        List<Long> pickedIds = mutableCategoryIds.subList(0, pickCount);
        return categoryRepository.findAllById(pickedIds);
    }

    private List<Long> findCandidateIdsForRandomPick(List<Category> excludedCategories) {
        List<Long> excludedIds = excludedCategories.stream()
                .map(Category::getId)
                .toList();
        if (excludedIds.isEmpty()) {
            return categoryRepository.findAllIds();
        }
        return categoryRepository.findIdsWithoutExcludedIds(excludedIds);
    }
}
