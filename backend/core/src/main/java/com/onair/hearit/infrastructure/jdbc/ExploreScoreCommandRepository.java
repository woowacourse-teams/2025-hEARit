package com.onair.hearit.infrastructure.jdbc;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ExploreScoreCommandRepository {

    private final JdbcTemplate jdbcTemplate;

    public void insertScores(String userUuid, Map<Long, Double> scores) {
        String insertSql = """
                INSERT INTO explore_score (user_uuid, hearit_id, score, cursor_id)
                VALUES (?, ?, ?, NULL)
                ON DUPLICATE KEY UPDATE
                    score = VALUES(score),
                    cursor_id = NULL
                """;

        List<Object[]> batchArgs = scores.entrySet().stream()
                .map(entry -> new Object[]{userUuid, entry.getKey(), entry.getValue()})
                .toList();

        jdbcTemplate.batchUpdate(insertSql, batchArgs);
    }

    public void updateCursorIds(String userUuid) {
        String updateCursorSql = """
                UPDATE explore_score
                SET cursor_id = (
                    SELECT rank_num FROM (
                        SELECT
                            id,
                            RANK() OVER (ORDER BY score DESC, hearit_id DESC) as rank_num
                        FROM explore_score
                        WHERE user_uuid = ?
                    ) ranked
                    WHERE ranked.id = explore_score.id
                )
                WHERE user_uuid = ?
                """;

        jdbcTemplate.update(updateCursorSql, userUuid, userUuid);
    }
}
