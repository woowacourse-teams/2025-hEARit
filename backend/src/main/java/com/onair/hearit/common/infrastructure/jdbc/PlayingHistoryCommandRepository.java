package com.onair.hearit.common.infrastructure.jdbc;

import com.onair.hearit.common.domain.PlayingHistory;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PlayingHistoryCommandRepository {

    private final JdbcTemplate jdbcTemplate;

    public void bulkInsert(List<PlayingHistory> histories) {
        String sql = """
                INSERT INTO playing_history (member_id, hearit_id, last_play_time, is_finished, updated_at)
                VALUES (?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    last_play_time = VALUES(last_play_time),
                    is_finished = VALUES(is_finished),
                    updated_at = VALUES(updated_at)
                """;

        jdbcTemplate.batchUpdate(sql, histories, histories.size(), (ps, history) -> {
            ps.setLong(1, history.getMemberId());
            ps.setLong(2, history.getHearitId());
            ps.setLong(3, history.getLastPlayTime());
            ps.setBoolean(4, history.isFinished());
            ps.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
        });
    }
}
