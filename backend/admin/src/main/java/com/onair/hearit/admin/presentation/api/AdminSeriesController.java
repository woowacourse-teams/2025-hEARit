package com.onair.hearit.admin.presentation.api;

import com.onair.hearit.admin.application.AdminSeriesService;
import com.onair.hearit.admin.dto.request.SeriesCreateRequest;
import com.onair.hearit.admin.dto.response.UploadUrlResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/series")
public class AdminSeriesController {

    private final AdminSeriesService adminSeriesService;

    @PostMapping("/upload-url")
    public ResponseEntity<UploadUrlResponse> createSeriesImageUploadUrl() {
        UploadUrlResponse response = adminSeriesService.getSeriesImageUploadUrl();
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<Void> createSeries(@RequestBody @Valid SeriesCreateRequest request) {
        adminSeriesService.addSeries(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
