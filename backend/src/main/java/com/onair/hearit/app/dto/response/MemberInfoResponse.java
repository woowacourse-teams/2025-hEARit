package com.onair.hearit.app.dto.response;

import com.onair.hearit.log.mask.Masking;
import com.onair.hearit.log.mask.MaskingType;
import com.onair.hearit.common.domain.Member;

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
