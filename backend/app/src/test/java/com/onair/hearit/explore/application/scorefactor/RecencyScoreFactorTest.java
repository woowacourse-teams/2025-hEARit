package com.onair.hearit.explore.application.scorefactor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.onair.hearit.common.TestClock;
import com.onair.hearit.core.fixture.TestFixture;
import com.onair.hearit.core.fixture.TestJpaAuditingConfig;
import com.onair.hearit.domain.Category;
import com.onair.hearit.domain.Hearit;
import com.onair.hearit.domain.Source;
import com.onair.hearit.explore.application.TestConfig;
import com.onair.hearit.fixture.DbHelper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

@DataJpaTest
@Sql("/dbclean.sql")
@Import({DbHelper.class, TestConfig.class, TestJpaAuditingConfig.class})
@ActiveProfiles("integration-test")
@AutoConfigureTestDatabase(replace = Replace.NONE)
class RecencyScoreFactorTest {


    @Autowired
    private DbHelper dbHelper;

    private final RecencyScoreFactor recencyScoreFactor = new RecencyScoreFactor();

    @AfterEach
    void tearDown() {
        TestClock.unfreeze();
    }

    @DisplayName("최근 히어릿일수록 더 높은 최신성 점수를 부여한다")
    @Test
    void calculateRecencyScores() {
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        TestClock.freezeAt(LocalDateTime.now());
        Hearit latest = dbHelper.insertHearit(createHearit(category));
        TestClock.freezeAt(LocalDateTime.now().minusDays(4));
        Hearit medium = dbHelper.insertHearit(createHearit(category));
        TestClock.freezeAt(LocalDateTime.now().minusDays(60));
        Hearit old = dbHelper.insertHearit(createHearit(category));

        List<Hearit> hearits = List.of(latest, medium, old);

        Map<Long, Double> scores = recencyScoreFactor.calculate("ignored", hearits);

        assertAll(
                () -> assertThat(scores).hasSize(hearits.size()),
                () -> assertThat(scores.get(latest.getId())).isEqualTo(20.0),
                () -> assertThat(scores.get(medium.getId())).isEqualTo(18.0),
                () -> assertThat(scores.get(old.getId())).isEqualTo(0.0)
        );
    }

    private Hearit createHearit(Category category) {
        return new Hearit(
                "title",
                "summary",
                500,
                "/hearit/audio/original/ORG_bf7c513e-579e-4224-8505-3824bb22ed01.mp3",
                "/hearit/audio/short/SHR_bf7c513e-579e-4224-8505-3824bb22ed01.mp3",
                "/hearit/script/SCR_bf7c513e-579e-4224-8505-3824bb22ed01.json",
                List.of(new Source("이 컨텐츠는 쿠버네티스 공식 문서 (저작자: The Kubernetes Authors)를 참고하여 만들어졌습니다.",
                        "https://example.com/1")),
                category
        );
    }
}
