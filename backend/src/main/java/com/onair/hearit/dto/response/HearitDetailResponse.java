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
        List<KeywordResponse> keywords
) {
    public static HearitDetailResponse from(Hearit hearit, List<Keyword> keywords) {
        List<KeywordResponse> keywordNames = getKeywordNames(keywords);
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
        List<KeywordResponse> keywordNames = getKeywordNames(keywords);
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

    private static List<KeywordResponse> getKeywordNames(List<Keyword> keywords) {
        return keywords.stream().map(KeywordResponse::from).toList();
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
