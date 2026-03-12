package com.onair.hearit.app.explore.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.onair.hearit.app.exception.custom.InvalidInputException;
import com.onair.hearit.app.explore.application.ExploreCursorCodec;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ExploreCursorTest {

    @DisplayName("encode 후 decode로 복원하면 원래 값이 복원된다")
    @Test
    void encodeAndDecodeRoundTrip() {
        // given
        ExploreCursor cursor = new ExploreCursor(85.5, 42L);

        // when
        String encoded = ExploreCursorCodec.encode(cursor);
        ExploreCursor decoded = ExploreCursorCodec.decode(encoded);

        // then
        assertAll(
                () -> assertThat(decoded.score()).isEqualTo(85.5),
                () -> assertThat(decoded.hearitId()).isEqualTo(42L)
        );
    }

    @DisplayName("null 또는 빈 문자열로 decode 호출하면 초기 커서를 반환한다")
    @Test
    void decodeNullOrBlankReturnsInitial() {
        assertAll(
                () -> assertThat(ExploreCursorCodec.decode(null).isInitial()).isTrue(),
                () -> assertThat(ExploreCursorCodec.decode("").isInitial()).isTrue(),
                () -> assertThat(ExploreCursorCodec.decode("  ").isInitial()).isTrue()
        );
    }

    @DisplayName("유효한 커서 문자열로 decode 호출하면 초기 커서가 아니다")
    @Test
    void decodeValidCursorIsNotInitial() {
        String encoded = ExploreCursorCodec.encode(new ExploreCursor(10.0, 1L));
        assertThat(ExploreCursorCodec.decode(encoded).isInitial()).isFalse();
    }

    @DisplayName("initial()은 MAX_VALUE 기반 커서를 반환한다")
    @Test
    void initialReturnsMaxValues() {
        ExploreCursor cursor = ExploreCursor.initial();
        assertAll(
                () -> assertThat(cursor.score()).isEqualTo(Double.MAX_VALUE),
                () -> assertThat(cursor.hearitId()).isEqualTo(Long.MAX_VALUE),
                () -> assertThat(cursor.isInitial()).isTrue()
        );
    }

    @DisplayName("잘못된 Base64 문자열은 예외를 발생시킨다")
    @Test
    void decodeInvalidBase64ThrowsException() {
        assertThatThrownBy(() -> ExploreCursorCodec.decode("not-valid-base64!!!"))
                .isInstanceOf(InvalidInputException.class);
    }
}
