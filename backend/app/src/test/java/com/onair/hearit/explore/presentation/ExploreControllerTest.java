package com.onair.hearit.explore.presentation;

import static com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static com.epages.restdocs.apispec.RestAssuredRestDocumentationWrapper.document;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.Schema;
import com.onair.hearit.auth.infrastructure.jwt.JwtTokenProvider;
import com.onair.hearit.common.dto.response.CursorResponseV1;
import com.onair.hearit.common.dto.response.CursorResponseV2;
import com.onair.hearit.core.docs.ApiDocSnippets;
import com.onair.hearit.core.fixture.TestFixture;
import com.onair.hearit.domain.Category;
import com.onair.hearit.domain.Hearit;
import com.onair.hearit.domain.HearitKeyword;
import com.onair.hearit.domain.Keyword;
import com.onair.hearit.domain.Member;
import com.onair.hearit.explore.dto.ExploredHearitResponse;
import com.onair.hearit.fixture.IntegrationTest;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import java.util.Arrays;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.restdocs.payload.FieldDescriptor;

class ExploreControllerTest extends IntegrationTest {

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
                .filter(document("hearit-read-explore-member-v2",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Hearit API")
                                .summary("탐색 히어릿 목록 조회 V2")
                                .description("사용자 별 최대 10개의 히어릿 목록을 조회합니다.")
                                .queryParameters(
                                        parameterWithName("cursorId").description("시작 Cursor ID").defaultValue("0"),
                                        parameterWithName("size").description("필요한 히어릿 항목 수").defaultValue("10")
                                )
                                .responseSchema(Schema.schema("CursorExploredHearitResponse"))
                                .responseFields(
                                        Stream.concat(
                                                Arrays.stream(new FieldDescriptor[]{
                                                        fieldWithPath("content[].id").description("히어릿 ID"),
                                                        fieldWithPath("content[].title").description("히어릿 제목"),
                                                        fieldWithPath("content[].categoryColorCode").description(
                                                                "카테고리 색상"),
                                                        fieldWithPath("content[].isBookmarked").description("북마크 여부"),
                                                        fieldWithPath("content[].bookmarkId").description(
                                                                "북마크 ID (북마크된 경우)").optional(),
                                                        fieldWithPath("content[].keywords").description(
                                                                "히어릿에 포함된 키워드 목록"),
                                                        fieldWithPath("content[].keywords[].id").description("키워드 ID"),
                                                        fieldWithPath("content[].keywords[].name").description(
                                                                "키워드 이름"),
                                                        fieldWithPath("content[].cursorId").description("커서 ID"),
                                                }),
                                                Arrays.stream(ApiDocSnippets.getCustomCursorResponseFields())
                                        ).toArray(FieldDescriptor[]::new)
                                )
                                .build())
                ))
                .when()
                .get("/api/v2/hearits/explore")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(new TypeRef<>() {
                });

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
                .filter(document("hearit-read-explore-member-v1",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Hearit API")
                                .summary("탐색 히어릿 목록 조회 V1")
                                .description("사용자 별 최대 10개의 히어릿 목록을 조회합니다.")
                                .queryParameters(
                                        parameterWithName("cursorId").description("시작 Cursor ID").defaultValue("0"),
                                        parameterWithName("size").description("필요한 히어릿 항목 수").defaultValue("10")
                                )
                                .responseSchema(Schema.schema("CursorExploredHearitResponse"))
                                .responseFields(
                                        Stream.concat(
                                                Arrays.stream(new FieldDescriptor[]{
                                                        fieldWithPath("content[].id").description("히어릿 ID"),
                                                        fieldWithPath("content[].title").description("히어릿 제목"),
                                                        fieldWithPath("content[].categoryColorCode").description(
                                                                "카테고리 색상"),
                                                        fieldWithPath("content[].isBookmarked").description("북마크 여부"),
                                                        fieldWithPath("content[].bookmarkId").description(
                                                                "북마크 ID (북마크된 경우)").optional(),
                                                        fieldWithPath("content[].keywords").description(
                                                                "히어릿에 포함된 키워드 목록"),
                                                        fieldWithPath("content[].keywords[].id").description("키워드 ID"),
                                                        fieldWithPath("content[].keywords[].name").description(
                                                                "키워드 이름"),
                                                        fieldWithPath("content[].cursorId").description("커서 ID"),
                                                }),
                                                Arrays.stream(ApiDocSnippets.getCustomCursorResponseV1Fields())
                                        ).toArray(FieldDescriptor[]::new)
                                )
                                .build())
                ))
                .when()
                .get("/api/v1/hearits/explore")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(new TypeRef<>() {
                });

        // then
        assertAll(() -> {
            assertThat(responses.content()).hasSize(3);
            assertThat(responses.cursorId()).isEqualTo(3L);
            assertThat(responses.isEmpty()).isFalse();
        });
    }

    private String generateToken(Member member) {
        return jwtTokenProvider.createAccessToken(member.getId());
    }
}
