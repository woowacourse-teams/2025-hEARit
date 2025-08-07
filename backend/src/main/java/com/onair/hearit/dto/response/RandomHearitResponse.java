package com.onair.hearit.dto.response;

import com.onair.hearit.domain.Bookmark;
import com.onair.hearit.domain.Hearit;
import com.onair.hearit.domain.Keyword;
import java.util.List;

public record RandomHearitResponse(
        Long id,
        String title,
        String categoryColorCode,
        Boolean isBookmarked,
        Long bookmarkId,
        List<KeywordResponse> keywords
) {
    public static RandomHearitResponse from(Hearit hearit, List<Keyword> keywords) {
        List<KeywordResponse> keywordResponses = getKeywordNames(keywords);
        return new RandomHearitResponse(
                hearit.getId(),
                hearit.getTitle(),
                hearit.getCategory().getColorCode(),
                false,
                null,
                keywordResponses
        );
    }

    public static RandomHearitResponse fromWithBookmark(Hearit hearit, Bookmark bookmark, List<Keyword> keywords) {
        List<KeywordResponse> keywordResponses = getKeywordNames(keywords);
        return new RandomHearitResponse(
                hearit.getId(),
                hearit.getTitle(),
                hearit.getCategory().getColorCode(),
                true,
                bookmark.getId(),
                keywordResponses
        );
    }

    private static List<KeywordResponse> getKeywordNames(List<Keyword> keywords) {
        return keywords.stream()
                .map(KeywordResponse::from)
                .toList();
    }

    private record KeywordResponse(
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
}
