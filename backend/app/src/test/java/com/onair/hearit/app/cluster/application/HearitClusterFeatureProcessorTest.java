package com.onair.hearit.app.cluster.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

import com.onair.hearit.app.fixture.DbHelper;
import com.onair.hearit.core.config.DataSourceConfig;
import com.onair.hearit.core.domain.Category;
import com.onair.hearit.core.fixture.TestFixture;
import com.onair.hearit.core.fixture.TestJpaAuditingConfig;
import com.onair.hearit.core.infrastructure.jdbc.HearitClusterCommandRepository;
import com.onair.hearit.core.infrastructure.jpa.HearitRepository;
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
@ActiveProfiles("integration-test")
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Import({
        HearitClusterCommandRepository.class,
        DbHelper.class,
        TestJpaAuditingConfig.class,
        DataSourceConfig.class
})
class HearitClusterFeatureProcessorTest {

    @Autowired
    private HearitRepository hearitRepository;

    @Autowired
    private HearitClusterCommandRepository hearitClusterCommandRepository;

    @Autowired
    private DbHelper dbHelper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private HearitClusterFeatureProcessor hearitClusterFeatureProcessor;
    private HearitClusterCommandRepository spyCommandRepository;

    @BeforeEach
    void setUp() {
        spyCommandRepository = spy(hearitClusterCommandRepository);
        hearitClusterFeatureProcessor = new HearitClusterFeatureProcessor(hearitRepository, spyCommandRepository);
    }

    @Test
    @DisplayName("한 페이지의 통계 데이터를 성공적으로 DB에 커밋한다.")
    void processPage_success() {
        // given
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));

        // when
        boolean hasNext = hearitClusterFeatureProcessor.processPage(0, 2);

        // then
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM hearit_cluster", Integer.class);
        assertAll(
                () -> assertThat(count).isEqualTo(2),
                () -> assertThat(hasNext).isFalse()
        );
    }

    @Test
    @DisplayName("저장 도중 예외가 발생하면 해당 페이지의 모든 변경사항이 롤백된다.")
    void processPage_rollback() {
        // given
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));

        // Spy를 통해 예외 상황 시뮬레이션
        doThrow(new RuntimeException("DB 장애")).when(spyCommandRepository).upsertStatistics(anyList());

        // when & then
        assertThatThrownBy(() -> hearitClusterFeatureProcessor.processPage(0, 10))
                .isInstanceOf(RuntimeException.class);

        // then: 트랜잭션 전파 덕분에 직접 생성한 객체라도 롤백
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM hearit_cluster", Integer.class);
        assertThat(count).isZero();
    }
}
