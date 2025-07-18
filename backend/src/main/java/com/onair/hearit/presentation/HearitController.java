package com.onair.hearit.presentation;

import com.onair.hearit.application.HearitService;
import com.onair.hearit.dto.response.HearitDetailResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/hearits")
public class HearitController {

    private final HearitService hearitService;

    @GetMapping("/{hearitId}")
    public ResponseEntity<HearitDetailResponse> readHearit(@PathVariable Long hearitId) {
        HearitDetailResponse response = hearitService.getHearitDetail(hearitId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/random")
    public ResponseEntity<List<HearitDetailResponse>> readRandomHearits() {
        List<HearitDetailResponse> responses = hearitService.getRandomHearits();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/recommend")
    public ResponseEntity<List<HearitDetailResponse>> readRecommendedHearits() {
        List<HearitDetailResponse> responses = hearitService.getRecommendedHearits();
        return ResponseEntity.ok(responses);
    }
}
