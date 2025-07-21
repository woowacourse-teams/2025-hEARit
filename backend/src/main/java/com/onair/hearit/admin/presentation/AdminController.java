package com.onair.hearit.admin.presentation;

import com.onair.hearit.admin.application.AdminAuthService;
import com.onair.hearit.admin.application.AdminHearitService;
import com.onair.hearit.admin.dto.AdminLoginRequest;
import com.onair.hearit.admin.dto.HearitUploadRequest;
import com.onair.hearit.admin.dto.KeywordInfoResponse;
import com.onair.hearit.admin.dto.PagedResponse;
import com.onair.hearit.dto.response.CategoryInfoResponse;
import com.onair.hearit.dto.response.HearitDetailResponse;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminAuthService adminAuthService;
    private final AdminHearitService adminHearitService;

    @PostMapping("/login")
    public void login(@RequestBody AdminLoginRequest request) {
        adminAuthService.login(request);
    }

    @GetMapping("/hearits")
    public ResponseEntity<PagedResponse<HearitDetailResponse>> getPageHearits(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {
        PagedResponse<HearitDetailResponse> response = adminHearitService.getPageHearits(page, size);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/hearits")
    public ResponseEntity<HearitDetailResponse> addHearit(@RequestBody HearitUploadRequest request) {
        HearitDetailResponse response = adminHearitService.uploadHearit(request);
        return ResponseEntity.created(URI.create("/hearits/" + response.id())).body(response);
    }

    @DeleteMapping("/hearits/{hearitId}")
    public ResponseEntity<Void> deleteHearitById(@PathVariable Long hearitId) {
        adminHearitService.deleteHearitById(hearitId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/categories")
    public ResponseEntity<List<CategoryInfoResponse>> getAllCategories() {
        List<CategoryInfoResponse> response = adminHearitService.getAllCategories();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/keywords")
    public ResponseEntity<List<KeywordInfoResponse>> getAllKeywords() {
        List<KeywordInfoResponse> response = adminHearitService.getAllKeywords();
        return ResponseEntity.ok(response);
    }
}
