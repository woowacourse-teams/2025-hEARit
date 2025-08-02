package com.onair.hearit.presentation;

import com.onair.hearit.application.CategoryService;
import com.onair.hearit.dto.request.PagingRequest;
import com.onair.hearit.dto.response.CategoryResponse;
import com.onair.hearit.dto.response.HearitOfCategoryResponse;
import com.onair.hearit.dto.response.PagedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/categories")
@Tag(name = "Category", description = "카테고리")
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "전체 카테고리 목록을 조회", description = "전체 카테고리 목록들을 조회합니다.")
    @GetMapping
    public ResponseEntity<PagedResponse<CategoryResponse>> readCategories(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        PagingRequest pagingRequest = new PagingRequest(page, size);
        PagedResponse<CategoryResponse> responses = categoryService.getCategories(pagingRequest);
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "카테고리 id로 히어릿 카테고리로 조회", description = "히어릿의 카테고리 id, page 정보를 입력해 히어릿을 조회합니다. ")
    @GetMapping("/{categoryId}/hearits")
    public ResponseEntity<PagedResponse<HearitOfCategoryResponse>> searchHearitsByCategory(
            @PathVariable Long categoryId,
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "size", defaultValue = "20") Integer size) {
        PagingRequest pagingRequest = new PagingRequest(page, size);
        PagedResponse<HearitOfCategoryResponse> response = categoryService.getHearitsOfCategory(categoryId,
                pagingRequest);
        return ResponseEntity.ok(response);
    }
}
