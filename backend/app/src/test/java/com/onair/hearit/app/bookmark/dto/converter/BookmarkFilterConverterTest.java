package com.onair.hearit.app.bookmark.dto.converter;

import static org.assertj.core.api.Assertions.assertThat;

import com.onair.hearit.app.bookmark.dto.param.BookmarkFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class BookmarkFilterConverterTest {

    private BookmarkFilterConverter converter = new BookmarkFilterConverter();

    @ParameterizedTest
    @CsvSource({
            "UNFINISHED, UNFINISHED",
            "unfinished, UNFINISHED",
            "ALL, ALL",
            "all, ALL"})
    @DisplayName("문자열과 일치하는 BookmarkFilter를 반환한다.")
    void convertSuccessTest(String queryParam, BookmarkFilter expectedBookmarkFilter) {
        // when
        BookmarkFilter actualBookmarkFilter = converter.convert(queryParam);

        // then
        assertThat(actualBookmarkFilter).isEqualTo(expectedBookmarkFilter);
    }
}
