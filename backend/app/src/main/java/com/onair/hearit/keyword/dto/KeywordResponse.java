package com.onair.hearit.keyword.dto;

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
