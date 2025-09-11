package com.onair.hearit.app.dto.response;

import com.onair.hearit.common.domain.Bookmark;
import com.onair.hearit.common.domain.Hearit;
import com.onair.hearit.common.domain.Keyword;
import java.util.List;

public record ExploredHearitResponse(
        Long id,
        String title,
        String categoryColorCode,
        Boolean isBookmarked,
        Long bookmarkId,
        List<KeywordResponse> keywords,
        Long cursorId
) {
    public static ExploredHearitResponse from(Hearit hearit, List<Keyword> keywords, Long cursorId) {
        List<KeywordResponse> keywordResponses = getKeywordNames(keywords);
        return new ExploredHearitResponse(
                hearit.getId(),
                hearit.getTitle(),
                hearit.getCategory().getColorCode(),
                false,
                null,
                keywordResponses,
                cursorId
        );
    }

    public static ExploredHearitResponse fromWithBookmark(Hearit hearit, Bookmark bookmark, List<Keyword> keywords, Long cursorId) {
        List<KeywordResponse> keywordResponses = getKeywordNames(keywords);
        return new ExploredHearitResponse(
                hearit.getId(),
                hearit.getTitle(),
                hearit.getCategory().getColorCode(),
                true,
                bookmark.getId(),
                keywordResponses,
                cursorId
        );
    }

    private static List<KeywordResponse> getKeywordNames(List<Keyword> keywords) {
        return keywords.stream()
                .map(KeywordResponse::from)
                .toList();
    }

    public record KeywordResponse(
            Long id,
            String name
    ) {
        private static KeywordResponse from(Keyword keyword) {
            return new KeywordResponse(
                    keyword.getId(),
                    keyword.getName()
            );
        }
    }
}
