package com.onair.hearit.dto.response;

import com.onair.hearit.domain.Bookmark;
import com.onair.hearit.domain.Hearit;
import com.onair.hearit.domain.Keyword;
import java.time.LocalDateTime;
import java.util.List;

public record HearitDetailResponse(
        Long id,
        String title,
        String summary,
        String source,
        Integer playTime,
        LocalDateTime createdAt,
        Boolean isBookmarked,
        Long bookmarkId,
        String category,
        List<KeywordNameResponse> keywords
) {
    public static HearitDetailResponse from(Hearit hearit, List<Keyword> keywords) {
        List<KeywordNameResponse> keywordNames = getKeywordNames(keywords);
        return new HearitDetailResponse(
                hearit.getId(),
                hearit.getTitle(),
                hearit.getSummary(),
                hearit.getSource(),
                hearit.getPlayTime(),
                hearit.getCreatedAt(),
                false,
                null,
                hearit.getCategory().getName(),
                keywordNames);
    }

    public static HearitDetailResponse fromWithBookmark(Hearit hearit, Bookmark bookmark, List<Keyword> keywords) {
        List<KeywordNameResponse> keywordNames = getKeywordNames(keywords);
        return new HearitDetailResponse(
                hearit.getId(),
                hearit.getTitle(),
                hearit.getSummary(),
                hearit.getSource(),
                hearit.getPlayTime(),
                hearit.getCreatedAt(),
                true,
                bookmark.getId(),
                hearit.getCategory().getName(),
                keywordNames);
    }

    private static List<KeywordNameResponse> getKeywordNames(List<Keyword> keywords) {
        return keywords.stream().map(KeywordNameResponse::from).toList();
    }

    record KeywordNameResponse(
            String name
    ) {

        public static KeywordNameResponse from(Keyword keyword) {
            return new KeywordNameResponse(keyword.getName());
        }
    }
}
