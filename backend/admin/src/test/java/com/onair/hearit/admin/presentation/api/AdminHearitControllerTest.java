package com.onair.hearit.admin.presentation.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import com.onair.hearit.admin.dto.request.HearitInfoUpdateRequest;
import com.onair.hearit.admin.dto.request.HearitInfoUpdateRequest.SourceUpdateRequest;
import com.onair.hearit.admin.dto.response.AdminHearitResponse;
import com.onair.hearit.admin.dto.response.AdminPagedResponse;
import com.onair.hearit.admin.fixture.AdminSecurityTestHelper;
import com.onair.hearit.admin.fixture.AdminSecurityTestHelper.CsrfSession;
import com.onair.hearit.admin.fixture.IntegrationTest;
import com.onair.hearit.admin.infrastructure.s3.FileStorage;
import com.onair.hearit.core.domain.Category;
import com.onair.hearit.core.domain.FileType;
import com.onair.hearit.core.domain.Hearit;
import com.onair.hearit.core.domain.Keyword;
import com.onair.hearit.core.domain.Source;
import com.onair.hearit.core.fixture.TestFixture;
import com.onair.hearit.core.infrastructure.jpa.HearitRepository;
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
    private FileStorage fileStorage;

    @Test
    @DisplayName("히어릿 목록을 페이징 조회할 수 있다")
    void getPagedHearits() {
        // given
        insertTestHearits(20); // 테스트용 더미 hearit 20개 삽입
        CsrfSession csrfSession = AdminSecurityTestHelper.loginAdminAndGetCsrfSession(dbHelper);

        // when & then
        AdminPagedResponse<AdminHearitResponse> response =
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
                () -> assertThat(response.page()).isZero(),
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

        String expectedOriginalPath = "/hearit/audio/original/ORG_test.mp3";
        String expectedShortPath = "/hearit/audio/short/SHR_test.mp3";
        String expectedScriptPath = "/hearit/script/SCR_test.json";

        given(fileStorage.uploadFile(any(), eq(FileType.ORIGINAL))).willReturn(expectedOriginalPath);
        given(fileStorage.uploadFile(any(), eq(FileType.SHORT))).willReturn(expectedShortPath);
        given(fileStorage.uploadFile(any(), eq(FileType.SCRIPT))).willReturn(expectedScriptPath);

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
                .multiPart("sources[0].sourceName", "출처명1")
                .multiPart("sources[0].sourceUrl", "https://example.com/1")
                .multiPart("sources[1].sourceName", "출처명2")
                .multiPart("sources[1].sourceUrl", "https://example.com/2")
                .multiPart("categoryId", category.getId().toString())
                .multiPart("keywordIds", keyword.getId().toString())
                .when()
                .post("/api/v1/admin/hearits")
                .then().log().all()
                .statusCode(HttpStatus.CREATED);
    }

    @Test
    @DisplayName("히어릿 메타데이터를 수정할 수 있다")
    void updateMetaDataHearit() {
        // given
        CsrfSession csrfSession = AdminSecurityTestHelper.loginAdminAndGetCsrfSession(dbHelper);

        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Hearit hearit = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));

        HearitInfoUpdateRequest request = new HearitInfoUpdateRequest(
                "수정 제목", "수정 요약", 100, "/hearit/audio/original/ORG_test.mp3", "/hearit/audio/short/SHR_test.mp3",
                "/hearit/script/SCR_test.json", List.of(new SourceUpdateRequest("출처", "url")), category.getId(),
                List.of()
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

        Hearit updatedHearit = hearitRepository.findByIdWithCategoryAndSources(hearit.getId()).orElseThrow();
        assertAll(() -> {
            assertThat(updatedHearit.getTitle()).isEqualTo("수정 제목");
            assertThat(updatedHearit.getSummary()).isEqualTo("수정 요약");
        });
    }

    @Test
    @DisplayName("히어릿 메타데이터-출처를 수정할 수 있다")
    void updateSourceDataHearit() {
        // given
        CsrfSession csrfSession = AdminSecurityTestHelper.loginAdminAndGetCsrfSession(dbHelper);

        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Hearit hearit = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));

        HearitInfoUpdateRequest request = new HearitInfoUpdateRequest(
                "수정 제목", "수정 요약", 100, "origin-audio", "short-audio",
                "script-url",
                List.of(
                        new SourceUpdateRequest("수정출처1", "수정 url1"),
                        new SourceUpdateRequest("수정출처2", "수정 url2")
                ), category.getId(), List.of()
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

        Hearit updatedHearit = hearitRepository.findByIdWithCategoryAndSources(hearit.getId()).orElseThrow();
        assertAll(() -> {
            assertThat(updatedHearit.getSources().get(0).getSourceName()).isEqualTo("수정출처1");
            assertThat(updatedHearit.getSources().get(1).getSourceName()).isEqualTo("수정출처2");
            assertThat(updatedHearit.getSources().get(0).getSourceUrl()).isEqualTo("수정 url1");
            assertThat(updatedHearit.getSources().get(1).getSourceUrl()).isEqualTo("수정 url2");
        });
    }

    private void insertTestHearits(int count) {
        Category category = TestFixture.createFixedCategory();
        dbHelper.insertCategory(category);

        for (int i = 0; i < count; i++) {
            Hearit hearit = new Hearit("title" + i,
                    "summary" + i,
                    100,
                    "/hearit/audio/original/ORG_test" + i + ".mp3",
                    "/hearit/audio/short/SHR_test" + i + ".mp3",
                    "/hearit/script/SCR_test" + i + ".json",
                    List.of(new Source("출처", "https://example.com")),
                    category
            );
            dbHelper.insertHearit(hearit);
        }
    }
}
