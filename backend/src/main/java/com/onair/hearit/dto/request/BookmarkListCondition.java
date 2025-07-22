package com.onair.hearit.dto.request;

import com.onair.hearit.common.exception.custom.InvalidInputException;

public record BookmarkListCondition(
        int page,
        int size
) {

    public BookmarkListCondition {
        if (page < 0) {
            throw new InvalidInputException("page는 0 이상이어야 합니다.");
        }
        if (size < 0 || size > 50) {
            throw new InvalidInputException("size는 0 이상 50 이하이어야 합니다.");
        }
    }
}
