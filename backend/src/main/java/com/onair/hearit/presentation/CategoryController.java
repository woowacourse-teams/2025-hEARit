package com.onair.hearit.presentation;

import com.onair.hearit.application.CategoryService;
import com.onair.hearit.dto.request.CategoryListCondition;
import com.onair.hearit.dto.response.CategoryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/category")
@Tag(name = "Category", description = "카테고리")
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "전체 카테고리 목록 조회", description = "전체 카테고리 목록들을 조회합니다.")
    @GetMapping
    public ResponseEntity<List<CategoryResponse>> readCategories(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size
    ) {
        CategoryListCondition condition = new CategoryListCondition(page, size);
        List<CategoryResponse> responses = categoryService.getCategories(condition);
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "단일 카테고리 조회", description = "단일 카테고리를 조회합니다.")
    @GetMapping("/{categoryId}")
    public ResponseEntity<CategoryResponse> readCategory(@PathVariable Long categoryId) {
        CategoryResponse response = categoryService.getCategory(categoryId);
        return ResponseEntity.ok(response);
    }
}
