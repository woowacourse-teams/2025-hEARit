package com.onair.hearit.presentation;

import com.onair.hearit.application.HearitService;
import com.onair.hearit.dto.response.HearitDetailResponse;
import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.onair.hearit.auth.dto.CurrentMember;
import com.onair.hearit.dto.response.HearitPersonalDetailResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/hearits")
@Tag(name = "Hearit", description = "히어릿")
public class HearitController {

    private final HearitService hearitService;

    @Operation(summary = "단일 히어릿 상세 조회", description = "히어릿ID룰 통해 하나의 히어릿 상세 정보를 조회합니다.")
    @GetMapping("/{hearitId}")
    public ResponseEntity<HearitPersonalDetailResponse> readHearit(@PathVariable Long hearitId,
                                                                   @AuthenticationPrincipal CurrentMember member) {
        HearitPersonalDetailResponse response = hearitService.getHearitPersonalDetail(hearitId, member.memberId());
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
}
