package com.onair.hearit.dto.response;

import com.onair.hearit.domain.Category;

public record CategoryResponse(
        Long id,
        String name,
        String colorCode
) {

    public static CategoryResponse from(final Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getColorCode());
    }
}
