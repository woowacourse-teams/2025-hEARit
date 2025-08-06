package com.onair.hearit.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MemberHearitScoreCommandRepository {

    private final JdbcTemplate jdbcTemplate;

    public void generatePersonalScore(Long memberId) {
        String sql = """
                INSERT INTO member_explore_score (member_id, hearit_id, score, cursor_id)
                SELECT
                    ? AS member_id,
                    h.id AS hearit_id,
                    s.score AS score,
                    RANK() OVER (ORDER BY s.score DESC, h.id DESC) AS cursor_id
                FROM hearit h
                CROSS JOIN (
                    SELECT
                        hh.id AS hearit_id,
                        (
                            ROUND(
                                LEAST(
                                    30,
                                    (
                                        COALESCE((
                                            SELECT COUNT(*)
                                            FROM bookmark b
                                            JOIN hearit hh2 ON b.hearit_id = hh2.id
                                            WHERE b.member_id = ?
                                              AND hh2.category_id = hh.category_id
                                        ), 0) * 1.0
                                        /
                                        GREATEST(
                                            1,
                                            (
                                                SELECT COUNT(*)
                                                FROM bookmark b
                                                JOIN hearit hh2 ON b.hearit_id = hh2.id
                                                WHERE b.member_id = ?
                                            )
                                        ) * 30
                                    )
                                )
                            )
                            + GREATEST(0, 20 - FLOOR(DATEDIFF(NOW(), hh.created_at) / 2) * 2)
                            + (RAND() * 10 - 5)
                        ) AS score
                    FROM hearit hh
                ) s
                WHERE h.id = s.hearit_id
                ON DUPLICATE KEY UPDATE
                    score = VALUES(score),
                    cursor_id = VALUES(cursor_id)
                """;

        jdbcTemplate.update(sql, memberId, memberId, memberId);
    }

//    public void generateDefaultScore() {
//        String sql = """
//                INSERT INTO member_explore_score (member_id, hearit_id, score, cursor_id)
//                SELECT
//                    member_id,
//                    hearit_id,
//                    score,
//                    RANK() OVER (ORDER BY sub.score DESC, hearit_id DESC) AS cursor_id
//                FROM (
//                    SELECT
//                        NULL AS member_id,
//                        h.id AS hearit_id,
//                        (
//                            ROUND(
//                                GREATEST(0, 20 - FLOOR(DATEDIFF(NOW(), h.created_at) / 2) * 2)
//                                + (RAND() * 10 - 5)
//                            )
//                        ) AS score
//                    FROM hearit h
//                ) AS sub
//                ON DUPLICATE KEY UPDATE
//                    score = VALUES(score),
//                    cursor_id = VALUES(cursor_id)
//                """;
//
//        jdbcTemplate.update(sql);
//    }

    public void generateDefaultScore() {
        String sql = """
        INSERT INTO member_explore_score (member_id, hearit_id, score, cursor_id)
        SELECT
            NULL AS member_id,
            h.id AS hearit_id,
            s.score AS score,
            RANK() OVER (ORDER BY s.score DESC, h.id DESC) AS cursor_id
        FROM hearit h
        CROSS JOIN (
            SELECT
                hh.id AS hearit_id,
                ROUND(
                    GREATEST(0, 20 - FLOOR(DATEDIFF(NOW(), hh.created_at) / 2) * 2)
                    + (RAND() * 10 - 5)
                ) AS score
            FROM hearit hh
        ) s
        WHERE h.id = s.hearit_id
        ON DUPLICATE KEY UPDATE
            score = VALUES(score),
            cursor_id = VALUES(cursor_id)
        """;

        jdbcTemplate.update(sql);
    }
}
