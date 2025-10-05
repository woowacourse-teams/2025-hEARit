package com.onair.hearit.app.recommendation.presentation.dto;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RecommendationRequestTest {

    @Test
    @DisplayName("정상 범위 내 값이면 그대로 반영된다")
    void validValues() {
        RecommendationRequest request = new RecommendationRequest(10, 15);

        assertThat(request.categorySize()).isEqualTo(10);
        assertThat(request.hearitSize()).isEqualTo(15);
    }

    @Test
    @DisplayName("카테고리 크기가 범위를 벗어나면 default 값(5)으로 세팅된다")
    void categoryOutOfRange() {
        RecommendationRequest tooSmall = new RecommendationRequest(0, 10);
        RecommendationRequest tooLarge = new RecommendationRequest(25, 10);

        assertThat(tooSmall.categorySize()).isEqualTo(5);
        assertThat(tooLarge.categorySize()).isEqualTo(5);
    }

    @Test
    @DisplayName("히어릿 크기가 범위를 벗어나면 default 값(5)으로 세팅된다")
    void hearitOutOfRange() {
        RecommendationRequest tooSmall = new RecommendationRequest(10, 0);
        RecommendationRequest tooLarge = new RecommendationRequest(10, 40);

        assertThat(tooSmall.hearitSize()).isEqualTo(5);
        assertThat(tooLarge.hearitSize()).isEqualTo(5);
    }

    @Test
    @DisplayName("값이 null이면 default 값(5)으로 세팅된다")
    void nullValues() {
        RecommendationRequest request = new RecommendationRequest(null, null);

        assertThat(request.categorySize()).isEqualTo(5);
        assertThat(request.hearitSize()).isEqualTo(5);
    }
}
