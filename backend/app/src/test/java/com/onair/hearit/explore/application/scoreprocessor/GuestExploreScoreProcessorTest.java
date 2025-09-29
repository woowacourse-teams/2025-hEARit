package com.onair.hearit.explore.application.scoreprocessor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.onair.hearit.core.fixture.TestFixture;
import com.onair.hearit.core.fixture.TestJpaAuditingConfig;
import com.onair.hearit.domain.Category;
import com.onair.hearit.domain.Hearit;
import com.onair.hearit.domain.HearitKeyword;
import com.onair.hearit.domain.Keyword;
import com.onair.hearit.domain.UserInfo;
import com.onair.hearit.domain.UserType;
import com.onair.hearit.explore.application.ExploreScoreRefresher;
import com.onair.hearit.explore.application.TestConfig;
import com.onair.hearit.explore.dto.ExploredHearitResponse;
import com.onair.hearit.fixture.DbHelper;
import com.onair.hearit.infrastructure.jdbc.ExploreScoreCommandRepository;
import com.onair.hearit.infrastructure.jpa.ExploredHearitQueryRepository;
import com.onair.hearit.infrastructure.jpa.HearitKeywordRepository;
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
@Import({DbHelper.class, TestJpaAuditingConfig.class, TestConfig.class, ExploreScoreCommandRepository.class,
        ExploreScoreRefresher.class})
@ActiveProfiles("integration-test")
@AutoConfigureTestDatabase(replace = Replace.NONE)
class GuestExploreScoreProcessorTest {

    private static final String GUEST_ID = UUID.randomUUID().toString();

    @Autowired
    private DbHelper dbHelper;

    @Autowired
    private ExploredHearitQueryRepository exploredHearitQueryRepository;

    @Autowired
    private HearitKeywordRepository hearitKeywordRepository;

    @Autowired
    private ExploreScoreRefresher exploreScoreRefresher;

    private GuestExploreScoreProcessor guestExploreScoreProcessor;

    @BeforeEach
    void setup() {
        guestExploreScoreProcessor = new GuestExploreScoreProcessor(exploredHearitQueryRepository,
                hearitKeywordRepository);
    }

    @DisplayName("게스트 UUID를 그대로 반환한다")
    @Test
    void resolveUserUuidReturnsGuestId() {
        // given
        UserInfo guestInfo = new UserInfo(null, GUEST_ID);

        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Keyword keyword = dbHelper.insertKeyword(TestFixture.createFixedKeyword());

        Hearit hearit1 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        Hearit hearit2 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        Hearit hearit3 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));

        dbHelper.insertHearitKeyword(new HearitKeyword(hearit1, keyword));
        dbHelper.insertHearitKeyword(new HearitKeyword(hearit2, keyword));
        dbHelper.insertHearitKeyword(new HearitKeyword(hearit3, keyword));

        exploreScoreRefresher.refreshIfNeeded(0L, GUEST_ID, UserType.GUEST);

        // when
        String uuid = guestExploreScoreProcessor.resolveUserUuid(guestInfo);

        // then
        assertThat(uuid).isEqualTo(GUEST_ID);
    }

    @DisplayName("게스트 탐색 응답에 키워드와 커서가 포함된다")
    @Test
    void fetchExploreResponsesForGuest() {
        // given
        UserInfo guestInfo = new UserInfo(null, GUEST_ID);

        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Keyword keyword = dbHelper.insertKeyword(TestFixture.createFixedKeyword());

        Hearit hearit1 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        Hearit hearit2 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        Hearit hearit3 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));

        dbHelper.insertHearitKeyword(new HearitKeyword(hearit1, keyword));
        dbHelper.insertHearitKeyword(new HearitKeyword(hearit2, keyword));
        dbHelper.insertHearitKeyword(new HearitKeyword(hearit3, keyword));

        exploreScoreRefresher.refreshIfNeeded(0L, GUEST_ID, UserType.GUEST);

        // when
        List<ExploredHearitResponse> responses = guestExploreScoreProcessor.getExploreHearitsResponse(guestInfo,
                GUEST_ID, 0L, 3);

        // then
        assertAll(
                () -> assertThat(responses).hasSize(3),
                () -> assertThat(responses).allMatch(response -> !response.isBookmarked()),
                () -> assertThat(responses).allMatch(
                        response -> response.cursorId() != null && response.cursorId() > 0),
                () -> assertThat(responses.getFirst().keywords()).isNotEmpty()
        );
    }
}
