package com.onair.hearit.dto.request;

import com.onair.hearit.common.exception.custom.InvalidInputException;

public record TitleSearchCondition(
        String search,
        Integer page,
        Integer size
) {
    public TitleSearchCondition {
        if (search == null || search.length() > 20) {
            throw new InvalidInputException("검색어는 20자를 이하어야 합니다.");
        }
        if (page == null || page < 0) {
            throw new InvalidInputException("page는 0 이상의 값이어야 합니다.");
        }
        if (size == null || size < 0 || size > 50) {
            throw new InvalidInputException("size는 0 이상 50 이하의 값이어야 합니다.");
        }
    }
}