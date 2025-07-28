package com.onair.hearit.dto.request;

import com.onair.hearit.common.exception.custom.InvalidInputException;

public record PagingRequest(
        int page,
        int size
) {
    public PagingRequest {
        if (page < 0) {
            throw new InvalidInputException("page는 0 이상이어야합니다.");
        }
        if (size < 0 || size > 100) {
            throw new InvalidInputException("size는 0 ~ 100 이어야합니다.");
        }
    }
}
