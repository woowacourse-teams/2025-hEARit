package com.onair.hearit.core.infrastructure.jpa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.onair.hearit.core.domain.Category;
import com.onair.hearit.core.domain.ExploreScore;
import com.onair.hearit.core.domain.Hearit;
import com.onair.hearit.core.domain.Member;
import com.onair.hearit.core.fixture.DbHelper;
import com.onair.hearit.core.fixture.TestFixture;
import com.onair.hearit.core.fixture.TestJpaAuditingConfig;
import com.onair.hearit.core.infrastructure.projection.ExploredHearitProjection;
import com.onair.hearit.core.infrastructure.projection.ExploredHearitScoreProjection;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("fake-test")
@Import({DbHelper.class, TestJpaAuditingConfig.class})
class ExploredHearitQueryRepositoryTest {

    @Autowired
    private ExploredHearitQueryRepository exploredHearitQueryRepository;

    @Autowired
    private DbHelper dbHelper;

    @Test
    @DisplayName("회원의 점수 기반 히어릿과 cursorId를 커서 이후부터 조회한다")
    void findExploredHearits_ForMember_byMember() {
        // given
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
        List<ExploreScore> exploreScores = insertTestExploreScoreByMemberIdAndCount(member.getUuid(), 5);
        List<Long> exploreScoreHearitIds = exploreScores.stream()
                .map(ExploreScore::getHearitId)
                .toList();

        // when
        List<ExploredHearitProjection> result = exploredHearitQueryRepository.findExploredHearits(
                member.getUuid(), 2L, Pageable.ofSize(3));

        // then
        assertAll(() -> {
            assertThat(result).hasSize(3);
            assertThat(result).extracting(
                            exploredHearitInfo -> exploredHearitInfo.getHearit().getId()) // cusorId 이후 size 만큼 조회
                    .contains(exploreScoreHearitIds.get(2), exploreScoreHearitIds.get(3), exploreScoreHearitIds.get(4));
            assertThat(result).extracting(ExploredHearitProjection::getCursorId)
                    .contains(3L, 4L, 5L);
        });
    }

    @Test
    @DisplayName("MAX_VALUE 커서로 조회하면 전체를 score 내림차순으로 반환한다 (초기 요청)")
    void findExploredHearitsByScoreCursor_initial() {
        // given
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
        List<ExploreScore> scores = insertTestExploreScoreByMemberIdAndCount(member.getUuid(), 5);

        // when
        List<ExploredHearitScoreProjection> result = exploredHearitQueryRepository
                .findExploredHearitsByScoreCursor(member.getUuid(), Double.MAX_VALUE, Long.MAX_VALUE, Pageable.ofSize(3));

        // then
        assertAll(() -> {
            assertThat(result).hasSize(3);
            // score DESC 순서: 50.0, 40.0, 30.0
            assertThat(result).extracting(ExploredHearitScoreProjection::getScore)
                    .containsExactly(50.0, 40.0, 30.0);
        });
    }

    @Test
    @DisplayName("score 기반 커서 이후 히어릿을 조회한다")
    void findExploredHearitsByScoreCursor_afterCursor() {
        // given
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
        List<ExploreScore> scores = insertTestExploreScoreByMemberIdAndCount(member.getUuid(), 5);
        // score=30.0 (3번째) 항목의 hearitId를 커서로 사용
        Long cursorHearitId = scores.get(2).getHearitId();

        // when - score=30.0, hearitId=cursorHearitId 이후의 데이터
        List<ExploredHearitScoreProjection> result = exploredHearitQueryRepository
                .findExploredHearitsByScoreCursor(member.getUuid(), 30.0, cursorHearitId, Pageable.ofSize(10));

        // then
        assertAll(() -> {
            assertThat(result).hasSize(2);
            // score DESC 순서: 20.0, 10.0
            assertThat(result).extracting(ExploredHearitScoreProjection::getScore)
                    .containsExactly(20.0, 10.0);
        });
    }

    private List<ExploreScore> insertTestExploreScoreByMemberIdAndCount(java.util.UUID userUuid, int count) {
        List<ExploreScore> exploreScores = new ArrayList<>();

        Category category = dbHelper.insertCategory(new Category("Test", "#000000"));

        for (int i = 1; i <= count; i++) {
            Hearit hearit = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
            exploreScores.add(dbHelper.insertMemberExploreScore(
                    new ExploreScore(userUuid, hearit.getId(), i * 10.0, (long) i))); // cursorId = i
        }
        return exploreScores;
    }
}
