package com.onair.hearit.dto.response;

import com.onair.hearit.domain.Hearit;
import com.onair.hearit.domain.Keyword;
import java.util.List;

public record HearitSearchResponse(
        Long id,
        String title,
        Integer playTime,
        List<KeywordResponse> keywords
) {
    public static HearitSearchResponse from(Hearit hearit, List<Keyword> keywords) {
        List<KeywordResponse> keywordResponses = getKeywordNames(keywords);
        return new HearitSearchResponse(
                hearit.getId(),
                hearit.getTitle(),
                hearit.getPlayTime(),
                keywordResponses
        );
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
