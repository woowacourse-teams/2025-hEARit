package com.onair.hearit.app.explore.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.onair.hearit.app.explore.application.scorefactor.BookmarkScoreFactor;
import com.onair.hearit.app.common.DefaultRandomNumberGenerator;
import com.onair.hearit.app.explore.application.scorefactor.RandomScoreFactor;
import com.onair.hearit.app.explore.application.scorefactor.RecencyScoreFactor;
import com.onair.hearit.app.explore.application.scorefactor.generator.DefaultRandomNumberGenerator;
import com.onair.hearit.app.fixture.DbHelper;
import com.onair.hearit.core.config.DataSourceConfig;
import com.onair.hearit.core.domain.Category;
import com.onair.hearit.core.domain.Hearit;
import com.onair.hearit.core.domain.UserType;
import com.onair.hearit.core.fixture.TestFixture;
import com.onair.hearit.core.fixture.TestJpaAuditingConfig;
import com.onair.hearit.core.infrastructure.jdbc.ExploreScoreCommandRepository;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

@DataJpaTest
@Sql("/dbclean.sql")
@Import({DbHelper.class, TestJpaAuditingConfig.class, DataSourceConfig.class, ExploreScoreCommandRepository.class,
        DefaultRandomNumberGenerator.class, RandomScoreFactor.class, RecencyScoreFactor.class,
        BookmarkScoreFactor.class, ExploreScoreCalculator.class, ScoreFactorWeightConfig.class})
@ActiveProfiles("integration-test")
@AutoConfigureTestDatabase(replace = Replace.NONE)
class ExploreScoreInitializerTest {

    @Autowired
    private DbHelper dbHelper;

    @Autowired
    private ExploreScoreCommandRepository exploreScoreCommandRepository;

    @Autowired
    private ExploreScoreCalculator exploreScoreCalculator;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private ExploreScoreInitializer exploreScoreInitializer;

    @BeforeEach
    void setUp() {
        exploreScoreInitializer = new ExploreScoreInitializer(exploreScoreCalculator, exploreScoreCommandRepository);
    }

    @DisplayName("cursorId가 0이 아니면 갱신하지 않는다")
    @Test
    void skipRefreshingWhenCursorIsNotZero() {
        // given
        UUID userUuid = UUID.randomUUID();
        long cursorId = 1L;

        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));

        // when
        exploreScoreInitializer.refreshScores(cursorId, userUuid, UserType.GUEST);

        // then
        assertThat(findExploreScores(userUuid)).isEmpty();
    }

    @DisplayName("cursorId가 0이면 점수를 갱신하고 커서 ID를 부여한다")
    @Test
    void refreshScoresScoresWhenCursorIsZero() {
        // given
        UUID userUuid = UUID.randomUUID();
        long cursorId = 0L;

        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Hearit hearit = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));

        exploreScoreInitializer.refreshScores(cursorId, userUuid, UserType.GUEST);

        List<ExploreScoreRow> rows = findExploreScores(userUuid);

        assertAll(
                () -> assertThat(rows).hasSize(1),
                () -> assertThat(rows.get(0).hearitId()).isEqualTo(hearit.getId()),
                () -> assertThat(rows.get(0).score()).isNotZero(),
                () -> assertThat(rows.get(0).cursorId()).isNotNull()
        );
    }

    private List<ExploreScoreRow> findExploreScores(UUID userUuid) {
        return jdbcTemplate.query(
                """
                        SELECT hearit_id, score, cursor_id
                        FROM explore_score
                        WHERE user_uuid = ?
                        ORDER BY cursor_id
                        """,
                (rs, rowNum) -> new ExploreScoreRow(
                        rs.getLong("hearit_id"),
                        rs.getDouble("score"),
                        rs.getObject("cursor_id", Long.class)
                ),
                uuidToBytes(userUuid)
        );
    }

    private byte[] uuidToBytes(UUID uuid) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[16]);
        byteBuffer.putLong(uuid.getMostSignificantBits());
        byteBuffer.putLong(uuid.getLeastSignificantBits());
        return byteBuffer.array();
    }

    private record ExploreScoreRow(Long hearitId, double score, Long cursorId) {
    }
}
