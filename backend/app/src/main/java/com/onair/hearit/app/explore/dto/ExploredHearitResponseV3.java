package com.onair.hearit.app.explore.dto;

import com.onair.hearit.core.domain.Bookmark;
import com.onair.hearit.core.domain.Hearit;
import com.onair.hearit.core.domain.Keyword;
import java.util.List;

public record ExploredHearitResponseV3(
        Long id,
        String title,
        String categoryColorCode,
        Boolean isBookmarked,
        Long bookmarkId,
        List<KeywordResponse> keywords,
        String cursor
) {
    public static ExploredHearitResponseV3 from(Hearit hearit, List<Keyword> keywords, double score) {
        ExploreCursor exploreCursor = new ExploreCursor(score, hearit.getId());
        return new ExploredHearitResponseV3(
                hearit.getId(),
                hearit.getTitle(),
                hearit.getCategory().getColorCode(),
                false,
                null,
                getKeywordResponses(keywords),
                exploreCursor.encode()
        );
    }

    public static ExploredHearitResponseV3 fromWithBookmark(Hearit hearit, Bookmark bookmark,
                                                             List<Keyword> keywords, double score) {
        ExploreCursor exploreCursor = new ExploreCursor(score, hearit.getId());
        return new ExploredHearitResponseV3(
                hearit.getId(),
                hearit.getTitle(),
                hearit.getCategory().getColorCode(),
                true,
                bookmark.getId(),
                getKeywordResponses(keywords),
                exploreCursor.encode()
        );
    }

    private static List<KeywordResponse> getKeywordResponses(List<Keyword> keywords) {
        return keywords.stream()
                .map(k -> new KeywordResponse(k.getId(), k.getName()))
                .toList();
    }

    public record KeywordResponse(
            Long id,
            String name
    ) {
    }
}
