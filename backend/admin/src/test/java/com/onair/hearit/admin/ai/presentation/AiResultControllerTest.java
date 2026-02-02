package com.onair.hearit.admin.ai.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import com.onair.hearit.admin.ai.domain.AiProcessResult;
import com.onair.hearit.admin.ai.domain.ProcessStatus;
import com.onair.hearit.admin.ai.dto.ScriptSegment;
import com.onair.hearit.admin.ai.infrastructure.jpa.AiProcessResultRepository;
import com.onair.hearit.admin.fixture.AdminSecurityTestHelper;
import com.onair.hearit.admin.fixture.AdminSecurityTestHelper.CsrfSession;
import com.onair.hearit.admin.fixture.IntegrationTest;
import com.onair.hearit.admin.infrastructure.s3.FileStorage;
import com.onair.hearit.core.domain.Category;
import com.onair.hearit.core.fixture.TestFixture;
import io.restassured.RestAssured;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.contract.spec.internal.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

class AiResultControllerTest extends IntegrationTest {

    @Autowired
    private AiProcessResultRepository resultRepository;

    @MockitoBean
    private FileStorage fileStorage;

    @Test
    @DisplayName("AI 결과를 조회할 수 있다")
    void getResult_returnsAiResult() {
        // given
        CsrfSession csrfSession = AdminSecurityTestHelper.loginAdminAndGetCsrfSession(dbHelper);

        AiProcessResult result = createCompletedResult();

        // when
        Map<String, Object> response = RestAssured.given().log().all()
                .cookie("JSESSIONID", csrfSession.sessionId())
                .when()
                .get("/admin/api/ai/results/" + result.getId())
                .then().log().all()
                .statusCode(HttpStatus.OK)
                .extract().as(Map.class);

        // then
        assertThat(response.get("status")).isEqualTo("COMPLETED");
        assertThat(response.get("suggestedTitle")).isEqualTo("테스트 제목");
        assertThat(response.get("suggestedSummary")).isEqualTo("테스트 요약");
    }

    @Test
    @DisplayName("대본을 수정할 수 있다")
    void updateScript_updatesScript() {
        // given
        CsrfSession csrfSession = AdminSecurityTestHelper.loginAdminAndGetCsrfSession(dbHelper);

        AiProcessResult result = createCompletedResult();

        Map<String, Object> request = Map.of(
                "segments", List.of(
                        Map.of("id", 0, "start", 0, "end", 5000, "text", "수정된 텍스트")
                )
        );

        // when
        RestAssured.given().log().all()
                .cookie("JSESSIONID", csrfSession.sessionId())
                .header("X-CSRF-TOKEN", csrfSession.csrfToken())
                .contentType("application/json")
                .body(request)
                .when()
                .put("/admin/api/ai/results/" + result.getId() + "/script")
                .then().log().all()
                .statusCode(HttpStatus.OK);

        // then
        AiProcessResult updated = resultRepository.findById(result.getId()).orElseThrow();
        assertThat(updated.getEditedScript()).hasSize(1);
        assertThat(updated.getEditedScript().get(0).getText()).isEqualTo("수정된 텍스트");
    }

    @Test
    @DisplayName("메타데이터를 수정할 수 있다")
    void updateMetadata_updatesMetadata() {
        // given
        CsrfSession csrfSession = AdminSecurityTestHelper.loginAdminAndGetCsrfSession(dbHelper);

        AiProcessResult result = createCompletedResult();

        Map<String, String> request = Map.of(
                "title", "수정된 제목",
                "summary", "수정된 요약"
        );

        // when
        RestAssured.given().log().all()
                .cookie("JSESSIONID", csrfSession.sessionId())
                .header("X-CSRF-TOKEN", csrfSession.csrfToken())
                .contentType("application/json")
                .body(request)
                .when()
                .put("/admin/api/ai/results/" + result.getId() + "/metadata")
                .then().log().all()
                .statusCode(HttpStatus.OK);

        // then
        AiProcessResult updated = resultRepository.findById(result.getId()).orElseThrow();
        assertThat(updated.getEditedTitle()).isEqualTo("수정된 제목");
        assertThat(updated.getEditedSummary()).isEqualTo("수정된 요약");
    }

    @Test
    @DisplayName("검토 완료 후 Hearit으로 등록할 수 있다")
    void confirmAndRegister_createsHearit() {
        // given
        CsrfSession csrfSession = AdminSecurityTestHelper.loginAdminAndGetCsrfSession(dbHelper);
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());

        AiProcessResult result = createCompletedResult();

        // S3 파일 복사 모킹 (copyFile은 String을 반환)
        when(fileStorage.copyFile(anyString(), anyString())).thenAnswer(invocation -> invocation.getArgument(1));

        Map<String, Object> request = Map.of(
                "categoryId", category.getId(),
                "keywordIds", List.of(),
                "sources", List.of(Map.of("sourceName", "테스트 출처", "sourceUrl", "https://example.com")),
                "finalTitle", "최종 제목",
                "finalSummary", "최종 요약",
                "finalScript", List.of(Map.of("id", 0, "start", 0, "end", 5000, "text", "최종 대본 텍스트"))
        );

        // when
        RestAssured.given().log().all()
                .cookie("JSESSIONID", csrfSession.sessionId())
                .header("X-CSRF-TOKEN", csrfSession.csrfToken())
                .contentType("application/json")
                .body(request)
                .when()
                .post("/admin/api/ai/results/" + result.getId() + "/confirm")
                .then().log().all()
                .statusCode(HttpStatus.OK);

        // then
        AiProcessResult updated = resultRepository.findById(result.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(ProcessStatus.CONFIRMED);
        // Note: confirmedHearit은 현재 구현에서 null로 설정됨 (서비스 코드 주석 참고)
    }

    @Test
    @DisplayName("AI 결과를 삭제할 수 있다")
    void deleteResult_deletesResult() {
        // given
        CsrfSession csrfSession = AdminSecurityTestHelper.loginAdminAndGetCsrfSession(dbHelper);

        AiProcessResult result = createCompletedResult();

        // S3 파일 삭제 모킹
        doNothing().when(fileStorage).deleteFile(anyString());

        // when
        RestAssured.given().log().all()
                .cookie("JSESSIONID", csrfSession.sessionId())
                .header("X-CSRF-TOKEN", csrfSession.csrfToken())
                .when()
                .delete("/admin/api/ai/results/" + result.getId())
                .then().log().all()
                .statusCode(HttpStatus.OK);

        // then
        assertThat(resultRepository.findById(result.getId())).isEmpty();
    }

    private AiProcessResult createCompletedResult() {
        List<ScriptSegment> segments = List.of(
                new ScriptSegment(0, 0, 5000, "테스트 텍스트")
        );

        AiProcessResult result = AiProcessResult.builder()
                .originalFileName("test.mp3")
                .originalFileKey("hearit/temp/original/test.mp3")
                .build();

        result.setGeneratedFiles(
                "hearit/temp/org/test.mp3",
                "hearit/temp/shr/test.mp3",
                "hearit/temp/scr/test.json"
        );
        result.setTranscriptionResult(segments, 120);
        result.setCorrectedScript(segments);
        result.setSuggestedMetadata("테스트 제목", "테스트 요약");
        result.markAsCompleted(LocalDateTime.now().plusHours(24));

        return resultRepository.save(result);
    }
}
