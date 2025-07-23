package com.onair.hearit.dto.request;

import com.onair.hearit.common.exception.custom.InvalidInputException;
import com.onair.hearit.common.exception.custom.InvalidPageException;

public record SearchCondition(
        String searchTerm,
        int page,
        int size
) {

    public SearchCondition {
        if (searchTerm == null || searchTerm.length() > 20) {
            throw new InvalidInputException("검색어는 20자를 이하어야 합니다.");
        }
        if (page < 0) {
            throw new InvalidPageException(0);
        }
        if (size < 0 || size > 50) {
            throw new InvalidPageException(0, 50);
        }
    }
}
