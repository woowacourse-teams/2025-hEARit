package com.onair.hearit.application;

import com.onair.hearit.application.explore.DefaultExploreScoreCalculator;
import com.onair.hearit.application.explore.HearitExploreService;
import com.onair.hearit.application.explore.score.BookmarkScoreFactor;
import com.onair.hearit.application.explore.score.RandomScoreFactor;
import com.onair.hearit.application.explore.score.RecencyScoreFactor;
import com.onair.hearit.config.TestJpaAuditingConfig;
import com.onair.hearit.domain.Bookmark;
import com.onair.hearit.domain.Category;
import com.onair.hearit.domain.Hearit;
import com.onair.hearit.domain.Member;
import com.onair.hearit.domain.Source;
import com.onair.hearit.dto.response.CursorResponse;
import com.onair.hearit.dto.response.ExploredHearitResponse;
import com.onair.hearit.fixture.DbHelper;
import com.onair.hearit.fixture.TestFixture;
import com.onair.hearit.infrastructure.BookmarkRepository;
import com.onair.hearit.infrastructure.ExploreScoreCommandRepository;
import com.onair.hearit.infrastructure.ExploredHearitQueryRepository;
import com.onair.hearit.infrastructure.HearitKeywordRepository;
import com.onair.hearit.infrastructure.HearitRepository;
import java.util.List;
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
        DefaultExploreScoreCalculator.class, BookmarkScoreFactor.class, RecencyScoreFactor.class,
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
    private HearitRepository hearitRepository;

    @Autowired
    private HearitKeywordRepository hearitKeywordRepository;

    @Autowired
    private BookmarkRepository bookmarkRepository;

    @Autowired
    private DefaultExploreScoreCalculator exploreScoreCalculator;

    @Autowired
    private BookmarkScoreFactor bookmarkScoreFactor;

    @Autowired
    private RecencyScoreFactor recencyScoreFactor;

    @Autowired
    private RandomScoreFactor randomScoreFactor;

    private HearitExploreService hearitExploreService;

    @BeforeEach
    void setup() {
        hearitExploreService = new HearitExploreService(
                exploreScoreCommandRepository, exploredHearitQueryRepository, hearitRepository,
                hearitKeywordRepository, bookmarkRepository, exploreScoreCalculator,
                bookmarkScoreFactor, recencyScoreFactor, randomScoreFactor);
    }

    @DisplayName("회원 점수판 생성 - 북마크한카테고리, 최신성, 랜덤성")
    @Test
    void generatePersonalScore() {
        // given
        Category category1 = dbHelper.insertCategory(new Category("category1", "#000000"));
        Category category2 = dbHelper.insertCategory(new Category("category2", "#000000"));
        Category category3 = dbHelper.insertCategory(new Category("category3", "#000000"));

        Hearit hearit1 = dbHelper.insertHearit(createHearitByNameAndCategory("hearit1", category1));
        Hearit hearit2 = dbHelper.insertHearit(createHearitByNameAndCategory("hearit2", category1));
        Hearit hearit3 = dbHelper.insertHearit(createHearitByNameAndCategory("hearit3", category1));
        Hearit hearit4 = dbHelper.insertHearit(createHearitByNameAndCategory("hearit4", category1));
        Hearit hearit5 = dbHelper.insertHearit(createHearitByNameAndCategory("hearit5", category2));
        Hearit hearit6 = dbHelper.insertHearit(createHearitByNameAndCategory("hearit6", category2));
        Hearit hearit7 = dbHelper.insertHearit(createHearitByNameAndCategory("hearit7", category2));
        Hearit hearit8 = dbHelper.insertHearit(createHearitByNameAndCategory("hearit8", category3));
        Hearit hearit9 = dbHelper.insertHearit(createHearitByNameAndCategory("hearit9", category3));
        Hearit hearit10 = dbHelper.insertHearit(createHearitByNameAndCategory("hearit10", category3));

        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
        dbHelper.insertBookmark(new Bookmark(member, hearit5));

        // when
        CursorResponse<ExploredHearitResponse> exploredHearits = hearitExploreService.getExploredHearits(member.getId(),
                0L, 10);

        // then
        exploredHearits.content().forEach(System.out::println);
    }

    @DisplayName("바회원 점수판 생성 - 최신성, 랜덤성")
    @Test
    void generateGuestScore() {
        // given
        Category category1 = dbHelper.insertCategory(new Category("category1", "#000000"));
        Category category2 = dbHelper.insertCategory(new Category("category2", "#000000"));
        Category category3 = dbHelper.insertCategory(new Category("category3", "#000000"));

        Hearit hearit1 = dbHelper.insertHearit(createHearitByNameAndCategory("hearit1", category1));
        Hearit hearit2 = dbHelper.insertHearit(createHearitByNameAndCategory("hearit2", category1));
        Hearit hearit3 = dbHelper.insertHearit(createHearitByNameAndCategory("hearit3", category1));
        Hearit hearit4 = dbHelper.insertHearit(createHearitByNameAndCategory("hearit4", category1));
        Hearit hearit5 = dbHelper.insertHearit(createHearitByNameAndCategory("hearit5", category2));
        Hearit hearit6 = dbHelper.insertHearit(createHearitByNameAndCategory("hearit6", category2));
        Hearit hearit7 = dbHelper.insertHearit(createHearitByNameAndCategory("hearit7", category2));
        Hearit hearit8 = dbHelper.insertHearit(createHearitByNameAndCategory("hearit8", category3));
        Hearit hearit9 = dbHelper.insertHearit(createHearitByNameAndCategory("hearit9", category3));
        Hearit hearit10 = dbHelper.insertHearit(createHearitByNameAndCategory("hearit10", category3));

        // when
        CursorResponse<ExploredHearitResponse> exploredHearits = hearitExploreService.getExploredHearits(null, 0L, 10);

        // then
        exploredHearits.content().forEach(System.out::println);
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
