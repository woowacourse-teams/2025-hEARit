package com.onair.hearit.admin.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.onair.hearit.admin.dto.request.KeywordCreateRequest;
import com.onair.hearit.admin.dto.response.KeywordInfoResponse;
import com.onair.hearit.admin.presentation.AdminSecurityTestHelper.CsrfSession;
import com.onair.hearit.domain.Keyword;
import com.onair.hearit.dto.response.PagedResponse;
import com.onair.hearit.fixture.IntegrationTest;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class AdminKeywordControllerTest extends IntegrationTest {

    @Test
    @DisplayName("키워드를 페이징 조회할 수 있다")
    void getPagedKeywords() {
        // given
        insertTestKeywords(20);
        CsrfSession csrf = AdminSecurityTestHelper.loginAdminAndGetCsrfSession(dbHelper);

        // when
        PagedResponse<KeywordInfoResponse> response = RestAssured.given().log().all()
                .cookie("JSESSIONID", csrf.sessionId())
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/api/v1/admin/keywords")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract().as(new TypeRef<>() {
                });

        // then
        assertAll(() -> {
            assertThat(response.page()).isEqualTo(0);
            assertThat(response.size()).isEqualTo(10);
            assertThat(response.totalElements()).isEqualTo(20);
        });
    }

    @Test
    @DisplayName("모든 키워드를 조회할 수 있다")
    void getAllKeywords() {
        // given
        insertTestKeywords(3);
        CsrfSession csrf = AdminSecurityTestHelper.loginAdminAndGetCsrfSession(dbHelper);

        // when
        List<KeywordInfoResponse> response = RestAssured.given().log().all()
                .cookie("JSESSIONID", csrf.sessionId())
                .when()
                .get("/api/v1/admin/keywords/all")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract().as(new TypeRef<>() {
                });

        // then
        assertThat(response).hasSize(3);
    }

    @Test
    @DisplayName("새 키워드를 등록할 수 있다")
    void createKeyword() {
        // given
        CsrfSession csrf = AdminSecurityTestHelper.loginAdminAndGetCsrfSession(dbHelper);
        KeywordCreateRequest request = new KeywordCreateRequest("AI");

        // when & then
        RestAssured.given().log().all()
                .cookie("JSESSIONID", csrf.sessionId())
                .header("X-CSRF-TOKEN", csrf.csrfToken())
                .contentType("application/json")
                .body(request)
                .when()
                .post("/api/v1/admin/keywords")
                .then().log().all()
                .statusCode(HttpStatus.CREATED.value());
    }

    private void insertTestKeywords(int count) {
        for (int i = 0; i < count; i++) {
            dbHelper.insertKeyword(new Keyword("키워드" + i));
        }
    }
}
