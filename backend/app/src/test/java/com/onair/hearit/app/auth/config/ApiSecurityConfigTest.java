package com.onair.hearit.app.auth.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.onair.hearit.app.auth.infrastructure.jwt.JwtTokenProvider;
import com.onair.hearit.app.fixture.IntegrationTest;
import com.onair.hearit.core.domain.Category;
import com.onair.hearit.core.domain.Hearit;
import com.onair.hearit.core.domain.Member;
import com.onair.hearit.core.fixture.TestFixture;
import io.restassured.RestAssured;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.contract.spec.internal.HttpStatus;
import org.springframework.http.ProblemDetail;

class ApiSecurityConfigTest extends IntegrationTest {

    @Autowired
    JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("PUBLIC GET API 경로는 HTTP Method GET만 인증 없이 접근할 수 있다")
    void canAccessPublicGetListPathWithoutAuth() {
        // when & then
        RestAssured.given().log().all()
                .header("X-Device-UUID", UUID.randomUUID().toString())
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
                .get("/api/v1/bookmarks/hearits") // 인증 필요한 경로
                .then().log().all()
                .statusCode(HttpStatus.UNAUTHORIZED)
                .body("detail", equalTo("인증이 필요한 요청입니다."));
    }

    @Test
    @DisplayName("유효하지 않은 토큰일 경우 401 응답을 반환한다")
    void rejectInvalidToken() {
        RestAssured.given().log().all()
                .header("Authorization", "Bearer invalid-token")
                .when()
                .get("/api/v1/bookmarks/hearits") // 인증 필요한 경로
                .then().log().all()
                .statusCode(HttpStatus.UNAUTHORIZED)
                .body("detail", equalTo("유효하지 않은 토큰입니다."));
    }

    @Test
    @DisplayName("Authorization 헤더가 없으면 보호된 경로는 접근이 거부된다")
    void rejectIfNoAuthHeader() {
        RestAssured.given().log().all()
                .when()
                .get("/api/v1/bookmarks/hearits") // 인증 필요한 경로
                .then().log().all()
                .statusCode(HttpStatus.UNAUTHORIZED)
                .body("detail", equalTo("인증이 필요한 요청입니다."));
    }

    @Test
    @DisplayName("유효한 토큰이 있으면 보호된 API에 접근할 수 있다")
    void allowAccessWithValidToken() {
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
        String token = generateToken(member);
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Hearit hearit = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));

        RestAssured.given().log().all()
                .header("Authorization", "Bearer " + token)
                .when()
                .post("/api/v1/bookmarks/hearits/" + hearit.getId()) // 인증 필요한 경로
                .then().log().all()
                .statusCode(HttpStatus.CREATED);
    }

    @Test
    @DisplayName("인증 실패 시 application/problem+json 형식으로 응답하며 토큰재발급 여부를 위한 properties를 포함한다.")
    void returnProblemDetailOnAuthFailure() {
        ProblemDetail problemDetail = RestAssured.given().log().all()
                .header("Authorization", "Bearer invalid-token")
                .when()
                .get("/api/v1/bookmarks/hearits") // 인증 필요한 경로
                .then().log().all()
                .statusCode(401)
                .header("Content-Type", containsString("application/problem+json"))
                .extract()
                .as(ProblemDetail.class);

        assertAll(
                () -> assertThat(problemDetail.getTitle()).isEqualTo("엑세스 토큰이 유효하지 않습니다."),
                () -> assertThat(problemDetail.getDetail()).isEqualTo("유효하지 않은 토큰입니다."),
                () -> assertThat(problemDetail.getProperties().get("code")).isNotNull(),
                () -> assertThat(problemDetail.getProperties().get("reissuable")).isNotNull()
        );
    }

    private String generateToken(Member member) {
        return jwtTokenProvider.createAccessToken(member.getId());
    }
}
