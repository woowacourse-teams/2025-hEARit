package com.onair.hearit.app.member.presentation;

import com.onair.hearit.app.member.dto.MemberInfoResponse;
import com.onair.hearit.app.member.application.MemberService;
import com.onair.hearit.app.auth.domain.RequestUser;
import com.onair.hearit.core.domain.UserInfo;
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
        MemberInfoResponse response = memberService.getMember(userInfo.getMemberUuid());
        return ResponseEntity.ok(response);
    }
}
