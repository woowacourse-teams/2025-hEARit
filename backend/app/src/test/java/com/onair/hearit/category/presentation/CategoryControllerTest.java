package com.onair.hearit.category.presentation;

import static com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.onair.hearit.category.application.CategoryService;
import com.onair.hearit.category.dto.CategoryResponse;
import com.onair.hearit.common.dto.response.PagedResponse;
import com.onair.hearit.fixture.ApiDocSnippets;
import com.onair.hearit.fixture.ControllerTest;
import java.util.Arrays;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@WebMvcTest(controllers = CategoryController.class)
class CategoryControllerTest extends ControllerTest {

    @MockitoBean
    private CategoryService categoryService;

    @Test
    @DisplayName("카테고리 목록 조회 V1 - 200 OK")
    void readCategoriesV1_OK() throws Exception {
        // given
        var responses = IntStream.range(1, 25).mapToObj(i -> new CategoryResponse(
                        (long) i,
                        "category" + i,
                        "#" + i * 111111
                ))
                .toList();
        var pagedResponses = PagedResponse.from(new PageImpl<>(responses, PageRequest.of(0, 20), responses.size()));

        given(categoryService.getCategories(any())).willReturn(pagedResponses);

        // when & then
        mockMvc.perform(get("/api/v1/categories")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andDo(MockMvcRestDocumentationWrapper.document("v1-get-categories-ok",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Category API")
                                .summary("카테고리 목록 조회 V1")
                                .description("전체 카테고리 목록을 `Page` 단위로 조회합니다.")
                                .queryParameters(
                                        parameterWithName("page").description("페이지 번호 (start 0)").defaultValue("0"),
                                        parameterWithName("size").description("페이지 당 항목 수").defaultValue("20")
                                )
                                .responseFields(Stream.concat(
                                                Arrays.stream(new FieldDescriptor[]{
                                                        fieldWithPath("content[].id").description("카테고리 ID"),
                                                        fieldWithPath("content[].name").description("카테고리 이름"),
                                                        fieldWithPath("content[].colorCode").description("카테고리 색상 코드")
                                                }),
                                                Arrays.stream(ApiDocSnippets.getCustomPagedResponseFields())
                                        ).toArray(FieldDescriptor[]::new)
                                )
                                .build())
                ));
    }

    @Test
    @DisplayName("카테고리 목록 조회 V1 - 400 Bad Request")
    void readCategoriesV1_BadRequest() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/categories")
                        .param("page", "-1")
                        .param("size", "20"))
                .andExpect(status().isBadRequest())
                .andDo(MockMvcRestDocumentationWrapper.document("v1-get-categories-bad-request",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Category API")
                                .summary("카테고리 목록 조회 V1")
                                .responseFields(ApiDocSnippets.getProblemDetailResponseFields())
                                .build())
                ));
    }
}
