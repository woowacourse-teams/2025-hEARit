package com.onair.hearit.core.infrastructure.jdbc;

import com.onair.hearit.core.infrastructure.projection.HearitClusterStatisticsProjection;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class HearitClusterCommandRepository {

    private static final int BATCH_SIZE = 100;

    private final JdbcTemplate jdbcTemplate;

    /**
     * 통계 데이터를 기반으로 hearit_cluster 테이블에 upsert 수행합니다. cluster_id는 0으로 같은 군집 통일합니다.
     */
    public void upsertStatistics(List<HearitClusterStatisticsProjection> stats) {
        String sql = """
                    INSERT INTO hearit_cluster
                    (hearit_id, view_count, like_count, bookmark_count, avg_play_time, completion_rate, created_at, cluster_id, updated_at)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                    ON DUPLICATE KEY UPDATE
                    view_count = VALUES(view_count),
                    like_count = VALUES(like_count),
                    bookmark_count = VALUES(bookmark_count),
                    avg_play_time = VALUES(avg_play_time),
                    completion_rate = VALUES(completion_rate),
                    updated_at = VALUES(updated_at)
                """;
        LocalDateTime now = LocalDateTime.now();

        jdbcTemplate.batchUpdate(sql, stats, BATCH_SIZE, (ps, argument) -> {
            ps.setLong(1, argument.getHearitId());
            ps.setLong(2, argument.getViewCount());
            ps.setLong(3, argument.getLikeCount());
            ps.setLong(4, argument.getBookmarkCount());
            ps.setDouble(5, argument.getAvgPlayTime());
            ps.setDouble(6, argument.getCompletionRate());
            ps.setTimestamp(7, Timestamp.valueOf(argument.getCreatedAt()));
            ps.setInt(8, 0); // cluster_id = 0
            ps.setTimestamp(9, Timestamp.valueOf(now));
        });
    }

    /**
     * 군집화 결과(cluster_id)를 일괄 업데이트합니다.
     */
    public void updateClusterIds(Map<Long, Integer> clusterResults) {
        String sql = "UPDATE hearit_cluster SET cluster_id = ?, updated_at = ? WHERE hearit_id = ?";
        List<Long> ids = new ArrayList<>(clusterResults.keySet());
        LocalDateTime now = LocalDateTime.now();

        jdbcTemplate.batchUpdate(sql, ids, BATCH_SIZE, (ps, id) -> {
            ps.setInt(1, clusterResults.get(id));
            ps.setTimestamp(2, Timestamp.valueOf(now));
            ps.setLong(3, id);
        });
    }
}
