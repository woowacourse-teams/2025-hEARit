package com.onair.hearit.app.hearit.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.onair.hearit.app.auth.infrastructure.jwt.JwtTokenProvider;
import com.onair.hearit.app.common.dto.response.PagedResponse;
import com.onair.hearit.app.fixture.IntegrationTest;
import com.onair.hearit.app.hearit.dto.HearitDetailResponse;
import com.onair.hearit.app.hearit.dto.HearitOfCategoryResponse;
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
        PagedResponse<HearitOfCategoryResponse> pagedResponse = RestAssured.given(this.spec)
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
        List<HearitOfCategoryResponse> responses = pagedResponse.content();

        // then
        assertAll(
                () -> assertThat(responses).hasSize(2),
                () -> assertThat(responses.get(0).id()).isEqualTo(hearit2.getId()), // 최신 hearit 먼저
                () -> assertThat(responses.get(1).id()).isEqualTo(hearit1.getId())
        );
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
