package com.onair.hearit.app.keyword.dto;

import com.onair.hearit.core.domain.Keyword;

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
