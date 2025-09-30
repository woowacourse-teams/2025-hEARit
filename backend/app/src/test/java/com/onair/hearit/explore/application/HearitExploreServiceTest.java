package com.onair.hearit.explore.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.BDDMockito.given;

import com.onair.hearit.common.dto.request.CursorRequest;
import com.onair.hearit.common.dto.response.CursorResponseV2;
import com.onair.hearit.core.fixture.TestFixture;
import com.onair.hearit.core.fixture.TestJpaAuditingConfig;
import com.onair.hearit.domain.Bookmark;
import com.onair.hearit.domain.Category;
import com.onair.hearit.domain.Hearit;
import com.onair.hearit.domain.Member;
import com.onair.hearit.domain.Source;
import com.onair.hearit.domain.UserInfo;
import com.onair.hearit.explore.application.scorefactor.BookmarkScoreFactor;
import com.onair.hearit.explore.application.scorefactor.RandomNumberGenerator;
import com.onair.hearit.explore.application.scorefactor.RandomScoreFactor;
import com.onair.hearit.explore.application.scorefactor.RecencyScoreFactor;
import com.onair.hearit.explore.application.scoreprocessor.GuestExploreScoreProcessor;
import com.onair.hearit.explore.application.scoreprocessor.MemberExploreScoreProcessor;
import com.onair.hearit.explore.dto.ExploredHearitResponse;
import com.onair.hearit.fixture.DbHelper;
import com.onair.hearit.infrastructure.jdbc.ExploreScoreCommandRepository;
import com.onair.hearit.infrastructure.jpa.BookmarkRepository;
import com.onair.hearit.infrastructure.jpa.ExploredHearitQueryRepository;
import com.onair.hearit.infrastructure.jpa.HearitKeywordRepository;
import com.onair.hearit.infrastructure.jpa.HearitRepository;
import com.onair.hearit.infrastructure.jpa.MemberRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

@DataJpaTest
@Sql("/dbclean.sql")
@Import({DbHelper.class, RandomScoreFactor.class, RecencyScoreFactor.class, BookmarkScoreFactor.class,
        ExploreScoreCalculator.class,
        ExploreScoreCommandRepository.class, ExploreScoreRefresher.class, TestJpaAuditingConfig.class
})
@ActiveProfiles("integration-test")
@AutoConfigureTestDatabase(replace = Replace.NONE)
class HearitExploreServiceTest {

    @MockBean
    private RandomNumberGenerator randomNumberGenerator;

    @Autowired
    private DbHelper dbHelper;

    @Autowired
    private HearitRepository hearitRepository;

    @Autowired
    private ExploredHearitQueryRepository exploredHearitQueryRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private BookmarkRepository bookmarkRepository;

    @Autowired
    private HearitKeywordRepository hearitKeywordRepository;

    @Autowired
    private ExploreScoreRefresher exploreScoreRefresher;

    private HearitExploreService hearitExploreService;

    @BeforeEach
    void setup() {
        hearitExploreService = new HearitExploreService(
                List.of(
                        new GuestExploreScoreProcessor(exploreScoreRefresher, exploredHearitQueryRepository,
                                hearitKeywordRepository),
                        new MemberExploreScoreProcessor(exploreScoreRefresher, exploredHearitQueryRepository,
                                hearitKeywordRepository,
                                memberRepository, bookmarkRepository)
                )
        );
    }

    @DisplayName("회원이 처음 탐색 요청 시(cursorId=0), 점수 순으로 정렬하여 반환한다")
    @Test
    void getExploredHearitsForMember_firstRequest() {
        given(randomNumberGenerator.nextDouble()).willReturn(0.1d);
        // given
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime fourDaysAgo = now.minusDays(4);
        LocalDateTime sixtyDaysAgo = now.minusDays(60);

        Category category1 = dbHelper.insertCategory(new Category("Java", "#112233"));
        Category category2 = dbHelper.insertCategory(new Category("Android", "#445566"));
        Category category3 = dbHelper.insertCategory(new Category("Kotlin", "#778899"));
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
        UserInfo memberInfo = TestFixture.createFixedMemberUserInfo(member);

        Hearit hearit1 = dbHelper.insertHearitAt(createHearit(category1), now);
        Hearit hearit2 = dbHelper.insertHearitAt(createHearit(category1), now);
        Hearit hearit3 = dbHelper.insertHearitAt(createHearit(category1), now);
        Hearit hearit4 = dbHelper.insertHearitAt(createHearit(category2), now);

        dbHelper.insertBookmark(new Bookmark(member, hearit1));
        dbHelper.insertBookmark(new Bookmark(member, hearit2));
        dbHelper.insertBookmark(new Bookmark(member, hearit3));
        dbHelper.insertBookmark(new Bookmark(member, hearit4));

        Hearit hearit5 = dbHelper.insertHearitAt(createHearit(category1), now);
        Hearit hearit6 = dbHelper.insertHearitAt(createHearit(category2), fourDaysAgo);
        Hearit hearit7 = dbHelper.insertHearitAt(createHearit(category3), sixtyDaysAgo);

        // when
        CursorResponseV2<ExploredHearitResponse> response = hearitExploreService.getExploredHearits(
                memberInfo, new CursorRequest(0L, 10));

        // then
        assertThat(response.content())
                .hasSize(7)
                .extracting("id", "isBookmarked")
                .containsExactly(
                        tuple(hearit5.getId(), false),
                        tuple(hearit3.getId(), true),
                        tuple(hearit2.getId(), true),
                        tuple(hearit1.getId(), true),
                        tuple(hearit4.getId(), true),
                        tuple(hearit6.getId(), false),
                        tuple(hearit7.getId(), false)
                );

    }

    @DisplayName("회원이 두 번째 이후 탐색 요청 시(cursorId!=0), 점수판을 갱신하지 않는다")
    @Test
    void reusePersonalScoreOnSecondRequest() {
        given(randomNumberGenerator.nextDouble()).willReturn(0.1d);
        // given
        Category category1 = dbHelper.insertCategory(new Category("Java", "#112233"));
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
        UserInfo memberInfo = TestFixture.createFixedMemberUserInfo(member);

        LocalDateTime now = LocalDateTime.now();
        dbHelper.insertHearitAt(createHearit(category1), now);

        CursorResponseV2<ExploredHearitResponse> firstResponse = hearitExploreService.getExploredHearits(
                memberInfo, new CursorRequest(0L, 2));
        List<ExploredHearitResponse> firstContent = firstResponse.content();
        assertThat(firstContent).isNotEmpty();
        long nextCursorId = firstContent.get(firstContent.size() - 1).cursorId();

        // when
        Hearit newHearit = dbHelper.insertHearitAt(createHearit(category1), now.plusMinutes(1));
        CursorResponseV2<ExploredHearitResponse> secondResponse = hearitExploreService.getExploredHearits(
                memberInfo, new CursorRequest(nextCursorId, 2));

        assertThat(secondResponse.content())
                .extracting("id")
                .doesNotContain(newHearit.getId());

    }

    @DisplayName("게스트가 탐색 요청 시, 북마크를 제외한 점수 순으로 정렬하여 반환한다")
    @Test
    void getExploredHearitsForGuest() {
        given(randomNumberGenerator.nextDouble()).willReturn(0.1d);
        // given
        LocalDateTime now = LocalDateTime.now();
        Category category1 = dbHelper.insertCategory(new Category("Java", "#112233"));
        UserInfo guestInfo = TestFixture.createFixedGuestUserInfo(UUID.randomUUID().toString());

        Hearit hearit1 = dbHelper.insertHearitAt(createHearit(category1), now);
        Hearit hearit2 = dbHelper.insertHearitAt(createHearit(category1), now.minusDays(2));
        Hearit hearit3 = dbHelper.insertHearitAt(createHearit(category1), now.minusDays(4));

        // when
        CursorResponseV2<ExploredHearitResponse> response = hearitExploreService.getExploredHearits(
                guestInfo, new CursorRequest(0L, 10));

        // then
        assertThat(response.content())
                .hasSize(3)
                .extracting("id", "isBookmarked")
                .containsExactly(
                        tuple(hearit1.getId(), false),
                        tuple(hearit2.getId(), false),
                        tuple(hearit3.getId(), false)
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
                List.of(new Source("??컨텐츠는 쿠버?�티??공식 문서 (?�?�자: The Kubernetes Authors)�?참고?�여 만들?�졌?�니??",
                        "https://example.com/1")),
                category
        );
    }
}



