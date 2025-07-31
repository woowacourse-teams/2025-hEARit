package com.onair.hearit.presentation;

import com.onair.hearit.application.CategoryService;
import com.onair.hearit.dto.request.PagingRequest;
import com.onair.hearit.dto.response.CategoryResponse;
import com.onair.hearit.dto.response.HearitSearchResponse;
import com.onair.hearit.dto.response.PagedResponse;
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
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<PagedResponse<CategoryResponse>> readCategories(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        PagingRequest pagingRequest = new PagingRequest(page, size);
        PagedResponse<CategoryResponse> responses = categoryService.getCategories(pagingRequest);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{categoryId}/hearits")
    public ResponseEntity<PagedResponse<HearitSearchResponse>> searchHearitsByCategory(
            @PathVariable Long categoryId,
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "size", defaultValue = "20") Integer size) {
        PagingRequest pagingRequest = new PagingRequest(page, size);
        PagedResponse<HearitSearchResponse> response = categoryService.getHearitsByCategory(categoryId, pagingRequest);
        return ResponseEntity.ok(response);
    }
}
