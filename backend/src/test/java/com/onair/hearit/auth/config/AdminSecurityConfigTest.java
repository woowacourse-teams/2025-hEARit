package com.onair.hearit.auth.config;

import static org.hamcrest.Matchers.containsString;

import com.onair.hearit.admin.domain.Admin;
import com.onair.hearit.fixture.IntegrationTest;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.contract.spec.internal.HttpStatus;

class AdminSecurityConfigTest extends IntegrationTest {

    @Test
    @DisplayName("로그인 페이지는 인증 없이 접근 허용된다.")
    void canAccess_adminLoginPage() {
        // when & then
        RestAssured.given().log().all()
                .when()
                .get("/admin/login")
                .then().log().all()
                .statusCode(HttpStatus.OK);
    }

    @Test
    @DisplayName("인증되지 않은 사용자는 어드민 페이지에 접근할 수 없다.")
    void redirectToLoginPage_when_unauthenticated() {
        // when & then
        RestAssured.given().log().all()
                .redirects().follow(false)
                .when()
                .get("/admin")
                .then().log().all()
                .statusCode(HttpStatus.FOUND)
                .header("Location", containsString("/admin/login"));
    }

    @Test
    @DisplayName("CSRF 토큰과 세션을 포함 시 로그인이 성공하고, 어드민 페이지로 리디렉션된다.")
    public void adminLogin_withCsrf() {
        // given
        dbHelper.insertAdmin(new Admin("admin", "1234", "관리자"));
        CsrfSession csrfSession = extractCsrfAndSession();

        // when & then
        // 로그인 POST 요청 (CSRF 포함)
        RestAssured.given().log().all()
                .cookie("JSESSIONID", csrfSession.sessionId())
                .formParam("loginId", "admin")
                .formParam("password", "1234")
                .formParam("_csrf", csrfSession.csrfToken())
                .post("/admin/login")
                .then().log().all()
                .statusCode(302)
                .header("Location", containsString("/admin"));
    }

    @Test
    @DisplayName("인증된 사용자 어드민 페이지 접근 허용된다.")
    void canAccessAdminPage_when_authenticated() {
        // given
        dbHelper.insertAdmin(new Admin("admin", "1234", "관리자"));
        CsrfSession csrfSession = extractCsrfAndSession();

        // when
        // 로그인 요청
        RestAssured.given().log().all()
                .cookie("JSESSIONID", csrfSession.sessionId())
                .formParam("loginId", "admin")
                .formParam("password", "1234")
                .formParam("_csrf", csrfSession.csrfToken())
                .post("/admin/login");

        // then
        // 로그인 후 어드민 페이지 접근
        RestAssured.given().log().all()
                .cookie("JSESSIONID", csrfSession.sessionId())
                .when()
                .get("/admin")
                .then().log().all()
                .statusCode(HttpStatus.OK);
    }

    @Test
    @DisplayName("로그인 실패 시 에러 파라미터 포함 리디렉션된다.")
    void redirectToLoginPageWithError_when_loginFails() {
        // given
        CsrfSession csrfSession = extractCsrfAndSession();

        // when & then
        // 잘못된 자격증명으로 로그인
        RestAssured.given().log().all()
                .cookie("JSESSIONID", csrfSession.sessionId())
                .formParam("loginId", "wrong")
                .formParam("password", "invalid")
                .formParam("_csrf", csrfSession.csrfToken())
                .post("/admin/login")
                .then().log().all()
                .statusCode(HttpStatus.FOUND)
                .header("Location", containsString("/admin/login?error"));
    }

    private record CsrfSession(String csrfToken, String sessionId) {
    }

    // 로그인 페이지 GET → CSRF 토큰 추출
    private CsrfSession extractCsrfAndSession() {
        Response response = RestAssured.given().log().all()
                .when()
                .get("/admin/login");

        String csrfToken = Jsoup.parse(response.body().asString())
                .select("input[name=_csrf]")
                .val();

        String sessionId = response.getCookie("JSESSIONID");

        return new CsrfSession(csrfToken, sessionId);
    }

}
