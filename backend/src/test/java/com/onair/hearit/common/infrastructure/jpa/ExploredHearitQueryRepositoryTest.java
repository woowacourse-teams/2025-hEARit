package com.onair.hearit.common.infrastructure.jpa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.onair.hearit.common.domain.Category;
import com.onair.hearit.common.domain.ExploreScore;
import com.onair.hearit.common.domain.Hearit;
import com.onair.hearit.common.domain.Member;
import com.onair.hearit.fixture.DbHelper;
import com.onair.hearit.fixture.TestFixture;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
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
    @DisplayName("회원의 점수 기반 히어릿을 커서 이후부터 조회한다")
    void findExploredHearits_ForMember_byMember() {
        // given
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
        List<ExploreScore> exploreScores = insertTestExploreScoreByMemberIdAndCount(UUID.fromString(member.getUuid()), 5);
        List<Long> exploreScoreHearitIds = exploreScores.stream()
                .map(ExploreScore::getHearitId)
                .toList();

        // when
        List<Hearit> result = exploredHearitQueryRepository.findExploredHearits(member.getUuid(), 2L, 3);

        // then
        assertAll(() -> {
            assertThat(result).hasSize(3);
            assertThat(result).extracting(Hearit::getId) // cusorId 이후 size 만큼 조회
                    .contains(exploreScoreHearitIds.get(2), exploreScoreHearitIds.get(3), exploreScoreHearitIds.get(4));
        });
    }

    private List<ExploreScore> insertTestExploreScoreByMemberIdAndCount(UUID userUuid, int count) {
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
