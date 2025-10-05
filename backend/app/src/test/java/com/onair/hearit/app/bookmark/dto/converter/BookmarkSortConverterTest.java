package com.onair.hearit.app.bookmark.dto.converter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.onair.hearit.app.bookmark.dto.param.BookmarkSort;
import com.onair.hearit.app.bookmark.dto.param.BookmarkSort.BookmarkSortDirection;
import com.onair.hearit.app.bookmark.dto.param.BookmarkSort.BookmarkSortType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class BookmarkSortConverterTest {

    private BookmarkSortConverter converter = new BookmarkSortConverter();

    @Test
    @DisplayName("sort type을 BookmarkSort로 변환한다.")
    void convertBookmarkSort_fromType() {
        // given
        String input = "createdAt,   ";

        // when
        BookmarkSort actual = converter.convert(input);

        // then
        assertAll(
                () -> assertThat(actual.getDirection()).isEqualTo(BookmarkSortDirection.DESC),
                () -> assertThat(actual.getType()).isEqualTo(BookmarkSortType.CREATED_AT)
        );
    }

    @ParameterizedTest
    @CsvSource({
            "createdAt, desc, CREATED_AT, DESC",
            "createdAt, asc, CREATED_AT, ASC"})
    @DisplayName("sort type, sort direction을 BookmarkSort로 변환한다.")
    void convertBookmarkSort_fromTypeAndDirection(String type, String direction,
                                                  BookmarkSortType expectedType,
                                                  BookmarkSortDirection expectedDirection) {
        // given
        String source = type + "," + direction;

        // when
        BookmarkSort actual = converter.convert(source);

        // then
        assertAll(
                () -> assertThat(actual.getType()).isEqualTo(expectedType),
                () -> assertThat(actual.getDirection()).isEqualTo(expectedDirection));
    }
}
