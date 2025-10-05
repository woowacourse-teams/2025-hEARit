package com.onair.hearit.app.recommendation.application;

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
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CategoryRecommender {

    private static final String IT_TREND_CATEGORY_NAME = "IT 트렌드";
    private static final int RESERVED_RANDOM_COUNT = 1;

    private final CategoryRepository categoryRepository;
    private final MemberRepository memberRepository;

    public List<Category> getRecommendedCategories(UserInfo userInfo, int totalRecommendCount) {
        List<Category> recommendations = new ArrayList<>();
        recommendations.add(getItTrendCategory());

        int userBasedCount = calculateUserBasedCount(totalRecommendCount);
        if (userInfo.isMember()) {
            recommendations.addAll(getUserBasedRecommendations(userInfo, userBasedCount, recommendations));
        }

        int randomCount = totalRecommendCount - recommendations.size();
        recommendations.addAll(getRandomRecommendations(randomCount, recommendations));
        return recommendations;
    }

    private Category getItTrendCategory() {
        return categoryRepository.findByName(IT_TREND_CATEGORY_NAME)
                .orElseThrow(() -> new NotFoundException("category", IT_TREND_CATEGORY_NAME));
    }

    private int calculateUserBasedCount(int totalRecommendCount) {
        int reservedCount = 1 /* IT 트렌드 */ + RESERVED_RANDOM_COUNT;
        return Math.max(totalRecommendCount - reservedCount, 0);
    }

    private List<Category> getUserBasedRecommendations(UserInfo userInfo, int userBasedCount, List<Category> alreadyRecommended) {
        Member member = getMemberById(userInfo.getMemberId());
        List<Long> excludedIds = alreadyRecommended.stream()
                .map(Category::getId)
                .toList();
        return categoryRepository.findTopCategoriesByMemberBookmarks(
                member.getId(),
                userBasedCount,
                excludedIds
        );
    }

    private Member getMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("memberId", memberId.toString()));
    }

    private List<Category> getRandomRecommendations(int randomCount, List<Category> excludedCategories) {
        if (randomCount <= 0) {
            return Collections.emptyList();
        }
        return pickRandomCategories(excludedCategories, randomCount);
    }

    private List<Category> pickRandomCategories(List<Category> excludedCategories, int count) {
        List<Long> categoryIds = findCandidateIdsForRandomPick(excludedCategories);
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
