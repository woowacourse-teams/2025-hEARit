package com.onair.hearit.app.hearit.dto.param;

import com.onair.hearit.app.exception.custom.InvalidInputException;
import java.util.Arrays;
import lombok.Getter;
import org.springframework.data.domain.Sort.Direction;

@Getter
public enum SortDirection {
    ASC("asc", Direction.ASC),
    DESC("desc", Direction.DESC);

    private final String sort;
    private final Direction direction;

    SortDirection(String sort, Direction direction) {
        this.sort = sort;
        this.direction = direction;
    }

    public static SortDirection from(String value) {
        return Arrays.stream(values())
                .filter(d -> d.sort.equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new InvalidInputException("지원하지 않는 정렬 기준입니다."));
    }
}
