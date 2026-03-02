package com.onair.hearit.app.hearit.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.onair.hearit.app.auth.infrastructure.jwt.JwtTokenProvider;
import com.onair.hearit.app.common.dto.response.PagedResponse;
import com.onair.hearit.app.fixture.IntegrationTest;
import com.onair.hearit.app.hearit.dto.HearitDetailResponse;
import com.onair.hearit.app.hearit.dto.HearitOverviewResponse;
import com.onair.hearit.core.domain.Category;
import com.onair.hearit.core.domain.Hearit;
import com.onair.hearit.core.domain.HearitKeyword;
import com.onair.hearit.core.domain.Keyword;
import com.onair.hearit.core.domain.Member;
import com.onair.hearit.core.domain.PlayingHistory;
import com.onair.hearit.core.domain.Source;
import com.onair.hearit.core.fixture.TestFixture;
import com.onair.hearit.core.infrastructure.jpa.HearitRepository;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

class HearitIntegrationTest extends IntegrationTest {

    @Autowired
    JwtTokenProvider jwtTokenProvider;

    @Autowired
    HearitRepository hearitRepository;

    @Nested
    class HearitDetailTest {

        @Test
        @DisplayName("로그인한 사용자가 히어릿 단일 조회 시, 200 OK 및 히어릿 정보를 제공한다.")
        void readHearitWithSuccessWithMember() {
            // given
            Member member = dbHelper.insertMember(TestFixture.createFixedMember());
            String token = generateToken(member);
            Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
            Hearit hearit = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
            PlayingHistory playingHistory = dbHelper.insertPlayingHistory(
                    new PlayingHistory(member.getUuid(), hearit, 1_000));
            Keyword keyword1 = dbHelper.insertKeyword(new Keyword("Java"));
            Keyword keyword2 = dbHelper.insertKeyword(new Keyword("Spring"));
            dbHelper.insertHearitKeyword(new HearitKeyword(hearit, keyword1));
            dbHelper.insertHearitKeyword(new HearitKeyword(hearit, keyword2));

            // when & then
            HearitDetailResponse response = RestAssured.given(HearitIntegrationTest.this.spec)
                    .header("Authorization", "Bearer " + token)
                    .when()
                    .get("/api/v1/hearits/{hearitId}", hearit.getId())
                    .then().log().all()
                    .statusCode(HttpStatus.OK.value())
                    .extract().as(HearitDetailResponse.class);

            assertThat(response.id()).isEqualTo(hearit.getId());
        }

        @Test
        @DisplayName("로그인 하지 않은 사용자가 히어릿 단일 조회 시, 200 OK 및 히어릿 정보를 제공한다.")
        void readHearitWithSuccessWithNotMember() {
            // given
            Member member = dbHelper.insertMember(TestFixture.createFixedMember());
            String token = generateToken(member);
            Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
            Hearit hearit = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
            Keyword keyword1 = dbHelper.insertKeyword(new Keyword("Java"));
            dbHelper.insertHearitKeyword(new HearitKeyword(hearit, keyword1));

            // when & then
            HearitDetailResponse response = RestAssured.given(HearitIntegrationTest.this.spec)
                    .when()
                    .get("/api/v1/hearits/{hearitId}", hearit.getId())
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .extract().as(HearitDetailResponse.class);

            assertAll(
                    () -> assertThat(response.id()).isEqualTo(hearit.getId()),
                    () -> assertThat(response.isBookmarked()).isFalse()
            );
        }

        @Test
        @DisplayName("히어릿 단일 조회 시, 존재하지 않는 아이디인 경우 404 NOT_FOUND를 반환한다.")
        void readHearitWithNotFound() {
            // given
            Member member = dbHelper.insertMember(TestFixture.createFixedMember());
            String token = generateToken(member);
            Long notFoundHearitId = 9999L;

            // when & then
            RestAssured.given(HearitIntegrationTest.this.spec)
                    .header("Authorization", "Bearer " + token)
                    .when()
                    .get("/api/v1/hearits/{hearitId}", notFoundHearitId)
                    .then()
                    .statusCode(HttpStatus.NOT_FOUND.value());
        }
    }

    @Nested
    class FilteredHearitTest {

        @Test
        @DisplayName("카테고리로 히어릿 검색 시 200 OK 및 해당 카테고리의 히어릿들을 최신순으로 반환한다.")
        void getHearitsByCategoryWithPagination() {
            // given
            Member member = dbHelper.insertMember(TestFixture.createFixedMember());
            String token = generateToken(member);
            Category category1 = dbHelper.insertCategory(new Category("Spring", "#000001"));
            Category category2 = dbHelper.insertCategory(new Category("Java", "#000002"));

            Keyword keyword = dbHelper.insertKeyword(TestFixture.createFixedKeyword());
            Hearit hearit1 = saveHearitWithCategoryAndKeyword(category1, keyword);
            Hearit hearit2 = saveHearitWithCategoryAndKeyword(category1, keyword);
            Hearit hearit3 = saveHearitWithCategoryAndKeyword(category2, keyword); // 카테고리 2의 히어릿

            // when
            PagedResponse<HearitOverviewResponse> pagedResponse = RestAssured.given(HearitIntegrationTest.this.spec)
                    .header("Authorization", "Bearer " + token)
                    .queryParam("categoryId", category1.getId())
                    .queryParam("page", 0)
                    .queryParam("size", 10)
                    .when()
                    .get("/api/v1/hearits")
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .extract()
                    .as(new TypeRef<>() {
                    });
            List<HearitOverviewResponse> responses = pagedResponse.content();

            // then
            assertAll(
                    () -> assertThat(responses).hasSize(2),
                    () -> assertThat(responses.get(0).id()).isEqualTo(hearit2.getId()), // 최신 hearit 먼저
                    () -> assertThat(responses.get(1).id()).isEqualTo(hearit1.getId())
            );
        }

        @Test
        @DisplayName("히어릿을 요청 파라미터 기준으로 정렬하여 반환한다.")
        void readRecentHearit() {
            // given
            Member member = dbHelper.insertMember(TestFixture.createFixedMember());
            String token = generateToken(member);
            Category category1 = dbHelper.insertCategory(new Category("Java", "#FF0000"));
            Category category2 = dbHelper.insertCategory(new Category("Spring", "#00FF00"));
            Category category3 = dbHelper.insertCategory(new Category("React1", "#0000FF"));

            dbHelper.insertHearit(TestFixture.createFixedHearitWith(category1));
            dbHelper.insertHearit(TestFixture.createFixedHearitWith(category1));
            dbHelper.insertHearit(TestFixture.createFixedHearitWith(category1));
            dbHelper.insertHearit(TestFixture.createFixedHearitWith(category2));
            dbHelper.insertHearit(TestFixture.createFixedHearitWith(category2));
            dbHelper.insertHearit(TestFixture.createFixedHearitWith(category2));
            dbHelper.insertHearit(TestFixture.createFixedHearitWith(category3));
            dbHelper.insertHearit(TestFixture.createFixedHearitWith(category3));
            dbHelper.insertHearit(TestFixture.createFixedHearitWith(category3));
            dbHelper.insertHearit(TestFixture.createFixedHearitWith(category3));

            // when
            PagedResponse<HearitOverviewResponse> pagedResponse = RestAssured.given(HearitIntegrationTest.this.spec)
                    .header("Authorization", "Bearer " + token)
                    .queryParam("sort", "createdAt,asc") // 카테고리 상관없이 필터링
                    .queryParam("page", 0)
                    .queryParam("size", 10)
                    .when()
                    .get("/api/v1/hearits")
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .extract()
                    .as(new TypeRef<>() {
                    });
            List<HearitOverviewResponse> responses = pagedResponse.content();

            // then
            assertThat(responses).hasSize(10);
        }
    }

    @Nested
    class ViewCountTest {

        @Test
        @DisplayName("히어릿 조회수 증가 API 호출 시 204 No Content와 함께 viewCount가 1 증가한다.")
        void increaseViewCount_success() {
            // given
            Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
            Hearit hearit = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
            Long hearitId = hearit.getId();

            // when
            RestAssured.given(HearitIntegrationTest.this.spec)
                    .when()
                    .post("/api/v1/hearits/{hearitId}/view", hearitId)
                    .then()
                    .statusCode(HttpStatus.NO_CONTENT.value());

            // then
            Hearit updatedHearit = hearitRepository.findById(hearitId).get();
            assertThat(updatedHearit.getViewCount()).isEqualTo(1L);
        }

        @Test
        @DisplayName("TTL 내 동일 사용자의 중복 조회 요청은 viewCount가 증가하지 않는다.")
        void increaseViewCount_duplicatedKey_beforeTTL() {
            // given
            Member member = dbHelper.insertMember(TestFixture.createFixedMember());
            String token = generateToken(member);
            Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
            Hearit hearit = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
            Long hearitId = hearit.getId();

            // when
            RestAssured.given(spec)
                    .when()
                    .header("Authorization", "Bearer " + token)
                    .post("/api/v1/hearits/{hearitId}/view", hearitId)
                    .then()
                    .statusCode(HttpStatus.NO_CONTENT.value());

            RestAssured.given(spec)
                    .header("Authorization", "Bearer " + token)
                    .when()
                    .post("/api/v1/hearits/{hearitId}/view", hearitId)
                    .then()
                    .statusCode(HttpStatus.NO_CONTENT.value());

            // then
            Hearit updatedHearit = hearitRepository.findById(hearitId).get();
            assertThat(updatedHearit.getViewCount()).isEqualTo(1L);
        }

        @Test
        @Disabled("CI 환경에서는 느릴 수 있어서 비활성화합니다.")
        @DisplayName("TTL 만료 이후에는 동일 사용자 요청 시 viewCount가 다시 증가한다")
        void increaseViewCount_afterTTL() throws InterruptedException {
            // given
            Member member = dbHelper.insertMember(TestFixture.createFixedMember());
            String token = generateToken(member);
            Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
            Hearit hearit = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
            Long hearitId = hearit.getId();

            // when
            RestAssured.given(spec)
                    .header("Authorization", "Bearer " + token)
                    .when()
                    .post("/api/v1/hearits/{hearitId}/view", hearitId)
                    .then()
                    .statusCode(HttpStatus.NO_CONTENT.value());

            Thread.sleep(11_000); // TTL 대기 (ViewCountRateLimiter의 TTL_SECONDS보다 크게)

            RestAssured.given(spec)
                    .header("Authorization", "Bearer " + token)
                    .when()
                    .post("/api/v1/hearits/{hearitId}/view", hearitId)
                    .then()
                    .statusCode(HttpStatus.NO_CONTENT.value());

            // then
            Hearit updatedHearit = hearitRepository.findById(hearitId).get();
            assertThat(updatedHearit.getViewCount()).isEqualTo(2L);
        }

        @Test
        @DisplayName("히어릿 조회수 증가 시 존재하지 않는 ID면 404를 반환한다.")
        void increaseViewCount_notFound() {
            // given
            Long notFoundHearitId = 9999L;

            // when & then
            RestAssured.given(HearitIntegrationTest.this.spec)
                    .when()
                    .post("/api/v1/hearits/{hearitId}/view", notFoundHearitId)
                    .then()
                    .statusCode(HttpStatus.NOT_FOUND.value());
        }

        @Test
        @Disabled("멀티 스레드 환경에서의 DB 동시성 검증 테스트입니다. CI 환경에서는 스레드 스케줄링 및 DB 상태에 따라 결과가 예측 불가능해 비활성화합니다.")
        @DisplayName("동일 사용자의 동시 요청은 1번만 조회수가 증가한다.")
        void increaseViewCount_concurrent_onlyOne() throws InterruptedException {
            // given
            Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
            Hearit hearit = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
            Long hearitId = hearit.getId();
            Member member = dbHelper.insertMember(TestFixture.createFixedMember());
            String token = generateToken(member);

            int threadCount = 50;
            ExecutorService executorService = Executors.newFixedThreadPool(10);
            CountDownLatch latch = new CountDownLatch(threadCount);

            // when
            for (int i = 0; i < threadCount; i++) {
                executorService.execute(() -> {
                    try {
                        RestAssured.given(HearitIntegrationTest.this.spec)
                                .header("Authorization", "Bearer " + token) // 동일 토큰 주입
                                .when()
                                .post("/api/v1/hearits/{hearitId}/view", hearitId)
                                .then()
                                .statusCode(HttpStatus.NO_CONTENT.value());
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await();
            executorService.shutdown();

            // then
            Hearit updatedHearit = hearitRepository.findById(hearitId).get();
            assertThat(updatedHearit.getViewCount()).isEqualTo(1L);
        }

        @Test
        @Disabled("멀티 스레드 환경에서의 DB 동시성 검증 테스트입니다. CI 환경에서는 스레드 스케줄링 및 DB 상태에 따라 결과가 예측 불가능해 비활성화합니다.")
        @DisplayName("서로 다른 사용자의 동시 요청은 요청 수 만큼 조회수가 증가한다.")
        void increaseViewCount_concurrent_differentUsers() throws InterruptedException {
            // given
            Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
            Hearit hearit = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
            Long hearitId = hearit.getId();

            int threadCount = 50;
            ExecutorService executorService = Executors.newFixedThreadPool(10);
            CountDownLatch latch = new CountDownLatch(threadCount);

            List<String> tokens = IntStream.range(0, threadCount)
                    .mapToObj(i -> {
                        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
                        return generateToken(member);
                    })
                    .toList();

            // when
            for (int i = 0; i < threadCount; i++) {
                final String token = tokens.get(i);
                executorService.execute(() -> {
                    try {
                        RestAssured.given(HearitIntegrationTest.this.spec)
                                .header("Authorization", "Bearer " + token)
                                .when()
                                .post("/api/v1/hearits/{hearitId}/view", hearitId)
                                .then()
                                .statusCode(HttpStatus.NO_CONTENT.value());
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await();
            executorService.shutdown();

            // then
            Hearit updatedHearit = hearitRepository.findById(hearitId).get();
            assertThat(updatedHearit.getViewCount()).isEqualTo(threadCount);
        }
    }

    private String generateToken(Member member) {
        return jwtTokenProvider.createAccessToken(member.getUuid());
    }

    private Hearit saveHearitWithCategoryAndKeyword(Category category, Keyword keyword) {
        Hearit hearit = new Hearit(
                "title",
                "summary",
                100,
                "/hearit/audio/original/ORG_test.mp3",
                "/hearit/audio/short/SHR_test.mp3",
                "/hearit/script/SCR_test.json",
                List.of(new Source("출처", "url")),
                category);
        Hearit savedHearit = dbHelper.insertHearit(hearit);
        dbHelper.insertHearitKeyword(new HearitKeyword(savedHearit, keyword));
        return savedHearit;
    }
}
