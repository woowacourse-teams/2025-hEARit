package com.onair.hearit.admin.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.onair.hearit.admin.application.S3Uploader;
import com.onair.hearit.admin.dto.request.HearitUpdateRequest;
import com.onair.hearit.admin.dto.response.HearitAdminResponse;
import com.onair.hearit.admin.presentation.AdminSecurityTestHelper.CsrfSession;
import com.onair.hearit.domain.Category;
import com.onair.hearit.domain.Hearit;
import com.onair.hearit.domain.Keyword;
import com.onair.hearit.dto.response.PagedResponse;
import com.onair.hearit.fixture.IntegrationTest;
import com.onair.hearit.fixture.TestFixture;
import com.onair.hearit.infrastructure.HearitRepository;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import java.io.File;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.contract.spec.internal.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

class AdminHearitControllerTest extends IntegrationTest {

    @Autowired
    private HearitRepository hearitRepository;

    @MockitoBean
    private S3Uploader s3Uploader;
    @Test
    @DisplayName("히어릿 목록을 페이징 조회할 수 있다")
    void getPagedHearits() {
        // given
        insertTestHearits(20); // 테스트용 더미 hearit 20개 삽입
        CsrfSession csrfSession = AdminSecurityTestHelper.loginAdminAndGetCsrfSession(dbHelper);

        // when & then
        PagedResponse<HearitAdminResponse> response =
                RestAssured.given().log().all()
                        .cookie("JSESSIONID", csrfSession.sessionId())
                        .queryParam("page", 0)
                        .queryParam("size", 10)
                        .when()
                        .get("/api/v1/admin/hearits")
                        .then().log().all()
                        .statusCode(HttpStatus.OK)
                        .extract().as(new TypeRef<>() {
                        });

        assertAll(
                () -> assertThat(response.page()).isEqualTo(0),
                () -> assertThat(response.size()).isEqualTo(10),
                () -> assertThat(response.totalElements()).isEqualTo(20)
        );
    }

    @Test
    @DisplayName("히어릿을 생성할 수 있다")
    void createHearit() {
        // given
        CsrfSession csrfSession = AdminSecurityTestHelper.loginAdminAndGetCsrfSession(dbHelper);

        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Keyword keyword = dbHelper.insertKeyword(TestFixture.createFixedKeyword());

        given(s3Uploader.uploadOriginalAudio(any())).willReturn("mock/origin.mp3");
        given(s3Uploader.uploadShortAudio(any())).willReturn("mock/short.mp3");
        given(s3Uploader.uploadScriptFile(any())).willReturn("mock/script.json");

        // when & then
        RestAssured.given().log().uri()
                .cookie("JSESSIONID", csrfSession.sessionId())
                .header("X-CSRF-TOKEN", csrfSession.csrfToken())
                .multiPart("title", "히어릿 제목")
                .multiPart("summary", "히어릿 요약")
                .multiPart("playTime", "100")
                .multiPart("originalAudio", new File("src/test/resources/ORG_test.mp3"))
                .multiPart("shortAudio", new File("src/test/resources/SHR_test.mp3"))
                .multiPart("scriptFile", new File("src/test/resources/SCR_test.json"))
                .multiPart("source", "출처")
                .multiPart("categoryId", category.getId().toString())
                .multiPart("keywordIds", keyword.getId().toString())
                .when()
                .post("/api/v1/admin/hearits")
                .then().log().all()
                .statusCode(HttpStatus.CREATED);
    }

    @Test
    @DisplayName("히어릿을 수정할 수 있다")
    void updateHearit() {
        // given
        CsrfSession csrfSession = AdminSecurityTestHelper.loginAdminAndGetCsrfSession(dbHelper);

        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Hearit hearit = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));

        HearitUpdateRequest request = new HearitUpdateRequest(
                "수정 제목", "수정 요약", 100, "origin-audio", "short-audio",
                "script-url", "출처", category.getId(), List.of()
        );

        // when & then
        RestAssured.given().log().all()
                .cookie("JSESSIONID", csrfSession.sessionId())
                .header("X-CSRF-TOKEN", csrfSession.csrfToken())
                .contentType("application/json")
                .body(request)
                .when()
                .put("/api/v1/admin/hearits/" + hearit.getId())
                .then().log().all()
                .statusCode(HttpStatus.NO_CONTENT);

        Hearit updatedHearit = hearitRepository.findById(hearit.getId()).orElseThrow();
        assertAll(() -> {
            assertThat(updatedHearit.getTitle()).isEqualTo("수정 제목");
            assertThat(updatedHearit.getSummary()).isEqualTo("수정 요약");
        });
    }

    private void insertTestHearits(int count) {
        Category category = TestFixture.createFixedCategory();
        dbHelper.insertCategory(category);

        for (int i = 0; i < count; i++) {
            Hearit hearit = new Hearit("title" + i,
                    "summary" + i,
                    100,
                    "origin-audio" + i,
                    "short-audio-url" + i,
                    "script-url" + i,
                    "source" + i,
                    category
            );
            dbHelper.insertHearit(hearit);
        }
    }
}
