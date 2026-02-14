package com.onair.hearit.app.explore.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ExploreCursorTest {

    @DisplayName("encode 후 decode하면 원래 값이 복원된다")
    @Test
    void encodeAndDecodeRoundTrip() {
        // given
        ExploreCursor cursor = new ExploreCursor(85.5, 42L);

        // when
        String encoded = cursor.encode();
        ExploreCursor decoded = ExploreCursor.decode(encoded);

        // then
        assertAll(
                () -> assertThat(decoded.score()).isEqualTo(85.5),
                () -> assertThat(decoded.hearitId()).isEqualTo(42L)
        );
    }

    @DisplayName("null 또는 빈 문자열은 초기 요청으로 판별한다")
    @Test
    void isInitialRequest() {
        assertAll(
                () -> assertThat(ExploreCursor.isInitialRequest(null)).isTrue(),
                () -> assertThat(ExploreCursor.isInitialRequest("")).isTrue(),
                () -> assertThat(ExploreCursor.isInitialRequest("  ")).isTrue()
        );
    }

    @DisplayName("유효한 커서 문자열은 초기 요청이 아니다")
    @Test
    void isNotInitialRequest() {
        String encoded = new ExploreCursor(10.0, 1L).encode();
        assertThat(ExploreCursor.isInitialRequest(encoded)).isFalse();
    }

    @DisplayName("잘못된 Base64 문자열은 예외를 발생시킨다")
    @Test
    void decodeInvalidBase64ThrowsException() {
        assertThatThrownBy(() -> ExploreCursor.decode("not-valid-base64!!!"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
