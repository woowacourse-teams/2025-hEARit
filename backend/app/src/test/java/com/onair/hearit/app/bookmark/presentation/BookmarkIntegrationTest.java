package com.onair.hearit.app.bookmark.presentation;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.onair.hearit.app.auth.infrastructure.jwt.JwtTokenProvider;
import com.onair.hearit.app.bookmark.dto.BookmarkInfoResponse;
import com.onair.hearit.app.fixture.IntegrationTest;
import com.onair.hearit.core.domain.Bookmark;
import com.onair.hearit.core.domain.Category;
import com.onair.hearit.core.domain.Hearit;
import com.onair.hearit.core.domain.Member;
import com.onair.hearit.core.domain.PlayingHistory;
import com.onair.hearit.core.fixture.TestFixture;
import io.restassured.RestAssured;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

class BookmarkIntegrationTest extends IntegrationTest {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("로그인한 사용자가 북마크 목록 조회 시, 200 OK 및 페이지에 따른 북마크 목록을 반환한다.")
    void readBookmarkHearitsTest_v2() {
        // given
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        String token = generateToken(member);
        int bookmarkCount = 30;
        for (int i = 0; i < bookmarkCount; i++) {
            Hearit hearit = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
            dbHelper.insertBookmark(TestFixture.createFixedBookmark(member, hearit));
        }

        // when & then
        RestAssured.given()
                .header("Authorization", "Bearer " + token)
                .param("page", 0)
                .param("size", 5)
                .when()
                .get("/api/v2/bookmarks/hearits")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("content.size()", equalTo(5));
    }

    @Test
    @DisplayName("로그인한 사용자가 북마크 목록 조회 시, page가 0 미만인 경우 400 BADREQUEST가 발생한다.")
    void readBookmarkHearitsTestWithBadRequestByPage() {
        // given
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
        String token = generateToken(member);
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Hearit hearit = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        dbHelper.insertBookmark(TestFixture.createFixedBookmark(member, hearit));

        // when & then
        RestAssured.given()
                .header("Authorization", "Bearer " + token)
                .param("page", -1)
                .when()
                .get("/api/v2/bookmarks/hearits")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 101})
    @DisplayName("로그인한 사용자가 북마크 목록 조회 시, size가 0 ~ 100이 아닌 경우 400 BADREQUEST가 발생한다.")
    void readBookmarkHearitsTestWithBadRequestBySize(int size) {
        // given
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
        String token = generateToken(member);

        // when & then
        RestAssured.given(this.spec)
                .header("Authorization", "Bearer " + token)
                .param("size", size)
                .when()
                .get("/api/v1/bookmarks")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @DisplayName("로그인한 사용자가 전체 북마크 목록 조회 시, 200 OK와 리스트를 반환한다.")
    void readBookmarkHearits_all() {
        // given
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
        String token = generateToken(member);
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());

        Hearit hearit1 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        Bookmark bookmark1 = dbHelper.insertBookmark(TestFixture.createFixedBookmark(member, hearit1));

        Hearit hearit2 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        Bookmark bookmark2 = dbHelper.insertBookmark(TestFixture.createFixedBookmark(member, hearit2));

        // when and then
        RestAssured.given(this.spec)
                .header("Authorization", "Bearer " + token)
                .param("page", 0)
                .param("size", 5)
                .when()
                .get("/api/v1/bookmarks")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("content.size()", equalTo(2));
    }

    @Test
    @DisplayName("로그인한 사용자가 미완료 북마크 목록 조회 시, 200 OK와 리스트를 반환한다.")
    void readBookmarkHearits_unfinished() {
        // given
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
        String token = generateToken(member);
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());

        Hearit finished = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        Bookmark bookmark1 = dbHelper.insertBookmark(TestFixture.createFixedBookmark(member, finished));
        PlayingHistory playingHistory = dbHelper.insertPlayingHistory(
                new PlayingHistory(member.getId(), finished, 500_000L));

        Hearit unfinished = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        Bookmark bookmark2 = dbHelper.insertBookmark(TestFixture.createFixedBookmark(member, unfinished));

        // when and then
        RestAssured.given(this.spec)
                .header("Authorization", "Bearer " + token)
                .param("page", 0)
                .param("size", 5)
                .param("filter", "unfinished")
                .when()
                .get("/api/v1/bookmarks")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("content.size()", equalTo(1));
    }

    @Test
    @DisplayName("로그인하지 않은 사용자가 북마크 목록 조회 시, 200 OK와 빈 리스트를 반환한다.")
    void readBookmarkHearits_empty_when_isNotLogin() {
        // when and then
        RestAssured.given(this.spec)
                .param("page", 0)
                .param("size", 20)
                .when()
                .get("/api/v1/bookmarks")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("content.size()", equalTo(0));
    }

    @Test
    @DisplayName("로그인한 사용자가 북마크 추가 시, 추가 후 201 CREATED를 반환한다.")
    void createBookmarkTest() {
        // given
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
        String token = generateToken(member);
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Hearit hearit = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));

        // when & then
        BookmarkInfoResponse response = RestAssured.given(this.spec)
                .header("Authorization", "Bearer " + token)
                .when()
                .post("/api/v1/bookmarks/hearits/{hearitId}", hearit.getId())
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .extract().as(BookmarkInfoResponse.class);

        assertThat(response.id()).isNotNull();
    }

    @Test
    @DisplayName("로그인한 사용자가 이미 추가된 북마크 추가 시, 추가 후 409 CONFLICT를 반환한다.")
    void createBookmarkTestWithConflict() {
        // given
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
        String token = generateToken(member);
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Hearit hearit = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        dbHelper.insertBookmark(TestFixture.createFixedBookmark(member, hearit));

        // when & then
        RestAssured.given(this.spec)
                .header("Authorization", "Bearer " + token)
                .when()
                .post("/api/v1/bookmarks/hearits/{hearitId}", hearit.getId())
                .then()
                .statusCode(HttpStatus.CONFLICT.value());
    }

    @Test
    @DisplayName("로그인한 사용자가 북마크 삭제 시, 삭제 후 204 NOCONTENT를 반환한다.")
    void deleteBookmark() {
        // given
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
        String token = generateToken(member);
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Hearit hearit = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        Bookmark bookmark = dbHelper.insertBookmark(TestFixture.createFixedBookmark(member, hearit));

        // when & then
        RestAssured.given(this.spec)
                .header("Authorization", "Bearer " + token)
                .when()
                .delete("/api/v1/bookmarks/{bookmarkId}", bookmark.getId())
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value());
    }

    @Test
    @DisplayName("자신의 북마크가 아닌 북마크 삭제 시, 403 FORBIDDEN을 반환한다.")
    void notFoundHearitId() {
        // given
        Member bookmarkMember = dbHelper.insertMember(TestFixture.createFixedMember());
        Member notBookmarkMember = dbHelper.insertMember(TestFixture.createFixedMember());
        String token = generateToken(notBookmarkMember);
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Hearit hearit = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        Bookmark bookmark = dbHelper.insertBookmark(TestFixture.createFixedBookmark(bookmarkMember, hearit));

        // when & then
        RestAssured.given(this.spec)
                .header("Authorization", "Bearer " + token)
                .when()
                .delete("/api/v1/bookmarks/{bookmarkId}", bookmark.getId())
                .then()
                .statusCode(HttpStatus.FORBIDDEN.value());
    }

    private String generateToken(Member member) {
        return jwtTokenProvider.createAccessToken(member.getId());
    }
}
