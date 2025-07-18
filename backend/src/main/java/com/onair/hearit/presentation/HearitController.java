package com.onair.hearit.presentation;

import com.onair.hearit.application.HearitService;
import com.onair.hearit.dto.response.HearitDetailResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<HearitDetailResponse> readHearit(@PathVariable Long hearitId) {
        HearitDetailResponse response = hearitService.getHearitDetail(hearitId);
        return ResponseEntity.ok(response);
    }
}
