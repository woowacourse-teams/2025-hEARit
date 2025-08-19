package com.onair.hearit.app.dto.response;

import com.onair.hearit.common.domain.Bookmark;
import com.onair.hearit.common.domain.Category;
import com.onair.hearit.common.domain.Hearit;
import com.onair.hearit.common.domain.Keyword;
import com.onair.hearit.common.domain.Source;
import java.time.LocalDateTime;
import java.util.List;

public record HearitDetailResponse(
        Long id,
        String title,
        String summary,
        List<SourceResponse> sources,
        Integer playTime,
        LocalDateTime createdAt,
        Boolean isBookmarked,
        Long bookmarkId,
        CategoryResponse category,
        List<KeywordResponse> keywords
) {
    public static HearitDetailResponse from(Hearit hearit, List<Keyword> keywords) {
        List<KeywordResponse> keywordNames = getKeywordNames(keywords);
        List<SourceResponse> sources = getSources(hearit.getSources());
        return new HearitDetailResponse(
                hearit.getId(),
                hearit.getTitle(),
                hearit.getSummary(),
                sources,
                hearit.getPlayTime(),
                hearit.getCreatedAt(),
                false,
                null,
                CategoryResponse.of(hearit.getCategory()),
                keywordNames);
    }

    public static HearitDetailResponse fromWithBookmark(Hearit hearit, Bookmark bookmark, List<Keyword> keywords) {
        List<KeywordResponse> keywordNames = getKeywordNames(keywords);
        List<SourceResponse> sources = getSources(hearit.getSources());
        return new HearitDetailResponse(
                hearit.getId(),
                hearit.getTitle(),
                hearit.getSummary(),
                sources,
                hearit.getPlayTime(),
                hearit.getCreatedAt(),
                true,
                bookmark.getId(),
                CategoryResponse.of(hearit.getCategory()),
                keywordNames);
    }

    private static List<KeywordResponse> getKeywordNames(List<Keyword> keywords) {
        return keywords.stream().map(KeywordResponse::from).toList();
    }

    private static List<SourceResponse> getSources(List<Source> sources) {
        return sources.stream().map(SourceResponse::from).toList();
    }

    public record CategoryResponse(
            Long id,
            String name,
            String colorCode
    ) {

        private static CategoryResponse of(Category category) {
            return new CategoryResponse(category.getId(), category.getName(), category.getColorCode());
        }
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

    public record SourceResponse(
            String sourceName,
            String sourceUrl
    ) {

        private static SourceResponse from(Source source) {
            return new SourceResponse(
                    source.getSourceName(),
                    source.getSourceUrl()
            );
        }
    }
}
