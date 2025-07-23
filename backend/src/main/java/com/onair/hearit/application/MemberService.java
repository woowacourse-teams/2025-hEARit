package com.onair.hearit.application;

import com.onair.hearit.common.exception.custom.NotFoundException;
import com.onair.hearit.domain.Member;
import com.onair.hearit.dto.response.MemberInfoResponse;
import com.onair.hearit.infrastructure.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberInfoResponse getMemberById(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("memberId", memberId.toString()));
        return MemberInfoResponse.from(member);
    }
}
