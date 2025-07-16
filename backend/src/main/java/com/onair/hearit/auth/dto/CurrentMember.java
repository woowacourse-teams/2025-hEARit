package com.onair.hearit.auth.dto;

import com.onair.hearit.domain.MemberRole;

public record CurrentMember(
        Long memberId,
        MemberRole role
) {
}
