package com.onair.hearit.app.reaction.presentation;

import com.onair.hearit.app.auth.infrastructure.jwt.JwtTokenProvider;
import com.onair.hearit.app.fixture.IntegrationTest;
import com.onair.hearit.core.domain.Category;
import com.onair.hearit.core.domain.Hearit;
import com.onair.hearit.core.domain.Member;
import com.onair.hearit.core.domain.Reaction;
import com.onair.hearit.core.domain.ReactionType;
import com.onair.hearit.core.fixture.TestFixture;
import io.restassured.RestAssured;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

class LikeIntegrationTest extends IntegrationTest {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("회원 좋아요 추가 - 201 CREATED")
    void createLike_201Created() {
        // given
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
        String token = generateToken(member);
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Hearit hearit = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));

        // when & then
        RestAssured.given(this.spec)
                .header("Authorization", "Bearer " + token)
                .when()
                .post("/api/v1/hearits/{hearitId}/likes", hearit.getId())
                .then()
                .statusCode(HttpStatus.CREATED.value());
    }

    @Test
    @DisplayName("회원 좋아요 삭제 - 204 NoContent")
    void deleteLike_204NoContent() {
        // given
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
        String token = generateToken(member);
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Hearit hearit = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        Reaction like = dbHelper.insertReaction(TestFixture.createFixedReaction(member, hearit, ReactionType.LIKE));

        // when & then
        RestAssured.given(this.spec)
                .header("Authorization", "Bearer " + token)
                .when()
                .delete("/api/v1/hearits/{hearitId}/likes", hearit.getId())
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value());
    }

    @Test
    @DisplayName("비회원 좋아요 추가 - 401Unauthorized")
    void createLike_403Forbidden() {
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Hearit hearit = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));

        // when & then
        RestAssured.given(this.spec)
                .when()
                .post("/api/v1/hearits/{hearitId}/likes", hearit.getId())
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    @DisplayName("비회원 좋아요 삭제 - 401Unauthorized")
    void deleteLike_401Unauthorized() {
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Hearit hearit = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));

        // when & then
        RestAssured.given(this.spec)
                .when()
                .delete("/api/v1/hearits/{hearitId}/likes", hearit.getId())
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value());

    }

    private String generateToken(Member member) {
        return jwtTokenProvider.createAccessToken(member.getUuid());
    }
}
