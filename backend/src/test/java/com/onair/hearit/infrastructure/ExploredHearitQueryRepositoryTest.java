package com.onair.hearit.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import com.onair.hearit.config.TestJpaAuditingConfig;
import com.onair.hearit.domain.Category;
import com.onair.hearit.domain.Hearit;
import com.onair.hearit.domain.Member;
import com.onair.hearit.domain.ExploreScore;
import com.onair.hearit.fixture.DbHelper;
import com.onair.hearit.fixture.TestFixture;
import java.util.List;
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
@ActiveProfiles("integration-test")
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Import({ExploreScoreCommandRepository.class, DbHelper.class, TestJpaAuditingConfig.class})
class ExploredHearitQueryRepositoryTest {

    @Autowired
    private ExploredHearitQueryRepository exploredHearitQueryRepository;

    @Autowired
    private DbHelper dbHelper;

    @Test
    @DisplayName("회원의 점수 기반 히어릿을 커서 이후부터 조회한다")
    void findExploredHearits_byMember() {
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
        insertTestExploreScoreByMemberIdAndCount(member.getId(), 5);

        List<Hearit> result = exploredHearitQueryRepository.findExploredHearits(member.getId(), 2L, 3);

        assertThat(result).hasSize(3);
        assertThat(result).extracting(Hearit::getId).contains(3L, 4L, 5L); //2L 이후 3L부터 size만큼 조회
    }

    @Test
    @DisplayName("비회원의 점수 기반 히어릿을 커서 이후부터 조회한다")
    void findExploredHearits_byGuest() {
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
        insertTestExploreScoreByMemberIdAndCount(null, 5);

        List<Hearit> result = exploredHearitQueryRepository.findExploredHearitsForGuest(2L, 3);

        assertThat(result).hasSize(3);
        assertThat(result).extracting(Hearit::getId).contains(3L, 4L, 5L); //2L 이후 3L부터 size만큼 조회
    }

    private void insertTestExploreScoreByMemberIdAndCount(Long memberId, int count) {
        Category category = dbHelper.insertCategory(new Category("Test", "#000000"));

        for (int i = 1; i <= count; i++) {
            Hearit hearit = dbHelper.insertHearit(
                    new Hearit("title" + i, "summary", 100, "...", "...", "...", "source", category));

            ExploreScore score = dbHelper.insertMemberExploreScore(
                    new ExploreScore(memberId, hearit.getId(), i * 10.0, (long) i)); // cursorId = i
        }
    }
}

