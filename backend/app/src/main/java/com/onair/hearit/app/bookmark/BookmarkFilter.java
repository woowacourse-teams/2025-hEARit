package com.onair.hearit.app.bookmark;

import java.util.Arrays;

public enum BookmarkFilter {

    UNFINISHED("unfinished"),
    ALL("all"),
    ;

    private final String name;

    BookmarkFilter(String name) {
        this.name = name;
    }

    public static BookmarkFilter fromName(String queryParam) {
        return Arrays.stream(values())
                .filter(value -> value.name.equalsIgnoreCase(queryParam))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }

    public String getName() {
        return name;
    }
}
