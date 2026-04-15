package com.onair.hearit.core.infrastructure.jpa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.onair.hearit.core.domain.Category;
import com.onair.hearit.core.domain.Hearit;
import com.onair.hearit.core.domain.Member;
import com.onair.hearit.core.domain.PlayingHistory;
import com.onair.hearit.core.domain.Series;
import com.onair.hearit.core.fixture.DbHelper;
import com.onair.hearit.core.fixture.TestFixture;
import com.onair.hearit.core.fixture.TestJpaAuditingConfig;
import com.onair.hearit.core.infrastructure.projection.HearitWithPlayTimeProjection;
import jakarta.persistence.EntityManager;
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
    EntityManager em;

    @Autowired
    DbHelper dbHelper;

    @Autowired
    HearitRepository hearitRepository;

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
    void findTopNHearitWithPlayTimeTest() {
        // given
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());

        LocalDateTime baseTime = LocalDateTime.of(2025, 1, 1, 0, 0);
        Hearit hearit1 = dbHelper.insertHearitAt(TestFixture.createFixedHearitWith(category),
                baseTime.minusMinutes(2));
        Hearit hearit2 = dbHelper.insertHearitAt(TestFixture.createFixedHearitWith(category),
                baseTime.minusMinutes(1));
        Hearit hearit3 = dbHelper.insertHearitAt(TestFixture.createFixedHearitWith(category), baseTime);

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

    @Test
    @DisplayName("히어릿에 대한 조회수 증가 시 정상적으로 1을 반환한다.")
    void increaseViewCount_success() {
        // given
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Hearit hearit = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));

        // when
        int updated = hearitRepository.increaseViewCount(hearit.getId());

        em.clear();
        Hearit updatedHearit = hearitRepository.findById(hearit.getId()).get();

        // then
        assertAll(
                () -> assertThat(updated).isEqualTo(1),
                () -> assertThat(updatedHearit.getViewCount()).isEqualTo(1L)
        );
    }

    @Test
    @DisplayName("존재하지 않은 히어릿 ID로 조회수 증가 시 0을 반환한다.")
    void increaseViewCount_notFound() {
        // given
        Long notExistHearitId = 9999L;

        // when
        int updated = hearitRepository.increaseViewCount(notExistHearitId);

        // then
        assertThat(updated).isEqualTo(0);
    }

    @Test
    @DisplayName("시리즈 ID로 히어릿을 createdAt 역순으로 조회한다.")
    void findBySeriesOrderByCreatedAtDesc_orderedByCreatedAtDesc() {
        // given
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Series series = dbHelper.insertSeries(TestFixture.createFixedSeries());

        LocalDateTime baseTime = LocalDateTime.of(2026, 4, 1, 0, 0);
        Hearit hearit1 = dbHelper.insertHearitAt(TestFixture.createFixedHearitWith(category), baseTime.minusDays(2));
        Hearit hearit2 = dbHelper.insertHearitAt(TestFixture.createFixedHearitWith(category), baseTime.minusDays(1));
        Hearit hearit3 = dbHelper.insertHearitAt(TestFixture.createFixedHearitWith(category), baseTime);
        dbHelper.assignHearitToSeries(hearit1, series);
        dbHelper.assignHearitToSeries(hearit2, series);
        dbHelper.assignHearitToSeries(hearit3, series);

        // when
        List<Hearit> result = hearitRepository.findBySeriesOrderByCreatedAtDesc(series.getId());

        // then
        assertAll(
                () -> assertThat(result).hasSize(3),
                () -> assertThat(result.get(0).getId()).isEqualTo(hearit3.getId()),
                () -> assertThat(result.get(1).getId()).isEqualTo(hearit2.getId()),
                () -> assertThat(result.get(2).getId()).isEqualTo(hearit1.getId())
        );
    }

    @Test
    @DisplayName("시리즈 ID로 조회 시 다른 시리즈의 히어릿은 포함되지 않는다.")
    void findBySeriesOrderByCreatedAtDesc_excludesOtherSeries() {
        // given
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Series series1 = dbHelper.insertSeries(TestFixture.createFixedSeries());
        Series series2 = dbHelper.insertSeries(TestFixture.createFixedSeries());

        Hearit hearit1 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        Hearit hearit2 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        Hearit hearit3 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        dbHelper.assignHearitToSeries(hearit1, series1);
        dbHelper.assignHearitToSeries(hearit2, series1);
        dbHelper.assignHearitToSeries(hearit3, series2);

        // when
        List<Hearit> result = hearitRepository.findBySeriesOrderByCreatedAtDesc(series1.getId());

        // then
        assertAll(
                () -> assertThat(result).hasSize(2),
                () -> assertThat(result).extracting(Hearit::getId)
                        .containsExactlyInAnyOrder(hearit1.getId(), hearit2.getId())
        );
    }

    @Test
    @DisplayName("시리즈 ID로 조회 시 카테고리가 함께 로딩된다.")
    void findBySeriesOrderByCreatedAtDesc_fetchesCategory() {
        // given
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Series series = dbHelper.insertSeries(TestFixture.createFixedSeries());

        Hearit hearit = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        dbHelper.assignHearitToSeries(hearit, series);

        em.clear();

        // when
        List<Hearit> result = hearitRepository.findBySeriesOrderByCreatedAtDesc(series.getId());

        // then
        assertThat(result.get(0).getCategory().getId()).isEqualTo(category.getId());
    }

    @Test
    @DisplayName("전체 히어릿 ID만 반환한다.")
    void findAllIds() {
        // given
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());

        Hearit hearit1 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        Hearit hearit2 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        Hearit hearit3 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));

        // when
        List<Long> result = hearitRepository.findAllIds();

        // then
        assertAll(
                () -> assertThat(result).hasSize(3),
                () -> assertThat(result).containsExactlyInAnyOrder(hearit1.getId(), hearit2.getId(), hearit3.getId())
        );
    }
}
