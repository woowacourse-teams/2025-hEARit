package com.onair.hearit.hearit.dto;

import com.onair.hearit.domain.Hearit;
import com.onair.hearit.domain.Keyword;
import java.util.List;

public record HearitOfCategoryResponse(
        Long id,
        String title,
        Integer playTime,
        Long lastPlayTime,
        List<KeywordResponse> keywords
) {
    public static HearitOfCategoryResponse from(Hearit hearit, List<Keyword> keywords, Long lastPlayTime) {
        List<KeywordResponse> keywordResponses = getKeywordNames(keywords);
        return new HearitOfCategoryResponse(
                hearit.getId(),
                hearit.getTitle(),
                hearit.getPlayTime(),
                lastPlayTime,
                keywordResponses
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
}
