package com.onair.hearit.admin.presentation;

import com.onair.hearit.admin.application.AdminAuthService;
import com.onair.hearit.admin.application.AdminHearitService;
import com.onair.hearit.admin.dto.AdminLoginRequest;
import com.onair.hearit.admin.dto.HearitUploadRequest;
import com.onair.hearit.admin.dto.KeywordInfoResponse;
import com.onair.hearit.dto.response.CategoryInfoResponse;
import com.onair.hearit.dto.response.HearitDetailResponse;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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

    @PostMapping("/hearits")
    public ResponseEntity<HearitDetailResponse> addHearit(@RequestBody HearitUploadRequest request) {
        HearitDetailResponse response = adminHearitService.uploadHearit(request);
        return ResponseEntity.created(URI.create("/hearits/" + response.id())).body(response);
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
