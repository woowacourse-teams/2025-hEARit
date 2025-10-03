package com.onair.hearit.app.hearit.dto;

import org.springframework.data.domain.Sort;

public record HearitSortRequest(HearitSortField field, SortDirection direction) {
    public Sort toSort() {
        return Sort.by(direction.getDirection(), field.getField());
    }
}
