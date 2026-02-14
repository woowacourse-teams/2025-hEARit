package com.onair.hearit.core.infrastructure.jpa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.onair.hearit.core.config.DataSourceConfig;
import com.onair.hearit.core.domain.Category;
import com.onair.hearit.core.domain.Hearit;
import com.onair.hearit.core.domain.HearitCluster;
import com.onair.hearit.core.domain.PlayingHistory;
import com.onair.hearit.core.fixture.DbHelper;
import com.onair.hearit.core.fixture.TestFixture;
import com.onair.hearit.core.fixture.TestJpaAuditingConfig;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
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
class ClusteredHearitRepositoryTest {

    @Autowired
    private ClusteredHearitRepository clusteredHearitRepository;

    @Autowired
    private DbHelper dbHelper;

    @Test
    @DisplayName("재생 기록을 기반으로 가장 많이 청취한 순서대로 클러스터 ID를 반환한다.")
    void findTopClusterIdsByUser_Success() {
        // given
        UUID userUuid = UUID.randomUUID();
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());

        Hearit h1 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        Hearit h2 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        Hearit h3 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));

        // 클러스터 정보 저장 (h1, h2 -> Cluster 10 / h3 -> Cluster 20)
        dbHelper.insertHearitCluster(createCluster(h1.getId(), 10));
        dbHelper.insertHearitCluster(createCluster(h2.getId(), 10));
        dbHelper.insertHearitCluster(createCluster(h3.getId(), 20));

        // 재생 기록 생성 (Cluster 10 총 2회 / Cluster 20 총 1회)
        dbHelper.insertPlayingHistory(new PlayingHistory(userUuid, h1, 10));
        dbHelper.insertPlayingHistory(new PlayingHistory(userUuid, h2, 20));
        dbHelper.insertPlayingHistory(new PlayingHistory(userUuid, h3, 30));

        // when
        List<Integer> topClusters = clusteredHearitRepository.findTopClusterIdsByUser(userUuid, 5);

        // then
        assertThat(topClusters).containsExactly(10, 20);
    }

    @Test
    @DisplayName("제외 리스트에 포함되지 않은 클러스터 ID들을 랜덤하게 가져온다.")
    void findRandomClusterIdsExcluding_Success() {
        // given
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Hearit h1 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        Hearit h2 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        Hearit h3 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));

        dbHelper.insertHearitCluster(createCluster(h1.getId(), 100));
        dbHelper.insertHearitCluster(createCluster(h2.getId(), 200));
        dbHelper.insertHearitCluster(createCluster(h3.getId(), 300));

        List<Integer> excludedIds = List.of(100);

        // when
        List<Integer> result = clusteredHearitRepository.findRandomClusterIdsExcluding(excludedIds, 2);

        // then
        assertAll(
                () -> assertThat(result).hasSize(2),
                () -> assertThat(result).doesNotContain(100),
                () -> assertThat(result).containsAnyOf(200, 300)
        );
    }

    @Test
    @DisplayName("제외 리스트가 비어있어도(첫 이용 유저) 랜덤 클러스터 ID를 가져와야 한다.")
    void findRandomClusterIdsExcluding_EmptyList() {
        // given
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        dbHelper.insertHearitCluster(
                createCluster(dbHelper.insertHearit(TestFixture.createFixedHearitWith(category)).getId(), 100));
        dbHelper.insertHearitCluster(
                createCluster(dbHelper.insertHearit(TestFixture.createFixedHearitWith(category)).getId(), 200));

        List<Integer> excludedIds = List.of();

        // when
        List<Integer> result = clusteredHearitRepository.findRandomClusterIdsExcluding(excludedIds, 2);

        // then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyInAnyOrder(100, 200);
    }

    @Test
    @DisplayName("특정 클러스터에 속한 히어릿 ID들을 지정된 개수만큼 랜덤하게 반환한다.")
    void findRandomHearitIdsByCluster_Success() {
        // given
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Hearit h1 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        Hearit h2 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));

        dbHelper.insertHearitCluster(createCluster(h1.getId(), 777));
        dbHelper.insertHearitCluster(createCluster(h2.getId(), 777));

        // when
        List<Long> result = clusteredHearitRepository.findRandomHearitIdsByCluster(777, 1);

        // then
        assertAll(
                () -> assertThat(result).hasSize(1),
                () -> assertThat(result.get(0)).isIn(h1.getId(), h2.getId())
        );
    }

    private HearitCluster createCluster(Long hearitId, int clusterId) {
        return new HearitCluster(
                hearitId, 100L, 10L, 5L, 120.5, 0.8,
                LocalDateTime.now().minusDays(1), clusterId, LocalDateTime.now()
        );
    }
}
