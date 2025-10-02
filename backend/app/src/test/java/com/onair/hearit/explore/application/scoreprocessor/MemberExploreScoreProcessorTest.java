package com.onair.hearit.explore.application.scoreprocessor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.onair.hearit.core.fixture.TestFixture;
import com.onair.hearit.core.fixture.TestJpaAuditingConfig;
import com.onair.hearit.domain.Category;
import com.onair.hearit.domain.ExploreScore;
import com.onair.hearit.domain.Hearit;
import com.onair.hearit.domain.Member;
import com.onair.hearit.domain.UserInfo;
import com.onair.hearit.explore.application.ExploreScoreCalculator;
import com.onair.hearit.explore.application.ExploreScoreRefresher;
import com.onair.hearit.explore.application.scorefactor.BookmarkScoreFactor;
import com.onair.hearit.explore.application.scorefactor.RandomNumberGenerator;
import com.onair.hearit.explore.application.scorefactor.RandomScoreFactor;
import com.onair.hearit.explore.application.scorefactor.RecencyScoreFactor;
import com.onair.hearit.explore.dto.ExploredHearitResponse;
import com.onair.hearit.fixture.DbHelper;
import com.onair.hearit.infrastructure.jdbc.ExploreScoreCommandRepository;
import com.onair.hearit.infrastructure.jpa.BookmarkRepository;
import com.onair.hearit.infrastructure.jpa.ExploredHearitQueryRepository;
import com.onair.hearit.infrastructure.jpa.HearitKeywordRepository;
import com.onair.hearit.infrastructure.jpa.MemberRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;

@DataJpaTest
@Sql("/dbclean.sql")
@Import({DbHelper.class, TestJpaAuditingConfig.class, RandomScoreFactor.class, RecencyScoreFactor.class,
        BookmarkScoreFactor.class, ExploreScoreCalculator.class, ExploreScoreCommandRepository.class,
        ExploreScoreRefresher.class})
@ActiveProfiles("integration-test")
@AutoConfigureTestDatabase(replace = Replace.NONE)
class MemberExploreScoreProcessorTest {

    @MockitoBean
    private RandomNumberGenerator randomNumberGenerator;

    @Autowired
    private DbHelper dbHelper;

    @Autowired
    private BookmarkRepository bookmarkRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ExploredHearitQueryRepository exploredHearitQueryRepository;

    @Autowired
    private HearitKeywordRepository hearitKeywordRepository;

    @Autowired
    private ExploreScoreRefresher exploreScoreRefresher;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private MemberExploreScoreProcessor memberExploreScoreProcessor;

    @BeforeEach
    void setup() {
        memberExploreScoreProcessor = new MemberExploreScoreProcessor(exploreScoreRefresher,
                exploredHearitQueryRepository,
                hearitKeywordRepository,
                memberRepository,
                bookmarkRepository);
    }

    @DisplayName("회원 사용자를 지원한다")
    @Test
    void isSupportedForMember() {
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
        UserInfo memberInfo = new UserInfo(member.getId(), null);
        assertThat(memberExploreScoreProcessor.isSupported(memberInfo)).isTrue();
    }

    @DisplayName("회원이 아니면 지원하지 않는다")
    @Test
    void isSupportedForNonMember() {
        UserInfo guestInfo = new UserInfo(null, UUID.randomUUID().toString());
        UserInfo nonExistingMember = new UserInfo(999L, null);
        assertAll(
                () -> assertThat(memberExploreScoreProcessor.isSupported(null)).isFalse(),
                () -> assertThat(memberExploreScoreProcessor.isSupported(guestInfo)).isFalse(),
                () -> assertThat(memberExploreScoreProcessor.isSupported(nonExistingMember)).isFalse()
        );
    }

    @DisplayName("탐색 점수를 조회하면 DB의 score 데이터를 DTO로 변환하여 반환한다")
    @Test
    void getExploreHearitsReturnsResponses() {
        // given
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
        UserInfo memberInfo = new UserInfo(member.getId(), null);
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Hearit hearit1 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        Hearit hearit2 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));

        dbHelper.insertExploreScore(new ExploreScore(member.getUuid(), hearit1.getId(), 50.0, 1L));
        dbHelper.insertExploreScore(new ExploreScore(member.getUuid(), hearit2.getId(), 40.0, 2L));
        // when
        List<ExploredHearitResponse> responses = memberExploreScoreProcessor.getExploreHearits(memberInfo, 0L, 3);

        // then
        ExploredHearitResponse response1 = responses.get(0);
        ExploredHearitResponse response2 = responses.get(1);

        assertAll(
                () -> assertThat(responses).hasSize(2),
                () -> assertThat(response1.id()).isEqualTo(hearit1.getId()),
                () -> assertThat(response1.cursorId()).isEqualTo(1L),
                () -> assertThat(response2.id()).isEqualTo(hearit2.getId()),
                () -> assertThat(response2.cursorId()).isEqualTo(2L)
        );
    }
}
