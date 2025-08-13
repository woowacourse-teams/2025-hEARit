package com.onair.hearit.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.onair.hearit.config.TestJpaAuditingConfig;
import com.onair.hearit.domain.Category;
import com.onair.hearit.domain.Hearit;
import com.onair.hearit.domain.Member;
import com.onair.hearit.fixture.DbHelper;
import com.onair.hearit.fixture.TestFixture;
import java.util.Map;
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
        Long memberId = 1L;
        Map<Long, Double> scores = Map.of(
                10L, 15.5,
                20L, 20.0,
                30L, 5.0
        );

        // when
        exploreScoreCommandRepository.insertScores(memberId, scores);

        // then
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM explore_score WHERE member_id = ?", Integer.class, memberId);
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

        // when
        exploreScoreCommandRepository.insertScores(-1L, scores);

        // then
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM explore_score WHERE member_id = -1", Integer.class); //memberId null인 경우 -1 취급
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

        Long memberId = member.getId();
        Map<Long, Double> scores = Map.of(
                hearit1.getId(), 10.0,
                hearit2.getId(), 20.0
        );
        exploreScoreCommandRepository.insertScores(memberId, scores);

        // when
        exploreScoreCommandRepository.updateCursorIds(memberId);

        // then
        Integer cursorHigh = jdbcTemplate.queryForObject(
                "SELECT cursor_id FROM explore_score WHERE member_id = ? AND hearit_id = ?",
                Integer.class,
                memberId,
                hearit1.getId()
        );
        Integer cursorLow = jdbcTemplate.queryForObject(
                "SELECT cursor_id FROM explore_score WHERE member_id = ? AND hearit_id = ?",
                Integer.class,
                memberId,
                hearit2.getId()
        );

        assertAll(() -> {
            assertThat(cursorHigh).isEqualTo(2);
            assertThat(cursorLow).isEqualTo(1);
        });
    }
}
