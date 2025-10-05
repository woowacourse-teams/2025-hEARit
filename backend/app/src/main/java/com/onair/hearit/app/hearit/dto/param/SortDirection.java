package com.onair.hearit.app.hearit.dto.param;

import com.onair.hearit.app.exception.custom.InvalidInputException;
import java.util.Arrays;
import org.springframework.data.domain.Sort.Direction;

public enum SortDirection {
    ASC(Direction.ASC),
    DESC(Direction.DESC);

    private final Direction direction;

    SortDirection(Direction direction) {
        this.direction = direction;
    }

    public Direction getDirection() {
        return direction;
    }

    public static SortDirection from(String value) {
        return Arrays.stream(values())
                .filter(d -> d.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new InvalidInputException("지원하지 않는 정렬 기준입니다."));
    }
}
