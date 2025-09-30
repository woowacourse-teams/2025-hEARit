package com.onair.hearit.app.recommendhearit.presentation;

import com.onair.hearit.app.recommendhearit.application.RecommendHearitService;
import com.onair.hearit.app.recommendhearit.dto.RecommendHearitResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class RecommendHearitController {

    private final RecommendHearitService recommendHearitService;

    @GetMapping("/api/v1/hearits/recommend")
    public ResponseEntity<List<RecommendHearitResponse>> readRecommendedHearits() {
        List<RecommendHearitResponse> responses = recommendHearitService.getRecommendedHearits();
        return ResponseEntity.ok(responses);
    }
}
