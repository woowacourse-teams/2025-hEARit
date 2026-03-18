package com.onair.hearit.core.infrastructure.elasticsearch.domain;

import co.elastic.clients.elasticsearch._types.SortOrder;
import java.util.Arrays;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;

public enum HearitSearchSortField {

    OLDEST("oldest") {
        @Override
        public void applySort(NativeQueryBuilder builder) {
            builder.withSort(s -> s.field(f -> f.field(CREATED_AT_FIELD).order(SortOrder.Asc)));
        }
    },
    LATEST("latest") {
        @Override
        public void applySort(NativeQueryBuilder builder) {
            builder.withSort(s -> s.field(f -> f.field(CREATED_AT_FIELD).order(SortOrder.Desc)));
        }
    },
    // TODO: Recommend 조회수, 좋아요 등 추가 반영 필요
    RECOMMENDED("recommend") {
        @Override
        public void applySort(NativeQueryBuilder builder) {
            builder.withSort(s -> s.score(sc -> sc.order(SortOrder.Desc)));
            builder.withSort(s -> s.field(f -> f.field(CREATED_AT_FIELD).order(SortOrder.Desc)));
        }
    },
    ACCURACY("accuracy") {
        @Override
        public void applySort(NativeQueryBuilder builder) {
            builder.withSort(s -> s.score(sc -> sc.order(SortOrder.Desc)));
            builder.withSort(s -> s.field(f -> f.field(CREATED_AT_FIELD).order(SortOrder.Desc)));
        }
    },
    ;

    private static final String CREATED_AT_FIELD = "created_at";
    private final String field;

    public abstract void applySort(NativeQueryBuilder builder);

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
