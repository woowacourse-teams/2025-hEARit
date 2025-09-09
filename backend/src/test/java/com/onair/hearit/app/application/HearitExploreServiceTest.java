package com.onair.hearit.app.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.onair.hearit.app.application.explore.ExploreScoreCalculator;
import com.onair.hearit.app.application.explore.HearitExploreService;
import com.onair.hearit.app.application.explore.scorefactor.BookmarkScoreFactor;
import com.onair.hearit.app.application.explore.scorefactor.RandomScoreFactor;
import com.onair.hearit.app.application.explore.scorefactor.RecencyScoreFactor;
import com.onair.hearit.app.application.explore.scoreprocessor.GuestExploreScoreProcessor;
import com.onair.hearit.app.application.explore.scoreprocessor.MemberExploreScoreProcessor;
import com.onair.hearit.auth.domain.UserContext;
import com.onair.hearit.common.infrastructure.jpa.TestJpaAuditingConfig;
import com.onair.hearit.common.domain.Bookmark;
import com.onair.hearit.common.domain.Category;
import com.onair.hearit.common.domain.Hearit;
import com.onair.hearit.common.domain.Member;
import com.onair.hearit.common.domain.Source;
import com.onair.hearit.app.dto.request.CursorRequest;
import com.onair.hearit.app.dto.response.CursorResponse;
import com.onair.hearit.app.dto.response.ExploredHearitResponse;
import com.onair.hearit.fixture.DbHelper;
import com.onair.hearit.fixture.TestFixture;
import com.onair.hearit.common.infrastructure.jpa.BookmarkRepository;
import com.onair.hearit.common.infrastructure.jdbc.ExploreScoreCommandRepository;
import com.onair.hearit.common.infrastructure.jpa.ExploredHearitQueryRepository;
import com.onair.hearit.common.infrastructure.jpa.HearitKeywordRepository;
import com.onair.hearit.common.infrastructure.jpa.HearitRepository;
import com.onair.hearit.common.infrastructure.jpa.MemberRepository;
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
import org.springframework.test.context.jdbc.Sql;

@DataJpaTest
@Sql("/dbclean.sql")
@Import({DbHelper.class, TestJpaAuditingConfig.class, ExploreScoreCommandRepository.class,
        ExploreScoreCalculator.class, BookmarkScoreFactor.class, RecencyScoreFactor.class,
        RandomScoreFactor.class})
@ActiveProfiles("integration-test")
@AutoConfigureTestDatabase(replace = Replace.NONE)
class HearitExploreServiceTest {

    @Autowired
    private DbHelper dbHelper;

    @Autowired
    private ExploreScoreCommandRepository exploreScoreCommandRepository;

    @Autowired
    private ExploredHearitQueryRepository exploredHearitQueryRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private HearitRepository hearitRepository;

    @Autowired
    private HearitKeywordRepository hearitKeywordRepository;

    @Autowired
    private BookmarkRepository bookmarkRepository;

    @Autowired
    private ExploreScoreCalculator exploreScoreCalculator;

    @Autowired
    private BookmarkScoreFactor bookmarkScoreFactor;

    @Autowired
    private RecencyScoreFactor recencyScoreFactor;

    @Autowired
    private RandomScoreFactor randomScoreFactor;

    private HearitExploreService hearitExploreService;

    @BeforeEach
    void setup() {
        hearitExploreService = new HearitExploreService(List.of(
                new GuestExploreScoreProcessor(
                        exploreScoreCalculator, exploreScoreCommandRepository,
                        exploredHearitQueryRepository, hearitKeywordRepository),
                new MemberExploreScoreProcessor(
                        exploreScoreCalculator, exploreScoreCommandRepository,
                        exploredHearitQueryRepository, hearitKeywordRepository,
                        memberRepository, bookmarkRepository)
        ));
    }

    @DisplayName("회원 점수판 생성 - 북마크한카테고리, 최신성, 랜덤성")
    @Test
    void generatePersonalScore() {
        // given
        Category category1 = dbHelper.insertCategory(new Category("category1", "#000000"));
        Category category2 = dbHelper.insertCategory(new Category("category2", "#000000"));
        Category category3 = dbHelper.insertCategory(new Category("category3", "#000000"));

        Hearit hearit = dbHelper.insertHearit(createHearitByNameAndCategory("hearit5", category2));
        dbHelper.insertHearit(createHearitByNameAndCategory("hearit1", category1));
        dbHelper.insertHearit(createHearitByNameAndCategory("hearit2", category1));
        dbHelper.insertHearit(createHearitByNameAndCategory("hearit3", category1));
        dbHelper.insertHearit(createHearitByNameAndCategory("hearit4", category1));
        dbHelper.insertHearit(createHearitByNameAndCategory("hearit6", category2));
        dbHelper.insertHearit(createHearitByNameAndCategory("hearit7", category2));
        dbHelper.insertHearit(createHearitByNameAndCategory("hearit8", category3));
        dbHelper.insertHearit(createHearitByNameAndCategory("hearit9", category3));
        dbHelper.insertHearit(createHearitByNameAndCategory("hearit10", category3));

        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
        dbHelper.insertBookmark(new Bookmark(member, hearit));

        // when
        CursorResponse<ExploredHearitResponse> exploredHearits = hearitExploreService.getExploredHearits(
                UserContext.member(member.getId()), new CursorRequest(0L, 10));

        // then
        // 출력을 해보고 싶으면 아래 주석 해제
        // exploredHearits.content().forEach(System.out::println);
        assertThat(exploredHearits.content()).hasSize(10);
    }

    @DisplayName("비회원 점수판 생성 - 최신성, 랜덤성")
    @Test
    void generateGuestScore() {
        // given
        Category category1 = dbHelper.insertCategory(new Category("category1", "#000000"));
        Category category2 = dbHelper.insertCategory(new Category("category2", "#000000"));
        Category category3 = dbHelper.insertCategory(new Category("category3", "#000000"));

        dbHelper.insertHearit(createHearitByNameAndCategory("hearit2", category1));
        dbHelper.insertHearit(createHearitByNameAndCategory("hearit1", category1));
        dbHelper.insertHearit(createHearitByNameAndCategory("hearit3", category1));
        dbHelper.insertHearit(createHearitByNameAndCategory("hearit4", category1));
        dbHelper.insertHearit(createHearitByNameAndCategory("hearit5", category2));
        dbHelper.insertHearit(createHearitByNameAndCategory("hearit6", category2));
        dbHelper.insertHearit(createHearitByNameAndCategory("hearit7", category2));
        dbHelper.insertHearit(createHearitByNameAndCategory("hearit8", category3));
        dbHelper.insertHearit(createHearitByNameAndCategory("hearit9", category3));
        dbHelper.insertHearit(createHearitByNameAndCategory("hearit10", category3));

        // when
        CursorResponse<ExploredHearitResponse> exploredHearits = hearitExploreService.getExploredHearits(
                UserContext.guest(UUID.randomUUID().toString()), new CursorRequest(0L, 10));

        // then
        // 출력을 해보고 싶으면 아래 주석 해제
        // exploredHearits.content().forEach(System.out::println);
        assertThat(exploredHearits.content()).hasSize(10);
    }

    private Hearit createHearitByNameAndCategory(String name, Category category) {
        return new Hearit(
                "title" + name,
                "summary" + name,
                500,
                "/hearit/audio/original/ORG_test.mp3",
                "/hearit/audio/short/SHR_test.mp3",
                "/hearit/script/SCR_test.json",
                List.of(new Source("source", "sourceUrl")),
                category);
    }
}
