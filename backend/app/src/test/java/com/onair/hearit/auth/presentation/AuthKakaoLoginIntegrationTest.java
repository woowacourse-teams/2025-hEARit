package com.onair.hearit.auth.presentation;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.onair.hearit.auth.dto.request.OAuthLoginRequest;
import com.onair.hearit.auth.dto.response.LoginTokenResponse;
import com.onair.hearit.auth.infrastructure.oauth.kakao.KakaoOAuthService;
import com.onair.hearit.fixture.IntegrationTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = "kakao.user-info.base-url=http://localhost:8089")
class AuthKakaoLoginIntegrationTest extends IntegrationTest {

    private static WireMockServer wireMockServer;

    @Autowired
    KakaoOAuthService kakaoOAuthService;

    @BeforeAll
    static void setup() {
        wireMockServer = new WireMockServer(8089);
        wireMockServer.start();
    }

    @AfterAll
    static void tearDown() {
        wireMockServer.stop();
    }

    @Test
    @DisplayName("카카오 로그인 요청 성공 시 히어릿서비스의 accessToken을 반환한다.")
    void loginWithKakao_success_then_responseAccessToken() {
        wireMockServer.stubFor(WireMock.get(urlEqualTo("/v2/user/me"))
                .withHeader("Authorization", WireMock.matching("Bearer .*"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                    {
                                      "id": 123456789,
                                      "properties": {
                                          "nickname": "멍구"
                                      }
                                    }
                                """)));

        OAuthLoginRequest kakaoLoginRequest = new OAuthLoginRequest("accessToken-test-example");
        LoginTokenResponse loginTokenResponse = RestAssured.given(this.spec).log().all()
                .contentType(ContentType.JSON)
                .body(kakaoLoginRequest)
                .when()
                .post("/api/v1/auth/kakao-login")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract().as(LoginTokenResponse.class);

        assertAll(() -> {
            assertThat(loginTokenResponse).isNotNull();
            assertThat(loginTokenResponse.accessToken()).isNotNull();
            assertThat(loginTokenResponse.refreshToken()).isNotNull();
        });
    }

    @Test
    @DisplayName("카카오 로그인 요청 실패 시 401 Unauthorized 응답")
    void loginWithKakao_error_Unauthorized() {
        wireMockServer.stubFor(WireMock.get(urlEqualTo("/v2/user/me"))
                .withHeader("Authorization", WireMock.matching("Bearer .*"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(401)
                        .withBody("""
                                {
                                  "msg": "this access token is already expired",
                                  "code": -401
                                }
                                """)));

        OAuthLoginRequest kakaoLoginRequest = new OAuthLoginRequest("accessToken-test-example");
        ProblemDetail problemDetail = given(this.spec)
                .contentType(ContentType.JSON)
                .body(kakaoLoginRequest)
                .when()
                .post("/api/v1/auth/kakao-login")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .extract().as(ProblemDetail.class);

        assertAll(() -> {
            assertThat(problemDetail).isNotNull();
            assertThat(problemDetail.getDetail()).contains("this access token is already expired");
        });
    }

    //TODO: 401 외 카카오API 사용 중 발생할 수 있는 모든 예외 처리 필요
}
