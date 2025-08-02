package com.onair.hearit.auth.presentation;


import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static com.epages.restdocs.apispec.RestAssuredRestDocumentationWrapper.document;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.Schema;
import com.onair.hearit.auth.dto.request.LoginRequest;
import com.onair.hearit.auth.dto.request.SignupRequest;
import com.onair.hearit.auth.dto.response.TokenResponse;
import com.onair.hearit.docs.ApiDocSnippets;
import com.onair.hearit.domain.Member;
import com.onair.hearit.fixture.DbHelper;
import com.onair.hearit.fixture.IntegrationTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
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

    @Test
    @DisplayName("로그인 성공 시 200 OK 및 accessToken를 반환한다.")
    void login_success() {
        // given
        Member member = Member.createLocalUser(
                "test1234",
                "testName",
                passwordEncoder.encode("pass1234"),
                "profile.jpg"
        );
        dbHelper.insertMember(member);

        LoginRequest request = new LoginRequest("test1234", "pass1234");

        // when
        TokenResponse tokenResponse = RestAssured.given(this.spec)
                .contentType(ContentType.JSON)
                .body(request)
                .filter(document("auth-login",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Auth API")
                                .summary("일반 로그인")
                                .description("아이디/비밀번호로 로그인하여 토큰을 발급받습니다.")
                                .requestSchema(Schema.schema("LoginRequest"))
                                .requestFields(
                                        fieldWithPath("localId").description("사용자 아이디"),
                                        fieldWithPath("password").description("비밀번호")
                                )
                                .responseSchema(Schema.schema("TokenResponse"))
                                .responseFields(
                                        fieldWithPath("accessToken").description("발급된 액세스 토큰")
                                )
                                .build())
                ))
                .when()
                .post("/api/v1/auth/login")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract().as(TokenResponse.class);

        // then
        assertThat(tokenResponse.accessToken()).isNotNull();
    }

    @Test
    @DisplayName("비밀번호 틀리면 401 Unauthorized 반환한다.")
    void login_invalidPassword() {
        // given
        Member member = Member.createLocalUser(
                "test1234",
                "testName",
                passwordEncoder.encode("pass1234"),
                "profile.jpg"
        );
        dbHelper.insertMember(member);

        LoginRequest request = new LoginRequest("test1234", "wrong-pass");

        // when
        // then
        RestAssured.given(this.spec)
                .contentType(ContentType.JSON)
                .body(request)
                .filter(document("auth-login-unauthorized",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Auth API")
                                .summary("일반 로그인")
                                .responseSchema(Schema.schema("ProblemDetail"))
                                .responseFields(ApiDocSnippets.getProblemDetailResponseFields())
                                .build())
                ))
                .when()
                .post("/api/v1/auth/login")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    @DisplayName("존재하지 않는 회원이면 401 Unauthorized 반환한다.")
    void login_nonexistentMember() {
        // given
        LoginRequest request = new LoginRequest("ghost123", "pass1234");

        // when & then
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/api/v1/auth/login")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    @DisplayName("회원가입 성공 시 201 CREATED를 반환한다.")
    void signup_success() {
        // given
        SignupRequest request = new SignupRequest("newUser123", "newNickname", "password1234");

        // when & then
        RestAssured.given(this.spec)
                .contentType(ContentType.JSON)
                .body(request)
                .filter(document("auth-signup",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Auth API")
                                .summary("회원가입")
                                .description("새로운 계정을 생성합니다.")
                                .requestSchema(Schema.schema("SignupRequest"))
                                .requestFields(
                                        fieldWithPath("localId").description("사용자 아이디"),
                                        fieldWithPath("nickname").description("닉네임"),
                                        fieldWithPath("password").description("비밀번호")
                                )
                                .build())
                ))
                .when()
                .post("/api/v1/auth/signup")
                .then()
                .statusCode(HttpStatus.CREATED.value());
    }

    @Test
    @DisplayName("이미 존재하는 아이디로 회원가입 시 400 BAD REQUEST를 반환한다.")
    void signup_fail_with_duplicate_id() {
        // given
        Member existingMember = Member.createLocalUser(
                "existingUser",
                "existingNickname",
                passwordEncoder.encode("password1234"),
                "profile.jpg"
        );
        dbHelper.insertMember(existingMember);

        SignupRequest request = new SignupRequest("existingUser", "newNickname", "password1234");

        // when & then
        RestAssured.given(this.spec)
                .contentType(ContentType.JSON)
                .body(request)
                .filter(document("auth-signup-bad-request",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Auth API")
                                .summary("회원가입")
                                .responseSchema(Schema.schema("ProblemDetail"))
                                .responseFields(ApiDocSnippets.getProblemDetailResponseFields())
                                .build())
                ))
                .when()
                .post("/api/v1/auth/signup")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }
}
