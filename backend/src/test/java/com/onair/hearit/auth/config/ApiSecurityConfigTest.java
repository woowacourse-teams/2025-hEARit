package com.onair.hearit.auth.config;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

import com.onair.TestFixture;
import com.onair.hearit.IntegrationTest;
import com.onair.hearit.auth.infrastructure.jwt.JwtTokenProvider;
import com.onair.hearit.domain.Member;
import io.restassured.RestAssured;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.contract.spec.internal.HttpStatus;

class ApiSecurityConfigTest extends IntegrationTest {

    @Autowired
    JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("화이트리스트 경로는 인증 없이 접근할 수 있다")
    void canAccessWhitelistedPathWithoutAuth() {
        // when & then
        RestAssured.given().log().all()
                .when()
                .get("/api/v1/categories") // 인증 필요없는 경로
                .then().log().all()
                .statusCode(HttpStatus.OK);
    }

    @Test
    @DisplayName("보호된 API는 인증 없이 접근할 수 없다")
    void cannotAccessProtectedApiWithoutToken() {
        RestAssured.given().log().all()
                .when()
                .get("/api/v1/bookmarks") // 인증 필요한 경로
                .then().log().all()
                .statusCode(HttpStatus.UNAUTHORIZED)
                .body("detail", equalTo("유효하지 않은 토큰입니다."));
    }

    @Test
    @DisplayName("유효하지 않은 토큰일 경우 401 응답을 반환한다")
    void rejectInvalidToken() {
        RestAssured.given().log().all()
                .header("Authorization", "Bearer invalid-token")
                .when()
                .get("/api/v1/bookmarks") // 인증 필요한 경로
                .then().log().all()
                .statusCode(HttpStatus.UNAUTHORIZED)
                .body("detail", equalTo("유효하지 않은 토큰입니다."));
    }

    @Test
    @DisplayName("Authorization 헤더가 없으면 보호된 경로는 접근이 거부된다")
    void rejectIfNoAuthHeader() {
        RestAssured.given().log().all()
                .when()
                .get("/api/v1/bookmarks") // 인증 필요한 경로
                .then().log().all()
                .statusCode(HttpStatus.UNAUTHORIZED)
                .body("detail", equalTo("유효하지 않은 토큰입니다."));
    }

    @Test
    @DisplayName("유효한 토큰이 있으면 보호된 API에 접근할 수 있다")
    void allowAccessWithValidToken() {
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
        String token = generateToken(member);

        RestAssured.given().log().all()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/api/v1/bookmarks") // 인증 필요한 경로
                .then().log().all()
                .statusCode(HttpStatus.OK);
    }

    @Test
    @DisplayName("인증 실패 시 application/problem+json 형식으로 응답한다")
    void returnProblemDetailOnAuthFailure() {
        RestAssured.given().log().all()
                .header("Authorization", "Bearer invalid-token")
                .when()
                .get("/api/v1/bookmarks") // 인증 필요한 경로
                .then().log().all()
                .statusCode(401)
                .header("Content-Type", containsString("application/problem+json"))
                .body("title", equalTo("인증되지 않은 요청입니다."))
                .body("detail", equalTo("유효하지 않은 토큰입니다."));
    }

    private String generateToken(Member member) {
        return jwtTokenProvider.createToken(member.getId());
    }
}
