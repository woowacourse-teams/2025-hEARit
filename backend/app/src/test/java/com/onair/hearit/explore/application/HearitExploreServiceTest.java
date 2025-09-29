package com.onair.hearit.explore.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import com.onair.hearit.common.TestClock;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
@Import({DbHelper.class, TestConfig.class, ExploreScoreCommandRepository.class, ExploreScoreRefresher.class,
        TestJpaAuditingConfig.class
})
@ActiveProfiles("integration-test")
@AutoConfigureTestDatabase(replace = Replace.NONE)
class HearitExploreServiceTest {

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
                List.of(new GuestExploreScoreProcessor(exploredHearitQueryRepository, hearitKeywordRepository),
                        new MemberExploreScoreProcessor(exploredHearitQueryRepository, hearitKeywordRepository,
                                memberRepository, bookmarkRepository)), exploreScoreRefresher);
    }

    @AfterEach
    void tearDown() {
        TestClock.unfreeze();
    }

    @DisplayName("회원이 처음 탐색 요청 시(cursorId=0), 점수 순으로 정렬하여 반환한다")
    @Test
    void getExploredHearitsForMember_firstRequest() {
        // given
        LocalDateTime fourDaysAgo = LocalDateTime.now().minusDays(4);
        LocalDateTime sixtyDaysAgo = LocalDateTime.now().minusDays(60);

        Category category1 = dbHelper.insertCategory(new Category("Java", "#112233"));
        Category category2 = dbHelper.insertCategory(new Category("Android", "#445566"));
        Category category3 = dbHelper.insertCategory(new Category("Kotlin", "#778899"));
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
        UserInfo memberInfo = TestFixture.createFixedMemberUserInfo(member);

        // -- 북마크 이력용 Hearit들 --
        TestClock.freezeAt(LocalDateTime.now());
        // 최신성 20, 카테고리 22.5
        Hearit hearit1 = dbHelper.insertHearit(createHearit(category1));
        // 최신성 20, 카테고리 22.5
        Hearit hearit2 = dbHelper.insertHearit(createHearit(category1));
        // 최신성 20, 카테고리 22.5
        Hearit hearit3 = dbHelper.insertHearit(createHearit(category1));
        // 최성 20, 카테고리 7.5
        Hearit hearit4 = dbHelper.insertHearit(createHearit(category2));

        //  카테고리별 북마크 - category1 : 3개, category2: 1개
        dbHelper.insertBookmark(new Bookmark(member, hearit1));
        dbHelper.insertBookmark(new Bookmark(member, hearit2));
        dbHelper.insertBookmark(new Bookmark(member, hearit3));
        dbHelper.insertBookmark(new Bookmark(member, hearit4));

        // 북마크 제외 히어릿들
        TestClock.freezeAt(LocalDateTime.now());
        // 최신성 20, 카테고리 22.5
        Hearit hearit5 = dbHelper.insertHearit(createHearit(category1));
        // 최신성 18, 카테고리 7.5
        TestClock.freezeAt(fourDaysAgo);
        Hearit hearit6 = dbHelper.insertHearit(createHearit(category2));
        // 최신성 0, 카테고리 0
        TestClock.freezeAt(sixtyDaysAgo);
        Hearit hearit7 = dbHelper.insertHearit(createHearit(category3));

        // when
        CursorResponseV2<ExploredHearitResponse> response = hearitExploreService.getExploredHearits(
                memberInfo, new CursorRequest(0L, 10));

        // then
        // 점수가 높은 순서(내림차순)로 정렬
        assertThat(response.content())
                .hasSize(7)
                .extracting("id", "isBookmarked")
                .containsExactly(
                        tuple(hearit5.getId(), false),
                        tuple(hearit3.getId(), true),
                        tuple(hearit2.getId(), true),
                        tuple(hearit1.getId(), true),
                        // --- 28.5점 ---
                        tuple(hearit4.getId(), true),
                        // --- 26.5점 ---
                        tuple(hearit6.getId(), false),
                        // --- 1.0점 ---
                        tuple(hearit7.getId(), false)
                );
    }

    @DisplayName("회원이 두 번째 이후 탐색 요청 시(cursorId!=0), 점수판을 갱신하지 않는다")
    @Test
    void reusePersonalScoreOnSecondRequest() {
        // given
        Category category1 = dbHelper.insertCategory(new Category("Java", "#112233"));
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
        UserInfo memberInfo = TestFixture.createFixedMemberUserInfo(member);

        TestClock.freezeAt(LocalDateTime.now());
        dbHelper.insertHearit(createHearit(category1));

        CursorResponseV2<ExploredHearitResponse> firstResponse = hearitExploreService.getExploredHearits(
                memberInfo, new CursorRequest(0L, 2));
        List<ExploredHearitResponse> firstContent = firstResponse.content();
        assertThat(firstContent).isNotEmpty();
        long nextCursorId = firstContent.get(firstContent.size() - 1).cursorId();

        // when
        TestClock.freezeAt(LocalDateTime.now().plusMinutes(1));
        Hearit newHearit = dbHelper.insertHearit(createHearit(category1));
        CursorResponseV2<ExploredHearitResponse> secondResponse = hearitExploreService.getExploredHearits(
                memberInfo, new CursorRequest(nextCursorId, 2));

        assertThat(secondResponse.content())
                .extracting("id")
                .doesNotContain(newHearit.getId());
    }

    @DisplayName("게스트가 탐색 요청 시, 북마크를 제외한 점수 순으로 정렬하여 반환한다")
    @Test
    void getExploredHearitsForGuest() {
        // given
        Category category1 = dbHelper.insertCategory(new Category("Java", "#112233"));
        UserInfo guestInfo = TestFixture.createFixedGuestUserInfo(UUID.randomUUID().toString());

        TestClock.freezeAt(LocalDateTime.now());
        Hearit hearit1 = dbHelper.insertHearit(createHearit(category1));
        TestClock.freezeAt(LocalDateTime.now().minusDays(2));
        Hearit hearit2 = dbHelper.insertHearit(createHearit(category1));
        TestClock.freezeAt(LocalDateTime.now().minusDays(4));
        Hearit hearit3 = dbHelper.insertHearit(createHearit(category1));

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
                List.of(new Source("이 컨텐츠는 쿠버네티스 공식 문서 (저작자: The Kubernetes Authors)를 참고하여 만들어졌습니다.",
                        "https://example.com/1")),
                category
        );
    }
}
