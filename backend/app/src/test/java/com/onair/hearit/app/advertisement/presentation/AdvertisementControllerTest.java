package com.onair.hearit.app.advertisement.presentation;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.onair.hearit.app.advertisement.application.AdvertisementService;
import com.onair.hearit.app.exception.custom.NotFoundException;
import com.onair.hearit.app.fixture.ControllerTest;
import com.onair.hearit.core.domain.Advertisement;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@WebMvcTest(controllers = AdvertisementController.class)
class AdvertisementControllerTest extends ControllerTest {

    @MockitoBean
    private AdvertisementService advertisementService;

    @Nested
    @DisplayName("GET /api/v1/advertisements/random - 랜덤 광고 조회")
    class GetRandomAdvertisement {

        @Test
        @DisplayName("랜덤 광고 조회에 성공하면 200 OK와 광고 정보를 반환한다.")
        void getRandomAdvertisement_success() throws Exception {
            // given
            Advertisement advertisement = new Advertisement(
                    "https://example.com/image.jpg",
                    "https://example.com/link",
                    "광고 제목"
            );
            given(advertisementService.getRandomAdvertisement()).willReturn(advertisement);

            // when & then
            mockMvc.perform(get("/api/v1/advertisements/random"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.imageUrl").value("https://example.com/image.jpg"))
                    .andExpect(jsonPath("$.linkUrl").value("https://example.com/link"))
                    .andExpect(jsonPath("$.title").value("광고 제목"))
                    .andDo(document("get-random-advertisement-ok",
                            resource(ResourceSnippetParameters.builder()
                                    .tag("Advertisement API")
                                    .summary("랜덤 광고 조회")
                                    .description("등록된 광고 중 랜덤으로 하나를 조회합니다.")
                                    .responseFields(
                                            fieldWithPath("imageUrl").description("광고 이미지 URL"),
                                            fieldWithPath("linkUrl").description("광고 링크 URL"),
                                            fieldWithPath("title").description("광고 제목")
                                    )
                                    .build()
                            )));
        }

        @Test
        @DisplayName("등록된 광고가 없으면 404 NOT FOUND를 반환한다.")
        void getRandomAdvertisement_notFound() throws Exception {
            // given
            given(advertisementService.getRandomAdvertisement())
                    .willThrow(new NotFoundException("advertisement", "전체"));

            // when & then
            mockMvc.perform(get("/api/v1/advertisements/random"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.title").value("해당 정보를 찾을 수 없습니다."))
                    .andExpect(jsonPath("$.detail").value("advertisement을(를) 찾을 수 없습니다. 입력값: 전체"))
                    .andDo(document("get-random-advertisement-not-found",
                            resource(ResourceSnippetParameters.builder()
                                    .tag("Advertisement API")
                                    .summary("랜덤 광고 조회 - 광고 없음")
                                    .description("등록된 광고가 없는 경우 404 NOT FOUND를 반환합니다.")
                                    .responseFields(
                                            fieldWithPath("title").description("에러 제목"),
                                            fieldWithPath("detail").description("에러 상세 메시지")
                                    )
                                    .build()
                            )));
        }
    }
}
