package com.onair.hearit.presentation;

import com.onair.hearit.application.HearitService;
import com.onair.hearit.dto.response.HearitDetailResponse;
import com.onair.hearit.dto.response.HearitPersonalDetailResponse;
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

    //TODO 실제 멤버로 변경
    @GetMapping("/{hearitId}")
    public ResponseEntity<HearitPersonalDetailResponse> readHearit(@PathVariable Long hearitId, Long memberId) {
        HearitPersonalDetailResponse response = hearitService.getHearitPersonalDetail(hearitId, memberId);
        return ResponseEntity.ok(response);
    }
}
