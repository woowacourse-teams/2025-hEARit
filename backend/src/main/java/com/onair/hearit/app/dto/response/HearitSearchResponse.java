package com.onair.hearit.app.dto.response;

import com.onair.hearit.common.domain.Hearit;
import com.onair.hearit.common.domain.Keyword;
import java.util.List;

public record HearitSearchResponse(
        Long id,
        String title,
        Integer playTime,
        Long lastPlayTime,
        List<KeywordResponse> keywords
) {
    public static HearitSearchResponse of(Hearit hearit, List<Keyword> keywords, Long lastPlayTime) {
        List<KeywordResponse> keywordResponses = getKeywordNames(keywords);
        return new HearitSearchResponse(
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
