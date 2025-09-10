package com.onair.hearit.common.infrastructure.jpa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.onair.hearit.common.domain.Category;
import com.onair.hearit.common.domain.Hearit;
import com.onair.hearit.common.domain.HearitKeyword;
import com.onair.hearit.common.domain.Keyword;
import com.onair.hearit.common.domain.Member;
import com.onair.hearit.common.domain.PlayingHistory;
import com.onair.hearit.common.domain.Source;
import com.onair.hearit.common.infrastructure.dto.HearitWithPlayTimeProjection;
import com.onair.hearit.fixture.DbHelper;
import com.onair.hearit.fixture.TestFixture;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@Import({DbHelper.class, TestJpaAuditingConfig.class})
@ActiveProfiles("fake-test")
class HearitRepositoryTest {

    @Autowired
    private DbHelper dbHelper;

    @Autowired
    private HearitRepository hearitRepository;

    @Test
    @DisplayName("단일 히어릿 조회 시 카테고리도 함께 조회한다.")
    void findWithCategoryById() {
        // given
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Hearit savedHearit = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));

        // when
        Hearit hearit = hearitRepository.findWithCategoryById(savedHearit.getId()).get();

        // then
        assertAll(
                () -> assertThat(hearit.getId()).isEqualTo(savedHearit.getId()),
                () -> assertThat(hearit.getCategory().getId()).isEqualTo(savedHearit.getCategory().getId())
        );
    }

    @Test
    @DisplayName("제목 또는 키워드에 검색어가 포함된 히어릿을 반환한다.")
    void searchByTerm_filterByTitleOrKeyword() {
        // given
        Keyword keyword1 = dbHelper.insertKeyword(new Keyword("Springboot"));
        Keyword keyword2 = dbHelper.insertKeyword(new Keyword("NotMatched"));

        Hearit titleMatched = saveHearitWithTitleAndKeyword("SpringBoot is great", keyword2); // 제목만 매칭
        Hearit keywordMatched = saveHearitWithTitleAndKeyword("No match in title", keyword1); // 키워드만 매칭
        Hearit notMatched = saveHearitWithTitleAndKeyword("No match at all", keyword2);       // 둘 다 매칭 안 됨

        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Hearit> result = hearitRepository.searchByTerm("spring", pageable);

        // then
        assertAll(
                () -> assertThat(result.getContent()).hasSize(2),
                () -> assertThat(result.getContent()).extracting(Hearit::getTitle)
                        .containsExactlyInAnyOrder(
                                titleMatched.getTitle(),
                                keywordMatched.getTitle())
        );
    }

    @Test
    @DisplayName("제목과 키워드 둘 다 검색어가 포함돼도 중복 없이 하나만 반환된다.")
    void searchByTerm_avoidDuplicateWhenTitleAndKeywordMatch() {
        // given
        Keyword keyword = dbHelper.insertKeyword(new Keyword("springboot"));
        Hearit hearit = saveHearitWithTitleAndKeyword("SpringBoot", keyword);

        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Hearit> result = hearitRepository.searchByTerm("spring", pageable);

        // then
        assertAll(
                () -> assertThat(result.getContent()).hasSize(1),
                () -> assertThat(result.getContent().get(0).getId()).isEqualTo(hearit.getId())
        );
    }

    @Test
    @DisplayName("카테고리 ID로 원하는 개수의 히어릿을 조회한다.")
    void findByCategory() {
        // given
        Category category1 = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Category category2 = dbHelper.insertCategory(TestFixture.createFixedCategory());

        // category1에 6개 저장
        Hearit hearit1 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category1));
        Hearit hearit2 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category1));
        Hearit hearit3 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category1));
        Hearit hearit4 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category1));
        Hearit hearit5 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category1));
        Hearit hearit6 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category1));
        // category2에 1개 저장
        Hearit hearit7 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category2));

        // when
        List<Hearit> result = hearitRepository.findByCategory(category1.getId(), 5);

        // then
        assertAll(
                () -> assertThat(result).hasSize(5),
                () -> assertThat(result).allMatch(hearit -> hearit.getCategory().getId().equals(category1.getId()))
        );
    }

    @Test
    @DisplayName("히어릿 아이디들로 히어릿 리스트를 카테고리와 함께 조회한다.")
    void findAllByIdInWithCategoryTest() {
        // given
        Category category1 = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Hearit hearit1 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category1));
        Hearit hearit2 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category1));
        Hearit hearit3 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category1));
        List<Long> hearitIds = List.of(hearit1.getId(), hearit2.getId(), hearit3.getId());

        // when
        List<Hearit> hearits = hearitRepository.findAllByIdInWithCategory(hearitIds);

        // then
        assertAll(() -> {
            assertThat(hearits).hasSize(3);
            assertThat(hearits.get(0).getId()).isEqualTo(hearit1.getId());
            assertThat(hearits.get(1).getId()).isEqualTo(hearit2.getId());
            assertThat(hearits.get(2).getId()).isEqualTo(hearit3.getId());
        });
    }

    @Test
    @DisplayName("멤버별 카테고리 내 히어릿 조회 시 마지막 재생 시간도 포함된다.")
    void findWithPlayTimeByCategoryIdTest() {
        // given
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());

        Hearit hearit1 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        Hearit hearit2 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        Hearit hearit3 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));

        PlayingHistory playingHistory1 = dbHelper.insertPlayingHistory(new PlayingHistory(member.getId(), hearit1, 14));
        PlayingHistory playingHistory2 = dbHelper.insertPlayingHistory(
                new PlayingHistory(member.getId(), hearit3, 300));

        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<HearitWithPlayTimeProjection> result =
                hearitRepository.findWithPlayTimeByCategoryId(category.getId(), member.getId(), pageable);

        HearitWithPlayTimeProjection projection3 = result.getContent().get(0);
        HearitWithPlayTimeProjection projection2 = result.getContent().get(1);
        HearitWithPlayTimeProjection projection1 = result.getContent().get(2);

        // then
        assertAll(() -> {
            assertThat(result.getContent()).hasSize(3);
            assertThat(projection1.getHearit().getId()).isEqualTo(hearit1.getId());
            assertThat(projection1.getLastPlayTime()).isEqualTo(playingHistory1.getLastPlayTime());
            assertThat(projection2.getHearit().getId()).isEqualTo(hearit2.getId());
            assertThat(projection2.getLastPlayTime()).isNull();
            assertThat(projection3.getHearit().getId()).isEqualTo(hearit3.getId());
            assertThat(projection3.getLastPlayTime()).isEqualTo(playingHistory2.getLastPlayTime());
        });
    }

    private Hearit saveHearitWithTitleAndKeyword(String title, Keyword keyword) {
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Hearit hearit = new Hearit(
                title,
                "summary",
                100,
                "/hearit/audio/original/ORG_test.mp3",
                "/hearit/audio/short/SHR_test.mp3",
                "/hearit/script/SCR_test.json",
                List.of(new Source("출처", "url")),
                category);
        Hearit savedHearit = dbHelper.insertHearit(hearit);
        dbHelper.insertHearitKeyword(new HearitKeyword(savedHearit, keyword));
        return savedHearit;
    }
}
