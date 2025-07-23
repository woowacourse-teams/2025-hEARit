package com.onair.hearit.dto.response;

import com.onair.hearit.domain.Keyword;

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