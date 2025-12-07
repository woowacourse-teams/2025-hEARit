package com.onair.hearit.app.userInfo.application;

import com.onair.hearit.app.exception.custom.NotFoundException;
import com.onair.hearit.core.domain.UserInfo;
import com.onair.hearit.core.infrastructure.jpa.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserInfoService {

    private final MemberRepository memberRepository;

    public String getUuid(UserInfo userInfo) {
        if (userInfo.isGuest()) {
            return userInfo.getGuestId();
        }

        return memberRepository.findUuidById(userInfo.getMemberId())
                .orElseThrow(() -> new NotFoundException("memberId", userInfo.getMemberId().toString()));
    }
}
