package com.onair.hearit.admin.presentation;

import com.onair.hearit.admin.application.AdminCategoryService;
import com.onair.hearit.admin.dto.request.CategoryCreateRequest;
import com.onair.hearit.admin.dto.request.CategoryUpdateRequest;
import com.onair.hearit.admin.dto.response.CategoryInfoResponse;
import com.onair.hearit.dto.request.PagingRequest;
import com.onair.hearit.dto.response.PagedResponse;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Hidden
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/categories")
public class AdminCategoryController {

    private final AdminCategoryService adminCategoryService;

    @GetMapping
    public ResponseEntity<PagedResponse<CategoryInfoResponse>> readCategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {
        PagingRequest pagingRequest = new PagingRequest(page, size);
        PagedResponse<CategoryInfoResponse> response = adminCategoryService.getCategories(pagingRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/all")
    public ResponseEntity<List<CategoryInfoResponse>> readAllCategories() {
        List<CategoryInfoResponse> response = adminCategoryService.getAllCategories();
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<Void> createCategory(@RequestBody @Valid CategoryCreateRequest request) {
        adminCategoryService.addCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/{categoryId}")
    public ResponseEntity<Void> updateCategory(
            @PathVariable Long categoryId,
            @RequestBody @Valid CategoryUpdateRequest request
    ) {
        adminCategoryService.updateCategory(categoryId, request);
        return ResponseEntity.noContent().build();
    }
}
