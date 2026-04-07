package com.onair.hearit.app.search.dto;

import org.hibernate.validator.constraints.Range;

public record SearchAutocompleteRequest(
        String searchTerm,
        @Range(min = 1, max = 30, message = "자동완성 요청 사이즈는 1에서 30 사이여야 합니다.")
        Integer size
) {

    public SearchAutocompleteRequest {
        if (size == null) {
            size = 5;
        }
    }
}
