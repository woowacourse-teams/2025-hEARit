package com.onair.hearit.app.recommendation.presentation;

import com.onair.hearit.app.auth.domain.RequestUser;
import com.onair.hearit.app.recommendation.dto.RecommendationByCategoryResponse;
import com.onair.hearit.app.recommendation.application.RecommendationService;
import com.onair.hearit.app.recommendation.presentation.dto.RecommendationRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    @GetMapping({"/api/v1/recommendations/categories",
            "/api/v1/hearits/recommend-category" /* To be deprecated */})
    public ResponseEntity<List<RecommendationByCategoryResponse>> readRecommendationsByCategory(
            @AuthenticationPrincipal RequestUser requestUser,
            @ModelAttribute RecommendationRequest request) {
        List<RecommendationByCategoryResponse> responses =
                recommendationService.getCategoryRecommendations(
                        requestUser.getUserInfo(),
                        request.categorySize(),
                        request.hearitSize()
                );
        return ResponseEntity.ok(responses);
    }
}
