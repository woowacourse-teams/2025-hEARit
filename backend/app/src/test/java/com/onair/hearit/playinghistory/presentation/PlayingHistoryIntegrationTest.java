package com.onair.hearit.playinghistory.presentation;

import static org.assertj.core.api.Assertions.assertThat;

import com.onair.hearit.auth.infrastructure.jwt.JwtTokenProvider;
import com.onair.hearit.core.fixture.TestFixture;
import com.onair.hearit.domain.Category;
import com.onair.hearit.domain.Hearit;
import com.onair.hearit.domain.Member;
import com.onair.hearit.domain.PlayingHistory;
import com.onair.hearit.domain.Source;
import com.onair.hearit.fixture.IntegrationTest;
import com.onair.hearit.playinghistory.dto.PlayingHistoryRequest;
import com.onair.hearit.playinghistory.dto.RecentlyPlayedHearitResponse;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

class PlayingHistoryIntegrationTest extends IntegrationTest {

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
