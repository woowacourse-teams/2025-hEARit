package com.onair.hearit.presentation;

import com.onair.hearit.application.MemberService;
import com.onair.hearit.dto.response.MemberInfoResponse;
import com.onair.hearit.auth.domain.RequestUser;
import com.onair.hearit.domain.UserInfo;
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
    public ResponseEntity<MemberInfoResponse> readCurrentMemberInfo(@AuthenticationPrincipal RequestUser requestUser) {
        UserInfo userInfo = requestUser.getUserInfo();
        MemberInfoResponse response = memberService.getMember(userInfo.getMemberId());
        return ResponseEntity.ok(response);
    }
}
