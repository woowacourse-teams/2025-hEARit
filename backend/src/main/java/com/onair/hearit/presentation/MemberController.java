package com.onair.hearit.presentation;

import com.onair.hearit.application.MemberService;
import com.onair.hearit.auth.dto.CurrentMember;
import com.onair.hearit.dto.response.MemberInfoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/members")
@Tag(name = "Member", description = "사용자")
public class MemberController {

    private final MemberService memberService;

    @Operation(summary = "사용자 정보 조회", description = "현재 로그인되어있는 사용자의 정보",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "401", description = "로그인하지 않은 사용자",
                            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
            })
    @GetMapping("/me")
    public ResponseEntity<MemberInfoResponse> readCurrentMemberInfo(@AuthenticationPrincipal CurrentMember member) {
        MemberInfoResponse response = memberService.getMember(member.memberId());
        return ResponseEntity.ok(response);
    }
}
