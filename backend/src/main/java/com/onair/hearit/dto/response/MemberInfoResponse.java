package com.onair.hearit.dto.response;

import com.onair.hearit.domain.Member;

public record MemberInfoResponse(
        Long id,
        String nickname,
        String profileImage
) {

    public static MemberInfoResponse from(Member member) {
        return new MemberInfoResponse(member.getId(), member.getNickname(), member.getProfileImage());
    }
}
