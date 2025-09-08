package com.onair.hearit.app.presentation;

import com.onair.hearit.app.application.MemberService;
import com.onair.hearit.auth.domain.UserContext;
import com.onair.hearit.app.dto.response.MemberInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/members")
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/me")
    public ResponseEntity<MemberInfoResponse> readCurrentMemberInfo(@AuthenticationPrincipal UserContext userContext) {
        MemberInfoResponse response = memberService.getMember(userContext.getMemberId());
        return ResponseEntity.ok(response);
    }
}
