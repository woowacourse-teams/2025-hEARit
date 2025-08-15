package com.onair.hearit.dto.response;

import com.onair.hearit.common.log.mask.Masking;
import com.onair.hearit.common.log.mask.MaskingType;
import com.onair.hearit.domain.Member;

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
