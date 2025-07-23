package com.onair.hearit.dto.request;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.onair.hearit.common.exception.custom.InvalidPageException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SearchConditionTest {

    @Test
    @DisplayName("page, size가 유효 범위를 벗어나면 예외가 발생한다.")
    void throwExceptionWhenPageOrSizeIsOutOfRange() {
        // given
        String validSearchTerm = "searchTerm";

        int negativePage = -1;
        int negativeSize = -5;
        int oversizedSize = 100;

        int validPage = 0;
        int validSize = 10;

        // when & then
        assertAll(
                () -> assertThatThrownBy(() -> new SearchCondition(validSearchTerm, negativePage, validSize))
                        .isInstanceOf(InvalidPageException.class),
                () -> assertThatThrownBy(() -> new SearchCondition(validSearchTerm, validPage, negativeSize))
                        .isInstanceOf(InvalidPageException.class),
                () -> assertThatThrownBy(() -> new SearchCondition(validSearchTerm, validPage, oversizedSize))
                        .isInstanceOf(InvalidPageException.class)
        );
    }
}
