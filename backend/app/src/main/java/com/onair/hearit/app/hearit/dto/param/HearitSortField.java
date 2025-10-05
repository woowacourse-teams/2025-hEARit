package com.onair.hearit.app.hearit.dto.param;

import com.onair.hearit.app.exception.custom.InvalidInputException;
import java.util.Arrays;

public enum HearitSortField {
    CREATED_AT("createdAt"),
    TITLE("title");

    private final String field;

    HearitSortField(String field) {
        this.field = field;
    }

    public String getField() {
        return field;
    }

    public static HearitSortField from(String value) {
        return Arrays.stream(values())
                .filter(f -> f.field.equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new InvalidInputException("지원하지 않는 정렬 필드입니다."));
    }
}
