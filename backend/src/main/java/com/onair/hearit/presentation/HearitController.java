package com.onair.hearit.presentation;

import com.onair.hearit.application.HearitService;
import com.onair.hearit.dto.response.HearitDetailResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/hearit")
public class HearitController {

    private final HearitService hearitService;

    @GetMapping("/{id}")
    public ResponseEntity<HearitDetailResponse> readHearit(@RequestParam(name = "id") Long hearitId) {
        HearitDetailResponse response = hearitService.getHearitDetail(hearitId);
        return ResponseEntity.ok(response);
    }
}
