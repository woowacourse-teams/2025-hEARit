package com.onair.hearit.app.explore.application.scorefactor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.onair.hearit.core.domain.Category;
import com.onair.hearit.core.domain.Hearit;
import com.onair.hearit.core.domain.Source;
import com.onair.hearit.core.fixture.TestFixture;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RandomScoreFactorTest {

    @DisplayName("랜덤 점수는 0 이상 10 이하의 범위를 만족한다.")
    @Test
    void calculateRandomScores() {
        // given
        DefaultRandomNumberGenerator randomNumberGenerator = new DefaultRandomNumberGenerator();
        RandomScoreFactor randomScoreFactor = new RandomScoreFactor(randomNumberGenerator);
        Category category = TestFixture.createFixedCategory();
        List<Hearit> hearits = new ArrayList<>();
        for (long id = 1; id <= 5; id++) {
            hearits.add(createHearitWith(id, category, LocalDateTime.now()));
        }

        // when
        Map<Long, Double> scores = randomScoreFactor.calculate(UUID.randomUUID().toString(), hearits);

        // then
        assertAll(
                () -> assertThat(scores).hasSize(hearits.size()),
                () -> assertThat(scores.values()).allMatch(value -> value >= 0.0 && value <= 10.0)
        );
    }

    @DisplayName("랜덤 생성기가 0.1을 반환할 때, 모든 점수는 1.0이 된다.")
    @Test
    void calculateRandomScores_withFixedGenerator() {
        // given
        RandomNumberGenerator fixedGenerator = () -> 0.1d;
        RandomScoreFactor randomScoreFactor = new RandomScoreFactor(fixedGenerator);
        Category category = TestFixture.createFixedCategory();
        List<Hearit> hearits = new ArrayList<>();
        hearits.add(createHearitWith(1, category, LocalDateTime.now()));
        hearits.add(createHearitWith(2, category, LocalDateTime.now()));

        // when
        Map<Long, Double> scores = randomScoreFactor.calculate(UUID.randomUUID().toString(), hearits);

        // then
        assertAll(
                () -> assertThat(scores).hasSize(hearits.size()),
                () -> assertThat(scores.values()).allSatisfy(score -> assertThat(score).isEqualTo(1.0))
                // 0.1 * 10 = 1.0
        );
    }

    private Hearit createHearitWith(long id, Category category, LocalDateTime createdAt) {
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
