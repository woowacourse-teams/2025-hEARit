package com.onair.hearit.dto.request;

import com.onair.hearit.common.exception.custom.InvalidInputException;

public record BookmarkListCondition(
        int page,
        int size
) {

    public BookmarkListCondition {
        if(page < 0) {
            throw new InvalidInputException("page는 0 이상이어야합니다.");
        }
        if(size < 0 || size > 50) {
            throw new InvalidInputException("size는 0 ~ 50 사이여야합니다.");
        }
    }
}
