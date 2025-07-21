package com.onair.hearit.presentation;

import com.onair.hearit.application.HearitSearchService;
import com.onair.hearit.application.HearitService;
import com.onair.hearit.dto.request.TitleSearchRequest;
import com.onair.hearit.dto.response.HearitDetailResponse;
import com.onair.hearit.dto.response.HearitSearchResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/hearits")
@Tag(name = "Hearit", description = "히어릿")
public class HearitController {

    private final HearitService hearitService;
    private final HearitSearchService hearitSearchService;

    @Operation(summary = "단일 히어릿 상세 조회", description = "히어릿ID룰 통해 하나의 히어릿 상세 정보를 조회합니다.")
    @GetMapping("/{hearitId}")
    public ResponseEntity<HearitDetailResponse> readHearit(@PathVariable Long hearitId) {
        HearitDetailResponse response = hearitService.getHearitDetail(hearitId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "랜덤 히어릿 10개 조회", description = "랜덤 히어릿을 5개 조회합니다.")
    @GetMapping("/random")
    public ResponseEntity<List<HearitDetailResponse>> readRandomHearits() {
        List<HearitDetailResponse> responses = hearitService.getRandomHearits();
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "추천 히어릿 5개 조회", description = "추천 히어릿을 5개 조회합니다.")
    @GetMapping("/recommend")
    public ResponseEntity<List<HearitDetailResponse>> readRecommendedHearits() {
        List<HearitDetailResponse> responses = hearitService.getRecommendedHearits();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/search")
    public ResponseEntity<List<HearitSearchResponse>> searchHearitsByTitle(
            @Valid @ModelAttribute TitleSearchRequest condition) {
        List<HearitSearchResponse> response = hearitSearchService.searchByTitle(condition);
        return ResponseEntity.ok(response);
    }
}
