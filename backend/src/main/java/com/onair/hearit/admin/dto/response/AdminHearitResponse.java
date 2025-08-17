package com.onair.hearit.admin.dto.response;

import com.onair.hearit.common.domain.Hearit;
import com.onair.hearit.common.domain.Source;
import java.time.LocalDateTime;
import java.util.List;

public record AdminHearitResponse(
        Long id,
        String title,
        String summary,
        String originalAudioUrl,
        String shortAudioUrl,
        String scriptUrl,
        List<SourceInHearit> sources,
        Integer playTime,
        LocalDateTime createdAt,
        AdminCategoryResponse category,
        List<KeywordInHearit> keywords
) {
    public static AdminHearitResponse from(Hearit hearit, List<KeywordInHearit> keywords) {
        List<SourceInHearit> sources = getSources(hearit.getSources());
        return new AdminHearitResponse(
                hearit.getId(),
                hearit.getTitle(),
                hearit.getSummary(),
                hearit.getOriginalAudioUrl(),
                hearit.getShortAudioUrl(),
                hearit.getScriptUrl(),
                sources,
                hearit.getPlayTime(),
                hearit.getCreatedAt(),
                AdminCategoryResponse.from(hearit.getCategory()),
                keywords
        );
    }

    private static List<SourceInHearit> getSources(List<Source> sources) {
        return sources.stream().map(SourceInHearit::from).toList();
    }

    public record KeywordInHearit(
            String name
    ) {
    }

    public record SourceInHearit(
            String sourceName,
            String sourceUrl
    ) {
        public static SourceInHearit from(Source source) {
            return new SourceInHearit(
                    source.getSourceName(),
                    source.getSourceUrl()
            );
        }
    }
}
