package com.onair.hearit.app.explore.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.BDDMockito.given;

import com.onair.hearit.app.explore.application.scorefactor.BookmarkScoreFactor;
import com.onair.hearit.app.explore.application.scorefactor.RandomScoreFactor;
import com.onair.hearit.app.explore.application.scorefactor.RecencyScoreFactor;
import com.onair.hearit.app.explore.application.scorefactor.randomgenerator.RandomNumberGenerator;
import com.onair.hearit.app.explore.application.scoreprocessor.GuestExploreScoreProcessor;
import com.onair.hearit.app.explore.application.scoreprocessor.MemberExploreScoreProcessor;
import com.onair.hearit.app.explore.dto.CursorRequest;
import com.onair.hearit.app.explore.dto.CursorResponseV2;
import com.onair.hearit.app.explore.dto.ExploredHearitResponse;
import com.onair.hearit.app.fixture.DbHelper;
import com.onair.hearit.core.config.DataSourceConfig;
import com.onair.hearit.core.domain.Bookmark;
import com.onair.hearit.core.domain.Category;
import com.onair.hearit.core.domain.Hearit;
import com.onair.hearit.core.domain.Member;
import com.onair.hearit.core.domain.Source;
import com.onair.hearit.core.domain.UserInfo;
import com.onair.hearit.core.fixture.TestFixture;
import com.onair.hearit.core.fixture.TestJpaAuditingConfig;
import com.onair.hearit.core.infrastructure.jdbc.ExploreScoreCommandRepository;
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
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;

@DataJpaTest
@Sql("/dbclean.sql")
@Import({DbHelper.class, TestJpaAuditingConfig.class, DataSourceConfig.class, RandomScoreFactor.class,
        RecencyScoreFactor.class, BookmarkScoreFactor.class, ExploreScoreCalculator.class,
        ExploreScoreCommandRepository.class, ExploreScoreInitializer.class, GuestExploreScoreProcessor.class,
        MemberExploreScoreProcessor.class})
@ActiveProfiles("integration-test")
@AutoConfigureTestDatabase(replace = Replace.NONE)
class HearitExploreServiceTest {

    @MockitoBean
    private RandomNumberGenerator randomNumberGenerator;

    @Autowired
    private DbHelper dbHelper;

    @Autowired
    private GuestExploreScoreProcessor guestExploreScoreProcessor;

    @Autowired
    private MemberExploreScoreProcessor memberExploreScoreProcessor;

    private HearitExploreService hearitExploreService;

    @BeforeEach
    void setUp() {
        hearitExploreService = new HearitExploreService(
                List.of(guestExploreScoreProcessor, memberExploreScoreProcessor));
    }

    @DisplayName("회원이 처음 탐색 요청 시(cursorId=0), 최신, 랜덤, 북마크 점수 순으로 정렬하여 반환한다")
    @Test
    void getExploredHearitsForMember_firstRequest() {
        // given
        // 랜덤 점수를 1.0점으로 고정
        double fixedRandomDouble = 0.1d;
        given(randomNumberGenerator.getDouble()).willReturn(fixedRandomDouble);

        LocalDateTime now = LocalDateTime.now();
        Category category1 = dbHelper.insertCategory(new Category("Java", "#112233"));
        Category category2 = dbHelper.insertCategory(new Category("Android", "#445566"));
        Category category3 = dbHelper.insertCategory(new Category("Kotlin", "#778899"));
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
        UserInfo memberInfo = TestFixture.createFixedMemberUserInfo(member);

        // 북마크 이력용 데이터 생성 (카테고리1: 3개, 카테고리2: 1개)
        Hearit hearit1 = dbHelper.insertHearitAt(createHearit(category1), now);
        Hearit hearit2 = dbHelper.insertHearitAt(createHearit(category1), now);
        Hearit hearit3 = dbHelper.insertHearitAt(createHearit(category1), now);
        Hearit hearit4 = dbHelper.insertHearitAt(createHearit(category2), now);
        dbHelper.insertBookmark(new Bookmark(member, hearit1));
        dbHelper.insertBookmark(new Bookmark(member, hearit2));
        dbHelper.insertBookmark(new Bookmark(member, hearit3));
        dbHelper.insertBookmark(new Bookmark(member, hearit4));

        Hearit hearit5 = dbHelper.insertHearitAt(createHearit(category1), now);
        Hearit hearit6 = dbHelper.insertHearitAt(createHearit(category2), now.minusDays(4));
        Hearit hearit7 = dbHelper.insertHearitAt(createHearit(category3), now.minusDays(60));

        // when
        CursorResponseV2<ExploredHearitResponse> response = hearitExploreService.getExploredHearits(
                memberInfo, new CursorRequest(0L, 10));

        // then
        // 예상 점수 계산 (회원 점수 = 북마크 점수 + 최신성 점수 + 랜덤 점수)
        // hearit 1,2,3,5 (cat1, now): 22.5 + 20.0 + 1.0 = 43.5점
        // hearit 4 (cat2, now): 7.5 + 20.0 + 1.0 = 28.5점
        // hearit 6 (cat2, 4일 전): 7.5 + 18.0 + 1.0 = 26.5점
        // hearit 7 (cat3, 60일 전): 0.0 + 0.0 + 1.0 = 1.0점
        assertThat(response.content())
                .hasSize(7)
                .extracting("id", "isBookmarked")
                .as("점수가 높은 순으로 정렬되어야 한다")
                .containsExactly(
                        tuple(hearit5.getId(), false), // 43.5점
                        tuple(hearit3.getId(), true),  // 43.5점
                        tuple(hearit2.getId(), true),  // 43.5점
                        tuple(hearit1.getId(), true),  // 43.5점
                        tuple(hearit4.getId(), true),  // 28.5점
                        tuple(hearit6.getId(), false), // 26.5점
                        tuple(hearit7.getId(), false)  // 1.0점
                );
    }

    @DisplayName("회원이 두 번째 이후 탐색 요청 시(cursorId!=0), 점수판을 갱신하지 않는다")
    @Test
    void reusePersonalScoreOnSecondRequest() {
        // given
        given(randomNumberGenerator.getDouble()).willReturn(0.1d);

        Category category1 = dbHelper.insertCategory(new Category("Java", "#112233"));
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
        UserInfo memberInfo = TestFixture.createFixedMemberUserInfo(member);
        LocalDateTime now = LocalDateTime.now();

        dbHelper.insertHearitAt(createHearit(category1), now);

        // 첫 번째 요청으로 점수판 생성
        CursorResponseV2<ExploredHearitResponse> firstResponse = hearitExploreService.getExploredHearits(
                memberInfo, new CursorRequest(0L, 1));
        long nextCursorId = firstResponse.content().getFirst().cursorId();

        // when
        // 새로운 Hearit이 추가되었지만, cursorId가 0이 아니므로 점수판은 갱신되지 않아야 함
        Hearit newHearit = dbHelper.insertHearitAt(createHearit(category1), now.plusMinutes(1));
        CursorResponseV2<ExploredHearitResponse> secondResponse = hearitExploreService.getExploredHearits(
                memberInfo, new CursorRequest(nextCursorId, 1));

        // then
        assertThat(secondResponse.content())
                .extracting("id")
                .doesNotContain(newHearit.getId());
    }

    @DisplayName("게스트가 탐색 요청 시, 최신, 랜덤 순으로 정렬하여 반환한다")
    @Test
    void getExploredHearitsForGuest() {
        // given
        given(randomNumberGenerator.getDouble()).willReturn(0.1d);

        Category category1 = dbHelper.insertCategory(new Category("Java", "#112233"));
        UserInfo guestInfo = TestFixture.createGuestUserInfo(UUID.randomUUID().toString());
        LocalDateTime now = LocalDateTime.now();

        Hearit hearit1 = dbHelper.insertHearitAt(createHearit(category1), now);
        Hearit hearit2 = dbHelper.insertHearitAt(createHearit(category1), now.minusDays(2));
        Hearit hearit3 = dbHelper.insertHearitAt(createHearit(category1), now.minusDays(4));

        // when
        CursorResponseV2<ExploredHearitResponse> response = hearitExploreService.getExploredHearits(
                guestInfo, new CursorRequest(0L, 10));

        // then
        // 예상 점수 계산 (게스트 점수 = 최신성 점수 + 랜덤 점수)
        // hearit1 (now): 20.0 + 1.0 = 21.0점
        // hearit2 (2일 전): 19.0 + 1.0 = 20.0점
        // hearit3 (4일 전): 18.0 + 1.0 = 19.0점
        assertThat(response.content())
                .hasSize(3)
                .extracting("id", "isBookmarked")
                .containsExactly(
                        tuple(hearit1.getId(), false), // 21.0점
                        tuple(hearit2.getId(), false), // 20.0점
                        tuple(hearit3.getId(), false)  // 19.0점
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
