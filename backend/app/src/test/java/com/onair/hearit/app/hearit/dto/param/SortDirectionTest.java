package com.onair.hearit.app.hearit.dto.param;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.onair.hearit.app.exception.custom.InvalidInputException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SortDirectionTest {

    @Test
    @DisplayName("정렬 방향 변환 성공 - 대소문자 무관하게 asc 입력 시 ASC 반환")
    void from_Success() {
        // when
        SortDirection result = SortDirection.from("asc");

        // then
        assertThat(result).isEqualTo(SortDirection.ASC);
    }

    @Test
    @DisplayName("정렬 방향 변환 실패 - 지원하지 않는 값 입력 시 예외 발생")
    void from_Fail() {
        // when & then
        assertThatThrownBy(() -> SortDirection.from("up")).isInstanceOf(InvalidInputException.class);
    }
}
