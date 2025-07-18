package com.onair.hearit.presentation;

import com.onair.hearit.application.HearitService;
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
public class HearitController {

    private final HearitService hearitService;

    @GetMapping("/{hearitId}")
    public ResponseEntity<HearitPersonalDetailResponse> readHearit(@PathVariable Long hearitId,
                                                                   @AuthenticationPrincipal CurrentMember member) {
        HearitPersonalDetailResponse response = hearitService.getHearitPersonalDetail(hearitId, member.memberId());
        return ResponseEntity.ok(response);
    }
}
