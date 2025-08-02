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
import io.restassured.RestAssured;
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
    @DisplayName("로그인 성공 시 200 OK 및 엑세스토큰 + 리프레시토큰을 반환한다.")
    void login_success() {
        // given
        Member member = Member.createLocalUser(
                "test123",
                "testName",
                passwordEncoder.encode("pass1234"),
                "profile.jpg"
        );
        dbHelper.insertMember(member);

        LoginRequest request = new LoginRequest("test123", "pass1234");

        // when
        LoginTokenResponse loginTokenResponse = given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/api/v1/auth/login")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract().as(LoginTokenResponse.class);

        // then
        assertAll(() -> {
            assertThat(loginTokenResponse.accessToken()).isNotNull();
            assertThat(loginTokenResponse.refreshToken()).isNotNull();
        });
    }

    @DisplayName("유효한 리프레시토큰으로 엑세스토큰 재발급 요청 시 새 엑세스토큰을 반환한다.")
    @Test
    void reissueToken_requestWithValidRefreshToken() {
        // given
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
        RefreshToken validRefreshToken = createAndSaveRefreshTokenFrom(member);

        TokenReissueRequest tokenReissueRequest = new TokenReissueRequest(validRefreshToken.getToken());

        // when
        TokenReissueResponse response = RestAssured.given().log().all()
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
        // given
        Member member = Member.createLocalUser(
                "test123",
                "testName",
                passwordEncoder.encode("pass1234"),
                "profile.jpg"
        );
        dbHelper.insertMember(member);

        LoginRequest request = new LoginRequest("test123", "wrong-pass");

        RestAssured.given()
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

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/api/v1/auth/login")
                .then().log().all()
                .statusCode(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    @DisplayName("로그아웃 시 해당 회원의 리프레시토큰을 삭제한다.")
    void logout_then_deleteRefreshToken() {
        // given
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
        createAndSaveRefreshTokenFrom(member);
        assertThat(refreshTokenRepository.findByMemberId(member.getId())).isPresent();

        String accessToken = jwtTokenProvider.createAccessToken(member.getId());

        // when
        RestAssured.given().log().all()
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .post("/api/v1/auth/logout")
                .then().log().all()
                .statusCode(HttpStatus.NO_CONTENT.value());

        // then
        assertThat(refreshTokenRepository.findByMemberId(member.getId())).isEmpty();
    }

    private RefreshToken createAndSaveRefreshTokenFrom(Member member) {
        String refreshToken = jwtTokenProvider.createRefreshToken(member.getId());
        LocalDateTime expiryDate = jwtTokenProvider.extractExpiry(refreshToken);
        return refreshTokenRepository.save(new RefreshToken(member.getId(), refreshToken, expiryDate));
    }
}
