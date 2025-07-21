package com.onair.hearit.auth.presentation;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static io.restassured.RestAssured.given;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.onair.hearit.IntegrationTest;
import com.onair.hearit.auth.infrastructure.client.KakaoUserInfoClient;
import com.onair.hearit.auth.dto.request.KakaoLoginRequest;
import com.onair.hearit.auth.dto.response.TokenResponse;
import io.restassured.http.ContentType;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = "kakao.user-info.base-url=http://localhost:8089")
class AuthKakaoLoginControllerTest extends IntegrationTest {

    private static WireMockServer wireMockServer;

    @Autowired
    KakaoUserInfoClient kakaoUserInfoClient;

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

        KakaoLoginRequest kakaoLoginRequest = new KakaoLoginRequest("accessToken-test-example");
        TokenResponse tokenResponse = given().log().all()
                .contentType(ContentType.JSON)
                .body(kakaoLoginRequest)
                .when()
                .post("/api/v1/auth/kakao-login")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract().as(TokenResponse.class);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(tokenResponse).isNotNull();
            softly.assertThat(tokenResponse.accessToken()).isNotNull();
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

        KakaoLoginRequest kakaoLoginRequest = new KakaoLoginRequest("accessToken-test-example");
        ProblemDetail problemDetail = given().log().all()
                .contentType(ContentType.JSON)
                .body(kakaoLoginRequest)
                .when()
                .post("/api/v1/auth/kakao-login")
                .then().log().all()
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .extract().as(ProblemDetail.class);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(problemDetail).isNotNull();
            softly.assertThat(problemDetail.getDetail()).isEqualTo("this access token is already expired");
        });
    }

    //TODO: 401 외 카카오API 사용 중 발생할 수 있는 모든 예외 처리 필요
}
