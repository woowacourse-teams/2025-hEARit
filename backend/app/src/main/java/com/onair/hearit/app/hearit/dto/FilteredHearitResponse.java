package com.onair.hearit.app.hearit.dto;

import com.onair.hearit.core.domain.Category;
import com.onair.hearit.core.domain.Hearit;
import com.onair.hearit.core.domain.Keyword;
import java.time.LocalDateTime;
import java.util.List;

public record FilteredHearitResponse(
        Long id,
        String title,
        Integer playTime,
        Long lastPlayTime,
        LocalDateTime createdAt,
        List<KeywordResponse> keywords,
        CategoryResponse category
) {
    public static FilteredHearitResponse from(Hearit hearit, List<Keyword> keywords, Long lastPlayTime) {
        List<KeywordResponse> keywordResponses = getKeywordNames(keywords);
        return new FilteredHearitResponse(
                hearit.getId(),
                hearit.getTitle(),
                hearit.getPlayTime(),
                lastPlayTime,
                hearit.getCreatedAt(),
                keywordResponses,
                CategoryResponse.from(hearit.getCategory())
        );
    }

    private static List<KeywordResponse> getKeywordNames(List<Keyword> keywords) {
        return keywords.stream().map(KeywordResponse::from).toList();
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

    public record CategoryResponse(Long id, String name, String colorCode) {
        public static CategoryResponse from(Category category) {
            return new CategoryResponse(category.getId(), category.getName(), category.getColorCode());
        }
    }
}
