package com.onair.hearit.admin.presentation.api;

import com.onair.hearit.admin.application.AdminHearitService;
import com.onair.hearit.admin.dto.request.AdminPagingRequest;
import com.onair.hearit.admin.dto.request.HearitInfoUpdateRequest;
import com.onair.hearit.admin.dto.request.HearitMetaDataRequest;
import com.onair.hearit.admin.dto.response.AdminHearitResponse;
import com.onair.hearit.admin.dto.response.AdminPagedResponse;
import jakarta.validation.Valid;
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

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/hearits")
public class AdminHearitController {

    private final AdminHearitService adminHearitService;

    @GetMapping
    public ResponseEntity<AdminPagedResponse<AdminHearitResponse>> readHearits(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {
        AdminPagingRequest pagingRequest = new AdminPagingRequest(page, size);
        AdminPagedResponse<AdminHearitResponse> response = adminHearitService.getHearits(pagingRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<Void> createHearit(@RequestBody @Valid HearitMetaDataRequest request) {
        adminHearitService.addHearitMetaData(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/{hearitId}")
    public ResponseEntity<Void> updateHearitById(
            @PathVariable Long hearitId,
            @RequestBody @Valid HearitInfoUpdateRequest request) {
        adminHearitService.modifyHearitMetaData(hearitId, request);
        return ResponseEntity.noContent().build();
    }
}
