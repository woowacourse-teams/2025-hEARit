package com.onair.hearit.core.infrastructure.elasticsearch.event;

import com.onair.hearit.core.domain.Category;
import com.onair.hearit.core.domain.Hearit;
import com.onair.hearit.core.domain.Keyword;
import java.time.LocalDate;
import java.util.List;

public record HearitUpsertEvent(
        Long id,
        String title,
        String summary,
        List<String> keywords,
        String category,
        LocalDate createdAt
) {

    public static HearitUpsertEvent of(Hearit hearit, Category category, List<Keyword> keywords) {
        List<String> keywordNames = keywords.stream()
                .map(Keyword::getName)
                .toList();

        return new HearitUpsertEvent(
                hearit.getId(),
                hearit.getTitle(),
                hearit.getSummary(),
                keywordNames,
                category.getName(),
                hearit.getCreatedAt().toLocalDate());
    }
}
