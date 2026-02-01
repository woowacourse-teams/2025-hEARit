package com.onair.hearit.app.member.application;

import com.onair.hearit.app.member.dto.MemberInfoResponse;
import com.onair.hearit.core.domain.Member;
import com.onair.hearit.app.exception.custom.NotFoundException;
import com.onair.hearit.core.infrastructure.jpa.MemberRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberInfoResponse getMember(UUID memberUuid) {
        Member member = memberRepository.findByUuid(memberUuid)
                .orElseThrow(() -> new NotFoundException("memberUuid", memberUuid.toString()));
        return MemberInfoResponse.from(member);
    }
}
