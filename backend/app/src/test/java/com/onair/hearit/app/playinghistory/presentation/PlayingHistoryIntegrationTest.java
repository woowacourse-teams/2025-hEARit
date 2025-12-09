package com.onair.hearit.app.playinghistory.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.onair.hearit.app.auth.infrastructure.jwt.JwtTokenProvider;
import com.onair.hearit.app.fixture.IntegrationTest;
import com.onair.hearit.app.playinghistory.dto.PlayingHistoryRequest;
import com.onair.hearit.app.playinghistory.dto.RecentlyPlayedHearitResponse;
import com.onair.hearit.app.playinghistory.infrastructure.scheduler.PlayingHistoryBuffer;
import com.onair.hearit.core.domain.Category;
import com.onair.hearit.core.domain.Hearit;
import com.onair.hearit.core.domain.Member;
import com.onair.hearit.core.domain.PlayingHistory;
import com.onair.hearit.core.fixture.TestFixture;
import com.onair.hearit.core.infrastructure.jpa.PlayingHistoryRepository;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import java.util.List;
import java.util.UUID;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

class PlayingHistoryIntegrationTest extends IntegrationTest {

    @Autowired
    JwtTokenProvider jwtTokenProvider;

    @Autowired
    PlayingHistoryRepository playingHistoryRepository;

    @Autowired
    PlayingHistoryBuffer playingHistoryBuffer;

    @Nested
    @DisplayName("유저들은 최근 재생기록을 최대 10개 조회할 수 있다.")
    class GetRecentPlayingHistories {

        @Test
        @DisplayName("로그인한 사용자는 최근 재생 기록 최대 10개와 200 OK를 반환한다.")
        void getRecentPlayingHistories_whenMember() {
            // given
            Member member = dbHelper.insertMember(TestFixture.createFixedMember());
            String token = generateToken(member);
            Category category = dbHelper.insertCategory(new Category("name", "#000000"));

            for (int i = 0; i < 12; i++) {
                Hearit hearit = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
                dbHelper.insertPlayingHistory(new PlayingHistory(member.getUuid(), hearit, 10L * i));
            }

            // when & then
            List<RecentlyPlayedHearitResponse> response = RestAssured.given(PlayingHistoryIntegrationTest.this.spec)
                    .header("Authorization", "Bearer " + token)
                    .contentType("application/json")
                    .when()
                    .get("/api/v1/playing-histories/hearits")
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .extract().as(new TypeRef<>() {
                    });

            SoftAssertions softly = new SoftAssertions();
            softly.assertThat(response)
                    .hasSize(10);
            List<Long> times = response.stream()
                    .map(RecentlyPlayedHearitResponse::lastPlayTime)
                    .toList();
            softly.assertThat(times)
                    .as("lastPlayTime 내림차순 정렬 검증")
                    .isSortedAccordingTo((a, b) -> Long.compare(b, a));
            softly.assertAll();
        }

        @Test
        @DisplayName("비회원은 최근 재생 기록 최대 10개와 200 OK를 반환한다.")
        void getRecentPlayingHistories_whenGuest() {
            // given
            String guestUuid = UUID.randomUUID().toString();
            Category category = dbHelper.insertCategory(new Category("name", "#000000"));

            for (int i = 0; i < 12; i++) {
                Hearit hearit = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
                dbHelper.insertPlayingHistory(new PlayingHistory(guestUuid, hearit, 10L * i));
            }

            // when & then
            List<RecentlyPlayedHearitResponse> response = RestAssured.given(PlayingHistoryIntegrationTest.this.spec)
                    .contentType("application/json")
                    .header("Device-UUID", guestUuid)
                    .when()
                    .get("/api/v1/playing-histories/hearits")
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .extract().as(new TypeRef<>() {
                    });

            SoftAssertions softly = new SoftAssertions();
            softly.assertThat(response)
                    .hasSize(10);
            List<Long> times = response.stream()
                    .map(RecentlyPlayedHearitResponse::lastPlayTime)
                    .toList();
            softly.assertThat(times)
                    .as("lastPlayTime 내림차순 정렬 검증")
                    .isSortedAccordingTo((a, b) -> Long.compare(b, a));
            softly.assertAll();
        }
    }

    @Nested
    @DisplayName("재생기록 저장 테스트")
    class CreatePlayingHistory {

        @Test
        @DisplayName("로그인한 사용자가 재생기록 저장 시, 200 OK를 반환한다.")
        void createPlayingHistory_member() {
            // given
            Member member = dbHelper.insertMember(TestFixture.createFixedMember());
            String token = generateToken(member);
            Category category = dbHelper.insertCategory(new Category("name", "#000000"));
            Hearit hearit = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));

            PlayingHistoryRequest request = new PlayingHistoryRequest(hearit.getId(), 100L);

            // when
            RestAssured.given(PlayingHistoryIntegrationTest.this.spec)
                    .header("Authorization", "Bearer " + token)
                    .contentType("application/json")
                    .body(request)
                    .when()
                    .post("/api/v1/playing-histories")
                    .then()
                    .statusCode(HttpStatus.OK.value());
            playingHistoryBuffer.flush();

            // then
            List<PlayingHistory> histories = playingHistoryRepository.findAll();
            assertAll(
                    () -> assertThat(histories).hasSize(1),
                    () -> assertThat(histories.getFirst().getUserUuid()).isEqualTo(member.getUuid()),
                    () -> assertThat(histories.getFirst().getHearitId()).isEqualTo(hearit.getId()),
                    () -> assertThat(histories.getFirst().getLastPlayTime()).isEqualTo(100L)
            );
        }

        @Test
        @DisplayName("로그인하지 않은 사용자가 재생기록 저장 시, 200 OK를 반환한다.")
        void createPlayingHistory_guest() {
            // given
            String guestUuid = UUID.randomUUID().toString();
            Category category = dbHelper.insertCategory(new Category("name", "#000000"));
            Hearit hearit = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));

            PlayingHistoryRequest request = new PlayingHistoryRequest(hearit.getId(), 100L);

            // when
            RestAssured.given(PlayingHistoryIntegrationTest.this.spec)
                    .contentType("application/json")
                    .header("Device-UUID", guestUuid)
                    .body(request)
                    .when()
                    .post("/api/v1/playing-histories")
                    .then()
                    .statusCode(HttpStatus.OK.value());
            playingHistoryBuffer.flush();

            // then
            List<PlayingHistory> histories = playingHistoryRepository.findAll();
            assertAll(
                    () -> assertThat(histories).hasSize(1),
                    () -> assertThat(histories.getFirst().getUserUuid()).isEqualTo(guestUuid),
                    () -> assertThat(histories.getFirst().getHearitId()).isEqualTo(hearit.getId()),
                    () -> assertThat(histories.getFirst().getLastPlayTime()).isEqualTo(100L)
            );
        }

        @Test
        @DisplayName("로그인한 사용자가 다시 재생기록 저장 시, 200 OK를 반환한다.")
        void updatePlayingHistory_member() {
            // given
            Member member = dbHelper.insertMember(TestFixture.createFixedMember());
            String token = generateToken(member);
            Category category = dbHelper.insertCategory(new Category("name", "#000000"));
            Hearit hearit = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
            dbHelper.insertPlayingHistory(new PlayingHistory(member.getUuid(), hearit, 20_000));

            PlayingHistoryRequest request = new PlayingHistoryRequest(hearit.getId(), 50_000L);

            // when
            RestAssured.given(PlayingHistoryIntegrationTest.this.spec)
                    .header("Authorization", "Bearer " + token)
                    .contentType("application/json")
                    .body(request)
                    .when()
                    .post("/api/v1/playing-histories")
                    .then()
                    .statusCode(HttpStatus.OK.value());
            playingHistoryBuffer.flush();

            // then
            List<PlayingHistory> histories = playingHistoryRepository.findAll();
            assertAll(
                    () -> assertThat(histories).hasSize(1),
                    () -> assertThat(histories.getFirst().getLastPlayTime()).isEqualTo(50_000L)
            );
        }

        @Test
        @DisplayName("비회원이 동일한 히어릿에 다시 재생기록 저장 시, 200 OK를 반환한다.")
        void updatePlayingHistory_guest() {
            // given
            String guestUuid = UUID.randomUUID().toString();
            Category category = dbHelper.insertCategory(new Category("name", "#000000"));
            Hearit hearit = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
            dbHelper.insertPlayingHistory(new PlayingHistory(guestUuid, hearit, 20_000));

            PlayingHistoryRequest request = new PlayingHistoryRequest(hearit.getId(), 100L);

            // when
            RestAssured.given(PlayingHistoryIntegrationTest.this.spec)
                    .contentType("application/json")
                    .header("Device-UUID", guestUuid)
                    .body(request)
                    .when()
                    .post("/api/v1/playing-histories")
                    .then()
                    .statusCode(HttpStatus.OK.value());
            playingHistoryBuffer.flush();

            // then
            List<PlayingHistory> histories = playingHistoryRepository.findAll();
            assertAll(
                    () -> assertThat(histories).hasSize(1),
                    () -> assertThat(histories.getFirst().getLastPlayTime()).isEqualTo(100L)
            );
        }
    }

    private String generateToken(Member member) {
        return jwtTokenProvider.createAccessToken(member.getId());
    }
}
