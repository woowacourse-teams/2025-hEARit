package com.onair.hearit.admin.ai.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.onair.hearit.admin.ai.domain.AiProcessResult;
import com.onair.hearit.admin.ai.domain.ProcessStatus;
import com.onair.hearit.admin.ai.infrastructure.jpa.AiProcessResultRepository;
import com.onair.hearit.admin.fixture.AdminSecurityTestHelper;
import com.onair.hearit.admin.fixture.AdminSecurityTestHelper.CsrfSession;
import com.onair.hearit.admin.fixture.IntegrationTest;
import com.onair.hearit.admin.infrastructure.s3.FileStorage;
import io.restassured.RestAssured;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.contract.spec.internal.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

class AiProcessControllerTest extends IntegrationTest {

    @Autowired
    private AiProcessResultRepository resultRepository;

    @MockitoBean
    private FileStorage fileStorage;

    @Test
    @DisplayName("AI 처리를 시작하고 processId를 반환한다")
    void startProcess_returnsProcessId() {
        // given
        CsrfSession csrfSession = AdminSecurityTestHelper.loginAdminAndGetCsrfSession(dbHelper);

        // S3 업로드 모킹 (uploadBytes는 String을 반환)
        when(fileStorage.uploadBytes(any(), anyString(), anyString())).thenAnswer(invocation -> invocation.getArgument(1));

        // 유효한 MP3 데이터 생성 (ID3 태그)
        byte[] mp3Data = createValidMp3Data();

        // when
        Map<String, Object> response = RestAssured.given().log().all()
                .cookie("JSESSIONID", csrfSession.sessionId())
                .header("X-CSRF-TOKEN", csrfSession.csrfToken())
                .multiPart("audioFile", "test.mp3", mp3Data, "audio/mpeg")
                .when()
                .post("/admin/api/ai/process")
                .then().log().all()
                .statusCode(HttpStatus.OK)
                .extract().as(Map.class);

        // then
        assertThat(response).containsKey("processId");
        Long processId = ((Number) response.get("processId")).longValue();
        assertThat(processId).isPositive();

        // DB에 저장되었는지 확인
        AiProcessResult savedResult = resultRepository.findById(processId).orElse(null);
        assertThat(savedResult).isNotNull();
        assertThat(savedResult.getOriginalFileName()).isEqualTo("test.mp3");
    }

    @Test
    @DisplayName("MP3가 아닌 파일은 400 에러를 반환한다")
    void startProcess_rejectsNonMp3File() {
        // given
        CsrfSession csrfSession = AdminSecurityTestHelper.loginAdminAndGetCsrfSession(dbHelper);

        byte[] invalidData = "not mp3 data".getBytes();

        // when & then
        RestAssured.given().log().all()
                .cookie("JSESSIONID", csrfSession.sessionId())
                .header("X-CSRF-TOKEN", csrfSession.csrfToken())
                .multiPart("audioFile", "test.wav", invalidData, "audio/wav")
                .when()
                .post("/admin/api/ai/process")
                .then().log().all()
                .statusCode(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("처리 상태를 조회할 수 있다")
    void getStatus_returnsCurrentStatus() {
        // given
        CsrfSession csrfSession = AdminSecurityTestHelper.loginAdminAndGetCsrfSession(dbHelper);

        AiProcessResult result = AiProcessResult.builder()
                .originalFileName("test.mp3")
                .originalFileKey("hearit/temp/original/test.mp3")
                .build();
        result.markAsTranscribing();
        result = resultRepository.save(result);

        // when
        Map<String, Object> response = RestAssured.given().log().all()
                .cookie("JSESSIONID", csrfSession.sessionId())
                .when()
                .get("/admin/api/ai/process/" + result.getId() + "/status")
                .then().log().all()
                .statusCode(HttpStatus.OK)
                .extract().as(Map.class);

        // then
        assertThat(response.get("status")).isEqualTo("TRANSCRIBING");
        assertThat(response.get("progress")).isEqualTo(50);
    }

    @Test
    @DisplayName("존재하지 않는 processId로 상태 조회 시 404를 반환한다")
    void getStatus_returns404ForNonExistentProcess() {
        // given
        CsrfSession csrfSession = AdminSecurityTestHelper.loginAdminAndGetCsrfSession(dbHelper);

        // when & then
        RestAssured.given().log().all()
                .cookie("JSESSIONID", csrfSession.sessionId())
                .when()
                .get("/admin/api/ai/process/999999/status")
                .then().log().all()
                .statusCode(HttpStatus.NOT_FOUND);
    }

    private byte[] createValidMp3Data() {
        byte[] data = new byte[1000];
        // ID3v2 태그
        data[0] = 'I';
        data[1] = 'D';
        data[2] = '3';
        return data;
    }
}
