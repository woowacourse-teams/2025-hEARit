package com.onair.hearit.app.explore.presentation;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.onair.hearit.app.auth.infrastructure.jwt.JwtTokenProvider;
import com.onair.hearit.app.explore.dto.CursorResponseV1;
import com.onair.hearit.app.explore.dto.CursorResponseV2;
import com.onair.hearit.app.explore.dto.ExploredHearitResponse;
import com.onair.hearit.app.fixture.IntegrationTest;
import com.onair.hearit.core.domain.Category;
import com.onair.hearit.core.domain.Hearit;
import com.onair.hearit.core.domain.HearitKeyword;
import com.onair.hearit.core.domain.Keyword;
import com.onair.hearit.core.domain.Member;
import com.onair.hearit.core.fixture.TestFixture;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

class ExploreIntegrationTest extends IntegrationTest {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("탐색 히어릿을 조회 시, 200 OK 및 최대 10개 히어릿 정보 목록을 제공한다.")
    void readExploredHearits_byMember_v2() {
        // given
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Keyword keyword = dbHelper.insertKeyword(new Keyword("Keyword"));

        Hearit hearit1 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        Hearit hearit2 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        Hearit hearit3 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        dbHelper.insertHearitKeyword(new HearitKeyword(hearit1, keyword));
        dbHelper.insertHearitKeyword(new HearitKeyword(hearit2, keyword));
        dbHelper.insertHearitKeyword(new HearitKeyword(hearit3, keyword));

        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
        String token = generateToken(member);

        // when
        CursorResponseV2<ExploredHearitResponse> responses = RestAssured.given(this.spec)
                .header("Authorization", "Bearer " + token)
                .queryParam("cursorId", 0)
                .queryParam("size", 10)
                .when()
                .get("/api/v2/hearits/explore")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(new TypeRef<>() {});

        // then
        assertAll(() -> {
            assertThat(responses.content()).hasSize(3);
            assertThat(responses.content()).extracting(ExploredHearitResponse::cursorId)
                    .containsExactly(1L, 2L, 3L);
            assertThat(responses.isEmpty()).isFalse();
        });
    }

    @Test
    @DisplayName("탐색 히어릿을 조회 시, 200 OK 및 최대 10개 히어릿 정보 목록을 제공한다.")
    void readExploredHearits_byMember_v1() {
        // given
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        Keyword keyword = dbHelper.insertKeyword(new Keyword("Keyword"));

        Hearit hearit1 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        Hearit hearit2 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        Hearit hearit3 = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
        dbHelper.insertHearitKeyword(new HearitKeyword(hearit1, keyword));
        dbHelper.insertHearitKeyword(new HearitKeyword(hearit2, keyword));
        dbHelper.insertHearitKeyword(new HearitKeyword(hearit3, keyword));

        Member member = dbHelper.insertMember(TestFixture.createFixedMember());
        String token = generateToken(member);

        // when
        CursorResponseV1<ExploredHearitResponse> responses = RestAssured.given(this.spec)
                .header("Authorization", "Bearer " + token)
                .queryParam("cursorId", 0)
                .queryParam("size", 10)
                .when()
                .get("/api/v1/hearits/explore")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(new TypeRef<>() {});

        // then
        assertAll(() -> {
            assertThat(responses.content()).hasSize(3);
            assertThat(responses.cursorId()).isEqualTo(3L);
            assertThat(responses.isEmpty()).isFalse();
        });
    }

    private String generateToken(Member member) {
        return jwtTokenProvider.createAccessToken(member.getUuid());
    }
}
