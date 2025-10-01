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

    public List<Category> getRecommendedCategoriesFor(UserInfo userInfo) {
        List<Category> composedCategories = new ArrayList<>();

        Category itTrendCategory = getItTrendCategory();
        composedCategories.add(itTrendCategory);

        if (userInfo != null && !userInfo.isGuest()) {
            Member member = memberRepository.findById(userInfo.getMemberId())
                    .orElseThrow(() -> new NotFoundException("memberId", userInfo.getMemberId().toString()));

            List<Category> userCategories = categoryRepository.findTopCategoriesByMemberBookmarks(
                    member.getId(),
                    USER_BASED_RECOMMEND_COUNT,
                    itTrendCategory.getId()
            );
            composedCategories.addAll(userCategories);
        }

        int remainingCount = TOTAL_RECOMMEND_COUNT - composedCategories.size();
        if (remainingCount > 0) {
            composedCategories.addAll(pickRandomCategories(composedCategories, remainingCount));
        }
        return composedCategories;
    }

    private Category getItTrendCategory() {
        return categoryRepository.findByName(IT_TREND_CATEGORY_NAME)
                .orElseThrow(() -> new NotFoundException("category", IT_TREND_CATEGORY_NAME));
    }

    private List<Category> pickRandomCategories(List<Category> recommendCategories, int count) {
        List<Long> categoryIds = getAllCategoryIdsWithoutRecommend(recommendCategories);
        List<Long> mutableCategoryIds = new ArrayList<>(categoryIds);
        Collections.shuffle(mutableCategoryIds, new Random());
        int pickCount = Math.min(count, mutableCategoryIds.size());
        List<Long> pickedIds = mutableCategoryIds.subList(0, pickCount);
        return categoryRepository.findAllById(pickedIds);
    }

    private List<Long> getAllCategoryIdsWithoutRecommend(List<Category> recommendCategories) {
        List<Long> recommendCategoryIds = recommendCategories.stream().map(Category::getId).toList();
        List<Long> categoryIds = categoryRepository.findAllIds();
        return categoryIds.stream()
                .filter(id -> !recommendCategoryIds.contains(id))
                .toList();
    }
}
