package com.onair.hearit.app.bookmark.dto.param;

import java.util.Arrays;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BookmarkFilter {

    UNFINISHED("unfinished"),
    ALL("all"),
    ;

    private final String name;

    public static BookmarkFilter fromName(String queryParam) {
        return Arrays.stream(values())
                .filter(value -> value.name.equalsIgnoreCase(queryParam))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }

    public Boolean isFinished() {
        if (this.equals(ALL)) {
            return null;
        }
        return !this.equals(UNFINISHED);
    }
}
