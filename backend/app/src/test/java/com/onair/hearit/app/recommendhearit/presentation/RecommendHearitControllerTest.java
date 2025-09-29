package com.onair.hearit.app.recommendhearit.presentation;

import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static com.epages.restdocs.apispec.RestAssuredRestDocumentationWrapper.document;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.Schema;
import com.onair.hearit.core.fixture.TestFixture;
import com.onair.hearit.core.domain.Category;
import com.onair.hearit.core.domain.Hearit;
import com.onair.hearit.core.domain.RecommendHearit;
import com.onair.hearit.app.fixture.IntegrationTest;
import com.onair.hearit.app.recommendhearit.dto.RecommendHearitResponse;
import io.restassured.RestAssured;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class RecommendHearitControllerTest extends IntegrationTest {

    @Test
    @DisplayName("오늘의 추천 히어릿을 조회 시, 200 OK 및 5개 히어릿 정보 목록을 제공한다.")
    void readRecommendedHearits() {
        // given
        LocalDate today = LocalDate.now();
        Category category = dbHelper.insertCategory(TestFixture.createFixedCategory());
        for (int i = 0; i < 5; i++) {
            Hearit hearit = dbHelper.insertHearit(TestFixture.createFixedHearitWith(category));
            dbHelper.insertRecommendHearit(new RecommendHearit(hearit, today));
        }

        // when
        List<RecommendHearitResponse> responses = RestAssured.given(this.spec)
                .filter(document("hearit-read-recommend",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Hearit API")
                                .summary("추천 히어릿 목록 조회")
                                .description("추천 히어릿 목록을 최대 5개까지 조회합니다.")
                                .responseSchema(Schema.schema("RecommendHearitResponseList"))
                                .responseFields(
                                        fieldWithPath("[].id").description("히어릿 ID"),
                                        fieldWithPath("[].title").description("히어릿 제목"),
                                        fieldWithPath("[].playTime").description("재생 시간(초)"),
                                        fieldWithPath("[].createdAt").description("생성 일시"),
                                        fieldWithPath("[].categoryName").description("카테고리 이름"),
                                        fieldWithPath("[].categoryColor").description("카테고리 색상 코드")
                                )
                                .build())
                ))
                .when()
                .get("/api/v1/hearits/recommend")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .jsonPath()
                .getList(".", RecommendHearitResponse.class);

        // then
        assertThat(responses).hasSize(5);
    }
}
