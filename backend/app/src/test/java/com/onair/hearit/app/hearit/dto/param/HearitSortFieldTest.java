package com.onair.hearit.app.hearit.dto.param;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.onair.hearit.app.exception.custom.InvalidInputException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class HearitSortFieldTest {

    @Test
    @DisplayName("정렬 필드 변환 성공 - createdAt 입력 시 CREATED_AT 반환")
    void from_Success() {
        // when
        HearitSortField result = HearitSortField.from("createdAt");

        // then
        assertThat(result).isEqualTo(HearitSortField.CREATED_AT);
    }

    @Test
    @DisplayName("정렬 필드 변환 실패 - 지원하지 않는 필드 입력 시 예외 발생")
    void from_Fail() {
        // when & then
        assertThatThrownBy(() -> HearitSortField.from("not-applied")).isInstanceOf(InvalidInputException.class);
    }
}
