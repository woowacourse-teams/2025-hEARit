package com.onair.hearit.app.explore.application.performance;

import com.onair.hearit.app.explore.application.HearitExploreService;
import com.onair.hearit.app.explore.dto.CursorRequest;
import com.onair.hearit.app.explore.dto.CursorResponseV2;
import com.onair.hearit.app.explore.dto.ExploredHearitResponse;
import com.onair.hearit.app.fixture.IntegrationTest;
import com.onair.hearit.core.domain.Category;
import com.onair.hearit.core.domain.Hearit;
import com.onair.hearit.core.domain.Member;
import com.onair.hearit.core.domain.PlayingHistory;
import com.onair.hearit.core.domain.UserInfo;
import com.onair.hearit.core.fixture.TestFixture;
import com.onair.hearit.core.infrastructure.jpa.CategoryRepository;
import com.onair.hearit.core.infrastructure.jpa.HearitRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * !WARN!: 기능의 작동을 테스트하는 클래스가 아닙니다. 기본 값은 Disabled.
 * <p>
 * ExploreScore의 추천 알고리즘을 출력해보기 위한 클래스 입니다.
 */
@Disabled
class ExplorePerformanceTest extends IntegrationTest {

    @Autowired
    private HearitExploreService hearitExploreService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private HearitRepository hearitRepository;
    @Autowired
    private CategoryRepository categoryRepository;

    @BeforeEach
    void setUp() {
        // 백엔드
        Category spring = new Category("spring", "#1883B5");
        Category java = new Category("java", "#1883B5");

        // android
        Category kotlin = new Category("kotlin", "#1883B5");
        Category android = new Category("android", "#1883B5");

        // CS
        Category os = new Category("os", "#1883B5");
        Category db = new Category("db", "#1883B5");

        // 보편적인 애들
        Category itTrend = new Category("itTrend", "#1883B5");
        Category software = new Category("software", "#1883B5");

        dbHelper.insertCategory(spring);
        insertHearits(spring, "spring");

        dbHelper.insertCategory(java);
        insertHearits(java, "java");

        dbHelper.insertCategory(kotlin);
        insertHearits(kotlin, "kotlin");

        dbHelper.insertCategory(android);
        insertHearits(android, "android");

        dbHelper.insertCategory(os);
        insertHearits(os, "os");

        dbHelper.insertCategory(db);
        insertHearits(db, "db");

        dbHelper.insertCategory(itTrend);
        insertHearits(itTrend, "itTrend");

        dbHelper.insertCategory(software);
        insertHearits(software, "software");
    }

    @Test
    @DisplayName("비회원 추천: 매번 다른 guest uuid로 Top-10의 id, 날짜(신선도) 확인")
    void guestTopKTest() {
        int runs = 5;
        int k = 10;

        List<List<Long>> topKIdResults = new ArrayList<>();
        List<List<Long>> topKDaysAgoResults = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        // when
        for (int i = 0; i < runs; i++) {
            UserInfo guestUserInfo = TestFixture.createFixedGuestUserInfo();

            CursorResponseV2<ExploredHearitResponse> exploredHearits = hearitExploreService.getExploredHearits(
                    guestUserInfo, new CursorRequest(0, k));

            List<Long> topKIds = exploredHearits.content()
                    .stream()
                    .limit(k)
                    .map(ExploredHearitResponse::id)
                    .toList();

            List<LocalDateTime> topKCreatedAts = topKIds.stream()
                    .map(id -> hearitRepository.findById(id).orElseThrow())
                    .map(Hearit::getCreatedAt)
                    .toList();

            List<Long> topKDaysAgo = topKCreatedAts.stream()
                    .map(createdAt -> java.time.Duration.between(createdAt, now).toDays())
                    .toList();

            topKIdResults.add(topKIds);
            topKDaysAgoResults.add(topKDaysAgo);
        }

        Set<Long> baseIds = new HashSet<>(topKIdResults.getFirst());

        // then
        System.out.println("[비회원 1회차] 추천 히어릿 ID 목록=" + topKIdResults.getFirst());
        System.out.println("[비회원 1회차] 추천 히어릿 신선도(daysAgo)=" + topKDaysAgoResults.getFirst());
        System.out.println();

        int idOverlapSum = 0;

        for (int i = 1; i < runs; i++) {
            List<Long> curIdList = topKIdResults.get(i);

            Set<Long> curIds = new HashSet<>(curIdList);
            curIds.retainAll(baseIds);
            int idOverlap = curIds.size();
            idOverlapSum += idOverlap;

            double idOverlapRate = (double) idOverlap / k;

            System.out.println("[비회원 " + (i + 1) + "회차] 추천 히어릿 ID 목록=" + curIdList);
            System.out.println("[비회원 " + (i + 1) + "회차] 추천 히어릿 신선도(daysAgo)=" + topKDaysAgoResults.get(i));
            System.out.println("[비회원 " + (i + 1) + "회차] 1회차 대비 ID 중복 개수=" + idOverlap + "/" + k
                    + " (중복률 " + String.format("%.0f%%", idOverlapRate * 100) + ")");
            System.out.println();
        }

        double avgIdOverlapRate = (double) idOverlapSum / ((runs - 1) * k);
        System.out.println("[비회원 요약] 평균 ID 중복률=" + String.format("%.1f%%", avgIdOverlapRate * 100));
    }

    @Test
    @DisplayName("회원 추천: 같은 회원의 Top-10의 id, 순서, 카테고리 분포 확인")
    void memberTopKTest() {
        int runs = 5;
        int k = 10;

        // given
        Member backendEngineer = TestFixture.createFixedMember();
        dbHelper.insertMember(backendEngineer);
        UserInfo memberUserInfo = TestFixture.createFixedMemberUserInfo(backendEngineer);

        addBookmarkAndPlayingHistory(backendEngineer, "java", 2, 5);
        addBookmarkAndPlayingHistory(backendEngineer, "spring", 1, 3);
        addBookmarkAndPlayingHistory(backendEngineer, "itTrend", 1, 4);

        List<List<Long>> topKIdsPerRun = new ArrayList<>();
        List<List<Long>> topKCategoryIdsPerRun = new ArrayList<>();

        // when
        for (int r = 0; r < runs; r++) {
            CursorResponseV2<ExploredHearitResponse> res = hearitExploreService.getExploredHearits(
                    memberUserInfo, new CursorRequest(0, k));

            List<Long> ids = res.content().stream()
                    .limit(k)
                    .map(ExploredHearitResponse::id)
                    .toList();
            topKIdsPerRun.add(ids);

            List<Hearit> hearits = ids.stream()
                    .map(id -> hearitRepository.findById(id).orElseThrow())
                    .toList();

            List<Long> categoryIds = hearits.stream()
                    .map(h -> h.getCategory().getId())
                    .toList();
            topKCategoryIdsPerRun.add(categoryIds);

            // then
            System.out.println("[회원 " + (r + 1) + "회차] 추천 히어릿 ID 목록=" + ids);
            System.out.println("[회원 " + (r + 1) + "회차] 추천 히어릿 카테고리 ID 목록=" + categoryIds);
            System.out.println("[회원 " + (r + 1) + "회차] 카테고리별 개수=" + countByValue(categoryIds));
            if (r > 0) {
                List<Long> baseIds = topKIdsPerRun.getFirst();
                int samePositionCount = countSamePositions(baseIds, ids);
                double samePositionRate = (double) samePositionCount / k;

                String verdict = samePositionRate >= 0.5 ? "⚠️ 순서가 너무 비슷함" : "OK";
                System.out.println("[회원 " + (r + 1) + "회차] 1회차와 같은 위치의 ID 개수=" + samePositionCount + "/" + k
                        + " (일치율 " + String.format("%.0f%%", samePositionRate * 100) + ") " + verdict);
            }
            System.out.println();
        }

        List<Long> baseCategoryIds = topKCategoryIdsPerRun.getFirst();
        int categoryOverlapSum = 0;
        for (int i = 1; i < runs; i++) {
            categoryOverlapSum += multisetOverlap(baseCategoryIds, topKCategoryIdsPerRun.get(i));
        }
        double avgCategoryOverlapRate = (double) categoryOverlapSum / ((runs - 1) * k);
        System.out.println("[회원 요약] 1회차 대비 평균 카테고리 중복률="
                + String.format("%.1f%%", avgCategoryOverlapRate * 100));
    }

    private void addBookmarkAndPlayingHistory(Member member, String categoryName, int bookmarkCount, int playCount) {
        Category category = categoryRepository.findByName(categoryName).get();
        List<Hearit> hearits = hearitRepository.findByCategory(category.getId(), 10);

        hearits.stream()
                .limit(bookmarkCount)
                .map(h -> TestFixture.createFixedBookmark(member, h))
                .forEach(bookmark -> dbHelper.insertBookmark(bookmark));
        hearits.stream()
                .limit(playCount)
                .map(hearit -> new PlayingHistory(member.getUuid(), hearit, 300L))
                .forEach(playingHistory -> dbHelper.insertPlayingHistory(playingHistory));
    }

    private int multisetOverlap(List<Long> a, List<Long> b) {
        Map<Long, Integer> freq = new HashMap<>();
        for (Long v : a) {
            freq.put(v, freq.getOrDefault(v, 0) + 1);
        }

        int overlap = 0;
        for (Long v : b) {
            Integer c = freq.get(v);
            if (c != null && c > 0) {
                overlap++;
                freq.put(v, c - 1);
            }
        }
        return overlap;
    }

    private Map<Long, Integer> countByValue(List<Long> values) {
        Map<Long, Integer> freq = new HashMap<>();
        for (Long v : values) {
            freq.put(v, freq.getOrDefault(v, 0) + 1);
        }
        return freq;
    }

    private int countSamePositions(List<Long> a, List<Long> b) {
        int n = Math.min(a.size(), b.size());
        int same = 0;
        for (int i = 0; i < n; i++) {
            if (a.get(i).equals(b.get(i))) {
                same++;
            }
        }
        return same;
    }

    private void insertHearits(Category category, String baseTitle) {
        LocalDateTime now = LocalDateTime.now();

        for (int i = 1; i <= 10; i++) {
            int daysAgo = i * 2;
            String title = baseTitle + i + " [" + daysAgo + "d_ago]";

            Hearit hearit = TestFixture.createHearitWith(title, category);
            Hearit saved = dbHelper.insertHearit(hearit);
            Long id = saved.getId();

            // Auditing(@CreatedDate)이 넣은 값을 db에서 직접 덮어쓰기
            jdbcTemplate.update(
                    "UPDATE hearit SET created_at = ? WHERE id = ?",
                    java.sql.Timestamp.valueOf(now.minusDays(daysAgo)),
                    id);
        }
    }
}
