package com.onair.hearit.explore.application.scorefactor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.onair.hearit.core.fixture.TestFixture;
import com.onair.hearit.domain.Category;
import com.onair.hearit.domain.Hearit;
import com.onair.hearit.domain.Source;
import com.onair.hearit.fixture.DbHelper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@Import({DbHelper.class})
@ActiveProfiles("fake-test")
class RecencyScoreFactorTest {

    @Autowired
    private DbHelper dbHelper;

    private final RecencyScoreFactor recencyScoreFactor = new RecencyScoreFactor();

    @DisplayName("최근 히어릿일수록 더 높은 최신성 점수를 부여한다")
    @Test
    void calculateRecencyScores() {
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Hearit latest = dbHelper.insertHearit(createHearitWithTime(LocalDateTime.now(), category));
        Hearit medium = dbHelper.insertHearit(createHearitWithTime(LocalDateTime.now().minusDays(4), category));
        Hearit old = dbHelper.insertHearit(createHearitWithTime(LocalDateTime.now().minusDays(60), category));

        List<Hearit> hearits = List.of(latest, medium, old);

        Map<Long, Double> scores = recencyScoreFactor.calculate("ignored", hearits);

        assertAll(
                () -> assertThat(scores).hasSize(hearits.size()),
                () -> assertThat(scores.get(1L)).isEqualTo(20.0),
                () -> assertThat(scores.get(2L)).isEqualTo(18.0),
                () -> assertThat(scores.get(3L)).isEqualTo(0.0)
        );
    }

    private Hearit createHearitWithTime(LocalDateTime createdAt, Category category) {
        return new Hearit(
                "title",
                "summary",
                500,
                "/hearit/audio/original/ORG_bf7c513e-579e-4224-8505-3824bb22ed01.mp3",
                "/hearit/audio/short/SHR_bf7c513e-579e-4224-8505-3824bb22ed01.mp3",
                "/hearit/script/SCR_bf7c513e-579e-4224-8505-3824bb22ed01.json",
                List.of(new Source("이 컨텐츠는 쿠버네티스 공식 문서 (저작자: The Kubernetes Authors)를 참고하여 만들어졌습니다.",
                        "https://example.com/1")),
                category,
                createdAt
        );
    }
}

