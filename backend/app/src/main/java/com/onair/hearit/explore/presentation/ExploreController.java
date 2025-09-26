package com.onair.hearit.explore.presentation;

import com.onair.hearit.auth.domain.RequestUser;
import com.onair.hearit.common.dto.request.CursorRequest;
import com.onair.hearit.common.dto.response.CursorResponseV1;
import com.onair.hearit.common.dto.response.CursorResponseV2;
import com.onair.hearit.explore.application.HearitExploreService;
import com.onair.hearit.explore.dto.ExploredHearitResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ExploreController {

    private final HearitExploreService hearitExploreService;

    @GetMapping("/api/v1/hearits/explore")
    public ResponseEntity<CursorResponseV1<ExploredHearitResponse>> readExploredHearitsV1(
            @AuthenticationPrincipal RequestUser requestUser,
            @RequestParam(name = "cursorId", defaultValue = "0") long cursorId,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        CursorRequest cursorRequest = new CursorRequest(cursorId, size);
        CursorResponseV2<ExploredHearitResponse> responses =
                hearitExploreService.getExploredHearits(requestUser.getUserInfo(), cursorRequest);
        CursorResponseV1<ExploredHearitResponse> responsesV1 = CursorResponseV1.from(responses);
        return ResponseEntity.ok(responsesV1);
    }

    @GetMapping("/api/v2/hearits/explore")
    public ResponseEntity<CursorResponseV2<ExploredHearitResponse>> readExploredHearitsV2(
            @AuthenticationPrincipal RequestUser requestUser,
            @RequestParam(name = "cursorId", defaultValue = "0") long cursorId,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        CursorRequest cursorRequest = new CursorRequest(cursorId, size);
        CursorResponseV2<ExploredHearitResponse> responses =
                hearitExploreService.getExploredHearits(requestUser.getUserInfo(), cursorRequest);
        return ResponseEntity.ok(responses);
    }
}
