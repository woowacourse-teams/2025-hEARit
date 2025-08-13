package com.onair.hearit.admin.dto.response;

import com.onair.hearit.domain.Hearit;
import java.time.LocalDate;
import java.util.List;

public record MonthlyRecommendHearitResponse(
        LocalDate recommendDate,
        List<RecommendHearitInfoResponse> recommendHearits
) {
    public static MonthlyRecommendHearitResponse from(LocalDate recommendDate, List<Hearit> recommendHearits) {
        List<RecommendHearitInfoResponse> simpleResponses = recommendHearits.stream()
                .map(RecommendHearitInfoResponse::from)
                .toList();
        return new MonthlyRecommendHearitResponse(recommendDate, simpleResponses);
    }

    record RecommendHearitInfoResponse(
            String title,
            String summary,
            String categoryName
    ) {
        static RecommendHearitInfoResponse from(Hearit hearit) {
            return new RecommendHearitInfoResponse(
                    hearit.getTitle(),
                    hearit.getSummary(),
                    hearit.getCategory().getName());
        }
    }
}
