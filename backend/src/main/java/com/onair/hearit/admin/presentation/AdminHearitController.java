package com.onair.hearit.admin.presentation;

import com.onair.hearit.admin.application.AdminAuthService;
import com.onair.hearit.admin.application.AdminHearitService;
import com.onair.hearit.admin.dto.request.AdminLoginRequest;
import com.onair.hearit.admin.dto.request.HearitCreateRequest;
import com.onair.hearit.admin.dto.request.HearitUpdateRequest;
import com.onair.hearit.admin.dto.response.HearitAdminResponse;
import com.onair.hearit.dto.request.PagingRequest;
import com.onair.hearit.dto.response.PagedResponse;
import io.swagger.v3.oas.annotations.Hidden;
import java.net.URI;
import lombok.RequiredArgsConstructor;
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
@RequestMapping("/api/v1/admin")
public class AdminHearitController {

    private final AdminAuthService adminAuthService;
    private final AdminHearitService adminHearitService;

    @PostMapping("/login")
    public void login(@RequestBody AdminLoginRequest request) {
        adminAuthService.login(request);
    }

    @GetMapping("/hearits")
    public ResponseEntity<PagedResponse<HearitAdminResponse>> readHearits(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {
        PagingRequest pagingRequest = new PagingRequest(page, size);
        PagedResponse<HearitAdminResponse> response = adminHearitService.getHearits(pagingRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/hearits")
    public ResponseEntity<Void> createHearit(@RequestBody HearitCreateRequest request) {
        adminHearitService.addHearit(request);
        return ResponseEntity.created(URI.create("/")).build();
    }

    @PutMapping("/hearits/{hearitId}")
    public ResponseEntity<Void> updateHearitById(
            @PathVariable Long hearitId,
            @RequestBody HearitUpdateRequest request) {
        adminHearitService.modifyHearit(hearitId, request);
        return ResponseEntity.noContent().build();
    }
}
