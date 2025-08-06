package com.onair.hearit.auth.presentation;


import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static com.epages.restdocs.apispec.RestAssuredRestDocumentationWrapper.document;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.Schema;
import com.onair.hearit.auth.domain.RefreshToken;
import com.onair.hearit.auth.dto.request.LoginRequest;
import com.onair.hearit.auth.dto.request.SignupRequest;
import com.onair.hearit.auth.dto.request.TokenReissueRequest;
import com.onair.hearit.auth.dto.response.LoginTokenResponse;
import com.onair.hearit.auth.dto.response.TokenReissueResponse;
import com.onair.hearit.auth.infrastructure.jwt.JwtTokenProvider;
import com.onair.hearit.auth.infrastructure.repository.RefreshTokenRepository;
import com.onair.hearit.docs.ApiDocSnippets;
import com.onair.hearit.domain.Member;
import com.onair.hearit.fixture.DbHelper;
import com.onair.hearit.fixture.IntegrationTest;
import com.onair.hearit.fixture.TestFixture;
import com.onair.hearit.infrastructure.MemberRepository;
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

    @Autowired
    MemberRepository memberRepository;

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
        LoginTokenResponse loginTokenResponse = RestAssured.given(this.spec).log().all()
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
                                .responseSchema(Schema.schema("LoginTokenResponse"))
                                .responseFields(
                                        fieldWithPath("accessToken").description("발급된 액세스 토큰"),
                                        fieldWithPath("refreshToken").description("발급된 리프레시 토큰")
                                )
                                .build())
                ))
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
        TokenReissueResponse response = RestAssured.given(this.spec).log().all()
                .contentType(ContentType.JSON)
                .body(tokenReissueRequest)
                .filter(document("auth-refresh",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Auth API")
                                .summary("엑세스토큰 재발급 요청")
                                .description("리프레시토큰으로 엑세스토큰 재발급 요청해 새 엑세스토큰을 반환받습니다.")
                                .requestSchema(Schema.schema("TokenReissueResponse"))
                                .requestFields(
                                        fieldWithPath("refreshToken").description("리프레시 토큰")
                                )
                                .responseSchema(Schema.schema("LoginTokenResponse"))
                                .responseFields(
                                        fieldWithPath("accessToken").description("발급된 액세스 토큰")
                                )
                                .build())
                ))
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

        // when
        // then
        RestAssured.given(this.spec)
                .contentType(ContentType.JSON)
                .body(request)
                .filter(document("auth-login-unauthorized-wrong-password",
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

        RestAssured.given(this.spec).log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .filter(document("auth-login-unauthorized-nonexistent-member",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Auth API")
                                .summary("일반 로그인")
                                .responseSchema(Schema.schema("ProblemDetail"))
                                .responseFields(ApiDocSnippets.getProblemDetailResponseFields())
                                .build())
                ))
                .when()
                .post("/api/v1/auth/login")
                .then().log().all()
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

    @Test
    @DisplayName("엑세스토큰 유효성 검증 성공 시 200 OK를 반환한다.")
    void check_success() {
        // given
        Member member = dbHelper.insertMember(
                Member.createLocalUser("localId", "nickname", "password", "profile.jpg"));
        String validAccessToken = jwtTokenProvider.createAccessToken(member.getId());

        // when & then
        RestAssured.given(this.spec)
                .header("Authorization", "Bearer " + validAccessToken)
                .filter(document("auth-check",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Auth API")
                                .summary("엑세스토큰 유효성 검증")
                                .description("엑세스토큰의 유효성을 검증합니다.")
                                .build())
                ))
                .when()
                .get("/api/v1/auth/check")
                .then()
                .statusCode(HttpStatus.OK.value());
    }

    @Test
    @DisplayName("엑세스토큰 유효성 검증 실패 시 401 Unauthorized를 반환한다.")
    void check_unauthorized() {
        // given
        String invalidAccessToken = "invalid-access-token";

        // when & then
        RestAssured.given(this.spec)
                .header("Authorization", "Bearer " + invalidAccessToken)
                .filter(document("auth-check-unauthorized",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Auth API")
                                .summary("엑세스토큰 유효성 검증")
                                .description("엑세스토큰의 유효성을 검증합니다.")
                                .responseSchema(Schema.schema("ProblemDetail"))
                                .responseFields(ApiDocSnippets.getProblemDetailResponseFields())
                                .build())
                ))
                .when()
                .get("/api/v1/auth/check")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    @DisplayName("회원탈퇴 시 해당 회원의 리프레시토큰을 삭제하고 회원탈퇴 시간이 기록된다.")
    void withdraw() {
        // given
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
        createAndSaveRefreshTokenFrom(member);
        assertThat(refreshTokenRepository.findByMemberId(member.getId())).isPresent();

        String accessToken = jwtTokenProvider.createAccessToken(member.getId());

        // when
        RestAssured.given(this.spec).log().all()
                .header("Authorization", "Bearer " + accessToken)
                .filter(document("auth-withdraw",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Auth API")
                                .summary("회원탈퇴")
                                .description("회원탈퇴 시 서버에서 회원을 탈퇴처리합니다.")
                                .build())
                ))
                .when()
                .delete("/api/v1/auth/withdraw")
                .then().log().all()
                .statusCode(HttpStatus.NO_CONTENT.value());

        // then
        assertAll(() -> {
            assertThat(refreshTokenRepository.findByMemberId(member.getId())).isEmpty();
            assertThat(memberRepository.findById(member.getId()).orElseThrow().getDeletedAt()).isNotNull();
        });
    }

    private RefreshToken createAndSaveRefreshTokenFrom(Member member) {
        String refreshToken = jwtTokenProvider.createRefreshToken(member.getId());
        LocalDateTime expiryDate = jwtTokenProvider.extractExpiry(refreshToken);
        return refreshTokenRepository.save(new RefreshToken(member.getId(), refreshToken, expiryDate));
    }
}
