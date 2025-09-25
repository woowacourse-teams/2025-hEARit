package com.onair.hearit.member.application;

import com.onair.hearit.member.dto.MemberInfoResponse;
import com.onair.hearit.domain.Member;
import com.onair.hearit.exception.custom.NotFoundException;
import com.onair.hearit.infrastructure.jpa.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberInfoResponse getMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("memberId", memberId.toString()));
        return MemberInfoResponse.from(member);
    }
}
