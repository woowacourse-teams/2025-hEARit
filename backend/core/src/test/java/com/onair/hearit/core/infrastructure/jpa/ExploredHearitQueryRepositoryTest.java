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
