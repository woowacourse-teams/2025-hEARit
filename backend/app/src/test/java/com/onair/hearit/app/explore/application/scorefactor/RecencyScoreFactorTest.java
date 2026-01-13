package com.onair.hearit.app.explore.application.scorefactor;

import static org.assertj.core.api.Assertions.assertThat;

import com.onair.hearit.core.domain.Category;
import com.onair.hearit.core.domain.Hearit;
import com.onair.hearit.core.domain.Source;
import com.onair.hearit.core.fixture.TestFixture;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("RecencyScoreFactor는")
class RecencyScoreFactorTest {

    private RecencyScoreFactor recencyScoreFactor;

    @BeforeEach
    void setUp() {
        recencyScoreFactor = new RecencyScoreFactor();
    }

    @Test
    @DisplayName("당일 생성된 히어릿은 최대 점수(1.0점)를 받는다.")
    void hearitIsCreatedToday() {
        // given
        LocalDateTime now = LocalDateTime.now();
        Category category = TestFixture.createFixedCategory();
        Hearit hearit = createHearit(1L, category, now);

        // when
        Map<Long, Double> scores = recencyScoreFactor.calculate("ignored", List.of(hearit));

        // then
        assertThat(scores).containsEntry(hearit.getId(), 1.0);
    }

    @Test
    @DisplayName("생성 후 6일이 지난 히어릿은 1점 감소한 0.9점을 받는다 (하루 0.016점 감소).")
    void returnDecreasedScore() {
        // given
        LocalDateTime now = LocalDateTime.now();
        Category category = TestFixture.createFixedCategory();
        Hearit hearit = createHearit(1L, category, now.minusDays(6));

        // when
        Map<Long, Double> scores = recencyScoreFactor.calculate("ignored", List.of(hearit));

        // then
        assertThat(scores).containsEntry(hearit.getId(), 0.9);
    }

    @Test
    @DisplayName("생성 후 60일이 지난 히어릿은 최소 점수(0점)를 받는다.")
    void returnMinScore() {
        // given
        LocalDateTime now = LocalDateTime.now();
        Category category = TestFixture.createFixedCategory();
        Hearit hearit = createHearit(1L, category, now.minusDays(60));

        // when
        Map<Long, Double> scores = recencyScoreFactor.calculate("ignored", List.of(hearit));

        // then
        assertThat(scores).containsEntry(hearit.getId(), 0.0);
    }

    @Test
    @DisplayName("생성 후 60일을 초과한 히어릿도 최소 점수(0점) 이하로 내려가지 않는다.")
    void returnMinScore2() {
        // given
        LocalDateTime now = LocalDateTime.now();
        Category category = TestFixture.createFixedCategory();
        Hearit hearit = createHearit(1L, category, now.minusDays(60));

        // when
        Map<Long, Double> scores = recencyScoreFactor.calculate("ignored", List.of(hearit));

        // then
        assertThat(scores).containsEntry(hearit.getId(), 0.0);
    }

    private Hearit createHearit(long id, Category category, LocalDateTime createdAt) {
        return new Hearit(
                id,
                "title",
                "summary",
                500,
                "/hearit/audio/original/ORG_bf7c513e-579e-4224-8505-3824bb22ed01.mp3",
                "/hearit/audio/short/SHR_bf7c513e-579e-4224-8505-3824bb22ed01.mp3",
                "/hearit/script/SCR_bf7c513e-579e-4224-8505-3824bb22ed01.json",
                List.of(new Source("kubernetes docs", "https://example.com/1")),
                category,
                createdAt
        );
    }
}
