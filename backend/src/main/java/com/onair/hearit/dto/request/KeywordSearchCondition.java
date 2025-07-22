package com.onair.hearit.dto.request;

import com.onair.hearit.common.exception.custom.InvalidInputException;
import com.onair.hearit.common.exception.custom.InvalidPageException;

public record KeywordSearchCondition(
        Long keywordId,
        int page,
        int size
) {

    public KeywordSearchCondition {
        if (keywordId == null) {
            throw new InvalidInputException("키워드 id는 null이 될 수 없습니다. ");
        }
        if (page < 0) {
            throw new InvalidPageException(0);
        }
        if (size < 0 || size > 50) {
            throw new InvalidPageException(0, 50);
        }
    }
}
