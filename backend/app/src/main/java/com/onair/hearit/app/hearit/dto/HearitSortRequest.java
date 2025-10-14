package com.onair.hearit.app.hearit.dto;

import com.onair.hearit.app.exception.custom.InvalidInputException;
import com.onair.hearit.app.hearit.dto.param.HearitSortField;
import com.onair.hearit.app.hearit.dto.param.SortDirection;
import org.springframework.data.domain.Sort;

public record HearitSortRequest(HearitSortField field, SortDirection direction) {

    public HearitSortRequest {
        // direction's default value = desc
        if (field == null) {
            throw new InvalidInputException("정렬 필드가 필요합니다.");
        }
    }

    public Sort toSort() {
        return Sort.by(direction.getDirection(), field.getField());
    }
}
