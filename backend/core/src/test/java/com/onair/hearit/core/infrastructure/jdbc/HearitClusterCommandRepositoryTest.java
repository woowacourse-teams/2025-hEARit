package com.onair.hearit.core.infrastructure.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.onair.hearit.core.config.DataSourceConfig;
import com.onair.hearit.core.domain.Category;
import com.onair.hearit.core.domain.Hearit;
import com.onair.hearit.core.fixture.DbHelper;
import com.onair.hearit.core.fixture.TestFixture;
import com.onair.hearit.core.fixture.TestJpaAuditingConfig;
import com.onair.hearit.core.infrastructure.projection.HearitClusterStatisticsProjection;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("integration-test")
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Import({HearitClusterCommandRepository.class, DbHelper.class, TestJpaAuditingConfig.class, DataSourceConfig.class})
class HearitClusterCommandRepositoryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DbHelper dbHelper;

    @Autowired
    private HearitClusterCommandRepository hearitClusterCommandRepository;

    @Test
    @DisplayName("복수의 히어릿 통계 데이터를 hearit_cluster 테이블에 일괄 저장한다.")
    void upsertStatistics_insert() {
        // given
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Hearit h1 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        Hearit h2 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));

        // 첫 번째 히어릿 데이터 정의
        long viewCount1 = 1000L;
        long likeCount1 = 10L;
        long bookmarkCount1 = 5L;
        double avgPlayTime1 = 250.0;
        double completionRate1 = 0.7;

        // 두 번째 히어릿 데이터 정의
        long viewCount2 = 2000L;
        long likeCount2 = 20L;
        long bookmarkCount2 = 15L;
        double avgPlayTime2 = 180.5;
        double completionRate2 = 0.4;

        HearitClusterStatisticsProjection stats1 = new StubStatProjection(
                h1.getId(), viewCount1, likeCount1, bookmarkCount1, avgPlayTime1, completionRate1, LocalDateTime.now());
        HearitClusterStatisticsProjection stats2 = new StubStatProjection(
                h2.getId(), viewCount2, likeCount2, bookmarkCount2, avgPlayTime2, completionRate2, LocalDateTime.now());

        // when
        hearitClusterCommandRepository.upsertStatistics(List.of(stats1, stats2));

        // then
        Map<String, Object> res1 = jdbcTemplate.queryForMap("SELECT * FROM hearit_cluster WHERE hearit_id = ?",
                h1.getId());
        Map<String, Object> res2 = jdbcTemplate.queryForMap("SELECT * FROM hearit_cluster WHERE hearit_id = ?",
                h2.getId());

        assertAll(
                () -> assertThat(res1.get("view_count")).isEqualTo(viewCount1),
                () -> assertThat(((Number) res1.get("like_count")).longValue()).isEqualTo(likeCount1),
                () -> assertThat(res1.get("avg_play_time")).isEqualTo(avgPlayTime1),
                () -> assertThat(res1.get("completion_rate")).isEqualTo(completionRate1),

                () -> assertThat(res2.get("view_count")).isEqualTo(viewCount2),
                () -> assertThat(((Number) res2.get("like_count")).longValue()).isEqualTo(likeCount2),
                () -> assertThat(res2.get("avg_play_time")).isEqualTo(avgPlayTime2),
                () -> assertThat(res2.get("completion_rate")).isEqualTo(completionRate2)
        );
    }

    @Test
    @DisplayName("이미 존재하는 히어릿 데이터의 경우, 새로운 통계 수치로 업데이트 한다.")
    void upsertStatistics_update() {
        // given
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Hearit hearit = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        Long hearitId = hearit.getId();

        // 1차 저장 (초기 상태)
        hearitClusterCommandRepository.upsertStatistics(List.of(
                new StubStatProjection(hearitId, 100L, 1L, 1L, 10.0, 0.1, LocalDateTime.now())
        ));

        // 업데이트할 새로운 변수 정의
        long updatedViewCount = 500L;
        long updatedLikeCount = 50L;
        double updatedCompletionRate = 0.9;

        HearitClusterStatisticsProjection updatedStats = new StubStatProjection(
                hearitId, updatedViewCount, updatedLikeCount, 20L, 300.0, updatedCompletionRate, LocalDateTime.now());

        // when
        hearitClusterCommandRepository.upsertStatistics(List.of(updatedStats));

        // then
        Map<String, Object> result = jdbcTemplate.queryForMap("SELECT * FROM hearit_cluster WHERE hearit_id = ?",
                hearitId);
        Integer totalRows = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM hearit_cluster", Integer.class);

        assertAll(
                () -> assertThat(totalRows).isEqualTo(1), // 중복 행 미생성 검증
                () -> assertThat(result.get("view_count")).isEqualTo(updatedViewCount),
                () -> assertThat(((Number) result.get("like_count")).longValue()).isEqualTo(updatedLikeCount),
                () -> assertThat(result.get("completion_rate")).isEqualTo(updatedCompletionRate)
        );
    }

    @Test
    @DisplayName("군집화 결과인 cluster_id를 대상 히어릿들에 일괄 업데이트한다.")
    void updateClusterIds() {
        // given
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Hearit h1 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        Hearit h2 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));

        // 초기 군집 정보 없이 upsert
        hearitClusterCommandRepository.upsertStatistics(List.of(
                new StubStatProjection(h1.getId(), 0, 0, 0, 0, 0, LocalDateTime.now()),
                new StubStatProjection(h2.getId(), 0, 0, 0, 0, 0, LocalDateTime.now())
        ));

        int targetClusterId1 = 3;
        int targetClusterId2 = 5;
        Map<Long, Integer> clusterResults = Map.of(
                h1.getId(), targetClusterId1,
                h2.getId(), targetClusterId2
        );

        // when
        hearitClusterCommandRepository.updateClusterIds(clusterResults);

        // then
        Integer actualClusterId1 = jdbcTemplate.queryForObject(
                "SELECT cluster_id FROM hearit_cluster WHERE hearit_id = ?", Integer.class, h1.getId());
        Integer actualClusterId2 = jdbcTemplate.queryForObject(
                "SELECT cluster_id FROM hearit_cluster WHERE hearit_id = ?", Integer.class, h2.getId());

        assertAll(
                () -> assertThat(actualClusterId1).isEqualTo(targetClusterId1),
                () -> assertThat(actualClusterId2).isEqualTo(targetClusterId2)
        );
    }

    private record StubStatProjection(
            Long hearitId, long viewCount, long likeCount, long bookmarkCount,
            double avgPlayTime, double completionRate, LocalDateTime createdAt
    ) implements HearitClusterStatisticsProjection {
        @Override
        public Long getHearitId() {
            return hearitId;
        }

        @Override
        public long getViewCount() {
            return viewCount;
        }

        @Override
        public long getLikeCount() {
            return likeCount;
        }

        @Override
        public long getBookmarkCount() {
            return bookmarkCount;
        }

        @Override
        public double getAvgPlayTime() {
            return avgPlayTime;
        }

        @Override
        public double getCompletionRate() {
            return completionRate;
        }

        @Override
        public LocalDateTime getCreatedAt() {
            return createdAt;
        }
    }
}
