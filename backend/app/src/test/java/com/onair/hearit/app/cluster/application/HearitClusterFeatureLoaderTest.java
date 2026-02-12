package com.onair.hearit.app.cluster.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.onair.hearit.app.fixture.DbHelper;
import com.onair.hearit.core.config.DataSourceConfig;
import com.onair.hearit.core.domain.Category;
import com.onair.hearit.core.fixture.TestFixture;
import com.onair.hearit.core.fixture.TestJpaAuditingConfig;
import com.onair.hearit.core.infrastructure.jdbc.HearitClusterCommandRepository;
import com.onair.hearit.core.infrastructure.jpa.ClusteredHearitRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

@DataJpaTest
@Sql("/dbclean.sql")
@ActiveProfiles("integration-test")
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Import({
        HearitClusterFeatureLoader.class,
        FeatureProcessor.class,
        HearitClusterCommandRepository.class,
        DbHelper.class,
        TestJpaAuditingConfig.class,
        DataSourceConfig.class
})
class HearitClusterFeatureLoaderTest {

    @Autowired
    private DbHelper dbHelper;

    @Autowired
    private HearitClusterFeatureLoader hearitClusterFeatureLoader;

    @Autowired
    private ClusteredHearitRepository clusteredHearitRepository;

    @Test
    @DisplayName("페이지 단위로 히어릿 데이터를 읽어와서 cluster 엔티티로 변환 후 저장한다.")
    void loadStatisticsFeature_success() {
        // given
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        int totalHearits = 10;
        for (int i = 0; i < totalHearits; i++) {
            dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        }

        int pageSize = 3; // 4번에 걸쳐 처리 (3, 3, 3, 1)

        // when
        hearitClusterFeatureLoader.loadStatisticsFeature(pageSize);

        // then
        long savedCount = clusteredHearitRepository.count();
        assertThat(savedCount).isEqualTo(totalHearits);
    }

    @Test
    @DisplayName("데이터가 없는 경우 저장되는 cluster 데이터도 없어야 한다.")
    void loadStatisticsFeature_empty() {
        // given
        int pageSize = 10;

        // when
        hearitClusterFeatureLoader.loadStatisticsFeature(pageSize);

        // then
        assertThat(clusteredHearitRepository.count()).isZero();
    }
}
