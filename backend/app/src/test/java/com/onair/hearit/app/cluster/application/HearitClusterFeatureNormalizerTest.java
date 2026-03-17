package com.onair.hearit.app.cluster.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.onair.hearit.app.cluster.dto.NormalizedHearitClusterFeature;
import com.onair.hearit.core.domain.HearitCluster;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class HearitClusterFeatureNormalizerTest {

    private final HearitClusterFeatureNormalizer hearitClusterFeatureNormalizer = new HearitClusterFeatureNormalizer();

    @Test
    @DisplayName("수치가 가장 높은 데이터를 기준으로 0.0 ~ 1.0 사이의 정규화된 벡터를 생성한다.")
    void normalizeFromEntities_success() {
        // given
        HearitCluster maxFeatureCluster = createCluster(1L, 100L, 10L, 5L, 200.0, 1.0, LocalDateTime.now());
        HearitCluster halfFeatureCluster = createCluster(2L, 50L, 5L, 0L, 100.0, 0.5, LocalDateTime.now());

        // when
        List<NormalizedHearitClusterFeature> result = hearitClusterFeatureNormalizer.normalizeFromEntities(
                List.of(maxFeatureCluster, halfFeatureCluster));

        // then
        NormalizedHearitClusterFeature maxFeature = result.get(0);
        NormalizedHearitClusterFeature halfFeature = result.get(1);
        assertAll(
                // Max 데이터 검증: 최댓값이므로 주요 지표가 1.0이어야 함
                () -> assertThat(maxFeature.viewCount()).isEqualTo(1.0),
                () -> assertThat(maxFeature.likeCount()).isEqualTo(1.0),
                () -> assertThat(maxFeature.bookmarkCount()).isEqualTo(1.0),
                () -> assertThat(maxFeature.avgPlayTime()).isEqualTo(1.0),
                () -> assertThat(maxFeature.completionRate()).isEqualTo(1.0),
                () -> assertThat(maxFeature.recencyScore()).isCloseTo(1.0, within(0.01)),

                // Half 데이터 검증: Max의 절반 수치이므로 0.5여야 함
                () -> assertThat(halfFeature.viewCount()).isEqualTo(0.5),
                () -> assertThat(halfFeature.likeCount()).isEqualTo(0.5),
                () -> assertThat(halfFeature.bookmarkCount()).isEqualTo(0.0),
                () -> assertThat(halfFeature.avgPlayTime()).isEqualTo(0.5),
                () -> assertThat(halfFeature.completionRate()).isEqualTo(0.5)
        );
    }

    @Test
    @DisplayName("생성일이 20일 전인 데이터는 지수 감쇠(Lambda=0.05)에 의해 약 0.367의 점수를 가진다.")
    void calculateRecency_with_exponential_decay() {
        // given
        LocalDateTime now = LocalDateTime.now();
        HearitCluster newCluster = createCluster(1L, 10, 1, 1, 10, 0.5, now);
        HearitCluster twentyDaysAgoCluster = createCluster(2L, 10, 1, 1, 10, 0.5, now.minusDays(20));

        // when
        List<NormalizedHearitClusterFeature> result = hearitClusterFeatureNormalizer.normalizeFromEntities(
                List.of(newCluster, twentyDaysAgoCluster));

        // then
        NormalizedHearitClusterFeature newFeature = result.get(0);
        NormalizedHearitClusterFeature oldFeature = result.get(1);

        assertAll(
                () -> assertThat(newFeature.recencyScore()).isCloseTo(1.0, within(0.01)),
                // e^(-0.05 * 20) = e^(-1) ≈ 0.3678
                () -> assertThat(oldFeature.recencyScore()).isCloseTo(0.367, within(0.01)),
                () -> assertThat(oldFeature.recencyScore()).isLessThan(newFeature.recencyScore())
        );
    }

    @Test
    @DisplayName("정규화된 DTO는 거리 계산을 위한 double 배열(벡터)을 올바르게 반환한다.")
    void toVector_conversion() {
        // given
        HearitCluster cluster = createCluster(1L, 100L, 10L, 5L, 200.0, 0.8, LocalDateTime.now());

        // when
        List<NormalizedHearitClusterFeature> result = hearitClusterFeatureNormalizer.normalizeFromEntities(
                List.of(cluster));
        double[] vector = result.get(0).vector();

        // then
        assertAll(
                () -> assertThat(vector).hasSize(6),
                () -> assertThat(vector[0]).isEqualTo(1.0), // viewCount
                () -> assertThat(vector[4]).isEqualTo(0.8)  // completionRate
        );
    }

    private HearitCluster createCluster(Long id, long view, long like, long bookmark, double playTime, double rate,
                                        LocalDateTime createdAt) {
        return new HearitCluster(id, view, like, bookmark, playTime, rate, createdAt, 0, LocalDateTime.now());
    }
}
