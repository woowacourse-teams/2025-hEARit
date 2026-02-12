package com.onair.hearit.core.infrastructure.elasticsearch.domain;

import java.util.Arrays;

public enum HearitSearchSortField {

    OLDEST("oldest"),
    LATEST("latest"),
    RECOMMENDED("recommend"),
    ACCURACY("accuracy");

    private final String field;

    HearitSearchSortField(String field) {
        this.field = field;
    }

    public static HearitSearchSortField from(String value) {
        return Arrays.stream(values())
                .filter(f -> f.field.equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("지원하지 않는 정렬 필드입니다."));
    }
}
