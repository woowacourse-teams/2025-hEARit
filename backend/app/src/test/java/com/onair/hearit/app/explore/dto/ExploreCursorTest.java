package com.onair.hearit.app.explore.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ExploreCursorTest {

    @DisplayName("encode 후 from으로 복원하면 원래 값이 복원된다")
    @Test
    void encodeAndFromRoundTrip() {
        // given
        ExploreCursor cursor = new ExploreCursor(85.5, 42L);

        // when
        String encoded = cursor.encode();
        ExploreCursor decoded = ExploreCursor.from(encoded);

        // then
        assertAll(
                () -> assertThat(decoded.score()).isEqualTo(85.5),
                () -> assertThat(decoded.hearitId()).isEqualTo(42L)
        );
    }

    @DisplayName("null 또는 빈 문자열로 from 호출하면 초기 커서를 반환한다")
    @Test
    void fromNullOrBlankReturnsInitial() {
        assertAll(
                () -> assertThat(ExploreCursor.from(null).isInitial()).isTrue(),
                () -> assertThat(ExploreCursor.from("").isInitial()).isTrue(),
                () -> assertThat(ExploreCursor.from("  ").isInitial()).isTrue()
        );
    }

    @DisplayName("유효한 커서 문자열로 from 호출하면 초기 커서가 아니다")
    @Test
    void fromValidCursorIsNotInitial() {
        String encoded = new ExploreCursor(10.0, 1L).encode();
        assertThat(ExploreCursor.from(encoded).isInitial()).isFalse();
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
    void fromInvalidBase64ThrowsException() {
        assertThatThrownBy(() -> ExploreCursor.from("not-valid-base64!!!"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
