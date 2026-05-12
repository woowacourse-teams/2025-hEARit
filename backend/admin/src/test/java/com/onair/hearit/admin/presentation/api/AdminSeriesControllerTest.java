package com.onair.hearit.admin.presentation.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.onair.hearit.admin.dto.request.SeriesCreateRequest;
import com.onair.hearit.admin.dto.response.UploadUrlResponse;
import com.onair.hearit.admin.fixture.AdminSecurityTestHelper;
import com.onair.hearit.admin.fixture.AdminSecurityTestHelper.CsrfSession;
import com.onair.hearit.admin.fixture.IntegrationTest;
import com.onair.hearit.admin.infrastructure.s3.FileStorage;
import com.onair.hearit.core.domain.Series;
import com.onair.hearit.core.infrastructure.jpa.SeriesRepository;
import io.restassured.RestAssured;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.contract.spec.internal.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

class AdminSeriesControllerTest extends IntegrationTest {

    @Autowired
    private SeriesRepository seriesRepository;

    @MockitoBean
    private FileStorage fileStorage;

    @Test
    @DisplayName("시리즈 이미지 업로드 URL을 발급받을 수 있다.")
    void createSeriesImageUploadUrl() throws MalformedURLException {
        // given
        CsrfSession csrf = AdminSecurityTestHelper.loginAdminAndGetCsrfSession(dbHelper);
        URI mockUri = URI.create("https://s3.amazonaws.com/bucket/series/image/test.jpg");
        given(fileStorage.createPutUrl(any())).willReturn(mockUri.toURL());

        // when
        UploadUrlResponse response = RestAssured.given().log().all()
                .cookie("JSESSIONID", csrf.sessionId())
                .header("X-CSRF-TOKEN", csrf.csrfToken())
                .when()
                .post("/api/v1/admin/series/upload-url")
                .then().log().all()
                .statusCode(HttpStatus.OK)
                .extract().as(UploadUrlResponse.class);

        // then
        assertAll(
                () -> assertThat(response.key()).startsWith("/series/image/"),
                () -> assertThat(response.key()).endsWith(".jpg"),
                () -> assertThat(response.url()).isNotNull()
        );
    }

    @Test
    @DisplayName("시리즈를 생성할 수 있다.")
    void createSeries() {
        // given
        CsrfSession csrf = AdminSecurityTestHelper.loginAdminAndGetCsrfSession(dbHelper);
        SeriesCreateRequest request = new SeriesCreateRequest(
                "테코톡 모음",
                "테코톡을 모은 시리즈입니다.",
                "/series/image/test.jpg"
        );

        // when
        RestAssured.given().log().all()
                .cookie("JSESSIONID", csrf.sessionId())
                .header("X-CSRF-TOKEN", csrf.csrfToken())
                .contentType("application/json")
                .body(request)
                .when()
                .post("/api/v1/admin/series")
                .then().log().all()
                .statusCode(HttpStatus.CREATED);

        // then
        List<Series> allSeries = seriesRepository.findAll();
        assertAll(
                () -> assertThat(allSeries).hasSize(1),
                () -> assertThat(allSeries.get(0).getTitle()).isEqualTo("테코톡 모음")
        );
    }

    @Test
    @DisplayName("시리즈 제목이 비어 있으면 400 BAD_REQUEST를 반환한다.")
    void createSeries_blankTitle() {
        // given
        CsrfSession csrf = AdminSecurityTestHelper.loginAdminAndGetCsrfSession(dbHelper);
        SeriesCreateRequest request = new SeriesCreateRequest("", null, null);

        // when & then
        RestAssured.given().log().all()
                .cookie("JSESSIONID", csrf.sessionId())
                .header("X-CSRF-TOKEN", csrf.csrfToken())
                .contentType("application/json")
                .body(request)
                .when()
                .post("/api/v1/admin/series")
                .then().log().all()
                .statusCode(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("미인증 상태로 시리즈 생성 요청 시 403을 반환한다.")
    void createSeries_unauthenticated() {
        SeriesCreateRequest request = new SeriesCreateRequest("테스트", null, null);

        RestAssured.given().log().all()
                .contentType("application/json")
                .body(request)
                .when()
                .post("/api/v1/admin/series")
                .then().log().all()
                .statusCode(HttpStatus.FORBIDDEN);
    }
}
