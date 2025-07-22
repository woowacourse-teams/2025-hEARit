package com.onair.hearit.dto.request;

import com.onair.hearit.common.exception.custom.InvalidPageException;

public record RandomHearitCondition(
        int page,
        int size
) {

    public RandomHearitCondition {
        if (page < 0) {
            throw new InvalidPageException(0);
        }
        if (size < 0 || size > 50) {
            throw new InvalidPageException(0, 50);
        }
    }
}
