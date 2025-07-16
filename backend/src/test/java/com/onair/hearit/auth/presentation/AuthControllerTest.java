package com.onair.hearit.auth.presentation;


import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import com.onair.hearit.IntegrationTest;
import com.onair.hearit.auth.dto.request.LoginRequest;
import com.onair.hearit.auth.dto.response.TokenResponse;
import com.onair.hearit.domain.Member;
import com.onair.hearit.infrastructure.MemberRepository;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

class AuthControllerTest extends IntegrationTest {

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("로그인 성공 시 200 OK 및 accessToken 반환")
    void login_success() {
        Member member = new Member(
                "test123",
                "testName",
                passwordEncoder.encode("pass1234")
        );
        memberRepository.save(member);

        LoginRequest request = new LoginRequest("test123", "pass1234");

        TokenResponse tokenResponse = given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/api/v1/auth/login")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract().as(TokenResponse.class);

        assertThat(tokenResponse.accessToken()).isNotNull();
    }

    @Test
    @DisplayName("비밀번호 틀리면 401 Unauthorized")
    void login_invalidPassword() {
        Member member = new Member(
                "test123",
                "testName",
                passwordEncoder.encode("pass1234")
        );
        memberRepository.save(member);

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
    @DisplayName("존재하지 않는 회원이면 401 Unauthorized")
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
