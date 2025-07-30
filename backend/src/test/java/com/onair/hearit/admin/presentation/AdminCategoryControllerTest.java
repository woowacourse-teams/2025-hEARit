package com.onair.hearit.admin.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.onair.hearit.admin.dto.request.CategoryCreateRequest;
import com.onair.hearit.admin.dto.response.CategoryInfoResponse;
import com.onair.hearit.admin.presentation.AdminSecurityTestHelper.CsrfSession;
import com.onair.hearit.domain.Category;
import com.onair.hearit.dto.response.PagedResponse;
import com.onair.hearit.fixture.IntegrationTest;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.contract.spec.internal.HttpStatus;

class AdminCategoryControllerTest extends IntegrationTest {

    @Test
    @DisplayName("카테고리 목록을 페이징 조회할 수 있다")
    void getPagedCategories() {
        // given
        insertTestCategories(20);
        CsrfSession csrf = AdminSecurityTestHelper.loginAdminAndGetCsrfSession(dbHelper);

        // when
        PagedResponse<CategoryInfoResponse> response = RestAssured.given().log().all()
                .cookie("JSESSIONID", csrf.sessionId())
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/api/v1/admin/categories")
                .then().log().all()
                .statusCode(HttpStatus.OK)
                .extract().as(new TypeRef<>() {});

        // then
        assertAll(() -> {
            assertThat(response.page()).isEqualTo(0);
            assertThat(response.size()).isEqualTo(10);
            assertThat(response.totalElements()).isEqualTo(20);
        });
    }

    @Test
    @DisplayName("전체 카테고리를 조회할 수 있다")
    void getAllCategories() {
        // given
        insertTestCategories(3);
        CsrfSession csrf = AdminSecurityTestHelper.loginAdminAndGetCsrfSession(dbHelper);

        // when
        List<CategoryInfoResponse> response = RestAssured.given().log().all()
                .cookie("JSESSIONID", csrf.sessionId())
                .when()
                .get("/api/v1/admin/categories/all")
                .then().log().all()
                .statusCode(HttpStatus.OK)
                .extract().as(new TypeRef<>() {});

        // then
        assertThat(response).hasSize(3);
    }

    @Test
    @DisplayName("카테고리를 생성할 수 있다")
    void createCategory() {
        // given
        CsrfSession csrf = AdminSecurityTestHelper.loginAdminAndGetCsrfSession(dbHelper);
        CategoryCreateRequest request = new CategoryCreateRequest("새 카테고리", "#12345678");

        // when & then
        RestAssured.given().log().all()
                .cookie("JSESSIONID", csrf.sessionId())
                .header("X-CSRF-TOKEN", csrf.csrfToken())
                .contentType("application/json")
                .body(request)
                .when()
                .post("/api/v1/admin/categories")
                .then().log().all()
                .statusCode(HttpStatus.CREATED);
    }

    private void insertTestCategories(int count) {
        for (int i = 0; i < count; i++) {
            dbHelper.insertCategory(new Category("카테고리" + i, "#12345678"));
        }
    }
}
