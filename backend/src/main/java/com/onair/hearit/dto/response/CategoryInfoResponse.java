package com.onair.hearit.dto.response;

import com.onair.hearit.domain.Category;

public record CategoryInfoResponse(
        Long id,
        String name,
        String colorCode
) {
    public static CategoryInfoResponse from(Category category) {
        return new CategoryInfoResponse(
                category.getId(),
                category.getName(),
                category.getColorCode()
        );
    }
}
