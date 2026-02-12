package com.onair.hearit.core.infrastructure.jpa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.onair.hearit.core.config.DataSourceConfig;
import com.onair.hearit.core.domain.Bookmark;
import com.onair.hearit.core.domain.Category;
import com.onair.hearit.core.domain.Hearit;
import com.onair.hearit.core.domain.Member;
import com.onair.hearit.core.domain.PlayingHistory;
import com.onair.hearit.core.domain.Reaction;
import com.onair.hearit.core.domain.ReactionType;
import com.onair.hearit.core.domain.Source;
import com.onair.hearit.core.fixture.DbHelper;
import com.onair.hearit.core.fixture.TestFixture;
import com.onair.hearit.core.fixture.TestJpaAuditingConfig;
import com.onair.hearit.core.infrastructure.projection.HearitClusterStatisticsProjection;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.transaction.TestTransaction;

@DataJpaTest
@Sql("/dbclean.sql")
@ActiveProfiles("integration-test")
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Import({DbHelper.class, TestJpaAuditingConfig.class, DataSourceConfig.class})
public class HearitRepositoryStatisticsTest {

    @Autowired
    private DbHelper dbHelper;

    @Autowired
    private HearitRepository hearitRepository;

    @Test
    @DisplayName("군집 분석을 위한 히어릿별 통계 데이터를 정확하게 조회한다.")
    void findClusterStatistics() {
        // given: 통계값 산출을 위한 기초 데이터 준비
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Member member1 = dbHelper.insertMember(Member.createLocalUser(java.util.UUID.randomUUID(), "u1", "n1", "p1", "i1"));
        Member member2 = dbHelper.insertMember(Member.createLocalUser(java.util.UUID.randomUUID(), "u2", "n2", "p2", "i2"));

        // 입력 변수 설정
        int inputViewCount = 500;
        Hearit hearit = saveHearit(inputViewCount, category);

        // 재생 시간 관련 변수 (단위: ms)
        long totalPlayTimeMs = (long) hearit.getPlayTime() * 1000;
        long member1PlayTimeMs = totalPlayTimeMs; // 완청 시나리오
        long member2PlayTimeMs = 1000L;           // 미완청 시나리오 (1초)

        // 활동 데이터 데이터 저장
        dbHelper.insertReaction(new Reaction(member1.getUuid(), hearit, ReactionType.LIKE));
        dbHelper.insertReaction(new Reaction(member2.getUuid(), hearit, ReactionType.LIKE));
        dbHelper.insertBookmark(new Bookmark(member1.getUuid(), hearit));

        // 재생 기록 저장
        dbHelper.insertPlayingHistory(new PlayingHistory(member1.getUuid(), hearit, member1PlayTimeMs));
        dbHelper.insertPlayingHistory(new PlayingHistory(member2.getUuid(), hearit, member2PlayTimeMs));

        flushAndClear();

        // 기대값 계산
        long expectedLikeCount = 2L;
        long expectedBookmarkCount = 1L;
        double expectedAvgPlayTime = (member1PlayTimeMs + member2PlayTimeMs) / 2.0;
        double expectedCompletionRate = (1.0 + 0.0) / 2.0; // member1은 완청(1.0), member2는 미완청(0.0)이므로 평균은 0.5

        // when
        Page<HearitClusterStatisticsProjection> result = hearitRepository.findClusterStatistics(
                PageRequest.of(0, 10)
        );

        // then
        HearitClusterStatisticsProjection stats = result.getContent().get(0);
        assertAll(
                () -> assertThat(stats.getHearitId()).isEqualTo(hearit.getId()),
                () -> assertThat(stats.getViewCount()).isEqualTo((long) inputViewCount),
                () -> assertThat(stats.getLikeCount()).isEqualTo(expectedLikeCount),
                () -> assertThat(stats.getBookmarkCount()).isEqualTo(expectedBookmarkCount),
                () -> assertThat(stats.getAvgPlayTime()).isEqualTo(expectedAvgPlayTime),
                () -> assertThat(stats.getCompletionRate()).isEqualTo(expectedCompletionRate)
        );
    }

    private Hearit saveHearit(int viewCount, Category category) {
        Hearit hearit = new Hearit(
                "통계 대상 히어릿",
                "테스트 요약",
                500,
                "/hearit/audio/original/ORG_bf7c513e-579e-4224-8505-3824bb22ed01.mp3",
                "/hearit/audio/short/SHR_bf7c513e-579e-4224-8505-3824bb22ed01.mp3",
                "/hearit/script/SCR_bf7c513e-579e-4224-8505-3824bb22ed01.json",
                List.of(
                        new Source("이 컨텐츠는 쿠버네티스 공식 문서 (저작자: The Kubernetes Authors)를 참고하여 만들어졌습니다.",
                                "https://example.com/1"),
                        new Source("원본은 CC BY 4.0 라이선스를 따릅니다.", "https://example.com/2")
                ),
                category,
                viewCount);
        return hearitRepository.save(hearit);
    }

    private void flushAndClear() {
        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();
    }
}
