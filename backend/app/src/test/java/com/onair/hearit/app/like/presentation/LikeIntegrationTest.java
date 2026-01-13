package com.onair.hearit.app.like.presentation;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.onair.hearit.app.auth.infrastructure.jwt.JwtTokenProvider;
import com.onair.hearit.app.bookmark.dto.BookmarkInfoResponse;
import com.onair.hearit.app.fixture.IntegrationTest;
import com.onair.hearit.core.domain.Category;
import com.onair.hearit.core.domain.Hearit;
import com.onair.hearit.core.domain.Member;
import com.onair.hearit.core.fixture.TestFixture;
import io.restassured.RestAssured;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

class LikeIntegrationTest extends IntegrationTest {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @DisplayName("회원 좋아요 추가 - 201 CREATED")
    void createLike_201Created() {
        // given
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
        String token = generateToken(member);
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Hearit hearit = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));

        // when & then
        BookmarkInfoResponse response = RestAssured.given(this.spec)
                .header("Authorization", "Bearer " + token)
                .when()
                .post("/api/v1/likes/hearits/{hearitId}", hearit.getId())
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .extract().as(BookmarkInfoResponse.class);

        assertThat(response.id()).isNotNull();
    }

    @DisplayName("회원 좋아요 삭제 - 204 NoContent")
    void deleteLike_204NoContent() {
        // given
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
        String token = generateToken(member);
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Hearit hearit = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        Like like = dbHelper.insertLike(TestFixture.createFixedLike(member, hearit));

        // when & then
        RestAssured.given(this.spec)
                .header("Authorization", "Bearer " + token)
                .when()
                .delete("/api/v1/likes/hearit/{hearitId}", like.getId())
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value());
    }

    @DisplayName("비회원 좋아요 추가 - 401Unauthorized")
    void createLike_401Unauthorized() {

    }

    @DisplayName("비회원 좋아요 삭제 - 401Unauthorized")
    void deleteLike_401Unauthorized() {

    }

    private String generateToken(Member member) {
        return jwtTokenProvider.createAccessToken(member.getId());
    }
}
