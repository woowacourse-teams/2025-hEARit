package com.onair.hearit.app.bookmark.dto.param;

import java.util.Arrays;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

@Getter
@RequiredArgsConstructor
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

    public static List<String> getAllPossibleName() {
        return Arrays.stream(BookmarkSortType.values())
                .flatMap(type -> Arrays.stream(BookmarkSortDirection.values())
                        .map(direction -> type.name + "," + direction.name))
                .toList();
    }

    public Sort toSort() {
        return Sort.by(direction.direction, type.name);
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

    @Getter
    @RequiredArgsConstructor
    public enum BookmarkSortDirection {

        ASC("asc", Direction.ASC),
        DESC("desc", Direction.DESC),
        ;

        private final String name;
        private final Direction direction;

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
