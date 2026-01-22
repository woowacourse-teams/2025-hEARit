package com.onair.hearit.core.infrastructure.jdbc;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ExploreScoreCommandRepository {

    private final JdbcTemplate jdbcTemplate;

    public void insertScores(UUID userUuid, Map<Long, Double> scores) {
        String insertSql = """
                INSERT INTO explore_score (user_uuid, hearit_id, score, cursor_id)
                VALUES (?, ?, ?, NULL) AS new_score
                ON DUPLICATE KEY UPDATE
                    score = new_score.score,
                    cursor_id = NULL
                """;

        byte[] userUuidBytes = uuidToBytes(userUuid);
        List<Object[]> batchArgs = scores.entrySet().stream()
                .map(entry -> new Object[]{userUuidBytes, entry.getKey(), entry.getValue()})
                .toList();

        jdbcTemplate.batchUpdate(insertSql, batchArgs);
    }

    public void updateCursorIds(UUID userUuid) {
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

        byte[] userUuidBytes = uuidToBytes(userUuid);
        jdbcTemplate.update(updateCursorSql, userUuidBytes, userUuidBytes);
    }

    private byte[] uuidToBytes(UUID uuid) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[16]);
        byteBuffer.putLong(uuid.getMostSignificantBits());
        byteBuffer.putLong(uuid.getLeastSignificantBits());
        return byteBuffer.array();
    }
}
