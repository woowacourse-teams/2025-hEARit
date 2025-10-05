package com.onair.hearit.app.hearit.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.onair.hearit.app.auth.infrastructure.jwt.JwtTokenProvider;
import com.onair.hearit.app.common.dto.response.PagedResponse;
import com.onair.hearit.app.fixture.IntegrationTest;
import com.onair.hearit.app.hearit.dto.HearitDetailResponse;
import com.onair.hearit.app.hearit.dto.HearitOverviewResponse;
import com.onair.hearit.app.hearit.dto.HearitsWithRecommendCategoryResponse;
import com.onair.hearit.core.domain.Category;
import com.onair.hearit.core.domain.Hearit;
import com.onair.hearit.core.domain.HearitKeyword;
import com.onair.hearit.core.domain.Keyword;
import com.onair.hearit.core.domain.Member;
import com.onair.hearit.core.domain.PlayingHistory;
import com.onair.hearit.core.domain.Source;
import com.onair.hearit.core.fixture.TestFixture;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

class HearitIntegrationTest extends IntegrationTest {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("로그인한 사용자가 히어릿 단일 조회 시, 200 OK 및 히어릿 정보를 제공한다.")
    void readHearitWithSuccessWithMember() {
        // given
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
        String token = generateToken(member);
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Hearit hearit = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        PlayingHistory playingHistory = dbHelper.insertPlayingHistory(
                new PlayingHistory(member.getId(), hearit, 1_000));
        Keyword keyword1 = dbHelper.insertKeyword(new Keyword("Java"));
        Keyword keyword2 = dbHelper.insertKeyword(new Keyword("Spring"));
        dbHelper.insertHearitKeyword(new HearitKeyword(hearit, keyword1));
        dbHelper.insertHearitKeyword(new HearitKeyword(hearit, keyword2));

        // when & then
        HearitDetailResponse response = RestAssured.given(this.spec)
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
        HearitDetailResponse response = RestAssured.given(this.spec)
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
        RestAssured.given(this.spec)
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/api/v1/hearits/{hearitId}", notFoundHearitId)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    @DisplayName("카테고리별로 그룹화된 히어릿들을 조회 시, 추천하는 3개의 카테고리와 히어릿들을 반환한다.")
    void readCategoriesHearit() {
        // given
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
        String token = generateToken(member);

        Category category1 = dbHelper.insertCategory(new Category("Java", "#FF0000"));
        Category category2 = dbHelper.insertCategory(new Category("Spring", "#00FF00"));
        Category category3 = dbHelper.insertCategory(new Category("React1", "#0000FF"));
        Category category4 = dbHelper.insertCategory(new Category("React2", "#0000FF"));
        Category category5 = dbHelper.insertCategory(new Category("React3", "#0000FF"));
        Category category6 = dbHelper.insertCategory(new Category("React4", "#0000FF"));

        Hearit hearit11 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category1));
        Hearit hearit12 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category1));
        Hearit hearit13 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category1));
        Hearit hearit21 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category2));
        Hearit hearit22 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category2));
        Hearit hearit31 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category3));

        dbHelper.insertBookmark(TestFixture.createFixedBookmark(member, hearit11));
        dbHelper.insertBookmark(TestFixture.createFixedBookmark(member, hearit12));
        dbHelper.insertBookmark(TestFixture.createFixedBookmark(member, hearit13));
        dbHelper.insertBookmark(TestFixture.createFixedBookmark(member, hearit21));
        dbHelper.insertBookmark(TestFixture.createFixedBookmark(member, hearit22));
        dbHelper.insertBookmark(TestFixture.createFixedBookmark(member, hearit31));

        // when
        List<HearitsWithRecommendCategoryResponse> responses = RestAssured.given(this.spec)
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/api/v1/hearits/recommend-category")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .jsonPath()
                .getList(".", HearitsWithRecommendCategoryResponse.class);

        // then
        assertAll(() -> {
            assertThat(responses).hasSize(3);
            assertThat(responses.get(0).hearits()).hasSize(3);
            assertThat(responses.get(1).hearits()).hasSize(2);
            assertThat(responses.get(2).hearits()).hasSize(1);
            assertThat(responses.get(0).categoryId()).isEqualTo(category1.getId());
            assertThat(responses.get(1).categoryId()).isEqualTo(category2.getId());
            assertThat(responses.get(2).categoryId()).isEqualTo(category3.getId());
        });
    }

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
        PagedResponse<HearitOverviewResponse> pagedResponse = RestAssured.given(this.spec)
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
        PagedResponse<HearitOverviewResponse> pagedResponse = RestAssured.given(this.spec)
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

    private String generateToken(Member member) {
        return jwtTokenProvider.createAccessToken(member.getId());
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
