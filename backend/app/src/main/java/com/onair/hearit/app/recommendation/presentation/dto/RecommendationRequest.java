package com.onair.hearit.app.recommendation.presentation.dto;

public record RecommendationRequest(
        Integer categorySize,
        Integer hearitSize
) {
    public RecommendationRequest {
        categorySize = validate(categorySize, MIN_CATEGORY_SIZE, MAX_CATEGORY_SIZE, DEFAULT_CATEGORY_SIZE);
        hearitSize = validate(hearitSize, MIN_HEARIT_SIZE, MAX_HEARIT_SIZE, DEFAULT_HEARIT_SIZE);
    }

    private static final int MIN_CATEGORY_SIZE = 1;
    private static final int MAX_CATEGORY_SIZE = 20;
    private static final int DEFAULT_CATEGORY_SIZE = 5;

    private static final int MIN_HEARIT_SIZE = 1;
    private static final int MAX_HEARIT_SIZE = 30;
    private static final int DEFAULT_HEARIT_SIZE = 5;

    private static int validate(Integer value, int min, int max, int defaultValue) {
        if (value == null || value < min || value > max) {
            return defaultValue;
        }
        return value;
    }
}
