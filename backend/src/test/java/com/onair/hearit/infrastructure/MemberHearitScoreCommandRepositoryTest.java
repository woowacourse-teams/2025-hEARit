package com.onair.hearit.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import com.onair.hearit.config.TestJpaAuditingConfig;
import com.onair.hearit.domain.Category;
import com.onair.hearit.domain.Hearit;
import com.onair.hearit.domain.Member;
import com.onair.hearit.fixture.DbHelper;
import com.onair.hearit.fixture.TestFixture;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
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
@Import({MemberHearitScoreCommandRepository.class, DbHelper.class, TestJpaAuditingConfig.class})
@ActiveProfiles("integration-test")
@AutoConfigureTestDatabase(replace = Replace.NONE)
public class MemberHearitScoreCommandRepositoryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DbHelper dbHelper;

    @Autowired
    private MemberHearitScoreCommandRepository memberHearitScoreCommandRepository;

    @Test
    @DisplayName("북마크한 카테고리에 속한 히어릿이 높은 순위로 들어간다")
    void generatePersonalScoreRanksHearitsByPreference() {
        // given
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
        Category category1 = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Category category2 = dbHelper.insertCategory(TestFixture.createFixedCategory());

        for (int i = 1; i <= 5; i++) {
            Hearit hearit = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category1));
            if (i <= 3) {
                dbHelper.insertBookmark(TestFixture.createFixedBookmark(member, hearit));
            }
        }
        for (int i = 6; i <= 10; i++) {
            Hearit hearit = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category2));
        }

        // when
        memberHearitScoreCommandRepository.generatePersonalScore(member.getId());

        // then
        Map<Long, Double> hearitScoreMap = jdbcTemplate.query(
                "SELECT hearit_id, score FROM member_explore_score WHERE member_id = ? ORDER BY cursor_id ASC",
                rs -> {
                    Map<Long, Double> result = new LinkedHashMap<>();
                    while (rs.next()) {
                        result.put(rs.getLong("hearit_id"), rs.getDouble("score"));
                    }
                    return result;
                }, member.getId()
        );

        hearitScoreMap.forEach((id, score) -> {
            System.out.println("Hearit ID: " + id + ", Score: " + score);
        });

        // then
        assertThat(hearitScoreMap).hasSize(10);

        // score가 내림차순으로 정렬됐는지 검증
        List<Double> scores = new ArrayList<>(hearitScoreMap.values());
        List<Double> sortedScores = new ArrayList<>(scores);
        sortedScores.sort(Comparator.reverseOrder());
        assertThat(scores).isEqualTo(sortedScores);
    }

    @Test
    @DisplayName("로그인을 안한 경우에는 최신 생성 날짜가 반영된 점수표가 만들어진다.")
    void generateDefaultScoreRanksHearitsByPreference() {
        // given
        Category category1 = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Category category2 = dbHelper.insertCategory(TestFixture.createFixedCategory());

        for (int i = 1; i <= 5; i++) {
            Hearit hearit = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category1));
        }
        for (int i = 6; i <= 10; i++) {
            Hearit hearit = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category2));
        }

        // when
        memberHearitScoreCommandRepository.generateDefaultScore();

        // then
        Map<Long, Double> hearitScoreMap = jdbcTemplate.query(
                "SELECT hearit_id, score FROM member_explore_score WHERE member_id IS NULL ORDER BY cursor_id ASC",
                rs -> {
                    Map<Long, Double> result = new LinkedHashMap<>();
                    while (rs.next()) {
                        result.put(rs.getLong("hearit_id"), rs.getDouble("score"));
                    }
                    return result;
                }
        );

        // then
        assertThat(hearitScoreMap).hasSize(10);

        // score가 내림차순으로 정렬됐는지 검증
        List<Double> scores = new ArrayList<>(hearitScoreMap.values());
        List<Double> sortedScores = new ArrayList<>(scores);
        sortedScores.sort(Comparator.reverseOrder());

        assertThat(scores).isEqualTo(sortedScores);
    }
}
