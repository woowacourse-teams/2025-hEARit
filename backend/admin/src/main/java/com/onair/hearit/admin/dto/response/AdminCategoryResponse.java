package com.onair.hearit.admin.dto.request;

import com.onair.hearit.core.domain.Category;

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
