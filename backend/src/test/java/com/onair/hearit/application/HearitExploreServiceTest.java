package com.onair.hearit.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.onair.hearit.config.TestJpaAuditingConfig;
import com.onair.hearit.domain.Category;
import com.onair.hearit.domain.Hearit;
import com.onair.hearit.domain.HearitKeyword;
import com.onair.hearit.domain.Keyword;
import com.onair.hearit.domain.Member;
import com.onair.hearit.dto.response.ExploredHearitResponse;
import com.onair.hearit.fixture.DbHelper;
import com.onair.hearit.fixture.TestFixture;
import com.onair.hearit.infrastructure.BookmarkRepository;
import com.onair.hearit.infrastructure.HearitKeywordRepository;
import com.onair.hearit.infrastructure.MemberHearitScoreCommandRepository;
import com.onair.hearit.infrastructure.MemberHearitScoreQueryRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@Import({DbHelper.class, TestJpaAuditingConfig.class})
@ActiveProfiles("fake-test")
class HearitExploreServiceTest {

    @Autowired
    DbHelper dbHelper;

    @Autowired
    MemberHearitScoreCommandRepository memberHearitScoreCommandRepository;

    @Autowired
    MemberHearitScoreQueryRepository memberHearitScoreQueryRepository;

    @Autowired
    HearitKeywordRepository hearitKeywordRepository;

    @Autowired
    BookmarkRepository bookmarkRepository;

    private HearitExploreService hearitExploreService;

    @BeforeEach
    void setup() {
        hearitExploreService = new HearitExploreService(
                memberHearitScoreCommandRepository, memberHearitScoreQueryRepository,
                hearitKeywordRepository, bookmarkRepository);
    }

//    @DisplayName("탐색 히어릿 조회 시 북마크 여부와 키워드를 함께 반환한다")

//    @DisplayName("비회원이 커서가 0인 상태에서 탐색 히어릿을 요청하면 기본 점수를 생성하고 조회한다")
//    @DisplayName("비회원이 커서가 0이 아닌 상태에서 탐색 히어릿을 요청하면 기본 점수 기반으로 조회한다")
//
//    @DisplayName("회원이 커서가 0인 상태에서 탐색 히어릿을 요청하면 개인 점수를 생성하고 조회한다")
//    @DisplayName("회원이 커서가 0이 아닌 상태에서 탐색 히어릿을 요청하면 개인 점수 기반으로 조회한다")
}
