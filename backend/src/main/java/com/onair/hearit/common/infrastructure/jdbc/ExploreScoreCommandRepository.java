package com.onair.hearit.common.infrastructure.jdbc;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ExploreScoreCommandRepository {

    private final JdbcTemplate jdbcTemplate;

    /**
     * 개인화된 탐색 점수들을 일괄 저장
     *
     * @param memberId 사용자 ID (nullable)
     * @param scores   hearitId를 키로 하고 점수를 값으로 하는 Map
     */
    public void insertScores(Long memberId, Map<Long, Double> scores) {
        String insertSql = """
                INSERT INTO explore_score (member_id, hearit_id, score, cursor_id)
                VALUES (?, ?, ?, NULL)
                ON DUPLICATE KEY UPDATE
                    score = VALUES(score),
                    cursor_id = NULL
                """;

        List<Object[]> batchArgs = scores.entrySet().stream()
                .map(entry -> new Object[]{memberId, entry.getKey(), entry.getValue()})
                .toList();

        jdbcTemplate.batchUpdate(insertSql, batchArgs);
    }

    /**
     * 점수에 따라 cursor_id를 업데이트 memberId가 -1이면 기본 점수, 아니면 개인화 점수의 cursor_id를 업데이트
     *
     * @param memberId 사용자 ID (-1인 경우 기본 점수)
     */
    public void updateCursorIds(Long memberId) {
        String updateCursorSql = """
                UPDATE explore_score
                SET cursor_id = (
                    SELECT rank_num FROM (
                        SELECT
                            id,
                            RANK() OVER (ORDER BY score DESC, hearit_id DESC) as rank_num
                        FROM explore_score
                        WHERE member_id = ?
                    ) ranked
                    WHERE ranked.id = explore_score.id
                )
                WHERE member_id = ?
                """;

        jdbcTemplate.update(updateCursorSql, memberId, memberId);
    }
}
