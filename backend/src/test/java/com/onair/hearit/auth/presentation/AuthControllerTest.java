package com.onair.hearit.auth.presentation;


import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.onair.hearit.auth.dto.request.LoginRequest;
import com.onair.hearit.auth.dto.request.TokenReissueRequest;
import com.onair.hearit.auth.dto.response.LoginTokenResponse;
import com.onair.hearit.auth.dto.response.TokenReissueResponse;
import com.onair.hearit.auth.infrastructure.jwt.JwtTokenProvider;
import com.onair.hearit.auth.infrastructure.jwt.RefreshToken;
import com.onair.hearit.auth.infrastructure.jwt.RefreshTokenRepository;
import com.onair.hearit.domain.Member;
import com.onair.hearit.fixture.DbHelper;
import com.onair.hearit.fixture.IntegrationTest;
import com.onair.hearit.fixture.TestFixture;
import io.restassured.http.ContentType;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

class AuthControllerTest extends IntegrationTest {

    @Autowired
    DbHelper dbHelper;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    JwtTokenProvider jwtTokenProvider;

    @Autowired
    RefreshTokenRepository refreshTokenRepository;

    @Test
    @DisplayName("로그인 성공 시 200 OK 및 accessToken + refreshToken을 반환한다.")
    void login_success() {
        Member member = Member.createLocalUser(
                "test123",
                "testName",
                passwordEncoder.encode("pass1234"),
                "profile.jpg"
        );
        dbHelper.insertMember(member);

        LoginRequest request = new LoginRequest("test123", "pass1234");

        LoginTokenResponse loginTokenResponse = given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/api/v1/auth/login")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract().as(LoginTokenResponse.class);

        assertAll(() -> {
            assertThat(loginTokenResponse.accessToken()).isNotNull();
            assertThat(loginTokenResponse.refreshToken()).isNotNull();
        });
    }

    @DisplayName("유효한 리프레시 토큰으로 엑시스토큰 재발급 요청 시 새 accessToken을 반환한다.")
    @Test
    void reissueToken_requestWithValidRefreshToken() {
        // given
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
        String validRefreshToken = jwtTokenProvider.createRefreshToken(member.getId());
        refreshTokenRepository.save(new RefreshToken(member.getId(), validRefreshToken, LocalDateTime.now()));

        TokenReissueRequest tokenReissueRequest = new TokenReissueRequest(validRefreshToken);

        // when
        TokenReissueResponse response = given().log().all()
                .contentType(ContentType.JSON)
                .body(tokenReissueRequest)
                .when()
                .post("/api/v1/auth/token/refresh")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract().as(TokenReissueResponse.class);

        // then
        assertThat(response.accessToken()).isNotNull();
    }
    @Test
    @DisplayName("비밀번호 틀리면 401 Unauthorized 반환한다.")
    void login_invalidPassword() {
        Member member = Member.createLocalUser(
                "test123",
                "testName",
                passwordEncoder.encode("pass1234"),
                "profile.jpg"
        );
        dbHelper.insertMember(member);

        LoginRequest request = new LoginRequest("test123", "wrong-pass");

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/api/v1/auth/login")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    @DisplayName("존재하지 않는 회원이면 401 Unauthorized 반환한다.")
    void login_nonexistentMember() {
        LoginRequest request = new LoginRequest("ghost123", "pass1234");

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/api/v1/auth/login")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value());
    }
}
