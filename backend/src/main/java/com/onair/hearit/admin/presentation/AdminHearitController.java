package com.onair.hearit.admin.presentation;

import com.onair.hearit.admin.application.AdminAuthService;
import com.onair.hearit.admin.application.AdminHearitService;
import com.onair.hearit.admin.dto.request.AdminLoginRequest;
import com.onair.hearit.admin.dto.request.HearitUploadRequest;
import com.onair.hearit.admin.dto.response.HearitAdminResponse;
import com.onair.hearit.admin.dto.response.PagedResponse;
import com.onair.hearit.dto.response.HearitDetailResponse;
import java.net.URI;
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
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
public class AdminHearitController {

    private final AdminAuthService adminAuthService;
    private final AdminHearitService adminHearitService;

    @PostMapping("/login")
    public void login(@RequestBody AdminLoginRequest request) {
        adminAuthService.login(request);
    }

    @GetMapping("/hearits")
    public ResponseEntity<PagedResponse<HearitAdminResponse>> getPageHearits(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {
        PagedResponse<HearitAdminResponse> response = adminHearitService.getPageHearits(page, size);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/hearits")
    public ResponseEntity<HearitDetailResponse> createHearit(@RequestBody HearitUploadRequest request) {
        HearitDetailResponse response = adminHearitService.uploadHearit(request);
        return ResponseEntity.created(URI.create("/hearits/" + response.id())).body(response);
    }

    @DeleteMapping("/hearits/{hearitId}")
    public ResponseEntity<Void> deleteHearitById(@PathVariable Long hearitId) {
        adminHearitService.deleteHearitById(hearitId);
        return ResponseEntity.noContent().build();
    }
}
