package com.onair.hearit.admin.dto.response;

import com.onair.hearit.domain.Hearit;
import java.time.LocalDate;
import java.util.List;

public record MonthlyRecommendedHearitResponse(
        LocalDate recommendDate,
        List<RecommendHearitSimpleResponse> recommendHearits
) {
    public static MonthlyRecommendedHearitResponse from(LocalDate recommendDate, List<Hearit> recommendHearits) {
        List<RecommendHearitSimpleResponse> simpleResponses = recommendHearits.stream()
                .map(RecommendHearitSimpleResponse::from).toList();
        return new MonthlyRecommendedHearitResponse(recommendDate, simpleResponses);
    }

    record RecommendHearitSimpleResponse(
            String title,
            String summary,
            String categoryName
    ) {
        static RecommendHearitSimpleResponse from(Hearit hearit) {
            return new RecommendHearitSimpleResponse(hearit.getTitle(), hearit.getSummary(),
                    hearit.getCategory().getName());
        }
    }
}
