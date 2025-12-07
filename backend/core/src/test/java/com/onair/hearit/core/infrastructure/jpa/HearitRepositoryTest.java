package com.onair.hearit.core.infrastructure.jpa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.onair.hearit.core.domain.Category;
import com.onair.hearit.core.domain.Hearit;
import com.onair.hearit.core.domain.Member;
import com.onair.hearit.core.domain.PlayingHistory;
import com.onair.hearit.core.fixture.DbHelper;
import com.onair.hearit.core.fixture.TestFixture;
import com.onair.hearit.core.fixture.TestJpaAuditingConfig;
import com.onair.hearit.core.infrastructure.projection.HearitWithPlayTimeProjection;
import java.time.LocalDateTime;
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
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
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
    @DisplayName("멤버별 카테고리 내 히어릿 조회 시 마지막 재생 시간도 포함된다.")
    void findWithPlayTimeByTest() {
        // given
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());

        Hearit hearit1 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        Hearit hearit2 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        Hearit hearit3 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));

        PlayingHistory playingHistory1 = dbHelper.insertPlayingHistory(
                new PlayingHistory(member.getUuid(), hearit1, 14));
        PlayingHistory playingHistory2 = dbHelper.insertPlayingHistory(
                new PlayingHistory(member.getUuid(), hearit3, 300));

        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<HearitWithPlayTimeProjection> result =
                hearitRepository.findWithPlayTimeBy(category.getId(), member.getUuid(), pageable);

        HearitWithPlayTimeProjection projection1 = result.getContent().get(0);
        HearitWithPlayTimeProjection projection2 = result.getContent().get(1);
        HearitWithPlayTimeProjection projection3 = result.getContent().get(2);

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

    @Test
    @Disabled
    @DisplayName("최근 업로드된 히어릿을 마지막 재생시간과 함께 조회한다.")
    void findTopNHearitWithPlayTimeTest() throws InterruptedException {
        // given
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());

        Hearit hearit1 = dbHelper.insertHearitAt(TestFixture.createFixedHearitWith(category),
                LocalDateTime.now().minusMinutes(2));
        Hearit hearit2 = dbHelper.insertHearitAt(TestFixture.createFixedHearitWith(category),
                LocalDateTime.now().minusMinutes(1));
        Hearit hearit3 = dbHelper.insertHearitAt(TestFixture.createFixedHearitWith(category), LocalDateTime.now());

        PlayingHistory playingHistory1 = dbHelper.insertPlayingHistory(
                new PlayingHistory(member.getUuid(), hearit1, 14));
        PlayingHistory playingHistory2 = dbHelper.insertPlayingHistory(
                new PlayingHistory(member.getUuid(), hearit3, 300));

        Pageable pageable = PageRequest.of(0, 10, Sort.by(Direction.DESC, "createdAt"));

        // when
        Page<HearitWithPlayTimeProjection> result =
                hearitRepository.findWithPlayTimeBy(null, member.getUuid(), pageable);

        HearitWithPlayTimeProjection projection3 = result.getContent().get(0);
        HearitWithPlayTimeProjection projection2 = result.getContent().get(1);
        HearitWithPlayTimeProjection projection1 = result.getContent().get(2);

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
}
