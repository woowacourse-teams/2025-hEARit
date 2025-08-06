package com.onair.hearit.presentation;

import com.onair.hearit.application.MemberService;
import com.onair.hearit.auth.dto.CurrentMember;
import com.onair.hearit.dto.response.MemberInfoResponse;
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
    public ResponseEntity<MemberInfoResponse> readCurrentMemberInfo(@AuthenticationPrincipal CurrentMember member) {
        MemberInfoResponse response = memberService.getMember(member.memberId());
        return ResponseEntity.ok(response);
    }
}
