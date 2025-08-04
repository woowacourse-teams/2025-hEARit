package com.onair.hearit.dto.response;

import com.onair.hearit.domain.Bookmark;
import com.onair.hearit.domain.Hearit;
import com.onair.hearit.domain.Keyword;
import java.util.List;

public record RandomHearitResponse(
        Long id,
        String title,
        Boolean isBookmarked,
        Long bookmarkId,
        List<RandomHearitResponse.KeywordResponse> keywords
) {
    public static RandomHearitResponse from(Hearit hearit, List<Keyword> keywords) {
        List<KeywordResponse> keywordResponses = getKeywordNames(keywords);
        return new RandomHearitResponse(
                hearit.getId(),
                hearit.getTitle(),
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
                true,
                bookmark.getId(),
                keywordResponses
        );
    }

    private static List<RandomHearitResponse.KeywordResponse> getKeywordNames(List<Keyword> keywords) {
        return keywords.stream()
                .map(RandomHearitResponse.KeywordResponse::from)
                .toList();
    }

    private record KeywordResponse(
            Long id,
            String name
    ) {
        public static RandomHearitResponse.KeywordResponse from(Keyword keyword) {
            return new RandomHearitResponse.KeywordResponse(
                    keyword.getId(),
                    keyword.getName()
            );
        }
    }
}
