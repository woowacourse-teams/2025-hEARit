package com.onair.hearit.app.advertisement.presentation;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.onair.hearit.app.advertisement.application.AdvertisementService;
import com.onair.hearit.app.exception.ApiGlobalExceptionHandler;
import com.onair.hearit.app.exception.custom.NotFoundException;
import com.onair.hearit.core.domain.Advertisement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class AdvertisementControllerTest {

    @Mock
    private AdvertisementService advertisementService;

    @InjectMocks
    private AdvertisementController advertisementController;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(advertisementController)
                .setControllerAdvice(new ApiGlobalExceptionHandler())
                .build();
    }

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
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.imageUrl").value("https://example.com/image.jpg"))
                    .andExpect(jsonPath("$.linkUrl").value("https://example.com/link"))
                    .andExpect(jsonPath("$.title").value("광고 제목"));
        }

        @Test
        @DisplayName("등록된 광고가 없으면 404 NOT FOUND를 반환한다.")
        void getRandomAdvertisement_notFound() throws Exception {
            // given
            given(advertisementService.getRandomAdvertisement())
                    .willThrow(new NotFoundException("등록된 광고가 없습니다."));

            // when & then
            mockMvc.perform(get("/api/v1/advertisements/random"))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.title").value("해당 정보를 찾을 수 없습니다."))
                    .andExpect(jsonPath("$.detail").value("등록된 광고가 없습니다."));
        }
    }
}
