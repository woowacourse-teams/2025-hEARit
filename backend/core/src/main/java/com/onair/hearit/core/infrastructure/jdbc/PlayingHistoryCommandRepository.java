package com.onair.hearit.core.infrastructure.jdbc;

import com.onair.hearit.core.domain.PlayingHistory;
import java.nio.ByteBuffer;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PlayingHistoryCommandRepository {

    private final JdbcTemplate jdbcTemplate;

    public void bulkInsert(List<PlayingHistory> histories) {
        LocalDateTime nowDateTime = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
        String sql = """
                INSERT INTO playing_history (user_uuid, hearit_id, last_play_time, is_finished, updated_at)
                VALUES (?, ?, ?, ?, ?) AS new_history
                ON DUPLICATE KEY UPDATE
                    last_play_time = new_history.last_play_time,
                    is_finished = playing_history.is_finished OR new_history.is_finished,
                    updated_at = new_history.updated_at
                """;

        jdbcTemplate.batchUpdate(sql, histories, histories.size(), (ps, history) -> {
            ps.setBytes(1, uuidToBytes(history.getUserUuid()));
            ps.setLong(2, history.getHearitId());
            ps.setLong(3, history.getLastPlayTime());
            ps.setBoolean(4, history.isFinished());
            ps.setTimestamp(5, Timestamp.valueOf(nowDateTime));
        });
    }

    private byte[] uuidToBytes(UUID uuid) {
        ByteBuffer byteBBuffer = ByteBuffer.wrap(new byte[16]);
        byteBBuffer.putLong(uuid.getMostSignificantBits());
        byteBBuffer.putLong(uuid.getLeastSignificantBits());
        return byteBBuffer.array();
    }
}
