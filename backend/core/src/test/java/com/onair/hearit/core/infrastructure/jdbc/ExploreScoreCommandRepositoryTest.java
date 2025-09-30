package com.onair.hearit.core.infrastructure.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.onair.hearit.core.domain.Category;
import com.onair.hearit.core.domain.Hearit;
import com.onair.hearit.core.domain.Member;
import com.onair.hearit.core.fixture.DbHelper;
import com.onair.hearit.core.fixture.TestFixture;
import com.onair.hearit.core.fixture.TestJpaAuditingConfig;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@Import({ExploreScoreCommandRepository.class, DbHelper.class, TestJpaAuditingConfig.class})
@ActiveProfiles("integration-test")
@AutoConfigureTestDatabase(replace = Replace.NONE)
public class ExploreScoreCommandRepositoryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DbHelper dbHelper;

    @Autowired
    private ExploreScoreCommandRepository exploreScoreCommandRepository;

    @Test
    @DisplayName("회원의 개인화된 탐색 점수들을 일괄 저장할 수 있다.")
    void insertScores() {
        // given
        String userUuid = UUID.randomUUID().toString();
        Map<Long, Double> scores = Map.of(
                10L, 15.5,
                20L, 20.0,
                30L, 5.0
        );

        // when
        exploreScoreCommandRepository.insertScores(userUuid, scores);

        // then
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM explore_score WHERE user_uuid = ?", Integer.class, userUuid);
        assertThat(count).isEqualTo(scores.size());
    }

    @Test
    @DisplayName("비회원 탐색 점수들을 일괄 저장할 수 있다.")
    void saveDefaultScores_and_verify() {
        // given
        Map<Long, Double> scores = Map.of(
                100L, 10.0,
                200L, 25.0
        );

        String guestId = UUID.randomUUID().toString();

        // when
        exploreScoreCommandRepository.insertScores(guestId, scores);

        // then
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM explore_score WHERE user_uuid = ?", Integer.class,
                guestId);
        assertThat(count).isEqualTo(scores.size());
    }

    @Test
    @DisplayName("점수에 따라 cursor_id를 계산할 수 있다.")
    void updateCursorIds() {
        // given
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Hearit hearit1 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        Hearit hearit2 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));

        String userUuid = member.getUuid();
        Map<Long, Double> scores = Map.of(
                hearit1.getId(), 10.0,
                hearit2.getId(), 20.0
        );
        exploreScoreCommandRepository.insertScores(userUuid, scores);

        // when
        exploreScoreCommandRepository.updateCursorIds(userUuid);

        // then
        Integer cursorHigh = jdbcTemplate.queryForObject(
                "SELECT cursor_id FROM explore_score WHERE user_uuid = ? AND hearit_id = ?",
                Integer.class,
                userUuid,
                hearit1.getId()
        );
        Integer cursorLow = jdbcTemplate.queryForObject(
                "SELECT cursor_id FROM explore_score WHERE user_uuid = ? AND hearit_id = ?",
                Integer.class,
                userUuid,
                hearit2.getId()
        );

        assertAll(() -> {
            assertThat(cursorHigh).isEqualTo(2);
            assertThat(cursorLow).isEqualTo(1);
        });
    }
}
