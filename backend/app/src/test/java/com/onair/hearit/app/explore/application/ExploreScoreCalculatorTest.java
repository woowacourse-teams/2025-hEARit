package com.onair.hearit.app.explore.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;

import com.onair.hearit.app.explore.application.scorefactor.ScoreFactor;
import com.onair.hearit.app.fixture.DbHelper;
import com.onair.hearit.core.config.DataSourceConfig;
import com.onair.hearit.core.domain.Category;
import com.onair.hearit.core.domain.Hearit;
import com.onair.hearit.core.domain.PlayingHistory;
import com.onair.hearit.core.domain.UserType;
import com.onair.hearit.core.fixture.TestFixture;
import com.onair.hearit.core.fixture.TestJpaAuditingConfig;
import com.onair.hearit.core.infrastructure.jpa.ClusteredHearitRepository;
import com.onair.hearit.core.infrastructure.jpa.HearitRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

@DataJpaTest
@Sql("/dbclean.sql")
@ActiveProfiles("integration-test")
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Import({DbHelper.class, TestJpaAuditingConfig.class, DataSourceConfig.class})
class ExploreScoreCalculatorTest {

    @Mock
    private ScoreFactor scoreFactor1;

    @Mock
    private ScoreFactor scoreFactor2;

    @Mock
    private ScoreFactor scoreFactor3;

    @Autowired
    private DbHelper dbHelper;

    @Autowired
    private HearitRepository hearitRepository;

    @Autowired
    private ClusteredHearitRepository clusteredHearitRepository;

    @Mock
    private ScoreFactorWeightConfig scoreFactorWeightConfig;

    private ExploreScoreCalculator exploreScoreCalculator;

    @BeforeEach
    void setUp() {
        exploreScoreCalculator = new ExploreScoreCalculator(hearitRepository, clusteredHearitRepository,
                List.of(scoreFactor1, scoreFactor2, scoreFactor3), scoreFactorWeightConfig);
    }

    @Nested
    @DisplayName("히어릿 점수 계산 테스트")
    class RankingTest {

        @Test
        @DisplayName("지원되는 모든 ScoreFactor 들의 점수를 합산하여 Map 으로 반환한다")
        void calculateTotalScores_sumsScoresFromMockedFactors() {
            // given
            Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
            Hearit hearit1 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
            Hearit hearit2 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));

            given(scoreFactor1.isSupported(any())).willReturn(true);
            given(scoreFactor1.calculate(any(), anyList())).willReturn(
                    Map.of(hearit1.getId(), 0.1, hearit2.getId(), 0.1));

            given(scoreFactor2.isSupported(any())).willReturn(true);
            given(scoreFactor2.calculate(any(), anyList())).willReturn(
                    Map.of(hearit1.getId(), 0.3, hearit2.getId(), 0.0));

            given(scoreFactor3.isSupported(any())).willReturn(false);
            given(scoreFactorWeightConfig.getWeight(any())).willReturn(1.0);

            Map<Long, Double> memberScores = exploreScoreCalculator.calculateTotalScores(java.util.UUID.randomUUID(),
                    UserType.MEMBER);

            // then
            assertAll(
                    () -> assertThat(memberScores).containsEntry(hearit1.getId(), 0.4),
                    () -> assertThat(memberScores).containsEntry(hearit2.getId(), 0.1)
            );
        }
    }

    @Nested
    @DisplayName("클러스터링 기반 후보 선정 테스트")
    class SelectTargetHearitsTest {

        @BeforeEach
        void mockScoreFactors() {
            given(scoreFactor1.isSupported(any())).willReturn(false);
            given(scoreFactor2.isSupported(any())).willReturn(false);
            given(scoreFactor3.isSupported(any())).willReturn(false);
            given(scoreFactorWeightConfig.getWeight(any())).willReturn(1.0);
        }

        @Test
        @DisplayName("Top 3 클러스터 기준으로 50/30/20 비율로 100개를 선정한다")
        void selectHearits_byClusterQuota() {
            // given
            Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
            UUID userId = UUID.randomUUID();

            List<Hearit> cluster1 = insertHearitsWithCluster(category, 1, 60);
            List<Hearit> cluster2 = insertHearitsWithCluster(category, 2, 40);
            List<Hearit> cluster3 = insertHearitsWithCluster(category, 3, 30);

            // 재생 이력으로 선호도 형성 (히어릿 개수 기준)
            insertPlayHistory(userId, cluster1.subList(0, 30));
            insertPlayHistory(userId, cluster2.subList(0, 20));
            insertPlayHistory(userId, cluster3.subList(0, 10));

            // when
            Map<Long, Double> result = exploreScoreCalculator.calculateTotalScores(userId, UserType.MEMBER);

            // then
            List<Long> selectedIds = new ArrayList<>(result.keySet());

            assertAll(
                    () -> assertThat(selectedIds).hasSize(100),
                    () -> assertThat(countByCluster(selectedIds, cluster1)).isEqualTo(50),
                    () -> assertThat(countByCluster(selectedIds, cluster2)).isEqualTo(30),
                    () -> assertThat(countByCluster(selectedIds, cluster3)).isEqualTo(20)
            );
        }

        @Test
        @DisplayName("유저 선호 클러스터가 3개 미만이면 랜덤 클러스터로 채운다")
        void selectHearits_whenClusterLessThanThree_thenFillRandomCluster() {
            // given
            Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
            UUID userId = UUID.randomUUID();

            List<Hearit> cluster1 = insertHearitsWithCluster(category, 1, 50);
            List<Hearit> cluster2 = insertHearitsWithCluster(category, 2, 50);
            List<Hearit> cluster3 = insertHearitsWithCluster(category, 3, 50);

            // 유저는 cluster 1만 재생
            insertPlayHistory(userId, cluster1.subList(0, 20));

            // when
            Map<Long, Double> result = exploreScoreCalculator.calculateTotalScores(userId, UserType.MEMBER);

            // then
            assertThat(result).hasSize(100);
        }

        @Test
        @DisplayName("클러스터 히어릿 합이 100개 미만이면 전체에서 추가로 채운다")
        void selectHearits_whenClusterHearitsLessThan100_thenFillFromAll() {
            // given
            Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
            UUID userId = UUID.randomUUID();

            List<Hearit> cluster1 = insertHearitsWithCluster(category, 1, 20);
            List<Hearit> cluster2 = insertHearitsWithCluster(category, 2, 20);
            List<Hearit> cluster3 = insertHearitsWithCluster(category, 3, 20);

            // 선호 클러스터는 정상적으로 3개
            insertPlayHistory(userId, cluster1.subList(0, 10));
            insertPlayHistory(userId, cluster2.subList(0, 8));
            insertPlayHistory(userId, cluster3.subList(0, 6));

            // fillMissingHearits용 전체 후보
            insertHearitsWithCluster(category, 4, 100);

            // when
            Map<Long, Double> result = exploreScoreCalculator.calculateTotalScores(userId, UserType.MEMBER);

            // then
            assertThat(result).hasSize(100);
        }

        private List<Hearit> insertHearitsWithCluster(Category category, int clusterId, int count) {
            return LongStream.range(0, count)
                    .mapToObj(i -> {
                        Hearit h = dbHelper.insertHearit(
                                TestFixture.createHearitWith("hearit-" + clusterId + "-" + i, category)
                        );
                        dbHelper.insertHearitCluster(
                                TestFixture.createHearitClusterWithId(h, clusterId)
                        );
                        return h;
                    }).toList();
        }

        private void insertPlayHistory(UUID userUuid, List<Hearit> hearits) {
            for (Hearit hearit : hearits) {
                dbHelper.insertPlayingHistory(
                        new PlayingHistory(userUuid, hearit, hearit.getPlayTime() - 10)
                );
            }
        }

        private long countByCluster(List<Long> selectedIds, List<Hearit> clusterHearits) {
            Set<Long> clusterIds = clusterHearits.stream()
                    .map(Hearit::getId)
                    .collect(Collectors.toSet());

            return selectedIds.stream()
                    .filter(clusterIds::contains)
                    .count();
        }
    }
}
