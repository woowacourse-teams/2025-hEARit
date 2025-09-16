package com.onair.hearit.admin.dto.request;

import com.onair.hearit.admin.exception.custom.AdminInvalidInputException;

public record AdminPagingRequest(
        int page,
        int size
) {
    public AdminPagingRequest {
        if (page < 0) {
            throw new AdminInvalidInputException("page는 0 이상이어야합니다.");
        }
        if (size < 0 || size > 100) {
            throw new AdminInvalidInputException("size는 0 ~ 100 이어야합니다.");
        }
    }
}
