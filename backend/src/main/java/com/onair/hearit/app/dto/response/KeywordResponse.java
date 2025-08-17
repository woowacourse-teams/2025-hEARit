package com.onair.hearit.app.dto.response;

import com.onair.hearit.common.domain.Keyword;

public record KeywordResponse(
        Long id,
        String name
) {
    public static KeywordResponse from(Keyword keyword) {
        return new KeywordResponse(
                keyword.getId(),
                keyword.getName()
        );
    }
}
