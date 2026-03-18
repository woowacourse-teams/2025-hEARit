package com.onair.hearit.app.search.dto;

import com.onair.hearit.app.exception.custom.InvalidInputException;
import com.onair.hearit.core.infrastructure.elasticsearch.domain.HearitSearchSortField;

public record SearchSortRequest(HearitSearchSortField field) {

    public SearchSortRequest {
        if (field == null) {
            throw new InvalidInputException("정렬 필드가 필요합니다.");
        }
    }
}
