package com.onair.hearit.admin.presentation.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import com.onair.hearit.admin.dto.request.UploadUrlRequest;
import com.onair.hearit.admin.dto.response.FilesUploadUrlResponse;
import com.onair.hearit.admin.fixture.AdminSecurityTestHelper;
import com.onair.hearit.admin.fixture.AdminSecurityTestHelper.CsrfSession;
import com.onair.hearit.admin.fixture.IntegrationTest;
import com.onair.hearit.admin.infrastructure.s3.FileStorage;
import com.onair.hearit.core.domain.Category;
import com.onair.hearit.core.domain.FileType;
import com.onair.hearit.core.domain.Hearit;
import com.onair.hearit.core.domain.Source;
import com.onair.hearit.core.fixture.TestFixture;
import com.onair.hearit.core.infrastructure.jpa.HearitRepository;
import io.restassured.RestAssured;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.contract.spec.internal.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

class AdminStorageControllerTest extends IntegrationTest {

    @Autowired
    private HearitRepository hearitRepository;

    @MockitoBean
    private FileStorage fileStorage;

    @Test
    @DisplayName("원본, 미리듣기, 대본의 업로드 URL을 발급 받을 수 있다")
    void getUploadUrlTest() throws MalformedURLException {
        // given
        CsrfSession csrfSession = AdminSecurityTestHelper.loginAdminAndGetCsrfSession(dbHelper);
        URI uri = URI.create("http://localhost:8080/presigned-url");
        given(fileStorage.createPutUrl(any())).willReturn(uri.toURL());

        UploadUrlRequest request = new UploadUrlRequest("ORG_test.mp3", "SHR_test.mp3", "SCR_test.json");

        // when & then
        FilesUploadUrlResponse response = RestAssured.given().log().all()
                .cookie("JSESSIONID", csrfSession.sessionId())
                .header("X-CSRF-TOKEN", csrfSession.csrfToken())
                .header("Content-Type", "application/json")
                .body(request)
                .when()
                .post("/api/v1/admin/storage/upload-urls")
                .then().log().all()
                .statusCode(HttpStatus.OK)
                .extract()
                .as(FilesUploadUrlResponse.class);

        assertAll(
                () -> assertThat(response.originalAudio().url()).isEqualTo(uri.toURL()),
                () -> assertThat(response.originalAudio().key()).isEqualTo("/hearit/audio/original/ORG_test.mp3"),
                () -> assertThat(response.shortAudio().url()).isEqualTo(uri.toURL()),
                () -> assertThat(response.shortAudio().key()).isEqualTo("/hearit/audio/short/SHR_test.mp3"),
                () -> assertThat(response.script().url()).isEqualTo(uri.toURL()),
                () -> assertThat(response.script().key()).isEqualTo("/hearit/script/SCR_test.json")
        );
    }

    @Test
    @DisplayName("히어릿 Original 음원 파일을 수정할 수 있다")
    void updateHearitOriginalFile() {
        // given
        CsrfSession csrfSession = AdminSecurityTestHelper.loginAdminAndGetCsrfSession(dbHelper);
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());

        String originalPath = "/hearit/audio/original/ORG_test.mp3";
        String shortPath = "/hearit/audio/short/SHR_test.mp3";
        String scriptPath = "/hearit/script/SCR_test.json";

        Hearit hearit = dbHelper.insertHearit(
                new Hearit("title", "summary",
                        10, originalPath,
                        shortPath, scriptPath,
                        List.of(new Source("출처", "url")), category));
        given(fileStorage.uploadFile(any(), eq(FileType.ORIGINAL))).willReturn(originalPath);

        // when & then
        RestAssured.given().log().all()
                .cookie("JSESSIONID", csrfSession.sessionId())
                .header("X-CSRF-TOKEN", csrfSession.csrfToken())
                .multiPart("file", new File("src/test/resources/ORG_test.mp3"))
                .when()
                .put("/api/v1/admin/hearits/" + hearit.getId() + "/original-audio")
                .then().log().all()
                .statusCode(HttpStatus.NO_CONTENT);

        Hearit updatedHearit = hearitRepository.findById(hearit.getId()).orElseThrow();
        assertThat(updatedHearit.getOriginalAudioUrl()).isEqualTo(originalPath);

    }

    @Test
    @DisplayName("히어릿 Short 음원 파일을 수정할 수 있다")
    void updateHearitShortFile() {
        // given
        CsrfSession csrfSession = AdminSecurityTestHelper.loginAdminAndGetCsrfSession(dbHelper);
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());

        String originalPath = "/hearit/audio/original/ORG_test.mp3";
        String shortPath = "/hearit/audio/short/SHR_test.mp3";
        String scriptPath = "/hearit/script/SCR_test.json";

        Hearit hearit = dbHelper.insertHearit(
                new Hearit("title", "summary",
                        10, originalPath,
                        shortPath, scriptPath,
                        List.of(new Source("출처", "url")), category));
        given(fileStorage.uploadFile(any(), eq(FileType.SHORT))).willReturn(shortPath);

        // when & then
        RestAssured.given().log().all()
                .cookie("JSESSIONID", csrfSession.sessionId())
                .header("X-CSRF-TOKEN", csrfSession.csrfToken())
                .multiPart("file", new File("src/test/resources/SHR_test.mp3"))
                .when()
                .put("/api/v1/admin/hearits/" + hearit.getId() + "/short-audio")
                .then().log().all()
                .statusCode(HttpStatus.NO_CONTENT);

        Hearit updatedHearit = hearitRepository.findById(hearit.getId()).orElseThrow();

        assertThat(updatedHearit.getShortAudioUrl()).isEqualTo(shortPath);

    }

    @Test
    @DisplayName("히어릿 Script 파일을 수정할 수 있다")
    void updateHearitScriptFile() {
        // given
        CsrfSession csrfSession = AdminSecurityTestHelper.loginAdminAndGetCsrfSession(dbHelper);
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());

        String originalPath = "/hearit/audio/original/ORG_test.mp3";
        String shortPath = "/hearit/audio/short/SHR_test.mp3";
        String scriptPath = "/hearit/script/SCR_test.json";

        Hearit hearit = dbHelper.insertHearit(
                new Hearit("title", "summary",
                        10, originalPath,
                        shortPath, scriptPath,
                        List.of(new Source("출처", "url")), category));
        given(fileStorage.uploadFile(any(), eq(FileType.SCRIPT))).willReturn(scriptPath);

        // when & then
        RestAssured.given().log().all()
                .cookie("JSESSIONID", csrfSession.sessionId())
                .header("X-CSRF-TOKEN", csrfSession.csrfToken())
                .multiPart("file", new File("src/test/resources/SCR_test.json"))
                .when()
                .put("/api/v1/admin/hearits/" + hearit.getId() + "/script")
                .then().log().all()
                .statusCode(HttpStatus.NO_CONTENT);

        Hearit updatedHearit = hearitRepository.findById(hearit.getId()).orElseThrow();

        assertThat(updatedHearit.getScriptUrl()).isEqualTo("/hearit/script/SCR_test.json");

    }
}
