package com.onair.hearit.app.hearit.dto.param;

import com.onair.hearit.app.exception.custom.InvalidInputException;
import java.util.Arrays;
import lombok.Getter;

@Getter
public enum HearitSortField {
    CREATED_AT("createdAt"),
    ;

    private final String field;

    HearitSortField(String field) {
        this.field = field;
    }

    public static HearitSortField from(String value) {
        return Arrays.stream(values())
                .filter(f -> f.field.equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new InvalidInputException("지원하지 않는 정렬 필드입니다."));
    }
}
