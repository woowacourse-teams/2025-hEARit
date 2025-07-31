package com.onair.hearit.utils;

import static com.epages.restdocs.apispec.ResourceDocumentation.headerWithName;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;

import com.epages.restdocs.apispec.HeaderDescriptorWithType;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;

public class ApiDocumentUtils {

    public static HeaderDescriptorWithType[] getAuthorizationHeader() {
        return new HeaderDescriptorWithType[]{
                headerWithName("Authorization").description("Bearer 인증 토큰")
        };
    }

    public static FieldDescriptor[] getCustomPagedResponseFields() {
        return new FieldDescriptor[]{
                fieldWithPath("content").description("페이지의 실제 컨텐츠 목록"),
                fieldWithPath("page").type(JsonFieldType.NUMBER).description("현재 페이지 번호 (0부터 시작)"),
                fieldWithPath("size").type(JsonFieldType.NUMBER).description("페이지 당 항목 수"),
                fieldWithPath("totalPages").type(JsonFieldType.NUMBER).description("전체 페이지 수"),
                fieldWithPath("totalElements").type(JsonFieldType.NUMBER).description("전체 항목(요소) 수"),
                fieldWithPath("isFirst").type(JsonFieldType.BOOLEAN).description("첫 페이지 여부"),
                fieldWithPath("isLast").type(JsonFieldType.BOOLEAN).description("마지막 페이지 여부")
        };
    }
}
