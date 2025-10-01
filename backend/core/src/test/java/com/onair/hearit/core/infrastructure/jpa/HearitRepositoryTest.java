package com.onair.hearit.core.infrastructure.jpa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.onair.hearit.core.domain.Category;
import com.onair.hearit.core.domain.Hearit;
import com.onair.hearit.core.domain.HearitKeyword;
import com.onair.hearit.core.domain.Keyword;
import com.onair.hearit.core.domain.Member;
import com.onair.hearit.core.domain.PlayingHistory;
import com.onair.hearit.core.domain.Source;
import com.onair.hearit.core.fixture.DbHelper;
import com.onair.hearit.core.fixture.TestFixture;
import com.onair.hearit.core.fixture.TestJpaAuditingConfig;
import com.onair.hearit.core.infrastructure.projection.HearitWithPlayTimeProjection;
import java.util.List;
import org.junit.jupiter.api.Disabled;
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
    @Disabled
    @DisplayName("최근 업로드된 히어릿을 마지막 재생시간과 함께 조회한다.")
    void findTopNHearitWithPlayTimeTest() throws InterruptedException {
        // given
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());

        Hearit hearit1 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        Thread.sleep(10);
        Hearit hearit2 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        Thread.sleep(10);
        Hearit hearit3 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));

        PlayingHistory playingHistory1 = dbHelper.insertPlayingHistory(new PlayingHistory(member.getId(), hearit1, 14));
        PlayingHistory playingHistory2 = dbHelper.insertPlayingHistory(
                new PlayingHistory(member.getId(), hearit3, 300));

        // when
        List<HearitWithPlayTimeProjection> result =
                hearitRepository.findTopNHearitWithPlayTime(member.getId(), 5);

        HearitWithPlayTimeProjection projection3 = result.get(0);
        HearitWithPlayTimeProjection projection2 = result.get(1);
        HearitWithPlayTimeProjection projection1 = result.get(2);

        // then
        assertAll(
                () -> assertThat(result).hasSize(3),
                () -> assertThat(projection1.getHearit().getId()).isEqualTo(hearit1.getId()),
                () -> assertThat(projection1.getLastPlayTime()).isEqualTo(playingHistory1.getLastPlayTime()),
                () -> assertThat(projection2.getHearit().getId()).isEqualTo(hearit2.getId()),
                () -> assertThat(projection2.getLastPlayTime()).isNull(),
                () -> assertThat(projection3.getHearit().getId()).isEqualTo(hearit3.getId()),
                () -> assertThat(projection3.getLastPlayTime()).isEqualTo(playingHistory2.getLastPlayTime())
        );
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
