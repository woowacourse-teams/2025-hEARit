package com.onair.hearit.app.presentation;

import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static com.epages.restdocs.apispec.RestAssuredRestDocumentationWrapper.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.Schema;
import com.onair.hearit.app.dto.request.PlayingHistoryRequest;
import com.onair.hearit.auth.infrastructure.jwt.JwtTokenProvider;
import com.onair.hearit.common.domain.Category;
import com.onair.hearit.common.domain.Hearit;
import com.onair.hearit.common.domain.Member;
import com.onair.hearit.common.domain.PlayingHistory;
import com.onair.hearit.common.domain.Source;
import com.onair.hearit.docs.ApiDocSnippets;
import com.onair.hearit.fixture.IntegrationTest;
import com.onair.hearit.fixture.TestFixture;
import io.restassured.RestAssured;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

class PlayingHistoryControllerTest extends IntegrationTest {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("로그인한 사용자가 처음 재생기록 저장 시, 201 CREATED를 반환한다.")
    void createPlayingHistory() {
        // given
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
        String token = generateToken(member);
        Category category = dbHelper.insertCategory(new Category("name", "#000000"));
        Hearit hearit = dbHelper.insertHearit(createHearitWith(100, category));

        PlayingHistoryRequest request = new PlayingHistoryRequest(hearit.getId(), 100L);

        // when & then
        RestAssured.given(this.spec)
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body(request)
                .filter(document("playing-history-createad",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Playing History API")
                                .summary("재생기록 생성 또는 수정")
                                .description("""
                                        사용자의 재생 기록을 생성하거나 업데이트합니다.
                                        - 사용자가 특정 히어릿에 대한 재생 기록이 없을 경우, 새로운 기록을 생성하고 `201 Created`를 반환합니다.
                                        - 사용자가 특정 히어릿에 대한 재생 기록이 있을 경우, 기존 기록을 수정하고 `200 OK`를 반환합니다.
                                        """)
                                .requestFields(
                                        fieldWithPath("hearitId").description("히어릿 ID"),
                                        fieldWithPath("lastPlayTime").description("마지막 재생 시간(ms)")
                                )
                                .build())
                ))
                .when()
                .post("/api/v1/playing-histories")
                .then()
                .statusCode(HttpStatus.OK.value());
    }

    @Test
    @DisplayName("로그인한 사용자가 다시 재생기록 저장 시, 200 OK를 반환한다.")
    void updatePlayingHistory() {
        // given
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
        String token = generateToken(member);
        Category category = dbHelper.insertCategory(new Category("name", "#000000"));
        Hearit hearit = dbHelper.insertHearit(createHearitWith(100, category));
        PlayingHistory playingHistory = dbHelper.insertPlayingHistory(
                new PlayingHistory(member.getId(), hearit, 20_000));

        PlayingHistoryRequest request = new PlayingHistoryRequest(hearit.getId(), 50_000L);

        // when & then
        RestAssured.given(this.spec)
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body(request)
                .filter(document("playing-history-ok",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Playing History API")
                                .summary("재생기록 생성 또는 저장")
                                .description("""
                                        사용자의 재생 기록을 생성하거나 업데이트합니다.
                                        - 사용자가 특정 히어릿에 대한 재생 기록이 없을 경우, 새로운 기록을 생성하고 `201 Created`를 반환합니다.
                                        - 사용자가 특정 히어릿에 대한 재생 기록이 있을 경우, 기존 기록을 수정하고 `200 OK`를 반환합니다.
                                        """)
                                .requestFields(
                                        fieldWithPath("hearitId").description("히어릿 ID"),
                                        fieldWithPath("lastPlayTime").description("마지막 재생 시간(ms)")
                                )
                                .build())
                ))
                .when()
                .post("/api/v1/playing-histories")
                .then()
                .statusCode(HttpStatus.OK.value());
    }

    @Test
    @DisplayName("로그인하지 않은 사용자가 재생기록 저장 시, 추가 후 401 UNAUTHORIZED 반환한다.")
    void createBookmarkTestWithConflict() {
        // given
        Category category = dbHelper.insertCategory(new Category("name", "#000000"));
        Hearit hearit = dbHelper.insertHearit(createHearitWith(100, category));

        PlayingHistoryRequest request = new PlayingHistoryRequest(hearit.getId(), 100L);

        // when & then
        RestAssured.given(this.spec)
                .contentType("application/json")
                .body(request)
                .filter(document("playing-history-unauthorized",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Playing History API")
                                .summary("재생기록 생성 또는 저장")
                                .description("""
                                        사용자의 재생 기록을 생성하거나 업데이트합니다.
                                        - 사용자가 특정 히어릿에 대한 재생 기록이 없을 경우, 새로운 기록을 생성하고 `201 Created`를 반환합니다.
                                        - 사용자가 특정 히어릿에 대한 재생 기록이 있을 경우, 기존 기록을 수정하고 `200 OK`를 반환합니다.
                                        """)
                                .requestFields(
                                        fieldWithPath("hearitId").description("히어릿 ID"),
                                        fieldWithPath("lastPlayTime").description("마지막 재생 시간(ms)")
                                )
                                .responseSchema(Schema.schema("ProblemDetail"))
                                .responseFields(ApiDocSnippets.getProblemDetailResponseFields())
                                .build())
                ))
                .when()
                .put("/api/v1/playing-histories")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value());
    }

    private Hearit createHearitWith(int playTime, Category category) {
        return new Hearit("title",
                "summary",
                playTime,
                "/hearit/audio/original/ORG_bf7c513e-579e-4224-8505-3824bb22ed01.mp3",
                "/hearit/audio/short/SHR_bf7c513e-579e-4224-8505-3824bb22ed01.mp3",
                "/hearit/script/SCR_bf7c513e-579e-4224-8505-3824bb22ed01.json",
                List.of(new Source("원본은 CC BY 4.0 라이선스를 따릅니다.", "https://example.com/2")),
                category
        );
    }

    private String generateToken(Member member) {
        return jwtTokenProvider.createAccessToken(member.getId());
    }
}
