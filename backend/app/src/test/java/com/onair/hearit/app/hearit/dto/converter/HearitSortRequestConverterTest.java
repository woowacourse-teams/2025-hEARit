package com.onair.hearit.app.hearit.dto.converter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.onair.hearit.app.exception.custom.InvalidInputException;
import com.onair.hearit.app.hearit.dto.HearitSortRequest;
import com.onair.hearit.app.hearit.dto.param.HearitSortField;
import com.onair.hearit.app.hearit.dto.param.SortDirection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class HearitSortRequestConverterTest {

    HearitSortRequestConverter converter = new HearitSortRequestConverter();

    @Test
    @DisplayName("필드와 방향이 모두 주어진 경우 해당 조건을 반환한다.")
    void convert_success_withFieldAndDirection() {
        // given
        String source = "createdAt,asc";

        // when
        HearitSortRequest result = converter.convert(source);

        // then
        assertAll(
                () -> assertThat(result.field()).isEqualTo(HearitSortField.CREATED_AT),
                () -> assertThat(result.direction()).isEqualTo(SortDirection.ASC)
        );
    }

    @Test
    @DisplayName("방향이 생략되면 기본값은 desc로 처리된다.")
    void convert_success_withDefaultDirection() {
        // given
        String source = "createdAt";

        // when
        HearitSortRequest result = converter.convert(source);

        // then
        assertAll(
                () -> assertThat(result.field()).isEqualTo(HearitSortField.CREATED_AT),
                () -> assertThat(result.direction()).isEqualTo(SortDirection.DESC)
        );
    }

    @Test
    @DisplayName("존재하지 않는 필드 이름을 주면 예외가 발생한다.")
    void convert_fail_invalidField() {
        // given
        String source = "invalidField,asc";

        // when & then
        assertThatThrownBy(() -> converter.convert(source)).isInstanceOf(InvalidInputException.class);
    }

    @Test
    @DisplayName("존재하지 않는 방향을 주면 예외가 발생한다.")
    void convert_fail_invalidDirection() {
        // given
        String source = "createdAt,upward";

        // when & then
        assertThatThrownBy(() -> converter.convert(source)).isInstanceOf(InvalidInputException.class);
    }
}
