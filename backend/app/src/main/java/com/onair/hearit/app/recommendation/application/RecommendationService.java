package com.onair.hearit.app.recommendation.application;

import com.onair.hearit.app.recommendation.dto.RecommendationByCategoryResponse;
import com.onair.hearit.core.domain.Category;
import com.onair.hearit.core.domain.Hearit;
import com.onair.hearit.core.domain.UserInfo;
import com.onair.hearit.core.infrastructure.jpa.HearitRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final CategoryRecommender categoryRecommender;
    private final HearitRepository hearitRepository;

    @Transactional(readOnly = true)
    public List<RecommendationByCategoryResponse> getCategoryRecommendations(UserInfo userInfo,
                                                                             int categorySize,
                                                                             int hearitSize) {
        List<Category> recommendCategories = categoryRecommender.getRecommendedCategories(userInfo, categorySize);
        return recommendCategories.stream()
                .map(category -> toRecommendationByCategoryResponse(category, hearitSize))
                .toList();
    }

    private RecommendationByCategoryResponse toRecommendationByCategoryResponse(Category category, int hearitSize) {
        List<Hearit> hearits = hearitRepository.findByCategory(category.getId(), hearitSize);
        return RecommendationByCategoryResponse.from(category, hearits);
    }
}
