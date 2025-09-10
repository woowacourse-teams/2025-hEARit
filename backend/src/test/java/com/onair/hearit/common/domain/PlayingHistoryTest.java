package com.onair.hearit.common.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PlayingHistoryTest {

    @Test
    @DisplayName("재생 기록의 마지막 시간은 히어릿의 총 재생 시간보다 작아야 한다")
    void validateHearitPlayTime() {
        // given
        Hearit hearit = createHearitWith(100);

        // when & then
        assertThatThrownBy(() -> new PlayingHistory(1L, hearit, 101_000));
    }

    @Test
    @DisplayName("재생 기록의 마지막 시간이 히어릿의 총 재생 시간과 차이가 10초 이하라면 재생 기록이 완료 상태이다.")
    void checkIsFinished() {
        // given
        Hearit hearit = createHearitWith(100);

        // when
        PlayingHistory playingHistory = new PlayingHistory(1L, hearit, 90_000);

        // then
        assertThat(playingHistory.isFinished()).isTrue();
    }

    private Hearit createHearitWith(int playTime) {
        Category category = new Category("name", "#000000");
        return new Hearit("title",
                "summary",
                playTime,
                "/hearit/audio/original/ORG_bf7c513e-579e-4224-8505-3824bb22ed01.mp3",
                "/hearit/audio/short/SHR_bf7c513e-579e-4224-8505-3824bb22ed01.mp3",
                "/hearit/script/SCR_bf7c513e-579e-4224-8505-3824bb22ed01.json",
                List.of(new Source("원본은 CC BY 4.0 라이선스를 따릅니다.", "https://example.com/2")),
                category
        );
    }
}
