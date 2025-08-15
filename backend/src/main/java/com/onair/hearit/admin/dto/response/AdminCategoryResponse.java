package com.onair.hearit.admin.dto.response;

import com.onair.hearit.domain.Category;

public record AdminCategoryResponse(
        Long id,
        String name,
        String colorCode
) {
    public static AdminCategoryResponse from(Category category) {
        return new AdminCategoryResponse(
                category.getId(),
                category.getName(),
                category.getColorCode()
        );
    }
}
