package com.onair.hearit.dto.request;

import com.onair.hearit.common.exception.custom.InvalidInputException;

public record CategorySearchCondition(
        Long categoryId,
        int page,
        int size
) {
    public CategorySearchCondition {
        if (categoryId == null) {
            throw new InvalidInputException("카테고리 id는 null이 될 수 없습니다. ");
        }
        if (page < 0) {
            throw new InvalidInputException("page는 0 이상의 값이어야 합니다.");
        }
        if (size < 0 || size > 50) {
            throw new InvalidInputException("size는 0 이상 50 이하의 값이어야 합니다.");
        }
    }
}
