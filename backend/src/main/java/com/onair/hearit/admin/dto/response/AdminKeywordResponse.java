package com.onair.hearit.admin.dto.response;

import com.onair.hearit.domain.Keyword;

public record AdminKeywordResponse(
        Long id,
        String name
) {
    public static AdminKeywordResponse from(Keyword keyword) {
        return new AdminKeywordResponse(
                keyword.getId(),
                keyword.getName()
        );
    }
}
