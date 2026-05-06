package com.onair.hearit.app.search.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.Range;

public record SearchAutocompleteRequest(
        @NotBlank(message = "검색어는 필수입니다.")
        @Size(max = 50, message = "검색어는 50자 이하여야 합니다.")
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
