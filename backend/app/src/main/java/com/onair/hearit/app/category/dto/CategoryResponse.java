package com.onair.hearit.app.category.dto;

import com.onair.hearit.core.domain.Category;

public record CategoryResponse(
        Long id,
        String name,
        String colorCode
) {
    public static CategoryResponse from(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getColorCode());
    }
}
