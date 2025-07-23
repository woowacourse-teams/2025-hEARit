package com.onair.hearit.dto.request;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.onair.hearit.common.exception.custom.InvalidInputException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TitleSearchConditionTest {

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
                () -> assertThatThrownBy(() -> new TitleSearchCondition(validSearchTerm, negativePage, validSize))
                        .isInstanceOf(InvalidInputException.class),
                () -> assertThatThrownBy(() -> new TitleSearchCondition(validSearchTerm, validPage, negativeSize))
                        .isInstanceOf(InvalidInputException.class),
                () -> assertThatThrownBy(() -> new TitleSearchCondition(validSearchTerm, validPage, oversizedSize))
                        .isInstanceOf(InvalidInputException.class)
        );
    }
}
