package com.onair.hearit.docs;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;

import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;

public class ApiDocSnippets {

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

    public static FieldDescriptor[] getCustomCursorResponseFields() {
        return new FieldDescriptor[]{
                fieldWithPath("content").description("페이지의 실제 컨텐츠 목록"),
                fieldWithPath("isEmpty").type(JsonFieldType.BOOLEAN).description("컨텐츠 존재 여부"),
        };
    }

    public static FieldDescriptor[] getCustomCursorResponseV1Fields() {
        return new FieldDescriptor[]{
                fieldWithPath("content").description("페이지의 실제 컨텐츠 목록"),
                fieldWithPath("isEmpty").type(JsonFieldType.BOOLEAN).description("컨텐츠 존재 여부"),
                fieldWithPath("cursorId").type(JsonFieldType.NUMBER).description("커서 ID"),
        };
    }

    public static FieldDescriptor[] getProblemDetailResponseFields() {
        return new FieldDescriptor[]{
                fieldWithPath("type").type(JsonFieldType.STRING).description("문제 유형을 식별하는 URI (요청 경로)"),
                fieldWithPath("title").type(JsonFieldType.STRING).description("문제 유형에 대한 요약 (에러 코드 제목)"),
                fieldWithPath("status").type(JsonFieldType.NUMBER).description("HTTP 상태 코드"),
                fieldWithPath("detail").type(JsonFieldType.STRING).description("문제 발생에 대한 상세 설명"),
                fieldWithPath("instance").type(JsonFieldType.STRING)
                        .description("문제의 특정 발생을 식별하는 URI (현재는 사용되지 않아 null)").optional(),
                fieldWithPath("properties").type(JsonFieldType.OBJECT)
                        .description("문제 유형에 대한 추가 세부 정보 (현재는 사용되지 않아 null)").optional()
        };
    }

    public static FieldDescriptor[] getProblemDetailResponseFieldsWithAuthProperties() {
        return new FieldDescriptor[]{
                fieldWithPath("type").type(JsonFieldType.STRING).description("문제 유형을 식별하는 URI (요청 경로)"),
                fieldWithPath("title").type(JsonFieldType.STRING).description("문제 유형에 대한 요약 (에러 코드 제목)"),
                fieldWithPath("status").type(JsonFieldType.NUMBER).description("HTTP 상태 코드"),
                fieldWithPath("detail").type(JsonFieldType.STRING).description("문제 발생에 대한 상세 설명"),
                fieldWithPath("instance").type(JsonFieldType.STRING)
                        .description("문제의 특정 발생을 식별하는 URI (현재는 사용되지 않아 null)").optional(),
                fieldWithPath("properties").type(JsonFieldType.OBJECT)
                        .description("문제 유형에 대한 추가 세부 정보").optional(),
                fieldWithPath("properties.code").type(JsonFieldType.STRING)
                        .description("에러 코드 (예: UNAUTHENTICATED)"),
                fieldWithPath("properties.reissuable").type(JsonFieldType.BOOLEAN)
                        .description("토큰 재발급 가능 여부")
        };
    }
}
