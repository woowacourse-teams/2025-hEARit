package com.onair.hearit.app.cluster.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.doThrow;

import com.onair.hearit.app.fixture.DbHelper;
import com.onair.hearit.core.config.DataSourceConfig;
import com.onair.hearit.core.domain.HearitCluster;
import com.onair.hearit.core.fixture.TestJpaAuditingConfig;
import com.onair.hearit.core.infrastructure.jdbc.HearitClusterCommandRepository;
import com.onair.hearit.core.infrastructure.jpa.ClusteredHearitRepository;
import jakarta.persistence.EntityManager;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.jdbc.Sql;

@DataJpaTest
@ActiveProfiles("integration-test")
@Sql({"/dbclean.sql", "/clustering_test_data.sql"})
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Import({
        HearitClusterCalculator.class,
        HearitClusterFeatureNormalizer.class,
        HearitClusterCommandRepository.class,
        DbHelper.class,
        TestJpaAuditingConfig.class,
        DataSourceConfig.class
})
class HearitClusterCalculatorTest {

    @Autowired
    private EntityManager em;

    @Autowired
    private HearitClusterCalculator hearitClusterCalculator;

    @Autowired
    private ClusteredHearitRepository clusteredHearitRepository;

    @MockitoSpyBean
    private HearitClusterCommandRepository hearitClusterCommandRepository;

    @Test
    @DisplayName("K-means 알고리즘을 통해 100개의 히어릿을 군집화하고 결과를 분석한다.")
    void calculateClusters_success_with_analysis() {
        // given
        int dataSize = 150; // clustering_test_data 개수
        int k = 5; // 5개의 군집으로 나눔

        // when
        hearitClusterCalculator.calculateClusters(k);
        em.flush();
        em.clear();

        // then
        List<HearitCluster> results = clusteredHearitRepository.findAll();
        Map<Integer, List<HearitCluster>> groups = results.stream()
                .collect(Collectors.groupingBy(HearitCluster::getClusterId));

        printClusterAnalysis(groups); // analyze clustering result

        assertAll(
                () -> assertThat(results).hasSize(dataSize),
                () -> assertThat(groups.size()).isEqualTo(k)
        );
    }

    @Test
    @DisplayName("업데이트 과정에서 예외가 발생하면 기존 군집 정보가 변경되지 않고 롤백된다.")
    void calculateClusters_rollback() {
        // given
        doThrow(new RuntimeException("Database Error")).when(hearitClusterCommandRepository).updateClusterIds(anyMap());

        // when & then
        assertThatThrownBy(() -> hearitClusterCalculator.calculateClusters(5))
                .isInstanceOf(RuntimeException.class);

        List<HearitCluster> results = clusteredHearitRepository.findAll();
        assertThat(results.stream().allMatch(c -> c.getClusterId() == 0)).isTrue(); // initial cluster_id = 0
    }

    private void printClusterAnalysis(Map<Integer, List<HearitCluster>> groups) {
        List<ClusterSummary> summaries = groups.entrySet().stream()
                .map(entry -> new ClusterSummary(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

        summaries.sort((a, b) -> Double.compare(b.avgView, a.avgView));

        System.out.println("\n========================================================");
        System.out.println("[Hearit 콘텐츠 상대적 군집 분석 리포트]");
        System.out.println("========================================================");

        for (int i = 0; i < summaries.size(); i++) {
            ClusterSummary summary = summaries.get(i);
            // 순위와 지표를 바탕으로 페르소나 판단
            String persona = inferRelativePersona(summary, i, summaries.size());

            System.out.printf("[%d위 그룹] Cluster %d\n", i + 1, summary.id);
            System.out.printf(" ▶ 페르소나: %s\n", persona);
            System.out.printf(" ▶ 그룹규모: %d개 콘텐츠\n", groups.get(summary.id).size());
            System.out.printf(" ▶ 평균지표: 조회수 %.1f회 | 완독률 %.1f%% | 최신성점수 %.2f\n",
                    summary.avgView, summary.avgCompletion * 100, summary.avgRecency);
            System.out.println("--------------------------------------------------------");
        }
    }

    /**
     * 정적 수치가 아니라, 현재 데이터 셋 내에서의 '상대적 위치'를 기반으로 그룹의 성격을 규정합니다.
     */
    private String inferRelativePersona(ClusterSummary target, int viewRank, int totalClusters) {
        // 1. Steady Seller: 전체 그룹 중 조회수 순위가 최상위권이면서 완독률도 검증된 경우
        if (viewRank == 0 && target.avgCompletion > 0.6) {
            return "Steady Seller (현재 서비스 내 최고 인기 그룹)";
        }

        // 2. Rising Star: 조회수는 중간일지라도 최신성 점수가 매우 높고 완독률이 폭발적인 경우
        if (target.avgRecency > 0.8 && target.avgCompletion > 0.8) {
            return "Rising Star (신규 유입 및 반응도가 가장 높은 그룹)";
        }

        // 3. Hidden Gem: 조회수 순위는 하위권(total-1, total-2)인데 완독률은 상위권 수준일 때
        if (viewRank >= totalClusters - 2 && target.avgCompletion > 0.85) {
            return "Hidden Gem (조회수는 낮으나 마니아층 몰입도가 압도적인 그룹)";
        }

        // 4. Old Archive: 조회수도 하위권이고 최신성 점수도 바닥인 경우
        if (viewRank >= totalClusters - 2 && target.avgRecency < 0.2) {
            return "Old Archive (오래되어 유입이 끊긴 콘텐츠 그룹)";
        }

        return "General Content (표준적인 지표를 가진 일반 콘텐츠 그룹)";
    }

    private static class ClusterSummary {
        int id;
        double avgView;
        double avgCompletion;
        double avgRecency;

        ClusterSummary(int id, List<HearitCluster> clusters) {
            this.id = id;
            this.avgView = clusters.stream().mapToLong(HearitCluster::getViewCount).average().orElse(0);
            this.avgCompletion = clusters.stream().mapToDouble(HearitCluster::getCompletionRate).average().orElse(0);
            this.avgRecency = clusters.stream().mapToDouble(c -> {
                long days = Duration.between(c.getCreatedAt(), LocalDateTime.now()).toDays();
                return Math.exp(-0.05 * days);
            }).average().orElse(0);
        }
    }
}
