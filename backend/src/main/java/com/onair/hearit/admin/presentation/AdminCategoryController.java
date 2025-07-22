package com.onair.hearit.admin.presentation;

import com.onair.hearit.admin.application.AdminCategoryService;
import com.onair.hearit.admin.dto.request.CategoryCreateRequest;
import com.onair.hearit.admin.dto.response.CategoryInfoResponse;
import io.swagger.v3.oas.annotations.Hidden;
import com.onair.hearit.admin.dto.response.PagedResponse;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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
    public ResponseEntity<PagedResponse<CategoryInfoResponse>> getAllCategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size
    ) {
        PagedResponse<CategoryInfoResponse> response = adminCategoryService.getPageCategories(page, size);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<CategoryInfoResponse> createCategory(@RequestBody CategoryCreateRequest request) {
        CategoryInfoResponse response = adminCategoryService.addCategory(request);
        return ResponseEntity.created(URI.create("/")).body(response);
    }
}
