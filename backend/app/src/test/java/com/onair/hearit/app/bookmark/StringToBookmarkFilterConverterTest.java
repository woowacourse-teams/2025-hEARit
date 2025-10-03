package com.onair.hearit.app.bookmark;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class StringToBookmarkFilterConverterTest {

    private StringToBookmarkFilterConverter converter = new StringToBookmarkFilterConverter();

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
