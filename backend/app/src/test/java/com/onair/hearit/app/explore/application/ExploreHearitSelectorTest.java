package com.onair.hearit.app.explore.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.onair.hearit.app.explore.application.scoreprocessor.ExploreHearitSelector;
import com.onair.hearit.app.fixture.DbHelper;
import com.onair.hearit.core.config.DataSourceConfig;
import com.onair.hearit.core.domain.Category;
import com.onair.hearit.core.domain.Hearit;
import com.onair.hearit.core.domain.PlayingHistory;
import com.onair.hearit.core.fixture.TestFixture;
import com.onair.hearit.core.fixture.TestJpaAuditingConfig;
import com.onair.hearit.core.infrastructure.jpa.ClusteredHearitRepository;
import com.onair.hearit.core.infrastructure.jpa.HearitRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.LongStream;
import org.junit.jupiter.api.BeforeEach;
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
@Import({DbHelper.class, TestJpaAuditingConfig.class, DataSourceConfig.class})
class ExploreHearitSelectorTest {

    @Autowired
    private DbHelper dbHelper;

    @Autowired
    private HearitRepository hearitRepository;

    @Autowired
    private ClusteredHearitRepository clusteredHearitRepository;

    private Category category;
    private UUID userId;
    private ExploreHearitSelector exploreHearitSelector;

    @BeforeEach
    void setUp() {
        category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        userId = UUID.randomUUID();
        exploreHearitSelector = new ExploreHearitSelector(hearitRepository, clusteredHearitRepository);
    }

    @Test
    @DisplayName("시나리오 1: 선호 클러스터가 충분할 때 (70:30 비율 확인)")
    void select_whenPreferredClustersAreSufficient() {
        // Given: 각 클러스터당 50개씩 넉넉히 준비 (총 250개)
        List<Hearit> c1 = insertCluster(1, 50);
        List<Hearit> c2 = insertCluster(2, 50);
        List<Hearit> c3 = insertCluster(3, 50);
        List<Hearit> c4 = insertCluster(4, 50);
        List<Hearit> c5 = insertCluster(5, 50);

        // c1, c2, c3를 선호로 만듦
        insertPlayHistory(c1.subList(0, 10));
        insertPlayHistory(c2.subList(0, 10));
        insertPlayHistory(c3.subList(0, 10));

        // When
        List<Hearit> result = exploreHearitSelector.select(userId);

        // Then
        long preferredCount = countClusters(result, c1, c2, c3);
        long exploreCount = countClusters(result, c4, c5);

        assertAll(
                () -> assertThat(result).hasSize(100),
                () -> assertThat(preferredCount).isEqualTo(70),
                () -> assertThat(exploreCount).isEqualTo(30)
        );
    }

    @Test
    @DisplayName("시나리오 2: 선호 클러스터 콘텐츠가 부족할 때 (탐색 콘텐츠로 보충)")
    void select_whenPreferredClustersAreDeficient() {
        // Given: 선호 클러스터(1,2,3)에는 총 20개만 존재, 탐색 클러스터(4,5)에는 200개 존재
        List<Hearit> c1 = insertCluster(1, 10);
        List<Hearit> c2 = insertCluster(2, 5);
        List<Hearit> c3 = insertCluster(3, 5);
        List<Hearit> c4 = insertCluster(4, 100);
        List<Hearit> c5 = insertCluster(5, 100);

        insertPlayHistory(c1.subList(0, 1));
        insertPlayHistory(c2.subList(0, 1));
        insertPlayHistory(c3.subList(0, 1));

        // When
        List<Hearit> result = exploreHearitSelector.select(userId);

        // Then
        long preferredCount = countClusters(result, c1, c2, c3);
        long exploreCount = countClusters(result, c4, c5);

        assertAll(
                () -> assertThat(result).hasSize(100),
                () -> assertThat(preferredCount).isEqualTo(20),
                () -> assertThat(exploreCount).isEqualTo(80)
        );
    }

    @Test
    @DisplayName("시나리오 3: 신규 회원이라 선호 정보가 아예 없을 때 (전체 랜덤 100개)")
    void select_forNewUser_shouldReturnFullExplore() {
        // Given: 아무 이력 없음. 총 150개 콘텐츠 존재
        insertCluster(1, 30);
        insertCluster(2, 30);
        insertCluster(3, 30);
        insertCluster(4, 30);
        insertCluster(5, 30);

        // When
        List<Hearit> result = exploreHearitSelector.select(userId);

        // Then
        assertThat(result).hasSize(100);
        // 선호가 없으므로 내부적으로 loadExplore가 전체 대상(1~5)에서 100개를 뽑아와야 함
    }

    @Test
    @DisplayName("시나리오 4: 전체 DB 콘텐츠가 100개 미만일 때 (가용 최대치 반환)")
    void select_whenTotalContentsAreLessThanLimit() {
        // Given: 총 45개만 존재
        insertCluster(1, 15);
        insertCluster(2, 15);
        insertCluster(3, 15);

        // When
        List<Hearit> result = exploreHearitSelector.select(userId);

        // Then
        assertThat(result).hasSize(45);
    }

    @Test
    @DisplayName("시나리오 5: 클러스터링되지 않은 콘텐츠만 있을 때 (Fallback 동작 확인)")
    void select_whenOnlyNonClusteredContentsExist() {
        // Given: Hearit은 있지만 HearitCluster 매핑 정보가 없는 경우 (현실적으로 드물지만 안전장치 테스트)
        for (int i = 0; i < 50; i++) {
            dbHelper.insertHearit(TestFixture.createHearitWith("none-" + i, category));
        }

        // When
        List<Hearit> result = exploreHearitSelector.select(userId);

        // Then
        // loadPreferred, loadExplore는 0건이지만 loadFallback이 마지막에 50개를 다 가져와야 함
        assertThat(result).hasSize(50);
    }

    private List<Hearit> insertCluster(int clusterId, int count) {
        return LongStream.range(0, count)
                .mapToObj(i -> {
                    Hearit h = dbHelper.insertHearit(
                            TestFixture.createHearitWith("h-" + clusterId + "-" + i, category));
                    dbHelper.insertHearitCluster(TestFixture.createHearitClusterWithId(h, clusterId));
                    return h;
                })
                .toList();
    }

    private void insertPlayHistory(List<Hearit> hearits) {
        for (Hearit h : hearits) {
            dbHelper.insertPlayingHistory(new PlayingHistory(userId, h, 100));
        }
    }

    @SafeVarargs
    private final long countClusters(List<Hearit> result, List<Hearit>... clusters) {
        Set<Long> ids = new HashSet<>();
        for (List<Hearit> cluster : clusters) {
            ids.addAll(cluster.stream().map(Hearit::getId).toList());
        }
        return result.stream().map(Hearit::getId).filter(ids::contains).count();
    }
}
