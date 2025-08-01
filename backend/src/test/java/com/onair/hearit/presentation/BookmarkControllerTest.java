package com.onair.hearit.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.onair.hearit.auth.infrastructure.jwt.JwtTokenProvider;
import com.onair.hearit.domain.Bookmark;
import com.onair.hearit.domain.Category;
import com.onair.hearit.domain.Hearit;
import com.onair.hearit.domain.Member;
import com.onair.hearit.dto.response.BookmarkInfoResponse;
import com.onair.hearit.fixture.TestFixture;
import io.restassured.RestAssured;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

class BookmarkControllerTest extends IntegrationTest {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("로그인한 사용자가 북마크 목록 조회 시, 200 OK 및 페이지에 따른 북마크 목록을 반환한다.")
    void readBookmarkHearitsTest() {
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
        int size = 5;
        for (int i = 0; i < bookmarkCount / size; i++) {
            RestAssured.given()
                    .header("Authorization", "Bearer " + token)
                    .param("page", i)
                    .param("size", size)
                    .when()
                    .get("/api/v1/bookmarks/hearits")
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("content.size()", equalTo(5));
        }
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
                .get("/api/v1/bookmarks/hearits")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value()).log();
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 51})
    @DisplayName("로그인한 사용자가 북마크 목록 조회 시, size가 0 ~ 50이 아닌 경우 400 BADREQUEST가 발생한다.")
    void readBookmarkHearitsTestWithBadRequestBySize() {
        // given
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
        String token = generateToken(member);
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Hearit hearit = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        dbHelper.insertBookmark(TestFixture.createFixedBookmark(member, hearit));

        // when & then
        RestAssured.given()
                .header("Authorization", "Bearer " + token)
                .param("size", -1)
                .when()
                .get("/api/v1/bookmarks/hearits")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value()).log();
    }

    @Test
    @DisplayName("로그인하지 않은 사용자가 북마크 목록 조회 시, 401 UNAUTHORIZATION가 발생한다.")
    void readBookmarkHearits_error_401_whenNotLogin() {
        // given
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        int bookmarkCount = 10;
        for (int i = 0; i < bookmarkCount; i++) {
            Hearit hearit = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
            dbHelper.insertBookmark(TestFixture.createFixedBookmark(member, hearit));
        }

        // when & then
        RestAssured.given()
                .header("Authorization", "")
                .param("page", 0)
                .param("size", 20)
                .when()
                .get("/api/v1/bookmarks/hearits")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value());
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
        BookmarkInfoResponse response = RestAssured.given()
                .header("Authorization", "Bearer " + token)
                .when()
                .post("/api/v1/bookmarks/hearits/" + hearit.getId())
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
        RestAssured.given()
                .header("Authorization", "Bearer " + token)
                .when()
                .post("/api/v1/bookmarks/hearits/" + hearit.getId())
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
        RestAssured.given()
                .header("Authorization", "Bearer " + token)
                .when()
                .delete("/api/v1/bookmarks/" + bookmark.getId())
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value());
    }

    @Test
    @DisplayName("자신의 북마크가 아닌 북마크 삭제 시, 403 UNAUTHORIZED를 반환한다.")
    void notFoundHearitId() {
        // given
        Member bookmarkMember = dbHelper.insertMember(TestFixture.createFixedMember());
        Member notBookmarkMember = dbHelper.insertMember(TestFixture.createFixedMember());
        String token = generateToken(notBookmarkMember);
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Hearit hearit = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        Bookmark bookmark = dbHelper.insertBookmark(TestFixture.createFixedBookmark(bookmarkMember, hearit));

        // when & then
        RestAssured.given()
                .header("Authorization", "Bearer " + token)
                .when()
                .delete("/api/v1/bookmarks/" + bookmark.getId())
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value());
    }

    private String generateToken(Member member) {
        return jwtTokenProvider.createToken(member.getId());
    }
}
