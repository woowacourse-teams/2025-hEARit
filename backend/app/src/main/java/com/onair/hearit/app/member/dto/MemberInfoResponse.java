package com.onair.hearit.app.member.dto;

import com.onair.hearit.core.log.mask.Masking;
import com.onair.hearit.core.log.mask.MaskingType;
import com.onair.hearit.core.domain.Member;

public record MemberInfoResponse(
        Long id,

        @Masking(type = MaskingType.FULL)
        String nickname,

        @Masking(type = MaskingType.FULL)
        String profileImage
) {
    public static MemberInfoResponse from(Member member) {
        return new MemberInfoResponse(member.getId(), member.getNickname(), member.getProfileImage());
    }
}
