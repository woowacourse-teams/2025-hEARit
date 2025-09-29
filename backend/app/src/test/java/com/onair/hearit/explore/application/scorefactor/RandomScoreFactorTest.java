package com.onair.hearit.explore.application.scorefactor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.onair.hearit.core.fixture.TestFixture;
import com.onair.hearit.core.fixture.TestJpaAuditingConfig;
import com.onair.hearit.domain.Category;
import com.onair.hearit.domain.Hearit;
import com.onair.hearit.fixture.DbHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@Import({DbHelper.class, TestJpaAuditingConfig.class})
@ActiveProfiles("fake-test")
class RandomScoreFactorTest {

    @Autowired
    private DbHelper dbHelper;

    private RandomScoreFactor randomScoreFactor;

    @BeforeEach
    void setup() {
        randomScoreFactor = new RandomScoreFactor(() -> 0.1d);
    }

    @DisplayName("랜덤 팩터는 0 이상 10 이하의 점수를 반환한며 고정된 값을 반환할 수 있다.")
    @Test
    void calculateRandomScores() {
        Category category = dbHelper.insertCategory(new Category("red", "#112233"));
        List<Hearit> hearits = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            Hearit hearit = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
            hearits.add(hearit);
        }

        Map<Long, Double> scores = randomScoreFactor.calculate(UUID.randomUUID().toString(), hearits);

        assertAll(
                () -> assertThat(scores).hasSize(hearits.size()),
                () -> assertThat(scores.values()).allMatch(value -> value >= 0.0 && value <= 10.0),
                () -> assertThat(scores.values()).allSatisfy(score -> assertThat(score).isEqualTo(1.0))
        );
    }
}
