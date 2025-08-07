package com.onair.hearit.admin.dto.response;

import com.onair.hearit.domain.Hearit;
import com.onair.hearit.domain.Source;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record HearitAdminResponse(
        Long id,
        String title,
        String summary,
        String originalAudioUrl,
        String shortAudioUrl,
        String scriptUrl,
        List<SourceInHearit> sources,
        Integer playTime,
        LocalDateTime createdAt,
        CategoryInfoResponse category,
        List<KeywordInHearit> keywords
) {
    public static HearitAdminResponse from(Hearit hearit, Map<Long, List<KeywordInHearit>> keywordMap) {
        List<SourceInHearit> sources = getSources(hearit.getSources());
        return new HearitAdminResponse(
                hearit.getId(),
                hearit.getTitle(),
                hearit.getSummary(),
                hearit.getOriginalAudioUrl(),
                hearit.getShortAudioUrl(),
                hearit.getScriptUrl(),
                sources,
                hearit.getPlayTime(),
                hearit.getCreatedAt(),
                CategoryInfoResponse.from(hearit.getCategory()),
                keywordMap.getOrDefault(hearit.getId(), List.of())
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
