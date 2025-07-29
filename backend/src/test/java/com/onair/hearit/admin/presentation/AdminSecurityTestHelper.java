package com.onair.hearit.admin.presentation;

import com.onair.hearit.admin.domain.Admin;
import com.onair.hearit.fixture.DbHelper;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class AdminSecurityTestHelper {

    public static CsrfSession loginAdminAndGetCsrfSession(DbHelper dbHelper) {
        dbHelper.insertAdmin(new Admin("admin", "1234", "관리자"));

        // 1. 로그인 폼 접근 → 초기 CSRF & JSESSIONID 얻기
        Response loginPage = RestAssured.get("/admin/login");
        String oldSessionId = loginPage.getCookie("JSESSIONID");

        Document doc = Jsoup.parse(loginPage.getBody().asString());
        String csrfToken = doc.selectFirst("input[name=_csrf]").attr("value");

        // 2. 로그인 요청
        Response loginResponse = RestAssured.given()
                .cookie("JSESSIONID", oldSessionId)
                .formParam("loginId", "admin")
                .formParam("password", "1234")
                .formParam("_csrf", csrfToken)
                .post("/admin/login");

        String newSessionId = loginResponse.getCookie("JSESSIONID");

        //3.  로그인 성공 후 CSRF 재획득
        Response afterLogin = RestAssured.given()
                .cookie("JSESSIONID", newSessionId)
                .get("/admin/login");

        String newCsrf = Jsoup.parse(afterLogin.getBody().asString())
                .selectFirst("input[name=_csrf]")
                .attr("value");

        return new CsrfSession(newSessionId, newCsrf);
    }

    public record CsrfSession(String sessionId, String csrfToken) {}
}
