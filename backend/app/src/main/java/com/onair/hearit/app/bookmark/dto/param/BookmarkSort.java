package com.onair.hearit.app.bookmark.dto.param;

import java.util.Arrays;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class BookmarkSort {

    private final BookmarkSortType type;
    private final BookmarkSortDirection direction;

    public static BookmarkSort from(String typeSource) {
        BookmarkSortType type = BookmarkSortType.from(typeSource);
        return new BookmarkSort(type, BookmarkSortDirection.getDefault());
    }

    public static BookmarkSort of(String typeSource, String directionSource) {
        BookmarkSortType type = BookmarkSortType.from(typeSource);
        BookmarkSortDirection direction = BookmarkSortDirection.from(directionSource);
        return new BookmarkSort(type, direction);
    }

    @RequiredArgsConstructor
    public enum BookmarkSortType {

        CREATED_AT("createdAt"),
        ;

        private final String name;

        static BookmarkSortType from(String typeSource) {
            return Arrays.stream(values())
                    .filter(value -> value.name.equalsIgnoreCase(typeSource))
                    .findFirst()
                    .orElseThrow(IllegalArgumentException::new);
        }
    }

    @RequiredArgsConstructor
    public enum BookmarkSortDirection {

        ASC("asc"),
        DESC("desc"),
        ;

        private final String name;

        static BookmarkSortDirection getDefault() {
            return DESC;
        }

        static BookmarkSortDirection from(String directionSource) {
            return Arrays.stream(values())
                    .filter(value -> value.name.equalsIgnoreCase(directionSource))
                    .findFirst()
                    .orElseThrow(IllegalArgumentException::new);
        }
    }
}
