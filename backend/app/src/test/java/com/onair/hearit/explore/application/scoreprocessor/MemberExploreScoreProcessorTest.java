package com.onair.hearit.explore.application.scoreprocessor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.onair.hearit.core.fixture.TestFixture;
import com.onair.hearit.core.fixture.TestJpaAuditingConfig;
import com.onair.hearit.domain.Category;
import com.onair.hearit.domain.Hearit;
import com.onair.hearit.domain.HearitKeyword;
import com.onair.hearit.domain.Keyword;
import com.onair.hearit.domain.Member;
import com.onair.hearit.domain.UserInfo;
import com.onair.hearit.explore.application.ExploreScoreRefresher;
import com.onair.hearit.explore.application.ExploreScoreTestConfig;
import com.onair.hearit.explore.dto.ExploredHearitResponse;
import com.onair.hearit.fixture.DbHelper;
import com.onair.hearit.infrastructure.jdbc.ExploreScoreCommandRepository;
import com.onair.hearit.infrastructure.jpa.BookmarkRepository;
import com.onair.hearit.infrastructure.jpa.ExploredHearitQueryRepository;
import com.onair.hearit.infrastructure.jpa.HearitKeywordRepository;
import com.onair.hearit.infrastructure.jpa.MemberRepository;
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
@Import({DbHelper.class, TestJpaAuditingConfig.class, ExploreScoreTestConfig.class, ExploreScoreCommandRepository.class,
        ExploreScoreRefresher.class})
@ActiveProfiles("integration-test")
@AutoConfigureTestDatabase(replace = Replace.NONE)
class MemberExploreScoreProcessorTest {

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

    private MemberExploreScoreProcessor memberExploreScoreProcessor;

    @BeforeEach
    void setup() {
        memberExploreScoreProcessor = new MemberExploreScoreProcessor(exploreScoreRefresher,
                exploredHearitQueryRepository,
                hearitKeywordRepository,
                memberRepository,
                bookmarkRepository);
    }

    @DisplayName("회원 탐색 응답에 북마크 정보와 키워드가 포함된다")
    @Test
    void fetchExploreResponsesForMember() {
        // given
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
        UserInfo memberInfo = new UserInfo(member.getId(), null);

        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Keyword keyword = dbHelper.insertKeyword(TestFixture.createFixedKeyword());

        Hearit hearit1 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        Hearit hearit2 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        Hearit hearit3 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));

        dbHelper.insertHearitKeyword(new HearitKeyword(hearit1, keyword));
        dbHelper.insertHearitKeyword(new HearitKeyword(hearit2, keyword));
        dbHelper.insertHearitKeyword(new HearitKeyword(hearit3, keyword));
        dbHelper.insertBookmark(TestFixture.createFixedBookmark(member, hearit1));

        // when
        List<ExploredHearitResponse> responses = memberExploreScoreProcessor.getExploreHearitsResponse(memberInfo,
                0L, 3);

        // then
        assertAll(
                () -> assertThat(responses).hasSize(3),
                () -> assertThat(responses).anyMatch(ExploredHearitResponse::isBookmarked),
                () -> assertThat(responses.stream()
                        .filter(ExploredHearitResponse::isBookmarked)
                        .map(ExploredHearitResponse::bookmarkId)
                        .allMatch(id -> id != null && id > 0)).isTrue(),
                () -> assertThat(responses).allMatch(response -> !response.keywords().isEmpty())
        );
    }
}
