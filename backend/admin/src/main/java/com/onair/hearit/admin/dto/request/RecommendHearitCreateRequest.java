package com.onair.hearit.admin.dto.request;

import java.time.LocalDate;
import java.util.List;

public record RecommendHearitCreateRequest(
        LocalDate recommendDate,
        List<Long> hearitIds
) {
}
