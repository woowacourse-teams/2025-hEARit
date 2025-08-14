package com.onair.hearit.admin.dto.response;

import com.onair.hearit.domain.Keyword;

public record KeywordInfoResponse(
        Long id,
        String name
) {
    public static KeywordInfoResponse from(Keyword keyword) {
        return new KeywordInfoResponse(
                keyword.getId(),
                keyword.getName()
        );
    }
} 
