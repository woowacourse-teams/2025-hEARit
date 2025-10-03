package com.onair.hearit.app.bookmark;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class BookmarkFilterTest {

    @ParameterizedTest
    @CsvSource({
            "UNFINISHED, UNFINISHED",
            "unfinished, UNFINISHED",
            "ALL, ALL",
            "all, ALL"})
    @DisplayName("문자열과 일치하는 Bookmark filter를 반환한다.")
    void fromName_success(String queryParam, BookmarkFilter expectedBookmarkFilter) {
        // when
        BookmarkFilter actualBookmarkFilter = BookmarkFilter.fromName(queryParam);

        // then
        assertThat(actualBookmarkFilter).isEqualTo(expectedBookmarkFilter);
    }

    @Test
    @DisplayName("문자열과 일치하는 Bookmark filter가 없으면 IllegalArgumentException을 던진다.")
    void fromName_exception() {
        // given
        String invalidQueryParam = "invalid-query-param";

        // when and then
        assertThatThrownBy(() -> BookmarkFilter.fromName(invalidQueryParam))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
