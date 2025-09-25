package com.onair.hearit.app.presentation;

import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static com.epages.restdocs.apispec.RestAssuredRestDocumentationWrapper.document;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.onair.hearit.playinghistory.dto.PlayingHistoryRequest;
import com.onair.hearit.playinghistory.dto.RecentlyPlayedHearitResponse;
import com.onair.hearit.auth.infrastructure.jwt.JwtTokenProvider;
import com.onair.hearit.domain.Category;
import com.onair.hearit.domain.Hearit;
import com.onair.hearit.domain.Member;
import com.onair.hearit.domain.PlayingHistory;
import com.onair.hearit.domain.Source;
import com.onair.hearit.core.fixture.TestFixture;
import com.onair.hearit.fixture.IntegrationTest;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

class PlayingHistoryControllerTest extends IntegrationTest {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("로그인한 사용자는 최근 재생 기록 최대 10개와 200 OK를 반환한다.")
    void getRecentPlayingHistories_whenMember() {
        // given
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
        String token = generateToken(member);
        Category category = dbHelper.insertCategory(new Category("name", "#000000"));

        for (int i = 0; i < 12; i++) {
            Hearit hearit = dbHelper.insertHearit(createHearitWith(100 + i, category));
            dbHelper.insertPlayingHistory(new PlayingHistory(member.getId(), hearit, 10L * i));
        }

        // when & then
        List<RecentlyPlayedHearitResponse> response = RestAssured.given(this.spec)
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .filter(document("playing-history-read-member",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Playing History API")
                                .summary("최근 재생 기록 조회")
                                .description("로그인한 사용자는 최근 재생 기록을 최대 10개까지 조회할 수 있습니다.\n\n"
                                             + "로그인하지 않은 사용자는 빈 리스트를 반환합니다.")
                                .responseFields(
                                        fieldWithPath("[].id").description("히어릿 ID"),
                                        fieldWithPath("[].title").description("히어릿 제목"),
                                        fieldWithPath("[].playTime").description("히어릿 전체 재생 시간(s)"),
                                        fieldWithPath("[].lastPlayTime").description("사용자가 마지막으로 재생한 시간(ms)"),
                                        fieldWithPath("[].createdAt").description("히어릿 생성일"),
                                        fieldWithPath("[].category.id").description("카테고리 ID"),
                                        fieldWithPath("[].category.name").description("카테고리 이름"),
                                        fieldWithPath("[].category.colorCode").description("카테고리 색상 코드")
                                ).build())
                ))
                .when()
                .get("/api/v1/playing-histories/hearits")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract().as(new TypeRef<>() {
                });

        assertThat(response).hasSize(10);
    }

    @Test
    @DisplayName("로그인하지 않은 사용자는 최근 재생 기록 조회 시 빈 리스트와 200 OK를 반환한다.")
    void getRecentPlayingHistories_whenGuest() {
        // given
        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
        Category category = dbHelper.insertCategory(new Category("name", "#000000"));
        for (int i = 0; i < 2; i++) {
            Hearit hearit = dbHelper.insertHearit(createHearitWith(100 + i, category));
            dbHelper.insertPlayingHistory(new PlayingHistory(member.getId(), hearit, 10L * i));
        }

        // when & then
        List<RecentlyPlayedHearitResponse> response = RestAssured.given(this.spec)
                .contentType("application/json")
                .filter(document("playing-history-read-guest",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Playing History API")
                                .summary("최근 재생 기록 조회")
                                .description("로그인한 사용자는 최근 재생 기록을 최대 10개까지 조회할 수 있습니다.\n\n"
                                             + "로그인하지 않은 사용자는 빈 리스트를 반환합니다.")
                                .responseFields(
                                        fieldWithPath("[]").description("빈 리스트")
                                ).build())
                ))
                .when()
                .get("/api/v1/playing-histories/hearits")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract().as(new TypeRef<>() {
                });

        assertThat(response).hasSize(0);
    }

    @Test
    @DisplayName("로그인한 사용자가 처음 재생기록 저장 시, 200 OK를 반환한다.")
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
                                .description("사용자의 재생 기록을 생성하거나 업데이트합니다.")
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
                                .description("사용자의 재생 기록을 생성하거나 업데이트합니다.")
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
    @DisplayName("로그인하지 않은 사용자는 재생기록 저장를 저장하지 않고, 200 OK를 반환한다.")
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
                                .description("사용자의 재생 기록을 생성하거나 업데이트합니다.")
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
