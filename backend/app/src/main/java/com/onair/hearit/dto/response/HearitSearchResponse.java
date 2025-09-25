package com.onair.hearit.dto.response;

import com.onair.hearit.domain.Hearit;
import com.onair.hearit.domain.Keyword;
import com.onair.hearit.domain.PlayingHistory;
import java.util.List;

public record HearitSearchResponse(
        Long id,
        String title,
        Integer playTime,
        Long lastPlayTime,
        Boolean isFinished,
        List<KeywordResponse> keywords
) {
    private static final int KEYWORD_PER_HEARIT = 3;

    public static HearitSearchResponse of(Hearit hearit, List<Keyword> keywords, PlayingHistory playingHistory) {
        List<KeywordResponse> keywordResponses = getKeywordNames(keywords);
        return new HearitSearchResponse(
                hearit.getId(),
                hearit.getTitle(),
                hearit.getPlayTime(),
                playingHistory != null ? playingHistory.getLastPlayTime() : null,
                playingHistory != null ? playingHistory.isFinished() : null,
                keywordResponses
        );
    }

    private static List<KeywordResponse> getKeywordNames(List<Keyword> keywords) {
        return keywords.stream().limit(KEYWORD_PER_HEARIT).map(KeywordResponse::from).toList();
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
